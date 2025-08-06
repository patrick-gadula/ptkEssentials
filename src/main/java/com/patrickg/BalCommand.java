package com.patrickg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalCommand implements CommandExecutor {

    private final Plugin plugin;

    public BalCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        // Balance Command
        if (label.equalsIgnoreCase("balance") || label.equalsIgnoreCase("bal")) {

            if (args.length == 0) {
                // No arguments: /balance
                double balance = plugin.getBalanceManager().getBalance(player.getUniqueId());
                player.sendMessage(ChatColor.GRAY + "Your balance: $" + ChatColor.GREEN + balance);
                return true;
            }

            // Now check if first argument is "set"
            if (args[0].equalsIgnoreCase("set")) {
                if (args.length != 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /balance set <player> <amount>");
                    return true;
                }

                if (!player.hasPermission("ptk.set")) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to set balances.");
                    return true;
                }

                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }

                double amount;
                try {
                    amount = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid amount.");
                    return true;
                }

                plugin.getBalanceManager().setBalance(target.getUniqueId(), amount);
                plugin.getBalanceManager().saveAllBalances();
                player.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s balance to $" + amount);
                plugin.logToFile(player.getName() + " set " + target.getName() + " balance to $" + amount);
                return true;
            }

            // If not "set" subcommand
            player.sendMessage(ChatColor.RED + "Unknown subcommand. Usage: /balance or /balance set <player> <amount>");
            return true;
        }


        // pay command
        if (label.equalsIgnoreCase("pay")){
            if(args.length != 2){
                player.sendMessage(ChatColor.GRAY + "Usage: /pay <player> <amount>");
                return true;
            }

            Player target = plugin.getServer().getPlayer(args[0]);
            if (target == null){
                player.sendMessage("Player not found.");
                return true;
            }

            double amount;
            try {
                amount = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage("Invalid amount.");
                return true;
            }

            if (amount <= 0){
                player.sendMessage("Payment amount must be greater than 0.");
                return true;
            }

            UUID targetId = target.getUniqueId();

            plugin.getBalanceManager().subtractBalance(player.getUniqueId(), amount);
            plugin.getBalanceManager().addBalance(targetId, amount);
            target.sendMessage(ChatColor.GREEN + "You received $" + amount + " from " + player.getName());
            plugin.logToFile(player.getName() + " payed $" + amount + " to " + target.getName());

            return true;
        }

        // Donation Command
        if (label.equalsIgnoreCase("donate")){
            if(!player.hasPermission("ptk.donate")){
                player.sendMessage(ChatColor.RED + "You do not have perms to use this command.");
                return true;
            }
            if(args.length != 2){
                player.sendMessage(ChatColor.GRAY + "Usage: /donate <player> <amount>");
                return true;
            }

            Player target = plugin.getServer().getPlayer(args[0]);
            if (target == null){
                player.sendMessage("Player not found.");
                return true;
            }

            double amount;
            try {
                amount = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage("Invalid amount.");
                return true;
            }

            if (amount <= 0){
                player.sendMessage("Donation amount must be greater than 0.");
                return true;
            }

            UUID targetId = target.getUniqueId();

            plugin.getBalanceManager().addBalance(targetId, amount);
            target.sendMessage(ChatColor.GREEN + "You received $" + amount + " from " + player.getName());
            plugin.logToFile(player.getName() + " donated $" + amount + " to " + target.getName());


            return true;
        }

        if (label.equalsIgnoreCase("baltop")){
            Map<UUID, Double> balances = plugin.getBalanceManager().getBalances();
    
            // Sort balances descending
            List<Map.Entry<UUID, Double>> sortedBalances = new ArrayList<>(balances.entrySet());
            sortedBalances.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

            Player senderPlayer = (Player) sender;
            senderPlayer.sendMessage(ChatColor.GOLD + "---- Top Balances ----");

            int maxShown = 10; // show top 10
            int rank = 1;
            for (Map.Entry<UUID, Double> entry : sortedBalances) {
                if (rank > maxShown) break;
                UUID uuid = entry.getKey();
                double bal = entry.getValue();

                // Try to get the player's name
                String name = plugin.getServer().getOfflinePlayer(uuid).getName();
                if (name == null) name = "Unknown";

                senderPlayer.sendMessage(ChatColor.YELLOW + "#" + rank + " " + ChatColor.AQUA + name + ChatColor.GRAY + " - $" + String.format("%.2f", bal));
                rank++;
            }
            return true;
        }

        return false;
    }
}
