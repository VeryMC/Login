package fr.verymc.login.runnables.redis.users;

import com.velocitypowered.api.proxy.ProxyServer;
import fr.verymc.login.listeners.UserLoginListener;
import fr.verymc.login.redis.RedisAccess;
import fr.verymc.login.runnables.redis.users.exceptions.LoginMessageHandler;
import redis.clients.jedis.Jedis;

public record LoginMessage(ProxyServer proxyServer) implements Runnable {

    @Override
    public void run() {
        Thread.setDefaultUncaughtExceptionHandler(new LoginMessageHandler(this.proxyServer));

        final Jedis connection = RedisAccess.INSTANCE.getPool().getResource();
        connection.subscribe(new UserLoginListener(this.proxyServer), "users:login");
    }
}
