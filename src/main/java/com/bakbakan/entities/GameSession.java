package com.bakbakan.entities;

public class GameSession {

    private HeroData selectedHero;

    public HeroData getSelectedHero() {
        return selectedHero;
    }

    public void setSelectedHero(HeroData selectedHero) {
        this.selectedHero = selectedHero;
    }

    public boolean hasSelectedHero() {
        return selectedHero != null;
    }

    public void reset() {
        selectedHero = null;
    }
}
