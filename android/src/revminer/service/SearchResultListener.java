/**
 * SearchResultListener is an interface that can be implemented by any object
 * that allows for a universal way to be notified of new search results.
 */
package revminer.service;

public interface SearchResultListener {
  public void onSearchResults(SearchResultEvent e);
}
