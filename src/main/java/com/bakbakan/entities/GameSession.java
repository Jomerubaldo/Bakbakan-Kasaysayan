package com.bakbakan.entities;

import com.bakbakan.combat.CombatConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameSession {

    private HeroData selectedHero;
    private HeroData activeHero;
    private List<HeroData> swapHeroes = new ArrayList<>();
    private int currentRound = 1;
    private int lastRoundNumber = 1;
    private boolean lastRoundWon;
    private String lastRoundReason = "";
    private boolean gameOver;
    private boolean victory;
    private final Map<String, double[]> heroResources = new HashMap<>();

    public HeroData getSelectedHero() {
        return selectedHero;
    }

    public void setSelectedHero(HeroData selectedHero) {
        this.selectedHero = selectedHero;
    }

    public boolean hasSelectedHero() {
        return selectedHero != null;
    }

    public HeroData getActiveHero() {
        return activeHero != null ? activeHero : selectedHero;
    }

    public void setActiveHero(HeroData activeHero) {
        this.activeHero = activeHero;
    }

    public List<HeroData> getSwapHeroes() {
        return swapHeroes;
    }

    public void setSwapHeroes(List<HeroData> heroes) {
        swapHeroes = heroes == null ? new ArrayList<>() : new ArrayList<>(heroes);
    }

    public boolean hasSwapHeroes() {
        return swapHeroes.size() == 3;
    }

    public void startNewGame(HeroData hero) {
        this.selectedHero = hero;
        this.activeHero = hero;
        swapHeroes = new ArrayList<>();
        restartProgress();
    }

    public void restartProgress() {
        activeHero = selectedHero;
        currentRound = 1;
        lastRoundNumber = 1;
        lastRoundWon = false;
        lastRoundReason = "";
        gameOver = false;
        victory = false;
        clearHeroResources();
    }

    public void storeHeroResources(String heroId, double health, double mana) {
        storeHeroResources(heroId, health, mana, 0);
    }

    public void storeHeroResources(String heroId, double health, double mana,
                                   double special) {
        if (heroId == null) {
            return;
        }
        heroResources.put(heroId, new double[] {Math.max(0, health),
                Math.max(0, mana), Math.max(0, special)});
    }

    public void clearHeroResources() {
        heroResources.clear();
    }

    public boolean hasHeroResources(String heroId) {
        return heroId != null && heroResources.containsKey(heroId);
    }

    public double getHeroHealth(String heroId) {
        double[] values = heroResources.get(heroId);
        return values == null ? CombatConstants.MAX_HEALTH : values[0];
    }

    public double getHeroMana(String heroId) {
        double[] values = heroResources.get(heroId);
        return values == null ? CombatConstants.MAX_MANA : values[1];
    }

    public double getHeroSpecial(String heroId) {
        double[] values = heroResources.get(heroId);
        return values == null || values.length < 3 ? 0 : values[2];
    }

    public List<HeroData> getLivingSwapHeroes(String excludeHeroId) {
        List<HeroData> living = new ArrayList<>();
        for (HeroData hero : swapHeroes) {
            if (hero == null || (excludeHeroId != null
                    && excludeHeroId.equals(hero.id()))) {
                continue;
            }
            if (getHeroHealth(hero.id()) > 0) {
                living.add(hero);
            }
        }
        return living;
    }

    public boolean hasLivingSwapHero(String excludeHeroId) {
        return !getLivingSwapHeroes(excludeHeroId).isEmpty();
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
        activeHero = null;
        swapHeroes = new ArrayList<>();
        restartProgress();
    }
}
