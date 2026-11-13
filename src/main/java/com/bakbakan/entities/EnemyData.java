package com.bakbakan.entities;

import com.bakbakan.combat.CombatConstants;

public record EnemyData(
        String name,
        HeroData moveset,
        double maxHealth,
        double damageMultiplier,
        boolean boss) {

    public static final String SPANISH_SOLDIER = "spanish_soldier";
    public static final String AMERICAN_SOLDIER = "american_soldier";

    public static EnemyData forRound(int round, HeroData playerHero) {
        int clamped = Math.max(1, Math.min(CombatConstants.TOTAL_ROUNDS, round));
        if (clamped == CombatConstants.TOTAL_ROUNDS) {
            return BossData.forHero(playerHero).toEnemyData(playerHero);
        }

        double multiplier = 1.0 + (clamped - 1) * 0.06;
        return switch (clamped) {
            case 1 -> new EnemyData(
                    "Spanish Scout",
                    soldier(SPANISH_SOLDIER, "Spanish Scout", "Rifle & Bayonet",
                            "A young Spanish colonial scout sent to test the hero.",
                            55, 55, 55, 35, 60, "#3B5B8C", "#C0392B"),
                    CombatConstants.MAX_HEALTH, multiplier, false);
            case 2 -> new EnemyData(
                    "Spanish Guard",
                    soldier(SPANISH_SOLDIER, "Spanish Guard", "Rifle & Bayonet",
                            "A hardened Spanish guard with disciplined drill.",
                            52, 60, 58, 42, 58, "#2F4C7A", "#E0B84C"),
                    CombatConstants.MAX_HEALTH, multiplier, false);
            case 3 -> new EnemyData(
                    "American Rifleman",
                    soldier(AMERICAN_SOLDIER, "American Rifleman", "Rifle & Saber",
                            "A marksman of the American colonial army.",
                            58, 62, 60, 45, 55, "#6B6B4A", "#D4AF37"),
                    CombatConstants.MAX_HEALTH, multiplier, false);
            default -> new EnemyData(
                    "American Sergeant",
                    soldier(AMERICAN_SOLDIER, "American Sergeant", "Rifle & Saber",
                            "A seasoned sergeant who leads the colonial advance.",
                            55, 68, 62, 50, 52, "#55553A", "#C9A227"),
                    CombatConstants.MAX_HEALTH, multiplier, false);
        };
    }

    private static HeroData soldier(String id, String name, String weapon,
                                    String description, double speed, double damage,
                                    double range, double defense, double manaRegen,
                                    String primaryColor, String accentColor) {
        return new HeroData(
                id, name, weapon, description,
                speed, damage, range, defense, manaRegen,
                primaryColor, accentColor);
    }
}
