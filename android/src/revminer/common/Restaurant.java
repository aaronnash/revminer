package revminer.common;

import java.util.HashMap;

public class Restaurant  {
	private String name;
	
	public Restaurant(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public RestaurantLocation getLocation() {
		throw new RuntimeException("Not Implmented");
	}
	
	public String getPhoneNumber() {
		throw new RuntimeException("Not Implmented");
	}
	
	public HashMap<String, String> getAttributes() {
		throw new RuntimeException("Not Implmented");
	}
	
	public RestaurantReviews getReviews() {
		throw new RuntimeException("Not Implmented");
	}
}
