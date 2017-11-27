package io.gitub.ticklemonster.robots.Actions;

public interface Action {
  void update(float deltaTime);
  boolean isComplete();
  void reset();
}
