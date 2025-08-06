package com.patrickg;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class SupplyDropManager implements CommandExecutor {

    private final Plugin plugin;
    private final Random random = new Random();
    private boolean supplyDropEnabled = false;


    public SupplyDropManager(Plugin plugin) {
        this.plugin = plugin;
        plugin.getCommand("supplydrop").setExecutor(this);


        // Auto-scheduler every 30 minutes
        new BukkitRunnable() {
            @Override
            public void run() {
                spawnSupplyDrop();
            }
        }.runTaskTimer(plugin, 0L, 60 * 60 * 20); // every 30 minutes (in ticks)
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (!sender.hasPermission("ptk.supplydrop")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /supplydrop enable|disable|spawn");
            return true;
        }

        if (args[0].equalsIgnoreCase("enable")) {
            supplyDropEnabled = true;
            sender.sendMessage(ChatColor.GREEN + "Supply drops are now ENABLED!");
            return true;
        }

        if (args[0].equalsIgnoreCase("disable")) {
            supplyDropEnabled = false;
            sender.sendMessage(ChatColor.RED + "Supply drops are now DISABLED.");
            return true;
        }

        if (args[0].equalsIgnoreCase("spawn")) {
            if (!supplyDropEnabled) {
                sender.sendMessage(ChatColor.RED + "Supply drops are currently DISABLED!");
                return true;
            }
            spawnSupplyDrop();
            sender.sendMessage(ChatColor.GREEN + "Supply drop spawned manually!");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Usage: /supplydrop enable|disable|spawn");
        return true;
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

    public void spawnSupplyDrop() {
        if (!supplyDropEnabled) {
            return; // Do nothing if disabled
        }

        World world = Bukkit.getWorlds().get(0); // Overworld
        int x = random.nextInt(500) - 500; // -0 to +1000
        int z = random.nextInt(500) - 500; // -0 to +1000
        int y = world.getHighestBlockYAt(x, z) + 1;

        Location loc = new Location(world, x, y, z);

        Block block = loc.getBlock();
        block.setType(Material.CHEST);

        if (block.getState() instanceof Chest chest) {
            Inventory inv = chest.getInventory();

            // Common Loot
            inv.addItem(new ItemStack(Material.DIAMOND, random.nextInt(6) + 2)); // 5-10 diamonds
            inv.addItem(new ItemStack(Material.EMERALD, random.nextInt(11) + 10)); // 10-20 emeralds
            inv.addItem(new ItemStack(Material.EXPERIENCE_BOTTLE, 32));
            inv.addItem(new ItemStack(Material.GOLD_BLOCK, random.nextInt(2) + 0));


            
            if (random.nextInt(100) < 25) {
                inv.addItem(createMoneyNote(1000)); // $1,000 note
            }
            // Rare Loot (5% chance each)
            if (random.nextInt(100) < 5) {
                inv.addItem(new ItemStack(Material.TOTEM_OF_UNDYING, 1));
            }
            if (random.nextInt(100) < 5) {
                inv.addItem(createMoneyNote(10000)); // $10,000 note
            }
            if (random.nextInt(100) < 1) {
                inv.addItem(createMoneyNote(50000)); // $50,000 note
            }
        }

        // Broadcast
        Bukkit.broadcastMessage(ChatColor.GOLD + "⚡ A Supply Drop has landed at "
                + ChatColor.AQUA + "X: " + x + " Y: " + y + " Z: " + z + ChatColor.GOLD + "!");
        Bukkit.broadcastMessage(ChatColor.GRAY + "First to find it gets the loot!");

        // Schedule removal if not found
        new BukkitRunnable() {
            @Override
            public void run() {
                if (block.getType() == Material.CHEST) {
                    block.setType(Material.AIR);
                    Bukkit.broadcastMessage(ChatColor.RED + "⚡ The supply drop at X: " + x + " Z: " + z + " disappeared!");
                }
            }
        }.runTaskLater(plugin, 15 * 60 * 20); // 15 minutes later
    }
}
