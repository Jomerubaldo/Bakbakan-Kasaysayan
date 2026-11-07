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
| HitEvent | CombatSystem | Hud | A fighter's attack connected; carries attacker/target, final damage and whether it was blocked. The HUD uses it to fill special bars. |
| DamageEvent | CombatSystem | Hud | Health changed for a fighter; carries amount and remaining health. The HUD health bars update only from this event. |
| ManaChangedEvent | Fighter | Hud | Mana changed from regeneration or a skill cost; the HUD mana bars update only from this event. |
| SpecialReadyEvent | Fighter | Hud | Special bar reached 100; the HUD shows "SPECIAL READY". |
| SpecialUsedEvent | Fighter | Hud | A special was activated; the HUD resets that fighter's special bar. |
| SkillUsedEvent | Fighter | — | Signals a successful skill activation after mana was spent. Skill SFX is requested separately with `SoundRequestEvent`. |
| RoundStartEvent | RoundManager | HUD | A new round begins; the HUD resets health/mana/special and the timer, and shows the round number. |
| RoundEndEvent | RoundManager | FightScreen | A round finished; the fight screen freezes combat and requests the matching result screen after a short delay. |
| TimerTickEvent | RoundManager | HUD | Round timer updated; carries seconds remaining. |
| TimeOutEvent | RoundManager | HUD, FightScreen | Round timer expired; carries both health values and result. The HUD stops the clock and the fight screen shows "TIME OUT". |
| FighterDefeatedEvent | CombatSystem | RoundManager | A fighter's health reached zero after a hit; the round manager resolves the KO. |
| GameOverEvent | RoundManager | FightScreen | Player lost; the fight screen requests the Game Over screen (Continue restarts Round 1). |
| VictoryEvent | RoundManager | FightScreen | Player beat Round 5; the fight screen requests the final Victory screen. |
| PauseEvent | InputHandler / ScreenManager | FightScreen, HUD | Game paused; show pause overlay. |
| ResumeEvent | InputHandler / ScreenManager | FightScreen, HUD | Game resumed; hide pause overlay. |
| ScreenChangeEvent | Screens (request), ScreenManager (notification) | ScreenManager (requests), AudioManager (notifications) | Request or announce a screen transition. Requests are handled synchronously by `ScreenManager`. |
| SoundRequestEvent | Screens, Fighter, CombatSystem | AudioManager | Request music or a sound effect by id. |

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

## Phase 4 skills, specials and resources

1. `Fighter` owns mana and special. Mana regenerates once per second at
   `4 + manaRegen * 0.09`; every change fires `ManaChangedEvent`.
2. K spends 30 mana and fires `SkillUsedEvent` plus `skill_<heroId>`. With less
   than 30 mana the skill is refused, `no_mana` plays, and "NO MANA" floats above
   the player.
3. E requires a full special bar. It fires `SpecialUsedEvent` plus
   `special_<heroId>`, resets the bar to 0, and starts the hero special.
4. `SkillSystem` spawns code-drawn projectiles and area effects. All damage goes
   through `CombatSystem.applyHit`, so every hit fires `HitEvent`, `DamageEvent`
   and the `hit`/`block` sound.
5. After a hit, the attacker gains `damage * 1.15` special and the defender gains
   `damage * 0.9`. At 100 the fighter fires `SpecialReadyEvent`.
6. The HUD updates health from `DamageEvent`, mana from `ManaChangedEvent`, and
   special from `HitEvent` / `SpecialReadyEvent` / `SpecialUsedEvent`.
7. High projectiles and the Decalogue beam pass over a ducking fighter. Low
   projectiles, waves, explosions and the giant book still hit.
8. Rizal's J is a thrown book projectile; other heroes keep melee J attacks.

| Hero | K skill | E special | Projectile/effect |
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
2. `RoundManager.startRound()` resets both fighters' health, mana and special,
   places them at their start positions, and fires `RoundStartEvent`,
   `TimerTickEvent(180)` and `round_start`.
3. Every second `RoundManager` fires `TimerTickEvent`; at 0 it fires
   `TimeOutEvent` and awards the win to the fighter with more health. A tie is a
   player loss.
4. `CombatSystem` fires `FighterDefeatedEvent` on a KO; `RoundManager` records
   `RoundEndEvent`, then advances to the next round, fires `VictoryEvent` after
   round 5, or fires `GameOverEvent` on a loss.
5. `FightScreen` freezes combat for 1.35 seconds after `RoundEndEvent`, then
   requests `RoundWin`, `GameOver` or `Victory` through `ScreenChangeEvent`.
6. `EnemyAI` is a state machine (`APPROACH`, `RETREAT`, `ATTACK`, `BLOCK`,
   `DUCK`, `WAIT`, `JUMP`). Reaction time, aggression, block/duck chance, skill
   chance and special chance scale up each round. It uses the same
   `SkillSystem` and `CombatSystem` as the player, so AI hits fire the same
   `HitEvent`, `DamageEvent` and sound events.
7. Rounds 1-4 use generic colonial soldiers with increasing damage. Round 5
   uses `BossData` (150 health, 1.4x damage) and the HUD shows a `BOSS` tag and
   the fight screen shows a BOSS banner.
8. `ResultScreen` displays:
   - RoundWin: "You Win!" / "Congratulations!" with Next Round and Main Menu.
   - GameOver: "You Lose!" / "Better luck next time." with Continue (restarts
     from Round 1) and Main Menu.
   - Victory: "Victory!" / "Congrats! You finally defeated the boss!" with
     Main Menu.
9. `ResultScreen` requests `victory` music for RoundWin/Victory and `gameover`
   music for GameOver.

## Audio ids

- Music: `loading`, `menu`, `battle`, `victory`, `gameover`.
- SFX: `ui_click`, `jump`, `hit`, `block`, `no_mana`, `round_start`, `ko`,
  plus `skill_<heroId>` and `special_<heroId>` for all five heroes.

Missing files are replaced with generated WAV tones in the system temp folder.
