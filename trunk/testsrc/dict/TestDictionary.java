package dict;

import static org.junit.Assert.*;

import java.util.List;


import org.junit.Before;
import org.junit.Test;

import approdictio.dict.BKTree;
import approdictio.dict.Dictionary;
import approdictio.dict.NgramDict;
import approdictio.dict.ResultElem;
import approdictio.levenshtein.LevenshteinMetric;


public class TestDictionary {
  private Dictionary<String,Integer>[] dicts;
  
  @Before
  public void setup() {
    LevenshteinMetric lev = new LevenshteinMetric();

    @SuppressWarnings("unchecked")
    Dictionary<String,Integer>[] ds = new Dictionary[2];

    dicts = ds;
    dicts[0] =  new BKTree<String>(new LevenshteinMetric(), 2);
    dicts[1] = new NgramDict(3, '$', lev, 3);
  }


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
}
