package revminer.common;

/**
 * Enumeration specifying the category of the attributes
 */
 public enum ReviewCategory {
    REVIEW_FOOD("Food"),
    REVIEW_SERVICE("Service"),
    REVIEW_DECOR("Decor"),
    REVIEW_OVERALL("Overall"),
    REVIEW_OTHER("Other");

    private final String name;

    private ReviewCategory(String name) {
      this.name = name;
    }

    // There's only 5 values so we don't need some cool look up table
    public static ReviewCategory fromName(String name) {
      for (ReviewCategory cat : ReviewCategory.values()) {
        if (cat.name.equals(name)) {
          return cat;
        }
      }

      throw new IllegalArgumentException(
          "Argument must be the name of a ReviewCategory");
    }

    @Override
    public String toString() {
      return name;
    }
  }