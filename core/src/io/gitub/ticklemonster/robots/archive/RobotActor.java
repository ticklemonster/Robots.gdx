package io.gitub.ticklemonster.robots;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;


public class RobotActor extends Actor {
	TextureRegion region;

	public RobotActor ( TextureRegion region ) {
    super();
	  this.region = region;
    this.setBounds(0,0,region.getRegionWidth(),region.getRegionHeight());
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		Color color = getColor();
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		batch.draw(region, getX(), getY(), getOriginX(), getOriginY(),
			getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
	}

}
