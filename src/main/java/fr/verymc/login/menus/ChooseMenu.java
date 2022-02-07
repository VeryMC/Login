package fr.verymc.login.menus;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.simplix.cirrus.common.Cirrus;
import dev.simplix.cirrus.common.business.PlayerWrapper;
import dev.simplix.cirrus.common.configuration.MenuConfiguration;
import dev.simplix.cirrus.common.menus.SimpleMenu;
import fr.verymc.api.wrapper.Wrapper;
import fr.verymc.api.wrapper.users.login.dto.PremiumRegisterDto;
import fr.verymc.api.wrapper.users.login.replies.PremiumRegisterReply;
import fr.verymc.login.ConfigurationManager;
import fr.verymc.login.Login;
import fr.verymc.login.Manager;
import net.kyori.adventure.text.Component;

import java.util.Locale;

public class ChooseMenu extends SimpleMenu {

    public ChooseMenu(PlayerWrapper player, MenuConfiguration configuration, ProxyServer proxyServer, Login plugin) {
        super(player, configuration, Locale.FRENCH);

        registerActionHandler("choose", click -> {
            switch (click.arguments().get(0)) {
                case "premium" -> {
                    if(proxyServer.getPlayer(player().uniqueId()).isEmpty()) return;
                    final Player proxiedPlayer = proxyServer.getPlayer(player().uniqueId()).get();

                    player().closeInventory();

                    final Wrapper http = new Wrapper("https://api.verymc.fr");
                    http.getUsersManager().getLoginManager().registerPremium(new PremiumRegisterDto(player().name()));

                    proxiedPlayer.disconnect(ConfigurationManager.getComponent("messages", "premiumKick.json"));
                }
                case "cracked" -> new ChooseMenu(player, Cirrus.configurationFactory().loadFile("plugins/login/menus/method.json"), proxyServer, plugin).open();
            }
        });
    }
}