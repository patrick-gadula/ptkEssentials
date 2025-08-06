package com.patrickg;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CrateCommand implements CommandExecutor {

    private final Plugin plugin;
    private final int crateCost = 1000; // 💰 Spin costs $10,000

    public CrateCommand(Plugin plugin) {
        this.plugin = plugin;
        plugin.getCommand("crate").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can spin crates.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 1 && (args[0].equalsIgnoreCase("spin") || args[0].equalsIgnoreCase("open"))) {
            plugin.getCrateManager().openCrateSpinGUI(player, crateCost);
            return true;
        }

        player.sendMessage(ChatColor.RED + "Usage: /crate spin");
        return true;
    }
}
