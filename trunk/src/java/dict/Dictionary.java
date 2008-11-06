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
package dict;

import java.util.List;

/**
 * <p>
 * defines a dictionary for approximate lookup. The underlying assumption is
 * that values of the parameter type <T> can be compared and their similarity
 * described by a numerical value of type {@code DTYPE}. This interface does
 * not define wether {@code DTYPE} describes a distance measure or even a
 * metric or whether it describes a similarity weight, where large values
 * denote higher similarity.
 * </p>
 * 
 * @param <T> the value type to be stored with any key
 * @param <DTYPE> is the type of values of the similarity measure, most
 *        likely something likely a numerical type like Integer or Double.
 */
public interface Dictionary<T, DTYPE> {

  /**
   * <p>
   * adds the given value to the dictionary.
   * </p>
   * 
   * @param value
   */
  void add(T value);

  /**
   * <p>
   * looks up the {@code queryValue} in the dictionary and returns values
   * that have similar keys, where similarity depends on the implementation.
   * The list returned can be empty. If it contains more than one element,
   * all elements of the list are equally similar to {@code queryValue}.
   * </p>
   * 
   * @param queryValue is the value for which similar values are sought
   * @return a list of result elements containing objects most similar to the
   *         query.
   */
  List<ResultElem<T, DTYPE>> lookup(T queryValue);
}
