

package ru.fuctorial.portableradio.client.audio;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentTranslation;
import ru.fuctorial.portableradio.PortableRadio;
import ru.fuctorial.portableradio.common.audio.AudioConfig;
import ru.fuctorial.portableradio.common.network.PacketHandler;
import ru.fuctorial.portableradio.common.network.PacketVoiceData;
import tomp2p.opuswrapper.Opus;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
                } catch (Exception e) {  }
            }
        }
    }

    
    private static final float[] OPUS_SUPPORTED_RATES = { 48000.0F, 24000.0F, 16000.0F, 12000.0F, 8000.0F };

    private volatile boolean running = true;
    private final String selectedMicName;
    private final float microphoneVolume;
    private static final float PRE_AMPLIFICATION_FACTOR = 3.0f;
    private OpusEncoder encoder;

    public AudioCaptureThread(String micName, float volume) {
        this.selectedMicName = micName;
        this.microphoneVolume = Math.max(0.0f, Math.min(2.0f, volume));
        this.setName("AudioCaptureThread-Opus");
        this.setDaemon(true);
    }

    @Override
    public void run() {
        TargetDataLine microphone = null;
        try {
            
            
            encoder = new OpusEncoder((int)AudioConfig.SAMPLE_RATE, AudioConfig.CHANNELS, Opus.OPUS_APPLICATION_VOIP);

            
            AudioFormat captureFormat = findBestSupportedFormat(selectedMicName);
            if (captureFormat == null) {
                throw new LineUnavailableException("Microphone does not support any Opus-compatible sample rates.");
            }

            
            microphone = getMicrophoneLine(selectedMicName, captureFormat);
            if (microphone == null) {
                throw new LineUnavailableException("Could not get a line for the selected microphone.");
            }

            
            final int captureFrameSizeSamples = (int) (captureFormat.getSampleRate() * 0.020); 
            final int captureBufferSize = captureFrameSizeSamples * (captureFormat.getSampleSizeInBits() / 8);

            microphone.open(captureFormat, captureBufferSize * 2); 
            microphone.start();

            PortableRadio.logger.info("Audio capture started on device: '" + (selectedMicName == null ? "Default" : selectedMicName) + "' with sample rate: " + (int)captureFormat.getSampleRate() + " Hz");

            byte[] pcmBytes = new byte[captureBufferSize];
            short[] pcmShorts = new short[captureFrameSizeSamples];

            while (running) {
                int bytesRead = microphone.read(pcmBytes, 0, pcmBytes.length);

                if (bytesRead > 0) {
                    byte[] amplifiedData = applyMicrophoneVolume(pcmBytes, bytesRead, microphoneVolume);
                    ByteBuffer.wrap(amplifiedData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(pcmShorts);

                    
                    
                    byte[] opusData = encoder.encode(pcmShorts, captureFrameSizeSamples);

                    if (opusData.length > 0) {
                        PacketHandler.INSTANCE.sendToServer(new PacketVoiceData(opusData));
                    }
                }
            }
        } catch (Throwable e) {
            PortableRadio.logger.error("FATAL error during audio capture", e);
            showChatMessage("chat.portableradio.microphone_unavailable");
        } finally {
            if (encoder != null) {
                encoder.close();
            }
            if (microphone != null) {
                microphone.stop();
                microphone.close();
                PortableRadio.logger.info("Audio capture stopped.");
            }
        }
    }

    
    private AudioFormat findBestSupportedFormat(String micName) {
        Mixer.Info mixerInfo = findMixerInfo(micName);
        if (micName != null && mixerInfo == null) {
            PortableRadio.logger.warn("Could not find mixer for device name: " + micName);
            return null; 
        }

        for (float rate : OPUS_SUPPORTED_RATES) {
            AudioFormat format = new AudioFormat(rate, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (mixerInfo == null) { 
                if (AudioSystem.isLineSupported(info)) {
                    return format;
                }
            } else { 
                Mixer mixer = AudioSystem.getMixer(mixerInfo);
                if (mixer.isLineSupported(info)) {
                    return format;
                }
            }
        }
        return null; 
    }

    
    private TargetDataLine getMicrophoneLine(String micName, AudioFormat format) throws LineUnavailableException {
        if (micName == null) {
            return AudioSystem.getTargetDataLine(format);
        }
        Mixer.Info mixerInfo = findMixerInfo(micName);
        if (mixerInfo != null) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            return (TargetDataLine) mixer.getLine(new DataLine.Info(TargetDataLine.class, format));
        }
        return null;
    }

    
    private Mixer.Info findMixerInfo(String name) {
        if (name == null) return null;
        for (Mixer.Info info : AudioSystem.getMixerInfo()) {
            if (name.equals(info.getName())) {
                return info;
            }
        }
        return null;
    }

    
    private byte[] applyMicrophoneVolume(byte[] inputData, int length, float volume) {
        byte[] outputData = new byte[length];
        for (int i = 0; i < length; i += 2) {
            if (i + 1 < length) {
                short sample = (short) ((inputData[i + 1] << 8) | (inputData[i] & 0xFF));
                float amplified = sample * PRE_AMPLIFICATION_FACTOR * volume;
                if (amplified > 32767.0f) amplified = 32767.0f;
                if (amplified < -32768.0f) amplified = -32768.0f;
                short outputSample = (short) amplified;
                outputData[i] = (byte) (outputSample & 0xFF);
                outputData[i + 1] = (byte) ((outputSample >> 8) & 0xFF);
            }
        }
        return outputData;
    }

    private void showChatMessage(final String key) {
        clientThreadTasks.add(() -> {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer != null) mc.thePlayer.addChatMessage(new ChatComponentTranslation(key));
        });
    }

    public void stopCapture() {
        this.running = false;
    }
}