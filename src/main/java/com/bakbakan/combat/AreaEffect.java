package com.bakbakan.combat;

import com.bakbakan.entities.Fighter;
import com.bakbakan.util.BloodFX;
import com.bakbakan.util.GameConfig;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public final class AreaEffect {

    public enum Kind {
        FLAG_DASH, GIANT_BOOK, SHOCKWAVE, EXPLOSION, LIGHT_BEAM, FIRING_SQUAD
    }

    private final Kind kind;
    private final Fighter owner;
    private final Fighter target;
    private final double damage;
    private final boolean high;
    private final double delay;
    private final double duration;
    private final double targetX;
    private final double offsetX;
    private final double direction;
    private final double dashStartX;
    private final double dashEndX;

    private double elapsed;
    private boolean hit;
    private boolean alive = true;
    private boolean impactMomentFired;
    private boolean anchored;
    private boolean special;
    private double anchorX;
    private double anchorY;

    private AreaEffect(Kind kind, Fighter owner, Fighter target, double damage,
                       boolean high, double delay, double duration, double targetX,
                       double offsetX, double direction, double dashStartX,
                       double dashEndX) {
        this.kind = kind;
        this.owner = owner;
        this.target = target;
        this.damage = damage;
        this.high = high;
        this.delay = delay;
        this.duration = duration;
        this.targetX = targetX;
        this.offsetX = offsetX;
        this.direction = direction;
        this.dashStartX = dashStartX;
        this.dashEndX = dashEndX;
    }

    public static AreaEffect flagDash(Fighter owner, double targetX) {
        double end = Math.max(CombatConstants.MIN_X,
                Math.min(CombatConstants.MAX_X, targetX));
        return new AreaEffect(Kind.FLAG_DASH, owner, null,
                owner.scaleDamage(CombatConstants.SKILL_DAMAGE_FLAG_DASH), false,
                0, 0.28, end, 0, end >= owner.getX() ? 1 : -1,
                owner.getX(), end);
    }

    public static AreaEffect giantBook(Fighter owner, Fighter target) {
        double x = target != null ? target.getX() : owner.getX();
        return new AreaEffect(Kind.GIANT_BOOK, owner, target,
                owner.scaleDamage(CombatConstants.SPECIAL_DAMAGE_GIANT_BOOK), false,
                0.52, 0.78, x, 0, 1, x, x);
    }

    public static AreaEffect shockwave(Fighter owner) {
        return new AreaEffect(Kind.SHOCKWAVE, owner, null,
                owner.scaleDamage(CombatConstants.SPECIAL_DAMAGE_SHOCKWAVE), false,
                0.05, 0.62, owner.getX(), 0, 1, owner.getX(), owner.getX());
    }

    public static AreaEffect explosion(Fighter owner, Fighter target,
                                       double offsetX, double delay) {
        double x = target != null ? target.getX() : owner.getX();
        return new AreaEffect(Kind.EXPLOSION, owner, target,
                owner.scaleDamage(CombatConstants.SPECIAL_DAMAGE_ARTILLERY_HIT), false,
                delay, 0.34, x, offsetX, 1, x, x);
    }

    public static AreaEffect lightBeam(Fighter owner, Fighter target,
                                       boolean facingRight) {
        return new AreaEffect(Kind.LIGHT_BEAM, owner, target,
                owner.scaleDamage(CombatConstants.SPECIAL_DAMAGE_BEAM), true,
                0.05, 0.50, owner.getX(), 0, facingRight ? 1 : -1,
                owner.getX(), owner.getX());
    }

    public static AreaEffect firingSquad(Fighter owner, Fighter target,
                                         boolean facingRight) {
        return new AreaEffect(Kind.FIRING_SQUAD, owner, target,
                owner.scaleDamage(CombatConstants.SPECIAL_DAMAGE_FIRING_SQUAD), true,
                0.15, 0.45, owner.getX(), 0, facingRight ? 1 : -1,
                owner.getX(), owner.getX());
    }

    public static AreaEffect rifleStrike(Fighter owner, Fighter target,
                                         double offsetX, double delay) {
        double x = target != null ? target.getX() : owner.getX();
        boolean right = target == null || target.getX() >= owner.getX();
        return new AreaEffect(Kind.EXPLOSION, owner, target,
                owner.scaleDamage(CombatConstants.SPECIAL_DAMAGE_VOLLEY_HIT), true,
                delay, 0.22, x, offsetX, right ? 1 : -1, x, x);
    }

    public static AreaEffect saberStrike(Fighter owner, Fighter target,
                                         double offsetX, double delay) {
        double x = target != null ? target.getX() : owner.getX();
        boolean right = target == null || target.getX() >= owner.getX();
        return new AreaEffect(Kind.EXPLOSION, owner, target,
                owner.scaleDamage(CombatConstants.SPECIAL_DAMAGE_AMBUSH_HIT), false,
                delay, 0.22, x, offsetX, right ? 1 : -1, x, x);
    }

    public static AreaEffect bombardment(Fighter owner, Fighter target) {
        double x = target != null ? target.getX() : owner.getX();
        return new AreaEffect(Kind.EXPLOSION, owner, target,
                owner.scaleDamage(CombatConstants.SPECIAL_DAMAGE_BOMBARDMENT), false,
                0.30, 0.52, x, 0, 1, x, x);
    }

    public static AreaEffect conquistadorCharge(Fighter owner, double targetX) {
        double end = clamp(targetX, CombatConstants.MIN_X, CombatConstants.MAX_X);
        return new AreaEffect(Kind.FLAG_DASH, owner, null,
                owner.scaleDamage(CombatConstants.SPECIAL_DAMAGE_CONQUISTADOR_CHARGE),
                false, 0, 0.36, end, 0, end >= owner.getX() ? 1 : -1,
                owner.getX(), end);
    }

    private double liveX() {
        if (target != null && target.isAlive()) {
            return clamp(target.getX() + offsetX, CombatConstants.MIN_X,
                    CombatConstants.MAX_X);
        }
        return clamp(targetX + offsetX, CombatConstants.MIN_X,
                CombatConstants.MAX_X);
    }

    private double liveFeetY() {
        if (target != null && target.isAlive()) {
            return target.getY();
        }
        return CombatConstants.GROUND_Y;
    }

    public void update(double dt) {
        elapsed += dt;

        if (kind == Kind.FLAG_DASH) {
            double progress = Math.min(1, elapsed / duration);
            double eased = 1 - Math.pow(1 - progress, 2);
            owner.setPosition(dashStartX + (dashEndX - dashStartX) * eased, owner.getY());
            if (elapsed >= duration) {
                alive = false;
            }
            return;
        }

        double extra = switch (kind) {
            case GIANT_BOOK -> 0.70;
            case SHOCKWAVE -> 0.25;
            case EXPLOSION -> 0.30;
            case LIGHT_BEAM -> 0.25;
            case FIRING_SQUAD -> 0.30;
            default -> 0;
        };
        if (kind == Kind.GIANT_BOOK && !anchored && elapsed >= delay) {
            anchorX = liveX();
            anchorY = liveFeetY();
            anchored = true;
        }
        if (elapsed >= delay + duration + extra) {
            alive = false;
        }
    }

    public boolean consumeImpactMoment() {
        if (impactMomentFired || kind != Kind.GIANT_BOOK || elapsed < delay) {
            return false;
        }
        impactMomentFired = true;
        return true;
    }

    public boolean isActive() {
        if (kind == Kind.FLAG_DASH) {
            return elapsed <= duration;
        }
        return elapsed >= delay && elapsed <= delay + duration;
    }

    public Rectangle2D getHitbox() {
        if (!isActive()) {
            return null;
        }
        return switch (kind) {
            case FLAG_DASH -> new Rectangle2D(
                    owner.getX() - 40, owner.getY() - 120, 80, 120);
            case GIANT_BOOK -> {
                if (target != null && target.isAlive()) {
                    Rectangle2D hurt = target.getHurtbox();
                    yield new Rectangle2D(hurt.getMinX() - 110, 0,
                            hurt.getWidth() + 220, CombatConstants.GROUND_Y);
                }
                double x = liveX();
                yield new Rectangle2D(x - 140, 0, 280, CombatConstants.GROUND_Y);
            }
            case SHOCKWAVE -> {
                double progress = Math.min(1, elapsed / duration);
                double radius = 60 + progress * 1300;
                yield new Rectangle2D(targetX - radius, 0, radius * 2,
                        CombatConstants.GROUND_Y);
            }
            case EXPLOSION -> {
                if (target != null && target.isAlive()) {
                    Rectangle2D hurt = target.getHurtbox();
                    yield new Rectangle2D(hurt.getMinX() - 60, 0,
                            hurt.getWidth() + 120, CombatConstants.GROUND_Y);
                }
                double x = liveX();
                yield new Rectangle2D(x - 90, 0, 180, CombatConstants.GROUND_Y);
            }
            case LIGHT_BEAM -> {
                double start = direction > 0 ? targetX : 0;
                double width = direction > 0 ? GameConfig.WIDTH - targetX : targetX;
                yield new Rectangle2D(start, CombatConstants.GROUND_Y - 150,
                        width, 55);
            }
            case FIRING_SQUAD -> {
                double start = direction > 0 ? targetX + 30 : 0;
                double width = direction > 0
                        ? GameConfig.WIDTH - targetX - 30
                        : targetX - 30;
                yield new Rectangle2D(start, CombatConstants.GROUND_Y - 105,
                        Math.max(0, width), 65);
            }
        };
    }

    public void render(GraphicsContext gc) {
        gc.save();
        switch (kind) {
            case FLAG_DASH -> drawFlagDash(gc);
            case GIANT_BOOK -> drawGiantBook(gc);
            case SHOCKWAVE -> drawShockwave(gc);
            case EXPLOSION -> drawExplosion(gc);
            case LIGHT_BEAM -> drawLightBeam(gc);
            case FIRING_SQUAD -> drawFiringSquad(gc);
        }
        gc.restore();
    }

    private void drawFlagDash(GraphicsContext gc) {
        if ("boss_magellan".equals(owner.getSpriteId())) {
            drawConquistadorCharge(gc);
            return;
        }
        Color accent = Color.web(owner.getHero().accentColor());
        double x = owner.getX();
        double y = owner.getY();

        gc.setStroke(Color.web("#f5f0e1", 0.35));
        gc.setLineWidth(3);
        for (int i = 0; i < 4; i++) {
            double offset = 40 + i * 18;
            gc.strokeLine(x - direction * offset, y - 150 + i * 26,
                    x - direction * (offset + 34), y - 150 + i * 26);
        }

        gc.setFill(Color.web("#5a3f2b"));
        gc.fillRoundRect(x - direction * 8 - 3, y - 150, 6, 120, 3, 3);

        gc.setFill(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0.9));
        gc.fillPolygon(
                new double[]{x - direction * 8, x - direction * 8,
                        x - direction * 78, x - direction * 78},
                new double[]{y - 146, y - 92, y - 88, y - 142}, 4);

        gc.setFill(Color.web("#f4c542"));
        gc.fillOval(x - direction * 40 - 9, y - 126, 18, 18);
        gc.setStroke(Color.web("#f4c542"));
        gc.setLineWidth(2);
        for (int i = 0; i < 8; i++) {
            double angle = Math.PI * 2 * i / 8;
            gc.strokeLine(x - direction * 40,
                    y - 117,
                    x - direction * 40 + Math.cos(angle) * 12,
                    y - 117 + Math.sin(angle) * 12);
        }
    }

    private void drawConquistadorCharge(GraphicsContext gc) {
        double x = owner.getX();
        double y = owner.getY();
        double progress = Math.min(1, elapsed / duration);
        double fade = Math.max(0, 1 - progress);
        double dir = direction;

        gc.setStroke(alpha(Color.web("#8a939e"), 0.55 * fade));
        gc.setLineWidth(3);
        for (int i = 0; i < 5; i++) {
            double off = 24 + i * 16;
            gc.strokeLine(x - dir * off, y - 148 + i * 22,
                    x - dir * (off + 42), y - 150 + i * 22);
        }

        gc.setFill(alpha(Color.web("#a3171b"), 0.55 * fade));
        gc.fillPolygon(
                new double[]{x - dir * 16, x - dir * 16,
                        x - dir * 86, x - dir * 78},
                new double[]{y - 140, y - 92, y - 86, y - 132}, 4);

        gc.setStroke(alpha(Color.web("#e8edf4"), 0.95));
        gc.setLineWidth(7);
        gc.strokeArc(x + dir * 18, y - 138, 96, 90,
                dir > 0 ? -58 : 148, dir > 0 ? 116 : -116, ArcType.OPEN);
        gc.setStroke(alpha(Color.web("#fff6c8"), 0.85));
        gc.setLineWidth(2.5);
        gc.strokeArc(x + dir * 26, y - 130, 76, 72,
                dir > 0 ? -52 : 142, dir > 0 ? 104 : -104, ArcType.OPEN);

        gc.setFill(alpha(Color.web("#5b463a"), 0.45 * fade));
        for (int i = 0; i < 6; i++) {
            double s = rnd(i, 61);
            double px = x - dir * (30 + s * 90);
            double py = y - 6 - s * 14;
            gc.fillOval(px, py, 8 + s * 10, 5 + s * 6);
        }

        if (special) {
            BloodFX.splatter(gc, x, y - 90, progress, 0.9, dir, y);
        }

        gc.setFill(alpha(Color.web("#f4c542"), fade));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        gc.fillText("MACTAN!", x - dir * 40, y - 190);
    }

    private void drawGiantBook(GraphicsContext gc) {
        Color accent = Color.web(owner.getHero().accentColor());
        Color primary = Color.web(owner.getHero().primaryColor());
        double x = anchored ? anchorX : liveX();
        double feetY = anchored ? anchorY : liveFeetY();
        double landY = feetY - 58;
        double fall = clamp01(elapsed / delay);
        boolean landed = elapsed >= delay;
        double landTime = clamp01((elapsed - delay) / duration);

        drawBookSigil(gc, x, feetY, accent, landed ? 1 : fall);

        if (!landed) {
            double fallEase = fall * fall;
            double y = -260 + (landY + 260) * fallEase;
            double scale = 1.02 + fall * 0.68;
            double rotation = -16 + fall * 20;
            double alpha = Math.min(1, elapsed / 0.07);

            gc.setFill(new RadialGradient(0, 0, x, landY, 260, false,
                    CycleMethod.NO_CYCLE,
                    new Stop(0, alpha(accent, 0.28 * fall)),
                    new Stop(0.55, alpha(primary, 0.14 * fall)),
                    new Stop(1, alpha(accent, 0))));
            gc.fillOval(x - 260, landY - 260, 520, 520);

            gc.setStroke(alpha(Color.WHITE, 0.30 * fall));
            gc.setLineCap(StrokeLineCap.ROUND);
            for (int i = 0; i < 14; i++) {
                double s = rnd(i, 3);
                double sx = x - 150 + s * 300;
                double len = 90 + s * 220;
                gc.setLineWidth(1 + s * 3);
                gc.strokeLine(sx, y - 120 - len, sx, y - 110);
            }
            for (int i = 0; i < 9; i++) {
                double s = rnd(i, 4);
                double px = x - 160 + s * 320;
                double py = y - 100 - rnd(i, 5) * 190;
                drawPageSprite(gc, px, py, 0.5 + s * 0.7,
                        Math.sin(elapsed * 8 + i) * 0.6,
                        alpha(Color.web("#f6ecd6"), 0.55 * fall));
            }

            gc.save();
            gc.translate(x, y);
            gc.rotate(rotation);
            gc.scale(scale, scale);
            drawBookBody(gc, primary, accent, alpha);
            gc.restore();
            return;
        }

        double impact = landTime;
        double fade = 1 - clamp01((impact - 0.55) / 0.45);
        drawBookImpact(gc, x, landY, feetY, impact, accent, primary, fade);

        double squash = 1 - Math.max(0, 1 - impact * 6) * 0.22;
        gc.save();
        gc.translate(x, landY);
        gc.scale(1.22, 1.22 * squash);
        drawBookBody(gc, primary, accent, fade);
        gc.restore();
    }

    private void drawBookSigil(GraphicsContext gc, double x, double feetY,
                               Color accent, double intensity) {
        double pulse = 0.55 + 0.45 * Math.sin(elapsed * 12);
        gc.setStroke(alpha(accent, 0.20 + 0.30 * intensity));
        gc.setLineWidth(2.5);
        for (int i = 0; i < 3; i++) {
            double r = 58 + i * 30 + Math.sin(elapsed * 5 + i) * 5;
            gc.strokeOval(x - r, feetY - r * 0.42, r * 2, r * 0.84);
        }
        gc.setStroke(alpha(Color.WHITE, 0.18 + 0.20 * intensity));
        gc.setLineWidth(1.4);
        double rr = 120;
        gc.strokeOval(x - rr, feetY - rr * 0.42, rr * 2, rr * 0.84);
        gc.setFill(alpha(accent, 0.10 * pulse * intensity));
        gc.fillOval(x - 90, feetY - 38, 180, 76);
        gc.setStroke(alpha(accent, 0.55 * intensity));
        gc.setLineWidth(3);
        for (int i = 0; i < 8; i++) {
            double a = elapsed * 1.6 + i * Math.PI / 4;
            double ix = x + Math.cos(a) * 96;
            double iy = feetY + Math.sin(a) * 40;
            double ox = x + Math.cos(a) * 118;
            double oy = feetY + Math.sin(a) * 49;
            gc.strokeLine(ix, iy, ox, oy);
        }
    }

    private void drawBookImpact(GraphicsContext gc, double x, double landY,
                                double feetY, double t, Color accent, Color primary,
                                double fade) {
        if (special) {
            BloodFX.splatter(gc, x, landY, t, 1.0, direction, feetY);
        }

        for (int i = 0; i < 16; i++) {
            double s = rnd(i, 11);
            double a = rnd(i, 12) * Math.PI * 2;
            double dist = (60 + t * 260) * (0.4 + s);
            double px = x + Math.cos(a) * dist;
            double py = feetY + Math.sin(a) * dist * 0.28 - t * 60;
            double size = 8 + s * 20;
            gc.setFill(alpha(Color.web("#d8cbb0"), 0.55 * fade));
            gc.fillOval(px - size / 2, py - size / 2, size, size);
        }

        gc.setStroke(alpha(Color.web("#2a2118"), 0.55 * fade));
        gc.setLineWidth(2.5);
        for (int i = 0; i < 7; i++) {
            double s = rnd(i, 13);
            double a = Math.PI + (i / 6.0) * Math.PI;
            double len = 70 + s * 150 * Math.min(1, t * 2);
            gc.strokeLine(x + Math.cos(a) * 40, feetY + Math.sin(a) * 6,
                    x + Math.cos(a) * len, feetY + Math.sin(a) * 26);
        }

        for (int i = 0; i < 6; i++) {
            double s = rnd(i, 14);
            double a = -Math.PI / 2 + (s - 0.5) * 3.0;
            double dist = 40 + t * 220 * (0.5 + s);
            double px = x + Math.cos(a) * dist;
            double py = landY + Math.sin(a) * dist - t * 40;
            drawPageSprite(gc, px, py, 0.6 + s * 0.8,
                    elapsed * (2 + s * 4) + i,
                    alpha(Color.web("#f6ecd6"), 0.7 * fade));
        }
    }

    private void drawBookBody(GraphicsContext gc, Color primary, Color accent,
                              double alpha) {
        double a = clamp01(alpha);
        if (a <= 0) {
            return;
        }
        gc.setGlobalAlpha(a);

        gc.setFill(Color.web("#000000", 0.30));
        gc.fillRoundRect(-98, -56, 200, 130, 14, 14);

        gc.setFill(Color.web("#efe6cd"));
        gc.fillRoundRect(-82, -48, 176, 112, 9, 9);
        gc.setStroke(Color.web("#b9ad90", 0.9));
        gc.setLineWidth(1);
        for (int i = 0; i < 11; i++) {
            double y = -44 + i * 10;
            gc.strokeLine(-80, y, 90, y);
        }

        gc.setFill(primary.darker().darker());
        gc.fillRoundRect(-104, -64, 204, 130, 16, 16);

        gc.setFill(primary);
        gc.fillRoundRect(-100, -66, 196, 126, 16, 16);

        gc.setFill(primary.darker());
        gc.fillRoundRect(-104, -66, 32, 126, 12, 12);

        gc.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, alpha(Color.WHITE, 0.16)),
                new Stop(0.35, alpha(Color.WHITE, 0)),
                new Stop(1, alpha(Color.BLACK, 0.18))));
        gc.fillRoundRect(-100, -66, 196, 126, 16, 16);

        gc.setStroke(accent);
        gc.setLineWidth(3);
        gc.strokeRoundRect(-90, -56, 178, 106, 10, 10);
        gc.setStroke(alpha(accent, 0.5));
        gc.setLineWidth(1.2);
        gc.strokeRoundRect(-84, -50, 166, 94, 8, 8);

        double[] cx = {-90, -90, 88, 88};
        double[] cy = {-56, 50, 50, -56};
        for (int i = 0; i < 4; i++) {
            gc.setFill(accent);
            gc.fillPolygon(new double[]{cx[i], cx[i] + (i < 2 ? 10 : -10), cx[i]},
                    new double[]{cy[i] - 8, cy[i], cy[i] + 8}, 3);
        }

        gc.setFill(accent);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(javafx.geometry.VPos.CENTER);
        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 24));
        gc.fillText("NOLI ME", 4, -16);
        gc.fillText("TANGERE", 4, 12);
        gc.setFont(Font.font("Georgia", FontWeight.NORMAL, 10));
        gc.setFill(alpha(Color.WHITE, 0.82));
        gc.fillText("JOSE RIZAL", 4, 38);

        gc.setFill(accent);
        gc.fillRect(-6, 48, 12, 22);
        gc.setFill(alpha(accent, 0.6));
        gc.fillPolygon(new double[]{-6, 6, 6, -6},
                new double[]{70, 70, 78, 74}, 4);

        gc.setTextBaseline(javafx.geometry.VPos.BASELINE);
        gc.setGlobalAlpha(1);
    }

    private void drawPageSprite(GraphicsContext gc, double x, double y,
                                double scale, double rotation, Color color) {
        gc.save();
        gc.translate(x, y);
        gc.rotate(Math.toDegrees(rotation));
        gc.scale(scale, scale);
        gc.setFill(color);
        gc.fillRect(-13, -17, 26, 34);
        gc.setStroke(alpha(color, 0.6));
        gc.setLineWidth(1.2);
        gc.strokeRect(-13, -17, 26, 34);
        gc.setStroke(alpha(Color.web("#5a4630"), color.getOpacity() * 0.7));
        for (int i = 0; i < 4; i++) {
            gc.strokeLine(-8, -9 + i * 7, 8, -9 + i * 7);
        }
        gc.restore();
    }

    private void drawShockwave(GraphicsContext gc) {
        Color accent = Color.web(owner.getHero().accentColor());
        double progress = Math.min(1, elapsed / duration);
        double radius = 60 + progress * 640;
        double alpha = Math.max(0, 1 - progress);

        gc.setFill(new RadialGradient(0, 0, targetX, CombatConstants.GROUND_Y - 60,
                radius * 0.8, false, CycleMethod.NO_CYCLE,
                new Stop(0, alpha(accent, 0.18 * alpha)),
                new Stop(1, alpha(accent, 0))));
        gc.fillOval(targetX - radius, CombatConstants.GROUND_Y - radius * 0.6,
                radius * 2, radius * 1.2);

        gc.setStroke(alpha(accent, 0.85 * alpha));
        gc.setLineWidth(11);
        gc.strokeOval(targetX - radius, CombatConstants.GROUND_Y - radius * 0.55,
                radius * 2, radius * 1.1);
        gc.setStroke(alpha(Color.web("#f5f0e1"), 0.75 * alpha));
        gc.setLineWidth(3);
        gc.strokeOval(targetX - radius * 0.72,
                CombatConstants.GROUND_Y - radius * 0.4,
                radius * 1.44, radius * 0.8);
        gc.setStroke(alpha(accent, 0.45 * alpha));
        gc.setLineWidth(2);
        gc.strokeOval(targetX - radius * 1.1,
                CombatConstants.GROUND_Y - radius * 0.62,
                radius * 2.2, radius * 1.24);

        gc.setStroke(alpha(Color.web("#ffe6a8"), 0.6 * alpha));
        gc.setLineWidth(2);
        for (int i = 0; i < 16; i++) {
            double a = i * Math.PI / 8;
            gc.strokeLine(
                    targetX + Math.cos(a) * radius * 0.86,
                    CombatConstants.GROUND_Y + Math.sin(a) * radius * 0.47,
                    targetX + Math.cos(a) * radius * 1.02,
                    CombatConstants.GROUND_Y + Math.sin(a) * radius * 0.56);
        }

        for (int i = 0; i < 20; i++) {
            double s = rnd(i, 21);
            double a = rnd(i, 22) * Math.PI * 2;
            double dist = radius * (0.55 + s * 0.6);
            double px = targetX + Math.cos(a) * dist;
            double py = CombatConstants.GROUND_Y + Math.sin(a) * dist * 0.35
                    - progress * 40 * s;
            double size = 3 + s * 7;
            gc.setFill(alpha(mix(accent, Color.web("#fff3c4"), s), 0.7 * alpha));
            gc.fillOval(px, py, size, size);
        }

        if (special) {
            BloodFX.ringSpray(gc, targetX, CombatConstants.GROUND_Y - 60,
                    radius, progress, alpha);
            BloodFX.splatter(gc, targetX, CombatConstants.GROUND_Y - 70,
                    progress, 0.6 * alpha, 1, CombatConstants.GROUND_Y);
        }

        gc.setFill(alpha(Color.web("#f4c542"), alpha));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 30));
        gc.fillText("WAR CRY!", targetX, CombatConstants.GROUND_Y - radius - 24);
    }

    private void drawExplosion(GraphicsContext gc) {
        String spriteId = owner.getSpriteId();
        if ("boss_makapagal".equals(spriteId)) {
            drawVolleyShot(gc);
            return;
        }
        if ("boss_janolino".equals(spriteId)) {
            drawAmbushSlash(gc);
            return;
        }
        if ("boss_macarthur".equals(spriteId)) {
            drawBombardment(gc);
            return;
        }
        double x = liveX();
        if (elapsed < delay) {
            double fall = clamp01(elapsed / delay);
            double sy = -80 + (CombatConstants.GROUND_Y + 80) * fall;
            double flicker = 0.6 + 0.4 * Math.sin(elapsed * 40);
            gc.setStroke(alpha(Color.web("#ffd27a"), 0.5 * flicker));
            gc.setLineWidth(3);
            gc.strokeLine(x, Math.max(-40, sy - 70), x, sy);
            gc.setFill(alpha(Color.web("#2b2118"), 0.85));
            gc.save();
            gc.translate(x, sy);
            gc.rotate(0.2);
            gc.fillRoundRect(-4, -14, 8, 26, 3, 3);
            gc.setFill(alpha(Color.web("#f4c542"), 0.9 * flicker));
            gc.fillOval(-5, 8, 10, 10);
            gc.restore();
            gc.setFill(alpha(Color.web("#f4c542"), 0.35 * fall));
            gc.fillOval(x - 26, CombatConstants.GROUND_Y - 10, 52, 16);
            return;
        }

        double progress = Math.min(1, (elapsed - delay) / duration);
        double alpha = Math.max(0, 1 - progress);
        double radius = 30 + progress * 90;

        gc.setFill(new RadialGradient(0, 0, x, CombatConstants.GROUND_Y - 20,
                radius * 2, false, CycleMethod.NO_CYCLE,
                new Stop(0, alpha(Color.web("#fff2b0"), 0.9 * alpha)),
                new Stop(0.35, alpha(Color.web("#ff9c3a"), 0.85 * alpha)),
                new Stop(0.7, alpha(Color.web("#c0392b"), 0.55 * alpha)),
                new Stop(1, alpha(Color.web("#c0392b"), 0))));
        gc.fillOval(x - radius * 2, CombatConstants.GROUND_Y - radius * 2,
                radius * 4, radius * 3.2);

        for (int i = 0; i < 12; i++) {
            double s = rnd(i, 31);
            double a = rnd(i, 32) * Math.PI * 2;
            double dist = radius * (0.5 + s * 0.9);
            double px = x + Math.cos(a) * dist;
            double py = CombatConstants.GROUND_Y - radius * 0.6
                    + Math.sin(a) * dist * 0.5;
            double size = 8 + s * 22;
            gc.setFill(alpha(mix(Color.web("#5b463a"), Color.web("#2a2118"), s),
                    0.55 * alpha));
            gc.fillOval(px, py, size, size);
        }

        gc.setStroke(alpha(Color.WHITE, 0.8 * alpha));
        gc.setLineWidth(4);
        gc.strokeOval(x - radius * 1.8, CombatConstants.GROUND_Y - radius * 0.9,
                radius * 3.6, radius * 1.8);
        gc.setStroke(alpha(Color.web("#ffd27a"), 0.55 * alpha));
        gc.setLineWidth(2);
        for (int i = 0; i < 10; i++) {
            double a = i * Math.PI / 5 + progress;
            gc.strokeLine(x + Math.cos(a) * radius,
                    CombatConstants.GROUND_Y - radius * 0.5 + Math.sin(a) * radius * 0.5,
                    x + Math.cos(a) * radius * 1.9,
                    CombatConstants.GROUND_Y - radius * 0.5 + Math.sin(a) * radius * 0.95);
        }

        if (special) {
            BloodFX.splatter(gc, x, CombatConstants.GROUND_Y - 40, progress,
                    0.95, 1, CombatConstants.GROUND_Y);
        }
    }

    private void drawVolleyShot(GraphicsContext gc) {
        double x = liveX();
        double y = CombatConstants.GROUND_Y - 112;
        double dir = x >= owner.getX() ? 1 : -1;

        if (elapsed < delay) {
            double charge = Math.max(0, Math.min(1, elapsed / delay));
            gc.setFill(alpha(Color.web("#f4c542"), 0.5 * charge));
            gc.fillOval(x - 10, y - 10, 20, 20);
            return;
        }

        double progress = Math.min(1, (elapsed - delay) / duration);
        double fade = Math.max(0, 1 - progress);
        double sx = owner.getX() + dir * 34;

        gc.setStroke(alpha(Color.web("#fff2b0"), 0.9 * fade));
        gc.setLineWidth(3);
        gc.strokeLine(sx, y, x - dir * 12, y);
        gc.setStroke(alpha(Color.web("#e74c3c"), 0.35 * fade));
        gc.setLineWidth(7);
        gc.strokeLine(sx, y, x - dir * 12, y);
        gc.setFill(alpha(Color.web("#fff8d6"), fade));
        gc.fillOval(x - dir * 14 - 9, y - 11, 22, 22);

        for (int i = 0; i < 5; i++) {
            double s = rnd(i, 71);
            double px = x - dir * (8 + s * 42) * progress;
            double py = y - 20 + rnd(i, 72) * 38;
            gc.setFill(alpha(Color.web("#e8e0d0"), 0.4 * fade));
            gc.fillOval(px, py, 7 + s * 10, 7 + s * 10);
        }

        if (special) {
            BloodFX.splatter(gc, x, y, progress, 0.9, dir,
                    CombatConstants.GROUND_Y);
        }
    }

    private void drawAmbushSlash(GraphicsContext gc) {
        double x = liveX();
        double feetY = CombatConstants.GROUND_Y;
        if (elapsed < delay) {
            double charge = Math.max(0, Math.min(1, elapsed / delay));
            gc.setStroke(alpha(Color.web("#e74c3c"), 0.6 * charge));
            gc.setLineWidth(2);
            gc.strokeOval(x - 26, feetY - 126, 52, 104);
            return;
        }

        double progress = Math.min(1, (elapsed - delay) / duration);
        double fade = Math.max(0, 1 - progress);
        double dir = direction;

        gc.setStroke(alpha(Color.web("#7a1a1a"), 0.35 * fade));
        gc.setLineWidth(14);
        gc.strokeArc(x - 46, feetY - 146, 102, 112,
                dir > 0 ? -72 : 108, dir > 0 ? 144 : -144, ArcType.OPEN);
        gc.setStroke(alpha(Color.web("#ff8a7a"), 0.95 * fade));
        gc.setLineWidth(5);
        gc.strokeArc(x - 40, feetY - 140, 92, 100,
                dir > 0 ? -68 : 104, dir > 0 ? 136 : -136, ArcType.OPEN);
        gc.setStroke(alpha(Color.web("#f5f0e1"), 0.8 * fade));
        gc.setLineWidth(2);
        gc.strokeArc(x - 32, feetY - 132, 76, 84,
                dir > 0 ? -60 : 96, dir > 0 ? 120 : -120, ArcType.OPEN);

        if (special) {
            BloodFX.splatter(gc, x, feetY - 92, progress, 0.95, dir, feetY);
        }
    }

    private void drawBombardment(GraphicsContext gc) {
        double x = liveX();
        if (elapsed < delay) {
            double fall = clamp01(elapsed / delay);
            double sy = -60 + (CombatConstants.GROUND_Y + 60) * fall;
            gc.setStroke(alpha(Color.web("#dce9ff"), 0.5 * fall));
            gc.setLineWidth(2);
            gc.strokeLine(x, Math.max(-30, sy - 80), x, sy);
            gc.setFill(alpha(Color.web("#2b2b22"), 0.9));
            gc.save();
            gc.translate(x, sy);
            gc.rotate(0.35);
            gc.fillRoundRect(-6, -16, 12, 30, 4, 4);
            gc.setFill(alpha(Color.web("#8fc2ff"), 0.85 * fall));
            gc.fillRect(-3, -10, 6, 18);
            gc.restore();
            gc.setFill(alpha(Color.web("#dce9ff"), 0.35 * fall));
            gc.fillOval(x - 30, CombatConstants.GROUND_Y - 10, 60, 16);
            return;
        }

        double progress = Math.min(1, (elapsed - delay) / duration);
        double fade = Math.max(0, 1 - progress);
        double radius = 40 + progress * 130;

        gc.setFill(new RadialGradient(0, 0, x, CombatConstants.GROUND_Y - 24,
                radius * 2, false, CycleMethod.NO_CYCLE,
                new Stop(0, alpha(Color.web("#ffffff"), 0.9 * fade)),
                new Stop(0.30, alpha(Color.web("#ffd27a"), 0.85 * fade)),
                new Stop(0.62, alpha(Color.web("#6b6b4a"), 0.55 * fade)),
                new Stop(1, alpha(Color.web("#3a3a2a"), 0))));
        gc.fillOval(x - radius * 2, CombatConstants.GROUND_Y - radius * 2,
                radius * 4, radius * 3.4);

        gc.setStroke(alpha(Color.WHITE, 0.8 * fade));
        gc.setLineWidth(4);
        gc.strokeOval(x - radius * 1.8, CombatConstants.GROUND_Y - radius * 0.9,
                radius * 3.6, radius * 1.8);
        gc.setStroke(alpha(Color.web("#dce9ff"), 0.6 * fade));
        gc.setLineWidth(2);
        for (int i = 0; i < 10; i++) {
            double a = i * Math.PI / 5 + progress;
            gc.strokeLine(x + Math.cos(a) * radius,
                    CombatConstants.GROUND_Y - radius * 0.5 + Math.sin(a) * radius * 0.5,
                    x + Math.cos(a) * radius * 2,
                    CombatConstants.GROUND_Y - radius * 0.5 + Math.sin(a) * radius * 1.05);
        }

        for (int i = 0; i < 12; i++) {
            double s = rnd(i, 91);
            double a = rnd(i, 92) * Math.PI * 2;
            double dist = radius * (0.5 + s * 0.9);
            double px = x + Math.cos(a) * dist;
            double py = CombatConstants.GROUND_Y - radius * 0.6
                    + Math.sin(a) * dist * 0.5;
            double size = 8 + s * 20;
            gc.setFill(alpha(mix(Color.web("#6b6b4a"), Color.web("#2a2a20"), s),
                    0.6 * fade));
            gc.fillOval(px, py, size, size);
        }

        if (special) {
            BloodFX.splatter(gc, x, CombatConstants.GROUND_Y - 45, progress,
                    1.0, 1, CombatConstants.GROUND_Y);
        }

        gc.setFill(alpha(Color.web("#f4c542"), fade));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 30));
        gc.fillText("EXILE!", x, CombatConstants.GROUND_Y - 210);
    }

    private void drawLightBeam(GraphicsContext gc) {
        double progress = Math.min(1, elapsed / duration);
        double alpha = Math.max(0, 1 - Math.max(0, (progress - 0.6) / 0.4));
        double start = direction > 0 ? targetX : 0;
        double width = direction > 0 ? GameConfig.WIDTH - targetX : targetX;
        double y = CombatConstants.GROUND_Y - 140;
        Color accent = Color.web(owner.getHero().accentColor());

        gc.setFill(alpha(Color.web("#f4c542"), 0.10 * alpha));
        gc.fillRect(start, 0, width, combatGround());
        gc.setFill(alpha(Color.web("#f4c542"), 0.24 * alpha));
        gc.fillRect(start, y - 40, width, 120);
        gc.setFill(alpha(Color.web("#f4c542"), 0.48 * alpha));
        gc.fillRect(start, y - 14, width, 68);
        gc.setFill(alpha(Color.web("#fff8d6"), 0.95 * alpha));
        gc.fillRect(start, y, width, 40);

        gc.setStroke(alpha(Color.WHITE, 0.85 * alpha));
        gc.setLineWidth(3);
        gc.strokeLine(start, y + 20, start + width, y + 20);

        gc.setStroke(alpha(accent, alpha));
        gc.setLineWidth(2.4);
        for (double x = start + 20; x < start + width; x += 54) {
            gc.strokeOval(x, y + 6, 28, 28);
        }

        for (int i = 0; i < 18; i++) {
            double s = rnd(i, 41);
            double px = start + ((elapsed * (260 + s * 260) + s * width) % width);
            double py = y - 30 + rnd(i, 42) * 80;
            gc.setFill(alpha(mix(accent, Color.WHITE, s), 0.7 * alpha));
            gc.fillOval(px, py, 2 + s * 4, 2 + s * 4);
        }

        if (special && target != null && target.isAlive()) {
            double tx = target.getX();
            double ty = target.getY() - target.getCurrentHeight() * 0.55;
            BloodFX.splatter(gc, tx, ty, progress, 0.95, direction,
                    CombatConstants.GROUND_Y);
        }

        gc.setFill(alpha(Color.web("#2b1b12"), alpha));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        gc.fillText("DECALOGUE", start + width / 2,
                CombatConstants.GROUND_Y - 210);
    }

    private void drawFiringSquad(GraphicsContext gc) {
        if (elapsed < delay) {
            double charge = Math.max(0, Math.min(1, elapsed / delay));
            gc.setFill(alpha(Color.web("#e74c3c"), 0.5 * charge));
            gc.fillOval(targetX - 12, CombatConstants.GROUND_Y - 140, 24, 24);
            gc.setStroke(alpha(Color.web("#fff2b0"), 0.5 * charge));
            gc.setLineWidth(2);
            for (int i = 0; i < 3; i++) {
                double r = 20 + i * 12 + charge * 10;
                gc.strokeOval(targetX - r, CombatConstants.GROUND_Y - 140 - r,
                        r * 2, r * 2);
            }
            return;
        }

        double progress = Math.min(1, (elapsed - delay) / duration);
        double alpha = Math.max(0, 1 - progress);
        double muzzleX = targetX + direction * 34;
        double y = CombatConstants.GROUND_Y - 95;
        double endX = direction > 0 ? GameConfig.WIDTH : 0;

        gc.setFill(alpha(Color.web("#f4c542"), 0.30 * alpha));
        gc.fillOval(muzzleX - 40, y - 34, 90, 68);

        for (int i = 0; i < 3; i++) {
            double offset = (i - 1) * 13;
            gc.setStroke(alpha(Color.web("#fff2b0"), 0.85 * alpha));
            gc.setLineWidth(3 - i * 0.6);
            gc.strokeLine(muzzleX, y + offset, endX, y + offset);
            gc.setStroke(alpha(Color.web("#e74c3c"), 0.35 * alpha));
            gc.setLineWidth(7 - i);
            gc.strokeLine(muzzleX, y + offset, endX, y + offset);
        }

        gc.setFill(alpha(Color.web("#e74c3c"), 0.75 * alpha));
        gc.fillOval(muzzleX - 24, y - 24, 50, 50);
        gc.setFill(alpha(Color.web("#fff8d6"), alpha));
        gc.fillOval(muzzleX - 12, y - 13, 28, 28);

        for (int i = 0; i < 10; i++) {
            double s = rnd(i, 51);
            double px = targetX + direction * (60 + s * 260) * progress;
            double py = y - 30 + rnd(i, 52) * 70 - progress * 30;
            gc.setFill(alpha(Color.web("#e8e0d0"), 0.35 * alpha));
            gc.fillOval(px, py, 8 + s * 16, 8 + s * 16);
        }

        if (special && target != null && target.isAlive()) {
            double tx = target.getX();
            double ty = target.getY() - target.getCurrentHeight() * 0.62;
            BloodFX.splatter(gc, tx, ty, progress, 1.0, direction,
                    CombatConstants.GROUND_Y);
        }

        gc.setFill(alpha(Color.web("#f4c542"), alpha));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 30));
        String shout = "boss_polavieja".equals(owner.getSpriteId())
                ? "FIRE!"
                : "JUDGMENT!";
        gc.fillText(shout, targetX + direction * 130, y - 90);
    }

    private double combatGround() {
        return CombatConstants.GROUND_Y - 140;
    }

    public boolean isHigh() {
        return high;
    }

    public void markSpecial() {
        special = true;
    }

    public boolean isSpecial() {
        return special;
    }

    public boolean isAlive() {
        return alive;
    }

    public boolean hasHit() {
        return hit;
    }

    public void markHit() {
        hit = true;
    }

    public Fighter getOwner() {
        return owner;
    }

    public Fighter getTarget() {
        return target;
    }

    public double getDamage() {
        return damage;
    }

    public Kind getKind() {
        return kind;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp01(double value) {
        return Math.max(0, Math.min(1, value));
    }

    private static double rnd(int index, int salt) {
        double v = Math.sin(index * 12.9898 + salt * 78.233) * 43758.5453;
        return v - Math.floor(v);
    }

    private static Color alpha(Color color, double a) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(),
                clamp01(a));
    }

    private static Color mix(Color a, Color b, double t) {
        double f = clamp01(t);
        return new Color(
                a.getRed() + (b.getRed() - a.getRed()) * f,
                a.getGreen() + (b.getGreen() - a.getGreen()) * f,
                a.getBlue() + (b.getBlue() - a.getBlue()) * f,
                1);
    }
}
