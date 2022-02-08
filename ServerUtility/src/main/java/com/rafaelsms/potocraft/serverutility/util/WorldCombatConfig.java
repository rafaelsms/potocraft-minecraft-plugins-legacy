package com.rafaelsms.potocraft.serverutility.util;

import org.jetbrains.annotations.Nullable;

public class WorldCombatConfig {

    private final boolean preventSkipNight;
    private final boolean constantCombat;
    private final boolean constantCombatSetting;
    private final int startCombatTime;
    private final int endCombatTime;

    public WorldCombatConfig(boolean preventSkipNight,
                             boolean constantCombat,
                             boolean constantCombatSetting,
                             int startCombatTime,
                             int endCombatTime) {
        this.preventSkipNight = preventSkipNight;
        this.constantCombat = constantCombat;
        this.constantCombatSetting = constantCombatSetting;
        this.startCombatTime = startCombatTime;
        this.endCombatTime = endCombatTime;
    }

    public boolean isPreventSkipNight() {
        return preventSkipNight;
    }

    public boolean isConstantCombat() {
        return constantCombat;
    }

    public boolean getConstantCombatSetting() {
        return constantCombatSetting;
    }

    public int getStartCombatTime() {
        return startCombatTime;
    }

    public int getEndCombatTime() {
        return endCombatTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(@Nullable WorldCombatConfig defaultConfig) {
        if (defaultConfig == null) {
            return builder();
        }
        Builder builder = new Builder();
        builder.preventSkipNight = defaultConfig.preventSkipNight;
        builder.constantCombat = defaultConfig.constantCombat;
        builder.constantCombatSetting = defaultConfig.constantCombatSetting;
        builder.startCombatTime = defaultConfig.startCombatTime;
        builder.endCombatTime = defaultConfig.endCombatTime;
        return builder;
    }

    public static class Builder {

        private boolean preventSkipNight = false;
        private boolean constantCombat = true;
        private boolean constantCombatSetting = false;
        private int startCombatTime = 0;
        private int endCombatTime = 0;

        public void setPreventSkipNight(boolean preventSkipNight) {
            this.preventSkipNight = preventSkipNight;
        }

        public void setConstantCombat(boolean constantCombat) {
            this.constantCombat = constantCombat;
        }

        public void setConstantCombatSetting(boolean constantCombatSetting) {
            this.constantCombatSetting = constantCombatSetting;
        }

        public void setCombatTime(int startCombatTime, int endCombatTime) {
            this.startCombatTime = startCombatTime;
            this.endCombatTime = endCombatTime;
        }

        public WorldCombatConfig build() {
            return new WorldCombatConfig(preventSkipNight,
                                         constantCombat,
                                         constantCombatSetting,
                                         startCombatTime,
                                         endCombatTime);
        }
    }
}
