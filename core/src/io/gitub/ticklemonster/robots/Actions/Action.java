package io.gitub.ticklemonster.robots;

public interface Action {
  public void update(float deltaTime);
  public boolean isComplete();
  public void reset();
}
