package fr.verymc.login.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.verymc.login.ConfigurationManager;
import fr.verymc.login.Login;
import fr.verymc.login.Manager;

import java.util.Objects;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;

@Deprecated
public class LogCommand {

    private final Login plugin;
    private final ProxyServer proxyServer;

    public LogCommand(Login plugin, ProxyServer proxyServer) {
        this.plugin = plugin;
        this.proxyServer = proxyServer;
    }

    public static CommandMeta getCommandMeta(CommandManager cM) {
        return cM.metaBuilder("log").build();
    }

    public BrigadierCommand getBrigadierCommand() {
        final LiteralCommandNode<CommandSource> totalNode = LiteralArgumentBuilder
                .<CommandSource>literal("log")
                .executes(this::run)
                .build();

        final ArgumentCommandNode<CommandSource, String> typeNode = RequiredArgumentBuilder
                .<CommandSource, String>argument("type", string())
                .executes(this::run)
                .build();

        totalNode.addChild(typeNode);
        return new BrigadierCommand(totalNode);
    }

    private int run(final CommandContext<CommandSource> context) {
        final CommandSource source = context.getSource();
        if(!(source instanceof Player)) {
            source.sendMessage(ConfigurationManager.getComponent("messages/errors", "playerOnly.json"));
            return 0;
        }

        final Player player = (Player) source;

        try {getString(context, "type");} catch(IllegalArgumentException e) {return 0;}
        final String type = getString(context, "type");

        switch (type) {
            case "discord" -> new Manager().discord(player, this.proxyServer, this.plugin);
            case "password" -> new Manager().password(player, this.proxyServer, this.plugin);
        }

        return 0;
    }
}
