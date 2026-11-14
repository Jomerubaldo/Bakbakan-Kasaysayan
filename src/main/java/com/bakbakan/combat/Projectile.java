package com.bakbakan.combat;

import com.bakbakan.entities.Fighter;
import com.bakbakan.util.BloodFX;
import com.bakbakan.util.GameConfig;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;

public final class Projectile {

    public enum Kind {
        BOOK, INK, RIFLE, QUILL, KAMPILAN_WAVE, TIDAL_WAVE
    }

    private final Kind kind;
    private final Fighter owner;
    private final double damage;
    private final boolean high;
    private final double width;
    private final double height;
    private final double direction;
    private final double vx;
    private final double vy;
    private final double spin;

    private double x;
    private double y;
    private double ttl;
    private double age;
    private double rotation;
    private boolean alive = true;
    private boolean hit;
    private boolean special;

    private Projectile(Kind kind, Fighter owner, double damage, boolean high,
                       double x, double y, double vx, double vy,
                       double width, double height, double ttl, double spin,
                       boolean facingRight) {
        this.kind = kind;
        this.owner = owner;
        this.damage = damage;
        this.high = high;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.width = width;
        this.height = height;
        this.ttl = ttl;
        this.spin = spin;
        this.direction = facingRight ? 1 : -1;
    }

    public static Projectile book(Fighter owner, boolean facingRight) {
        return new Projectile(Kind.BOOK, owner, owner.getAttackDamage(), true,
                startX(owner, facingRight), owner.getY() - 122,
                speed(620, facingRight), 0,
                28, 24, 1.3, 9, facingRight);
    }

    public static Projectile ink(Fighter owner, boolean facingRight) {
        return new Projectile(Kind.INK, owner,
                owner.scaleDamage(CombatConstants.SKILL_DAMAGE_INK), false,
                startX(owner, facingRight), owner.getY() - 95,
                speed(480, facingRight), 0,
                34, 34, 1.0, 5, facingRight);
    }

    public static Projectile rifle(Fighter owner, boolean facingRight) {
        return new Projectile(Kind.RIFLE, owner,
                owner.scaleDamage(CombatConstants.SKILL_DAMAGE_RIFLE), true,
                startX(owner, facingRight), owner.getY() - 112,
                speed(950, facingRight), 0,
                36, 10, 1.2, 0, facingRight);
    }

    public static Projectile saberCommand(Fighter owner, boolean facingRight) {
        return new Projectile(Kind.KAMPILAN_WAVE, owner,
                owner.scaleDamage(CombatConstants.SKILL_DAMAGE_SABER_COMMAND), false,
                startX(owner, facingRight), owner.getY() - 100,
                speed(540, facingRight), 0,
                74, 60, 0.9, 0, facingRight);
    }

    public static Projectile boloStrike(Fighter owner, boolean facingRight) {
        return new Projectile(Kind.KAMPILAN_WAVE, owner,
                owner.scaleDamage(CombatConstants.SKILL_DAMAGE_BOLO_STRIKE), false,
                startX(owner, facingRight), owner.getY() - 84,
                speed(470, facingRight), 0,
                58, 46, 0.9, 0, facingRight);
    }

    public static Projectile rifleBurst(Fighter owner, boolean facingRight,
                                        double heightOffset) {
        return new Projectile(Kind.RIFLE, owner,
                owner.scaleDamage(CombatConstants.SKILL_DAMAGE_RIFLE_BURST_HIT), true,
                startX(owner, facingRight), owner.getY() - 112 + heightOffset,
                speed(1050, facingRight), 0,
                36, 10, 1.0, 0, facingRight);
    }

    public static Projectile heavyRifle(Fighter owner, boolean facingRight) {
        return new Projectile(Kind.RIFLE, owner,
                owner.scaleDamage(CombatConstants.SKILL_DAMAGE_HEAVY_RIFLE), true,
                startX(owner, facingRight), owner.getY() - 112,
                speed(820, facingRight), 0,
                48, 12, 1.2, 0, facingRight);
    }

    public static Projectile arquebus(Fighter owner, boolean facingRight) {
        return new Projectile(Kind.RIFLE, owner,
                owner.scaleDamage(CombatConstants.SKILL_DAMAGE_ARQUEBUS), true,
                startX(owner, facingRight), owner.getY() - 108,
                speed(620, facingRight), 0,
                44, 16, 1.4, 0, facingRight);
    }

    public static Projectile quill(Fighter owner, boolean facingRight) {
        return new Projectile(Kind.QUILL, owner,
                owner.scaleDamage(CombatConstants.SKILL_DAMAGE_QUILL), true,
                startX(owner, facingRight), owner.getY() - 118,
                speed(620, facingRight), 0,
                32, 14, 1.2, 0, facingRight);
    }

    public static Projectile kampilanWave(Fighter owner, boolean facingRight) {
        return new Projectile(Kind.KAMPILAN_WAVE, owner,
                owner.scaleDamage(CombatConstants.SKILL_DAMAGE_KAMPILAN_WAVE), false,
                startX(owner, facingRight), owner.getY() - 80,
                speed(430, facingRight), 0,
                64, 54, 1.2, 0, facingRight);
    }

    public static Projectile tidalWave(Fighter owner, boolean facingRight) {
        return new Projectile(Kind.TIDAL_WAVE, owner,
                owner.scaleDamage(CombatConstants.SPECIAL_DAMAGE_TIDAL_WAVE), false,
                startX(owner, facingRight), owner.getY() - 100,
                speed(360, facingRight), 0,
                120, 100, 1.6, 0, facingRight);
    }

    private static double startX(Fighter owner, boolean facingRight) {
        return owner.getX() + (facingRight ? 1 : -1)
                * (CombatConstants.FIGHTER_WIDTH / 2.0 + 26);
    }

    private static double speed(double speed, boolean facingRight) {
        return facingRight ? speed : -speed;
    }

    public void update(double dt) {
        age += dt;
        ttl -= dt;
        x += vx * dt;
        y += vy * dt;
        rotation += spin * dt;
        if (ttl <= 0 || x < -180 || x > GameConfig.WIDTH + 180) {
            alive = false;
        }
    }

    public Rectangle2D getHitbox() {
        return new Rectangle2D(x - width / 2.0, y - height / 2.0, width, height);
    }

    public void render(GraphicsContext gc) {
        gc.save();
        gc.translate(x, y);
        switch (kind) {
            case BOOK -> drawBook(gc);
            case INK -> drawInk(gc);
            case RIFLE -> drawRifle(gc);
            case QUILL -> drawQuill(gc);
            case KAMPILAN_WAVE -> drawKampilanWave(gc);
            case TIDAL_WAVE -> drawTidalWave(gc);
        }
        gc.restore();
    }

    private void drawBook(GraphicsContext gc) {
        Color accent = Color.web(owner.getHero().accentColor());
        gc.rotate(rotation);
        gc.setFill(Color.web("#000000", 0.2));
        gc.fillRoundRect(-14, -10, 30, 24, 5, 5);
        gc.setFill(accent);
        gc.fillRoundRect(-15, -12, 30, 24, 5, 5);
        gc.setFill(Color.web("#f5f0e1"));
        gc.fillRoundRect(-9, -9, 18, 18, 3, 3);
        gc.setStroke(Color.web("#b8b0a0"));
        gc.setLineWidth(1);
        gc.strokeLine(-6, -5, 6, -5);
        gc.strokeLine(-6, 0, 6, 0);
        gc.strokeLine(-6, 5, 6, 5);
    }

    private void drawInk(GraphicsContext gc) {
        Color accent = Color.web(owner.getHero().primaryColor());
        double wobble = Math.sin(age * 20) * 2;
        gc.setFill(new Color(accent.getRed() * 0.25,
                accent.getGreen() * 0.25, accent.getBlue() * 0.3, 0.9));
        gc.fillOval(-18, -18 + wobble, 36, 36);
        gc.setFill(Color.web("#10141f", 0.9));
        gc.fillOval(-13, -13 + wobble, 26, 26);
        gc.setFill(Color.web("#10141f", 0.7));
        gc.fillOval(-24, -8, 9, 9);
        gc.fillOval(14, 6, 8, 8);
        gc.fillOval(-4, 17, 7, 7);
        gc.setFill(Color.web("#f4f6ff", 0.35));
        gc.fillOval(-8, -9 + wobble, 6, 5);
    }

    private void drawRifle(GraphicsContext gc) {
        String spriteId = owner.getSpriteId();
        if ("boss_janolino".equals(spriteId)) {
            drawGuardBurst(gc);
            return;
        }
        if ("boss_macarthur".equals(spriteId)) {
            drawHeavyRifle(gc);
            return;
        }
        if ("boss_magellan".equals(spriteId)) {
            drawArquebus(gc);
            return;
        }
        gc.setStroke(Color.web("#f4c542", 0.25));
        gc.setLineWidth(12);
        gc.strokeLine(-direction * 56, 0, 0, 0);
        gc.setStroke(Color.web("#fff2b0", 0.9));
        gc.setLineWidth(3);
        gc.strokeLine(-direction * 48, 0, 0, 0);
        gc.setFill(Color.web("#f4c542"));
        gc.fillOval(-4, -4, 8, 8);
        gc.setFill(Color.WHITE);
        gc.fillOval(-2, -2, 4, 4);
    }

    private void drawGuardBurst(GraphicsContext gc) {
        gc.setStroke(Color.web("#c0392b", 0.22));
        gc.setLineWidth(11);
        gc.strokeLine(-direction * 52, 0, 0, 0);
        gc.setStroke(Color.web("#ff8a7a", 0.95));
        gc.setLineWidth(3);
        gc.strokeLine(-direction * 46, 0, 0, 0);
        gc.setFill(Color.web("#e74c3c"));
        gc.fillOval(-4, -4, 8, 8);
        gc.setFill(Color.WHITE);
        gc.fillOval(-2, -2, 4, 4);
    }

    private void drawHeavyRifle(GraphicsContext gc) {
        gc.setStroke(Color.web("#3f6fa8", 0.24));
        gc.setLineWidth(16);
        gc.strokeLine(-direction * 64, 0, 0, 0);
        gc.setStroke(Color.web("#dce9ff", 0.95));
        gc.setLineWidth(4);
        gc.strokeLine(-direction * 56, 0, 0, 0);
        gc.setFill(Color.web("#8fc2ff"));
        gc.fillOval(-5, -5, 10, 10);
        gc.setFill(Color.WHITE);
        gc.fillOval(-2, -2, 4, 4);
    }

    private void drawArquebus(GraphicsContext gc) {
        gc.setFill(Color.web("#e8e0d0", 0.35));
        for (int i = 0; i < 4; i++) {
            double px = -direction * (30 + i * 18);
            double size = 12 - i * 2;
            gc.fillOval(px - size / 2, -size / 2, size, size);
        }
        gc.setFill(Color.web("#2a2320"));
        gc.fillOval(-5, -5, 10, 10);
        gc.setFill(Color.web("#c7cdd6"));
        gc.fillOval(-3, -3, 6, 6);
        gc.setFill(Color.web("#fff2b0", 0.8));
        gc.fillOval(-2, -2, 4, 4);
    }

    private void drawQuill(GraphicsContext gc) {
        gc.scale(direction, 1);
        gc.rotate(-18);
        gc.setStroke(Color.web("#f5f0e1"));
        gc.setLineWidth(3);
        gc.strokeLine(-16, 0, 14, 0);
        gc.setFill(Color.web("#f5f0e1"));
        gc.fillPolygon(
                new double[]{-2, 8, 14, 4, -6},
                new double[]{-1, -10, -2, 4, 7}, 5);
        gc.setStroke(Color.web(owner.getHero().accentColor()));
        gc.setLineWidth(2);
        gc.strokeLine(-16, 0, -20, 0);
    }

    private void drawKampilanWave(GraphicsContext gc) {
        gc.scale(direction, 1);
        String spriteId = owner.getSpriteId();
        if ("boss_polavieja".equals(spriteId)) {
            drawSaberCommand(gc);
            return;
        }
        if ("boss_makapagal".equals(spriteId)) {
            drawBoloStrike(gc);
            return;
        }
        Color accent = Color.web(owner.getHero().accentColor());
        gc.setStroke(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0.25));
        gc.setLineWidth(16);
        gc.strokeArc(-30, -27, 64, 54, -70, 140, ArcType.OPEN);
        gc.setStroke(accent);
        gc.setLineWidth(6);
        gc.strokeArc(-30, -27, 64, 54, -70, 140, ArcType.OPEN);
        gc.setStroke(Color.web("#f5f0e1", 0.8));
        gc.setLineWidth(2);
        gc.strokeArc(-22, -19, 48, 38, -70, 140, ArcType.OPEN);
        gc.setLineCap(StrokeLineCap.ROUND);
    }

    private void drawSaberCommand(GraphicsContext gc) {
        gc.setStroke(Color.web("#d4af37", 0.22));
        gc.setLineWidth(20);
        gc.strokeArc(-38, -35, 82, 70, -72, 144, ArcType.OPEN);
        gc.setStroke(Color.web("#f4c542", 0.95));
        gc.setLineWidth(7);
        gc.strokeArc(-38, -35, 82, 70, -72, 144, ArcType.OPEN);
        gc.setStroke(Color.web("#fff6c8", 0.9));
        gc.setLineWidth(2.5);
        gc.strokeArc(-30, -27, 66, 54, -72, 144, ArcType.OPEN);
        gc.setStroke(Color.web("#a3171b", 0.7));
        gc.setLineWidth(2);
        gc.strokeArc(-46, -42, 98, 84, -66, 132, ArcType.OPEN);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setFill(Color.web("#d4af37", 0.5));
        gc.fillOval(20, -6, 10, 10);
    }

    private void drawBoloStrike(GraphicsContext gc) {
        gc.setStroke(Color.web("#5a3f2b", 0.30));
        gc.setLineWidth(16);
        gc.strokeArc(-28, -24, 62, 50, -66, 132, ArcType.OPEN);
        gc.setStroke(Color.web("#c9d1dc"));
        gc.setLineWidth(6);
        gc.strokeArc(-28, -24, 62, 50, -66, 132, ArcType.OPEN);
        gc.setStroke(Color.web("#8a2b23"));
        gc.setLineWidth(2.5);
        gc.strokeArc(-20, -17, 46, 36, -66, 132, ArcType.OPEN);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setFill(Color.web("#7a4a2a"));
        gc.fillRoundRect(-14, 14, 16, 8, 3, 3);
    }

    private void drawTidalWave(GraphicsContext gc) {
        gc.scale(direction, 1);

        gc.setFill(new RadialGradient(0, 0, 0, 20, 120, false,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#bfe9ff", 0.35)),
                new Stop(1, Color.web("#bfe9ff", 0))));
        gc.fillOval(-120, -100, 240, 240);

        gc.setFill(Color.web("#1b6fb8", 0.30));
        gc.fillPolygon(
                new double[]{-78, -34, 8, 44, 76, 58, 16, -30, -78},
                new double[]{48, 6, 16, -8, 30, 52, 44, 56, 54}, 9);

        gc.setFill(Color.web("#2e86de", 0.85));
        gc.fillPolygon(
                new double[]{-70, -28, 6, 38, 66, 48, 10, -36, -70},
                new double[]{44, 14, 22, 2, 34, 50, 46, 54, 52}, 9);

        gc.setFill(Color.web("#54b7f0", 0.9));
        gc.fillPolygon(
                new double[]{-62, -26, 4, 30, 52, 40, 6, -32, -62},
                new double[]{40, 22, 28, 14, 36, 48, 44, 50, 48}, 9);

        gc.setFill(Color.web("#d6f1ff", 0.95));
        gc.fillOval(-44, 12, 20, 15);
        gc.fillOval(-14, 4, 18, 13);
        gc.fillOval(16, 10, 22, 15);
        gc.fillOval(42, 24, 15, 11);

        gc.setStroke(Color.web("#eaf9ff", 0.9));
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineWidth(4);
        gc.beginPath();
        gc.moveTo(-48, 8);
        gc.bezierCurveTo(-20, -22, -4, -30, 26, -18);
        gc.bezierCurveTo(40, -12, 50, -4, 56, 6);
        gc.stroke();

        for (int i = 0; i < 9; i++) {
            double s = (Math.sin(age * 7 + i * 12.9898) * 43758.5453) % 1;
            if (s < 0) {
                s += 1;
            }
            double px = -50 + s * 120;
            double py = 6 - age * 40 - s * 30;
            gc.setFill(Color.web("#eaf9ff", 0.6));
            gc.fillOval(px, py, 3 + s * 4, 3 + s * 4);
        }

        BloodFX.splatter(gc, 4, 16, Math.min(1, age * 1.1), 0.45, 1, 52);

        gc.setFill(Color.web("#0b4f74", 0.25));
        gc.fillOval(-70, 46, 150, 12);
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

    public void setAlive(boolean alive) {
        this.alive = alive;
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

    public double getDamage() {
        return damage;
    }

    public Kind getKind() {
        return kind;
    }
}
