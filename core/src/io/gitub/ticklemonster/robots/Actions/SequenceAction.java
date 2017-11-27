package io.gitub.ticklemonster.robots.Actions;

import com.badlogic.gdx.utils.Array;

public class SequenceAction implements Action{
  Array<Action> actions;
  int activeAction;
  boolean isComplete;

  public SequenceAction( Action action ) {
    this.actions = new Array<Action>();
    this.actions.add(action);
    this.activeAction = 0;
    this.isComplete = false;
  }

  public SequenceAction( Array<Action> actionarray ) {
    this.actions = actionarray;
    this.activeAction = 0;
    this.isComplete = false;
  }

  public SequenceAction( Action[] actionarray ) {
    this.actions = new Array<Action>(actionarray);
    this.activeAction = 0;
    this.isComplete = false;
  }

  public SequenceAction( Action a1, Action a2 ) {
    this.actions = new Array<Action>();
    this.actions.add(a1);
    this.actions.add(a2);
    this.activeAction = 0;
    this.isComplete = false;
  }

  public SequenceAction( Action a1, Action a2, Action a3 ) {
    this.actions = new Array<Action>();
    this.actions.add(a1);
    this.actions.add(a2);
    this.actions.add(a3);
    this.activeAction = 0;
    this.isComplete = false;
  }

  public int add( Action a ) {
    this.actions.add(a);
    return this.actions.size;
  }

  @Override
  public void reset() {
    this.isComplete = false;
    this.activeAction = 0;
    for( Action a : this.actions ) { a.reset(); }
  }


  @Override
  public void update( float delta ) {
    if( isComplete ) return;

    Action currentAction = this.actions.get(activeAction);

    currentAction.update(delta);
    if( currentAction.isComplete() ) {
      activeAction++;
      if( activeAction >= actions.size ) isComplete = true;
    }
  }

  @Override
  public boolean isComplete() {
    return this.isComplete;
  }

  public int getSize() {
    return this.actions.size;
  }

  public int getCurrentActionIndex() {
    return this.activeAction;
  }

  public void restart() {
    this.activeAction = 0;
    this.isComplete = false;
  }

  public void clear() {
    this.actions.clear();
    this.activeAction = 0;
    this.isComplete = false;
  }

}
