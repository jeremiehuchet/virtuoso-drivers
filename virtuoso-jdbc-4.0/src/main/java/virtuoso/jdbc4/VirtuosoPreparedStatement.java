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
   protected VirtuosoResultSet ps_vresultSet;
   VirtuosoPreparedStatement(VirtuosoConnection connection, String sql) throws VirtuosoException
   {
      this (connection,sql,VirtuosoResultSet.TYPE_FORWARD_ONLY,VirtuosoResultSet.CONCUR_READ_ONLY);
   }
   VirtuosoPreparedStatement(VirtuosoConnection connection, String sql, int type, int concurrency) throws VirtuosoException
   {
      super(connection,type,concurrency);
      sparql_executed = sql.trim().regionMatches(true, 0, "sparql", 0, 6);
      synchronized (connection)
 {
   try
     {
       this.sql = sql;
       parse_sql();
       Object[] args = new Object[4];
       args[0] = (statid == null) ? statid = new String("ps" + connection.hashCode() + (req_no++)) : statid;
       args[1] = connection.escapeSQL(sql);
       args[2] = Long.valueOf(0);
       args[3] = getStmtOpts();
       future = connection.getFuture(VirtuosoFuture.prepare,args, this.rpc_timeout);
       ps_vresultSet = vresultSet = new VirtuosoResultSet(this,metaData, true);
       result_opened = true;
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
         if (future != null)
           {
             connection.removeFuture(future);
             future = null;
           }
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
             ps_vresultSet.isLastResult = false;
      ps_vresultSet.getMoreResults(false);
             ps_vresultSet.stmt_n_rows_to_get = this.prefetch;
             vresultSet = ps_vresultSet;
      result_opened = true;
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
             if (future != null)
               {
          connection.removeFuture(future);
          future = null;
               }
      args[5] = getStmtOpts();
      future = connection.getFuture(VirtuosoFuture.exec,args, this.rpc_timeout);
             vresultSet.isLastResult = false;
      for (inx = 0; inx < size; inx++)
      {
   vresultSet.setUpdateCount (0);
   vresultSet.getMoreResults (false);
   res[inx] = SUCCESS_NO_INFO;
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
   public void finalize() throws Throwable
   {
      close();
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
     if(close_flag)
       return;
     connection.removeStmtFromClose(this);
     synchronized (connection)
       {
  try
    {
      close_flag = true;
      if(statid == null)
        return;
      cancel();
      Object[] args = new Object[2];
      args[0] = statid;
      args[1] = Long.valueOf(VirtuosoTypes.STAT_DROP);
      future = connection.getFuture(VirtuosoFuture.close,args, this.rpc_timeout);
      future.nextResult();
      connection.removeFuture(future);
      future = null;
      result_opened = false;
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
      objparams.setElementAt(Boolean.valueOf(x),parameterIndex - 1);
   }
   public void setByte(int parameterIndex, byte x) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      objparams.setElementAt(Byte.valueOf(x),parameterIndex - 1);
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
      objparams.setElementAt(Double.valueOf(x),parameterIndex - 1);
   }
   public void setFloat(int parameterIndex, float x) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      objparams.setElementAt(Float.valueOf(x),parameterIndex - 1);
   }
   public void setInt(int parameterIndex, int x) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      objparams.setElementAt(Integer.valueOf(x),parameterIndex - 1);
   }
   public void setLong(int parameterIndex, long x) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      objparams.setElementAt(Long.valueOf(x),parameterIndex - 1);
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
   public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws VirtuosoException
   {
      if(x == null) {
          this.setNull(parameterIndex, Types.OTHER);
          return;
      }
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
      x = VirtuosoTypes.mapJavaTypeToSqlType (x, targetSqlType, scale);
      if (x instanceof java.io.Serializable)
 {
   if (x instanceof String && parameters != null
        && parameters.elementAt(parameterIndex - 1) instanceof openlink.util.Vector)
     {
       openlink.util.Vector pd = (openlink.util.Vector)parameters.elementAt(parameterIndex - 1);
       int dtp = ((Number)pd.elementAt (0)).intValue();
       VirtuosoExplicitString ret;
       ret = new VirtuosoExplicitString ((String)x, dtp, connection);
       objparams.setElementAt (ret, parameterIndex - 1);
     }
   else
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
      objparams.setElementAt(Short.valueOf(x),parameterIndex - 1);
   }
   public void setString(int parameterIndex, String x) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " +
      parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      if(x == null)
 this.setNull(parameterIndex, Types.VARCHAR);
      else
 {
   if (parameters != null && parameters.elementAt(parameterIndex - 1) instanceof openlink.util.Vector)
     {
       openlink.util.Vector pd = (openlink.util.Vector)parameters.elementAt(parameterIndex - 1);
       int dtp = ((Number)pd.elementAt (0)).intValue();
       VirtuosoExplicitString ret;
       ret = new VirtuosoExplicitString (x, dtp, connection);
       objparams.setElementAt (ret, parameterIndex - 1);
     }
   else
     {
       objparams.setElementAt(x,parameterIndex - 1);
     }
 }
   }
   protected void setString(int parameterIndex, VirtuosoExplicitString x) throws VirtuosoException
   {
     if(parameterIndex < 1 || parameterIndex > parameters.capacity())
       throw new VirtuosoException("Index " +
    parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
     if(x == null)
       this.setNull(parameterIndex, Types.VARCHAR);
     else
        objparams.setElementAt(x, parameterIndex - 1);
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
 throwBatchUpdateException (result, "Batch cannot execute calls with out params", 0);
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
      if(x == null){
          this.setNull(i, Types.ARRAY);
      } else if (x instanceof VirtuosoArray) {
          objparams.setElementAt(((VirtuosoArray)x).data, i - 1);
      } else {
          objparams.setElementAt(x,i - 1);
      }
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
        x = new java.sql.Date (VirtuosoTypes.timeFromCal(x, cal));
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
        x = new java.sql.Time (VirtuosoTypes.timeFromCal(x, cal));
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
        x = new java.sql.Timestamp(VirtuosoTypes.timeFromCal(x, cal));
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
    throw new VirtuosoFNSException ("Method  setRowId(parameterIndex, x)  is not supported", VirtuosoException.NOTIMPLEMENTED);
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
    throw new VirtuosoFNSException ("Method  setSQLXML(parameterIndex, xmlObject)  is not supported", VirtuosoException.NOTIMPLEMENTED);
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
    throw new VirtuosoFNSException ("Method  setAsciiStream(parameterIndex, x)  is not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public void setBinaryStream(int parameterIndex, java.io.InputStream x)
    throws SQLException
  {
    throw new VirtuosoFNSException ("Method  setAsciiStream(parameterIndex, x)  is not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public void setCharacterStream(int parameterIndex,
            java.io.Reader reader) throws SQLException
  {
    throw new VirtuosoFNSException ("Method  setCharacterStream(parameterIndex, reader)  is not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException
  {
    throw new VirtuosoFNSException ("Method  setNCharacterStream(parameterIndex, value)  is not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public void setClob(int parameterIndex, Reader reader)
       throws SQLException
  {
    throw new VirtuosoFNSException ("Method  setClob(parameterIndex, reader)  is not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public void setBlob(int parameterIndex, InputStream inputStream)
        throws SQLException
  {
    throw new VirtuosoFNSException ("Method  setBlob(parameterIndex, inputStream)  is not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public void setNClob(int parameterIndex, Reader reader)
       throws SQLException
  {
    throw new VirtuosoFNSException ("Method  setNClob(parameterIndex, reader)  is not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  protected synchronized void setClosed(boolean flag)
  {
    close_flag = flag;
  }
}
