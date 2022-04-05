package com.rafaelsms.potocraft.plugin.combat;

import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum CombatType {

    PLAYER_COMBAT(3), ENTITY_COMBAT(2), OTHER(1);

    private final int priority;

    CombatType(int priority) {
        this.priority = priority;
    }

    public boolean isHigherOrEqualPriority(@Nullable CombatType combatType) {
        if (combatType == null) {
            return true;
        }
        return this.priority >= combatType.priority;
    }

    public boolean isHigherPriority(@Nullable CombatType combatType) {
        if (combatType == null) {
            return true;
        }
        return this.priority > combatType.priority;
    }

    public static @NotNull CombatType getFromDamageSource(@NotNull EntityDamageEvent.DamageCause cause) {
        return switch (cause) {
            case CONTACT, DRYOUT, CRAMMING, HOT_FLOOR, FLY_INTO_WALL, FALL, SUICIDE, CUSTOM, STARVATION, LIGHTNING, MELTING -> OTHER;
            case ENTITY_ATTACK, DRAGON_BREATH, WITHER, PROJECTILE, ENTITY_SWEEP_ATTACK -> ENTITY_COMBAT;
            // To be future-proof, consider all other damage source as PLAYER_COMBAT
            // case SUFFOCATION, FREEZE, THORNS, FALLING_BLOCK, MAGIC, POISON, VOID, ENTITY_EXPLOSION, BLOCK_EXPLOSION, DROWNING, LAVA, FIRE_TICK, FIRE -> PLAYER_COMBAT;
            default -> PLAYER_COMBAT;
        };
    }
}
