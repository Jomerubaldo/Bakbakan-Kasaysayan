package com.bakbakan.screens;

import javafx.scene.Parent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public interface Screen {

    Parent getRoot();

    default String getName() {
        return getClass().getSimpleName();
    }

    default void onShow() {
    }

    default void onHide() {
    }

    default void update(double dt) {
    }

    default void render() {
    }

    default void onKeyPressed(KeyEvent event) {
    }

    default void onKeyReleased(KeyEvent event) {
    }

    default void onMouseMoved(MouseEvent event) {
    }

    default void onMouseClicked(MouseEvent event) {
    }
}
