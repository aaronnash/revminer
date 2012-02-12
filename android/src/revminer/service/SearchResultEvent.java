/**
 * SearchResultEvent is an object that describes a set of new search results.
 * It is normally used to notified interested parties of a response to a prior
 * search query.
 */
package revminer.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import revminer.common.Restaurant;

public class SearchResultEvent {
  private final Exception error;
  private final List<Restaurant> restaurants;

  public SearchResultEvent(Exception error) {
    this(error, null);
  }

  public SearchResultEvent(List<Restaurant> resturants) {
    this(null, resturants);
  }

  private SearchResultEvent(Exception error, List<Restaurant> resturants) {
    this.error = error;
    this.restaurants = new ArrayList<Restaurant>(resturants); // Defensive copy
  }

  public Exception getError() {
    return error;
  }

  public boolean hasError() {
    return error != null;
  }
  
  /**
   * Requires that hasError() == false
   * 
   * @return {@link List<Restaurant>} of all restaurants in the search results 
   */
  public List<Restaurant> getResturants() {
    if (hasError()) {
      throw new IllegalStateException("hasError()");
    }

    // Clients should not modify internal list
    return Collections.unmodifiableList(restaurants);
  }
}
