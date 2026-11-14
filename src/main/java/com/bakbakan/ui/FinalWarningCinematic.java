package com.bakbakan.ui;

import com.bakbakan.assets.SpriteProvider;
import com.bakbakan.entities.HeroData;
import com.bakbakan.util.GameConfig;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;

public final class FinalWarningCinematic {

    private static final int SLIDE_WARNING = 0;
    private static final int SLIDE_HEROES = 1;
    private static final int SLIDE_STORY = 2;
    private static final int SLIDE_BOSS = 3;
    private static final int SLIDE_COUNT = 4;

    private static final double BAR = 64;

    private HeroData hero;
    private HeroData boss;
    private String bossSpriteId;
    private int slide;
    private double slideTime;
    private boolean active;

    public void start(HeroData hero, HeroData boss) {
        start(hero, boss, boss != null ? boss.id() : null);
    }

    public void start(HeroData hero, HeroData boss, String bossSpriteId) {
        this.hero = hero;
        this.boss = boss;
        this.bossSpriteId = bossSpriteId;
        this.slide = 0;
        this.slideTime = 0;
        this.active = true;
    }

    public void stop() {
        active = false;
        hero = null;
        boss = null;
        bossSpriteId = null;
    }

    public boolean isActive() {
        return active;
    }

    public void update(double dt) {
        if (!active) {
            return;
        }
        slideTime += dt;
    }

    public void advance() {
        if (!active) {
            return;
        }
        nextSlide();
    }

    private void nextSlide() {
        slide++;
        slideTime = 0;
        if (slide >= SLIDE_COUNT) {
            active = false;
        }
    }

    public void render(GraphicsContext gc, SpriteProvider sprites) {
        if (!active || hero == null || boss == null) {
            return;
        }
        gc.save();
        drawBackdrop(gc);
        switch (slide) {
            case SLIDE_WARNING -> drawWarning(gc);
            case SLIDE_HEROES -> drawHeroes(gc);
            case SLIDE_STORY -> drawStory(gc, sprites);
            case SLIDE_BOSS -> drawBoss(gc, sprites);
            default -> {
            }
        }
        drawLetterbox(gc);
        drawHint(gc);
        gc.restore();
    }

    private void drawBackdrop(GraphicsContext gc) {
        gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#07070d")),
                new Stop(0.45, Color.web("#1b0c18")),
                new Stop(0.75, Color.web("#2a0c16")),
                new Stop(1, Color.web("#09060b"))));
        gc.fillRect(0, 0, GameConfig.WIDTH, GameConfig.HEIGHT);

        double pulse = 0.5 + 0.5 * Math.sin(slideTime * 3.2);
        gc.setFill(Color.web("#c0392b", 0.06 + 0.04 * pulse));
        gc.fillOval(GameConfig.WIDTH / 2.0 - 320, 80, 640, 640);

        gc.setStroke(Color.web("#ffffff", 0.035));
        gc.setLineWidth(1);
        for (double x = 80; x < GameConfig.WIDTH; x += 80) {
            gc.strokeLine(x, 0, x, GameConfig.HEIGHT);
        }
        for (double y = 80; y < GameConfig.HEIGHT; y += 80) {
            gc.strokeLine(0, y, GameConfig.WIDTH, y);
        }

        for (int i = 0; i < 34; i++) {
            double ex = (i * 157 + slideTime * 22 * (i % 3 + 1)) % GameConfig.WIDTH;
            double ey = (i * 97 + slideTime * (20 + (i % 5) * 8)) % GameConfig.HEIGHT;
            double alpha = 0.08 + 0.10 * Math.sin(slideTime * 2 + i);
            gc.setFill(Color.web("#ff6b6b", Math.max(0.02, alpha)));
            gc.fillOval(ex, ey, 2 + (i % 2), 2 + (i % 2));
        }
    }

    private void drawLetterbox(GraphicsContext gc) {
        gc.setFill(Color.web("#030306", 0.96));
        gc.fillRect(0, 0, GameConfig.WIDTH, BAR);
        gc.fillRect(0, GameConfig.HEIGHT - BAR, GameConfig.WIDTH, BAR);
        gc.setStroke(Color.web("#f4c542", 0.18));
        gc.setLineWidth(1);
        gc.strokeLine(0, BAR, GameConfig.WIDTH, BAR);
        gc.strokeLine(0, GameConfig.HEIGHT - BAR, GameConfig.WIDTH,
                GameConfig.HEIGHT - BAR);
    }

    private void drawHint(GraphicsContext gc) {
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web("#8f9ab8"));
        gc.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        gc.fillText("Press ENTER to continue",
                GameConfig.WIDTH / 2.0, GameConfig.HEIGHT - 26);
    }

    private void drawWarning(GraphicsContext gc) {
        double pulse = 0.75 + 0.25 * Math.sin(slideTime * 6.0);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web("#ff3b3b", 0.14 * pulse));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 132));
        gc.fillText("WARNING", GameConfig.WIDTH / 2.0 + 3, 258 + 3);
        gc.setFill(Color.web("#ff6b6b", pulse));
        gc.fillText("WARNING", GameConfig.WIDTH / 2.0, 258);

        gc.setFill(Color.web("#f4c542"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 34));
        gc.fillText("FINAL ROUND", GameConfig.WIDTH / 2.0, 330);

        gc.setStroke(Color.web("#ff6b6b", 0.55));
        gc.setLineWidth(2);
        gc.strokeLine(360, 356, GameConfig.WIDTH - 360, 356);

        gc.setFill(Color.web("#f4f6ff"));
        gc.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 22));
        gc.fillText("FINAL ENEMY", GameConfig.WIDTH / 2.0, 404);
        gc.setFill(Color.web("#e8b4b8"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 46));
        gc.fillText(boss.name(), GameConfig.WIDTH / 2.0, 458);
    }

    private void drawHeroes(GraphicsContext gc) {
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web("#f4c542"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 34));
        gc.fillText("THE FIVE HEROES", GameConfig.WIDTH / 2.0, 142);

        gc.setFill(Color.web("#b9c2da"));
        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
        gc.fillText("Their final days, as history recorded them.",
                GameConfig.WIDTH / 2.0, 172);

        String[][] rows = {
                {"Jose Rizal", "December 30, 1896", "Shot at Bagumbayan"},
                {"Andres Bonifacio", "May 10, 1897", "Shot at Maragondon"},
                {"Antonio Luna", "June 5, 1899", "Killed at Cabanatuan"},
                {"Apolinario Mabini", "May 13, 1903", "Died of cholera"},
                {"Lapulapu", "April 27, 1521", "Won at Mactan"}
        };

        double top = 216;
        double rowH = 64;
        for (int i = 0; i < rows.length; i++) {
            boolean chosen = hero.id().equals(switch (i) {
                case 0 -> "rizal";
                case 1 -> "bonifacio";
                case 2 -> "luna";
                case 3 -> "mabini";
                default -> "lapulapu";
            });
            double y = top + i * rowH;

            gc.setFill(chosen
                    ? Color.web("#f4c542", 0.16)
                    : Color.web("#ffffff", 0.04));
            gc.fillRoundRect(190, y - 32, GameConfig.WIDTH - 380, 52, 12, 12);
            if (chosen) {
                gc.setStroke(Color.web("#f4c542", 0.75));
                gc.setLineWidth(2);
                gc.strokeRoundRect(190, y - 32, GameConfig.WIDTH - 380, 52, 12, 12);
            }

            gc.setTextAlign(TextAlignment.LEFT);
            gc.setFill(chosen ? Color.web("#ffe89a") : Color.web("#f4f6ff"));
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
            gc.fillText(rows[i][0], 220, y);

            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFill(chosen ? Color.web("#f4c542") : Color.web("#8f9ab8"));
            gc.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 15));
            gc.fillText(rows[i][1], GameConfig.WIDTH / 2.0, y);

            gc.setTextAlign(TextAlignment.RIGHT);
            gc.setFill(chosen ? Color.web("#e8e8f0") : Color.web("#b9c2da"));
            gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
            gc.fillText(rows[i][2], GameConfig.WIDTH - 220, y);
        }
    }

    private void drawStory(GraphicsContext gc, SpriteProvider sprites) {
        sprites.renderPortrait(gc, hero, 300, 604, 2.35, true);

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFill(Color.web("#f4c542"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 40));
        gc.fillText(story().title(), 540, 150);

        gc.setFill(Color.web("#b9c2da"));
        gc.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 16));
        gc.fillText(story().dates(), 540, 182);

        gc.setFill(Color.web("#ff8a8a"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        gc.fillText(story().cause(), 540, 220);

        gc.setStroke(Color.web("#f4c542", 0.35));
        gc.setLineWidth(2);
        gc.strokeLine(540, 242, 1140, 242);

        drawWrappedText(gc, story().body(), 540, 286, 600, 32,
                Font.font("Segoe UI", FontWeight.NORMAL, 19),
                Color.web("#e8e8f0"), TextAlignment.LEFT);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web("#f4c542", 0.8));
        gc.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 15));
        gc.fillText("The memory lives on in every battle.",
                GameConfig.WIDTH / 2.0, 600);
    }

    private void drawBoss(GraphicsContext gc, SpriteProvider sprites) {
        sprites.renderPortrait(gc, boss, bossSpriteId, 985, 604, 2.45, false);

        double pulse = 0.6 + 0.4 * Math.sin(slideTime * 4.0);
        gc.setFill(Color.web("#ff3b3b", 0.10 + 0.06 * pulse));
        gc.fillOval(760, 170, 360, 360);

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFill(Color.web("#ff6b6b"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        gc.fillText("FINAL OPPONENT", 120, 180);

        gc.setFill(Color.web("#f4f6ff"));
        Font nameFont = Font.font("Segoe UI", FontWeight.BOLD, 46);
        gc.setFont(nameFont);
        double nameY = 248;
        for (String line : wrap(boss.name(), nameFont, 580)) {
            gc.fillText(line, 120, nameY);
            nameY += 50;
        }

        double weaponY = nameY + 2;
        gc.setFill(Color.web("#f4c542"));
        gc.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 22));
        gc.fillText(boss.weapon(), 120, weaponY);

        double dividerY = weaponY + 24;
        gc.setStroke(Color.web("#ff6b6b", 0.55));
        gc.setLineWidth(2);
        gc.strokeLine(120, dividerY, 700, dividerY);

        double bodyY = dividerY + 44;
        Font bodyFont = Font.font("Segoe UI", FontWeight.NORMAL, 21);
        List<String> bodyLines = wrap(boss.description(), bodyFont, 560);
        drawWrappedText(gc, boss.description(), 120, bodyY, 560, 30,
                bodyFont, Color.web("#e8b4b8"), TextAlignment.LEFT);

        double roundY = Math.min(614, bodyY + bodyLines.size() * 30 + 18);
        gc.setFill(Color.web("#f4c542", 0.9));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        gc.fillText("ROUND 5 — FINAL BATTLE", 120, roundY);
    }

    private void drawWrappedText(GraphicsContext gc, String text, double x, double y,
                                 double maxWidth, double lineHeight, Font font,
                                 Color color, TextAlignment align) {
        gc.setFont(font);
        gc.setFill(color);
        gc.setTextAlign(align);
        double cursor = y;
        for (String line : wrap(text, font, maxWidth)) {
            gc.fillText(line, x, cursor);
            cursor += lineHeight;
        }
    }

    private static List<String> wrap(String text, Font font, double maxWidth) {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : text.split(" ")) {
            String candidate = current.length() == 0 ? word : current + " " + word;
            Text measure = new Text(candidate);
            measure.setFont(font);
            if (measure.getLayoutBounds().getWidth() > maxWidth && current.length() > 0) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current = new StringBuilder(candidate);
            }
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines;
    }

    private Story story() {
        return switch (hero.id()) {
            case "rizal" -> new Story(
                    "Jose Rizal",
                    "June 19, 1861 — December 30, 1896",
                    "Executed by firing squad at Bagumbayan.",
                    "A Spanish military court convicted Rizal of rebellion. "
                            + "Governor-General Camilo de Polavieja approved the death "
                            + "sentence. He was shot at Bagumbayan on December 30, 1896. "
                            + "His novels and poems became the spark of the revolution.");
            case "bonifacio" -> new Story(
                    "Andres Bonifacio",
                    "November 30, 1863 — May 10, 1897",
                    "Executed on orders of the Council of War.",
                    "Bonifacio and his brother Procopio were sentenced by the Council "
                            + "of War led by General Mariano Noriel. Emilio Aguinaldo "
                            + "ordered the sentence carried out. Major Lazaro Makapagal "
                            + "led the shooting at Maragondon on May 10, 1897. Brother "
                            + "killed brother.");
            case "luna" -> new Story(
                    "Antonio Luna",
                    "October 29, 1866 — June 5, 1899",
                    "Assassinated at Cabanatuan.",
                    "Luna rushed to Cabanatuan to meet with Aguinaldo. There he was "
                            + "attacked by members of the Presidential Guard, including "
                            + "Captain Pedro Janolino. He suffered more than 30 wounds. "
                            + "He died in the plaza alongside his aide, Col. Francisco "
                            + "Roman, on June 5, 1899.");
            case "mabini" -> new Story(
                    "Apolinario Mabini",
                    "July 23, 1864 — May 13, 1903",
                    "Died of cholera after exile.",
                    "The Americans exiled Mabini to Guam for refusing to swear "
                            + "allegiance. When he returned to Manila in 1903, his body "
                            + "was already weak. He died of cholera on May 13, 1903. He "
                            + "was never defeated in battle—disease and exile overcame "
                            + "him.");
            default -> new Story(
                    "Lapulapu",
                    "Born before 1521 — exact date of death unknown",
                    "Won at Mactan; his final fate is unknown.",
                    "Lapulapu led the warriors of Mactan against Ferdinand Magellan "
                            + "on April 27, 1521. The Spanish force was defeated and "
                            + "Magellan was killed. No record shows how Lapulapu died. "
                            + "He was never defeated by the colonizers.");
        };
    }

    private record Story(String title, String dates, String cause, String body) {
    }
}
