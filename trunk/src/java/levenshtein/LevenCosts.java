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
