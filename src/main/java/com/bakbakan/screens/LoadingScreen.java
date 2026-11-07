package com.bakbakan.screens;

import com.bakbakan.assets.AssetManager;
import com.bakbakan.events.GameEventBus;
import com.bakbakan.events.ScreenChangeEvent;
import com.bakbakan.events.SoundRequestEvent;
import com.bakbakan.util.GameConfig;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class LoadingScreen implements Screen {

    private static final double MIN_DISPLAY_SECONDS = 1.4;
    private static final int ASSETS_PER_FRAME = 4;

    private final AssetManager assets;
    private final GameEventBus bus;
    private final StackPane root;
    private final Canvas canvas;

    private double elapsed;
    private boolean navigated;

    public LoadingScreen(AssetManager assets, GameEventBus bus) {
        this.assets = assets;
        this.bus = bus;
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
        return "Loading";
    }

    @Override
    public void onShow() {
        assets.beginLoading();
        elapsed = 0;
        navigated = false;
        bus.fire(new SoundRequestEvent("loading", true));
        render();
    }

    @Override
    public void update(double dt) {
        elapsed += dt;

        for (int i = 0; i < ASSETS_PER_FRAME && assets.loadNext(); i++) {
            // Assets are loaded incrementally so the progress bar can animate.
        }

        if (!navigated && assets.getProgress() >= 1.0 && elapsed >= MIN_DISPLAY_SECONDS) {
            navigated = true;
            bus.fire(new ScreenChangeEvent(getName(), "MainMenu", true));
        }
    }

    @Override
    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0c1630")),
                new Stop(1, Color.web("#1d0b12"))));
        gc.fillRect(0, 0, GameConfig.WIDTH, GameConfig.HEIGHT);

        gc.setFill(Color.web("#f4c542", 0.10));
        gc.fillOval(GameConfig.WIDTH / 2.0 - 240, 70, 480, 480);

        gc.setTextAlign(TextAlignment.CENTER);

        gc.setFill(Color.web("#f4c542"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 54));
        gc.fillText("Bakbakan-Kasaysayan", GameConfig.WIDTH / 2.0, 270);

        gc.setFill(Color.web("#e8e8f0"));
        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 22));
        gc.fillText("Loading the battle" + dots(), GameConfig.WIDTH / 2.0, 320);

        double barX = 340;
        double barY = 380;
        double barWidth = 600;
        double barHeight = 30;
        double progress = assets.getProgress();

        gc.setFill(Color.web("#232c4a"));
        gc.fillRoundRect(barX, barY, barWidth, barHeight, 14, 14);

        gc.setFill(new LinearGradient(barX, 0, barX + barWidth, 0, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#f4c542")),
                new Stop(1, Color.web("#e67e22"))));
        if (progress > 0) {
            gc.fillRoundRect(barX, barY, Math.max(barHeight, barWidth * progress), barHeight, 14, 14);
        }

        gc.setFill(Color.web("#0b1020"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        gc.fillText(Math.round(progress * 100) + "%", GameConfig.WIDTH / 2.0, barY + 21);

        gc.setFill(Color.web("#9aa4c0"));
        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        gc.fillText(assets.getLoadedCount() + " / " + assets.getTotalCount() + " assets",
                GameConfig.WIDTH / 2.0, barY + 62);

        gc.setFill(Color.web("#6d7796"));
        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        gc.fillText("Missing sprites and audio are replaced automatically. No downloads required.",
                GameConfig.WIDTH / 2.0, 620);
    }

    private String dots() {
        int count = ((int) (elapsed * 2)) % 4;
        return ".".repeat(count);
    }
}
