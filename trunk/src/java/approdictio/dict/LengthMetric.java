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
 * is a trivial example of {@link IntMetric} that uses
 * the difference of string length as the distance value.
 * </p>
 * 
 * @author harald
 * 
 */
public class LengthMetric implements IntMetric<String> {

  public int d(String s1, String s2) {
    int d = s1.length() - s2.length();
    if( d < 0 ) d = -d;
    // int e = ((int)s1.charAt(0)) - ((int)s2.charAt(0));
    // if( e<0 ) e = -e;
    return d;
  }

}
