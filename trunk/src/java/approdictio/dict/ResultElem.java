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

import java.util.Comparator;

/**
 * <p>
 * defines result elements returned by
 * {@link Dictionary#lookup(Object) lookup()}.
 * </p>
 */
public class ResultElem<T, DTYPE> {
  /** the value found in the {@link Dictionary} */
  public final T value;

  /** the distance of this value from the queried value */
  public final DTYPE d;

  /* +***************************************************************** */
  public static final Comparator<ResultElem<?, Integer>> cmpResult =
    new Comparator<ResultElem<?, Integer>>() {
    public int compare(ResultElem<?, Integer> arg0,
                       ResultElem<?, Integer> arg1)
    {
      return arg0.d - arg1.d;
    }
  };
  
  /* +***************************************************************** */
  public static final Comparator<ResultElem<?, Integer>> cmpResultInv =
    new Comparator<ResultElem<?, Integer>>() {
    public int compare(ResultElem<?, Integer> arg0,
                       ResultElem<?, Integer> arg1)
    {
      return arg1.d - arg0.d;
    }
  };
  /* +***************************************************************** */
  ResultElem(T value, DTYPE d) {
    this.value = value;
    this.d = d;
  }
  /*+******************************************************************/
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(value).append(':').append(d);
    return sb.toString();
  }
}