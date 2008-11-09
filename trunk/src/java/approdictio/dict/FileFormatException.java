package approdictio.dict;

/**
 * <p>
 * is the exception thrown if a file read does contain format errors.
 * </p>
 */
public class FileFormatException extends Exception {
  private final int lineNo;
  public FileFormatException(String msg, int lineNo) {
    super(msg);
    this.lineNo = lineNo;
  }
  /**
   * <p>the number of the line with the error</p>
   * @return the error line number
   */
  public int getLineNo() { return lineNo; }
}
