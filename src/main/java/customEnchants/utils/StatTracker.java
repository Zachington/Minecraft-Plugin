package customEnchants.utils;


import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

public class StatTracker {
    private final File playerFile;
    private final File serverFile;
    private final FileConfiguration playerConfig;
    private final FileConfiguration serverConfig;

    public StatTracker(File dataFolder) {
        playerFile = new File(dataFolder, "player-stats.yml");
        serverFile = new File(dataFolder, "server-stats.yml");

        if (!playerFile.exists()) {
            try {
                playerFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!serverFile.exists()) {
            try {
                serverFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        serverConfig = YamlConfiguration.loadConfiguration(serverFile);

        resetDailyIfNeeded();
    }

    public void incrementPlayerStat(UUID uuid, String stat) {
        int total = playerConfig.getInt(uuid + "." + stat + ".total", 0) + 1;
        int daily = playerConfig.getInt(uuid + "." + stat + ".daily", 0) + 1;

        playerConfig.set(uuid + "." + stat + ".total", total);
        playerConfig.set(uuid + "." + stat + ".daily", daily);
    }

    public void incrementServerStat(String stat) {
        int total = serverConfig.getInt(stat + ".total", 0) + 1;
        int daily = serverConfig.getInt(stat + ".daily.count", 0) + 1;

        serverConfig.set(stat + ".total", total);
        serverConfig.set(stat + ".daily.count", daily);
        serverConfig.set(stat + ".daily.date", LocalDate.now().toString());
    }

    public void save() {
        try {
            playerConfig.save(playerFile);
            serverConfig.save(serverFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resetDailyIfNeeded() {
    String today = LocalDate.now().toString();

    boolean serverNeedsReset = false;
    for (String stat : serverConfig.getKeys(false)) {
        String storedDate = serverConfig.getString(stat + ".daily.date", "");
        if (!today.equals(storedDate)) {
            serverConfig.set(stat + ".daily.count", 0);
            serverConfig.set(stat + ".daily.date", today);
            serverNeedsReset = true;
        }
    }

    boolean playerNeedsReset = false;

    for (String uuid : playerConfig.getKeys(false)) {
        String storedDate = playerConfig.getString(uuid + ".daily.date", "");
        if (!today.equals(storedDate)) {
            for (String stat : playerConfig.getConfigurationSection(uuid).getKeys(false)) {
                if (!stat.equals("daily.date")) {
                    playerConfig.set(uuid + "." + stat + ".daily", 0);
                }
            }
            playerConfig.set(uuid + ".daily.date", today);
            playerNeedsReset = true;
        }
    }

    if (serverNeedsReset || playerNeedsReset) {
        save();
    }
}

    public int getPlayerStat(UUID uuid, String stat, boolean daily) {
        return playerConfig.getInt(uuid + "." + stat + (daily ? ".daily" : ".total"), 0);
    }

    public int getServerStat(String stat, boolean daily) {
        return serverConfig.getInt(stat + (daily ? ".daily.count" : ".total"), 0);
    }
}

