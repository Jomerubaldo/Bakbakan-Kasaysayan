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
    private int slide;
    private double slideTime;
    private boolean active;

    public void start(HeroData hero, HeroData boss) {
        this.hero = hero;
        this.boss = boss;
        this.slide = 0;
        this.slideTime = 0;
        this.active = true;
    }

    public void stop() {
        active = false;
        hero = null;
        boss = null;
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
        gc.fillText("Pindutin ang ENTER upang magpatuloy",
                GameConfig.WIDTH / 2.0, GameConfig.HEIGHT - 26);
    }

    private void drawWarning(GraphicsContext gc) {
        double pulse = 0.75 + 0.25 * Math.sin(slideTime * 6.0);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web("#ff3b3b", 0.14 * pulse));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 132));
        gc.fillText("BABALA", GameConfig.WIDTH / 2.0 + 3, 258 + 3);
        gc.setFill(Color.web("#ff6b6b", pulse));
        gc.fillText("BABALA", GameConfig.WIDTH / 2.0, 258);

        gc.setFill(Color.web("#f4c542"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 34));
        gc.fillText("HULING ROUND", GameConfig.WIDTH / 2.0, 330);

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
        gc.fillText("ANG LIMANG BAYANI", GameConfig.WIDTH / 2.0, 142);

        gc.setFill(Color.web("#b9c2da"));
        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
        gc.fillText("Ang kanilang mga huling araw, ayon sa kasaysayan.",
                GameConfig.WIDTH / 2.0, 172);

        String[][] rows = {
                {"Jose Rizal", "Disyembre 30, 1896", "Binaril sa Bagumbayan"},
                {"Andres Bonifacio", "Mayo 10, 1897", "Binaril sa Maragondon"},
                {"Antonio Luna", "Hunyo 5, 1899", "Pinaslang sa Cabanatuan"},
                {"Apolinario Mabini", "Mayo 13, 1903", "Namatay sa cholera"},
                {"Lapulapu", "Abril 27, 1521", "Nanalo sa Mactan"}
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
        gc.fillText("Ang kanyang alaala ay buhay sa bawat laban.",
                GameConfig.WIDTH / 2.0, 600);
    }

    private void drawBoss(GraphicsContext gc, SpriteProvider sprites) {
        sprites.renderPortrait(gc, boss, 985, 604, 2.45, false);

        double pulse = 0.6 + 0.4 * Math.sin(slideTime * 4.0);
        gc.setFill(Color.web("#ff3b3b", 0.10 + 0.06 * pulse));
        gc.fillOval(760, 170, 360, 360);

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFill(Color.web("#ff6b6b"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        gc.fillText("HULING KALABAN", 120, 180);

        gc.setFill(Color.web("#f4f6ff"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 62));
        gc.fillText(boss.name(), 120, 252);

        gc.setFill(Color.web("#f4c542"));
        gc.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 22));
        gc.fillText("Ang anino ng kasaysayan", 120, 292);

        gc.setStroke(Color.web("#ff6b6b", 0.55));
        gc.setLineWidth(2);
        gc.strokeLine(120, 316, 700, 316);

        drawWrappedText(gc,
                "Ako ang anino ng lahat ng naging dahilan ng inyong kamatayan. "
                        + "Harapin mo ang kasaysayan, bayani.",
                120, 362, 560, 34,
                Font.font("Segoe UI", FontWeight.NORMAL, 22),
                Color.web("#e8b4b8"), TextAlignment.LEFT);

        gc.setFill(Color.web("#f4c542", 0.9));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        gc.fillText("ROUND 5 — FINAL BATTLE", 120, 520);
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
                    "Hunyo 19, 1861 — Disyembre 30, 1896",
                    "Binaril ng firing squad sa Bagumbayan.",
                    "Inakusahan si Rizal ng rebelyon ng military court ng Espanya. "
                            + "Inaprubahan ni Gobernador-Heneral Camilo de Polavieja "
                            + "ang hatol na kamatayan. Binaril siya sa Bagumbayan "
                            + "noong Disyembre 30, 1896. Ang kanyang mga nobela at tula "
                            + "ang naging apoy ng rebolusyon.");
            case "bonifacio" -> new Story(
                    "Andres Bonifacio",
                    "Nobyembre 30, 1863 — Mayo 10, 1897",
                    "Binaril sa utos ng Council of War.",
                    "Si Bonifacio at ang kapatid niyang si Procopio ay hinatulan "
                            + "ng Council of War na pinamunuan ni Heneral Mariano Noriel. "
                            + "Iniutos ni Emilio Aguinaldo na ipatupad ang hatol. "
                            + "Si Major Lazaro Makapagal ang nagsagawa ng pagbaril "
                            + "sa Maragondon noong Mayo 10, 1897. Kapatid ang pumatay "
                            + "sa kapatid.");
            case "luna" -> new Story(
                    "Antonio Luna",
                    "Oktubre 29, 1866 — Hunyo 5, 1899",
                    "Pinaslang sa Cabanatuan.",
                    "Sumugod si Luna sa Cabanatuan upang makipagpulong kay Aguinaldo. "
                            + "Doon siya sinalakay ng mga miyembro ng Presidential Guard, "
                            + "kasama si Kapitan Pedro Janolino. Mahigit 30 ang sugat niya. "
                            + "Namatay siya sa plaza kasama ang aide niyang si "
                            + "Col. Francisco Roman noong Hunyo 5, 1899.");
            case "mabini" -> new Story(
                    "Apolinario Mabini",
                    "Hulyo 23, 1864 — Mayo 13, 1903",
                    "Namatay sa cholera pagkatapos ng exile.",
                    "Ipinatapon si Mabini ng mga Amerikano sa Guam dahil sa "
                            + "pagtangging sumumpa ng katapatan. Pagbalik niya sa Manila "
                            + "noong 1903, mahina na ang kanyang katawan. Namatay siya "
                            + "sa cholera noong Mayo 13, 1903. Hindi siya natalo sa "
                            + "labanan—dinaig siya ng sakit at ng pagkatapon.");
            default -> new Story(
                    "Lapulapu",
                    "Ipinanganak bago ang 1521 — hindi tiyak ang petsa ng kamatayan",
                    "Nanalo sa Mactan; hindi alam ang naging wakas.",
                    "Pinamunuan ni Lapulapu ang mga mandirigma ng Mactan laban kay "
                            + "Ferdinand Magellan noong Abril 27, 1521. Natalo ang "
                            + "hukbong Espanyol at napatay si Magellan. Walang talaan "
                            + "kung paano namatay si Lapulapu. Hindi siya natalo ng "
                            + "mga mananakop.");
        };
    }

    private record Story(String title, String dates, String cause, String body) {
    }
}
