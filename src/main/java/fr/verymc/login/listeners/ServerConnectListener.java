package fr.verymc.login.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.util.UuidUtils;
import fr.verymc.login.ReflectManager;

import java.util.UUID;

public class ServerConnectListener {

    private final ProxyServer proxyServer;
    private RegisteredServer limbo;

    public ServerConnectListener(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
        if(this.proxyServer.getServer("limbo").isPresent()) this.limbo = this.proxyServer.getServer("limbo").get();
    }

    @Subscribe
    public void onPlayerConnectOnLobby(ServerPostConnectEvent event) {
        if(event.getPlayer().getCurrentServer().isEmpty()) return;
        if(!event.getPlayer().getCurrentServer().get().getServer().getServerInfo().getName().equals("lobby")) return;
        if(event.getPreviousServer() != null && !event.getPreviousServer().getServerInfo().getName().equals("limbo")) return;

        final Player player = event.getPlayer();
        final UUID uuid = UuidUtils.generateOfflinePlayerUuid(player.getUsername());

        try {
            new ReflectManager().removePlayer(uuid, this.limbo, this.proxyServer);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
