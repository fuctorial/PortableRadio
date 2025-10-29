package ru.fuctorial.portableradio.common.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import ru.fuctorial.portableradio.PortableRadio;
import ru.fuctorial.portableradio.client.handler.KeybindHandler;
import ru.fuctorial.portableradio.common.handler.GuiHandler;
import ru.fuctorial.portableradio.common.network.PacketHandler;
import ru.fuctorial.portableradio.common.network.PacketTogglePower;

import java.util.List;

public class ItemWalkieTalkie extends Item {

    private static final String NBT_KEY_MAIN = PortableRadio.MODID;
    private static final String NBT_KEY_IS_ON = "isOn";
    private static final String NBT_KEY_FREQUENCY = "frequency";
    private static final String NBT_KEY_MIC_VOLUME = "micVolume";
    private static final String NBT_KEY_SPEAKER_VOLUME = "speakerVolume";

    public ItemWalkieTalkie() {
        super();
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        if (player.isSneaking()) {
            if (world.isRemote) {
                PacketHandler.INSTANCE.sendToServer(new PacketTogglePower());
            }
        } else {
            if (!world.isRemote) {
                player.openGui(PortableRadio.instance, GuiHandler.WALKIE_TALKIE_GUI_ID, world, (int)player.posX, (int)player.posY, (int)player.posZ);
            }
        }
        return itemStack;
    }

    @Override
    public void onCreated(ItemStack stack, World world, EntityPlayer player) {
        initNBT(stack);
    }

    private void initNBT(ItemStack stack) {
        if (stack.stackTagCompound == null) {
            stack.setTagCompound(new NBTTagCompound());
        }
        if (!stack.stackTagCompound.hasKey(NBT_KEY_MAIN)) {
            NBTTagCompound radioData = new NBTTagCompound();
            radioData.setBoolean(NBT_KEY_IS_ON, false);
            radioData.setInteger(NBT_KEY_FREQUENCY, 100);
            radioData.setFloat(NBT_KEY_MIC_VOLUME, 0.5f);
            radioData.setFloat(NBT_KEY_SPEAKER_VOLUME, 1.0f);
            stack.stackTagCompound.setTag(NBT_KEY_MAIN, radioData);
        }
    }

    private NBTTagCompound getRadioData(ItemStack stack) {
        initNBT(stack);
        return stack.stackTagCompound.getCompoundTag(NBT_KEY_MAIN);
    }

    private void saveRadioData(ItemStack stack, NBTTagCompound data) {
        if (stack.stackTagCompound == null) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.stackTagCompound.setTag(NBT_KEY_MAIN, data);
        stack.getItem();
    }

    public boolean isRadioOn(ItemStack stack) {
        return getRadioData(stack).getBoolean(NBT_KEY_IS_ON);
    }

    public void setRadioOn(ItemStack stack, boolean isOn) {
        NBTTagCompound data = getRadioData(stack);
        data.setBoolean(NBT_KEY_IS_ON, isOn);
        saveRadioData(stack, data);
    }

    public int getFrequency(ItemStack stack) {
        return getRadioData(stack).getInteger(NBT_KEY_FREQUENCY);
    }

    public void setFrequency(ItemStack stack, int frequency) {
        if (frequency >= 100 && frequency <= 500) {
            NBTTagCompound data = getRadioData(stack);
            data.setInteger(NBT_KEY_FREQUENCY, frequency);
            saveRadioData(stack, data);
        }
    }

    public float getMicrophoneVolume(ItemStack stack) {
        NBTTagCompound data = getRadioData(stack);
        if (!data.hasKey(NBT_KEY_MIC_VOLUME)) {
            data.setFloat(NBT_KEY_MIC_VOLUME, 0.5f);
            saveRadioData(stack, data);
            return 0.5f;
        }
        return data.getFloat(NBT_KEY_MIC_VOLUME);
    }

    public void setMicrophoneVolume(ItemStack stack, float volume) {
        NBTTagCompound data = getRadioData(stack);
        float clampedVolume = Math.max(0.0f, Math.min(2.0f, volume));
        data.setFloat(NBT_KEY_MIC_VOLUME, clampedVolume);
        saveRadioData(stack, data);

        if (stack.stackTagCompound != null) {
            stack.stackTagCompound.setTag(NBT_KEY_MAIN, data);
        }

        // System.out.println("[PortableRadio] ItemWalkieTalkie: Set mic volume to " + clampedVolume + " (" + (clampedVolume * 100) + "%)");
    }

    public float getSpeakerVolume(ItemStack stack) {
        NBTTagCompound data = getRadioData(stack);
        if (!data.hasKey(NBT_KEY_SPEAKER_VOLUME)) {
            data.setFloat(NBT_KEY_SPEAKER_VOLUME, 1.0f);
            saveRadioData(stack, data);
            return 1.0f;
        }
        return data.getFloat(NBT_KEY_SPEAKER_VOLUME);
    }

    public void setSpeakerVolume(ItemStack stack, float volume) {
        NBTTagCompound data = getRadioData(stack);
        float clampedVolume = Math.max(0.0f, Math.min(2.0f, volume));
        data.setFloat(NBT_KEY_SPEAKER_VOLUME, clampedVolume);
        saveRadioData(stack, data);

        if (stack.stackTagCompound != null) {
            stack.stackTagCompound.setTag(NBT_KEY_MAIN, data);
        }

        // System.out.println("[PortableRadio] ItemWalkieTalkie: Set speaker volume to " + clampedVolume + " (" + (clampedVolume * 100) + "%)");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        initNBT(stack);

        boolean isOn = isRadioOn(stack);
        String status = isOn ? EnumChatFormatting.GREEN + I18n.format("gui.portableradio.status.on")
                : EnumChatFormatting.RED + I18n.format("gui.portableradio.status.off");
        tooltip.add(I18n.format("gui.portableradio.status") + ": " + status);

        if (isOn) {
            tooltip.add(EnumChatFormatting.GRAY + I18n.format("gui.portableradio.frequency") + ": " + getFrequency(stack));
        }

        tooltip.add("");

        tooltip.add(EnumChatFormatting.YELLOW + I18n.format("tooltip.portableradio.toggle", EnumChatFormatting.WHITE + "Shift + ПКМ" + EnumChatFormatting.YELLOW));
        tooltip.add(EnumChatFormatting.YELLOW + I18n.format("tooltip.portableradio.open_gui", EnumChatFormatting.WHITE + "ПКМ" + EnumChatFormatting.YELLOW));

        String pttKeyName = Keyboard.getKeyName(KeybindHandler.pttKey.getKeyCode());
        tooltip.add(EnumChatFormatting.YELLOW + I18n.format("tooltip.portableradio.transmit", EnumChatFormatting.WHITE + pttKeyName + EnumChatFormatting.YELLOW));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        return false;
    }
}