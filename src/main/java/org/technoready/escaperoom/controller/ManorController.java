package org.technoready.escaperoom.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.technoready.escaperoom.model.ManorResponse;
import org.technoready.escaperoom.service.GameStateService;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Haunted Manor Escape API", description = "REST API simulation for escaping a haunted manor through HTTP puzzles.")
public class ManorController {

    private final GameStateService gameState;

    public ManorController(GameStateService gameState) {
        this.gameState = gameState;
    }

    // -------------------- ROOM --------------------

    @Operation(
            summary = "Enter the haunted parlor",
            description = "Begins the game. Returns the first description and clue to start the escape adventure.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully entered the parlor",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ManorResponse.class),
                                    examples = @ExampleObject(value = """
                                        {
                                          "message": "You open your eyes to a dimly lit parlor...",
                                          "status": "parlor_entered",
                                          "hint": "\\"Only those who listen to the house will find the way out.\\"",
                                          "progress": 0
                                        }
                                        """)
                            )
                    )
            }
    )
    @GetMapping("/room")
    public ResponseEntity<ManorResponse> enterRoom(
            @Parameter(description = "Unique player identifier", example = "player1")
            @RequestParam(defaultValue = "player1") String playerId) {

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

    // -------------------- DOOR --------------------

    @Operation(
            summary = "Attempt to unlock the parlor door",
            description = "Sends a secret key to unlock the next room. Requires JSON body: `{ \"key\": \"knock\" }`.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Door successfully unlocked",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ManorResponse.class),
                                    examples = @ExampleObject(value = """
                                        {
                                          "message": "You knock three times...",
                                          "status": "door_unlocked",
                                          "hint": "Continue your journey with GET /hallway",
                                          "progress": 1
                                        }
                                        """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid key or door already unlocked",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @PostMapping("/door")
    public ResponseEntity<ManorResponse> unlockDoor(
            @Parameter(description = "Unique player identifier", example = "player1")
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

    // -------------------- HALLWAY --------------------

    @Operation(
            summary = "Enter the haunted hallway",
            description = "Access the hallway once the parlor door has been unlocked. Returns new clues to progress.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully entered hallway"),
                    @ApiResponse(responseCode = "400", description = "Door not unlocked or already explored")
            }
    )
    @GetMapping("/hallway")
    public ResponseEntity<ManorResponse> enterHallway(
            @Parameter(description = "Unique player identifier", example = "player1")
            @RequestParam(defaultValue = "player1") String playerId) {

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

    // -------------------- ESCAPE --------------------

    @Operation(
            summary = "Attempt to escape the manor",
            description = "Tries to break the curse and escape the manor. Requires JSON body: `{ \"key\": \"truth\" }`.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Escape successful – curse broken"),
                    @ApiResponse(responseCode = "400", description = "Failed escape or incomplete progress")
            }
    )
    @PostMapping("/escape")
    public ResponseEntity<ManorResponse> attemptEscape(
            @Parameter(description = "Unique player identifier", example = "player1")
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

    // -------------------- STATUS --------------------

    @Operation(
            summary = "Check current game status",
            description = "Returns the player's current location and progress within the haunted manor.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Status successfully retrieved")
            }
    )
    @GetMapping("/status")
    public ResponseEntity<ManorResponse> getStatus(
            @Parameter(description = "Unique player identifier", example = "player1")
            @RequestParam(defaultValue = "player1") String playerId) {

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
