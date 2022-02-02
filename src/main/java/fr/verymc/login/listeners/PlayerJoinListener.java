package fr.verymc.login.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.util.UuidUtils;
import fr.verymc.api.wrapper.Wrapper;
import fr.verymc.api.wrapper.users.login.LoginManager;
import fr.verymc.api.wrapper.users.login.dto.InitLoginDto;
import fr.verymc.api.wrapper.users.login.replies.InitLoginReply;
import fr.verymc.login.ConfigurationManager;
import fr.verymc.login.Login;
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

        if(proxyServer.getServer("lobby").isPresent()) this.lobby = proxyServer.getServer("lobby").get();
        if(proxyServer.getServer("limbo").isPresent()) this.limbo = proxyServer.getServer("limbo").get();
    }


    @Subscribe
    public void onPlayerConnect(PreLoginEvent event) {
        final InitLoginDto dto = new InitLoginDto();
        dto.setUsername(event.getUsername());
        dto.setCrackUUID(UuidUtils.generateOfflinePlayerUuid(event.getUsername()));

        final LoginManager loginManager = this.wrapper.getUsersManager().getLoginManager();
        final Optional<InitLoginReply> reply = loginManager.initLogin(dto);

        if(reply.isEmpty()) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text("§cLogin error")));
            return;
        }

        if(reply.get().getAuthMethod().equals("premium")) {
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
    public void onPlayerConnect(ServerConnectedEvent event) {
        if(!event.getServer().equals(this.limbo)) return;

        final Player player = event.getPlayer();
        final InitLoginReply reply = this.players.get(player.getUsername());

        if(reply.isRegistered() || reply.getAccount().equals("cracked")) {
            if(reply.getAuthMethod().equals("discord")) {
                final SchedulerUtil scheduler = new SchedulerUtil();
                AtomicInteger timer = new AtomicInteger(240);
                scheduler.setTask(proxyServer.getScheduler().buildTask(this.plugin, () -> {
                    if(timer.get() == 0) {
                        player.disconnect(Component.text("§cPersonne n'avait vu quelqu'un d'aussi long !\nRappelez-vous que vous avez 2 minutes pour vous connecter !"));
                        scheduler.cancel();
                        return;
                    }

                    timer.set(timer.get() - 5);
                    player.sendMessage(Component.text("§eVeuillez ouvrir https://api.verymc.fr/users/login/discord?uuid=" + player.getUniqueId() + " pour vous connecter."));
                }).repeat(5L, TimeUnit.SECONDS).schedule());
                return;
            }

            if(reply.getAuthMethod().equals("password")) {
                final SchedulerUtil scheduler = new SchedulerUtil();
                AtomicInteger timer = new AtomicInteger(240);
                scheduler.setTask(proxyServer.getScheduler().buildTask(this.plugin, () -> {
                    if(timer.get() == 0) {
                        player.disconnect(Component.text("§cPersonne n'avait vu quelqu'un d'aussi long !\nRappelez-vous que vous avez 2 minutes pour vous connecter !"));
                        scheduler.cancel();
                        return;
                    }

                    timer.set(timer.get() - 5);
                    player.sendMessage(Component.text("§eVeuillez vous connecter avec /login [mot de passe]"));
                }).repeat(5L, TimeUnit.SECONDS).schedule());
                return;
            }

            player.sendMessage(ConfigurationManager.getComponent("messages", "method.json"));
            return;
        }
        player.sendMessage(ConfigurationManager.getComponent("messages", "choose.json"));
    }
}
