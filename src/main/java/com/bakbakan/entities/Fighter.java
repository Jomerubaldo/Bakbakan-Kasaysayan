package com.bakbakan.entities;

import com.bakbakan.combat.CombatConstants;
import com.bakbakan.events.GameEventBus;
import com.bakbakan.events.ManaChangedEvent;
import com.bakbakan.events.SkillUsedEvent;
import com.bakbakan.events.SoundRequestEvent;
import com.bakbakan.events.SpecialReadyEvent;
import com.bakbakan.events.SpecialUsedEvent;
import javafx.geometry.Rectangle2D;

public class Fighter extends Entity {

    private final HeroData hero;
    private final boolean playerControlled;
    private final GameEventBus bus;
    private final double maxHealth;
    private final double damageMultiplier;

    private double health;
    private double mana = CombatConstants.MAX_MANA;
    private double special;
    private double manaAccumulator;
    private boolean specialReadyAnnounced;

    private double velocityX;
    private double velocityY;
    private boolean onGround = true;
    private boolean facingRight = true;
    private boolean ducking;
    private boolean blocking;

    private double moveDirection;
    private boolean duckInput;
    private boolean blockInput;

    private FighterState state = FighterState.IDLE;
    private double stateTime;
    private double attackCooldown;
    private double attackTime;
    private double actionLock;
    private boolean hitLanded;
    private double hitStun;
    private double hitFlash;
    private double walkPhase;

    public Fighter(String id, HeroData hero, double x, boolean playerControlled, GameEventBus bus) {
        this(id, hero, x, playerControlled, bus,
                CombatConstants.MAX_HEALTH, 1.0);
    }

    public Fighter(String id, HeroData hero, double x, boolean playerControlled,
                   GameEventBus bus, double maxHealth, double damageMultiplier) {
        super(id, x, CombatConstants.GROUND_Y);
        this.hero = hero;
        this.playerControlled = playerControlled;
        this.bus = bus;
        this.maxHealth = Math.max(1, maxHealth);
        this.damageMultiplier = Math.max(0, damageMultiplier);
        this.health = this.maxHealth;
    }

    public void resetForRound() {
        health = maxHealth;
        mana = CombatConstants.MAX_MANA;
        special = 0;
        manaAccumulator = 0;
        specialReadyAnnounced = false;

        velocityX = 0;
        velocityY = 0;
        onGround = true;
        ducking = false;
        blocking = false;

        moveDirection = 0;
        duckInput = false;
        blockInput = false;

        state = FighterState.IDLE;
        stateTime = 0;
        attackCooldown = 0;
        attackTime = 0;
        actionLock = 0;
        hitLanded = false;
        hitStun = 0;
        hitFlash = 0;
        walkPhase = 0;
    }

    public void setMoveInput(double direction, boolean duck, boolean block) {
        this.moveDirection = Math.max(-1, Math.min(1, direction));
        this.duckInput = duck;
        this.blockInput = block;
    }

    public boolean tryJump() {
        if (!onGround || ducking || blocking || hitStun > 0 || actionLock > 0
                || !isAlive() || state == FighterState.ATTACK
                || state == FighterState.SKILL || state == FighterState.SPECIAL) {
            return false;
        }
        velocityY = CombatConstants.JUMP_VELOCITY;
        onGround = false;
        state = FighterState.JUMP;
        return true;
    }

    public boolean tryAttack() {
        if (!isAlive() || attackCooldown > 0 || hitStun > 0 || actionLock > 0
                || ducking || blocking || !onGround
                || state == FighterState.SKILL || state == FighterState.SPECIAL) {
            return false;
        }
        state = FighterState.ATTACK;
        stateTime = 0;
        attackTime = 0;
        attackCooldown = CombatConstants.ATTACK_COOLDOWN;
        hitLanded = false;
        return true;
    }

    public boolean trySkill() {
        if (!canAct() || mana + 1e-6 < CombatConstants.SKILL_MANA_COST) {
            return false;
        }
        mana = Math.max(0, mana - CombatConstants.SKILL_MANA_COST);
        fireManaChanged();

        state = FighterState.SKILL;
        stateTime = 0;
        actionLock = CombatConstants.SKILL_CAST_LOCK;
        attackCooldown = CombatConstants.SKILL_COOLDOWN;
        velocityX = 0;

        bus.fire(new SkillUsedEvent(getId(), hero.id()));
        bus.fire(new SoundRequestEvent("skill_" + hero.id(), false));
        return true;
    }

    public boolean trySpecial() {
        if (!canAct() || !isSpecialReady()) {
            return false;
        }
        special = 0;
        specialReadyAnnounced = false;

        state = FighterState.SPECIAL;
        stateTime = 0;
        actionLock = CombatConstants.SPECIAL_CAST_LOCK;
        attackCooldown = CombatConstants.SPECIAL_COOLDOWN;
        velocityX = 0;

        bus.fire(new SpecialUsedEvent(getId(), hero.id()));
        bus.fire(new SoundRequestEvent("special_" + hero.id(), false));
        return true;
    }

    private boolean canAct() {
        return isAlive() && onGround && hitStun <= 0 && actionLock <= 0
                && !ducking && !blocking
                && state != FighterState.ATTACK
                && state != FighterState.SKILL
                && state != FighterState.SPECIAL;
    }

    public void update(double dt, double opponentX) {
        stateTime += dt;
        attackCooldown = Math.max(0, attackCooldown - dt);
        actionLock = Math.max(0, actionLock - dt);
        hitStun = Math.max(0, hitStun - dt);
        hitFlash = Math.max(0, hitFlash - dt);

        if (!isAlive()) {
            state = FighterState.KO;
            blocking = false;
            ducking = false;
            velocityX *= 0.86;
            applyPhysics(dt);
            return;
        }

        updateMana(dt);

        facingRight = opponentX >= x;

        if (hitStun > 0) {
            state = FighterState.HIT;
            blocking = false;
            ducking = false;
            velocityX *= 0.9;
            applyPhysics(dt);
            return;
        }

        if (actionLock > 0) {
            state = state == FighterState.SPECIAL
                    ? FighterState.SPECIAL
                    : FighterState.SKILL;
            velocityX = 0;
            applyPhysics(dt);
            return;
        }

        if (state == FighterState.SKILL || state == FighterState.SPECIAL) {
            state = onGround ? FighterState.IDLE : FighterState.JUMP;
        }

        if (state == FighterState.ATTACK && attackTime < CombatConstants.ATTACK_DURATION) {
            attackTime += dt;
            velocityX = 0;
            applyPhysics(dt);
            if (attackTime >= CombatConstants.ATTACK_DURATION) {
                state = onGround ? FighterState.IDLE : FighterState.JUMP;
            }
            return;
        }

        blocking = blockInput && onGround;
        ducking = duckInput && onGround && !blocking;

        if (blocking || ducking) {
            velocityX = 0;
        } else {
            double speed = CombatConstants.MOVE_BASE_SPEED
                    + hero.speed() * CombatConstants.MOVE_SPEED_SCALING;
            if (!onGround) {
                speed *= 0.7;
            }
            velocityX = moveDirection * speed;
        }

        applyPhysics(dt);

        if (!onGround) {
            state = FighterState.JUMP;
        } else if (blocking) {
            state = FighterState.BLOCK;
        } else if (ducking) {
            state = FighterState.DUCK;
        } else if (Math.abs(velocityX) > 5) {
            state = FighterState.WALK;
            walkPhase += dt * 10;
        } else {
            state = FighterState.IDLE;
        }
    }

    private void updateMana(double dt) {
        if (hero.manaRegen() <= 0) {
            return;
        }
        manaAccumulator += dt;
        while (manaAccumulator >= 1.0) {
            manaAccumulator -= 1.0;
            if (mana < CombatConstants.MAX_MANA) {
                double before = mana;
                mana = Math.min(CombatConstants.MAX_MANA,
                        mana + CombatConstants.MANA_REGEN_BASE
                                + hero.manaRegen() * CombatConstants.MANA_REGEN_SCALING);
                if (mana != before) {
                    fireManaChanged();
                }
            }
        }
    }

    private void fireManaChanged() {
        bus.fire(new ManaChangedEvent(getId(), mana, CombatConstants.MAX_MANA));
    }

    private void applyPhysics(double dt) {
        velocityY += CombatConstants.GRAVITY * dt;
        y += velocityY * dt;

        if (y >= CombatConstants.GROUND_Y) {
            y = CombatConstants.GROUND_Y;
            velocityY = 0;
            onGround = true;
        }

        x += velocityX * dt;

        if (x < CombatConstants.MIN_X) {
            x = CombatConstants.MIN_X;
            velocityX = 0;
        }
        if (x > CombatConstants.MAX_X) {
            x = CombatConstants.MAX_X;
            velocityX = 0;
        }
    }

    public void receiveHit(double damage, boolean blocked, double attackerX) {
        health = Math.max(0, health - damage);
        hitFlash = 0.18;
        hitStun = blocked ? CombatConstants.BLOCKED_HIT_STUN : CombatConstants.HIT_STUN;
        blocking = false;
        ducking = false;

        double direction = x >= attackerX ? 1 : -1;
        velocityX = direction * (blocked
                ? CombatConstants.BLOCKED_KNOCKBACK_SPEED
                : CombatConstants.KNOCKBACK_SPEED);

        if (!blocked && onGround) {
            velocityY = -120;
            onGround = false;
        }

        state = isAlive() ? FighterState.HIT : FighterState.KO;
    }

    public void addSpecial(double amount) {
        if (!isAlive() || amount <= 0) {
            return;
        }
        double before = special;
        special = Math.min(CombatConstants.MAX_SPECIAL, special + amount);
        if (special >= CombatConstants.MAX_SPECIAL
                && before < CombatConstants.MAX_SPECIAL
                && !specialReadyAnnounced) {
            specialReadyAnnounced = true;
            bus.fire(new SpecialReadyEvent(getId()));
        }
    }

    public Rectangle2D getAttackHitbox() {
        if (usesProjectileBasicAttack()) {
            return null;
        }
        if (state != FighterState.ATTACK || hitLanded) {
            return null;
        }
        if (attackTime < CombatConstants.ATTACK_ACTIVE_START
                || attackTime > CombatConstants.ATTACK_ACTIVE_END) {
            return null;
        }
        double reach = CombatConstants.ATTACK_RANGE_BASE
                + hero.range() * CombatConstants.ATTACK_RANGE_SCALING;
        double boxHeight = 62;
        double boxY = y - CombatConstants.FIGHTER_HEIGHT * 0.72;
        double boxX = facingRight
                ? x + CombatConstants.FIGHTER_WIDTH / 2.0
                : x - CombatConstants.FIGHTER_WIDTH / 2.0 - reach;
        return new Rectangle2D(boxX, boxY, reach, boxHeight);
    }

    public boolean usesProjectileBasicAttack() {
        return "rizal".equals(hero.id());
    }

    public Rectangle2D getHurtbox() {
        double height = getCurrentHeight();
        double width = CombatConstants.FIGHTER_WIDTH * 0.8;
        return new Rectangle2D(x - width / 2.0, y - height, width, height);
    }

    public void markHitLanded() {
        hitLanded = true;
    }

    public boolean isFacing(Fighter other) {
        boolean otherOnRight = other.getX() >= x;
        return facingRight == otherOnRight;
    }

    public double getAttackDamage() {
        return scaleDamage(CombatConstants.BASE_ATTACK_DAMAGE
                + hero.damage() * CombatConstants.ATTACK_DAMAGE_SCALING);
    }

    public double scaleDamage(double baseDamage) {
        return baseDamage * damageMultiplier;
    }

    public double getCurrentHeight() {
        return ducking ? CombatConstants.DUCK_HEIGHT : CombatConstants.FIGHTER_HEIGHT;
    }

    public HeroData getHero() {
        return hero;
    }

    public boolean isPlayerControlled() {
        return playerControlled;
    }

    public double getHealth() {
        return health;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public double getDamageMultiplier() {
        return damageMultiplier;
    }

    public double getMana() {
        return mana;
    }

    public double getSpecial() {
        return special;
    }

    public boolean isSpecialReady() {
        return special >= CombatConstants.MAX_SPECIAL - 1e-6;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public boolean isBlocking() {
        return blocking;
    }

    public boolean isDucking() {
        return ducking;
    }

    public FighterState getState() {
        return state;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public double getHitFlash() {
        return hitFlash;
    }

    public double getWalkPhase() {
        return walkPhase;
    }

    public double getStateTime() {
        return stateTime;
    }
}
