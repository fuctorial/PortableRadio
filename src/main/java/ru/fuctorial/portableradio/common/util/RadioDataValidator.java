package ru.fuctorial.portableradio.common.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import ru.fuctorial.portableradio.PortableRadio;
import ru.fuctorial.portableradio.common.item.ItemWalkieTalkie;

public class RadioDataValidator {

    private static final String NBT_KEY_MAIN = PortableRadio.MODID;
    private static final int MIN_FREQUENCY = 100;
    private static final int MAX_FREQUENCY = 500;
    private static final float MIN_VOLUME = 0.0f;
    private static final float MAX_VOLUME = 2.0f;
    private static final int MAX_DEVICE_INDEX = 100;

    public static boolean validateAndFix(ItemStack stack) {
        if (stack == null || stack.stackTagCompound == null) return false;

        NBTTagCompound nbt = stack.stackTagCompound;
        if (!nbt.hasKey(NBT_KEY_MAIN)) return false;

        NBTTagCompound radioData = nbt.getCompoundTag(NBT_KEY_MAIN);
        boolean wasFixed = false;

        if (radioData.hasKey("frequency")) {
            int freq = radioData.getInteger("frequency");
            if (freq < MIN_FREQUENCY || freq > MAX_FREQUENCY) {
                radioData.setInteger("frequency", 100);
                wasFixed = true;
                PortableRadio.logger.warn("SECURITY: Invalid frequency detected and fixed");
            }
        }

        if (radioData.hasKey("micVolume")) {
            float vol = radioData.getFloat("micVolume");
            if (vol < MIN_VOLUME || vol > MAX_VOLUME || Float.isNaN(vol) || Float.isInfinite(vol)) {
                radioData.setFloat("micVolume", 0.5f);
                wasFixed = true;
                PortableRadio.logger.warn("SECURITY: Invalid mic volume detected and fixed");
            }
        }

        if (radioData.hasKey("speakerVolume")) {
            float vol = radioData.getFloat("speakerVolume");
            if (vol < MIN_VOLUME || vol > MAX_VOLUME || Float.isNaN(vol) || Float.isInfinite(vol)) {
                radioData.setFloat("speakerVolume", 1.0f);
                wasFixed = true;
                PortableRadio.logger.warn("SECURITY: Invalid speaker volume detected and fixed");
            }
        }

        if (radioData.hasKey("selectedMicIndex")) {
            int idx = radioData.getInteger("selectedMicIndex");
            if (idx < 0 || idx > MAX_DEVICE_INDEX) {
                radioData.setInteger("selectedMicIndex", 0);
                wasFixed = true;
                PortableRadio.logger.warn("SECURITY: Invalid mic index detected and fixed");
            }
        }

        if (radioData.hasKey("selectedSpeakerIndex")) {
            int idx = radioData.getInteger("selectedSpeakerIndex");
            if (idx < 0 || idx > MAX_DEVICE_INDEX) {
                radioData.setInteger("selectedSpeakerIndex", 0);
                wasFixed = true;
                PortableRadio.logger.warn("SECURITY: Invalid speaker index detected and fixed");
            }
        }

        if (wasFixed) {
            nbt.setTag(NBT_KEY_MAIN, radioData);
        }

        return true;
    }

    public static boolean canPlayerUseRadio(EntityPlayer player, ItemStack stack) {
        if (player == null || stack == null) return false;
        if (!validateAndFix(stack)) return false;

        boolean foundInInventory = false;
        for (ItemStack invStack : player.inventory.mainInventory) {
            if (invStack == stack) {
                foundInInventory = true;
                break;
            }
        }

        if (!foundInInventory) {
            PortableRadio.logger.warn("SECURITY: Player " + player.getDisplayName() + " tried to use radio not in inventory");
            return false;
        }

        if (stack.getItem() instanceof ItemWalkieTalkie) {
            ItemWalkieTalkie radio = (ItemWalkieTalkie) stack.getItem();
            if (!radio.isRadioOn(stack)) {
                return false;
            }
        }

        return true;
    }
}