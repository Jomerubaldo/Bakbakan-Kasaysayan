# Bakbakan-Kasaysayan — Event Catalogue

All custom events extend `javafx.event.Event` and are dispatched through the central
`com.bakbakan.events.GameEventBus` using their own `EventType`.

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
| ScreenChangeEvent | ScreenManager | AudioManager, HUD | Active screen changed; triggers music switch and cleanup. |
| SoundRequestEvent | Any screen/system | AudioManager | Request a music track or SFX by id. |
