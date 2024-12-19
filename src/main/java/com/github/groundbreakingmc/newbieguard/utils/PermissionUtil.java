package com.github.groundbreakingmc.newbieguard.utils;

import lombok.Setter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;

public final class PermissionUtil {

    private static LuckPerms luckPerms;

    private PermissionUtil() {

    }

    public static void givePermission(final UUID playerUUID, final String permission) {
        if (luckPerms == null) {
            return;
        }

        luckPerms.getUserManager().loadUser(playerUUID).thenAccept(user -> {
            if (user != null) {
                final Node node = PermissionNode.builder(permission)
                        .value(true)
                        .build();

                user.data().add(node);

                luckPerms.getUserManager().saveUser(user);
            }
        });
    }

    static {
        final RegisteredServiceProvider<LuckPerms> registration = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (registration != null) {
            luckPerms = registration.getProvider();
        }
    }
}
