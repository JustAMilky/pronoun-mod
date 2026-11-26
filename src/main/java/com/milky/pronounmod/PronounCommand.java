package com.milky.pronounmod;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class PronounCommand {

    private static void setPronouns(
        String pronouns,
        MinecraftServer server,
        ServerPlayer player
    ) {
        PronounData.get(server).setPronouns(player.getUUID(), pronouns);

        ClientboundPlayerInfoUpdatePacket packet =
            new ClientboundPlayerInfoUpdatePacket(
                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
                player
            );

        var playerList = server.getPlayerList();

        for (ServerPlayer recipient : playerList.getPlayers()) {
            PacketSender.sendPlayerInfo(recipient.connection, packet, playerList);
        }
    }

    private static void clearPronouns(
        MinecraftServer server,
        ServerPlayer player
    ) {
        PronounData.get(server).clearPronouns(player.getUUID());

        ClientboundPlayerInfoUpdatePacket packet =
            new ClientboundPlayerInfoUpdatePacket(
                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
                player
            );

        var playerList = server.getPlayerList();

        for (ServerPlayer recipient : playerList.getPlayers()) {
            PacketSender.sendPlayerInfo(recipient.connection, packet, playerList);
        }
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("pronouns")
                .then(
                    Commands.literal("set").then(
                        Commands.argument(
                            "pronouns",
                            StringArgumentType.greedyString()
                        ).executes(context -> {
                            String newPronouns = StringArgumentType.getString(
                                context,
                                "pronouns"
                            );

                            if (newPronouns.length() > 11) {
                                context
                                    .getSource()
                                    .sendFailure(
                                        Component.literal(
                                            "[Pronoun Mod] Pronouns must be 11 characters or less."
                                        ).withStyle(ChatFormatting.AQUA)
                                    );
                                return 0;
                            }

                            var player = context
                                .getSource()
                                .getPlayerOrException();

                            setPronouns(
                                newPronouns,
                                context.getSource().getServer(),
                                player
                            );

                            context
                                .getSource()
                                .sendSuccess(
                                    () ->
                                        Component.literal(
                                            "[Pronoun Mod] Your pronouns are now: " +
                                                newPronouns
                                        ).withStyle(ChatFormatting.AQUA),
                                    false
                                );

                            return 1;
                        })
                    )
                )
                .then(
                    Commands.literal("clear").executes(context -> {
                        var player = context.getSource().getPlayerOrException();

                        clearPronouns(context.getSource().getServer(), player);

                        context
                            .getSource()
                            .sendSuccess(
                                () ->
                                    Component.literal(
                                        "[Pronoun Mod] Your pronouns have been cleared."
                                    ).withStyle(ChatFormatting.AQUA),
                                false
                            );

                        return 1;
                    })
                )
                .then(
                    Commands.literal("setother")
                        .requires(source -> source.hasPermission(1))
                        .then(
                            Commands.argument(
                                "player",
                                EntityArgument.player()
                            ).then(
                                Commands.argument(
                                    "pronouns",
                                    StringArgumentType.greedyString()
                                ).executes(context -> {
                                    String newPronouns =
                                        StringArgumentType.getString(
                                            context,
                                            "pronouns"
                                        );

                                    var player = EntityArgument.getPlayer(
                                        context,
                                        "player"
                                    );

                                    setPronouns(
                                        newPronouns,
                                        context.getSource().getServer(),
                                        player
                                    );

                                    context
                                        .getSource()
                                        .sendSuccess(
                                            () ->
                                                Component.literal(
                                                    "[Pronoun Mod] " +
                                                        player
                                                            .getName()
                                                            .getString() +
                                                        "'s pronouns are now: " +
                                                        newPronouns
                                                ).withStyle(
                                                    ChatFormatting.AQUA
                                                ),
                                            false
                                        );

                                    return 1;
                                })
                            )
                        )
                )
                .then(
                    Commands.literal("clearother")
                        .requires(source -> source.hasPermission(1))
                        .then(
                            Commands.argument(
                                "player",
                                EntityArgument.player()
                            ).executes(context -> {
                                var player = EntityArgument.getPlayer(
                                    context,
                                    "player"
                                );

                                clearPronouns(
                                    context.getSource().getServer(),
                                    player
                                );

                                context
                                    .getSource()
                                    .sendSuccess(
                                        () ->
                                            Component.literal(
                                                "[Pronoun Mod] " +
                                                    player
                                                        .getName()
                                                        .getString() +
                                                    "'s pronouns have been cleared."
                                            ).withStyle(ChatFormatting.AQUA),
                                        false
                                    );

                                return 1;
                            })
                        )
                )
        );
    }
}
