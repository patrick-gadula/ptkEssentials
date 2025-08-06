package com.patrickg.Clans;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ClanPrefixManager {

    private final Scoreboard scoreboard;
    private final Map<String, ChatColor> clanColors = new HashMap<>();
    private final Random random = new Random();


    private final ChatColor[] availableColors = {
        ChatColor.AQUA, ChatColor.BLUE, ChatColor.LIGHT_PURPLE,
        ChatColor.GREEN, ChatColor.YELLOW, ChatColor.GOLD
    };

    public ClanPrefixManager() {
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    }

    public void updatePlayerPrefix(Player player, String clanName) {
        removePlayerPrefix(player);
    
        String teamName = ("clan_" + clanName).toUpperCase().replaceAll("[^a-z0-9]", "");
        if (teamName.length() > 16) teamName = teamName.substring(0, 16);
    
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
            team.setPrefix(ChatColor.GRAY + "[ " + ChatColor.AQUA + clanName.toUpperCase() + ChatColor.GRAY + " ] " + ChatColor.RESET);
        }
        team.addEntry(player.getName());
    }
    

    public void removePlayerPrefix(Player player) {
        for (Team team : scoreboard.getTeams()) {
            if (team.hasEntry(player.getName())) {
                team.removeEntry(player.getName());
            }
        }
    }
}
