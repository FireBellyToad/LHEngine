package com.faust.lhengine.lwjgl3;

import java.util.Arrays;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.faust.lhengine.LHEngine;
import com.faust.lhengine.saves.impl.DesktopSaveFileManager;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {

    final static int SCALE_FACTOR = 6;

    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired())
            return; // This handles macOS support and helps on Windows.
        createApplication(args);
    }

    private static Lwjgl3Application createApplication(String[] args) {
        return new Lwjgl3Application(new LHEngine(false, new DesktopSaveFileManager()), getDefaultConfiguration(args));
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration(String[] args) {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("LHEngine");

        configuration.useVsync(true);

        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);

        // //if parameter w is set, go windowed
        // if(Arrays.stream(args).anyMatch(stringarg -> "w".equals(stringarg) || "windowed".equals(stringarg))){
            configuration.setWindowedMode((int) (LHEngine.GAME_WIDTH * SCALE_FACTOR), (int) (LHEngine.GAME_HEIGHT * SCALE_FACTOR));
            configuration.setResizable(false);
        // } else{
        //     configuration.setFullscreenMode( Lwjgl3ApplicationConfiguration.getDisplayMode());
        // }

        configuration.setWindowIcon("icon.png");
        return configuration;
    }
}