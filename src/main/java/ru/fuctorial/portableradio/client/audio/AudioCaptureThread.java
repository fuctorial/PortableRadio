package ru.fuctorial.portableradio.client.audio;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import ru.fuctorial.portableradio.PortableRadio;
import ru.fuctorial.portableradio.common.audio.AudioConfig;
import ru.fuctorial.portableradio.common.audio.RadioEffectProcessor;
import ru.fuctorial.portableradio.common.network.PacketHandler;
import ru.fuctorial.portableradio.common.network.PacketVoiceData;

import javax.sound.sampled.*;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

@SideOnly(Side.CLIENT)
public class AudioCaptureThread extends Thread {

    private static final ConcurrentLinkedQueue<Runnable> clientThreadTasks = new ConcurrentLinkedQueue<>();

    public static void processClientThreadTasks() {
        while (!clientThreadTasks.isEmpty()) {
            Runnable task = clientThreadTasks.poll();
            if (task != null) {
                try {
                    task.run();
                } catch (Exception e) {
                    // System.err.println("[PortableRadio] Error executing client task: " + e.getMessage());
                    // e.printStackTrace();
                }
            }
        }
    }

    private volatile boolean running = true;
    private TargetDataLine microphone;
    private String selectedMicName;
    private float microphoneVolume;

    private static final float ACTIVATION_THRESHOLD = 0.015f;

    private static final int RELEASE_TIME_CHUNKS = 10;

    private static final int ACTIVATION_DELAY_CHUNKS = 2;

    private boolean isGateOpen = false;
    private int consecutiveSilentChunks = 0;
    private int consecutiveLoudChunks = 0;
    private final byte[] silentBuffer = new byte[AudioConfig.BUFFER_SIZE];

    public AudioCaptureThread(String micName, float volume) {
        this.selectedMicName = micName;
        this.microphoneVolume = Math.max(0.0f, Math.min(2.0f, volume));
        this.setName("AudioCaptureThread");
        this.setDaemon(true);
        Arrays.fill(silentBuffer, (byte) 0);
    }

    @Override
    public void run() {
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, AudioConfig.getAudioFormat());

            if (selectedMicName != null && !selectedMicName.isEmpty()) {
                microphone = findMicrophoneByName(selectedMicName);
                if (microphone == null) {
                    PortableRadio.logger.error("Microphone not found: " + selectedMicName);
                    showChatMessage("chat.portableradio.microphone_not_found", selectedMicName); // ИСПРАВЛЕНИЕ: I18n
                    return;
                }
            } else {
                microphone = (TargetDataLine) AudioSystem.getLine(info);
            }

            microphone.open(AudioConfig.getAudioFormat());
            microphone.start();
            PortableRadio.debug("Microphone capture started: " + selectedMicName + " at " + (microphoneVolume * 100) + "% volume");

            byte[] buffer = new byte[AudioConfig.BUFFER_SIZE];
            RadioEffectProcessor effectProcessor = new RadioEffectProcessor();

            while (running) {
                int bytesRead = microphone.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    byte[] amplifiedData = applyNormalizedMicrophoneVolume(buffer, bytesRead, microphoneVolume);
                    boolean shouldTransmit = processVoiceActivity(amplifiedData, bytesRead);
                    if (shouldTransmit) {
                        byte[] processedData = effectProcessor.applyRadioEffect(amplifiedData, bytesRead);
                        PacketHandler.INSTANCE.sendToServer(new PacketVoiceData(processedData));
                    }
                }
            }

        } catch (LineUnavailableException e) {
            PortableRadio.logger.error("Microphone unavailable: " + e.getMessage());
            showChatMessage("chat.portableradio.microphone_unavailable"); // ИСПРАВЛЕНИЕ: I18n
        } catch (Exception e) {
            PortableRadio.logger.error("Error during audio capture", e);
        } finally {
            if (microphone != null) {
                microphone.stop();
                microphone.close();
                PortableRadio.debug("Microphone capture stopped.");
            }
        }
    }

    private boolean processVoiceActivity(byte[] audioData, int length) {
        float rms = (float) calculateRMS(audioData, length);

        if (rms > ACTIVATION_THRESHOLD) {
            consecutiveLoudChunks++;
            consecutiveSilentChunks = 0;
            if (consecutiveLoudChunks >= ACTIVATION_DELAY_CHUNKS) {
                isGateOpen = true;
            }
        } else {
            consecutiveSilentChunks++;
            consecutiveLoudChunks = 0;
            if (consecutiveSilentChunks >= RELEASE_TIME_CHUNKS) {
                isGateOpen = false;
            }
        }

        return isGateOpen;
    }

    private byte[] applyNormalizedMicrophoneVolume(byte[] inputData, int length, float volumePercent) {
        byte[] outputData = new byte[length];
        float baseAmplifier = 10.0f;
        float totalGain = baseAmplifier * volumePercent;
        for (int i = 0; i < length; i += 2) {
            if (i + 1 < length) {
                short sample = (short) ((inputData[i + 1] << 8) | (inputData[i] & 0xFF));
                float amplified = sample * totalGain;
                if (amplified > 32767) amplified = 32767;
                if (amplified < -32768) amplified = -32768;
                short outputSample = (short) amplified;
                outputData[i] = (byte) (outputSample & 0xFF);
                outputData[i + 1] = (byte) ((outputSample >> 8) & 0xFF);
            }
        }
        return outputData;
    }

    private byte[] applyMicrophoneVolume(byte[] inputData, int length, float volume) {
        byte[] outputData = new byte[length];

        for (int i = 0; i < length; i += 2) {
            if (i + 1 < length) {
                short sample = (short) ((inputData[i + 1] << 8) | (inputData[i] & 0xFF));
                float amplified = sample * volume;

                if (amplified > 32767) {
                    amplified = 32767.0f * (1.0f - (float)Math.exp(-(amplified - 32767) / 8192.0f));
                } else if (amplified < -32768) {
                    amplified = -32768.0f * (1.0f - (float)Math.exp(-(-amplified - 32768) / 8192.0f));
                }

                short outputSample = (short) Math.max(-32768, Math.min(32767, amplified));
                outputData[i] = (byte) (outputSample & 0xFF);
                outputData[i + 1] = (byte) ((outputSample >> 8) & 0xFF);
            }
        }

        return outputData;
    }

    private TargetDataLine findMicrophoneByName(String name) throws LineUnavailableException {
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo : mixers) {
            if (mixerInfo.getName().equals(name)) {
                Mixer mixer = AudioSystem.getMixer(mixerInfo);

                float[] sampleRates = {44100.0F, 48000.0F, 16000.0F, 22050.0F};

                for (float rate : sampleRates) {
                    AudioFormat format = new AudioFormat(rate, 16, 1, true, false);
                    DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

                    if (mixer.isLineSupported(info)) {
                        // System.out.println("[PortableRadio] Using format: " + rate + " Hz");
                        return (TargetDataLine) mixer.getLine(info);
                    }
                }
            }
        }
        return null;
    }

    private double calculateRMS(byte[] audioData, int length) {
        long sum = 0;
        for (int i = 0; i < length; i += 2) {
            if (i + 1 < length) {
                short sample = (short) ((audioData[i + 1] << 8) | (audioData[i] & 0xFF));
                sum += sample * sample;
            }
        }
        if (length < 2) return 0.0;
        int sampleCount = length / 2;
        double meanSquare = (double) sum / sampleCount;
        return Math.sqrt(meanSquare) / 32768.0;
    }

    private void showChatMessage(final String message) {
        clientThreadTasks.add(new Runnable() {
            @Override
            public void run() {
                Minecraft mc = Minecraft.getMinecraft();
                if (mc.thePlayer != null) {
                    mc.thePlayer.addChatMessage(new ChatComponentText(message));
                }
            }
        });
    }
    private void showChatMessage(final String key, final Object... args) {
        clientThreadTasks.add(new Runnable() {
            @Override
            public void run() {
                Minecraft mc = Minecraft.getMinecraft();
                if (mc.thePlayer != null) {
                    mc.thePlayer.addChatMessage(new ChatComponentTranslation(key, args));
                }
            }
        });
    }

    public void stopCapture() {
        this.running = false;
    }
}