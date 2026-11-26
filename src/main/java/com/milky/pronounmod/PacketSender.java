package com.milky.pronounmod;

import com.milky.pronounmod.mixin.ClientboundPlayerInfoUpdatePacketAccessor;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.scores.PlayerTeam;
import org.slf4j.Logger;

public class PacketSender {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static void handleSend(
        ServerGamePacketListenerImpl connection,
        Packet<?> packet,
        PlayerList playerList
    ) {
        LOGGER.info(
            "[Pronoun Mod Send Handler] Packet: {}",
            packet.getClass().getName()
        );

        if (
            packet instanceof ClientboundPlayerInfoUpdatePacket playerInfoPacket
        ) {
            PronounData pronounData = PronounData.get(playerList.getServer());

            List<ClientboundPlayerInfoUpdatePacket.Entry> entries =
                new ArrayList<>(playerInfoPacket.entries());

            for (int i = 0; i < entries.size(); i++) {
                var e = entries.get(i);

                String pronouns = pronounData.getPronouns(e.profileId());

                if (pronouns == null || pronouns.isBlank()) {
                    continue;
                }

                Component name =
                    e.displayName() != null
                        ? e.displayName()
                        : Component.literal(e.profile().getName());

                PlayerTeam team = playerList
                    .getServer()
                    .getScoreboard()
                    .getPlayersTeam(e.profile().getName());

                if (team != null) {
                    name = PlayerTeam.formatNameForTeam(team, name);
                }

                Component displayName = Component.empty()
                    .append(name)
                    .append(
                        Component.literal(" (" + pronouns + ")")
                            .withStyle(ChatFormatting.GRAY)
                            .withStyle(ChatFormatting.ITALIC)
                    );

                entries.set(
                    i,
                    new ClientboundPlayerInfoUpdatePacket.Entry(
                        e.profileId(),
                        e.profile(),
                        e.listed(),
                        e.latency(),
                        e.gameMode(),
                        displayName,
                        e.chatSession()
                    )
                );

                LOGGER.info("[Pronoun Mod] Entry: {}", e);
            }

            ClientboundPlayerInfoUpdatePacket newPacket =
                new ClientboundPlayerInfoUpdatePacket(
                    playerInfoPacket.actions(),
                    List.of()
                );

            (
                (ClientboundPlayerInfoUpdatePacketAccessor) newPacket
            ).pronounmod$setEntries(entries);

            connection.send(newPacket);
            return;
        }

        connection.send(packet);
    }
}
