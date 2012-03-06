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
  private final Restaurant exactMatch;
  private final List<Restaurant> restaurants;

  public SearchResultEvent(Exception error) {
    this(error, null, null);
  }

  public SearchResultEvent(List<Restaurant> restaurant) {
    this(null, null, restaurant);
  }

  public SearchResultEvent(Restaurant exactMatch) {
    this(null, exactMatch, null);
  }

  private SearchResultEvent(Exception error, Restaurant exactMatch,
      List<Restaurant> resturants) {
    this.error = error;
    this.exactMatch = exactMatch;
    this.restaurants = resturants != null
                          ? new ArrayList<Restaurant>(resturants) // Defensive copy
                          : null;
  }

  /*
   * Static constructor for an error
   */
  public static SearchResultEvent of(Exception e) {
    return new SearchResultEvent(e);
  }

  /*
   * Static constructor for result list
   */
  public static SearchResultEvent of(List<Restaurant> restaurants) {
    return new SearchResultEvent(restaurants);
  }

  /*
   * Static constructor for exact match
   */
  public static SearchResultEvent of(Restaurant exactMatch) {
    return new SearchResultEvent(exactMatch);
  }

  public Exception getError() {
    return error;
  }

  public boolean hasError() {
    return error != null;
  }

  public boolean isExactMatch() {
    return exactMatch != null;
  }

  /**
   * Requires that hasError() == false && isExactMatch() == false
   * 
   * @return {@link List<Restaurant>} of all restaurants in the search results 
   */
  public List<Restaurant> getResturants() {
    if (hasError() || isExactMatch()) {
      throw new IllegalStateException("hasError() || isExactMatch()");
    }

    // Clients should not modify internal list
    return Collections.unmodifiableList(restaurants);
  }

  /**
   * Requires that hasError() == false && isExactMatch() == false
   * 
   * @return A {@link Restaurant} that was determined to be an exact match 
   */
  public Restaurant getExactMatch() {
    if (hasError() || !isExactMatch()) {
      throw new IllegalStateException("hasError() || !isExactMatch()");
    }

    return exactMatch;
  }
}
