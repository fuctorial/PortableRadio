package ru.fuctorial.portableradio.client.audio;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ru.fuctorial.portableradio.PortableRadio;
import ru.fuctorial.portableradio.common.audio.AudioConfig;

import javax.sound.sampled.*;

@SideOnly(Side.CLIENT)
public enum AudioTestManager {
    INSTANCE;

    private Thread micTestThread;
    private Thread speakerTestThread;
    private volatile boolean isMicTesting = false;
    private volatile boolean isSpeakerTesting = false;
    private volatile float currentAudioLevel = 0.0f;
    private String currentTestStatus = "";

    public interface CalibrationCallback {
        void onCalibrated(float suggestedVolume);
    }

    public void testMicrophone(int micIndex, float volume) {
        if (isMicTesting) {
            stopAllTests();
            return;
        }

        String micName = AudioDeviceManager.INSTANCE.getRealMicName(micIndex);

        isMicTesting = true;
        currentTestStatus = "gui.portableradio.testing_microphone";

        micTestThread = new Thread(() -> {
            TargetDataLine microphone = null;
            SourceDataLine speaker = null;
            try {
                microphone = findMicrophoneByName(micName);
                if (microphone == null) {
                    currentTestStatus = "gui.portableradio.failed_open_microphone";
                    isMicTesting = false;
                    return;
                }
                microphone.open(microphone.getFormat());
                microphone.start();

                DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, microphone.getFormat());
                speaker = (SourceDataLine) AudioSystem.getLine(speakerInfo);
                speaker.open(microphone.getFormat());
                speaker.start();

                byte[] buffer = new byte[AudioConfig.BUFFER_SIZE];
                long startTime = System.currentTimeMillis();

                while (isMicTesting && (System.currentTimeMillis() - startTime) < 5000) {
                    int bytesRead = microphone.read(buffer, 0, buffer.length);
                    if (bytesRead > 0 && isMicTesting) {
                        currentAudioLevel = calculateRMS(buffer, bytesRead);
                        applyVolume(buffer, bytesRead, volume);
                        speaker.write(buffer, 0, bytesRead);
                    }
                }

                currentTestStatus = "gui.portableradio.test_completed";
            } catch (Exception e) {
                currentTestStatus = "gui.portableradio.error";
                // e.printStackTrace();
                PortableRadio.logger.error("Error during microphone test", e);
            } finally {
                isMicTesting = false;
                currentAudioLevel = 0.0f;
                if (microphone != null) {
                    microphone.stop();
                    microphone.close();
                }
                if (speaker != null) {
                    speaker.drain();
                    speaker.stop();
                    speaker.close();
                }
            }
        });
        micTestThread.setName("AudioTestManager-Microphone");
        micTestThread.setDaemon(true);
        micTestThread.start();
    }


    public void testSpeaker(int speakerIndex, float volume) {
        if (isSpeakerTesting) {
            stopAllTests();
            return;
        }

        String speakerName = AudioDeviceManager.INSTANCE.getRealSpeakerName(speakerIndex);

        isSpeakerTesting = true;
        currentTestStatus = "gui.portableradio.testing_speaker";

        speakerTestThread = new Thread(() -> {
            SourceDataLine speaker = null;
            try {
                speaker = findSpeakerByName(speakerName);
                if (speaker == null) {
                    currentTestStatus = "gui.portableradio.failed_open_speaker";
                    isSpeakerTesting = false;
                    return;
                }
                speaker.open(AudioConfig.getAudioFormat());
                speaker.start();

                float frequency = 440.0f;
                byte[] buffer = new byte[AudioConfig.BUFFER_SIZE];
                double angle = 0;

                long startTime = System.currentTimeMillis();
                while (isSpeakerTesting && (System.currentTimeMillis() - startTime) < 5000) {
                    for (int i = 0; i < buffer.length; i += 2) {
                        angle += 2.0 * Math.PI * frequency / AudioConfig.SAMPLE_RATE;
                        short sample = (short)(Math.sin(angle) * 8000 * volume);
                        buffer[i] = (byte)(sample & 0xFF);
                        buffer[i + 1] = (byte)((sample >> 8) & 0xFF);
                    }
                    speaker.write(buffer, 0, buffer.length);
                }

                currentTestStatus = "gui.portableradio.test_completed";
            } catch (Exception e) {
                currentTestStatus = "gui.portableradio.error";
                // e.printStackTrace();
                PortableRadio.logger.error("Error during speaker test", e);
            } finally {
                isSpeakerTesting = false;
                if (speaker != null) {
                    speaker.drain();
                    speaker.stop();
                    speaker.close();
                }
            }
        });
        speakerTestThread.setName("AudioTestManager-Speaker");
        speakerTestThread.setDaemon(true);
        speakerTestThread.start();
    }

    public void calibrateMicrophoneVolume(int micIndex, CalibrationCallback callback) {
        String micName = AudioDeviceManager.INSTANCE.getRealMicName(micIndex);
        if (micName == null || micName.isEmpty()) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                TargetDataLine microphone = null;
                try {
                    microphone = findMicrophoneByName(micName);
                    if (microphone == null) return;

                    microphone.open(AudioConfig.getAudioFormat());
                    microphone.start();

                    byte[] buffer = new byte[AudioConfig.BUFFER_SIZE];
                    float maxRMS = 0.0f;
                    int samples = 0;

                    long startTime = System.currentTimeMillis();
                    while ((System.currentTimeMillis() - startTime) < 2000) {
                        int bytesRead = microphone.read(buffer, 0, buffer.length);
                        if (bytesRead > 0) {
                            float rms = calculateRMS(buffer, bytesRead);
                            if (rms > maxRMS) {
                                maxRMS = rms;
                            }
                            samples++;
                        }
                    }

                    float suggestedVolume;
                    if (maxRMS < 0.1f) {
                        suggestedVolume = 100.0f;
                    } else if (maxRMS < 0.3f) {
                        suggestedVolume = 75.0f;
                    } else if (maxRMS < 0.6f) {
                        suggestedVolume = 50.0f;
                    } else {
                        suggestedVolume = 25.0f;
                    }

                    if (callback != null) {
                        callback.onCalibrated(suggestedVolume);
                    }
                } catch (Exception e) {
                    // e.printStackTrace();
                } finally {
                    if (microphone != null) {
                        microphone.stop();
                        microphone.close();
                    }
                }
            }
        }).start();
    }

    private float calculateRMS(byte[] audioData, int length) {
        long sum = 0;
        for (int i = 0; i < length; i += 2) {
            if (i + 1 < length) {
                short sample = (short) ((audioData[i + 1] << 8) | (audioData[i] & 0xFF));
                sum += sample * sample;
            }
        }
        int sampleCount = length / 2;
        double meanSquare = (double) sum / sampleCount;
        double rms = Math.sqrt(meanSquare) / 32768.0;
        return (float)Math.min(1.0, rms * 3.0);
    }

    private void applyVolume(byte[] buffer, int length, float volume) {
        for (int i = 0; i < length; i += 2) {
            if (i + 1 < length) {
                short sample = (short) ((buffer[i + 1] << 8) | (buffer[i] & 0xFF));
                sample = (short)(sample * volume);
                buffer[i] = (byte)(sample & 0xFF);
                buffer[i + 1] = (byte)((sample >> 8) & 0xFF);
            }
        }
    }

    public void stopAllTests() {
        isMicTesting = false;
        isSpeakerTesting = false;
        if (micTestThread != null && micTestThread.isAlive()) {
            try { micTestThread.join(200); } catch (InterruptedException e) { micTestThread.interrupt(); }
        }
        if (speakerTestThread != null && speakerTestThread.isAlive()) {
            try { speakerTestThread.join(200); } catch (InterruptedException e) { speakerTestThread.interrupt(); }
        }
        currentTestStatus = "";
        currentAudioLevel = 0.0f;
    }

    public void setTestStatus(String status) {
        this.currentTestStatus = status;
    }

    public String getTestStatus() {
        return currentTestStatus;
    }

    public boolean isMicrophoneTesting() {
        return isMicTesting;
    }

    public boolean isSpeakerTesting() {
        return isSpeakerTesting;
    }

    public float getCurrentAudioLevel() {
        return currentAudioLevel;
    }

    private TargetDataLine findMicrophoneByName(String name) throws LineUnavailableException {
        if (name == null || name.isEmpty()) {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, AudioConfig.getAudioFormat());
            if (AudioSystem.isLineSupported(info)) {
                // PortableRadio.debug("Using default system microphone.");
                return (TargetDataLine) AudioSystem.getLine(info);
            }
        }

        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo : mixers) {
            if (mixerInfo.getName().equals(name)) {
                Mixer mixer = AudioSystem.getMixer(mixerInfo);
                float[] sampleRates = {44100.0F, 48000.0F, 16000.0F, 22050.0F, 8000.0F};
                for (float rate : sampleRates) {
                    AudioFormat format = new AudioFormat(rate, 16, 1, true, false);
                    DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                    if (mixer.isLineSupported(info)) {
                        // PortableRadio.debug("AudioTest using format: " + rate + " Hz for " + name);
                        TargetDataLine line = (TargetDataLine) mixer.getLine(info);
                        return line;
                    }
                }
            }
        }
        return null;
    }

    private SourceDataLine findSpeakerByName(String name) throws LineUnavailableException {
        if (name == null || name.isEmpty()) {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, AudioConfig.getAudioFormat());
            if (AudioSystem.isLineSupported(info)) {
                // PortableRadio.debug("Using default system speaker.");
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
}