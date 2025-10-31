package org.technoready.escaperoom.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameStateService {
    private final Map<String, Integer> playerProgress = new ConcurrentHashMap<>();
    private final Map<String, Boolean> doorUnlocked = new ConcurrentHashMap<>();

    public void startGame(String playerId) {
        playerProgress.put(playerId, 0);
        doorUnlocked.put(playerId, false);
    }

    public int getProgress(String playerId) {
        return playerProgress.getOrDefault(playerId, 0);
    }

    public void setProgress(String playerId, int stage) {
        playerProgress.put(playerId, stage);
    }

    public boolean isDoorUnlocked(String playerId) {
        return doorUnlocked.getOrDefault(playerId, false);
    }

    public void unlockDoor(String playerId) {
        doorUnlocked.put(playerId, true);
    }
}
