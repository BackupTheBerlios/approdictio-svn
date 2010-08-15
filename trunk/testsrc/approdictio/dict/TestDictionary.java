package approdictio.dict;

import static org.junit.Assert.*;

import java.util.*;


import org.junit.Before;
import org.junit.Test;

import approdictio.levenshtein.LevenshteinMetric;


public class TestDictionary {
  private Dictionary<String,Integer>[] dicts = null;
  private Random random = null;
  
  @Before
  public void setup() {
    LevenshteinMetric lev = new LevenshteinMetric();

    @SuppressWarnings("unchecked")
    Dictionary<String,Integer>[] ds = new Dictionary[2];

    dicts = ds;
    dicts[0] =  new BKTree<String>(lev);
    dicts[1] = new NgramDict(3, lev);
    
    random = new Random(1);
  }

  /*+******************************************************************/
  @Test
  public void termItselfReturned() {
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
        List<ResultElem<String,Integer>> l = d.lookup(term, 2);
        assertEquals(name, 1, l.size());
        assertEquals(name, Integer.valueOf(0), l.get(0).d);
        assertEquals(name, term, l.get(0).value);
      }
    }
  }
  /*+******************************************************************/
  @Test
  public void requestDistinctTerm() {
    String[] dictTerm = {
        "halligalli", "blarilu", "pispopeia", "dingens",        
    };
    for(Dictionary<String,Integer> d : dicts) {
      String name = d.getClass().getName();
      for(String term : dictTerm) {
        d.add(term);
        d.add(term+"x");
      }
      for(String term : dictTerm) {
        List<ResultElem<String,Integer>> l = d.lookupDistinct(term, 2);
        assertEquals(name, 1, l.size());
        assertEquals(name, Integer.valueOf(1), l.get(0).d);
        assertEquals(name, term+"x", l.get(0).value);
      }
    }
  }
  /*+******************************************************************/
  @Test
  public void zeroResults() {
    for(Dictionary<String,Integer> d : dicts) {
      d.add("aaaaa");
      assertEquals(0, d.lookup("zzzz", 2).size());
      assertEquals(0, d.lookupDistinct("zzzz", 2).size());
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

      List<ResultElem<String,Integer>> l = d.lookup("abcde", 2);
      assertEquals(name, 2, l.size());

      l = d.lookupDistinct("abcde", 2);
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
    new NgramDict(0, new LevenshteinMetric());
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

      List<ResultElem<String,Integer>> l = d.lookup("00000a00000bbbbbbbbbb",2);
      assertEquals(name, 1, l.size());
      assertEquals(name, ttt[2], l.get(0).value);

      l = d.lookupDistinct("00000a00000bbbbbbbbbb",2);
      assertEquals(name, 0, l.size());
      
      l = d.lookup("00000a00000bbbbbbbbbc",2);
      assertEquals(name, 1, l.size());
      assertEquals(name, ttt[2], l.get(0).value);

      l = d.lookupDistinct("00000a00000bbbbbbbbbc",2);
      assertEquals(name, 1, l.size());
      assertEquals(name, ttt[2], l.get(0).value);

    }
  }
  /*+******************************************************************/
  @Test
  public void lookupOnEmptyDict() throws Exception {
    for(Dictionary<String,Integer> dict : dicts) {
      dict.lookup("abc",2);
    }
  }
  /*+******************************************************************/
  private char randomChar() {
    return (char)('a' + random.nextInt(6));
  }
  private String randomWord(int minLen, int range) {
    int len = minLen + random.nextInt(range);
    StringBuilder b = new StringBuilder();
    for(int i=0; i<len; i++) {
      b.append(randomChar());
    }
    return b.toString();
  }
  /*+******************************************************************/
  private static final class DictFiller implements Runnable {
    private final Dictionary<String,Integer> dict;
    private final List<String> words;
    private final Thread me;
    public DictFiller(Dictionary<String,Integer> dict, List<String> words) {
      this.dict = dict;
      this.words = words;
      me = new Thread(this);
      me.start();
    }
    public void join() throws InterruptedException {
      me.join();
      System.out.println("just joined");
    }
    public void run() {
      System.out.printf("starting%n");

      for(String word : words) {
        dict.add(word);
      }
      System.out.printf("done%n");
    }
  }
  /*+******************************************************************/
  //@Test ------ This does not work as a test, except very seldomly
  public void concurrentModification() throws InterruptedException {
    int numWords = 150000;
    List<String> words = new ArrayList<String>(numWords);
    for(int i=0; i<numWords; i++) {
      words.add(randomWord(3,7));
    }
    for(Dictionary<String,Integer> dict : dicts) {
      DictFiller filler = new DictFiller(dict, words);
      Exception e = null;
      int count = -1;
      for(int i=0; i<2*numWords; i++) {
        try {
          dict.lookup("abcdeffedcbafcabaceefaaabbbcccdddeeefff",2);
          count = i;
        } catch( ConcurrentModificationException ce) {
          e = ce;
          System.out.printf("---------------%n");
          break;
        }
      }
      System.out.printf("count = %d; e=%s%n", count, e);
      filler.join();
      assertTrue(e instanceof ConcurrentModificationException );
    }
  }
  /*+******************************************************************/
  @Test
  public void addSameWord() {
    for(Dictionary<String,Integer> dict : dicts) {
      String name = dict.getClass().getName();
      dict.add("blabla");
      dict.add("blabla");
      List<ResultElem<String,Integer>> l = dict.lookup("blabla",2);
      assertEquals(name, 1, l.size());

      l = dict.lookupDistinct("blibla",2);
      assertEquals(name, 1, l.size());
    }    
  }
  /*+******************************************************************/
  @Test
  public void testDifferentMaxDist() {
    for(Dictionary<String,Integer> dict : dicts) {
      String name = dict.getClass().getName();
      dict.add("aaa1aaa");
      dict.add("aaa11112222aaa");
      List<ResultElem<String,Integer>> l = dict.lookup("aaa11aaa",2);
      assertEquals(name, 1, l.size());

      l = dict.lookup("aaa11aaa", 3);
      assertEquals(name, 1, l.size());
      assertEquals(name, "aaa1aaa", l.get(0).value);

      l = dict.lookup("aaa1111aaa", 3);
      assertEquals(name, 1, l.size());
      assertEquals(name, "aaa1aaa", l.get(0).value);

      l = dict.lookup("aaa11112aaa", 3);
      assertEquals(name, 1, l.size());
      assertEquals(name, "aaa11112222aaa", l.get(0).value);
}
  }
  /*+******************************************************************/


}
