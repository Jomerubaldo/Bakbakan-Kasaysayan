# Bakbakan-Kasaysayan — Game Spec

1. OVERVIEW
- Single player 1v1 fighting game vs CPU. Theme: Philippine national heroes. 5 rounds. Cartoon, family-friendly: no blood, no gore. Respectful portrayal of heroes.
- Window 1280x720, Canvas-based rendering, AnimationTimer game loop with delta time (cap dt at 0.05s).

2. SCREENS / FLOW
Loading -> Main Menu (Play, Quit) -> Hero Select (5 heroes, stats preview, mouse + keyboard) -> Fight -> (Pause overlay) -> Round Win screen ("You Win! Congratulations!" then Next Round) / Game Over screen ("You Lose! Better luck next time." with Continue = restart from Round 1, and Main Menu) / Final Victory screen after beating Round 5 ("Congrats! You finally defeated the boss!").

3. CONTROLS
A/D move, W jump, S duck (hold), J basic attack, K skill (costs mana), L block (hold), E special (needs full special bar), Esc or P pause/resume, Enter/mouse for menus.

4. RULES
- Health 100 for both fighters, reset every round. Mana max 100, regenerates automatically every second; skill (K) costs mana and is blocked if mana is insufficient.
- Special bar 0-100: fills when dealing and receiving damage. At 100 show "SPECIAL READY"; pressing E uses the hero's special and resets the bar to 0.
- Block reduces melee damage by 70%. Ducking avoids high projectiles.
- Round timer 1:00. On timeout the fighter with higher health wins; a tie counts as a loss for the player.
- Rounds 1-2: Spanish colonial soldiers (Scout, Guard). Rounds 3-4: American colonial soldiers (Rifleman, Sergeant). Difficulty rises each round. Round 5: final boss "El Verdugo" with higher health/damage. A warning/story cinematic plays before Round 5 and tells the true fate of the five heroes, highlighting the chosen hero. It has no timer and no skip; the player advances each slide with Enter only.
- Losing to the final boss shows a short hero-specific legacy message on the Game Over screen (e.g., the hero's story lives on in the hearts of Filipinos and future generations).
- Player loses any round -> Game Over -> Continue restarts from Round 1 (even if lost in round 2 or 3).
- Player wins rounds 1-4 -> "You Win" screen -> next round. Wins round 5 -> Final Victory screen.

5. HEROES (each has different speed, damage, range, defense, mana regen)
- Jose Rizal: ranged. J throws a book (projectile). K ink splash. E giant "Noli Me Tangere" book slams down for big damage.
- Andres Bonifacio: fast, short range. J bolo slash. K Katipunan flag dash. E "Sigaw ng Pugad Lawin" battle-cry shockwave.
- Antonio Luna: strong, slower. J sword slash. K rifle shot. E artillery barrage (multi-hit).
- Apolinario Mabini: tactical, fastest mana regen. J cane strike. K quill throw. E "Decalogue" scroll light beam.
- Lapulapu: heavy defender. J kampilan slash. K kampilan wave. E "Mactan tidal strike" wave.
FINAL BOSS (put in one easy-to-edit BossData config; use a fictional archetype, not a real named person):
- "El Verdugo" — a fictional executioner/conqueror who embodies the forces behind the heroes' deaths. Per-hero descriptions reference the true historical circumstances: Rizal (firing squad at Bagumbayan), Bonifacio (Council of War execution), Luna (Presidential Guard at Cabanatuan), Mabini (American exile, died of cholera), Lapulapu (victory at Mactan; death unknown).

6. ASSETS
- No copyrighted material (no Street Fighter music or sprites).
- Sprites: draw placeholder characters procedurally with Canvas (distinct costume colors per hero) for states: idle, walk, jump, duck, block, attack, skill, special, hit, KO. Design a SpriteProvider so PNG sprite sheets placed in src/main/resources/sprites/<hero>/ are used automatically when present.
- Audio folders: src/main/resources/audio/music and audio/sfx. AudioManager loads a file if present, otherwise generates a simple placeholder WAV tone in a temp folder, so the game NEVER crashes when an audio file is missing. Music needed: loading, main menu, battle, victory, game over (loop where appropriate). SFX needed: UI click, jump, hit, block, round start, KO, each hero's skill and special.
- Keep strong references to MediaPlayer objects (otherwise they get garbage collected and stop). Use AudioClip for short SFX, MediaPlayer for music. Stop/dispose music on screen change.

7. ARCHITECTURE (EVENT-DRIVEN, MANDATORY)
- Packages under com.bakbakan: app, screens, entities, combat, ai, events, audio, assets, ui, util.
- Custom events extend javafx.event.Event, each with its own EventType: HitEvent, DamageEvent, ManaChangedEvent, SpecialReadyEvent, SpecialUsedEvent, SkillUsedEvent, RoundStartEvent, RoundEndEvent, TimerTickEvent, TimeOutEvent, FighterDefeatedEvent, GameOverEvent, VictoryEvent, PauseEvent, ResumeEvent, ScreenChangeEvent, SoundRequestEvent.
- A central GameEventBus (register/unregister EventHandler per EventType, fire). HUD, AudioManager, RoundManager and ScreenManager must communicate through events, not direct calls.
- Also use standard JavaFX events: KeyEvent (setOnKeyPressed/Released), MouseEvent (menu buttons hover/click), ActionEvent, WindowEvent (close request).
- Maintain docs/EVENTS.md: a table of event name | fired by | handled by | purpose.

8. QUALITY RULES (avoid bugs)
- Never block the JavaFX thread (no Thread.sleep). Update UI only on the FX thread.
- Remove handlers and stop timers/players when leaving a screen. No duplicate listeners.
- Request scene focus so key input works. Track pressed keys with a Set<KeyCode>.
- No NullPointerException on missing assets. No stub code that breaks compilation.
- Every phase must compile and run before you report.
