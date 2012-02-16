package revminer.common;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

public class Restaurant  {
  private static final String ATTR_NAME = "Business Name";
  private static final String ATTR_LATITUDE = "Latitude";
  private static final String ATTR_LONGITUDE = "Longitude";
  private static final String ATTR_PHONE_NUMBER = "Phone number";
  private static final String ATTR_ADDRESS = "Address";
  private static final String ATTR_CITY = "City";
  
  // We'll cache the results
  // TODO: Implement a size policy
  private static final Map<String, Restaurant> cache = 
      new HashMap<String, Restaurant>();

	private final String name;
	private final String phoneNumber;
	private final RestaurantLocation location;
	private final HashMap<String, String> attributes;
	private final RestaurantReviews reviews;

	public Restaurant(String name) {
    this(name, new RestaurantLocation(), "", new HashMap<String, String>(), null);
  }

	public Restaurant(String name, RestaurantLocation location,
	    String phoneNumber, Map<String, String> attributes) {
	  this(name, location, phoneNumber, attributes, null);
  }

	public Restaurant(String name, RestaurantLocation location,
      String phoneNumber, Map<String, String> attributes,
      RestaurantReviews reviews) {
    this.name = name;
    this.location = location;
    this.phoneNumber = phoneNumber;
    // Create a defensive copy
    this.attributes = new HashMap<String, String>(attributes);
    this.reviews = reviews;
  }

	/**
	 * 
	 * @param name Unique name that references the restaurant
	 * @return The {@link Restaurant} object associated with the given name,
	 *     null if no object is associated with the provided name.
	 */
	 public static Restaurant getInstance(String name) {
	   Log.d("revd", "Getting restaurant instance " + name);
	   return cache.get(name);
	 }

	  /**
    * Creates a new instance of a Restaurant object and associates it with the 
    * provided name. If an instance is already associated with the provided
    * name, the old instance is replace with the newly created instance.
    * 
    * @param name Unique name that references the restaurant
    * @param attributes A mapping of attribute value pairs that contains
    *    important information about the restaurant.
    *    TODO: DOCUMENT THE REQUIRED KEYS
    * @return
    * 
    * TODO: This should probably be part of a factory class.
	  */
	  public static Restaurant replaceInstance(String name,
	      Map<String, String> attributes, RestaurantReviews reviews) {
	    Log.d("revd", "Replacing restaurant instance " + name);
	  // TODO: verify that ATTR_* is indeed an element of attributes
	    RestaurantLocation location;
	    String fullName = attributes.get(ATTR_NAME);
	    String phoneNumber = attributes.get(ATTR_PHONE_NUMBER);

	    // Get the restaurants location
	    if (attributes.containsKey(ATTR_LATITUDE) && attributes.containsKey(ATTR_LONGITUDE)) {
	      double latitude = Double.parseDouble(attributes.get(ATTR_LATITUDE));
	      double longitude = Double.parseDouble(attributes.get(ATTR_LONGITUDE));

	      // Attempt to parse the address in the form of "123 Foo Rd, Seattle, WA"
	      // TODO: Format validation
	      String[] addressParts = attributes.get(ATTR_ADDRESS).split(",");
	      String address = addressParts[0];
	      String state = addressParts[addressParts.length - 1];
	      String city = attributes.get(ATTR_CITY);
	      
	      location = new RestaurantLocation(
	          latitude, longitude, address, city, state);     
	    } else { // No location available
	      Log.d("revd", "No location available for " + name);
	      location = new RestaurantLocation();
	    }

	    // Create the new restaurant object and cache it
	    Restaurant restaurant = new Restaurant(fullName, location, phoneNumber,
	        attributes, reviews);
	    cache.put(name, restaurant);

	    return restaurant;
	   }

	 /**
	  * Creates a new instance of a Restaurant object and associates it with the 
	  * provided name. If an instance is already associated with the provided
	  * name, a reference to the already associated object is returned.
	  * 
	  * @param name Unique name that references the restaurant
	  * @param attributes A mapping of attribute value pairs that contains
	  *    important information about the restaurant.
	  *    TODO: DOCUMENT THE REQUIRED KEYS
	  * @return
	  * 
	  * TODO: This should probably be part of a factory class.
	  */
	public static Restaurant createInstance(String name, Map<String, String> attributes) {
	  Restaurant restaurant = cache.get(name);
	  if (restaurant != null) {
	    Log.d("revd", "Using cached restaurant instance " + name);
	    return restaurant;
	  }

	  return replaceInstance(name, attributes, null);
	}

	public String getName() {
		return name;
	}

	public RestaurantLocation getLocation() {
		return location;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public HashMap<String, String> getAttributes() {
		return attributes;
	}

	/**
	 * 
	 * @return null if no review on record, otherwise a reference to a
	 *     {@link RestaurantReviews} object containing all the reviews for this 
	 *     restaurant.
	 */
	public RestaurantReviews getReviews() {
		return reviews;
	}
}
