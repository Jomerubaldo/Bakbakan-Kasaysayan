package com.bakbakan.combat;

public final class CombatConstants {

    public static final double MAX_HEALTH = 100;
    public static final double MAX_MANA = 100;
    public static final double MAX_SPECIAL = 100;
    public static final double BLOCK_DAMAGE_REDUCTION = 0.7;
    public static final int ROUND_TIME_SECONDS = 180;
    public static final int TOTAL_ROUNDS = 5;

    public static final double GROUND_Y = 620;
    public static final double FIGHTER_WIDTH = 76;
    public static final double FIGHTER_HEIGHT = 150;
    public static final double DUCK_HEIGHT = 95;
    public static final double MIN_X = 70;
    public static final double MAX_X = 1210;

    public static final double GRAVITY = 1750;
    public static final double JUMP_VELOCITY = -650;
    public static final double MOVE_BASE_SPEED = 140;
    public static final double MOVE_SPEED_SCALING = 2.2;

    public static final double BASE_ATTACK_DAMAGE = 8;
    public static final double ATTACK_DAMAGE_SCALING = 0.12;
    public static final double ATTACK_RANGE_BASE = 45;
    public static final double ATTACK_RANGE_SCALING = 0.75;
    public static final double ATTACK_DURATION = 0.28;
    public static final double ATTACK_ACTIVE_START = 0.06;
    public static final double ATTACK_ACTIVE_END = 0.18;
    public static final double ATTACK_COOLDOWN = 0.5;
    public static final double HIT_STUN = 0.3;
    public static final double BLOCKED_HIT_STUN = 0.12;
    public static final double KNOCKBACK_SPEED = 220;
    public static final double BLOCKED_KNOCKBACK_SPEED = 80;

    private CombatConstants() {
    }
}
