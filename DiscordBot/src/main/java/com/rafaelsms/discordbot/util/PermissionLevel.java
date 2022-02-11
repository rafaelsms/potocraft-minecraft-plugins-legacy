package com.rafaelsms.discordbot.util;

import net.dv8tion.jda.api.Permission;

import java.util.Collection;
import java.util.List;

public enum PermissionLevel {

    OPERATOR(List.of(Permission.VIEW_CHANNEL,
                     Permission.MESSAGE_HISTORY,
                     Permission.MESSAGE_SEND,
                     Permission.MESSAGE_EMBED_LINKS,
                     Permission.MESSAGE_SEND_IN_THREADS,
                     Permission.MESSAGE_ATTACH_FILES,
                     Permission.MESSAGE_ADD_REACTION,
                     Permission.MODERATE_MEMBERS),
             List.of(Permission.CREATE_PUBLIC_THREADS, Permission.CREATE_PRIVATE_THREADS, Permission.MANAGE_CHANNEL)),
    INTERACT(List.of(Permission.VIEW_CHANNEL,
                     Permission.MESSAGE_HISTORY,
                     Permission.MESSAGE_SEND,
                     Permission.MESSAGE_ATTACH_FILES,
                     Permission.MESSAGE_SEND_IN_THREADS,
                     Permission.MESSAGE_EMBED_LINKS),
             List.of(Permission.MESSAGE_ADD_REACTION,
                     Permission.CREATE_PUBLIC_THREADS,
                     Permission.CREATE_PRIVATE_THREADS)),
    VIEW_ONLY(List.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY),
              List.of(Permission.MESSAGE_SEND,
                      Permission.MESSAGE_EMBED_LINKS,
                      Permission.MESSAGE_SEND_IN_THREADS,
                      Permission.MESSAGE_ATTACH_FILES,
                      Permission.MESSAGE_ADD_REACTION,
                      Permission.CREATE_PUBLIC_THREADS,
                      Permission.CREATE_PRIVATE_THREADS)),
    NONE(List.of(),
         List.of(Permission.VIEW_CHANNEL,
                 Permission.MESSAGE_HISTORY,
                 Permission.MESSAGE_SEND,
                 Permission.MESSAGE_EMBED_LINKS,
                 Permission.MESSAGE_SEND_IN_THREADS,
                 Permission.MESSAGE_ATTACH_FILES,
                 Permission.MESSAGE_ADD_REACTION,
                 Permission.CREATE_PUBLIC_THREADS,
                 Permission.CREATE_PRIVATE_THREADS));

    private final Collection<Permission> allowedPermissions;
    private final Collection<Permission> deniedPermissions;

    PermissionLevel(Collection<Permission> allowedPermissions, Collection<Permission> deniedPermissions) {
        this.allowedPermissions = allowedPermissions;
        this.deniedPermissions = deniedPermissions;
    }

    public Collection<Permission> getAllowedPermissions() {
        return allowedPermissions;
    }

    public Collection<Permission> getDeniedPermissions() {
        return deniedPermissions;

    }
}
