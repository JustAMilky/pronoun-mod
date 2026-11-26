package com.milky.pronounmod.mixin;

import com.milky.pronounmod.PronounData;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = PlayerList.class)
public class PlayerListMixin {

    private static final Logger LOGGER = LogUtils.getLogger();

    private void handleSend(
        ServerGamePacketListenerImpl connection,
        Packet<?> packet
    ) {
        LOGGER.info(
            "[Pronoun Mod Send Handler] Packet: {}",
            packet.getClass().getName()
        );

        if (
            packet instanceof ClientboundPlayerInfoUpdatePacket playerInfoPacket
        ) {
            PlayerList playerList = (PlayerList) (Object) this;
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

    @Redirect(
        method = "placeNewPlayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"
        )
    )
    private void redirectSend(
        ServerGamePacketListenerImpl connection,
        Packet<?> packet
    ) {
        handleSend(connection, packet);
    }

    @Redirect(
        method = "broadcastAll(Lnet/minecraft/network/protocol/Packet;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"
        )
    )
    private void redirectBroadcastSend(
        ServerGamePacketListenerImpl connection,
        Packet<?> packet
    ) {
        handleSend(connection, packet);
    }
}
