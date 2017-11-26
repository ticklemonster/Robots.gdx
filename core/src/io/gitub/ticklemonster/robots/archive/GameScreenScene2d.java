package io.gitub.ticklemonster.robots;

import java.util.Iterator;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Interpolation;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

//import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;
//import static com.badlogic.gdx.math.Interpolation.*;


import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Input.Keys;

// TODO: Add move timer (as "difficult" variant?)

// TODO: Add pushbutton controls for movements (instead of touch?)

// TODO: Start of round animations:
// - Splatter removal animation to highlight removed splatters

// TODO: End of round animations:
// - Splatter off-screen spawns more splats on-screen at end of level

// TODO: Add "sepcial case" animations
// - Moving the player over the "teleport" button gets a free teleport?
// - moving the player over the "Score" gets free points?

// TODO: Add "achievements" (will require recording player details?)
// - First kill "that was easy"
// - Kill 5 bots without moving "cool hand splatterer"
// - First time off screen "boundless enthusiasm"
// - First off-screen splatter "splatter unbounded"
// - Kill all bots without moving "motionless killer"

// TODO: Refactor input handling

// TODO: Improve robot collision effects. Add sounds?



public class GameScreenScene2d implements Screen {
	static final float LEVEL_UP_WAIT_TIME = 3.0f;
	static final float ANIMATION_TIME = 0.33f;
	static final float TILE_SIZE = 32f;
	static enum GameState {
		PLAYER_TURN, TELEPORT,
		ROBOT_TURN, ROBOT_CHECK,
		LEVEL_UP, GAME_OVER, DEATH,
	};

	// TODO: Move these "constants" to preferences
	// 	and add a preference panel
	final float MOVE_TIMER = 3.0f;
	final int SPLATTER_LIFE = 5;
	final int INITIAL_TELEPORTS = 3;
	final int INITIAL_ROBOTS = 6;

	// game state
	final RobotsGame game;
	GameState gameState;
	int score;
	int level;
	int teleports;

	// user input management
	boolean touched;
	boolean touchTeleport;
	TextButton teleportButton;
	Sprite touchIndicator;

	// visible components
	Stage stage;
	Array<Actor> splatters;
	Array<Actor> robots;
	ParticleEffectPool explodeEffectPool;
	Array<PooledEffect> effects;
	Actor player;


	float animCounter;
	Label levelLabel;
	Label scoreLabel;
	Label teleportLabel;

	public GameScreenScene2d(final RobotsGame game) {
		this.game = game;

		Gdx.app.setLogLevel( Gdx.app.LOG_DEBUG );
		Gdx.app.debug("GameScreenScene2d","Starting with DEBUG");

		stage = new Stage(new FitViewport(800,480));

		// add Background to stage
		for( int x=0; x<15; x++ ) {
			for( int y=0; y<15; y++ ) {
				String tilename = (x+y)%2==0?"grey_tile":"white_tile";
				Image tile = new Image(game.textureRegions.get(tilename));
				tile.setPosition(160+x*32,y*32);
				tile.setZIndex(0);
				stage.addActor(tile);
			}
		}

		// create the player actor
		player = new RobotActor(game.textureRegions.get(game.PLAYER_SPRITE_NAME));
		player.setZIndex(1);
		stage.addActor(player);

		// Set up arrays for the robots and splatters
		splatters = new Array<Actor>();
		robots = new Array<Actor>();

		// set up an effect pool for explosions
		effects = new Array<PooledEffect>();
		ParticleEffect robotExplode = new ParticleEffect();
		robotExplode.load(Gdx.files.internal("robot_destruction.p"), game.atlas);
		robotExplode.setEmittersCleanUpBlendFunction(false);
		explodeEffectPool = new ParticleEffectPool(robotExplode, 1, 2);

		// set up buttons and UI components
		game.font.getData().setScale(1f);
		Label.LabelStyle labelStyle = new Label.LabelStyle(game.font,Color.WHITE);
		Label.LabelStyle valueStyle = new Label.LabelStyle(game.font,Color.GOLD);

		levelLabel = new Label(String.valueOf(level),valueStyle);
		scoreLabel = new Label(String.valueOf(score),valueStyle);
		teleportLabel = new Label(String.valueOf(teleports),valueStyle);

		Table scoreboard = new Table();
		//scoreboard.debug();
		scoreboard.setPosition(0,0);
		scoreboard.setSize(150,480);
		scoreboard.top().padTop(20);
		scoreboard.defaults().spaceBottom(10);
		scoreboard.add( new Label("Level",labelStyle) );
		scoreboard.row();
		scoreboard.add( levelLabel );
		scoreboard.row();
		scoreboard.add( new Label("Score",labelStyle) );
		scoreboard.row();
		scoreboard.add( scoreLabel );
		scoreboard.row();
		scoreboard.add( new Label("Teleports",labelStyle) );
		scoreboard.row();
		scoreboard.add( teleportLabel );
		stage.addActor(scoreboard);

		Table buttonboard = new Table();
		//buttonboard.debug();
		buttonboard.setPosition(650,0);
		buttonboard.setSize(150,480);
		buttonboard.bottom().padBottom(20);
		//buttonboard.add( new Touchpad(20,new Touchpad.TouchpadStyle()) );

		TextButton.TextButtonStyle teleportButtonStyle = new TextButton.TextButtonStyle();
		teleportButtonStyle.font = game.font;
		teleportButtonStyle.fontColor = Color.WHITE;
		teleportButtonStyle.downFontColor = Color.RED;
		teleportButton = new TextButton("teleport",teleportButtonStyle);
		teleportButton.addListener( new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				Gdx.app.debug("TeleportButton","Teleport requested in " + gameState);
				if(gameState != GameState.PLAYER_TURN) return;
				if(teleports <= 0) return;

				gameState = GameState.TELEPORT;
			}
		});
		buttonboard.add( teleportButton );
		stage.addActor(buttonboard);


		// start the new level
		touchIndicator = game.sprites.get(game.TOUCH_SPRITE_NAME);
		level = 1;
		startLevel();
		touchIndicator.setPosition(player.getX(),player.getY());
		touchIndicator.setAlpha(0f);

		// start listening for input
		touched = false;
		InputMultiplexer multiplexer = new InputMultiplexer();
		multiplexer.addProcessor( stage );	// buttons first
		multiplexer.addProcessor( new InputAdapter () {
			@Override
			public boolean keyUp (int keycode) {
				if( gameState != GameState.PLAYER_TURN ) return true;	// ignore keys

				// TELEPORT key
				if( keycode == Keys.NUMPAD_0 || keycode == Keys.SPACE ) {
					gameState = GameState.TELEPORT;
					return true;
				}

				// NO-MOVE key
				if( keycode == Keys.NUMPAD_5 ) {
					gameState = GameState.ROBOT_TURN;
					return true;
				}

				float move_x = player.getX();
				float move_y = player.getY();

				switch(keycode) {
					case Keys.NUMPAD_7:
						if( move_x > 0 && move_y < 480-TILE_SIZE ) {
							move_x -= TILE_SIZE;
							move_y += TILE_SIZE;
						}
						break;
					case Keys.UP:
					case Keys.NUMPAD_8:
						if( move_y < 480-TILE_SIZE ) {
							move_y += TILE_SIZE;
						}
						break;
					case Keys.NUMPAD_9:
						if( move_x < 800-TILE_SIZE && move_y < 480-TILE_SIZE ) {
							move_x += TILE_SIZE;
							move_y += TILE_SIZE;
						}
						break;
					case Keys.LEFT:
					case Keys.NUMPAD_4:
						if( move_x > 0 ) {
							move_x -= TILE_SIZE;
						}
						break;
					case Keys.RIGHT:
					case Keys.NUMPAD_6:
						if( move_x < 800-TILE_SIZE ) {
							move_x += TILE_SIZE;
						}
						break;
					case Keys.NUMPAD_1:
						if( move_x > 0 && move_y > 0 ) {
							move_x -= TILE_SIZE;
							move_y -= TILE_SIZE;
						}
						break;
					case Keys.DOWN:
					case Keys.NUMPAD_2:
						if( move_y > 0 ) {
							move_y -= TILE_SIZE;
						}
						break;
					case Keys.NUMPAD_3:
						if( move_x < 800-TILE_SIZE && move_y > 0) {
							move_x += TILE_SIZE;
							move_y -= TILE_SIZE;
						}
						break;
				}

				if( move_x != player.getX() || move_y != player.getY() ) {
					// a move was made - start animating...
					MoveToAction act = Actions.moveTo(move_x, move_y, ANIMATION_TIME);
					act.setInterpolation(Interpolation.linear);
					player.addAction( act );
					gameState = GameState.ROBOT_TURN;
				}

				return true;
			}

			@Override
			public boolean touchDown (int x, int y, int pointer, int button) {
				if(gameState != GameState.PLAYER_TURN) return false;
				Gdx.app.debug("touchDown","@("+x+","+y+") with "+pointer+"");

				touched = true;
				updateTouchIndicator( x, y );
				return true; // return true to indicate the event was handled
			}

			@Override
			public boolean touchDragged (int x, int y, int pointer) {
				if(gameState != GameState.PLAYER_TURN) return false;
				if( !touched ) return false;

				updateTouchIndicator( x, y );
				return true;
			}

			@Override
			public boolean touchUp (int x, int y, int pointer, int button) {
				if(gameState != GameState.PLAYER_TURN) return false;
				if(!touched) return false;

				// TODO: allow cancelling a touch

				// Accept the touch
				MoveToAction act = Actions.moveTo(
					touchIndicator.getX(),touchIndicator.getY(), ANIMATION_TIME
				);
				act.setInterpolation(Interpolation.linear);
				player.addAction( act );
				touched = false;
				gameState = GameState.ROBOT_TURN;
				return true; // return true to indicate the event was handled
			}
		});
		Gdx.input.setInputProcessor(multiplexer);

		// cheat!
		gameState = GameState.LEVEL_UP;
	}

	private void startLevel() {
		// get a bonus teleport for starting a level
		this.teleports = java.lang.Math.max(this.teleports+1,INITIAL_TELEPORTS);

		// put the player in the start position (assume centre - always)
		player.setPosition( 384, 224 );

		// clear out any existing effects...
		for (int i = effects.size - 1; i >= 0; i--)
    	effects.get(i).free();
		effects.clear();

		// remove any splatters that are in the start position
		// Iterator<Actor> iter = splatters.iterator();
    // while(iter.hasNext()) {
    //  	Actor splat = iter.next();
		// 	if( splat.getX() == player.getX() && splat.getY() == player.getY() ) {
    //     iter.remove();
    //   }
		// 	//else if( splat.getLevel() + SPLATTER_LIFE < level ) {
		// 		// dispose of all splatters
		// 		//TODO: Add splatter disposal animation/sound?
		// 	//	iter.remove();
		// 	// }
		// }

		// spawn an appropriate number of robots
		robots.clear();
		int robotCount = INITIAL_ROBOTS + (level-1)*2;
		for( int r=0; r < robotCount; r++ ) {
			RobotActor robot = new RobotActor( game.textureRegions.get(game.ROBOT_SPRITE_NAME) );

			Vector2 robotpos = randomEmptyLocation();
		  robot.setPosition( robotpos.x, robotpos.y );
      robots.add(robot);
			stage.addActor(robot);
		}

		Gdx.app.log("GameScreenScene2d::startLevel",
			"starting level " + level	+ " with " + robots.size + " robots"
		);
		gameState = GameState.PLAYER_TURN;
	}

	private boolean overlapsAny(Vector2 pos, Array<Actor> items) {
		for( Actor item : items ) {
			if( item.getX() == pos.x && item.getY() == pos.y ) {
				return true;
			}
		}
		return false;
	}

	private boolean playerOverlaps(Array<Actor> items) {
		for( Actor item : items ) {
			if( item.getX() == player.getX() && item.getY() == player.getY() ) {
				return true;
			}
		}
		return false;
	}

	private Vector2 randomLocation() {
		return new Vector2(
			MathUtils.random(0,14)*TILE_SIZE + 160,
			MathUtils.random(0,14)*TILE_SIZE
		);
	}

	private Vector2 randomEmptyLocation() {
		Vector2 dest = new Vector2();
		boolean isValid = false;

		while( !isValid ) {
			// get a random location
			dest.set(randomLocation());
			isValid = ( overlapsAny(dest,robots) == false
				&& overlapsAny(dest,splatters) == false
				&& dest.epsilonEquals(player.getX(), player.getY(), 0f) == false
				);
		}

		return dest;
	}

	private Vector2 randomSafeTeleportLocation() {
		Vector2 dest = new Vector2();
		int attempts = 0;
		boolean isValid = false;

		while( !isValid && attempts++ < 255 ) {
			dest.set( randomLocation() );
			if( overlapsAny(dest,splatters) ) {
				isValid = false;
			} else {
				isValid = true;
				for( Actor robot : robots ) {
					if( dest.dst2(robot.getX(),robot.getY()) < 64*64 ) {
						isValid = false;
						break;
					}
				}
			}

		}

		if( attempts >= 255 ) {
			Gdx.app.debug("GameScreenScene2d:randomSafeTeleportLocation","Failed to find safe place in 255 tries");
			dest.set(player.getX(), player.getY());
		} else {
			Gdx.app.debug("GameScreenScene2d:randomSafeTeleportLocation","Found location after " + attempts + " attempts");
		}

		return dest;
	}

	// map a screen touch to the stage and show the next resulting move
	private void updateTouchIndicator( float x, float y ) {
		if( !touched ) {
			touchIndicator.setAlpha(0f);
			return;
		}

		touchIndicator.setAlpha(1f);
		touchIndicator.setPosition(player.getX(), player.getY());

		Vector2 touchPos = stage.screenToStageCoordinates( new Vector2(x,y) );
		Vector2 vect = new Vector2(
			touchPos.x-player.getX() - TILE_SIZE/2,
			touchPos.y-player.getY() - TILE_SIZE/2
		);

		// if we're less than a square away, count it as no move
		if( vect.len2() < TILE_SIZE*TILE_SIZE ) {
			return;
		}

		// otherwise, look at the directional angle
		//		 90
		//	180 + 0
		//	   270
		float angle = vect.angle();


		if( angle > 270+22.5 || angle <= 90-22.5 ) {
			touchIndicator.translateX(TILE_SIZE);
		}
		if( angle > 22.5 && angle < 180-22.5 ) {
			touchIndicator.translateY(TILE_SIZE);
		}
		if( angle > 90+22.5 && angle < 270-22.5 ) {
			touchIndicator.translateX(-TILE_SIZE);
		}
		if( angle > 180+22.5 && angle < 360-22.5 ) {
			touchIndicator.translateY(-TILE_SIZE);
		}


	}


	@Override
	public void render (float delta) {

		updateGame();

		//
		// Render the resulting state
		//
		Gdx.gl.glClearColor(0.37f, 0.37f, 0.37f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// update counters
		levelLabel.setText(String.valueOf(level));
		scoreLabel.setText(String.valueOf(score));
		teleportLabel.setText(String.valueOf(teleports));

		// move other stage items
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));

		stage.draw();


		// CHECK OF ACTORS
		// int nrobots = 0;
		// int nsplats = 0;
		// for( Actor a : stage.getActors() ) {
		// 	if( a instanceof RobotActor ) nrobots++;
		// 	if( a instanceof SplatActor ) nsplats++;
		// }
		// if( nrobots != robots.size+1 ) {
		// 	Gdx.app.debug("GameScreenScene2d::render","Stage has " + nrobots + " but array has " + robots.size );
		// 	Gdx.app.exit();
		// }
		// if( nsplats != splatters.size ) {
		// 	Gdx.app.debug("GameScreenScene2d::render","Stage has " + nsplats + " but array has " + splatters.size );
		// 	Gdx.app.exit();
		// }

		// render any effects that are running (not on the stage)
		// Update and draw effects:
		game.batch.begin();
		for (int i = effects.size - 1; i >= 0; i--) {
			PooledEffect effect = effects.get(i);
			effect.draw(game.batch, delta);
			if (effect.isComplete()) {
				effect.free();
				effects.removeIndex(i);
			}
		}

		// and the curent touch
		if( gameState == GameState.PLAYER_TURN && touched ) {
			touchIndicator.draw(game.batch);
		}

		game.batch.end();

		// special overlay texts (top most)
		// if( gameState == GameState.LEVEL_UP ) {
		// 	game.font.getData().setScale( MathUtils.lerp(0.2f,5.5f,animCounter) );
		// 	game.font.draw(stage.getBatch(),"[GREEN]LEVEL CLEAR![]",0,240,800,Align.center,false);
		// }
		// if( gameState == GameState.GAME_OVER ) {
		// 	game.font.getData().setScale( 5.0f + MathUtils.sin(3*animCounter)/2 );
		// 	game.font.draw(stage.getBatch(),"[RED]GAME OVER ![]",0,240,800,Align.center,false);
		// }


	}

	void updateGame() {
		// Apply game state logic - pre rendering....

		// if there are any animations running, then wait for them to finish
		if( player.hasActions() ) return;
		for( Actor a : robots )
			if( a.hasActions() ) return;
		for( Actor a : splatters )
			if( a.hasActions() ) return;
		if( effects.size > 0 ) return;

		// if there are no actions, then look at the state
		switch( gameState ) {

			case ROBOT_TURN:
				float player_x = player.getX();
				float player_y = player.getY();

				// is the game already over?
				for( Actor a : robots ) {
					if( a.getX() == player_x && a.getY() == player_y ) {
						Gdx.app.debug("GameScreenScene2d:updateGame","Player contacted robot - Player dead!");
						gameState = GameState.DEATH;
						return;
					}
				}
				for( Actor a : splatters ) {
					if( a.getX() == player_x && a.getY() == player_y ) {
						Gdx.app.debug("GameScreenScene2d:updateGame","Player contacted splatter - Player dead!");
						gameState = GameState.DEATH;
						return;
					}
				}

				// set off the robot next move...
				for( Actor robot : robots ) {
					float r_x = robot.getX();
					float r_y = robot.getY();
					if( r_x < player_x) {
						r_x += TILE_SIZE;
					} else if( r_x > player_x ) {
						r_x -= TILE_SIZE;
					}
					if( r_y < player_y ) {
						r_y += TILE_SIZE;
					} if( r_y > player_y ) {
						r_y -= TILE_SIZE;
					}
					MoveToAction act = Actions.moveTo(r_x, r_y, ANIMATION_TIME);
					act.setInterpolation(Interpolation.linear);
					robot.addAction( act );
				}

				gameState = GameState.ROBOT_CHECK;
				break;

			case ROBOT_CHECK:
				//Gdx.app.debug("GameScreenScene2d","ROBOT_CHECK (animation finished)");

				int scoreInc = 1;
				for( int index = robots.size-1; index>=0; index-- ) {
					Actor robot = robots.get(index);
					boolean robotAlive = true;

					if( player.getX() == robot.getX() && player.getY() == robot.getY() ) {
						// a robot hitting the player ends the game
						Gdx.app.debug("GameScreenScene2d:updateGame","Robot captured player - Player dead!");
						gameState = GameState.DEATH;
						return;
					}
					else {
						for( Actor a : splatters ) {
							if( a.getX() == robot.getX() && a.getY() == robot.getY() ) {
								// a robot hitting a splatter is destroyed
								Gdx.app.debug("GameScreenScene2d:updateGame","robot hit existing splatter +" + scoreInc);
								robotAlive = false;
								break;
							}
						}
						if( robotAlive ) {
							for( int i=0; i<index; i++ ) {
								Actor r2 = robots.get(i);
								if( robot.getX() == r2.getX() && robot.getY() == r2.getY() ) {
									// robot collision - creates a splatter
									Gdx.app.debug("GameScreenScene2d:updateGame","Robot+Robot=Splatter. +" + scoreInc);
									robotAlive = false;

									SplatActor new_splatter = new SplatActor( game.textureRegions.get(game.SPLAT_SPRITE_NAME) );
									new_splatter.setPosition(robot.getX(),robot.getY());
									new_splatter.setLevel(level);
									splatters.add(new_splatter);
									stage.addActor(new_splatter);

									break;
								}
							}
						}

						if( !robotAlive ) {
							// create a new explode effect
							PooledEffect effect = explodeEffectPool.obtain();
							effect.setPosition(robot.getX(), robot.getY());
							effects.add(effect);

							// remove the robot and gain score
							robot.addAction( Actions.removeActor() );
							robots.removeIndex(index);
							score += scoreInc;
							scoreInc *= 2;
						}

					}

				}

				// Are there any robots left?
				if( robots.size == 0 ) {
					Gdx.app.debug("ROBOT_CHECK","No robots left. Level up!");
					gameState = GameState.LEVEL_UP;
				} else {
					gameState = GameState.PLAYER_TURN;
				}

				break;

			case TELEPORT:
				if( teleports <= 0 ) {
					Gdx.app.log("TELEPORT","Out of teleports!");
					gameState = GameState.PLAYER_TURN;
					break;
				}
				Vector2 nextpos = new Vector2(randomSafeTeleportLocation());
				if( nextpos.epsilonEquals(player.getX(), player.getY(), 0f) ) {
					Gdx.app.log("TELEPORT","Teleport failed!");
					gameState = GameState.PLAYER_TURN;
				}	else {
					Gdx.app.debug("TELEPORT","Teleport successful");
					//teleports--;
					MoveToAction act = Actions.moveTo(nextpos.x, nextpos.y, ANIMATION_TIME);
					act.setInterpolation(Interpolation.circle);
					player.addAction( act );
					gameState = GameState.ROBOT_TURN;
				}
				break;

			case LEVEL_UP:
				// Create a "LEVEL CLEAR" label with animation...
				// NEED TO USE A GRAPHIC FOR THIS?
				Label lbl = new Label("LEVEL CLEAR", new Label.LabelStyle(game.font,Color.GREEN));
				lbl.setAlignment(Align.center,Align.center);
				Container<Label> levelup = new Container<Label>(lbl);

				levelup.setTransform(true);   // for enabling scaling and rotation
				levelup.setPosition(400,240);
				levelup.setScale(0.1f,0.1f);

				levelup.addAction(
					Actions.sequence(
						Actions.scaleTo(1.0f,1.0f,ANIMATION_TIME),
						Actions.delay(ANIMATION_TIME),
						Actions.parallel(
							Actions.scaleTo(1.0f,1.0f,3.0f),
							Actions.fadeOut(3.0f)
						),
						Actions.run(new Runnable() {
							public void run () {
								Gdx.app.debug("LEVEL_UP","Action complete!");
								//level++;
								//startLevel();
							}
						}),
						Actions.removeActor()
					)
				);
				stage.addActor(levelup);

				break;

		// 	case DEATH:
		// 		// start a death animation and enter the GAME_OVER state
		// 		animCounter = 0f;
		// 		touched = false;
		// 		player.animateRotation(0f,-90f,LEVEL_UP_WAIT_TIME);
		//
		// 		int highscore = game.prefs.getInteger("highscore",0);
		// 		if( score > highscore ) {
		// 			game.prefs.putInteger("highscore",score);
		// 		}
		//
		// 		gameState = GameState.GAME_OVER;
		// 		break;
		//
		// 	case GAME_OVER:
		// 		animCounter += delta;
		// 		if( player.isAnimating() ) break;
		//
		// 		if( Gdx.input.isTouched() ) {
		// 			touched = true;
		// 		} else if( touched && !Gdx.input.isTouched() ) {
		// 			// was touched, not it's not (that's a press!)
		// 			game.setScreen(RobotsGame.Screens.MENU);
		//
		// 		}
		}
	}


	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void show() {
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
		game.prefs.flush();
		stage.dispose();
	}
}
