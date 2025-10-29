package ru.fuctorial.portableradio.common.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
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
            sender.addChatMessage(new ChatComponentText("§cThis command can only be used by players!"));
            return;
        }

        EntityPlayer player = (EntityPlayer) sender;
        ItemStack held = player.getHeldItem();

        if (held == null || !(held.getItem() instanceof ItemWalkieTalkie)) {
            player.addChatMessage(new ChatComponentText("§cYou must hold a walkie-talkie!"));
            return;
        }

        ItemWalkieTalkie radio = (ItemWalkieTalkie) held.getItem();

        player.addChatMessage(new ChatComponentText("§a=== Radio NBT Debug ==="));
        player.addChatMessage(new ChatComponentText("§eRadio ON: §f" + radio.isRadioOn(held)));
        player.addChatMessage(new ChatComponentText("§eFrequency: §f" + radio.getFrequency(held)));
        player.addChatMessage(new ChatComponentText("§eMic Volume: §f" + (radio.getMicrophoneVolume(held) * 100) + "%"));
        player.addChatMessage(new ChatComponentText("§eSpeaker Volume: §f" + (radio.getSpeakerVolume(held) * 100) + "%"));


        if (held.stackTagCompound != null) {
            player.addChatMessage(new ChatComponentText("§7Raw NBT: " + held.stackTagCompound.toString()));
        } else {
            player.addChatMessage(new ChatComponentText("§cNo NBT data found!"));
        }
    }
}



/*
@Mod.EventHandler
public void serverStarting(FMLServerStartingEvent event) {
    event.registerServerCommand(new RadioDebugCommand());
}
*/