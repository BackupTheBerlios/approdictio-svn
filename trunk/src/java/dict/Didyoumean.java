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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * is an implementation of a <em>did you mean</em> based on a
 * {@link Dictionary}. In addition to similarity lookup &mdash or rather
 * distance based approximate lookup &mdash; the list of equal distance
 * results is further weighted and sorted according to a weight available for
 * each dictionary term. Typically, the weights can be frequencies of the
 * term occuring in a corpus.
 * </p>
 * <p>
 * To create a {@code Didyoumean}, use one of the factory methods. The
 * backing {@code Dictionary} depends on the facotry method used.
 * </p>
 * 
 */
public class Didyoumean {
  private final Dictionary<String, Integer> dict;

  private final Map<String, Integer> weights;

  /* +***************************************************************** */
  private Didyoumean(Dictionary<String, Integer> dict) {
    this.dict = dict;
    weights = new HashMap<String, Integer>();
  }
  /* +***************************************************************** */
  /**
   * <p>
   * creates a {@code Didyoumean} backed by an {@link NgramDict}.
   * </p>
   */
  public Didyoumean instanceNgramDict(int n, char noChar,
                                      IntMetric<String> metric, int maxDist)
  {
    return new Didyoumean(new NgramDict(n, noChar, metric, maxDist));
  }
  /* +***************************************************************** */
  /**
   * <p>
   * creates a {@code Didyoumean} backed by a {@link BKTree}.
   * </p>
   */
  public Didyoumean instanceBKTree(IntMetric<String> metric, int maxDist) {
    final Dictionary<String, Integer> d = new BKTree<String>(metric, maxDist);
    return new Didyoumean(d);
  }
  /* +***************************************************************** */
  /**
   * <p>
   * adds a term together with its assigned weight.
   * </p>
   */
  public void add(String term, int weight) {
    Integer w = weights.get(term);
    if( w != null ) {
      weights.put(term, new Integer(weight + w.intValue()));
    } else {
      weights.put(term, new Integer(weight));
      dict.add(term);
    }
  }
  /* +***************************************************************** */
  /**
   * <p>
   * looks up the word in the internal {@link Dictionary} and then filters
   * for the term with the heighest weight according to the weights provied
   * when {@link #add add()}ing a term.
   * </p>
   * 
   * @return the list of stored elements most similar to {@code word} that
   *         have the highest weight assigned.
   */
  public List<ResultElem<String, Integer>> lookup(String word) {
    List<ResultElem<String, Integer>> tmp = dict.lookup(word);

    List<ResultElem<String, Integer>> result =
        new ArrayList<ResultElem<String, Integer>>();

    for(ResultElem<String, Integer> e : tmp) {
      ResultElem<String, Integer> newElem =
          new ResultElem<String, Integer>(e.value, weights.get(e.value));
      result.add(newElem);
    }
    Collections.sort(result, ResultElem.cmpResult);
    return result;
  }
}
