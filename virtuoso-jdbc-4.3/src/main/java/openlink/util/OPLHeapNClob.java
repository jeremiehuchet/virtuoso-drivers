package openlink.util;
import java.sql.Clob;
import java.sql.NClob;
import java.io.*;
import java.sql.SQLException;
public class OPLHeapNClob extends OPLHeapClob implements NClob, Serializable {
  public OPLHeapNClob() {
    super();
  }
  public OPLHeapNClob(String b) {
    super(b);
  }
  public OPLHeapNClob(char[] b) {
    super(b);
  }
  public OPLHeapNClob(Reader is) throws SQLException {
    super(is);
  }
}
