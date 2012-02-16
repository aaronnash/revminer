package revminer.ui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import revminer.common.SearchHistory;
import revminer.service.RevminerClient;
import revminer.service.SearchListener;
import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class HistoryActivity extends ListActivity {
	private SearchHistoryAdapter adapter;
	private List<SearchHistory> history;
	private static final String HISTORY_FILENAME = "search_history";

// Not saving history here anymore because of the unconventional way Android handles processes
//	@Override
//	public void onDestroy() {
//		saveHistory();
//		super.onDestroy();
//	}
	
	
	private String getHistoryFilename() {
		return getFilesDir() + HISTORY_FILENAME;
	}
	
	// Save search history to the filesystem
	private void saveHistory() {
	    BufferedWriter buf;
	    try {
	      buf = new BufferedWriter(
	    	  // truncate any existing history file
	          new FileWriter(getHistoryFilename(), false));

	      for (SearchHistory hist : history) {
	    	String line = hist.serialize() + "\n";
	    	Log.d("history.save", line);
	        buf.write(line);
	      }

	      buf.close();
	    } catch (IOException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	    }		
	}
	
	private ArrayList<SearchHistory> loadHistory() {
		ArrayList<SearchHistory> result = new ArrayList<SearchHistory>();

		// Load history from prior session
		try {
			BufferedReader reader =
					new BufferedReader(new FileReader(getHistoryFilename()));
			String line;
			while ((line = reader.readLine()) != null) {
				Log.d("history.load", line);
				result.add(SearchHistory.deserialize(line));
			}
		} catch (FileNotFoundException e) {
			// No history yet -- no error
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.history);
    
    history = loadHistory();

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
    lv.setOnItemClickListener(adapter);
  }
  
  /**
   * Private ArrayAdapter used to display a list of {@link SearchHistory}
   * objects within a {@link ListAdapter}
   *
   */
  private class SearchHistoryAdapter extends ArrayAdapter<SearchHistory>
      implements SearchListener, OnItemClickListener {
    // List of SearchHistory items
    private final List<SearchHistory> items;

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
                      if (queryText != null) {
                    	  String display = sh.getFriendlyName();
                    	  if (display == null)
                    		  display = sh.getQuery();
                        queryText.setText(display);
                      }
                      if(queryTime != null)
                        queryTime.setText(sh.getWhenStr());
                }
              return view;
      }

    public void onSearch(String query, String friendlyName) {
    	Log.d("history.onsearch", query);
    	
      // Remove the last element if the list is full
      if (items.size() >= MAX_HISTORY) {
        items.remove(items.size());
      }

      SearchHistory entry = new SearchHistory(query, friendlyName);
      int i = items.indexOf(entry);

      // if there is already an entry for the query, remove it
      if (i >= 0) {
        items.remove(i);
      }

      insert(entry, 0);
      
      // saving history here as a quick fix given the odd way Android handles process closures (and associated events)
      // TODO: this sort of sucks -- file IO shouldn't be done in the UI thread!
      saveHistory();
      
      notifyDataSetChanged();
    }
    
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    	if (position > items.size())
    		return;
  
    	RevminerClient.Client().sendSearchQuery(items.get(position).getQuery(), items.get(position).getFriendlyName());
    }
  }
}