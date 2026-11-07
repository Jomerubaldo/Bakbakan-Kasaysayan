package com.bakbakan.combat;

import javafx.scene.input.KeyCode;

import java.util.EnumSet;
import java.util.Set;

public class InputHandler {

    private final Set<KeyCode> pressed = EnumSet.noneOf(KeyCode.class);
    private final Set<KeyCode> justPressed = EnumSet.noneOf(KeyCode.class);

    public void keyPressed(KeyCode code) {
        if (code != null && pressed.add(code)) {
            justPressed.add(code);
        }
    }

    public void keyReleased(KeyCode code) {
        if (code != null) {
            pressed.remove(code);
        }
    }

    public boolean isDown(KeyCode code) {
        return pressed.contains(code);
    }

    public boolean consumePress(KeyCode code) {
        return justPressed.remove(code);
    }

    public void endFrame() {
        justPressed.clear();
    }

    public void clear() {
        pressed.clear();
        justPressed.clear();
    }
}
