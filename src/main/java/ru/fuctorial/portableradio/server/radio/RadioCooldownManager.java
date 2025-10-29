package ru.fuctorial.portableradio.server.radio;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public enum RadioCooldownManager {
    INSTANCE;

    private final Map<UUID, Long> powerToggleCooldowns = new HashMap<>();
    private final Map<UUID, Long> frequencyChangeCooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 1000;

    public boolean canTogglePower(UUID playerID) {
        long now = System.currentTimeMillis();
        if (now - powerToggleCooldowns.getOrDefault(playerID, 0L) >= COOLDOWN_MS) {
            powerToggleCooldowns.put(playerID, now);
            return true;
        }
        return false;
    }

    public boolean canChangeFrequency(UUID playerID) {
        long now = System.currentTimeMillis();
        if (now - frequencyChangeCooldowns.getOrDefault(playerID, 0L) >= COOLDOWN_MS) {
            frequencyChangeCooldowns.put(playerID, now);
            return true;
        }
        return false;
    }
}