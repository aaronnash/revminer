package revminer.service;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import revminer.common.AttributeValue;
import revminer.common.Restaurant;
import revminer.common.RestaurantAttribute;
import revminer.common.RestaurantReviewAccumlator;
import revminer.common.RestaurantReviews;
import revminer.common.ReviewCategory;
import android.text.Html;
import android.util.Log;

// singleton
public class RevminerClient implements SearchDataProvider {
  private static final String REST_URL = "http://cse454.local-box.org:3000/revminer/";
  private static final int MAX_RESULTS = 10;

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
	
	public boolean sendSearchQuery(String query) {
		return sendSearchQuery(query, query);
	}
	
	public boolean sendSearchQuery(String query, String friendlyName) {
        for (SearchListener listener : searchListeners)
        	listener.onSearch(query, friendlyName);

        // TODO: Lets add some parallelism
        Log.d("revd", "Query for: " + query);
        String result = SimpleHttpClient.get(
            REST_URL + URLEncoder.encode(query).replace("+", "%20"));
        if (result == null) {
          Log.d("revd", "null result");
          notifySearchResultEvent(new SearchResultEvent(new Exception("No response from server")));
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
              while (places.hasNext() && res.size() < MAX_RESULTS) { // MAX 10 results
                String place = places.next();
                Log.d("revd", place);

                Restaurant restaurant = Restaurant.getInstance(place);
                if (restaurant == null) { // don't have it cached, we need to make an instance
                  restaurant = Restaurant.createInstance(place, getAttributeMapping(place, meta.getJSONObject(place)));
                }
                res.add(Restaurant.getInstance(place));
              }
            }

            notifySearchResultEvent(new SearchResultEvent(res));
            return true;
          } else if (object.has("match")) {
            //TODO: Finish code to parse matched object 
            Log.d("revd", "Got full match back");

            JSONObject match = object.getJSONObject("match");
            if (match.has("place") && match.has("data") && match.has("meta")
                && match.has("attributeCategories")) {
              Log.d("revd", "All categories exist");

              String name = match.getString("place");
              Restaurant restaurant = Restaurant.getInstance(name);
              // If we don't have the restaurant and reviews cache we need to 
              // create a new instance
              if (restaurant == null || restaurant.getReviews() == null) {
                JSONObject meta = match.getJSONObject("meta");
                JSONArray data = match.getJSONArray("data");
                JSONObject polarities = match.getJSONObject("polarities");
                JSONObject attributeCategories =
                    match.getJSONObject("attributeCategories");
  
                RestaurantReviewAccumlator accum = new RestaurantReviewAccumlator();
  
                for (int i = 0; i < data.length(); i++) {
  
                  JSONArray cur = data.getJSONArray(i);
                  String attribute = cur.getString(0);
                  JSONObject values = cur.getJSONObject(1);
                  String category = attributeCategories.getString(attribute);
  
                  List<AttributeValue> attrValues = new ArrayList<AttributeValue>();
  
                  Iterator<String> itr = values.keys();
                  while (itr.hasNext()) {
                    String value = itr.next();
                    Double polarity = polarities.getDouble(value);
                    attrValues.add(AttributeValue.create(value,  polarity));
                  }
  
                  RestaurantAttribute attr = new RestaurantAttribute(attribute, attrValues);
                  accum.accumlate(ReviewCategory.fromName(category), attr);
                }

                RestaurantReviews reviews = new RestaurantReviews(accum);
                Log.d("revd", reviews.toString());
                restaurant = Restaurant.replaceInstance(name, getAttributeMapping(name, meta), reviews);
              }
              notifyExactMatchEvent(restaurant);
              return true;
            }
          }

        } catch (JSONException e) {
          notifySearchResultEvent(new SearchResultEvent(e));
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        // !

    notifySearchResultEvent(new SearchResultEvent(new ArrayList<Restaurant>(0)));
		return true;
	}
	
	 private static final HashMap<String, String> getAttributeMapping(String place, JSONObject attributes) {
	    HashMap<String, String> attributeMapping;
	    
	    try {
	      attributeMapping = new HashMap<String, String>();

	      Iterator<String> attributesIter = attributes.keys();
	      while (attributesIter.hasNext()) {
	        String attr = attributesIter.next();
	        String value = Html.fromHtml(attributes.getString(attr)).toString();
	        attributeMapping.put(attr,  value);                  
	      }
	      return attributeMapping;
	    } catch (JSONException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	      return null;
	    }
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