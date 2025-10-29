package ru.fuctorial.portableradio.server.radio;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import ru.fuctorial.portableradio.common.item.ItemWalkieTalkie;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public enum RadioManager {
    INSTANCE;
    private final Set<UUID> transmittingPlayers = new HashSet<>();

    public void startTransmitting(EntityPlayer player) {
        if (canPlayerTransmit(player)) {
            transmittingPlayers.add(player.getUniqueID());
            // System.out.println("Player " + player.getDisplayName() + " started transmitting.");
        }
    }

    public void stopTransmitting(EntityPlayer player) {
        if (transmittingPlayers.remove(player.getUniqueID())) {
            // System.out.println("Player " + player.getDisplayName() + " stopped transmitting.");
        }
    }

    public boolean isPlayerTransmitting(EntityPlayer player) {
        return transmittingPlayers.contains(player.getUniqueID());
    }

    private boolean canPlayerTransmit(EntityPlayer player) {
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack != null && stack.getItem() instanceof ItemWalkieTalkie) {
                ItemWalkieTalkie radio = (ItemWalkieTalkie) stack.getItem();
                if (radio.isRadioOn(stack)) {
                    return true;
                }
            }
        }
        return false;
    }
}