package com.rafaelsms.potocraft.serverprofile.util;

import org.jetbrains.annotations.NotNull;

public enum CombatType {

    MOB(0), PLAYER(1);

    private final int priority;

    CombatType(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public boolean canOverride(@NotNull CombatType other) {
        return priority > other.priority;
    }

    public boolean canResetTime(@NotNull CombatType other) {
        return priority == other.priority;
    }
}
