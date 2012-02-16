package revminer.common;

import android.location.Location;
import android.util.Log;

public class RestaurantLocation {
  private final double latitude;
  private final double longitude;
  private final String streetAddress;
  private final String city;
  private final String state;
  private final int zipCode;
  
  public static float METERS_PER_MILE = (float)1609.344;

  public RestaurantLocation(double latitude, double longitude,
      String streetAddress, String city, String state) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.streetAddress = streetAddress;
    this.city = city;
    this.state = state;
    state = "WA";
    zipCode = 98105;
  }
	/**
	 * @return The restaurant's latitude in degrees.
	 */
	public double getLatitude() {
		return latitude;
	}
	
	/**
	 * @return The restaurant's longitude in degrees.
	 */
	public double getLongitude() {
		return longitude;
	}
	
	/**
	 * @return The restaurant's street address (e.g. "123 15th Ave NE")
	 */
	public String getStreetAddress() {
		return streetAddress;
	}
	
	/**
	 * @return The restaurant's city (e.g. "Seattle")
	 */
	public String getCity() {
		return city;
	}
	
	/**
	 * @return The restaurant's state (e.g. "WA")
	 */
	public String getState() {
		return state;
	}
	
	/**
	 * @return The restaurant's zip code (e.g. "98105")
	 */
	public int getZipCode() {
		return zipCode;
	}
	
	// TODO: this could be further enhanced to also return the bearing.. but we'll save that for another time...
	// returns distance in miles
	public float getDistance(Location from) {
		float[] results = new float[1];
		
		Location.distanceBetween(latitude, longitude, from.getLatitude(), from.getLongitude(), results);
		
		return results[0] / METERS_PER_MILE;
	}
}
