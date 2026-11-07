package com.bakbakan.entities;

import com.bakbakan.combat.CombatConstants;

public record EnemyData(
        String name,
        HeroData moveset,
        double maxHealth,
        double damageMultiplier,
        boolean boss) {

    public static EnemyData forRound(int round, HeroData playerHero) {
        int clamped = Math.max(1, Math.min(CombatConstants.TOTAL_ROUNDS, round));
        if (clamped == CombatConstants.TOTAL_ROUNDS) {
            return BossData.forHero(playerHero).toEnemyData(playerHero);
        }

        double multiplier = 1.0 + (clamped - 1) * 0.06;
        return switch (clamped) {
            case 1 -> new EnemyData(
                    "Colonial Scout",
                    soldier("bonifacio", "Colonial Scout", "Bolo",
                            "A quick colonial scout testing the hero's resolve.",
                            "#6B1F2A", "#C0392B"),
                    CombatConstants.MAX_HEALTH, multiplier, false);
            case 2 -> new EnemyData(
                    "Colonial Guard",
                    soldier("luna", "Colonial Guard", "Sword & Rifle",
                            "A hardened guard with disciplined sword and rifle drills.",
                            "#4A3B6B", "#C9A227"),
                    CombatConstants.MAX_HEALTH, multiplier, false);
            case 3 -> new EnemyData(
                    "Colonial Captain",
                    soldier("lapulapu", "Colonial Captain", "Kampilan",
                            "A veteran captain who stands firm behind a heavy blade.",
                            "#2C3E50", "#D4AF37"),
                    CombatConstants.MAX_HEALTH, multiplier, false);
            default -> new EnemyData(
                    "Colonial Champion",
                    soldier("mabini", "Colonial Champion", "Cane & Quill",
                            "The colony's finest champion, calm and calculating.",
                            "#5A2D3A", "#E67E22"),
                    CombatConstants.MAX_HEALTH, multiplier, false);
        };
    }

    private static HeroData soldier(String movesetId, String name, String weapon,
                                    String description, String primaryColor,
                                    String accentColor) {
        HeroData base = HeroData.byId(movesetId);
        return new HeroData(
                base.id(), name, weapon, description,
                base.speed(), base.damage(), base.range(),
                base.defense(), base.manaRegen(),
                primaryColor, accentColor);
    }
}
