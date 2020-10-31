package virtuoso.javax;
import javax.sql.RowSet;
import javax.sql.RowSetListener;
import javax.sql.DataSource;
import javax.naming.*;
import java.util.Map;
import java.sql.*;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Calendar;
import java.net.URL;
public class OPLJdbcRowSet extends BaseRowSet {
    private Connection conn;
    private PreparedStatement pstmt;
    private ResultSet rs;
    private boolean doDisconnect = false;
  public synchronized void finalize () throws Throwable
  {
    close();
  }
  private Connection connect() throws SQLException {
      String connName;
      if ((connName = getDataSourceName()) != null)
        try {
          InitialContext initialcontext = new InitialContext();
          DataSource ds = (DataSource)initialcontext.lookup(connName);
          return ds.getConnection(getUsername(), getPassword());
        } catch(NamingException e) {
          throw OPLMessage_x.makeException(e);
        }
      else if ((connName = getUrl()) != null)
        return DriverManager.getConnection(connName, getUsername(), getPassword());
      else
        return null;
  }
  private void setParams(PreparedStatement pstmt, Object[] params) throws SQLException {
    if (params == null)
      return;
    for(int i = 0; i < params.length; i++) {
      Parameter par = (Parameter)params[i];
      switch(par.jType) {
        case Parameter.jObject:
            pstmt.setObject(i + 1, par.value);
            break;
        case Parameter.jObject_1:
            pstmt.setObject(i + 1, par.value, par.sqlType);
            break;
        case Parameter.jObject_2:
            pstmt.setObject(i + 1, par.value, par.sqlType, par.scale);
            break;
        case Parameter.jAsciiStream:
            pstmt.setAsciiStream(i + 1, (InputStream)par.value, par.length);
            break;
        case Parameter.jBinaryStream:
            pstmt.setBinaryStream(i + 1, (InputStream)par.value, par.length);
            break;
        case Parameter.jUnicodeStream:
            pstmt.setUnicodeStream(i + 1, (InputStream)par.value, par.length);
            break;
        case Parameter.jCharacterStream:
            pstmt.setCharacterStream(i + 1, (Reader)par.value, par.length);
            break;
        case Parameter.jDateWithCalendar:
            pstmt.setDate(i + 1, (Date)par.value, par.cal);
            break;
        case Parameter.jTimeWithCalendar:
            pstmt.setTime(i + 1, (Time)par.value, par.cal);
            break;
        case Parameter.jTimestampWithCalendar:
            pstmt.setTimestamp(i + 1, (Timestamp)par.value, par.cal);
            break;
        case Parameter.jNull_1:
            pstmt.setNull(i + 1, par.sqlType);
            break;
        case Parameter.jNull_2:
            pstmt.setNull(i + 1, par.sqlType, par.typeName);
            break;
        default:
          throw OPLMessage_x.makeException(OPLMessage_x.errx_Unknown_type_of_parameter);
      }
    }
  }
  public synchronized void execute() throws java.sql.SQLException {
    if (conn == null) {
      conn = connect();
      doDisconnect = true;
    }
    if (conn == null || getCommand() == null)
      throw OPLMessage_x.makeException(OPLMessage_x.errx_SQL_query_is_undefined);
    try {
      conn.setTransactionIsolation(getTransactionIsolation());
    } catch(Exception e) { }
    PreparedStatement pstmt = conn.prepareStatement(getCommand(), getType(),
        getConcurrency());
    setParams(pstmt, getParams());
    try {
      pstmt.setMaxRows(getMaxRows());
      pstmt.setMaxFieldSize(getMaxFieldSize());
      pstmt.setEscapeProcessing(getEscapeProcessing());
      pstmt.setQueryTimeout(getQueryTimeout());
    } catch(Exception e) { }
    rs = pstmt.executeQuery();
    notifyListener(ev_RowSetChanged);
  }
  public void execute(Connection _conn) throws SQLException {
    conn = _conn;
    execute();
  }
  public synchronized void close() throws SQLException {
    if (rs != null)
        rs.close();
    if (pstmt != null)
        pstmt.close();
    if (conn != null && doDisconnect)
        conn.close();
  }
  public synchronized void cancelRowUpdates() throws SQLException {
    check_close();
    rs.cancelRowUpdates();
    notifyListener(ev_RowChanged);
  }
  public synchronized boolean next() throws SQLException {
    check_close();
    boolean ret = rs.next();
    notifyListener(ev_CursorMoved);
    return ret;
  }
  public synchronized boolean previous() throws SQLException {
    check_close();
    boolean ret = rs.previous();
    notifyListener(ev_CursorMoved);
    return ret;
  }
  public synchronized boolean first() throws SQLException {
    check_close();
    boolean ret = rs.first();
    notifyListener(ev_CursorMoved);
    return ret;
  }
  public synchronized boolean last() throws SQLException {
    check_close();
    boolean ret = rs.last();
    notifyListener(ev_CursorMoved);
    return ret;
  }
  public synchronized boolean absolute(int row) throws SQLException {
    check_close();
    boolean ret = rs.absolute(row);
    notifyListener(ev_CursorMoved);
    return ret;
  }
  public synchronized boolean relative(int rows) throws SQLException {
    check_close();
    boolean ret = rs.relative(rows);
    notifyListener(ev_CursorMoved);
    return ret;
  }
  public synchronized void beforeFirst() throws SQLException {
    check_close();
    rs.beforeFirst();
    notifyListener(this.ev_CursorMoved);
  }
  public synchronized void afterLast() throws SQLException {
    check_close();
    rs.afterLast();
    notifyListener(this.ev_CursorMoved);
  }
  public synchronized boolean isBeforeFirst() throws SQLException {
    check_close();
    return rs.isBeforeFirst();
  }
  public synchronized boolean isAfterLast() throws SQLException {
    check_close();
    return rs.isAfterLast();
  }
  public synchronized boolean isFirst() throws SQLException {
    check_close();
    return rs.isFirst();
  }
  public synchronized boolean isLast() throws SQLException {
    check_close();
    return rs.isLast();
  }
  public synchronized int getRow() throws SQLException {
    check_close();
    return rs.getRow();
  }
  public synchronized boolean rowUpdated() throws SQLException {
    check_close();
    return rs.rowUpdated();
  }
  public synchronized boolean rowInserted() throws SQLException {
    check_close();
    return rs.rowInserted();
  }
  public synchronized boolean rowDeleted() throws SQLException {
    check_close();
    return rs.rowDeleted();
  }
  public synchronized void refreshRow() throws SQLException {
    check_close();
    rs.refreshRow();
  }
  public synchronized void insertRow() throws SQLException {
    check_close();
    rs.insertRow();
    notifyListener(ev_RowChanged);
  }
  public synchronized void updateRow() throws SQLException {
    check_close();
    rs.updateRow();
    notifyListener(ev_RowChanged);
  }
  public synchronized void deleteRow() throws SQLException {
    check_close();
    rs.deleteRow();
    notifyListener(ev_RowChanged);
  }
  public synchronized void moveToInsertRow() throws SQLException {
    check_close();
    rs.moveToInsertRow();
  }
  public synchronized void moveToCurrentRow() throws SQLException {
    check_close();
    rs.moveToCurrentRow();
  }
  public synchronized boolean wasNull() throws SQLException {
    check_close();
    return rs.wasNull();
  }
  public synchronized SQLWarning getWarnings() throws SQLException {
    check_close();
    return rs.getWarnings();
  }
  public synchronized void clearWarnings() throws SQLException {
    check_close();
    rs.clearWarnings();
  }
  public synchronized String getCursorName() throws SQLException {
    check_close();
    return rs.getCursorName();
  }
  public synchronized ResultSetMetaData getMetaData() throws SQLException {
    check_close();
    return rs.getMetaData();
  }
  public synchronized int findColumn(String columnName) throws SQLException {
    check_close();
    return rs.findColumn(columnName);
  }
  public synchronized String getString(int columnIndex) throws SQLException {
    check_close();
    return rs.getString(columnIndex);
  }
  public synchronized boolean getBoolean(int columnIndex) throws SQLException {
    check_close();
    return rs.getBoolean(columnIndex);
  }
  public synchronized byte getByte(int columnIndex) throws SQLException {
    check_close();
    return rs.getByte(columnIndex);
  }
  public synchronized short getShort(int columnIndex) throws SQLException {
    check_close();
    return rs.getShort(columnIndex);
  }
  public synchronized int getInt(int columnIndex) throws SQLException {
    check_close();
    return rs.getInt(columnIndex);
  }
  public synchronized long getLong(int columnIndex) throws SQLException {
    check_close();
    return rs.getLong(columnIndex);
  }
  public synchronized float getFloat(int columnIndex) throws SQLException {
    check_close();
    return rs.getFloat(columnIndex);
  }
  public synchronized double getDouble(int columnIndex) throws SQLException {
    check_close();
    return rs.getDouble(columnIndex);
  }
  public synchronized BigDecimal getBigDecimal(int columnIndex) throws SQLException {
    check_close();
    return rs.getBigDecimal(columnIndex);
  }
  public synchronized BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
    check_close();
    return rs.getBigDecimal(columnIndex, scale);
  }
  public synchronized byte[] getBytes(int columnIndex) throws SQLException {
    check_close();
    return rs.getBytes(columnIndex);
  }
  public synchronized Date getDate(int columnIndex) throws SQLException {
    check_close();
    return rs.getDate(columnIndex);
  }
  public synchronized Time getTime(int columnIndex) throws SQLException {
    check_close();
    return rs.getTime(columnIndex);
  }
  public synchronized Timestamp getTimestamp(int columnIndex) throws SQLException {
    check_close();
    return rs.getTimestamp(columnIndex);
  }
  public synchronized InputStream getAsciiStream(int columnIndex) throws SQLException {
    check_close();
    return rs.getAsciiStream(columnIndex);
  }
  public synchronized InputStream getUnicodeStream(int columnIndex) throws SQLException {
    check_close();
    return rs.getUnicodeStream(columnIndex);
  }
  public synchronized InputStream getBinaryStream(int columnIndex) throws SQLException {
    check_close();
    return rs.getBinaryStream(columnIndex);
  }
  public synchronized Object getObject(int columnIndex) throws SQLException {
    check_close();
    return rs.getObject(columnIndex);
  }
  public synchronized String getString(String columnName) throws SQLException {
    check_close();
    return rs.getString(columnName);
  }
  public synchronized boolean getBoolean(String columnName) throws SQLException {
    check_close();
    return rs.getBoolean(columnName);
  }
  public synchronized byte getByte(String columnName) throws SQLException {
    check_close();
    return rs.getByte(columnName);
  }
  public synchronized short getShort(String columnName) throws SQLException {
    check_close();
    return rs.getShort(columnName);
  }
  public synchronized int getInt(String columnName) throws SQLException {
    check_close();
    return rs.getInt(columnName);
  }
  public synchronized long getLong(String columnName) throws SQLException {
    check_close();
    return rs.getLong(columnName);
  }
  public synchronized float getFloat(String columnName) throws SQLException {
    check_close();
    return rs.getFloat(columnName);
  }
  public synchronized double getDouble(String columnName) throws SQLException {
    check_close();
    return rs.getDouble(columnName);
  }
  public synchronized BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
    check_close();
    return rs.getBigDecimal(columnName, scale);
  }
  public synchronized byte[] getBytes(String columnName) throws SQLException {
    check_close();
    return rs.getBytes(columnName);
  }
  public synchronized Date getDate(String columnName) throws SQLException {
    check_close();
    return rs.getDate(columnName);
  }
  public synchronized Time getTime(String columnName) throws SQLException {
    check_close();
    return rs.getTime(columnName);
  }
  public synchronized Timestamp getTimestamp(String columnName) throws SQLException {
    check_close();
    return rs.getTimestamp(columnName);
  }
  public synchronized InputStream getAsciiStream(String columnName) throws SQLException {
    check_close();
    return rs.getAsciiStream(columnName);
  }
  public synchronized InputStream getUnicodeStream(String columnName) throws SQLException {
    check_close();
    return rs.getUnicodeStream(columnName);
  }
  public synchronized InputStream getBinaryStream(String columnName) throws SQLException {
    check_close();
    return rs.getBinaryStream(columnName);
  }
  public synchronized Object getObject(String columnName) throws SQLException {
    check_close();
    return rs.getObject(columnName);
  }
  public synchronized Reader getCharacterStream(int columnIndex) throws SQLException {
    check_close();
    return rs.getCharacterStream(columnIndex);
  }
  public synchronized Reader getCharacterStream(String columnName) throws SQLException {
    check_close();
    return rs.getCharacterStream(columnName);
  }
  public synchronized BigDecimal getBigDecimal(String columnName) throws SQLException {
    check_close();
    return rs.getBigDecimal(columnName);
  }
  public synchronized void updateNull(int columnIndex) throws SQLException {
    check_close();
    rs.updateNull(columnIndex);
  }
  public synchronized void updateBoolean(int columnIndex, boolean x) throws SQLException {
    check_close();
    rs.updateBoolean(columnIndex, x);
  }
  public synchronized void updateByte(int columnIndex, byte x) throws SQLException {
    check_close();
    rs.updateByte(columnIndex, x);
  }
  public synchronized void updateShort(int columnIndex, short x) throws SQLException {
    check_close();
    rs.updateShort(columnIndex, x);
  }
  public synchronized void updateInt(int columnIndex, int x) throws SQLException {
    check_close();
    rs.updateInt(columnIndex, x);
  }
  public synchronized void updateLong(int columnIndex, long x) throws SQLException {
    check_close();
    rs.updateLong(columnIndex, x);
  }
  public synchronized void updateFloat(int columnIndex, float x) throws SQLException {
    check_close();
    rs.updateFloat(columnIndex, x);
  }
  public synchronized void updateDouble(int columnIndex, double x) throws SQLException {
    check_close();
    rs.updateDouble(columnIndex, x);
  }
  public synchronized void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
    check_close();
    rs.updateBigDecimal(columnIndex, x);
  }
  public synchronized void updateString(int columnIndex, String x) throws SQLException {
    check_close();
    rs.updateString(columnIndex, x);
  }
  public synchronized void updateBytes(int columnIndex, byte[] x) throws SQLException {
    check_close();
    rs.updateBytes(columnIndex, x);
  }
  public synchronized void updateDate(int columnIndex, Date x) throws SQLException {
    check_close();
    rs.updateDate(columnIndex, x);
  }
  public synchronized void updateTime(int columnIndex, Time x) throws SQLException {
    check_close();
    rs.updateTime(columnIndex, x);
  }
  public synchronized void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
    check_close();
    rs.updateTimestamp(columnIndex, x);
  }
  public synchronized void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
    check_close();
    rs.updateAsciiStream(columnIndex, x, length);
  }
  public synchronized void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
    check_close();
    rs.updateBinaryStream(columnIndex, x, length);
  }
  public synchronized void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
    check_close();
    rs.updateCharacterStream(columnIndex, x, length);
  }
  public synchronized void updateObject(int columnIndex, Object x, int scale) throws SQLException {
    check_close();
    rs.updateObject(columnIndex, x, scale);
  }
  public synchronized void updateObject(int columnIndex, Object x) throws SQLException {
    check_close();
    rs.updateObject(columnIndex, x);
  }
  public synchronized void updateNull(String columnName) throws SQLException {
    check_close();
    rs.updateNull(columnName);
  }
  public synchronized void updateBoolean(String columnName, boolean x) throws SQLException {
    check_close();
    rs.updateBoolean(columnName, x);
  }
  public synchronized void updateByte(String columnName, byte x) throws SQLException {
    check_close();
    rs.updateByte(columnName, x);
  }
  public synchronized void updateShort(String columnName, short x) throws SQLException {
    check_close();
    rs.updateShort(columnName, x);
  }
  public synchronized void updateInt(String columnName, int x) throws SQLException {
    check_close();
    rs.updateInt(columnName, x);
  }
  public synchronized void updateLong(String columnName, long x) throws SQLException {
    check_close();
    rs.updateLong(columnName, x);
  }
  public synchronized void updateFloat(String columnName, float x) throws SQLException {
    check_close();
    rs.updateFloat(columnName, x);
  }
  public synchronized void updateDouble(String columnName, double x) throws SQLException {
    check_close();
    rs.updateDouble(columnName, x);
  }
  public synchronized void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
    check_close();
    rs.updateBigDecimal(columnName, x);
  }
  public synchronized void updateString(String columnName, String x) throws SQLException {
    check_close();
    rs.updateString(columnName, x);
  }
  public synchronized void updateBytes(String columnName, byte[] x) throws SQLException {
    check_close();
    rs.updateBytes(columnName, x);
  }
  public synchronized void updateDate(String columnName, Date x) throws SQLException {
    check_close();
    rs.updateDate(columnName, x);
  }
  public synchronized void updateTime(String columnName, Time x) throws SQLException {
    check_close();
    rs.updateTime(columnName, x);
  }
  public synchronized void updateTimestamp(String columnName, Timestamp x) throws SQLException {
    check_close();
    rs.updateTimestamp(columnName, x);
  }
  public synchronized void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException {
    check_close();
    rs.updateAsciiStream(columnName, x, length);
  }
  public synchronized void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException {
    check_close();
    rs.updateBinaryStream(columnName, x, length);
  }
  public synchronized void updateCharacterStream(String columnName, Reader x, int length) throws SQLException {
    check_close();
    rs.updateCharacterStream(columnName, x, length);
  }
  public synchronized void updateObject(String columnName, Object x, int scale) throws SQLException {
    check_close();
    rs.updateObject(columnName, x, scale);
  }
  public synchronized void updateObject(String columnName, Object x) throws SQLException {
    check_close();
    rs.updateObject(columnName, x);
  }
  public synchronized Statement getStatement() throws SQLException {
    check_close();
    return rs.getStatement();
  }
  public synchronized Object getObject(int colIndex, Map<String,Class<?>> map)
   throws SQLException
  {
    check_close();
    return rs.getObject(colIndex, map);
  }
  public synchronized Ref getRef(int colIndex) throws SQLException {
    check_close();
    return rs.getRef(colIndex);
  }
  public synchronized Blob getBlob(int colIndex) throws SQLException {
    check_close();
    return rs.getBlob(colIndex);
  }
  public synchronized Clob getClob(int colIndex) throws SQLException {
    check_close();
    return rs.getClob(colIndex);
  }
  public synchronized Array getArray(int colIndex) throws SQLException {
    check_close();
    return rs.getArray(colIndex);
  }
  public synchronized Object getObject(String colName, Map<String,Class<?>> map)
     throws SQLException
  {
    check_close();
    return rs.getObject(colName, map);
  }
  public synchronized Ref getRef(String colName) throws SQLException {
    check_close();
    return rs.getRef(colName);
  }
  public synchronized Blob getBlob(String colName) throws SQLException {
    check_close();
    return rs.getBlob(colName);
  }
  public synchronized Clob getClob(String colName) throws SQLException {
    check_close();
    return rs.getClob(colName);
  }
  public synchronized Array getArray(String colName) throws SQLException {
    check_close();
    return rs.getArray(colName);
  }
  public synchronized Date getDate(int columnIndex, Calendar cal) throws SQLException {
    check_close();
    return rs.getDate(columnIndex, cal);
  }
  public synchronized Date getDate(String columnName, Calendar cal) throws SQLException {
    check_close();
    return rs.getDate(columnName, cal);
  }
  public synchronized Time getTime(int columnIndex, Calendar cal) throws SQLException {
    check_close();
    return rs.getTime(columnIndex, cal);
  }
  public synchronized Time getTime(String columnName, Calendar cal) throws SQLException {
    check_close();
    return rs.getTime(columnName, cal);
  }
  public synchronized Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
    check_close();
    return rs.getTimestamp(columnIndex, cal);
  }
  public synchronized Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
    check_close();
    return rs.getTimestamp(columnName, cal);
  }
  public synchronized java.net.URL getURL(int columnIndex)
          throws SQLException
  {
    check_close();
    return rs.getURL(columnIndex);
  }
  public synchronized java.net.URL getURL(String columnName)
          throws SQLException
  {
    check_close();
    return rs.getURL(columnName);
  }
  public synchronized void updateRef(int columnIndex, java.sql.Ref x) throws SQLException {
    check_close();
    rs.updateRef(columnIndex, x);
  }
  public synchronized void updateRef(String columnName, java.sql.Ref x) throws SQLException {
    check_close();
    rs.updateRef(columnName, x);
  }
  public synchronized void updateBlob(int columnIndex, java.sql.Blob x) throws SQLException {
    check_close();
    rs.updateBlob(columnIndex, x);
  }
  public synchronized void updateBlob(String columnName, java.sql.Blob x) throws SQLException {
    check_close();
    rs.updateBlob(columnName, x);
  }
  public synchronized void updateClob(int columnIndex, java.sql.Clob x) throws SQLException {
    check_close();
    rs.updateClob(columnIndex, x);
  }
  public synchronized void updateClob(String columnName, java.sql.Clob x) throws SQLException {
    check_close();
    rs.updateClob(columnName, x);
  }
  public synchronized void updateArray(int columnIndex, java.sql.Array x) throws SQLException {
    check_close();
    rs.updateArray(columnIndex, x);
  }
  public synchronized void updateArray(String columnName, java.sql.Array x) throws SQLException {
    check_close();
    rs.updateArray(columnName, x);
  }
  public synchronized RowId getRowId(int columnIndex) throws SQLException
  {
    check_close();
    return rs.getRowId(columnIndex);
  }
  public synchronized RowId getRowId(String columnLabel) throws SQLException
  {
    check_close();
    return rs.getRowId(columnLabel);
  }
  public synchronized void updateRowId(int columnIndex, RowId x) throws SQLException
  {
    check_close();
    rs.updateRowId(columnIndex, x);
  }
  public synchronized void updateRowId(String columnLabel, RowId x) throws SQLException
  {
    check_close();
    rs.updateRowId(columnLabel, x);
  }
  public synchronized int getHoldability() throws SQLException
  {
    check_close();
    return rs.getHoldability();
  }
  public synchronized boolean isClosed() throws SQLException
  {
    if (rs != null)
      return rs.isClosed();
    else
      return true;
  }
  public synchronized void updateNString(int columnIndex, String nString) throws SQLException
  {
    check_close();
    rs.updateNString(columnIndex, nString);
  }
  public synchronized void updateNString(String columnLabel, String nString) throws SQLException
  {
    check_close();
    rs.updateNString(columnLabel, nString);
  }
  public synchronized void updateNClob(int columnIndex, NClob nClob) throws SQLException
  {
    check_close();
    rs.updateNClob(columnIndex, nClob);
  }
  public synchronized void updateNClob(String columnLabel, NClob nClob) throws SQLException
  {
    check_close();
    rs.updateNClob(columnLabel, nClob);
  }
  public synchronized NClob getNClob(int columnIndex) throws SQLException
  {
    check_close();
    return rs.getNClob(columnIndex);
  }
  public synchronized NClob getNClob(String columnLabel) throws SQLException
  {
    check_close();
    return rs.getNClob(columnLabel);
  }
  public synchronized SQLXML getSQLXML(int columnIndex) throws SQLException
  {
    check_close();
    return rs.getSQLXML(columnIndex);
  }
  public synchronized SQLXML getSQLXML(String columnLabel) throws SQLException
  {
    check_close();
    return rs.getSQLXML(columnLabel);
  }
  public synchronized void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException
  {
    check_close();
    rs.updateSQLXML(columnIndex, xmlObject);
  }
  public synchronized void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException
  {
    check_close();
    rs.updateSQLXML(columnLabel, xmlObject);
  }
  public synchronized String getNString(int columnIndex) throws SQLException
  {
    check_close();
    return rs.getNString(columnIndex);
  }
  public synchronized String getNString(String columnLabel) throws SQLException
  {
    check_close();
    return rs.getNString(columnLabel);
  }
  public synchronized java.io.Reader getNCharacterStream(int columnIndex) throws SQLException
  {
    check_close();
    return rs.getNCharacterStream(columnIndex);
  }
  public synchronized java.io.Reader getNCharacterStream(String columnLabel) throws SQLException
  {
    check_close();
    return rs.getNCharacterStream(columnLabel);
  }
  public synchronized void updateNCharacterStream(int columnIndex,
   java.io.Reader x, long length) throws SQLException
  {
    check_close();
    rs.updateNCharacterStream(columnIndex, x, length);
  }
  public synchronized void updateNCharacterStream(String columnLabel,
        java.io.Reader reader,
        long length) throws SQLException
  {
    check_close();
    rs.updateNCharacterStream(columnLabel, reader, length);
  }
  public synchronized void updateAsciiStream(int columnIndex, java.io.InputStream x,
     long length) throws SQLException
  {
    check_close();
    rs.updateAsciiStream(columnIndex, x, length);
  }
  public synchronized void updateBinaryStream(int columnIndex, java.io.InputStream x,
       long length) throws SQLException
  {
    check_close();
    rs.updateBinaryStream(columnIndex, x, length);
  }
  public synchronized void updateCharacterStream(int columnIndex, java.io.Reader x,
        long length) throws SQLException
  {
    check_close();
    rs.updateCharacterStream(columnIndex, x, length);
  }
  public synchronized void updateAsciiStream(String columnLabel, java.io.InputStream x,
      long length) throws SQLException
  {
    check_close();
    rs.updateAsciiStream(columnLabel, x, length);
  }
  public synchronized void updateBinaryStream(String columnLabel, java.io.InputStream x,
       long length) throws SQLException
  {
    check_close();
    rs.updateBinaryStream(columnLabel, x, length);
  }
  public synchronized void updateCharacterStream(String columnLabel, java.io.Reader reader,
        long length) throws SQLException
  {
    check_close();
    rs.updateCharacterStream(columnLabel, reader, length);
  }
  public synchronized void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException
  {
    check_close();
    rs.updateBlob(columnIndex, inputStream, length);
  }
  public synchronized void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException
  {
    check_close();
    rs.updateBlob(columnLabel, inputStream, length);
  }
  public synchronized void updateClob(int columnIndex, Reader reader, long length) throws SQLException
  {
    check_close();
    rs.updateClob(columnIndex, reader, length);
  }
  public synchronized void updateClob(String columnLabel, Reader reader, long length) throws SQLException
  {
    check_close();
    rs.updateClob(columnLabel, reader, length);
  }
  public synchronized void updateNClob(int columnIndex, Reader reader, long length) throws SQLException
  {
    check_close();
    rs.updateNClob(columnIndex, reader, length);
  }
  public synchronized void updateNClob(String columnLabel, Reader reader, long length) throws SQLException
  {
    check_close();
    rs.updateNClob(columnLabel, reader, length);
  }
  public synchronized void updateNCharacterStream(int columnIndex, java.io.Reader x) throws SQLException
  {
    check_close();
    rs.updateNCharacterStream(columnIndex, x);
  }
  public synchronized void updateNCharacterStream(String columnLabel, java.io.Reader reader) throws SQLException
  {
    check_close();
    rs.updateNCharacterStream(columnLabel, reader);
  }
  public synchronized void updateAsciiStream(int columnIndex, java.io.InputStream x) throws SQLException
  {
    check_close();
    rs.updateAsciiStream(columnIndex, x);
  }
  public synchronized void updateBinaryStream(int columnIndex, java.io.InputStream x) throws SQLException
  {
    check_close();
    rs.updateBinaryStream(columnIndex, x);
  }
  public synchronized void updateCharacterStream(int columnIndex, java.io.Reader x) throws SQLException
  {
    check_close();
    rs.updateCharacterStream(columnIndex, x);
  }
  public synchronized void updateAsciiStream(String columnLabel, java.io.InputStream x) throws SQLException
  {
    check_close();
    rs.updateAsciiStream(columnLabel, x);
  }
  public synchronized void updateBinaryStream(String columnLabel, java.io.InputStream x) throws SQLException
  {
    check_close();
    rs.updateBinaryStream(columnLabel, x);
  }
  public synchronized void updateCharacterStream(String columnLabel, java.io.Reader reader) throws SQLException
  {
    check_close();
    rs.updateCharacterStream(columnLabel, reader);
  }
  public synchronized void updateBlob(int columnIndex, InputStream inputStream) throws SQLException
  {
    check_close();
    rs.updateBlob(columnIndex, inputStream);
  }
  public synchronized void updateBlob(String columnLabel, InputStream inputStream) throws SQLException
  {
    check_close();
    rs.updateBlob(columnLabel, inputStream);
  }
  public synchronized void updateClob(int columnIndex, Reader reader) throws SQLException
  {
    check_close();
    rs.updateClob(columnIndex, reader);
  }
  public synchronized void updateClob(String columnLabel, Reader reader) throws SQLException
  {
    check_close();
    rs.updateClob(columnLabel, reader);
  }
  public synchronized void updateNClob(int columnIndex, Reader reader) throws SQLException
  {
    check_close();
    rs.updateNClob(columnIndex, reader);
  }
  public synchronized void updateNClob(String columnLabel, Reader reader) throws SQLException
  {
    check_close();
    rs.updateNClob(columnLabel, reader);
  }
  public synchronized <T> T unwrap(java.lang.Class<T> iface) throws java.sql.SQLException
  {
    check_close();
    return rs.unwrap(iface);
  }
  public synchronized boolean isWrapperFor(java.lang.Class<?> iface) throws java.sql.SQLException
  {
    check_close();
    return rs.isWrapperFor(iface);
  }
  public <T> T getObject(int columnIndex, Class<T> type) throws SQLException
  {
    if (type == null) {
      throw new SQLException("Type parameter cannot be null", "S1009");
    }
    if (type.equals(String.class)) {
      return (T) getString(columnIndex);
    } else if (type.equals(BigDecimal.class)) {
      return (T) getBigDecimal(columnIndex);
    } else if (type.equals(Boolean.class) || type.equals(Boolean.TYPE)) {
      return (T) Boolean.valueOf(getBoolean(columnIndex));
    } else if (type.equals(Integer.class) || type.equals(Integer.TYPE)) {
      return (T) Integer.valueOf(getInt(columnIndex));
    } else if (type.equals(Long.class) || type.equals(Long.TYPE)) {
      return (T) Long.valueOf(getLong(columnIndex));
    } else if (type.equals(Float.class) || type.equals(Float.TYPE)) {
      return (T) Float.valueOf(getFloat(columnIndex));
    } else if (type.equals(Double.class) || type.equals(Double.TYPE)) {
      return (T) Double.valueOf(getDouble(columnIndex));
    } else if (type.equals(byte[].class)) {
      return (T) getBytes(columnIndex);
    } else if (type.equals(java.sql.Date.class)) {
      return (T) getDate(columnIndex);
    } else if (type.equals(Time.class)) {
      return (T) getTime(columnIndex);
    } else if (type.equals(Timestamp.class)) {
      return (T) getTimestamp(columnIndex);
    } else if (type.equals(Clob.class)) {
      return (T) getClob(columnIndex);
    } else if (type.equals(Blob.class)) {
      return (T) getBlob(columnIndex);
    } else if (type.equals(Array.class)) {
      return (T) getArray(columnIndex);
    } else if (type.equals(Ref.class)) {
      return (T) getRef(columnIndex);
    } else if (type.equals(java.net.URL.class)) {
      return (T) getURL(columnIndex);
    } else {
      try {
        return (T) getObject(columnIndex);
      } catch (ClassCastException cce) {
         throw new SQLException ("Conversion not supported for type " + type.getName(),
                    "S1009");
      }
    }
  }
  public <T> T getObject(String columnLabel, Class<T> type) throws SQLException
  {
    return getObject(findColumn(columnLabel), type);
  }
  private void check_close() throws SQLException
  {
    if (rs == null)
      throw OPLMessage_x.makeException (OPLMessage_x.errx_ResultSet_is_closed);
  }
}
