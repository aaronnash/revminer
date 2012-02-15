package revminer.ui;

import java.util.ArrayList;
import java.util.List;

import revminer.common.Restaurant;
import revminer.service.RevminerClient;
import revminer.service.SearchListener;
import revminer.ui.R;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ResultsActivity extends ListActivity {
	
	private ResultsAdapter adapter;
	
	private class ResultsAdapter extends ArrayAdapter<Restaurant> {
	
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
	                    TextView polarity1 = (TextView) view.findViewById(R.id.polarity1);
	                    TextView polarity2 = (TextView) view.findViewById(R.id.polarity2);
	                    TextView polarity3 = (TextView) view.findViewById(R.id.polarity3);
	                    TextView polarity4 = (TextView) view.findViewById(R.id.polarity4);
	                    TextView polarity5 = (TextView) view.findViewById(R.id.polarity5);
	                    TextView distance = (TextView) view.findViewById(R.id.distance);
	                    
	                    restaurantName.setText(r.getName());
	                    // TODO: update width of each polarity box
	                    // TODO: actually calculate the distance!
	                    distance.setText("0.3");
                }
	            return view;
	    }
  }
	
	
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
	// some sample results...
    List<Restaurant> results = new ArrayList<Restaurant>();
    results.add(new Restaurant("one"));
    results.add(new Restaurant("two"));
    results.add(new Restaurant("three"));
    results.add(new Restaurant("four"));
    results.add(new Restaurant("five"));
    results.add(new Restaurant("six"));
    
    adapter = new ResultsAdapter(this, R.layout.result_item, results);
    
    setListAdapter(adapter);
    
  }
}