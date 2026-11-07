package com.bakbakan.combat;

import com.bakbakan.entities.EnemyData;
import com.bakbakan.entities.Fighter;
import com.bakbakan.entities.GameSession;
import com.bakbakan.events.FighterDefeatedEvent;
import com.bakbakan.events.GameEventBus;
import com.bakbakan.events.GameOverEvent;
import com.bakbakan.events.RoundEndEvent;
import com.bakbakan.events.RoundStartEvent;
import com.bakbakan.events.SoundRequestEvent;
import com.bakbakan.events.TimeOutEvent;
import com.bakbakan.events.TimerTickEvent;
import com.bakbakan.events.VictoryEvent;
import javafx.event.EventHandler;

public class RoundManager {

    private static final double PLAYER_START_X = 380;
    private static final double OPPONENT_START_X = 900;

    private final GameEventBus bus;
    private final GameSession session;
    private final EventHandler<FighterDefeatedEvent> defeatedHandler = this::onFighterDefeated;

    private Fighter player;
    private Fighter opponent;
    private EnemyData enemy;
    private int roundNumber = 1;
    private double timeRemaining = CombatConstants.ROUND_TIME_SECONDS;
    private int lastReportedSecond = CombatConstants.ROUND_TIME_SECONDS;
    private boolean running;
    private boolean finished;
    private boolean attached;

    public RoundManager(GameEventBus bus, GameSession session) {
        this.bus = bus;
        this.session = session;
    }

    public void attach() {
        if (attached) {
            return;
        }
        bus.register(FighterDefeatedEvent.FIGHTER_DEFEATED, defeatedHandler);
        attached = true;
    }

    public void detach() {
        if (!attached) {
            return;
        }
        bus.unregister(FighterDefeatedEvent.FIGHTER_DEFEATED, defeatedHandler);
        attached = false;
    }

    public void startRound(Fighter player, Fighter opponent, EnemyData enemy) {
        this.player = player;
        this.opponent = opponent;
        this.enemy = enemy;
        this.roundNumber = session.getCurrentRound();
        this.timeRemaining = CombatConstants.ROUND_TIME_SECONDS;
        this.lastReportedSecond = (int) Math.ceil(timeRemaining);
        this.running = true;
        this.finished = false;

        player.resetForRound();
        opponent.resetForRound();
        player.setPosition(PLAYER_START_X, CombatConstants.GROUND_Y);
        opponent.setPosition(OPPONENT_START_X, CombatConstants.GROUND_Y);

        bus.fire(new RoundStartEvent(roundNumber,
                player.getHero().id(), opponent.getId()));
        bus.fire(new TimerTickEvent(lastReportedSecond));
        bus.fire(new SoundRequestEvent("round_start", false));
    }

    public void update(double dt) {
        if (!running || finished || player == null || opponent == null) {
            return;
        }

        timeRemaining = Math.max(0, timeRemaining - dt);
        int seconds = (int) Math.ceil(timeRemaining);
        if (seconds != lastReportedSecond) {
            lastReportedSecond = seconds;
            bus.fire(new TimerTickEvent(seconds));
        }

        if (timeRemaining <= 0) {
            boolean playerWon = player.getHealth() > opponent.getHealth();
            bus.fire(new TimeOutEvent(player.getHealth(), opponent.getHealth(), playerWon));
            finishRound(playerWon, "TIME OUT");
        }
    }

    private void onFighterDefeated(FighterDefeatedEvent event) {
        if (!running || finished || player == null || opponent == null) {
            return;
        }
        if (player.getId().equals(event.getFighterId())) {
            finishRound(false, "KO");
        } else if (opponent.getId().equals(event.getFighterId())) {
            finishRound(true, "KO");
        }
    }

    private void finishRound(boolean playerWon, String reason) {
        if (finished) {
            return;
        }
        finished = true;
        running = false;

        session.setLastRoundResult(roundNumber, playerWon, reason);
        bus.fire(new RoundEndEvent(roundNumber, playerWon, reason));
        if ("KO".equals(reason)) {
            bus.fire(new SoundRequestEvent("ko", false));
        }

        if (playerWon) {
            if (roundNumber >= CombatConstants.TOTAL_ROUNDS) {
                session.setVictory(true);
                bus.fire(new VictoryEvent(roundNumber, player.getHero().id()));
            } else {
                session.advanceRound();
            }
        } else {
            session.setGameOver(true);
            bus.fire(new GameOverEvent(roundNumber, reason));
        }
    }

    public boolean isRunning() {
        return running && !finished;
    }

    public boolean isFinished() {
        return finished;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public double getTimeRemaining() {
        return timeRemaining;
    }

    public EnemyData getEnemy() {
        return enemy;
    }
}
