package virtuoso.jdbc4;
import java.sql.*;
import java.util.*;
import openlink.util.*;
public class VirtuosoResultSetMetaData implements ResultSetMetaData
{
   protected Hashtable<VirtuosoColumn,Integer> hcolumns;
   private openlink.util.Vector columnsMetaData = new openlink.util.Vector(10,20);
   protected VirtuosoResultSetMetaData (VirtuosoConnection conn, String [] columns, int [] dtps)
   {
      hcolumns = new Hashtable<VirtuosoColumn,Integer>();
      for(int i = 0;i < columns.length;i++)
      {
         VirtuosoColumn col = new VirtuosoColumn(columns[i], dtps[i], conn);
         hcolumns.put(col,Integer.valueOf(i));
         columnsMetaData.insertElementAt(col,i);
      }
   }
   VirtuosoResultSetMetaData(openlink.util.Vector args, VirtuosoConnection conn) throws VirtuosoException
   {
      if(args == null)
         return;
      Object v = ((openlink.util.Vector)args).firstElement();
      openlink.util.Vector vect = null;
      if (v instanceof openlink.util.Vector)
         vect = (openlink.util.Vector)v;
      else
         return;
      hcolumns = new Hashtable<VirtuosoColumn,Integer>();
      for(int i = 0;i < vect.size();i++)
      {
         VirtuosoColumn col =
      new VirtuosoColumn((openlink.util.Vector)((openlink.util.Vector)vect).elementAt(i),
   conn);
         hcolumns.put(col,Integer.valueOf(i));
         columnsMetaData.insertElementAt(col,i);
      }
   }
   public int getColumnCount() throws VirtuosoException
   {
      return columnsMetaData.size();
   }
   public boolean isAutoIncrement(int column) throws VirtuosoException
   {
      if(column < 1 || column > columnsMetaData.size())
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + columnsMetaData.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoColumn)(columnsMetaData.elementAt(column - 1))).isAutoIncrement();
   }
   public boolean isCaseSensitive(int column) throws VirtuosoException
   {
      if(column < 1 || column > columnsMetaData.size())
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + columnsMetaData.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoColumn)(columnsMetaData.elementAt(column - 1))).isCaseSensitive();
   }
   public boolean isSearchable(int column) throws VirtuosoException
   {
      if(column < 1 || column > columnsMetaData.size())
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + columnsMetaData.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoColumn)(columnsMetaData.elementAt(column - 1))).isSearchable();
   }
   public boolean isCurrency(int column) throws VirtuosoException
   {
      if(column < 1 || column > columnsMetaData.size())
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + columnsMetaData.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoColumn)(columnsMetaData.elementAt(column - 1))).isCurrency();
   }
   public int isNullable(int column) throws VirtuosoException
   {
      if(column < 1 || column > columnsMetaData.size())
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + columnsMetaData.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoColumn)(columnsMetaData.elementAt(column - 1))).isNullable();
   }
   public boolean isSigned(int column) throws VirtuosoException
   {
      if(column < 1 || column > columnsMetaData.size())
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + columnsMetaData.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoColumn)(columnsMetaData.elementAt(column - 1))).isSigned();
   }
   public int getColumnDisplaySize(int column) throws VirtuosoException
   {
      if(column < 1 || column > columnsMetaData.size())
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + columnsMetaData.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoColumn)(columnsMetaData.elementAt(column - 1))).getColumnDisplaySize();
   }
   public String getColumnLabel(int column) throws VirtuosoException
   {
      return getColumnName(column);
   }
   public String getColumnName(int column) throws VirtuosoException
   {
      if(column < 1 || column > columnsMetaData.size())
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + columnsMetaData.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoColumn)(columnsMetaData.elementAt(column - 1))).getColumnName();
   }
   public void setColumnName(int column, String name) throws VirtuosoException
   {
      if(column < 1 || column > columnsMetaData.size())
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + columnsMetaData.size(),VirtuosoException.BADPARAM);
      ((VirtuosoColumn)(columnsMetaData.elementAt(column - 1))).setColumnName(name);
   }
   public int getPrecision(int column) throws VirtuosoException
   {
      if(column < 1 || column > columnsMetaData.size())
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + columnsMetaData.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoColumn)(columnsMetaData.elementAt(column - 1))).getPrecision();
   }
   public int getScale(int column) throws VirtuosoException
   {
      if(column < 1 || column > columnsMetaData.size())
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + columnsMetaData.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoColumn)(columnsMetaData.elementAt(column - 1))).getScale();
   }
   public int getColumnType(int column) throws VirtuosoException
   {
      if(column < 1 || column > columnsMetaData.size())
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + columnsMetaData.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoColumn)(columnsMetaData.elementAt(column - 1))).getColumnType();
   }
   public String getColumnTypeName(int column) throws VirtuosoException
   {
      if(column < 1 || column > columnsMetaData.size())
         throw new VirtuosoException(
   "Bad column number : " + column + " not in 1<n<" + columnsMetaData.size(),
   VirtuosoException.BADPARAM);
     VirtuosoColumn c = (VirtuosoColumn)columnsMetaData.elementAt(column - 1);
     if (c.isXml ())
        return "XMLType";
     return _getColumnTypeName (getColumnType(column));
   }
   protected static String _getColumnTypeName(int columnType) throws VirtuosoException
   {
      switch(columnType)
      {
         case Types.ARRAY:
            return "ARRAY";
         case Types.BIGINT:
            return "BIGINT";
         case Types.BINARY:
            return "BINARY";
         case Types.BIT:
            return "BIT";
         case Types.BLOB:
            return "BLOB";
         case Types.CHAR:
            return "CHAR";
         case Types.CLOB:
            return "CLOB";
         case Types.DATE:
            return "DATE";
         case Types.DECIMAL:
            return "DECIMAL";
         case Types.DISTINCT:
            return "DISTINCT";
         case Types.DOUBLE:
            return "DOUBLE PRECISION";
         case Types.FLOAT:
            return "FLOAT";
         case Types.INTEGER:
            return "INTEGER";
         case Types.JAVA_OBJECT:
            return "JAVA_OBJECT";
         case Types.LONGVARBINARY:
            return "LONG VARBINARY";
         case Types.LONGVARCHAR:
            return "LONG VARCHAR";
         case Types.NULL:
            return "NULL";
         case Types.NUMERIC:
            return "NUMERIC";
         case Types.OTHER:
            return "OTHER";
         case Types.REAL:
            return "REAL";
         case Types.SMALLINT:
            return "SMALLINT";
         case Types.STRUCT:
            return "STRUCT";
         case Types.TIME:
            return "TIME";
         case Types.TIMESTAMP:
            return "TIMESTAMP";
         case Types.TINYINT:
            return "TINYINT";
         case Types.VARBINARY:
            return "VARBINARY";
         case Types.VARCHAR:
            return "VARCHAR";
         case Types.NVARCHAR:
     return "NVARCHAR";
         case Types.LONGNVARCHAR:
     return "LONG NVARCHAR";
      }
      ;
      return "";
   }
   public boolean isReadOnly(int column) throws VirtuosoException
   {
      if(column < 1 || column > columnsMetaData.size())
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + columnsMetaData.size(),VirtuosoException.BADPARAM);
      return !((VirtuosoColumn)(columnsMetaData.elementAt(column - 1))).isUpdateable();
   }
   public boolean isWritable(int column) throws VirtuosoException
   {
      if(column < 1 || column > columnsMetaData.size())
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + columnsMetaData.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoColumn)(columnsMetaData.elementAt(column - 1))).isUpdateable();
   }
   public boolean isDefinitelyWritable(int column) throws VirtuosoException
   {
      return isWritable(column);
   }
   public String getColumnClassName(int column) throws VirtuosoException
   {
      if(column < 1 || column > columnsMetaData.size())
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + columnsMetaData.size(),VirtuosoException.BADPARAM);
      return ((VirtuosoColumn)(columnsMetaData.elementAt(column - 1))).getColumnClassName();
   }
   public String getSchemaName(int column) throws VirtuosoException
   {
      return "";
   }
   public String getTableName(int column) throws VirtuosoException
   {
      return "";
   }
   public String getCatalogName(int column) throws VirtuosoException
   {
      return "";
   }
   public void close() throws VirtuosoException
   {
      if(hcolumns != null)
      {
         hcolumns.clear();
         hcolumns = null;
      }
      if(columnsMetaData != null)
      {
         columnsMetaData.removeAllElements();
         columnsMetaData = null;
      }
   }
   protected int getColumnDtp (int column)
     {
       if(column < 1 || column > columnsMetaData.size())
  return 0;
       else
  return ((VirtuosoColumn)(columnsMetaData.elementAt(column - 1))).getDtp();
     }
   protected boolean isXml (int column)
     {
       if(column < 1 || column > columnsMetaData.size())
  return false;
       else
  return ((VirtuosoColumn)(columnsMetaData.elementAt(column - 1))).isXml();
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
