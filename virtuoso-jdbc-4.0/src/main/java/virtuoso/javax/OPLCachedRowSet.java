package virtuoso.javax;
import java.io.*;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;
import java.util.BitSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.net.URL;
import java.sql.*;
import javax.sql.*;
import javax.naming.*;
import openlink.util.OPLHeapBlob;
import openlink.util.OPLHeapClob;
import openlink.util.OPLHeapNClob;
public class OPLCachedRowSet extends BaseRowSet
        implements RowSetInternal, Serializable, Cloneable {
    private static final long serialVersionUID = -8262862611500365291L;
    private static final int BEFOREFIRST = 0;
    private static final int FIRSTROW = 1;
    private static final int BODYROW = 2;
    private static final int LASTROW = 3;
    private static final int AFTERLAST = 4;
    private static final int NOROWS = 5;
    private RowSetReader rowSetReader;
    private RowSetWriter rowSetWriter;
    private transient Connection conn;
    private RowSetMetaData rowSMD;
    private int keyCols[];
    private String tableName;
    private ArrayList<Object> rowsData;
    private int curState = NOROWS;
    private int curRow;
    private int absolutePos;
    private int countDeleted;
    private int countRows;
    private Row updateRow;
    private boolean onInsertRow;
    private boolean showDeleted;
    private InputStream objInputStream = null;
    private Reader objReader = null;
    private boolean _wasNull = false;
  public OPLCachedRowSet() throws SQLException {
    rowSetReader = new RowSetReader();
    rowSetWriter = new RowSetWriter();
    rowsData = new ArrayList<Object>();
    onInsertRow = false;
    updateRow = null;
    setType(ResultSet.TYPE_SCROLL_INSENSITIVE);
    setConcurrency(ResultSet.CONCUR_READ_ONLY);
    showDeleted = false;
    curRow = -1;
    absolutePos = 0;
  }
  protected Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
  public synchronized void finalize () throws Throwable
  {
    close();
  }
  public synchronized void setCommand(String cmd) throws SQLException {
    tableName = null;
    keyCols = null;
    super.setCommand(cmd);
  }
  public synchronized void setConcurrency(int concurrency) throws SQLException {
    if (tableName == null && concurrency == ResultSet.CONCUR_UPDATABLE)
      throw OPLMessage_x.makeException(OPLMessage_x.errx_The_name_of_table_is_not_defined);
    super.setConcurrency(concurrency);
  }
  public synchronized void acceptChanges() throws SQLException {
    check_InsertMode("'acceptChanges()'");
    if (rowSetWriter == null)
      throw OPLMessage_x.makeException(OPLMessage_x.errx_RowSetWriter_is_not_defined);
    int _curRow = curRow;
    int _absolutePos = absolutePos;
    int _curState = curState;
    boolean success = true;
    SQLException ex = null;
    try {
      success = rowSetWriter.writeData(this);
    } catch (SQLException e) {
      ex = e;
    } finally {
      curRow = _curRow;
      absolutePos = _absolutePos;
      curState = _curState;
    }
    if (success) {
      setOriginal();
    } else {
      if (ex == null)
         throw OPLMessage_x.makeException(OPLMessage_x.errx_acceptChanges_Failed);
      else
         throw ex;
    }
  }
  public void acceptChanges(Connection _conn) throws SQLException {
    conn = _conn;
    acceptChanges();
  }
  public synchronized void execute() throws java.sql.SQLException {
    rowSetReader.readData(this);
    if (tableName == null) {
      Scanner scan = new Scanner(getCommand());
      tableName = scan.check_Select();
      if (tableName == null)
        setConcurrency(ResultSet.CONCUR_READ_ONLY);
      else
        setConcurrency(ResultSet.CONCUR_UPDATABLE);
    }
  }
  public void execute(Connection _conn) throws SQLException {
    conn = _conn;
    execute();
  }
  public synchronized void populate(ResultSet rs) throws SQLException {
    check_InsertMode("'populate((...)'");
    tableName = null;
    keyCols = null;
    Map<String,Class<?>> map = getTypeMap();
    ResultSetMetaData rsmd = rs.getMetaData();
    int colCount = rsmd.getColumnCount();
    int i;
    for(i = 0; rs.next(); i++) {
      Row row = new Row(colCount);
      for (int j = 1; j <= colCount; j++) {
        Object x;
        if (map == null)
          x = rs.getObject(j);
        else
          x = rs.getObject(j, map);
        if (x instanceof Blob)
          x = new OPLHeapBlob(((Blob)x).getBytes(0L, (int)((Blob)x).length()));
        else
        if (x instanceof Clob)
          x = new OPLHeapClob(((Clob)x).getSubString(0L, (int)((Clob)x).length()));
        else
        if (x instanceof NClob)
          x = new OPLHeapNClob(((NClob)x).getSubString(0L, (int)((NClob)x).length()));
        row.setOrigColData(j, x);
      }
      rowsData.add(row);
    }
    countRows = i;
    if (countRows > 0)
      curState = BEFOREFIRST;
    else
      curState = NOROWS;
    curRow = -1;
    absolutePos = 0;
    rowSMD = new OPLRowSetMetaData(rsmd);
    notifyListener(ev_RowSetChanged);
  }
  public synchronized void setShowDeleted(boolean value) throws SQLException {
    check_InsertMode("'setShowDeleted(...)'");
    if (showDeleted && !value && rowDeleted()) {
      showDeleted = value;
      switch(curState) {
        case FIRSTROW:
          _first();
          notifyListener(ev_RowChanged);
          break;
        case BODYROW:
          int _absPos = absolutePos;
          _next();
          absolutePos = _absPos;
          notifyListener(ev_RowChanged);
          break;
        case LASTROW:
          _last();
          notifyListener(ev_RowChanged);
          break;
      }
    } else {
      showDeleted = value;
      switch(curState) {
        case FIRSTROW:
        case LASTROW:
        case BODYROW:
          if (curRow < countRows / 2) {
              int _row = curRow;
             _beforeFirst();
             while(_next() && _row != curRow) ;
          } else {
              int _row = curRow;
             _afterLast();
             while(_previous() && _row != curRow) ;
          }
          break;
        case BEFOREFIRST:
          _beforeFirst();
          break;
        case AFTERLAST:
          _afterLast();
          break;
      }
    }
  }
  public boolean getShowDeleted() throws SQLException {
    return showDeleted;
  }
  public String getTableName() throws SQLException {
    return tableName;
  }
  public synchronized void setTableName(String _tableName) throws SQLException {
    tableName = _tableName;
  }
  public int[] getKeyCols() throws SQLException {
    return keyCols;
  }
  public void setKeyColumns(int[] keys) throws SQLException {
     int colsCount = (rowSMD != null ? rowSMD.getColumnCount() : 0);
     for (int i = 0; i < keys.length; i++) {
       if (keys[i] < 1 || keys[i] > colsCount)
          throw OPLMessage_x.makeException(OPLMessage_x.errx_Column_Index_out_of_range);
     }
     if (keys.length > colsCount)
         throw OPLMessage_x.makeException(OPLMessage_x.errx_Invalid_key_columns);
    keyCols = new int[keys.length];
    System.arraycopy(keys, 0, keyCols, 0, keys.length);
  }
  public synchronized void cancelRowDelete() throws SQLException {
    if (!showDeleted)
       return;
    check_pos("'cancelRowDelete()'");
    check_InsertMode("'cancelRowDelete()'");
    Row row = (Row)getCurRow();
    if (row.isDeleted) {
       row.isDeleted = false;
       countDeleted--;
       notifyListener(this.ev_RowChanged);
    }
  }
  public synchronized void cancelRowInsert() throws SQLException {
    check_pos("'cancelRowInsert()'");
    check_InsertMode("'cancelRowInsert()'");
    Row row = (Row)getCurRow();
    if (row.isInserted) {
       rowsData.remove(curRow);
       notifyListener(ev_RowChanged);
       countRows--;
       if (countRows == 0) {
          curState = NOROWS;
          curRow = -1;
          absolutePos = 0;
       }
       switch(curState) {
         case FIRSTROW:
            _first();
            break;
         case LASTROW:
            _last();
            break;
         case BODYROW:
            if (curRow == countRows - 1) {
              curState = LASTROW;
            } else {
              boolean found = false;
              int i = curRow;
              while (!found) {
                i++;
                if (i < countRows)
                  found = true;
                else
                  break;
                if (!showDeleted && ((Row)rowsData.get(i)).isDeleted)
                 found = false;
              }
              if (!found)
                curState = LASTROW;
            }
            break;
       }
    } else {
       throw OPLMessage_x.makeException(OPLMessage_x.errx_Illegal_operation_on_non_inserted_row);
    }
  }
  public synchronized void cancelRowUpdates() throws SQLException {
    check_pos("'cancelRowUpdates()'");
    cancelUpdates();
    Row row = (Row)getCurRow();
    if (row.isUpdated) {
       row.clearUpdated();
       notifyListener(ev_RowChanged);
    }
  }
  public synchronized boolean columnUpdated(int columnIndex) throws SQLException {
    check_pos("'columnUpdated(...)'");
    check_InsertMode("'columnUpdated(...)'");
    return ((Row)getCurRow()).isColUpdated(columnIndex);
  }
  public synchronized void setOriginal() throws SQLException {
    if (countRows == 0)
      return;
    for(Iterator i = rowsData.iterator(); i.hasNext(); ) {
      Row row = (Row)i.next();
      if (row.isDeleted) {
        i.remove();
        countRows--;
      } else {
        row.moveCurToOrig();
      }
    }
    countDeleted = 0;
    curState = BEFOREFIRST;
    curRow = -1;
    absolutePos = 0;
    _wasNull = false;
    notifyListener(ev_RowSetChanged);
  }
  public synchronized void setOriginalRow() throws SQLException {
    if (countRows == 0)
      return;
    check_InsertMode("'setOriginalRow()'");
    check_pos("'setOriginalRow()'");
    Row row = (Row)getCurRow();
      if (row.isDeleted) {
        rowsData.remove(curRow);
        countRows--;
        countDeleted--;
        _next();
      } else {
        row.moveCurToOrig();
      }
    notifyListener(ev_RowChanged);
  }
  public synchronized void restoreOriginal() throws SQLException {
    closeInputStream();
    cancelUpdates();
    if (countRows == 0)
      return;
    for(Iterator i = rowsData.iterator(); i.hasNext(); ) {
       Row row = (Row)i.next();
       if (row.isInserted) {
           i.remove();
           countRows--;
       } else {
          if (row.isDeleted)
             row.isDeleted = false;
          if (row.isUpdated)
             row.clearUpdated();
       }
    }
    curRow = -1;
    absolutePos = 0;
    curState = BEFOREFIRST;
    _wasNull = false;
    notifyListener(ev_RowSetChanged);
  }
  public int size() {
    return countRows;
  }
  public synchronized Collection toCollection() throws SQLException {
    int count = countRows - countDeleted;
    if (count == 0)
      return null;
    ArrayList<Object> tmpRowset = new ArrayList<Object>(count);
    int colCount = rowSMD.getColumnCount();
    for(Iterator i = rowsData.iterator(); i.hasNext(); ) {
      Row row = (Row)i.next();
      if (!row.isDeleted) {
        ArrayList<Object> tmpCol = new ArrayList<Object>(colCount);
        for(int j = 1; j <= colCount; j++)
          tmpCol.add(row.getColData(j));
        tmpRowset.add(tmpCol);
      }
    }
    return tmpRowset;
  }
  public synchronized Collection toCollection(int col) throws SQLException {
    int count = countRows - countDeleted;
    if (count == 0)
      return null;
    ArrayList<Object> tmpRowset = new ArrayList<Object>(count);
    checkColumnIndex(col);
    for(Iterator i = rowsData.iterator(); i.hasNext(); ) {
      Row row = (Row)i.next();
      if (!row.isDeleted)
        tmpRowset.add(row.getColData(col));
    }
    return tmpRowset;
  }
  public synchronized void release() throws SQLException {
    closeInputStream();
    cancelUpdates();
    rowsData.clear();
    curState = NOROWS;
    onInsertRow = false;
    updateRow = null;
    showDeleted = false;
    curRow = -1;
    absolutePos = 0;
    countRows = 0;
    countDeleted = 0;
    notifyListener(ev_RowSetChanged);
  }
  public RowSet createCopy() throws SQLException {
    try {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      ObjectOutputStream obj_os = new ObjectOutputStream(os);
      obj_os.writeObject(this);
      ObjectInputStream obj_in = new ObjectInputStream(new ByteArrayInputStream(os.toByteArray()));
      return (RowSet)obj_in.readObject();
    } catch(Exception e) {
       throw new SQLException("createCopy failed: " + e.getMessage());
    }
  }
  public RowSet createShared() throws SQLException {
    RowSet rowset;
    try {
      rowset = (RowSet)clone();
    } catch(CloneNotSupportedException e) {
       throw OPLMessage_x.makeException(e);
    }
    return rowset;
  }
  public void setMetaData(RowSetMetaData md) throws SQLException {
    rowSMD = md;
  }
  public Connection getConnection() throws SQLException {
    return conn;
  }
  public synchronized ResultSet getOriginal() throws SQLException {
    OPLCachedRowSet crs = new OPLCachedRowSet();
    crs.rowSMD = rowSMD;
    crs.countRows = countRows;
    crs.curRow = -1;
    crs.rowSetReader = null;
    crs.rowSetWriter = null;
    crs.curState = BEFOREFIRST;
    crs._wasNull = false;
    for(Iterator i = rowsData.iterator(); i.hasNext(); ) {
      crs.rowsData.add( new Row( ((Row)i.next()).getOrigData() ) );
    }
    return crs;
  }
  public synchronized ResultSet getOriginalRow() throws SQLException {
    OPLCachedRowSet crs = new OPLCachedRowSet();
    crs.rowSMD = rowSMD;
    crs.countRows = 1;
    crs.rowSetReader = null;
    crs.rowSetWriter = null;
    crs.curState = BEFOREFIRST;
    crs._wasNull = false;
    crs.rowsData.add( new Row( getCurRow().getOrigData() ) );
    return crs;
  }
  public synchronized void close() throws SQLException {
    release();
    super.close();
    conn = null;
  }
  public synchronized boolean next() throws SQLException {
    check_move("'next()'", true);
    closeInputStream();
    cancelUpdates();
    boolean ret = _next();
    notifyListener(ev_CursorMoved);
    return ret;
  }
  public synchronized boolean previous() throws SQLException {
    check_move("'previous()'", false);
    closeInputStream();
    cancelUpdates();
    boolean ret = _previous();
    notifyListener(ev_CursorMoved);
    return ret;
  }
  public synchronized boolean first() throws SQLException {
    check_move("'first()'", false);
    closeInputStream();
    cancelUpdates();
    boolean ret = _first();
    notifyListener(ev_CursorMoved);
    return ret;
  }
  public synchronized boolean last() throws SQLException {
    check_move("'last()'", false);
    closeInputStream();
    cancelUpdates();
    boolean ret = _last();
    notifyListener(ev_CursorMoved);
    return ret;
  }
  public synchronized boolean absolute(int row) throws SQLException {
    check_move("'absolute(...)'", false);
    closeInputStream();
    cancelUpdates();
    if (row == 0)
       throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Invalid_row_number_for_XX, "'absolute(...)'");
    boolean ret;
    if (!showDeleted) {
      if (row > 0) {
        if (row == 1) {
           _first();
        } else {
           while(absolutePos != row) {
             if (absolutePos >= row)
               ret = _previous();
             else
               ret = _next();
             if (!ret)
               break;
           }
        }
      } else {
        ret = _last();
        if (ret && row < -1) {
          int pos = -1;
          while (pos != row && ret) {
            ret = _previous();
            if (ret)
               pos--;
          }
        }
      }
    } else {
      if (row > 0) {
        if (row > countRows) {
          _afterLast();
        } else {
          curRow = row - 1;
          absolutePos = row;
          curState = BODYROW;
        }
      } else {
        if (row * -1 > countRows) {
          _beforeFirst();
        } else {
          curRow = countRows + row;
          absolutePos = curRow + 1;
          curState = BODYROW;
        }
      }
    }
    notifyListener(ev_CursorMoved);
    return !isAfterLast() && !isBeforeFirst();
  }
  public synchronized boolean relative(int rows) throws SQLException {
    check_move("'relative(...)'", false);
    closeInputStream();
    cancelUpdates();
    if (rows == 0)
      return true;
    if (rows > 0) {
       if (curRow + rows >= countRows) {
         _afterLast();
       } else {
         for (int i = 0; i < rows; i++)
            if (!_next())
               break;
       }
    } else {
      if (curRow + rows < 0) {
         beforeFirst();
      } else {
         for (int i = rows; i < 0; i++)
            if (!_previous())
               break;
      }
    }
    notifyListener(this.ev_CursorMoved);
    return !isAfterLast() && !isBeforeFirst();
  }
  public synchronized void beforeFirst() throws SQLException {
    check_move("'beforeFirst()'", false);
    closeInputStream();
    cancelUpdates();
    _beforeFirst();
    notifyListener(this.ev_CursorMoved);
  }
  public synchronized void afterLast() throws SQLException {
    check_move("'afterLast()'", false);
    closeInputStream();
    cancelUpdates();
    _afterLast();
    notifyListener(this.ev_CursorMoved);
  }
  public boolean isBeforeFirst() throws SQLException {
    check_InsertMode("'isBeforeFirst()'");
    if (curState == BEFOREFIRST)
      return true;
    else
      return false;
  }
  public boolean isAfterLast() throws SQLException {
    check_InsertMode("'isAfterLast()'");
    if (curState == AFTERLAST)
      return true;
    else
      return false;
  }
  public synchronized boolean isFirst() throws SQLException {
    check_InsertMode("'isFirst()'");
    if (curState == FIRSTROW) {
      return true;
    } else if (curState == LASTROW) {
      int _curRow = curRow;
      int _absolutePos = absolutePos;
      boolean prev_exists = _previous();
      curRow = _curRow;
      absolutePos = _absolutePos;
      curState = LASTROW;
      if (!prev_exists)
        return true;
    }
    return false;
  }
  public synchronized boolean isLast() throws SQLException {
    check_InsertMode("'isLast()'");
    if (curState == LASTROW) {
      return true;
    } else if (curState == FIRSTROW) {
      int _curRow = curRow;
      int _absolutePos = absolutePos;
      boolean next_exists = _next();
      curRow = _curRow;
      absolutePos = _absolutePos;
      curState = FIRSTROW;
      if (!next_exists)
        return true;
    }
    return false;
  }
  public synchronized int getRow() throws SQLException {
    check_InsertMode("'getRow()'");
    if (curState == BEFOREFIRST || curState == AFTERLAST || curState == NOROWS)
      return 0;
    return absolutePos;
  }
  public synchronized boolean rowUpdated() throws SQLException {
    check_InsertMode("'rowUpdated()'");
    if (curState == BEFOREFIRST || curState == AFTERLAST || curState == NOROWS)
       return false;
    return ((Row)rowsData.get(curRow)).isUpdated;
  }
  public synchronized boolean rowInserted() throws SQLException {
    check_InsertMode("'rowInserted()'");
    if (curState == BEFOREFIRST || curState == AFTERLAST || curState == NOROWS)
       return false;
    return ((Row)rowsData.get(curRow)).isInserted;
  }
  public synchronized boolean rowDeleted() throws SQLException {
    check_InsertMode("'rowDeleted()'");
    if (curState == BEFOREFIRST || curState == AFTERLAST || curState == NOROWS)
       return false;
    return ((Row)rowsData.get(curRow)).isDeleted;
  }
  public synchronized void refreshRow() throws SQLException {
    check_move("'refreshRow()'", false);
    closeInputStream();
    cancelUpdates();
  }
  public synchronized void insertRow() throws SQLException {
    if (!onInsertRow)
        throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_XX_was_called_when_the_insert_row_is_off, "'insertRow()'");
    check_Update("'insertRow()'");
    if (updateRow == null || !updateRow.isCompleted())
        throw OPLMessage_x.makeException(OPLMessage_x.errx_Failed_to_insert_Row);
    Row row = new Row(updateRow.getCurData());
    row.isInserted = true;
    switch(curState) {
      case FIRSTROW:
      case LASTROW:
      case BODYROW:
          rowsData.add(curRow, row);
          break;
      case BEFOREFIRST:
      case NOROWS:
          rowsData.add(0, row);
          curState = BEFOREFIRST;
          break;
      case AFTERLAST:
          rowsData.add(row);
        break;
    }
    countRows++;
    notifyListener(ev_RowChanged);
  }
  public synchronized void updateRow() throws SQLException {
    if (onInsertRow)
        throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_XX_was_called_when_the_insert_row_is_off, "'updateRow()'");
    check_Update("'updateRow()'");
    check_pos("'updateRow()'");
    if (updateRow != null) {
      ((Row)getCurRow()).update(updateRow.getCurData(), updateRow.getListUpdatedCols());
      notifyListener(ev_RowChanged);
      updateRow.clear();
      updateRow = null;
    }
  }
  public synchronized void deleteRow() throws SQLException {
    if (onInsertRow)
        throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_XX_was_called_when_the_insert_row_is_off, "'deleteRow()'");
    check_Update("'deleteRow()'");
    check_pos("'deleteRow()'");
    Row row = (Row)getCurRow();
    if (!row.isDeleted) {
       row.isDeleted = true;
       countDeleted++;
       if (!showDeleted) {
         int _absPos = absolutePos;
         _next();
         absolutePos = _absPos;
      }
       notifyListener(ev_RowChanged);
    }
  }
  public synchronized void moveToInsertRow() throws SQLException {
    check_Update("'moveToInsertRow()'");
    if (updateRow != null)
      updateRow.clear();
    int count = rowSMD.getColumnCount();
    if (count > 0) {
      updateRow = new Row(count);
      onInsertRow = true;
    }
  }
  public synchronized void moveToCurrentRow() throws SQLException {
    if (onInsertRow) {
      cancelUpdates();
      onInsertRow = false;
      if (curState == AFTERLAST) {
        _last();
      }
      return;
    }
  }
  public boolean wasNull() throws SQLException {
    return _wasNull;
  }
  public SQLWarning getWarnings() throws SQLException {
    return null;
  }
  public void clearWarnings() throws SQLException {
  }
  public String getCursorName() throws SQLException {
    return null;
  }
  public ResultSetMetaData getMetaData() throws SQLException {
    return rowSMD;
  }
  public int findColumn(String columnName) throws SQLException {
    if (rowSMD == null)
          throw OPLMessage_x.makeException(OPLMessage_x.errx_Names_of_columns_are_not_found);
    int count = rowSMD.getColumnCount();
    for (int i = 1; i <= count; i++) {
      String name = rowSMD.getColumnName(i);
      if (name != null && name.equalsIgnoreCase(columnName))
        return i;
    }
    throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Invalid_column_name, columnName);
  }
  public synchronized String getString(int columnIndex) throws SQLException {
    checkColumnIndex(columnIndex);
    Object x = getCurRow().getColData(columnIndex);
    if (_wasNull = (x == null)) {
      return null;
    } else {
      if (x instanceof byte[])
        return Bin2Hex((byte[])x);
      else if (x instanceof Blob)
        return Bin2Hex(((Blob)x).getBytes(0L, (int)((Blob)x).length()));
      else if (x instanceof Clob)
        return ((Clob)x).getSubString(0L, (int)((Clob)x).length());
      else if (x instanceof NClob)
        return ((NClob)x).getSubString(0L, (int)((NClob)x).length());
      else
        return x.toString();
    }
  }
  public synchronized boolean getBoolean(int columnIndex) throws SQLException {
    checkColumnIndex(columnIndex);
    Object x = getCurRow().getColData(columnIndex);
    if (_wasNull = (x == null)) {
      return false;
    } else {
      int c;
      if (x instanceof Boolean)
         return ((Boolean)x).booleanValue();
      else if (x instanceof String) {
        c =((String)x).charAt(0);
        return (c == 'T' || c == 't' || c == '1');
      }else if (x instanceof byte[])
        return ((byte[])x)[0] != 0;
      else if (x instanceof Blob)
        return ((Blob)x).getBytes(0L, 1)[0] != 0;
      else if (x instanceof Clob) {
        c =((Clob)x).getSubString(0L, 1).charAt(0);
        return (c == 'T' || c == 't' || c == '1');
      }else if (x instanceof NClob) {
        c =((NClob)x).getSubString(0L, 1).charAt(0);
        return (c == 'T' || c == 't' || c == '1');
      }else if (x instanceof Number)
        return ((Number)x).intValue() != 0;
      else
        throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'boolean'");
    }
  }
  public synchronized byte getByte(int columnIndex) throws SQLException {
    checkColumnIndex(columnIndex);
    Object x = getCurRow().getColData(columnIndex);
    if (_wasNull = (x == null)) {
      return 0;
    } else {
      int c;
      if (x instanceof Number)
         return ((Number)x).byteValue();
      else if (x instanceof Boolean)
        return (byte)(((Boolean)x).booleanValue()? 1 : 0);
      else if (x instanceof String) {
        return (new BigDecimal(((String)x).toString())).byteValue();
      }else
        throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'byte'");
    }
  }
  public synchronized short getShort(int columnIndex) throws SQLException {
    checkColumnIndex(columnIndex);
    Object x = getCurRow().getColData(columnIndex);
    if (_wasNull = (x == null)) {
      return 0;
    } else {
      int c;
      if (x instanceof Number)
         return ((Number)x).shortValue();
      else if (x instanceof Boolean)
        return (short)(((Boolean)x).booleanValue()? 1 : 0);
      else if (x instanceof String) {
        return (new BigDecimal(((String)x).toString())).shortValue();
      }else
        throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'short'");
    }
  }
  public synchronized int getInt(int columnIndex) throws SQLException {
    checkColumnIndex(columnIndex);
    Object x = getCurRow().getColData(columnIndex);
    if (_wasNull = (x == null)) {
      return 0;
    } else {
      int c;
      if (x instanceof Number)
         return ((Number)x).intValue();
      else if (x instanceof Boolean)
        return (((Boolean)x).booleanValue()? 1 : 0);
      else if (x instanceof String) {
        return (new BigDecimal(((String)x).toString())).intValue();
      }else
        throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'int'");
    }
  }
  public synchronized long getLong(int columnIndex) throws SQLException {
    checkColumnIndex(columnIndex);
    Object x = getCurRow().getColData(columnIndex);
    if (_wasNull = (x == null)) {
      return 0;
    } else {
      int c;
      if (x instanceof Number)
         return ((Number)x).longValue();
      else if (x instanceof Boolean)
        return (((Boolean)x).booleanValue()? 1L : 0L);
      else if (x instanceof String) {
        return (new BigDecimal(((String)x).toString())).longValue();
      }else
        throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'long'");
    }
  }
  public synchronized float getFloat(int columnIndex) throws SQLException {
    checkColumnIndex(columnIndex);
    Object x = getCurRow().getColData(columnIndex);
    if (_wasNull = (x == null)) {
      return 0;
    } else {
      int c;
      if (x instanceof Number)
         return ((Number)x).floatValue();
      else if (x instanceof Boolean)
        return (float)(((Boolean)x).booleanValue()? 1 : 0);
      else if (x instanceof String) {
        return Float.parseFloat(((String)x).toString().trim());
      }else
        throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'float'");
    }
  }
  public synchronized double getDouble(int columnIndex) throws SQLException {
    checkColumnIndex(columnIndex);
    Object x = getCurRow().getColData(columnIndex);
    if (_wasNull = (x == null)) {
      return 0;
    } else {
      int c;
      if (x instanceof Number)
         return ((Number)x).doubleValue();
      else if (x instanceof Boolean)
        return (double)(((Boolean)x).booleanValue()? 1 : 0);
      else if (x instanceof String) {
        return Double.parseDouble(((String)x).toString().trim());
      }else
        throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'double'");
    }
  }
  public synchronized BigDecimal getBigDecimal(int columnIndex) throws SQLException {
    checkColumnIndex(columnIndex);
    Object x = getCurRow().getColData(columnIndex);
    if (_wasNull = (x == null)) {
      return null;
    } else {
      if (x instanceof Boolean)
        return new BigDecimal((((Boolean)x).booleanValue()? 1L : 0L));
      else
        try {
            return new BigDecimal(x.toString().trim());
        } catch(NumberFormatException e) {
            throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'BigDecimal'");
        }
    }
  }
  public synchronized BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
    return getBigDecimal(columnIndex).setScale(scale);
  }
  public synchronized byte[] getBytes(int columnIndex) throws SQLException {
    checkColumnIndex(columnIndex);
    Object x = getCurRow().getColData(columnIndex);
    if (_wasNull = (x == null)) {
      return null;
    } else {
      if (x instanceof byte[])
        return (byte[])x;
      else if (x instanceof Blob)
        return ((Blob)x).getBytes(0L, (int)((Blob)x).length());
      else if (x instanceof Clob)
        return ((Clob)x).getSubString(0L, (int)((Clob)x).length()).getBytes();
      else if (x instanceof NClob)
        return ((NClob)x).getSubString(0L, (int)((NClob)x).length()).getBytes();
      else if (x instanceof String)
        return ((String)x).getBytes();
      else
         throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'byte[]'");
    }
  }
  public synchronized Date getDate(int columnIndex) throws SQLException {
    checkColumnIndex(columnIndex);
    Object x = getCurRow().getColData(columnIndex);
    if (_wasNull = (x == null)) {
      return null;
    } else {
      if (x instanceof Date) {
        return (Date)x;
      } else if (x instanceof Timestamp) {
        return new Date(((Timestamp)x).getTime());
      } else if (x instanceof String) {
        Date dt = _getDate((String)x);
        if (dt == null)
              throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'Date'");
        return dt;
      } else if (x instanceof Clob) {
        Date dt = _getDate(((Clob)x).getSubString(0L, (int)((Clob)x).length()));
        if (dt == null)
              throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'Date'");
        return dt;
      } else if (x instanceof NClob) {
        Date dt = _getDate(((NClob)x).getSubString(0L, (int)((NClob)x).length()));
        if (dt == null)
              throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'Date'");
        return dt;
       } else
         throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'Date'");
    }
  }
  public synchronized Time getTime(int columnIndex) throws SQLException {
    checkColumnIndex(columnIndex);
    Object x = getCurRow().getColData(columnIndex);
    if (_wasNull = (x == null)) {
      return null;
    } else {
      if (x instanceof Time) {
        return (Time)x;
      } else if (x instanceof Timestamp) {
        return new Time(((Timestamp)x).getTime());
      } else if (x instanceof String) {
        Time dt = _getTime((String)x);
        if (dt == null)
              throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'Time'");
        return dt;
      } else if (x instanceof Clob) {
        Time dt = _getTime(((Clob)x).getSubString(0L, (int)((Clob)x).length()));
        if (dt == null)
              throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'Time'");
        return dt;
      } else if (x instanceof NClob) {
        Time dt = _getTime(((NClob)x).getSubString(0L, (int)((NClob)x).length()));
        if (dt == null)
              throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'Time'");
        return dt;
       } else
         throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'Time'");
    }
  }
  public synchronized Timestamp getTimestamp(int columnIndex) throws SQLException {
    checkColumnIndex(columnIndex);
    Object x = getCurRow().getColData(columnIndex);
    if (_wasNull = (x == null)) {
      return null;
    } else {
      if (x instanceof Timestamp) {
        return (Timestamp)x;
      } else if (x instanceof Time) {
        return new Timestamp(((Time)x).getTime());
      } else if (x instanceof Date) {
        return new Timestamp(((Date)x).getTime());
      } else if (x instanceof String) {
        Timestamp dt = _getTimestamp((String)x);
        if (dt == null)
           throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'Timestamp'");
        return dt;
      } else if (x instanceof Clob) {
        Timestamp dt = _getTimestamp(((Clob)x).getSubString(0L, (int)((Clob)x).length()));
        if (dt == null)
           throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'Timestamp'");
        return dt;
      } else if (x instanceof NClob) {
        Timestamp dt = _getTimestamp(((NClob)x).getSubString(0L, (int)((NClob)x).length()));
        if (dt == null)
           throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'Timestamp'");
        return dt;
       } else
         throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'Timestamp'");
    }
  }
  public synchronized InputStream getAsciiStream(int columnIndex) throws SQLException {
    checkColumnIndex(columnIndex);
    Object x = getCurRow().getColData(columnIndex);
    closeInputStream();
    if (_wasNull = (x == null)) {
      return (objInputStream = null);
    } else {
      if (x instanceof String)
        return objInputStream = new ByteArrayInputStream(((String)x).getBytes());
      else if (x instanceof Clob)
        return objInputStream = ((Clob)x).getAsciiStream();
      else if (x instanceof NClob)
        return objInputStream = ((NClob)x).getAsciiStream();
      else
        throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'AsciiStream'");
    }
  }
  public synchronized InputStream getUnicodeStream(int columnIndex) throws SQLException {
    checkColumnIndex(columnIndex);
    Object x = getCurRow().getColData(columnIndex);
    closeInputStream();
    if (_wasNull = (x == null)) {
      return (objInputStream = null);
    } else {
      if (x instanceof String)
        return objInputStream = new ByteArrayInputStream(((String)x).getBytes());
      else if (x instanceof Clob)
        return objInputStream = ((Clob)x).getAsciiStream();
      else if (x instanceof NClob)
        return objInputStream = ((NClob)x).getAsciiStream();
      else
        throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'UnicodeStream'");
    }
  }
  public synchronized InputStream getBinaryStream(int columnIndex) throws SQLException {
    checkColumnIndex(columnIndex);
    Object x = getCurRow().getColData(columnIndex);
    closeInputStream();
    if (_wasNull = (x == null)) {
      return (objInputStream = null);
    } else {
      if (x instanceof byte[])
        return objInputStream = new ByteArrayInputStream(((byte[])x));
      else if (x instanceof String)
        return objInputStream = new ByteArrayInputStream(((String)x).getBytes());
      else if (x instanceof Blob)
        return objInputStream = ((Blob)x).getBinaryStream();
      else if (x instanceof Clob)
        return objInputStream = ((Clob)x).getAsciiStream();
      else if (x instanceof NClob)
        return objInputStream = ((NClob)x).getAsciiStream();
      else
        throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'BinaryStream'");
    }
  }
  public synchronized Object getObject(int columnIndex) throws SQLException {
    checkColumnIndex(columnIndex);
    Object x = getCurRow().getColData(columnIndex);
    InputStream retVal = null;
    if (_wasNull = (x == null)) {
      return null;
    } else {
      return x;
    }
  }
  public String getString(String columnName) throws SQLException {
    return getString(findColumn (columnName));
  }
  public boolean getBoolean(String columnName) throws SQLException {
    return getBoolean(findColumn (columnName));
  }
  public byte getByte(String columnName) throws SQLException {
    return getByte(findColumn (columnName));
  }
  public short getShort(String columnName) throws SQLException {
    return getShort(findColumn (columnName));
  }
  public int getInt(String columnName) throws SQLException {
    return getInt(findColumn (columnName));
  }
  public long getLong(String columnName) throws SQLException {
    return getLong(findColumn (columnName));
  }
  public float getFloat(String columnName) throws SQLException {
    return getFloat(findColumn (columnName));
  }
  public double getDouble(String columnName) throws SQLException {
    return getDouble(findColumn (columnName));
  }
  public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
    return getBigDecimal(findColumn (columnName), scale);
  }
  public byte[] getBytes(String columnName) throws SQLException {
    return getBytes(findColumn (columnName));
  }
  public Date getDate(String columnName) throws SQLException {
    return getDate(findColumn (columnName));
  }
  public Time getTime(String columnName) throws SQLException {
    return getTime(findColumn (columnName));
  }
  public Timestamp getTimestamp(String columnName) throws SQLException {
    return getTimestamp(findColumn (columnName));
  }
  public InputStream getAsciiStream(String columnName) throws SQLException {
    return getAsciiStream(findColumn (columnName));
  }
  public InputStream getUnicodeStream(String columnName) throws SQLException {
    return getUnicodeStream(findColumn (columnName));
  }
  public InputStream getBinaryStream(String columnName) throws SQLException {
    return getBinaryStream(findColumn (columnName));
  }
  public Object getObject(String columnName) throws SQLException {
    return getObject(findColumn (columnName));
  }
  public synchronized Reader getCharacterStream(int columnIndex) throws SQLException {
    checkColumnIndex(columnIndex);
    Object x = getCurRow().getColData(columnIndex);
    closeInputStream();
    if (_wasNull = (x == null)) {
      return (objReader = null);
    } else {
      if (x instanceof String)
        return objReader = new StringReader((String)x);
      else if (x instanceof Clob)
        return objReader = ((Clob)x).getCharacterStream();
      else if (x instanceof NClob)
        return objReader = ((NClob)x).getCharacterStream();
      else
        throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'CharacterStream'");
    }
  }
  public Reader getCharacterStream(String columnName) throws SQLException {
    return getCharacterStream(findColumn (columnName));
  }
  public BigDecimal getBigDecimal(String columnName) throws SQLException {
    return getBigDecimal(findColumn (columnName));
  }
  public synchronized void updateNull(int columnIndex) throws SQLException {
    Row r = this.getRowForUpdate(columnIndex, "'updateNull(...)'");
    r.setColData(columnIndex, null);
  }
  public synchronized void updateBoolean(int columnIndex, boolean x) throws SQLException {
    Row r = this.getRowForUpdate(columnIndex, "'updateBoolean(...)'");
    switch(rowSMD.getColumnType(columnIndex)) {
     case Types.BOOLEAN:
        r.setColData(columnIndex, new Boolean(x));
        break;
      case Types.BIT:
      case Types.TINYINT:
      case Types.SMALLINT:
      case Types.INTEGER:
      case Types.BIGINT:
      case Types.REAL:
      case Types.FLOAT:
      case Types.DOUBLE:
      case Types.DECIMAL:
      case Types.NUMERIC:
        r.setColData(columnIndex, new Integer((x ?1:0)));
        break;
      case Types.CHAR:
      case Types.VARCHAR:
      case Types.LONGVARCHAR:
     case Types.NCHAR:
     case Types.NVARCHAR:
     case Types.LONGNVARCHAR:
        r.setColData(columnIndex, String.valueOf(x));
        break;
      default:
          throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_set_XX_value_to_field, "'boolean'");
    }
  }
  public void updateByte(int columnIndex, byte x) throws SQLException {
    updateNumber(columnIndex, new Byte(x), "'byte'", "'updateByte(...)'");
  }
  public void updateShort(int columnIndex, short x) throws SQLException {
    updateNumber(columnIndex, new Short(x), "'short'", "'updateShort(...)'");
  }
  public void updateInt(int columnIndex, int x) throws SQLException {
    updateNumber(columnIndex, new Integer(x), "'int'", "'updateInt(...)'");
  }
  public void updateLong(int columnIndex, long x) throws SQLException {
    updateNumber(columnIndex, new Long(x), "'long'", "'updateLong(...)'");
  }
  public void updateFloat(int columnIndex, float x) throws SQLException {
    updateNumber(columnIndex, new Float(x), "'float'", "'updateFloat(...)'");
  }
  public void updateDouble(int columnIndex, double x) throws SQLException {
    updateNumber(columnIndex, new Double(x), "'double'", "'updateDouble(...)'");
  }
  public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
    if (x == null)
      updateNull(columnIndex);
    else
      updateNumber(columnIndex, x, "'BigDecimal'", "'updateBigDecimal(...)'");
  }
  public synchronized void updateString(int columnIndex, String x) throws SQLException {
    Row r = this.getRowForUpdate(columnIndex, "'updateString(...)'");
    if (x == null)
      updateNull(columnIndex);
    else
      switch(rowSMD.getColumnType(columnIndex)) {
      case Types.BOOLEAN:
        r.setColData(columnIndex, new Boolean(x));
        break;
      case Types.BIT:
      case Types.TINYINT:
      case Types.SMALLINT:
      case Types.INTEGER:
      case Types.BIGINT:
      case Types.REAL:
      case Types.FLOAT:
      case Types.DOUBLE:
      case Types.DECIMAL:
      case Types.NUMERIC:
      case Types.CHAR:
      case Types.VARCHAR:
      case Types.LONGVARCHAR:
      case Types.DATALINK:
      case Types.NCHAR:
      case Types.NVARCHAR:
      case Types.LONGNVARCHAR:
      case Types.NCLOB:
      case Types.BLOB:
      case Types.CLOB:
        r.setColData(columnIndex, x);
        break;
      case Types.BINARY:
      case Types.VARBINARY:
      case Types.LONGVARBINARY:
        r.setColData(columnIndex, HexString2Bin(x));
        break;
      case Types.TIME:
       {
        Time val = _getTime(x);
        if (val == null)
          throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_set_XX_value_to_field, "'String'");
        r.setColData(columnIndex, val);
        break;
       }
      case Types.TIMESTAMP:
       {
        Timestamp val = _getTimestamp(x);
        if (val == null)
          throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_set_XX_value_to_field, "'String'");
        r.setColData(columnIndex, val);
        break;
       }
      case Types.DATE:
       {
        Date val = _getDate(x);
        if (val == null)
          throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_set_XX_value_to_field, "'String'");
        r.setColData(columnIndex, val);
        break;
       }
      default:
          throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_set_XX_value_to_field, "'String'");
    }
  }
  public synchronized void updateBytes(int columnIndex, byte[] x) throws SQLException {
    Row r = this.getRowForUpdate(columnIndex, "'updateBytes(...)'");
    if (x == null)
      updateNull(columnIndex);
    else
      switch(rowSMD.getColumnType(columnIndex)) {
      case Types.CHAR:
      case Types.VARCHAR:
      case Types.LONGVARCHAR:
      case Types.CLOB:
      case Types.NCLOB:
      case Types.NCHAR:
      case Types.NVARCHAR:
      case Types.LONGNVARCHAR:
        r.setColData(columnIndex, Bin2Hex(x));
        break;
      case Types.BLOB:
        r.setColData(columnIndex, x);
        break;
      case Types.BINARY:
      case Types.VARBINARY:
      case Types.LONGVARBINARY:
        r.setColData(columnIndex, x);
        break;
      default:
          throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_set_XX_value_to_field, "'byte[]'");
    }
  }
  public synchronized void updateDate(int columnIndex, Date x) throws SQLException {
    Row r = this.getRowForUpdate(columnIndex, "'updateDate(...)'");
    if (x == null)
      updateNull(columnIndex);
    else
      switch(rowSMD.getColumnType(columnIndex)) {
      case Types.CHAR:
      case Types.VARCHAR:
      case Types.LONGVARCHAR:
      case Types.CLOB:
      case Types.NCLOB:
      case Types.NCHAR:
      case Types.NVARCHAR:
      case Types.LONGNVARCHAR:
        r.setColData(columnIndex, x.toString());
        break;
      case Types.DATE:
        r.setColData(columnIndex, x);
        break;
      case Types.TIMESTAMP:
        r.setColData(columnIndex, new Timestamp(x.getTime()));
        break;
      default:
          throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_set_XX_value_to_field, "'Date'");
    }
  }
  public synchronized void updateTime(int columnIndex, Time x) throws SQLException {
    Row r = this.getRowForUpdate(columnIndex, "'updateTime(...)'");
    if (x == null)
      updateNull(columnIndex);
    else
      switch(rowSMD.getColumnType(columnIndex)) {
      case Types.CHAR:
      case Types.VARCHAR:
      case Types.LONGVARCHAR:
      case Types.CLOB:
      case Types.NCLOB:
      case Types.NCHAR:
      case Types.NVARCHAR:
      case Types.LONGNVARCHAR:
        r.setColData(columnIndex, x.toString());
        break;
      case Types.TIME:
        r.setColData(columnIndex, x);
        break;
      case Types.TIMESTAMP:
        r.setColData(columnIndex, new Timestamp(x.getTime()));
        break;
      default:
          throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_set_XX_value_to_field, "'Time'");
    }
  }
  public synchronized void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
    Row r = this.getRowForUpdate(columnIndex, "'updateTimestamp(...)'");
    if (x == null)
      updateNull(columnIndex);
    else
      switch(rowSMD.getColumnType(columnIndex)) {
      case Types.CHAR:
      case Types.VARCHAR:
      case Types.LONGVARCHAR:
      case Types.CLOB:
      case Types.NCLOB:
      case Types.NCHAR:
      case Types.NVARCHAR:
      case Types.LONGNVARCHAR:
        r.setColData(columnIndex, x.toString());
        break;
      case Types.TIMESTAMP:
        r.setColData(columnIndex, x);
        break;
      case Types.DATE:
        r.setColData(columnIndex, new Date(x.getTime()));
        break;
      default:
          throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_set_XX_value_to_field, "'Timestamp'");
    }
  }
  public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
    if (x == null)
      updateNull(columnIndex);
    else
      try {
        byte[] buf = new byte[length];
        int count = 0;
        do {
          int n = x.read(buf, count, length - count);
          if (n <=0)
            break;
          count += n;
        } while (count < length);
        updateString(columnIndex, new String(buf, 0, count));
      } catch(IOException e) {
        throw OPLMessage_x.makeException(e);
      }
  }
  public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
    if (x == null)
      updateNull(columnIndex);
    else
      try {
        byte[] buf = new byte[length];
        int count = 0;
        do {
          int n = x.read(buf, count, length - count);
          if (n <=0)
            break;
          count += n;
        } while (count < length);
        updateBytes(columnIndex, buf);
      } catch(IOException e) {
        throw OPLMessage_x.makeException(e);
      }
  }
  public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
    if (x == null)
      updateNull(columnIndex);
    else
      try {
        char[] buf = new char[length];
        int count = 0;
        do {
          int n = x.read(buf, count, length - count);
          if (n <=0)
            break;
          count += n;
        } while (count < length);
        updateString(columnIndex, new String(buf, 0, count));
      } catch(IOException e) {
        throw OPLMessage_x.makeException(e);
      }
  }
  public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
    if (x == null)
      updateNull(columnIndex);
    else {
      if (x instanceof BigDecimal)
        ((BigDecimal)x).setScale(scale);
      synchronized(this) {
       Row r = this.getRowForUpdate(columnIndex, "'updateObject(...)'");
       r.setColData(columnIndex, x);
      }
    }
  }
  public synchronized void updateObject(int columnIndex, Object x) throws SQLException {
    if (x == null)
      updateNull(columnIndex);
    else
      synchronized(this) {
       Row r = this.getRowForUpdate(columnIndex, "'updateObject(...)'");
       r.setColData(columnIndex, x);
      }
  }
  public void updateNull(String columnName) throws SQLException {
    updateNull(findColumn(columnName));
  }
  public void updateBoolean(String columnName, boolean x) throws SQLException {
    updateBoolean(findColumn(columnName), x);
  }
  public void updateByte(String columnName, byte x) throws SQLException {
    updateByte(findColumn(columnName), x);
  }
  public void updateShort(String columnName, short x) throws SQLException {
    updateShort(findColumn(columnName), x);
  }
  public void updateInt(String columnName, int x) throws SQLException {
    updateInt(findColumn(columnName), x);
  }
  public void updateLong(String columnName, long x) throws SQLException {
    updateLong(findColumn(columnName), x);
  }
  public void updateFloat(String columnName, float x) throws SQLException {
    updateFloat(findColumn(columnName), x);
  }
  public void updateDouble(String columnName, double x) throws SQLException {
    updateDouble(findColumn(columnName), x);
  }
  public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
    updateBigDecimal(findColumn(columnName), x);
  }
  public void updateString(String columnName, String x) throws SQLException {
    updateString(findColumn(columnName), x);
  }
  public void updateBytes(String columnName, byte[] x) throws SQLException {
    updateBytes(findColumn(columnName), x);
  }
  public void updateDate(String columnName, Date x) throws SQLException {
    updateDate(findColumn(columnName), x);
  }
  public void updateTime(String columnName, Time x) throws SQLException {
    updateTime(findColumn(columnName), x);
  }
  public void updateTimestamp(String columnName, Timestamp x) throws SQLException {
    updateTimestamp(findColumn(columnName), x);
  }
  public void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException {
    updateAsciiStream(findColumn(columnName), x, length);
  }
  public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException {
    updateBinaryStream(findColumn(columnName), x, length);
  }
  public void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException {
    updateCharacterStream(findColumn(columnName), reader, length);
  }
  public void updateObject(String columnName, Object x, int scale) throws SQLException {
    updateObject(findColumn(columnName), x, scale);
  }
  public void updateObject(String columnName, Object x) throws SQLException {
    updateObject(findColumn(columnName), x);
  }
  public Statement getStatement() throws SQLException {
    return null;
  }
  public Object getObject(int colIndex, Map map) throws SQLException {
    return getObject(colIndex);
  }
  public synchronized Ref getRef(int colIndex) throws SQLException {
    checkColumnIndex(colIndex);
    Object x = getCurRow().getColData(colIndex);
    if (_wasNull = (x == null)) {
      return null;
    } else {
      if (x instanceof Ref)
        return (Ref)x;
      else
        throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'Ref'");
    }
  }
  public synchronized Blob getBlob(int colIndex) throws SQLException {
    checkColumnIndex(colIndex);
    Object x = getCurRow().getColData(colIndex);
    if (_wasNull = (x == null)) {
      return null;
    } else {
      if (x instanceof Blob)
        return (Blob)x;
      else if (x instanceof byte[])
        return new OPLHeapBlob((byte[])x);
      else if (x instanceof String)
        return new OPLHeapBlob(((String)x).getBytes());
      else
         throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'Blob'");
    }
  }
  public synchronized Clob getClob(int colIndex) throws SQLException {
    checkColumnIndex(colIndex);
    Object x = getCurRow().getColData(colIndex);
    if (_wasNull = (x == null)) {
      return null;
    } else {
      if (x instanceof Clob)
        return (Clob)x;
      else if (x instanceof byte[])
        return new OPLHeapClob(Bin2Hex((byte[])x));
      else
        return new OPLHeapClob(x.toString());
    }
  }
  public synchronized Array getArray(int colIndex) throws SQLException {
    checkColumnIndex(colIndex);
    Object x = getCurRow().getColData(colIndex);
    if (_wasNull = (x == null)) {
      return null;
    } else {
      if (x instanceof Array)
        return (Array)x;
      else
        throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'Array'");
    }
  }
  public Object getObject(String colName, Map map) throws SQLException {
    return getObject(findColumn (colName), map);
  }
  public Ref getRef(String colName) throws SQLException {
    return getRef(findColumn (colName));
  }
  public Blob getBlob(String colName) throws SQLException {
    return getBlob(findColumn (colName));
  }
  public Clob getClob(String colName) throws SQLException {
    return getClob(findColumn (colName));
  }
  public Array getArray(String colName) throws SQLException {
    return getArray(findColumn (colName));
  }
  public Date getDate(int columnIndex, Calendar cal) throws SQLException {
    Date dt = getDate(columnIndex);
    if (dt == null)
       return null;
    Calendar def_cal = Calendar.getInstance();
    def_cal.setTime(dt);
    cal.set(Calendar.YEAR, def_cal.get(Calendar.YEAR));
    cal.set(Calendar.MONTH, def_cal.get(Calendar.MONTH));
    cal.set(Calendar.DAY_OF_MONTH, def_cal.get(Calendar.DAY_OF_MONTH));
    return new Date(cal.getTime().getTime());
  }
  public Date getDate(String columnName, Calendar cal) throws SQLException {
    return getDate(findColumn (columnName), cal);
  }
  public Time getTime(int columnIndex, Calendar cal) throws SQLException {
    Time dt = getTime(columnIndex);
    if (dt == null)
       return null;
    Calendar def_cal = Calendar.getInstance();
    def_cal.setTime(dt);
    cal.set(Calendar.HOUR_OF_DAY, def_cal.get(Calendar.HOUR_OF_DAY));
    cal.set(Calendar.MINUTE, def_cal.get(Calendar.MINUTE));
    cal.set(Calendar.SECOND, def_cal.get(Calendar.SECOND));
    return new Time(cal.getTime().getTime());
  }
  public Time getTime(String columnName, Calendar cal) throws SQLException {
    return getTime(findColumn (columnName), cal);
  }
  public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
    Timestamp dt = getTimestamp(columnIndex);
    if (dt == null)
       return null;
    Calendar def_cal = Calendar.getInstance();
    def_cal.setTime(dt);
    cal.set(Calendar.YEAR, def_cal.get(Calendar.YEAR));
    cal.set(Calendar.MONTH, def_cal.get(Calendar.MONTH));
    cal.set(Calendar.DAY_OF_MONTH, def_cal.get(Calendar.DAY_OF_MONTH));
    cal.set(Calendar.HOUR_OF_DAY, def_cal.get(Calendar.HOUR_OF_DAY));
    cal.set(Calendar.MINUTE, def_cal.get(Calendar.MINUTE));
    cal.set(Calendar.SECOND, def_cal.get(Calendar.SECOND));
    Timestamp ts = new Timestamp(cal.getTime().getTime());
    ts.setNanos(dt.getNanos());
    return ts;
  }
  public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
    return getTimestamp(findColumn (columnName), cal);
  }
  public synchronized java.net.URL getURL(int columnIndex)
          throws SQLException
  {
    checkColumnIndex(columnIndex);
    Object x = getCurRow().getColData(columnIndex);
    if (_wasNull = (x == null)) {
      return null;
    } else {
      if (x instanceof java.net.URL)
        return (java.net.URL)x;
      else if (x instanceof String)
        try {
          return new java.net.URL((String)x);
        } catch(java.net.MalformedURLException e) {
          throw OPLMessage_x.makeException(e);
        }
      else
        throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'URL'");
    }
  }
  public java.net.URL getURL(String columnName)
          throws SQLException
  {
    return getURL(findColumn (columnName));
  }
  public void updateRef(int columnIndex, java.sql.Ref x) throws SQLException {
    if (x == null)
      updateNull(columnIndex);
    else
      synchronized(this) {
       Row r = this.getRowForUpdate(columnIndex, "'updateRef(...)'");
       r.setColData(columnIndex, x);
      }
  }
  public void updateRef(String columnName, java.sql.Ref x) throws SQLException {
    updateRef (findColumn (columnName), x);
  }
  public void updateBlob(int columnIndex, java.sql.Blob x) throws SQLException {
    if (x == null)
      updateNull(columnIndex);
    else
      synchronized(this) {
       Row r = this.getRowForUpdate(columnIndex, "'updateBlob(...)'");
       x = new OPLHeapBlob(((Blob)x).getBytes(0L, (int)((Blob)x).length()));
       r.setColData(columnIndex, x);
      }
  }
  public void updateBlob(String columnName, java.sql.Blob x) throws SQLException {
    updateBlob (findColumn (columnName), x);
  }
  public void updateClob(int columnIndex, java.sql.Clob x) throws SQLException {
    if (x == null)
      updateNull(columnIndex);
    else
      synchronized(this) {
       Row r = this.getRowForUpdate(columnIndex, "'updateClob(...)'");
       x = new OPLHeapClob(((Clob)x).getSubString(0L, (int)((Clob)x).length()));
       r.setColData(columnIndex, x);
      }
  }
  public void updateClob(String columnName, java.sql.Clob x) throws SQLException {
    updateClob (findColumn (columnName), x);
  }
  public void updateArray(int columnIndex, java.sql.Array x) throws SQLException {
    if (x == null)
      updateNull(columnIndex);
    else
      synchronized(this) {
       Row r = this.getRowForUpdate(columnIndex, "'updateArray(...)'");
       r.setColData(columnIndex, x);
      }
  }
  public void updateArray(String columnName, java.sql.Array x) throws SQLException {
    updateArray (findColumn (columnName), x);
  }
  public synchronized RowId getRowId(int columnIndex) throws SQLException
  {
    checkColumnIndex(columnIndex);
    Object x = getCurRow().getColData(columnIndex);
    if (_wasNull = (x == null)) {
      return null;
    } else {
      if (x instanceof RowId)
        return (RowId)x;
      else
        throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'RowId'");
    }
  }
  public RowId getRowId(String columnLabel) throws SQLException
  {
    return getRowId(findColumn (columnLabel));
  }
  public void updateRowId(int columnIndex, RowId x) throws SQLException
  {
    if (x == null)
      updateNull(columnIndex);
    else
      synchronized(this) {
       Row r = this.getRowForUpdate(columnIndex, "'updateRowId(...)'");
       r.setColData(columnIndex, x);
      }
  }
  public void updateRowId(String columnLabel, RowId x) throws SQLException
  {
    updateRowId (findColumn (columnLabel), x);
  }
  public int getHoldability() throws SQLException
  {
    return ResultSet.HOLD_CURSORS_OVER_COMMIT;
  }
  public boolean isClosed() throws SQLException
  {
    return (conn == null ? true : false);
  }
  public synchronized void updateNString(int columnIndex, String nString) throws SQLException
  {
    updateString (columnIndex, nString);
  }
  public void updateNString(String columnLabel, String nString) throws SQLException
  {
    updateNString (findColumn (columnLabel), nString);
  }
  public synchronized void updateNClob(int columnIndex, NClob x) throws SQLException
  {
    if (x == null)
      updateNull(columnIndex);
    else
      synchronized(this) {
       Row r = this.getRowForUpdate(columnIndex, "'updateNClob(...)'");
       x = new OPLHeapNClob(((NClob)x).getSubString(0L, (int)((NClob)x).length()));
       r.setColData(columnIndex, x);
      }
  }
  public void updateNClob(String columnLabel, NClob nClob) throws SQLException
  {
    updateNClob (findColumn (columnLabel), nClob);
  }
  public synchronized NClob getNClob(int columnIndex) throws SQLException
  {
    checkColumnIndex(columnIndex);
    Object x = getCurRow().getColData(columnIndex);
    if (_wasNull = (x == null)) {
      return null;
    } else {
      if (x instanceof NClob)
        return (NClob)x;
      else if (x instanceof byte[])
        return new OPLHeapNClob(Bin2Hex((byte[])x));
      else
        return new OPLHeapNClob(x.toString());
    }
  }
  public NClob getNClob(String columnLabel) throws SQLException
  {
    return getNClob(findColumn (columnLabel));
  }
  public SQLXML getSQLXML(int columnIndex) throws SQLException
  {
    checkColumnIndex(columnIndex);
    Object x = getCurRow().getColData(columnIndex);
    if (_wasNull = (x == null)) {
      return null;
    } else {
      if (x instanceof SQLXML)
        return (SQLXML)x;
      else
        throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'SQLXML'");
    }
  }
  public SQLXML getSQLXML(String columnLabel) throws SQLException
  {
    return getSQLXML(findColumn (columnLabel));
  }
  public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException
  {
    if (xmlObject == null)
      updateNull(columnIndex);
    else
      synchronized(this) {
       Row r = this.getRowForUpdate(columnIndex, "'updateSQLXML(...)'");
       r.setColData(columnIndex, xmlObject);
      }
  }
  public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException
  {
    updateSQLXML (findColumn (columnLabel), xmlObject);
  }
  public synchronized String getNString(int columnIndex) throws SQLException
  {
    return getString(columnIndex);
  }
  public String getNString(String columnLabel) throws SQLException
  {
    return getNString(findColumn (columnLabel));
  }
  public synchronized java.io.Reader getNCharacterStream(int columnIndex) throws SQLException
  {
    checkColumnIndex(columnIndex);
    Object x = getCurRow().getColData(columnIndex);
    closeInputStream();
    if (_wasNull = (x == null)) {
      return (objReader = null);
    } else {
      if (x instanceof String)
        return objReader = new StringReader((String)x);
      else if (x instanceof Clob)
        return objReader = ((Clob)x).getCharacterStream();
      else if (x instanceof NClob)
        return objReader = ((NClob)x).getCharacterStream();
      else
        throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_convert_parameter_to_XX, "'NCharacterStream'");
    }
  }
  public java.io.Reader getNCharacterStream(String columnLabel) throws SQLException
  {
    return getNCharacterStream (findColumn (columnLabel));
  }
  public synchronized void updateNCharacterStream(int columnIndex,
        java.io.Reader x,
       long length) throws SQLException
  {
    updateCharacterStream(columnIndex, x, (int)length);
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
    updateBinaryStream(columnIndex, x, (int)length);
  }
  public synchronized void updateCharacterStream(int columnIndex,
        java.io.Reader x,
        long length) throws SQLException
  {
    updateCharacterStream(columnIndex, x, (int)length);
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
    updateBinaryStream(columnIndex, inputStream, (int)length);
  }
  public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException
  {
    updateBlob (findColumn (columnLabel), inputStream, length);
  }
  public synchronized void updateClob(int columnIndex, Reader reader, long length) throws SQLException
  {
    updateCharacterStream(columnIndex, reader, (int)length);
  }
  public void updateClob(String columnLabel, Reader reader, long length) throws SQLException
  {
    updateClob (findColumn (columnLabel), reader, length);
  }
  public synchronized void updateNClob(int columnIndex, Reader reader, long length) throws SQLException
  {
    updateNCharacterStream(columnIndex, reader, (int)length);
  }
  public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException
  {
    updateNClob (findColumn (columnLabel), reader, length);
  }
  public void updateNCharacterStream(int columnIndex, java.io.Reader x) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "updateNCharacterStream(columnIndex, x)");
  }
  public void updateNCharacterStream(String columnLabel, java.io.Reader reader) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "updateNCharacterStream(columnLabel, reader)");
  }
  public void updateAsciiStream(int columnIndex, java.io.InputStream x) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "updateAsciiStream(columnIndex, x)");
  }
  public void updateBinaryStream(int columnIndex, java.io.InputStream x) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "updateBinaryStream(columnIndex, x)");
  }
  public void updateCharacterStream(int columnIndex, java.io.Reader x) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "updateCharacterStream(columnIndex, x)");
  }
  public void updateAsciiStream(String columnLabel, java.io.InputStream x) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "updateAsciiStream(columnLabel, x)");
  }
  public void updateBinaryStream(String columnLabel, java.io.InputStream x) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "updateBinaryStream(columnLabel, x)");
  }
  public void updateCharacterStream(String columnLabel, java.io.Reader reader) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "updateCharacterStream(columnLabel, reader)");
  }
  public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "updateBlob(columnIndex, inputStream)");
  }
  public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "updateBlob(columnLabel, inputStream)");
  }
  public void updateClob(int columnIndex, Reader reader) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "updateClob(columnIndex,  reader)");
  }
  public void updateClob(String columnLabel, Reader reader) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "updateClob(columnLabel,  reader)");
  }
  public void updateNClob(int columnIndex, Reader reader) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "updateNClob(columnIndex,  reader)");
  }
  public void updateNClob(String columnLabel, Reader reader) throws SQLException
  {
    throw OPLMessage_x.makeFExceptionV(OPLMessage_x.errx_Method_XX_not_yet_implemented, "updateNClob(columnLabel,  reader)");
  }
  public <T> T unwrap(java.lang.Class<T> iface) throws java.sql.SQLException
  {
    try {
      return iface.cast(this);
    } catch (ClassCastException cce) {
      throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Unable_to_unwrap_to_XX, iface.toString());
    }
  }
  public boolean isWrapperFor(java.lang.Class<?> iface) throws java.sql.SQLException
  {
    return iface.isInstance(this);
  }
  private Row getCurRow() {
    if (onInsertRow)
      return updateRow;
    else
      return (Row)rowsData.get(curRow);
  }
  private void check_pos(String s) throws SQLException {
    if (isAfterLast() || isBeforeFirst())
        throw OPLMessage_x.makeException(OPLMessage_x.errx_Invalid_cursor_position);
  }
  private void check_move(String s, boolean isNext) throws SQLException
  {
    if (onInsertRow)
      throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_call_XX_when_the_cursor_on_the_insert_row, s);
    if (!isNext && getType() == ResultSet.TYPE_FORWARD_ONLY)
      throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_call_XX_on_a_TYPE_FORWARD_ONLY_result_set, s);
  }
  private void check_InsertMode(String s) throws SQLException
  {
    if (onInsertRow)
      throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_call_XX_when_the_cursor_on_the_insert_row, s);
  }
  private void closeInputStream()
  {
    if(objInputStream != null) {
      try {
        objInputStream.close();
      } catch(Exception _ex) {
      }
      objInputStream = null;
    }
    if(objReader != null) {
      try {
        objReader.close();
      } catch(Exception _ex) {
      }
      objReader = null;
    }
  }
  private void check_Update(String s) throws SQLException
  {
    if (getConcurrency() == ResultSet.CONCUR_READ_ONLY)
      throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_call_XX_on_a_CONCUR_READ_ONLY_result_set, s);
  }
  private int checkColumnIndex(int columnIndex) throws SQLException
  {
    if (rowSMD == null || ((curState == NOROWS || countRows == 0) && !onInsertRow) )
       throw OPLMessage_x.makeException(OPLMessage_x.errx_No_row_is_currently_available);
    if(!onInsertRow && (isAfterLast() || isBeforeFirst()))
        throw OPLMessage_x.makeException(OPLMessage_x.errx_Invalid_cursor_position);
    if (columnIndex < 1 || columnIndex > rowSMD.getColumnCount())
        throw OPLMessage_x.makeException(OPLMessage_x.errx_Column_Index_out_of_range);
    return columnIndex;
  }
  private void cancelUpdates() {
    if (updateRow != null)
       updateRow.clear();
    updateRow = null;
  }
  private Row getRowForUpdate(int columnIndex, String cmd) throws SQLException {
    check_Update(cmd);
    checkColumnIndex(columnIndex);
    if (updateRow == null) {
        updateRow = new Row(rowSMD.getColumnCount());
    }
    return updateRow;
  }
  private synchronized void updateNumber(int columnIndex, Number val, String typeName, String funcName)
      throws SQLException
  {
    Row r = this.getRowForUpdate(columnIndex, funcName);
    switch(rowSMD.getColumnType(columnIndex)) {
      case Types.BOOLEAN:
        r.setColData(columnIndex, new Boolean((val.intValue()!=0? true:false)));
        break;
      case Types.BIT:
      case Types.TINYINT:
      case Types.SMALLINT:
      case Types.INTEGER:
      case Types.BIGINT:
      case Types.REAL:
      case Types.FLOAT:
      case Types.DOUBLE:
      case Types.DECIMAL:
      case Types.NUMERIC:
        r.setColData(columnIndex, val);
        break;
      case Types.CHAR:
      case Types.VARCHAR:
      case Types.LONGVARCHAR:
      case Types.NCHAR:
      case Types.NVARCHAR:
      case Types.LONGNVARCHAR:
        r.setColData(columnIndex, val.toString());
        break;
      default:
          throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Could_not_set_XX_value_to_field, typeName);
    }
  }
  private boolean _next() throws SQLException {
    if (countRows == 0) {
      curState = NOROWS;
      return false;
    }
    if (curRow >= countRows) {
      curState = AFTERLAST;
      return false;
    }
    boolean ret = false;
    while(!ret) {
      curRow++;
      if (curRow < countRows)
        ret = true;
      else
        break;
      if (!showDeleted && ((Row)rowsData.get(curRow)).isDeleted)
         ret = false;
    }
    if (ret) {
       absolutePos++;
       if (curState == BEFOREFIRST) {
          curState = FIRSTROW;
       } else {
          curState = BODYROW;
          if (curRow == countRows - 1) {
            curState = LASTROW;
          } else {
            boolean found = false;
            int i = curRow;
            while (!found) {
              i++;
              if (i < countRows)
                found = true;
              else
                break;
              if (!showDeleted && ((Row)rowsData.get(i)).isDeleted)
                 found = false;
            }
            if (!found)
              curState = LASTROW;
          }
       }
    } else {
       if (curState == LASTROW)
          absolutePos++;
       curState = AFTERLAST;
    }
    return ret;
  }
  private boolean _previous() throws SQLException {
    if (countRows == 0) {
      curState = NOROWS;
      return false;
    }
    if (curRow < 0) {
      curState = BEFOREFIRST;
      return false;
    }
    boolean ret = false;
    while(!ret) {
      curRow--;
      if (curRow >= 0)
        ret = true;
      else
        break;
      if (!showDeleted && ((Row)rowsData.get(curRow)).isDeleted)
         ret = false;
    }
    if (ret) {
       absolutePos--;
       if (curState == AFTERLAST) {
          curState = LASTROW;
       } else {
          curState = BODYROW;
          if (curRow == 0) {
            curState = FIRSTROW;
          } else {
            boolean found = false;
            int i = curRow;
            while (!found) {
              i--;
              if (i >= 0)
                found = true;
              else
                break;
              if (!showDeleted && ((Row)rowsData.get(i)).isDeleted)
                 found = false;
            }
            if (!found)
              curState = FIRSTROW;
          }
       }
    } else {
       if (curState == FIRSTROW)
          absolutePos--;
       curState = BEFOREFIRST;
    }
    return ret;
  }
  private boolean _first() throws SQLException {
    _beforeFirst();
    return _next();
  }
  private boolean _last() throws SQLException {
    _afterLast();
    return _previous();
  }
  private void _afterLast() throws SQLException {
    if (countRows == 0) {
      curState = NOROWS;
    } else {
      curRow = countRows;
      absolutePos = countRows - (showDeleted ? 0 : countDeleted) + 1;
      curState = AFTERLAST;
    }
  }
  private void _beforeFirst() throws SQLException {
    if (countRows == 0) {
      curState = NOROWS;
    } else {
      curRow = -1;
      absolutePos = 0;
      curState = BEFOREFIRST;
    }
  }
  private byte[] HexString2Bin (String str)
    throws SQLException
  {
    if (str == null)
      return null;
    int slen = (str.length() / 2) * 2;
    byte[] bdata = new byte[slen / 2];
    int c1, c0, i, j;
    for (i = 0, j = 0 ; i < slen; i += 2, j++)
    {
      c1 = Character.digit(str.charAt(i), 16);
      c0 = Character.digit(str.charAt(i + 1), 16);
      if ( c1 == -1 || c0 == -1)
         throw OPLMessage_x.makeException(OPLMessage_x.errx_Invalid_hex_number);
      bdata[j] = (byte) (c1 * 16 + c0);
    }
    return bdata;
  }
  private String Bin2Hex(byte[] bdata)
  {
    if (bdata == null)
       return null;
    String hex = "0123456789ABCDEF";
    StringBuffer hstr = new StringBuffer(bdata.length * 2);
    byte val;
    for (int i = 0; i < bdata.length; i++) {
      val = bdata[i];
      hstr.append(hex.charAt(val >>> 4 & 0x0F));
      hstr.append(hex.charAt(val & 0xF));
    }
    return hstr.toString();
  }
  private java.sql.Date _getDate (String s)
  {
    java.sql.Date dt = null;
    if (s == null)
       return null;
    try {
 dt = java.sql.Date.valueOf (s);
    } catch (Exception e) {
    }
    if (dt == null)
      {
 try {
     java.text.DateFormat df = java.text.DateFormat.getDateInstance();
     java.util.Date juD = df.parse (s);
     dt = new java.sql.Date (juD.getTime());
        } catch (Exception e) {
        }
      }
    return dt;
  }
  private java.sql.Timestamp _getTimestamp (String s)
  {
    java.sql.Timestamp ts = null;
    if (s == null)
      return null;
    try {
 ts = java.sql.Timestamp.valueOf (s);
    } catch (Exception e) {
    }
    if (ts == null)
      {
 try {
     java.text.DateFormat df = java.text.DateFormat.getDateInstance();
     java.util.Date juD = df.parse (s);
     ts = new java.sql.Timestamp (juD.getTime());
        } catch (Exception e) {
        }
      }
    return ts;
  }
  private java.sql.Time _getTime (String s)
  {
    java.sql.Time tm = null;
    if (s == null)
       return null;
    try {
 tm = java.sql.Time.valueOf (s);
    } catch (Exception e) {
    }
    if (tm == null)
      {
 try {
     java.text.DateFormat df = java.text.DateFormat.getTimeInstance();
     java.util.Date juD = df.parse(s);
     tm = new java.sql.Time(juD.getTime());
        } catch (Exception e) {
        }
      }
    return tm;
  }
  protected class Row implements Serializable, Cloneable {
    private Object[] origData;
    private Object[] curData;
    private BitSet colUpdated;
    private int cols;
    protected boolean isDeleted;
    protected boolean isUpdated;
    protected boolean isInserted;
    private Row(int count) {
      origData = new Object[count];
      curData = new Object[count];
      colUpdated = new BitSet(count);
      cols = count;
    }
    private Row(Object[] data) {
      cols = data.length;
      origData = new Object[cols];
      curData = new Object[cols];
      colUpdated = new BitSet(cols);
      for(int i = 0; i < cols; i++)
        origData[i] = data[i];
    }
    private void clear() {
      for(int i = 0; i < cols; i++) {
        origData[i] = null;
        curData[i] = null;
        colUpdated.clear(i);
      }
      cols = 0;
    }
    private void setOrigColData(int col, Object data) {
      origData[col - 1] = data;
    }
    private boolean isColUpdated(int col) {
      return colUpdated.get(col - 1);
    }
    private Object getColData(int col) {
      col--;
      if (colUpdated.get(col))
        return curData[col];
      else
        return origData[col];
    }
    private void setColData(int col, Object data) {
      col--;
      colUpdated.set(col);
      curData[col] = data;
    }
    private Object[] getOrigData() {
      return origData;
    }
    private Object[] getCurData() {
      return curData;
    }
    private BitSet getListUpdatedCols() {
      return colUpdated;
    }
    private void update(Object[] data, BitSet changedCols) {
      if (data.length != cols)
        throw new IllegalArgumentException();
      isUpdated = true;
      for (int i = 0; i < cols; i++)
        if (changedCols.get(i)) {
          colUpdated.set(i);
          curData[i] = data[i];
        }
    }
    private void clearUpdated() {
      isUpdated = false;
      for(int i = 0; i < cols; i++) {
        curData[i] = null;
        colUpdated.clear(i);
      }
    }
    private boolean isCompleted() throws SQLException {
      if (rowSMD == null)
        return false;
      for(int i = 0; i < cols; i++) {
         if(!colUpdated.get(i) && rowSMD.isNullable(i + 1) == 0)
            return false;
      }
      return true;
    }
    private void moveCurToOrig() {
      for(int i = 0; i < cols; i++)
        if( colUpdated.get(i)) {
            origData[i] = curData[i];
            colUpdated.clear(i);
            curData[i] = null;
          }
      isUpdated = false;
      isInserted = false;
    }
  }
  private class Scanner {
      int pos;
      int end;
      char[] query;
      final static String blankChars = " \t\n\r\f";
      final static String symb = "_-$#";
      HashMap<String,Integer> keywords = new HashMap<String,Integer>();
      Token tok = null;
    private Scanner(String sql) {
      pos = 0;
      query = sql.toCharArray();
      end = query.length - 1;
      keywords.put("SELECT", new Integer(Token.T_SELECT));
      keywords.put("FROM", new Integer(Token.T_FROM));
      keywords.put("WHERE", new Integer(Token.T_WHERE));
      keywords.put("ORDER", new Integer(Token.T_ORDER));
      keywords.put("BY", new Integer(Token.T_BY));
      keywords.put("GROUP", new Integer(Token.T_GROUP));
      keywords.put("UNION", new Integer(Token.T_UNION));
      keywords.put("HAVING", new Integer(Token.T_HAVING));
    }
    private String check_Select() {
      String tableName = null;
      if ((tok = nextToken()) == null || tok.type != Token.T_SELECT)
        return null;
      while((tok = nextToken()) != null) {
        if (tok.type == Token.T_FROM)
          break;
      }
      if ((tableName = table_name()) == null)
        return null;
      if (tok == null)
        return tableName;
      if (tok.type == Token.T_STRING) {
          if ((tok = nextToken()) == null)
             return tableName;
      }
      if (tok.type == Token.T_WHERE) {
        while((tok = nextToken()) != null) {
          if (tok.type == Token.T_GROUP || tok.type == Token.T_HAVING)
            break;
        }
      } else {
        return null;
      }
      if (tok != null)
        return null;
      else
        return tableName;
    }
    private String table_name() {
      int state = 0;
      StringBuffer table = new StringBuffer();
      while((tok = nextToken()) != null) {
        switch (state) {
          case 0:
              if (tok.type == Token.T_STRING) {
                state = 1;
                table.append(new String(query, tok.start, tok.length));
              } else
                return null;
              break;
          case 1:
              switch(tok.type) {
                case Token.T_DOT:
                      table.append('.');
                      state = 2;
                      break;
                case Token.T_DELIM:
                      table.append('@');
                      state = 3;
                      break;
                case Token.T_COLON:
                      table.append(':');
                      state = 4;
                      break;
                default:
                      return table.toString();
              }
              break;
          case 2:
              switch(tok.type) {
                case Token.T_STRING:
                      table.append(new String(query, tok.start, tok.length));
                      state = 5;
                      break;
                case Token.T_DOT:
                      table.append('.');
                      state = 6;
                      break;
               default:
                      return null;
              }
              break;
          case 3:
              if (tok.type == Token.T_STRING) {
                 table.append(new String(query, tok.start, tok.length));
                 state = 99;
              } else {
                 return null;
              }
              break;
          case 4:
              if (tok.type == Token.T_STRING) {
                 table.append(new String(query, tok.start, tok.length));
                 state = 7;
              } else {
                 return null;
              }
              break;
          case 5:
              switch(tok.type) {
                case Token.T_DOT:
                      table.append('.');
                      state = 8;
                      break;
                case Token.T_DELIM:
                      table.append('@');
                      state = 9;
                      break;
                default:
                      return table.toString();
              }
              break;
          case 6:
              if (tok.type == Token.T_STRING) {
                 table.append(new String(query, tok.start, tok.length));
                 state = 99;
              } else {
                 return null;
              }
              break;
          case 7:
              if (tok.type == Token.T_DOT) {
                 table.append('.');
                 state = 10;
              } else {
                 return table.toString();
              }
              break;
          case 8:
              if (tok.type == Token.T_STRING) {
                 table.append(new String(query, tok.start, tok.length));
                 state = 99;
              } else {
                 return null;
              }
              break;
          case 9:
              if (tok.type == Token.T_STRING) {
                 table.append(new String(query, tok.start, tok.length));
                 state = 99;
              } else {
                 return null;
              }
              break;
          case 10:
              if (tok.type == Token.T_STRING) {
                 table.append(new String(query, tok.start, tok.length));
                 state = 99;
              } else {
                return null;
              }
              break;
          case 99:
              return table.toString();
          default:
              return null;
        }
      }
      if (state == 1 || state == 5 || state == 7 || state == 99)
        return table.toString();
      else
        return null;
    }
    private Token nextToken() {
      int start;
      while (pos <= end) {
        while(pos <= end && isBlank(query[pos])) pos++;
        if (pos > end)
          return null;
        switch(query[pos++]) {
          case '.': return new Token(Token.T_DOT);
          case ':': return new Token(Token.T_COLON);
          case '@': return new Token(Token.T_DELIM);
          case ',': return new Token(Token.T_COMMA);
          case '\'':
          case '\"':
              {
                char ch = query[pos - 1];
                start = pos - 1;
                if (pos <= end && (query[pos] == '_' || Character.isLetterOrDigit(query[pos]))) {
                   while(pos <= end && isLetterOrDigit(query[pos])) pos++;
                   if (pos > end || (pos <= end && query[pos] != ch)) {
                      return new Token(Token.T_ERROR);
                   } else {
                      pos++;
                      return new Token(Token.T_STRING, start, pos - 1, true);
                   }
                } else
                   return new Token(Token.T_ERROR);
              }
          default:
             start = pos - 1;
             if (pos <= end && (query[pos] == '_' || Character.isLetterOrDigit(query[pos]))) {
                while(pos <= end && isLetterOrDigit(query[pos])) pos++;
                Object tok_type = keywords.get(new String(query, start, pos - start).toUpperCase());
                if (tok_type != null)
                   return new Token(((Integer)tok_type).intValue(), start, pos - 1);
                else
                   return new Token(Token.T_STRING, start, pos - 1);
             } else
                return new Token(query[start]);
        }
      }
      return null;
    }
    private boolean isBlank(int ch) {
      return (blankChars.indexOf(ch) != -1);
    }
    private boolean isLetterOrDigit(int ch) {
      return (Character.isLetterOrDigit((char)ch) || symb.indexOf(ch) != -1);
    }
    private class Token {
        final static int T_ERROR = -1;
        final static int T_CHAR = 0;
        final static int T_DOT = 1;
        final static int T_COLON = 2;
        final static int T_DELIM = 3;
        final static int T_COMMA = 4;
        final static int T_STRING = 5;
        final static int T_SELECT = 6;
        final static int T_FROM = 7;
        final static int T_WHERE = 8;
        final static int T_ORDER = 9;
        final static int T_BY = 10;
        final static int T_GROUP = 11;
        final static int T_UNION = 12;
        final static int T_HAVING = 13;
        private int type;
        private int start;
        private int end;
        private int length;
        private boolean quoted;
        private char symbol;
        private Token(int _type, int _start, int _end) {
          type = _type;
          start = _start;
          end = _end;
          length = end - start + 1;
        }
        private Token(int _type, int _start, int _end, boolean _quoted) {
          this(_type, _start, _end);
          quoted = _quoted;
        }
        private Token(int _type) {
          type = _type;
        }
        private Token(char _symbol) {
          type = T_CHAR;
          symbol = _symbol;
        }
    }
  }
  private class RowSetReader implements Serializable, Cloneable {
    private transient Connection conn;
    protected Connection connect(RowSet rowSet) throws SQLException {
      String connName;
      if ((connName = rowSet.getDataSourceName()) != null)
        try {
          InitialContext initialcontext = new InitialContext();
          DataSource ds = (DataSource)initialcontext.lookup(connName);
          return ds.getConnection(rowSet.getUsername(), rowSet.getPassword());
        } catch(NamingException e) {
          throw OPLMessage_x.makeException(e);
        }
      else if((connName = rowSet.getUrl()) != null)
        return DriverManager.getConnection(connName, rowSet.getUsername(), rowSet.getPassword());
      else
        return null;
      }
    private void setParams(PreparedStatement pstmt, Object[] params) throws SQLException {
      if (params == null)
        return;
      for(int i = 0; i < params.length; i++) {
        BaseRowSet.Parameter par = (BaseRowSet.Parameter)params[i];
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
    private void readData(RowSetInternal x) throws java.sql.SQLException {
      boolean doDisconnect = false;
      close();
      try {
        OPLCachedRowSet crs = (OPLCachedRowSet)x;
        crs.release();
        conn = x.getConnection();
        if (conn == null) {
          conn = connect(crs);
          doDisconnect = true;
        }
        if (conn == null || crs.getCommand() == null)
          throw OPLMessage_x.makeException(OPLMessage_x.errx_SQL_query_is_undefined);
        try {
          conn.setTransactionIsolation(crs.getTransactionIsolation());
        } catch(Exception e) { }
        PreparedStatement pstmt = conn.prepareStatement(crs.getCommand(),
          crs.getType(), crs.getConcurrency());
        setParams(pstmt, x.getParams());
        try {
          pstmt.setMaxRows(crs.getMaxRows());
          pstmt.setMaxFieldSize(crs.getMaxFieldSize());
          pstmt.setEscapeProcessing(crs.getEscapeProcessing());
          pstmt.setQueryTimeout(crs.getQueryTimeout());
        } catch(Exception e) { }
        ResultSet rs = pstmt.executeQuery();
        crs.populate(rs);
        rs.close();
        pstmt.close();
        try {
          conn.commit();
        } catch(SQLException e) { }
      } finally {
        if (conn != null && doDisconnect)
          conn.close();
        else
          conn = null;
      }
    }
    private void close() throws SQLException {
      if (conn != null)
        conn.close();
      conn = null;
    }
  }
  private class RowSetWriter implements Serializable, Cloneable {
    private transient Connection conn;
    private String updateSQL;
    private String deleteSQL;
    private String insertSQL;
    private int[] keyCols;
    private ResultSetMetaData rsmd;
    private int colCount;
    private LinkedList<Object> params = new LinkedList<Object>();
    private boolean writeData(RowSetInternal x) throws java.sql.SQLException {
      OPLCachedRowSet crs = (OPLCachedRowSet)x;
      boolean showDel = false;
      boolean conflict = false;
      conn = crs.rowSetReader.connect(crs);
      if (conn == null)
        throw OPLMessage_x.makeException(OPLMessage_x.errx_Unable_to_get_a_Connection);
      if (conn.getAutoCommit())
        conn.setAutoCommit(false);
      conn.setTransactionIsolation(crs.getTransactionIsolation());
      initializeStmts(crs);
      showDel = crs.getShowDeleted();
      crs.setShowDeleted(true);
      try {
        crs.beforeFirst();
        while(!conflict && crs.next()) {
          if (crs.rowDeleted() && !crs.rowInserted())
            conflict = doDelete(crs);
        }
        crs.beforeFirst();
        while(!conflict && crs.next()) {
          if (crs.rowUpdated() && !crs.rowDeleted() && !crs.rowInserted())
            conflict = doUpdate(crs);
        }
        PreparedStatement pstmtInsert = conn.prepareStatement(insertSQL);
        try {
          pstmtInsert.setMaxFieldSize(crs.getMaxFieldSize());
          pstmtInsert.setEscapeProcessing(crs.getEscapeProcessing());
          pstmtInsert.setQueryTimeout(crs.getQueryTimeout());
        } catch (Exception e) { }
        crs.beforeFirst();
        while(!conflict && crs.next()) {
          if (crs.rowInserted() && !crs.rowDeleted())
            conflict = doInsert(pstmtInsert, crs);
        }
        try {
          pstmtInsert.close();
        } catch (Exception e) { }
      } finally {
        crs.setShowDeleted(showDel);
      }
      try {
        if (conflict) {
          conn.rollback();
          return false;
        }
        conn.commit();
        return true;
      } finally {
        crs.rowSetReader.close();
        conn = null;
        rsmd = null;
        params.clear();
      }
    }
    private boolean doUpdate(OPLCachedRowSet crs) throws SQLException {
      ResultSet rs_orig = crs.getOriginalRow();
      if (!rs_orig.next())
        return true;
      StringBuffer tmpSQL = new StringBuffer(updateSQL);
      LinkedList<Object> setData = new LinkedList<Object>();
      boolean comma = false;
      for (int i = 1; i <= colCount; i++)
        if (crs.columnUpdated(i)) {
          if (!comma)
            comma = true;
          else
            tmpSQL.append(", ");
          tmpSQL.append(rsmd.getColumnName(i));
          tmpSQL.append(" = ? ");
          setData.add(new Integer(i));
        }
      tmpSQL.append(" WHERE ");
      tmpSQL.append(createWhere(keyCols, rs_orig));
      PreparedStatement pstmt = conn.prepareStatement(tmpSQL.toString());
      try {
        pstmt.setMaxFieldSize(crs.getMaxFieldSize());
        pstmt.setEscapeProcessing(crs.getEscapeProcessing());
        pstmt.setQueryTimeout(crs.getQueryTimeout());
      } catch (Exception e) { }
      int par = 0;
      for (Iterator i = setData.iterator(); i.hasNext(); ) {
        int col = ((Integer)i.next()).intValue();
        Object x = crs.getObject(col);
        if (crs.wasNull())
          pstmt.setNull(++par, rsmd.getColumnType(col));
        else
          pstmt.setObject(++par, x);
      }
      for (Iterator i = params.iterator(); i.hasNext(); )
        pstmt.setObject(++par, i.next());
      if (pstmt.executeUpdate() != 1)
        return true;
      pstmt.close();
      return false;
    }
    private boolean doInsert(PreparedStatement insertPStmt, OPLCachedRowSet crs)
      throws SQLException
    {
      for (int i = 1; i <= colCount; i++) {
        Object x = crs.getObject(i);
        if (crs.wasNull())
          insertPStmt.setNull(i, rsmd.getColumnType(i));
        else
          insertPStmt.setObject(i, x);
      }
      if (insertPStmt.executeUpdate() != 1)
        return true;
      return false;
    }
    private boolean doDelete(OPLCachedRowSet crs) throws SQLException {
      ResultSet rs = crs.getOriginalRow();
      if (!rs.next())
        return true;
      String delWhere = createWhere(keyCols, rs);
      PreparedStatement pstmt = conn.prepareStatement(deleteSQL + delWhere);
      try {
        pstmt.setMaxFieldSize(crs.getMaxFieldSize());
        pstmt.setEscapeProcessing(crs.getEscapeProcessing());
        pstmt.setQueryTimeout(crs.getQueryTimeout());
      } catch (Exception e) { }
      int par = 0;
      for (Iterator i = params.iterator(); i.hasNext(); )
        pstmt.setObject(++par, i.next());
      if (pstmt.executeUpdate() != 1)
        return true;
      pstmt.close();
      return false;
    }
    private String createWhere(int[] keys, ResultSet rs) throws SQLException {
      StringBuffer tmp = new StringBuffer();
      params.clear();
      for (int i = 0; i < keys.length; i++) {
        if (i > 0)
          tmp.append("AND ");
        tmp.append(rsmd.getColumnName(keys[i]));
        Object x = rs.getObject(keys[i]);
        if (rs.wasNull()) {
          tmp.append(" IS NULL ");
        } else {
          tmp.append(" = ? ");
          params.add(x);
        }
      }
      return tmp.toString();
    }
    private void initializeStmts(OPLCachedRowSet crs) throws SQLException {
      if ((rsmd = crs.getMetaData()) == null)
        throw OPLMessage_x.makeException(OPLMessage_x.errx_RowSetMetaData_is_not_defined);
      if ((colCount = rsmd.getColumnCount()) < 1)
        return;
      DatabaseMetaData dbmd = conn.getMetaData();
      String tableName = crs.getTableName();
      if (tableName == null) {
        String schName = rsmd.getSchemaName(1);
        if (schName != null && schName.length() == 0)
          schName = null;
        String tabName = rsmd.getTableName(1);
        if (tabName == null || (tabName != null && tabName.length() == 0))
          throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_XX_can_not_determine_the_table_name, "'RowSetWriter'");
        tableName = schName + "." + tabName;
      }
      StringBuffer _updateSQL = new StringBuffer("UPDATE ");
      StringBuffer _insertSQL = new StringBuffer("INSERT INTO ");
      StringBuffer _deleteSQL = new StringBuffer("DELETE FROM ");
      StringBuffer listColName = new StringBuffer();
      StringBuffer fullListParm = new StringBuffer();
      for (int i = 1; i <= colCount; i++) {
        if (i > 1) {
          listColName.append(", ");
          fullListParm.append(", ");
        } else {
          listColName.append(" ");
          fullListParm.append(" ");
        }
        listColName.append(rsmd.getColumnName(i));
        fullListParm.append('?');
      }
      _updateSQL.append(tableName);
        _updateSQL.append(" SET ");
      _deleteSQL.append(tableName);
        _deleteSQL.append(" WHERE ");
      _insertSQL.append(tableName);
        _insertSQL.append("(");
        _insertSQL.append(listColName.toString());
        _insertSQL.append(") VALUES ( ");
        _insertSQL.append(fullListParm.toString());
        _insertSQL.append(")");
      insertSQL = _insertSQL.toString();
      updateSQL = _updateSQL.toString();
      deleteSQL = _deleteSQL.toString();
      setKeyCols(crs);
    }
    private void setKeyCols(OPLCachedRowSet crs) throws SQLException {
      keyCols = crs.getKeyCols();
      if (keyCols == null || keyCols.length == 0) {
        int count = 0;
        int[] tmpCols = new int[colCount];
        for (int i = 1; i <= colCount; i++)
          switch(rsmd.getColumnType(i)) {
            case Types.BIGINT:
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.REAL:
            case Types.FLOAT:
            case Types.DOUBLE:
            case Types.DECIMAL:
            case Types.NUMERIC:
            case Types.BIT:
            case Types.BOOLEAN:
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
            case Types.DATALINK:
            case Types.NCHAR:
            case Types.ROWID:
            case Types.NVARCHAR:
            case Types.DISTINCT:
              tmpCols[count++] = i;
              break;
          }
        if (count > 0) {
          keyCols = new int[count];
          System.arraycopy(tmpCols, 0, keyCols, 0, count);
        }
      }
      if (keyCols == null && keyCols.length == 0)
        throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_XX_can_not_determine_the_keyCols, "'RowSetWriter'");
    }
  }
}
