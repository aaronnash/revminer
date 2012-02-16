package revminer.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RestaurantAttribute {
  private final String name;
  private final List<AttributeValue> values;

  public RestaurantAttribute(String name, List<AttributeValue> values) {
    this.name = name;
    // Defense copy + store as an immutable list
    this.values = Collections.unmodifiableList(new ArrayList<AttributeValue>(values));
  }

	public String getName() {
		return name;
	}

	public List<AttributeValue> getValues() {
	  return values;
	}

	/**
	 * Returns a string representation of the object for debugging purposes.
	 */
	@Override
	public String toString() {
	  StringBuilder sb = new StringBuilder();
	  sb.append("{\"" + name + "\": ");
	  for (AttributeValue value : getValues()) {
	    sb.append(value.toString());
	    sb.append(", ");
	  }

	  if (getValues().size() > 0) {
	    sb.delete(sb.length() - 2, sb.length());
	  }

	  sb.append("}");

	  return sb.toString();
	}
}
