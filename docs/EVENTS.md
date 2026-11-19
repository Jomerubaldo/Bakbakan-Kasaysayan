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
| HitEvent | CombatSystem | FightScreen | A fighter's attack connected; carries attacker/target, final damage, whether it was blocked, and whether it came from an R special. The fight screen spawns blood droplets, with a heavier burst on special hits. |
| DamageEvent | CombatSystem | Hud | Health changed for a fighter; carries amount and remaining health. The HUD health bars update only from this event. |
| ManaChangedEvent | Fighter | Hud | Mana changed from regeneration or a skill cost; the HUD mana bars update only from this event. |
| SpecialChangedEvent | Fighter | Hud | Special bar value changed (damage gain, special use, swap restore, or round restore); carries the current and max values plus a ready flag. The HUD special bars update only from this event. |
| SpecialReadyEvent | Fighter | Hud | Special bar reached 100; the HUD shows "SPECIAL READY". |
| SpecialUsedEvent | Fighter | Hud | A special was activated; the HUD resets that fighter's special bar. |
| SkillUsedEvent | Fighter | — | Signals a successful skill activation after mana was spent. Skill SFX is requested separately with `SoundRequestEvent`. |
| RoundStartEvent | RoundManager | HUD | A new round begins; the HUD resets the timer and round number, then applies the carried health/mana/special values from the sync events. |
| RoundEndEvent | RoundManager | FightScreen | A round finished; the fight screen freezes combat and requests the matching result screen after a short delay. |
| TimerTickEvent | RoundManager | HUD | Round timer updated; carries seconds remaining. |
| TimeOutEvent | RoundManager | HUD, FightScreen | Round timer expired; carries both health values and result. The HUD stops the clock and the fight screen shows "TIME OUT". |
| FighterDefeatedEvent | CombatSystem | RoundManager | A fighter's health reached zero after a hit; the round manager resolves the KO. |
| PlayerHeroDownEvent | RoundManager | FightScreen | The player's active hero was KO'd but at least one swap hero still has health; the fight screen automatically swaps in a living teammate. |
| GameOverEvent | RoundManager | FightScreen | Player lost; the fight screen requests the Game Over screen (Continue restarts Round 1). |
| VictoryEvent | RoundManager | FightScreen | Player beat Round 5; the fight screen requests the final Victory screen. |
| PauseEvent | FightScreen | FightScreen, AudioManager | Game paused; show the pause overlay, freeze combat/timer/AI, and pause music. |
| ResumeEvent | FightScreen | FightScreen, AudioManager | Game resumed; hide the pause overlay, continue from the frozen state, and resume music. |
| ScreenChangeEvent | Screens (request), ScreenManager (notification) | ScreenManager (requests), AudioManager (notifications) | Request or announce a screen transition. Requests are handled synchronously by `ScreenManager`. |
| SoundRequestEvent | Screens, Fighter, CombatSystem | AudioManager | Request music or a sound effect by id. |

## Phase 2 screen usage

| Screen | Music requested | Navigation |
|---|---|---|
| LoadingScreen | `loading` (loop) | Fires `ScreenChangeEvent(..., "MainMenu", true)` after assets load. |
| MainMenuScreen | `menu` (loop) | Play -> `HeroSelect`; Quit -> `Platform.exit()`. |
| HeroSelectScreen | `menu` (loop) | Select -> stores `HeroData` in `GameSession`, then `SwapSelect`; Back -> `MainMenu`. |
| SwapSelectScreen | `menu` (loop) | Picks exactly three of the five heroes and stores them as the battle swap pool in `GameSession`; Start -> `Fight`; Back -> `HeroSelect`. |
| FightScreen | `battle` (loop) | Change Hero -> `HeroSelect`; Main Menu -> `MainMenu`. |

## Phase 3 combat flow

1. `FightScreen` forwards `KeyEvent`s to `InputHandler`, which tracks held keys and one-shot presses.
2. A/D move, W jumps, S ducks (hold), J attacks, L blocks (hold). Esc/P
   toggle the pause overlay.
3. `Fighter` applies gravity, ground collision, screen bounds, facing and attack cooldown.
4. `CombatSystem` tests the attacker hitbox against the defender hurtbox.
5. A blocked melee hit deals 30% damage (70% reduction) and uses shorter hit stun.
6. `CombatSystem` fires `HitEvent`, `DamageEvent`, a `hit`/`block` `SoundRequestEvent`,
   and `FighterDefeatedEvent` when health reaches zero.
7. `Hud` updates both health bars exclusively from `DamageEvent`.
8. `FightScreen` fires `jump` SFX and requests looping `battle` music.

## Hero swapping

1. `SwapSelectScreen` requires exactly three heroes chosen from the same five
   `HeroData` entries and stores them with `GameSession.setSwapHeroes`.
2. `FightScreen` shows the three choices in a small swap panel. The `Tab` key
   randomly swaps to one of the other heroes in that pool (never the current
   hero).
3. `Fighter.swapHero` changes the hero identity and resets action state while
   restoring the incoming hero's stored special bar. `FightScreen` stores the
   outgoing hero's health, mana and special in `GameSession` and restores the
   incoming hero's own stored values, so each hero keeps consistent health,
   mana and special across swaps and rounds. A hero's first appearance in a run
   starts at full health and mana with an empty special bar. HUD health, mana
   and special update through `DamageEvent`, `ManaChangedEvent` and
   `SpecialChangedEvent`. `GameSession.setActiveHero` keeps the current hero
   across rounds, while the final boss stays based on the hero originally
   chosen on the Hero Select screen.
4. Swapping is refused mid-attack, mid-skill, mid-special, mid-hit-stun or
   while airborne, so each swap happens cleanly.
5. A successful swap plays a `SwapEntrance` animation: the incoming hero drops
   from above the screen, dives into a flying kick that damages and knocks the
   enemy back, then lands on the battlefield. Combat, AI and the timer freeze
   for the duration and resume when the hero lands.
6. The player's team is the three chosen swap heroes. If the active hero is
   KO'd and any teammate still has health, `RoundManager` fires
   `PlayerHeroDownEvent` instead of ending the round, and `FightScreen`
   auto-swaps to a random living teammate through the same entrance move. The
   round is lost only when every team hero is down. Fallen heroes stay at 0 HP
   across rounds and are skipped by `Tab` random swaps.
7. Every swap starts a 5-second `SWAP_COOLDOWN_SECONDS` timer. The swap panel
   shows the remaining seconds and a progress bar, and `Tab` during the
   cooldown only flashes a "SWAP COOLDOWN" notice. The cooldown never blocks the
   automatic KO swap and resets when a new round starts.

`SpriteProvider` draws every hero procedurally in all states. If a PNG exists at
`/sprites/<heroId>/<state>.png`, that image is used instead.

## Phase 4 skills, specials and resources

1. `Fighter` owns mana and special. Mana regenerates once per second at
   `4 + manaRegen * 0.09`; every change fires `ManaChangedEvent`.
2. K spends 30 mana and fires `SkillUsedEvent` plus `skill_<heroId>`. With less
   than 30 mana the skill is refused, `no_mana` plays, and "NO MANA" floats above
   the player.
3. R requires a full special bar. It fires `SpecialUsedEvent` plus
   `special_<heroId>`, resets the bar to 0, and starts the hero special.
4. `SkillSystem` spawns code-drawn projectiles and area effects. All damage goes
   through `CombatSystem.applyHit`, so every hit fires `HitEvent`, `DamageEvent`
   and the `hit`/`block` sound.
5. After a hit, the attacker gains `damage * 1.15` special and the defender gains
   `damage * 0.9`. At 100 the fighter fires `SpecialReadyEvent`.
6. The HUD updates health from `DamageEvent`, mana from `ManaChangedEvent`, and
   special from `SpecialChangedEvent`.
7. High projectiles and the Decalogue beam pass over a ducking fighter. Low
   projectiles, waves, explosions and the giant book still hit.
8. Rizal's J is a thrown book projectile; other heroes keep melee J attacks.

| Hero | K skill | R special | Projectile/effect |
|---|---|---|---|
| Rizal | Ink splash | Giant Noli book slam | Ink projectile; falling giant book |
| Bonifacio | Katipunan flag dash | Sigaw ng Pugad Lawin shockwave | Forward dash; expanding ring |
| Luna | Rifle shot | Artillery barrage | Fast high bullet; three explosions |
| Mabini | Quill throw | Decalogue light beam | High quill; high horizontal beam |
| Lapulapu | Kampilan wave | Mactan tidal strike | Low wave; large low tidal wave |

## Phase 5 rounds, AI and results

1. `FightScreen.onShow()` reads `GameSession.getCurrentRound()`, builds an
   `EnemyData` with `EnemyData.forRound(round, hero)`, creates both fighters and
   attaches the HUD, `RoundManager` and `EnemyAI`.
2. `RoundManager.startRound()` resets the enemy fully and resets the player's
   action state. The active player hero's health, mana and special are restored
   from `GameSession`'s per-hero resource map, so damage, mana spent and
   special-bar progress in earlier rounds persist all the way to the final boss.
   It then places both fighters, fires `RoundStartEvent`, sync events for the
   carried values, `TimerTickEvent(180)` and `round_start`.
3. When the player wins a round that is not the last, `RoundManager` stores the
   active hero's remaining health, mana and special in `GameSession` before
   advancing. Swapping also stores the outgoing hero's values. The map is
   cleared on a new game or Continue restart.
4. Every second `RoundManager` fires `TimerTickEvent`; at 0 it fires
   `TimeOutEvent` and awards the win to the fighter with more health. A tie is a
   player loss.
5. `CombatSystem` fires `FighterDefeatedEvent` on a KO. If the player's active
   hero falls and a swap hero is still alive, `RoundManager` fires
   `PlayerHeroDownEvent` and the round continues with an automatic swap. Only
   when no team hero remains does it record `RoundEndEvent`, then advance to the
   next round, fire `VictoryEvent` after round 5, or fire `GameOverEvent` on a
   loss.
6. `FightScreen` freezes combat for 1.35 seconds after `RoundEndEvent`, then
   requests `RoundWin`, `GameOver` or `Victory` through `ScreenChangeEvent`.
7. `EnemyAI` is a state machine (`APPROACH`, `RETREAT`, `ATTACK`, `BLOCK`,
   `DUCK`, `WAIT`, `JUMP`). Reaction time, aggression, block/duck chance, skill
   chance and special chance scale up each round. It uses the same
   `SkillSystem` and `CombatSystem` as the player, so AI hits fire the same
   `HitEvent`, `DamageEvent` and sound events.
8. Rounds 1-4 use generic colonial soldiers with increasing damage. Round 5
   uses `BossData` (150 health, 1.4x damage) and the HUD shows a `BOSS` tag and
   the fight screen shows a BOSS banner.
9. `ResultScreen` displays:
   - RoundWin: "You Win!" / "Congratulations!" with Next Round and Main Menu.
   - GameOver: "You Lose!" / "Better luck next time." with Continue (restarts
     from Round 1) and Main Menu.
   - Victory: "Victory!" / "Congrats! You finally defeated the boss!" with
     Main Menu.
9. `ResultScreen` requests `victory` music for RoundWin/Victory and `gameover`
   music for GameOver.

## Phase 6 pause, resume and lifecycle

1. `FightScreen` owns the pause state. Esc or P fires `PauseEvent`; pressing
   Esc/P again, choosing Resume, or clicking the Resume button fires
   `ResumeEvent`.
2. `FightScreen.onPause()` clears input, zeroes both fighters' movement input,
   and shows the pause overlay. `update()` returns immediately while paused, so
   the round timer, enemy AI, projectiles, cooldowns, and result delay all freeze.
3. `FightScreen.onResume()` clears stale input, hides the overlay, and lets the
   next frame continue from the exact frozen state. No time is accumulated while
   paused.
4. The pause overlay uses real JavaFX `Button` nodes. Their `setOnAction`
   handlers are the project's `ActionEvent` usage: Resume fires `ResumeEvent`;
   Quit to Main Menu fires `ResumeEvent` (to unpause audio), then requests the
   `MainMenu` screen.
5. `AudioManager` handles `PauseEvent` by pausing the current `MediaPlayer` and
   handles `ResumeEvent` by resuming it only if it was playing when paused.
   Leaving a screen still stops and disposes the player.
6. `ScreenManager` listens to the stage focus property. On focus loss it clears
   the tracked key set and calls `Screen.onFocusLost()`; `FightScreen` clears its
   input handler and fighter movement so keys cannot remain stuck.
7. `Screen.onHide()` / `ScreenManager.stop()` detach event handlers and stop the
   shared `AnimationTimer`. No per-screen timers or duplicate listeners remain.

## Audio ids

- Music: `loading`, `menu`, `battle`, `victory`, `gameover`.
- SFX: `ui_click`, `jump`, `hit`, `block`, `no_mana`, `round_start`, `ko`,
  plus `skill_<heroId>` and `special_<heroId>` for all five heroes.

Missing files are replaced with generated WAV tones in the system temp folder.
