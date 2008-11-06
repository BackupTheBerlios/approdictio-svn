// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; 
// version 2.1 of the License.

// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.

// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
package approdictio.dict;

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
