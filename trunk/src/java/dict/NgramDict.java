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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import levenshtein.CostFunctions;
import levenshtein.LevenshteinMetric;

/**
 * <p>
 * a dictionary for fast approximate lookup using n-grams.
 * </p>
 * <p>
 * <b>Background:</b> When a term is added to the dictionary, all its
 * overlapping n-grams are computed. A map is prepared that maps n-grams to
 * the terms in which they occur. When looking up a term, the term's n-grams
 * are again computed and used to retrieve all terms that contain the same
 * n-grams. Ther terms with the highest number of matches are returned.
 * </p>
 */
public class NgramDict implements Dictionary<String, Integer> {
  // the length of the n-grams
  private final int N;

  private final char noChar;

  private final IntMetric<String> metric;

  private final int maxDist;

  // an index mapping ngrams to their strings.
  private final Map<String, Set<String>> index =
      new HashMap<String, Set<String>>();
  /* +***************************************************************** */
  // public static final class ResultElem extends
  // dict.ResultElem<String, Integer> {
  // public ResultElem(String value, Integer d) {
  // super(value, d);
  // }
  // }
  /* +***************************************************************** */
  /**
   * <p>
   * creates a dictionary that uses n-grams with {@code n} characters.
   * </p>
   * 
   * @param n is the length of the n-grams use, must be greater zero
   * @param noChar is the character used to pad n-grams at the start of
   *        words. This character should better not appear in any n-grams.
   * 
   * @throws IllegalArgumentException if {@code n} is not greater zero.
   */
  public NgramDict(int n, char noChar, IntMetric<String> metric, int maxDist) {
    if( n < 1 ) {
      throw new IllegalArgumentException("n must be greater zero "
          + " but is " + n);
    }
    this.N = n;
    this.noChar = noChar;
    this.metric = metric;
    this.maxDist = maxDist;
  }
  /* +***************************************************************** */
  private Set<String> ngrams(String s) {
    Set<String> result = new HashSet<String>(s.length());
    char buf[] = new char[N];
    Arrays.fill(buf, noChar);
    for(int i = 0; i < s.length(); i++) {
      char ch = s.charAt(i);
      System.arraycopy(buf, 1, buf, 0, N - 1);
      buf[N - 1] = ch;
      result.add(new String(buf));
    }
    return result;
  }
  /* +***************************************************************** */
  /**
   * <p>
   * adds the {@code value} to the dictionary.
   * </p>
   */
  public void add(String value) {
    Set<String> ngrams = ngrams(value);
    for(String ngram : ngrams) {
      Set<String> values = index.get(ngram);
      if( values == null ) {
        index.put(ngram, values = new HashSet<String>());
      }
      values.add(value);
    }
  }
  /* +***************************************************************** */
  private static final class MutInt {
    public int value;
    public MutInt(int value) {
      this.value = value;
    }
  }
  /* +***************************************************************** */
  /**
   * <p>
   * look up a term and return the top matches accordingn to
   * ngram-similarity. Ngram-similarity counts the number of common ngrams
   * between the query term and terms in the dictionary. Internally,
   * {@link #lookup lookup()} uses this function and only computes the metric
   * distance on top for the terms returned here.
   * </p>
   */
  public List<ResultElem<String, Integer>> lookupSimilarity(String queryValue)
  {
    // map result terms to a count of the n-grams for which they are found
    Map<String, MutInt> m = new HashMap<String, MutInt>();

    Set<String> queryNgrams = ngrams(queryValue);

    // no. of score points still to get
    int rest = queryNgrams.size();

    // best score so far
    int best = 0;

    // for each n-gram of the queryValue fetch the terms that also contain
    // that value and count in m how often each term is found
    for(String ngram : queryNgrams) {
      Set<String> terms = index.get(ngram);
      rest -= 1;
      if( terms == null ) continue;
      for(String termFound : terms) {
        MutInt re = m.get(termFound);
        int cfound;
        if( re == null ) cfound = 1;
        else cfound = re.value + 1;
        // No need to deal with loosers that cannot catch up anymore with
        // best
        if( cfound + rest >= best ) {
          if( cfound > best ) best = cfound;
          if( re == null ) {
            m.put(termFound, re = new MutInt(1));
          }
          re.value = cfound;
          // System.out.printf("%s of %s, c=%d, rest=%d%n", ngram, termFound,
          // re.value, rest);
        }
      }
    }

    // create the result list while keeping only the best scoring elements
    List<ResultElem<String, Integer>> result =
        new ArrayList<ResultElem<String, Integer>>(4 + m.size() / 4);
    for(Entry<String, MutInt> e : m.entrySet()) {
      MutInt v = e.getValue();
      if( v.value < best ) continue;
      result.add(new ResultElem<String, Integer>(e.getKey(), v.value));
    }
    return result;
  }
  /* +***************************************************************** */
  public List<ResultElem<String, Integer>> lookup(String queryValue) {
    List<dict.ResultElem<String, Integer>> tmp =
        lookupSimilarity(queryValue);
    return curate(queryValue, tmp, metric, maxDist);
  }
  /* +***************************************************************** */
  /**
   * <p>
   * curates a result provided by {@link #lookup(Object) lookup()} such that
   * it computes real distances according to the given metric. The method
   * compares the value in each result element with the given query and keeps
   * only those, which are no more distant than {@code maxDist}. Calling
   * this method really only makes sense as a post processing of the lookup()
   * method and requires two constraints:
   * </p>
   * <ol>
   * <li>The {@code query} was used to for lookup() too.</li>
   * <li>The metric is somehow compatible with n-gram similarity.</li>
   * </ol>
   * <p>
   * An example metric to use is a {@link LevenshteinMetric}.
   * </p>
   * 
   * @return contains the metric distance values computed, not the original
   *         trigram similarities and is sorted in ascending order of the
   *         metric distance values.
   */
  private List<ResultElem<String, Integer>> curate(
                                                   String query,
                                                   List<ResultElem<String, Integer>> l,
                                                   IntMetric<String> metric,
                                                   int maxDist)
  {
    List<ResultElem<String, Integer>> result =
        new ArrayList<ResultElem<String, Integer>>(1 + l.size() / 2);
    for(ResultElem<String, Integer> re : l) {
      int d = metric.d(query, re.value);
      if( d > maxDist ) continue;
      result.add(new ResultElem<String, Integer>(re.value, d));
    }
    Collections.sort(result, ResultElem.cmpResult);
    return result;
  }
  /* +***************************************************************** */
  /**
   * <p>
   * for testing purposes only.
   * </p>
   * 
   * @param argv
   */
  public static void main(String[] argv) throws Exception {
    IntMetric<String> metric =
        new LevenshteinMetric(CostFunctions.caseIgnore);
    NgramDict t = new NgramDict(3, '$', metric, 2);
    Util.readFileDict(argv[0], t);
    System.out.println("starting lookup");
    long start = System.currentTimeMillis();
    int r = 0;
    for(int i = 1; i < argv.length; i++) {
      List<ResultElem<String, Integer>> l = t.lookup(argv[i]);
      r = r + l.size();
      if( i % 1000 == 0 ) System.out.println(i);
      /* */
      System.out.printf("%s -->", argv[i]);
      for(ResultElem<String, Integer> e : l) {
        System.out.printf(" %s", e);
      }
      System.out.println();
      /* */
    }
    long end = System.currentTimeMillis();
    double avg = ((double) (end - start)) / (double) (argv.length - 1);
    System.out.printf("avg lookup: %.3fms%n", avg);
  }
}
