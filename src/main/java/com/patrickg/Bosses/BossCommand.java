package com.patrickg.Bosses;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BossCommand implements CommandExecutor {

    private final BossManager bossManager;

    public BossCommand(BossManager bossManager) {
        this.bossManager = bossManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("boss.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "/boss spawn - Manually spawn a boss");
            sender.sendMessage(ChatColor.YELLOW + "/boss enable - Enable random boss spawns");
            sender.sendMessage(ChatColor.YELLOW + "/boss disable - Disable random boss spawns");
            sender.sendMessage(ChatColor.YELLOW + "/boss killall - Kill all active bosses");
            return true;
        }

        if (args[0].equalsIgnoreCase("spawn")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                bossManager.spawnRandomBoss(player.getWorld());
            } else {
                sender.sendMessage(ChatColor.RED + "Only players can spawn bosses manually.");
            }
        } else if (args[0].equalsIgnoreCase("enable")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                bossManager.enableAutoSpawn(player.getWorld(), 72000L);
            } else {
                sender.sendMessage(ChatColor.RED + "Only players can enable bosses.");
            }
        } else if (args[0].equalsIgnoreCase("disable")) {
            bossManager.disableAutoSpawn();
        } else if (args[0].equalsIgnoreCase("killall")) {
            bossManager.killAllBosses();
        } else {
            sender.sendMessage(ChatColor.RED + "Unknown subcommand.");
        }

        return true;
    }

}
