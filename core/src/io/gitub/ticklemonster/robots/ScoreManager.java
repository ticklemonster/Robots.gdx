package io.gitub.ticklemonster.robots;

/**
 * The ScoreManager takes care of scoring logic
 *
 * ScoreManager is stateful. It should be updated with the number of kills
 * made once per round using postKills(nkills).
 *
 * The current score and multiplier are available via getScore() and
 * getMultiplier().
 *
 * A rolling score can be displayed using update() to click over the score
 * counter and getDisplayScore() to show a current displayable score.
 *
 * Reset the score manager with reset()
 *
 */
public class ScoreManager {
  static final int[] MULTIPLIER_POINTS = { 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610, 987, 1597, 2584, 4181, 6765, 10946, 17711, 28657, 46368, 75025, 121393, 196418, 317811 };

  // takes care of the score counting and animation logic
  private long score;
  private long displayScore;
  private int  multiplier;
  private int  moveCounter;

  public ScoreManager() {
    this.reset();
  }

  /**
   * Record the number of kills made this round.
   * Should only be called once per round.
   *
   * @param  nkills Number of kills
   * @return points earned for this round
   */
  public long postKills(int nkills) {
    long newscore = 0;

    if( nkills <= 0 ) {
      multiplier = 1;
      moveCounter = 0;
    } else {
      moveCounter++;
      if( multiplier < MULTIPLIER_POINTS.length
          && moveCounter > MULTIPLIER_POINTS[multiplier]) {
        multiplier++;
      }

      long p = 1;
      for( int n=nkills; n>0; ) {
        newscore += Math.min(n,p) * Math.pow(2,p-1);
        n -= p;
        p++;
      }
      //newscore = (long)Math.pow(2,nkills-1);
      newscore *= multiplier;
    }


    score += newscore;
    return newscore;
  }

  /**
   * Update the score display counter for next increment
   * Should be called for every screen refresh
   */
  public void update() {
		if( displayScore >= score ) {
      // no update required
      displayScore = score;
    } else {
      // roll digits that need to be changed
			long diff = score - displayScore;
			long scoreinc = 1;
			for( int digits=0; digits < Math.floor(Math.log10(diff)); digits++ ) {
				scoreinc = scoreinc*10 + 1;
			}
      displayScore += Math.min(scoreinc,diff);
		}
  }

  /**
   * Return the current score
   *
   * @return current score
   */
  public long getScore() {
    return this.score;
  }

  /**
   * Return the current displayable score value.
   * For use with the update() function to have a rolling score counter.
   *
   * @return score to be displayed (may differ from actual score)
   */
  public long getDisplayScore() {
    return this.displayScore;
  }

  /**
   * Return the current score multiplier
   *
   * @return current score multiplier
   */
  public int getMultiplier() {
    return this.multiplier;
  }

  /**
   * Retrun the
  /*
   * Reset the score total and all counters
   */
  public void reset() {
    score = 0;
    displayScore = 0;
    multiplier = 1;
    moveCounter = 1;
  }
}
