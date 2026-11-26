package com.milky.pronounmod.mixin;

import com.milky.pronounmod.PacketSender;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = PlayerList.class)
public class PlayerListMixin {

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
        PacketSender.sendPlayerInfo(connection, packet, (PlayerList) (Object) this);
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
        PacketSender.sendPlayerInfo(connection, packet, (PlayerList) (Object) this);
    }
}
