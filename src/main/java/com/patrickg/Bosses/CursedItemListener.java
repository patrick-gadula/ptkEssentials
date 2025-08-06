package com.patrickg.Bosses;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class CursedItemListener implements Listener {

    private final Map<UUID, BukkitRunnable> lingeringTasks = new HashMap<>();
    private final Map<UUID, Long> cursedLingering = new HashMap<>();

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack oldItem = player.getInventory().getItem(event.getPreviousSlot());
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());

        boolean switchedOffCursed = isCursedDoomBow(oldItem) && !isCursedDoomBow(newItem);
        boolean switchedToCursed = isCursedDoomBow(newItem);

        if (switchedToCursed) {
            // Now holding cursed bow → apply curse effects immediately
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(12.0);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 2)); // Slowness III
        } else if (switchedOffCursed) {
            // If an old lingering task exists, cancel it
            if (lingeringTasks.containsKey(player.getUniqueId())) {
                lingeringTasks.get(player.getUniqueId()).cancel();
            }
        
            cursedLingering.put(player.getUniqueId(), System.currentTimeMillis() + 3000);
        
            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    ItemStack currentItem = player.getInventory().getItem(player.getInventory().getHeldItemSlot());
                    if (!isCursedDoomBow(currentItem)) {
                        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
                        player.removePotionEffect(PotionEffectType.SLOW);
                        cursedLingering.remove(player.getUniqueId());
                        player.sendMessage(ChatColor.GRAY + "You feel the curse fade.");
                    } else {
                        cursedLingering.remove(player.getUniqueId());
                    }
                    lingeringTasks.remove(player.getUniqueId()); // ✅ Clean up after task ends
                }
            };
        
            task.runTaskLater(Bukkit.getPluginManager().getPlugin("ptkEssentials"), 60L);
            lingeringTasks.put(player.getUniqueId(), task); // ✅ Track the new task
        }
        
    }


    private boolean isCursedDoomBow(ItemStack item) {
        if (item == null || item.getType() != Material.BOW) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase("Cursed Doom Bow");
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        ItemStack item = player.getInventory().getItemInMainHand();

        boolean isHolding = isCursedDoomBow(item);
        boolean isLingering = cursedLingering.containsKey(player.getUniqueId()) &&
                            System.currentTimeMillis() < cursedLingering.get(player.getUniqueId());

        if (isHolding || isLingering) {
            event.setDamage(event.getDamage() * 1.10); // 10% more damage
        }
    }

    @EventHandler
    public void onAnvilUse(PrepareAnvilEvent event) {
        ItemStack first = event.getInventory().getItem(0);
        if (isCursedDoomBow(first)) {
            event.setResult(null);
            event.getView().getPlayer().sendMessage(ChatColor.RED + "The cursed item resists being modified...");
        }
    }

    @EventHandler
    public void onGrindstoneClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getInventory().getType() != InventoryType.GRINDSTONE) return;

        ItemStack clicked = event.getCurrentItem();
        if (isCursedDoomBow(clicked)) {
            event.setCancelled(true);
            ((Player) event.getWhoClicked()).sendMessage(ChatColor.RED + "This cursed item cannot be cleansed...");
        }
    }

    @EventHandler
    public void onGrindstoneDrag(InventoryDragEvent event) {
        if (event.getInventory().getType() != InventoryType.GRINDSTONE) return;

        ItemStack item = event.getOldCursor();
        if (isCursedDoomBow(item)) {
            event.setCancelled(true);
            ((Player) event.getWhoClicked()).sendMessage(ChatColor.RED + "The curse rejects purification...");
        }
    }



}
