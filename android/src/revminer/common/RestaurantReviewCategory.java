/**
 * ResturantReviewCategory represents a categorization for review attributes of
 * a restaurant.
 * 
 * 
 */
package revminer.common;

import revminer.common.RestaurantAttribute.AttributeRating;

public class RestaurantReviewCategory {
	public enum ReviewCategory {
		REVIEW_FOOD,
		REVIEW_SERVICE,
		REVIEW_DECORE,
		REVIEW_OVERALL,
		REVIEW_OTHER,
	};
	
	/*
	 * @return The {@link ReviewCategory} type of this category
	 */
	public ReviewCategory getReviewCategory() {
	  throw new RuntimeException("Not Implemented");
	}
	
	/*
	 * @return Total number of unique attributes in this review category
	 */
	public int getAttributeCount() {
		throw new RuntimeException("Not Implemented");
	}
	
	/*
	 * @return A {@link List<ResturantAttribute>} of all attributes for the 
	 *     specified {@link AttributeRating} 
	 */
	public int getAttributes(AttributeRating rating) {
		throw new RuntimeException("Not Implemented");
	}
}
