package com.bakbakan.screens;

import com.bakbakan.entities.GameSession;
import com.bakbakan.entities.HeroData;
import com.bakbakan.events.GameEventBus;
import com.bakbakan.events.ScreenChangeEvent;
import com.bakbakan.events.SoundRequestEvent;
import com.bakbakan.util.GameConfig;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
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

public class HeroSelectScreen implements Screen {

    private static final double CARD_WIDTH = 210;
    private static final double CARD_HEIGHT = 430;
    private static final double CARD_GAP = 20;
    private static final double CARD_Y = 140;
    private static final double START_X =
            (GameConfig.WIDTH - (CARD_WIDTH * 5 + CARD_GAP * 4)) / 2.0;

    private static final double BUTTON_W = 150;
    private static final double BUTTON_H = 46;
    private static final double BACK_X = 40;
    private static final double BACK_Y = 646;
    private static final double CONFIRM_X = GameConfig.WIDTH - BACK_X - BUTTON_W;

    private static final Font NAME_FONT = Font.font("Segoe UI", FontWeight.BOLD, 20);
    private static final Font WEAPON_FONT = Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13);
    private static final Font DESC_FONT = Font.font("Segoe UI", FontWeight.NORMAL, 12);
    private static final Font STAT_FONT = Font.font("Segoe UI", FontWeight.NORMAL, 11);

    private final GameEventBus bus;
    private final GameSession session;
    private final StackPane root;
    private final Canvas canvas;
    private final List<List<String>> descriptionLines = new ArrayList<>();

    private int selected;
    private boolean backHovered;
    private boolean confirmHovered;

    public HeroSelectScreen(GameEventBus bus, GameSession session) {
        this.bus = bus;
        this.session = session;
        this.canvas = new Canvas(GameConfig.WIDTH, GameConfig.HEIGHT);
        this.root = new StackPane(canvas);
        this.root.setStyle("-fx-background-color: #0b1020;");
        for (HeroData hero : HeroData.ALL) {
            descriptionLines.add(wrap(hero.description(), DESC_FONT, CARD_WIDTH - 28));
        }
    }

    @Override
    public StackPane getRoot() {
        return root;
    }

    @Override
    public String getName() {
        return "HeroSelect";
    }

    @Override
    public void onShow() {
        selected = 0;
        if (session.hasSelectedHero()) {
            int index = HeroData.ALL.indexOf(session.getSelectedHero());
            if (index >= 0) {
                selected = index;
            }
        }
        backHovered = false;
        confirmHovered = false;
        bus.fire(new SoundRequestEvent("menu", true));
        render();
    }

    @Override
    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0c1630")),
                new Stop(1, Color.web("#170a10"))));
        gc.fillRect(0, 0, GameConfig.WIDTH, GameConfig.HEIGHT);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web("#f4c542"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 40));
        gc.fillText("Choose Your Hero", GameConfig.WIDTH / 2.0, 76);

        gc.setFill(Color.web("#9aa4c0"));
        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
        gc.fillText("Arrow keys or mouse to browse. Enter or click a card to fight.",
                GameConfig.WIDTH / 2.0, 106);

        for (int i = 0; i < HeroData.ALL.size(); i++) {
            drawCard(gc, i, START_X + i * (CARD_WIDTH + CARD_GAP));
        }

        drawBottomButton(gc, BACK_X, BACK_Y, "Back", backHovered);
        drawBottomButton(gc, CONFIRM_X, BACK_Y, "Select", confirmHovered);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web("#6d7796"));
        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        gc.fillText("Esc = Back   |   Every hero is portrayed with respect.",
                GameConfig.WIDTH / 2.0, 712);
    }

    private void drawCard(GraphicsContext gc, int index, double x) {
        HeroData hero = HeroData.ALL.get(index);
        Color primary = Color.web(hero.primaryColor());
        Color accent = Color.web(hero.accentColor());
        boolean isSelected = index == selected;

        if (isSelected) {
            gc.setFill(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0.20));
            gc.fillRoundRect(x - 7, CARD_Y - 7, CARD_WIDTH + 14, CARD_HEIGHT + 14, 24, 24);
        }

        gc.setFill(Color.web("#141c33"));
        gc.fillRoundRect(x, CARD_Y, CARD_WIDTH, CARD_HEIGHT, 16, 16);
        gc.setStroke(isSelected ? accent : Color.web("#2a3350"));
        gc.setLineWidth(isSelected ? 3 : 1.5);
        gc.strokeRoundRect(x, CARD_Y, CARD_WIDTH, CARD_HEIGHT, 16, 16);

        gc.setFill(primary);
        gc.fillRect(x + 2, CARD_Y + 2, CARD_WIDTH - 4, 8);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web("#f4f6ff"));
        gc.setFont(NAME_FONT);
        gc.fillText(hero.name(), x + CARD_WIDTH / 2.0, CARD_Y + 44);

        gc.setFill(accent);
        gc.setFont(WEAPON_FONT);
        gc.fillText(hero.weapon(), x + CARD_WIDTH / 2.0, CARD_Y + 68);

        gc.setStroke(Color.web("#2a3350"));
        gc.setLineWidth(1);
        gc.strokeLine(x + 14, CARD_Y + 82, x + CARD_WIDTH - 14, CARD_Y + 82);

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFill(Color.web("#b9c2da"));
        gc.setFont(DESC_FONT);
        List<String> lines = descriptionLines.get(index);
        for (int line = 0; line < lines.size(); line++) {
            gc.fillText(lines.get(line), x + 14, CARD_Y + 104 + line * 16);
        }

        drawStat(gc, x + 14, CARD_Y + 220, "SPD", hero.speed(), accent);
        drawStat(gc, x + 14, CARD_Y + 262, "DMG", hero.damage(), accent);
        drawStat(gc, x + 14, CARD_Y + 304, "RNG", hero.range(), accent);
        drawStat(gc, x + 14, CARD_Y + 346, "DEF", hero.defense(), accent);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web("#7f8aa8"));
        gc.setFont(STAT_FONT);
        gc.fillText("Mana regen " + (int) hero.manaRegen(), x + CARD_WIDTH / 2.0, CARD_Y + 404);
    }

    private void drawStat(GraphicsContext gc, double x, double y,
                          String label, double value, Color accent) {
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFill(Color.web("#8f9ab8"));
        gc.setFont(STAT_FONT);
        gc.fillText(label, x, y + 9);

        double barX = x + 54;
        double barWidth = CARD_WIDTH - 28 - 54;
        gc.setFill(Color.web("#242d49"));
        gc.fillRoundRect(barX, y, barWidth, 8, 6, 6);
        gc.setFill(accent);
        gc.fillRoundRect(barX, y, Math.max(4, barWidth * (value / 100.0)), 8, 6, 6);
    }

    private void drawBottomButton(GraphicsContext gc, double x, double y,
                                  String label, boolean hovered) {
        gc.setFill(hovered ? Color.web("#f4c542") : Color.web("#1c2745"));
        gc.fillRoundRect(x, y, BUTTON_W, BUTTON_H, 14, 14);
        gc.setStroke(hovered ? Color.web("#ffe89a") : Color.web("#34405f"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, BUTTON_W, BUTTON_H, 14, 14);

        gc.setFill(hovered ? Color.web("#0b1020") : Color.web("#e8e8f0"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(label, x + BUTTON_W / 2.0, y + 30);
    }

    @Override
    public void onKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code == KeyCode.LEFT || code == KeyCode.A) {
            selected = Math.floorMod(selected - 1, HeroData.ALL.size());
        } else if (code == KeyCode.RIGHT || code == KeyCode.D) {
            selected = Math.floorMod(selected + 1, HeroData.ALL.size());
        } else if (code == KeyCode.ENTER || code == KeyCode.SPACE) {
            confirm();
        } else if (code == KeyCode.ESCAPE || code == KeyCode.BACK_SPACE) {
            back();
        }
    }

    @Override
    public void onMouseMoved(MouseEvent event) {
        int card = cardAt(event.getX(), event.getY());
        if (card >= 0) {
            selected = card;
        }
        backHovered = inside(event.getX(), event.getY(), BACK_X, BACK_Y, BUTTON_W, BUTTON_H);
        confirmHovered = inside(event.getX(), event.getY(), CONFIRM_X, BACK_Y, BUTTON_W, BUTTON_H);
    }

    @Override
    public void onMouseClicked(MouseEvent event) {
        int card = cardAt(event.getX(), event.getY());
        if (card >= 0) {
            selected = card;
            confirm();
            return;
        }
        if (inside(event.getX(), event.getY(), BACK_X, BACK_Y, BUTTON_W, BUTTON_H)) {
            back();
            return;
        }
        if (inside(event.getX(), event.getY(), CONFIRM_X, BACK_Y, BUTTON_W, BUTTON_H)) {
            confirm();
        }
    }

    private int cardAt(double x, double y) {
        if (y < CARD_Y || y > CARD_Y + CARD_HEIGHT) {
            return -1;
        }
        for (int i = 0; i < HeroData.ALL.size(); i++) {
            double cardX = START_X + i * (CARD_WIDTH + CARD_GAP);
            if (x >= cardX && x <= cardX + CARD_WIDTH) {
                return i;
            }
        }
        return -1;
    }

    private boolean inside(double x, double y, double rx, double ry, double rw, double rh) {
        return x >= rx && x <= rx + rw && y >= ry && y <= ry + rh;
    }

    private void confirm() {
        HeroData hero = HeroData.ALL.get(selected);
        session.startNewGame(hero);
        bus.fire(new SoundRequestEvent("ui_click", false));
        bus.fire(new ScreenChangeEvent(getName(), "Fight", true));
    }

    private void back() {
        bus.fire(new SoundRequestEvent("ui_click", false));
        bus.fire(new ScreenChangeEvent(getName(), "MainMenu", true));
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
}
