package revminer.ui;

import revminer.service.GPSClient;
import revminer.service.RevminerClient;
import revminer.service.SearchListener;
import revminer.service.SearchResultEvent;
import revminer.service.SearchResultListener;
import revminer.ui.R;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class MainActivity extends TabActivity implements SearchResultListener, SearchListener {
	
	private EditText searchBox;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		GPSClient.create((LocationManager)this.getSystemService(Context.LOCATION_SERVICE));
		
		searchBox = (EditText)findViewById(R.id.searchBox);
		
		createTab("History", HistoryActivity.class);
		createTab("Results", ResultsActivity.class);
		createTab("Map", ViewMapActivity.class);
		getTabHost().setCurrentTabByTag("History");
		
		searchBox.setOnEditorActionListener(new OnEditorActionListener() {
	        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
	        	if (actionId == EditorInfo.IME_ACTION_DONE)
	                search(v);
	            return false;
	        }
	    });
		
		RevminerClient.Client().addSearchResultListener(this);
		RevminerClient.Client().addSearchListener(this);
		
		Log.d("revd", "MainActivity.onCreate()");
	}
	
	private void createTab(String label, Class<?> contentActivity) {
		View tabview = LayoutInflater.from(getTabHost().getContext()).inflate(R.layout.tab, null);
		
		((TextView)tabview.findViewById(R.id.tabsText)).setText(label);
		
		TabSpec spec = getTabHost().newTabSpec(label);
		spec.setIndicator(tabview);
		Intent intent = new Intent().setClass(this, contentActivity);
		spec.setContent(intent);
		
		getTabHost().addTab(spec);
	}
	
	public void search(View view)
	{
		if (searchBox.getText().toString().isEmpty()) 
			return;
		
		RevminerClient.Client().sendSearchQuery(searchBox.getText().toString());
	}

	public void onSearchResults(SearchResultEvent e) {
		if (e.hasError()) {
			Toast.makeText(getApplicationContext(), "search error", Toast.LENGTH_SHORT).show();
		} else if (e.getResturants().isEmpty()) {
			Toast.makeText(getApplicationContext(), "no results found", Toast.LENGTH_SHORT).show();
			getTabHost().setCurrentTabByTag("History");
		} else {
			getTabHost().setCurrentTabByTag("Results");
		}
	}

	public void onSearch(String query, String friendlyName) {
		// hide keyboard from searchBox if it is currently visible
	    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
	    
		searchBox.setText("");
	}
}