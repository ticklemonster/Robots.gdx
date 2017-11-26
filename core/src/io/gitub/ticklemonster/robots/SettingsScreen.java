package io.gitub.ticklemonster.robots;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;


import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class SettingsScreen implements Screen {
  final RobotsGame game;

  Stage stage;

  public SettingsScreen(final RobotsGame game) {
		this.game = game;
    Skin skin = new Skin(Gdx.files.internal("skins/vhs-ui.json"));

    stage = new Stage(new ScreenViewport());

    // Create a table that fills the screen. Everything else will go inside this table.
    Table table = new Table();
    table.setFillParent(true);
    table.pad(10);
    //table.setBackground(skin.getTiledDrawable("tile"));
    //table.setDebug(true);
    stage.addActor(table);

    table.add( new Label("Settings",skin,"title") ).colspan(2).expandX().spaceBottom(20);
    table.row();

    Table userSettings = new Table(skin);
      userSettings.add( new Label("Sounds",skin) ).expandX().right();
      userSettings.add( new CheckBox("soundcb",skin)).expandX().left();
      userSettings.row().pad(10,0,10,0);
      userSettings.add( new Label("Animations",skin)).expandX().right();
      CheckBox animCheckbox =  new CheckBox("anims",skin);
      animCheckbox.setChecked(true);
      userSettings.add(animCheckbox).expandX().left();
    table.add(userSettings);
    table.row().pad(10, 0, 10, 0);

    final TextField numberField = new TextField("1234",skin);
    Slider sliderField = new Slider(2f,12f,1f,false,skin);
    sliderField.addListener( new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        float snapValue = ((Slider)actor).getValue();
        numberField.setText( String.format("%2.0f",snapValue) );
      }
    });
    TextButton button3 = new TextButton("Done",skin);
    button3.addListener(new ChangeListener() {
    	@Override
    	public void changed(ChangeEvent event, Actor actor) {
    		game.setScreen(RobotsGame.Screens.MENU);
    	}
    });

    table.add(new Label("TextFieldLabel", skin)).expandX();
    table.add(numberField).expandX();
    table.row();

    table.add(new Label("SliderField", skin)).expandX();
    table.add(sliderField).expandX();
    table.row();


    table.add(button3).fillX();

	}

  @Override
  public void render(float delta) {
    Gdx.gl.glClearColor(0, 0, 0.2f, 1);
  	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
  	stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
  	stage.draw();
	}

  @Override
	public void resize(int width, int height) {
    stage.getViewport().update(width, height, true);
	}

  @Override
	public void show() {
    Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

  @Override
  public void dispose () {
    stage.dispose();
  }

}
