package dict;

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
  public static final Comparator<ResultElem<String, Integer>> cmpResult =
      new Comparator<ResultElem<String, Integer>>() {
        public int compare(ResultElem<String, Integer> arg0,
                           ResultElem<String, Integer> arg1)
        {
          return arg0.d - arg1.d;
        }
      };
  /* +***************************************************************** */
  ResultElem(T value, DTYPE d) {
    this.value = value;
    this.d = d;
  }
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(value).append(':').append(d);
    return sb.toString();
  }
}