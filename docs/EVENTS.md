# Bakbakan-Kasaysayan — Event Catalogue

All custom events extend `javafx.event.Event` and are dispatched through the central
`com.bakbakan.events.GameEventBus` using their own `EventType`.

`ScreenChangeEvent` carries a `request` flag:

- `request = true` — a screen asks `ScreenManager` to navigate.
- `request = false` — `ScreenManager` notifies listeners that navigation completed.

`AudioManager` ignores requests and stops/disposes music on completed screen changes.
Screens then request their own music with `SoundRequestEvent`.

| Event name | Fired by | Handled by | Purpose |
|---|---|---|---|
| HitEvent | CombatSystem | HUD, AudioManager, SpecialMeter | A fighter's attack connected; carries attacker/target, final damage and whether it was blocked. |
| DamageEvent | CombatSystem | Hud | Health changed for a fighter; carries amount and remaining health. The HUD bars update only from this event. |
| ManaChangedEvent | Fighter / ManaSystem | HUD | Mana value changed; drives the mana bar. |
| SpecialReadyEvent | Fighter / CombatSystem | HUD, AudioManager | Special bar reached 100; show "SPECIAL READY". |
| SpecialUsedEvent | Fighter | HUD, AudioManager, CombatSystem | A hero's special move was activated; resets special bar and plays SFX. |
| SkillUsedEvent | Fighter | HUD, AudioManager, CombatSystem | A hero's skill was activated; plays skill SFX and applies mana cost. |
| RoundStartEvent | RoundManager | HUD, AudioManager, ScreenManager | A new round begins; carries round number and fighters. |
| RoundEndEvent | RoundManager | ScreenManager, HUD, AudioManager | A round finished; carries round number, winner and reason. |
| TimerTickEvent | RoundManager | HUD | Round timer updated; carries seconds remaining. |
| TimeOutEvent | RoundManager | ScreenManager, HUD | Round timer expired; carries both health values and result. |
| FighterDefeatedEvent | CombatSystem | RoundManager, HUD, AudioManager | A fighter's health reached zero after a hit. |
| GameOverEvent | RoundManager | ScreenManager, AudioManager | Player lost; game over screen (Continue restarts Round 1). |
| VictoryEvent | RoundManager | ScreenManager, AudioManager | Player beat Round 5; final victory screen. |
| PauseEvent | InputHandler / ScreenManager | FightScreen, HUD | Game paused; show pause overlay. |
| ResumeEvent | InputHandler / ScreenManager | FightScreen, HUD | Game resumed; hide pause overlay. |
| ScreenChangeEvent | Screens (request), ScreenManager (notification) | ScreenManager (requests), AudioManager (notifications) | Request or announce a screen transition. Requests are handled synchronously by `ScreenManager`. |
| SoundRequestEvent | Screens, CombatSystem | AudioManager | Request music or a sound effect by id. |

## Phase 2 screen usage

| Screen | Music requested | Navigation |
|---|---|---|
| LoadingScreen | `loading` (loop) | Fires `ScreenChangeEvent(..., "MainMenu", true)` after assets load. |
| MainMenuScreen | `menu` (loop) | Play -> `HeroSelect`; Quit -> `Platform.exit()`. |
| HeroSelectScreen | `menu` (loop) | Select -> stores `HeroData` in `GameSession`, then `Fight`; Back -> `MainMenu`. |
| FightScreen | `battle` (loop) | Change Hero -> `HeroSelect`; Main Menu -> `MainMenu`. |

## Phase 3 combat flow

1. `FightScreen` forwards `KeyEvent`s to `InputHandler`, which tracks held keys and one-shot presses.
2. A/D move, W jumps, S ducks (hold), J attacks, L blocks (hold). Esc/P are reserved for Phase 6.
3. `Fighter` applies gravity, ground collision, screen bounds, facing and attack cooldown.
4. `CombatSystem` tests the attacker hitbox against the defender hurtbox.
5. A blocked melee hit deals 30% damage (70% reduction) and uses shorter hit stun.
6. `CombatSystem` fires `HitEvent`, `DamageEvent`, a `hit`/`block` `SoundRequestEvent`,
   and `FighterDefeatedEvent` when health reaches zero.
7. `Hud` updates both health bars exclusively from `DamageEvent`.
8. `FightScreen` fires `jump` SFX and requests looping `battle` music.

`SpriteProvider` draws every hero procedurally in all states. If a PNG exists at
`/sprites/<heroId>/<state>.png`, that image is used instead.

## Audio ids

- Music: `loading`, `menu`, `battle`, `victory`, `gameover`.
- SFX: `ui_click`, `jump`, `hit`, `block`, `round_start`, `ko`,
  plus `skill_<heroId>` and `special_<heroId>` for all five heroes.

Missing files are replaced with generated WAV tones in the system temp folder.
