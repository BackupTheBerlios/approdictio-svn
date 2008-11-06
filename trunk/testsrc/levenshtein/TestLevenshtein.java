package levenshtein;

import static org.junit.Assert.*;

import org.junit.Test;

import approdictio.dict.IntMetric;
import approdictio.levenshtein.CostFunctions;
import approdictio.levenshtein.LevenshteinMetric;


public class TestLevenshtein {
  IntMetric[] tmp = {
      new LevenshteinMetric(),
      new LevenshteinMetric(CostFunctions.caseIgnore),
  };
  @SuppressWarnings("unchecked")
  IntMetric<String>[] metrics = tmp;

  /*+******************************************************************/
  @Test
  public void identity() throws Exception {
    String tests[] = {
        "", "a", "aa", "Hastunichjesen"
    };
    for(IntMetric<String> m : metrics) {
      for(String s : tests) {
        assertEquals(0, m.d(s,s));
      }
    }
  }
  /*+******************************************************************/
  @Test
  public void d1() throws Exception {
    String tests[] = {
        "", "a", "aa", "axa", "bbb", "bxb",
    };
    for(IntMetric<String> m : metrics) {
      for(int i=0; i<tests.length; i+=2) {
        assertEquals(1, m.d(tests[i], tests[i+1]));
      }
    }
  }
  /*+******************************************************************/
  @Test
  public void d5() throws Exception {
    String tests[] = {
        "", "abcde", "01234", "56789",
    };
    for(IntMetric<String> m : metrics) {
      for(int i=0; i<tests.length; i+=2) {
        assertEquals(5, m.d(tests[i], tests[i+1]));
      }
    }
  }
  /*+******************************************************************/
  @Test
  public void ignoreCase() throws Exception {
    IntMetric<String> m = metrics[1];
    assertEquals(0, m.d("abcde", "ABcDE"));
    assertEquals(1, m.d("abcde", "ABCDF"));
    
  }
  /*+******************************************************************/
}
