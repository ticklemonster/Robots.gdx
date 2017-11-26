package io.gitub.ticklemonster.robots;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;

public class DisposeTexture implements Action {
  float duration, timer;
  Texture texture;
  boolean isComplete;

  public DisposeTexture( Texture texture, float delay ) {
    this.duration = delay;
    this.texture = texture;
    timer = 0f;
    isComplete = false;
  }

  public DisposeTexture( Sprite sprite, float delay ) {
    this.duration = delay;
    this.texture = sprite.getTexture();
    timer = 0f;
    isComplete = false;
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
      if( this.texture != null ) this.texture.dispose();
      this.texture = null;
    }
  }

  @Override
  public boolean isComplete() {
    return this.isComplete;
  }
}
