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
import com.velocitypowered.api.util.GameProfile;
import fr.verymc.api.wrapper.Wrapper;
import fr.verymc.api.wrapper.users.login.LoginManager;
import fr.verymc.api.wrapper.users.login.dto.PasswordCheckDto;
import fr.verymc.api.wrapper.users.login.replies.PasswordCheckReply;
import fr.verymc.login.ConfigurationManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.util.List;
import java.util.Optional;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class LoginCommand {

    private final Wrapper http;

    public LoginCommand(Wrapper http) {
        this.http = http;
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
            source.sendMessage(ConfigurationManager.getComponent("messages/errors", "missingPasswordArg.json"));
            return 0;
        }
        final String password = getString(context, "Mot de passe");

        final LoginManager loginManager = http.getUsersManager().getLoginManager();
        final Optional<PasswordCheckReply> reply = loginManager.passwordCheck(new PasswordCheckDto(player.getUniqueId(), password));

        if(reply.isEmpty()) {
            source.sendMessage(ConfigurationManager.getComponent("messages/errors", "unknownError.json"));
            return 0;
        }

        final PasswordCheckReply passwordCheckReply = reply.get();

        return 0;
    }
}
