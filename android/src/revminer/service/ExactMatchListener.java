package revminer.service;

import revminer.common.Restaurant;

public interface ExactMatchListener {
  public void onExactMatch(Restaurant restaurant);
}
