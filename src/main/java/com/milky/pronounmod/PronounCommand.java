package com.milky.pronounmod;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class PronounCommand {

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

                            PronounData.get(
                                context.getSource().getServer()
                            ).setPronouns(player.getUUID(), newPronouns);

                            ClientboundPlayerInfoUpdatePacket packet =
                                new ClientboundPlayerInfoUpdatePacket(
                                    ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
                                    player
                                );

                            var playerList = context.getSource().getServer().getPlayerList();

                            for (ServerPlayer recipient : playerList.getPlayers()) {
                                PacketSender.handleSend(
                                    recipient.connection,
                                    packet,
                                    playerList
                                );
                            }

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

                        PronounData.get(
                            context.getSource().getServer()
                        ).clearPronouns(player.getUUID());

                        context
                            .getSource()
                            .sendSuccess(
                                () ->
                                    Component.literal(
                                        "[Pronoun Mod] Your pronouns have been cleared."
                                    ).withStyle(ChatFormatting.AQUA),
                                false
                            );

                        ClientboundPlayerInfoUpdatePacket packet =
                            new ClientboundPlayerInfoUpdatePacket(
                                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
                                player
                            );

                        var playerList = context.getSource().getServer().getPlayerList();

                        for (ServerPlayer recipient : playerList.getPlayers()) {
                            PacketSender.handleSend(
                                recipient.connection,
                                packet,
                                playerList
                            );
                        }

                        return 1;
                    })
                )
        );
    }
}
