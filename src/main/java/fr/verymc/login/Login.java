package fr.verymc.login;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.simplix.cirrus.velocity.CirrusVelocity;
import fr.verymc.api.wrapper.Wrapper;
import fr.verymc.login.commands.LoginCommand;
import fr.verymc.login.listeners.PlayerJoinListener;
import fr.verymc.login.listeners.ServerConnectListener;
import fr.verymc.login.redis.RedisAccess;
import fr.verymc.login.runnables.redis.users.LoginMessage;
import fr.verymc.login.utils.SchedulerUtil;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

@Plugin(
        id = "login",
        name = "Login",
        version = "1.0.0-SNAPSHOT",
        dependencies = {
                @Dependency(id = "luckperms")
        },
        description = "Login system fo VeryMC",
        authors = {"Exodius SAS"}
)
public class Login {

    public static Logger LOGGER;

    private final ProxyServer proxyServer;
    public final static HashMap<UUID, SchedulerUtil> tasks = new HashMap<>();

    @Inject
    public Login(Logger logger, ProxyServer proxyServer, @DataDirectory Path dataDirectory) {
        LOGGER = logger;
        this.proxyServer = proxyServer;

        LOGGER.info("Login plugin loading...");

        LOGGER.info("Loading configurations...");
        try {
            new ConfigurationManager(dataDirectory);
            LOGGER.info("Loading configurations... Done");
        } catch (IOException e) {
            e.printStackTrace();
        }

        LOGGER.info("Init Redis...");
        RedisAccess.init();

        new Thread(new LoginMessage(this.proxyServer)).start();
        LOGGER.info("Init Redis... Done");

        LOGGER.info("Loading commands...");
        final CommandManager cm = this.proxyServer.getCommandManager();
        cm.register(LoginCommand.getCommandMeta(cm), new LoginCommand(new Wrapper("https://api.verymc.fr"), this.proxyServer).getBrigadierCommand());
        LOGGER.info("Loading commands... Done");

        LOGGER.info("Login plugin loaded!");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        CirrusVelocity.init(this.proxyServer, this);

        LOGGER.info("Loading listeners...");
        proxyServer.getEventManager().register(this, new PlayerJoinListener(this, proxyServer, new Wrapper("https://api.verymc.fr")));
        proxyServer.getEventManager().register(this, new ServerConnectListener(this.proxyServer));
        LOGGER.info("Loading listeners... Done");
    }

    @Subscribe
    public void onProxyStop(ProxyShutdownEvent event) {
        LOGGER.info("Close Redis...");
        RedisAccess.INSTANCE.getPool().close();
        LOGGER.info("Close Redis... Done");

        LOGGER.info("Good bye!");
    }
}
