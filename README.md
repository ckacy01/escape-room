
## Spookathon / escape-room 

### Project Overview
Escape room is a Java-based REST API that simulates an interactive escape room experience.
Players find themselves trapped inside a haunted server, and must solve puzzles by sending specific HTTP requests to uncover clues, collect keys, and ultimately escape.

The game is entirely played via HTTP interactions — each endpoint returns narrative feedback and clues guiding players toward the next step. The combination of storytelling and API exploration creates an engaging environment.

---
### Objectives 
- Educational Purpose: Teach users how to interact with REST APIs through an immersive, game-like experience.

- Interactive Storytelling: Blend narrative feedback with HTTP mechanics for an engaging simulation.

- Progressive Challenge: Require logical reasoning and correct use of HTTP methods, parameters, and JSON payloads.

### Technologies Used
| Technology      | Purpose                                             |
|-----------------|-----------------------------------------------------|
| **Java 17+**    | Core programming language                           |
| **Spring Boot** | Framework for building REST APIs                    |
| **Maven**       | Dependency management and project build tool        |
| **Lombok**      | Reduces boilerplate code for models and controllers |
| **JSON**        | Data exchange format between client and server      |

### System Architecture
The application follows a standard **Spring Boot REST architecture**, with a controller handling HTTP requests and services managing the game logic.

**Structure Example:**

``` md
src/
├── main/
│ ├── java/org/technoreadr/escaperoom/
│ │ ├── controller/
│ │ │ └── ManorController.java
│ │ ├── model/
│ │ │ └── ManorResponse.java
│ │ ├── service/
│ │ │ └── GameStateService.java
│ │ └── EscapeRoomApplication.java
│ └── resources/
│     └── application.properties
└── test/
```
---
### Gameplay Flow
1. The user starts the game by sending a `GET` request to `/room`.  
  They receive a **narrative introduction** and the **first clue**.
2. The user must find and send the correct **key** using `POST /door` (either via JSON or query parameter).
3. Upon success, they access `/hallway` for a new clue.
4. After collecting all keys, they use `POST /escape` to attempt escaping.
5. The API verifies if all required keys are collected — if so, the user **escapes the haunted server**!
---
### API endpoints
1.**GET** /api/room

**Narrative:**

You open your eyes to a dimly lit parlor. The fire in the chimney is cold, and a portrait watches you from the wall.
A message is carved into the wooden table:
“Only those who listen to the house will find the way out.”

JSON response:
```json
{
  "message": "You open your eyes to a dimly lit parlor. The fire in the chimney is cold, and a portrait watches you from the wall. Dust particles float in the stale air, catching what little light filters through the grimy windows. The floorboards creak beneath your feet. A message is carved into the wooden table.",
  "status": "parlor_entered",
  "hint": "\"Only those who listen to the house will find the way out.\"",
  "progress": 0
}
```
2.**POST** /api/door

**Goal:**

The player must send the right “key” (e.g. { "key": "knock" }).

**Success Narrative:**

You knock three times. A loud creak echoes through the house. The door slowly opens to a candlelit hallway.

Response:
```json
{
  "message": "You knock three times. The sound echoes through the empty house like thunder. A loud creak echoes through the house. The door slowly opens to a candlelit hallway, revealing flickering shadows that dance along the walls.",
  "status": "door_unlocked",
  "hint": "Continue your journey with GET /hallway",
  "progress": 1
}
```

**Failure Narrative:**

You whisper the wrong word. The silence grows heavier. Something moves in the dark behind you.

Response:
```json
{
  "message": "You whisper the wrong word. The silence grows heavier. Something moves in the dark behind you. A cold breath touches the back of your neck. The house is watching... waiting.",
  "status": "door_locked",
  "hint": "Listen to the house. What does it want you to do?",
  "progress": 0
}
```

If the door is already unlock the response will be:
```json
{
  "message": "You've already passed through this door. The way forward lies elsewhere.",
  "status": "door_already_unlocked",
  "hint": null,
  "progress": 1
}
```

3.**GET** api/hallway

**Narrative:**

The hallway stretches endlessly. Portraits of long-dead family members line the walls. One painting has eyes that seem to follow you. Beneath it, a small box sits on a dusty table with an inscription:
“Truth opens what fear locks.”

Response:
```json
{
  "message": "🕯️ The hallway stretches endlessly before you. Candles flicker as you pass, though there's no breeze. Portraits of long-dead family members line the walls, their eyes seeming to follow your every move. One painting has eyes that seem particularly alive, almost... knowing. Beneath it, a small box sits on a dusty table, locked with an ornate mechanism. An inscription glows faintly in the candlelight.",
  "status": "hallway_entered",
  "hint": "\"Truth opens what fear locks.\"",
  "progress": 2
}
```
If the door is not unlocked yet:
```json
{
  "message": "The door to the hallway is locked. You cannot proceed without unlocking it first. The house whispers: 'You must earn your passage...'",
  "status": "access_denied",
  "hint": "Return to the parlor and unlock the door.",
  "progress": 0
}
```
If the Hallway is already explored:
```json
{
  "message": "You've already explored this hallway. The way forward lies at the end of your journey.",
  "status": "hallway_already_explored",
  "hint": null,
  "progress": 2
}
```
4.**POST** api/escape

**Goal:** 

The player must use the right “key” (for example { "key": "truth" }) to break the curse and escape.

**Success Narrative:**

The manor trembles as the walls begin to fade. You step into the moonlight — free at last. Behind you, the house crumbles into mist.

Response:
```json
{
  "message": "You speak the word 'truth' into the silence. The house shudders violently. The portraits begin to smile, their eyes closing peacefully for the first time in centuries. The manor trembles as the walls begin to fade like morning mist. Light floods through dissolving windows. You step into the moonlight — free at last. Behind you, Blackwood Manor crumbles into silvery mist, its spirits finally released. The curse is broken.",
  "status": "escaped_successfully",
  "hint": "Congratulations! You have survived the Haunting of Blackwood Manor!",
  "progress": 3
}
```
**Failure Narrative:**

A ghostly laughter echoes through the house. You weren’t ready. The door slams shut again.

Response:
```json
{
  "message": "😈 A ghostly laughter echoes through the house, growing louder and more sinister. You weren't ready. The door slams shut again with a deafening bang. The candles extinguish one by one. In the darkness, you hear footsteps... approaching... closer... closer... Then silence. The curse remains unbroken.",
  "status": "escape_failed",
  "hint": "The house demands truth. What is the key to breaking the curse?",
  "progress": 2
}
```
If you attempt to escape too early the response will be:
```json
{
  "message": "⛔ You cannot escape yet. You haven't explored enough of the manor. The house won't let you leave so easily. Dark whispers fill your ears: 'Not yet... not yet...'",
  "status": "escape_premature",
  "hint": "Complete your journey through the manor first.",
  "progress": 0
}
```

5.**GET** /api/status

Stage 0 (default)
```json
{
"message": "You are in the parlor. The door awaits. POST to /door with the correct key.",
"status": "in_parlor",
"hint": null,
"progress": 0
}
```
Stage 1
```json
{
"message": "You've unlocked the door. Use GET /hallway to continue your journey.",
"status": "door_unlocked",
"hint": null,
"progress": 1
}
```
🕯Stage 2
```json
{
"message": "You're in the hallway. The final challenge awaits. POST to /escape with the key to freedom.",
"status": "in_hallway",
"hint": null,
"progress": 2
}
```
Stage 3
```json
{
"message": "You have escaped Blackwood Manor! The nightmare is over.",
"status": "escaped",
"hint": null,
"progress": 3
}
```
Unknown Stage
```json
{
"message": "Unknown location. The house is confused...",
"status": "lost",
"hint": null,
"progress": -1
}
```
### Game Logic

Each endpoint represents a puzzle requiring user interaction through correct request formation.

The game maintains a progress tracking:

 - Progress status (e.g., 0, 1, 2)

Incorrect requests trigger narrative feedback hinting at the correct solution.

### API Swagger documentation
To access to the api documentation with swagger:
> Swagger UI: http://localhost:8080/swagger-ui.html