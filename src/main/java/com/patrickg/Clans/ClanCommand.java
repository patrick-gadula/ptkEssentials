package com.patrickg.Clans;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClanCommand implements CommandExecutor {

    private final ClanManager clanManager;
    private final ClanPrefixManager prefixManager;

    public ClanCommand(ClanManager clanManager, ClanPrefixManager prefixManager) {
        this.clanManager = clanManager;
        this.prefixManager = prefixManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use clan commands.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /clan <create|join|leave|info|sethome|home> [name]");
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /clan create <name>");
                    return true;
                }
                clanManager.createClan(player, args[1]);
                prefixManager.updatePlayerPrefix(player, args[1]);
                break;
            case "join":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /clan join <name>");
                    return true;
                }
                clanManager.joinClan(player, args[1]);
                prefixManager.updatePlayerPrefix(player, args[1]);
                break;
            case "leave":
                clanManager.leaveClan(player);
                prefixManager.removePlayerPrefix(player);
                break;
            case "info":
                Clan clan = clanManager.getClanByPlayer(player);
                if (clan == null) {
                    player.sendMessage(ChatColor.RED + "You are not in a clan.");
                } else {
                    player.sendMessage(ChatColor.GOLD + "Clan: " + ChatColor.YELLOW + clan.getName());
                    player.sendMessage(ChatColor.GOLD + "Owner: " + ChatColor.YELLOW + Bukkit.getOfflinePlayer(clan.getOwner()).getName());
                    player.sendMessage(ChatColor.GOLD + "Members: " + ChatColor.YELLOW + clan.getMembers().size());
                }
                break;
                case "sethome":
                Clan setHomeClan = clanManager.getClanByPlayer(player);
                if (setHomeClan == null) {
                    player.sendMessage(ChatColor.RED + "You are not in a clan.");
                    return true;
                }
                setHomeClan.setHome(player.getLocation());
                player.sendMessage(ChatColor.GREEN + "Clan home set!");
                break;
            
            case "home":
                Clan homeClan = clanManager.getClanByPlayer(player);
                if (homeClan == null) {
                    player.sendMessage(ChatColor.RED + "You are not in a clan.");
                    return true;
                }
                if (homeClan.getHome() == null) {
                    player.sendMessage(ChatColor.RED + "Your clan does not have a home set yet.");
                    return true;
                }
                player.teleport(homeClan.getHome());
                player.sendMessage(ChatColor.GREEN + "Teleported to your clan home!");
                break;            
            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Use /clan <create|join|leave|info>");
                break;
        }

        return true;
    }
}
