package com.mygdx.game.desktop;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.Master;

public class DesktopLauncher 
{
	public static void main (String[] arg) 
	{
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.foregroundFPS = 60;
		config.backgroundFPS = 60;
		config.title = "Ashbound";
		config.width = 1920;
		config.height = 1080;
		config.resizable = false;
		config.addIcon("App Icons\\Celtsoup32x32.png", FileType.Internal);
		config.addIcon("App Icons\\Celtsoup16x16.png", FileType.Internal);
		
		new LwjglApplication(new Master(), config);
	}
}
