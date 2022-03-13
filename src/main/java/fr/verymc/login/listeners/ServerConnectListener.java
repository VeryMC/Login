package fr.verymc.login.listeners;

import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.permission.PermissionsSetupEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.permission.PermissionFunction;
import com.velocitypowered.api.permission.PermissionProvider;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.util.UuidUtils;
import dev.simplix.protocolize.api.util.ReflectionUtil;
import fr.verymc.login.ConfigurationManager;
import fr.verymc.login.Login;
import fr.verymc.login.ReflectManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.UserManager;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public class ServerConnectListener {

    private static PermissionProvider DEFAULT_PERMISSIONS = s -> PermissionFunction.ALWAYS_UNDEFINED;

    private final ProxyServer proxyServer;
    private RegisteredServer limbo;

    public ServerConnectListener(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
        if(this.proxyServer.getServer("limbo").isPresent()) this.limbo = this.proxyServer.getServer("limbo").get();
    }

    @Subscribe
    public void onPlayerConnectOnLobby(ServerPostConnectEvent event) {
        if(event.getPlayer().getCurrentServer().isEmpty()) return;
        if(event.getPlayer().isOnlineMode()) return;
        if(!event.getPlayer().getCurrentServer().get().getServer().getServerInfo().getName().equals("lobby")) return;
        if(event.getPreviousServer() != null && !event.getPreviousServer().getServerInfo().getName().equals("limbo")) return;

        final Player player = event.getPlayer();
        final UUID uuid = UuidUtils.generateOfflinePlayerUuid(player.getUsername());

        try {
            new ReflectManager().removePlayerFromServer(uuid, this.limbo);
            new ReflectManager().rechargePlayerFromProxy(UuidUtils.generateOfflinePlayerUuid(player.getUsername()), player.getUniqueId(), this.proxyServer);

            System.out.println(player.getClass().getName());
            System.out.println(player.getClass().getPackageName());
            System.out.println(player.getClass().getSuperclass().getName());

            final EventManager eventManager = proxyServer.getEventManager();
            eventManager.fire(new PermissionsSetupEvent(player, DEFAULT_PERMISSIONS))
                    .thenAcceptAsync(permEvent -> {
                        final PermissionFunction permissionFunction = permEvent.createFunction(player);
                        if(permissionFunction == null) {
                            Login.LOGGER.error(
                                    "A plugin permission provider {} provided an invalid permission function"
                                            + " for player {}. This is a bug in the plugin, not in Velocity. Falling"
                                            + " back to the default permission function.",
                                    permEvent.getProvider().getClass().getName(),
                                    player.getUsername());
                        } else {
                            try {
                                new ReflectManager().setPlayersPerms(player, permissionFunction);
                            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
