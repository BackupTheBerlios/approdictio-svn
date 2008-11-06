package levenshtein;

import dict.IntMetric;

/**
 * <p>
 * is an implementation of the Levenshtein edit distance (or metric). Objects of
 * this class can be parameterized by implemenations of {@link LevenCosts} to
 * tune the costs of the edit operations.
 * </p>
 * 
 * @author harald
 * 
 */
public class LevenshteinMetric implements IntMetric<String> {
  private final LevenCosts costs;

  // +********************************************************************
  /**
   * <p>
   * provides a metric with the default costs as provided by
   * {@link CostFunctions#defaultCosts}.
   * </p>
   */
  public LevenshteinMetric() {
    this.costs = CostFunctions.defaultCosts;
  }
  /**
   * <p>
   * creates a metric based on the given costs for edit operations. Example cost
   * function implementations can be found in {@link CostFunctions}.
   * </p>
   * 
   * @param c
   *          a cost function provider
   */
  public LevenshteinMetric(LevenCosts c) {
    this.costs = c;
  }
  // +********************************************************************
  /**
   * <p>
   * computes the Levenshtein edit distance between the given strings according
   * to the cost function provided at construction of this object.
   * </p>
   */
  public int d(String v1, String v2) {
    int m = v1.length() + 1;
    int n = v2.length() + 1;
    int[][] d = new int[m][n];

    for(int i = 0; i < m; i++)
      d[i][0] = i;
    for(int i = 1; i < n; i++)
      d[0][i] = i;

    for(int i = 1; i < m; i++) {
      char ch1 = v1.charAt(i - 1);
      for(int j = 1; j < n; j++) {
        char ch2 = v2.charAt(j - 1);
        int subst = d[i - 1][j - 1] + costs.substCost(ch1, ch2);
        int delete = d[i - 1][j] + costs.insdelCost(ch1);
        int insert = d[i][j - 1] + costs.insdelCost(ch2);
        d[i][j] = Math.min(Math.min(subst, delete), insert);
      }

    }
    // System.out.printf("%s--%s: %d%n", v1, v2, d[m-1][n-1]);
    return d[m - 1][n - 1];
  }
  // +********************************************************************
  /**
   * <p>for testing only.</p>
   */
  public static void main(String[] argv) {
    LevenshteinMetric lm = new LevenshteinMetric(CostFunctions.caseIgnore);
    for(int i = 0; i < argv.length - 1; i += 2) {
      int d = lm.d(argv[i], argv[i + 1]);
      System.out.printf("%s--%s: %d%n", argv[i], argv[i + 1], d);
    }
  }
}
