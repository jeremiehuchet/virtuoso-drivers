package virtuoso.jdbc4;
import java.sql.*;
import java.util.*;
import java.io.*;
import openlink.util.*;
public class VirtuosoStatement implements Statement
{
   protected openlink.util.Vector parameters, objparams;
   protected LinkedList<Object> batch;
   private int concurrency;
   protected int type;
   protected int exec_type = VirtuosoTypes.QT_UNKNOWN;
   private int fetchDirection = VirtuosoResultSet.FETCH_FORWARD;
   protected VirtuosoConnection connection;
   private int maxFieldSize;
   private int prefetch = VirtuosoTypes.DEFAULTPREFETCH;
   private int maxRows;
   protected int txn_timeout;
   protected int rpc_timeout;
   protected String statid, cursorName;
   protected boolean close_flag = false;
   protected static int req_no;
   protected VirtuosoResultSet vresultSet;
   protected VirtuosoFuture future;
   protected VirtuosoResultSetMetaData metaData;
   protected boolean isCached = false;
   protected VirtuosoParameterMetaData paramsMetaData = null;
   VirtuosoStatement(VirtuosoConnection connection) throws VirtuosoException
   {
      this.connection = connection;
      this.type = VirtuosoResultSet.TYPE_FORWARD_ONLY;
      this.concurrency = VirtuosoResultSet.CONCUR_READ_ONLY;
      this.rpc_timeout = connection.timeout;
      this.txn_timeout = connection.txn_timeout;
      this.prefetch = connection.fbs;
   }
   VirtuosoStatement(VirtuosoConnection connection, int type, int concurrency) throws VirtuosoException
   {
      this.connection = connection;
      if(type == VirtuosoResultSet.TYPE_FORWARD_ONLY || type == VirtuosoResultSet.TYPE_SCROLL_SENSITIVE || type == VirtuosoResultSet.TYPE_SCROLL_INSENSITIVE)
         this.type = type;
      else
         throw new VirtuosoException("Bad parameters.",VirtuosoException.BADPARAM);
      if(concurrency == VirtuosoResultSet.CONCUR_READ_ONLY || concurrency == VirtuosoResultSet.CONCUR_UPDATABLE)
         this.concurrency = concurrency;
      else
         throw new VirtuosoException("Bad parameters.",VirtuosoException.BADPARAM);
      this.rpc_timeout = connection.timeout;
      this.txn_timeout = connection.txn_timeout;
      this.prefetch = connection.fbs;
   }
   protected VectorOfLong getStmtOpts () throws VirtuosoException
     {
       Long[] arrLong = new Long[11];
       if (connection.isReadOnly ())
         arrLong[0] = new Long (VirtuosoTypes.SQL_CONCUR_ROWVER);
       else
  arrLong[0] = new Long(concurrency == VirtuosoResultSet.CONCUR_READ_ONLY ?
  VirtuosoTypes.SQL_CONCUR_READ_ONLY : VirtuosoTypes.SQL_CONCUR_LOCK);
       arrLong[1] = new Long(0);
       arrLong[2] = new Long(maxRows);
       if (connection.getGlobalTransaction()) {
           VirtuosoXAConnection xac = (VirtuosoXAConnection) connection.xa_connection;
    if (VirtuosoFuture.rpc_log != null)
    {
        synchronized (VirtuosoFuture.rpc_log)
        {
     VirtuosoFuture.rpc_log.println ("VirtuosoStatement.getStmtOpts () xa_res=" + xac.getVirtuosoXAResource().hashCode() + " :" + hashCode());
     VirtuosoFuture.rpc_log.flush();
        }
    }
           arrLong[3] = new Long(xac.getVirtuosoXAResource().txn_timeout * 1000);
       } else {
           arrLong[3] = new Long(txn_timeout * 1000);
       }
     if (VirtuosoFuture.rpc_log != null)
       {
  synchronized (VirtuosoFuture.rpc_log)
    {
      VirtuosoFuture.rpc_log.println ("VirtuosoStatement.getStmtOpts (txn_timeout=" + arrLong[3] + ") (con=" + connection.hashCode() + ") :" + hashCode());
      VirtuosoFuture.rpc_log.flush();
    }
       }
       arrLong[4] = new Long(prefetch);
       arrLong[5] = new Long((connection.getAutoCommit()) ? 1 : 0);
       arrLong[6] = new Long (rpc_timeout * 1000);
       switch(type)
  {
    case VirtuosoResultSet.TYPE_FORWARD_ONLY:
        arrLong[7] = new Long(VirtuosoTypes.SQL_CURSOR_FORWARD_ONLY);
        break;
    case VirtuosoResultSet.TYPE_SCROLL_SENSITIVE:
        arrLong[7] = new Long(VirtuosoTypes.SQL_CURSOR_DYNAMIC);
        break;
    case VirtuosoResultSet.TYPE_SCROLL_INSENSITIVE:
        arrLong[7] = new Long(VirtuosoTypes.SQL_CURSOR_STATIC);
        break;
  }
       ;
       arrLong[8] = new Long(0);
       arrLong[9] = new Long(1);
       arrLong[10] = new Long(connection.getTransactionIsolation());
       return new VectorOfLong(arrLong);
     }
   protected VirtuosoResultSet sendQuery(String sql) throws VirtuosoException
   {
       try
       {
    synchronized (connection)
    {
        if (close_flag)
     throw new VirtuosoException("Statement is already closed",VirtuosoException.CLOSED);
        Object[] args = new Object[6];
        openlink.util.Vector vect = new openlink.util.Vector(1);
        if (future != null)
        {
     close();
     close_flag = false;
        }
        else
     cancel();
        args[0] = (statid == null) ? statid = new String("s" + connection.hashCode() + (req_no++)) : statid;
        args[2] = (cursorName == null) ? args[0] : cursorName;
        args[1] = connection.escapeSQL (sql);
        args[3] = vect;
        args[4] = null;
        try
        {
     vect.addElement(new openlink.util.Vector(0));
     args[5] = getStmtOpts();
     future = connection.getFuture(VirtuosoFuture.exec,args, this.rpc_timeout);
     return new VirtuosoResultSet(this,metaData);
        }
        catch(IOException e)
        {
     throw new VirtuosoException("Problem during serialization : " + e.getMessage(),VirtuosoException.IOERROR);
        }
    }
       }
       catch (Throwable e)
       {
    notify_error (e);
    return null;
       }
   }
   public void finalize() throws Throwable
   {
      close();
      if(metaData != null)
         metaData.close();
   }
   public void cancel() throws VirtuosoException
   {
     synchronized (connection)
       {
  if(vresultSet != null)
    {
      vresultSet = null;
    }
  if(future != null)
    {
      connection.removeFuture(future);
      future = null;
    }
       }
   }
   public void clearWarnings() throws VirtuosoException
   {
   }
   public void close() throws VirtuosoException
   {
     synchronized (connection)
       {
  try
    {
      if(close_flag)
        return;
             close_flag = true;
      if(statid == null)
        return;
      cancel();
      Object[] args = new Object[2];
      args[0] = statid;
      args[1] = new Long(VirtuosoTypes.STAT_DROP);
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
   public boolean execute(String sql) throws VirtuosoException
   {
      exec_type = VirtuosoTypes.QT_UNKNOWN;
      vresultSet = sendQuery(sql);
      return (vresultSet.kindop() != VirtuosoTypes.QT_UPDATE);
   }
   public ResultSet executeQuery(String sql) throws VirtuosoException
   {
      exec_type = VirtuosoTypes.QT_SELECT;
      vresultSet = sendQuery(sql);
      return vresultSet;
   }
   public int executeUpdate(String sql) throws VirtuosoException
   {
      exec_type = VirtuosoTypes.QT_UPDATE;
      vresultSet = sendQuery(sql);
      return vresultSet.getUpdateCount();
   }
   public int getMaxFieldSize() throws VirtuosoException
   {
      return maxFieldSize;
   }
   public int getMaxRows() throws VirtuosoException
   {
      return maxRows;
   }
   public boolean getMoreResults() throws VirtuosoException
   {
       try
       {
    synchronized (connection)
    {
        try
        {
     if(vresultSet == null || vresultSet.isLastResult)
         return false;
     Object[] args = new Object[2];
     args[0] = statid;
     args[1] = new Long(future.hashCode());
     future.send_message(VirtuosoFuture.fetch,args);
     vresultSet.getMoreResults();
     return true;
        }
        catch(IOException e)
        {
     throw new VirtuosoException("Problem during serialization : " + e.getMessage(),VirtuosoException.IOERROR);
        }
    }
       } catch (Throwable e) {
    notify_error (e);
    return false;
       }
   }
   public int getQueryTimeout() throws VirtuosoException
   {
      return rpc_timeout;
   }
   public ResultSet getResultSet() throws VirtuosoException
   {
      return (vresultSet.kindop() != VirtuosoTypes.QT_UPDATE)?vresultSet:null;
   }
   public int getUpdateCount() throws VirtuosoException
   {
      if(vresultSet != null)
         switch(vresultSet.kindop())
         {
            case VirtuosoTypes.QT_UPDATE:
     case VirtuosoTypes.QT_PROC_CALL:
               return vresultSet.getUpdateCount();
            default:
               return -1;
         }
      ;
      return -1;
   }
   public SQLWarning getWarnings() throws VirtuosoException
   {
      return null;
   }
   public void setMaxFieldSize(int max) throws VirtuosoException
   {
      if(max < 0)
         throw new VirtuosoException("Bad parameters.",VirtuosoException.BADPARAM);
      maxFieldSize = max;
   }
   public void setMaxRows(int max) throws VirtuosoException
   {
      if(max < 0)
         throw new VirtuosoException("Bad parameters.",VirtuosoException.BADPARAM);
      maxRows = max;
   }
   public void setQueryTimeout(int seconds) throws VirtuosoException
   {
      if(seconds < 0)
         throw new VirtuosoException("Bad parameters.",VirtuosoException.BADPARAM);
      rpc_timeout = seconds;
   }
    public Connection getConnection() throws VirtuosoException {
 return connection;
    }
   public int getFetchDirection() throws VirtuosoException
   {
      return fetchDirection;
   }
   public int getFetchSize() throws VirtuosoException
   {
      return prefetch;
   }
   public int getResultSetConcurrency() throws VirtuosoException
   {
      return concurrency;
   }
   public int getResultSetType() throws VirtuosoException
   {
      return type;
   }
   public void setFetchDirection(int direction) throws VirtuosoException
   {
      if(direction == VirtuosoResultSet.FETCH_FORWARD || direction == VirtuosoResultSet.FETCH_REVERSE || direction == VirtuosoResultSet.FETCH_UNKNOWN)
         fetchDirection = direction;
      else
         throw new VirtuosoException("Bad parameters.",VirtuosoException.BADPARAM);
   }
   public void setFetchSize(int rows) throws VirtuosoException
   {
      if(rows < 0 || (maxRows > 0 && rows > maxRows))
         throw new VirtuosoException("Bad parameters.",VirtuosoException.BADPARAM);
      prefetch = rows;
   }
   public void setCursorName(String name) throws VirtuosoException
   {
      cursorName = name;
   }
   public void addBatch(String sql) throws VirtuosoException
   {
      if(sql == null)
         return;
      if(batch == null)
         batch = new LinkedList<Object>();
      batch.add(sql);
   }
   public void clearBatch() throws VirtuosoException
   {
      if(batch != null)
         batch.clear();
   }
   public int[] executeBatch() throws BatchUpdateException
   {
      if(batch == null)
         return new int[0];
      int[] result = new int[batch.size()];
      int[] outres = null;
      int outcount = 0;
      int i;
      boolean error = false;
      VirtuosoException ex = null;
      i = 0;
      for(ListIterator it = batch.listIterator(); it.hasNext(); )
      {
         try
         {
            String stmt = (String)it.next();
            VirtuosoResultSet rset = sendQuery(stmt);
            result[i] = rset.getUpdateCount();
            if(rset.kindop()==VirtuosoTypes.QT_SELECT)
            {
              error = true;
              break;
            }
         }
         catch(VirtuosoException e)
         {
            error = true;
            result[i] = -3;
            ex = e;
         }
         outcount++;
         i++;
      }
      batch.clear();
      if(error)
      {
         outres = new int[outcount];
         for (i=0; i<outcount; i++)
           outres[i]=result[i];
         if (ex != null)
           throw new BatchUpdateException(ex.getMessage(), ex.getSQLState(), ex.getErrorCode(), outres);
         else
           throw new BatchUpdateException(outres);
      }
      return result;
   }
   public void setEscapeProcessing(boolean enable) throws VirtuosoException
   {
   }
   public int getExecType()
   {
     return exec_type;
   }
   public boolean isClosed ( )
   {
     return close_flag;
   }
   public boolean getMoreResults(int current) throws SQLException
     {
       if (current == KEEP_CURRENT_RESULT)
         throw new VirtuosoException ("Keeping the current result open not supported", "IM001",
           VirtuosoException.NOTIMPLEMENTED);
       return getMoreResults();
     }
   public ResultSet getGeneratedKeys() throws SQLException
     {
       return new VirtuosoResultSet (connection);
     }
   public int executeUpdate(String sql,
       int autoGeneratedKeys) throws SQLException
     {
       return executeUpdate (sql);
     }
   public int executeUpdate(String sql,
       int[] columnIndexes) throws SQLException
     {
       return executeUpdate (sql);
     }
   public int executeUpdate(String sql,
       String[] columnNames) throws SQLException
     {
       return executeUpdate (sql);
     }
   public boolean execute(String sql,
       int autoGeneratedKeys) throws SQLException
     {
       return execute (sql);
     }
   public boolean execute(String sql,
       int[] columnIndexes) throws SQLException
     {
       return execute (sql);
     }
   public boolean execute(String sql,
       String[] columnNames) throws SQLException
     {
       return execute (sql);
     }
   public int getResultSetHoldability() throws SQLException
     {
       return ResultSet.CLOSE_CURSORS_AT_COMMIT;
     }
   protected void notify_error (Throwable e) throws VirtuosoException
   {
       VirtuosoConnection c = connection;
       if (c != null)
    throw c.notify_error (e);
       else
       {
    VirtuosoException ve = new VirtuosoException(e.getMessage(), VirtuosoException.IOERROR);
           ve.initCause (e);
    throw ve;
       }
   }
  private boolean isPoolable = true;
  public void setPoolable(boolean poolable) throws SQLException
  {
    isPoolable = poolable;
  }
  public boolean isPoolable() throws SQLException
  {
    return isPoolable;
  }
  public <T> T unwrap(java.lang.Class<T> iface) throws java.sql.SQLException
  {
    try {
      return iface.cast(this);
    } catch (ClassCastException cce) {
      throw new VirtuosoException ("Unable to unwrap to "+iface.toString(), "22023", VirtuosoException.BADPARAM);
    }
  }
  public boolean isWrapperFor(java.lang.Class<?> iface) throws java.sql.SQLException
  {
    return iface.isInstance(this);
  }
}