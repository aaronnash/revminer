package revminer.common;

import java.util.HashMap;

public class AttributeValue {
  public static final HashMap<AttributeValue, AttributeValue> instances
      = new HashMap<AttributeValue, AttributeValue>();
  private final String name;
  private final double polarity;

  /**
   * Constructs a new AttributeValue.
   * 
   * @param name Name of the value
   * @param polarity Polarity of the value. Must be within the range [0d,5d]
   */
  private AttributeValue(String name, double polarity) {
    this.name = name;
    this.polarity = polarity;
  }

  /**
   * Factory method for creating instances of this class. Uses a previous
   * instance if available.
   */
  public static AttributeValue create(String name, double polarity) {
    AttributeValue value = new AttributeValue(name, polarity);
    if (instances.containsKey(value)) {
      return instances.get(value);
    }

    return value;
  }

  public String getName() {
    return name;
  }

  public double getPolarity() {
    return polarity;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof AttributeValue)) {
      return false;
    }

    AttributeValue value = (AttributeValue)other;
    return getName().equals(value.getName())
        && getPolarity() == value.getPolarity();
  }

  @Override
  public int hashCode() {
    return getName().hashCode() + (int)(293 * getPolarity());
  }

  @Override
  public String toString() {
    return "{\"" + name + "\", " + polarity + "}"; 
  }
}
