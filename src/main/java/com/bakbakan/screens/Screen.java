package com.bakbakan.screens;

import javafx.scene.Parent;

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
}
