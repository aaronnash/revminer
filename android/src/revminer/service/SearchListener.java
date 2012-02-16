/**
 * SearchListener is an interface that can be implemented by any object
 * that allows for a universal way to be notified of new search queries.
 */
package revminer.service;

public interface SearchListener {
	// NOTE: friendlyName may be null
	public void onSearch(String query, String friendlyName);

}