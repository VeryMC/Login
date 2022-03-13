package fr.verymc.login.runnables.redis.users.exceptions;

import com.velocitypowered.api.proxy.ProxyServer;
import fr.verymc.login.Login;
import fr.verymc.login.runnables.redis.users.LoginMessage;

public record LoginMessageHandler(
        ProxyServer proxyServer) implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Login.LOGGER.error("Exception with thread #" + t.getId() + "\n"
                + "Exception: " + e.getClass().getName() + " | " + e.getMessage());

        Login.LOGGER.info("Thread #" + t.getId() + " will be automatically restarted!");
        new Thread(new LoginMessage(this.proxyServer)).start();
    }
}
