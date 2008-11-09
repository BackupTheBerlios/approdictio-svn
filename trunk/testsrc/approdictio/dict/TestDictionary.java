package approdictio.dict;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.junit.Before;
import org.junit.Test;

import approdictio.levenshtein.LevenshteinMetric;


public class TestDictionary {
  private Dictionary<String,Integer>[] dicts;
  
  @Before
  public void setup() {
    LevenshteinMetric lev = new LevenshteinMetric();

    @SuppressWarnings("unchecked")
    Dictionary<String,Integer>[] ds = new Dictionary[2];

    dicts = ds;
    dicts[0] =  new BKTree<String>(lev, 2);
    dicts[1] = new NgramDict(3, lev, 3);
  }

  /*+******************************************************************/
  @Test
  public void minimalRoundtrip() {
    String[] ttt = {
        "halligalli", "blarilu", "pispopeia", "dingens",
        "hallgalli", "blapilu", "pisopeia", "xdingens",
    };

    for(Dictionary<String,Integer> d : dicts) {
      String name = d.getClass().getName();
      for(String term : ttt) {
        d.add(term);
      }
      for(String term : ttt) {
        List<ResultElem<String,Integer>> l = d.lookup(term);
        assertEquals("on "+name, 1, l.size());
        assertEquals("on "+name, 0, l.get(0).d);
        assertEquals("on "+name, term, l.get(0).value);
      }
    }
  }
  /*+******************************************************************/
  @Test
  public void zeroResults() {
    for(Dictionary<String,Integer> d : dicts) {
      d.add("aaaaa");
      assertEquals(0, d.lookup("zzzz").size());
    }    
  }
  /*+******************************************************************/
  @Test
  public void twoResults() {
    String[] ttt = {
        "ab1de", "ab2de", "ab23de", "ab44de",
    };
    for(Dictionary<String,Integer> d : dicts) {
      for(String term : ttt) d.add(term);
      String name = d.getClass().getName();

      List<ResultElem<String,Integer>> l = d.lookup("abcde");
      assertEquals(name, 2, l.size());
      Set<String> s = new HashSet<String>(2);
      s.add(ttt[0]);
      s.add(ttt[1]);
      Set<String> t = new HashSet<String>(2);
      for(int i=0; i<2; i++) t.add(l.get(i).value);
      assertEquals(name, s, t);
    }    
  }
  /*+******************************************************************/
  @Test(expected=IllegalArgumentException.class)
  public void zeroNgram() {
    new NgramDict(0, new LevenshteinMetric(), 2);
  }
  /*+******************************************************************/
  @Test
  public void widespreadDict() {
    String[] ttt = {
        "a", "00000a00000", "00000a00000bbbbbbbbbb"     
    };
    for(Dictionary<String,Integer> d : dicts) {
      for(String term : ttt) d.add(term);
      String name = d.getClass().getName();

      List<ResultElem<String,Integer>> l = d.lookup("00000a00000bbbbbbbbbb");
      assertEquals(name, 1, l.size());
      assertEquals(name, ttt[2], l.get(0).value);
      
      l = d.lookup("00000a00000bbbbbbbbbc");
      assertEquals(name, 1, l.size());
      assertEquals(name, ttt[2], l.get(0).value);
    }
  }
  /*+******************************************************************/


}
