package io.gitub.ticklemonster.robots;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.ObjectMap;


public class RobotsGame extends Game {
	public enum Screens {
		MENU, SETTINGS, GAME, EXIT
	}

	static final String PLAYER_SPRITE_NAME = "human";
	static final String ROBOT_SPRITE_NAME = "robot";
	static final String SPLAT_SPRITE_NAME = "splat_tile";
	static final String TOUCH_SPRITE_NAME = "selector_red";

	MainMenuScreen mainMenuScreen;
	SettingsScreen settingsScreen;
	GameScreen gameScreen;

	Preferences prefs;
	SpriteBatch batch;
	BitmapFont font;
	TextureAtlas atlas;
	ObjectMap<String,Sprite> sprites;
	ObjectMap<String,TextureRegion> textureRegions;

	public void create() {
		prefs = Gdx.app.getPreferences("RobotsGamePrefs");

		batch = new SpriteBatch();

		font = new BitmapFont(
			Gdx.files.internal("boxy_bold_font.fnt"),
			Gdx.files.internal("boxy_bold_font.png"),
			//Gdx.files.internal("PressStart2P.fnt"),
			//Gdx.files.internal("PressStart2P.png"),
			false
		);
		font.getData().markupEnabled = true;

		// load the sprites from the atlas
		atlas = new TextureAtlas("RobotsGame.atlas");
		textureRegions = new ObjectMap<String,TextureRegion>();
		for (AtlasRegion region : atlas.getRegions() ) {
			textureRegions.put(region.name, region);
		}

		// Start with the Main Menu...
		this.setScreen( RobotsGame.Screens.MENU );
	}

	@SuppressWarnings("EmptyMethod")
	public void render() {
		super.render(); //important!
	}

	public void setScreen(Screens screen){
		switch(screen){
			case MENU:
				if(mainMenuScreen == null) mainMenuScreen = new MainMenuScreen(this);
        super.setScreen(mainMenuScreen);
				break;
			case SETTINGS:
				if(settingsScreen == null) settingsScreen = new SettingsScreen(this);
				this.setScreen(settingsScreen);
				break;
			case GAME:
				if(gameScreen == null) gameScreen = new GameScreen(this);
				super.setScreen(gameScreen);
				break;
			case EXIT:
				Gdx.app.exit();
				break;
		}

	}

	public void dispose() {
		Gdx.app.log("RobotsGame","dispose - cleaning up...");

		prefs.flush();
		if( mainMenuScreen != null ) mainMenuScreen.dispose();
		if( settingsScreen != null ) settingsScreen.dispose();
		if( gameScreen != null ) gameScreen.dispose();
		atlas.dispose();
		batch.dispose();
		font.dispose();
	}

}
