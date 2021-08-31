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
   protected int prefetch = VirtuosoTypes.DEFAULTPREFETCH;
   private int maxRows;
   protected int txn_timeout;
   protected int rpc_timeout;
   protected String statid, cursorName;
   protected volatile boolean close_flag = false;
   protected boolean wait_result = false;
   protected boolean result_opened = false;
   protected boolean sparql_executed = false;
   protected static int req_no;
   protected volatile VirtuosoResultSet vresultSet;
   protected VirtuosoFuture future;
   protected VirtuosoResultSetMetaData metaData;
   protected boolean isCached = false;
   protected boolean closeOnCompletion = false;
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
      if(concurrency == VirtuosoResultSet.CONCUR_READ_ONLY || concurrency == VirtuosoResultSet.CONCUR_UPDATABLE || concurrency == VirtuosoResultSet.CONCUR_VALUES)
         this.concurrency = concurrency;
      else
         throw new VirtuosoException("Bad parameters.",VirtuosoException.BADPARAM);
      this.rpc_timeout = connection.timeout;
      this.txn_timeout = connection.txn_timeout;
      this.prefetch = connection.fbs;
   }
   protected VectorOfLong getStmtOpts () throws VirtuosoException
     {
       Long[] arrLong = new Long [11];
       if (connection.isReadOnly ()) {
         arrLong[0] = Long.valueOf (VirtuosoTypes.SQL_CONCUR_ROWVER);
       }
       else {
         if (concurrency == VirtuosoResultSet.CONCUR_VALUES)
           arrLong[0] = Long.valueOf(VirtuosoTypes.SQL_CONCUR_VALUES);
       else
  arrLong[0] = Long.valueOf(concurrency == VirtuosoResultSet.CONCUR_READ_ONLY ?
  VirtuosoTypes.SQL_CONCUR_READ_ONLY : VirtuosoTypes.SQL_CONCUR_LOCK);
       }
       arrLong[1] = Long.valueOf(0);
       arrLong[2] = Long.valueOf(maxRows);
       if (connection.getGlobalTransaction()) {
           VirtuosoXAConnection xac = (VirtuosoXAConnection) connection.xa_connection;
    if (VirtuosoFuture.rpc_log != null)
    {
     VirtuosoFuture.rpc_log.println ("VirtuosoStatement.getStmtOpts () xa_res=" + xac.getVirtuosoXAResource().hashCode() + " :" + hashCode());
    }
           arrLong[3] = Long.valueOf(xac.getVirtuosoXAResource().txn_timeout * 1000);
       } else {
           arrLong[3] = Long.valueOf(txn_timeout * 1000);
       }
     if (VirtuosoFuture.rpc_log != null)
       {
      VirtuosoFuture.rpc_log.println ("VirtuosoStatement.getStmtOpts (txn_timeout=" + arrLong[3] + ") (con=" + connection.hashCode() + ") :" + hashCode());
       }
       arrLong[4] = Long.valueOf(prefetch);
       arrLong[5] = Long.valueOf((connection.getAutoCommit()) ? 1 : 0);
       arrLong[6] = Long.valueOf (rpc_timeout);
       switch(type)
  {
    case VirtuosoResultSet.TYPE_FORWARD_ONLY:
        arrLong[7] = Long.valueOf(VirtuosoTypes.SQL_CURSOR_FORWARD_ONLY);
        break;
    case VirtuosoResultSet.TYPE_SCROLL_SENSITIVE:
        arrLong[7] = Long.valueOf(VirtuosoTypes.SQL_CURSOR_DYNAMIC);
        break;
    case VirtuosoResultSet.TYPE_SCROLL_INSENSITIVE:
        arrLong[7] = Long.valueOf(VirtuosoTypes.SQL_CURSOR_STATIC);
        break;
  }
       ;
       arrLong[8] = Long.valueOf(0);
       arrLong[9] = Long.valueOf(1);
       arrLong[10] = Long.valueOf(connection.getTransactionIsolation());
       return new VectorOfLong(arrLong);
     }
   protected VirtuosoResultSet sendQuery(String sql) throws VirtuosoException
   {
       sparql_executed = sql.trim().regionMatches(true, 0, "sparql", 0, 6);
       try
       {
    if (close_flag)
               throw new VirtuosoException("Statement is already closed",VirtuosoException.CLOSED);
    synchronized (connection)
    {
        if (future != null)
        {
     close();
     close_flag = false;
        }
        else
     cancel_rs();
        Object[] args = new Object[6];
        openlink.util.Vector vect = new openlink.util.Vector(1);
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
     result_opened = true;
     return new VirtuosoResultSet(this,metaData,false);
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
   }
   public void cancel() throws VirtuosoException
   {
     synchronized (this)
     {
       if (future != null && wait_result == true)
         future.sendCancelFuture();
     }
     cancel_rs();
   }
   protected void cancel_rs() throws VirtuosoException
   {
     if(vresultSet != null)
     {
       vresultSet = null;
     }
     if(future != null)
       {
         synchronized (connection)
         {
    connection.removeFuture(future);
  }
  future = null;
       }
   }
   public void close_rs(boolean close_stmt, boolean is_prepared) throws VirtuosoException
   {
     if(close_flag)
       return;
     if(statid == null)
       return;
     if(vresultSet != null)
       vresultSet = null;
     if (!close_stmt && !result_opened)
        return;
     synchronized (connection)
       {
  try
    {
      if(close_flag)
        return;
      Object[] args = new Object[2];
      args[0] = statid;
      args[1] = close_stmt ? Long.valueOf(VirtuosoTypes.STAT_DROP): Long.valueOf(VirtuosoTypes.STAT_CLOSE);
      future = connection.getFuture(VirtuosoFuture.close,args, this.rpc_timeout);
      future.nextResult();
      connection.removeFuture(future);
             if (close_stmt)
               close_flag = true;
      future = null;
      result_opened = false;
      if (!is_prepared)
        metaData = null;
    }
  catch(IOException e)
    {
      throw new VirtuosoException("Problem during closing : " + e.getMessage(),VirtuosoException.IOERROR);
    }
       }
   }
   public void clearWarnings() throws VirtuosoException
   {
   }
   public void close() throws VirtuosoException
   {
     if(close_flag)
       return;
     connection.removeStmtFromClose(this);
     close_rs(true, false);
   }
   public boolean execute(String sql) throws VirtuosoException
   {
      exec_type = VirtuosoTypes.QT_UNKNOWN;
      VirtuosoResultSet rs = vresultSet = sendQuery(sql);
      return (rs.kindop() != VirtuosoTypes.QT_UPDATE);
   }
   public ResultSet executeQuery(String sql) throws VirtuosoException
   {
      exec_type = VirtuosoTypes.QT_SELECT;
      VirtuosoResultSet rs = vresultSet = sendQuery(sql);
      return rs;
   }
   public int executeUpdate(String sql) throws VirtuosoException
   {
      exec_type = VirtuosoTypes.QT_UPDATE;
      VirtuosoResultSet rs = vresultSet = sendQuery(sql);
      return rs.getUpdateCount();
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
     if(vresultSet == null || vresultSet.isLastResult) {
         if (vresultSet.isLastResult)
           vresultSet = null;
         return false;
     }
     Object[] args = new Object[2];
     args[0] = statid;
     args[1] = Long.valueOf(future.hashCode());
     future.send_message(VirtuosoFuture.fetch,args);
     vresultSet.getMoreResults(false);
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
      return rpc_timeout/1000;
   }
   public ResultSet getResultSet() throws VirtuosoException
   {
      return (vresultSet!=null && vresultSet.kindop() != VirtuosoTypes.QT_UPDATE)?vresultSet:null;
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
      rpc_timeout = seconds*1000;
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
      prefetch = (rows == 0 ? VirtuosoTypes.DEFAULTPREFETCH : rows);
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
  public void closeOnCompletion() throws SQLException
  {
    synchronized (this) {
      closeOnCompletion = true;
    }
  }
  public boolean isCloseOnCompletion() throws SQLException
  {
    synchronized (this) {
      return closeOnCompletion;
    }
  }
}
