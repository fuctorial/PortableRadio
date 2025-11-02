package ru.fuctorial.portableradio.common.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import ru.fuctorial.portableradio.PortableRadio;
import ru.fuctorial.portableradio.common.item.ItemWalkieTalkie;
import ru.fuctorial.portableradio.common.util.RadioDataValidator;
import java.util.List;

public class PacketVoiceDataServerHandler implements IMessageHandler<PacketVoiceData, IMessage> {
    @Override
    public IMessage onMessage(PacketVoiceData message, MessageContext ctx) {
        EntityPlayerMP sender = ctx.getServerHandler().playerEntity;

        ItemStack senderRadio = findActiveRadio(sender);
        if (senderRadio == null) {
            
            return null;
        }

        if (!RadioDataValidator.canPlayerUseRadio(sender, senderRadio)) {
            
            return null;
        }

        ItemWalkieTalkie radioItem = (ItemWalkieTalkie) senderRadio.getItem();
        int senderFrequency = radioItem.getFrequency(senderRadio);

        List<EntityPlayerMP> players = sender.worldObj.playerEntities;

        for (EntityPlayerMP receiver : players) {
            if (receiver.equals(sender)) {
                continue;
            }
            if (receiver.playerNetServerHandler == null || !receiver.playerNetServerHandler.netManager.isChannelOpen()) {
                continue;
            }

            ItemStack receiverRadio = findActiveRadio(receiver);
            if (receiverRadio == null) {
                continue;
            }

            if (!RadioDataValidator.canPlayerUseRadio(receiver, receiverRadio)) {
                continue;
            }

            ItemWalkieTalkie receiverRadioItem = (ItemWalkieTalkie) receiverRadio.getItem();
            int receiverFrequency = receiverRadioItem.getFrequency(receiverRadio);

            if (receiverFrequency == senderFrequency) {
                PacketHandler.INSTANCE.sendTo(message, receiver);
            }
        }
        return null;
    }

    private ItemStack findActiveRadio(EntityPlayer player) {
        if (player == null || player.inventory == null) return null;
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack != null && stack.getItem() instanceof ItemWalkieTalkie) {
                if (((ItemWalkieTalkie) stack.getItem()).isRadioOn(stack)) {
                    return stack;
                }
            }
        }
        return null;
    }
}
