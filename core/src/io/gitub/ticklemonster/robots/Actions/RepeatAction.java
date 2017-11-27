package io.gitub.ticklemonster.robots.Actions;

public class RepeatAction implements Action {
  Action action;
  int loops, counter;
  boolean isComplete;

  public RepeatAction( Action a, int times ) {
    this.action = a;
    this.loops = times;
    this.counter = 0;
    this.isComplete = false;
  }

  @Override
  public void reset() {
    this.isComplete = false;
    this.counter = 0;
  }

  @Override
  public void update( float delta ) {
    if( isComplete ) return;

    this.action.update(delta);
    if( this.action.isComplete() ) {
      this.counter++;
      if( this.counter < this.loops ) {
        this.action.reset();
      } else {
        this.isComplete = true;
      }
    }
  }

  @Override
  public boolean isComplete() {
    return this.isComplete;
  }

  public void cancel() {
    this.isComplete = true;
  }

}
