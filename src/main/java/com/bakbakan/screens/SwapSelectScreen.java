package com.bakbakan.screens;

import com.bakbakan.assets.AssetManager;
import com.bakbakan.assets.SpriteProvider;
import com.bakbakan.entities.GameSession;
import com.bakbakan.entities.HeroData;
import com.bakbakan.events.GameEventBus;
import com.bakbakan.events.ScreenChangeEvent;
import com.bakbakan.events.SoundRequestEvent;
import com.bakbakan.ui.BackdropArt;
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
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;

public class SwapSelectScreen implements Screen {

    private static final int SWAP_SLOTS = 3;
    private static final double CARD_WIDTH = 200;
    private static final double CARD_HEIGHT = 390;
    private static final double CARD_GAP = 22;
    private static final double CARD_Y = 150;
    private static final double START_X =
            (GameConfig.WIDTH - (CARD_WIDTH * 5 + CARD_GAP * 4)) / 2.0;

    private static final double BUTTON_W = 180;
    private static final double BUTTON_H = 48;
    private static final double BACK_X = 60;
    private static final double START_BUTTON_X = GameConfig.WIDTH - 60 - BUTTON_W;
    private static final double BUTTON_Y = 638;

    private final GameEventBus bus;
    private final GameSession session;
    private final SpriteProvider sprites;
    private final StackPane root;
    private final Canvas canvas;
    private final List<Integer> chosen = new ArrayList<>();

    private int highlighted;
    private boolean backHovered;
    private boolean startHovered;
    private double elapsed;

    public SwapSelectScreen(GameEventBus bus, GameSession session, AssetManager assets) {
        this.bus = bus;
        this.session = session;
        this.sprites = new SpriteProvider(assets);
        this.canvas = new Canvas(GameConfig.WIDTH, GameConfig.HEIGHT);
        this.root = new StackPane(canvas);
        this.root.setStyle("-fx-background-color: #0b1020;");
    }

    @Override
    public StackPane getRoot() {
        return root;
    }

    @Override
    public String getName() {
        return "SwapSelect";
    }

    @Override
    public void onShow() {
        highlighted = 0;
        chosen.clear();
        backHovered = false;
        startHovered = false;
        elapsed = 0;
        bus.fire(new SoundRequestEvent("menu", true));
        render();
    }

    @Override
    public void update(double dt) {
        elapsed += dt;
    }

    @Override
    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawBackdrop(gc);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web("#f4c542"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 40));
        gc.fillText("Choose 3 Swap Heroes", GameConfig.WIDTH / 2.0, 68);

        gc.setFill(Color.web("#9aa4c0"));
        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
        gc.fillText("Pick exactly three heroes from the five. Only these can be swapped in battle.",
                GameConfig.WIDTH / 2.0, 98);

        gc.setFill(Color.web("#ffe89a"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        gc.fillText("SELECTED  " + chosen.size() + " / " + SWAP_SLOTS,
                GameConfig.WIDTH / 2.0, 128);

        for (int i = 0; i < HeroData.ALL.size(); i++) {
            drawCard(gc, i, START_X + i * (CARD_WIDTH + CARD_GAP));
        }

        drawBottomButton(gc, BACK_X, BUTTON_Y, "Back", backHovered);
        drawBottomButton(gc, START_BUTTON_X, BUTTON_Y, "Start Battle", startHovered,
                chosen.size() == SWAP_SLOTS);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web("#6d7796"));
        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        gc.fillText("Left/Right browse   |   Space pick   |   Enter start   |   Esc back",
                GameConfig.WIDTH / 2.0, 712);
    }

    private void drawBackdrop(GraphicsContext gc) {
        gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#061726")),
                new Stop(0.45, Color.web("#0b2b35")),
                new Stop(1, Color.web("#2a1810"))));
        gc.fillRect(0, 0, GameConfig.WIDTH, GameConfig.HEIGHT);

        gc.setStroke(Color.web("#f4c542", 0.045));
        gc.setLineWidth(2);
        for (double x = -220; x < GameConfig.WIDTH + 220; x += 46) {
            gc.strokeLine(x, 0, x + 250, GameConfig.HEIGHT);
            gc.strokeLine(x, 0, x - 250, GameConfig.HEIGHT);
        }

        double cx = GameConfig.WIDTH / 2.0;
        gc.save();
        gc.translate(cx, 150);
        gc.rotate(elapsed * 3);
        gc.translate(-cx, -150);
        BackdropArt.sunRays(gc, cx, 150, 340, Color.web("#f4c542", 0.07));
        gc.restore();

        gc.setFill(Color.web("#f4c542", 0.08));
        gc.fillOval(cx - 150, 0, 300, 300);
        gc.setFill(Color.web("#ffd97a", 0.12));
        gc.fillOval(cx - 90, 60, 180, 180);

        BackdropArt.star(gc, 260, 108, 15, Color.web("#f4c542", 0.55));
        BackdropArt.star(gc, 1020, 108, 15, Color.web("#f4c542", 0.55));

        gc.setFill(Color.web("#082029"));
        gc.fillPolygon(new double[]{0, 190, 380, 600, 820, 1040, 1280},
                new double[]{600, 470, 590, 440, 590, 460, 600}, 7);
        gc.setFill(Color.web("#0a2630"));
        gc.fillPolygon(new double[]{0, 250, 470, 720, 980, 1280},
                new double[]{640, 540, 630, 510, 620, 640}, 6);
        gc.setFill(Color.web("#101a20"));
        gc.fillRect(0, 620, GameConfig.WIDTH, GameConfig.HEIGHT - 620);
    }

    private void drawCard(GraphicsContext gc, int index, double x) {
        HeroData hero = HeroData.ALL.get(index);
        Color primary = Color.web(hero.primaryColor());
        Color accent = Color.web(hero.accentColor());
        boolean isHighlighted = index == highlighted;
        int pickOrder = chosen.indexOf(index);

        if (isHighlighted) {
            gc.setFill(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0.20));
            gc.fillRoundRect(x - 7, CARD_Y - 7, CARD_WIDTH + 14, CARD_HEIGHT + 14, 24, 24);
        }

        gc.setFill(Color.web("#0f1b2a"));
        gc.fillRoundRect(x, CARD_Y, CARD_WIDTH, CARD_HEIGHT, 16, 16);
        gc.setStroke(pickOrder >= 0 ? Color.web("#4cd97b")
                : isHighlighted ? accent : Color.web("#2a3350"));
        gc.setLineWidth(pickOrder >= 0 || isHighlighted ? 3 : 1.5);
        gc.strokeRoundRect(x, CARD_Y, CARD_WIDTH, CARD_HEIGHT, 16, 16);

        gc.setFill(primary);
        gc.fillRect(x + 2, CARD_Y + 2, CARD_WIDTH - 4, 8);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web("#f4f6ff"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        gc.fillText(hero.name(), x + CARD_WIDTH / 2.0, CARD_Y + 42);

        gc.setFill(accent);
        gc.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        gc.fillText(hero.weapon(), x + CARD_WIDTH / 2.0, CARD_Y + 62);

        double frameX = x + 14;
        double frameY = CARD_Y + 76;
        double frameW = CARD_WIDTH - 28;
        double frameH = 190;
        gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, primary.interpolate(Color.web("#0a1018"), 0.45)),
                new Stop(1, Color.web("#070c14"))));
        gc.fillRoundRect(frameX, frameY, frameW, frameH, 12, 12);

        gc.save();
        gc.beginPath();
        gc.rect(frameX, frameY, frameW, frameH);
        gc.clip();
        gc.setFill(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0.20));
        gc.fillOval(frameX + frameW / 2.0 - 40, frameY + 16, 80, 80);
        gc.setFill(Color.web("#000000", 0.30));
        gc.fillOval(frameX + frameW / 2.0 - 44, frameY + frameH - 24, 88, 16);
        sprites.renderPortrait(gc, hero, frameX + frameW / 2.0,
                frameY + frameH - 10, 0.68, true);
        gc.restore();

        gc.setStroke(pickOrder >= 0 ? Color.web("#4cd97b") : Color.web("#2a3350"));
        gc.setLineWidth(pickOrder >= 0 ? 2.5 : 1.5);
        gc.strokeRoundRect(frameX, frameY, frameW, frameH, 12, 12);

        gc.setFill(Color.web("#b9c2da"));
        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        gc.fillText("SPD " + (int) hero.speed() + "   DMG " + (int) hero.damage(),
                x + CARD_WIDTH / 2.0, CARD_Y + 288);
        gc.fillText("RNG " + (int) hero.range() + "   DEF " + (int) hero.defense(),
                x + CARD_WIDTH / 2.0, CARD_Y + 304);

        double badgeY = CARD_Y + 322;
        if (pickOrder >= 0) {
            gc.setFill(Color.web("#4cd97b"));
            gc.fillRoundRect(x + 30, badgeY, CARD_WIDTH - 60, 34, 12, 12);
            gc.setFill(Color.web("#05140c"));
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
            gc.fillText("SWAP " + (pickOrder + 1), x + CARD_WIDTH / 2.0, badgeY + 23);
        } else {
            gc.setFill(Color.web("#1c2745"));
            gc.fillRoundRect(x + 30, badgeY, CARD_WIDTH - 60, 34, 12, 12);
            gc.setFill(Color.web("#9aa4c0"));
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            gc.fillText("PICK", x + CARD_WIDTH / 2.0, badgeY + 23);
        }
    }

    private void drawBottomButton(GraphicsContext gc, double x, double y,
                                  String label, boolean hovered) {
        drawBottomButton(gc, x, y, label, hovered, true);
    }

    private void drawBottomButton(GraphicsContext gc, double x, double y, String label,
                                  boolean hovered, boolean enabled) {
        boolean active = hovered && enabled;
        gc.setFill(!enabled ? Color.web("#161d31")
                : active ? Color.web("#f4c542") : Color.web("#1c2745"));
        gc.fillRoundRect(x, y, BUTTON_W, BUTTON_H, 14, 14);
        gc.setStroke(!enabled ? Color.web("#2a3350")
                : active ? Color.web("#ffe89a") : Color.web("#34405f"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, BUTTON_W, BUTTON_H, 14, 14);

        gc.setFill(!enabled ? Color.web("#5c6478")
                : active ? Color.web("#0b1020") : Color.web("#e8e8f0"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(label, x + BUTTON_W / 2.0, y + 31);
    }

    @Override
    public void onKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code == KeyCode.LEFT || code == KeyCode.A) {
            highlighted = Math.floorMod(highlighted - 1, HeroData.ALL.size());
        } else if (code == KeyCode.RIGHT || code == KeyCode.D) {
            highlighted = Math.floorMod(highlighted + 1, HeroData.ALL.size());
        } else if (code == KeyCode.SPACE) {
            toggle(highlighted);
        } else if (code == KeyCode.ENTER) {
            startBattle();
        } else if (code == KeyCode.ESCAPE || code == KeyCode.BACK_SPACE) {
            back();
        }
    }

    @Override
    public void onMouseMoved(MouseEvent event) {
        int card = cardAt(event.getX(), event.getY());
        if (card >= 0) {
            highlighted = card;
        }
        backHovered = inside(event.getX(), event.getY(), BACK_X, BUTTON_Y);
        startHovered = inside(event.getX(), event.getY(), START_BUTTON_X, BUTTON_Y);
    }

    @Override
    public void onMouseClicked(MouseEvent event) {
        int card = cardAt(event.getX(), event.getY());
        if (card >= 0) {
            highlighted = card;
            toggle(card);
            return;
        }
        if (inside(event.getX(), event.getY(), BACK_X, BUTTON_Y)) {
            back();
            return;
        }
        if (inside(event.getX(), event.getY(), START_BUTTON_X, BUTTON_Y)) {
            startBattle();
        }
    }

    private void toggle(int index) {
        if (chosen.contains(index)) {
            chosen.remove(Integer.valueOf(index));
            bus.fire(new SoundRequestEvent("ui_click", false));
            return;
        }
        if (chosen.size() >= SWAP_SLOTS) {
            return;
        }
        chosen.add(index);
        bus.fire(new SoundRequestEvent("ui_click", false));
    }

    private void startBattle() {
        if (chosen.size() != SWAP_SLOTS) {
            return;
        }
        List<HeroData> heroes = new ArrayList<>();
        for (int index : chosen) {
            heroes.add(HeroData.ALL.get(index));
        }
        session.setSwapHeroes(heroes);
        if (!session.hasSelectedHero()) {
            session.startNewGame(heroes.get(0));
            session.setSwapHeroes(heroes);
        } else {
            session.setActiveHero(session.getSelectedHero());
        }
        bus.fire(new SoundRequestEvent("ui_click", false));
        bus.fire(new ScreenChangeEvent(getName(), "Fight", true));
    }

    private void back() {
        bus.fire(new SoundRequestEvent("ui_click", false));
        bus.fire(new ScreenChangeEvent(getName(), "HeroSelect", true));
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

    private boolean inside(double x, double y, double rx, double ry) {
        return x >= rx && x <= rx + BUTTON_W && y >= ry && y <= ry + BUTTON_H;
    }
}
