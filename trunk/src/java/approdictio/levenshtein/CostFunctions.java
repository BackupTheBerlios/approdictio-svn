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
package approdictio.levenshtein;

/**
 * <p>
 * provides several implementations of {@link LevenCosts}.
 * </p>
 * 
 * @author harald
 * 
 */
public class CostFunctions {
  // Nothing to instantiate here.
  private CostFunctions() {}

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
