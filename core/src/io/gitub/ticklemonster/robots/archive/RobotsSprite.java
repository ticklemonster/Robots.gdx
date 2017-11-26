package io.gitub.ticklemonster.robots;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Interpolation;

class RobotsSprite extends Sprite {
  int level = 0;
  boolean isAnimating = false;
  float animationDuration = 1.0f;
  float animationTime = 0f;
  Interpolation lerper = Interpolation.linear;

  float fromX, fromY, toX, toY;
  float fromR, toR, fromA, toA; // TODO: Alpha and Rotation too

  RobotsSprite() {
    // creates an empty rectangle
    super();
    level = 0;
    isAnimating = false;
  }

  RobotsSprite(Sprite sprite) {
    // Constructs a new rectangle with the given corner point in the bottom left, with dimensions and lifetime
    super(sprite);
    this.level = 0;
    this.isAnimating = false;

    //Gdx.app.debug("RobotsSprite","created from sprite: x=" + getX() + " y=" + getY() + " w=" + getWidth() + " h=" + getHeight() );
  }

  RobotsSprite(Sprite sprite, float x, float y, float width, float height, int level) {
    // Constructs a new rectangle with the given corner point in the bottom left and dimensions.
    super(sprite);
    this.setBounds(x,y,width,height);
    this.level = level;
    this.isAnimating = false;

    //Gdx.app.debug("RobotsSprite","created with x=" + x + " y=" + y + " w=" + width + " h=" + height );
  }

  RobotsSprite(Sprite sprite, Rectangle rect, int level) {
    // Constructs a new rectangle with the given corner point in the bottom left and dimensions.
    super(sprite);
    this.setBounds(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight() );
    this.level = level;
    this.isAnimating = false;

    //Gdx.app.debug("RobotsSprite","created with rect=" + rect.toString() );
  }


  RobotsSprite(RobotsSprite other) {
    super((Sprite)other);
    this.level = other.level;
  }


  public void setLevel( int life ) {
    this.level = life;
  }

  public int getLevel() {
    return this.level;
  }

  public Rectangle getCollisionRect() {
    return new Rectangle( this.getX(), this.getY(), this.getWidth(), this.getHeight() );
  }

  public boolean overlaps( RobotsSprite other ) {
    return this.getCollisionRect().overlaps(other.getCollisionRect());
  }

  public boolean overlaps( Rectangle rect ) {
    return this.getCollisionRect().overlaps(rect);
  }

  public boolean isAnimating() {
    return this.isAnimating;
  }

  @Override
  public void setPosition( float x, float y ) {
    super.setPosition(x,y);
    this.fromX = x;
    this.fromY = y;
    this.toX = x;
    this.toY = y;
    isAnimating = false;
  }

  @Override
  public void setRotation( float r ) {
    super.setRotation(r);
    this.fromR = r;
    this.toR = r;
    isAnimating = false;
  }

  public void setInterpolation( Interpolation i ) {
    this.lerper = i;
  }

  public void resetInterpolation() {
    this.lerper = Interpolation.linear;
  }

  public void animateTo( float x, float y, float time ) {
    this.fromX = this.getX();
    this.fromY = this.getY();
    this.toX = x;
    this.toY = y;
    this.animationDuration = time;
    this.isAnimating = true;
    if( this.lerper == null ) this.lerper = Interpolation.linear;
    this.animationTime = 0f;
  }

  public void animateRotation( float r1, float r2, float time ) {
    this.fromR = r1;
    this.toR = r2;
    this.animationTime = time;
    this.isAnimating = true;
    if( this.lerper == null ) this.lerper = Interpolation.linear;
    this.animationTime = 0f;
  }

  @Override
  public void draw( Batch batch ) {
    // do we need to move?
    if( isAnimating ) {
        animationTime += Gdx.graphics.getDeltaTime();
        if( animationTime < animationDuration ) {
          float animationPct = animationTime/animationDuration;
          super.setPosition(
            lerper.apply(fromX,toX,animationPct),
            lerper.apply(fromY,toY,animationPct)
          );
          super.setRotation(
            lerper.apply(fromR,toR,animationPct)
          );
        } else {
          super.setPosition(toX, toY);
          super.setRotation( toR );
          fromX = toX;
          fromY = toY;
          fromR = toR;
          isAnimating = false;
          lerper = null;
        }
    }

    // do the draw
    super.draw(batch);

  }
}
