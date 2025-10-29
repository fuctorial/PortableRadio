/*
этот класс больше не нужен. сохраню на всякий

package ru.fuctorial.portableradio.common.network;


import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.entity.player.EntityPlayerMP;
import ru.fuctorial.portableradio.common.inventory.ContainerWalkieTalkie;

public class PacketUpdateAudioDevice implements IMessage {
    private int deviceIndex;
    private boolean isMicrophone;

    public PacketUpdateAudioDevice() {}

    public PacketUpdateAudioDevice(int deviceIndex, boolean isMicrophone) {
        this.deviceIndex = deviceIndex;
        this.isMicrophone = isMicrophone;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.deviceIndex);
        buf.writeBoolean(this.isMicrophone);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.deviceIndex = buf.readInt();
        this.isMicrophone = buf.readBoolean();
    }

    public static class Handler implements IMessageHandler<PacketUpdateAudioDevice, IMessage> {
        @Override
        public IMessage onMessage(PacketUpdateAudioDevice message, MessageContext ctx) {
            EntityPlayerMP serverPlayer = ctx.getServerHandler().playerEntity;
            if (serverPlayer.openContainer instanceof ContainerWalkieTalkie) {
                ContainerWalkieTalkie container = (ContainerWalkieTalkie) serverPlayer.openContainer;

                if (message.isMicrophone) {
                    container.updateMicrophoneIndex(message.deviceIndex);
                } else {
                    container.updateSpeakerIndex(message.deviceIndex);
                }

            }
                    return null;
                    }
                    }
                    }
}*/
