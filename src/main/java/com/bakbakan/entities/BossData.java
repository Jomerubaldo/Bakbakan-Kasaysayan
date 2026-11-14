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
        String accentColor,
        String spriteId) {

    public static final String BOSS_ID = "final_boss";
    public static final double DEFAULT_BOSS_HEALTH = 160;
    public static final double DEFAULT_BOSS_DAMAGE_MULTIPLIER = 1.35;

    private static final List<BossData> ALL = List.of(
            new BossData(
                    "rizal",
                    "Governor-General Camilo de Polavieja",
                    "Saber & Rifle",
                    "He approved Rizal's death sentence. Under his orders, a firing "
                            + "squad executed the hero at Bagumbayan on December 30, 1896.",
                    DEFAULT_BOSS_HEALTH, DEFAULT_BOSS_DAMAGE_MULTIPLIER,
                    "#1E2A4D", "#D4AF37", "boss_polavieja"),
            new BossData(
                    "bonifacio",
                    "Mayor Lazaro Makapagal",
                    "Rifle & Bolo",
                    "He led the squad that shot Andres and Procopio Bonifacio at "
                            + "Maragondon on May 10, 1897, acting on the Council of War's "
                            + "orders.",
                    DEFAULT_BOSS_HEALTH, DEFAULT_BOSS_DAMAGE_MULTIPLIER,
                    "#5C6B7A", "#C0392B", "boss_makapagal"),
            new BossData(
                    "luna",
                    "Captain Pedro Janolino",
                    "Sword & Rifle",
                    "He captained the Presidential Guard that attacked General Luna "
                            + "at Cabanatuan on June 5, 1899. He was among the first to "
                            + "strike the general.",
                    DEFAULT_BOSS_HEALTH, DEFAULT_BOSS_DAMAGE_MULTIPLIER,
                    "#2E4A7A", "#E74C3C", "boss_janolino"),
            new BossData(
                    "mabini",
                    "General Arthur MacArthur",
                    "Saber & Rifle",
                    "As military governor, he exiled Mabini to Guam. No weapon killed "
                            + "Mabini—cholera defeated him after the exile.",
                    DEFAULT_BOSS_HEALTH, DEFAULT_BOSS_DAMAGE_MULTIPLIER,
                    "#6B6B4A", "#C9A227", "boss_macarthur"),
            new BossData(
                    "lapulapu",
                    "Ferdinand Magellan",
                    "Sword & Arquebus",
                    "He was defeated and killed at the Battle of Mactan on April 27, "
                            + "1521. Lapulapu's death is unrecorded, so Magellan stands "
                            + "as his legendary nemesis.",
                    DEFAULT_BOSS_HEALTH, DEFAULT_BOSS_DAMAGE_MULTIPLIER,
                    "#4A5560", "#C0392B", "boss_magellan")
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
                maxHealth, damageMultiplier, true, spriteId);
    }
}
