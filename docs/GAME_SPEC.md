# Bakbakan-Kasaysayan — Game Spec

1. OVERVIEW
- Single player 1v1 fighting game vs CPU. Theme: Philippine national heroes. 5 rounds. Cartoon, family-friendly: no dismemberment or gore; damage and R specials use stylized blood splatter with droplets, streaks, and ground stains. Respectful portrayal of heroes.
- All in-game text, labels, cinematics, and result messages are in standard English.
- Window 1280x720, Canvas-based rendering, AnimationTimer game loop with delta time (cap dt at 0.05s).

2. SCREENS / FLOW
Loading -> Main Menu (Play, Quit) -> Hero Select (5 heroes, stats preview, mouse + keyboard) -> Swap Select (pick exactly 3 of the same 5 heroes as in-battle swap options) -> Fight -> (Pause overlay) -> Round Win screen ("You Win! Round cleared! Keep the streak alive." then Next Round) / Game Over screen ("You Lose! They got you this time. Get up and run it back!" with Continue = restart from Round 1, and Main Menu) / Final Victory screen after beating Round 5 ("Victory! Boss defeated! Your hero's legend is complete.").

3. CONTROLS
A/D move, Space jump, S duck (hold), J basic attack, K skill (costs mana), L block (hold), R special (needs full special bar), Tab swaps to a random one of the three pre-selected battle heroes (never the current hero) with a 5-second cooldown, Esc or P pause/resume, Enter/mouse for menus.

4. RULES
- Health 100, mana max 100 regenerating automatically every second; skill (K) costs mana and is blocked if mana is insufficient. The enemy resets every round. Every player hero keeps its own health, mana, and special for the whole run: damage, mana spent, and special-bar progress in round 1 persist through rounds 2-5 and the final boss, and swapping out and back restores that hero's stored values instead of healing or resetting. Only a new game restores everyone to full health/mana and an empty special bar.
- The first time a hero enters a run they arrive at full health and mana with an empty special bar. The swap plays an entrance move where the new hero drops from above, flying-kicks the enemy, and lands on the battlefield; combat pauses during the entrance.
- The player fields a team made of the three chosen swap heroes. Swapping has a 5-second cooldown that starts when the swap begins; it never blocks the automatic KO swap. When the active hero is KO'd, a living teammate is automatically swapped in and the round continues; the player is only defeated when all three team heroes are down (0 HP). Fallen heroes stay down for the rest of the run and cannot be swapped in.
- Special bar 0-100: fills when dealing and receiving damage. At 100 show "SPECIAL READY"; pressing R uses the hero's special and resets the bar to 0.
- Block reduces melee damage by 70%. Ducking avoids high projectiles.
- Round timer 1:00. On timeout the fighter with higher health wins; a tie counts as a loss for the player.
- Rounds 1-2: Spanish colonial soldiers (Scout, Guard). Rounds 3-4: American colonial soldiers (Rifleman, Sergeant). Difficulty rises each round. Round 5: a hero-specific historical final boss with higher health/damage. A warning/story cinematic plays before Round 5 and tells the true fate of the five heroes, highlighting the chosen hero. It has no timer and no skip; the player advances each slide with Enter only.
- Losing to the final boss shows a short hero-specific English legacy message on the Game Over screen (e.g., Rizal's words live on in every generation).
- Player loses any round -> Game Over -> Continue restarts from Round 1 (even if lost in round 2 or 3).
- Player wins rounds 1-4 -> "You Win" screen -> next round. Wins round 5 -> Final Victory screen.

5. HEROES (each has different speed, damage, range, defense, mana regen)
- Jose Rizal: ranged. J throws a book (projectile). K ink splash. R giant "Noli Me Tangere" book slams down for big damage.
- Andres Bonifacio: fast, short range. J bolo slash. K Katipunan flag dash. R "Sigaw ng Pugad Lawin" battle-cry shockwave.
- Antonio Luna: strong, slower. J sword slash. K rifle shot. R artillery barrage (multi-hit).
- Apolinario Mabini: tactical, fastest mana regen. J cane strike. K quill throw. R "Decalogue" scroll light beam.
- Lapulapu: heavy defender. J kampilan slash. K kampilan wave. R "Mactan tidal strike" wave.
FINAL BOSS (put in one easy-to-edit BossData config; historically accurate per selected hero):
- Jose Rizal -> Governor-General Camilo de Polavieja, who approved the death sentence carried out by firing squad at Bagumbayan.
- Andres Bonifacio -> Mayor Lazaro Makapagal, who led the execution squad at Maragondon under the Council of War.
- Antonio Luna -> Captain Pedro Janolino of the Presidential Guard at Cabanatuan.
- Apolinario Mabini -> General Arthur MacArthur, whose military government exiled Mabini to Guam; Mabini died of cholera, not by weapon.
- Lapulapu -> Ferdinand Magellan, defeated at Mactan. Lapulapu's own death is unrecorded, so Magellan serves as his historical nemesis.
- Each boss keeps the shared "final_boss" melee moveset and AI logic but uses a distinct procedural costume, headgear, weapon, skill, and special:
  - Polavieja: K "Saber Command" (gold command wave); R "Bagumbayan Firing Squad" (long-range volley).
  - Makapagal: K "Bolo Strike" (short bolo wave); R "Maragondon Volley" (three staggered rifle hits).
  - Janolino: K "Guard Rifle Burst" (three-shot burst); R "Cabanatuan Ambush" (three close-range sword slashes).
  - MacArthur: K "Heavy Rifle" (slow blue tracer); R "Colonial Bombardment" (single heavy shell).
  - Magellan: K "Arquebus" (slow heavy shot); R "Mactan Charge" (armored sword dash).

6. ASSETS
- No copyrighted material (no Street Fighter music or sprites).
- Sprites: draw placeholder characters procedurally with Canvas (distinct costume colors per hero) for states: idle, walk, jump, duck, block, attack, skill, special, hit, KO. Design a SpriteProvider so PNG sprite sheets placed in src/main/resources/sprites/<hero>/ are used automatically when present.
- Audio folders: src/main/resources/audio/music and audio/sfx. AudioManager loads a file if present, otherwise generates a simple placeholder WAV tone in a temp folder, so the game NEVER crashes when an audio file is missing. Music needed: loading, main menu, battle, victory, game over (loop where appropriate). SFX needed: UI click, jump, hit, block, round start, KO, each hero's skill and special.
- Keep strong references to MediaPlayer objects (otherwise they get garbage collected and stop). Use AudioClip for short SFX, MediaPlayer for music. Stop/dispose music on screen change.

7. ARCHITECTURE (EVENT-DRIVEN, MANDATORY)
- Packages under com.bakbakan: app, screens, entities, combat, ai, events, audio, assets, ui, util.
- Custom events extend javafx.event.Event, each with its own EventType: HitEvent, DamageEvent, ManaChangedEvent, SpecialChangedEvent, SpecialReadyEvent, SpecialUsedEvent, SkillUsedEvent, RoundStartEvent, RoundEndEvent, TimerTickEvent, TimeOutEvent, FighterDefeatedEvent, PlayerHeroDownEvent, GameOverEvent, VictoryEvent, PauseEvent, ResumeEvent, ScreenChangeEvent, SoundRequestEvent.
- A central GameEventBus (register/unregister EventHandler per EventType, fire). HUD, AudioManager, RoundManager and ScreenManager must communicate through events, not direct calls.
- Also use standard JavaFX events: KeyEvent (setOnKeyPressed/Released), MouseEvent (menu buttons hover/click), ActionEvent, WindowEvent (close request).
- Maintain docs/EVENTS.md: a table of event name | fired by | handled by | purpose.

8. QUALITY RULES (avoid bugs)
- Never block the JavaFX thread (no Thread.sleep). Update UI only on the FX thread.
- Remove handlers and stop timers/players when leaving a screen. No duplicate listeners.
- Request scene focus so key input works. Track pressed keys with a Set<KeyCode>.
- No NullPointerException on missing assets. No stub code that breaks compilation.
- Every phase must compile and run before you report.
