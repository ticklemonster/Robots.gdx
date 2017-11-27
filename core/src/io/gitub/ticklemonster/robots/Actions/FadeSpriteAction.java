package io.gitub.ticklemonster.robots.Actions;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Interpolation;

public class FadeSpriteAction implements Action {
  Sprite sprite;
  float fromA, toA;
  float duration, timer;
  Interpolation lerper;
  boolean isComplete;

  public FadeSpriteAction( Sprite sprite, float fromA, float toA, float duration, Interpolation lerp ) {
    this.sprite = sprite;
    this.fromA = fromA;
    this.toA = toA;
    this.duration = duration;
    this.lerper = lerp;
    this.timer = 0f;
    this.isComplete = false;
  }

  public static FadeSpriteAction FadeOut( Sprite sprite, float duration ) {
    return new FadeSpriteAction(sprite, 1f, 0f, duration, Interpolation.linear);
  }

  public static FadeSpriteAction FadeIn( Sprite sprite, float duration ) {
    return new FadeSpriteAction(sprite, 0f, 1f, duration, Interpolation.linear);
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
      // finished
      sprite.setAlpha( toA );
      this.isComplete = true;
    } else {
      // still going...
      float pct = timer / duration;
      sprite.setAlpha(lerper.apply(fromA, toA, pct));
      this.isComplete = false;
    }
  }

  @Override
  public boolean isComplete() {
    return this.isComplete;
  }


}
