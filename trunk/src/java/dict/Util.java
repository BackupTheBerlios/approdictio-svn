package dict;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * <p>
 * some static utility methods.
 * </p>
 */
public class Util {
  private Util() {
  }
  /* +***************************************************************** */
  /**
   * <p>
   * reads the filel given by its {@code fname} line by line, trims each line
   * and then adds it to {@code dict}.
   * </p>
   * 
   * @param <DTYPE> is not needed here
   * @param fname the file to read
   * @param dict the dictionary to fill
   * @throws IOException
   */
  public static <DTYPE> void readFileDict(String fname,
                                          Dictionary<String, DTYPE> dict)
    throws IOException
  {
    BufferedReader in = new BufferedReader(new FileReader(fname));
    String line;
    int i = 0;
    try {
      while( null != (line = in.readLine()) ) {
        line = line.trim();
        if( line.length() == 0 ) continue;
        dict.add(line);
        i += 1;
        // if( i%1000==0 ) {System.out.print('.'); System.out.flush();}
      }
    } finally {
      in.close();
    }
  }

}
