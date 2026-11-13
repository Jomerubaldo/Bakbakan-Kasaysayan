package com.bakbakan.assets;

import com.bakbakan.combat.CombatConstants;
import com.bakbakan.entities.Fighter;
import com.bakbakan.entities.FighterState;
import com.bakbakan.entities.HeroData;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;

public class SpriteProvider {

    private static final Color SKIN = Color.web("#e0ac7e");
    private static final Color SKIN_SHADOW = Color.web("#c98e63");
    private static final Color HAIR = Color.web("#2b1b12");
    private static final Color METAL = Color.web("#d9dee8");
    private static final Color DARK_METAL = Color.web("#8f98a8");
    private static final Color WOOD = Color.web("#7a4a2a");
    private static final Color BOOT = Color.web("#2a2320");
    private static final Color EYE = Color.web("#221a16");
    private static final Color GOLD = Color.web("#d4af37");
    private static final Color RED = Color.web("#b03a2e");

    private static final double HIP_Y = -54;
    private static final double THIGH_LEN = 27;
    private static final double SHIN_LEN = 25;
    private static final double SHOULDER_Y = -105;
    private static final double UPPER_ARM_LEN = 20;
    private static final double FOREARM_LEN = 19;

    private final AssetManager assets;

    public SpriteProvider(AssetManager assets) {
        this.assets = assets;
    }

    public void render(GraphicsContext gc, Fighter fighter, double time) {
        HeroData hero = fighter.getHero();
        String stateName = fighter.getState().name().toLowerCase();
        Image png = assets.getSprite(hero.id(), stateName);

        if (png != null) {
            drawPng(gc, fighter, png);
            return;
        }

        gc.save();
        gc.translate(fighter.getX(), fighter.getY());
        if (!fighter.isFacingRight()) {
            gc.scale(-1, 1);
        }
        if (fighter.getState() == FighterState.KO) {
            drawKo(gc, hero);
        } else {
            drawProcedural(gc, fighter, hero, time);
        }
        gc.restore();
    }

    public void renderPortrait(GraphicsContext gc, HeroData hero, double x, double feetY,
                               double scale, boolean facingRight) {
        Palette p = paletteFor(hero);
        Arm back = armPose(-12, -0.14, 0.22);
        Arm front = armPose(12, 0.10, 0.26);
        gc.save();
        gc.translate(x, feetY);
        gc.scale(facingRight ? scale : -scale, scale);
        drawLegs(gc, p, 0, false, false, false, false);
        drawArm(gc, hero.id(), p, back, true);
        drawTorso(gc, hero.id(), p, p.torso());
        drawArm(gc, hero.id(), p, front, false);
        drawHead(gc, hero.id(), p);
        drawWeapon(gc, hero, FighterState.IDLE, false, front, 0);
        gc.restore();
    }

    private void drawPng(GraphicsContext gc, Fighter fighter, Image image) {
        double height = fighter.getCurrentHeight();
        gc.save();
        gc.translate(fighter.getX(), fighter.getY());
        if (!fighter.isFacingRight()) {
            gc.scale(-1, 1);
        }
        gc.drawImage(image,
                -CombatConstants.FIGHTER_WIDTH / 2.0,
                -height,
                CombatConstants.FIGHTER_WIDTH,
                height);
        gc.restore();
    }

    private Palette paletteFor(HeroData hero) {
        Color accent = Color.web(hero.accentColor());
        return switch (hero.id()) {
            case "rizal" -> new Palette(SKIN, SKIN_SHADOW, HAIR,
                    Color.web("#23262d"), Color.web("#e8dcb0"),
                    Color.web("#2f2f36"), Color.web("#191a1f"), accent);
            case "bonifacio" -> new Palette(SKIN, SKIN_SHADOW, HAIR,
                    Color.web("#efe6d2"), RED,
                    Color.web("#9e2b25"), Color.web("#3a2a1e"), RED);
            case "lapulapu" -> new Palette(Color.web("#c98a58"), Color.web("#a06a40"),
                    Color.web("#1a120d"), Color.web("#c98a58"), GOLD,
                    Color.web("#c98a58"), Color.web("#c98a58"), accent);
            case "luna" -> new Palette(SKIN, SKIN_SHADOW, HAIR,
                    Color.web("#25335e"), GOLD,
                    Color.web("#1e2a4d"), Color.web("#14161c"), GOLD);
            case "mabini" -> new Palette(SKIN, SKIN_SHADOW, HAIR,
                    Color.web("#1c1c24"), Color.web("#f2efe6"),
                    Color.web("#22222a"), Color.web("#141418"), Color.web("#f0e68c"));
            case "spanish_soldier" -> new Palette(SKIN, SKIN_SHADOW, HAIR,
                    Color.web("#3B5B8C"), Color.web("#C0392B"),
                    Color.web("#2A3F66"), BOOT, Color.web("#E0B84C"));
            case "american_soldier" -> new Palette(SKIN, SKIN_SHADOW, HAIR,
                    Color.web("#6B6B4A"), Color.web("#D4AF37"),
                    Color.web("#55553A"), BOOT, Color.web("#D4AF37"));
            case "final_boss" -> new Palette(Color.web("#d8b48a"),
                    Color.web("#b98f65"), Color.web("#141014"),
                    Color.web("#2A1520"), GOLD,
                    Color.web("#1A0F14"), Color.web("#0E0A0C"),
                    Color.web("#C0392B"));
            default -> new Palette(SKIN, SKIN_SHADOW, HAIR,
                    Color.web(hero.primaryColor()), accent,
                    Color.web(hero.primaryColor()).darker(), BOOT, accent);
        };
    }

    private void drawProcedural(GraphicsContext gc, Fighter fighter, HeroData hero, double time) {
        Palette p = paletteFor(hero);
        Color accent = p.accent();
        Color body = fighter.getHitFlash() > 0
                ? p.torso().interpolate(Color.WHITE, 0.55)
                : p.torso();

        FighterState state = fighter.getState();
        boolean ducking = state == FighterState.DUCK;
        boolean blocking = state == FighterState.BLOCK;
        boolean attacking = state == FighterState.ATTACK
                || state == FighterState.SKILL
                || state == FighterState.SPECIAL;
        boolean jumping = state == FighterState.JUMP;
        boolean walking = state == FighterState.WALK;
        boolean hurt = state == FighterState.HIT;
        boolean rising = jumping && fighter.getVelocityY() < 0;

        double phase = fighter.getWalkPhase();
        if (walking && Math.abs(fighter.getVelocityX()) > 5) {
            double facingSign = fighter.isFacingRight() ? 1 : -1;
            if (Math.signum(fighter.getVelocityX()) != facingSign) {
                phase = -phase;
            }
        }

        double attackProgress = fighter.getAttackProgress();
        double castProgress = fighter.getCastProgress();
        boolean casting = state == FighterState.SKILL || state == FighterState.SPECIAL;
        boolean specialCast = state == FighterState.SPECIAL;
        double idleBob = state == FighterState.IDLE ? Math.sin(time * 2.2) * 1.6 : 0;
        double stepBob = walking ? Math.abs(Math.sin(phase)) * 1.8 : 0;

        gc.save();
        if (hurt) {
            gc.translate(-6, 0);
        }
        if (ducking) {
            gc.scale(1, 0.68);
        }

        if (casting) {
            drawCastingLegs(gc, p, hero.id(), specialCast, castProgress);
        } else {
            drawLegs(gc, p, phase, walking, jumping, rising, hurt);
        }

        gc.save();
        gc.translate(0, idleBob + stepBob);
        double lean = 0;
        if (walking) {
            lean = 3.5;
        } else if (jumping) {
            lean = rising ? -4 : 3;
        } else if (casting) {
            lean = castLean(hero.id(), specialCast, castProgress);
        } else if (attacking) {
            lean = 3;
        } else if (hurt) {
            lean = -9;
        }
        gc.translate(0, HIP_Y);
        gc.rotate(lean);
        gc.translate(0, -HIP_Y);

        Arms arms = computeArms(hero.id(), state, phase, time,
                attackProgress, castProgress, rising);
        drawArm(gc, hero.id(), p, arms.back(), true);
        drawTorso(gc, hero.id(), p, body);
        drawArm(gc, hero.id(), p, arms.front(), false);
        drawHead(gc, hero.id(), p);
        drawWeapon(gc, hero, state, blocking, arms.front(), castProgress);

        if (state == FighterState.ATTACK) {
            double strike = clamp01((fighter.getAttackProgress() - 0.25) / 0.30);
            if (strike > 0 && strike < 1) {
                double alpha = Math.sin(strike * Math.PI) * 0.75;
                gc.setStroke(new Color(accent.getRed(), accent.getGreen(),
                        accent.getBlue(), alpha));
                gc.setLineWidth(3 + strike * 3);
                gc.strokeArc(4 - strike * 12, -138 + strike * 10,
                        104, 96, -55, 110, ArcType.OPEN);
            }
        }
        if (casting) {
            drawCastSpark(gc, accent, castProgress);
        }
        if (blocking) {
            gc.setStroke(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0.45));
            gc.setLineWidth(6);
            gc.strokeArc(-6, -136, 96, 110, -65, 130, ArcType.OPEN);
        }

        gc.restore();
        gc.restore();
    }

    private Arms computeArms(String id, FighterState state, double phase, double time,
                             double attackProgress, double castProgress, boolean rising) {
        double sway = Math.sin(time * 2.0) * 0.05;
        double backUpper = -0.14 + sway;
        double backFlex = 0.22;
        double frontUpper = 0.10 + sway;
        double frontFlex = 0.26;

        switch (state) {
            case WALK -> {
                double frontPhase = phase + Math.PI;
                backUpper = Math.sin(phase) * 0.42;
                backFlex = 0.30 + Math.max(0, Math.sin(phase)) * 0.28;
                frontUpper = Math.sin(frontPhase) * 0.42;
                frontFlex = 0.30 + Math.max(0, Math.sin(frontPhase)) * 0.28;
            }
            case JUMP -> {
                if (rising) {
                    backUpper = -2.45;
                    backFlex = 0.20;
                    frontUpper = 2.45;
                    frontFlex = 0.20;
                } else {
                    backUpper = -1.40;
                    backFlex = 0.18;
                    frontUpper = 1.50;
                    frontFlex = 0.16;
                }
            }
            case BLOCK -> {
                backUpper = 0.95;
                backFlex = 1.85;
                frontUpper = 1.15;
                frontFlex = 1.70;
            }
            case ATTACK -> {
                double t = attackProgress;
                if (t < 0.28) {
                    double k = ease(t / 0.28);
                    frontUpper = lerp(0.10, -0.85, k);
                    frontFlex = lerp(0.26, 1.15, k);
                    backUpper = lerp(-0.14, 0.35, k);
                    backFlex = lerp(0.22, 0.55, k);
                } else if (t < 0.5) {
                    double k = ease((t - 0.28) / 0.22);
                    frontUpper = lerp(-0.85, 1.45, k);
                    frontFlex = lerp(1.15, 0.12, k);
                    backUpper = lerp(0.35, -0.55, k);
                    backFlex = lerp(0.55, 0.30, k);
                } else {
                    double k = (t - 0.5) / 0.5;
                    frontUpper = lerp(1.45, 0.10, k);
                    frontFlex = lerp(0.12, 0.26, k);
                    backUpper = lerp(-0.55, -0.14, k);
                    backFlex = lerp(0.30, 0.22, k);
                }
            }
            case SKILL -> {
                double[] pose = castArmAngles(id, false, castProgress);
                backUpper = pose[0];
                backFlex = pose[1];
                frontUpper = pose[2];
                frontFlex = pose[3];
            }
            case SPECIAL -> {
                double[] pose = castArmAngles(id, true, castProgress);
                backUpper = pose[0];
                backFlex = pose[1];
                frontUpper = pose[2];
                frontFlex = pose[3];
            }
            case HIT -> {
                backUpper = -0.85;
                backFlex = 0.50;
                frontUpper = -0.55;
                frontFlex = 0.60;
            }
            case DUCK -> {
                backUpper = 0.35;
                backFlex = 0.85;
                frontUpper = 0.45;
                frontFlex = 0.80;
            }
            default -> {
            }
        }

        return new Arms(
                armPose(-12, backUpper, backFlex),
                armPose(12, frontUpper, frontFlex));
    }

    private double[] castArmAngles(String id, boolean special, double progress) {
        double t = clamp01(progress);
        double backUpper = -0.14;
        double backFlex = 0.22;
        double frontUpper = 0.10;
        double frontFlex = 0.26;

        switch (id) {
            case "rizal" -> {
                if (special) {
                    if (t < 0.45) {
                        double k = ease(t / 0.45);
                        frontUpper = lerp(0.10, 2.45, k);
                        frontFlex = lerp(0.26, 0.25, k);
                        backUpper = lerp(-0.14, -2.45, k);
                        backFlex = lerp(0.22, 0.25, k);
                    } else if (t < 0.72) {
                        double k = ease((t - 0.45) / 0.27);
                        frontUpper = lerp(2.45, 0.65, k);
                        frontFlex = lerp(0.25, 0.05, k);
                        backUpper = lerp(-2.45, -0.45, k);
                        backFlex = lerp(0.25, 0.15, k);
                    } else {
                        double k = (t - 0.72) / 0.28;
                        frontUpper = lerp(0.65, 0.10, k);
                        frontFlex = lerp(0.05, 0.26, k);
                        backUpper = lerp(-0.45, -0.14, k);
                        backFlex = lerp(0.15, 0.22, k);
                    }
                } else if (t < 0.30) {
                    double k = ease(t / 0.30);
                    backUpper = lerp(-0.14, 0.55, k);
                    backFlex = lerp(0.22, 0.35, k);
                    frontUpper = lerp(0.10, -0.95, k);
                    frontFlex = lerp(0.26, 0.95, k);
                } else if (t < 0.48) {
                    double k = ease((t - 0.30) / 0.18);
                    backUpper = lerp(0.55, -0.55, k);
                    backFlex = lerp(0.35, 0.30, k);
                    frontUpper = lerp(-0.95, 1.42, k);
                    frontFlex = lerp(0.95, 0.10, k);
                } else {
                    double k = (t - 0.48) / 0.52;
                    backUpper = lerp(-0.55, -0.14, k);
                    backFlex = lerp(0.30, 0.22, k);
                    frontUpper = lerp(1.42, 0.10, k);
                    frontFlex = lerp(0.10, 0.26, k);
                }
            }
            case "bonifacio" -> {
                if (special) {
                    if (t < 0.30) {
                        double k = ease(t / 0.30);
                        frontUpper = lerp(0.10, 2.20, k);
                        frontFlex = lerp(0.26, 0.35, k);
                        backUpper = lerp(-0.14, 2.20, k);
                        backFlex = lerp(0.22, 0.35, k);
                    } else if (t < 0.50) {
                        double k = ease((t - 0.30) / 0.20);
                        frontUpper = lerp(2.20, 0.45, k);
                        frontFlex = lerp(0.35, 0.12, k);
                        backUpper = lerp(2.20, -0.70, k);
                        backFlex = lerp(0.35, 0.30, k);
                    } else {
                        double k = (t - 0.50) / 0.50;
                        frontUpper = lerp(0.45, 0.10, k);
                        frontFlex = lerp(0.12, 0.26, k);
                        backUpper = lerp(-0.70, -0.14, k);
                        backFlex = lerp(0.30, 0.22, k);
                    }
                } else if (t < 0.15) {
                    double k = ease(t / 0.15);
                    frontUpper = lerp(0.10, -0.75, k);
                    frontFlex = lerp(0.26, 0.85, k);
                    backUpper = lerp(-0.14, 0.35, k);
                    backFlex = lerp(0.22, 0.45, k);
                } else if (t < 0.45) {
                    double k = ease((t - 0.15) / 0.30);
                    frontUpper = lerp(-0.75, 1.25, k);
                    frontFlex = lerp(0.85, 0.10, k);
                    backUpper = lerp(0.35, -0.90, k);
                    backFlex = lerp(0.45, 0.15, k);
                } else {
                    double k = (t - 0.45) / 0.55;
                    frontUpper = lerp(1.25, 0.10, k);
                    frontFlex = lerp(0.10, 0.26, k);
                    backUpper = lerp(-0.90, -0.14, k);
                    backFlex = lerp(0.15, 0.22, k);
                }
            }
            case "luna" -> {
                if (special) {
                    if (t < 0.28) {
                        double k = ease(t / 0.28);
                        frontUpper = lerp(0.10, 2.35, k);
                        frontFlex = lerp(0.26, 0.15, k);
                        backUpper = lerp(-0.14, -0.20, k);
                        backFlex = lerp(0.22, 0.30, k);
                    } else {
                        double k = clamp01((t - 0.28) / 0.72);
                        double hold = 2.35 - Math.sin(k * Math.PI) * 0.18;
                        double lower = ease(clamp01((k - 0.50) / 0.50));
                        frontUpper = lerp(hold, 0.10, lower);
                        frontFlex = lerp(0.15, 0.26, lower);
                        backUpper = lerp(-0.20, -0.14, lower);
                        backFlex = lerp(0.30, 0.22, lower);
                    }
                } else if (t < 0.30) {
                    double k = ease(t / 0.30);
                    frontUpper = lerp(0.10, 1.50, k);
                    frontFlex = lerp(0.26, 0.02, k);
                    backUpper = lerp(-0.14, 1.05, k);
                    backFlex = lerp(0.22, 0.95, k);
                } else if (t < 0.42) {
                    double k = ease((t - 0.30) / 0.12);
                    frontUpper = lerp(1.50, 1.62, k);
                    frontFlex = lerp(0.02, 0.10, k);
                    backUpper = lerp(1.05, 1.18, k);
                    backFlex = lerp(0.95, 1.02, k);
                } else {
                    double k = (t - 0.42) / 0.58;
                    frontUpper = lerp(1.62, 0.10, k);
                    frontFlex = lerp(0.10, 0.26, k);
                    backUpper = lerp(1.18, -0.14, k);
                    backFlex = lerp(1.02, 0.22, k);
                }
            }
            case "mabini" -> {
                if (special) {
                    if (t < 0.32) {
                        double k = ease(t / 0.32);
                        frontUpper = lerp(0.10, 1.45, k);
                        frontFlex = lerp(0.26, 0.10, k);
                        backUpper = lerp(-0.14, 1.10, k);
                        backFlex = lerp(0.22, 0.30, k);
                    } else if (t < 0.70) {
                        double k = ease((t - 0.32) / 0.38);
                        frontUpper = lerp(1.45, 1.55, k);
                        frontFlex = lerp(0.10, 0.05, k);
                        backUpper = lerp(1.10, 1.20, k);
                        backFlex = lerp(0.30, 0.25, k);
                    } else {
                        double k = (t - 0.70) / 0.30;
                        frontUpper = lerp(1.55, 0.10, k);
                        frontFlex = lerp(0.05, 0.26, k);
                        backUpper = lerp(1.20, -0.14, k);
                        backFlex = lerp(0.25, 0.22, k);
                    }
                } else if (t < 0.28) {
                    double k = ease(t / 0.28);
                    frontUpper = lerp(0.10, -1.15, k);
                    frontFlex = lerp(0.26, 0.80, k);
                    backUpper = lerp(-0.14, 0.50, k);
                    backFlex = lerp(0.22, 0.35, k);
                } else if (t < 0.46) {
                    double k = ease((t - 0.28) / 0.18);
                    frontUpper = lerp(-1.15, 1.35, k);
                    frontFlex = lerp(0.80, 0.08, k);
                    backUpper = lerp(0.50, -0.50, k);
                    backFlex = lerp(0.35, 0.25, k);
                } else {
                    double k = (t - 0.46) / 0.54;
                    frontUpper = lerp(1.35, 0.10, k);
                    frontFlex = lerp(0.08, 0.26, k);
                    backUpper = lerp(-0.50, -0.14, k);
                    backFlex = lerp(0.25, 0.22, k);
                }
            }
            case "lapulapu" -> {
                if (special) {
                    if (t < 0.30) {
                        double k = ease(t / 0.30);
                        frontUpper = lerp(0.10, -0.90, k);
                        frontFlex = lerp(0.26, 0.50, k);
                        backUpper = lerp(-0.14, -1.15, k);
                        backFlex = lerp(0.22, 0.45, k);
                    } else if (t < 0.52) {
                        double k = ease((t - 0.30) / 0.22);
                        frontUpper = lerp(-0.90, 1.10, k);
                        frontFlex = lerp(0.50, 0.15, k);
                        backUpper = lerp(-1.15, 0.70, k);
                        backFlex = lerp(0.45, 0.20, k);
                    } else {
                        double k = (t - 0.52) / 0.48;
                        frontUpper = lerp(1.10, 0.10, k);
                        frontFlex = lerp(0.15, 0.26, k);
                        backUpper = lerp(0.70, -0.14, k);
                        backFlex = lerp(0.20, 0.22, k);
                    }
                } else if (t < 0.28) {
                    double k = ease(t / 0.28);
                    frontUpper = lerp(0.10, 2.35, k);
                    frontFlex = lerp(0.26, 0.30, k);
                    backUpper = lerp(-0.14, -0.60, k);
                    backFlex = lerp(0.22, 0.45, k);
                } else if (t < 0.46) {
                    double k = ease((t - 0.28) / 0.18);
                    frontUpper = lerp(2.35, 0.80, k);
                    frontFlex = lerp(0.30, 0.05, k);
                    backUpper = lerp(-0.60, 0.45, k);
                    backFlex = lerp(0.45, 0.20, k);
                } else {
                    double k = (t - 0.46) / 0.54;
                    frontUpper = lerp(0.80, 0.10, k);
                    frontFlex = lerp(0.05, 0.26, k);
                    backUpper = lerp(0.45, -0.14, k);
                    backFlex = lerp(0.20, 0.22, k);
                }
            }
            case "spanish_soldier", "american_soldier" -> {
                if (t < 0.30) {
                    double k = ease(t / 0.30);
                    frontUpper = lerp(0.10, 1.45, k);
                    frontFlex = lerp(0.26, 0.05, k);
                    backUpper = lerp(-0.14, 0.95, k);
                    backFlex = lerp(0.22, 0.85, k);
                } else if (t < 0.46) {
                    frontUpper = 1.45;
                    frontFlex = 0.05;
                    backUpper = 0.95;
                    backFlex = 0.85;
                } else {
                    double k = (t - 0.46) / 0.54;
                    frontUpper = lerp(1.45, 0.10, k);
                    frontFlex = lerp(0.05, 0.26, k);
                    backUpper = lerp(0.95, -0.14, k);
                    backFlex = lerp(0.85, 0.22, k);
                }
            }
            case "final_boss" -> {
                if (special) {
                    if (t < 0.35) {
                        double k = ease(t / 0.35);
                        frontUpper = lerp(0.10, 2.40, k);
                        frontFlex = lerp(0.26, 0.20, k);
                        backUpper = lerp(-0.14, -1.80, k);
                        backFlex = lerp(0.22, 0.40, k);
                    } else if (t < 0.55) {
                        double k = ease((t - 0.35) / 0.20);
                        frontUpper = lerp(2.40, 0.40, k);
                        frontFlex = lerp(0.20, 0.05, k);
                        backUpper = lerp(-1.80, -0.30, k);
                        backFlex = lerp(0.40, 0.20, k);
                    } else {
                        double k = (t - 0.55) / 0.45;
                        frontUpper = lerp(0.40, 0.10, k);
                        frontFlex = lerp(0.05, 0.26, k);
                        backUpper = lerp(-0.30, -0.14, k);
                        backFlex = lerp(0.20, 0.22, k);
                    }
                } else if (t < 0.30) {
                    double k = ease(t / 0.30);
                    frontUpper = lerp(0.10, 1.45, k);
                    frontFlex = lerp(0.26, 0.05, k);
                    backUpper = lerp(-0.14, 0.95, k);
                    backFlex = lerp(0.22, 0.85, k);
                } else if (t < 0.48) {
                    frontUpper = 1.45;
                    frontFlex = 0.05;
                    backUpper = 0.95;
                    backFlex = 0.85;
                } else {
                    double k = (t - 0.48) / 0.52;
                    frontUpper = lerp(1.45, 0.10, k);
                    frontFlex = lerp(0.05, 0.26, k);
                    backUpper = lerp(0.95, -0.14, k);
                    backFlex = lerp(0.85, 0.22, k);
                }
            }
            default -> {
            }
        }

        return new double[]{backUpper, backFlex, frontUpper, frontFlex};
    }

    private double castLean(String id, boolean special, double progress) {
        double t = clamp01(progress);
        return switch (id) {
            case "rizal" -> special
                    ? (t < 0.45
                    ? lerp(0, -7, ease(t / 0.45))
                    : lerp(-7, 14, ease(clamp01((t - 0.45) / 0.27))))
                    : lerp(1, 8, ease(clamp01(t / 0.35)));
            case "bonifacio" -> special
                    ? (t < 0.30
                    ? lerp(0, -8, ease(t / 0.30))
                    : lerp(-8, 9, ease(clamp01((t - 0.30) / 0.20))))
                    : lerp(2, 16, ease(clamp01(t / 0.25)));
            case "luna" -> special
                    ? -5
                    : (t < 0.30
                    ? lerp(1, 4, ease(t / 0.30))
                    : lerp(4, -2, ease(clamp01((t - 0.30) / 0.15))));
            case "mabini" -> special
                    ? lerp(0, 3, ease(clamp01(t / 0.40)))
                    : lerp(1, 6, ease(clamp01(t / 0.30)));
            case "lapulapu" -> special
                    ? (t < 0.30
                    ? lerp(0, -9, ease(t / 0.30))
                    : lerp(-9, 10, ease(clamp01((t - 0.30) / 0.20))))
                    : lerp(1, 9, ease(clamp01(t / 0.30)));
            case "spanish_soldier", "american_soldier" ->
                    lerp(1, 4, ease(clamp01(t / 0.30)));
            case "final_boss" -> special
                    ? (t < 0.35
                    ? lerp(0, -7, ease(t / 0.35))
                    : lerp(-7, 10, ease(clamp01((t - 0.35) / 0.20))))
                    : lerp(1, 4, ease(clamp01(t / 0.30)));
            default -> 0;
        };
    }

    private void drawCastingLegs(GraphicsContext gc, Palette p, String id,
                                 boolean special, double progress) {
        double spread = 0.28;
        double knee = 0.22;
        if ("bonifacio".equals(id) && !special) {
            spread = 0.50;
            knee = 0.28;
        } else if ("lapulapu".equals(id) && !special) {
            spread = 0.36;
            knee = 0.26;
        } else if ("luna".equals(id) && !special) {
            spread = 0.34;
            knee = 0.18;
        } else if ("rizal".equals(id) && special && progress > 0.45) {
            knee = 0.34;
        } else if (("spanish_soldier".equals(id) || "american_soldier".equals(id))
                && !special) {
            spread = 0.34;
            knee = 0.18;
        } else if ("final_boss".equals(id)) {
            spread = 0.42;
            knee = 0.24;
        }
        drawLeg(gc, p, -7, -spread, knee, true);
        drawLeg(gc, p, 7, spread, knee, false);
    }

    private void drawCastSpark(GraphicsContext gc, Color accent, double progress) {
        double t = clamp01(progress);
        double intensity = Math.max(0, 1 - Math.abs(t - 0.34) / 0.26);
        if (intensity <= 0.02) {
            return;
        }
        gc.setStroke(new Color(accent.getRed(), accent.getGreen(),
                accent.getBlue(), 0.42 * intensity));
        gc.setLineWidth(3);
        double r = 66 + t * 54;
        gc.strokeOval(-r / 2, -110 - r / 2, r, r);
        gc.setStroke(new Color(1, 1, 1, 0.22 * intensity));
        gc.setLineWidth(1.5);
        gc.strokeOval(-r / 2.4, -110 - r / 2.4, r / 1.2, r / 1.2);
    }

    private double clamp01(double value) {
        return Math.max(0, Math.min(1, value));
    }

    private Arm armPose(double shoulderX, double upperAngle, double elbowFlex) {
        double elbowX = shoulderX + Math.sin(upperAngle) * UPPER_ARM_LEN;
        double elbowY = SHOULDER_Y + Math.cos(upperAngle) * UPPER_ARM_LEN;
        double foreAngle = upperAngle + elbowFlex;
        double handX = elbowX + Math.sin(foreAngle) * FOREARM_LEN;
        double handY = elbowY + Math.cos(foreAngle) * FOREARM_LEN;
        return new Arm(shoulderX, SHOULDER_Y, elbowX, elbowY, handX, handY, foreAngle);
    }

    private double ease(double t) {
        double x = Math.max(0, Math.min(1, t));
        return x * x * (3 - 2 * x);
    }

    private double lerp(double from, double to, double t) {
        return from + (to - from) * t;
    }

    private void drawLegs(GraphicsContext gc, Palette p, double phase, boolean moving,
                          boolean airborne, boolean rising, boolean hurt) {
        if (airborne) {
            if (rising) {
                drawLeg(gc, p, -7, -0.10, 0.80, true);
                drawLeg(gc, p, 7, 0.50, 1.05, false);
            } else {
                drawLeg(gc, p, -7, -0.12, 0.30, true);
                drawLeg(gc, p, 7, 0.18, 0.35, false);
            }
            return;
        }
        if (moving) {
            drawLeg(gc, p, -7, Math.sin(phase + Math.PI) * 0.52,
                    0.10 + Math.max(0, -Math.sin(phase + Math.PI)) * 0.95, true);
            drawLeg(gc, p, 7, Math.sin(phase) * 0.52,
                    0.10 + Math.max(0, -Math.sin(phase)) * 0.95, false);
            return;
        }
        double stance = hurt ? 0.20 : 0.05;
        drawLeg(gc, p, -7, -stance, hurt ? 0.28 : 0.12, true);
        drawLeg(gc, p, 7, stance, hurt ? 0.32 : 0.10, false);
    }

    private void drawLeg(GraphicsContext gc, Palette p, double hipX, double thighAngle,
                         double kneeFlex, boolean back) {
        double kneeX = hipX + Math.sin(thighAngle) * THIGH_LEN;
        double kneeY = HIP_Y + Math.cos(thighAngle) * THIGH_LEN;
        double shinAngle = thighAngle - kneeFlex;
        double ankleX = kneeX + Math.sin(shinAngle) * SHIN_LEN;
        double ankleY = kneeY + Math.cos(shinAngle) * SHIN_LEN;

        gc.setStroke(back ? p.trousers().darker() : p.trousers());
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineWidth(13);
        gc.strokeLine(hipX, HIP_Y, kneeX, kneeY);
        gc.setLineWidth(11);
        gc.strokeLine(kneeX, kneeY, ankleX, ankleY);
        gc.setLineCap(StrokeLineCap.BUTT);

        gc.save();
        gc.translate(ankleX, ankleY);
        gc.rotate(Math.toDegrees(shinAngle) * 0.25);
        gc.setFill(back ? p.boots().darker() : p.boots());
        gc.fillRoundRect(-5, -4, 17, 9, 4, 4);
        gc.restore();
    }

    private void drawTorso(GraphicsContext gc, String id, Palette p, Color body) {
        gc.setFill(body);
        gc.fillRoundRect(-28, -116, 56, 72, 18, 18);

        switch (id) {
            case "rizal" -> drawRizalTorso(gc, p, body);
            case "bonifacio" -> drawBonifacioTorso(gc, p);
            case "lapulapu" -> drawLapulapuTorso(gc, p);
            case "luna" -> drawLunaTorso(gc, p, body);
            case "mabini" -> drawMabiniTorso(gc, p, body);
            case "spanish_soldier" -> drawSoldierTorso(gc, p, true);
            case "american_soldier" -> drawSoldierTorso(gc, p, false);
            case "final_boss" -> drawVerdugoTorso(gc, p, body);
            default -> {
            }
        }
    }

    private void drawRizalTorso(GraphicsContext gc, Palette p, Color body) {
        gc.setFill(Color.web("#f4f0e4"));
        gc.fillRoundRect(-13, -115, 26, 32, 8, 8);

        gc.setFill(p.trim());
        gc.fillPolygon(new double[]{-5, -1, 3, 7},
                new double[]{-116, -105, -105, -116}, 4);

        gc.setFill(body.darker());
        gc.fillPolygon(new double[]{-28, -13, -15, -28},
                new double[]{-116, -116, -64, -56}, 4);
        gc.fillPolygon(new double[]{28, 13, 15, 28},
                new double[]{-116, -116, -64, -56}, 4);

        gc.setFill(p.trim());
        for (int i = 0; i < 4; i++) {
            gc.fillOval(-3, -96 + i * 12, 6, 6);
        }

        gc.setFill(body);
        gc.fillRoundRect(-30, -62, 23, 42, 10, 10);
        gc.fillRoundRect(7, -62, 23, 42, 10, 10);

        gc.setStroke(p.trim());
        gc.setLineWidth(2);
        gc.strokeArc(-15, -94, 30, 22, 200, 140, ArcType.OPEN);
        gc.setLineWidth(1);
    }

    private void drawBonifacioTorso(GraphicsContext gc, Palette p) {
        gc.setStroke(p.torso().darker());
        gc.setLineWidth(1.5);
        gc.strokeLine(0, -102, 0, -60);

        gc.setFill(p.trim());
        gc.fillPolygon(new double[]{-18, -2, 14}, new double[]{-116, -98, -116}, 3);
        gc.fillOval(-5, -110, 10, 10);
        gc.fillRect(-28, -62, 56, 11);
        gc.fillPolygon(new double[]{-6, 6, 10, -10},
                new double[]{-51, -51, 8, 0}, 4);

        gc.setFill(p.trim().darker());
        for (int i = 0; i < 3; i++) {
            gc.fillOval(-1, -96 + i * 11, 3, 3);
        }
        gc.setLineWidth(1);
    }

    private void drawLapulapuTorso(GraphicsContext gc, Palette p) {
        gc.setStroke(Color.web("#37414a", 0.7));
        gc.setLineWidth(2);
        gc.strokeLine(-16, -104, 16, -104);
        gc.strokeLine(-14, -96, -5, -84);
        gc.strokeLine(14, -96, 5, -84);
        gc.strokeLine(-10, -90, 10, -90);
        gc.strokeLine(-16, -80, -8, -68);
        gc.strokeLine(16, -80, 8, -68);
        gc.setLineWidth(1);

        gc.setFill(GOLD);
        for (int i = 0; i < 7; i++) {
            double t = i / 6.0;
            double nx = -20 + t * 40;
            double ny = -110 + Math.sin(t * Math.PI) * 10;
            gc.fillOval(nx - 3, ny, 6, 6);
        }

        gc.setFill(RED);
        gc.fillRoundRect(-30, -56, 60, 20, 8, 8);
        gc.setFill(GOLD);
        gc.fillRect(-30, -44, 60, 4);
        gc.setFill(RED);
        gc.fillPolygon(new double[]{-14, 14, 7, -7},
                new double[]{-42, -42, -4, 4}, 4);
        gc.setFill(Color.web("#8f2b23"));
        gc.fillPolygon(new double[]{-2, 2, 3, -3},
                new double[]{-42, -42, -6, 2}, 4);
    }

    private void drawLunaTorso(GraphicsContext gc, Palette p, Color body) {
        gc.setFill(body);
        gc.fillRoundRect(-30, -62, 23, 40, 8, 8);
        gc.fillRoundRect(7, -62, 23, 40, 8, 8);

        gc.setFill(RED);
        gc.fillRect(-16, -116, 32, 9);
        gc.setFill(p.trim());
        gc.fillRect(-2, -108, 4, 46);

        for (int i = 0; i < 3; i++) {
            gc.fillOval(-16, -102 + i * 14, 6, 6);
            gc.fillOval(10, -102 + i * 14, 6, 6);
        }

        gc.setStroke(p.trim());
        gc.setLineWidth(5);
        gc.strokeLine(-22, -110, 22, -72);
        gc.setLineWidth(1);

        gc.setFill(p.trim());
        gc.fillRoundRect(-36, -118, 18, 9, 4, 4);
        gc.fillRoundRect(18, -118, 18, 9, 4, 4);
        gc.setFill(RED);
        gc.fillRect(-34, -120, 4, 5);
        gc.fillRect(30, -120, 4, 5);

        gc.setFill(Color.web("#14161c"));
        gc.fillRect(-28, -62, 56, 11);
        gc.setFill(p.trim());
        gc.fillRect(-8, -62, 16, 11);
    }

    private void drawMabiniTorso(GraphicsContext gc, Palette p, Color body) {
        gc.setFill(p.trim());
        gc.fillPolygon(new double[]{-13, 13, 4, -4},
                new double[]{-116, -116, -88, -88}, 4);

        gc.setFill(Color.web("#101014"));
        gc.fillOval(-11, -114, 9, 9);
        gc.fillOval(2, -114, 9, 9);
        gc.fillRect(-3, -108, 6, 10);

        gc.setFill(body.brighter());
        gc.fillPolygon(new double[]{-28, -13, -15, -28},
                new double[]{-116, -116, -80, -74}, 4);
        gc.fillPolygon(new double[]{28, 13, 15, 28},
                new double[]{-116, -116, -80, -74}, 4);

        gc.setFill(Color.web("#3a3a44"));
        for (int i = 0; i < 4; i++) {
            gc.fillOval(-2, -100 + i * 12, 4, 4);
        }

        gc.setFill(body);
        gc.fillRoundRect(-30, -64, 60, 50, 10, 10);
        gc.setFill(p.trousers());
        gc.fillRect(-3, -62, 6, 46);

        gc.setStroke(GOLD);
        gc.setLineWidth(2);
        gc.strokeArc(-18, -92, 26, 18, 200, 140, ArcType.OPEN);
        gc.setLineWidth(1);
    }

    private void drawSoldierTorso(GraphicsContext gc, Palette p, boolean spanish) {
        gc.setFill(spanish ? Color.web("#C0392B") : Color.web("#8A7A4A"));
        gc.fillRect(-28, -116, 56, 7);
        gc.setFill(p.torso().darker());
        gc.fillRect(-28, -109, 56, 4);

        gc.setStroke(p.torso().darker());
        gc.setLineWidth(1.4);
        gc.strokeLine(-16, -110, 10, -68);

        gc.setFill(Color.web("#2A2320"));
        gc.fillRect(-30, -66, 60, 10);
        gc.setFill(GOLD);
        gc.fillRect(-5, -65, 10, 8);

        gc.setFill(p.trim());
        for (int i = 0; i < 4; i++) {
            gc.fillOval(-2, -102 + i * 10, 4, 4);
        }

        gc.setFill(p.torso().darker());
        gc.fillRoundRect(-30, -58, 22, 30, 7, 7);
        gc.fillRoundRect(8, -58, 22, 30, 7, 7);

        gc.setFill(Color.web("#3A2A1E"));
        gc.fillRoundRect(-34, -60, 10, 14, 3, 3);
        gc.setFill(spanish ? Color.web("#E0B84C") : Color.web("#C9A227"));
        gc.fillRect(-33, -58, 8, 3);
    }

    private void drawVerdugoTorso(GraphicsContext gc, Palette p, Color body) {
        gc.setFill(body.darker());
        gc.fillPolygon(new double[]{-28, -13, -15, -30},
                new double[]{-116, -116, -60, -52}, 4);
        gc.fillPolygon(new double[]{28, 13, 15, 30},
                new double[]{-116, -116, -60, -52}, 4);

        gc.setFill(Color.web("#C0392B"));
        gc.fillPolygon(new double[]{-20, -12, 18, 12},
                new double[]{-114, -112, -66, -68}, 4);

        gc.setFill(GOLD);
        for (int i = 0; i < 5; i++) {
            gc.fillOval(-3, -106 + i * 10, 6, 6);
        }

        gc.setFill(GOLD);
        gc.fillRoundRect(-38, -118, 20, 10, 4, 4);
        gc.fillRoundRect(18, -118, 20, 10, 4, 4);
        gc.setFill(Color.web("#C0392B"));
        gc.fillRect(-36, -120, 4, 5);
        gc.fillRect(30, -120, 4, 5);

        gc.setFill(Color.web("#141014"));
        gc.fillRect(-30, -64, 60, 12);
        gc.setFill(GOLD);
        gc.fillRect(-7, -63, 14, 9);
        gc.fillRect(-2, -63, 4, 9);

        gc.setFill(body);
        gc.fillRoundRect(-30, -60, 22, 34, 8, 8);
        gc.fillRoundRect(8, -60, 22, 34, 8, 8);
    }

    private void drawArm(GraphicsContext gc, String id, Palette p, Arm arm, boolean back) {
        boolean bare = "bonifacio".equals(id) || "lapulapu".equals(id);
        Color upper = bare ? p.skin() : p.torso();
        Color fore = bare ? p.skin() : p.torso();
        if (back) {
            upper = upper.darker();
            fore = fore.darker();
        }

        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setStroke(upper);
        gc.setLineWidth(9);
        gc.strokeLine(arm.shoulderX(), arm.shoulderY(), arm.elbowX(), arm.elbowY());
        gc.setStroke(fore);
        gc.setLineWidth(8);
        gc.strokeLine(arm.elbowX(), arm.elbowY(), arm.handX(), arm.handY());
        gc.setLineCap(StrokeLineCap.BUTT);

        gc.setFill(p.skin());
        gc.fillOval(arm.handX() - 5, arm.handY() - 5, 10, 10);
    }

    private void drawHead(GraphicsContext gc, String id, Palette p) {
        gc.setFill(p.hair());
        gc.fillOval(-20, -160, 44, 34);

        if ("lapulapu".equals(id)) {
            gc.fillRoundRect(-25, -150, 12, 42, 6, 6);
            gc.fillRoundRect(15, -150, 12, 42, 6, 6);
        }

        gc.setFill(p.skinShadow());
        gc.fillOval(-22, -140, 8, 12);
        gc.fillOval(16, -140, 8, 12);

        gc.setFill(p.skin());
        gc.fillOval(-17, -150, 36, 40);

        drawHairTop(gc, id, p);

        gc.setFill(EYE);
        gc.fillOval(2, -139, 4, 5);
        gc.fillOval(11, -139, 4, 5);

        gc.setStroke(p.hair());
        gc.setLineWidth(1.6);
        gc.strokeLine(1, -144, 6, -145);
        gc.strokeLine(10, -145, 15, -144);
        gc.setLineWidth(1);

        gc.setFill(p.skinShadow());
        gc.fillOval(6, -135, 4, 5);

        drawFacialHair(gc, id, p);
    }

    private void drawHairTop(GraphicsContext gc, String id, Palette p) {
        switch (id) {
            case "rizal" -> {
                gc.setFill(p.hair());
                gc.fillOval(-19, -161, 40, 22);
                gc.setStroke(p.skinShadow());
                gc.setLineWidth(1.5);
                gc.strokeLine(2, -160, 5, -148);
                gc.setLineWidth(1);
            }
            case "bonifacio" -> {
                gc.setFill(p.hair());
                gc.fillOval(-19, -162, 40, 20);
                gc.setFill(RED);
                gc.fillRoundRect(-19, -154, 40, 12, 4, 4);
                gc.fillOval(16, -151, 11, 9);
                gc.fillPolygon(new double[]{24, 38, 31},
                        new double[]{-151, -145, -137}, 3);
            }
            case "lapulapu" -> {
                gc.setFill(p.hair());
                gc.fillOval(-21, -163, 46, 22);
                gc.setFill(GOLD);
                gc.fillRoundRect(-22, -153, 46, 8, 3, 3);
                gc.setFill(RED);
                gc.fillRect(-22, -153, 46, 3);
                gc.setFill(GOLD);
                gc.fillOval(-23, -133, 7, 10);
                gc.fillOval(18, -133, 7, 10);
            }
            case "luna" -> {
                gc.setFill(p.hair());
                gc.fillOval(-18, -161, 38, 18);
                gc.setFill(p.torso());
                gc.fillRoundRect(-23, -167, 47, 16, 5, 5);
                gc.setFill(p.trim());
                gc.fillRect(-23, -155, 47, 5);
                gc.setFill(Color.web("#0e1016"));
                gc.fillRoundRect(3, -151, 30, 5, 3, 3);
                gc.setFill(p.torso().brighter());
                gc.fillOval(-19, -169, 38, 9);
            }
            case "mabini" -> {
                gc.setFill(p.hair());
                gc.fillOval(-19, -162, 40, 20);
                gc.setFill(p.skin());
                gc.fillOval(-14, -162, 28, 11);
                gc.setFill(p.hair());
                gc.fillOval(-11, -161, 10, 6);
                gc.fillOval(4, -161, 9, 6);
            }
            case "spanish_soldier" -> {
                gc.setFill(p.hair());
                gc.fillOval(-19, -162, 40, 18);
                gc.setFill(Color.web("#2F4C7A"));
                gc.fillRoundRect(-22, -166, 46, 15, 5, 5);
                gc.setFill(Color.web("#C0392B"));
                gc.fillRect(-22, -156, 46, 5);
                gc.setFill(GOLD);
                gc.fillOval(-4, -163, 8, 8);
                gc.setFill(Color.web("#1A2A44"));
                gc.fillRoundRect(14, -158, 20, 5, 3, 3);
            }
            case "american_soldier" -> {
                gc.setFill(p.hair());
                gc.fillOval(-19, -162, 40, 18);
                gc.setFill(Color.web("#5A4A2A"));
                gc.fillOval(-28, -160, 56, 12);
                gc.setFill(Color.web("#6B5A34"));
                gc.fillRoundRect(-20, -170, 40, 16, 6, 6);
                gc.setFill(Color.web("#8A7A4A"));
                gc.fillRect(-20, -160, 40, 4);
            }
            case "final_boss" -> {
                gc.setFill(p.hair());
                gc.fillOval(-19, -162, 40, 18);
                gc.setFill(Color.web("#141014"));
                gc.fillRoundRect(-24, -168, 50, 16, 6, 6);
                gc.setFill(GOLD);
                gc.fillRect(-24, -156, 50, 4);
                gc.setFill(Color.web("#C0392B"));
                gc.fillOval(-31, -178, 14, 22);
                gc.fillOval(-25, -184, 12, 20);
                gc.setFill(GOLD);
                gc.fillOval(-2, -166, 8, 8);
            }
            default -> {
            }
        }
    }

    private void drawFacialHair(GraphicsContext gc, String id, Palette p) {
        gc.setFill(p.hair());
        switch (id) {
            case "rizal" -> gc.fillRoundRect(-5, -126, 17, 4, 2, 2);
            case "bonifacio" -> {
                gc.fillRoundRect(-6, -126, 18, 4, 2, 2);
                gc.fillOval(-2, -118, 13, 6);
            }
            case "lapulapu" -> {
                gc.fillRoundRect(-5, -126, 16, 3, 2, 2);
                gc.fillPolygon(new double[]{-5, 5, 8, 0, -8},
                        new double[]{-120, -120, -107, -103, -107}, 5);
            }
            case "luna" -> {
                gc.fillOval(-9, -128, 13, 5);
                gc.fillOval(8, -128, 13, 5);
                gc.fillOval(-11, -131, 5, 6);
                gc.fillOval(18, -131, 5, 6);
            }
            case "mabini" -> {
                gc.fillRoundRect(-6, -127, 18, 4, 2, 2);
                gc.fillPolygon(new double[]{-5, 5, 8, 0, -8},
                        new double[]{-119, -119, -106, -101, -106}, 5);
            }
            case "spanish_soldier" -> {
                gc.fillRoundRect(-6, -126, 18, 4, 2, 2);
                gc.fillPolygon(new double[]{-4, 4, 6, 0, -6},
                        new double[]{-120, -120, -108, -104, -108}, 5);
            }
            case "american_soldier" -> gc.fillRoundRect(-5, -126, 16, 3, 2, 2);
            case "final_boss" -> {
                gc.fillRoundRect(-8, -126, 22, 4, 2, 2);
                gc.fillPolygon(new double[]{-6, 6, 9, 0, -9},
                        new double[]{-119, -119, -103, -99, -103}, 5);
            }
            default -> {
            }
        }
    }

    private void drawWeapon(GraphicsContext gc, HeroData hero, FighterState state,
                            boolean blocking, Arm front, double castProgress) {
        if (blocking) {
            return;
        }

        boolean lunaRifle = "luna".equals(hero.id()) && state == FighterState.SKILL;
        boolean mabiniQuill = "mabini".equals(hero.id()) && state == FighterState.SKILL;
        boolean soldierRifle = ("spanish_soldier".equals(hero.id())
                || "american_soldier".equals(hero.id()))
                && state == FighterState.SKILL;
        boolean bossRifle = "final_boss".equals(hero.id())
                && state == FighterState.SKILL;

        gc.save();
        gc.translate(front.handX(), front.handY());
        gc.rotate(90 - Math.toDegrees(front.foreAngle()));

        if (lunaRifle || soldierRifle || bossRifle) {
            drawRifle(gc, castProgress);
            gc.restore();
            return;
        }
        if (mabiniQuill) {
            drawQuillInHand(gc, Color.web(hero.accentColor()));
            gc.restore();
            return;
        }

        switch (hero.id()) {
            case "rizal" -> {
                gc.setFill(Color.web("#f5f0e1"));
                gc.fillRoundRect(-2, -14, 24, 28, 3, 3);
                gc.setFill(Color.web(hero.accentColor()));
                gc.fillRect(-2, -14, 6, 28);
                gc.setStroke(Color.web("#b8b0a0"));
                gc.setLineWidth(1);
                gc.strokeLine(8, -8, 18, -8);
                gc.strokeLine(8, 0, 18, 0);
                gc.strokeLine(8, 8, 18, 8);
            }
            case "bonifacio" -> {
                gc.setFill(METAL);
                gc.fillPolygon(
                        new double[]{2, 34, 40, 8},
                        new double[]{-5, -11, 5, 7}, 4);
                gc.setFill(WOOD);
                gc.fillRoundRect(-9, -5, 13, 10, 3, 3);
            }
            case "luna" -> {
                gc.setStroke(Color.web("#c8a24a"));
                gc.setLineWidth(4);
                gc.strokeLine(2, -9, 2, 9);
                gc.setStroke(DARK_METAL);
                gc.setLineWidth(4);
                gc.strokeLine(-9, 0, 4, 0);
                gc.setStroke(METAL);
                gc.setLineWidth(5);
                gc.strokeLine(2, 0, 60, -3);
            }
            case "mabini" -> {
                gc.setStroke(WOOD);
                gc.setLineWidth(6);
                gc.strokeLine(-6, 2, 38, -3);
                gc.setFill(Color.web("#f0e68c"));
                gc.fillOval(34, -9, 9, 9);
            }
            case "lapulapu" -> {
                gc.setFill(METAL);
                gc.fillPolygon(
                        new double[]{2, 52, 60, 10},
                        new double[]{-4, -16, -6, 6}, 4);
                gc.setFill(WOOD);
                gc.fillRoundRect(-10, -5, 14, 10, 3, 3);
            }
            case "spanish_soldier", "american_soldier" -> drawSoldierRifle(gc);
            case "final_boss" -> drawSaber(gc);
            default -> {
            }
        }
        gc.restore();
    }

    private void drawSoldierRifle(GraphicsContext gc) {
        gc.setFill(WOOD);
        gc.fillRoundRect(-30, -6, 26, 12, 3, 3);
        gc.setFill(DARK_METAL);
        gc.fillRect(-6, -3, 62, 6);
        gc.setFill(METAL);
        gc.fillRect(50, -4, 12, 8);
        gc.setFill(Color.web("#3A2A1E"));
        gc.fillRect(-8, 4, 12, 5);
        gc.setFill(METAL);
        gc.fillPolygon(new double[]{62, 92, 96, 66},
                new double[]{-2, -4, 1, 3}, 4);
    }

    private void drawSaber(GraphicsContext gc) {
        gc.setStroke(Color.web("#C8A24A"));
        gc.setLineWidth(4);
        gc.strokeLine(-10, 0, 6, 0);
        gc.setFill(GOLD);
        gc.fillOval(-14, -4, 8, 8);
        gc.setStroke(METAL);
        gc.setLineWidth(5);
        gc.strokeLine(6, 0, 70, -6);
    }

    private void drawRifle(GraphicsContext gc, double progress) {
        gc.setFill(WOOD);
        gc.fillRoundRect(-36, -7, 30, 14, 4, 4);
        gc.setFill(DARK_METAL);
        gc.fillRect(-10, -3, 62, 6);
        gc.setFill(METAL);
        gc.fillRect(46, -4, 14, 8);
        gc.setFill(Color.web("#c8a24a"));
        gc.fillRect(-2, -8, 6, 16);
        gc.setFill(Color.web("#3a2a1e"));
        gc.fillRect(-9, 4, 13, 5);

        double t = clamp01(progress);
        if (t >= 0.30 && t <= 0.48) {
            double flash = 1 - Math.abs((t - 0.39) / 0.09);
            gc.setFill(Color.web("#fff2b0", 0.9 * flash));
            gc.fillOval(58, -9, 20, 18);
            gc.setFill(Color.web("#f4c542", 0.7 * flash));
            gc.fillOval(66, -5, 15, 10);
        }
    }

    private void drawQuillInHand(GraphicsContext gc, Color accent) {
        gc.setStroke(Color.web("#f5f0e1"));
        gc.setLineWidth(3);
        gc.strokeLine(-12, 0, 30, 0);
        gc.setFill(Color.web("#f5f0e1"));
        gc.fillPolygon(
                new double[]{4, 18, 30, 14, 2},
                new double[]{-2, -12, -2, 9, 9}, 5);
        gc.setStroke(accent);
        gc.setLineWidth(2);
        gc.strokeLine(28, 0, 36, 0);
        gc.setFill(accent);
        gc.fillOval(-14, -3, 6, 6);
    }

    private void drawKo(GraphicsContext gc, HeroData hero) {
        Palette p = paletteFor(hero);

        gc.translate(0, -12);

        gc.setFill(p.torso().darker());
        gc.fillRoundRect(-62, -18, 36, 18, 8, 8);

        gc.setFill(p.torso());
        gc.fillRoundRect(-34, -30, 78, 30, 14, 14);

        gc.setFill(p.accent());
        gc.fillRect(-20, -30, 48, 7);

        gc.setFill(p.skin());
        gc.fillOval(38, -34, 36, 36);
        gc.setFill(p.hair());
        gc.fillOval(36, -40, 38, 22);

        gc.setStroke(EYE);
        gc.setLineWidth(2);
        gc.strokeLine(50, -20, 56, -14);
        gc.strokeLine(56, -20, 50, -14);
        gc.strokeLine(60, -20, 66, -14);
        gc.strokeLine(66, -20, 60, -14);
    }

    private record Arm(double shoulderX, double shoulderY, double elbowX, double elbowY,
                       double handX, double handY, double foreAngle) {
    }

    private record Arms(Arm back, Arm front) {
    }

    private record Palette(Color skin, Color skinShadow, Color hair, Color torso,
                           Color trim, Color trousers, Color boots, Color accent) {
    }
}
