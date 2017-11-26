package io.gitub.ticklemonster.robots;

import java.util.Iterator;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Interpolation;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Input.Keys;

// TODO: Add move timer (as "difficult" variant?)

// TODO: Add "sepcial case" animations??
// - Moving the player over the "teleport" button gets a free teleport?
// - moving the player over the "Score" gets free points?

// TODO: Add "achievements" (will require recording player details?)
// - First kill "that was easy"
// - Kill 5 bots without moving "cool hand splatterer"
// - First time off screen "boundless enthusiasm"
// - First off-screen splatter "splatter unbounded"
// - Kill all bots without moving "motionless killer"

// TODO: Add sounds?

// TODO: Allow screen rotation - and deal with off-board rotations



public class GameScreen implements Screen {
	static final float LEVEL_UP_WAIT_TIME = 3.0f;
	static final float ANIMATION_TIME = 0.33f;
	static final float TILE_SIZE = 32f;
	static enum GameState {
		START_LEVEL, CLEANUP_SPLATTERS, SPAWN_PLAYER, SPAWN_ROBOTS,
		PLAYER_TURN, TELEPORT, PLAYER_MOVED, AFTER_PLAYER_MOVE,
		ROBOT_TURN, AFTER_ROBOT_MOVE,
		LEVEL_UP, DEATH, GAME_OVER
	};

	// TODO: Move these "constants" to preferences
	// 	and add a preference panel
	final float MOVE_TIMER = 3.0f;
	final int SPLATTER_LIFE = 4;
	final int INITIAL_TELEPORTS = 3;
	final int INITIAL_SPLATTERS = 3;
	final int INITIAL_ROBOTS = 6;

	// game state
	final RobotsGame game;
	GameState gameState;
	GameState nextState;
	ScoreManager scoreManager;
	int wave;
	int teleports;

	// user input management
	boolean touched;
	boolean touchTeleport;
	Rectangle teleportButtonRect;

	// visible components
	OrthographicCamera camera;
	TextureRegion[] tileTextures;
	Array<Sprite> splatters;
	Array<Sprite> robots;
	Array<Sprite> faders;	// faders will live until they fade out
	ParticleEffectPool explodeEffectPool;
	Array<PooledEffect> effects;
	Array<Action> animations;
	Sprite textEffect;
	Sprite player;
	Sprite touchIndicator;
	Sprite waveClearSprite;
	Sprite gameOverSprite;
	float animCounter;


	public GameScreen(final RobotsGame game) {
		this.game = game;

		Gdx.app.setLogLevel(Gdx.app.LOG_INFO);
		Gdx.app.debug("GameScreen","Starting with DEBUG");

		// cache sprites from the game object
		player = new Sprite( game.textureRegions.get(game.PLAYER_SPRITE_NAME) );
		touchIndicator = new Sprite( game.textureRegions.get(game.TOUCH_SPRITE_NAME) );
		tileTextures = new TextureRegion[2];
		tileTextures[0] = game.textureRegions.get("grey_tile");
		tileTextures[1] = game.textureRegions.get("white_tile");

		waveClearSprite = new Sprite(CreateTexture.FromBitmapFont(game.font,"Wave Clear"));
		waveClearSprite.setColor( Color.GREEN );

		gameOverSprite = new Sprite(CreateTexture.FromBitmapFont(game.font,"GAME OVER"));
		gameOverSprite.setColor( Color.RED );

		// set up an effect pool for explosions
		effects = new Array<PooledEffect>();
		ParticleEffect robotExplode = new ParticleEffect();
		robotExplode.load(Gdx.files.internal("robot_destruction.p"), game.atlas);
		robotExplode.setEmittersCleanUpBlendFunction(false);
		explodeEffectPool = new ParticleEffectPool(robotExplode, 1, 2);

		// initialise data structures
		splatters = new Array<Sprite>();
		robots = new Array<Sprite>();
		animations = new Array<Action>();
		faders = new Array<Sprite>();
		scoreManager = new ScoreManager();

		initGame();
	}

	private void initGame() {
		camera = new OrthographicCamera();
		camera.setToOrtho(false,800,480);

		// Prepare the display data, arrays and pools...
		splatters.clear();
		robots.clear();
		faders.clear();

		// start listening for input
		touched = false;
		touchTeleport = false;
		teleportButtonRect = null;

		Gdx.input.setInputProcessor( new GameInputAdapter() );

		// put the player in the start position (assume centre - always)
		player.setAlpha(0f);
		player.setRotation(0f);
		player.setPosition(384, 224);

		// set initial counter values
		scoreManager.reset();
		wave = 1;
		teleports = 0;
		gameState = null;
		nextState = GameState.START_LEVEL;

	}

	//
	// Input handling
	//
	private class GameInputAdapter implements InputProcessor {
		private int numTouches;

		public GameInputAdapter() {
			super();
			numTouches = 0;
		}

		@Override
		public boolean keyUp (int keycode) {
			if( !canAcceptInput() ) return true;	// ignore keys

			float player_x = player.getX();
			float player_y = player.getY();
			float move_x = 0f, move_y = 0f;
			switch(keycode) {
				case Keys.UP:
				case Keys.NUMPAD_8:
					if( player_y < 480-TILE_SIZE ) move_y = TILE_SIZE;
					break;
				case Keys.DOWN:
				case Keys.NUMPAD_2:
					if( player_y > 0 ) move_y = -TILE_SIZE;
					break;
				case Keys.LEFT:
				case Keys.NUMPAD_4:
					if( player_x > 0 )	move_x = -TILE_SIZE;
					break;
				case Keys.RIGHT:
				case Keys.NUMPAD_6:
					if( player_x < 800-TILE_SIZE ) move_x = TILE_SIZE;
					break;
				case Keys.NUMPAD_7:	// UP+LEFT
					if( player_x > 0 && player_y < 480-TILE_SIZE ) {
						move_x = -TILE_SIZE;
						move_y = TILE_SIZE;
					}
					break;
				case Keys.NUMPAD_9:	// UP+RIGHT
					if( player_x < 800-TILE_SIZE && player_y < 480-TILE_SIZE ) {
						move_x = TILE_SIZE;
						move_y = TILE_SIZE;
					}
					break;
				case Keys.NUMPAD_1:	// DOWN+LEFT
					if( player_x > 0 && player_y > 0 ) {
						move_x = -TILE_SIZE;
						move_y = -TILE_SIZE;
					}
					break;
				case Keys.NUMPAD_3:	// DOWN+RIGHT
					if( player_x < 800-TILE_SIZE && player_y > 0 ) {
						move_x = TILE_SIZE;
						move_y = -TILE_SIZE;
					}
					break;
				case Keys.NUMPAD_0:
				case Keys.SPACE:
					nextState = GameState.TELEPORT;
					break;
				case Keys.NUMPAD_5:
					// deliberate "no move"
					nextState = GameState.AFTER_PLAYER_MOVE;
					break;
			 default:

			}

			if( move_x!=0 || move_y!=0 ) {
				touchIndicator.setPosition(player_x + move_x, player_y + move_y);
				nextState = GameState.PLAYER_MOVED;
			}

			return true;
		}

		@Override
		public boolean touchDown (int x, int y, int pointer, int button) {
			if( !canAcceptInput() ) return false;

			Gdx.app.log("GameScreen::GameInputAdapter","touch down with pointer=" + pointer);

			// more than one finger touch => cancel any touch
			numTouches++;
			if( numTouches > 1 ) {
				touched = false;
				touchTeleport = false;
				return true;
			};

			// set touchTeleport if a touch started over the teleport label
			Vector3 touchPos = new Vector3(x,y,0);
			camera.unproject(touchPos);
			touchTeleport = teleportButtonRect.contains(touchPos.x,touchPos.y);
			touched = true;
			updateTouch(x,y);

			return true; // return true to indicate the event was handled
		}

		@Override
		public boolean touchDragged (int x, int y, int pointer) {
			if( !canAcceptInput() ) return false;

			updateTouch(x,y);
			return true;
		}

		@Override
		public boolean touchUp (int x, int y, int pointer, int button) {
			if( numTouches > 0 ) numTouches--;
		  if( !canAcceptInput() ) return false;
			if( !touched ) return true;

			// Accept the touch and make the move
			if( touchTeleport ) {
				nextState = GameState.TELEPORT;
			} else {
				nextState = GameState.PLAYER_MOVED;
			}

			touched = false;
			touchTeleport = false;
			return true; // return true to indicate the event was handled
		}

		private boolean canAcceptInput() {
			return (gameState == GameState.PLAYER_TURN); //&& animations.size == 0);
		}

		private void updateTouch( float x, float y ) {
		 Vector3 touchPos = new Vector3(x,y,0);
		 camera.unproject(touchPos);

		 if( touchTeleport && teleportButtonRect.contains(touchPos.x,touchPos.y) ) {
			 // started on Teleport and still there
			 touchTeleport = true;	touched = true;
			 return;
		 }
		 else if( touchTeleport ) {
			 // was touching teleport, now we're not = cancel
			 touched = false; touchTeleport = false;
			 return;
		 }

		 // otherwise, where are we pointing?
		 Vector2 vect = new Vector2(
			 touchPos.x-player.getX() - TILE_SIZE/2,
			 touchPos.y-player.getY() - TILE_SIZE/2
		 );

		 // pointing close to player => no move;
		 float magn = vect.len2();
		 if( magn < TILE_SIZE*TILE_SIZE ) {
			 touchIndicator.setPosition( player.getX(), player.getY() );
			 return;
		 }

		 // pointing further away => use angles
		 //		 90
		 //	180 + 0
		 //	   270
		 float angle = vect.angle();
		 touchIndicator.setPosition( player.getX(), player.getY() );
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


		// unused
		@Override
		public boolean mouseMoved(int screenX, int screenY) { return false;	}
		@Override
		public boolean scrolled(int amount) { return false; }
		@Override
		public boolean keyDown( int keycode ) { return false;	}
		@Override
		public boolean keyTyped(char character) { return false;	}


	};


	//
	// Game Board handling
	//
	private boolean overlaps( Sprite a, Sprite b ) {
		Rectangle ra = new Rectangle( a.getX(), a.getY(), a.getWidth(), a.getHeight() );
		Rectangle rb = new Rectangle( b.getX(), b.getY(), b.getWidth(), b.getHeight() );
		return ra.overlaps(rb);
	}

	private boolean overlaps( Sprite s, Rectangle r ) {
		Rectangle rs = new Rectangle( s.getX(), s.getY(), s.getWidth(), s.getHeight() );
		return rs.overlaps(r);
	}

	private boolean overlapsAny(Rectangle rect, Array<Sprite> items) {
		for( Sprite item : items ) {
			if( overlaps(item,rect) ) {
				return true;
			}
		}
		return false;
	}

	private boolean overlapsAny(Sprite s, Array<Sprite> items) {
		for( Sprite item : items ) {
			if( overlaps(item,s) ) {
				return true;
			}
		}
		return false;
	}

	private boolean isOutOfBounds(Sprite s) {
		// Landscape view:
		return( s.getX() < 160 || s.getX() > 160+14*TILE_SIZE );
	}

	// private boolean isOnBoard(Vector2 v) {
  //
	// }

	private Rectangle randomLocation() {
		return new Rectangle(
			MathUtils.random(0,14)*TILE_SIZE + 160,
			MathUtils.random(0,14)*TILE_SIZE,
			TILE_SIZE,
			TILE_SIZE
		);
	}

	private Rectangle randomEmptyLocation() {
		Rectangle dest = new Rectangle();
		boolean isValid = false;
		int attempts = 0;

		while( !isValid && attempts++ < 255 ) {
			// get a random location
			dest.set(randomLocation());
			isValid = (
				overlapsAny(dest,robots) == false
				&& overlapsAny(dest,splatters) == false
				&& overlaps(player,dest) == false
			);
		}

		return isValid?dest:null;
	}

	private Rectangle exhaustiveSafeTeleportLocation() {
		/*
		 * Version 2 - build a list of valid locations and choose one
		 */
		Gdx.app.log("GameScreen::exhaustiveSafeTeleportLocation", "starting");
		Array<Vector2> safe = new Array<Vector2>(15*15);

		// initialise to every location
		for( int y=0; y<480; y+=32 ) {
			for( int x=160; x<640; x+=32 ) {
				safe.add( new Vector2(x,y) );
			}
		}
		// remove splat locations
		for( Sprite splat : splatters ) {
			Vector2 splatpos = new Vector2(splat.getX(),splat.getY());
			safe.removeValue(splatpos,false);
		}
		// remove potential robot locations
		for( Sprite robot : robots ) {
			Vector2 robotpos = new Vector2(robot.getX(), robot.getY());
			safe.removeValue(robotpos,false);
			safe.removeValue(robotpos.add(-32,  0),false);
			safe.removeValue(robotpos.add(  0, 32),false);
			safe.removeValue(robotpos.add( 32,  0),false);
			safe.removeValue(robotpos.add( 32,  0),false);
			safe.removeValue(robotpos.add(  0,-32),false);
			safe.removeValue(robotpos.add(  0,-32),false);
			safe.removeValue(robotpos.add(-32,  0),false);
			safe.removeValue(robotpos.add(-32,  0),false);
		}
		Gdx.app.log("GameScreen::exhaustiveSafeTeleportLocation", "- " + robots.size + " robots: " + safe.size + " safe locations");

		// first pass - is there something here already...
		Vector2 safepos = safe.random();
		if( safepos == null )	{
			return new Rectangle( player.getX(), player.getY(), player.getWidth(), player.getHeight() );
		}

		return new Rectangle( safepos.x, safepos.y, player.getWidth(), player.getHeight() );
	}

	private Rectangle randomSafeTeleportLocation() {
		// try randomly to find a safe location.
		Rectangle dest = new Rectangle();
		int attempts = 0;
		boolean isValid = false;

		while( !isValid && attempts++ < 255 ) {
			dest.set( randomLocation() );
			if( overlapsAny(dest,splatters) ) {
				isValid = false;
			} else {
				isValid = true;
				for( Sprite robot : robots ) {
					float dx = Math.abs(robot.getX() - dest.x)/32;
					float dy = Math.abs(robot.getY() - dest.y)/32;
					if( dx < 2 && dy < 2 ) {
						isValid = false;
						break;
					}
				}
			}
		}

		if( attempts >= 255 ) {
			Gdx.app.log("GameScreen::randomSafeTeleportLocation","Failed to find safe place in 255 tries");
			dest = exhaustiveSafeTeleportLocation();
		} else {
			Gdx.app.debug("GameScreen::randomSafeTeleportLocation","Found location after " + attempts + " attempts");
		}

		return dest;
	}

	// public void update() {
	// 	Gdx.app.log("GameScreen","update");
	// }

	@Override
	public void render (float delta) {
		animCounter += delta;

		// STEP 1: Update running animations and effects
		//
		for( int i = animations.size - 1; i >= 0; i-- ) {
			Action anim = animations.get(i);
			anim.update(delta);
			if (anim.isComplete()) {
				//anim.free();
				animations.removeIndex(i);
			}
		}

		// score animation (with max 50 frames)
		scoreManager.update();

		// STEP 2: Apply game state logic
		// - move to next state if the animations have finished
		if( animations.size == faders.size ) {
			gameState = nextState;
			updateGameState();
		}


		// STEP3: Render the resulting state
		//
		Gdx.gl.glClearColor(0.37f, 0.37f, 0.37f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();
		game.batch.setProjectionMatrix(camera.combined);
		game.batch.begin();

		// render static portions
		drawBackground(game.batch);
		drawLabels(game.batch);

		// draw Splatters below moving things
		for( Sprite splat : splatters ) {
			splat.draw(game.batch);
		}

		// show touch indicator below things
		if( gameState == GameState.PLAYER_TURN && touched && !touchTeleport ) {
			touchIndicator.draw(game.batch);
		}

		// draw the player in the middle
		player.draw( game.batch );

		// draw robots on top
		for( Sprite robot : robots ) {
			robot.draw( game.batch );
		}

		// render any effects that are running
		// Update and draw effects:
		for (int i = effects.size - 1; i >= 0; i--) {
			PooledEffect effect = effects.get(i);
			effect.draw(game.batch, delta);
			if (effect.isComplete()) {
				effect.free();
				effects.removeIndex(i);
			}
		}

		// render floating items - and destroy them when they are faded
		for( int i=faders.size - 1; i >= 0; i-- ) {
			Sprite s = faders.get(i);
			if( s.getColor().a <= 0 ) {
				// faded out - get rid of this item
				faders.removeIndex(i);
			} else {
				s.draw( game.batch );
			}
		}

		// special overlay texts (top most)
		// if( gameState == GameState.GAME_OVER ) {
		// 	game.font.getData().setScale( 5.0f + MathUtils.sin(3*animCounter)/2 );
		// 	game.font.draw(game.batch,"[RED]GAME OVER ![]",0,240,800,Align.center,false);
		// }

		game.batch.end();
	}

	private void drawBackground(SpriteBatch batch) {
		for( int x=0; x<15; x++ ) {
			for( int y=0; y<15; y++ ) {
				batch.draw(tileTextures[(x+y)%2], 160+x*TILE_SIZE, y*TILE_SIZE);
			}
		}
	}

	private void drawLabels(SpriteBatch batch) {
		game.font.getData().setScale(2.5f);
		game.font.draw(batch,
			String.format("Wave\n%d\n\nTeleports\n[GOLD]%d[]\n\nScore\n%,d\n\nx %s",
				wave,teleports,scoreManager.getDisplayScore(),scoreManager.getMultiplier()),
			10,400,140,Align.center,false);

		if( gameState==GameState.PLAYER_TURN && touchTeleport ) {
			game.font.draw(batch,"[RED]TELEPORT[]",650,32,150,Align.center,false);
		} else {
			GlyphLayout gl = game.font.draw(batch,"TELEPORT",650,32,150,Align.center,false);
			if( teleportButtonRect == null ) {
					teleportButtonRect = new Rectangle(725-(gl.width-TILE_SIZE)/2,gl.height+TILE_SIZE/2,gl.width+TILE_SIZE,gl.height+TILE_SIZE);
			}
		}
	}

	private void updateGameState() {
		switch( gameState ) {
			case START_LEVEL:
				// get a bonus teleport for starting a wave
				this.teleports = java.lang.Math.max(this.teleports+1,INITIAL_TELEPORTS);

				// cleanup running animations
				effects.clear();
				animations.clear();
				//spawnMessage("Wave " + wave, Color.WHITE);

				// reset any touches
				nextState = GameState.CLEANUP_SPLATTERS;
				break;

			case CLEANUP_SPLATTERS:
				// remove any splatters that have aged out
				for (int i = splatters.size - 1; i >= 0; i--) {
					Sprite splat = splatters.get(i);
					// use the alpha channel to store liveness
					Color  color = splat.getColor();
					color.a -= 0.1f;

					if( color.a < 1.0f - 0.1f*SPLATTER_LIFE ) {
						// Add splatter disposal animation/sound

						ParallelAction a = new ParallelAction(
							FadeSpriteAction.FadeOut(splat,ANIMATION_TIME*3),
							ScaleSpriteAction.ScaleTo(splat,5.0f,ANIMATION_TIME*3,Interpolation.exp5In)
						);
						animations.add(a);
						faders.add(splat);
						splatters.removeIndex(i);
					}
					else {
						splat.setColor(color);
					}

					if( isOutOfBounds(splat) ) {
						//TODO: Deal with no possible free spaces

						// splat off-screen - create 3 new ones ...
						Rectangle to_r0 = new Rectangle(randomEmptyLocation());
						Rectangle to_r1 = new Rectangle(randomEmptyLocation());
						while( to_r0.overlaps(to_r1) ) {
							to_r1.set(randomEmptyLocation());
						}
						Rectangle to_r2 = new Rectangle(randomEmptyLocation());
						while( to_r2.overlaps(to_r0) || to_r2.overlaps(to_r1) ) {
							to_r2.set(randomEmptyLocation());
						}

						// create new splat sprites...
						Sprite new_s1 = new Sprite(game.textureRegions.get(game.SPLAT_SPRITE_NAME));
						new_s1.setPosition(splat.getX(), splat.getY());
						new_s1.setAlpha(1.0f);
						splatters.add(new_s1);

						Sprite new_s2 = new Sprite(game.textureRegions.get(game.SPLAT_SPRITE_NAME));
						new_s2.setPosition(splat.getX(), splat.getY());
						new_s2.setAlpha(1.0f);
						splatters.add(new_s2);

						// animate the three sprites to their new locations
						float midx = (to_r0.x + to_r1.x + to_r2.x)/3;
						float midy = (to_r0.y + to_r1.y + to_r2.y)/3;
						float speed0 = ANIMATION_TIME;
						float speed1 = ANIMATION_TIME * MathUtils.random(1.0f,2.0f);
						float speed2 = ANIMATION_TIME * MathUtils.random(1.0f,2.0f);

						ParallelAction flyUpAct = new ParallelAction();
						flyUpAct.add( MoveSpriteAction.MoveTo(splat, midx, midy, ANIMATION_TIME, Interpolation.exp5Out) );
						flyUpAct.add( MoveSpriteAction.MoveTo(new_s1, midx, midy, ANIMATION_TIME, Interpolation.exp5Out) );
						flyUpAct.add( MoveSpriteAction.MoveTo(new_s2, midx, midy, ANIMATION_TIME, Interpolation.exp5Out) );
						flyUpAct.add( ScaleSpriteAction.ScaleFromTo(splat, 1.0f, 1.6f, ANIMATION_TIME) );
						flyUpAct.add( ScaleSpriteAction.ScaleFromTo(new_s1, 1.0f, 1.6f, ANIMATION_TIME) );
						flyUpAct.add( ScaleSpriteAction.ScaleFromTo(new_s2, 1.0f, 1.6f, ANIMATION_TIME) );
						ParallelAction flyDownAct = new ParallelAction();
						flyDownAct.add( MoveSpriteAction.MoveFromTo(splat, midx, midy, to_r0.x, to_r0.y, ANIMATION_TIME, Interpolation.exp5In) );
						flyDownAct.add( MoveSpriteAction.MoveFromTo(new_s1, midx, midy, to_r1.x, to_r1.y, speed1, Interpolation.exp5In) );
						flyDownAct.add( MoveSpriteAction.MoveFromTo(new_s2, midx, midy, to_r2.x, to_r2.y, speed2, Interpolation.exp5In) );
						flyDownAct.add( ScaleSpriteAction.ScaleFromTo(splat, 1.6f, 1.0f, ANIMATION_TIME) );
						flyDownAct.add( ScaleSpriteAction.ScaleFromTo(new_s1, 1.6f, 1.0f, speed1) );
						flyDownAct.add( ScaleSpriteAction.ScaleFromTo(new_s2, 1.6f, 1.0f, speed2) );

						animations.add(
							new SequenceAction(	flyUpAct, flyDownAct )
						);
					}
				}

				// set initial splatters for first wave...
				if( wave == 1 ) {
					// spawn initial splatters
					for( int i=0; i<INITIAL_SPLATTERS; i++ ) {
						Rectangle pos = randomEmptyLocation();
						Sprite splat = new Sprite(game.textureRegions.get(game.SPLAT_SPRITE_NAME));
						splat.setPosition( pos.getX(), pos.getY() );
						splatters.add(splat);
						// animations.add(
						// 	ScaleSpriteAction.ScaleFromTo(splat, 0.1f, 1.0f, ANIMATION_TIME * 3, Interpolation.bounceOut)
						// );
					}
				}

				nextState = GameState.SPAWN_PLAYER;
				break;

			case SPAWN_PLAYER:
				if( wave == 1 ) {
					animations.add(FadeSpriteAction.FadeIn(player,ANIMATION_TIME*2));
				}

				// Force the player back on-screen if they finish off-screen
				if( isOutOfBounds(player) ) {
					Rectangle nextRect = randomSafeTeleportLocation();
					Gdx.app.debug("GameScreen::SPAWN_PLAYER","Auto-Teleport back to board");
					teleports++;
					animations.add(
						MoveSpriteAction.MoveTo(
							player, nextRect.x, nextRect.y,
							ANIMATION_TIME, Interpolation.elastic
						)
					);
				}

				nextState = GameState.SPAWN_ROBOTS;
				break;

			case SPAWN_ROBOTS:
				// spawn an appropriate number of robots
				robots.clear();
				int robotCount = INITIAL_ROBOTS + (wave-1)*2;
				for( int r=0; r < robotCount; r++ ) {
					Sprite robot = new Sprite(game.textureRegions.get(game.ROBOT_SPRITE_NAME));
					Rectangle robotpos = randomEmptyLocation();
					if( robotpos == null ) break;	// can't make any more robots!
					robot.setPosition( robotpos.getX(), robotpos.getY() );
					robot.setAlpha(0f);
					robots.add(robot);
				}

				for( Sprite robot : robots ) {
					SequenceAction robotdrop = new SequenceAction(
						DelayAction.RandomDelay(0.2f,0.8f),
					 	new ParallelAction(
					 		MoveSpriteAction.MoveFromTo(
								robot,
								robot.getX(), robot.getY()+TILE_SIZE*1.5f,
								robot.getX(), robot.getY(),
								ANIMATION_TIME,
								Interpolation.bounceOut
							),
							FadeSpriteAction.FadeIn(robot, ANIMATION_TIME/2)
						)
					);
					animations.add(robotdrop);
				}

				nextState = GameState.PLAYER_TURN;
				break;

			case PLAYER_TURN:
				// TODO: Add countdown timer for moves?

				break;

			case PLAYER_MOVED:
				// start a player movement animation
				Action act = MoveSpriteAction.MoveTo(
					player,
					touchIndicator.getX(),
					touchIndicator.getY(),
					ANIMATION_TIME
				);
				animations.add( act );

				touched = false;
				touchTeleport = false;
				nextState = GameState.AFTER_PLAYER_MOVE;
				break;

			case TELEPORT:
				touchTeleport = false;
				if( teleports <= 0 ) {
					Gdx.app.debug("GameScreen::TELEPORT","No teleports available. Do nothing");
					nextState = GameState.PLAYER_TURN;	// can't teleport - try something else
				} else {
					Rectangle nextRect = randomSafeTeleportLocation();
					if( overlaps(player,nextRect) ) {
						Gdx.app.log("GameScreen::TELEPORT","Teleport failed!");
						spawnMessage("No Safe Teleports!", Color.ORANGE);
						nextState = GameState.PLAYER_TURN;
					}	else {
						Gdx.app.debug("GameScreen::TELEPORT","Teleport successful");
						teleports--;

						animations.add(
							MoveSpriteAction.MoveTo(
								player, nextRect.x, nextRect.y,
								ANIMATION_TIME, Interpolation.elastic
							)
						);
						nextState = GameState.AFTER_PLAYER_MOVE;
					}
				}
				break;

			case AFTER_PLAYER_MOVE:
				if( overlapsAny(player,robots) || overlapsAny(player,splatters) ) {
					// Player is dead!
					Gdx.app.debug("GameScreen::AFTER_PLAYER_MOVE","Player is dead!");
					nextState = GameState.DEATH;
				} else {
					nextState = GameState.ROBOT_TURN;
				}
				break;

			case ROBOT_TURN:
				// determine the robot next move...
				float p_x = player.getX();
				float p_y = player.getY();
				for( Sprite r : robots ) {
					float r_x = r.getX();
					float r_y = r.getY();

					if( r_x < p_x ) {
						r_x += TILE_SIZE;
					} else if( r_x > p_x ) {
						r_x -= TILE_SIZE;
					}
					if( r_y < p_y) {
						r_y += TILE_SIZE;
					} else if( r_y > p_y ) {
						r_y -= TILE_SIZE;
					}

					animations.add(
						MoveSpriteAction.MoveTo(r,r_x,r_y,ANIMATION_TIME)
					);
				}

				nextState = GameState.AFTER_ROBOT_MOVE;
				break;

			case AFTER_ROBOT_MOVE:
				nextState = GameState.PLAYER_TURN;

				if( overlapsAny(player,robots) ) {
					// a robot hitting the player ends the game
					Gdx.app.debug("GameScreen::AFTER_ROBOT_MOVE","robot caught player - GAME OVER");
					nextState = GameState.DEATH;
					break;
				}

				int robotsKilled = 0;
				for( int i=robots.size-1; i>=0; i-- ) {
					Sprite robot = robots.get(i);
					boolean robotAlive = true;

					// has "robot" hit a splatter?
					for( Sprite splat : splatters ) {
						if( overlaps(robot,splat) ) {
							// a robot hitting a splatter is destroyed
							Gdx.app.debug("GameScreen::AFTER_ROBOT_MOVE","robot hit existing splatter (+" + (robotsKilled+1) + ")");
							splat.setAlpha(1.0f);	// refresh the splat
							robotAlive = false;
							break;
						}
					}

					if( robotAlive ) {
						// has "robot" hit any other robots?
						for( int j=0;j<i; j++ ) {
							Sprite r2 = robots.get(j);
							if( overlaps(robot, r2) ) {
								// robot-robot collision - creates a splatter
								Gdx.app.debug("GameScreen::AFTER_ROBOT_MOVE","robot hit robot. create new splatter (+" + (robotsKilled+1) + ")");
								Sprite new_splat = new Sprite(game.textureRegions.get(game.SPLAT_SPRITE_NAME));
								new_splat.setAlpha(1.0f);
								new_splat.setPosition(robot.getX(), robot.getY());
								splatters.add(new_splat);
								robotAlive = false;
								break;
							}
						}
					}

					if( !robotAlive ) {
						// create a new explode effect
						PooledEffect effect = explodeEffectPool.obtain();
						effect.setPosition(robot.getX(), robot.getY());
						effects.add(effect);

						// remove the robot and earn some points
						robots.removeIndex(i);
						robotsKilled++;
					}
				}

				long scoreinc = scoreManager.postKills(robotsKilled);
				if( scoreinc > 10 ) {
					// show score floaer
					Sprite scoreSprite = new Sprite(CreateTexture.FromBitmapFont(game.font, "+"+scoreinc));
					if( scoreinc > 1000 ) {
						scoreSprite.setColor(Color.GOLD);
						scoreSprite.setScale(1.5f);
					}
					else if( scoreinc > 100 ) {
						scoreSprite.setColor(Color.BLUE);
						scoreSprite.setScale(1.25f);
					}
					scoreSprite.setPosition(80f - scoreSprite.getWidth()/2, 140f);
					faders.add(scoreSprite);

					animations.add(
						new SequenceAction(
							floatUpAndFade(scoreSprite, 120f, ANIMATION_TIME*5),
							new DisposeTexture(scoreSprite, 0f)
						)
					);
				}

				// Are there any robots left?
				if( robots.size == 0 ) {
					// LEVEL_UP
					Gdx.app.debug("GameScreen::AFTER_ROBOT_MOVE","No robots left. Level up!");

					waveClearSprite.setPosition(400-waveClearSprite.getWidth()/2, 240-waveClearSprite.getHeight()/2);
					waveClearSprite.setScale(2.5f);
					waveClearSprite.setAlpha(1.0f);
					faders.add(waveClearSprite);

					ParallelAction levelupaction = new ParallelAction(
						//MoveSpriteAction.MoveTo(waveClearSprite,waveClearSprite.getX(),500,3.0f),
						ScaleSpriteAction.ScaleFromTo(waveClearSprite,2.5f,10.0f,2.5f),
						FadeSpriteAction.FadeOut(waveClearSprite,2.0f)
					);
					animations.add( levelupaction );
					nextState = GameState.LEVEL_UP;
				}

				break;

			case LEVEL_UP:
				// need ALL animations finished, incuding fades...
				if( animations.size == 0 ) {
					wave++;
					nextState = GameState.START_LEVEL;
				}
				break;

			case DEATH:
				// start a death animation and enter the GAME_OVER state
				animCounter = 0f;
				touched = false;

				animations.add(
					RotateSpriteAction.RotateTo(player, -90f, ANIMATION_TIME*2)
				);

				gameOverSprite.setPosition(400-gameOverSprite.getWidth()/2, 240-gameOverSprite.getHeight()/2);
				gameOverSprite.setScale(3f);
				faders.add(gameOverSprite);
				animations.add(
					new RepeatAction(
						new SequenceAction(
							ScaleSpriteAction.ScaleFromTo(gameOverSprite,3f,5f,ANIMATION_TIME*4,Interpolation.sine),
							ScaleSpriteAction.ScaleFromTo(gameOverSprite,5f,3f,ANIMATION_TIME*4,Interpolation.sine)
						),
						1000
					)
				);

				long highscore = game.prefs.getLong("highscore",0l);
				if( scoreManager.getScore() > highscore ) {
					game.prefs.putLong("highscore",scoreManager.getScore());
				}

				nextState = GameState.GAME_OVER;
				break;

			case GAME_OVER:
				if( Gdx.input.isTouched() ) {
					touched = true;
				} else if( touched && !Gdx.input.isTouched() ) {
					// was touched, not it's not (that's a press!)
					for( Action a : animations ) {
						if( a instanceof RepeatAction ) ((RepeatAction)a).cancel();
					}
					animations.clear();
					game.setScreen(RobotsGame.Screens.MENU);
				}
				break;
		}

	}


	//
	// Builders from some complex adnimations...
	//
	Action flyInAction(Sprite sprite, float x, float y, float duration) {
		float midx = (x + sprite.getX()) / 2;
		float midy = (y + sprite.getY()) / 2;
		float halfd = duration / 2;

		return new SequenceAction(
			new ParallelAction(
			MoveSpriteAction.MoveTo(sprite, midx, midy, halfd, Interpolation.sine),
			ScaleSpriteAction.ScaleFromTo(sprite, 1.0f, 1.6f, halfd )
			),
			new ParallelAction(
				MoveSpriteAction.MoveFromTo(sprite, midx, midy, x, y, halfd, Interpolation.sine),
				ScaleSpriteAction.ScaleFromTo(sprite, 1.6f, 1.0f, halfd )
			)
		);
	}

	Action floatUpAndFade(Sprite sprite, float height, float duration) {
		return new ParallelAction(
			MoveSpriteAction.MoveTo(sprite,	sprite.getX(), sprite.getY()+height, duration/2),
			new SequenceAction(
				new DelayAction(duration/6),
				FadeSpriteAction.FadeOut(sprite, duration/3)
			)
		);
	}

	void spawnMessage(String msg, Color c) {
		Sprite msgSprite = new Sprite(CreateTexture.FromBitmapFont(game.font, msg));
		msgSprite.setPosition(400 - msgSprite.getWidth()/2, 240-msgSprite.getHeight()/2);
		msgSprite.setScale(0.1f);
		msgSprite.setColor(c);
		faders.add(msgSprite);

		animations.add(
			new SequenceAction(
				new ParallelAction(
					FadeSpriteAction.FadeIn(msgSprite,ANIMATION_TIME),
					ScaleSpriteAction.ScaleFromTo(msgSprite,0.1f,2.5f,ANIMATION_TIME*2)
				),
				new DelayAction(ANIMATION_TIME * 3),
				new ParallelAction(
					FadeSpriteAction.FadeOut(msgSprite,ANIMATION_TIME*2),
					ScaleSpriteAction.ScaleFromTo(msgSprite,2.5f,10f,ANIMATION_TIME*2)
				)
			)
		);

	}


	@Override
	public void resize(int width, int height) {
		Gdx.app.log("GameScreen::resize","new size = " + width + "," + height);
	}

	@Override
	public void show() {
		Gdx.app.debug("GameScreen::show","show from gameState=" + gameState);

		if(gameState == GameState.GAME_OVER) {
			// showing a finished game? Probably should be restarting
			initGame();
		}
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
	}
}
