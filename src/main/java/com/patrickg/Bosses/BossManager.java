package com.patrickg.Bosses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.patrickg.Clans.Clan;
import com.patrickg.Clans.ClanManager;

public class BossManager implements Listener {

    private final ClanManager clanManager;
    private final Random random = new Random();
    private LivingEntity currentBoss = null;
    private final Map<UUID, Double> playerDamage = new HashMap<>();
    private BossBar bossBar = null;
    private BukkitRunnable bossAbilityTask = null;



    private boolean autoSpawnEnabled = false;
    private long spawnIntervalTicks = 72000L; // 60 minutes default
    private World bossWorld = null;

    public BossManager(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    public void spawnRandomBoss(World world) {
        if (currentBoss != null && !currentBoss.isDead()) {
            Bukkit.broadcastMessage(ChatColor.RED + "A boss is already active!");
            return;
        }

        // int x = random.nextInt(1000) - 500;
        // int z = random.nextInt(1000) - 500;
        int x = -348;
        int z = -151;
        int y = world.getHighestBlockYAt(x, z) + 1;

        Location spawnLoc = new Location(world, x, y, z);

        // Cast immediately when spawning
        Skeleton skeleton = (Skeleton) world.spawnEntity(spawnLoc, EntityType.SKELETON);

        // Set name
        skeleton.setCustomName(ChatColor.DARK_RED + "Doom Archer");
        skeleton.setCustomNameVisible(true);

        skeleton.setRemoveWhenFarAway(false);

        // ✅ FIRST, set MAX HEALTH ATTRIBUTE
        skeleton.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(500.0);

        // ✅ THEN, set health
        skeleton.setHealth(500.0);

        // Now add potion effects
        skeleton.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));    // Speed III
        skeleton.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 1)); // Strength II
        skeleton.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0)); // Fire resist
        skeleton.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0));    // Regen

        // Equip bow and armor (same as before)
        ItemStack bow = new ItemStack(Material.BOW);
        bow.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 5); // Power V
        bow.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 1);   // Flame I
        skeleton.getEquipment().setItemInMainHand(bow);
        skeleton.getEquipment().setItemInMainHandDropChance(0.0f);

        skeleton.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
        skeleton.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
        skeleton.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        skeleton.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
        skeleton.getEquipment().setHelmetDropChance(0.0f);
        skeleton.getEquipment().setChestplateDropChance(0.0f);
        skeleton.getEquipment().setLeggingsDropChance(0.0f);
        skeleton.getEquipment().setBootsDropChance(0.0f);

        // ✅ Now finally track the boss
        currentBoss = skeleton;

        // ✅ Create Boss Bar
        bossBar = Bukkit.createBossBar(
            ChatColor.DARK_RED + "Doom Archer",
            BarColor.PURPLE,
            BarStyle.SEGMENTED_10
        );

        // Add all online players to see the boss bar
        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(player);
        }

        // Set progress to 100%
        bossBar.setProgress(1.0);
        bossBar.setVisible(true);

        // Start Boss Abilities
        startBossAbilities();



        playerDamage.clear();

        Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "The Doom Archer has spawned at " +
                ChatColor.AQUA + "(" + x + ", " + z + ")!");
    }


    @EventHandler
    public void onBossDamage(EntityDamageByEntityEvent event) {
        if (currentBoss == null || currentBoss.isDead()) return;
        if (!event.getEntity().getUniqueId().equals(currentBoss.getUniqueId())) return;
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        UUID uuid = player.getUniqueId();
        double damage = event.getFinalDamage();
        playerDamage.put(uuid, playerDamage.getOrDefault(uuid, 0.0) + damage);

        // ✅ Update Boss Bar Progress
        double health = currentBoss.getHealth();
        double maxHealth = currentBoss.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        double progress = Math.max(0.0, Math.min(1.0, health / maxHealth));
        bossBar.setProgress(progress);
    }

    private ItemStack createMoneyNote(int amount) {
        ItemStack note = new ItemStack(Material.PAPER);
        ItemMeta meta = note.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Money Note: $" + amount);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Right-click to redeem!");
        lore.add(ChatColor.DARK_GRAY + "Amount: " + amount);
        meta.setLore(lore);
        note.setItemMeta(meta);
        return note;
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        if (currentBoss == null || !event.getEntity().getUniqueId().equals(currentBoss.getUniqueId())) return;

        Player killer = event.getEntity().getKiller();

        if (killer == null) {
            Bukkit.broadcastMessage(ChatColor.RED + "The boss has died, but no player dealt the final blow!");
            currentBoss = null;
            playerDamage.clear();
            return;
        }

        Clan clan = clanManager.getClanByPlayer(killer);
        if (clan != null) {
            Bukkit.broadcastMessage(ChatColor.GOLD + "[Clan " + clan.getName().toUpperCase() + "] " +
                    ChatColor.GREEN + killer.getName() + " has slain the Doom Archer!");

                    for (UUID memberUUID : clan.getMembers()) {
                        Player member = Bukkit.getPlayer(memberUUID);
                        if (member != null && member.isOnline()) {
                            member.getInventory().addItem(createMoneyNote(5000)); // Give 5000 dollars note (adjust amount if you want)
                            member.sendMessage(ChatColor.GREEN + "🎁 You received a money note for your clan's boss kill!");
                        }
                    }
        } else {
            Bukkit.broadcastMessage(ChatColor.GOLD + " " + ChatColor.GREEN + killer.getName() +
                    " has slain the Doom Archer!");

            killer.getInventory().addItem(createMoneyNote(5000));
            killer.sendMessage(ChatColor.GREEN + "🎁 You received a money note for killing the boss!");
        }

        if (bossBar != null) {
            bossBar.removeAll(); // ✅ Removes it from all players
            bossBar.setVisible(false);
            bossBar = null;
        }

        ItemStack cursedBow = new ItemStack(Material.BOW);
        ItemMeta meta = cursedBow.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_RED + "Cursed Doom Bow");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Flame I, Power VII");
        lore.add(ChatColor.RED + "⚠ Wielding this bow reduces your max health!");
        meta.setLore(lore);
        meta.addEnchant(Enchantment.ARROW_DAMAGE, 7, true); // Power VII
        meta.addEnchant(Enchantment.ARROW_FIRE, 1, true);   // Flame I
        cursedBow.setItemMeta(meta);

        // Drop it
        killer.getInventory().addItem(cursedBow);

        currentBoss = null;
        playerDamage.clear();
    }


    // Called once by command to enable auto spawn
    public void enableAutoSpawn(World world, long intervalTicks) {
        if (autoSpawnEnabled) {
            Bukkit.broadcastMessage(ChatColor.RED + "Auto boss spawning is already enabled!");
            return;
        }

        autoSpawnEnabled = true;
        bossWorld = world;
        spawnIntervalTicks = intervalTicks;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (autoSpawnEnabled && bossWorld != null) {
                    spawnRandomBoss(bossWorld);
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("ptkEssentials"), intervalTicks, intervalTicks);

        Bukkit.broadcastMessage(ChatColor.GREEN + "Auto boss spawning has been ENABLED!");
    }

    public void killAllBosses() {
        if (currentBoss == null || currentBoss.isDead()) {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "No active boss to kill!");
            return;
        }
    
        currentBoss.setHealth(0.0);
    
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar.setVisible(false);
            bossBar = null;
        }
    
        if (bossAbilityTask != null) {
            bossAbilityTask.cancel();
            bossAbilityTask = null;
        }
    
        Bukkit.broadcastMessage(ChatColor.RED + "An admin has force-killed the active boss!");
    
        currentBoss = null;
        playerDamage.clear();
    }
    

    public void disableAutoSpawn() {
        autoSpawnEnabled = false;
        Bukkit.broadcastMessage(ChatColor.RED + "Auto boss spawning has been DISABLED!");
    }

    private void startBossAbilities() {
        if (bossAbilityTask != null) {
            bossAbilityTask.cancel();
        }
    
        bossAbilityTask = new BukkitRunnable() {
            int tickCounter = 0;
    
            @Override
            public void run() {
                if (currentBoss == null || currentBoss.isDead()) {
                    this.cancel();
                    return;
                }
    
                tickCounter += 20; // 1 second = 20 ticks
    
                // Every 10 seconds: Summon Minions
                if (tickCounter % 600 == 0) { // 30 seconds
                    summonMinions();
                }
    
                // Every 15 seconds: Random Teleport
                if (tickCounter % 300 == 0) { // 15 seconds
                    teleportRandomly();
                }
            }
        };
    
        bossAbilityTask.runTaskTimer(Bukkit.getPluginManager().getPlugin("ptkEssentials"), 20L, 20L); // Start after 1s, repeat every 1s
    }

    private void summonMinions() {
        if (currentBoss == null || currentBoss.isDead()) return;
    
        Location bossLocation = currentBoss.getLocation();
    
        for (int i = 0; i < 3; i++) { // spawn 3 minions
            Location spawnLoc = bossLocation.clone().add(
                    random.nextInt(6) - 3, 0, random.nextInt(6) - 3
            );
            spawnLoc.setY(bossLocation.getWorld().getHighestBlockYAt(spawnLoc) + 1);
    
            Skeleton minion = (Skeleton) bossLocation.getWorld().spawnEntity(spawnLoc, EntityType.SKELETON);
            minion.setCustomName(ChatColor.GRAY + "Minion of Doom");
            minion.setCustomNameVisible(true);
            minion.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
            minion.getEquipment().setItemInMainHand(new ItemStack(Material.BOW));
        }
    
        Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "Doom Archer has summoned minions!");
    }

    private void teleportRandomly() {
        if (currentBoss == null || currentBoss.isDead()) return;
    
        Location currentLoc = currentBoss.getLocation();
        int xOffset = random.nextInt(10) - 5;
        int zOffset = random.nextInt(10) - 5;
    
        Location newLoc = currentLoc.clone().add(xOffset, 0, zOffset);
        newLoc.setY(currentBoss.getWorld().getHighestBlockYAt(newLoc) + 1);
    
        currentBoss.teleport(newLoc);
    
        Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "⚡ Doom Archer teleports through the shadows!");
    }
    
}
