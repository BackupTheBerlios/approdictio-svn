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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import approdictio.levenshtein.CostFunctions;
import approdictio.levenshtein.LevenshteinMetric;
import static approdictio.dict.Util.*;
/**
 * <p>
 * is an implementation of a <em>did you mean</em> based on a
 * {@link Dictionary}. In addition to similarity lookup &mdash or rather
 * distance based approximate lookup &mdash; the list of equal distance
 * results is further weighted and sorted according to a weight available for
 * each dictionary term. The weights can be frequencies of the
 * term occuring in a corpus.
 * </p>
 * <p>
 * To create a {@code Didyoumean}, use one of the factory methods. The
 * backing {@code Dictionary} depends on the facotry method used. While
 * {@link NgramDict} ({@link #instanceNgramDict instanceNgramDict}) seems
 * to be about three times faster (in completely unscientific comparisons),
 * the {@link BKTree} ({@link #instanceBKTree instanceBKTree}) provides a
 * 100%-Levenshtein implementation for the term distance.
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
  public static Didyoumean instanceNgramDict(int n,
                                             IntMetric<String> metric,
                                             int maxDist)
  {
    return new Didyoumean(new NgramDict(n, metric, maxDist));
  }
  /* +***************************************************************** */
  /**
   * <p>
   * creates a {@code Didyoumean} backed by a {@link BKTree}.
   * </p>
   */
  public static Didyoumean instanceBKTree(IntMetric<String> metric,
                                          int maxDist)
  {
    final Dictionary<String, Integer> d =
        new BKTree<String>(metric, maxDist);
    return new Didyoumean(d);
  }
  /* +***************************************************************** */
  /**
   * <p>
   * adds a term together with its assigned weight. Higher weights are
   * better. The weight can be the frequency of a term in a corpus.
   * If the term being added exists already in the dictionary, the given
   * {@code weight} is added to the weight already stored.
   * </p>
   */
  public void add(String term, int weight) {
    //System.out.printf("dym add: [%s:%d]%n", term, weight);
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
   * feeds the {@code Didyoumean} with terms and weights from a file. The
   * file format is line based. Each line must contain the term, the
   * separator character provided and an integer weight. Both, term and
   * weight, are trimmed before use.
   * </p>
   * 
   * @param fname is the file to read
   * @param separator is the character used in the file to separate the term
   *        from the weight
   * @param encoding is the character encoding of the file to read
   * @throws FileFormatException if the file contains a line that does not
   *         have the format described above.
   */
  public void addFile(String fname, char separator, String encoding)
    throws IOException, FileFormatException
  {
    InputStream in = new FileInputStream(fname);
    Reader r = new InputStreamReader(in, encoding);
    try {
      addFile(r, separator);
    } catch( FileFormatException e ) {
      e.printStackTrace();
      throw new FileFormatException(fname + e.getMessage(), e.getLineNo());
    } finally {
      r.close();
    }
  }
  /* +***************************************************************** */
  /**
   * <p>
   * feeds {@code Didyoumean} with terms and weights from a {@code Reader}.
   * This method works exactly like the three parameter method
   * {@link #addFile(String,char,String) addFile}, except that it is up to
   * the caller to provide a {@code Reader}.
   * </p>
   */
  public void addFile(Reader in, char separator) throws IOException,
    FileFormatException
  {
    BufferedReader bin;
    if( in instanceof BufferedReader ) bin = (BufferedReader) in;
    else bin = new BufferedReader(in);

    String splitRe = "[" + separator + "]";
    String line = null;
    int lcount = 0;
    while( null != (line = bin.readLine()) ) {
      lcount += 1;
      String[] pair = line.split(splitRe);
      checkLineFormat(pair, lcount);
      
      int weight = convertWeight(pair[1], lcount);
      add(pair[0], weight);
    }
  }
  /* +***************************************************************** */
  private static void checkLineFormat(String[] pair, int lineNo)
    throws FileFormatException
  {
    if( pair.length!=2 ) {
      String msg = "line does not have 2 elements";
      throw new FileFormatException(msg, lineNo);
    }

    pair[0] = pair[0].trim();
    pair[1] = pair[1].trim();
    if( pair[0].length()==0||pair[1].length()==0 ) {
      String msg = "line contains empty element";
      throw new FileFormatException(msg, lineNo);
    }
  }
  /* +***************************************************************** */
  private static int convertWeight(String weight, int lineNo)
    throws FileFormatException
  {
    try {
      return Integer.parseInt(weight);
    } catch( NumberFormatException e ) {
      String msg = String.format("cannot convert %s to int", weight);
      throw new FileFormatException(msg, lineNo);
    }

  }
  /* +***************************************************************** */
  /**
   * <p>
   * looks up the word in the internal {@link Dictionary} and then filters
   * for the term with the heighest weight according to the weights provied
   * when {@link #add add}ing a term.
   * </p>
   * 
   * @return the list of stored elements most similar to {@code word} that
   *         have the highest weight assigned.
   */
  public List<ResultElem<String, Integer>> lookup(String word) {
    return lookup(word, false);
  }
  /* +***************************************************************** */
  /**
   * <p>
   * like {@link #lookup lookup()} but never returns the {@code word} itself,
   * even if it is in the dictionary.
   * </p>
   */
  public List<ResultElem<String,Integer>> lookupDistinct(String word) {
    return lookup(word, true);
  }
  /* +***************************************************************** */
  private List<ResultElem<String,Integer>> lookup(String word,
                                                 boolean distinct)
  {
    List<ResultElem<String,Integer>> result = newResultList();

    List<ResultElem<String,Integer>> similarWords;
    if( distinct ) {
      similarWords = dict.lookupDistinct(word);
    } else {
      similarWords = dict.lookup(word);
    }
    int bestWeight = convertToWeights(similarWords, result);

    return filterBest(result, bestWeight);
  }
  /* +***************************************************************** */
  private int convertToWeights(List<ResultElem<String, Integer>> in,
                               List<ResultElem<String, Integer>> out) {
    int bestWeight = Integer.MIN_VALUE;
    
    for(ResultElem<String, Integer> e : in) {
      int weight = weights.get(e.value);
      if( weight>bestWeight ) bestWeight = weight;
      ResultElem<String, Integer> newElem = newResultElem(e.value, weight);
      out.add(newElem);
    }
    return bestWeight;
  }
  /* +***************************************************************** */
  private static List<ResultElem<String, Integer>> filterBest(
                                                              List<ResultElem<String, Integer>> candidates,
                                                              int bestWeight)
  {
    List<ResultElem<String, Integer>> result = newResultList();
    for(ResultElem<String,Integer> elem : candidates) {
      if( elem.d==bestWeight ) result.add(elem);
    }
    return result;
  }
  /* +***************************************************************** */
  /**
   * <p>
   * to support inspection and debugging, the class (not the object) of
   * the internally used {@link Dictionary} is returned.
   * </p>
   */
  public Class<?> getDictClass() {
    dict.add("s");
    return dict.getClass();
  }
  /* +***************************************************************** */
  /**
   * <p>
   * for ad-hoc testing only. First argument is a file to be used with
   * {@link #addFile addFile}, further arguments are terms to be looked up.
   * </p>
   */
  public static void main(String[] argv) throws Exception {
    IntMetric<String> metric =
        new LevenshteinMetric(CostFunctions.caseIgnore);
    Didyoumean dym = instanceNgramDict(3, metric, 2);
    // Didyoumean dym = instanceBKTree(metric, 2);
    long start = System.currentTimeMillis();
    dym.addFile(argv[0], ':', "UTF-8");
    long end = System.currentTimeMillis();
    System.out.printf("reading dict took %dms, now starting lookup%n", end
        - start);
    start = System.currentTimeMillis();
    int count = 0;
    for(int i = 1; i < argv.length; i++) {
      List<ResultElem<String, Integer>> res = dym.lookup(argv[i]);
      count = count + res.size();
      System.out.printf("%s->%s%n", argv[i], res);
    }
    end = System.currentTimeMillis();
    long dt = end - start;
    System.out.printf("dt=%dms, avg=%.2fms%n", dt, (double) dt
        / (argv.length - 1));
  }
}
