package revminer.common;

import java.util.HashMap;
import java.util.List;


public class RestaurantReviews {
  private final HashMap<ReviewCategory, List<RestaurantAttribute>> categories;

  public RestaurantReviews(RestaurantReviewAccumlator accum) {
    categories = accum.build();
  }

  /**
   * 
   * @param category {@link ReviewCategory} specifying the category of reviews
   *    to return. 
   * @return A {@link List<RestaurantAttribute>} that contains all the review
   * attributes for the specified category.
   */
	public List<RestaurantAttribute> getReviewCategory(ReviewCategory category) {
	  return categories.get(category);
	}

	/**
   * Returns a string representation of the object for debugging purposes.
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (ReviewCategory cat : categories.keySet()) {
      sb.append("{\"" + cat + "\": ");
      for (RestaurantAttribute attr : categories.get(cat)) {
        sb.append(attr.toString());
        sb.append(", ");
      }
  
      if (categories.get(cat).size() > 0) {
        sb.delete(sb.length() - 2, sb.length());
      }
  
      sb.append("}\n");
    }

    return sb.toString();
  }
}
