package revminer.ui;

import java.util.ArrayList;
import java.util.List;

import revminer.common.Restaurant;
import revminer.service.GPSClient;
import revminer.service.RevminerClient;
import revminer.service.SearchResultEvent;
import revminer.service.SearchResultListener;
import revminer.ui.R;
import android.app.ListActivity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ResultsActivity extends ListActivity {
	
	private ResultsAdapter adapter;
	
	private class ResultsAdapter extends ArrayAdapter<Restaurant> implements SearchResultListener, OnItemClickListener {
	
	    private List<Restaurant> items;
	
	    public ResultsAdapter(Context context, int textViewResourceId, List<Restaurant> list) {
	            super(context, textViewResourceId, list);
	            this.items = list;
	    }
	    
	    
	    
	
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	            View view = convertView;
	            if (view == null) {
	                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	                view = vi.inflate(R.layout.result_item, null);
	            }
	            Restaurant r = items.get(position);
	            if (r != null) {
	                    TextView restaurantName = (TextView) view.findViewById(R.id.restaurantName);
	                    TextView status = (TextView) view.findViewById(R.id.status);
	                    TextView price = (TextView) view.findViewById(R.id.price);
	                    TextView distance = (TextView) view.findViewById(R.id.distance);
	                    TextView distanceSuffix = (TextView) view.findViewById(R.id.distanceSuffix);
	                    
//	                    Log.d("results.getView", r.getName());
	                    
	                    restaurantName.setText(r.getName());
	                    
	                    // TODO: properly determine open hours
//	                    String hours = r.getAttributes().get("Hours");
//	                    if (hours == null)
//	                    	hours = "";
//	                    status.setText(hours);
	                    status.setText("");
	                    
	                    String priceRange = r.getAttributes().get("Price Range");
	                    if (priceRange == null)
	                    	priceRange = "";
	                    price.setText(priceRange);
	                    
	                    Location curLocation = GPSClient.Client().getLocation();
	                    
	                    if (curLocation == null || r.getLocation() == null) {
	                    	distance.setText("");
	                    	distanceSuffix.setText("");
	                    } else {
	                    	float miles = r.getLocation().getDistance(curLocation);
	                    	
	                      	// round to one decimal place
	                    	miles = (float)Math.round(miles * 10) / 10;
	                    	
	                    	distance.setText(Float.toString(miles));
	                    	distanceSuffix.setText("mi");
	                    }
	                    
	                    
	                    
//	                    TextView polarity1 = (TextView) view.findViewById(R.id.polarity1);
//	                    TextView polarity2 = (TextView) view.findViewById(R.id.polarity2);
//	                    TextView polarity3 = (TextView) view.findViewById(R.id.polarity3);
//	                    TextView polarity4 = (TextView) view.findViewById(R.id.polarity4);
//	                    TextView polarity5 = (TextView) view.findViewById(R.id.polarity5);
//	                    
//	            		REVIEW_FOOD,
//	            		REVIEW_SERVICE,
//	            		REVIEW_DECORE,
//	            		REVIEW_OVERALL,
//	            		REVIEW_OTHER,
	                    
	                    //////RestaurantReviewCategory reviewCategory = r.getReviews().getReviewCategory(RestaurantReviewCategory.ReviewCategory.REVIEW_FOOD);
	                    
	                    
	                    
	                    // TODO: update width of each polarity box
                }
	            return view;
	    }

		public void onSearchResults(SearchResultEvent e) {
	    	items.clear();
	    	
	    	if (e.hasError()) {
	    		notifyDataSetChanged();
	    		return;
	    	}
	    	
	    	StringBuilder sb = new StringBuilder();
	    	sb.append("got ").append(e.getResturants().size()).append(" results");
	    	Log.d("results.onresults", sb.toString());
	    	
	    	// make our own copy of the results
	    	for (Restaurant r : e.getResturants())
	    		items.add(r);
	    	
	        notifyDataSetChanged();
		}
		
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (position > items.size())
				return;

			RevminerClient.Client().sendSearchQuery(items.get(position).getName());
		}
  }
	
	
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.results);
    
	// initialize results
    List<Restaurant> results;
    SearchResultEvent e = RevminerClient.Client().getLastSearchResultEvent();
    if (e == null || e.hasError()) {
    	results = new ArrayList<Restaurant>();
    } else {
    	results = new ArrayList<Restaurant>(e.getResturants());
    }
    
    adapter = new ResultsAdapter(this, R.layout.result_item, results);
    setListAdapter(adapter);
    RevminerClient.Client().addSearchResultListener(adapter);
    getListView().setOnItemClickListener(adapter);
    
  }
}