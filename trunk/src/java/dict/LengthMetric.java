package dict;

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
