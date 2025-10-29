package ru.fuctorial.portableradio.common.network;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import ru.fuctorial.portableradio.common.inventory.ContainerWalkieTalkie;
import ru.fuctorial.portableradio.common.item.ItemWalkieTalkie;
import ru.fuctorial.portableradio.server.radio.RadioCooldownManager;

public class PacketUpdateFrequency implements IMessage {
    private int frequency;

    public PacketUpdateFrequency() {}

    public PacketUpdateFrequency(int frequency) {
        this.frequency = frequency;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.frequency);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.frequency = buf.readInt();
    }

    public static class Handler implements IMessageHandler<PacketUpdateFrequency, IMessage> {
        @Override
        public IMessage onMessage(PacketUpdateFrequency message, MessageContext ctx) {
            EntityPlayerMP serverPlayer = ctx.getServerHandler().playerEntity;
            if (serverPlayer == null) return null;

            if (RadioCooldownManager.INSTANCE.canChangeFrequency(serverPlayer.getUniqueID())) {
                if (serverPlayer.openContainer instanceof ContainerWalkieTalkie) {
                    ((ContainerWalkieTalkie) serverPlayer.openContainer).updateFrequency(message.frequency);
                }
            }
            return null;
        }


        private static ItemStack findRadioInInventory(EntityPlayerMP player) {
            ItemStack held = player.getHeldItem();
            if (held != null && held.getItem() instanceof ItemWalkieTalkie) {
                return held;
            }
            for (ItemStack stack : player.inventory.mainInventory) {
                if (stack != null && stack.getItem() instanceof ItemWalkieTalkie) {
                    return stack;
                }
            }
            return null;
        }
    }
}