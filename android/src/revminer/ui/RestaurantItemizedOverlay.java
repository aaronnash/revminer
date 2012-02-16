package revminer.ui;

import java.util.ArrayList;
import java.util.List;

import revminer.service.RevminerClient;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;

public class RestaurantItemizedOverlay extends ItemizedOverlay<RestaurantOverlayItem> {
  private List<RestaurantOverlayItem> mOverlays = new ArrayList<RestaurantOverlayItem>();
  private final Context mContext;

  public RestaurantItemizedOverlay(Drawable defaultMarker, Context context) {
    super(boundCenterBottom(defaultMarker));
    mContext = context;
  }


  public void addOverlay(RestaurantOverlayItem overlay) {
    mOverlays.add(overlay);
    populate();
  }

  @Override
  protected RestaurantOverlayItem createItem(int i) {
    return mOverlays.get(i);
  }

  @Override
  public int size() {
    return mOverlays.size();
  }

  @Override
  protected boolean onTap(int index) {
    final RestaurantOverlayItem item = mOverlays.get(index);
    AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
    dialog.setTitle(item.getTitle());
    dialog.setMessage(item.getSnippet());
    dialog.setCancelable(true);
    dialog.setPositiveButton("View", new OnClickListener() {
        public void onClick(DialogInterface arg0, int arg1) {
          RevminerClient.Client().sendSearchQuery(item.getRestaurant().getUniqueId());
        }});
    dialog.setNegativeButton("Cancel", new OnClickListener() {
        public void onClick(DialogInterface arg0, int arg1) {}});
    dialog.show();
    return true;
  }
}
