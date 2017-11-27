package io.gitub.ticklemonster.robots.Actions;

import com.badlogic.gdx.utils.Array;

public class ParallelAction implements Action{
  Array<Action> actions;
  boolean isComplete;

  public ParallelAction() {
    this.actions = new Array<Action>();
    this.isComplete = false;
  }

  public ParallelAction( Action action ) {
    this.actions = new Array<Action>();
    this.actions.add(action);
    this.isComplete = false;
  }

  public ParallelAction( Array<Action> actionarray ) {
    this.actions = actionarray;
    this.isComplete = false;
  }

  public ParallelAction( Action[] actionarray ) {
    this.actions = new Array<Action>(actionarray);
    this.isComplete = false;
  }

  public ParallelAction( Action a1, Action a2 ) {
    this.actions = new Array<Action>(2);
    this.actions.add(a1);
    this.actions.add(a2);
    this.isComplete = false;
  }

  public ParallelAction( Action a1, Action a2, Action a3 ) {
    this.actions = new Array<Action>(3);
    this.actions.add(a1);
    this.actions.add(a2);
    this.actions.add(a3);
    this.isComplete = false;
  }


  public int add( Action a ) {
    this.actions.add(a);
    return this.actions.size;
  }

  @Override
  public void reset() {
    this.isComplete = false;
    for( Action a : this.actions ) { a.reset(); }
  }


  @Override
  public void update( float delta ) {
    if( isComplete ) return;

    int nComplete = 0;
    for( Action act : actions ) {
      if( act.isComplete() ) {
        nComplete++;
      } else {
        act.update(delta);
      }
    }

    if( nComplete == actions.size ) {
      // all actions are complete!
      isComplete = true;
    }
  }

  @Override
  public boolean isComplete() {
    return this.isComplete;
  }

  public int getSize() {
    return this.actions.size;
  }

  public void clear() {
    this.actions.clear();
    this.isComplete = false;
  }

}
