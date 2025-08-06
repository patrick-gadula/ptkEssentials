package com.patrickg;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InsuranceCommand implements CommandExecutor {

    private final Plugin plugin;
    private final int insuranceCost = 50000; // 💰 Cost for insurance ($50,000)

    public InsuranceCommand(Plugin plugin) {
        this.plugin = plugin;
        plugin.getCommand("insurance").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /insurance buy or /insurance status");
            return true;
        }

        if (args[0].equalsIgnoreCase("buy")) {
            // Buying insurance
            if (plugin.getInsuranceManager().isInsured(player)) {
                player.sendMessage(ChatColor.RED + "You already have active insurance!");
                return true;
            }

            double balance = plugin.getBalanceManager().getBalance(player.getUniqueId());
            if (balance < insuranceCost) {
                player.sendMessage(ChatColor.RED + "You do not have enough money for insurance! ($" + insuranceCost + ")");
                return true;
            }

            plugin.getBalanceManager().subtractBalance(player.getUniqueId(), insuranceCost);
            plugin.getInsuranceManager().insurePlayer(player);

            player.sendMessage(ChatColor.GREEN + "You have bought death insurance! You will keep your inventory and XP on death.");
            return true;
        }

        if (args[0].equalsIgnoreCase("status")) {
            // Checking insurance status
            if (plugin.getInsuranceManager().isInsured(player)) {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "You are currently insured!");
            } else {
                player.sendMessage(ChatColor.RED + "You are NOT insured.");
            }
            return true;
        }

        player.sendMessage(ChatColor.RED + "Usage: /insurance buy or /insurance status");
        return true;
    }
}
