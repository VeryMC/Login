package fr.verymc.login.menus;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.simplix.cirrus.common.business.PlayerWrapper;
import dev.simplix.cirrus.common.configuration.MenuConfiguration;
import dev.simplix.cirrus.common.menus.SimpleMenu;
import fr.verymc.login.Login;
import fr.verymc.login.Manager;

import java.util.Locale;

public class MethodMenu extends SimpleMenu {

    public MethodMenu(PlayerWrapper player, MenuConfiguration configuration, ProxyServer proxyServer, Login plugin) {
        super(player, configuration, Locale.FRENCH);

        registerActionHandler("method", click -> {
            if(proxyServer.getPlayer(player().uniqueId()).isEmpty()) return;
            final Player proxiedPlayer = proxyServer.getPlayer(player().uniqueId()).get();

            player().closeInventory();

            switch (click.arguments().get(0)) {
                case "discord" -> new Manager().discord(proxiedPlayer, proxyServer, plugin);
                case "password" -> new Manager().password(proxiedPlayer, proxyServer, plugin);
            }
        });
    }
}