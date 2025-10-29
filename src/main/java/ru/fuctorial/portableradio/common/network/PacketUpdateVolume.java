package ru.fuctorial.portableradio.common.network;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import ru.fuctorial.portableradio.common.inventory.ContainerWalkieTalkie;
import ru.fuctorial.portableradio.common.item.ItemWalkieTalkie;

public class PacketUpdateVolume implements IMessage {
    private int volume;
    private boolean isMicrophone;

    public PacketUpdateVolume() {}

    public PacketUpdateVolume(int volume, boolean isMicrophone) {
        this.volume = volume;
        this.isMicrophone = isMicrophone;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.volume);
        buf.writeBoolean(this.isMicrophone);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.volume = buf.readInt();
        this.isMicrophone = buf.readBoolean();
    }

    public static class Handler implements IMessageHandler<PacketUpdateVolume, IMessage> {
        @Override
        public IMessage onMessage(PacketUpdateVolume message, MessageContext ctx) {
            EntityPlayerMP serverPlayer = ctx.getServerHandler().playerEntity;

            System.out.println("[PortableRadio] PacketUpdateVolume received: volume=" + message.volume + ", isMic=" + message.isMicrophone);

            ItemStack radioStack = findRadioInInventory(serverPlayer);

            if (radioStack == null) {
                System.err.println("[PortableRadio] ERROR: No radio found in player inventory!");
                return null;
            }

            ItemWalkieTalkie radioItem = (ItemWalkieTalkie) radioStack.getItem();
            float normalizedVolume = message.volume / 100.0f;

            if (message.isMicrophone) {
                radioItem.setMicrophoneVolume(radioStack, normalizedVolume);
                System.out.println("[PortableRadio] Updated MIC volume to " + message.volume + "% in inventory");
            } else {
                radioItem.setSpeakerVolume(radioStack, normalizedVolume);
                System.out.println("[PortableRadio] Updated SPEAKER volume to " + message.volume + "% in inventory");
            }

            serverPlayer.inventory.markDirty();

            return null;
        }

        private static ItemStack findRadioInInventory(EntityPlayerMP player) {

            ItemStack held = player.getHeldItem();
            if (held != null && held.getItem() instanceof ItemWalkieTalkie) {
                return held;
            }

            for (int i = 0; i < player.inventory.mainInventory.length; i++) {
                ItemStack stack = player.inventory.mainInventory[i];
                if (stack != null && stack.getItem() instanceof ItemWalkieTalkie) {
                    return stack;
                }
            }

            return null;
        }
    }
}