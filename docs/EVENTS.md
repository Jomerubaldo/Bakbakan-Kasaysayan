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
| HitEvent | CombatSystem | HUD, AudioManager, SpecialMeter | A fighter's attack connected; carries attacker/target, damage and whether it was blocked. |
| DamageEvent | CombatSystem | HUD, RoundManager | Health changed for a fighter; carries amount and remaining health. |
| ManaChangedEvent | Fighter / ManaSystem | HUD | Mana value changed; drives the mana bar. |
| SpecialReadyEvent | Fighter / CombatSystem | HUD, AudioManager | Special bar reached 100; show "SPECIAL READY". |
| SpecialUsedEvent | Fighter | HUD, AudioManager, CombatSystem | A hero's special move was activated; resets special bar and plays SFX. |
| SkillUsedEvent | Fighter | HUD, AudioManager, CombatSystem | A hero's skill was activated; plays skill SFX and applies mana cost. |
| RoundStartEvent | RoundManager | HUD, AudioManager, ScreenManager | A new round begins; carries round number and fighters. |
| RoundEndEvent | RoundManager | ScreenManager, HUD, AudioManager | A round finished; carries round number, winner and reason. |
| TimerTickEvent | RoundManager | HUD | Round timer updated; carries seconds remaining. |
| TimeOutEvent | RoundManager | ScreenManager, HUD | Round timer expired; carries both health values and result. |
| FighterDefeatedEvent | Fighter / CombatSystem | RoundManager, HUD, AudioManager | A fighter's health reached zero. |
| GameOverEvent | RoundManager | ScreenManager, AudioManager | Player lost; game over screen (Continue restarts Round 1). |
| VictoryEvent | RoundManager | ScreenManager, AudioManager | Player beat Round 5; final victory screen. |
| PauseEvent | InputHandler / ScreenManager | FightScreen, HUD | Game paused; show pause overlay. |
| ResumeEvent | InputHandler / ScreenManager | FightScreen, HUD | Game resumed; hide pause overlay. |
| ScreenChangeEvent | Screens (request), ScreenManager (notification) | ScreenManager (requests), AudioManager (notifications) | Request or announce a screen transition. Requests are handled synchronously by `ScreenManager`. |
| SoundRequestEvent | LoadingScreen, MainMenuScreen, HeroSelectScreen, FightScreen, future combat systems | AudioManager | Request music or a sound effect by id. |

## Phase 2 usage

| Screen | Music requested | Navigation |
|---|---|---|
| LoadingScreen | `loading` (loop) | Fires `ScreenChangeEvent(..., "MainMenu", true)` after assets load. |
| MainMenuScreen | `menu` (loop) | Play -> `HeroSelect`; Quit -> `Platform.exit()`. |
| HeroSelectScreen | `menu` (loop) | Select -> stores `HeroData` in `GameSession`, then `Fight`; Back -> `MainMenu`. |
| FightScreen (placeholder) | `battle` (loop) | Change Hero -> `HeroSelect`; Main Menu -> `MainMenu`. |

## Audio ids

- Music: `loading`, `menu`, `battle`, `victory`, `gameover`.
- SFX: `ui_click`, `jump`, `hit`, `block`, `round_start`, `ko`,
  plus `skill_<heroId>` and `special_<heroId>` for all five heroes.

Missing files are replaced with generated WAV tones in the system temp folder.
