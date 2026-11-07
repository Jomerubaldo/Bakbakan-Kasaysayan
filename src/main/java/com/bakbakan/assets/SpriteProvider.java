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

    private void drawProcedural(GraphicsContext gc, Fighter fighter, HeroData hero, double time) {
        Color primary = Color.web(hero.primaryColor());
        Color accent = Color.web(hero.accentColor());
        Color body = fighter.getHitFlash() > 0
                ? primary.interpolate(Color.WHITE, 0.55)
                : primary;

        FighterState state = fighter.getState();
        boolean ducking = state == FighterState.DUCK;
        boolean blocking = state == FighterState.BLOCK;
        boolean attacking = state == FighterState.ATTACK
                || state == FighterState.SKILL
                || state == FighterState.SPECIAL;
        boolean jumping = state == FighterState.JUMP;
        boolean walking = state == FighterState.WALK;
        boolean hurt = state == FighterState.HIT;

        double bob = state == FighterState.IDLE ? Math.sin(time * 3.0) * 2 : 0;
        double walkSwing = walking ? Math.sin(fighter.getWalkPhase()) * 12 : 0;

        gc.save();
        gc.translate(0, bob);
        if (ducking) {
            gc.scale(1, 0.66);
        }
        if (hurt) {
            gc.translate(-5, 0);
            gc.rotate(-8);
        }

        gc.setFill(body.darker());
        if (jumping) {
            gc.fillRoundRect(-22, -42, 16, 30, 8, 8);
            gc.fillRoundRect(6, -48, 16, 32, 8, 8);
        } else {
            gc.fillRoundRect(-22 + walkSwing * 0.4, -52, 16, 52, 8, 8);
            gc.fillRoundRect(6 - walkSwing * 0.4, -52, 16, 52, 8, 8);
        }

        gc.setFill(BOOT);
        gc.fillRoundRect(-24 + walkSwing * 0.4, -10, 20, 10, 5, 5);
        gc.fillRoundRect(4 - walkSwing * 0.4, -10, 20, 10, 5, 5);

        gc.setFill(body);
        gc.fillRoundRect(-28, -116, 56, 72, 18, 18);
        gc.setFill(accent);
        gc.fillRect(-28, -58, 56, 8);
        gc.fillOval(-8, -104, 16, 16);

        gc.setStroke(SKIN);
        gc.setLineWidth(10);
        gc.setLineCap(StrokeLineCap.ROUND);
        if (blocking) {
            gc.strokeLine(-6, -100, 18, -82);
            gc.strokeLine(10, -108, 24, -74);
        } else if (attacking) {
            gc.strokeLine(16, -100, 48, -90);
            gc.strokeLine(-8, -100, -24, -74);
        } else if (jumping) {
            gc.strokeLine(-10, -102, -26, -130);
            gc.strokeLine(12, -102, 28, -130);
        } else {
            gc.strokeLine(-10, -102, -22, -72);
            gc.strokeLine(14, -102, 26, -72);
        }
        gc.setLineCap(StrokeLineCap.BUTT);

        gc.setFill(HAIR);
        gc.fillOval(-18, -156, 38, 30);
        gc.setFill(SKIN);
        gc.fillOval(-17, -148, 36, 36);
        gc.setFill(SKIN_SHADOW);
        gc.fillOval(-17, -118, 36, 8);
        gc.setFill(EYE);
        gc.fillOval(4, -138, 4, 5);
        gc.fillOval(12, -138, 4, 5);
        gc.setFill(accent);
        gc.fillRect(-17, -148, 36, 5);

        drawWeapon(gc, hero, attacking, blocking);

        if (attacking) {
            gc.setStroke(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0.7));
            gc.setLineWidth(4);
            gc.strokeArc(4, -138, 104, 96, -55, 110, ArcType.OPEN);
        }
        if (blocking) {
            gc.setStroke(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0.45));
            gc.setLineWidth(6);
            gc.strokeArc(-6, -136, 96, 110, -65, 130, ArcType.OPEN);
        }

        gc.restore();
    }

    private void drawWeapon(GraphicsContext gc, HeroData hero, boolean attacking, boolean blocking) {
        if (blocking) {
            return;
        }

        double handX = attacking ? 46 : 28;
        double handY = attacking ? -88 : -76;

        switch (hero.id()) {
            case "rizal" -> {
                gc.setFill(Color.web("#f5f0e1"));
                gc.fillRoundRect(handX - 4, handY - 12, 24, 28, 3, 3);
                gc.setFill(Color.web(hero.accentColor()));
                gc.fillRect(handX - 4, handY - 12, 6, 28);
                gc.setStroke(Color.web("#b8b0a0"));
                gc.setLineWidth(1);
                gc.strokeLine(handX + 6, handY - 8, handX + 16, handY - 8);
                gc.strokeLine(handX + 6, handY, handX + 16, handY);
            }
            case "bonifacio" -> {
                gc.setFill(METAL);
                gc.fillPolygon(
                        new double[]{handX, handX + 34, handX + 40, handX + 6},
                        new double[]{handY - 6, handY - 12, handY + 4, handY + 6}, 4);
                gc.setFill(WOOD);
                gc.fillRoundRect(handX - 6, handY - 4, 10, 10, 3, 3);
            }
            case "luna" -> {
                gc.setStroke(METAL);
                gc.setLineWidth(5);
                gc.strokeLine(handX, handY, handX + 58, handY - 16);
                gc.setStroke(DARK_METAL);
                gc.setLineWidth(3);
                gc.strokeLine(handX - 2, handY + 6, handX + 12, handY - 4);
            }
            case "mabini" -> {
                gc.setStroke(WOOD);
                gc.setLineWidth(6);
                gc.strokeLine(handX - 4, handY + 14, handX + 34, handY - 34);
                gc.setFill(Color.web("#f0e68c"));
                gc.fillOval(handX + 30, handY - 40, 8, 10);
            }
            case "lapulapu" -> {
                gc.setFill(METAL);
                gc.fillPolygon(
                        new double[]{handX, handX + 56, handX + 62, handX + 8},
                        new double[]{handY - 4, handY - 22, handY - 14, handY + 6}, 4);
                gc.setFill(WOOD);
                gc.fillRoundRect(handX - 8, handY - 4, 12, 10, 3, 3);
            }
            default -> {
            }
        }
    }

    private void drawKo(GraphicsContext gc, HeroData hero) {
        Color primary = Color.web(hero.primaryColor());
        Color accent = Color.web(hero.accentColor());

        gc.translate(0, -12);

        gc.setFill(primary.darker());
        gc.fillRoundRect(-62, -18, 36, 18, 8, 8);

        gc.setFill(primary);
        gc.fillRoundRect(-34, -30, 78, 30, 14, 14);

        gc.setFill(accent);
        gc.fillRect(-20, -30, 48, 7);

        gc.setFill(SKIN);
        gc.fillOval(38, -34, 36, 36);
        gc.setFill(HAIR);
        gc.fillOval(36, -40, 38, 22);

        gc.setStroke(EYE);
        gc.setLineWidth(2);
        gc.strokeLine(50, -20, 56, -14);
        gc.strokeLine(56, -20, 50, -14);
        gc.strokeLine(60, -20, 66, -14);
        gc.strokeLine(66, -20, 60, -14);
    }
}
