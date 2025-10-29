package ru.fuctorial.portableradio.client.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

@SideOnly(Side.CLIENT)
public class TextureValidator {

    private static final int REQUIRED_SIZE = 256;

    /**
     * Проверяет размер текстуры
     * @param resourcePath путь к ресурсу (например, "/assets/portableradio/textures/gui/gui_walkie_talkie.png")
     * @return true если размер корректен
     */
    public static boolean validateTextureSize(String resourcePath) {
        try {
            InputStream stream = TextureValidator.class.getResourceAsStream(resourcePath);
            if (stream == null) {
                System.err.println("[PortableRadio] Texture not found: " + resourcePath);
                return false;
            }

            BufferedImage image = ImageIO.read(stream);
            stream.close();

            if (image.getWidth() != REQUIRED_SIZE || image.getHeight() != REQUIRED_SIZE) {
                System.err.println(String.format(
                        "[PortableRadio] WARNING: Texture %s has invalid size: %dx%d (expected %dx%d)",
                        resourcePath, image.getWidth(), image.getHeight(), REQUIRED_SIZE, REQUIRED_SIZE
                ));
                return false;
            }

            return true;
        } catch (Exception e) {
            System.err.println("[PortableRadio] Error validating texture: " + e.getMessage());
            return false;
        }
    }
}