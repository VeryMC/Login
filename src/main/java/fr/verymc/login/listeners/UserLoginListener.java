package fr.verymc.login.listeners;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import fr.verymc.login.Login;
import fr.verymc.login.ReflectManager;
import net.kyori.adventure.text.Component;
import redis.clients.jedis.JedisPubSub;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class UserLoginListener extends JedisPubSub {

    private final ProxyServer proxy;
    private RegisteredServer lobby;

    public UserLoginListener(ProxyServer proxy) {
        System.out.println("Init listener");
        this.proxy = proxy;
        if(this.proxy.getServer("lobby").isPresent()) this.lobby = this.proxy.getServer("lobby").get();
    }

    public void onMessage(String channel, String message) {
        if(!channel.equals("users:login")) return;

        final String[] msg = message.split(":");
        if(!msg[0].equals("success")) return;

        final Optional<Player> optionalPlayer = this.proxy.getPlayer(UUID.fromString(msg[1]));
        if(optionalPlayer.isEmpty()) return;
        try {
            final Player player = new ReflectManager().changeUUID(optionalPlayer.get(), UUID.fromString(msg[2]));
            player.createConnectionRequest(this.lobby).connect();

            Login.tasks.get(UUID.fromString(msg[1])).cancel();
            Login.tasks.remove(UUID.fromString(msg[1]));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            optionalPlayer.get().disconnect(Component.text(""));
            e.printStackTrace();
        }
    }
}
