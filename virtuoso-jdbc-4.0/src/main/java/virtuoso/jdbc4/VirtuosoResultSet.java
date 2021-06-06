package virtuoso.jdbc4;
import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.*;
import openlink.util.*;
public class VirtuosoResultSet implements ResultSet
{
   private static final String er1 = "I/O error on output stream.";
   protected openlink.util.Vector rows = new openlink.util.Vector(20);
   private Object[] row;
   private int concurrency;
   private int fetchDirection;
   private int prefetch;
   private int type;
   private VirtuosoStatement statement;
   private boolean is_prepared;
   protected VirtuosoResultSetMetaData metaData;
   private int maxRows;
   protected int totalRows;
   private boolean is_complete;
   private int updateCount;
   private String cursorName;
   protected int currentRow;
   protected int stmt_current_of;
   protected int stmt_n_rows_to_get;
   protected boolean stmt_co_last_in_batch;
   private int oldRow;
   private boolean wasNull = false, rowIsDeleted = false, rowIsUpdated = false, rowIsInserted = false;
   private boolean more_result;
   protected boolean isLastResult = false;
   protected boolean isLastRow = false;
   private int kindop;
   private VirtuosoPreparedStatement pstmt;
   private int rowNum = 0;
   public static final int TYPE_FORWARD_ONLY = 1003;
   public static final int TYPE_SCROLL_INSENSITIVE = 1004;
   public static final int TYPE_SCROLL_SENSITIVE = 1005;
   public static final int FETCH_FORWARD = 1000;
   public static final int FETCH_REVERSE = 1001;
   public static final int FETCH_UNKNOWN = 1002;
   public static final int CONCUR_READ_ONLY = 1007;
   public static final int CONCUR_UPDATABLE = 1008;
   public static final int CONCUR_VALUES = 1009;
   VirtuosoResultSet(VirtuosoStatement statement, VirtuosoResultSetMetaData metaData, boolean isPrepare) throws VirtuosoException
   {
      this.statement = statement;
      this.metaData = metaData;
      fetchDirection = statement.getFetchDirection();
      concurrency = statement.getResultSetConcurrency();
      type = statement.getResultSetType();
      prefetch = statement.getFetchSize();
      maxRows = statement.getMaxRows();
      cursorName = (statement.cursorName == null) ? statement.statid : statement.cursorName;
      stmt_current_of = -1;
      stmt_n_rows_to_get = prefetch;
      stmt_co_last_in_batch = false;
      is_prepared = isPrepare;
      process_result(isPrepare);
   }
   VirtuosoResultSet(VirtuosoConnection vc) throws VirtuosoException
   {
      this.statement = new VirtuosoStatement (vc);
      this.metaData = new VirtuosoResultSetMetaData (null, vc);
      type = VirtuosoResultSet.TYPE_FORWARD_ONLY;
      is_complete = true;
   }
   protected VirtuosoResultSet(VirtuosoConnection vc, String [] col_names, int [] col_dtps) throws VirtuosoException
   {
      this.statement = new VirtuosoStatement (vc);
      this.metaData = new VirtuosoResultSetMetaData (vc, col_names, col_dtps);
      type = VirtuosoResultSet.TYPE_FORWARD_ONLY;
      is_complete = true;
   }
   protected void getMoreResults(boolean isPrepare) throws VirtuosoException
   {
     synchronized (statement.connection)
       {
  rowIsDeleted = rowIsUpdated = rowIsInserted = is_complete = false;
  currentRow = 0;
  if(rows == null)
    rows = new openlink.util.Vector(20);
  else
    rows.removeAllElements();
  process_result(isPrepare);
  more_result = true;
       }
   }
   protected int getUpdateCount()
   {
      return updateCount;
   }
   protected void setUpdateCount(int n)
   {
      updateCount = n;
   }
   private void fetch_rpc() throws VirtuosoException
   {
      try
      {
 synchronized (statement.connection)
   {
     Object[] args = new Object[2];
     args[0] = statement.statid;
     args[1] = Long.valueOf(statement.future.hashCode());
     statement.connection.removeFuture(statement.connection.getFuture(
    VirtuosoFuture.fetch,args, statement.rpc_timeout));
   }
      }
      catch(IOException e)
      {
         throw new VirtuosoException(e, "Problem during serialization : " + e.getMessage(),VirtuosoException.IOERROR);
      }
   }
   private void extended_fetch(int op, long firstline, long nbline) throws VirtuosoException
   {
      try
      {
 synchronized (statement.connection)
   {
     Object[] args = new Object[6];
     args[0] = statement.statid;
     args[1] = Long.valueOf(op);
     args[2] = Long.valueOf(firstline);
     args[3] = Long.valueOf(nbline);
     args[4] = Long.valueOf((statement.connection.getAutoCommit()) ? 1 : 0);
     args[5] = null;
     statement.future = statement.connection.getFuture(
  VirtuosoFuture.extendedfetch,args, statement.rpc_timeout);
     getMoreResults(false);
     if (statement.connection.getAutoCommit())
       process_result(false);
   }
      }
      catch(IOException e)
      {
         throw new VirtuosoException(e, "Problem during serialization : " + e.getMessage(),VirtuosoException.IOERROR);
      }
   }
   private void set_pos(int op, openlink.util.Vector args, long num) throws VirtuosoException
   {
      if(pstmt == null)
         pstmt = (VirtuosoPreparedStatement)(statement.connection.prepareStatement("__set_pos(?,?,?,?)"));
      pstmt.setString(1,statement.statid);
      pstmt.setLong(2,op);
      pstmt.setLong(3,num);
      if (args!=null) {
         for(int i=0; i < args.size(); i++) {
            int dtp = metaData.getColumnDtp (i+1);
            Object v = args.elementAt(i);
            if (v instanceof String)
               switch (dtp){
                  case VirtuosoTypes.DV_ANY:
                  case VirtuosoTypes.DV_WIDE:
                  case VirtuosoTypes.DV_LONG_WIDE:
                  case VirtuosoTypes.DV_BLOB_WIDE:
                  case VirtuosoTypes.DV_STRING:
                  case VirtuosoTypes.DV_SHORT_STRING_SERIAL:
                  case VirtuosoTypes.DV_STRICT_STRING:
                  case VirtuosoTypes.DV_C_STRING:
                  case VirtuosoTypes.DV_BLOB:
                     args.setElementAt(new VirtuosoExplicitString((String)v, dtp, this.statement.connection), i);
                     break;
               }
         }
      }
      pstmt.setVector(4,args);
      pstmt.execute();
      synchronized (statement.connection)
 {
   switch(op)
     {
       case VirtuosoTypes.SQL_DELETE:
    do
      {
        pstmt.vresultSet.getMoreResults(false);
      }
    while(!pstmt.vresultSet.isLastRow && isLastResult);
    rowIsDeleted = (pstmt.vresultSet.getUpdateCount() > 0);
    break;
       case VirtuosoTypes.SQL_UPDATE:
    pstmt.vresultSet.metaData = metaData;
    pstmt.vresultSet.rows = rows;
    pstmt.vresultSet.totalRows = totalRows;
    pstmt.vresultSet.currentRow = currentRow;
    do
      {
        pstmt.vresultSet.is_complete = pstmt.vresultSet.more_result = false;
        pstmt.vresultSet.process_result(false);
      }
    while(!pstmt.vresultSet.isLastRow && isLastResult);
    rowIsUpdated = (pstmt.vresultSet.getUpdateCount() > 0);
    break;
       case VirtuosoTypes.SQL_ADD:
    do
      {
        pstmt.vresultSet.getMoreResults(false);
      }
    while(!pstmt.vresultSet.isLastRow && isLastResult);
    rowIsInserted = (pstmt.vresultSet.getUpdateCount() > 0);
    break;
       case VirtuosoTypes.SQL_REFRESH:
    pstmt.vresultSet.metaData = metaData;
    pstmt.vresultSet.rows = rows;
    pstmt.vresultSet.totalRows = totalRows;
    pstmt.vresultSet.currentRow = currentRow;
    do
      {
        pstmt.vresultSet.is_complete = pstmt.vresultSet.more_result = false;
        pstmt.vresultSet.process_result(false);
      }
    while(pstmt.vresultSet.more_result());
    totalRows = pstmt.vresultSet.totalRows;
    break;
     }
   ;
   pstmt.vresultSet.rows = null;
 }
   }
   private void process_result(boolean isPrepare) throws VirtuosoException
   {
      Object curr;
      openlink.util.Vector result;
      more_result = false;
      for(int i = 0;!is_complete;)
      {
         if(rows.size() == statement.getMaxRows() && rows.size() > 0)
         {
            is_complete = true;
     continue;
         }
  if (statement == null || statement.future == null)
    throw new VirtuosoException ("Statement closed. Operation not applicable",
        VirtuosoException.MISCERROR);
  synchronized (statement) { statement.wait_result = true; }
  curr = statement.future.nextResult();
  synchronized (statement) { statement.wait_result = false; }
  curr = (curr==null)?null:((openlink.util.Vector)curr).firstElement();
         if(curr instanceof openlink.util.Vector)
         {
            result = (openlink.util.Vector)curr;
            switch(((Short)result.firstElement()).intValue())
            {
               case VirtuosoTypes.QA_LOGIN:
    statement.connection.qualifier = (String)result.elementAt(1);
    break;
               case VirtuosoTypes.QA_ROW_LAST_IN_BATCH:
    if (statement.type != TYPE_FORWARD_ONLY)
      is_complete = true;
    else
      stmt_co_last_in_batch = true;
               case VirtuosoTypes.QA_ROW:
                  isLastRow = false;
                  result.removeElementAt(0);
                  fixReturnedData(result);
                  if(currentRow == 0)
                     rows.insertElementAt(new VirtuosoRow(this,result),i++);
                  else
                     rows.setElementAt(new VirtuosoRow(this,result),currentRow - 1);
    if (statement.type == TYPE_FORWARD_ONLY)
      return;
    else if (i >= ((prefetch == 0) ? VirtuosoTypes.DEFAULTPREFETCH : prefetch))
      return;
                  break;
               case VirtuosoTypes.QA_COMPILED:
                  openlink.util.Vector v = (openlink.util.Vector)result.elementAt(1);
                  Number kop = (Number)v.elementAt(1);
                  if(kop == null)
                  {
       kindop = VirtuosoTypes.QT_UPDATE;
                     if (statement != null)
                     {
                        if (statement.getExecType() == VirtuosoTypes.QT_SELECT)
                          throw new VirtuosoException("executeUpdate cannot execute update/insert/delete queries",VirtuosoException.BADPARAM);
                     }
                     if(!(statement instanceof VirtuosoPreparedStatement))
                     {
                        statement.metaData = metaData =
       new VirtuosoResultSetMetaData(null,statement.connection);
                        break;
                     }
                  }
                  else
                  {
                     kindop = kop.intValue();
                     if (statement != null)
                     {
                        if (statement.getExecType() == VirtuosoTypes.QT_UPDATE && kindop == VirtuosoTypes.QT_SELECT)
                          throw new VirtuosoException("executeUpdate can execute only update/insert/delete queries",VirtuosoException.BADPARAM);
                        if (statement.getExecType() == VirtuosoTypes.QT_SELECT && kindop == VirtuosoTypes.QT_UPDATE)
                          throw new VirtuosoException("executeUpdate cannot execute update/insert/delete queries",VirtuosoException.BADPARAM);
                     }
                  }
                  if(metaData != null)
                     metaData.close();
                  statement.metaData = metaData =
        new VirtuosoResultSetMetaData(v,statement.connection);
                  if(statement instanceof PreparedStatement && isPrepare)
                  {
                     Object obj = v.elementAt(3);
                     statement.objparams = null;
       statement.paramsMetaData = null;
                     if(obj!=null && obj instanceof openlink.util.Vector)
         {
           fixReturnedData((openlink.util.Vector)obj);
                         statement.objparams = (openlink.util.Vector)obj;
    statement.parameters = (openlink.util.Vector)statement.objparams.clone();
                         if (statement instanceof CallableStatement)
                           {
                             VirtuosoCallableStatement _statement = (VirtuosoCallableStatement)statement;
        _statement.param_type = new int[statement.parameters.capacity()];
        _statement.param_scale = new int[statement.parameters.capacity()];
        for (int _i = 0; _i < _statement.param_type.length; _i++)
          {
     _statement.param_type[_i] = Types.OTHER;
     _statement.param_scale[_i] = 0;
          }
      }
    is_complete = true;
    statement.paramsMetaData =
                          new VirtuosoParameterMetaData ((openlink.util.Vector)obj, statement.connection);
         }
       else
         {
           statement.parameters = null;
         }
                  }
                  break;
               case VirtuosoTypes.QA_ROWS_AFFECTED:
                  if(type != VirtuosoResultSet.TYPE_FORWARD_ONLY)
                  {
                     totalRows = ((Number)result.elementAt(1)).intValue();
                     updateCount = 0;
                     is_complete = true;
                  }
                  else
                  {
                     updateCount = ((Number)result.elementAt(1)).intValue();
                     is_complete = true;
                  }
                  isLastRow = true;
                  if (kindop != VirtuosoTypes.QT_PROC_CALL)
                    isLastResult = true;
                  break;
               case VirtuosoTypes.QA_ROW_DELETED:
                  result.removeElementAt(0);
                  rows.removeElementAt(currentRow - 1);
                  totalRows--;
                  break;
               case VirtuosoTypes.QA_ROW_UPDATED:
                  result.removeElementAt(0);
                  result.removeElementAt(result.size() - 1);
                  fixReturnedData(result);
                  rows.setElementAt(new VirtuosoRow(this,result),currentRow - 1);
                  break;
               case VirtuosoTypes.QA_ERROR:
    if (VirtuosoFuture.rpc_log != null)
    {
     VirtuosoFuture.rpc_log.println ("---> QA_ERROR err=[" + (String)result.elementAt(2) + "] stat=[" + (String)result.elementAt(1) + "]");
    }
                  isLastResult = true;
                  isLastRow = true;
                  throw new VirtuosoException((String)result.elementAt(2),(String)result.elementAt(1),VirtuosoException.SQLERROR);
               case VirtuosoTypes.QA_WARNING:
                  statement.connection.setWarning (
     new SQLWarning(
         (String)result.elementAt(2),
         (String)result.elementAt(1),
         VirtuosoException.SQLERROR));
    break;
               case VirtuosoTypes.QA_PROC_RETURN:
                  if (statement.objparams == null)
        statement.objparams = new openlink.util.Vector(result.size() - 2);
    for(int j = 0; j < statement.objparams.size() && (j+2) < result.size(); j++)
    {
       Object val = result.elementAt(j+2);
       if (val instanceof DateObject)
         statement.objparams.setElementAt(((DateObject)val).getValue(statement.sparql_executed),j);
       else
         statement.objparams.setElementAt(val,j);
    }
                  is_complete = true;
                  isLastResult = true;
                  isLastRow = true;
    break;
               case VirtuosoTypes.QA_NEED_DATA:
    sendBlobData(result);
    break;
               default:
                  throw new VirtuosoException(curr.toString(),VirtuosoException.UNKNOWN);
            }
            ;
         }
         else
         {
            isLastRow = true;
            if (kindop != VirtuosoTypes.QT_PROC_CALL)
               isLastResult = true;
            if(curr == null)
            {
               more_result = true;
               is_complete = true;
               continue;
            }
            if(((Short)curr).shortValue() == VirtuosoTypes.QC_STATUS ||
  ((Short)curr).shortValue() == 100)
            {
               is_complete = true;
     }
         }
      }
   }
   public void finalize() throws Throwable
   {
      close();
   }
   void fixReturnedData(openlink.util.Vector data)
   {
     if (data == null)
       return;
     for(int i=0; i < data.size(); i++)
     {
       Object val = data.elementAt(i);
       if (val instanceof DateObject)
         data.setElementAt(((DateObject)val).getValue(statement.sparql_executed), i);
     }
   }
   public SQLWarning getWarnings() throws VirtuosoException
   {
      return null;
   }
   public void clearWarnings() throws VirtuosoException
   {
   }
   public int getType() throws VirtuosoException
   {
      return type;
   }
   public int getConcurrency() throws VirtuosoException
   {
      return concurrency;
   }
   public void setFetchDirection(int direction) throws VirtuosoException
   {
      if(direction == VirtuosoResultSet.FETCH_FORWARD || direction == VirtuosoResultSet.FETCH_REVERSE || direction == VirtuosoResultSet.FETCH_UNKNOWN)
         fetchDirection = direction;
      else
         throw new VirtuosoException("Bad parameters.",VirtuosoException.BADPARAM);
   }
   public int getFetchDirection() throws VirtuosoException
   {
      return fetchDirection;
   }
   public void setFetchSize(int rows) throws VirtuosoException
   {
      if(rows < 0 || rows > statement.getMaxRows())
         throw new VirtuosoException("Bad parameters.",VirtuosoException.BADPARAM);
      prefetch = (rows == 0 ? VirtuosoTypes.DEFAULTPREFETCH : rows);
   }
   public int getFetchSize() throws VirtuosoException
   {
      return prefetch;
   }
   public Statement getStatement() throws VirtuosoException
   {
      return statement;
   }
   public ResultSetMetaData getMetaData() throws VirtuosoException
   {
      return metaData;
   }
   protected int kindop() throws VirtuosoException
   {
      return kindop;
   }
   protected boolean more_result() throws VirtuosoException
   {
      return more_result;
   }
   public int findColumn(String name) throws VirtuosoException
   {
      if(name == null)
         throw new VirtuosoException("Bad parameters.",VirtuosoException.BADPARAM);
      Integer i = (Integer)(metaData.hcolumns.get(
     new VirtuosoColumn(name, VirtuosoTypes.DV_STRING, statement.connection)));
      if (i == null)
         throw new VirtuosoException("findColumn() cannot found column with name '"+name+"' in resultSet", "S0022", VirtuosoException.MISCERROR);
      return i.intValue() + 1;
   }
   public boolean wasNull() throws VirtuosoException
   {
      return wasNull;
   }
   public String getString(int columnIndex) throws VirtuosoException
   {
      if(currentRow < 1 || currentRow > rows.size())
         throw new VirtuosoException("Bad current row selected : " + currentRow + " not in 1<n<" + rows.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoRow)rows.elementAt(currentRow - 1)).getString(columnIndex);
   }
   public boolean getBoolean(int columnIndex) throws VirtuosoException
   {
      if(currentRow < 1 || currentRow > rows.size())
         throw new VirtuosoException("Bad current row selected : " + currentRow + " not in 1<n<" + rows.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoRow)rows.elementAt(currentRow - 1)).getBoolean(columnIndex);
   }
   public byte getByte(int columnIndex) throws VirtuosoException
   {
      if(currentRow < 1 || currentRow > rows.size())
         throw new VirtuosoException("Bad current row selected : " + currentRow + " not in 1<n<" + rows.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoRow)rows.elementAt(currentRow - 1)).getByte(columnIndex);
   }
   public short getShort(int columnIndex) throws VirtuosoException
   {
      if(currentRow < 1 || currentRow > rows.size())
         throw new VirtuosoException("Bad current row selected : " + currentRow + " not in 1<n<" + rows.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoRow)rows.elementAt(currentRow - 1)).getShort(columnIndex);
   }
   public int getInt(int columnIndex) throws VirtuosoException
   {
      if(currentRow < 1 || currentRow > rows.size())
         throw new VirtuosoException("Bad current row selected : " + currentRow + " not in 1<n<" + rows.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoRow)rows.elementAt(currentRow - 1)).getInt(columnIndex);
   }
   public long getLong(int columnIndex) throws VirtuosoException
   {
      if(currentRow < 1 || currentRow > rows.size())
         throw new VirtuosoException("Bad current row selected : " + currentRow + " not in 1<n<" + rows.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoRow)rows.elementAt(currentRow - 1)).getLong(columnIndex);
   }
   public float getFloat(int columnIndex) throws VirtuosoException
   {
      if(currentRow < 1 || currentRow > rows.size())
         throw new VirtuosoException("Bad current row selected : " + currentRow + " not in 1<n<" + rows.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoRow)rows.elementAt(currentRow - 1)).getFloat(columnIndex);
   }
   public double getDouble(int columnIndex) throws VirtuosoException
   {
      if(currentRow < 1 || currentRow > rows.size())
         throw new VirtuosoException("Bad current row selected : " + currentRow + " not in 1<n<" + rows.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoRow)rows.elementAt(currentRow - 1)).getDouble(columnIndex);
   }
   public BigDecimal getBigDecimal(int columnIndex, int scale) throws VirtuosoException
   {
      return getBigDecimal(columnIndex).setScale(scale,BigDecimal.ROUND_UNNECESSARY);
   }
   public byte[] getBytes(int columnIndex) throws VirtuosoException
   {
      if(currentRow < 1 || currentRow > rows.size())
         throw new VirtuosoException("Bad current row selected : " + currentRow + " not in 1<n<" + rows.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoRow)rows.elementAt(currentRow - 1)).getBytes(columnIndex);
   }
   public BigDecimal getBigDecimal(int columnIndex) throws VirtuosoException
   {
      if(currentRow < 1 || currentRow > rows.size())
         throw new VirtuosoException("Bad current row selected : " + currentRow + " not in 1<n<" + rows.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoRow)rows.elementAt(currentRow - 1)).getBigDecimal(columnIndex);
   }
   public InputStream getAsciiStream(int columnIndex) throws VirtuosoException
   {
      if(currentRow < 1 || currentRow > rows.size())
         throw new VirtuosoException("Bad current row selected : " + currentRow + " not in 1<n<" + rows.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoRow)rows.elementAt(currentRow - 1)).getAsciiStream(columnIndex);
   }
   public InputStream getUnicodeStream(int columnIndex) throws VirtuosoException
   {
      return getAsciiStream(columnIndex);
   }
   public InputStream getBinaryStream(int columnIndex) throws VirtuosoException
   {
      if(currentRow < 1 || currentRow > rows.size())
         throw new VirtuosoException("Bad current row selected : " + currentRow + " not in 1<n<" + rows.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoRow)rows.elementAt(currentRow - 1)).getBinaryStream(columnIndex);
   }
   public Reader getCharacterStream(int columnIndex) throws VirtuosoException
   {
      if(currentRow < 1 || currentRow > rows.size())
         throw new VirtuosoException("Bad current row selected : " + currentRow + " not in 1<n<" + rows.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoRow)rows.elementAt(currentRow - 1)).getCharacterStream(columnIndex);
   }
   public String getString(String columnName) throws VirtuosoException
   {
      return getString(findColumn(columnName));
   }
   public boolean getBoolean(String columnName) throws VirtuosoException
   {
      return getBoolean(findColumn(columnName));
   }
   public byte getByte(String columnName) throws VirtuosoException
   {
      return getByte(findColumn(columnName));
   }
   public short getShort(String columnName) throws VirtuosoException
   {
      return getShort(findColumn(columnName));
   }
   public int getInt(String columnName) throws VirtuosoException
   {
      return getInt(findColumn(columnName));
   }
   public long getLong(String columnName) throws VirtuosoException
   {
      return getLong(findColumn(columnName));
   }
   public float getFloat(String columnName) throws VirtuosoException
   {
      return getFloat(findColumn(columnName));
   }
   public double getDouble(String columnName) throws VirtuosoException
   {
      return getDouble(findColumn(columnName));
   }
   public BigDecimal getBigDecimal(String columnName, int scale) throws VirtuosoException
   {
      return getBigDecimal(findColumn(columnName));
   }
   public byte[] getBytes(String columnName) throws VirtuosoException
   {
      return getBytes(findColumn(columnName));
   }
   public Reader getCharacterStream(String columnName) throws VirtuosoException
   {
      return getCharacterStream(findColumn(columnName));
   }
   public BigDecimal getBigDecimal(String columnName) throws VirtuosoException
   {
      return getBigDecimal(findColumn(columnName));
   }
   public InputStream getAsciiStream(String columnName) throws VirtuosoException
   {
      return getAsciiStream(findColumn(columnName));
   }
   public InputStream getUnicodeStream(String columnName) throws VirtuosoException
   {
      return getUnicodeStream(findColumn(columnName));
   }
   public InputStream getBinaryStream(String columnName) throws VirtuosoException
   {
      return getBinaryStream(findColumn(columnName));
   }
   public Object getObject(int columnIndex) throws VirtuosoException
   {
      if(currentRow < 1 || currentRow > rows.size())
         throw new VirtuosoException("Bad current row selected : " + currentRow + " not in 1<n<" + rows.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoRow)rows.elementAt(currentRow - 1)).getObject(columnIndex);
   }
   public Object getObject(String columnName) throws VirtuosoException
   {
      return getObject(findColumn(columnName));
   }
   public java.sql.Date getDate(int columnIndex) throws VirtuosoException
   {
      return getDate(columnIndex, null);
   }
   public java.sql.Time getTime(int columnIndex) throws VirtuosoException
   {
      return getTime(columnIndex, null);
   }
   public java.sql.Timestamp getTimestamp(int columnIndex) throws VirtuosoException
   {
      return getTimestamp(columnIndex, null);
   }
   public java.sql.Date getDate(int columnIndex, Calendar cal) throws VirtuosoException
   {
      java.sql.Date date;
      if(currentRow < 1 || currentRow > rows.size())
         throw new VirtuosoException("Bad current row selected : " + currentRow + " not in 1<n<" + rows.size(),VirtuosoException.BADPARAM);
      date = ((VirtuosoRow)rows.elementAt(currentRow - 1)).getDate(columnIndex);
      if(cal != null && date != null)
        date = new java.sql.Date(VirtuosoTypes.timeToCal(date, cal));
      return date;
   }
   public java.sql.Time getTime(int columnIndex, Calendar cal) throws VirtuosoException
   {
      java.sql.Time _time;
      if(currentRow < 1 || currentRow > rows.size())
         throw new VirtuosoException("Bad current row selected : " + currentRow + " not in 1<n<" + rows.size(),VirtuosoException.BADPARAM);
      _time = ((VirtuosoRow)rows.elementAt(currentRow - 1)).getTime(columnIndex);
      if(cal != null && _time != null)
        _time = new java.sql.Time(VirtuosoTypes.timeToCal(_time, cal));
      return _time;
   }
   public java.sql.Timestamp getTimestamp(int columnIndex, Calendar cal) throws VirtuosoException
   {
      java.sql.Timestamp _ts, val;
      if(currentRow < 1 || currentRow > rows.size())
         throw new VirtuosoException("Bad current row selected : " + currentRow + " not in 1<n<" + rows.size(),VirtuosoException.BADPARAM);
      _ts = val = ((VirtuosoRow)rows.elementAt(currentRow - 1)).getTimestamp(columnIndex);
      if(cal != null && _ts != null)
        _ts = new java.sql.Timestamp(VirtuosoTypes.timeToCal(_ts, cal));
      if (_ts!=null)
       _ts.setNanos(val.getNanos());
      return _ts;
   }
   public java.sql.Date getDate(String columnName) throws VirtuosoException
   {
      return getDate(findColumn(columnName), null);
   }
   public java.sql.Time getTime(String columnName) throws VirtuosoException
   {
      return getTime(findColumn(columnName), null);
   }
   public java.sql.Timestamp getTimestamp(String columnName) throws VirtuosoException
   {
      return getTimestamp(findColumn(columnName), null);
   }
   public void updateNull(String columnName) throws VirtuosoException
   {
      updateNull(findColumn(columnName));
   }
   public void updateBoolean(String columnName, boolean x) throws VirtuosoException
   {
      updateBoolean(findColumn(columnName),x);
   }
   public void updateByte(String columnName, byte x) throws VirtuosoException
   {
      updateByte(findColumn(columnName),x);
   }
   public void updateShort(String columnName, short x) throws VirtuosoException
   {
      updateShort(findColumn(columnName),x);
   }
   public void updateInt(String columnName, int x) throws VirtuosoException
   {
      updateInt(findColumn(columnName),x);
   }
   public void updateLong(String columnName, long x) throws VirtuosoException
   {
      updateLong(findColumn(columnName),x);
   }
   public void updateFloat(String columnName, float x) throws VirtuosoException
   {
      updateFloat(findColumn(columnName),x);
   }
   public void updateDouble(String columnName, double x) throws VirtuosoException
   {
      updateDouble(findColumn(columnName),x);
   }
   public void updateBigDecimal(String columnName, BigDecimal x) throws VirtuosoException
   {
      updateBigDecimal(findColumn(columnName),x);
   }
   public void updateString(String columnName, String x) throws VirtuosoException
   {
      updateString(findColumn(columnName),x);
   }
   public void updateBytes(String columnName, byte x[]) throws VirtuosoException
   {
      updateBytes(findColumn(columnName),x);
   }
   public void updateDate(String columnName, java.sql.Date x) throws VirtuosoException
   {
      updateDate(findColumn(columnName),x);
   }
   public void updateTime(String columnName, java.sql.Time x) throws VirtuosoException
   {
      updateTime(findColumn(columnName),x);
   }
   public void updateTimestamp(String columnName, java.sql.Timestamp x) throws VirtuosoException
   {
      updateTimestamp(findColumn(columnName),x);
   }
   public void updateAsciiStream(String columnName, InputStream x, int length) throws VirtuosoException
   {
      updateAsciiStream(findColumn(columnName),x,length);
   }
   public void updateBinaryStream(String columnName, InputStream x, int length) throws VirtuosoException
   {
      updateBinaryStream(findColumn(columnName),x,length);
   }
   public void updateCharacterStream(String columnName, Reader reader, int length) throws VirtuosoException
   {
      updateCharacterStream(findColumn(columnName),reader,length);
   }
   public void updateObject(String columnName, Object x, int scale) throws VirtuosoException
   {
      updateObject(findColumn(columnName),x,scale);
   }
   public void updateObject(String columnName, Object x) throws VirtuosoException
   {
      updateObject(findColumn(columnName),x);
   }
   public Object getObject(String columnName, Map map) throws VirtuosoException
   {
      return getObject(findColumn(columnName),map);
   }
   public Ref getRef(String columnName) throws VirtuosoException
   {
      return getRef(findColumn(columnName));
   }
   public
   Blob
   getBlob(String columnName) throws VirtuosoException
   {
      return getBlob(findColumn(columnName));
   }
   public
   Clob
   getClob(String columnName) throws VirtuosoException
   {
      return getClob(findColumn(columnName));
   }
   public Array getArray(String columnName) throws VirtuosoException
   {
      return getArray(findColumn(columnName));
   }
   public java.sql.Date getDate(String columnName, Calendar cal) throws VirtuosoException
   {
      return getDate(findColumn(columnName),cal);
   }
   public java.sql.Time getTime(String columnName, Calendar cal) throws VirtuosoException
   {
      return getTime(findColumn(columnName),cal);
   }
   public java.sql.Timestamp getTimestamp(String columnName, Calendar cal) throws VirtuosoException
   {
      return getTimestamp(findColumn(columnName),cal);
   }
   protected void wasNull(boolean flag)
   {
      this.wasNull = flag;
   }
   public void close() throws VirtuosoException
   {
      if(pstmt != null)
      {
         pstmt.close();
         pstmt = null;
      }
      if(rows != null)
      {
         rows.removeAllElements();
      }
      if (statement != null)
      {
         statement.close_rs(false, is_prepared);
         if (!is_prepared) {
           statement = null;
         }
      }
      row = null;
      cursorName = null;
   }
   public boolean next() throws VirtuosoException
   {
       try
       {
    int nextRow = currentRow + 1;
    if (statement == null)
        throw new VirtuosoException ("Activity on a closed statement 1", "IM001", VirtuosoException.SQLERROR);
    if (statement.isClosed() )
        throw new VirtuosoException ("Activity on a closed statement 2", "IM001", VirtuosoException.SQLERROR);
    if(type == VirtuosoResultSet.TYPE_FORWARD_ONLY)
    {
        if (rowNum >= maxRows && maxRows > 0)
            return false;
        while (true)
        {
     synchronized (statement.connection)
     {
         if (is_complete)
         {
      return false;
         }
         Object elt = rows.firstElement();
         if (currentRow == 0 && elt != null)
         {
      stmt_current_of++;
      currentRow ++;
      rowNum++;
      return true;
         }
         if ((stmt_co_last_in_batch || stmt_current_of == stmt_n_rows_to_get - 1)
          && metaData != null && kindop == 1)
         {
      rows.removeElementAt (1);
      fetch_rpc();
      stmt_current_of = -1;
      stmt_co_last_in_batch = false;
         }
         process_result (false);
     }
     currentRow = 0;
        }
    }
    if(nextRow > 0 && nextRow <= rows.size())
    {
        currentRow = nextRow;
        return true;
    }
    else
        if(type == VirtuosoResultSet.TYPE_FORWARD_ONLY)
        {
     currentRow = rows.size() + 1;
     return false;
        }
    if(type == VirtuosoResultSet.TYPE_SCROLL_INSENSITIVE && getRow() == totalRows)
    {
        currentRow = rows.size() + 1;
        return false;
    }
    extended_fetch(VirtuosoTypes.SQL_FETCH_NEXT,0,(prefetch == 0) ? VirtuosoTypes.DEFAULTPREFETCH : prefetch);
    if(rows.size() == 0)
    {
        currentRow = 1;
        return false;
    }
    currentRow = 1;
    return true;
       }
       catch (Throwable e)
       {
    statement.notify_error (e);
    return false;
       }
   }
   public int getRow() throws VirtuosoException
   {
      if(currentRow > 0 && currentRow <= rows.size())
      {
         int r;
         if (type == VirtuosoResultSet.TYPE_SCROLL_INSENSITIVE)
           r = ((VirtuosoRow)(rows.elementAt(currentRow - 1))).getRow();
         else if (type == VirtuosoResultSet.TYPE_SCROLL_SENSITIVE)
           r = ((Number)((openlink.util.Vector)(((VirtuosoRow)(rows.elementAt(currentRow - 1))).getBookmark()).elementAt(1)).elementAt(0)).intValue();
         else if (type == VirtuosoResultSet.TYPE_FORWARD_ONLY)
           r = rowNum;
         else
           r = 0;
         if(r == 0)
            return currentRow;
         return r;
      }
      return currentRow;
   }
   public boolean previous() throws VirtuosoException
   {
      if(type == VirtuosoResultSet.TYPE_FORWARD_ONLY)
         throw new VirtuosoException("Cannot access to the previous row, the type is forward only.",VirtuosoException.ERRORONTYPE);
      if (currentRow == 0)
 return false;
      int previousRow = currentRow - 1;
      if(previousRow > 0 && previousRow <= rows.size())
      {
         currentRow = previousRow;
         return true;
      }
      if(type == VirtuosoResultSet.TYPE_SCROLL_INSENSITIVE && getRow() == 1)
      {
         currentRow = 0;
         return false;
      }
      if(type == VirtuosoResultSet.TYPE_SCROLL_INSENSITIVE)
      {
         int book = getRow();
         extended_fetch(VirtuosoTypes.SQL_FETCH_PRIOR,0,(prefetch == 0) ? VirtuosoTypes.DEFAULTPREFETCH : prefetch);
         if(rows.size() == 0)
         {
            currentRow = 0;
            return false;
         }
         for(int i = (rows.size() - 1);i >= 0;i--)
         {
            if(rows.elementAt(i) != null && ((VirtuosoRow)rows.elementAt(i)).getRow() == book)
            {
               currentRow = i;
               return true;
            }
         }
      }
      if(type == VirtuosoResultSet.TYPE_SCROLL_SENSITIVE)
      {
  VirtuosoRow row = (VirtuosoRow)(rows.elementAt(currentRow - 1));
  openlink.util.Vector book = null;
  if (row != null)
    book = row.getBookmark();
         extended_fetch(VirtuosoTypes.SQL_FETCH_PRIOR,0,(prefetch == 0) ? VirtuosoTypes.DEFAULTPREFETCH : prefetch);
         if(rows.size() == 0)
         {
            currentRow = 0;
            return false;
         }
  if (book != null)
    {
      for(int i = (rows.size() - 1);i >= 0;i--)
        {
   if(rows.elementAt(i) != null && ((VirtuosoRow)rows.elementAt(i)).getBookmark().equals(book))
     {
       currentRow = i;
       return true;
     }
        }
    }
      }
      currentRow = rows.size();
      return true;
   }
   public void beforeFirst() throws VirtuosoException
   {
      absolute(1);
      previous();
   }
   public void afterLast() throws VirtuosoException
   {
      absolute(-1);
      next();
   }
   public boolean first() throws VirtuosoException
   {
      return absolute(1);
   }
   public boolean isBeforeFirst() throws VirtuosoException
   {
      return getRow() == 0;
   }
   public boolean isAfterLast() throws VirtuosoException
   {
      return getRow() == (rows.size() + 1);
   }
   public boolean isFirst() throws VirtuosoException
   {
      return getRow() == 1;
   }
   public boolean isLast() throws VirtuosoException
   {
      return getRow() == totalRows;
   }
   public boolean last() throws VirtuosoException
   {
      return absolute(-1);
   }
   public boolean absolute(int row) throws VirtuosoException
   {
      if(type == VirtuosoResultSet.TYPE_FORWARD_ONLY)
         throw new VirtuosoException("Cannot go before the first row, the type is forward only.",VirtuosoException.ERRORONTYPE);
      extended_fetch(VirtuosoTypes.SQL_FETCH_ABSOLUTE,row,(prefetch == 0) ? VirtuosoTypes.DEFAULTPREFETCH : prefetch);
      if(rows.size() == 0)
      {
         currentRow = (row > 0) ? 0 : 1;
         return false;
      }
      currentRow = 1;
      return true;
   }
   public boolean relative(int row) throws VirtuosoException
   {
      if(type == VirtuosoResultSet.TYPE_FORWARD_ONLY)
         throw new VirtuosoException("Cannot go before the first row, the type is forward only.",VirtuosoException.ERRORONTYPE);
      extended_fetch(VirtuosoTypes.SQL_FETCH_RELATIVE,row,(prefetch == 0) ? VirtuosoTypes.DEFAULTPREFETCH : prefetch);
      if(rows.size() == 0)
      {
         currentRow = (row > 0) ? 0 : 1;
         return false;
      }
      currentRow = 1;
      return true;
   }
   public String getCursorName() throws VirtuosoException
   {
      return cursorName;
   }
   public
   Blob
   getBlob(int i) throws VirtuosoException
   {
      if(currentRow < 1 || currentRow > rows.size())
         throw new VirtuosoException("Bad current row selected : " + currentRow + " not in 1<n<" + rows.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoRow)rows.elementAt(currentRow - 1)).getBlob(i);
   }
   public
   Clob
   getClob(int i) throws VirtuosoException
   {
      if(currentRow < 1 || currentRow > rows.size())
         throw new VirtuosoException("Bad current row selected : " + currentRow + " not in 1<n<" + rows.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoRow)rows.elementAt(currentRow - 1)).getClob(i);
   }
   public boolean rowUpdated() throws VirtuosoException
   {
      return rowIsUpdated;
   }
   public boolean rowInserted() throws VirtuosoException
   {
      return rowIsInserted;
   }
   public boolean rowDeleted() throws VirtuosoException
   {
      return rowIsDeleted;
   }
   public void updateNull(int columnIndex) throws VirtuosoException
   {
      if(columnIndex < 1 || columnIndex > metaData.getColumnCount())
         throw new VirtuosoException("Index " + columnIndex + " is not 1<n<" + metaData.getColumnCount(),VirtuosoException.BADPARAM);
      if(row == null)
      {
         row = new Object[metaData.getColumnCount()];
         if(!(currentRow < 1 || currentRow > rows.size()))
            ((VirtuosoRow)(rows.elementAt(currentRow - 1))).getContent(row);
      }
      row[columnIndex - 1] = null;
   }
   public void updateBoolean(int columnIndex, boolean x) throws VirtuosoException
   {
      if(columnIndex < 1 || columnIndex > metaData.getColumnCount())
         throw new VirtuosoException("Index " + columnIndex + " is not 1<n<" + metaData.getColumnCount(),VirtuosoException.BADPARAM);
      if(row == null)
      {
         row = new Object[metaData.getColumnCount()];
         if(!(currentRow < 1 || currentRow > rows.size()))
            ((VirtuosoRow)(rows.elementAt(currentRow - 1))).getContent(row);
      }
      row[columnIndex - 1] = Boolean.valueOf(x);
   }
   public void updateByte(int columnIndex, byte x) throws VirtuosoException
   {
      if(columnIndex < 1 || columnIndex > metaData.getColumnCount())
         throw new VirtuosoException("Index " + columnIndex + " is not 1<n<" + metaData.getColumnCount(),VirtuosoException.BADPARAM);
      if(row == null)
      {
         row = new Object[metaData.getColumnCount()];
         if(!(currentRow < 1 || currentRow > rows.size()))
            ((VirtuosoRow)(rows.elementAt(currentRow - 1))).getContent(row);
      }
      row[columnIndex - 1] = Byte.valueOf(x);
   }
   public void updateShort(int columnIndex, short x) throws VirtuosoException
   {
      if(columnIndex < 1 || columnIndex > metaData.getColumnCount())
         throw new VirtuosoException("Index " + columnIndex + " is not 1<n<" + metaData.getColumnCount(),VirtuosoException.BADPARAM);
      if(row == null)
      {
         row = new Object[metaData.getColumnCount()];
         if(!(currentRow < 1 || currentRow > rows.size()))
            ((VirtuosoRow)(rows.elementAt(currentRow - 1))).getContent(row);
      }
      row[columnIndex - 1] = Short.valueOf(x);
   }
   public void updateInt(int columnIndex, int x) throws VirtuosoException
   {
      if(columnIndex < 1 || columnIndex > metaData.getColumnCount())
         throw new VirtuosoException("Index " + columnIndex + " is not 1<n<" + metaData.getColumnCount(),VirtuosoException.BADPARAM);
      if(row == null)
      {
         row = new Object[metaData.getColumnCount()];
         if(!(currentRow < 1 || currentRow > rows.size()))
            ((VirtuosoRow)(rows.elementAt(currentRow - 1))).getContent(row);
      }
      row[columnIndex - 1] = Integer.valueOf(x);
   }
   public void updateLong(int columnIndex, long x) throws VirtuosoException
   {
      if(columnIndex < 1 || columnIndex > metaData.getColumnCount())
         throw new VirtuosoException("Index " + columnIndex + " is not 1<n<" + metaData.getColumnCount(),VirtuosoException.BADPARAM);
      if(row == null)
      {
         row = new Object[metaData.getColumnCount()];
         if(!(currentRow < 1 || currentRow > rows.size()))
            ((VirtuosoRow)(rows.elementAt(currentRow - 1))).getContent(row);
      }
      row[columnIndex - 1] = Long.valueOf(x);
   }
   public void updateFloat(int columnIndex, float x) throws VirtuosoException
   {
      if(columnIndex < 1 || columnIndex > metaData.getColumnCount())
         throw new VirtuosoException("Index " + columnIndex + " is not 1<n<" + metaData.getColumnCount(),VirtuosoException.BADPARAM);
      if(row == null)
      {
         row = new Object[metaData.getColumnCount()];
         if(!(currentRow < 1 || currentRow > rows.size()))
            ((VirtuosoRow)(rows.elementAt(currentRow - 1))).getContent(row);
      }
      row[columnIndex - 1] = Float.valueOf(x);
   }
   public void updateDouble(int columnIndex, double x) throws VirtuosoException
   {
      if(columnIndex < 1 || columnIndex > metaData.getColumnCount())
         throw new VirtuosoException("Index " + columnIndex + " is not 1<n<" + metaData.getColumnCount(),VirtuosoException.BADPARAM);
      if(row == null)
      {
         row = new Object[metaData.getColumnCount()];
         if(!(currentRow < 1 || currentRow > rows.size()))
            ((VirtuosoRow)(rows.elementAt(currentRow - 1))).getContent(row);
      }
      row[columnIndex - 1] = Double.valueOf(x);
   }
   public void updateBigDecimal(int columnIndex, BigDecimal x) throws VirtuosoException
   {
      if(columnIndex < 1 || columnIndex > metaData.getColumnCount())
         throw new VirtuosoException("Index " + columnIndex + " is not 1<n<" + metaData.getColumnCount(),VirtuosoException.BADPARAM);
      if(row == null)
      {
         row = new Object[metaData.getColumnCount()];
         if(!(currentRow < 1 || currentRow > rows.size()))
            ((VirtuosoRow)(rows.elementAt(currentRow - 1))).getContent(row);
      }
      row[columnIndex - 1] = x;
   }
   public void updateString(int columnIndex, String x) throws VirtuosoException
   {
      if(columnIndex < 1 || columnIndex > metaData.getColumnCount())
         throw new VirtuosoException("Index " + columnIndex + " is not 1<n<" + metaData.getColumnCount(),VirtuosoException.BADPARAM);
      if(row == null)
      {
         row = new Object[metaData.getColumnCount()];
         if(!(currentRow < 1 || currentRow > rows.size()))
            ((VirtuosoRow)(rows.elementAt(currentRow - 1))).getContent(row);
      }
      row[columnIndex - 1] = x;
   }
   public void updateBytes(int columnIndex, byte x[]) throws VirtuosoException
   {
      if(columnIndex < 1 || columnIndex > metaData.getColumnCount())
         throw new VirtuosoException("Index " + columnIndex + " is not 1<n<" + metaData.getColumnCount(),VirtuosoException.BADPARAM);
      if(row == null)
      {
         row = new Object[metaData.getColumnCount()];
         if(!(currentRow < 1 || currentRow > rows.size()))
            ((VirtuosoRow)(rows.elementAt(currentRow - 1))).getContent(row);
      }
      try
 {
   row[columnIndex - 1] = new String(x, "8859_1");
 }
      catch (java.io.UnsupportedEncodingException e)
 {
   if (x == null)
     row[columnIndex - 1] = new String (x);
   char [] chars = new char[x.length];
   for (int i = 0; i < x.length; i++)
     chars[i] = (char) x[i];
   row[columnIndex - 1] = new String (chars);
 }
   }
   public void updateDate(int columnIndex, java.sql.Date x) throws VirtuosoException
   {
      if(columnIndex < 1 || columnIndex > metaData.getColumnCount())
         throw new VirtuosoException("Index " + columnIndex + " is not 1<n<" + metaData.getColumnCount(),VirtuosoException.BADPARAM);
      if(row == null)
      {
         row = new Object[metaData.getColumnCount()];
         if(!(currentRow < 1 || currentRow > rows.size()))
            ((VirtuosoRow)(rows.elementAt(currentRow - 1))).getContent(row);
      }
      row[columnIndex - 1] = x;
   }
   public void updateTime(int columnIndex, java.sql.Time x) throws VirtuosoException
   {
      if(columnIndex < 1 || columnIndex > metaData.getColumnCount())
         throw new VirtuosoException("Index " + columnIndex + " is not 1<n<" + metaData.getColumnCount(),VirtuosoException.BADPARAM);
      if(row == null)
      {
         row = new Object[metaData.getColumnCount()];
         if(!(currentRow < 1 || currentRow > rows.size()))
            ((VirtuosoRow)(rows.elementAt(currentRow - 1))).getContent(row);
      }
      row[columnIndex - 1] = x;
   }
   public void updateTimestamp(int columnIndex, java.sql.Timestamp x) throws VirtuosoException
   {
      if(columnIndex < 1 || columnIndex > metaData.getColumnCount())
         throw new VirtuosoException("Index " + columnIndex + " is not 1<n<" + metaData.getColumnCount(),VirtuosoException.BADPARAM);
      if(row == null)
      {
         row = new Object[metaData.getColumnCount()];
         if(!(currentRow < 1 || currentRow > rows.size()))
            ((VirtuosoRow)(rows.elementAt(currentRow - 1))).getContent(row);
      }
      row[columnIndex - 1] = x;
   }
   public void updateAsciiStream(int columnIndex, InputStream x, int length) throws VirtuosoException
   {
      if(columnIndex < 1 || columnIndex > metaData.getColumnCount())
         throw new VirtuosoException("Index " + columnIndex + " is not 1<n<" + metaData.getColumnCount(),VirtuosoException.BADPARAM);
      if(x == null || length < 0)
         throw new VirtuosoException("Bad parameters.",VirtuosoException.BADPARAM);
      if(row == null)
      {
         row = new Object[metaData.getColumnCount()];
         if(!(currentRow < 1 || currentRow > rows.size()))
            ((VirtuosoRow)(rows.elementAt(currentRow - 1))).getContent(row);
      }
      Object _obj = row[columnIndex - 1];
      if(_obj instanceof VirtuosoBlob)
      {
         ((VirtuosoBlob)_obj).setInputStream(x,length);
         return;
      }
      row[columnIndex - 1] = new VirtuosoBlob(x,length,columnIndex - 1);
      pstmt.objparams.setElementAt(row[columnIndex - 1],columnIndex - 1);
   }
   public void updateBinaryStream(int columnIndex, InputStream x, int length) throws VirtuosoException
   {
      if(columnIndex < 1 || columnIndex > metaData.getColumnCount())
         throw new VirtuosoException("Index " + columnIndex + " is not 1<n<" + metaData.getColumnCount(),VirtuosoException.BADPARAM);
      if(x == null || length < 0)
         throw new VirtuosoException("Bad parameters.",VirtuosoException.BADPARAM);
      if(row == null)
      {
         row = new Object[metaData.getColumnCount()];
         if(!(currentRow < 1 || currentRow > rows.size()))
            ((VirtuosoRow)(rows.elementAt(currentRow - 1))).getContent(row);
      }
      Object _obj = row[columnIndex - 1];
      if(_obj instanceof VirtuosoBlob)
      {
         ((VirtuosoBlob)_obj).setInputStream(x,length);
         return;
      }
      row[columnIndex - 1] = new VirtuosoBlob(x,length,columnIndex - 1);
      pstmt.objparams.setElementAt(row[columnIndex - 1],columnIndex - 1);
   }
   public void updateCharacterStream(int columnIndex, Reader x, int length) throws VirtuosoException
   {
      if(columnIndex < 1 || columnIndex > metaData.getColumnCount())
         throw new VirtuosoException("Index " + columnIndex + " is not 1<n<" + metaData.getColumnCount(),VirtuosoException.BADPARAM);
      if(x == null || length < 0)
         throw new VirtuosoException("Bad parameters.",VirtuosoException.BADPARAM);
      if(row == null)
      {
         row = new Object[metaData.getColumnCount()];
         if(!(currentRow < 1 || currentRow > rows.size()))
            ((VirtuosoRow)(rows.elementAt(currentRow - 1))).getContent(row);
      }
      Object _obj = row[columnIndex - 1];
      if(_obj instanceof VirtuosoBlob)
      {
         ((VirtuosoBlob)_obj).setReader(x,length);
         return;
      }
      row[columnIndex - 1] = new VirtuosoBlob(x,length,columnIndex - 1);
      pstmt.objparams.setElementAt(row[columnIndex - 1],columnIndex - 1);
   }
   public void updateObject(int columnIndex, Object x) throws VirtuosoException
   {
      if(columnIndex < 1 || columnIndex > metaData.getColumnCount())
         throw new VirtuosoException("Index " + columnIndex + " is not 1<n<" + metaData.getColumnCount(),VirtuosoException.BADPARAM);
      if(x == null)
         throw new VirtuosoException("Bad parameters.",VirtuosoException.BADPARAM);
      if(row == null)
      {
         row = new Object[metaData.getColumnCount()];
         if(!(currentRow < 1 || currentRow > rows.size()))
            ((VirtuosoRow)(rows.elementAt(currentRow - 1))).getContent(row);
      }
      Object _obj = row[columnIndex - 1];
      if(_obj instanceof VirtuosoBlob)
      {
         ((VirtuosoBlob)_obj).setObject(x);
         return;
      }
      row[columnIndex - 1] = new VirtuosoBlob(x,columnIndex - 1);
   }
   public void updateObject(int columnIndex, Object x, int scale) throws VirtuosoException
   {
      updateObject(columnIndex,x);
   }
   public void cancelRowUpdates() throws VirtuosoException
   {
      if(row != null)
         row = null;
   }
   public void insertRow() throws VirtuosoException
   {
      Object[] obj = new Object[1];
      obj[0] = new openlink.util.Vector(row);
      set_pos(VirtuosoTypes.SQL_ADD,new openlink.util.Vector(obj),0);
      row = null;
   }
   public void updateRow() throws VirtuosoException
   {
      if(currentRow < 0 || currentRow > rows.size())
         throw new VirtuosoException("Bad current row selected : " + currentRow + " not in 1<n<" + rows.size(),VirtuosoException.BADPARAM);
      if(currentRow != 0)
      {
         set_pos(VirtuosoTypes.SQL_UPDATE,new openlink.util.Vector(row),currentRow);
         row = null;
      }
      else
         if(oldRow != 0)
            insertRow();
   }
   public void deleteRow() throws VirtuosoException
   {
      if(currentRow < 1 || currentRow > rows.size())
         throw new VirtuosoException("Bad current row selected : " + currentRow + " not in 1<n<" + rows.size(),VirtuosoException.BADPARAM);
      set_pos(VirtuosoTypes.SQL_DELETE,null,currentRow);
   }
   public void refreshRow() throws VirtuosoException
   {
      if(currentRow < 1 || currentRow > rows.size())
         throw new VirtuosoException("Bad current row selected : " + currentRow + " not in 1<n<" + rows.size(),VirtuosoException.BADPARAM);
      set_pos(VirtuosoTypes.SQL_REFRESH,null,currentRow);
   }
   public void moveToInsertRow() throws VirtuosoException
   {
      if(oldRow == 0)
      {
         oldRow = getRow();
         currentRow = 0;
      }
   }
   public void moveToCurrentRow() throws VirtuosoException
   {
      if(oldRow != 0)
      {
         absolute(oldRow);
         oldRow = 0;
      }
   }
   public Object getObject(int i, Map map) throws VirtuosoException
   {
      return null;
   }
   public Ref getRef(int i) throws VirtuosoException
   {
      return null;
   }
   public Array getArray(int i) throws VirtuosoException
   {
      return null;
   }
   protected void sendBlobData (openlink.util.Vector result) throws VirtuosoException
     {
       try
  {
    int index = ((Number)result.elementAt(1)).intValue();
    VirtuosoBlob blob = (VirtuosoBlob)statement.objparams.elementAt(index);
    Reader rd = blob.getCharacterStream ();
    long pos = 0;
    int dtp = VirtuosoTypes.DV_STRING;
    if (statement.parameters != null &&
        statement.parameters.elementAt(index) instanceof openlink.util.Vector)
      {
        openlink.util.Vector pd = (openlink.util.Vector)statement.parameters.elementAt(index);
        dtp = ((Number)pd.elementAt (0)).intValue();
        if (dtp == VirtuosoTypes.DV_BLOB_BIN)
   dtp = VirtuosoTypes.DV_BIN;
        else if (dtp == VirtuosoTypes.DV_BLOB_WIDE)
   dtp = VirtuosoTypes.DV_WIDE;
        else
   dtp = VirtuosoTypes.DV_STRING;
      }
    char[] _obj = new char[VirtuosoTypes.PAGELEN];
    int off;
    do
      {
        off = 0;
        while (off < VirtuosoTypes.PAGELEN && off < blob.length () - pos)
   {
     int read = rd.read(_obj, off,
         (int) ((blob.length () - pos < VirtuosoTypes.PAGELEN - off) ?
         (blob.length () - pos) : (VirtuosoTypes.PAGELEN - off)));
     if (read == -1)
       {
         break;
       }
     off += read;
     pos += read;
   }
        if (off > 0)
   {
     Object toSend;
     if (dtp == VirtuosoTypes.DV_BIN)
       toSend = new String (_obj, 0, off);
     else
       {
         toSend =
      new VirtuosoExplicitString(new String (_obj, 0, off), dtp,
        statement.connection);
       }
     statement.connection.write_object (toSend);
   }
      }
    while (off > 0);
    byte[] end = new byte[1];
    end[0] = 0;
    statement.connection.write_bytes(end);
  }
       catch(IOException e)
  {
    throw new VirtuosoException(er1,VirtuosoException.IOERROR);
  }
     }
   public boolean equals(Object obj)
   {
      if(obj != null && (obj instanceof VirtuosoResultSet))
      {
        return true;
      }
      return false;
   }
   public java.net.URL getURL(int columnIndex) throws SQLException
     {
       throw new VirtuosoException ("DATALINK not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public java.net.URL getURL(String columnName) throws SQLException
     {
       throw new VirtuosoException ("DATALINK not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void updateRef(int columnIndex, Ref x) throws SQLException
     {
       throw new VirtuosoException ("SQL REF not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void updateRef(String columnName, Ref x) throws SQLException
     {
       throw new VirtuosoException ("SQL REF not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void updateBlob(int columnIndex, Blob x) throws SQLException
     {
       updateBinaryStream (columnIndex, x.getBinaryStream(), (int) x.length());
     }
   public void updateBlob(String columnName, Blob x) throws SQLException
     {
       updateBinaryStream (columnName, x.getBinaryStream(), (int) x.length());
     }
   public void updateClob(int columnIndex, Clob x) throws SQLException
     {
       updateCharacterStream (columnIndex, x.getCharacterStream(), (int) x.length());
     }
   public void updateClob(String columnName, Clob x) throws SQLException
     {
       updateCharacterStream (columnName, x.getCharacterStream(), (int) x.length());
     }
   public void updateArray(int columnIndex, Array x) throws SQLException
     {
       throw new VirtuosoException ("Arrays not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void updateArray(String columnName, Array x) throws SQLException
     {
       throw new VirtuosoException ("Arrays not supported", VirtuosoException.NOTIMPLEMENTED);
     }
  public RowId getRowId(int columnIndex) throws SQLException
  {
    throw new VirtuosoFNSException ("getRowId(columnIndex)  not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public RowId getRowId(String columnLabel) throws SQLException
  {
    throw new VirtuosoFNSException ("getRowId(columnLabel)  not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public void updateRowId(int columnIndex, RowId x) throws SQLException
  {
    throw new VirtuosoFNSException ("updateRowId(columnIndex, x)  not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public void updateRowId(String columnLabel, RowId x) throws SQLException
  {
    throw new VirtuosoFNSException ("updateRowId(columnLabel, x)  not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public int getHoldability() throws SQLException
  {
    throw new VirtuosoFNSException ("getHoldability()  not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public boolean isClosed() throws SQLException
  {
    if (statement == null || statement.future == null)
      return true;
    else
      return false;
  }
  public synchronized void updateNString(int columnIndex, String x) throws SQLException
  {
      if(columnIndex < 1 || columnIndex > metaData.getColumnCount())
         throw new VirtuosoException("Index " + columnIndex + " is not 1<n<" + metaData.getColumnCount(),VirtuosoException.BADPARAM);
      if(row == null)
      {
         row = new Object[metaData.getColumnCount()];
         if(!(currentRow < 1 || currentRow > rows.size()))
            ((VirtuosoRow)(rows.elementAt(currentRow - 1))).getContent(row);
      }
      row[columnIndex - 1] = x;
  }
  public void updateNString(String columnLabel, String nString) throws SQLException
  {
    updateNString (findColumn (columnLabel), nString);
  }
  public synchronized void updateNClob(int columnIndex, NClob nClob) throws SQLException
  {
    updateCharacterStream (columnIndex, nClob.getCharacterStream(), (int) nClob.length());
  }
  public void updateNClob(String columnLabel, NClob nClob) throws SQLException
  {
    updateNClob (findColumn (columnLabel), nClob);
  }
  public synchronized NClob getNClob(int columnIndex) throws SQLException
  {
      if(currentRow < 1 || currentRow > rows.size())
         throw new VirtuosoException("Bad current row selected : " + currentRow + " not in 1<n<" + rows.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoRow)rows.elementAt(currentRow - 1)).getClob(columnIndex);
  }
  public NClob getNClob(String columnLabel) throws SQLException
  {
    return getNClob(findColumn (columnLabel));
  }
  public SQLXML getSQLXML(int columnIndex) throws SQLException
  {
    throw new VirtuosoFNSException ("getSQLXML(columnIndex)  not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public SQLXML getSQLXML(String columnLabel) throws SQLException
  {
    throw new VirtuosoFNSException ("getSQLXML(String columnLabel)  not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException
  {
    throw new VirtuosoFNSException ("updateSQLXML(columnIndex, xmlObject)  not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException
  {
    throw new VirtuosoFNSException ("updateSQLXML(columnLabel, xmlObject)  not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public synchronized String getNString(int columnIndex) throws SQLException
  {
      if(currentRow < 1 || currentRow > rows.size())
         throw new VirtuosoException("Bad current row selected : " + currentRow + " not in 1<n<" + rows.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoRow)rows.elementAt(currentRow - 1)).getString(columnIndex);
  }
  public String getNString(String columnLabel) throws SQLException
  {
    return getNString(findColumn (columnLabel));
  }
  public synchronized java.io.Reader getNCharacterStream(int columnIndex) throws SQLException
  {
      if(currentRow < 1 || currentRow > rows.size())
         throw new VirtuosoException("Bad current row selected : " + currentRow + " not in 1<n<" + rows.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoRow)rows.elementAt(currentRow - 1)).getCharacterStream(columnIndex);
  }
  public java.io.Reader getNCharacterStream(String columnLabel) throws SQLException
  {
    return getNCharacterStream (findColumn (columnLabel));
  }
  public synchronized void updateNCharacterStream(int columnIndex,
        java.io.Reader x,
       long length) throws SQLException
  {
      updateCharacterStream(columnIndex, x, length);
  }
  public void updateNCharacterStream(String columnLabel,
        java.io.Reader reader,
        long length) throws SQLException
  {
    updateNCharacterStream (findColumn (columnLabel), reader, length);
  }
  public synchronized void updateAsciiStream(int columnIndex,
      java.io.InputStream x,
      long length) throws SQLException
  {
    updateAsciiStream(columnIndex, x, (int)length);
  }
  public synchronized void updateBinaryStream(int columnIndex,
       java.io.InputStream x,
       long length) throws SQLException
  {
    updateBinaryStream(columnIndex, x, length);
  }
  public synchronized void updateCharacterStream(int columnIndex,
        java.io.Reader x,
        long length) throws SQLException
  {
    updateCharacterStream(columnIndex, x, length);
  }
  public void updateAsciiStream(String columnLabel,
      java.io.InputStream x,
      long length) throws SQLException
  {
    updateAsciiStream (findColumn (columnLabel), x, length);
  }
  public void updateBinaryStream(String columnLabel,
       java.io.InputStream x,
       long length) throws SQLException
  {
    updateBinaryStream (findColumn (columnLabel), x, length);
  }
  public void updateCharacterStream(String columnLabel,
        java.io.Reader reader,
        long length) throws SQLException
  {
    updateCharacterStream (findColumn (columnLabel), reader, length);
  }
  public synchronized void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException
  {
    updateBinaryStream (columnIndex, inputStream, (int)length);
  }
  public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException
  {
    updateBlob (findColumn (columnLabel), inputStream, length);
  }
  public synchronized void updateClob(int columnIndex, Reader reader, long length) throws SQLException
  {
    updateCharacterStream (columnIndex, reader, (int)length);
  }
  public void updateClob(String columnLabel, Reader reader, long length) throws SQLException
  {
    updateClob (findColumn (columnLabel), reader, length);
  }
  public synchronized void updateNClob(int columnIndex, Reader reader, long length) throws SQLException
  {
    updateCharacterStream (columnIndex, reader, (int)length);
  }
  public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException
  {
    updateNClob (findColumn (columnLabel), reader, length);
  }
  public void updateNCharacterStream(int columnIndex, java.io.Reader x) throws SQLException
  {
    throw new VirtuosoFNSException ("updateNCharacterStream(columnIndex, x)  not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public void updateNCharacterStream(String columnLabel, java.io.Reader reader) throws SQLException
  {
    throw new VirtuosoFNSException ("updateNCharacterStream(columnLabel, reader)  not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public void updateAsciiStream(int columnIndex, java.io.InputStream x) throws SQLException
  {
    throw new VirtuosoFNSException ("updateAsciiStream(columnIndex, x)  not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public void updateBinaryStream(int columnIndex, java.io.InputStream x) throws SQLException
  {
    throw new VirtuosoFNSException ("updateBinaryStream(columnIndex, x)  not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public void updateCharacterStream(int columnIndex, java.io.Reader x) throws SQLException
  {
    throw new VirtuosoFNSException ("updateCharacterStream(columnIndex, x)  not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public void updateAsciiStream(String columnLabel, java.io.InputStream x) throws SQLException
  {
    throw new VirtuosoFNSException ("updateAsciiStream(columnLabel, x)  not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public void updateBinaryStream(String columnLabel, java.io.InputStream x) throws SQLException
  {
    throw new VirtuosoFNSException ("updateBinaryStream(columnLabel, x)  not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public void updateCharacterStream(String columnLabel, java.io.Reader reader) throws SQLException
  {
    throw new VirtuosoFNSException ("updateCharacterStream(columnLabel, reader)  not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException
  {
    throw new VirtuosoFNSException ("updateBlob(columnIndex, inputStream)  not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException
  {
    throw new VirtuosoFNSException ("updateBlob(columnLabel, inputStream)  not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public void updateClob(int columnIndex, Reader reader) throws SQLException
  {
    throw new VirtuosoFNSException ("updateClob(columnIndex,  reader)  not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public void updateClob(String columnLabel, Reader reader) throws SQLException
  {
    throw new VirtuosoFNSException ("updateClob(columnLabel,  reader)  not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public void updateNClob(int columnIndex, Reader reader) throws SQLException
  {
    throw new VirtuosoFNSException ("updateNClob(columnIndex,  reader)  not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public void updateNClob(String columnLabel, Reader reader) throws SQLException
  {
    throw new VirtuosoFNSException ("updateNClob(columnLabel,  reader)  not supported", VirtuosoException.NOTIMPLEMENTED);
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
