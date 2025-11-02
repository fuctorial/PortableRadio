package ru.fuctorial.portableradio.client.audio;

import com.sun.jna.ptr.PointerByReference;
import tomp2p.opuswrapper.Opus;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class OpusEncoder implements Closeable {

    private final PointerByReference encoder;

    public OpusEncoder(int sampleRate, int channels, int application) {
        IntBuffer error = IntBuffer.allocate(1);
        this.encoder = Opus.INSTANCE.opus_encoder_create(sampleRate, channels, application, error);
        if (error.get(0) != Opus.OPUS_OK) {
            throw new IllegalStateException("Failed to create Opus encoder: " + Opus.INSTANCE.opus_strerror(error.get(0)));
        }

        
        setBitrate(32000);

        
        Opus.INSTANCE.opus_encoder_ctl(this.encoder, Opus.OPUS_SET_VBR_REQUEST, 0);

        
        Opus.INSTANCE.opus_encoder_ctl(this.encoder, Opus.OPUS_SET_COMPLEXITY_REQUEST, 10);

        
        Opus.INSTANCE.opus_encoder_ctl(this.encoder, Opus.OPUS_SET_SIGNAL_REQUEST, Opus.OPUS_SIGNAL_VOICE);

        
        Opus.INSTANCE.opus_encoder_ctl(this.encoder, Opus.OPUS_SET_DTX_REQUEST, 0);
    }

    public void setBitrate(int bitrate) {
        Opus.INSTANCE.opus_encoder_ctl(this.encoder, Opus.OPUS_SET_BITRATE_REQUEST, bitrate);
    }

    public byte[] encode(short[] pcmData, int frameSize) {
        ShortBuffer pcmBuffer = ShortBuffer.wrap(pcmData);
        ByteBuffer encodedBuffer = ByteBuffer.allocate(4096);

        int encodedLength = Opus.INSTANCE.opus_encode(this.encoder, pcmBuffer, frameSize, encodedBuffer, encodedBuffer.capacity());

        if (encodedLength < 0) {
            return new byte[0];
        }

        byte[] opusData = new byte[encodedLength];
        encodedBuffer.get(opusData);
        return opusData;
    }

    @Override
    public void close() {
        if (encoder != null) {
            Opus.INSTANCE.opus_encoder_destroy(encoder);
        }
    }
}