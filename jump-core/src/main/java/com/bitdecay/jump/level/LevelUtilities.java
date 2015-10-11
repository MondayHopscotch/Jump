package com.bitdecay.jump.level;

import java.io.File;

public class LevelUtilities {
	public static Level loadLevel() {
		return FileUtils.loadFileAs(Level.class);
	}

	public static Level loadLevel(String fileName) {
		return FileUtils.loadFileAs(Level.class, new File(fileName));
	}

	public static Level loadLevel(File file) {

		return null;
	}

	public static Level saveLevel(LevelBuilder builder) {
		Level level = builder.tilizeLevel();
		String savedContent = FileUtils.saveToFile(level);
		if (savedContent != null) {
//			return levelFromJson(savedContent);
			return null;
		} else {
			return null;
		}
	}

//	private static Level levelFromJson(String json) {
//		return new GsonBuilder().create().fromJson(json, Level.class);
//	}
}
