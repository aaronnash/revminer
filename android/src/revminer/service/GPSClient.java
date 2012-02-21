package revminer.service;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.lang.IllegalStateException;

// for testing on emulator do the following:
// $ telnet localhost 5554
// $ geo fix -122.331519 47.606698
// this will set your position as being in downtown Seattle

public class GPSClient implements LocationListener {
	
	private LocationManager locationManager;
	private Location curLocation;
	private String provider;
	private static GPSClient singleton;
	
	// TODO: better conserve the users battery with more advanced logic
	private GPSClient(LocationManager locationManager) {
		Log.d("gps.provider.best", locationManager.getBestProvider(new Criteria(), true));
		
		curLocation = null;
		
		// listening to all providers that are enabled
		for (String provider: locationManager.getProviders(true)) {
			Location l = locationManager.getLastKnownLocation(provider);
			if (l == null) {
				Log.d("gps.provider." + provider + ".last", "null");
			} else {
				Log.d("gps.provider." + provider + ".last", l.toString());
				if (curLocation == null || curLocation.getAccuracy() < l.getAccuracy())
					curLocation = l;
			}
			
			// register ourselves as a listener for each provider to receive location updates
			locationManager.requestLocationUpdates(provider, 0, 0, this);
		}
	}
	
	public static GPSClient create(LocationManager locationManager) {
		if (singleton != null) {
			// TODO: better manage how our code responds to screen rotations
			//throw new IllegalStateException();
			return singleton;
		}
		
		singleton = new GPSClient(locationManager);
		
		return singleton;
	}
	
	public static GPSClient Client() {
		if (singleton == null)
			throw new IllegalStateException();
		
		return singleton;
	}
	
	 public void onLocationChanged(Location location) {
		 // TODO: this isn't taking into consideration different accuracies / time since a location update
		 curLocation = location;
		 Log.d("gps.location.changed", curLocation.toString());
	 }
	 
	 // may return null if no location can be gathered
	 public Location getLocation() {
		 return curLocation;
	 }

	 public void onStatusChanged(String provider, int status, Bundle extras) {}

	 public void onProviderEnabled(String provider) {}

	 public void onProviderDisabled(String provider) {}
}