package ru.fuctorial.portableradio.common.network;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.entity.player.EntityPlayerMP;
import ru.fuctorial.portableradio.common.inventory.ContainerWalkieTalkie;

public class PacketGuiInteraction implements IMessage {

    private int buttonId;

    public PacketGuiInteraction() {}

    public PacketGuiInteraction(int buttonId) {
        this.buttonId = buttonId;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.buttonId);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.buttonId = buf.readInt();
    }

    public static class Handler implements IMessageHandler<PacketGuiInteraction, IMessage> {
        @Override
        public IMessage onMessage(PacketGuiInteraction message, MessageContext ctx) {
            EntityPlayerMP serverPlayer = ctx.getServerHandler().playerEntity;
            if (serverPlayer.openContainer instanceof ContainerWalkieTalkie) {
                ((ContainerWalkieTalkie) serverPlayer.openContainer).handleGuiPacket(message.buttonId);
            }
            return null;
        }
    }
}