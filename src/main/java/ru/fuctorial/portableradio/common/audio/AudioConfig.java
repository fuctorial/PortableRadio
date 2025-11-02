
package ru.fuctorial.portableradio.common.audio;

import javax.sound.sampled.AudioFormat;

public class AudioConfig {

    
    
    public static final float SAMPLE_RATE = 48000.0F;

    
    public static final int FRAME_SIZE_SAMPLES = 960; 

    
    public static final int SAMPLE_SIZE_IN_BITS = 16;
    public static final int CHANNELS = 1;
    public static final boolean SIGNED = true;
    public static final boolean BIG_ENDIAN = false;
    public static final int BUFFER_SIZE = FRAME_SIZE_SAMPLES * 2; 

    public static AudioFormat getAudioFormat() {
        
        return new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
    }
}