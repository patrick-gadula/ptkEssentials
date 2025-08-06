package com.patrickg;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;

public class BalanceManager {
    private final HashMap<UUID, Double> balances = new HashMap<>();
    private final Plugin plugin;

    public BalanceManager(Plugin plugin) {
        this.plugin = plugin;
        loadBalancesFromConfig();
    }

    public Map<UUID, Double> getBalances() {
        return balances;
    }   

    public double getBalance(UUID uuid) {
        return balances.getOrDefault(uuid, 0.0);
    }

    public void setBalance(UUID uuid, double amount) {
        balances.put(uuid, amount);
    }

    public void addBalance(UUID uuid, double amount) {
        double current = getBalance(uuid);
        balances.put(uuid, current + amount);
    }

    public void subtractBalance(UUID uuid, double amount) {
        double current = getBalance(uuid);
        balances.put(uuid, current - amount);
    }

    public void loadBalancesFromConfig() {
        FileConfiguration config = plugin.balanceData;

        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                double bal = config.getDouble(key);
                balances.put(uuid, bal);
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Invalid UUID in balances.yml: " + key);
            }
        }
    }

    public void saveAllBalances() {
        FileConfiguration config = plugin.balanceData;
        for (UUID uuid : balances.keySet()) {
            config.set(uuid.toString(), balances.get(uuid));
        }
        try {
            config.save(new File(plugin.getDataFolder(), "balances.yml"));
        } catch (Exception e) {
            plugin.getLogger().severe("Could not save balances.yml!");
            e.printStackTrace();
        }
    }
}
