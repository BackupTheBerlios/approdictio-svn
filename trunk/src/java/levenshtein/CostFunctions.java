package levenshtein;

/**
 * <p>
 * provides several implementations of {@link LevenCosts}.
 * </p>
 * 
 * @author harald
 * 
 */
public class CostFunctions {
  // +********************************************************************
  /**
   * <p>
   * is a {@link LevenCosts} implementation that ignores character case. The
   * cost of replacing an uppercase character with its lowercase equivalent or
   * vice versa has zero cost.
   * </p>
   */
  public static final LevenCosts caseIgnore = new LevenCosts() {
    public int insdelCost(char c) {
      return 1;
    }

    public int substCost(char c1, char c2) {
      if( Character.toLowerCase(c1) == Character.toLowerCase(c2) ) return 0;
      return 1;
    }
  };

  // +********************************************************************
  /**
   * <p>
   * provides the default implementation of {@link LevenCosts} where all
   * operations have cost 1.
   * </p>
   */
  public static final LevenCosts defaultCosts = new LevenCosts() {
    public int insdelCost(char c) {
      return 1;
    }
    public int substCost(char c1, char c2) {
      if( c1 == c2 ) return 0;
      return 1;
    }
  };
  // +********************************************************************
}
