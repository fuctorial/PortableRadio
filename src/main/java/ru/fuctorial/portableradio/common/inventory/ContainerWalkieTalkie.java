package ru.fuctorial.portableradio.common.inventory;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.ItemStack;
import ru.fuctorial.portableradio.client.gui.GuiWalkieTalkie;
import ru.fuctorial.portableradio.common.item.ItemWalkieTalkie;

public class ContainerWalkieTalkie extends Container {

    private final EntityPlayer player;
    private int radioSlotIndex = -1;
    private ItemWalkieTalkie radioItem;

    private int lastFrequency;
    private boolean lastIsOn;
    private int lastMicVolume;
    private int lastSpeakerVolume;

    private GuiWalkieTalkie gui;

    public ContainerWalkieTalkie(EntityPlayer player) {
        this.player = player;
        this.radioSlotIndex = findRadioSlotIndex(player);

        if (radioSlotIndex >= 0) {
            ItemStack radioStack = player.inventory.getStackInSlot(radioSlotIndex);
            if (radioStack != null && radioStack.getItem() instanceof ItemWalkieTalkie) {
                this.radioItem = (ItemWalkieTalkie) radioStack.getItem();
                this.lastFrequency = radioItem.getFrequency(radioStack);
                this.lastIsOn = radioItem.isRadioOn(radioStack);
                this.lastMicVolume = (int)(radioItem.getMicrophoneVolume(radioStack) * 100);
                this.lastSpeakerVolume = (int)(radioItem.getSpeakerVolume(radioStack) * 100);
            }
        }
    }

    private int findRadioSlotIndex(EntityPlayer player) {
        if (player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemWalkieTalkie) {
            return player.inventory.currentItem;
        }

        for (int i = 0; i < player.inventory.mainInventory.length; i++) {
            ItemStack stack = player.inventory.mainInventory[i];
            if (stack != null && stack.getItem() instanceof ItemWalkieTalkie) {
                return i;
            }
        }
        return -1;
    }

    private ItemStack getRadioStack() {
        if (radioSlotIndex < 0 || radioSlotIndex >= player.inventory.mainInventory.length) {
            return null;
        }
        return player.inventory.getStackInSlot(radioSlotIndex);
    }

    private void saveRadioStack(ItemStack stack) {
        if (radioSlotIndex >= 0 && radioSlotIndex < player.inventory.mainInventory.length) {
            player.inventory.setInventorySlotContents(radioSlotIndex, stack);
            player.inventory.markDirty();
            // System.out.println("[PortableRadio] Container: Saved ItemStack to slot " + radioSlotIndex);
        }
    }

    public void setGui(GuiWalkieTalkie gui) {
        this.gui = gui;
    }

    public void updateMicrophoneVolume(int volume) {
        if (this.radioItem != null && radioSlotIndex >= 0) {
            ItemStack radioStack = player.inventory.getStackInSlot(radioSlotIndex);

            if (radioStack != null) {
                float normalizedVolume = volume / 100.0f;
                this.radioItem.setMicrophoneVolume(radioStack, normalizedVolume);

                player.inventory.markDirty();

                if (!player.worldObj.isRemote) {
                    detectAndSendChanges();
                }

                // System.out.println("[PortableRadio] Container: Updated mic volume to " + volume + "%, normalized: " + normalizedVolume);
                // System.out.println("[PortableRadio] NBT after update: " + radioStack.stackTagCompound);
            }
        }
    }

    public void updateSpeakerVolume(int volume) {
        if (this.radioItem != null && radioSlotIndex >= 0) {
            ItemStack radioStack = player.inventory.getStackInSlot(radioSlotIndex);

            if (radioStack != null) {
                float normalizedVolume = volume / 100.0f;
                this.radioItem.setSpeakerVolume(radioStack, normalizedVolume);

                player.inventory.markDirty();

                if (!player.worldObj.isRemote) {
                    detectAndSendChanges();
                }

                // System.out.println("[PortableRadio] Container: Updated speaker volume to " + volume + "%, normalized: " + normalizedVolume);
                // System.out.println("[PortableRadio] NBT after update: " + radioStack.stackTagCompound);
            }
        }
    }

    public int getSelectedMicrophoneIndex() {
        return 0;
    }

    public int getSelectedSpeakerIndex() {
        return 0;
    }

    public int getFrequency() {
        ItemStack radioStack = getRadioStack();
        if (this.radioItem != null && radioStack != null) {
            return this.radioItem.getFrequency(radioStack);
        }
        return 100;
    }

    public float getMicrophoneVolume() {
        ItemStack radioStack = getRadioStack();
        if (this.radioItem != null && radioStack != null) {
            float volume = this.radioItem.getMicrophoneVolume(radioStack) * 100.0f;
            return volume;
        }
        return 50.0f;
    }

    public float getSpeakerVolume() {
        ItemStack radioStack = getRadioStack();
        if (this.radioItem != null && radioStack != null) {
            float volume = this.radioItem.getSpeakerVolume(radioStack) * 100.0f;
            return volume;
        }
        return 100.0f;
    }

    public void updateFrequency(int frequency) {
        ItemStack radioStack = getRadioStack();
        if (this.radioItem != null && radioStack != null) {
            ItemStack modifiedStack = radioStack.copy();
            this.radioItem.setFrequency(modifiedStack, frequency);
            saveRadioStack(modifiedStack);
            // System.out.println("[PortableRadio] Container: Updated frequency to " + frequency);
        }
    }

    @Override
    public void addCraftingToCrafters(ICrafting crafter) {
        super.addCraftingToCrafters(crafter);
        ItemStack radioStack = getRadioStack();
        if (this.radioItem != null && radioStack != null) {
            crafter.sendProgressBarUpdate(this, 0, this.radioItem.getFrequency(radioStack));
            crafter.sendProgressBarUpdate(this, 1, this.radioItem.isRadioOn(radioStack) ? 1 : 0);
            crafter.sendProgressBarUpdate(this, 2, (int)(this.radioItem.getMicrophoneVolume(radioStack) * 100));
            crafter.sendProgressBarUpdate(this, 3, (int)(this.radioItem.getSpeakerVolume(radioStack) * 100));
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        ItemStack radioStack = getRadioStack();
        if (this.radioItem == null || radioStack == null) return;

        int currentFrequency = radioItem.getFrequency(radioStack);
        boolean currentIsOn = radioItem.isRadioOn(radioStack);
        int currentMicVolume = (int)(radioItem.getMicrophoneVolume(radioStack) * 100);
        int currentSpeakerVolume = (int)(radioItem.getSpeakerVolume(radioStack) * 100);

        for (Object crafterObj : this.crafters) {
            ICrafting crafter = (ICrafting) crafterObj;
            if (this.lastFrequency != currentFrequency) {
                crafter.sendProgressBarUpdate(this, 0, currentFrequency);
            }
            if (this.lastIsOn != currentIsOn) {
                crafter.sendProgressBarUpdate(this, 1, currentIsOn ? 1 : 0);
            }
            if (this.lastMicVolume != currentMicVolume) {
                crafter.sendProgressBarUpdate(this, 2, currentMicVolume);
            }
            if (this.lastSpeakerVolume != currentSpeakerVolume) {
                crafter.sendProgressBarUpdate(this, 3, currentSpeakerVolume);
            }
        }
        this.lastFrequency = currentFrequency;
        this.lastIsOn = currentIsOn;
        this.lastMicVolume = currentMicVolume;
        this.lastSpeakerVolume = currentSpeakerVolume;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void updateProgressBar(int id, int data) {
        if (this.gui != null) {
            if (id == 0) {
                this.gui.updateFrequency(data);
            } else if (id == 1) {
                this.gui.updateStatus(data == 1);
            } else if (id == 2) {
                this.gui.updateMicrophoneVolume(data);
            } else if (id == 3) {
                this.gui.updateSpeakerVolume(data);
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return getRadioStack() != null;
    }

    public void handleGuiPacket(int buttonId) {
        // заглушка
    }
}