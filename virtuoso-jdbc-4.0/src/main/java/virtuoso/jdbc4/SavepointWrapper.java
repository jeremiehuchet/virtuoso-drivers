package virtuoso.jdbc4;
import java.sql.Savepoint;
import java.sql.SQLException;
public class SavepointWrapper implements Savepoint {
  private Savepoint wsp;
  private ConnectionWrapper wconn;
  protected SavepointWrapper(Savepoint _sp, ConnectionWrapper _wconn) {
    wsp = _sp;
    wconn = _wconn;
  }
  private void exceptionOccurred(SQLException sqlEx) {
    if (wconn != null)
      wconn.exceptionOccurred(sqlEx);
  }
  public int getSavepointId() throws java.sql.SQLException {
    try {
      return wsp.getSavepointId();
    } catch (SQLException ex) {
      exceptionOccurred(ex);
      throw ex;
    }
  }
  public String getSavepointName() throws java.sql.SQLException {
    try {
      return wsp.getSavepointName();
    } catch (SQLException ex) {
      exceptionOccurred(ex);
      throw ex;
    }
  }
}
