package com.bakbakan.screens;

import com.bakbakan.events.GameEventBus;
import com.bakbakan.events.ScreenChangeEvent;
import com.bakbakan.util.GameConfig;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class ScreenManager {

    private final Stage stage;
    private final GameEventBus bus;
    private final StackPane root;
    private final Scene scene;
    private final Set<KeyCode> pressedKeys = EnumSet.noneOf(KeyCode.class);

    private Screen current;
    private AnimationTimer loop;
    private long lastFrameNanos = -1L;

    public ScreenManager(Stage stage, GameEventBus bus) {
        this.stage = stage;
        this.bus = bus;
        this.root = new StackPane();
        this.root.setFocusTraversable(true);
        this.scene = new Scene(root, GameConfig.WIDTH, GameConfig.HEIGHT);

        this.scene.setOnKeyPressed(event -> pressedKeys.add(event.getCode()));
        this.scene.setOnKeyReleased(event -> pressedKeys.remove(event.getCode()));
        this.scene.setOnMousePressed(event -> root.requestFocus());

        this.stage.setTitle(GameConfig.TITLE);
        this.stage.setScene(scene);
        this.stage.setResizable(false);
        this.stage.setOnCloseRequest(event -> Platform.exit());
    }

    public void showInitial(Screen screen) {
        switchTo(screen, "none");
        startLoop();
    }

    public void switchTo(Screen next) {
        switchTo(next, current == null ? "none" : current.getName());
    }

    private void switchTo(Screen next, String fromName) {
        if (next == null) {
            return;
        }
        if (current != null) {
            current.onHide();
        }
        current = next;
        root.getChildren().setAll(next.getRoot());
        next.onShow();

        Platform.runLater(() -> {
            root.requestFocus();
            if (stage.getScene() != null && stage.getScene().getRoot() != null) {
                stage.getScene().getRoot().requestFocus();
            }
        });

        bus.fire(new ScreenChangeEvent(fromName, next.getName()));
    }

    private void startLoop() {
        if (loop != null) {
            return;
        }
        loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastFrameNanos < 0) {
                    lastFrameNanos = now;
                    return;
                }
                double dt = (now - lastFrameNanos) / 1_000_000_000.0;
                lastFrameNanos = now;
                if (dt > GameConfig.MAX_DELTA) {
                    dt = GameConfig.MAX_DELTA;
                }
                if (current != null) {
                    current.update(dt);
                    current.render();
                }
            }
        };
        loop.start();
    }

    public void stop() {
        if (loop != null) {
            loop.stop();
            loop = null;
        }
        if (current != null) {
            current.onHide();
        }
        pressedKeys.clear();
    }

    public Set<KeyCode> getPressedKeys() {
        return Collections.unmodifiableSet(pressedKeys);
    }

    public Scene getScene() {
        return scene;
    }

    public Stage getStage() {
        return stage;
    }

    public GameEventBus getBus() {
        return bus;
    }

    public Screen getCurrent() {
        return current;
    }
}
