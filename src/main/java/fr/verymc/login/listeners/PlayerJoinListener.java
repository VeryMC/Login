package fr.verymc.login.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.util.UuidUtils;
import dev.simplix.cirrus.common.Cirrus;
import dev.simplix.cirrus.common.business.PlayerWrapper;
import dev.simplix.cirrus.common.converter.Converters;
import fr.verymc.api.wrapper.Wrapper;
import fr.verymc.api.wrapper.users.login.LoginManager;
import fr.verymc.api.wrapper.users.login.dto.InitLoginDto;
import fr.verymc.api.wrapper.users.login.replies.InitLoginReply;
import fr.verymc.login.ConfigurationManager;
import fr.verymc.login.Login;
import fr.verymc.login.Manager;
import fr.verymc.login.menus.ChooseMenu;
import fr.verymc.login.menus.MethodMenu;
import fr.verymc.login.utils.SchedulerUtil;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerJoinListener {

    private final Wrapper wrapper;
    private ProxyServer proxyServer;
    private Login plugin;

    private RegisteredServer lobby;
    private RegisteredServer limbo;

    private HashMap<String, InitLoginReply> players;

    public PlayerJoinListener(Login plugin, ProxyServer proxyServer, Wrapper wrapper) {
        this.proxyServer = proxyServer;
        this.wrapper = wrapper;
        this.plugin = plugin;

        this.players = new HashMap<>();

        if(proxyServer.getServer("lobby").isPresent()) this.lobby = proxyServer.getServer("lobby").get();
        if(proxyServer.getServer("limbo").isPresent()) this.limbo = proxyServer.getServer("limbo").get();
    }


    @Subscribe
    public void onPlayerConnect(PreLoginEvent event) {
        final InitLoginDto dto = new InitLoginDto(event.getUsername(), UuidUtils.generateOfflinePlayerUuid(event.getUsername()));

        final LoginManager loginManager = this.wrapper.getUsersManager().getLoginManager();
        final Optional<InitLoginReply> reply = loginManager.initLogin(dto);

        if(reply.isEmpty()) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(ConfigurationManager.getComponent("messages/errors", "unknown.json")));
            return;
        }

        if(reply.get().getAuthMethod() == null && reply.get().getAccount().equals("premium")) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());
            return;
        }

        event.setResult(PreLoginEvent.PreLoginComponentResult.allowed());
        this.players.put(event.getUsername(), reply.get());
    }

    @Subscribe
    public void onPlayerJoin(PlayerChooseInitialServerEvent event) {
        final Player player = event.getPlayer();

        if(player.isOnlineMode() && this.lobby != null) {
            event.setInitialServer(this.lobby);
            return;
        }

        event.setInitialServer(this.limbo);
    }

    @Subscribe
    public void onPlayerConnect(ServerPostConnectEvent event) {
        final Player player = event.getPlayer();

        if(player.getCurrentServer().isEmpty()) return;
        if(!player.getCurrentServer().get().getServer().equals(this.limbo)) return;

        final InitLoginReply reply = this.players.get(player.getUsername());

        if(reply.isRegistered() || reply.getAccount().equals("cracked")) {
            if(reply.getAuthMethod() == null) {
                new MethodMenu(Converters.convert(player, PlayerWrapper.class),
                        Cirrus.configurationFactory().loadFile("plugins/login/menus/method.json"),
                        this.proxyServer, this.plugin).open();
                return;
            }

            switch (reply.getAuthMethod()) {
                case "discord" -> new Manager().discord(player, this.proxyServer, this.plugin);
                case "password" -> new Manager().password(player, this.proxyServer, this.plugin);
            }

            return;
        }

        new ChooseMenu(Converters.convert(player, PlayerWrapper.class),
                Cirrus.configurationFactory().loadFile("plugins/login/menus/choose.json"),
                this.proxyServer, this.plugin).open();
    }
}
