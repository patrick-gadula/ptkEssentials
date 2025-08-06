package com.patrickg;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;



public class ChallengeCommand implements CommandExecutor {

    private static final List<String> EPIC_TARGETS = Arrays.asList(
    "ENDERMAN",
    "DIAMOND_ORE",
    "NETHERITE_BLOCK",
    "WITHER",
    "ANCIENT_DEBRIS");

    private final Plugin plugin;

    public ChallengeCommand(Plugin plugin) {
        this.plugin = plugin;
        plugin.getCommand("challenge").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        plugin.getChallengeManager().setupPlayerChallenges(player);

        if (args.length == 0) {
            showChallenges(player);
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("claim")) {
            try {
                int index = Integer.parseInt(args[1]) - 1; // Player sees 1,2,3 but internally 0,1,2
                boolean success = plugin.getChallengeManager().claimChallenge(player, index);

                if (success) {
                    player.sendMessage(ChatColor.GREEN + "You have claimed your reward!");
                } else {
                    player.sendMessage(ChatColor.RED + "You cannot claim that challenge yet!");
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid challenge number.");
            }
            return true;
        }

        player.sendMessage(ChatColor.RED + "Usage: /challenge OR /challenge claim <1/2/3>");
        return true;
    }

    private void showChallenges(Player player) {
        List<Map<String, Object>> challenges = plugin.getChallengeManager().getPlayerChallenges(player);

        player.sendMessage(ChatColor.GOLD + "---- Your Daily Challenges ----");

        for (int i = 0; i < challenges.size(); i++) {
            Map<String, Object> challenge = challenges.get(i);

            String type = (String) challenge.get("type");
            String target = (String) challenge.get("target");
            int amount = (int) challenge.get("amount");
            int progress = (int) challenge.get("progress");
            boolean completed = (boolean) challenge.get("completed");
            boolean claimed = (boolean) challenge.getOrDefault("claimed", false);
            int reward = (int) challenge.get("reward");

            ChatColor statusColor;

            if (claimed) {
                statusColor = ChatColor.DARK_GRAY;
            } else if (completed) {
                statusColor = ChatColor.GREEN;
            } else if (EPIC_TARGETS.contains(target.toUpperCase())) {
                statusColor = ChatColor.LIGHT_PURPLE; // 🌟 EPIC challenges are purple
            } else {
                statusColor = ChatColor.YELLOW;
            }

            player.sendMessage(statusColor + "#" + (i + 1) + " [" + type + "] " + target + " (" + progress + "/" + amount + ")" +
                    (completed ? (claimed ? " (Claimed)" : " (Completed!)") : "") +
                    " - Reward: $" + reward);
        }
    }
}
