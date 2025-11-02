package ru.fuctorial.portableradio.common.network;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.entity.player.EntityPlayerMP;
import ru.fuctorial.portableradio.server.radio.RadioManager; 

public class PacketPtt implements IMessage {
    private boolean isTransmitting;

    public PacketPtt() {}

    public PacketPtt(boolean isTransmitting) {
        this.isTransmitting = isTransmitting;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.isTransmitting);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.isTransmitting = buf.readBoolean();
    }

    public static class Handler implements IMessageHandler<PacketPtt, IMessage> {
        @Override
        public IMessage onMessage(PacketPtt message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (message.isTransmitting) {
                RadioManager.INSTANCE.startTransmitting(player);
            } else {
                RadioManager.INSTANCE.stopTransmitting(player);
            }
            return null;
        }
    }
}