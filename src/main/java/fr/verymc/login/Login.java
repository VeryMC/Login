package fr.verymc.login;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.simplix.cirrus.velocity.CirrusVelocity;
import fr.verymc.api.wrapper.Wrapper;
import fr.verymc.login.commands.LogCommand;
import fr.verymc.login.commands.LoginCommand;
import fr.verymc.login.listeners.PlayerJoinListener;
import fr.verymc.login.listeners.ServerConnectListener;
import fr.verymc.login.listeners.UserLoginListener;
import fr.verymc.login.redis.RedisAccess;
import fr.verymc.login.utils.SchedulerUtil;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

@Plugin(
        id = "login",
        name = "Login",
        version = "1.0.0-SNAPSHOT",
        description = "Login system fo VeryMC",
        authors = {"Exodius SAS"}
)
public class Login {

    private final Logger logger;
    private final ProxyServer proxyServer;
    public final static HashMap<UUID, SchedulerUtil> tasks = new HashMap<>();

    private Jedis redisConnection;

    @Inject
    public Login(Logger logger, ProxyServer proxyServer, @DataDirectory Path dataDirectory) {
        this.logger = logger;
        this.proxyServer = proxyServer;

        logger.info("Login plugin loading...");

        logger.info("Loading configurations...");
        try {
            new ConfigurationManager(dataDirectory);
            logger.info("Loading configurations... Done");
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("Init Redis...");
        RedisAccess.init();

        new Thread(() -> {
            this.redisConnection = RedisAccess.INSTANCE.getPool().getResource();
            this.redisConnection.subscribe(new UserLoginListener(this.proxyServer), "users:login");
        }).start();
        logger.info("Init Redis... Done");

        logger.info("Loading commands...");
        final CommandManager cm = this.proxyServer.getCommandManager();
        cm.register(LoginCommand.getCommandMeta(cm), new LoginCommand(new Wrapper("https://api.verymc.fr"), this.proxyServer).getBrigadierCommand());
        logger.info("Loading commands... Done");

        logger.info("Login plugin loaded!");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        CirrusVelocity.init(this.proxyServer, this);

        logger.info("Loading listeners...");
        proxyServer.getEventManager().register(this, new PlayerJoinListener(this, proxyServer, new Wrapper("https://api.verymc.fr")));
        proxyServer.getEventManager().register(this, new ServerConnectListener(this.proxyServer));
        logger.info("Loading listeners... Done");
    }

    @Subscribe
    public void onProxyStop(ProxyShutdownEvent event) {
        logger.info("Close Redis...");
        this.redisConnection.close();
        RedisAccess.INSTANCE.getPool().close();
        logger.info("Close Redis... Done");

        logger.info("Good bye!");
    }
}
