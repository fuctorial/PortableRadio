package ru.fuctorial.portableradio.common.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import ru.fuctorial.portableradio.common.item.ItemWalkieTalkie;
import ru.fuctorial.portableradio.server.radio.RadioCooldownManager;

public class PacketTogglePower implements IMessage {

    public PacketTogglePower() {}

    @Override public void fromBytes(ByteBuf buf) {}
    @Override public void toBytes(ByteBuf buf) {}

    public static class Handler implements IMessageHandler<PacketTogglePower, IMessage> {
        @Override
        public IMessage onMessage(PacketTogglePower message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player == null) return null;

            if (RadioCooldownManager.INSTANCE.canTogglePower(player.getUniqueID())) {
                ItemStack heldItem = player.getHeldItem();
                if (heldItem != null && heldItem.getItem() instanceof ItemWalkieTalkie) {
                    ItemWalkieTalkie radio = (ItemWalkieTalkie) heldItem.getItem();
                    boolean newState = !radio.isRadioOn(heldItem);
                    radio.setRadioOn(heldItem, newState);
                }
            } else {
                player.addChatMessage(new ChatComponentTranslation("chat.portableradio.cooldown"));
            }
            return null;
        }
    }
}