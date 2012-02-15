package revminer.ui;

import java.util.ArrayList;
import java.util.List;

import revminer.common.SearchHistory;
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

public class HistoryActivity extends ListActivity {
	
	private SearchHistoryAdapter adapter;
	
	private class SearchHistoryAdapter extends ArrayAdapter<SearchHistory> implements SearchListener {
	
	    private List<SearchHistory> items;
	    
		public static final int MAX_HISTORY = 20;
	
	    public SearchHistoryAdapter(Context context, int textViewResourceId, List<SearchHistory> list) {
	            super(context, textViewResourceId, list);
	            this.items = list;
	    }
	
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	            View view = convertView;
	            if (view == null) {
	                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	                view = vi.inflate(R.layout.history_item, null);
	            }
	            SearchHistory sh = items.get(position);
	            if (sh != null) {
	                    TextView queryText = (TextView) view.findViewById(R.id.querytext);
	                    TextView queryTime = (TextView) view.findViewById(R.id.querytime);
	                    if (queryText != null)
	                    	queryText.setText(sh.getQuery());
	                    if(queryTime != null)
	                    	queryTime.setText(sh.getWhenStr());
                }
	            return view;
	    }
	    
		public void onSearch(String query) {
			
	        // TODO: better handle existing items in search history (just bring them up to the most recent)
	        // TODO: truncate history at MAX_HISTORY items
			insert(new SearchHistory(query), 0);
		    notifyDataSetChanged();
		}
  }
	
	
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
	// initialize history with some sample searches
	// TODO: make search histories properly persistent between sessions...
    List<SearchHistory> history = new ArrayList<SearchHistory>();
    history.add(new SearchHistory("margaritas"));
    history.add(new SearchHistory("good dim sum"));
    history.add(new SearchHistory("agua verde cafe"));
    history.add(new SearchHistory("cheap indian food"));
    history.add(new SearchHistory("mexican"));
    history.add(new SearchHistory("free parking friendly staff fresh fish"));
    
    adapter = new SearchHistoryAdapter(this, R.layout.history_item, history);
    
    ListView lv = getListView();
    
    // TODO: still working to make the full background of this tab white
    // TODO: still working to define text for this list when it is empty
//    // NOTE: the footer must be added before calling setListAdapter(...)
//    View v = getLayoutInflater().inflate(R.layout.history_footer, null);
//    //lv.addFooterView(v);
//    lv.addFooterView(v, null, false);
    
    setListAdapter(adapter);
    RevminerClient.Client().addSearchListener(adapter);

    lv.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    	  RevminerClient.Client().sendSearchQuery(((TextView)view.findViewById(R.id.querytext)).getText().toString(), getApplicationContext());
      }
    });   
  }
}