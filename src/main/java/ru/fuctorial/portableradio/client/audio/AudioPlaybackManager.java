package ru.fuctorial.portableradio.client.audio;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import ru.fuctorial.portableradio.client.ClientConfig;
import ru.fuctorial.portableradio.common.audio.AudioConfig;
import ru.fuctorial.portableradio.common.item.ItemWalkieTalkie;

import javax.sound.sampled.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@SideOnly(Side.CLIENT)
public class AudioPlaybackManager {

    private SourceDataLine speaker;
    private String selectedSpeakerName;
    private float targetVolume = 1.0f;
    private final ConcurrentLinkedQueue<byte[]> audioQueue = new ConcurrentLinkedQueue<>();
    private Thread playbackThread;
    private volatile boolean isPlaying = false;

    private volatile long lastPacketTime = 0;
    private static final int TIMEOUT_MS = 200;

    private float currentGain = 1.0f;
    private static final float TARGET_RMS = 1.0f;
    private static final float LOWER_THRESHOLD_RMS = 0.20f;
    private static final float UPPER_THRESHOLD_RMS = 0.50f;
    private static final float QUIET_BOOST_FACTOR = 1.5f;
    private static final float LOUD_REDUCTION_FACTOR = 0.7f;
    private static final float MAX_GAIN = 5.0f;
    private static final float GAIN_SMOOTH_FACTOR = 0.1f;

    public void startPlayback(String speakerName, float volume) {
        if (isPlaying) {
            lastPacketTime = System.currentTimeMillis();
            return;
        }

        this.selectedSpeakerName = speakerName;
        this.targetVolume = volume;
        this.isPlaying = true;

        playbackThread = new Thread(() -> {
            try {
                speaker = findSpeakerByName(selectedSpeakerName);
                if (speaker == null) return;

                speaker.open(AudioConfig.getAudioFormat());
                speaker.start();
                lastPacketTime = System.currentTimeMillis();

                while (isPlaying) {
                    byte[] audioData = audioQueue.poll();
                    if (audioData != null) {
                        lastPacketTime = System.currentTimeMillis();
                        byte[] processedData = processAudio(audioData);
                        speaker.write(processedData, 0, processedData.length);
                    } else {
                        if (System.currentTimeMillis() - lastPacketTime > TIMEOUT_MS) {
                            isPlaying = false;
                        } else {
                            Thread.sleep(10);
                        }
                    }
                }
                speaker.drain();
            } catch (Exception e) {
                System.err.println("Playback error: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (speaker != null) {
                    speaker.stop();
                    speaker.close();
                }
                isPlaying = false;
                audioQueue.clear();
            }
        });
        playbackThread.setName("AudioPlaybackManager-Thread");
        playbackThread.setDaemon(true);
        playbackThread.start();
    }

    public void queueAudio(byte[] audioData) {
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

        if (isPlaying && audioData != null && audioData.length > 0) {
            audioQueue.offer(audioData);
            lastPacketTime = System.currentTimeMillis();
        }
    }

    private byte[] processAudio(byte[] inputData) {
        float currentRMS = calculateRMS(inputData, inputData.length);
        float targetGain;

        if (currentRMS < 0.01f) {
            targetGain = 1.0f;
        } else if (currentRMS < LOWER_THRESHOLD_RMS) {
            targetGain = (TARGET_RMS / currentRMS) * QUIET_BOOST_FACTOR;
        } else if (currentRMS > UPPER_THRESHOLD_RMS) {
            targetGain = (TARGET_RMS / currentRMS) * LOUD_REDUCTION_FACTOR;
        } else {
            targetGain = TARGET_RMS / currentRMS;
        }

        targetGain = Math.min(targetGain, MAX_GAIN);
        currentGain += (targetGain - currentGain) * GAIN_SMOOTH_FACTOR;
        float totalGain = currentGain * targetVolume;

        byte[] outputData = new byte[inputData.length];
        for (int i = 0; i < inputData.length; i += 2) {
            if (i + 1 < inputData.length) {
                short sample = (short) ((inputData[i + 1] << 8) | (inputData[i] & 0xFF));
                float amplified = sample * totalGain;
                amplified = softClip(amplified);
                short outputSample = (short) Math.max(-32768, Math.min(32767, amplified));
                outputData[i] = (byte) (outputSample & 0xFF);
                outputData[i + 1] = (byte) ((outputSample >> 8) & 0xFF);
            }
        }
        return outputData;
    }

    private float softClip(float sample) {
        if (sample > 32767) {
            return 32767.0f;
        } else if (sample < -32768) {
            return -32768.0f;
        }
        return sample;
    }

    private float calculateRMS(byte[] audioData, int length) {
        long sum = 0;
        int sampleCount = 0;
        for (int i = 0; i < length; i += 2) {
            if (i + 1 < length) {
                short sample = (short) ((audioData[i + 1] << 8) | (audioData[i] & 0xFF));
                sum += sample * sample;
                sampleCount++;
            }
        }
        if (sampleCount == 0) return 0.0f;
        double meanSquare = (double) sum / sampleCount;
        return (float) (Math.sqrt(meanSquare) / 32768.0);
    }

    public void setVolume(float volume) {
        this.targetVolume = Math.max(0.0f, Math.min(2.0f, volume));
    }

    public void stopPlayback() {
        isPlaying = false;
        audioQueue.clear();
        if (playbackThread != null && playbackThread.isAlive()) {
            try {
                playbackThread.join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    private SourceDataLine findSpeakerByName(String name) throws LineUnavailableException {
        if (name == null || name.isEmpty()) {
            // ИСПРАВЛЕНИЕ: Добавляем поддержку Default для динамика
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, AudioConfig.getAudioFormat());
            if (AudioSystem.isLineSupported(info)) {
                return (SourceDataLine) AudioSystem.getLine(info);
            }
            return null;
        }
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo : mixers) {
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
            if (stack != null && stack.getItem() instanceof ItemWalkieTalkie) {
                if (((ItemWalkieTalkie) stack.getItem()).isRadioOn(stack)) {
                    return stack;
                }
            }
        }
        return null;
    }
}