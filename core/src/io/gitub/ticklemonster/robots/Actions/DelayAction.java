package io.gitub.ticklemonster.robots;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;

public class DelayAction implements Action{
  float duration, timer;
  boolean isComplete;

  public DelayAction( float delay ) {
    duration = delay;
    timer = 0f;
    isComplete = false;
  }

  public static DelayAction RandomDelay( float min, float max ) {
    return new DelayAction( MathUtils.random(min,max) );
  }

  public static DelayAction RandomDelay( float max ) {
      return DelayAction.RandomDelay(0f,max);
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
      this.isComplete = true;
    }
  }

  @Override
  public boolean isComplete() {
    return this.isComplete;
  }
}
