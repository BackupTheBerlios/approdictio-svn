package approdictio.dict;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import approdictio.levenshtein.LevenshteinMetric;

public class TestBKStableLookup {
  private BKTree<WeightedString> tree;
  
  private static IntMetric<WeightedString> metric = 
    new IntMetric<WeightedString>() {
    private LevenshteinMetric lm = new LevenshteinMetric();
    @Override
    public int d(WeightedString v1, WeightedString v2) {
      return lm.d(v1.word, v2.word);
    }
  };

  private static Comparator<WeightedString> weightCompare = 
    new Comparator<WeightedString>() {
    @Override
    public int compare(WeightedString o1, WeightedString o2) {
      if (o1.weight<o2.weight) return -1;
      if (o1.weight>o2.weight) return 1;
      return 0;
    }
  };

  private static final class WeightedString {
    public int weight;
    public final String word;
    public WeightedString(String word, int weight) {
      this.word = word;
      this.weight = weight;
    }
    public String getWord() {
      return word;
    }
    public String toString() {
      return word+":"+weight;
    }
  }
  
  private static WeightedString ws(String word) {
    return new WeightedString(word, word.length());
  }
  
  @Before
  public void setup() {
    tree = new BKTree<WeightedString>(metric);
  }
  private void fillTree(String... words) {
    for(String w : words) {
      tree.add(ws(w));
    }
  }
  /*+******************************************************************/
  @Test
  public void testBasic() {
    String[] words = {"a", "ab", "abc", "abcd", "abcde"};
    fillTree(words);
    BKStableLookup<WeightedString> bkosl =
      new BKStableLookup<WeightedString>(tree, ws("abc"), 1, weightCompare);
    for(int i=1; i<=3; i++) {
      assertTrue(bkosl.hasNext());
      assertEquals(words[i], bkosl.next().getWord());
    }
  }
  
  @Test
  public void testEmptyTree() {
    BKStableLookup<WeightedString> bkosl =
      new BKStableLookup<WeightedString>(tree, ws("abc"), 1, weightCompare);
    assertFalse(bkosl.hasNext());
  }
  
  @Test 
  public void testNoMatching() {
    fillTree("a", "b", "c", "xxxxxxxx");
    BKStableLookup<WeightedString> bkosl =
      new BKStableLookup<WeightedString>(tree, ws("rrr"), 1, weightCompare);
    assertFalse(bkosl.hasNext());
  }
  
  
  @Test
  public void testTrySpecialStructure() {
    fillTree("a", 
             "dddd", "ddde", "xxxx", 
             "iiiiii", "iiiiij", "yyyyyy",
             "iiiiiiii", "ooooooop", "zzzzzzzz");
    //tree.dump(System.out);
    BKStableLookup<WeightedString> bkosl =
      new BKStableLookup<WeightedString>(tree, ws("iiiiij"), 3, weightCompare);
    String[] expected = {"iiiiii", "iiiiij", "iiiiiiii"};
    for(String exp : expected) {
      assertEquals(exp, bkosl.next().getWord());
    }
  }
  
  @Test
  public void testRandomVariationWeight() {
    long birthOfWernerHeisenberg = 19011205L;
    Random rand = new Random(birthOfWernerHeisenberg);
    for(int i=0; i<20; i++) {
      checkRandomVariationWeight(rand);
      setup();
    }
  }
  
  
  public void checkRandomVariationWeight(Random rand) {
    int N = 1000;
    WeightedString[] wwords = genZeroToN(N, rand);
    for(int i=0; i<N; i++) {
     tree.add(wwords[i]);
    }
    //tree.dump(System.out);
    int maxDist = 2;
    for(int i=0; i<N; i++) {
      List<WeightedString> hardResults 
      = findResultsTheHardWay(wwords, wwords[i], maxDist);
    
      BKStableLookup<WeightedString> bkosl =
        new BKStableLookup<WeightedString>(tree, wwords[i],
                                                maxDist,
                                                weightCompare);
      for(int k=0; k<hardResults.size(); k++) {
        assertEquals(hardResults.get(k), bkosl.next());
      }
    }
  }

  private List<WeightedString> 
  findResultsTheHardWay(WeightedString[] wwords,
                        WeightedString testWord,
                        int maxDist) {
    List<WeightedString> result = new ArrayList<WeightedString>(100);
    for(int i=0; i<wwords.length; i++) {
      if (metric.d(testWord, wwords[i])<=maxDist) {
        result.add(wwords[i]);
      }
    }
    Collections.sort(result, weightCompare);
    //System.out.println("hard result for "+testWord+" is "+result);
    return result;
  }

  private WeightedString[] genZeroToN(int N, Random rand) {
    String f = "%0"+((int)Math.log10(N+1))+"d";
    Set<Integer> used = new HashSet<Integer>(2*N);
    WeightedString[] result = new WeightedString[N];
    for(int i=0; i<N; i++) {
      int weight = uniqueWeight(used, rand, 100*N);
      result[i] = new WeightedString(String.format(f, i), weight);
    }
    Arrays.sort(result, weightCompare);
    return result;
  }

  private int uniqueWeight(Set<Integer> used, Random rand, int maxWeight) {
    int newRand;
    do {
      newRand = rand.nextInt();
    } while (used.contains(newRand));
    used.add(newRand);
    return newRand;
  }
}
