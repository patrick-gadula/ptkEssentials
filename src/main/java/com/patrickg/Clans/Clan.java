package com.patrickg.Clans;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;


public class Clan {
    private final String name;
    private final UUID owner;
    private final Set<UUID> members = new HashSet<>();
    private Location home = null;

    public Clan(String name, UUID owner) {
        this.name = name;
        this.owner = owner;
        this.members.add(owner);
    }

    public String getName() {
        return name;
    }

    public UUID getOwner() {
        return owner;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public Location getHome() {
        return home;
    }

    public void setHome(Location home) {
        this.home = home;
    }
    
}
