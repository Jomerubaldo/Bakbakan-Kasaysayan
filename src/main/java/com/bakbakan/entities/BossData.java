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

    public static final String BOSS_ID = "final_boss";
    public static final double DEFAULT_BOSS_HEALTH = 160;
    public static final double DEFAULT_BOSS_DAMAGE_MULTIPLIER = 1.35;

    private static final String BOSS_NAME = "El Verdugo";
    private static final String BOSS_WEAPON = "Saber & Rifle";
    private static final String BOSS_PRIMARY = "#2A1520";

    private static final List<BossData> ALL = List.of(
            new BossData(
                    "rizal",
                    BOSS_NAME,
                    BOSS_WEAPON,
                    "The executioner who carried out the sentence at Bagumbayan.",
                    DEFAULT_BOSS_HEALTH, DEFAULT_BOSS_DAMAGE_MULTIPLIER,
                    BOSS_PRIMARY, "#D4AF37"),
            new BossData(
                    "bonifacio",
                    BOSS_NAME,
                    BOSS_WEAPON,
                    "The executioner of the brotherhood, sent by the Council of War.",
                    DEFAULT_BOSS_HEALTH, DEFAULT_BOSS_DAMAGE_MULTIPLIER,
                    BOSS_PRIMARY, "#C0392B"),
            new BossData(
                    "luna",
                    BOSS_NAME,
                    BOSS_WEAPON,
                    "The assassin of Cabanatuan, hidden among the Presidential Guard.",
                    DEFAULT_BOSS_HEALTH, DEFAULT_BOSS_DAMAGE_MULTIPLIER,
                    BOSS_PRIMARY, "#E74C3C"),
            new BossData(
                    "mabini",
                    BOSS_NAME,
                    BOSS_WEAPON,
                    "The cold hand of exile that broke the Sublime Paralytic.",
                    DEFAULT_BOSS_HEALTH, DEFAULT_BOSS_DAMAGE_MULTIPLIER,
                    BOSS_PRIMARY, "#D4AF37"),
            new BossData(
                    "lapulapu",
                    BOSS_NAME,
                    BOSS_WEAPON,
                    "The last conquistador, demanding vengeance for Mactan.",
                    DEFAULT_BOSS_HEALTH, DEFAULT_BOSS_DAMAGE_MULTIPLIER,
                    BOSS_PRIMARY, "#C0C0C0")
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
                BOSS_ID,
                name,
                weapon,
                description,
                55, 95, 70, 70, 60,
                primaryColor,
                accentColor);
    }

    public EnemyData toEnemyData(HeroData playerHero) {
        return new EnemyData(name, toHeroData(playerHero),
                maxHealth, damageMultiplier, true);
    }
}
