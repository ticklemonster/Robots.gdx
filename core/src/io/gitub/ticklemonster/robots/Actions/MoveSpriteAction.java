package io.gitub.ticklemonster.robots;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Interpolation;

public class MoveSpriteAction implements Action{
  Sprite sprite;
  float fromX, fromY, toX, toY;
  float duration, timer;
  Interpolation lerper;
  boolean isComplete;

  public MoveSpriteAction( Sprite sprite, float fromX, float fromY, float toX, float toY, float duration, Interpolation lerp ) {
    this.sprite = sprite;
    this.fromX = fromX;
    this.fromY = fromY;
    this.toX = toX;
    this.toY = toY;
    this.duration = duration;
    this.lerper = lerp;
    this.timer = 0f;
    this.isComplete = false;
  }

  public static MoveSpriteAction MoveFromTo( Sprite sprite, float x1, float y1, float x2, float y2, float duration, Interpolation lerp ) {
    return new MoveSpriteAction(sprite, x1, y1, x2, y2, duration, lerp);
  }
  public static MoveSpriteAction MoveFromTo( Sprite sprite, float x1, float y1, float x2, float y2, float duration ) {
    return new MoveSpriteAction(sprite, x1, y1, x2, y2, duration, Interpolation.linear);
  }
  public static MoveSpriteAction MoveTo( Sprite sprite, float to_x, float to_y, float duration, Interpolation lerp ) {
    return new MoveSpriteAction(sprite, sprite.getX(), sprite.getY(), to_x, to_y, duration, lerp );
  }
  public static MoveSpriteAction MoveTo( Sprite sprite, float to_x, float to_y, float duration ) {
    return new MoveSpriteAction(sprite, sprite.getX(), sprite.getY(), to_x, to_y, duration, Interpolation.linear );
  }

  public void setInterpolation( Interpolation i ) {
    this.lerper = i;
  }

  @Override
  public void reset() {
    this.isComplete = false;
    this.timer = 0f;
  }

  @Override
  public void update( float delta ) {
    if( isComplete ) return;

    timer += delta;
    if( timer >= duration ) {
      sprite.setX( toX );
      sprite.setY( toY );
      this.isComplete = true;
    } else {
      // still going...
      float pct = timer / duration;
      sprite.setX(lerper.apply(fromX, toX, pct));
      sprite.setY(lerper.apply(fromY, toY, pct));
      this.isComplete = false;
    }
  }

  @Override
  public boolean isComplete() {
    return this.isComplete;
  }


}
