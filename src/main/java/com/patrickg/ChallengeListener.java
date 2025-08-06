package com.patrickg;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class ChallengeListener implements Listener {

    private final Plugin plugin;

    public ChallengeListener(Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // System.out.println("[DEBUG] Block broken: " + event.getBlock().getType().name()); // ADD
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material blockType = block.getType();

        if (plugin.getChallengeManager() != null) {
            // Mining challenges (STONE, ORE, DEEPSLATE, etc.)
            plugin.getChallengeManager().updateChallengeProgress(player, "MINING", blockType.name());

            // Farming challenges (WHEAT, CARROTS, etc.)
            if (isCrop(blockType)) {
                plugin.getChallengeManager().updateChallengeProgress(player, "FARMING", blockType.name());
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof org.bukkit.entity.LivingEntity)) return;
        org.bukkit.entity.LivingEntity living = (org.bukkit.entity.LivingEntity) event.getEntity();
        //System.out.println("[DEBUG] Entity killed: " + living.getType().name()); // ADD


        if (living.getKiller() == null) return;
        Player killer = living.getKiller();
        EntityType type = living.getType();

        if (plugin.getChallengeManager() != null) {
            plugin.getChallengeManager().updateChallengeProgress(killer, "KILLING", type.name());
        }
    }

    private boolean isCrop(Material material) {
        switch (material) {
            case WHEAT:
            case CARROTS:
            case POTATOES:
                return true;
            default:
                return false;
        }
    }
}
