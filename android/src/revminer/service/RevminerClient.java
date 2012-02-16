package revminer.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import revminer.common.Restaurant;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

// singleton
public class RevminerClient implements SearchDataProvider {
  private static final String REST_URL = "http://cse454.local-box.org:3000/revminer/";
  private static final String JSON_TEST = "{\"results\":{\"meta\":{\"ristorante-machiavelli-seattle-2\":{\"Attire\":\"Casual\",\"Accepts Credit Cards\":\"Yes\",\"Phone number\":\"(206) 621-7941\",\"Takes Reservations\":\"No\",\"Neighborhood\":\"Capitol Hill\",\"Ambience\":\"Casual\",\"Good For\":\"Dinner\",\"Latitude\":\"47.614966\",\"Business Name\":\"Ristorante Machiavelli\",\"Good for Groups\":\"No\",\"Outdoor Seating\":\"No\",\"Business type\":\"restaurant\",\"Wi-Fi\":\"No\",\"Alcohol\":\"Full Bar\",\"Waiter Service\":\"Yes\",\"Delivery\":\"No\",\"Good for Kids\":\"No\",\"Parking\":\"Street\",\"Hours\":\"Mon-Sat 5 pm - 11 pm\",\"Take-out\":\"Yes\",\"Price Range\":\"$$\",\"City\":\"Seattle\",\"Category\":\"Restaurants, Italian\",\"Noise Level\":\"Average\",\"Wheelchair Accessible\":\"No\",\"Has TV\":\"No\",\"Address\":\"1215 Pine St, Seattle, WA\",\"Longitude\":\"-122.3285881\"},\"the-pink-door-seattle\":{\"Attire\":\"Casual\",\"Accepts Credit Cards\":\"Yes\",\"Phone number\":\"(206) 443-3241\",\"Takes Reservations\":\"Yes\",\"Neighborhood\":\"Downtown\",\"Ambience\":\"Romantic, Casual, Intimate\",\"Good For\":\"Dinner\",\"Latitude\":\"47.6103351\",\"Good for Groups\":\"Yes\",\"Business Name\":\"The Pink Door\",\"Outdoor Seating\":\"Yes\",\"Business type\":\"restaurant\",\"Alcohol\":\"Full Bar\",\"Waiter Service\":\"Yes\",\"Delivery\":\"No\",\"Good for Kids\":\"No\",\"Parking\":\"Street, Validated\",\"Hours\":\"Mon-Thu 11:30 am - 11:30 pm & Fri-Sat 11:30 am - 1 am & Sun 4 pm - 11 pm\",\"Take-out\":\"Yes\",\"Price Range\":\"$$\",\"City\":\"Seattle\",\"Category\":\"Restaurants, Italian\",\"Noise Level\":\"Loud\",\"Wheelchair Accessible\":\"No\",\"Has TV\":\"No\",\"Address\":\"1919 Post Alley, between Stuart &amp; Virginia, Seattle, WA\",\"Longitude\":\"-122.3424066\"},\"maggianos-little-italy-bellevue\":{\"Attire\":\"Casual\",\"Accepts Credit Cards\":\"Yes\",\"Phone number\":\"(425) 519-6476\",\"Takes Reservations\":\"Yes\",\"Caters\":\"Yes\",\"Good For\":\"Dinner\",\"Latitude\":\"47.6173233\",\"Business Name\":\"Maggiano's Little Italy\",\"Good for Groups\":\"Yes\",\"Outdoor Seating\":\"No\",\"Business type\":\"restaurant\",\"Wi-Fi\":\"No\",\"Alcohol\":\"Full Bar\",\"Waiter Service\":\"Yes\",\"Delivery\":\"No\",\"Good for Kids\":\"Yes\",\"Parking\":\"Garage\",\"Hours\":\"Mon-Thu, Sun 11:30 am - 10 pm & Fri-Sat 11:30 am - 11 pm\",\"Take-out\":\"Yes\",\"Price Range\":\"$$\",\"City\":\"Bellevue\",\"Category\":\"Restaurants, Italian\",\"Noise Level\":\"Loud\",\"Wheelchair Accessible\":\"Yes\",\"Address\":\"10455 NE 8th St, Bellevue, WA\",\"Has TV\":\"No\",\"Longitude\":\"-122.2007859\"},\"ristorante-mamma-melina-seattle\":{\"Attire\":\"Casual\",\"Accepts Credit Cards\":\"Yes\",\"Phone number\":\"(206) 632-2271\",\"Takes Reservations\":\"Yes\",\"Good For\":\"Dinner\",\"Latitude\":\"47.6677833\",\"Good for Groups\":\"Yes\",\"Business Name\":\"Ristorante Mamma Melina\",\"Outdoor Seating\":\"Yes\",\"Business type\":\"restaurant\",\"Alcohol\":\"Full Bar\",\"Waiter Service\":\"Yes\",\"Delivery\":\"No\",\"Good for Kids\":\"Yes\",\"Parking\":\"Street\",\"Hours\":\"Mon-Thu, Sun 11:30 am - 9:30 pm & Fri-Sat 11:30 am - 10 pm\",\"Take-out\":\"No\",\"Price Range\":\"$$\",\"City\":\"Seattle\",\"Category\":\"Restaurants, Italian\",\"Noise Level\":\"Very Loud\",\"Address\":\"5101 25th Ave NE, Seattle, WA\",\"Has TV\":\"No\",\"Wheelchair Accessible\":\"Yes\",\"Longitude\":\"-122.3008284\"},\"osteria-la-spiga-seattle\":{\"Attire\":\"Casual\",\"Accepts Credit Cards\":\"Yes\",\"Phone number\":\"(206) 323-8881\",\"Takes Reservations\":\"Yes\",\"Caters\":\"No\",\"Neighborhood\":\"Capitol Hill\",\"Ambience\":\"Classy\",\"Good For\":\"Dinner\",\"Latitude\":\"47.613272\",\"Business Name\":\"Osteria La Spiga\",\"Good for Groups\":\"Yes\",\"Outdoor Seating\":\"Yes\",\"Business type\":\"restaurant\",\"Wi-Fi\":\"No\",\"Alcohol\":\"Full Bar\",\"Waiter Service\":\"Yes\",\"Delivery\":\"No\",\"Good for Kids\":\"No\",\"Parking\":\"Street\",\"Hours\":\"Mon-Thu, Sun 5 pm - 11 pm & Fri-Sat 5 pm - 12 am\",\"Take-out\":\"Yes\",\"Price Range\":\"$$\",\"City\":\"Seattle\",\"Category\":\"Restaurants, Italian\",\"Noise Level\":\"Average\",\"Wheelchair Accessible\":\"Yes\",\"Has TV\":\"No\",\"Address\":\"1429 12th Ave, Seattle, WA\",\"Longitude\":\"-122.316821\"},\"bizzarro-italian-cafe-seattle\":{\"Attire\":\"Casual\",\"Accepts Credit Cards\":\"Yes\",\"Phone number\":\"(206) 632-7277\",\"Takes Reservations\":\"Yes\",\"Caters\":\"No\",\"Neighborhood\":\"Wallingford\",\"Ambience\":\"Hipster, Romantic, Trendy, Casual, Intimate\",\"Good For\":\"Dinner\",\"Latitude\":\"47.6620534\",\"Business Name\":\"Bizzarro Italian CafÈ\",\"Good for Groups\":\"Yes\",\"Outdoor Seating\":\"No\",\"Business type\":\"restaurant\",\"Wi-Fi\":\"No\",\"Alcohol\":\"Beer &amp; Wine Only\",\"Waiter Service\":\"Yes\",\"Delivery\":\"No\",\"Good for Kids\":\"No\",\"Parking\":\"Street\",\"Hours\":\"Mon-Sun 5 pm - 10 pm\",\"Take-out\":\"No\",\"Price Range\":\"$$\",\"City\":\"Seattle\",\"Category\":\"Restaurants, Italian\",\"Noise Level\":\"Average\",\"Wheelchair Accessible\":\"Yes\",\"Has TV\":\"No\",\"Address\":\"1307 N 46th St, Seattle, WA\",\"Longitude\":\"-122.3417311\"},\"buca-di-beppo-seattle\":{\"Attire\":\"Casual\",\"Accepts Credit Cards\":\"Yes\",\"Phone number\":\"(206) 284-1892\",\"Takes Reservations\":\"Yes\",\"Caters\":\"Yes\",\"Ambience\":\"Casual\",\"Good For\":\"Dinner\",\"Latitude\":\"47.625575\",\"Good for Groups\":\"Yes\",\"Business Name\":\"Buca di Beppo\",\"Outdoor Seating\":\"No\",\"Business type\":\"restaurant\",\"Wi-Fi\":\"No\",\"Alcohol\":\"Full Bar\",\"Waiter Service\":\"Yes\",\"Delivery\":\"No\",\"Good for Kids\":\"Yes\",\"Parking\":\"Garage, Street\",\"Hours\":\"Mon-Thu 4 pm - 10 pm & Fri 4 pm - 11 pm & Sat 11 am - 11 pm & Sun 11 am - 10 pm\",\"Take-out\":\"Yes\",\"Price Range\":\"$$\",\"City\":\"Seattle\",\"Category\":\"Restaurants, Italian\",\"Noise Level\":\"Loud\",\"Wheelchair Accessible\":\"Yes\",\"Has TV\":\"Yes\",\"Address\":\"701 9th Ave N, Seattle, WA\",\"Longitude\":\"-122.340132\"},\"volterra-seattle\":{\"Attire\":\"Casual\",\"Accepts Credit Cards\":\"Yes\",\"Phone number\":\"(206) 789-5100\",\"Takes Reservations\":\"Yes\",\"Neighborhood\":\"Ballard\",\"Good For\":\"Dinner\",\"Latitude\":\"47.6678009033\",\"Business Name\":\"Volterra\",\"Good for Groups\":\"Yes\",\"Outdoor Seating\":\"Yes\",\"Business type\":\"restaurant\",\"Alcohol\":\"Full Bar\",\"Waiter Service\":\"Yes\",\"Delivery\":\"No\",\"Good for Kids\":\"No\",\"Parking\":\"Street\",\"Take-out\":\"No\",\"Price Range\":\"$$$\",\"City\":\"Seattle\",\"Category\":\"Restaurants, Italian\",\"Noise Level\":\"Quiet\",\"Address\":\"5411 Ballard Ave NW, Seattle, WA\",\"Has TV\":\"No\",\"Wheelchair Accessible\":\"Yes\",\"Longitude\":\"-122.385002136\"},\"tavolata-seattle\":{\"Attire\":\"Casual\",\"Accepts Credit Cards\":\"Yes\",\"Phone number\":\"(206) 838-8008\",\"Takes Reservations\":\"Yes\",\"Neighborhood\":\"Belltown\",\"Ambience\":\"Hipster, Romantic, Trendy, Intimate\",\"Good For\":\"Dinner\",\"Latitude\":\"47.614423\",\"Good for Groups\":\"Yes\",\"Business Name\":\"Tavolata\",\"Outdoor Seating\":\"No\",\"Business type\":\"restaurant\",\"Alcohol\":\"Full Bar\",\"Waiter Service\":\"Yes\",\"Delivery\":\"No\",\"Good for Kids\":\"No\",\"Parking\":\"Street\",\"Hours\":\"Mon-Thu, Sun 5 pm - 11 pm & Fri-Sat 5 pm - 12 am\",\"Take-out\":\"Yes\",\"Price Ra ˛∫æ   1 –  revminer/ui/ViewMapActivity  #com/google/android/maps/MapActivity  %revminer/service/SearchResultListener DEG_TO_MICRODEG D ConstantValueA.ÑÄ     PADDING?Ù       <init> ()V Code
     LineNumberTable LocalVariableTable this Lrevminer/ui/ViewMapActivity; onCreate (Landroid/os/Bundle;)V
     
      setContentView (I)V 
  # $ % findViewById (I)Landroid/view/View; ' com/google/android/maps/MapView
 & ) * + setBuiltInZoomControls (Z)V
 - / . revminer/service/RevminerClient 0 1 Client #()Lrevminer/service/RevminerClient;
 - 3 4 5 addSearchResultListener *(Lrevminer/service/SearchResultListener;)V
 - 7 8 9 getLastSearchResultEvent &()Lrevminer/service/SearchResultEvent;
  ; < = onSearchResults '(Lrevminer/service/SearchResultEvent;)V savedInstanceState Landroid/os/Bundle; mapView !Lcom/google/android/maps/MapView; event $Lrevminer/service/SearchResultEvent; isRouteDisplayed ()Z
 G I H "revminer/service/SearchResultEvent J E hasError
 & L M N getController )()Lcom/google/android/maps/MapController;
  P Q R getResources !()Landroid/content/res/Resources; 
 U W V android/content/res/Resources X Y getDrawable '(I)Landroid/graphics/drawable/Drawable; [ %revminer/ui/RestaurantItemizedOverlay
 Z ]  ^ @(Landroid/graphics/drawable/Drawable;Landroid/content/Context;)VˇˇˇÄ   
 G b c d getResturants ()Ljava/util/List; f h g java/util/List i j iterator ()Ljava/util/Iterator; l n m java/util/Iterator o p next ()Ljava/lang/Object; r revminer/common/Restaurant
 q t u v getLocation &()Lrevminer/common/RestaurantLocation;
 x z y "revminer/common/RestaurantLocation { | getLatitude ()D
 x ~  | getLongitude Å  com/google/android/maps/GeoPoint
 Ä É  Ñ (II)V
 Ü à á java/lang/Math â ä max (II)I
 Ü å ç ä min è #com/google/android/maps/OverlayItem
 q ë í ì getName ()Ljava/lang/String;
 x ï ñ ì getStreetAddress
 é ò  ô I(Lcom/google/android/maps/GeoPoint;Ljava/lang/String;Ljava/lang/String;)V
 Z õ ú ù 
addOverlay ((Lcom/google/android/maps/OverlayItem;)V l ü † E hasNext
 & ¢ £ d getOverlays f • ¶  clear f ® © ™ add (Ljava/lang/Object;)Z
 Ü ¨ ≠ Æ abs (I)I
 ∞ ≤ ± %com/google/android/maps/MapController ≥ Ñ 
zoomToSpan
 ∞ µ ∂ ∑ 	animateTo %(Lcom/google/android/maps/GeoPoint;)V e mapController 'Lcom/google/android/maps/MapController; 	mapMarker $Landroid/graphics/drawable/Drawable; itemizedOverlay 'Lrevminer/ui/RestaurantItemizedOverlay; minLat I maxLat minLon maxLon 
restaurant Lrevminer/common/Restaurant; loc $Lrevminer/common/RestaurantLocation; latitude 	longitude point "Lcom/google/android/maps/GeoPoint; overlayItem %Lcom/google/android/maps/OverlayItem; 
SourceFile ViewMapActivity.java !          	    
     	             /     *∑ ±                               û     2*+∑ *∂ *!∂ "¿ &M,∂ (∏ ,*∂ 2∏ ,∂ 6N-∆ *-∂ :±       * 
            ! " ' ! ( # , $ 1 &    *    2       2 > ?    @ A  ( 
 B C   D E     ,     ¨           +              < =    U    +∂ Fô ±*!∂ "¿ &M,∂ KN*∂ OS∂ T:ª ZY*∑ \:_6`6_6`6	+∂ aπ e :ß {π k ¿ q:

∂ s:∂ w 
ké6∂ } 
ké6ª ÄY∑ Ç:∏ Ö6∏ ã6	∏ Ö6	∏ ã6ª éY
∂ ê∂ î∑ ó:∂ öπ û öˇÅ,∂ °π § ,∂ °π ß W-d∏ ´á ké	d∏ ´á ké∂ Ø-ª ÄY`l	`l∑ Ç∂ ¥±       ~    /  0  3  4  6 " 7 . : 2 ; 6 < : = > ? X @ _ A k B w C Ñ E ç F ñ G ü H ® J Ω K ƒ ? Œ N ◊ O „ R Ú S  R T U T V    ¢          ∏ C   @ A   π ∫  " ˚ ª º  . Ô Ω æ  2 Î ø ¿  6 Á ¡ ¿  : „ ¬ ¿  > ﬂ √ ¿ 	 X l ƒ ≈ 
 _ e ∆ «  k Y » ¿  w M … ¿  Ñ @   À  Ω  Ã Õ   Œ    œ                                                                                                                                                                                                                                                 package revminer.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import revminer.common.Restaurant;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

// singleton
public class RevminerClient implements SearchDataProvider {
  private static final String REST_URL = "http://cse454.local-box.org:3000/revminer/";

	private final List<SearchResultListener> resultListeners;
	private final List<ExactMatchListener> matchListeners;
	private final List<SearchListener> searchListeners;

	private SearchResultEvent lastSearchResultEvent;
	private Restaurant lastExactMatch;

	private static RevminerClient client;
	
	private RevminerClient() {
		resultListeners = new ArrayList<SearchResultListener>();
		matchListeners = new ArrayList<ExactMatchListener>();
		searchListeners = new ArrayList<SearchListener>();

		lastSearchResultEvent = null;
		lastExactMatch = null;
	}
	
	public static RevminerClient Client() {
		if (client == null)
			client = new RevminerClient();
		return client;
	}

	public void addSearchResultListener(SearchResultListener listener) {
		resultListeners.add(listener);
	}

  public void addExactMatchListener(ExactMatchListener listener) {
    matchListeners.add(listener);
    
  }

	public void addSearchListener(SearchListener listener) {
		searchListeners.add(listener);
	}

	// TODO: remove context from this interface once we actually implement the query logic
	public boolean sendSearchQuery(String query, Context context) {

		// TODO: actually perform query instead of making this little popup
        Toast.makeText(context, "Query: \"" + query +"\"", Toast.LENGTH_SHORT).show();

        for (SearchListener listener : searchListeners)
        	listener.onSearch(query);

        // TODO: Lets add some parallelism
        Log.d("revd", "Query for: " + query);

        String result = SimpleHttpClient.get(
            REST_URL + URLEncoder.encode(query).replace("+", "%20"));
        if (result == null) {
          Log.d("revd", "null result");
          return false;
        }
        Log.d("revd", "results received for " + query);

        // TODO: update once true query logic is implemented
        // TODO: handle case when we get "suggestions" or "match"
        try {
          JSONObject object = (JSONObject) new JSONTokener(result).nextValue();

          if (object.has("results")) {
            List<Restaurant> res = new ArrayList<Restaurant>();
            JSONObject results = object.getJSONObject("results");; 

            if (results.has("meta") && results.has("data")) {
              JSONObject meta = results.getJSONObject("meta");
              JSONObject data = results.getJSONObject("data");

              Iterator<String> places = data.keys();
              while (places.hasNext()) {
                String place = places.next();
                Log.d("revd", place);

                Restaurant restaurant = Restaurant.getInstance(place);
                if (restaurant == null) { // don't have it cached, we need to make an instance
                  restaurant = getRestaurantFromMeta(place, meta);
                }
                res.add(Restaurant.getInstance(place));
              }
            }

            notifySearchResultEvent(new SearchResultEvent(res));
          } else if (object.has("match")) {
            //TODO: Parse matched object 
            Log.d("revd", "Got full match back");
          }

        } catch (JSONException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        // !

		return false;
	}

	private static final Restaurant getRestaurantFromMeta(String name, JSONObject meta) {
	  String key = name;
	  HashMap<String, String> attributeMapping;
	  
    try {
      attributeMapping = new HashMap<String, String>();
      JSONObject attributes = attributes = meta.getJSONObject(key);
      
      Iterator<String> attributesIter = attributes.keys();
      while (attributesIter.hasNext()) {
        String attr = attributesIter.next();
        String value = attributes.getString(attr);
        attributeMapping.put(attr,  value);                  
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }

     return Restaurant.createInstance(key, attributeMapping);
	}

  public SearchResultEvent getLastSearchResultEvent() {
    return lastSearchResultEvent;
  }

  public Restaurant getLastExactMatch() {
    return lastExactMatch;
  }

  private void notifySearchResultEvent(SearchResultEvent e) {
    this.lastSearchResultEvent = e;
    for (SearchResultListener listener : resultListeners) {
      listener.onSearchResults(e);
    }
  }

  private void notifyExactMatchEvent(Restaurant r) {
    this.lastExactMatch = r;
    for (ExactMatchListener listener : matchListeners) {
      listener.onExactMatch(r);
    }
  }
}                                                                                                                                                                                                                                                                                                         ","City":"Seattle","Category":"Restaurants, Italian","Noise Level":"Loud","Wheelchair Accessible":"No","Has TV":"No","Address":"1919 Post Alley, between Stuart &amp; Virginia, Seattle, WA","Longitude":"-122.3424066"},"maggianos-little-italy-bellevue":{"Attire":"Casual","Accepts Credit Cards":"Yes","Phone number":"(425) 519-6476","Takes Reservations":"Yes","Caters":"Yes","Good For":"Dinner","Latitude":"47.6173233","Business Name":"Maggiano's Little Italy","Good for Groups":"Yes","Outdoor Seating":"No","Business type":"restaurant","Wi-Fi":"No","Alcohol":"Full Bar","Waiter Service":"Yes","Delivery":"No","Good for Kids":"Yes","Parking":"Garage","Hours":"Mon-Thu, Sun 11:30 am - 10 pm & Fri-Sat 11:30 am - 11 pm","Take-out":"Yes","Price Range":"$$","City":"Bellevue","Category":"Restaurants, Italian","Noise Level":"Loud","Wheelchair Accessible":"Yes","Address":"10455 NE 8th St, Bellevue, WA","Has TV":"No","Longitude":"-122.2007859"},"ristorante-mamma-melina-seattle":{"Attire":"Casual","Accepts Credit Cards":"Yes","Phone number":"(206) 632-2271","Takes Reservations":"Yes","Good For":"Dinner","Latitude":"47.6677833","Good for Groups":"Yes","Business Name":"Ristorante Mamma Melina","Outdoor Seating":"Yes","Business type":"restaurant","Alcohol":"Full Bar","Waiter Service":"Yes","Delivery":"No","Good for Kids":"Yes","Parking":"Street","Hours":"Mon-Thu, Sun 11:30 am - 9:30 pm & Fri-Sat 11:30 am - 10 pm","Take-out":"No","Price Range":"$$","City":"Seattle","Category":"Restaurants, Italian","Noise Level":"Very Loud","Address":"5101 25th Ave NE, Seattle, WA","Has TV":"No","Wheelchair Accessible":"Yes","Longitude":"-122.3008284"},"osteria-la-spiga-seattle":{"Attire":"Casual","Accepts Credit Cards":"Yes","Phone number":"(206) 323-8881","Takes Reservations":"Yes","Caters":"No","Neighborhood":"Capitol Hill","Ambience":"Classy","Good For":"Dinner","Latitude":"47.613272","Business Name":"Osteria La Spiga","Good for Groups":"Yes","Outdoor Seating":"Yes","Business type":"restaurant","Wi-Fi":"No","Alcohol":"Full Bar","Waiter Service":"Yes","Delivery":"No","Good for Kids":"No","Parking":"Street","Hours":"Mon-Thu, Sun 5 pm - 11 pm & Fri-Sat 5 pm - 12 am","Take-out":"Yes","Price Range":"$$","City":"Seattle","Category":"Restaurants, Italian","Noise Level":"Average","Wheelchair Accessible":"Yes","Has TV":"No","Address":"1429 12th Ave, Seattle, WA","Longitude":"-122.316821"},"bizzarro-italian-cafe-seattle":{"Attire":"Casual","Accepts Credit Cards":"Yes","Phone number":"(206) 632-7277","Takes Reservations":"Yes","Caters":"No","Neighborhood":"Wallin ˛∫æ   1  revminer/service/RevminerClient  java/lang/Object  #revminer/service/SearchDataProvider REST_URL Ljava/lang/String; ConstantValue  *http://cse454.local-box.org:3000/revminer/ resultListeners Ljava/util/List; 	Signature 9Ljava/util/List<Lrevminer/service/SearchResultListener;>; matchListeners 7Ljava/util/List<Lrevminer/service/ExactMatchListener;>; searchListeners 3Ljava/util/List<Lrevminer/service/SearchListener;>; lastSearchResultEvent $Lrevminer/service/SearchResultEvent; lastExactMatch Lrevminer/common/Restaurant; client !Lrevminer/service/RevminerClient; <init> ()V Code
       java/util/ArrayList
  	  #  	  %  	  '  	  )  	  +   LineNumberTable LocalVariableTable this Client #()Lrevminer/service/RevminerClient;	  2  
   addSearchResultListener *(Lrevminer/service/SearchResultListener;)V 7 9 8 java/util/List : ; add (Ljava/lang/Object;)Z listener 'Lrevminer/service/SearchResultListener; addExactMatchListener ((Lrevminer/service/ExactMatchListener;)V %Lrevminer/service/ExactMatchListener; addSearchListener $(Lrevminer/service/SearchListener;)V !Lrevminer/service/SearchListener; sendSearchQuery .(Ljava/lang/String;Landroid/content/Context;)Z G java/lang/StringBuilder I Query: "
 F K  L (Ljava/lang/String;)V
 F N O P append -(Ljava/lang/String;)Ljava/lang/StringBuilder; R "
 F T U V toString ()Ljava/lang/String;
 X Z Y android/widget/Toast [ \ makeText J(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;
 X ^ _  show 7 a b c iterator ()Ljava/util/Iterator; e g f java/util/Iterator h i next ()Ljava/lang/Object; k revminer/service/SearchListener j m n L onSearch e p q r hasNext ()Z t revd v Query for: 
 x z y android/util/Log { | d '(Ljava/lang/String;Ljava/lang/String;)I
 ~ Ä  java/net/URLEncoder Å Ç encode &(Ljava/lang/String;)Ljava/lang/String; Ñ + Ü %20
 à ä â java/lang/String ã å replace D(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
 é ê è !revminer/service/SimpleHttpClient ë Ç get ì null result ï results received for  ó org/json/JSONTokener
 ñ K
 ñ ö õ i 	nextValue ù org/json/JSONObject ü results
 ú ° ¢ £ has (Ljava/lang/String;)Z
 ú • ¶ ß getJSONObject )(Ljava/lang/String;)Lorg/json/JSONObject; © meta ´ data
 ú ≠ Æ c keys
 ∞ ≤ ± revminer/common/Restaurant ≥ ¥ getInstance 0(Ljava/lang/String;)Lrevminer/common/Restaurant;
  ∂ ∑ ∏ getRestaurantFromMeta E(Ljava/lang/String;Lorg/json/JSONObject;)Lrevminer/common/Restaurant; ∫ "revminer/service/SearchResultEvent
 π º  Ω (Ljava/util/List;)V
  ø ¿ ¡ notifySearchResultEvent '(Lrevminer/service/SearchResultEvent;)V √ match ≈ Got full match back
 « … » org/json/JSONException    printStackTrace query context Landroid/content/Context; result object Lorg/json/JSONObject; res places Ljava/util/Iterator; place 
restaurant e Lorg/json/JSONException; LocalVariableTypeTable .Ljava/util/List<Lrevminer/common/Restaurant;>; (Ljava/util/Iterator<Ljava/lang/String;>; ‹ java/util/HashMap
 € 
 ú ﬂ ‡ Ç 	getString
 € ‚ „ ‰ put 8(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
 ∞ Ê Á Ë createInstance ?(Ljava/lang/String;Ljava/util/Map;)Lrevminer/common/Restaurant; name key attributeMapping Ljava/util/HashMap; 
attributes attributesIter attr value 9Ljava/util/HashMap<Ljava/lang/String;Ljava/lang/String;>; getLastSearchResultEvent &()Lrevminer/service/SearchResultEvent; getLastExactMatch ()Lrevminer/common/Restaurant; ˜ %revminer/service/SearchResultListener ˆ ˘ ˙ ¡ onSearchResults notifyExactMatchEvent (Lrevminer/common/Restaurant;)V ˛ #revminer/service/ExactMatchListener ˝  ¸ onExactMatch r 
SourceFile RevminerClient.java !          	    
                                      
             r     0*∑ *ª Y∑ !µ "*ª Y∑ !µ $*ª Y∑ !µ &*µ (*µ *±    ,       %  &  '  ( % * * + / , -       0 .    	 / 0     <      ≤ 1« ª Y∑ 3≥ 1≤ 1∞    ,       /  0  1 -       4 5     D     *¥ "+π 6 W±    ,   
    5  6 -        .       < =   > ?     D     *¥ $+π 6 W±    ,   
    9  ; -        .       < @   A B     D     *¥ &+π 6 W±    ,   
    >  ? -        .       < C   D E    π    i,ª FYH∑ J+∂ MQ∂ M∂ S∏ W∂ ]*¥ &π ` :ß π d ¿ jN-+π l π o öˇÁsª FYu∑ J+∂ M∂ S∏ wWª FY
∑ J+∏ }ÉÖ∂ á∂ M∂ S∏ çN-« sí∏ wW¨sª FYî∑ J+∂ M∂ S∏ wWª ñY-∑ ò∂ ô¿ ú:û∂ †ô ïª Y∑ !:û∂ §:®∂ †ô i™∂ †ô _®∂ §:™∂ §:∂ ¨:	ß 9	π d ¿ à:
s
∏ wW
∏ Ø:« 
∏ µ:
∏ Øπ 6 W	π o öˇ√*ª πY∑ ª∑ æß ¬∂ †ô sƒ∏ wWß 
:∂ ∆¨  ü]` «  ,   Ü !   E  G 6 H = G G K ] N w M { O  P á Q â S ü X Ø Z π [ ¬ \ À ^ ﬂ _ Ë ` Ò b ¯ c ˚ d e g h i$ k1 c; oK pU r` ub wg { -   é   i .     i À    i Ã Õ  6  < C  { Ó Œ   Ø ± œ –  ¬ â —   À Ä ü –  Ë S © –  Ò J ´ –  ¯ C “ ” 	 * ‘  
  ’  b  ÷ ◊  ÿ     ¬ â — Ÿ  ¯ C “ ⁄ 	  ∑ ∏    :     X*Mª €Y∑ ›N+,∂ §Y::∂ ¨:ß !π d ¿ à:∂ ﬁ:-∂ ·Wπ o öˇ€ß :∂ ∆∞,-∏ Â∞   F I «  ,   6      É 
 Ñ  Ü  á  à * â 3 ä < á I å K é P è R í -   f 
   X È      X © –   V Í   
 ? Î Ï  R  Î Ï   7 Ì –   . Ó ”  *  Ô   3 	    K  ÷ ◊  ÿ      
 ? Î Ò  R  Î Ò   . Ó ⁄   Ú Û     /     *¥ (∞    ,       ñ -        .     Ù ı     /     *¥ *∞    ,       ö -        .     ¿ ¡     {     -*+µ (*¥ "π ` Nß -π d ¿ ˆM,+π ¯ -π o öˇÈ±    ,       û  ü  † # ü , ¢ -        - .      - ÷     < =   ˚ ¸     {     -*+µ **¥ $π ` Nß -π d ¿ ˝M,+π ˇ -π o öˇÈ±    ,       •  ¶  ß # ¶ , © -        - .      -     < @                                                                                                                                                                                                   (206) 633-3800","Takes Reservations":"Yes","Categories":"Restaurants, Italian, Restaurants, Pizza","Caters":"No","Neighborhood":"Wallingford","Ambience":"Casual","Good For":"Lunch, Dinner","Latitude":"47.6604393","Business Name":"Tutta Bella Neapolitan Pizzeria","Good for Groups":"Yes","Outdoor Seating":"Yes","Business type":"restaurant","Wi-Fi":"No","Alcohol":"Beer &amp; Wine Only","Waiter Service":"Yes","Delivery":"No","Good for Kids":"Yes","Parking":"Street, Private Lot","Hours":"Mon-Thu, Sun 11 am - 10 pm & Fri-Sat 11 am - 11 pm","Take-out":"Yes","Price Range":"$$","City":"Seattle","Noise Level":"Loud","Wheelchair Accessible":"Yes","Has TV":"No","Address":"4411 Stone Way N, Seattle, WA","Longitude":"-122.342378"},"perche-no-pasta-and-vino-seattle-2":{"Attire":"Casual","Accepts Credit Cards":"Yes","Phone number":"(206) 547-0222","Takes Reservations":"Yes","Neighborhood":"Wallingford","Good For":"Dinner","Latitude":"47.664142","Good for Groups":"Yes","Business Name":"Perche' No Pasta &amp; Vino","Outdoor Seating":"Yes","Business type":"restaurant","Alcohol":"Full Bar","Waiter Service":"Yes","Delivery":"No","Good for Kids":"Yes","Parking":"Street","Hours":"Tue-Thu, Sun 4 pm - 10 pm & Fri-Sat 4 pm - 11 pm","Take-out":"Yes","Price Range":"$$","City":"Seattle","Category":"Restaurants, Italian","Noise Level":"Average","Wheelchair Accessible":"Yes","Address":"1319 N 49th St, Seattle, WA","Has TV":"No","Longitude":"-122.341201"},"cascina-spinasse-seattle":{"Attire":"Casual","Accepts Credit Cards":"Yes","Phone number":"(206) 251-7673","Takes Reservations":"Yes","Neighborhood":"Capitol Hill","Good For":"Dinner","Latitude":"47.6149922","Good for Groups":"Yes","Business Name":"Cascina Spinasse","Outdoor Seating":"No","Business type":"restaurant","Alcohol":"Full Bar","Waiter Service":"Yes","Delivery":"No","Good for Kids":"No","Parking":"Street","Hours":"Mon, Wed-Thu, Sun 5 pm - 10 pm & Fri-Sat 5 pm - 11 pm","Take-out":"No","Price Range":"$$$","City":"Seattle","Category":"Restaurants, Italian","Noise Level":"Quiet","Wheelchations\":\"Yes\",\"Categories\":\"Restaurants, Italian, Restaurants, Seafood\",\"Neighborhood\":\"Capitol Hill\",\"Ambience\":\"Romantic, Trendy, Intimate\",\"Good For\":\"Dinner\",\"Latitude\":\"47.6153297\",\"Good for Groups\":\"Yes\",\"Business Name\":\"Anchovies &amp; Olives\",\"Outdoor Seating\":\"No\",\"Business type\":\"restaurant\",\"Alcohol\":\"Full Bar\",\"Waiter Service\":\"Yes\",\"Delivery\":\"No\",\"Good for Kids\":\"No\",\"Parking\":\"Street\",\"Hours\":\"Mon-Thu, Sun 5 pm - 11 pm & Fri-Sat 5 pm - 12 am\",\"Take-out\":\"No\",\"Price Range\":\"$$$\",\"City\":\"Seattle\",\"Noise Level\":\"Average\",\"Wheelchair Accessible\":\"Yes\",\"Has TV\":\"No\",\"Address\":\"1550 15th Ave, Seattle, WA\",\"Longitude\":\"-122.3127949\"},\"lucia-italian-restaurant-kirkland\":{\"Attire\":\"Casual\",\"Accepts Credit Cards\":\"Yes\",\"Phone number\":\"(425) 889-0200\",\"Takes Reservations\":\"Yes\",\"Good For\":\"Dinner\",\"Latitude\":\"47.675394\",\"Good for Groups\":\"Yes\",\"Business Name\":\"Lucia Italian Restaurant\",\"Outdoor Seating\":\"Yes\",\"Business type\":\"restaurant\",\"Wi-Fi\":\"No\",\"Alcohol\":\"Full Bar\",\"Waiter Service\":\"Yes\",\"Delivery\":\"No\",\"Good for Kids\":\"No\",\"Parking\":\"Private Lot\",\"Hours\":\"Mon-Thu 11 am - 12 am & Fri-Sat 11 am - 1 am & Sun 11 am - 11 pm\",\"Take-out\":\"Yes\",\"Price Range\":\"$$\",\"City\":\"Kirkland\",\"Category\":\"Restaurants, Italian\",\"Address\":\"222 Parkplace Center, Ste C, Kirkland, WA\",\"Has TV\":\"No\",\"Wheelchair Accessible\":\"Yes\",\"Longitude\":\"-122.196985\"},\"il-terrazzo-carmine-seattle\":{\"Alcohol\":\"Full Bar\",\"Waiter Service\":\"Yes\",\"Delivery\":\"No\",\"Attire\":\"Dressy\",\"Good for Kids\":\"No\",\"Parking\":\"Street\",\"Hours\":\"Mon-Fri 11:30 am - 2:30 pm & Mon-Sat 5:30 pm - 10 pm\",\"Take-out\":\"No\",\"Accepts Credit Cards\":\"Yes\",\"Price Range\":\"$$$\",\"Phone number\":\"(206) 467-7797\",\"Takes Reservations\":\"Yes\",\"City\":\"Seattle\",\"Category\":\"Restaurants, Italian\",\"Neighborhood\":\"Pioneer Square\",\"Wheelchair Accessible\":\"Yes\",\"Good For\":\"Dinner\",\"Address\":\"411 1st Ave S, Seattle, WA\",\"Latitude\":\"47.598793\",\"Longitude\":\"-122.33436\",\"Outdoor Seating\":\"Yes\",\"Good for Groups\":\"Yes\",\"Business Name\":\"Il Terrazzo Carmine\",\"Business type\":\"restaurant\"},\"marcello-ristorante-seattle\":{\"Attire\":\"Casual\",\"Accepts Credit Cards\":\"Yes\",\"Phone number\":\"(206) 527-4778\",\"Takes Reservations\":\"Yes\",\"Neighborhood\":\"Roosevelt\",\"Ambience\":\"Romantic\",\"Good For\":\"Dinner\",\"Latitude\":\"47.6801986694\",\"Business Name\":\"Marcello Ristorante\",\"Good for Groups\":\"Yes\",\"Outdoor Seating\":\"Yes\",\"Business type\":\"restaurant\",\"Wi-Fi\":\"No\",\"Alcohol\":\"Beer &amp; Wine Only\",\"Waiter Service\":\"Yes\",\"Delivery\":\"No\",\"Good for Kids\":\"Yes\",\"Parking\":\"Street, Private Lot\",\"Take-out\":\"Yes\",\"Price Range\":\"$$\",\"City\":\"Seattle\",\"Category\":\"Restaurants, Italian\",\"Noise Level\":\"Quiet\",\"Wheelchair Accessible\":\"Yes\",\"Has TV\":\"No\",\"Address\":\"7115 Roosevelt Way NE, Seattle, WA\",\"Longitude\":\"-122.317001343\"},\"pomodoro-ristorante-italiano-seattle\":{\"Attire\":\"Casual\",\"Accepts Credit Cards\":\"Yes\",\"Phone number\":\"(206) 324-3160\",\"Takes Reservations\":\"Yes\",\"Categories\":\"Restaurants, Italian, Restaurants, Spanish, Restaurants, Basque\",\"Neighborhood\":\"Eastlake\",\"Good For\":\"Dinner\",\"Latitude\":\"47.6405172\",\"Good for Groups\":\"Yes\",\"Business Name\":\"Pomodoro Ristorante Italiano\",\"Outdoor Seating\":\"No\",\"Business type\":\"restaurant\",\"Alcohol\":\"Full Bar\",\"Waiter Service\":\"Yes\",\"Delivery\":\"No\",\"Good for Kids\":\"No\",\"Parking\":\"Garage, Street\",\"Hours\":\"Tue-Thu, Sun 5:30 pm - 12 am & Fri-Sat 5:30 pm - 1 am\",\"Take-out\":\"Yes\",\"Price Range\":\"$$\",\"City\":\"Seattle\",\"Noise Level\":\"Average\",\"Wheelchair Accessible\":\"Yes\",\"Address\":\"2366 Eastlake Ave E, Ste 101, Seattle, WA\",\"Has TV\":\"No\",\"Longitude\":\"-122.3258753\"},\"thats-amore-italian-restaurant-seattle\":{\"Attire\":\"Casual\",\"Accepts Credit Cards\":\"Yes\",\"Phone number\":\"(206) 322-3677\",\"Takes Reservations\":\"Yes\",\"Categories\":\"Restaurants, Italian, Restaurants, Pizza\",\"Neighborhood\":\"Mt. Baker\",\"Good For\":\"Dinner\",\"Latitude\":\"47.589943\",\"Good for Groups\":\"Yes\",\"Business Name\":\"That's Amore Italian Restaurant\",\"Outdoor Seating\":\"Yes\",\"Business type\":\"restaurant\",\"Alcohol\":\"Beer &amp; Wine Only\",\"Waiter Service\":\"Yes\",\"Delivery\":\"No\",\"Good for Kids\":\"Yes\",\"Parking\":\"Street\",\"Hours\":\"Mon-Thu 5 pm - 9 pm & Fri-Sat 5 pm - 10 pm & Sun 10 am - 9 pm\",\"Take-out\":\"Yes\",\"Price Range\":\"$$\",\"City\":\"Seattle\",\"Noise Level\":\"Average\",\"Wheelchair Accessible\":\"Yes\",\"Address\":\"1425 31st Ave S, Seattle, WA\",\"Has TV\":\"No\",\"Longitude\":\"-122.292401\"},\"tutta-bella-neapolitan-pizzeria-seattle-8\":{\"Attire\":\"Casual\",\"Accepts Credit Cards\":\"Yes\",\"Phone number\":\"(206) 624-4422\",\"Takes Reservations\":\"Yes\",\"Neighborhood\":\"Belltown\",\"Good For\":\"Lunch, Dinner\",\"Latitude\":\"47.618213\",\"Good for Groups\":\"Yes\",\"Business Name\":\"Tutta Bella Neapolitan Pizzeria\",\"Outdoor Seating\":\"Yes\",\"Business type\":\"restaurant\",\"Wi-Fi\":\"No\",\"Alcohol\":\"Full Bar\",\"Waiter Service\":\"Yes\",\"Delivery\":\"No\",\"Good for Kids\":\"Yes\",\"Parking\":\"Garage, Street, Validated\",\"Hours\":\"Mon-Thu, Sun 11 am - 10 pm & Fri-Sat 11 am - 11 pm\",\"Take-out\":\"Yes\",\"Price Range\":\"$$\",\"City\":\"Seattle\",\"Category\":\"Restaurants, Pizza\",\"Noise Level\":\"Average\",\"Wheelchair Accessible\":\"Yes\",\"Address\":\"2200 Westlake Ave, Ste 112, Seattle, WA\",\"Longitude\":\"-122.338214\"},\"angelinas-seattle\":{\"Attire\":\"Casual\",\"Accepts Credit Cards\":\"Yes\",\"Phone number\":\"(206) 932-4550\",\"Takes Reservations\":\"Yes\",\"Neighborhood\":\"West Seattle\",\"Good For\":\"Dinner\",\"Latitude\":\"47.582553\",\"Good for Groups\":\"Yes\",\"Business Name\":\"Angelina's\",\"Outdoor Seating\":\"Yes\",\"Business type\":\"restaurant\",\"Alcohol\":\"Full Bar\",\"Waiter Service\":\"Yes\",\"Delivery\":\"No\",\"Good for Kids\":\"Yes\",\"Parking\":\"Street, Private Lot\",\"Hours\":\"Mon-Fri 11:30 am - 1 am & Sat-Sun 9 am - 1 am\",\"Take-out\":\"Yes\",\"Price Range\":\"$$\",\"City\":\"Seattle\",\"Category\":\"Restaurants, Italian\",\"Noise Level\":\"Loud\",\"Wheelchair Accessible\":\"Yes\",\"Address\":\"2311 California Ave SW, Seattle, WA\",\"Has TV\":\"Yes\",\"Longitude\":\"-122.386879\"},\"branzino-seattle-3\":{\"Attire\":\"Casual\",\"Accepts Credit Cards\":\"Yes\",\"Phone number\":\"(206) 728-5181\",\"Takes Reservations\":\"Yes\",\"Categories\":\"Restaurants, Seafood, Restaurants, Italian\",\"Neighborhood\":\"Belltown\",\"Ambience\":\"Romantic, Intimate\",\"Good For\":\"Dinner\",\"Latitude\":\"47.6151226\",\"Good for Groups\":\"Yes\",\"Business Name\":\"Branzino\",\"Outdoor Seating\":\"Yes\",\"Business type\":\"restaurant\",\"Wi-Fi\":\"No\",\"Alcohol\":\"Full Bar\",\"Waiter Service\":\"Yes\",\"Delivery\":\"No\",\"Good for Kids\":\"No\",\"Parking\":\"Street\",\"Hours\":\"Mon, Sun 5 pm - 11 pm & Tue-Thu 5 pm - 12 am & Fri-Sat 5 pm - 1 am\",\"Take-out\":\"No\",\"Price Range\":\"$$$\",\"City\":\"Seattle\",\"Noise Level\":\"Loud\",\"Wheelchair Accessible\":\"Yes\",\"Has TV\":\"No\",\"Address\":\"2429 2nd Ave, Seattle, WA\",\"Longitude\":\"-122.3475235\"},\"cafe-veloce-kirkland\":{\"Alcohol\":\"Beer &amp; Wine Only\",\"Waiter Service\":\"Yes\",\"Delivery\":\"No\",\"Attire\":\"Casual\",\"Good for Kids\":\"Yes\",\"Parking\":\"Private Lot\",\"Take-out\":\"Yes\",\"Accepts Credit Cards\":\"Yes\",\"Phone number\":\"(425) 814-2972\",\"Price Range\":\"$$\",\"Takes Reservations\":\"Yes\",\"City\":\"Kirkland\",\"Categories\":\"Restaurants, Italian, Restaurants, Pizza\",\"Ambience\":\"Casual\",\"Wheelchair Accessible\":\"Yes\",\"Has TV\":\"Yes\",\"Good For\":\"Dinner\",\"Address\":\"12514 120th Ave NE, Kirkland, WA\",\"Latitude\":\"47.7106018066\",\"Longitude\":\"-122.179000854\",\"Outdoor Seating\":\"Yes\",\"Business Name\":\"Cafe Veloce\",\"Good for Groups\":\"Yes\",\"Business type\":\"restaurant\"},\"piatti-ristorante-and-bar-seattle\":{\"Attire\":\"Casual\",\"Accepts Credit Cards\":\"Yes\",\"Phone number\":\"(206) 524-9088\",\"Takes Reservations\":\"Yes\",\"Neighborhood\":\"University District\",\"Good For\":\"Dinner\",\"Latitude\":\"47.6622665\",\"Good for Groups\":\"Yes\",\"Business Name\":\"Piatti Ristorante &amp; Bar\",\"Outdoor Seating\":\"Yes\",\"Business type\":\"restaurant\",\"Alcohol\":\"Full Bar\",\"Waiter Service\":\"Yes\",\"Delivery\":\"No\",\"Good for Kids\":\"Yes\",\"Parking\":\"Private Lot\",\"Hours\":\"Mon-Thu 11:30 am - 9 pm & Fri-Sat 11 am - 10 pm & Sun 11 am - 9 pm\",\"Take-out\":\"Yes\",\"Price Range\":\"$$\",\"City\":\"Seattle\",\"Category\":\"Restaurants, Italian\",\"Noise Level\":\"Average\",\"Wheelchair Accessible\":\"Yes\",\"Address\":\"2695 NE Village Ln, Seattle, WA\",\"Has TV\":\"Yes\",\"Longitude\":\"-122.2977309\"},\"pasta-nova-woodinville\":{\"Alcohol\":\"Beer &amp; Wine Only\",\"Waiter Service\":\"Yes\",\"Delivery\":\"No\",\"Attire\":\"Casual\",\"Good for Kids\":\"Yes\",\"Parking\":\"Private Lot\",\"Take-out\":\"Yes\",\"Accepts Credit Cards\":\"Yes\",\"Phone number\":\"(425) 483-3716\",\"Price Range\":\"$$\",\"Takes Reservations\":\"Yes\",\"City\":\"Woodinville\",\"Category\":\"Restaurants, Italian\",\"Noise Level\":\"Average\",\"Wheelchair Accessible\":\"Yes\",\"Good For\":\"Dinner\",\"Address\":\"17310 140th Ave NE, Woodinville, WA\",\"Latitude\":\"47.7532293\",\"Longitude\":\"-122.1525067\",\"Outdoor Seating\":\"Yes\",\"Business Name\":\"Pasta Nova\",\"Good for Groups\":\"Yes\",\"Business type\":\"restaurant\"},\"grazie-ristorante-italiano-bothell\":{\"Alcohol\":\"Full Bar\",\"Waiter Service\":\"Yes\",\"Delivery\":\"No\",\"Attire\":\"Casual\",\"Good for Kids\":\"Yes\",\"Parking\":\"Private Lot\",\"Hours\":\"Mon-Thu 11 am - 9 pm & Fri 11 am - 10 pm & Sat 4 pm - 10 pm & Sun 4 pm - 9 pm\",\"Take-out\":\"Yes\",\"Accepts Credit Cards\":\"Yes\",\"Phone number\":\"(425) 402-9600\",\"Price Range\":\"$$\",\"Takes Reservations\":\"Yes\",\"City\":\"Bothell\",\"Categories\":\"Restaurants, Pizza, Restaurants, Italian\",\"Noise Level\":\"Quiet\",\"Wheelchair Accessible\":\"Yes\",\"Good For\":\"Dinner\",\"Address\":\"23207 Bothell Everett Hwy, Bothell, WA\",\"Latitude\":\"47.7872009277\",\"Longitude\":\"-122.21900177\",\"Outdoor Seating\":\"Yes\",\"Good for Groups\":\"Yes\",\"Business Name\":\"Grazie Ristorante Italiano\",\"Business type\":\"restaurant\"},\"tilth-seattle\":{\"Attire\":\"Casual\",\"Accepts Credit Cards\":\"Yes\",\"Phone number\":\"(206) 633-0801\",\"Takes Reservations\":\"Yes\",\"Neighborhood\":\"Wallingford\",\"Good For\":\"Dinner\",\"Latitude\":\"47.66137\",\"Good for Groups\":\"No\",\"Business Name\":\"Tilth\",\"Outdoor Seating\":\"Yes\",\"Business type\":\"restaurant\",\"Wi-Fi\":\"No\",\"Alcohol\":\"Full Bar\",\"Waiter Service\":\"Yes\",\"Delivery\":\"No\",\"Good for Kids\":\"No\",\"Parking\":\"Street\",\"Hours\":\"Mon-Thu, Sun 5 pm - 10 pm & Fri-Sat 5 pm - 10:30 pm & Sat-Sun 10 am - 2 pm\",\"Take-out\":\"No\",\"Price Range\":\"$$$\",\"City\":\"Seattle\",\"Category\":\"Restaurants, American (New)\",\"Noise Level\":\"Average\",\"Wheelchair Accessible\":\"No\",\"Has TV\":\"No\",\"Address\":\"1411 N 45th St, Seattle, WA\",\"Longitude\":\"-122.340586\"}},\"data\":{\"ristorante-machiavelli-seattle-2\":6643,\"the-pink-door-seattle\":6357,\"maggianos-little-italy-bellevue\":5562,\"ristorante-mamma-melina-seattle\":5152,\"osteria-la-spiga-seattle\":4633,\"bizzarro-italian-cafe-seattle\":4500,\"buca-di-beppo-seattle\":3036,\"volterra-seattle\":2835,\"tavolata-seattle\":2352,\"barolo-ristorante-seattle\":2112,\"serafina-seattle\":2080,\"serious-pie-seattle\":1665,\"panevino-seattle\":1653,\"tutta-bella-neapolitan-pizzeria-seattle-9\":1323,\"perche-no-pasta-and-vino-seattle-2\":1254,\"cascina-spinasse-seattle\":1248,\"tropea-ristorante-italiano-redmond\":1066,\"salvatore-ristorante-seattle\":1044,\"il-bistro-seattle\":1040,\"via-tribunali-seattle\":992,\"palomino-restaurant-rotisseria-bar-seattle\":980,\"la-vita-e-bella-seattle\":969,\"ristorante-picolinos-seattle\":944,\"firenze-ristorante-italiano-bellevue-2\":899,\"assaggio-ristorante-seattle\":836,\"frankies-pizza-and-pasta-redmond\":820,\"il-fornaio-seattle\":806,\"la-rustica-seattle\":806,\"brads-swingside-cafe-seattle\":792,\"old-spaghetti-factory-seattle-2\":742,\"cantinetta-seattle\":682,\"tulio-ristorante-seattle\":610,\"romanos-macaroni-grill-seattle\":605,\"salumi-artisan-cured-meats-seattle-2\":550,\"how-to-cook-a-wolf-seattle\":540,\"list-seattle\":496,\"pasta-freska-seattle\":484,\"anchovies-and-olives-seattle\":483,\"lucia-italian-restaurant-kirkland\":450,\"il-terrazzo-carmine-seattle\":448,\"marcello-ristorante-seattle\":441,\"pomodoro-ristorante-italiano-seattle\":420,\"thats-amore-italian-restaurant-seattle\":406,\"tutta-bella-neapolitan-pizzeria-seattle-8\":385,\"angelinas-seattle\":384,\"branzino-seattle-3\":360,\"cafe-veloce-kirkland\":360,\"piatti-ristorante-and-bar-seattle\":360,\"pasta-nova-woodinville\":360,\"grazie-ristorante-italiano-bothell\":345,\"tilth-seattle\":330},\"query\":\"[ { v: 'good' }, { v: 'italian' } ]\",\"num\":345,\"suggestions\":[],\"time\":4379}}";
	private final List<SearchResultListener> resultListeners;
	private final List<SearchListener> searchListeners;

	private SearchResultEvent lastSearchResultEvent;

	private static RevminerClient client;
	
	private RevminerClient() {
		resultListeners = new ArrayList<SearchResultListener>();
		searchListeners = new ArrayList<SearchListener>();

		// TODO: set to null, currently test code
		List<Restaurant> res = new ArrayList<Restaurant>();
    res.add(new Restaurant("one"));
    res.add(new Restaurant("two"));
    res.add(new Restaurant("three"));
    res.add(new Restaurant("four"));
		lastSearchResultEvent = new SearchResultEvent(res);
	}
	
	public static RevminerClient Client() {
		if (client == null)
			client = new RevminerClient();
		return client;
	}

	public void addSearchResultListener(SearchResultListener listener) {
		resultListeners.add(listener);
	}
	
	public void addSearchListener(SearchListener listener) {
		searchListeners.add(listener);
	}

	// TODO: remove context from this interface once we actually implement the query logic
	public boolean sendSearchQuery(String query, Context context) {

		// TODO: actually perform query instead of making this little popup
        Toast.makeText(context, "Query: \"" + query +"\"", Toast.LENGTH_SHORT).show();

        for (SearchListener listener : searchListeners)
        	listener.onSearch(query);

        // TODO: Lets add some parallelism
        Log.d("revd", "Query for: " + query);

        String result = SimpleHttpClient.get(
            REST_URL + URLEncoder.encode(query).replace("+", "%20"));
        if (result == null) {
          Log.d("revd", "null result");
          return false;
        }
        Log.d("revd", "results received for " + query);

        // TODO: update once true query logic is implemented
        // TODO: handle case when we get "suggestions" or "match"
        try {
          JSONObject object = (JSONObject) new JSONTokener(result).nextValue();

          if (object.has("results")) {
            List<Restaurant> res = new ArrayList<Restaurant>();
            JSONObject results = object.getJSONObject("results");; 

            if (results.has("meta") && results.has("data")) {
              JSONObject meta = results.getJSONObject("meta");
              JSONObject data = results.getJSONObject("data");

              Iterator<String> places = data.keys();
              while (places.hasNext()) {
                String place = places.next();
                Log.d("revd", place);

                Restaurant restaurant = Restaurant.getInstance(place);
                if (restaurant == null) { // don't have it cached, we need to make an instance
                  restaurant = getRestaurantFromMeta(place, meta);
                }
                res.add(Restaurant.getInstance(place));
              }
            }

            notifySearchResultEvent(new SearchResultEvent(res));
          } else if (object.has("match")) {
            //TODO: Parse matched object 
            Log.d("revd", "Got full match back");
          }

        } catch (JSONException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        // !

		return false;
	}

	private static final Restaurant getRestaurantFromMeta(String name, JSONObject meta) {
	  String key = name;
	  HashMap<String, String> attributeMapping;
	  
    try {
      attributeMapping = new HashMap<String, String>();
      JSONObject attributes = attributes = meta.getJSONObject(key);
      
      Iterator<String> attributesIter = attributes.keys();
      while (attributesIter.hasNext()) {
        String attr = attributesIter.next();
        String value = attributes.getString(attr);
        attributeMapping.put(attr,  value);                  
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }

     return Restaurant.createInstance(key, attributeMapping);
	}

  public SearchResultEvent getLastSearchResultEvent() {
    return lastSearchResultEvent;
  }

  private void notifySearchResultEvent(SearchResultEvent e) {
    this.lastSearchResultEvent = e;
    for (SearchResultListener listener : resultListeners) {
      listener.onSearchResults(e);
    }
  }
  
  
}                                                                                                                                                                                                                            estaurant> res = new ArrayList<Restaurant>();
    res.add(new Restaurant("one"));
    res.add(new Restaurant("two"));
    res.add(new Restaurant("three"));
    res.add(new Restaurant("four"));
		lastSearchResultEvent = new SearchResultEvent(res);
	}
	
	public static RevminerClient Client() {
		if (client == null)
			client = new RevminerClient();
		return client;
	}

	public void addSearchResultListener(SearchResultListener listener) {
		resultListeners.add(listener);
	}
	
	public void addSearchListener(SearchListener listener) {
		searchListeners.add(listener);
	}

	// TODO: remove context from this interface once we actually implement the query logic
	public boolean sendSearchQuery(String query, Context context) {

		// TODO: actually perform query instead of making this little popup
        Toast.makeText(context, "Query: \"" + query +"\"", Toast.LENGTH_SHORT).show();

        for (SearchListener listener : searchListeners)
        	listener.onSearch(query);

        // TODO: Lets add some parallelism
        Log.d("revd", "Query for: " + query);

        String result = SimpleHttpClient.get(REST_URL + URLEncoder.encode(query));
        if (result == null) {
          Log.d("revd", "null result");
          return false;
        }
        Log.d("revd", "results received for " + query);

        
        // TODO: update once true query logic is implemented
        // TODO: handle case when we get "suggestions" or "match"
        List<Restaurant> res = new ArrayList<Restaurant>();
        try {
          JSONObject object = (JSONObject) new JSONTokener(result).nextValue();
          if (object.has("results")) {
            JSONObject results = object.getJSONObject("results");; 

            if (results.has("meta") && results.has("data")) {
              JSONObject meta = results.getJSONObject("meta");
              JSONObject data = results.getJSONObject("data");

              Iterator<String> places = data.keys();
              while (places.hasNext()) {
                String place = places.next();
                Log.d("revd", place);

                Restaurant restaurant = Restaurant.getInstance(place);
                if (restaurant == null) { // don't have it cached, we need to make an instance
                  restaurant = getRestaurantFromMeta(place, meta);
                }
                res.add(Restaurant.getInstance(place));
              }
            }
          }
          
          notifySearchResultEvent(new SearchResultEvent(res));
        } catch (JSONException e) {
          // TODO Auto-gˇˇÚ r    entryˇˇˇÚ lang   
  sˇˇˇ˙ 	ViewGroup    mContextˇˇˇ BufferedWriterˇˇˇÚ FileNotFoundExceptionˇˇˇÚ paramˇˇˇÛ ListActivity    ListView    resˇˇˇ¯ ATTR_LATITUDEˇˇˇˇ long    JSONTokenerˇˇˇ¯ List<SearchHistory>ˇˇˇÚ LENGTH_SHORTˇˇˇ¯ list    actionIdˇˇˇÒ intentˇˇˇÒ PADDINGˇˇˇÓ 	mOverlaysˇˇˇ http    netˇˇˇ¯ SearchHistory    Mapˇˇˇˇ attributesIterˇˇˇ¯ List<Restaurant>    labelˇˇˇÒ matchListenersˇˇˇ¯ results    mapˇˇˇÓ impl    	Exceptionˇˇˇı lvˇˇˇÚ keyˇˇˇ¯ resultListenersˇˇˇ¯ metaˇˇˇ¯ content   	 app    org    SearchResultListener   	 Doubleˇˇˇˇ HeaderElementˇˇˇÛ layout    pointˇˇˇÓ addressPartsˇˇˇˇ void   
	
 HttpResponse    SearchResultEvent   	 view    TabHostˇˇˇÒ charˇˇˇÛ RestaurantLocation    ItemizedOverlayˇˇˇ Toast    AdapterView    adapter    revminer   	 TabSpecˇˇˇÒ Integer    AlertDialogˇˇˇ SimpleHttpClientˇˇˇ¯ 	polarity4ˇˇˇÔ mainˇˇˇÒ MapControllerˇˇˇÓ itemˇˇˇ RestaurantReviewCategoryˇˇˇ˚ methods    GeoPointˇˇˇÓ 
EditorInfoˇˇˇÒ os    lastExactMat