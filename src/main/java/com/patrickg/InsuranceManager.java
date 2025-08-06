package com.patrickg;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class InsuranceManager {

    private final Plugin plugin;
    private final Set<UUID> insuredPlayers = new HashSet<>();
    private final File insuranceFile;
    private final FileConfiguration insuranceData;

    public InsuranceManager(Plugin plugin) {
        this.plugin = plugin;
        this.insuranceFile = new File(plugin.getDataFolder(), "insurance.yml");

        if (!insuranceFile.exists()) {
            try {
                insuranceFile.getParentFile().mkdirs();
                insuranceFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.insuranceData = YamlConfiguration.loadConfiguration(insuranceFile);

        // Load insured players
        loadInsurance();
    }

    private void loadInsurance() {
        if (insuranceData.contains("insured")) {
            for (String uuidStr : insuranceData.getStringList("insured")) {
                insuredPlayers.add(UUID.fromString(uuidStr));
            }
        }
    }

    private void saveInsurance() {
        Set<String> toSave = new HashSet<>();
        for (UUID uuid : insuredPlayers) {
            toSave.add(uuid.toString());
        }
        insuranceData.set("insured", new ArrayList<>(toSave));

        try {
            insuranceData.save(insuranceFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isInsured(Player player) {
        return insuredPlayers.contains(player.getUniqueId());
    }

    public void insurePlayer(Player player) {
        insuredPlayers.add(player.getUniqueId());
        saveInsurance();
    }

    public void removeInsurance(Player player) {
        insuredPlayers.remove(player.getUniqueId());
        saveInsurance();
    }
}
