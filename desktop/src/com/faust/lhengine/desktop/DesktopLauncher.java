package com.faust.lhengine.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.faust.lhengine.LHEngine;
import com.faust.lhengine.saves.impl.DesktopSaveFileManager;

public class DesktopLauncher {
	private static final int SCALE_FACTOR = 6;

	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "LHEngine";
		config.resizable = false;
		config.width = (int) (LHEngine.GAME_WIDTH * SCALE_FACTOR);
		config.height = (int) (LHEngine.GAME_HEIGHT * SCALE_FACTOR);
		//if parameter w is set, go windowed
		config.fullscreen = true;//!Arrays.stream(arg).anyMatch(stringarg -> "w".equals(stringarg) || "windowed".equals(stringarg));
		config.addIcon("icon.png", Files.FileType.Internal);
		new LwjglApplication(new LHEngine(false, new DesktopSaveFileManager()), config);
	}
}
