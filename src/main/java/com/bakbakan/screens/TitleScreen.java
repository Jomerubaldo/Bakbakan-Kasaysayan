package com.bakbakan.screens;

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

public class TitleScreen implements Screen {

    private final StackPane root;
    private final Canvas canvas;
    private double elapsed;

    public TitleScreen() {
        canvas = new Canvas(GameConfig.WIDTH, GameConfig.HEIGHT);
        root = new StackPane(canvas);
        root.setStyle("-fx-background-color: #0b1020;");
        render();
    }

    @Override
    public StackPane getRoot() {
        return root;
    }

    @Override
    public String getName() {
        return "Title";
    }

    @Override
    public void update(double dt) {
        elapsed += dt;
    }

    @Override
    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#101a3a")),
                new Stop(1, Color.web("#1d0b12"))));
        gc.fillRect(0, 0, GameConfig.WIDTH, GameConfig.HEIGHT);

        gc.setFill(Color.web("#f4c542", 0.12));
        gc.fillOval(GameConfig.WIDTH / 2.0 - 260, 110, 520, 520);

        gc.setTextAlign(TextAlignment.CENTER);

        gc.setFill(Color.web("#f4c542"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 72));
        gc.fillText("Bakbakan-Kasaysayan", GameConfig.WIDTH / 2.0, 300);

        gc.setFill(Color.web("#e8e8f0"));
        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 26));
        gc.fillText("Philippine Heroes Fighting Game", GameConfig.WIDTH / 2.0, 360);

        double pulse = 0.55 + 0.45 * Math.sin(elapsed * 3.0);
        gc.setFill(Color.web("#ffffff", pulse));
        gc.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 20));
        gc.fillText("Phase 1 placeholder screen", GameConfig.WIDTH / 2.0, 470);

        gc.setFill(Color.web("#9aa4c0"));
        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        gc.fillText("Main menu, hero select and battles arrive in later phases.",
                GameConfig.WIDTH / 2.0, 505);
    }
}
