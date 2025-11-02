package ru.fuctorial.portableradio.client.audio;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import ru.fuctorial.portableradio.PortableRadio;
import ru.fuctorial.portableradio.client.ClientConfig;
import ru.fuctorial.portableradio.common.audio.AudioConfig;
import ru.fuctorial.portableradio.common.item.ItemWalkieTalkie;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ConcurrentLinkedQueue;

@SideOnly(Side.CLIENT)
public class AudioPlaybackManager {

    private SourceDataLine speaker;
    private String selectedSpeakerName;
    private float targetVolume = 1.0f;

    private final ConcurrentLinkedQueue<byte[]> opusAudioQueue = new ConcurrentLinkedQueue<>();
    private OpusDecoder decoder;
    private final short[] pcmOutputBuffer = new short[AudioConfig.FRAME_SIZE_SAMPLES];
    private final byte[] byteOutputBuffer = new byte[AudioConfig.BUFFER_SIZE];

    private Thread playbackThread;
    private volatile boolean isPlaying = false;
    private volatile long lastPacketTime = 0;
    private static final int TIMEOUT_MS = 250;

    public void startPlayback(String speakerName, float volume) {
        if (isPlaying) {
            lastPacketTime = System.currentTimeMillis();
            return;
        }

        PortableRadio.logger.debug("Starting audio playback...");
        PortableRadio.logger.debug("Speaker: " + (speakerName.isEmpty() ? "Default" : speakerName) + ", Volume: " + volume);

        this.selectedSpeakerName = speakerName;
        this.targetVolume = volume;
        this.isPlaying = true;

        playbackThread = new Thread(() -> {
            try {
                decoder = new OpusDecoder((int)AudioConfig.SAMPLE_RATE, AudioConfig.CHANNELS);

                speaker = findSpeakerByName(selectedSpeakerName);
                if (speaker == null) {
                    PortableRadio.logger.error("Speaker device NOT FOUND!");
                    isPlaying = false;
                    return;
                }
                speaker.open(AudioConfig.getAudioFormat());
                speaker.start();
                PortableRadio.logger.debug("Speaker opened and started.");

                while (isPlaying) {
                    byte[] opusData = opusAudioQueue.poll();
                    if (opusData != null) {
                        lastPacketTime = System.currentTimeMillis();

                        
                        decoder.decode(opusData, pcmOutputBuffer, AudioConfig.FRAME_SIZE_SAMPLES);
                        ByteBuffer.wrap(byteOutputBuffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(pcmOutputBuffer);

                        
                        byte[] compressedData = applyCompression(byteOutputBuffer);

                        
                        byte[] finalData = applyVolumeToPcm(compressedData, targetVolume);

                        
                        if (speaker != null && speaker.isOpen()) {
                            speaker.write(finalData, 0, finalData.length);
                        }
                    } else {
                        if (System.currentTimeMillis() - lastPacketTime > TIMEOUT_MS) {
                            isPlaying = false;
                        } else {
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                isPlaying = false;
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                PortableRadio.logger.error("Error during audio playback", e);
            } finally {
                PortableRadio.logger.debug("Stopping audio playback...");
                if (decoder != null) {
                    decoder.close();
                }
                if (speaker != null) {
                    speaker.drain();
                    speaker.flush();
                    speaker.stop();
                    speaker.close();
                    speaker = null;
                }
                isPlaying = false;
                opusAudioQueue.clear();
                PortableRadio.logger.debug("Playback cleanup completed.");
            }
        });
        playbackThread.setName("AudioPlaybackManager-Thread");
        playbackThread.setDaemon(true);
        playbackThread.start();
    }

    public void queueOpusAudio(byte[] opusData) {
        if (opusData == null || opusData.length == 0) return;

        if (!isPlaying) {
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            if (player != null) {
                ItemStack radioStack = findActiveRadio(player);
                if (radioStack != null) {
                    ItemWalkieTalkie radioItem = (ItemWalkieTalkie) radioStack.getItem();
                    int speakerIndex = ClientConfig.getSelectedSpeakerIndex();
                    float speakerVolume = radioItem.getSpeakerVolume(radioStack);
                    String speakerName = AudioDeviceManager.INSTANCE.getRealSpeakerName(speakerIndex);
                    startPlayback(speakerName, speakerVolume);
                }
            }
        }

        if (isPlaying) {
            opusAudioQueue.offer(opusData);
            lastPacketTime = System.currentTimeMillis();
        }
    }

    public void stopPlayback() {
        isPlaying = false;
        opusAudioQueue.clear();
        if (playbackThread != null) {
            try {
                playbackThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private byte[] applyVolumeToPcm(byte[] inputData, float volume) {
        byte[] outputData = new byte[inputData.length];
        for (int i = 0; i < inputData.length; i += 2) {
            if (i + 1 < inputData.length) {
                short sample = (short) ((inputData[i+1] << 8) | (inputData[i] & 0xFF));
                float amplified = sample * volume;
                if (amplified > 32767.0f) amplified = 32767.0f;
                if (amplified < -32768.0f) amplified = -32768.0f;
                short outputSample = (short) amplified;
                outputData[i] = (byte) (outputSample & 0xFF);
                outputData[i + 1] = (byte) ((outputSample >> 8) & 0xFF);
            }
        }
        return outputData;
    }

    public void setVolume(float volume) {
        this.targetVolume = Math.max(0.0f, Math.min(2.0f, volume));
    }

    private byte[] applyCompression(byte[] inputData) {
        final float THRESHOLD = 0.3f;
        final float RATIO = 3.0f;
        byte[] outputData = new byte[inputData.length];
        for (int i = 0; i < inputData.length; i += 2) {
            if (i + 1 < inputData.length) {
                short sample = (short) ((inputData[i + 1] << 8) | (inputData[i] & 0xFF));
                float normalizedSample = sample / 32768.0f;
                float magnitude = Math.abs(normalizedSample);
                if (magnitude > THRESHOLD) {
                    float compressedMagnitude = THRESHOLD + (magnitude - THRESHOLD) / RATIO;
                    normalizedSample = compressedMagnitude * Math.signum(normalizedSample);
                }
                short outputSample = (short) (normalizedSample * 32767.0f);
                outputData[i] = (byte) (outputSample & 0xFF);
                outputData[i + 1] = (byte) ((outputSample >> 8) & 0xFF);
            }
        }
        return outputData;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    private SourceDataLine findSpeakerByName(String name) throws LineUnavailableException {
        if (name == null || name.isEmpty()) {
            return (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, AudioConfig.getAudioFormat()));
        }
        for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo()) {
            if (mixerInfo.getName().equals(name)) {
                Mixer mixer = AudioSystem.getMixer(mixerInfo);
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, AudioConfig.getAudioFormat());
                if (mixer.isLineSupported(info)) {
                    return (SourceDataLine) mixer.getLine(info);
                }
            }
        }
        return null;
    }

    private ItemStack findActiveRadio(EntityPlayer player) {
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack != null && stack.getItem() instanceof ItemWalkieTalkie && ((ItemWalkieTalkie)stack.getItem()).isRadioOn(stack)) {
                return stack;
            }
        }
        return null;
    }
}