package com.rafaelsms.potocraft.database.pojo;

import java.time.ZonedDateTime;

public class ServerProfile extends PlayerObject {

    private ZonedDateTime lastTeleportDate;
    private LocationObject lastTeleportLocation;
    private ZonedDateTime lastDeathDate;
    private LocationObject lastDeathLocation;
    private ZonedDateTime lastTotemConsumeDate;
    private long playTimeMillis;
    private int mobKills;
    private int playerKills;
    private int deathCount;
    private int blocksPlaced;
    private int blocksBroken;
    private long experiencePickedUp;
    private int totemUsage;

    public ServerProfile() {
    }

    public ZonedDateTime getLastTeleportDate() {
        return lastTeleportDate;
    }

    public void setLastTeleportDate(ZonedDateTime lastTeleportDate) {
        this.lastTeleportDate = lastTeleportDate;
    }

    public LocationObject getLastTeleportLocation() {
        return lastTeleportLocation;
    }

    public void setLastTeleportLocation(LocationObject lastTeleportLocation) {
        this.lastTeleportLocation = lastTeleportLocation;
    }

    public ZonedDateTime getLastDeathDate() {
        return lastDeathDate;
    }

    public void setLastDeathDate(ZonedDateTime lastDeathDate) {
        this.lastDeathDate = lastDeathDate;
    }

    public LocationObject getLastDeathLocation() {
        return lastDeathLocation;
    }

    public void setLastDeathLocation(LocationObject lastDeathLocation) {
        this.lastDeathLocation = lastDeathLocation;
    }

    public ZonedDateTime getLastTotemConsumeDate() {
        return lastTotemConsumeDate;
    }

    public void setLastTotemConsumeDate(ZonedDateTime lastTotemConsumeDate) {
        this.lastTotemConsumeDate = lastTotemConsumeDate;
    }

    public long getPlayTimeMillis() {
        return playTimeMillis;
    }

    public void setPlayTimeMillis(long playTimeMillis) {
        this.playTimeMillis = playTimeMillis;
    }

    public int getMobKills() {
        return mobKills;
    }

    public void setMobKills(int mobKills) {
        this.mobKills = mobKills;
    }

    public int getPlayerKills() {
        return playerKills;
    }

    public void setPlayerKills(int playerKills) {
        this.playerKills = playerKills;
    }

    public int getDeathCount() {
        return deathCount;
    }

    public void setDeathCount(int deathCount) {
        this.deathCount = deathCount;
    }

    public int getBlocksPlaced() {
        return blocksPlaced;
    }

    public void setBlocksPlaced(int blocksPlaced) {
        this.blocksPlaced = blocksPlaced;
    }

    public int getBlocksBroken() {
        return blocksBroken;
    }

    public void setBlocksBroken(int blocksBroken) {
        this.blocksBroken = blocksBroken;
    }

    public long getExperiencePickedUp() {
        return experiencePickedUp;
    }

    public void setExperiencePickedUp(long experiencePickedUp) {
        this.experiencePickedUp = experiencePickedUp;
    }

    public int getTotemUsage() {
        return totemUsage;
    }

    public void setTotemUsage(int totemUsage) {
        this.totemUsage = totemUsage;
    }
}
