package revminer.common;

public class RestaurantAttribute {
	public enum AttributeRating {
		ATTRIBUTE_EXTREMELY_POSITIVE,
		ATTRIBUTE_POSITIVE,
		ATTRIBUTE_NUETRAL,
		ATTRIBUTE_NEGATIVE,
		ATTRIBUTE_EXTREMLY_NEGATIVE,
	};
	
	public String getName() {
		throw new RuntimeException("Not Implemented");
	}
	
	public int getCount() {
		throw new RuntimeException("Not Implemented");
	}
	
	public AttributeRating getRating() {
		throw new RuntimeException("Not Implemented");
	}
}
