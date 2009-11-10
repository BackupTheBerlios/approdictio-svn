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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import approdictio.levenshtein.CostFunctions;
import approdictio.levenshtein.LevenshteinMetric;
import static approdictio.dict.Util.*;

/**
 * <p>
 * a dictionary for fast approximate lookup of words. This implementation uses
 * n-gram indexing to retrieve candidate words.
 * </p>
 * <p>
 * <b>Background:</b> When a term is added to the dictionary, all its
 * overlapping n-grams are computed. A map is prepared that maps n-grams to
 * the terms in which they occur. When looking up a term, the term's n-grams
 * are again computed and used to retrieve candidate terms. Candidate terms
 * have at least one n-gram in common with the query term. Further the list
 * of candidates is trimmed by parameter {@code maxDist}. The ngram-metric
 * used is |union(P,Q)|-|intersection(P,Q)| for n-gram sets P and Q.
 * </p>
 */
public class NgramDict implements Dictionary<String, Integer> {

  private final int ngramLen;

  private final char noChar = '\u00B7';

  private final IntMetric<String> metric;

  private final int maxDist;

  // an index mapping ngrams to their strings.
  private final Map<String, Set<String>> index =
      new HashMap<String, Set<String>>();
  /* +***************************************************************** */
  /**
   * <p>
   * creates a dictionary that uses n-grams with {@code ngramLen} characters.
   * </p>
   * 
   * @param ngramLen is the length of the n-grams use, must be greater zero
   * @param metric is the metric used to compare strings <em>after</em>
   *        candidates were retrieved using ngram-metric.
   * @param maxDist is used as a cutoff when retrieving candidates according
   *        to ngram-metric (2 is a good candidate for most cases)
   * 
   * @throws IllegalArgumentException if {@code ngramLen} is not greater
   *         zero.
   */
  public NgramDict(int ngramLen, IntMetric<String> metric, int maxDist) {
    if( ngramLen<1 ) {
      throw new IllegalArgumentException("n must be greater zero "
          +" but is "+ngramLen);
    }
    this.ngramLen = ngramLen;
    this.metric = metric;
    this.maxDist = maxDist;
  }
  /* +***************************************************************** */
  public void add(String value) {

    for(String ngram : ngrams(value)) {
      Set<String> values = index.get(ngram);
      if( values==null ) {
        index.put(ngram, values = new HashSet<String>());
      }
      values.add(value);
    }
    // System.out.printf("%s->%s%n", value, ngrams);
  }
  /* +***************************************************************** */
  private Set<String> ngrams(String s) {
    char[] padded = padWithNoChar(s);
    int numNgrams = padded.length-ngramLen+1;

    Set<String> result = new HashSet<String>(numNgrams);

    for(int i = 0; i<numNgrams; i++) {
      result.add(new String(padded, i, ngramLen));
    }
    return result;
  }
  /* +***************************************************************** */
  private char[] padWithNoChar(String s) {
    int halfNgramRoundedUp = (ngramLen+1)/2;
    char[] result = new char[s.length()+2*halfNgramRoundedUp];
    Arrays.fill(result, noChar);
    s.getChars(0, s.length(), result, halfNgramRoundedUp);
    return result;
  }
  /* +***************************************************************** */
  /**
   * <p>
   * looks up elements in the dictionary according to ngram metric. Ngram
   * metric for terms p and q is computed based on their respective ngram
   * sets P and Q as |union(P,Q)|-|intersection(P,Q)|. This is indeed a
   * metric.
   * </p>
   * 
   * @see <a
   *      href="http://en.wikipedia.org/wiki/Symmetric_difference#Symmetric_difference_on_measure_spaces">Wikipedia
   *      Symmetric Difference</a>
   */
  private List<ResultElem<String, Integer>> ngramSimilar(String queryValue) {

    List<ResultElem<String, Integer>> result = newResultList(0);

    Set<String> queryNgrams = ngrams(queryValue);

    Set<String> termsSeen = new HashSet<String>();
    int minDistSeen = Integer.MAX_VALUE;

    // for each n-gram of the queryValue fetch the terms that also contain
    // that value
    for(String ngram : queryNgrams) {
      Set<String> terms = index.get(ngram);
      if( terms==null ) continue;

      for(String termFound : terms) {
        if( termsSeen.contains(termFound) ) continue;
        termsSeen.add(termFound);

        int symDist = symmetricDistance(queryNgrams, ngrams(termFound));

        // drop bad candidates as early as possible
        if( !eligible(symDist, minDistSeen) ) continue;

        if( symDist<minDistSeen ) minDistSeen = symDist;
        result.add(newResultElem(termFound, symDist));
        // System.out.printf("++++ added %s%n", termFound);
      }
    }
    result = filterEligible(result, minDistSeen);

    return result;
  }
  /* +***************************************************************** */
  private List<ResultElem<String, Integer>> 
          filterEligible(List<ResultElem<String, Integer>> candidates,
                         int minDistSeen)
  {
    List<ResultElem<String, Integer>> result =
        newResultList(candidates.size());

    for(ResultElem<String, Integer> cand : candidates) {
      if( eligible(cand.d, minDistSeen) ) result.add(cand);
    }
    return result;
  }
  /* +***************************************************************** */
  private boolean eligible(int candidateDistance, int bestDistance) {
    // TODO: can this be optimized to use a smaller margin than ngramLen? No,
    // it is not obvious why ngramLen is a good choice, but it works.
    return candidateDistance-ngramLen<=bestDistance;
  }
  /* +***************************************************************** */
  private int symmetricDistance(Set<?> precious, Set<?> scratch) {
    Set<Object> union = new HashSet<Object>(precious.size()+scratch.size());
    union.addAll(precious);
    union.addAll(scratch);
    scratch.retainAll(precious);
    return union.size()-scratch.size();
  }
  /* +***************************************************************** */
  /**
   * @throws ConcurrentModificationException may be thrown in cases where the
   *         dictionary is updated while a lookup tries to find a query
   *         value.
   */
  public List<ResultElem<String,Integer>> lookup(String queryValue) {

    List<ResultElem<String, Integer>> tmp = ngramSimilar(queryValue);
    List<ResultElem<String, Integer>> result = newResultList(0);
    for(ResultElem<String, Integer> re : curate(queryValue, tmp))
      result.add(re);
    // return result;
    return curate(queryValue, tmp);
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
  private List<ResultElem<String, Integer>> 
          curate(String query, List<ResultElem<String, Integer>> candidates)
  {
    List<ResultElem<String, Integer>> result =
        newResultList(1+candidates.size()/2);

    int minDistSeen = Integer.MAX_VALUE;

    for(ResultElem<String, Integer> re : candidates) {
      int d = metric.d(query, re.value);

      // drop insufficient candidates early
      if( d>maxDist||d>minDistSeen ) continue;

      minDistSeen = d;
      result.add(newResultElem(re.value, d));
    }

    return filterBest(result, minDistSeen);
  }
  /* +***************************************************************** */
  private List<ResultElem<String, Integer>> 
          filterBest(List<ResultElem<String, Integer>> candidates,
                     int bestDist)
  {
    List<ResultElem<String, Integer>> result =
        newResultList(candidates.size());

    for(ResultElem<String, Integer> cand : candidates) {
      if( cand.d==bestDist ) result.add(cand);
    }
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
    NgramDict t = new NgramDict(4, metric, 2);
    Util.readFileDict(argv[0], t);
    System.out.println("starting lookup");
    long start = System.currentTimeMillis();
    int r = 0;
    for(int i = 1; i<argv.length; i++) {
      List<ResultElem<String, Integer>> l = t.lookup(argv[i]);
      r = r+l.size();
      if( i%1000==0 ) System.out.println(i);
      /*
       * * / System.out.printf("%s -->", argv[i]); for(ResultElem<String,
       * Integer> e : l) { System.out.printf(" %s", e); }
       * System.out.println(); /*
       */
    }
    long end = System.currentTimeMillis();
    double avg = ((double)(end-start))/(double)(argv.length-1);
    System.out.printf("avg lookup: %.3fms%n", avg);
  }
}
