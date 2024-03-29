package com.rafaelsms.potocraft.pet.commands;

import com.rafaelsms.potocraft.pet.Permissions;
import com.rafaelsms.potocraft.pet.PetPlugin;
import com.rafaelsms.potocraft.pet.player.User;
import com.rafaelsms.potocraft.util.TextUtil;
import com.rafaelsms.potocraft.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sittable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PetCommand implements TabExecutor {

    private final @NotNull Set<EntityType> allowedTypes = Set.of(EntityType.CHICKEN,
                                                                 EntityType.BEE,
                                                                 EntityType.COW,
                                                                 EntityType.MUSHROOM_COW,
                                                                 EntityType.ZOMBIE,
                                                                 EntityType.AXOLOTL,
                                                                 EntityType.GOAT,
                                                                 EntityType.FOX,
                                                                 EntityType.BAT,
                                                                 EntityType.BLAZE,
                                                                 EntityType.VEX,
                                                                 EntityType.CAT,
                                                                 EntityType.CAVE_SPIDER,
                                                                 EntityType.SPIDER,
                                                                 EntityType.SILVERFISH,
                                                                 EntityType.MAGMA_CUBE,
                                                                 EntityType.SHEEP,
                                                                 EntityType.SLIME,
                                                                 EntityType.SNOWMAN,
                                                                 EntityType.LLAMA,
                                                                 EntityType.RABBIT,
                                                                 EntityType.WOLF,
                                                                 EntityType.ZOMBIFIED_PIGLIN,
                                                                 EntityType.SKELETON);

    private final @NotNull PetPlugin plugin;

    public PetCommand(@NotNull PetPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfiguration().getPlayerOnlyCommand());
            return true;
        }
        if (!sender.hasPermission(Permissions.PET_PERMISSION)) {
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return true;
        }

        User user = plugin.getUserManager().getUser(player);
        if (args.length == 0) {
            // No arguments = Help + toggle
            showHelp(sender);
            showEntityTypeList(sender);
            togglePet(user);
            return true;
        }

        String subCommand = args[0];
        if (subCommand.equalsIgnoreCase("renomear") || subCommand.equalsIgnoreCase("rename")) {
            Optional<String> fullName = TextUtil.joinStrings(args, 1);
            if (fullName.isEmpty()) {
                showHelp(sender);
                return true;
            }
            if (fullName.get().length() > 16) {
                sender.sendMessage(plugin.getConfiguration().getPetNameTooLong());
                return true;
            }
            renamePet(user, fullName.get());
        } else if (subCommand.equalsIgnoreCase("sentar") || subCommand.equalsIgnoreCase("sit")) {
            makePetSit(user);
        } else if (subCommand.equalsIgnoreCase("adulto")) {
            makePetGrownUp(user);
        } else if (subCommand.equalsIgnoreCase("filhote") || subCommand.equalsIgnoreCase("baby")) {
            makePetBaby(user);
        } else {
            // Must be changing pet's type
            EntityType entityType;
            try {
                entityType = EntityType.valueOf(subCommand.toUpperCase());
                if (!allowedTypes.contains(entityType)) {
                    throw new IllegalArgumentException("Can't choose this entity type.");
                }
            } catch (IllegalArgumentException ignored) {
                sender.sendMessage(plugin.getConfiguration().getCommandEntityTypeUnavailable());
                showEntityTypeList(user.getPlayer());
                return true;
            }
            setPetType(user, entityType);
        }
        plugin.getDatabase().saveProfile(user.getProfile());
        return true;
    }

    private void makePetSit(@NotNull User user) {
        user.getPet().ifPresent(npc -> {
            if (npc.getEntity() instanceof Sittable sittable) {
                sittable.setSitting(!sittable.isSitting());
            }
        });
    }

    private void setPetType(@NotNull User user, @NotNull EntityType entityType) {
        user.getProfile().setPetType(entityType);
        respawnPet(user);
    }

    private void makePetBaby(@NotNull User user) {
        user.getProfile().setPetBaby(true);
        respawnPet(user);
    }

    private void makePetGrownUp(@NotNull User user) {
        user.getProfile().setPetBaby(false);
        respawnPet(user);
    }

    private void renamePet(@NotNull User user, @NotNull String petName) {
        user.getProfile().setPetColoredName(TextUtil.toColorizedString(petName));
        respawnPet(user);
    }

    private void respawnPet(@NotNull User user) {
        user.getProfile().setPetEnabled(true);
        user.spawnPet(); // this already despawns
    }

    private void togglePet(@NotNull User user) {
        boolean newEnabled = !user.getProfile().isPetEnabled();
        user.getProfile().setPetEnabled(newEnabled);
        if (newEnabled) {
            user.spawnPet();
            user.getPlayer().sendMessage(plugin.getConfiguration().getCommandPetEnabled());
        } else {
            user.despawnPet();
            user.getPlayer().sendMessage(plugin.getConfiguration().getCommandPetDisabled());
        }
    }

    private void showHelp(@NotNull CommandSender sender) {
        sender.sendMessage(plugin.getConfiguration().getCommandHelp());
    }

    private void showEntityTypeList(@NotNull CommandSender sender) {
        sender.sendMessage(plugin.getConfiguration().getCommandEntityTypeList(allowedTypes));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {
        List<String> suggestions = new LinkedList<>();
        suggestions.addAll(List.of("sentar", "filhote", "adulto", "renomear"));
        suggestions.addAll(Util.convertList(allowedTypes, type -> type.name().toLowerCase()));
        return suggestions;
    }
}
