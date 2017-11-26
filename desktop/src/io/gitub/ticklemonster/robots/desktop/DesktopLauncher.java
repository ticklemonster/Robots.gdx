package io.gitub.ticklemonster.robots.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import io.gitub.ticklemonster.robots.RobotsGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Robots";
    config.width = 800;
    config.height = 480;
		new LwjglApplication(new RobotsGame(), config);
	}
}
