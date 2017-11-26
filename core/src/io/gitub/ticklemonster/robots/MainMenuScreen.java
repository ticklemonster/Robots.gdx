package io.gitub.ticklemonster.robots;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.math.MathUtils;

public class MainMenuScreen implements Screen {
  final RobotsGame game;
	OrthographicCamera camera;

  long highscore;
  float counter;
  Sprite human;
  Sprite robot;
  int direction;
  boolean touched;

  public MainMenuScreen(final RobotsGame game) {
		this.game = game;

		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);

    human = new Sprite(game.textureRegions.get(game.PLAYER_SPRITE_NAME));
    robot = new Sprite(game.textureRegions.get(game.ROBOT_SPRITE_NAME));
    human.setPosition(-1f,400f);
    robot.setPosition(-1f,400f);
    direction = 1;
	}

  @Override
  public void render(float delta) {
    // get some necessary data...
    highscore = game.prefs.getLong("highscore",0);

    counter += delta;
    if( counter > 1.0f ) {
      // time for an animation...
      counter -= 1f;

      // if there is no human, create one?
      if( human.getX() < 0f && robot.getX() < 0f && MathUtils.random()<0.2 ) {
        // there is no human on screen.
        // 20% chance of making one...
        direction = 2*MathUtils.random(0,1) - 1;
        human.setPosition( (direction<0)?800f:0f, MathUtils.random(100f,300f) );
      }
      else if( robot.getX() < 0f && human.getX() > 32f && human.getX() < 768f && MathUtils.random()<0.2) {
        // there is a human, but no robot.
        // 20% chance of making a robot...
        direction = 2*MathUtils.random(0,1) - 1;
        robot.setPosition( (direction<0)?800f:0f, human.getY() );
      }
    }

    // clear the screen
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    // setup projection
		camera.update();
		game.batch.setProjectionMatrix(camera.combined);

    // draw menu screen
		game.batch.begin();
    game.font.getData().setScale(4.0f);
		game.font.draw(game.batch, "Welcome to [GOLD] Robots Game [] !!", 100, 300,
      600, Align.center, false);
    game.font.getData().setScale(3.0f);
    game.font.draw(game.batch, String.format("High score: [RED] %,d []",highscore),
      100,200,600,Align.center,false);
    game.font.getData().setScale(2.0f);
    game.font.draw(game.batch, "Touch to start",
      100,20,600,Align.center,false);

    // update and drawe current human and robot
    if( human.getX() >= 0f ) {
      human.translateX( direction * 128f * delta );
      human.draw(game.batch);
    }
    if( robot.getX() >= 0f ) {
      robot.translateX( direction * 128f * delta );
      robot.draw(game.batch);
    }

    // have the sprites fallen off the screen?
    // left edge (x<0) is covered, deal with right edge
    if( direction>0 && human.getX()>800 ) human.setX(-1f);
    if( direction>0 && robot.getX()>800 ) robot.setX(-1f);

		game.batch.end();

		if( Gdx.input.isTouched() ) {
     touched = true;
   } else if( touched == true ) {
     game.setScreen( RobotsGame.Screens.GAME );
		}
	}

  @Override
	public void resize(int width, int height) {
    highscore = game.prefs.getLong("highscore",0);
	}

  @Override
	public void show() {
    Gdx.app.log("MainMenuScreen","Showing menu screen...");
    highscore = game.prefs.getLong("highscore",0);
    counter = 100.0f;
    touched = false;
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
    highscore = game.prefs.getLong("highscore",0);
	}

  @Override
  public void dispose () {
    Gdx.app.log("MainMenuScreen","dispose");
  }

}
