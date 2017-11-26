package io.gitub.ticklemonster.robots;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;


public class SplatActor extends Actor {
	TextureRegion region;
  int level;

	public SplatActor ( TextureRegion region ) {
    super();
	  this.region = region;
    this.setBounds(0,0,region.getRegionWidth(),region.getRegionHeight());
	}

  public void setLevel( int level ) {
    this.level = level;
  }
  public int getLevel() {
    return this.level;
  }

	@Override
	public void draw (Batch batch, float parentAlpha) {
		Color color = getColor();
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		batch.draw(region, getX(), getY(), getOriginX(), getOriginY(),
			getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
	}

}
