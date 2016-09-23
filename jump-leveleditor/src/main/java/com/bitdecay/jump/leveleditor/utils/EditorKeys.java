package com.bitdecay.jump.leveleditor.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * Created by Monday on 9/3/2016.
 */
public enum EditorKeys {
    // Help
    HELP("Show Hotkey Help", false, Input.Keys.F1),

    // World Controls
    PAUSE("Pause/Resume", false, Input.Keys.GRAVE),
    STEP_WORLD("Step World", false, Input.Keys.PLUS, Input.Keys.EQUALS),
    ROLL_WORLD("Roll World", false, Input.Keys.MINUS),

    // Camera controls
    TOGGLE_GAME("Toggle Game Camera", false, Input.Keys.BACKSLASH),
    PAN_LEFT("Camera Left", false, Input.Keys.LEFT),
    PAN_RIGHT("Camera Right", false, Input.Keys.RIGHT),
    PAN_UP("Camera Up", false, Input.Keys.UP),
    PAN_DOWN("Camera Down", false, Input.Keys.DOWN),
    ZOOM_IN("Camera Zoom In", false, Input.Keys.NUM_2, Input.Keys.NUMPAD_2),
    ZOOM_OUT("Camera Zoom Out", false, Input.Keys.NUM_1, Input.Keys.NUMPAD_1),

    // Tool Controls
    REFRESH("Refresh Level", true, Input.Keys.CONTROL_LEFT, Input.Keys.R),
    UNDO("Undo", true, Input.Keys.CONTROL_LEFT, Input.Keys.Z),
    REDO("Redo", true, Input.Keys.CONTROL_LEFT, Input.Keys.Y),
    DELETE_SELECTED("Deleted Selected Objects", false, Input.Keys.BACKSPACE, Input.Keys.FORWARD_DEL),
    DISABLE_SNAP("Disable Snap (Hold)", false, Input.Keys.CONTROL_LEFT),
    DROP_MULTI("Drop Multiple Objects", false, Input.Keys.SHIFT_LEFT);


    private final String name;
    private final int[] keys;
    private final boolean isKeyCombo;
    private final String keyHelp;

    EditorKeys(String name, boolean isKeyCombo, int... keyList) {
        this.name = name;
        this.isKeyCombo = isKeyCombo;
        this.keys = keyList;

        StringBuilder builder = new StringBuilder();

        for (int key : keys) {
            if (builder.length() > 0) {
                if (isKeyCombo) {
                    builder.append(" and ");
                } else {
                    builder.append(" or ");
                }
            }
            builder.append(Input.Keys.toString(key));
        }
        keyHelp = builder.toString();
    }

    public boolean isPressed() {
        if (isKeyCombo) {
            return allKeysPressed();
        } else {
            return atLeastOneKeyPressed();
        }

    }

    private boolean allKeysPressed() {
        for (int key : keys) {
            if (!Gdx.input.isKeyPressed(key)) {
                return false;
            }
        }
        return true;
    }

    private boolean atLeastOneKeyPressed() {
        for (int key : keys) {
            if (Gdx.input.isKeyPressed(key)) {
                if (isKeyAModifer(key) || !isAnyModiferPressed()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isJustPressed() {
        if (isKeyCombo) {
            return comboJustPressed();
        } else {
            return atLeastOneKeyJustPressed();
        }
    }

    private boolean comboJustPressed() {
        boolean atLeastOneJustPressed = false;
        for (int key : keys) {
            if (!Gdx.input.isKeyPressed(key)) {
                // some part of our combo isn't pressed.
                return false;
            } else if (Gdx.input.isKeyJustPressed(key)) {
                atLeastOneJustPressed = true;
            }
        }
        // combo is all pressed and at least one key was just pressed
        return atLeastOneJustPressed;
    }

    private boolean atLeastOneKeyJustPressed() {
        for (int key : keys) {
            if (Gdx.input.isKeyJustPressed(key)) {
                if (isKeyAModifer(key) || !isAnyModiferPressed()) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getHelp() {

        return name + ": " + keyHelp;
    }

    private static boolean isKeyAModifer(int key) {
        if (Input.Keys.SHIFT_LEFT == key) {
            return true;
        }
        if (Input.Keys.SHIFT_RIGHT == key) {
            return true;
        }
        if (Input.Keys.CONTROL_LEFT == key) {
            return true;
        }
        if (Input.Keys.CONTROL_RIGHT == key) {
            return true;
        }
        if (Input.Keys.ALT_LEFT == key) {
            return true;
        }
        if (Input.Keys.ALT_RIGHT == key) {
            return true;
        }
        return false;
    }

    private static boolean isAnyModiferPressed() {
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            return true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
            return true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            return true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
            return true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)) {
            return true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT)) {
            return true;
        }
        return false;
    }
}
