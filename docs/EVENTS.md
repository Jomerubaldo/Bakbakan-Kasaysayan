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

## Audio ids

- Music: `loading`, `menu`, `battle`, `victory`, `gameover`.
- SFX: `ui_click`, `jump`, `hit`, `block`, `no_mana`, `round_start`, `ko`,
  plus `skill_<heroId>` and `special_<heroId>` for all five heroes.

Missing files are replaced with generated WAV tones in the system temp folder.
