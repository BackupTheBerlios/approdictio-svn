package dict;

/**
 * <p>
 * Defines a function to compute an integer valued metric.
 * </p>
 * 
 * @author harald
 * 
 */
public interface IntMetric<T> {
  /**
   * <p>
   * computes the distance between the two given objects. This distance function
   * must satisfy the requirements of a metric:
   * </p>
   * <ul>
   * <li>The result must be zero if and only if the two objects are equal.</li>
   * <li>The function must be symmetric.</li>
   * <li>The function must satisfy the triangular inequality.</li>
   * </ul>
   * <p>
   * It follows that the function must also return non negative values only.
   * </p>
   * 
   * @return the distance between the objects given.
   */
  int d(T v1, T v2);
}
