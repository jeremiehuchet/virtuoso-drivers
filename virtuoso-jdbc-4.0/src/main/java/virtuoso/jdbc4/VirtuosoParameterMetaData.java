package virtuoso.jdbc4;
import java.sql.*;
import java.util.*;
import openlink.util.*;
public class VirtuosoParameterMetaData implements ParameterMetaData
{
   protected openlink.util.Vector parameters;
   VirtuosoParameterMetaData(openlink.util.Vector args, VirtuosoConnection conn) throws VirtuosoException
   {
     if (args == null)
       {
         parameters = new openlink.util.Vector(0);
       }
     else
       {
         parameters = (openlink.util.Vector) args.clone();
       }
   }
   public int getParameterCount() throws VirtuosoException
   {
     return parameters.size();
   }
   private openlink.util.Vector getPd (int param) throws VirtuosoException
   {
     Object pd;
     openlink.util.Vector pdd;
     if (param < 0 || param > getParameterCount())
       throw new VirtuosoException ("No such parameter", "22023", VirtuosoException.BADPARAM);
     pd = parameters.elementAt (param);
     if (pd == null || !(pd instanceof openlink.util.Vector))
       throw new VirtuosoException ("Invalid param info type", "22023", VirtuosoException.BADPARAM);
     pdd = (openlink.util.Vector) pd;
     if (pdd.size () < 5)
       throw new VirtuosoException ("Invalid param info size", "22023", VirtuosoException.BADPARAM);
     return pdd;
   }
   public int isNullable(int param) throws VirtuosoException
   {
     int nullable = ((Number) getPd (param).elementAt (3)).intValue();
     return (nullable != 0) ?
       ParameterMetaData.parameterNullable : ParameterMetaData.parameterNoNulls;
   }
   public boolean isSigned(int param) throws VirtuosoException
   {
     int dtp = ((Number) getPd (param).elementAt (3)).intValue();
     return (
        dtp == VirtuosoTypes.DV_SHORT_CONT_STRING ||
 dtp == VirtuosoTypes.DV_SHORT_STRING_SERIAL ||
 dtp == VirtuosoTypes.DV_STRICT_STRING ||
 dtp == VirtuosoTypes.DV_LONG_CONT_STRING ||
 dtp == VirtuosoTypes.DV_STRING ||
 dtp == VirtuosoTypes.DV_C_STRING ||
 dtp == VirtuosoTypes.DV_WIDE ||
 dtp == VirtuosoTypes.DV_LONG_WIDE ||
 dtp == VirtuosoTypes.DV_C_SHORT ||
 dtp == VirtuosoTypes.DV_SHORT_INT ||
 dtp == VirtuosoTypes.DV_LONG_INT ||
 dtp == VirtuosoTypes.DV_C_INT ||
 dtp == VirtuosoTypes.DV_SINGLE_FLOAT ||
 dtp == VirtuosoTypes.DV_DOUBLE_FLOAT ||
 dtp == VirtuosoTypes.DV_CHARACTER ||
 dtp == VirtuosoTypes.DV_NUMERIC);
   }
   public int getPrecision(int param) throws VirtuosoException
   {
     return ((Number) getPd (param).elementAt (1)).intValue();
   }
   public int getScale(int param) throws VirtuosoException
   {
     return ((Number) getPd (param).elementAt (2)).intValue();
   }
   public int getParameterType(int param) throws VirtuosoException
   {
     return VirtuosoColumn.getColumnType (((Number) getPd (param).elementAt (0)).intValue());
   }
   public String getParameterTypeName(int param) throws VirtuosoException
   {
     return VirtuosoResultSetMetaData._getColumnTypeName (
       getParameterType ((((Number) getPd (param).elementAt (0)).intValue())));
   }
   public String getParameterClassName(int param) throws VirtuosoException
   {
     return VirtuosoColumn.getColumnClassName (((Number) getPd (param).elementAt (0)).intValue());
   }
   public int getParameterMode(int param) throws VirtuosoException
   {
     switch (((Number) getPd (param).elementAt (0)).intValue())
     {
        case VirtuosoTypes.SQL_PARAM_INPUT: return parameterModeIn;
        case VirtuosoTypes.SQL_PARAM_INPUT_OUTPUT: return parameterModeInOut;
        case VirtuosoTypes.SQL_PARAM_OUTPUT: return parameterModeOut;
        default: return parameterModeUnknown;
     }
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
