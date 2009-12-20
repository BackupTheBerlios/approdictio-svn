package approdictio.dict;

import static org.junit.Assert.*;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import approdictio.levenshtein.LevenshteinMetric;

public class TestDidyoumean {
  private Didyoumean[] dyms;
 
  /*+******************************************************************/
  @Before
  public void setup() {
    LevenshteinMetric lev = new LevenshteinMetric();

    dyms = new Didyoumean[2];

    dyms[0] = Didyoumean.instanceBKTree(lev, 2);
    dyms[1] = Didyoumean.instanceNgramDict(3, lev, 5);
  }
  /*+******************************************************************/
  private static final class WeightedTerm {
    public final String term;
    public final int w;
    public WeightedTerm(String t, int w) {
      this.term = t;
      this.w = w;
    }
  }
  /*+******************************************************************/
  @Test 
  public void dymMinimal() {
    WeightedTerm[] ttt = {
        new WeightedTerm("a1bcde", 10),
        new WeightedTerm("ab2cde", 11),
        new WeightedTerm("abc3de", 11),
        new WeightedTerm("abcd4e", 11),
        // the next to test if adding a term twice does the right thing
        new WeightedTerm("fiffy", 2),
        new WeightedTerm("fiffi", 2),
        new WeightedTerm("fiffy", 2),
    };
    for(Didyoumean dym: dyms) {
      for(WeightedTerm p: ttt) dym.add(p.term, p.w);
      String name = dym.getDictClass().getName();

      List<ResultElem<String,Integer>> l = dym.lookup("abcde");
      assertEquals(name, 3, l.size());
      assertEquals(name, 11, l.get(0).d);
      assertEquals(name, 11, l.get(1).d);
      assertEquals(name, 11, l.get(2).d);

      l = dym.lookupDistinct("abcd4e");
      assertEquals(name, 2, l.size());
      assertEquals(name, 11, l.get(0).d);
      assertEquals(name, 11, l.get(1).d);
      
      l = dym.lookup("fiffa");
      assertEquals(name, 1, l.size());
      assertEquals(name, ttt[4].term, l.get(0).value);
      assertEquals(name, 4, l.get(0).d);
      
      // try the trivial things too
      for(int i=0; i<4; i++) {
        l = dym.lookup(ttt[i].term);
        assertEquals(name, 1, l.size());
        assertEquals(name, ttt[i].term, l.get(0).value);
        assertEquals(name, ttt[i].w, l.get(0).d);
      }
    }
  }
  /*+******************************************************************/
  @Test
  public void dymFromfile() throws Exception {
    String fileContent = 
      "abcdef:10\n" +
      "abXdef: 10\n" +
      "abYdef : 11\n" +
      " abZZZf : 12  \n";
    for(Didyoumean dym: dyms) {
      Reader r = new StringReader(fileContent);
      dym.addFile(r, ':');
      String name = dym.getDictClass().getName();

      List<ResultElem<String,Integer>> l = dym.lookup("ab.de");
      assertEquals(name, 1, l.size());
      assertEquals(name, "abYdef", l.get(0).value);
    }
  }
  /*+******************************************************************/
  @Test
  public void fromFileExc1() throws Exception {
    String fileContents[] = {
      "x:1\n" + "hahaha\n" + "y:2\n",
      "x:1\n :2\n a:3\n",
      "x:1\n adsf:\n a:1\n",
      "x:1\n asdf:bla\n b:1",
      "x:1\n aa:1:aa\n b:1",
    };

    for(Didyoumean dym : dyms) {
      String name = dym.getDictClass().getName();

      int i = 0;
      for(String fileContent : fileContents) {
        Reader r = new StringReader(fileContent);
        FileFormatException ex = null;
        try {
          dym.addFile(r, ':');
        } catch( FileFormatException e ) {
          ex = e;
        }
        assertEquals(name+" test"+(++i), 2, ex.getLineNo());
      }
    }
  }
  /*+******************************************************************/

}
