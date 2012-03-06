package revminer.ui;

import java.util.ArrayList;
import java.util.List;

import revminer.common.Restaurant;
import revminer.common.RestaurantLocation;
import revminer.service.GPSClient;
import revminer.service.RevminerClient;
import revminer.service.SearchResultEvent;
import revminer.service.SearchResultListener;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class ViewMapActivity extends MapActivity
    implements SearchResultListener {
  private static final double DEG_TO_MICRODEG = 1000000d;
  private static final double PADDING = 1.25;

  private RestaurantItemizedOverlay currentResultsOverlay = null;
  private MyLocationOverlay myLocOverlay; 

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.map);

    MapView mapView = (MapView) findViewById(R.id.mapview);
    mapView.setBuiltInZoomControls(true);

    // Set up to listen to events
    RevminerClient.Client().addSearchResultListener(this);

    // Initialize my location overlay and display it
    myLocOverlay = new MyLocationOverlay(this, mapView);
    mapView.getOverlays().add(myLocOverlay);

    // Load the most recent results if they exist
    SearchResultEvent event =
        RevminerClient.Client().getLastSearchResultEvent();
    if (event != null) {
      onSearchResults(event);
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    myLocOverlay.enableMyLocation();
  }

  @Override
  public void onPause() {
    super.onPause();
    myLocOverlay.disableMyLocation();
  }

  @Override
  protected boolean isRouteDisplayed() {
    // TODO Auto-generated method stub
    return false;
  }

  synchronized public void onSearchResults(SearchResultEvent e) {
    if (e.hasError()) { // Don't do anything if there was an error
      return;
    }

    MapView mapView = (MapView) findViewById(R.id.mapview);
    MapController mapController = mapView.getController();

    if (!e.isExactMatch() && e.getResturants().isEmpty()) { // clear map on empty results
      mapView.getOverlays().clear();
      return;
    }

    // List of restaurants to display
    List<Restaurant> restaurants;
    if (e.isExactMatch()) {
      restaurants = new ArrayList<Restaurant>();
      restaurants.add(e.getExactMatch());
    } else {
      restaurants = e.getResturants();
    }

    // Current location
    Location myLoc = GPSClient.Client().getLocation();

    Drawable mapMarker = this.getResources().getDrawable(R.drawable.map_pointer);
    RestaurantItemizedOverlay itemizedOverlay = new RestaurantItemizedOverlay(mapMarker, this);

    // We'll use these to find the appropriate bounding box for the map
    int minLat = myLoc != null ? (int)(myLoc.getLatitude() * DEG_TO_MICRODEG) : Integer.MAX_VALUE;
    int maxLat = myLoc != null ? (int)(myLoc.getLatitude() * DEG_TO_MICRODEG) : Integer.MIN_VALUE;
    int minLon = myLoc != null ? (int)(myLoc.getLongitude() * DEG_TO_MICRODEG) : Integer.MAX_VALUE;
    int maxLon = myLoc != null ? (int)(myLoc.getLongitude() * DEG_TO_MICRODEG) : Integer.MIN_VALUE;

    for (Restaurant restaurant : restaurants) {
      RestaurantLocation loc = restaurant.getLocation();
      int latitude = (int)(loc.getLatitude() * DEG_TO_MICRODEG);
      int longitude = (int)(loc.getLongitude() * DEG_TO_MICRODEG);
      GeoPoint point = new GeoPoint(latitude, longitude);

      maxLat = Math.max(latitude, maxLat);
      minLat = Math.min(latitude, minLat);
      maxLon = Math.max(longitude, maxLon);
      minLon = Math.min(longitude, minLon);

      RestaurantOverlayItem overlayItem = new RestaurantOverlayItem(
          point, restaurant.getName(), loc.getStreetAddress(), restaurant);
      itemizedOverlay.addOverlay(overlayItem);
    }

    if (currentResultsOverlay != null) {
      mapView.getOverlays().remove(currentResultsOverlay); // Remove the old markers
    }
    mapView.getOverlays().add(itemizedOverlay); // Add  the new markers
    currentResultsOverlay = itemizedOverlay;

    // Reposition the map to show all the current results
    mapController.zoomToSpan((int)(Math.abs(maxLat - minLat) * PADDING),
        (int)(Math.abs(maxLon - minLon) * PADDING));
    mapController.animateTo(new GeoPoint( (maxLat + minLat)/2, 
        (maxLon + minLon)/2 )); 
  }

}
