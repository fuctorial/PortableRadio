package ru.fuctorial.portableradio.common.item;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import ru.fuctorial.portableradio.PortableRadio;

public class ModItems {

    public static Item walkieTalkie;
    private static boolean isInitialized = false;

    public static void init() {
        if (isInitialized) return;

        walkieTalkie = new ItemWalkieTalkie();
        walkieTalkie.setUnlocalizedName("walkieTalkie");
        walkieTalkie.setMaxStackSize(1);
        walkieTalkie.setCreativeTab(CreativeTabs.tabRedstone);

        GameRegistry.registerItem(walkieTalkie, "walkieTalkie");
        System.out.println("[PortableRadio] Item registered successfully!");

        isInitialized = true;
    }

    @SideOnly(Side.CLIENT)
    public static void registerRenders() {
        walkieTalkie.setTextureName(PortableRadio.MODID + ":walkie_talkie");
    }
}