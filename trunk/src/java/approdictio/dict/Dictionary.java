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

import java.util.List;

/**
 * <p>
 * defines a dictionary for approximate lookup. The underlying assumption is
 * that values of the parameter type {@code T} can be compared and their similarity
 * described by a numerical value of type {@code DTYPE}. This interface does
 * not define whether {@code DTYPE} describes a distance measure or even a
 * metric or whether it describes a similarity weight, where large values
 * denote higher similarity.
 * </p>
 * 
 * @param <T> the type of values stored in the dictionary
 * @param <DTYPE> the type of values of the similarity measure, usually
 *        some numeric type.
 */
public interface Dictionary<T, DTYPE> {

  /**
   * <p>
   * adds the given value to the dictionary.
   * </p>
   */
  void add(T value);

  /**
   * <p>
   * looks up {@code queryValue} in the dictionary and returns similar
   * values. The similarity depends on the implementation. An implementation
   * is allowed to return an empty list, if no element in the dictionary is
   * sufficiently similar to {@code queryValue}. If the result contains more
   * than one element, all its elements are equally similar to {@code
   * queryValue}.
   * </p>
   * 
   * @return a possibly empty list of result elements holding dictionary
   *         entries most similar to the query. If the query value is
   *         contained in the dictionary, the result would typically include
   *         it, as it should be most similar to itself. If this is not
   *         intended, use {@link #lookupDistinct lookupDistinct}.
   */
  List<ResultElem<T,DTYPE>> lookup(T queryValue);
  
  /**
   * <p>
   * looks up {@code queryValue} in the dictionary and returns similar
   * values, but never the {@code queryValue} itself.
   * </p>
   * 
   * @return a possibly empty list of result elements holding dictionary
   *         entries most similar to the query. If the query value is
   *         contained in the dictionary, it is disregarded to make sure
   *         other values are found.
   */
  List<ResultElem<T,DTYPE>> lookupDistinct(T queryValue);
  
}
