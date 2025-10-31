package org.technoready.escaperoom.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.technoready.escaperoom.model.ManorResponse;
import org.technoready.escaperoom.service.GameStateService;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ManorController {

    private final GameStateService gameState;

    public ManorController(GameStateService gameState) {
        this.gameState = gameState;
    }

    @GetMapping("/room")
    public ResponseEntity<ManorResponse> enterRoom(@RequestParam(defaultValue = "player1") String playerId) {
        gameState.startGame(playerId);

        ManorResponse response = new ManorResponse(
                "You open your eyes to a dimly lit parlor. The fire in the chimney is cold, and a portrait watches you from the wall. " +
                        "Dust particles float in the stale air, catching what little light filters through the grimy windows. " +
                        "The floorboards creak beneath your feet. A message is carved into the wooden table.",
                "parlor_entered",
                "\"Only those who listen to the house will find the way out.\"",
                0
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/door")
    public ResponseEntity<ManorResponse> unlockDoor(
            @RequestParam(defaultValue = "player1") String playerId,
            @RequestBody Map<String, String> body) {

        if (gameState.getProgress(playerId) != 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ManorResponse(
                            "You've already passed through this door. The way forward lies elsewhere.",
                            "door_already_unlocked",
                            null,
                            gameState.getProgress(playerId)
                    ));
        }

        String key = body.get("key");

        if (key == null || !key.equalsIgnoreCase("knock")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ManorResponse(
                            "You whisper the wrong word. The silence grows heavier. Something moves in the dark behind you. " +
                                    "A cold breath touches the back of your neck. The house is watching... waiting.",
                            "door_locked",
                            "Listen to the house. What does it want you to do?",
                            0
                    ));
        }

        gameState.unlockDoor(playerId);
        gameState.setProgress(playerId, 1);

        ManorResponse response = new ManorResponse(
                "You knock three times. The sound echoes through the empty house like thunder. " +
                        "A loud creak echoes through the house. The door slowly opens to a candlelit hallway, " +
                        "revealing flickering shadows that dance along the walls.",
                "door_unlocked",
                "Continue your journey with GET /hallway",
                1
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/hallway")
    public ResponseEntity<ManorResponse> enterHallway(@RequestParam(defaultValue = "player1") String playerId) {
        if (!gameState.isDoorUnlocked(playerId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ManorResponse(
                            "The door to the hallway is locked. You cannot proceed without unlocking it first. " +
                                    "The house whispers: 'You must earn your passage...'",
                            "access_denied",
                            "Return to the parlor and unlock the door.",
                            gameState.getProgress(playerId)
                    ));
        }

        if (gameState.getProgress(playerId) != 1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ManorResponse(
                            "You've already explored this hallway. The way forward lies at the end of your journey.",
                            "hallway_already_explored",
                            null,
                            gameState.getProgress(playerId)
                    ));
        }

        gameState.setProgress(playerId, 2);

        ManorResponse response = new ManorResponse(
                "🕯️ The hallway stretches endlessly before you. Candles flicker as you pass, though there's no breeze. " +
                        "Portraits of long-dead family members line the walls, their eyes seeming to follow your every move. " +
                        "One painting has eyes that seem particularly alive, almost... knowing. " +
                        "Beneath it, a small box sits on a dusty table, locked with an ornate mechanism. " +
                        "An inscription glows faintly in the candlelight.",
                "hallway_entered",
                "\"Truth opens what fear locks.\"",
                2
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/escape")
    public ResponseEntity<ManorResponse> attemptEscape(
            @RequestParam(defaultValue = "player1") String playerId,
            @RequestBody Map<String, String> body) {

        if (gameState.getProgress(playerId) < 2) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ManorResponse(
                            "⛔ You cannot escape yet. You haven't explored enough of the manor. " +
                                    "The house won't let you leave so easily. Dark whispers fill your ears: 'Not yet... not yet...'",
                            "escape_premature",
                            "Complete your journey through the manor first.",
                            gameState.getProgress(playerId)
                    ));
        }

        String key = body.get("key");

        if (key == null || !key.equalsIgnoreCase("truth")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ManorResponse(
                            "😈 A ghostly laughter echoes through the house, growing louder and more sinister. " +
                                    "You weren't ready. The door slams shut again with a deafening bang. " +
                                    "The candles extinguish one by one. In the darkness, you hear footsteps... " +
                                    "approaching... closer... closer... Then silence. " +
                                    "The curse remains unbroken.",
                            "escape_failed",
                            "The house demands truth. What is the key to breaking the curse?",
                            2
                    ));
        }

        gameState.setProgress(playerId, 3);

        ManorResponse response = new ManorResponse(
                "You speak the word 'truth' into the silence. The house shudders violently. " +
                        "The portraits begin to smile, their eyes closing peacefully for the first time in centuries. " +
                        "The manor trembles as the walls begin to fade like morning mist. " +
                        "Light floods through dissolving windows. You step into the moonlight — free at last. " +
                        "Behind you, Blackwood Manor crumbles into silvery mist, its spirits finally released. " +
                        "The curse is broken.",
                "escaped_successfully",
                "Congratulations! You have survived the Haunting of Blackwood Manor!",
                3
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<ManorResponse> getStatus(@RequestParam(defaultValue = "player1") String playerId) {
        int stage = gameState.getProgress(playerId);

        String narrative = switch (stage) {
            case 0 -> "You are in the parlor. The door awaits. POST to /door with the correct key.";
            case 1 -> "You've unlocked the door. Use GET /hallway to continue your journey.";
            case 2 -> "You're in the hallway. The final challenge awaits. POST to /escape with the key to freedom.";
            case 3 -> "You have escaped Blackwood Manor! The nightmare is over.";
            default -> "Unknown location. The house is confused...";
        };

        String status = switch (stage) {
            case 0 -> "in_parlor";
            case 1 -> "door_unlocked";
            case 2 -> "in_hallway";
            case 3 -> "escaped";
            default -> "lost";
        };

        return ResponseEntity.ok(new ManorResponse(narrative, status, null, stage));
    }
}
