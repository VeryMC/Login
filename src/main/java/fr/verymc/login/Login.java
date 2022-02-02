package fr.verymc.login;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.verymc.api.wrapper.Wrapper;
import fr.verymc.login.listeners.PlayerJoinListener;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

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

        logger.info("Login plugin loaded!");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Loading listeners...");
        proxyServer.getEventManager().register(this, new PlayerJoinListener(this, proxyServer, new Wrapper("https://api.verymc.fr")));
    }
}
