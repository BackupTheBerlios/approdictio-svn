package levenshtein;

/**
 * <p>
 * Defines the methods necessary to parameterize the cost of the
 * {@link LevenshteinMetric}. Some useful implementations can be found in
 * {@link CostFunctions}.
 * </p>
 * </p>
 * 
 * @author harald
 * 
 */
public interface LevenCosts {
  /**
   * <p>
   * must provide the cost of inserting or deleting a given character. Insertion
   * and deletion have to be identical to keep the distance metric symmetic.
   * </p>
   */
  int insdelCost(char c);

  /**
   * <p>
   * must provide the cost of replacing one character with the other. The result
   * must be symmetric with regard to exchanging the characters.
   * </p>
   * <p>
   * Question: Does it have to return zero if the two characters are equal?
   * </p>
   */
  int substCost(char c1, char c2);
}
