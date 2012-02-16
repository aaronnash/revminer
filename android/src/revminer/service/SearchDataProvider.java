package revminer.service;

public interface SearchDataProvider {
  /**
   * Registers an event listen that is notified when new search results are
   * returned from the server
   *
   * @param listener {@link SearchResultListener} to be notified when new search
   *    results are available
   */
  public void addSearchResultListener(SearchResultListener listener);

  /**
   * Registers an event listen that is notified when an exact match is returned
   * fromthe server
   *
   * @param listener {@link ExactMatchListener} to be notified an exact match
   *    is available
   */
  public void addExactMatchListener(ExactMatchListener listener);

  /**
   * @return Last {@link SearchResultEvent} fired, or null if none.
   */
  public SearchResultEvent getLastSearchResultEvent();

  public void addSearchListener(SearchListener listener);

  /**
   * Sends a search query to a remote service for processing. Upon return of 
   * results all registered SearchResultListerns are notified.
   * 
   * @param query
   * @return true when query is successfully sent (does not guarantee any
   *     response will be returned). false when query fails to send.
   */
  public boolean sendSearchQuery(String query);
}
