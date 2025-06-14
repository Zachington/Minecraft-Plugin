package customEnchants.listeners;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
//import java.util.logging.LOGGER;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import customEnchants.TestEnchants;
import customEnchants.utils.GuiUtil;
import customEnchants.utils.crateTableUtil;
import customEnchants.utils.customItemUtil;
import customEnchants.utils.crateTableUtil.LootEntry;

public class CrateListener implements Listener {


    private final World world;
    private final int xMin, xMax, yMin, yMax, zMin, zMax;
    private final Map<Player, String> guiOpenMethod = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private final Map<Player, Long> shiftRightCooldown = new HashMap<>();

    public CrateListener(World world, int xMin, int xMax, int yMin, int yMax, int zMin, int zMax) {
        this.world = world;
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.zMin = zMin;
        this.zMax = zMax;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Location loc = event.getClickedBlock().getLocation();

        if (!loc.getWorld().equals(world)) return;
        if (loc.getBlockX() < xMin || loc.getBlockX() > xMax) return;
        if (loc.getBlockY() < yMin || loc.getBlockY() > yMax) return;
        if (loc.getBlockZ() < zMin || loc.getBlockZ() > zMax) return;

        Player player = event.getPlayer();
        Material clickedType = event.getClickedBlock().getType();
        boolean isSneaking = player.isSneaking();

        
        switch (event.getAction()) {
            case LEFT_CLICK_BLOCK:
                if (handleLeftClick(player, clickedType)) {
                    event.setCancelled(true);
                }
                break;

            case RIGHT_CLICK_BLOCK:
                if (isSneaking) {
                    long now = System.currentTimeMillis();
                    Long lastUse = shiftRightCooldown.getOrDefault(player, 0L);
                    if (now - lastUse < 300) return;
                    shiftRightCooldown.put(player, now);
                    if (handleShiftRightClick(player, clickedType, loc)) {
                        event.setCancelled(true);
                    }
            } else {
                if (handleRightClick(player, clickedType, loc)) {
                    event.setCancelled(true);
                }
            }
                break;
            default:
                break;
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player)) return;

    InventoryView view = event.getView();
    String title = view.getTitle();

    if (title.equals("§5Enchanter Crate") 
    || title.equals("§3Mining Crate") 
    || title.equals("§1Prison Crate") 
    || title.equals("§cDivine Crate") 
    || title.equals("§8Durability Crate")) {

        int rawSlot = event.getRawSlot();
        int topSize = view.getTopInventory().getSize();

        // Cancel any click inside the GUI slots
        if (rawSlot < topSize) {
            event.setCancelled(true);
            return;
        }

        // Also cancel shift-clicks that try to put items into the GUI
        if (event.isShiftClick()) {
            ItemStack cursorItem = event.getCurrentItem();
            if (cursorItem != null) {
                // Prevent shift-click if it would place items into the GUI
                event.setCancelled(true);
            }
        }
    }
}

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
    InventoryView view = event.getView();
    Player player = (Player) event.getPlayer();

    String title = view.getTitle();

    if (title.equals("§5Enchanter Crate")) {
        String method = guiOpenMethod.remove(player);
        if (!"RIGHT_CLICK".equals(method)) return;
        handleLootDistribution(player, view.getTopInventory(), "Enchant Key");
    }
    else if (title.equals("§3Mining Crate")) {
        String method = guiOpenMethod.remove(player);
        if (!"RIGHT_CLICK".equals(method)) return;
        handleLootDistribution(player, view.getTopInventory(), "Mining Key");
    }
    else if (title.equals("§8Durability Crate")) {
        String method = guiOpenMethod.remove(player);
        if (!"RIGHT_CLICK".equals(method)) return;
        handleLootDistribution(player, view.getTopInventory(), "Durability Key");
    }
    else if (title.equals("§cDivine Crate")) {
        String method = guiOpenMethod.remove(player);
        if (!"RIGHT_CLICK".equals(method)) return;
        handleLootDistribution(player, view.getTopInventory(), "Divine Key");
    }
    else if (title.equals("§1Prison Crate")) {
        String method = guiOpenMethod.remove(player);
        if (!"RIGHT_CLICK".equals(method)) return;
        handleLootDistribution(player, view.getTopInventory(), "Prison Key");
    }
}

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
    String title = event.getView().getTitle();
    String strippedTitle = ChatColor.stripColor(title);

    if (strippedTitle.equals("Mining Crate") 
            || strippedTitle.equals("Enchanter Crate")) {
        int guiSize = event.getView().getTopInventory().getSize();
        for (int slot : event.getRawSlots()) {
            if (slot < guiSize) {
                event.setCancelled(true);
                break;
            }
        }
    }
}

    private void handleLootDistribution(Player player, Inventory topInv, String lootTableName) {
    ItemStack chosenItem = getRandomLoot(lootTableName);
    if (chosenItem == null) {
        player.sendMessage("No loot available.");
        return;
    }

    if (chosenItem.hasItemMeta()) {
        ItemMeta meta = chosenItem.getItemMeta();
        if (meta.hasLore()) {
            List<String> lore = new ArrayList<>(meta.getLore());
            int randomChance = 1 + new Random().nextInt(100);
            for (int i = 0; i < lore.size(); i++) {
                String line = ChatColor.stripColor(lore.get(i));
                if (line.startsWith("Success Rate:")) {
                    lore.set(i, ChatColor.GREEN + "Success Rate: " + randomChance + "%");
                    break;
                }
            }
            meta.setLore(lore);
            chosenItem.setItemMeta(meta);
        }
    }

    int foundSlot = -1;
    ItemMeta chosenMeta = chosenItem.hasItemMeta() ? chosenItem.getItemMeta() : null;
    String chosenName = (chosenMeta != null && chosenMeta.hasDisplayName())
        ? ChatColor.stripColor(chosenMeta.getDisplayName())
        : null;

    for (int i = 0; i < topInv.getSize(); i++) {
    ItemStack item = topInv.getItem(i);
    if (item == null) continue;

    // Match by display name if available
    if (chosenItem.hasItemMeta() && chosenItem.getItemMeta().hasDisplayName()) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) continue;

        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        if (chosenName != null && displayName.equalsIgnoreCase(chosenName)) {
            foundSlot = i;
            break;
        }
    } else {
        // Fallback to matching by Material and amount
        if (item.getType() == chosenItem.getType() && item.getAmount() == chosenItem.getAmount()) {
            foundSlot = i;
            break;
        }
    }
}


    if (foundSlot == -1) {
        return;
    }

    final int chosenSlot = foundSlot;
    List<Integer> slotsToChange = new ArrayList<>();
    for (int i = 0; i < topInv.getSize(); i++) {
        if (i != chosenSlot) {
            ItemStack item = topInv.getItem(i);
            if (item != null && item.getType() == Material.GRAY_STAINED_GLASS_PANE) continue;
            slotsToChange.add(i);
        }
    }
    Collections.shuffle(slotsToChange);

    new BukkitRunnable() {
        int index = 0;

        @Override
    public void run() {
        if (index >= slotsToChange.size()) {
            this.cancel();

            // Clean "Success Rate" lore line before giving
            if (chosenItem.hasItemMeta()) {
                ItemMeta meta = chosenItem.getItemMeta();
                if (meta.hasLore()) {
                    List<String> lore = new ArrayList<>(meta.getLore());
                    lore.removeIf(line -> ChatColor.stripColor(line).startsWith("Chance"));
                    meta.setLore(lore);
                    chosenItem.setItemMeta(meta);
                }
            }

            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(chosenItem.clone());
            if (!leftover.isEmpty()) {
                player.getWorld().dropItemNaturally(player.getLocation(), chosenItem.clone());
            }

            topInv.setItem(chosenSlot, null);
            ItemMeta meta = chosenItem.getItemMeta();
            String itemName;

            if (meta != null && meta.hasDisplayName()) {
                itemName = ChatColor.stripColor(meta.getDisplayName());
            } else {
                itemName = chosenItem.getType().name().replace('_', ' ').toLowerCase();
                itemName = itemName.substring(0, 1).toUpperCase() + itemName.substring(1);
            }
            player.sendMessage("§aYou received: " + itemName);
            player.closeInventory();
            return;
        }
        int slot = slotsToChange.get(index);
        topInv.setItem(slot, GuiUtil.createPane(Material.GRAY_STAINED_GLASS_PANE));
        index++;
    }
}.runTaskTimer(TestEnchants.getInstance(), 10L, 2L);
}

    public static ItemStack getRandomLoot(String lootTableName) {
        List<LootEntry> lootEntries = crateTableUtil.LOOT_TABLES.get(lootTableName);
        if (lootEntries == null || lootEntries.isEmpty()) return null;

        double totalWeight = lootEntries.stream().mapToDouble(e -> e.chance).sum();
        double randomValue = Math.random() * totalWeight;

        double currentWeight = 0;
        for (LootEntry entry : lootEntries) {
            currentWeight += entry.chance;
            if (randomValue <= currentWeight) {
                return entry.item.clone();
            }
        }
        return null;
    }

    private boolean handleLeftClick(Player player, Material clickedType) {
    if (clickedType == Material.ENCHANTING_TABLE) {
        player.openInventory(GuiUtil.enchantKeyInventory(player));
        guiOpenMethod.put(player, "LEFT_CLICK");
        return true;
    } else if (clickedType == Material.DIAMOND_BLOCK) {
        player.openInventory(GuiUtil.divineKeyInventory(player));
        guiOpenMethod.put(player, "LEFT_CLICK");
        return true;
    } else if (clickedType == Material.BEDROCK) {
        player.openInventory(GuiUtil.durabilityKeyInventory(player));
        guiOpenMethod.put(player, "LEFT_CLICK");
        return true;
    } else if (clickedType == Material.CREAKING_HEART) {
        player.openInventory(GuiUtil.miningKeyInventory(player));
        guiOpenMethod.put(player, "LEFT_CLICK");
        return true;
    } else if (clickedType == Material.CRYING_OBSIDIAN) {
        player.openInventory(GuiUtil.prisonKeyInventory(player));
        guiOpenMethod.put(player, "LEFT_CLICK");
        return true;
    }
    return false;
}

    private boolean handleRightClick(Player player, Material clickedType, Location clickedBlockLoc) {
    ItemStack handItem = player.getInventory().getItemInMainHand();

    if (clickedType == Material.ENCHANTING_TABLE) {
        if (isValidCustomItem(handItem)) {
            String plainName = stripColorCodes(handItem.getItemMeta().getDisplayName());
            if (plainName.equalsIgnoreCase("Enchant Key")) {
                if (handItem.getAmount() <= 1) {
                    player.getInventory().setItemInMainHand(null);
                } else {
                    handItem.setAmount(handItem.getAmount() - 1);
                    player.getInventory().setItemInMainHand(handItem);
                }

                Inventory gui = GuiUtil.enchantKeyInventory(player);
                guiOpenMethod.put(player, "RIGHT_CLICK");
                player.openInventory(gui);
                return true;
            } else {
                player.sendMessage("You must hold an Enchant Key to open this.");
                pushPlayerBack(player, clickedBlockLoc);
                return false;
            }
        } else {
            player.sendMessage("You must hold an Enchant Key to open this.");
            pushPlayerBack(player, clickedBlockLoc);
            return false;
        }
    } else if (clickedType == Material.DIAMOND_BLOCK) {
        if (isValidCustomItem(handItem)) {
            String plainName = stripColorCodes(handItem.getItemMeta().getDisplayName());
            if (plainName.equalsIgnoreCase("Divine Key")) {
                if (handItem.getAmount() <= 1) {
                    player.getInventory().setItemInMainHand(null);
                } else {
                    handItem.setAmount(handItem.getAmount() - 1);
                    player.getInventory().setItemInMainHand(handItem);
                }

                Inventory gui = GuiUtil.divineKeyInventory(player);
                guiOpenMethod.put(player, "RIGHT_CLICK");
                player.openInventory(gui);
                return true;
            } else {
                player.sendMessage("You must hold a Divine Key to open this.");
                pushPlayerBack(player, clickedBlockLoc);
                return false;
            }
        } else {
            player.sendMessage("You must hold a Divine Key to open this.");
            pushPlayerBack(player, clickedBlockLoc);
            return false;
        }
    } else if (clickedType == Material.BEDROCK) {
        if (isValidCustomItem(handItem)) {
            String plainName = stripColorCodes(handItem.getItemMeta().getDisplayName());
            if (plainName.equalsIgnoreCase("Durability Key")) {
                if (handItem.getAmount() <= 1) {
                    player.getInventory().setItemInMainHand(null);
                } else {
                    handItem.setAmount(handItem.getAmount() - 1);
                    player.getInventory().setItemInMainHand(handItem);
                }

                Inventory gui = GuiUtil.durabilityKeyInventory(player);
                guiOpenMethod.put(player, "RIGHT_CLICK");
                player.openInventory(gui);
                return true;
            } else {
                player.sendMessage("You must hold a Durability Key to open this.");
                pushPlayerBack(player, clickedBlockLoc);
                return false;
            }
        } else {
            player.sendMessage("You must hold a Durability Key to open this.");
            pushPlayerBack(player, clickedBlockLoc);
            return false;
        }
    } else if (clickedType == Material.CREAKING_HEART) {
        if (isValidCustomItem(handItem)) {
            String plainName = stripColorCodes(handItem.getItemMeta().getDisplayName());
            if (plainName.equalsIgnoreCase("Mining Key")) {
                if (handItem.getAmount() <= 1) {
                    player.getInventory().setItemInMainHand(null);
                } else {
                    handItem.setAmount(handItem.getAmount() - 1);
                    player.getInventory().setItemInMainHand(handItem);
                }

                Inventory gui = GuiUtil.miningKeyInventory(player);
                guiOpenMethod.put(player, "RIGHT_CLICK");
                player.openInventory(gui);
                return true;
            } else {
                player.sendMessage("You must hold a Mining Key to open this.");
                pushPlayerBack(player, clickedBlockLoc);
                return false;
            }
        } else {
            player.sendMessage("You must hold a Mining Key to open this.");
            pushPlayerBack(player, clickedBlockLoc);
            return false;
        }
    } else if (clickedType == Material.CRYING_OBSIDIAN) {
        if (isValidCustomItem(handItem)) {
            String plainName = stripColorCodes(handItem.getItemMeta().getDisplayName());
            if (plainName.equalsIgnoreCase("Prison Key")) {
                if (handItem.getAmount() <= 1) {
                    player.getInventory().setItemInMainHand(null);
                } else {
                    handItem.setAmount(handItem.getAmount() - 1);
                    player.getInventory().setItemInMainHand(handItem);
                }

                Inventory gui = GuiUtil.prisonKeyInventory(player);
                guiOpenMethod.put(player, "RIGHT_CLICK");
                player.openInventory(gui);
                return true;
            } else {
                player.sendMessage("You must hold a Prison Key to open this.");
                pushPlayerBack(player, clickedBlockLoc);
                return false;
            }
        } else {
            player.sendMessage("You must hold a Prison Key to open this.");
            pushPlayerBack(player, clickedBlockLoc);
            return false;
        }
    }

    // If no matching material, return false
    return false;
}

    private boolean handleShiftRightClick(Player player, Material clickedType, Location clickedBlockLoc) {
    ItemStack handItem = player.getInventory().getItemInMainHand();

    if (clickedType != Material.ENCHANTING_TABLE &&
        clickedType != Material.DIAMOND_BLOCK &&
        clickedType != Material.BEDROCK &&
        clickedType != Material.CREAKING_HEART &&
        clickedType != Material.CRYING_OBSIDIAN) {
        return false;
    }

    if (!isValidCustomItem(handItem)) {
        player.sendMessage("You must hold a valid crate key to open this.");
        pushPlayerBack(player, clickedBlockLoc);
        return false;
    }

    String plainName = stripColorCodes(handItem.getItemMeta().getDisplayName());
    String crateType = null;

    if (clickedType == Material.ENCHANTING_TABLE && plainName.equalsIgnoreCase("Enchant Key")) {
        crateType = "Enchant Key";
    } else if (clickedType == Material.DIAMOND_BLOCK && plainName.equalsIgnoreCase("Divine Key")) {
        crateType = "Divine Key";
    } else if (clickedType == Material.BEDROCK && plainName.equalsIgnoreCase("Durability Key")) {
        crateType = "Durability Key";
    } else if (clickedType == Material.CREAKING_HEART && plainName.equalsIgnoreCase("Mining Key")) {
        crateType = "Mining Key";
    } else if (clickedType == Material.CRYING_OBSIDIAN && plainName.equalsIgnoreCase("Prison Key")) {
        crateType = "Prison Key";
    }

    if (crateType == null) {
        player.sendMessage("You must hold the correct key to open this crate.");
        pushPlayerBack(player, clickedBlockLoc);
        return false;
    }

    int amount = Math.min(handItem.getAmount(), 32);
    handItem.setAmount(handItem.getAmount() - amount);
    if (handItem.getAmount() <= 0) {
        player.getInventory().setItemInMainHand(null);
    } else {
        player.getInventory().setItemInMainHand(handItem);
    }

    for (int i = 0; i < amount; i++) {
        ItemStack loot = getRandomLoot(crateType);
        if (loot == null) {
            player.sendMessage("No loot available.");
            break;
        }

        if (loot.hasItemMeta()) {
            ItemMeta meta = loot.getItemMeta();
            if (meta.hasLore()) {
                List<String> lore = new ArrayList<>(meta.getLore());
                int randomChance = 1 + random.nextInt(100);
                for (int j = 0; j < lore.size(); j++) {
                    String line = ChatColor.stripColor(lore.get(j));
                    if (line.startsWith("Success Rate:")) {
                        lore.set(j, ChatColor.GREEN + "Success Rate: " + randomChance + "%");
                        break;
                    }
                }
                meta.setLore(lore);
                loot.setItemMeta(meta);
            }
        }

        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(loot);
        if (!leftover.isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), loot);
        }
    }

    player.sendMessage("You received " + amount + " item" + (amount > 1 ? "s" : "") + " from the " + crateType + " crate.");
    guiOpenMethod.remove(player);
    return true;
}

    private void pushPlayerBack(Player player, Location blockLocation) {
        Location playerLoc = player.getLocation();
        double dx = playerLoc.getX() - blockLocation.getX();
        double dy = playerLoc.getY() - blockLocation.getY();
        double dz = playerLoc.getZ() - blockLocation.getZ();
        Vector pushVector = new Vector(dx, dy, dz).normalize().multiply(1);
        Vector currentVelocity = player.getVelocity();
        player.setVelocity(new Vector(pushVector.getX(), currentVelocity.getY(), pushVector.getZ()));
    }

    private String stripColorCodes(String input) {
        if (input == null) return null;
        return ChatColor.stripColor(input);
    }

    private boolean isValidCustomItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        String name = item.getItemMeta().getDisplayName();
        if (name == null) return false;

        String plainName = stripColorCodes(name);
        for (int i = 0; i < customItemUtil.CUSTOM_ITEM.length; i++) {
            if (plainName.equalsIgnoreCase(stripColorCodes(customItemUtil.CUSTOM_ITEM[i]))) {
                return item.getType() == customItemUtil.CUSTOM_ITEM_MATERIAL[i];
            }
        }
        return false;
    }
}