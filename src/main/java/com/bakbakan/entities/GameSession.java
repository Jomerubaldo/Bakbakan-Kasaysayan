package com.bakbakan.entities;

import com.bakbakan.combat.CombatConstants;

public class GameSession {

    private HeroData selectedHero;
    private int currentRound = 1;
    private int lastRoundNumber = 1;
    private boolean lastRoundWon;
    private String lastRoundReason = "";
    private boolean gameOver;
    private boolean victory;

    public HeroData getSelectedHero() {
        return selectedHero;
    }

    public void setSelectedHero(HeroData selectedHero) {
        this.selectedHero = selectedHero;
    }

    public boolean hasSelectedHero() {
        return selectedHero != null;
    }

    public void startNewGame(HeroData hero) {
        this.selectedHero = hero;
        restartProgress();
    }

    public void restartProgress() {
        currentRound = 1;
        lastRoundNumber = 1;
        lastRoundWon = false;
        lastRoundReason = "";
        gameOver = false;
        victory = false;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(int round) {
        currentRound = Math.max(1, Math.min(CombatConstants.TOTAL_ROUNDS, round));
    }

    public void advanceRound() {
        setCurrentRound(currentRound + 1);
    }

    public int getLastRoundNumber() {
        return lastRoundNumber;
    }

    public boolean isLastRoundWon() {
        return lastRoundWon;
    }

    public String getLastRoundReason() {
        return lastRoundReason;
    }

    public void setLastRoundResult(int round, boolean won, String reason) {
        lastRoundNumber = round;
        lastRoundWon = won;
        lastRoundReason = reason == null ? "" : reason;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public boolean isVictory() {
        return victory;
    }

    public void setVictory(boolean victory) {
        this.victory = victory;
    }

    public void reset() {
        selectedHero = null;
        restartProgress();
    }
}
