package com.patrickg;

import java.util.Locale;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class RouletteCommand implements CommandExecutor, Listener {

    private boolean lastWasRed = false;
    private int spinCounter = 0;

    private final Plugin plugin;
    private final Random random = new Random();

    public RouletteCommand(Plugin plugin) {
        this.plugin = plugin;
        plugin.getCommand("roulette").setExecutor(this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin); // ✅ Add this
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.DARK_GREEN + "Roulette Spinning...")) {
            event.setCancelled(true);
        }
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use roulette.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 3) {
            player.sendMessage(ChatColor.RED + "Usage: /roulette bet <red|black|green> <amount>");
            return true;
        }

        String colorGuess = args[1].toLowerCase();
        int betAmount;
        try {
            betAmount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid amount.");
            return true;
        }

        if (betAmount <= 0) {
            player.sendMessage(ChatColor.RED + "Bet must be greater than 0.");
            return true;
        }

        if (plugin.getBalanceManager().getBalance(player.getUniqueId()) < betAmount) {
            player.sendMessage(ChatColor.RED + "You don't have enough money to bet.");
            return true;
        }

        if (!colorGuess.equals("red") && !colorGuess.equals("black") && !colorGuess.equals("green")) {
            player.sendMessage(ChatColor.RED + "Color must be red, black, or green.");
            return true;
        }

        plugin.getBalanceManager().subtractBalance(player.getUniqueId(), betAmount);
        plugin.getBalanceManager().saveAllBalances();

        openRouletteGUI(player, colorGuess, betAmount);
        return true;
    }

    private void openRouletteGUI(Player player, String guess, int betAmount) {
        Inventory rouletteInv = Bukkit.createInventory(null, 9, ChatColor.DARK_GREEN + "Roulette Spinning...");

        // Fill the GUI first
        for (int i = 0; i < 9; i++) {
            rouletteInv.setItem(i, createColoredBlock(Material.BLACK_STAINED_GLASS_PANE, " "));
        }

        player.openInventory(rouletteInv);

        startRouletteSpin(player, rouletteInv, guess, betAmount, 0, 50 + random.nextInt(20), 2);
    }

    private void startRouletteSpin(Player player, Inventory rouletteInv, String guess, int betAmount, int spins, int totalSpins, int currentDelay) {
        new BukkitRunnable() {
            @Override
            public void run() {
                int newSpins = spins + 1;

                // Shift left
                rouletteInv.setItem(2, rouletteInv.getItem(3));
                rouletteInv.setItem(3, rouletteInv.getItem(4));
                rouletteInv.setItem(4, rouletteInv.getItem(5));
                rouletteInv.setItem(5, rouletteInv.getItem(6));

                ItemStack colorItem;
                spinCounter++;

                // Every 10 spins, force green
                if (spinCounter % 8 == 0) {
                    colorItem = createColoredBlock(Material.GREEN_CONCRETE, "Green");
                } else {
                    if (lastWasRed) {
                        colorItem = createColoredBlock(Material.BLACK_CONCRETE, "Black");
                        lastWasRed = false;
                    } else {
                        colorItem = createColoredBlock(Material.RED_CONCRETE, "Red");
                        lastWasRed = true;
                    }
                }

                rouletteInv.setItem(6, colorItem);

                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.5f);

                int newDelay = currentDelay;
                if (newSpins > totalSpins * 0.6) newDelay = 4;
                if (newSpins > totalSpins * 0.8) newDelay = 6;
                if (newSpins > totalSpins * 0.9) newDelay = 10;

                if (newSpins >= totalSpins) {
                    // Finished spinning
                    this.cancel();

                    ItemStack finalItem = rouletteInv.getItem(4);
                    String finalColor = ChatColor.stripColor(finalItem.getItemMeta().getDisplayName()).toLowerCase(Locale.ROOT);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            int winnings = 0;
                            if (finalColor.equals(guess)) {
                                if (finalColor.equals("green")) {
                                    winnings = betAmount * 14;
                                } else {
                                    winnings = betAmount * 2;
                                }
                                plugin.getBalanceManager().addBalance(player.getUniqueId(), winnings);
                                player.sendMessage(ChatColor.GOLD + "You won $" + winnings + "!");
                                plugin.getBalanceManager().saveAllBalances();
                            } else {
                                player.sendMessage(ChatColor.RED + "You lost!");
                            }
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                            player.closeInventory();
                        }
                    }.runTaskLater(plugin, 40L);
                } else {
                    startRouletteSpin(player, rouletteInv, guess, betAmount, newSpins, totalSpins, newDelay);
                }
            }
        }.runTaskLater(plugin, currentDelay);
    }

    private ItemStack createColoredBlock(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + name);
        item.setItemMeta(meta);
        return item;
    }
}
