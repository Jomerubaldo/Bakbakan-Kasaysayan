package com.bakbakan.screens;

import com.bakbakan.events.GameEventBus;
import com.bakbakan.events.ScreenChangeEvent;
import com.bakbakan.util.GameConfig;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ScreenManager {

    private final Stage stage;
    private final GameEventBus bus;
    private final StackPane root;
    private final Scene scene;
    private final Set<KeyCode> pressedKeys = EnumSet.noneOf(KeyCode.class);
    private final Map<String, Supplier<Screen>> factories = new LinkedHashMap<>();
    private final EventHandler<ScreenChangeEvent> screenChangeHandler;

    private Screen current;
    private AnimationTimer loop;
    private long lastFrameNanos = -1L;
    private long inputLockUntilNanos;
    private boolean switching;

    public ScreenManager(Stage stage, GameEventBus bus) {
        this.stage = stage;
        this.bus = bus;
        this.root = new StackPane();
        this.root.setFocusTraversable(true);
        this.scene = new Scene(root, GameConfig.WIDTH, GameConfig.HEIGHT);

        this.scene.setOnKeyPressed(event -> {
            pressedKeys.add(event.getCode());
            if (System.nanoTime() >= inputLockUntilNanos && current != null) {
                current.onKeyPressed(event);
            }
        });
        this.scene.setOnKeyReleased(event -> {
            pressedKeys.remove(event.getCode());
            if (current != null) {
                current.onKeyReleased(event);
            }
        });
        this.scene.setOnMousePressed(event -> root.requestFocus());
        this.scene.setOnMouseMoved(event -> {
            if (current != null) {
                current.onMouseMoved(event);
            }
        });
        this.scene.setOnMouseClicked(event -> {
            if (current != null) {
                current.onMouseClicked(event);
            }
        });

        this.stage.setTitle(GameConfig.TITLE);
        this.stage.setScene(scene);
        this.stage.setResizable(false);
        this.stage.setOnCloseRequest(event -> Platform.exit());
        this.stage.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (!isFocused) {
                pressedKeys.clear();
                if (current != null) {
                    current.onFocusLost();
                }
            }
        });

        this.screenChangeHandler = this::handleScreenChangeRequest;
        this.bus.register(ScreenChangeEvent.SCREEN_CHANGE, screenChangeHandler);
    }

    public void registerScreen(String name, Supplier<Screen> factory) {
        if (name == null || factory == null) {
            return;
        }
        factories.put(name, factory);
    }

    public void showInitial(String name) {
        Supplier<Screen> factory = factories.get(name);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown initial screen: " + name);
        }
        switchTo(factory.get(), "none");
        startLoop();
    }

    private void handleScreenChangeRequest(ScreenChangeEvent event) {
        if (switching || !event.isRequest()) {
            return;
        }
        String target = event.getToScreen();
        if (target == null || (current != null && target.equals(current.getName()))) {
            return;
        }
        Supplier<Screen> factory = factories.get(target);
        if (factory == null) {
            System.err.println("No screen registered for: " + target);
            return;
        }
        switchTo(factory.get(), current == null ? "none" : current.getName());
    }

    private void switchTo(Screen next, String fromName) {
        if (next == null) {
            return;
        }
        switching = true;
        try {
            if (current != null) {
                current.onHide();
            }
            current = next;
            root.getChildren().setAll(next.getRoot());
            pressedKeys.clear();
            inputLockUntilNanos = System.nanoTime() + 250_000_000L;

            bus.fire(new ScreenChangeEvent(fromName, next.getName()));

            next.onShow();

            Platform.runLater(() -> {
                root.requestFocus();
                if (stage.getScene() != null && stage.getScene().getRoot() != null) {
                    stage.getScene().getRoot().requestFocus();
                }
            });
        } finally {
            switching = false;
        }
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
        bus.unregister(ScreenChangeEvent.SCREEN_CHANGE, screenChangeHandler);
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
