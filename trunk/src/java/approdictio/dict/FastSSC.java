package approdictio.dict;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * implements <em>fast string search with candidates</em>.
 * </p>
 * 
 * <p>
 * This is unfinished, since a simple test with 150000 words from a 30000
 * page intranet show that the heap requirements are just enormous compared
 * to NgramDict and BKTree.
 * </p>
 * 
 * @see <a href="http://fastss.csg.uzh.ch/">FastSS</a>
 */
public class FastSSC implements Dictionary<String, Integer> {
  private final Map<String, Set<String>> index =
      new HashMap<String, Set<String>>();

  private final int k;
  /* +***************************************************************** */
  /**
   * <p>
   * creates a {@code FastSSC} for deletion depths of {@code k}. Good values
   * for {@code k} are 2 or 3.
   */
  public FastSSC(int k) {
    this.k = k;
  }
  /* +***************************************************************** */
  private void add(String dkey, String value, int depth) {
    Set<String> s = index.get(dkey);
    if( s == null ) index.put(dkey, s = new HashSet<String>(4));
    s.add(value);

    if( depth == 0 || dkey.length() < 2 ) return;

    char[] buf = new char[dkey.length() - 1];
    dkey.getChars(1, dkey.length(), buf, 0);
    char keptChar = dkey.charAt(0);
    for(int i = 0; i < buf.length; i++) {
      String childKey = new String(buf);
      add(childKey, value, depth - 1);
      char tmp = buf[i];
      buf[i] = keptChar;
      keptChar = tmp;
    }
    add(new String(buf), value, depth - 1);
  }
  /* +***************************************************************** */
  public void add(String value) {
    add(value, value, k);
  }
  /* +***************************************************************** */
  public List<ResultElem<String, Integer>> lookup(String queryValue) {
    // TODO Auto-generated method stub
    return null;
  }
  /* +***************************************************************** */
  public static void main(String[] argv) throws Exception {
    FastSSC t = new FastSSC(2);
    Util.readFileDict(argv[0], t);

    for(Map.Entry<String, Set<String>> e : t.index.entrySet()) {
      System.out.printf("%s->%s%n", e.getKey(), e.getValue());
    }
  }
}
