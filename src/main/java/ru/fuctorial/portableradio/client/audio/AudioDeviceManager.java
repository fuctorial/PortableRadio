package ru.fuctorial.portableradio.client.audio;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import javax.sound.sampled.*;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public enum AudioDeviceManager {
    INSTANCE;

    private final List<String> realMicNames = new ArrayList<>();
    private final List<String> displayMicNames = new ArrayList<>();
    private final List<String> realSpeakerNames = new ArrayList<>();
    private final List<String> displaySpeakerNames = new ArrayList<>();


    private boolean devicesScanned = false;


    private void ensureDevicesScanned() {
        if (!devicesScanned) {
            scanDevices();
            devicesScanned = true;
        }
    }

    private void scanDevices() {
        realMicNames.clear();
        displayMicNames.clear();
        realSpeakerNames.clear();
        displaySpeakerNames.clear();
        realMicNames.add("");
        displayMicNames.add("Default");
        realSpeakerNames.add("");
        displaySpeakerNames.add("Default");
        try {
            Mixer.Info[] mixers = AudioSystem.getMixerInfo();
            for (Mixer.Info mixerInfo : mixers) {
                Mixer mixer = AudioSystem.getMixer(mixerInfo);
                String originalName = mixerInfo.getName();
                String displayName = fixDisplayEncoding(originalName);
                if (mixer.isLineSupported(new Line.Info(TargetDataLine.class))) {
                    realMicNames.add(originalName);
                    displayMicNames.add(displayName);
                }
                if (mixer.isLineSupported(new Line.Info(SourceDataLine.class))) {
                    realSpeakerNames.add(originalName);
                    displaySpeakerNames.add(displayName);
                }
            }
        } catch (Exception e) {
            System.err.println("[PortableRadio] Error scanning audio devices: " + e.getMessage());
        }
    }

    private String fixDisplayEncoding(String originalName) {
        try {
            byte[] rawBytes = originalName.getBytes(StandardCharsets.ISO_8859_1);
            return new String(rawBytes, "Windows-1251");
        } catch (UnsupportedEncodingException e) {
            return originalName;
        }
    }

    public List<String> getDisplayMicNames() {
        ensureDevicesScanned();
        return Collections.unmodifiableList(displayMicNames);
    }
    public List<String> getDisplaySpeakerNames() {
        ensureDevicesScanned();
        return Collections.unmodifiableList(displaySpeakerNames);
    }
    public String getRealMicName(int index) {
        ensureDevicesScanned();
        if (index >= 0 && index < realMicNames.size()) {
            return realMicNames.get(index);
        }
        return "";
    }
    public String getRealSpeakerName(int index) {
        ensureDevicesScanned();
        if (index >= 0 && index < realSpeakerNames.size()) {
            return realSpeakerNames.get(index);
        }
        return "";
    }
}