package ru.fuctorial.portableradio.common.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import ru.fuctorial.portableradio.PortableRadio;
import ru.fuctorial.portableradio.common.item.ItemWalkieTalkie;

public class ItemTossHandler {

    @SubscribeEvent
    public void onItemToss(ItemTossEvent event) {
        ItemStack tossedStack = event.entityItem.getEntityItem();

        if (tossedStack != null && tossedStack.getItem() instanceof ItemWalkieTalkie) {

            if (!event.player.worldObj.isRemote) {
                ItemWalkieTalkie radioItem = (ItemWalkieTalkie) tossedStack.getItem();

                if (radioItem.isRadioOn(tossedStack)) {
                    radioItem.setRadioOn(tossedStack, false);
                    // PortableRadio.debug("Radio tossed by " + event.player.getDisplayName() + " was turned off.");
                }
            }
        }
    }
}