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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.List;

import approdictio.levenshtein.LevenshteinMetric;


/**
 * <p>
 * An dicionary implementation using <em>Burkhard Keller Tree</em>s. This
 * data structure allows to store objects of a class <T> such that
 * approximate retrieval is efficient. A necessary condition is, that on
 * class T a metric can be defined and provided as an implemenationn of
 * {@link IntMetric}.
 * <p>
 * <p>
 * In a typical use case of this class, you first {@link #add(Object) add()}
 * values to the tree. Then call {@link #lookup(Object) lookup(query)} to
 * find stored values that are at most {@code d} away from your {@code query},
 * where {@code d} is the maximum distance specified in the constructor.
 * </p>
 * 
 * <p>
 * There is currently no way to delete values from the tree.
 * </p>
 * 
 * @param <V> denotes the type of objects to be stored in teh tree. To use
 *        the BKTree, additionally a metric {@link IntMetric} must be defined
 *        on {@code T}
 * 
 * @author harald
 * 
 * @see <a href="http://portal.acm.org/citation.cfm?id=362003.362025">paper
 *      by Burkhard and Keller</a>
 * @see <a href="http://en.wikipedia.org/wiki/BK-tree">English Wikipedia on
 *      BK-Trees</a>
 */

public class BKTree<V> implements Dictionary<V, Integer> {
  private BKNode<V> root;

  private final IntMetric<V> metric;

  private final int maxDist;

  // +********************************************************************
  /**
   * <p>
   * creates a BKTree to store objects of type T while using the metric
   * provided to organize lookup.
   * </p>
   * 
   * @param maxDist is used when looking up values to restrict to those that
   *        are no farther away from the query than {@code maxDist}.
   */
  public BKTree(IntMetric<V> metric, int maxDist) {
    this.metric = metric;
    this.maxDist = maxDist;
  }

  // +********************************************************************
  private interface LinkTable<T> {
    BKNode<T> get(int d);
    BKNode<T> set(int d, BKNode<T> node);
    int size();
  }

  // +********************************************************************
  private static class ArrayLinkTable<T> implements LinkTable<T> {
    @SuppressWarnings("unchecked")
    private BKNode<T>[] a = new BKNode[0];

    public int size() {
      return a.length;
    }
    public BKNode<T> get(int d) {
      if( d >= a.length ) return null;
      else return a[d];
    }

    public BKNode<T> set(int d, BKNode<T> node) {
      if( d >= a.length ) {
        @SuppressWarnings("unchecked")
        BKNode<T>[] tmp = new BKNode[d + 3];
        System.arraycopy(a, 0, tmp, 0, a.length);
        a = tmp;
      }
      BKNode<T> old = a[d];
      a[d] = node;
      return old;
    }
  }

  // +********************************************************************
  private static class BKNode<V> {
    private final V value;

    private final LinkTable<V> links;

    public BKNode(V value) {
      this.value = value;
      this.links = new ArrayLinkTable<V>();
    }
    public V getValue() {
      return value;
    }
    public void set(int d, BKNode<V> node) {
      links.set(d, node);
    }
    public BKNode<V> get(int d) {
      return links.get(d);
    }
    public void dump(Appendable out, int d, String indent) {
      Formatter f = new Formatter(out);
      f.format("%s%d: %s%n", indent, d, value);
      int l = links.size();
      for(int i = 0; i < l; i++) {
        BKNode node = links.get(i);
        if( node == null ) continue;
        node.dump(out, i, "  " + indent);
      }
    }
  }

  // +********************************************************************
  private void add(BKNode<V> node, V token) {
    int d = metric.d(node.getValue(), token);
    BKNode<V> child = node.get(d);
    if( child == null ) {
      node.set(d, new BKNode<V>(token));
    } else {
      add(child, token);
    }
  }
  // +********************************************************************
  /**
   * <p>
   * for debugging only.
   * </p>
   */
  public void dump(Appendable out) {
    if( root == null ) return;
    root.dump(out, -1, "");
  }
  // +********************************************************************
  /**
   * <p>
   * adds the value to the tree.
   * </p>
   */
  public void add(V value) {
    if( root == null ) {
      root = new BKNode<V>(value);
      return;
    }
    add(root, value);
  }
  // +********************************************************************
  // public String nearest(String queryToken) {
  // return null;
  // }
  // +********************************************************************
  private void lookup(BKNode<V> node, List<ResultElem<V, Integer>> result,
                      V value, int maxDist)
  {
    int d = metric.d(node.getValue(), value);
    if( d <= maxDist )
      result.add(new ResultElem<V, Integer>(node.getValue(), d));
    int from = d - maxDist;
    if( from < 0 ) from = 0;
    int to = d + maxDist;
    for(int i = from; i <= to; i++) {
      BKNode<V> child = node.get(i);
      if( child == null ) continue;
      lookup(child, result, value, maxDist);
    }
  }
  // +********************************************************************
  /**
   * <p>
   * looks up the given value and returns all values stored that are at most
   * {@code maxDist} away from the given value. In particular a value
   * {@code x} is returned, if {@code metric.d(queryValue, x)<=maxDist}.
   * </p>
   */
  public List<ResultElem<V, Integer>> lookup(V queryValue) {
    List<ResultElem<V, Integer>> result =
        new ArrayList<ResultElem<V, Integer>>();
    lookup(root, result, queryValue, maxDist);
    if( result.size()==0 ) return result;
    
    Collections.sort(result, new Comparator<ResultElem<V, Integer>>() {
      public int compare(ResultElem<V, Integer> arg0,
                         ResultElem<V, Integer> arg1)
      {
        return arg0.d - arg1.d;
      }
    });

    // leave over only the closes elements
    int best = result.get(0).d;
    int l=1;
    while( l<result.size() && result.get(l).d==best) l += 1;
    return result.subList(0, l);
  }
  // +********************************************************************
  /**
   * <p>
   * for testing purposes only.
   * </p>
   * 
   * @param argv
   */
  public static void main(String[] argv) throws Exception {
    BKTree<String> t = new BKTree<String>(new LevenshteinMetric(), 2);
    Util.readFileDict(argv[0], t);

    // t.dump(System.out);
    int sum = 0;
    long start = System.currentTimeMillis();
    for(int i = 1; i < argv.length; i++) {
      List<ResultElem<String, Integer>> l = t.lookup(argv[i]);
      sum = sum + l.size();
      if( i % 1000 == 0 ) System.out.println(i);
      /**********************************************************************
       * System.out.printf("%s -->", argv[i]); for(ResultElem<String,
       * Integer> e : l) { System.out.printf(" %s", e); }
       * System.out.println();
       */
    }
    long end = System.currentTimeMillis();
    double avg = ((double) (end - start)) / (double) (argv.length - 1);
    System.out.printf("avg lookup: %.3fms%n", avg);
  }
}
