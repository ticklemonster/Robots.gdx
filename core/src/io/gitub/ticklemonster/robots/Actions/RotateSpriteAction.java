package io.gitub.ticklemonster.robots;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Interpolation;

public class RotateSpriteAction implements Action{
  Sprite sprite;
  float fromR, toR;
  float duration, timer;
  Interpolation lerper;
  boolean isComplete;

  public RotateSpriteAction( Sprite sprite, float fromR, float toR, float duration, Interpolation lerp ) {
    this.sprite = sprite;
    this.fromR = fromR;
    this.toR = toR;
    this.duration = duration;
    this.lerper = lerp;
    this.timer = 0f;
    this.isComplete = false;
  }

  public static RotateSpriteAction RotateTo( Sprite sprite, float to_r, float duration, Interpolation lerp ) {
    return new RotateSpriteAction(sprite, sprite.getRotation(), to_r, duration, lerp);
  }
  public static RotateSpriteAction RotateTo( Sprite sprite, float to_r, float duration ) {
    return new RotateSpriteAction(sprite, sprite.getRotation(), to_r, duration, Interpolation.linear);
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
      sprite.setRotation( toR );
      this.isComplete = true;
    } else {
      // still going...
      float pct = timer / duration;
      sprite.setRotation(lerper.apply(fromR, toR, pct));
      this.isComplete = false;
    }
  }

  @Override
  public boolean isComplete() {
    return this.isComplete;
  }


}
