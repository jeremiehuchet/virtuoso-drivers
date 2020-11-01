package virtuoso.jdbc4;
import java.sql.*;
import java.util.*;
import openlink.util.*;
class VirtuosoColumn
{
   private int typeObject;
   private String name;
   private int typeSQL;
   private int length;
   private int scale;
   private int precision;
   private boolean isAutoIncrement = false;
   private boolean isCaseSensitive = false;
   private boolean isCurrency = false;
   private int isNullable = ResultSetMetaData.columnNullable;
   private boolean isSearchable = false;
   private boolean isUpdateable = false;
   private boolean isSigned = false;
   private int _case;
   private boolean _isXml = false;
   VirtuosoColumn(String name, int dtp, VirtuosoConnection connection)
   {
      this.name = name;
      this._case = connection.getCase();
      this.typeObject = dtp;
   }
   VirtuosoColumn(openlink.util.Vector args, VirtuosoConnection connection)
   {
      try
      {
  if (connection.charset != null)
    name = connection.uncharsetBytes((String)args.firstElement());
  else if (connection.utf8_execs)
    name = new String (((String)args.firstElement()).getBytes("8859_1"), "UTF8");
  else
    name = (String)args.firstElement();
         if (null != args.elementAt(1))
    typeObject = ((Number)args.elementAt(1)).intValue();
         else
           typeObject = VirtuosoTypes.DV_STRING;
         if( args.elementAt(2)!=null)
         length = scale = ((Number)args.elementAt(2)).intValue();
         if( args.elementAt(3)!=null)
         precision = ((Number)args.elementAt(3)).intValue();
         if( args.elementAt(4)!=null && ((Number)args.elementAt(4)).intValue() == 1)
            isNullable = ResultSetMetaData.columnNullable;
         if( args.elementAt(5)!=null && ((Number)args.elementAt(5)).intValue() == 1)
            isUpdateable = true;
         if( args.elementAt(6)!=null && ((Number)args.elementAt(6)).intValue() == 1)
            isSearchable = true;
         this._case = connection.getCase();
  if (args.size () >= 12)
    {
        int cd_flags = ((Number)args.elementAt(11)).intValue();
        if ((cd_flags & VirtuosoTypes.CDF_XMLTYPE) != 0)
   _isXml = true;
        if ((cd_flags & VirtuosoTypes.CDF_AUTOINCREMENT) != 0)
   isAutoIncrement = true;
    }
      }
      catch(Exception e)
      {
      }
   }
  protected String getColumnClassName () throws VirtuosoException
    {
      if (_isXml)
        return "org.w3c.dom.Document";
      return getColumnClassName (typeObject);
    }
  protected static String getColumnClassName (int _typeObject) throws VirtuosoException
  {
    switch (_typeObject)
      {
      case VirtuosoTypes.DV_NULL:
 return "java.lang.Void";
 case VirtuosoTypes.DV_SHORT_CONT_STRING:
 case VirtuosoTypes.DV_SHORT_STRING_SERIAL:
 case VirtuosoTypes.DV_STRICT_STRING:
 case VirtuosoTypes.DV_LONG_CONT_STRING:
 case VirtuosoTypes.DV_STRING:
 case VirtuosoTypes.DV_C_STRING:
 case VirtuosoTypes.DV_WIDE:
 case VirtuosoTypes.DV_LONG_WIDE:
 return "java.lang.String";
 case VirtuosoTypes.DV_STRING_SESSION:
 return "java.lang.StringBuffer";
 case VirtuosoTypes.DV_C_SHORT:
 case VirtuosoTypes.DV_SHORT_INT:
 return "java.lang.Short";
 case VirtuosoTypes.DV_LONG_INT:
 return "java.lang.Integer";
 case VirtuosoTypes.DV_C_INT:
 return "java.lang.Integer";
 case VirtuosoTypes.DV_SINGLE_FLOAT:
 return "java.lang.Float";
 case VirtuosoTypes.DV_DOUBLE_FLOAT:
 return "java.lang.Double";
 case VirtuosoTypes.DV_CHARACTER:
 return "java.lang.Character";
 case VirtuosoTypes.DV_ARRAY_OF_LONG_PACKED:
 case VirtuosoTypes.DV_ARRAY_OF_FLOAT:
 case VirtuosoTypes.DV_ARRAY_OF_DOUBLE:
 case VirtuosoTypes.DV_ARRAY_OF_LONG:
 case VirtuosoTypes.DV_ARRAY_OF_POINTER:
 case VirtuosoTypes.DV_LIST_OF_POINTER:
 return "java.util.Vector";
 case VirtuosoTypes.DV_OBJECT_AND_CLASS:
 case VirtuosoTypes.DV_OBJECT_REFERENCE:
 return "java.lang.Object";
 case VirtuosoTypes.DV_BLOB_BIN:
 case VirtuosoTypes.DV_BLOB:
 case VirtuosoTypes.DV_BLOB_HANDLE:
 case VirtuosoTypes.DV_BIN:
 case VirtuosoTypes.DV_LONG_BIN:
 return "java.sql.Blob";
 case VirtuosoTypes.DV_NUMERIC:
 return "java.lang.BigDecimal";
 case VirtuosoTypes.DV_DATE:
 case VirtuosoTypes.DV_DATETIME:
 case VirtuosoTypes.DV_TIMESTAMP:
 case VirtuosoTypes.DV_TIMESTAMP_OBJ:
 case VirtuosoTypes.DV_TIME:
 return "java.lang.Date";
 default:
 return "java.lang.Object";
      }
  }
   protected String getColumnName()
   {
      return name;
   }
   protected void setColumnName(String s)
   {
      name = s;
   }
   protected int getColumnDisplaySize()
   {
      return length;
   }
  protected int getColumnType () throws VirtuosoException
  {
    return getColumnType (typeObject);
  }
  protected static int getColumnType (int _typeObject) throws VirtuosoException
  {
    switch (_typeObject)
      {
      case VirtuosoTypes.DV_C_SHORT:
      case VirtuosoTypes.DV_SHORT_INT:
 return Types.SMALLINT;
      case VirtuosoTypes.DV_LONG_INT:
      case VirtuosoTypes.DV_C_INT:
 return Types.INTEGER;
      case VirtuosoTypes.DV_DOUBLE_FLOAT:
 return Types.DOUBLE;
      case VirtuosoTypes.DV_NUMERIC:
 return Types.NUMERIC;
      case VirtuosoTypes.DV_SINGLE_FLOAT:
 return Types.REAL;
      case VirtuosoTypes.DV_BLOB:
 return Types.LONGVARCHAR;
      case VirtuosoTypes.DV_BLOB_BIN:
 return Types.LONGVARBINARY;
      case VirtuosoTypes.DV_BLOB_WIDE:
        return Types.LONGNVARCHAR;
      case VirtuosoTypes.DV_DATE:
 return Types.DATE;
      case VirtuosoTypes.DV_TIMESTAMP:
      case VirtuosoTypes.DV_TIMESTAMP_OBJ:
      case VirtuosoTypes.DV_DATETIME:
 return Types.TIMESTAMP;
      case VirtuosoTypes.DV_TIME:
 return Types.TIME;
      case VirtuosoTypes.DV_LONG_BIN:
      case VirtuosoTypes.DV_BIN:
 return Types.VARBINARY;
      case VirtuosoTypes.DV_WIDE:
      case VirtuosoTypes.DV_LONG_WIDE:
        return Types.NVARCHAR;
      case VirtuosoTypes.DV_DB_NULL:
      case VirtuosoTypes.DV_NULL:
 return Types.NULL;
      case VirtuosoTypes.DV_SHORT_CONT_STRING:
      case VirtuosoTypes.DV_SHORT_STRING_SERIAL:
      case VirtuosoTypes.DV_STRICT_STRING:
      case VirtuosoTypes.DV_LONG_CONT_STRING:
      case VirtuosoTypes.DV_STRING:
      case VirtuosoTypes.DV_C_STRING:
 return Types.VARCHAR;
      case VirtuosoTypes.DV_STRING_SESSION:
 return Types.LONGVARCHAR;
      case VirtuosoTypes.DV_CHARACTER:
 return Types.CHAR;
      case VirtuosoTypes.DV_ARRAY_OF_LONG_PACKED:
      case VirtuosoTypes.DV_ARRAY_OF_FLOAT:
      case VirtuosoTypes.DV_ARRAY_OF_DOUBLE:
      case VirtuosoTypes.DV_ARRAY_OF_LONG:
      case VirtuosoTypes.DV_ARRAY_OF_POINTER:
      case VirtuosoTypes.DV_LIST_OF_POINTER:
 return Types.ARRAY;
      case VirtuosoTypes.DV_OBJECT_AND_CLASS:
      case VirtuosoTypes.DV_OBJECT_REFERENCE:
 return Types.OTHER;
      case VirtuosoTypes.DV_BLOB_HANDLE:
      case VirtuosoTypes.DV_BLOB_WIDE_HANDLE:
 return Types.BLOB;
      default:
 return Types.OTHER;
      }
  }
   protected int getPrecision()
   {
      return precision;
   }
   protected int getScale()
   {
      return scale;
   }
   protected boolean isAutoIncrement()
   {
      return isAutoIncrement;
   }
   protected boolean isCaseSensitive()
   {
      return isCaseSensitive;
   }
   protected boolean isCurrency()
   {
      return isCurrency;
   }
   protected int isNullable()
   {
      return isNullable;
   }
   protected boolean isSearchable()
   {
      return isSearchable;
   }
   protected boolean isSigned()
   {
      return isSigned;
   }
   protected boolean isUpdateable()
   {
      return isUpdateable;
   }
   public int hashCode()
   {
      if(name != null && _case == 2)
         return name.toUpperCase().hashCode();
      if(name != null)
         return name.hashCode();
      return 0;
   }
   public boolean equals(Object obj)
   {
      if(obj != null && (obj instanceof VirtuosoColumn))
      {
         if(name != null && _case == 2)
            return ((VirtuosoColumn)obj).name.toUpperCase().equals(name.toUpperCase());
         if(name != null)
            return ((VirtuosoColumn)obj).name.equals(name);
      }
      return false;
   }
   protected int getDtp ()
     {
       return typeObject;
     }
   protected boolean isXml ()
     {
       return _isXml;
     }
}
