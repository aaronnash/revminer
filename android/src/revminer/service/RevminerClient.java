package revminer.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import revminer.common.SearchHistory;

import android.content.Context;
import android.widget.Toast;

// singleton
public class RevminerClient implements SearchDataProvider {
	
	private List<SearchResultListener> resultListeners;
	private List<SearchListener> searchListeners;
	
	private static RevminerClient client;
	
	private RevminerClient() {
		resultListeners = new ArrayList<SearchResultListener>();
		searchListeners = new ArrayList<SearchListener>();
	}
	
	public static RevminerClient Client() {
		if (client == null)
			client = new RevminerClient();
		return client;
	}

	public void addSearchResultListener(SearchResultListener listener) {
		resultListeners.add(listener);
	}
	
	public void addSearchListener(SearchListener listener) {
		searchListeners.add(listener);
	}

	// TODO: remove context from this interface once we actually implement the query logic
	public boolean sendSearchQuery(String query, Context context) {

		// TODO: actually perform query instead of making this little popup
        Toast.makeText(context, "Query: \"" + query +"\"", Toast.LENGTH_SHORT).show();
        
        for (SearchListener listener : searchListeners)
        	listener.onSearch(query);
        
        // TODO: update once true query logic is implemented
		return false;
	}
}