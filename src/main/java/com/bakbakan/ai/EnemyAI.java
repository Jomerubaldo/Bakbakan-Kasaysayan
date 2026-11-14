package com.bakbakan.ai;

import com.bakbakan.combat.CombatConstants;
import com.bakbakan.combat.SkillSystem;
import com.bakbakan.entities.Fighter;
import com.bakbakan.entities.FighterState;

import java.util.Random;

public class EnemyAI {

    public enum State {
        APPROACH, RETREAT, ATTACK, BLOCK, DUCK, WAIT, JUMP
    }

    private final Fighter self;
    private final Fighter player;
    private final SkillSystem skills;
    private final Random random = new Random();

    private final double reactionTime;
    private final double aggression;
    private final double blockChance;
    private final double duckChance;
    private final double skillChance;
    private final double specialChance;

    private State state = State.APPROACH;
    private double stateTimer;
    private double decisionTimer;
    private double blockCooldown;
    private double duckCooldown;
    private double skillCooldown;
    private double specialCooldown;
    private double jumpCooldown;

    public EnemyAI(Fighter self, Fighter player, SkillSystem skills, int round) {
        this.self = self;
        this.player = player;
        this.skills = skills;

        double difficulty = (Math.max(1, Math.min(CombatConstants.TOTAL_ROUNDS, round)) - 1)
                / (double) (CombatConstants.TOTAL_ROUNDS - 1);
        this.reactionTime = 0.55 - difficulty * 0.25;
        this.aggression = 0.45 + difficulty * 0.4;
        this.blockChance = 0.18 + difficulty * 0.32;
        this.duckChance = 0.12 + difficulty * 0.28;
        this.skillChance = 0.35 + difficulty * 0.4;
        this.specialChance = isSoldier(self.getHero().id())
                ? 0
                : 0.55 + difficulty * 0.4;
    }

    private static boolean isSoldier(String id) {
        return "spanish_soldier".equals(id) || "american_soldier".equals(id);
    }

    public void update(double dt) {
        if (self == null || player == null) {
            return;
        }
        if (!self.isAlive() || !player.isAlive()) {
            self.setMoveInput(0, false, false);
            return;
        }

        blockCooldown = Math.max(0, blockCooldown - dt);
        duckCooldown = Math.max(0, duckCooldown - dt);
        skillCooldown = Math.max(0, skillCooldown - dt);
        specialCooldown = Math.max(0, specialCooldown - dt);
        jumpCooldown = Math.max(0, jumpCooldown - dt);
        decisionTimer = Math.max(0, decisionTimer - dt);

        double dx = player.getX() - self.getX();
        double distance = Math.abs(dx);
        double direction = dx == 0 ? 1 : Math.signum(dx);

        if (state == State.BLOCK) {
            stateTimer -= dt;
            self.setMoveInput(0, false, true);
            if (stateTimer <= 0) {
                state = State.WAIT;
                stateTimer = 0.12;
            }
            return;
        }

        if (state == State.DUCK) {
            stateTimer -= dt;
            self.setMoveInput(0, true, false);
            if (stateTimer <= 0) {
                state = State.WAIT;
                stateTimer = 0.12;
            }
            return;
        }

        if (state == State.JUMP) {
            self.setMoveInput(0, false, false);
            if (self.tryJump()) {
                jumpCooldown = 2.0;
                state = State.APPROACH;
                decisionTimer = 0;
            } else {
                state = State.WAIT;
                stateTimer = 0.15;
            }
            return;
        }

        if (reactToHighThreat(distance)) {
            return;
        }
        if (reactToCloseAttack(distance)) {
            return;
        }

        if (state == State.WAIT) {
            self.setMoveInput(0, false, false);
            stateTimer -= dt;
            if (stateTimer <= 0) {
                chooseState(distance, direction);
            }
            return;
        }

        if (state == State.ATTACK) {
            self.setMoveInput(0, false, false);
            if (tryOffense(distance)) {
                state = State.WAIT;
                stateTimer = 0.16 + random.nextDouble() * 0.18;
            } else {
                state = State.WAIT;
                stateTimer = 0.08;
            }
            return;
        }

        if (state == State.RETREAT) {
            self.setMoveInput(-direction, false, false);
            if (decisionTimer <= 0) {
                chooseState(distance, direction);
            }
            return;
        }

        if (state == State.APPROACH) {
            if (distance > attackRange()) {
                self.setMoveInput(direction, false, false);
            } else {
                self.setMoveInput(0, false, false);
                state = State.ATTACK;
                return;
            }
            if (decisionTimer <= 0) {
                chooseState(distance, direction);
            }
        }
    }

    private void chooseState(double distance, double direction) {
        decisionTimer = reactionTime + random.nextDouble() * 0.18;

        if (jumpCooldown <= 0 && distance > 300 && random.nextDouble() < 0.10) {
            state = State.JUMP;
            return;
        }

        if (self.isSpecialReady()
                && distance <= specialRange()
                && specialCooldown <= 0
                && random.nextDouble() < specialChance) {
            state = State.ATTACK;
            return;
        }

        if (self.getMana() >= CombatConstants.SKILL_MANA_COST
                && distance <= skillRange()
                && skillCooldown <= 0
                && random.nextDouble() < skillChance) {
            state = State.ATTACK;
            return;
        }

        if (distance <= attackRange()) {
            state = State.ATTACK;
            return;
        }

        double preferred = preferredDistance();
        if (distance > preferred + 70) {
            state = State.APPROACH;
            return;
        }
        if (distance < preferred - 60) {
            state = State.RETREAT;
            return;
        }

        if (random.nextDouble() < aggression) {
            state = State.APPROACH;
        } else if (random.nextDouble() < 0.35) {
            state = State.WAIT;
            stateTimer = 0.15 + random.nextDouble() * 0.25;
        } else {
            state = State.RETREAT;
        }
    }

    private boolean tryOffense(double distance) {
        if (self.isSpecialReady()
                && distance <= specialRange()
                && specialCooldown <= 0
                && random.nextDouble() < specialChance) {
            if (self.trySpecial()) {
                skills.castSpecial(self, player);
                specialCooldown = 2.8;
                return true;
            }
        }

        if (self.getMana() >= CombatConstants.SKILL_MANA_COST
                && distance <= skillRange()
                && skillCooldown <= 0
                && random.nextDouble() < skillChance) {
            if (self.trySkill()) {
                skills.castSkill(self, player);
                skillCooldown = 1.1;
                return true;
            }
        }

        if (distance <= attackRange() && self.tryAttack()) {
            if (self.usesProjectileBasicAttack()) {
                skills.castRizalBook(self, player);
            }
            return true;
        }
        return false;
    }

    private boolean reactToHighThreat(double distance) {
        if (duckCooldown > 0 || distance < 140 || !isHighProjectileThreat()) {
            return false;
        }
        if (random.nextDouble() < duckChance) {
            state = State.DUCK;
            stateTimer = 0.28 + random.nextDouble() * 0.22;
            duckCooldown = 1.4;
            self.setMoveInput(0, true, false);
            return true;
        }
        return false;
    }

    private boolean reactToCloseAttack(double distance) {
        if (blockCooldown > 0 || distance > 230) {
            return false;
        }
        FighterState playerState = player.getState();
        boolean attacking = playerState == FighterState.ATTACK
                || playerState == FighterState.SKILL
                || playerState == FighterState.SPECIAL;
        if (!attacking) {
            return false;
        }
        if (random.nextDouble() < blockChance) {
            state = State.BLOCK;
            stateTimer = 0.30 + random.nextDouble() * 0.30;
            blockCooldown = 1.2;
            self.setMoveInput(0, false, true);
            return true;
        }
        return false;
    }

    private boolean isHighProjectileThreat() {
        FighterState playerState = player.getState();
        return switch (player.getHero().id()) {
            case "rizal" -> playerState == FighterState.ATTACK;
            case "luna" -> playerState == FighterState.SKILL;
            case "mabini" -> playerState == FighterState.SKILL
                    || playerState == FighterState.SPECIAL;
            default -> false;
        };
    }

    private double attackRange() {
        if (self.usesProjectileBasicAttack()) {
            return 620;
        }
        return CombatConstants.ATTACK_RANGE_BASE
                + self.getHero().range() * CombatConstants.ATTACK_RANGE_SCALING
                + CombatConstants.FIGHTER_WIDTH * 0.9;
    }

    private double skillRange() {
        return switch (self.getSpriteId()) {
            case "bonifacio" -> 280;
            case "lapulapu" -> 560;
            case "luna" -> 760;
            case "mabini" -> 700;
            case "spanish_soldier" -> 600;
            case "american_soldier" -> 660;
            case "boss_polavieja" -> 800;
            case "boss_makapagal" -> 320;
            case "boss_janolino" -> 760;
            case "boss_macarthur" -> 840;
            case "boss_magellan" -> 680;
            case "final_boss" -> 780;
            default -> 620;
        };
    }

    private double specialRange() {
        return switch (self.getSpriteId()) {
            case "bonifacio" -> 270;
            case "lapulapu" -> 600;
            case "spanish_soldier", "american_soldier" -> 0;
            case "boss_polavieja" -> 900;
            case "boss_makapagal" -> 900;
            case "boss_janolino" -> 290;
            case "boss_macarthur" -> 950;
            case "boss_magellan" -> 430;
            case "final_boss" -> 900;
            default -> 900;
        };
    }

    private double preferredDistance() {
        return switch (self.getSpriteId()) {
            case "rizal" -> 340;
            case "luna" -> 190;
            case "mabini" -> 210;
            case "bonifacio" -> 100;
            case "lapulapu" -> 120;
            case "spanish_soldier" -> 260;
            case "american_soldier" -> 290;
            case "boss_polavieja" -> 300;
            case "boss_makapagal" -> 140;
            case "boss_janolino" -> 220;
            case "boss_macarthur" -> 320;
            case "boss_magellan" -> 180;
            case "final_boss" -> 230;
            default -> 140;
        };
    }

    public State getState() {
        return state;
    }
}
