package io.gitub.ticklemonster.robots;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Interpolation;

public class ScaleSpriteAction implements Action{
  Sprite sprite;
  float fromS, toS;
  float duration, timer;
  Interpolation lerper;
  boolean isComplete;

  public ScaleSpriteAction( Sprite sprite, float fromS, float toS, float duration, Interpolation lerp ) {
    this.sprite = sprite;
    this.fromS = fromS;
    this.toS = toS;
    this.duration = duration;
    this.lerper = lerp;
    this.timer = 0f;
    this.isComplete = false;
  }

  public static ScaleSpriteAction ScaleFromTo( Sprite sprite, float from_s, float to_s, float duration, Interpolation lerp ) {
    return new ScaleSpriteAction(sprite, from_s, to_s, duration, lerp);
  }
  public static ScaleSpriteAction ScaleFromTo( Sprite sprite, float from_s, float to_s, float duration ) {
    return new ScaleSpriteAction(sprite, from_s, to_s, duration, Interpolation.linear);
  }
  public static ScaleSpriteAction ScaleTo( Sprite sprite, float to_s, float duration, Interpolation lerp ) {
    return new ScaleSpriteAction(sprite, sprite.getScaleX(), to_s, duration, lerp);
  }
  public static ScaleSpriteAction ScaleTo( Sprite sprite, float to_s, float duration ) {
    return new ScaleSpriteAction(sprite, sprite.getScaleX(), to_s, duration, Interpolation.linear);
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
      sprite.setScale( toS );
      this.isComplete = true;
    } else {
      // still going...
      float pct = timer / duration;
      sprite.setScale(lerper.apply(fromS, toS, pct));
      this.isComplete = false;
    }
  }

  @Override
  public boolean isComplete() {
    return this.isComplete;
  }


}
