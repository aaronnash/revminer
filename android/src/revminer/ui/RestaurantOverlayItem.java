package revminer.ui;

import revminer.common.Restaurant;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class RestaurantOverlayItem extends OverlayItem {
  private final Restaurant restaurant;
  public RestaurantOverlayItem(GeoPoint point, String title, String snippet,
      Restaurant restaurant) {
    super(point, title, snippet);
    this.restaurant = restaurant;
  }

  public Restaurant getRestaurant() {
    return restaurant;
  }
}
