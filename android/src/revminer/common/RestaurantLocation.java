package revminer.common;

public class RestaurantLocation {
  private final double latitude;
  private final double longitude;
  private final String streetAddress;
  private final String city;
  private final String state;
  private final int zipCode;

  public RestaurantLocation() { // TODO: replace with functional constructor
    this(30d * Math.random(), 30d * Math.random(), "Addr", "Seattle", "WA");
  }

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
}
