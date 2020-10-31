package virtuoso.jdbc4;
import java.sql.*;
import java.util.*;
import java.io.*;
import java.math.*;
import openlink.util.*;
import java.sql.RowId;
import java.sql.SQLXML;
import java.sql.NClob;
public class VirtuosoPreparedStatement extends VirtuosoStatement implements PreparedStatement
{
   protected String sql;
   private static final int _EXECUTE_FAILED = Statement.EXECUTE_FAILED;
   VirtuosoPreparedStatement(VirtuosoConnection connection, String sql) throws VirtuosoException
   {
      this (connection,sql,VirtuosoResultSet.TYPE_FORWARD_ONLY,VirtuosoResultSet.CONCUR_READ_ONLY);
   }
   VirtuosoPreparedStatement(VirtuosoConnection connection, String sql, int type, int concurrency) throws VirtuosoException
   {
      super(connection,type,concurrency);
      synchronized (connection)
 {
   try
     {
       this.sql = sql;
       parse_sql();
       Object[] args = new Object[4];
       args[0] = (statid == null) ? statid = new String("s" + connection.hashCode() + (req_no++)) : statid;
       args[1] = connection.escapeSQL(sql);
       args[2] = new Long(0);
       args[3] = getStmtOpts();
       future = connection.getFuture(VirtuosoFuture.prepare,args, this.rpc_timeout);
       vresultSet = new VirtuosoResultSet(this,metaData);
              clearParameters();
     }
   catch(IOException e)
     {
       throw new VirtuosoException("Problem during serialization : " + e.getMessage(),VirtuosoException.IOERROR);
     }
 }
   }
   private void parse_sql()
   {
      String sql = this.sql;
      int count = 0;
      do
      {
         int index = sql.indexOf("?");
         if(index >= 0)
         {
            count++;
            sql = sql.substring(index + 1,sql.length());
            if(sql == null)
               sql = "";
         }
         else
            sql = "";
      }
      while(sql.length() != 0);
      parameters = new openlink.util.Vector(count);
      objparams = new openlink.util.Vector(count);
   }
   private void sendQuery() throws VirtuosoException
   {
     synchronized (connection)
       {
  Object[] args = new Object[6];
  openlink.util.Vector vect = new openlink.util.Vector(1);
  args[0] = statid;
  args[2] = (cursorName == null) ? args[0] : cursorName;
  args[1] = null;
  args[3] = vect;
  args[4] = null;
  try
    {
      vect.addElement(objparams);
      args[5] = getStmtOpts();
      future = connection.getFuture(VirtuosoFuture.exec,args, this.rpc_timeout);
      vresultSet.getMoreResults();
    }
  catch(IOException e)
    {
      throw new VirtuosoException("Problem during serialization : " + e.getMessage(),VirtuosoException.IOERROR);
    }
       }
   }
   protected void setVector(int parameterIndex, openlink.util.Vector x) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      if(x == null) this.setNull(parameterIndex, Types.ARRAY);
  else objparams.setElementAt(x,parameterIndex - 1);
   }
   public void clearParameters() throws VirtuosoException
   {
      objparams.removeAllElements();
   }
   public boolean execute() throws VirtuosoException
   {
      exec_type = VirtuosoTypes.QT_UNKNOWN;
      sendQuery();
      return (vresultSet.kindop() != VirtuosoTypes.QT_UPDATE);
   }
   public int executeUpdate() throws VirtuosoException
   {
      exec_type = VirtuosoTypes.QT_UPDATE;
      sendQuery();
      return vresultSet.getUpdateCount();
   }
   public int[] executeBatchUpdate() throws BatchUpdateException
   {
     int size = batch.size();
     int[] res = new int[size];
     int inx = 0;
     synchronized (connection)
       {
  Object[] args = new Object[6];
  args[0] = statid;
  args[2] = (cursorName == null) ? args[0] : cursorName;
  args[1] = null;
  args[3] = batch;
  args[4] = null;
  try
    {
      args[5] = getStmtOpts();
      future = connection.getFuture(VirtuosoFuture.exec,args, this.rpc_timeout);
      for (inx = 0; inx < size; inx++)
      {
   vresultSet.setUpdateCount (0);
   vresultSet.getMoreResults ();
   res[inx] = vresultSet.getUpdateCount();
      }
    }
  catch(IOException e)
    {
      throwBatchUpdateException(res, "Problem during serialization : " + e.getMessage(), inx);
    }
  catch(VirtuosoException e)
   {
      throwBatchUpdateException (res, e, inx);
   }
       }
     return res;
   }
   public ResultSet executeQuery() throws VirtuosoException
   {
      exec_type = VirtuosoTypes.QT_SELECT;
      sendQuery();
      return vresultSet;
   }
   public ResultSetMetaData getMetaData() throws VirtuosoException
   {
      if(vresultSet != null)
         return vresultSet.getMetaData();
      throw new VirtuosoException("Prepared statement closed",VirtuosoException.CLOSED);
   }
   public void close() throws VirtuosoException
   {
    if (isCached) {
      close_flag = true;
      try {
        connection.recacheStmt(this);
      } catch (SQLException ex) {
        throw new VirtuosoException(ex.getMessage(), ex.getSQLState(), ex.getErrorCode());
      }
      return;
    }
     synchronized (connection)
       {
  try
    {
      if(statid == null)
        return;
      cancel();
      Object[] args = new Object[2];
      args[0] = statid;
      args[1] = new Long(VirtuosoTypes.STAT_CLOSE);
      future = connection.getFuture(VirtuosoFuture.close,args, this.rpc_timeout);
      future.nextResult();
      connection.removeFuture(future);
      future = null;
    }
  catch(IOException e)
    {
      throw new VirtuosoException("Problem during closing : " + e.getMessage(),VirtuosoException.IOERROR);
    }
       }
   }
   public void setAsciiStream(int parameterIndex, InputStream x, int length) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      Object _obj = objparams.elementAt(parameterIndex - 1);
      if (parameters != null && parameters.elementAt(parameterIndex - 1) instanceof openlink.util.Vector)
 {
   openlink.util.Vector pd = (openlink.util.Vector)parameters.elementAt(parameterIndex - 1);
   int dtp = ((Number)pd.elementAt (0)).intValue();
   if (dtp != VirtuosoTypes.DV_BLOB &&
       dtp != VirtuosoTypes.DV_BLOB_BIN &&
       dtp != VirtuosoTypes.DV_BLOB_WIDE)
     throw new VirtuosoException ("Passing streams to non-blob columns not supported",
  "IM001", VirtuosoException.NOTIMPLEMENTED);
   if (dtp == VirtuosoTypes.DV_BLOB_BIN)
     throw new VirtuosoException ("Passing ASCII stream to LONG VARBINARY columns not supported",
  "IM001", VirtuosoException.NOTIMPLEMENTED);
 }
      if(_obj instanceof VirtuosoBlob)
 {
   ((VirtuosoBlob)_obj).setInputStream(x,length);
   try
     {
       ((VirtuosoBlob)_obj).setReader(new InputStreamReader (x, "ASCII"),length);
     }
   catch (UnsupportedEncodingException e)
     {
       ((VirtuosoBlob)_obj).setReader(new InputStreamReader (x),length);
     }
 }
      else
 {
   if(x == null)
     this.setNull(parameterIndex, Types.CLOB);
   else
     {
       InputStreamReader rd;
       try
  {
    rd = new InputStreamReader (x, "ASCII");
  }
       catch (UnsupportedEncodingException e)
  {
    rd = new InputStreamReader (x);
  }
       VirtuosoBlob bl = new VirtuosoBlob(rd, length, parameterIndex - 1);
       bl.setInputStream (x, length);
       objparams.setElementAt(bl, parameterIndex - 1);
     }
 }
   }
   public void setBigDecimal(int parameterIndex, BigDecimal x) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      if(x == null) this.setNull(parameterIndex, Types.NUMERIC);
  else objparams.setElementAt(x,parameterIndex - 1);
   }
   public void setBinaryStream(int parameterIndex, InputStream x, int length) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      Object _obj = objparams.elementAt(parameterIndex - 1);
      if (parameters != null && parameters.elementAt(parameterIndex - 1) instanceof openlink.util.Vector)
 {
   openlink.util.Vector pd = (openlink.util.Vector)parameters.elementAt(parameterIndex - 1);
   int dtp = ((Number)pd.elementAt (0)).intValue();
   if (dtp != VirtuosoTypes.DV_BLOB &&
       dtp != VirtuosoTypes.DV_BLOB_BIN &&
       dtp != VirtuosoTypes.DV_BLOB_WIDE)
     throw new VirtuosoException ("Passing streams to non-blob columns not supported",
  "IM001", VirtuosoException.NOTIMPLEMENTED);
   if (dtp == VirtuosoTypes.DV_BLOB_WIDE)
     throw new VirtuosoException ("Passing binary stream to LONG NVARCHAR columns not supported",
  "IM001", VirtuosoException.NOTIMPLEMENTED);
 }
      if(_obj instanceof VirtuosoBlob)
 {
   ((VirtuosoBlob)_obj).setInputStream(x,length);
   try
     {
       ((VirtuosoBlob)_obj).setReader(new InputStreamReader (x, "8859_1"),length);
     }
   catch (UnsupportedEncodingException e)
     {
       ((VirtuosoBlob)_obj).setReader(new InputStreamReader (x),length);
     }
 }
      else
 {
   if(x == null)
     this.setNull(parameterIndex, Types.BLOB);
   else
     {
       InputStreamReader rd;
       try
  {
    rd = new InputStreamReader (x, "8859_1");
  }
       catch (UnsupportedEncodingException e)
  {
    rd = new InputStreamReader (x);
  }
       VirtuosoBlob bl = new VirtuosoBlob(rd, length, parameterIndex - 1);
       bl.setInputStream (x, length);
       objparams.setElementAt(bl, parameterIndex - 1);
     }
 }
   }
   public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      Object _obj = objparams.elementAt(parameterIndex - 1);
      if (parameters != null && parameters.elementAt(parameterIndex - 1) instanceof openlink.util.Vector)
 {
   openlink.util.Vector pd = (openlink.util.Vector)parameters.elementAt(parameterIndex - 1);
   int dtp = ((Number)pd.elementAt (0)).intValue();
   if (dtp != VirtuosoTypes.DV_BLOB &&
       dtp != VirtuosoTypes.DV_BLOB_BIN &&
       dtp != VirtuosoTypes.DV_BLOB_WIDE)
     throw new VirtuosoException ("Passing streams to non-blob columns not supported",
  "IM001", VirtuosoException.NOTIMPLEMENTED);
   if (dtp == VirtuosoTypes.DV_BLOB_BIN)
     throw new VirtuosoException ("Passing unicode stream to LONG VARBINARY columns not supported",
  "IM001", VirtuosoException.NOTIMPLEMENTED);
 }
      if(_obj instanceof VirtuosoBlob)
 {
   ((VirtuosoBlob)_obj).setInputStream(x,length);
   try
     {
       ((VirtuosoBlob)_obj).setReader(new InputStreamReader (x, "UTF8"),length);
     }
   catch (UnsupportedEncodingException e)
     {
       ((VirtuosoBlob)_obj).setReader(new InputStreamReader (x),length);
     }
 }
      else
 {
   if(x == null)
     this.setNull(parameterIndex, Types.CLOB);
   else
     {
       InputStreamReader rd;
       try
  {
    rd = new InputStreamReader (x, "UTF8");
  }
       catch (UnsupportedEncodingException e)
  {
    rd = new InputStreamReader (x);
  }
       VirtuosoBlob bl = new VirtuosoBlob(rd, length, parameterIndex - 1);
       bl.setInputStream (x, length);
       objparams.setElementAt(bl, parameterIndex - 1);
     }
 }
   }
   public void setBoolean(int parameterIndex, boolean x) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      objparams.setElementAt(new Boolean(x),parameterIndex - 1);
   }
   public void setByte(int parameterIndex, byte x) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      objparams.setElementAt(new Byte(x),parameterIndex - 1);
   }
   public void setBytes(int parameterIndex, byte x[]) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex +
      " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      if(x == null)
 this.setNull(parameterIndex, Types.VARBINARY);
      else
 {
   objparams.setElementAt(x, parameterIndex - 1);
 }
   }
   public void setDate(int parameterIndex, java.sql.Date x) throws VirtuosoException
   {
      setDate(parameterIndex,x,null);
   }
   public void setDouble(int parameterIndex, double x) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      objparams.setElementAt(new Double(x),parameterIndex - 1);
   }
   public void setFloat(int parameterIndex, float x) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      objparams.setElementAt(new Float(x),parameterIndex - 1);
   }
   public void setInt(int parameterIndex, int x) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      objparams.setElementAt(new Integer(x),parameterIndex - 1);
   }
   public void setLong(int parameterIndex, long x) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      objparams.setElementAt(new Long(x),parameterIndex - 1);
   }
   public void setNull(int parameterIndex, int sqlType) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      objparams.setElementAt(new VirtuosoNullParameter(sqlType, true),parameterIndex - 1);
   }
   public void setObject(int parameterIndex, Object x) throws VirtuosoException
   {
      setObject(parameterIndex,x,Types.OTHER);
   }
   public void setObject(int parameterIndex, Object x, int targetSqlType) throws VirtuosoException
   {
      setObject(parameterIndex,x,targetSqlType, 0);
   }
   protected Object mapJavaTypeToSqlType (Object x, int targetSqlType, int scale) throws VirtuosoException
   {
     if (x == null)
       return x;
     if (x instanceof java.lang.Boolean)
       x = new Integer (((Boolean)x).booleanValue() ? 1 : 0);
     switch (targetSqlType)
       {
   case Types.CHAR:
   case Types.VARCHAR:
       if (x instanceof java.util.Date || x instanceof java.lang.String)
  return x;
       else
  return x.toString();
   case Types.LONGVARCHAR:
              if (x instanceof java.sql.Clob || x instanceof java.sql.Blob || x instanceof java.lang.String)
                return x;
              else
  return x.toString();
   case Types.DATE:
   case Types.TIME:
   case Types.TIMESTAMP:
       if (x instanceof java.util.Date || x instanceof java.lang.String)
  return x;
              break;
          case Types.NUMERIC:
          case Types.DECIMAL:
              {
                java.math.BigDecimal bd = null;
  if (x instanceof java.math.BigDecimal)
    bd = (java.math.BigDecimal) x;
                else if (x instanceof java.lang.String)
    bd = new java.math.BigDecimal ((String) x);
  else if (x instanceof java.lang.Number)
    bd = new java.math.BigDecimal (x.toString());
                if (bd != null)
    return bd.setScale (scale);
              }
       break;
          case Types.BIGINT:
              if (x instanceof java.math.BigDecimal || x instanceof java.lang.String)
                return new Long(x.toString());
              else if (x instanceof java.lang.Number)
                return new Long(((Number)x).longValue());
       break;
          case Types.FLOAT:
          case Types.DOUBLE:
              if (x instanceof java.lang.Double)
                return x;
              else if (x instanceof java.lang.Number)
                return new Double (((Number)x).doubleValue());
              else if (x instanceof java.lang.String)
                return new Double ((String) x);
       break;
          case Types.INTEGER:
              if (x instanceof java.lang.Integer)
                return x;
              else if (x instanceof java.lang.Number)
                return new Integer (((Number)x).intValue());
              else if (x instanceof java.lang.String)
                return new Integer ((String) x);
       break;
          case Types.REAL:
              if (x instanceof java.lang.Float)
                return x;
              else if (x instanceof java.lang.Number)
                return new Float (((Number)x).floatValue());
              else if (x instanceof java.lang.String)
                return new Float ((String) x);
       break;
          case Types.SMALLINT:
          case Types.TINYINT:
          case Types.BIT:
          case Types.BOOLEAN:
              if (x instanceof java.lang.Short)
                return x;
              else if (x instanceof java.lang.String)
                return new Short ((String) x);
              else if (x instanceof java.lang.Number)
                return new Short (((Number)x).shortValue());
       break;
   case Types.ARRAY:
   case Types.DATALINK:
      case Types.ROWID:
          case Types.DISTINCT:
   case Types.REF:
       throw new VirtuosoException ("Type not supported", VirtuosoException.NOTIMPLEMENTED);
          case Types.VARBINARY:
              if (x instanceof byte[])
                return x;
              break;
          case Types.LONGVARBINARY:
              if (x instanceof java.sql.Blob || x instanceof byte [])
                return x;
              break;
   default:
       return x;
       }
     throw new VirtuosoException ("Invalid value specified", VirtuosoException.BADPARAM);
   }
   public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      if (x instanceof VirtuosoExplicitString)
 {
   objparams.setElementAt(x, parameterIndex - 1);
   return;
 }
      Object _obj = objparams.elementAt(parameterIndex - 1);
      if(_obj instanceof VirtuosoBlob)
      {
         ((VirtuosoBlob)_obj).setObject(x);
         return;
      }
      if(x == null) this.setNull(parameterIndex, Types.OTHER);
      x = mapJavaTypeToSqlType (x, targetSqlType, scale);
      if (x instanceof java.io.Serializable)
 {
   objparams.setElementAt (x, parameterIndex - 1);
 }
      else
 throw new VirtuosoException ("Object " + x.getClass().getName() + " not serializable", "22023",
     VirtuosoException.BADFORMAT);
   }
   public void setShort(int parameterIndex, short x) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      objparams.setElementAt(new Short(x),parameterIndex - 1);
   }
   public void setString(int parameterIndex, String x1) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " +
      parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      if(x1 == null)
 this.setNull(parameterIndex, Types.VARCHAR);
      else
 {
   String x;
     x = x1;
   if (parameters != null && parameters.elementAt(parameterIndex - 1) instanceof openlink.util.Vector)
     {
       openlink.util.Vector pd = (openlink.util.Vector)parameters.elementAt(parameterIndex - 1);
       int dtp = ((Number)pd.elementAt (0)).intValue();
       VirtuosoExplicitString ret;
       ret = new
    VirtuosoExplicitString (x, dtp, connection);
       objparams.setElementAt (ret, parameterIndex - 1);
     }
   else
     objparams.setElementAt(x,parameterIndex - 1);
 }
   }
   protected void setString(int parameterIndex, VirtuosoExplicitString x1) throws VirtuosoException
   {
     if(parameterIndex < 1 || parameterIndex > parameters.capacity())
       throw new VirtuosoException("Index " +
    parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
     if(x1 == null)
       this.setNull(parameterIndex, Types.VARCHAR);
     else
        objparams.setElementAt(x1, parameterIndex - 1);
   }
   public void setTime(int parameterIndex, java.sql.Time x) throws VirtuosoException
   {
      setTime(parameterIndex,x,null);
   }
   public void setTimestamp(int parameterIndex, java.sql.Timestamp x) throws VirtuosoException
   {
      setTimestamp(parameterIndex,x,null);
   }
   public void addBatch() throws VirtuosoException
   {
      if(parameters == null)
         return;
      if(batch == null)
         batch = new LinkedList<Object>();
      batch.add(objparams.clone());
   }
   private void throwBatchUpdateException (int [] result, SQLException ex, int inx) throws BatchUpdateException
   {
     int [] _result = new int[inx + 1];
     System.arraycopy (result, 0, _result, 0, inx);
     _result[inx] = _EXECUTE_FAILED;
     throw new BatchUpdateException(ex.getMessage(), ex.getSQLState(), ex.getErrorCode(), _result);
   }
   private void throwBatchUpdateException (int [] result, String mess, int inx) throws BatchUpdateException
   {
     int [] _result = new int[inx + 1];
     System.arraycopy (result, 0, _result, 0, inx);
     _result[inx] = _EXECUTE_FAILED;
     throw new BatchUpdateException(mess, "HY000", 0, _result);
   }
   public int[] executeBatch() throws BatchUpdateException
   {
      if(batch == null)
         return new int[0];
      int[] result = new int[batch.size()];
      boolean error = false;
      if (this instanceof VirtuosoCallableStatement && ((VirtuosoCallableStatement)this).hasOut())
 throwBatchUpdateException (result, "Batch can't execute calls with out params", 0);
      try
 {
         if (vresultSet.kindop()==VirtuosoTypes.QT_SELECT)
     throwBatchUpdateException (result, "Batch executes only update statements", 0);
   result = executeBatchUpdate ();
 }
      catch(VirtuosoException ex)
        {
     throwBatchUpdateException (result, ex, 0);
 }
      finally
        {
   batch.clear();
 }
      return result;
   }
   public void setArray(int i, Array x) throws VirtuosoException
   {
      if(i < 1 || i > parameters.capacity())
         throw new VirtuosoException("Index " + i + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      if(x == null) this.setNull(i, Types.ARRAY);
      else objparams.setElementAt(x,i - 1);
   }
   public void setBlob(int i, Blob x) throws VirtuosoException
   {
      if(i < 1 || i > parameters.capacity())
         throw new VirtuosoException("Index " + i + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      if(x == null) this.setNull(i, Types.BLOB);
      else objparams.setElementAt(x,i - 1);
   }
   public void setCharacterStream(int parameterIndex, Reader x, int length) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      if (parameters != null && parameters.elementAt(parameterIndex - 1) instanceof openlink.util.Vector)
 {
   openlink.util.Vector pd = (openlink.util.Vector)parameters.elementAt(parameterIndex - 1);
   int dtp = ((Number)pd.elementAt (0)).intValue();
   if (dtp != VirtuosoTypes.DV_BLOB &&
       dtp != VirtuosoTypes.DV_BLOB_BIN &&
       dtp != VirtuosoTypes.DV_BLOB_WIDE)
     {
       try
  {
    StringBuffer buf = new StringBuffer();
    char chars[] = new char [4096];
    int read;
    int total_read = 0;
    int to_read;
    String ret;
    do
      {
        to_read = (length - total_read) > chars.length ? chars.length : (length - total_read);
        read = x.read (chars, 0, to_read);
        if (read > 0)
   {
     buf.append (chars, 0, read);
     total_read += read;
   }
      }
    while (read > 0 && total_read < length);
    ret = buf.toString();
    if (connection.charset != null)
      {
        objparams.setElementAt (
     new VirtuosoExplicitString (connection.charsetBytes (ret),
       VirtuosoTypes.DV_STRING),
     parameterIndex - 1);
      }
    else
      objparams.setElementAt (ret, parameterIndex - 1);
    return;
  }
       catch (java.io.IOException e)
  {
    throw new VirtuosoException ("Error reading from a character stream " + e.getMessage(),
        VirtuosoException.IOERROR);
  }
     }
   if (dtp == VirtuosoTypes.DV_BLOB_BIN)
     throw new VirtuosoException ("Passing character stream to LONG VARBINARY columns not supported",
  "IM001", VirtuosoException.NOTIMPLEMENTED);
 }
      Object _obj = objparams.elementAt(parameterIndex - 1);
      if(_obj instanceof VirtuosoBlob)
      {
         ((VirtuosoBlob)_obj).setReader(x,length);
         return;
      }
      if(x == null) this.setNull(parameterIndex, Types.BLOB);
      else objparams.setElementAt(new VirtuosoBlob(x,length,parameterIndex - 1),parameterIndex - 1);
   }
   public void setClob(int i, Clob x) throws VirtuosoException
   {
      if(i < 1 || i > parameters.capacity())
         throw new VirtuosoException("Index " + i + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      if(x == null) this.setNull(i, Types.CLOB);
      else objparams.setElementAt(x,i - 1);
   }
   public void setNull(int paramIndex, int sqlType, String typeName) throws VirtuosoException
   {
      setNull(paramIndex,sqlType);
   }
   public void setRef(int i, Ref x) throws VirtuosoException
   {
      if(i < 1 || i > parameters.capacity())
         throw new VirtuosoException("Index " + i + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      if(x == null) this.setNull(i, Types.REF);
      else objparams.setElementAt(x,i - 1);
   }
   public void setDate(int parameterIndex, java.sql.Date x, Calendar cal) throws VirtuosoException
     {
       if(parameterIndex < 1 || parameterIndex > parameters.capacity())
  throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
       if(x == null)
  this.setNull(parameterIndex, Types.DATE);
       else
  {
    if(cal != null)
      {
        cal.setTime((java.util.Date)x);
        x = new java.sql.Date (cal.getTime().getTime());
      }
    objparams.setElementAt(x,parameterIndex - 1);
  }
     }
   public void setTime(int parameterIndex, java.sql.Time x, Calendar cal) throws VirtuosoException
     {
       if(parameterIndex < 1 || parameterIndex > parameters.capacity())
  throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
       if(x == null)
  this.setNull(parameterIndex, Types.TIME);
       else
  {
    if(cal != null)
      {
        cal.setTime((java.util.Date)x);
        x = new java.sql.Time (cal.getTime().getTime());
      }
    objparams.setElementAt(x,parameterIndex - 1);
  }
     }
   public void setTimestamp(int parameterIndex, java.sql.Timestamp x, Calendar cal) throws VirtuosoException
     {
       if(parameterIndex < 1 || parameterIndex > parameters.capacity())
  throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
       if(x == null)
  this.setNull(parameterIndex, Types.TIMESTAMP);
       else
  {
    if(cal != null)
      {
        int nanos = x.getNanos();
        cal.setTime((java.util.Date)x);
        x = new java.sql.Timestamp (cal.getTime().getTime());
               x.setNanos (nanos);
      }
    objparams.setElementAt(x,parameterIndex - 1);
  }
     }
   public void setURL(int parameterIndex, java.net.URL x) throws SQLException
     {
       throw new VirtuosoException ("DATALINK not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public ParameterMetaData getParameterMetaData() throws SQLException
     {
       return paramsMetaData == null ? new VirtuosoParameterMetaData(null, connection) : paramsMetaData;
     }
  public void setRowId(int parameterIndex, RowId x) throws SQLException
  {
    throw new VirtuosoFNSException ("Method  setRowId(parameterIndex, x)  isn't supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public synchronized void setNString(int parameterIndex, String value) throws SQLException
  {
    setString(parameterIndex, value);
  }
  public synchronized void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException
  {
    setCharacterStream(parameterIndex, value, length);
  }
  public synchronized void setNClob(int parameterIndex, NClob value) throws SQLException
  {
    if (value == null) {
      setNull(parameterIndex, java.sql.Types.NCLOB);
    } else {
      setNCharacterStream(parameterIndex, value.getCharacterStream(), value.length());
    }
  }
  public void setClob(int parameterIndex, Reader reader, long length)
       throws SQLException
  {
    setCharacterStream(parameterIndex, reader, length);
  }
  public void setBlob(int parameterIndex, InputStream inputStream, long length)
        throws SQLException
  {
    setBinaryStream(parameterIndex, inputStream, (int)length);
  }
  public synchronized void setNClob(int parameterIndex, Reader reader, long length)
       throws SQLException
  {
    if (reader == null) {
      setNull(parameterIndex, java.sql.Types.LONGVARCHAR);
    } else {
      setNCharacterStream(parameterIndex, reader, length);
    }
  }
  public synchronized void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException
  {
    throw new VirtuosoFNSException ("Method  setSQLXML(parameterIndex, xmlObject)  isn't supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public void setAsciiStream(int parameterIndex, java.io.InputStream x, long length)
     throws SQLException
  {
    setAsciiStream(parameterIndex, x, (int)length);
  }
  public void setBinaryStream(int parameterIndex, java.io.InputStream x,
    long length) throws SQLException
  {
    setBinaryStream(parameterIndex, x, (int)length);
  }
  public void setCharacterStream(int parameterIndex, java.io.Reader reader,
     long length) throws SQLException
  {
    setCharacterStream(parameterIndex, reader, (int)length);
  }
  public void setAsciiStream(int parameterIndex, java.io.InputStream x)
     throws SQLException
  {
    throw new VirtuosoFNSException ("Method  setAsciiStream(parameterIndex, x)  isn't supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public void setBinaryStream(int parameterIndex, java.io.InputStream x)
    throws SQLException
  {
    throw new VirtuosoFNSException ("Method  setAsciiStream(parameterIndex, x)  isn't supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public void setCharacterStream(int parameterIndex,
            java.io.Reader reader) throws SQLException
  {
    throw new VirtuosoFNSException ("Method  setCharacterStream(parameterIndex, reader)  isn't supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException
  {
    throw new VirtuosoFNSException ("Method  setNCharacterStream(parameterIndex, value)  isn't supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public void setClob(int parameterIndex, Reader reader)
       throws SQLException
  {
    throw new VirtuosoFNSException ("Method  setClob(parameterIndex, reader)  isn't supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public void setBlob(int parameterIndex, InputStream inputStream)
        throws SQLException
  {
    throw new VirtuosoFNSException ("Method  setBlob(parameterIndex, inputStream)  isn't supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public void setNClob(int parameterIndex, Reader reader)
       throws SQLException
  {
    throw new VirtuosoFNSException ("Method  setNClob(parameterIndex, reader)  isn't supported", VirtuosoException.NOTIMPLEMENTED);
  }
  protected synchronized void setClosed(boolean flag)
  {
    close_flag = flag;
  }
}
