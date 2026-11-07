package com.bakbakan.entities;

import java.util.List;

public record BossData(
        String playerHeroId,
        String name,
        String weapon,
        String description,
        double maxHealth,
        double damageMultiplier,
        String primaryColor,
        String accentColor) {

    public static final double DEFAULT_BOSS_HEALTH = 150;
    public static final double DEFAULT_BOSS_DAMAGE_MULTIPLIER = 1.4;

    private static final List<BossData> ALL = List.of(
            new BossData(
                    "rizal",
                    "Firing Squad Commander",
                    "Rifle",
                    "A stern colonial commander who leads the firing squad.",
                    DEFAULT_BOSS_HEALTH, DEFAULT_BOSS_DAMAGE_MULTIPLIER,
                    "#7B1E1E", "#D4AF37"),
            new BossData(
                    "bonifacio",
                    "Colonial General",
                    "Saber",
                    "A decorated colonial general who defends the old order.",
                    DEFAULT_BOSS_HEALTH, DEFAULT_BOSS_DAMAGE_MULTIPLIER,
                    "#5A1F2A", "#C9A227"),
            new BossData(
                    "luna",
                    "Colonial Army Commander",
                    "Saber & Pistol",
                    "A disciplined army commander with a commander's pride.",
                    DEFAULT_BOSS_HEALTH, DEFAULT_BOSS_DAMAGE_MULTIPLIER,
                    "#3E335C", "#C0392B"),
            new BossData(
                    "mabini",
                    "Colonial Governor",
                    "Governor's Cane",
                    "A calculating governor who rules through strategy and fear.",
                    DEFAULT_BOSS_HEALTH, DEFAULT_BOSS_DAMAGE_MULTIPLIER,
                    "#243447", "#D4AF37"),
            new BossData(
                    "lapulapu",
                    "Ferdinand Magellan",
                    "Sword",
                    "The armored expedition commander who challenges Mactan.",
                    DEFAULT_BOSS_HEALTH, DEFAULT_BOSS_DAMAGE_MULTIPLIER,
                    "#6E2C24", "#C0C0C0")
    );

    public static BossData forHero(HeroData hero) {
        if (hero != null) {
            for (BossData boss : ALL) {
                if (boss.playerHeroId.equals(hero.id())) {
                    return boss;
                }
            }
        }
        return ALL.get(0);
    }

    public HeroData toHeroData(HeroData playerHero) {
        return new HeroData(
                playerHero.id(),
                name,
                weapon,
                description,
                playerHero.speed(),
                playerHero.damage(),
                playerHero.range(),
                playerHero.defense(),
                playerHero.manaRegen(),
                primaryColor,
                accentColor);
    }

    public EnemyData toEnemyData(HeroData playerHero) {
        return new EnemyData(name, toHeroData(playerHero),
                maxHealth, damageMultiplier, true);
    }
}
