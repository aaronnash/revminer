package revminer.ui;

import revminer.service.SearchResultEvent;
import revminer.service.SearchResultListener;
import android.os.Bundle;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class ViewMapActivity extends MapActivity implements SearchResultListener {
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.map);

    MapView mapView = (MapView) findViewById(R.id.mapview);
    mapView.setBuiltInZoomControls(true);
  }

  @Override
  protected boolean isRouteDisplayed() {
    // TODO Auto-generated method stub
    return false;
  }

  public void onSearchResults(SearchResultEvent e) {
    // TODO: Paint search results on map
  }

}
