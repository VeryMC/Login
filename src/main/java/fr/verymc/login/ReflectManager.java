package fr.verymc.login;

import com.velocitypowered.api.permission.PermissionFunction;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.util.GameProfile;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectManager {

    public Player changeUUID(Player player, UUID newUUID) throws NoSuchFieldException, IllegalAccessException {
        final Class<?> playerClass = player.getClass();

        final Field gameProfileField = playerClass.getDeclaredField("profile");
        gameProfileField.setAccessible(true);

        final GameProfile gameProfile = (GameProfile) gameProfileField.get(player);

        final GameProfile newGameProfile = new GameProfile(newUUID, gameProfile.getName(), gameProfile.getProperties());
        gameProfileField.set(player, newGameProfile);

        gameProfileField.setAccessible(false);

        return player;
    }

    public void rechargePlayerFromProxy(UUID oldUuid, UUID newUuid, ProxyServer proxy) throws NoSuchFieldException, IllegalAccessException {
        final Class<?> proxyClass = proxy.getClass();
        final Field proxiedPlayers = proxyClass.getDeclaredField("connectionsByUuid");
        proxiedPlayers.setAccessible(true);

        final ConcurrentHashMap<UUID, Object> proxiedPlayersMap = (ConcurrentHashMap<UUID, Object>) proxiedPlayers.get(proxy);
        final Object player = proxiedPlayersMap.get(oldUuid);

        proxiedPlayersMap.remove(oldUuid);
        proxiedPlayersMap.put(newUuid, player);

        proxiedPlayers.set(proxy, proxiedPlayersMap);
    }

    public void removePlayerFromServer(UUID player, RegisteredServer server) throws NoSuchFieldException, IllegalAccessException {
        final Class<?> serverClass = server.getClass();
        final Field playersOnLimbo = serverClass.getDeclaredField("players");
        playersOnLimbo.setAccessible(true);
        final ConcurrentHashMap<UUID, ?> playersLimboMap = (ConcurrentHashMap<UUID, ?>) playersOnLimbo.get(server);
        playersLimboMap.remove(player);
        playersOnLimbo.set(server, playersLimboMap);

        /*final Class<?> proxyClass = proxy.getClass();
        System.out.println(proxyClass.getName());
        System.out.println(proxyClass.getSuperclass().getName());
        final Field proxiedPlayers = proxyClass.getDeclaredField("connectionsByUuid");
        proxiedPlayers.setAccessible(true);
        final ConcurrentHashMap<UUID, ?> proxiedPlayersMap = (ConcurrentHashMap<UUID, ?>) proxiedPlayers.get(proxy);
        proxiedPlayersMap.remove(player);
        proxiedPlayers.set(proxy, proxiedPlayersMap);*/
    }

    public void setPlayersPerms(Player player, PermissionFunction permissionFunction) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Class<?> playerClass = player.getClass();
        final Method setPermissionsFunction = playerClass.getDeclaredMethod("setPermissionFunction", PermissionFunction.class);

        setPermissionsFunction.setAccessible(true);
        setPermissionsFunction.invoke(player, permissionFunction);
        setPermissionsFunction.setAccessible(false);
    }
}