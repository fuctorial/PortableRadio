package ru.fuctorial.portableradio.common.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import ru.fuctorial.portableradio.common.item.ItemWalkieTalkie;

public class RadioDebugCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "radiodebug";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/radiodebug - Shows NBT data of radio in hand";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (!(sender instanceof EntityPlayer)) {
            sender.addChatMessage(new ChatComponentTranslation("command.portableradio.player_only"));
            return;
        }

        EntityPlayer player = (EntityPlayer) sender;
        ItemStack held = player.getHeldItem();

        if (held == null || !(held.getItem() instanceof ItemWalkieTalkie)) {
            player.addChatMessage(new ChatComponentTranslation("command.portableradio.hold_radio"));
            return;
        }

        ItemWalkieTalkie radio = (ItemWalkieTalkie) held.getItem();

        player.addChatMessage(new ChatComponentTranslation("command.portableradio.debug_title"));
        player.addChatMessage(new ChatComponentTranslation("command.portableradio.radio_on", radio.isRadioOn(held)));
        player.addChatMessage(new ChatComponentTranslation("command.portableradio.frequency", radio.getFrequency(held)));
        player.addChatMessage(new ChatComponentTranslation("command.portableradio.mic_volume", (radio.getMicrophoneVolume(held) * 100)));
        player.addChatMessage(new ChatComponentTranslation("command.portableradio.speaker_volume", (radio.getSpeakerVolume(held) * 100)));


        if (held.stackTagCompound != null) {
            player.addChatMessage(new ChatComponentTranslation("command.portableradio.raw_nbt", held.stackTagCompound.toString()));
        } else {
            player.addChatMessage(new ChatComponentTranslation("command.portableradio.no_nbt_data"));
        }
    }
}




