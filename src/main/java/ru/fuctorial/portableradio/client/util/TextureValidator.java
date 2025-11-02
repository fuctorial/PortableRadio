package ru.fuctorial.portableradio.client.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ru.fuctorial.portableradio.PortableRadio;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

@SideOnly(Side.CLIENT)
public class TextureValidator {

    private static final int REQUIRED_SIZE = 256;

    
    public static boolean validateTextureSize(String resourcePath) {
        try {
            InputStream stream = TextureValidator.class.getResourceAsStream(resourcePath);
            if (stream == null) {
                PortableRadio.logger.warn("Texture not found: " + resourcePath);
                return false;
            }

            BufferedImage image = ImageIO.read(stream);
            stream.close();

            if (image.getWidth() != REQUIRED_SIZE || image.getHeight() != REQUIRED_SIZE) {
                PortableRadio.logger.warn(String.format(
                        "WARNING: Texture %s has invalid size: %dx%d (expected %dx%d)",
                        resourcePath, image.getWidth(), image.getHeight(), REQUIRED_SIZE, REQUIRED_SIZE
                ));
                return false;
            }

            return true;
        } catch (Exception e) {
            PortableRadio.logger.error("Error validating texture: " + e.getMessage());
            return false;
        }
    }
}