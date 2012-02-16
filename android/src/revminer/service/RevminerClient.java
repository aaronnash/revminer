package revminer.service;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import revminer.common.Restaurant;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

// singleton
public class RevminerClient implements SearchDataProvider {
  private static final String REST_URL = "http://cse454.local-box.org:3000/revminer/";

	private final List<SearchResultListener> resultListeners;
	private final List<ExactMatchListener> matchListeners;
	private final List<SearchListener> searchListeners;

	private SearchResultEvent lastSearchResultEvent;
	private Restaurant lastExactMatch;

	private static RevminerClient client;
	
	private RevminerClient() {
		resultListeners = new ArrayList<SearchResultListener>();
		matchListeners = new ArrayList<ExactMatchListener>();
		searchListeners = new ArrayList<SearchListener>();

		lastSearchResultEvent = null;
		lastExactMatch = null;
	}
	
	public static RevminerClient Client() {
		if (client == null)
			client = new RevminerClient();
		return client;
	}

	public void addSearchResultListener(SearchResultListener listener) {
		resultListeners.add(listener);
	}

  public void addExactMatchListener(ExactMatchListener listener) {
    matchListeners.add(listener);
    
  }

	public void addSearchListener(SearchListener listener) {
		searchListeners.add(listener);
	}

	// TODO: remove context from this interface once we actually implement the query logic
	public boolean sendSearchQuery(String query) {
        for (SearchListener listener : searchListeners)
        	listener.onSearch(query);

        // TODO: Lets add some parallelism
        Log.d("revd", "Query for: " + query);

        String result = SimpleHttpClient.get(
            REST_URL + URLEncoder.encode(query).replace("+", "%20"));
        if (result == null) {
          Log.d("revd", "null result");
          return false;
        }
        Log.d("revd", "results received for " + query);

        // TODO: update once true query logic is implemented
        // TODO: handle case when we get "suggestions" or "match"
        try {
          JSONObject object = (JSONObject) new JSONTokener(result).nextValue();

          if (object.has("results")) {
            List<Restaurant> res = new ArrayList<Restaurant>();
            JSONObject results = object.getJSONObject("results");

            if (results.has("meta") && results.has("data")) {
              JSONObject meta = results.getJSONObject("meta");
              JSONObject data = results.getJSONObject("data");

              Iterator<String> places = data.keys();
              while (places.hasNext()) {
                String place = places.next();
                Log.d("revd", place);

                Restaurant restaurant = Restaurant.getInstance(place);
                if (restaurant == null) { // don't have it cached, we need to make an instance
                  restaurant = getRestaurantFromMeta(place, meta);
                }
                res.add(Restaurant.getInstance(place));
              }
            }

            notifySearchResultEvent(new SearchResultEvent(res));
          } else if (object.has("match")) {
            //TODO: Finish code to parse matched object 
            Log.d("revd", "Got full match back");

            JSONObject match = object.getJSONObject("match");
            if (match.has("place") && match.has("data") && match.has("meta")
                && match.has("attributeCategories")) {
              Log.d("revd", "All categories exist");
              JSONObject meta = match.getJSONObject("meta"); Log.d("revd", "1");
//              JSONObject data = match.getJSONObject("data"); Log.d("revd", "2");
//              JSONObject place = match.getJSONObject("place"); Log.d("revd", "3");
              JSONObject attributeCategories =
                  match.getJSONObject("attributeCategories"); Log.d("revd", "4");

//              Iterator<String> attributes = data.keys(); 
//              while (attributes.hasNext()) {
//                String attribute = attributes.next();
//                String category = "";//attributeCategories.getString(attribute);
//                Log.d("revd", attribute + " => " + category);
//              }
            }
          }

        } catch (JSONException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        // !

		return false;
	}

	private static final Restaurant getRestaurantFromMeta(String name, JSONObject meta) {
	  String key = name;
	  HashMap<String, String> attributeMapping;
	  
    try {
      attributeMapping = new HashMap<String, String>();
      JSONObject attributes = attributes = meta.getJSONObject(key);
      
      Iterator<String> attributesIter = attributes.keys();
      while (attributesIter.hasNext()) {
        String attr = attributesIter.next();
        String value = attributes.getString(attr);
        attributeMapping.put(attr,  value);                  
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }

     return Restaurant.createInstance(key, attributeMapping);
	}

  public SearchResultEvent getLastSearchResultEvent() {
    return lastSearchResultEvent;
  }

  public Restaurant getLastExactMatch() {
    return lastExactMatch;
  }

  private void notifySearchResultEvent(SearchResultEvent e) {
    this.lastSearchResultEvent = e;
    for (SearchResultListener listener : resultListeners) {
      listener.onSearchResults(e);
    }
  }

  private void notifyExactMatchEvent(Restaurant r) {
    this.lastExactMatch = r;
    for (ExactMatchListener listener : matchListeners) {
      listener.onExactMatch(r);
    }
  }
}