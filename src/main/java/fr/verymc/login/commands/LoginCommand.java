package fr.verymc.login.commands;

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
import com.velocitypowered.api.proxy.server.RegisteredServer;
import fr.verymc.api.wrapper.Wrapper;
import fr.verymc.api.wrapper.users.login.LoginManager;
import fr.verymc.api.wrapper.users.login.dto.PasswordCheckDto;
import fr.verymc.api.wrapper.users.login.replies.PasswordCheckReply;
import fr.verymc.login.ConfigurationManager;
import fr.verymc.login.Login;
import fr.verymc.login.ReflectManager;
import net.kyori.adventure.text.Component;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class LoginCommand {

    private final Wrapper http;

    private RegisteredServer lobby;

    public LoginCommand(Wrapper http, ProxyServer proxyServer) {
        this.http = http;

        if(proxyServer.getServer("lobby").isPresent()) this.lobby = proxyServer.getServer("lobby").get();
    }

    public static CommandMeta getCommandMeta(CommandManager cM) {
        return cM.metaBuilder("login").aliases("register").build();
    }

    public BrigadierCommand getBrigadierCommand() {
        final LiteralCommandNode<CommandSource> totalNode = LiteralArgumentBuilder
                .<CommandSource>literal("login")
                .executes(this::run)
                .build();

        final ArgumentCommandNode<CommandSource, String> passwordNode = RequiredArgumentBuilder
                .<CommandSource, String>argument("Mot de passe", greedyString())
                .executes(this::run)
                .build();

        totalNode.addChild(passwordNode);
        return new BrigadierCommand(totalNode);
    }

    private int run(final CommandContext<CommandSource> context) {
        final CommandSource source = context.getSource();
        if(!(source instanceof Player)) {
            source.sendMessage(ConfigurationManager.getComponent("messages/errors", "playerOnly.json"));
            return 0;
        }

        final Player player = (Player) source;

        try {getString(context, "Mot de passe");} catch(IllegalArgumentException e) {
            player.sendMessage(ConfigurationManager.getComponent("messages/errors", "missingPasswordArg.json"));
            return 0;
        }
        final String password = getString(context, "Mot de passe");

        final LoginManager loginManager = http.getUsersManager().getLoginManager();
        final Optional<PasswordCheckReply> reply = loginManager.passwordCheck(new PasswordCheckDto(player.getUniqueId(), password));

        if(reply.isEmpty()) {
            player.disconnect(ConfigurationManager.getComponent("messages/errors", "unknown.json"));
            return 0;
        }

        final PasswordCheckReply passwordCheckReply = reply.get();
        if(!player.getUniqueId().equals(passwordCheckReply.getCrackUUID())) return 0;

        try {
            final Player newPlayer = new ReflectManager().changeUUID(player, passwordCheckReply.getUUID());
            newPlayer.createConnectionRequest(this.lobby).connect();

            Login.tasks.get(passwordCheckReply.getCrackUUID()).cancel();
            Login.tasks.remove(passwordCheckReply.getCrackUUID());

            return 0;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            player.disconnect(ConfigurationManager.getComponent("messages/errors", "unknown.json"));
            return 0;
        }
    }
}
