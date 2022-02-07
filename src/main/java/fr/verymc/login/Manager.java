package fr.verymc.login;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.verymc.login.utils.SchedulerUtil;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Manager {
    public void discord(Player player, ProxyServer proxyServer, Login plugin) {
        final SchedulerUtil scheduler = new SchedulerUtil();
        AtomicInteger timer = new AtomicInteger(240);

        Login.tasks.put(player.getUniqueId(), scheduler.setTask(proxyServer.getScheduler().buildTask(plugin, () -> {
            if(timer.get() == 0) {
                player.disconnect(ConfigurationManager.getComponent("messages/errors", "timer.json", new HashMap<>(){{
                    put("time", "4 minutes");
                }}));
                scheduler.cancel();
                Login.tasks.remove(player.getUniqueId());
                return;
            }

            timer.set(timer.get() - 10);
            player.sendMessage(ConfigurationManager.getComponent("messages", "discordLogin.json", new HashMap<>(){{
                put("link", "https://api.verymc.fr/users/login/discord?uuid=" + player.getUniqueId());
            }}));
        }).repeat(10L, TimeUnit.SECONDS).schedule()));
    }

    public void password(Player player, ProxyServer proxyServer, Login plugin) {
        final SchedulerUtil scheduler = new SchedulerUtil();
        AtomicInteger timer = new AtomicInteger(240);
        Login.tasks.put(player.getUniqueId(), scheduler.setTask(proxyServer.getScheduler().buildTask(plugin, () -> {
            if(timer.get() == 0) {
                player.disconnect(ConfigurationManager.getComponent("messages/errors", "timer.json", new HashMap<>(){{
                    put("time", "4 minutes");
                }}));
                scheduler.cancel();
                Login.tasks.remove(player.getUniqueId());
                return;
            }

            timer.set(timer.get() - 10);
            player.sendMessage(ConfigurationManager.getComponent("messages", "passwordLogin.json"));
        }).repeat(10L, TimeUnit.SECONDS).schedule()));
    }
}
