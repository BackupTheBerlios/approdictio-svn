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

import java.util.Arrays;

import approdictio.levenshtein.LevenshteinMetric;

/**
 * <p>Just a test, not functional.</p>
 *
 */
public class AbcMetric implements IntMetric<String> {
  private final char noChar;
  
  /*+******************************************************************/
  private static final class Chint implements Comparable<Chint> {
    public final char ch;
    public int pos;
    public Chint(char ch, int pos) {
      this.ch = ch;
      this.pos = pos;
    }
    public void dec(int limit) { 
      if( pos>limit ) pos -=1; 
      }
    public int compareTo(Chint other) {
      if( ch<other.ch ) return -1;
      if( ch>other.ch ) return 1;
      if( pos<other.pos ) return -1;
      if( pos>other.pos ) return 1;
      return 0;
    }
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append('[').append(ch).append(pos).append(']');
      return sb.toString();
    }
  }
  /*+******************************************************************/
  public AbcMetric(char noChar) {
    this.noChar = noChar;
  }
  /*+******************************************************************/
  private Chint[] chints(String s) {
    Chint[] r = new Chint[s.length()];
    int i;
    for(i=0; i<s.length(); i++) {
      r[i] = new Chint(s.charAt(i), i);
    }
    Arrays.sort(r);
    return r;
  }
  /*+******************************************************************/
  private void dumpChints(String s, Chint[] chs, int startat) {
    System.out.printf("%10s->(%d)", s, startat);
    for(int i=startat; i<chs.length; i++) {
      System.out.printf("%s", chs[i]);
    }
    System.out.println();
  }
  /*+******************************************************************/
  private void dec(Chint[] ch, int start, int limit) {
    for(int k=start; k<ch.length; k++) ch[k].dec(limit);
  }
  /*+******************************************************************/
  public int d(String s0, String s1) {
    Chint[] chi0 = chints(s0);
    Chint[] chi1 = chints(s1);
    int l0 = chi0.length;
    int l1 = chi1.length;
    int i0 = 0;
    int i1 = 0;
    
    int d = 0;

    while( i0<l0 && i1<l1 ) {
      System.out.printf("...d=%d%n", d);
      dumpChints(s0, chi0, i0);
      dumpChints(s1, chi1, i1);
      
      if( chi0[i0].ch==chi1[i1].ch) {
        d += Math.abs(chi0[i0].pos-chi1[i1].pos);
        i0 += 1;
        i1 += 1;
        continue;
      }
      if( chi0[i0].ch<chi1[i1].ch ) {
        i0 += 1;
        d += 1;
      } else {
        i1 += 1;
        d += 1;
      }      
    }
    System.out.printf("---d=%d%n", d);
    dumpChints(s0, chi0, i0);
    dumpChints(s1, chi1, i1);
    d += (l0-i0);
    d += (l1-i1);
    //for(;i0<l0; i0++) if( i0<=chi0[i0].pos ) d += 1;
    //for(;i1<l1; i1++) if( i1<=chi1[i1].pos ) d += 1;

    return d;
  }
  /*+******************************************************************/
  public static void main(String[] argv) {
    AbcMetric abc = new AbcMetric('$');
    LevenshteinMetric lev = new LevenshteinMetric();
    
    for(int i=0; i<argv.length-1; i+=2) {
      int dabc = abc.d(argv[i], argv[i+1]);
      int dlev = lev.d(argv[i], argv[i+1]);
      System.out.printf("%s--%s: lev=%d, abc=%d%n", argv[i], argv[i+1],
                        dlev, dabc);
    }
  }
}
