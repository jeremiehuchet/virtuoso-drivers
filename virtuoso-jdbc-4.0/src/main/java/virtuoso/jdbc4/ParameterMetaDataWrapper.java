package virtuoso.jdbc4;
import java.sql.ParameterMetaData;
import java.sql.SQLException;
import java.sql.ParameterMetaData;
import java.sql.Types;
public class ParameterMetaDataWrapper implements ParameterMetaData {
  private ParameterMetaData wmd;
  private ConnectionWrapper wconn;
  protected ParameterMetaDataWrapper(ParameterMetaData _prmd,
   ConnectionWrapper _wconn)
  {
    wmd = _prmd;
    wconn = _wconn;
  }
  private void exceptionOccurred(SQLException sqlEx) {
    if (wconn != null)
      wconn.exceptionOccurred(sqlEx);
  }
  public synchronized void finalize () throws Throwable {
    close();
  }
  protected void close() throws SQLException {
    if (wmd == null)
      return;
    wmd = null;
    wconn = null;
  }
  public int getParameterCount() throws java.sql.SQLException {
    try {
      return wmd.getParameterCount();
    } catch (SQLException ex) {
      exceptionOccurred(ex);
      throw ex;
    }
  }
  public int isNullable(int param) throws java.sql.SQLException {
    try {
      return wmd.isNullable(param);
    } catch (SQLException ex) {
      exceptionOccurred(ex);
      throw ex;
    }
  }
  public boolean isSigned(int param) throws java.sql.SQLException {
    try {
      return wmd.isSigned(param);
    } catch (SQLException ex) {
      exceptionOccurred(ex);
      throw ex;
    }
  }
  public int getPrecision(int param) throws java.sql.SQLException {
    try {
      return wmd.getPrecision(param);
    } catch (SQLException ex) {
      exceptionOccurred(ex);
      throw ex;
    }
  }
  public int getScale(int param) throws java.sql.SQLException {
    try {
      return wmd.getScale(param);
    } catch (SQLException ex) {
      exceptionOccurred(ex);
      throw ex;
    }
  }
  public int getParameterType(int param) throws java.sql.SQLException {
    try {
      return wmd.getParameterType(param);
    } catch (SQLException ex) {
      exceptionOccurred(ex);
      throw ex;
    }
  }
  public String getParameterTypeName(int param) throws java.sql.SQLException {
    try {
      return wmd.getParameterTypeName(param);
    } catch (SQLException ex) {
      exceptionOccurred(ex);
      throw ex;
    }
  }
  public String getParameterClassName(int param) throws java.sql.SQLException {
    try {
      return wmd.getParameterClassName(param);
    } catch (SQLException ex) {
      exceptionOccurred(ex);
      throw ex;
    }
  }
  public int getParameterMode(int param) throws java.sql.SQLException {
    try {
      return wmd.getParameterMode(param);
    } catch (SQLException ex) {
      exceptionOccurred(ex);
      throw ex;
    }
  }
  public <T> T unwrap(java.lang.Class<T> iface) throws java.sql.SQLException
  {
    try {
      return wmd.unwrap(iface);
    } catch (SQLException ex) {
      exceptionOccurred(ex);
      throw ex;
    }
  }
  public boolean isWrapperFor(java.lang.Class<?> iface) throws java.sql.SQLException
  {
    try {
      return wmd.isWrapperFor(iface);
    } catch (SQLException ex) {
      exceptionOccurred(ex);
      throw ex;
    }
  }
}
