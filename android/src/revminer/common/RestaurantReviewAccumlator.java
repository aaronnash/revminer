/**
 * ResturantReviewCategory represents a categorization for review attributes of
 * a restaurant.
 * 
 * 
 */
package revminer.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RestaurantReviewAccumlator {
  private HashMap<ReviewCategory, List<RestaurantAttribute>> categories;
  
  public RestaurantReviewAccumlator() {
    categories = new HashMap<ReviewCategory, List<RestaurantAttribute>>(); 
    for (ReviewCategory cat : ReviewCategory.values()) {
      categories.put(cat, new ArrayList<RestaurantAttribute>());
    }
  }

	public void accumlate(ReviewCategory cat, RestaurantAttribute attr) {
	  if (categories == null) {
	    throw new IllegalStateException("accumlate cannot be called after build");
	  }
	  
	  categories.get(cat).add(attr);
	}

	public HashMap<ReviewCategory, List<RestaurantAttribute>> build() {
	  HashMap<ReviewCategory, List<RestaurantAttribute>> temp = categories;
	  categories = null;
	  return temp;
	}
}
