package com.patrickg;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class ChallengeManager {

    private static final List<String> EPIC_TARGETS = Arrays.asList(
    "ENDERMAN",
    "DIAMOND_ORE",
    "WITHER",
    "NETHERITE_BLOCK",
    "ANCIENT_DEBRIS"
    );


    private final Plugin plugin;
    private final File challengeFile;
    private final FileConfiguration challengeData;
    private final Random random = new Random();

    public ChallengeManager(Plugin plugin) {
        this.plugin = plugin;
        this.challengeFile = new File(plugin.getDataFolder(), "challenges.yml");

        if (!challengeFile.exists()) {
            try {
                challengeFile.getParentFile().mkdirs();
                challengeFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.challengeData = YamlConfiguration.loadConfiguration(challengeFile);
        checkForDailyReset();
    }

    public void saveChallenges() {
        try {
            challengeData.save(challengeFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setupPlayerChallenges(Player player) {
        String uuid = player.getUniqueId().toString();
        if (!challengeData.contains(uuid)) {
            List<Map<String, Object>> challenges = new ArrayList<>();
    
            challenges.add(generateChallengeOfType("MINING"));
            challenges.add(generateChallengeOfType("KILLING"));
            challenges.add(generateChallengeOfType("FARMING"));
    
            challengeData.set(uuid, challenges);
            saveChallenges();
        }
    }

    private void checkForDailyReset() {
        String today = java.time.LocalDate.now().toString(); // "YYYY-MM-DD"

        String lastReset = challengeData.getString("last_reset");

        if (!today.equals(lastReset)) {
            challengeData.set("last_reset", today);
            resetAllChallenges();
            saveChallenges();
            Bukkit.getLogger().info("[Challenges] Daily challenges have been reset.");
        }
    }
    
    private void resetAllChallenges() {
        for (String key : challengeData.getKeys(false)) {
            if (key.equals("last_reset")) continue;
    
            List<Map<String, Object>> newChallenges = new ArrayList<>();
            newChallenges.add(generateChallengeOfType("MINING"));
            newChallenges.add(generateChallengeOfType("KILLING"));
            newChallenges.add(generateChallengeOfType("FARMING"));
    
            challengeData.set(key, newChallenges);
        }
    }

    public List<Map<String, Object>> getPlayerChallenges(Player player) {
        String uuid = player.getUniqueId().toString();
        List<Map<String, Object>> list = new ArrayList<>();

        if (challengeData.contains(uuid)) {
            List<?> rawList = challengeData.getList(uuid);
            for (Object obj : rawList) {
                if (obj instanceof Map) {
                    list.add((Map<String, Object>) obj);
                }
            }
        }

        return list;
    }

    public void updateChallengeProgress(Player player, String type, String targetName) {
        String uuid = player.getUniqueId().toString();
        if (!challengeData.contains(uuid)) return;

        List<Map<String, Object>> challenges = getPlayerChallenges(player);
        boolean changed = false;

        for (Map<String, Object> challenge : challenges) {
            String challengeType = (String) challenge.get("type");
            String target = (String) challenge.get("target");

            if (challengeType.equalsIgnoreCase(type) && target.contains(targetName)) {
                if (!(boolean) challenge.get("completed")) {
                    int progress = (int) challenge.get("progress");
                    int required = (int) challenge.get("amount");

                    progress++;
                    challenge.put("progress", progress);
                    // System.out.println("[Challenge DEBUG] " + player.getName() + " progressed challenge: " 
                    // + challengeType + " -> " + target + " (" + progress + "/" + required + ")");

                    if (progress >= required) {
                        challenge.put("completed", true);

                        if (EPIC_TARGETS.contains(target.toUpperCase())) {
                            // 🎯 EPIC Challenge
                            player.sendTitle(
                                ChatColor.LIGHT_PURPLE + "EPIC Challenge Completed!",
                                ChatColor.DARK_PURPLE + "[" + challengeType + "] " + target,
                                10, 60, 10
                            );
                            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
                        } else {
                            // 🟡 Normal Challenge
                            player.sendTitle(
                                ChatColor.GOLD + "Challenge Completed!",
                                ChatColor.YELLOW + "[" + challengeType + "] " + target,
                                10, 60, 10
                            );
                            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                        }
                        

                    }

                    changed = true;
                }
            }
        }

        if (changed) {
            challengeData.set(uuid, challenges);
            saveChallenges();
        }
    }

    public boolean claimChallenge(Player player, int challengeIndex) {
        String uuid = player.getUniqueId().toString();
        if (!challengeData.contains(uuid)) return false;

        List<Map<String, Object>> challenges = getPlayerChallenges(player);

        if (challengeIndex < 0 || challengeIndex >= challenges.size()) return false;

        Map<String, Object> challenge = challenges.get(challengeIndex);
        if ((boolean) challenge.get("completed") && !(boolean) challenge.getOrDefault("claimed", false)) {
            int reward = (int) challenge.get("reward");

            plugin.getBalanceManager().addBalance(player.getUniqueId(), reward);
            challenge.put("claimed", true);

            challengeData.set(uuid, challenges);
            saveChallenges();
            return true;
        }

        return false;
    }

    private Map<String, Object> generateChallengeOfType(String type) {
        Map<String, Object> challenge = new HashMap<>();
        challenge.put("type", type);

        if (type.equals("MINING")) {
            Material[] miningMaterials = {Material.COAL_ORE, Material.IRON_ORE, Material.DIAMOND_ORE, Material.GOLD_ORE};
            Material target = miningMaterials[random.nextInt(miningMaterials.length)];
            if(target == Material.DIAMOND_ORE){
                challenge.put("target", target.name());
                challenge.put("amount", random.nextInt(10) + 10); // 10-20
                challenge.put("reward", random.nextInt(500) + 1500); // $1500-$2000
            } else {
                challenge.put("target", target.name());
                challenge.put("amount", random.nextInt(30) + 20); // 20-50
                challenge.put("reward", random.nextInt(200) + 300); // $300-$500
            }
        } else if (type.equals("KILLING")) {
            EntityType[] mobs = {EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, EntityType.ENDERMAN};
            EntityType target = mobs[random.nextInt(mobs.length)];
            if(target == EntityType.ENDERMAN){
                challenge.put("target", target.name());
                challenge.put("amount", random.nextInt(10) + 15); // 15-25 kills
                challenge.put("reward", random.nextInt(500) + 1500); // $1500-2000
            } else {
                challenge.put("target", target.name());
                challenge.put("amount", random.nextInt(10) + 5); // 5-15 kills
                challenge.put("reward", random.nextInt(200) + 300); // $300-$500
            }
        } else if (type.equals("FARMING")) {
            Material[] crops = {Material.WHEAT, Material.CARROTS, Material.POTATOES};
            Material target = crops[random.nextInt(crops.length)];
            challenge.put("target", target.name());
            challenge.put("amount", random.nextInt(30) + 20); // 20-50 crops
            challenge.put("reward", random.nextInt(150) + 200); // $200-$350
        }

        challenge.put("progress", 0);
        challenge.put("completed", false);
        challenge.put("claimed", false);

        return challenge;
    }
}
