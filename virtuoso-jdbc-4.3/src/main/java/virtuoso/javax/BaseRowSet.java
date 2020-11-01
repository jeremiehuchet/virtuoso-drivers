package virtuoso.javax;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Calendar;
import java.util.Iterator;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.math.BigDecimal;
import javax.sql.*;
import java.sql.*;
public abstract class BaseRowSet implements RowSet, Serializable {
    private static final long serialVersionUID = 5374661472998522423L;
    protected static final int ev_CursorMoved = 1;
    protected static final int ev_RowChanged = 2;
    protected static final int ev_RowSetChanged = 3;
    private String command;
    private String url;
    private String dataSource;
    private transient String username;
    private transient String password;
    private int rsType = ResultSet.TYPE_SCROLL_INSENSITIVE;
    private int rsConcurrency = ResultSet.CONCUR_UPDATABLE;
    private int queryTimeout = 0;
    private int maxRows = 0;
    private int maxFieldSize = 0;
    private boolean readOnly = true;
    private boolean escapeProcessing = true;
    private int txn_isolation = Connection.TRANSACTION_READ_COMMITTED;
    private int fetchDir = ResultSet.FETCH_FORWARD;
    private int fetchSize = 0;
    protected java.util.Map<String,Class<?>> map = null;
    private LinkedList<RowSetListener> listeners;
    private ArrayList<Parameter> params;
  public BaseRowSet() {
    listeners = new LinkedList<RowSetListener>();
    params = new ArrayList<Parameter>();
  }
  public void close() throws SQLException{
    clearParameters();
    listeners.clear();
  }
  public void addRowSetListener(RowSetListener rowsetlistener) {
    synchronized(listeners) {
        listeners.add(rowsetlistener);
    }
  }
  public void removeRowSetListener(RowSetListener rowsetlistener) {
    synchronized(listeners) {
        listeners.remove(rowsetlistener);
    }
  }
  public void clearParameters() throws SQLException {
    params.clear();
  }
  public String getCommand() {
    return command;
  }
  public int getConcurrency() throws SQLException {
    return rsConcurrency;
  }
  public String getDataSourceName() {
    return dataSource;
  }
  public boolean getEscapeProcessing() throws SQLException {
    return escapeProcessing;
  }
  public int getFetchDirection() throws SQLException {
    return fetchDir;
  }
  public int getFetchSize() throws SQLException {
    return fetchSize;
  }
  public int getMaxFieldSize() throws SQLException {
    return maxFieldSize;
  }
  public int getMaxRows() throws SQLException {
    return maxRows;
  }
  public Object[] getParams() throws SQLException {
    return params.toArray();
  }
  public String getPassword() {
    return password;
  }
  public int getQueryTimeout() throws SQLException {
    return queryTimeout;
  }
  public int getTransactionIsolation() {
    return txn_isolation;
  }
  public int getType() throws SQLException {
    return rsType;
  }
  public Map<String,Class<?>> getTypeMap() throws SQLException{
    return map;
  }
  public String getUrl() throws SQLException {
    return url;
  }
  public String getUsername() {
    return username;
  }
  public boolean isReadOnly() {
    return readOnly;
  }
  public synchronized void setArray(int parameterIndex, Array x)
      throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = x;
    param.jType = Parameter.jObject;
  }
  public synchronized void setAsciiStream(int parameterIndex, InputStream x, int length)
        throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = x;
    param.jType = Parameter.jAsciiStream;
    param.length = length;
  }
  public synchronized void setBigDecimal(int parameterIndex, BigDecimal x)
        throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = x;
    param.jType = Parameter.jObject;
  }
  public synchronized void setBinaryStream(int parameterIndex, InputStream x, int length)
        throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = x;
    param.jType = Parameter.jBinaryStream;
    param.length = length;
  }
  public synchronized void setBlob(int parameterIndex, Blob x)
        throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = x;
    param.jType = Parameter.jObject;
  }
  public synchronized void setBlob(int parameterIndex, InputStream inputStream, long length)
        throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = inputStream;
    param.jType = Parameter.jBinaryStream;
    param.length = (int)length;
  }
  public void setBlob(int parameterIndex, InputStream inputStream)
        throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setBlob(parameterIndex, inputStream)");
  }
  public void setBlob(String parameterName, InputStream inputStream, long length)
        throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setBlob(parameterName, inputStream, length)");
  }
  public void setBlob (String parameterName, Blob x) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setBlob (parameterName, x)");
  }
  public void setBlob(String parameterName, InputStream inputStream)
        throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setBlob(parameterName, inputStream)");
  }
  public synchronized void setClob(int parameterIndex, Reader reader, long length)
       throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = reader;
    param.jType = Parameter.jCharacterStream;
    param.length = (int)length;
  }
  public void setClob(int parameterIndex, Reader reader) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setClob(parameterIndex, reader)");
  }
  public void setClob(String parameterName, Reader reader, long length)
       throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setClob(parameterName, reader, length)");
  }
  public void setClob (String parameterName, Clob x) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setClob (String parameterName, Clob x)");
  }
  public void setClob(String parameterName, Reader reader) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setClob(parameterName, reader)");
  }
  public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setSQLXML(parameterIndex, xmlObject)");
  }
  public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setSQLXML(parameterName, xmlObject)");
  }
  public void setRowId(int parameterIndex, RowId x) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setRowId(parameterIndex, x)");
  }
  public void setRowId(String parameterName, RowId x) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setRowId(parameterName, x)");
  }
  public void setNString(int parameterIndex, String value) throws SQLException
  {
    setString(parameterIndex, value);
  }
  public void setNString(String parameterName, String value)
            throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setNString(parameterName, value)");
  }
  public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException
  {
    setCharacterStream(parameterIndex, value, (int)length);
  }
  public void setNCharacterStream(String parameterName, Reader value, long length)
            throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setNCharacterStream(parameterName, value, length)");
  }
  public void setNCharacterStream(String parameterName, Reader value) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setNCharacterStream(parameterName, value)");
  }
  public void setNClob(String parameterName, NClob value) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setNClob(parameterName, value)");
  }
  public synchronized void setNClob(String parameterName, Reader reader, long length)
       throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setNClob(parameterName, value)");
  }
  public void setNClob(String parameterName, Reader reader) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setNClob(parameterName, reader)");
  }
  public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = reader;
    param.jType = Parameter.jCharacterStream;
    param.length = (int)length;
  }
  public synchronized void setNClob(int parameterIndex, NClob value) throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = value;
    param.jType = Parameter.jObject;
  }
  public void setNClob(int parameterIndex, Reader reader) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setNClob(parameterIndex, reader)");
  }
  public synchronized void setBoolean(int parameterIndex, boolean x)
        throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = new Boolean(x);
    param.jType = Parameter.jObject;
  }
  public synchronized void setByte(int parameterIndex, byte x)
        throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = new Byte(x);
    param.jType = Parameter.jObject;
  }
  public synchronized void setBytes(int parameterIndex, byte[] x)
        throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = x;
    param.jType = Parameter.jObject;
  }
  public synchronized void setCharacterStream(int parameterIndex, Reader x, int length)
        throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = x;
    param.jType = Parameter.jCharacterStream;
    param.length = length;
  }
 public void setAsciiStream(int parameterIndex, java.io.InputStream x)
                      throws SQLException
 {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setAsciiStream(parameterIndex, x)");
 }
  public void setAsciiStream(String parameterName, java.io.InputStream x)
            throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setAsciiStream(parameterName, x)");
  }
  public void setBinaryStream(int parameterIndex, java.io.InputStream x)
                       throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setBinaryStream(parameterIndex, x)");
  }
  public void setBinaryStream(String parameterName, java.io.InputStream x)
    throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setBinaryStream(parameterName, x)");
  }
  public void setCharacterStream(int parameterIndex,
                          java.io.Reader reader) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setCharacterStream(parameterIndex, reader)");
  }
  public void setCharacterStream(String parameterName,
                          java.io.Reader reader) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setCharacterStream(parameterName, reader)");
  }
  public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setNCharacterStream(parameterIndex, value)");
  }
  public synchronized void setClob(int parameterIndex, Clob x)
        throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = x;
    param.jType = Parameter.jObject;
  }
  public synchronized void setDate(int parameterIndex, Date x)
        throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = x;
    param.jType = Parameter.jObject;
  }
  public synchronized void setDate(int parameterIndex, Date x, Calendar cal)
        throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = x;
    param.jType = Parameter.jDateWithCalendar;
    param.cal = cal;
  }
  public synchronized void setDouble(int parameterIndex, double x)
        throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = new Double(x);
    param.jType = Parameter.jObject;
  }
  public synchronized void setFloat(int parameterIndex, float x)
        throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = new Float(x);
    param.jType = Parameter.jObject;
  }
  public synchronized void setInt(int parameterIndex, int x)
        throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = new Integer(x);
    param.jType = Parameter.jObject;
  }
  public synchronized void setLong(int parameterIndex, long x)
        throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = new Long(x);
    param.jType = Parameter.jObject;
  }
  public synchronized void setRef(int parameterIndex, Ref x)
        throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = x;
    param.jType = Parameter.jObject;
  }
  public synchronized void setShort(int parameterIndex, short x)
        throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = new Short(x);
    param.jType = Parameter.jObject;
  }
  public synchronized void setString(int parameterIndex, String x)
        throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = x;
    param.jType = Parameter.jObject;
  }
  public synchronized void setURL(int parameterIndex, java.net.URL x) throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = x;
    param.jType = Parameter.jObject;
  }
  public synchronized void setTime(int parameterIndex, Time x)
        throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = x;
    param.jType = Parameter.jObject;
  }
  public synchronized void setTime(int parameterIndex, Time x, Calendar cal)
        throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = x;
    param.jType = Parameter.jTimeWithCalendar;
    param.cal = cal;
  }
  public synchronized void setTimestamp(int parameterIndex, Timestamp x)
        throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = x;
    param.jType = Parameter.jObject;
  }
  public synchronized void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
        throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = x;
    param.jType = Parameter.jObject;
    param.cal = cal;
  }
  public synchronized void setUnicodeStream(int parameterIndex, InputStream x, int length)
        throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = x;
    param.jType = Parameter.jUnicodeStream;
    param.length = length;
  }
  public synchronized void setNull(int parameterIndex, int sqlType)
        throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = null;
    param.jType = Parameter.jNull_1;
    param.sqlType = sqlType;
  }
  public synchronized void setNull(int parameterIndex, int sqlType, String typeName)
        throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = null;
    param.jType = Parameter.jNull_2;
    param.sqlType = sqlType;
    param.typeName = typeName;
  }
  public synchronized void setObject(int parameterIndex, Object x)
        throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = x;
    param.jType = Parameter.jObject;
  }
  public synchronized void setObject(int parameterIndex, Object x, int targetSqlType)
        throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = x;
    param.jType = Parameter.jObject;
    param.sqlType = targetSqlType;
  }
  public synchronized void setObject(int parameterIndex, Object x, int targetSqlType, int scale)
        throws SQLException
  {
    Parameter param = getParam(parameterIndex);
    param.value = x;
    param.jType = Parameter.jObject;
    param.sqlType = targetSqlType;
    param.scale = scale;
  }
  public void setNull(String parameterName, int sqlType) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setNull(parameterName, sqlType)");
  }
  public void setNull (String parameterName, int sqlType, String typeName)
        throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setNull (parameterName, sqlType, typeName)");
  }
  public void setBoolean(String parameterName, boolean x) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setBoolean(parameterName, x)");
  }
  public void setByte(String parameterName, byte x) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setByte(parameterName, x)");
  }
  public void setShort(String parameterName, short x) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setShort(parameterName, x)");
  }
  public void setInt(String parameterName, int x) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setInt(parameterName, x)");
  }
  public void setLong(String parameterName, long x) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setLong(parameterName, x)");
  }
  public void setFloat(String parameterName, float x) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setFloat(parameterName, x)");
  }
  public void setDouble(String parameterName, double x) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setDouble(parameterName, x)");
  }
  public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setBigDecimal(parameterName, x)");
  }
  public void setString(String parameterName, String x) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setString(parameterName, x)");
  }
  public void setBytes(String parameterName, byte x[]) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setBytes(parameterName, x[])");
  }
  public void setTimestamp(String parameterName, java.sql.Timestamp x) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setTimestamp(parameterName, x)");
  }
  public void setAsciiStream(String parameterName, java.io.InputStream x, int length) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setAsciiStream(parameterName, x, length)");
  }
  public void setBinaryStream(String parameterName, java.io.InputStream x, int length) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setBinaryStream(parameterName, x, length)");
  }
  public void setCharacterStream(String parameterName, java.io.Reader reader, int length) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setCharacterStream(parameterName, reader, length)");
  }
  public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setObject(parameterName, x, targetSqlType, scale)");
  }
  public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setObject(parameterName, x, targetSqlType)");
  }
  public void setObject(String parameterName, Object x) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setObject(parameterName, x)");
  }
  public void setDate(String parameterName, java.sql.Date x) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setDate(parameterName, x)");
  }
  public void setDate(String parameterName, java.sql.Date x, Calendar cal) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setDate(parameterName, x, cal)");
  }
  public void setTime(String parameterName, java.sql.Time x) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setTime(parameterName, x)");
  }
  public void setTime(String parameterName, java.sql.Time x, Calendar cal) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setTime(parameterName, x, cal)");
  }
  public void setTimestamp(String parameterName, java.sql.Timestamp x, Calendar cal) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "setTimestamp(parameterName, x, cal)");
  }
  public synchronized void setCommand(String s)
        throws SQLException
  {
    command = new String(s);
    params.clear();
  }
  public void setConcurrency(int i) throws SQLException {
    rsConcurrency = i;
  }
  public void setDataSourceName(String s) throws SQLException {
    if(s != null)
       dataSource = new String(s);
    else
       dataSource = null;
    url = null;
  }
  public void setEscapeProcessing(boolean flag) throws SQLException {
    escapeProcessing = flag;
  }
  public void setFetchDirection(int direction) throws SQLException {
    fetchDir = direction;
  }
  public void setFetchSize(int rows) throws SQLException {
    fetchSize = rows;
  }
  public void setMaxFieldSize(int max) throws SQLException {
    maxFieldSize = max;
  }
  public void setMaxRows(int max) throws SQLException {
    maxRows = max;
  }
  public void setQueryTimeout(int seconds) throws SQLException {
    queryTimeout = seconds;
  }
  public void setReadOnly(boolean value) throws SQLException {
    readOnly = value;
  }
  public void setPassword(String s) throws SQLException {
    if (s != null)
      password = new String(s);
    else
      password = null;
  }
  public void setTransactionIsolation(int value) throws SQLException
  {
    txn_isolation = value;
  }
  public void setType(int value) throws SQLException
  {
    rsType = value;
  }
  public void setTypeMap(Map<String,Class<?>> value) throws SQLException
  {
     map = value;
  }
  public void setUrl(String s) throws SQLException {
    if(s != null)
      url = new String(s);
    else
      url = null;
    dataSource = null;
  }
  public void setUsername(String s) throws SQLException {
    if ( s!= null)
      username = new String(s);
    else
      username = null;
  }
  protected void notifyListener(int event) {
    if(!listeners.isEmpty()) {
       LinkedList l = (LinkedList)listeners.clone();
       RowSetEvent ev = new RowSetEvent(this);
       for(Iterator i = l.iterator(); i.hasNext(); )
         switch (event) {
           case ev_CursorMoved:
              ((RowSetListener)i.next()).cursorMoved(ev);
              break;
           case ev_RowChanged:
              ((RowSetListener)i.next()).rowChanged(ev);
              break;
           case ev_RowSetChanged:
              ((RowSetListener)i.next()).rowSetChanged(ev);
              break;
         }
       l.clear();
    }
  }
  protected Parameter getParam(int paramIndex)
    throws SQLException
  {
    if(paramIndex < 1)
        throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Invalid_parameter_index_XX, String.valueOf(paramIndex));
    paramIndex--;
    int sz = params.size();
    if( paramIndex < sz ) {
      return (Parameter)params.get(paramIndex);
    } else {
       for(; sz < paramIndex; sz++)
           params.add(new Parameter());
       Parameter param = new Parameter();
       params.add(param);
       return param;
    }
  }
   protected class Parameter {
    protected Object value;
    protected int sqlType = java.sql.Types.VARCHAR;
    protected String typeName;
    protected int scale;
    protected int length;
    protected Calendar cal;
    protected int jType = jObject;
    protected static final int jObject = 0;
    protected static final int jObject_1 = 1;
    protected static final int jObject_2 = 2;
    protected static final int jAsciiStream = 3;
    protected static final int jBinaryStream = 4;
    protected static final int jUnicodeStream = 5;
    protected static final int jCharacterStream = 6;
    protected static final int jDateWithCalendar = 7;
    protected static final int jTimeWithCalendar = 8;
    protected static final int jTimestampWithCalendar = 9;
    protected static final int jNull_1 = 10;
    protected static final int jNull_2 = 11;
  }
}
