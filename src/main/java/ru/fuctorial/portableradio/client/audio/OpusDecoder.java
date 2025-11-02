package ru.fuctorial.portableradio.client.audio;

import com.sun.jna.ptr.PointerByReference;
import tomp2p.opuswrapper.Opus;
import ru.fuctorial.portableradio.PortableRadio;

import java.io.Closeable;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class OpusDecoder implements Closeable {

    private final PointerByReference decoder;

    public OpusDecoder(int sampleRate, int channels) {
        IntBuffer error = IntBuffer.allocate(1);
        this.decoder = Opus.INSTANCE.opus_decoder_create(sampleRate, channels, error);
        if (error.get(0) != Opus.OPUS_OK) {
            throw new IllegalStateException("Failed to create Opus decoder: " + Opus.INSTANCE.opus_strerror(error.get(0)));
        }
        PortableRadio.logger.debug("OpusDecoder created successfully for " + sampleRate + " Hz, " + channels + " channels");
    }

    
    public void decode(byte[] opusData, short[] pcmOutput, int frameSize) {
        ShortBuffer pcmBuffer = ShortBuffer.wrap(pcmOutput);

        int decodedSamples = Opus.INSTANCE.opus_decode(
                this.decoder,
                opusData,
                opusData.length,
                pcmBuffer,
                frameSize,
                0
        );

        if (decodedSamples < 0) {
            PortableRadio.logger.error("Opus decode error: " + Opus.INSTANCE.opus_strerror(decodedSamples));
            
            for (int i = 0; i < pcmOutput.length; i++) {
                pcmOutput[i] = 0;
            }
        }
    }

    @Override
    public void close() {
        if (decoder != null) {
            Opus.INSTANCE.opus_decoder_destroy(decoder);
            PortableRadio.logger.debug("OpusDecoder destroyed");
        }
    }
}