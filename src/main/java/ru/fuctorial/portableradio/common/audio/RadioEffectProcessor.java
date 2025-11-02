package ru.fuctorial.portableradio.common.audio;

import java.util.Random;

public class RadioEffectProcessor {

    private static final float COMPRESSION_RATIO = 0.925F;

    private static final float DISTORTION_AMOUNT = 0.025F;

    private static final float NOISE_AMOUNT = 0.0025F;

    private final Random random = new Random();
    private float lastSample = 0.0F;

    public byte[] applyRadioEffect(byte[] inputData, int length) {
        byte[] outputData = new byte[length];

        for (int i = 0; i < length; i += 2) {
            if (i + 1 < length) {
                short sample = (short) ((inputData[i + 1] << 8) | (inputData[i] & 0xFF));
                float normalizedSample = sample / 32768.0F;

                normalizedSample = applyCompression(normalizedSample);
                normalizedSample = applyHighPassFilter(normalizedSample);
                normalizedSample = applyDistortion(normalizedSample);
                normalizedSample = addRadioNoise(normalizedSample);

                normalizedSample *= 0.75f;

                normalizedSample = Math.max(-1.0F, Math.min(1.0F, normalizedSample));

                short processedSample = (short) (normalizedSample * 32767.0F);

                outputData[i] = (byte) (processedSample & 0xFF);
                outputData[i + 1] = (byte) ((processedSample >> 8) & 0xFF);
            }
        }

        return outputData;
    }

    private float applyCompression(float sample) {
        float sign = Math.signum(sample);
        float magnitude = Math.abs(sample);

        if (magnitude > 0.5F) {
            magnitude = 0.5F + (magnitude - 0.5F) * COMPRESSION_RATIO;
        }

        return sign * magnitude;
    }

    private float applyHighPassFilter(float sample) {
        float alpha = 0.95F;
        float output = alpha * (lastSample + sample - lastSample);
        lastSample = sample;
        return output;
    }

    private float applyDistortion(float sample) {
        return (float) Math.tanh(sample * (1.0F + DISTORTION_AMOUNT));
    }

    private float addRadioNoise(float sample) {
        float noise = (random.nextFloat() * 2.0F - 1.0F) * NOISE_AMOUNT;
        return sample + noise;
    }

    public void reset() {
        lastSample = 0.0F;
    }
}