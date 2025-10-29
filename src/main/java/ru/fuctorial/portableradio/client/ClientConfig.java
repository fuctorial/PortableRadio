package ru.fuctorial.portableradio.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.config.Configuration;
import java.io.File;

@SideOnly(Side.CLIENT)
public class ClientConfig {

    private static Configuration config;

    private static final String CATEGORY_AUDIO_DEVICES = "audio_devices";
    private static final String KEY_MIC_INDEX = "selectedMicrophoneIndex";
    private static final String KEY_SPEAKER_INDEX = "selectedSpeakerIndex";

    private static int micIndex = 0; // 0 = "Default"
    private static int speakerIndex = 0; // 0 = "Default"

    public static void init(File suggestedConfigFile) {
        config = new Configuration(suggestedConfigFile);
        syncConfig();
    }

    public static void syncConfig() {
        config.load();

        micIndex = config.getInt(KEY_MIC_INDEX, CATEGORY_AUDIO_DEVICES, 0, 0, 100,
                "The index of the selected microphone. 0 is Default.");
        speakerIndex = config.getInt(KEY_SPEAKER_INDEX, CATEGORY_AUDIO_DEVICES, 0, 0, 100,
                "The index of the selected speaker. 0 is Default.");

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static int getSelectedMicIndex() {
        return micIndex;
    }

    public static void setSelectedMicIndex(int index) {
        micIndex = index;
        config.get(CATEGORY_AUDIO_DEVICES, KEY_MIC_INDEX, 0).set(index);
        config.save();
    }

    public static int getSelectedSpeakerIndex() {
        return speakerIndex;
    }

    public static void setSelectedSpeakerIndex(int index) {
        speakerIndex = index;
        config.get(CATEGORY_AUDIO_DEVICES, KEY_SPEAKER_INDEX, 0).set(index);
        config.save();
    }
}