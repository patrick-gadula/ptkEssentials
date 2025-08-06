package com.patrickg.Clans;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ClanStorage {

    private final File file;
    private FileConfiguration config;

    public ClanStorage(File dataFolder) {
        this.file = new File(dataFolder, "clans.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveClans(Map<String, Clan> clans) {
        // Clear the config first
        for (String key : config.getKeys(false)) {
            config.set(key, null);
        }
    
        // Now save current clans
        for (Map.Entry<String, Clan> entry : clans.entrySet()) {
            String name = entry.getKey();
            Clan clan = entry.getValue();
            config.set(name + ".owner", clan.getOwner().toString());
            List<String> memberList = new ArrayList<>();
            for (UUID uuid : clan.getMembers()) {
                memberList.add(uuid.toString());
            }
            config.set(name + ".members", memberList);
    
            if (clan.getHome() != null) {
                Location loc = clan.getHome();
                config.set(name + ".home.world", loc.getWorld().getName());
                config.set(name + ".home.x", loc.getX());
                config.set(name + ".home.y", loc.getY());
                config.set(name + ".home.z", loc.getZ());
                config.set(name + ".home.yaw", loc.getYaw());
                config.set(name + ".home.pitch", loc.getPitch());
            }
        }
    
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    public Map<String, Clan> loadClans() {
        Map<String, Clan> clans = new HashMap<>();
        for (String name : config.getKeys(false)) {
            UUID owner = UUID.fromString(config.getString(name + ".owner"));
            Clan clan = new Clan(name, owner);

            List<String> members = config.getStringList(name + ".members");
            for (String memberUUID : members) {
                clan.getMembers().add(UUID.fromString(memberUUID));
            }

            if (config.contains(name + ".home")) {
                Location loc = new Location(
                        Bukkit.getWorld(config.getString(name + ".home.world")),
                        config.getDouble(name + ".home.x"),
                        config.getDouble(name + ".home.y"),
                        config.getDouble(name + ".home.z"),
                        (float) config.getDouble(name + ".home.yaw"),
                        (float) config.getDouble(name + ".home.pitch")
                );
                clan.setHome(loc);
            }

            clans.put(name, clan);
        }
        return clans;
    }
}
