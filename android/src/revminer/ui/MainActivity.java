package revminer.ui;

import revminer.service.RevminerClient;
import revminer.ui.R;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
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

public class MainActivity extends TabActivity {
	
	private EditText searchBox;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		searchBox = (EditText)findViewById(R.id.searchBox);
		
		createTab("History", HistoryActivity.class);
		createTab("Results", ResultsActivity.class);
		createTab("Map", ViewMapActivity.class);
		getTabHost().setCurrentTabByTag("History");
		
		searchBox.setOnEditorActionListener(new OnEditorActionListener() {
	        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
	        	if (actionId == EditorInfo.IME_ACTION_DONE)
	                search();
	            return false;
	        }
	    });
		
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
	
	// wrapper for search button
	private void search(View view)
	{
		search(null);
	}
	
	private void search()
	{
		if (searchBox.getText().toString().isEmpty())
			return;
		
    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
		RevminerClient.Client().sendSearchQuery(searchBox.getText().toString(), getApplicationContext());
		searchBox.setText("");		
	}
}