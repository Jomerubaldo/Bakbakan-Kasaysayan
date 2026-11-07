# Bakbakan-Kasaysayan

A single-player 1v1 fighting game starring five Philippine national heroes.
Fight five rounds against colonial soldiers and a final boss, using melee,
projectiles, skills, specials, blocking, ducking, and jumping.

The game is a family-friendly fan tribute. All art and audio are original or
procedurally generated; no copyrighted game assets are included.

## Requirements

- JDK 21 (tested with `C:\Program Files\Java\jdk-21.0.12.1`)
- The included Maven wrapper (`mvnw.cmd` on Windows, `./mvnw` on Unix-like systems)
- No external assets are required. Missing sprites and audio fall back safely.

## Run

Windows PowerShell:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12.1"
.\mvnw.cmd clean javafx:run
```

If `JAVA_HOME` is already set correctly:

```powershell
.\mvnw.cmd javafx:run
```

Compile only:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12.1"
.\mvnw.cmd -q clean compile
```

## Controls

| Input | Action |
|---|---|
| `A` / `D` or Left / Right | Move |
| `W` or Up | Jump |
| `S` or Down (hold) | Duck |
| `J` | Basic attack |
| `K` | Skill (costs 30 mana) |
| `L` (hold) | Block |
| `E` | Special (requires a full special bar) |
| `Esc` or `P` | Pause / resume |
| `Enter` / `Space` | Confirm menu selection |
| Mouse | Hover and click menus, cards, and pause buttons |

## Game Flow

`Loading` -> `MainMenu` -> `HeroSelect` -> `Fight` -> result screen.

- Win rounds 1-4: `RoundWin` -> next round.
- Lose any round: `GameOver` -> Continue restarts from Round 1.
- Beat round 5: `Victory`.
- Pause overlay: Resume or Quit to Main Menu.

## Project Layout

- `src/main/java/com/bakbakan/app` - JavaFX application entry point and screen registration.
- `src/main/java/com/bakbakan/screens` - Loading, menu, hero select, fight, result, and screen manager.
- `src/main/java/com/bakbakan/entities` - Heroes, fighters, enemies, bosses, and game session.
- `src/main/java/com/bakbakan/combat` - Combat, input, round, projectile, and skill systems.
- `src/main/java/com/bakbakan/ai` - Enemy AI state machine.
- `src/main/java/com/bakbakan/events` - Custom events and the central `GameEventBus`.
- `src/main/java/com/bakbakan/audio` - Music and SFX playback with safe placeholders.
- `src/main/java/com/bakbakan/assets` - Asset manifest, sprite lookup, and procedural sprites.
- `src/main/java/com/bakbakan/ui` - HUD.
- `src/main/java/com/bakbakan/util` - Game configuration.
- `docs/GAME_SPEC.md` - Game specification.
- `docs/EVENTS.md` - Event catalogue and phase-by-phase event flow.

## Sprite Assets

Sprites are individual PNG files, one per hero state. Place them here:

```text
src/main/resources/sprites/<heroId>/<state>.png
```

Hero IDs:

| Hero | Folder |
|---|---|
| Jose Rizal | `src/main/resources/sprites/rizal/` |
| Andres Bonifacio | `src/main/resources/sprites/bonifacio/` |
| Antonio Luna | `src/main/resources/sprites/luna/` |
| Apolinario Mabini | `src/main/resources/sprites/mabini/` |
| Lapulapu | `src/main/resources/sprites/lapulapu/` |

Exact state filenames:

```text
idle.png
walk.png
jump.png
duck.png
block.png
attack.png
skill.png
special.png
hit.png
ko.png
```

Example:

```text
src/main/resources/sprites/rizal/idle.png
src/main/resources/sprites/rizal/walk.png
src/main/resources/sprites/rizal/jump.png
src/main/resources/sprites/rizal/duck.png
src/main/resources/sprites/rizal/block.png
src/main/resources/sprites/rizal/attack.png
src/main/resources/sprites/rizal/skill.png
src/main/resources/sprites/rizal/special.png
src/main/resources/sprites/rizal/hit.png
src/main/resources/sprites/rizal/ko.png
```

Any missing state falls back to the procedural `SpriteProvider` drawing, so the
game still runs with zero PNG files.

## Audio Assets

The game accepts `.wav`, `.mp3`, and `.aiff` files. It checks extensions in that
order. Use lowercase filenames matching the IDs below.

### Music

Folder:

```text
src/main/resources/audio/music/
```

Required IDs and exact filenames with the preferred extension:

```text
src/main/resources/audio/music/loading.wav
src/main/resources/audio/music/menu.wav
src/main/resources/audio/music/battle.wav
src/main/resources/audio/music/victory.wav
src/main/resources/audio/music/gameover.wav
```

`loading`, `menu`, and `battle` loop. `victory` and `gameover` play once.

### SFX

Folder:

```text
src/main/resources/audio/sfx/
```

Base SFX:

```text
src/main/resources/audio/sfx/ui_click.wav
src/main/resources/audio/sfx/jump.wav
src/main/resources/audio/sfx/hit.wav
src/main/resources/audio/sfx/block.wav
src/main/resources/audio/sfx/round_start.wav
src/main/resources/audio/sfx/ko.wav
src/main/resources/audio/sfx/no_mana.wav
```

Hero skill and special SFX:

```text
src/main/resources/audio/sfx/skill_rizal.wav
src/main/resources/audio/sfx/skill_bonifacio.wav
src/main/resources/audio/sfx/skill_luna.wav
src/main/resources/audio/sfx/skill_mabini.wav
src/main/resources/audio/sfx/skill_lapulapu.wav
src/main/resources/audio/sfx/special_rizal.wav
src/main/resources/audio/sfx/special_bonifacio.wav
src/main/resources/audio/sfx/special_luna.wav
src/main/resources/audio/sfx/special_mabini.wav
src/main/resources/audio/sfx/special_lapulapu.wav
```

Missing audio is replaced with a generated WAV tone in the system temp folder.
The game never crashes because an audio file is absent.

## Event-Driven Architecture

All custom events extend `javafx.event.Event` and are dispatched through the
central `com.bakbakan.events.GameEventBus`. Screens, the HUD, the round manager,
and the audio manager communicate through events rather than direct calls.

Custom events include:

- `HitEvent`, `DamageEvent`, `ManaChangedEvent`, `SpecialReadyEvent`,
  `SpecialUsedEvent`, `SkillUsedEvent`
- `RoundStartEvent`, `RoundEndEvent`, `TimerTickEvent`, `TimeOutEvent`,
  `FighterDefeatedEvent`, `GameOverEvent`, `VictoryEvent`
- `PauseEvent`, `ResumeEvent`, `ScreenChangeEvent`, `SoundRequestEvent`

See `docs/EVENTS.md` for the full event table.

Standard JavaFX events are used as follows:

| Event | Where it is used |
|---|---|
| `KeyEvent` | `ScreenManager` registers `setOnKeyPressed` / `setOnKeyReleased` on the scene and forwards events to the current `Screen`. `FightScreen` forwards combat keys to `InputHandler` and handles `Esc`/`P` for pause. |
| `MouseEvent` | `ScreenManager` forwards scene mouse movement and clicks to the current `Screen`. `MainMenuScreen`, `HeroSelectScreen`, `ResultScreen`, and `FightScreen` use it for hover and click selection. |
| `ActionEvent` | The pause overlay in `FightScreen` uses real JavaFX `Button` nodes. `resumeButton.setOnAction(...)` and `quitButton.setOnAction(...)` handle button activation. |
| `WindowEvent` | `ScreenManager` registers `stage.setOnCloseRequest(...)`, which exits the application. `MainApp.stop()` then stops the loop and disposes audio. |

Pause and resume are event-driven:

1. `FightScreen` fires `PauseEvent` when `Esc`/`P` is pressed.
2. `FightScreen` shows the overlay and freezes its update loop.
3. `AudioManager` pauses the current `MediaPlayer`.
4. `FightScreen` fires `ResumeEvent` to hide the overlay, continue from the
   frozen state, and resume music.
5. Quit to Main Menu fires `ResumeEvent`, then requests `ScreenChangeEvent` to
   `MainMenu`. Leaving the screen detaches handlers and disposes music.

## Verification

The final compile command is:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12.1"
.\mvnw.cmd -q clean compile
```

Phase 6 checks cover all five heroes: each starts at Round 1, Round 5 uses the
boss, pause freezes the round timer, resume continues from the frozen value,
and Quit from pause clears the pause state. The Game Over Continue path resets
the session to Round 1.

## Known Issues

- On Windows displays set to 125% scaling, the 1280x720 window may be clipped by
  the desktop work area. The game canvas itself remains 1280x720 and all HUD
  coordinates are correct.
- Generated placeholder audio depends on the system's JavaFX media support. If
  audio playback is unavailable, the game continues silently.
