package com.bitdecay.jump.leveleditor;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Launcher {

    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.resizable = false;
        config.width = 1600;
        config.height = 900;
        config.title = "Jump Level Editor";
        new LwjglApplication(new EditorApp(), config);
    }
}
