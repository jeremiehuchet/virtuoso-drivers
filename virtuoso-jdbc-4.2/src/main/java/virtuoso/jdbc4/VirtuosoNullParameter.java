package virtuoso.jdbc4;
import java.sql.*;
public class VirtuosoNullParameter
{
 private int type;
   VirtuosoNullParameter(int type) throws VirtuosoException
   {
    this(type, false);
   }
   VirtuosoNullParameter(int type, boolean isSql) throws VirtuosoException
   {
    if(isSql == false) this.type = type;
      else this.type = this.fromSQLType(type);
   }
   private int fromSQLType(int type) throws VirtuosoException
 {
    switch (type)
  {
     case Types.NULL:
      return VirtuosoTypes.DV_DB_NULL;
   case Types.VARCHAR:
      return VirtuosoTypes.DV_STRING;
   case Types.LONGVARCHAR:
      return VirtuosoTypes.DV_STRING;
         case Types.BIT:
         case Types.TINYINT:
   case Types.SMALLINT:
      return VirtuosoTypes.DV_SHORT_INT;
   case Types.INTEGER:
      return VirtuosoTypes.DV_LONG_INT;
   case Types.REAL:
      return VirtuosoTypes.DV_SINGLE_FLOAT;
   case Types.DOUBLE:
      return VirtuosoTypes.DV_DOUBLE_FLOAT;
   case Types.CHAR:
      return VirtuosoTypes.DV_CHARACTER;
   case Types.ARRAY:
      return VirtuosoTypes.DV_LIST_OF_POINTER;
   case Types.OTHER:
      return VirtuosoTypes.DV_OBJECT_REFERENCE;
   case Types.BLOB:
   case Types.CLOB:
      return VirtuosoTypes.DV_BLOB;
   case Types.BINARY:
   case Types.VARBINARY:
      return VirtuosoTypes.DV_BIN;
   case Types.LONGVARBINARY:
      return VirtuosoTypes.DV_LONG_BIN;
         case Types.BIGINT:
   case Types.NUMERIC:
         case Types.DECIMAL:
      return VirtuosoTypes.DV_NUMERIC;
   case Types.TIMESTAMP:
      return VirtuosoTypes.DV_TIMESTAMP;
   case Types.DATE:
      return VirtuosoTypes.DV_DATETIME;
   case Types.TIME:
      return VirtuosoTypes.DV_TIME;
   default:
    throw new VirtuosoException("SQL Type " + type + " not defined.", VirtuosoException.BADTAG);
  }
 }
}
