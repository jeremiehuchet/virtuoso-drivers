package virtuoso.jdbc4;
import java.sql.*;
import java.util.*;
import java.io.*;
import java.math.*;
import openlink.util.*;
public class VirtuosoCallableStatement extends VirtuosoPreparedStatement implements CallableStatement
{
   private boolean _wasNull = false;
   private boolean _hasOut = false;
   protected int [] param_type;
   protected int [] param_scale;
   VirtuosoCallableStatement(VirtuosoConnection connection, String sql) throws VirtuosoException
   {
      this (connection,sql,VirtuosoResultSet.TYPE_FORWARD_ONLY,VirtuosoResultSet.CONCUR_READ_ONLY);
   }
   VirtuosoCallableStatement(VirtuosoConnection connection, String sql, int type, int concurrency) throws VirtuosoException
   {
      super(connection,sql ,type,concurrency);
      param_type = new int[parameters.capacity()];
      param_scale = new int[parameters.capacity()];
      for (int i = 0; i < param_type.length; i++)
        {
          param_type[i] = Types.OTHER;
          param_scale[i] = 0;
        }
   }
   public void registerOutParameter(int parameterIndex, int sqlType) throws VirtuosoException
   {
      registerOutParameter(parameterIndex,sqlType,0);
   }
   public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws VirtuosoException
   {
      _hasOut = true;
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      param_type[parameterIndex - 1] = sqlType;
      param_scale[parameterIndex - 1] = scale;
      switch(sqlType)
      {
         case Types.ARRAY:
         case Types.STRUCT:
            return;
         case Types.BIGINT:
            if (objparams.elementAt(parameterIndex - 1) == null)
              objparams.setElementAt(new Long(Long.MAX_VALUE),parameterIndex - 1);
            return;
         case Types.LONGVARBINARY:
         case Types.VARBINARY:
         case Types.BINARY:
            return;
         case Types.BIT:
            if (objparams.elementAt(parameterIndex - 1) == null)
              objparams.setElementAt(new Boolean(false),parameterIndex - 1);
            return;
         case Types.BLOB:
         case Types.CLOB:
            return;
         case Types.LONGVARCHAR:
         case Types.VARCHAR:
         case Types.CHAR:
            if (objparams.elementAt(parameterIndex - 1) == null)
              objparams.setElementAt(new String(),parameterIndex - 1);
            return;
         case Types.DATE:
            if (objparams.elementAt(parameterIndex - 1) == null)
              objparams.setElementAt(new java.sql.Date(0),parameterIndex - 1);
            return;
         case Types.TIME:
            if (objparams.elementAt(parameterIndex - 1) == null)
              objparams.setElementAt(new java.sql.Time(0),parameterIndex - 1);
            return;
         case Types.TIMESTAMP:
            if (objparams.elementAt(parameterIndex - 1) == null)
              objparams.setElementAt(new java.sql.Timestamp(0),parameterIndex - 1);
            return;
         case Types.NUMERIC:
         case Types.DECIMAL:
            if (objparams.elementAt(parameterIndex - 1) == null)
              objparams.setElementAt(new BigDecimal(0.0d).setScale(scale),parameterIndex - 1);
            return;
         case Types.FLOAT:
         case Types.DOUBLE:
            if (objparams.elementAt(parameterIndex - 1) == null)
              objparams.setElementAt(new Double(Double.MAX_VALUE),parameterIndex - 1);
            return;
         case Types.OTHER:
         case Types.JAVA_OBJECT:
            if (objparams.elementAt(parameterIndex - 1) == null)
              objparams.setElementAt(new Object(),parameterIndex - 1);
            return;
         case Types.NULL:
            if (objparams.elementAt(parameterIndex - 1) == null)
              objparams.setElementAt(new VirtuosoNullParameter(sqlType, true),parameterIndex - 1);
            return;
         case Types.REAL:
            if (objparams.elementAt(parameterIndex - 1) == null)
              objparams.setElementAt(new Float(Float.MAX_VALUE),parameterIndex - 1);
            return;
         case Types.SMALLINT:
            if (objparams.elementAt(parameterIndex - 1) == null)
              objparams.setElementAt(new Short(Short.MAX_VALUE),parameterIndex - 1);
            return;
         case Types.INTEGER:
         case Types.TINYINT:
            if (objparams.elementAt(parameterIndex - 1) == null)
              objparams.setElementAt(new Integer(Integer.MAX_VALUE),parameterIndex - 1);
            return;
      }
      ;
   }
   public boolean wasNull() throws VirtuosoException
   {
      return _wasNull;
   }
   public boolean hasOut()
   {
      return _hasOut;
   }
   public String getString(int parameterIndex) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      Object obj = VirtuosoTypes.mapJavaTypeToSqlType (objparams.elementAt(parameterIndex - 1),
                     param_type[parameterIndex - 1],
                     param_scale[parameterIndex - 1]);
      if (_wasNull = (obj == null))
 return null;
      else
 return obj.toString();
   }
   public boolean getBoolean(int parameterIndex) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      Object obj = VirtuosoTypes.mapJavaTypeToSqlType (objparams.elementAt(parameterIndex - 1),
                     param_type[parameterIndex - 1],
                     param_scale[parameterIndex - 1]);
      _wasNull = (obj == null);
      if (_wasNull)
        return false;
      else
        {
          java.lang.Number nret = (java.lang.Number) obj;
          return (nret.intValue() != 0);
        }
   }
   public byte getByte(int parameterIndex) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      Object obj = VirtuosoTypes.mapJavaTypeToSqlType (objparams.elementAt(parameterIndex - 1),
                     param_type[parameterIndex - 1],
                     param_scale[parameterIndex - 1]);
      if (_wasNull = (obj == null))
 return 0;
      else
 return ((Number) obj).byteValue();
   }
   public short getShort(int parameterIndex) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      Object obj = VirtuosoTypes.mapJavaTypeToSqlType (objparams.elementAt(parameterIndex - 1),
                     param_type[parameterIndex - 1],
                     param_scale[parameterIndex - 1]);
      if (_wasNull = (obj == null))
 return 0;
      else
 return ((Number) obj).shortValue();
   }
   public int getInt(int parameterIndex) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      Object obj = VirtuosoTypes.mapJavaTypeToSqlType (objparams.elementAt(parameterIndex - 1),
                     param_type[parameterIndex - 1],
                     param_scale[parameterIndex - 1]);
      if (_wasNull = (obj == null))
 return 0;
      else
 return ((Number) obj).intValue();
   }
   public long getLong(int parameterIndex) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      Object obj = VirtuosoTypes.mapJavaTypeToSqlType (objparams.elementAt(parameterIndex - 1),
                     param_type[parameterIndex - 1],
                     param_scale[parameterIndex - 1]);
      if (_wasNull = (obj == null))
 return 0;
      else
 return ((Number) obj).longValue();
   }
   public float getFloat(int parameterIndex) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      Object obj = VirtuosoTypes.mapJavaTypeToSqlType (objparams.elementAt(parameterIndex - 1),
                     param_type[parameterIndex - 1],
                     param_scale[parameterIndex - 1]);
      if (_wasNull = (obj == null))
 return 0;
      else
 return ((Number) obj).floatValue();
   }
   public double getDouble(int parameterIndex) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      Object obj = VirtuosoTypes.mapJavaTypeToSqlType (objparams.elementAt(parameterIndex - 1),
                     param_type[parameterIndex - 1],
                     param_scale[parameterIndex - 1]);
      if (_wasNull = (obj == null))
 return 0;
      else
 return ((Number) obj).doubleValue();
   }
   public BigDecimal getBigDecimal(int parameterIndex, int scale) throws VirtuosoException
   {
     BigDecimal ret = getBigDecimal (parameterIndex);
     if (ret != null)
       ret = ret.setScale(scale);
     return ret;
   }
   public byte[] getBytes(int parameterIndex) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      Object obj = VirtuosoTypes.mapJavaTypeToSqlType (objparams.elementAt(parameterIndex - 1),
                     param_type[parameterIndex - 1],
                     param_scale[parameterIndex - 1]);
      if (_wasNull = (obj == null))
 return null;
      else
 return (byte []) obj;
   }
   public java.sql.Date getDate(int parameterIndex) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      Object obj = VirtuosoTypes.mapJavaTypeToSqlType (objparams.elementAt(parameterIndex - 1),
                     Types.DATE,
                     param_scale[parameterIndex - 1]);
      if (_wasNull = (obj == null))
 return null;
      else
 return (java.sql.Date)obj;
   }
   public java.sql.Time getTime(int parameterIndex) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      Object obj = VirtuosoTypes.mapJavaTypeToSqlType (objparams.elementAt(parameterIndex - 1),
                     Types.TIME,
                     param_scale[parameterIndex - 1]);
      if (_wasNull = (obj == null))
 return null;
      else
        return (java.sql.Time)obj;
   }
   public java.sql.Timestamp getTimestamp(int parameterIndex) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      Object obj = VirtuosoTypes.mapJavaTypeToSqlType (objparams.elementAt(parameterIndex - 1),
                     Types.TIMESTAMP,
                     param_scale[parameterIndex - 1]);
      if (_wasNull = (obj == null))
 return null;
      else
 return (java.sql.Timestamp)obj;
   }
   public Object getObject(int parameterIndex) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      Object obj = VirtuosoTypes.mapJavaTypeToSqlType (objparams.elementAt(parameterIndex - 1),
                     param_type[parameterIndex - 1],
                     param_scale[parameterIndex - 1]);
      _wasNull = (obj == null);
      return obj;
   }
   public BigDecimal getBigDecimal(int parameterIndex) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      Object obj = VirtuosoTypes.mapJavaTypeToSqlType (objparams.elementAt(parameterIndex - 1),
                     Types.DECIMAL,
                     param_scale[parameterIndex - 1]);
      if (_wasNull = (obj == null))
 return null;
      else
 return ((BigDecimal) obj);
   }
   public Object getObject(int parameterIndex, java.util.Map map) throws VirtuosoException
   {
     return this.getObject (parameterIndex);
   }
   public Ref getRef(int parameterIndex) throws VirtuosoException
   {
     throw new VirtuosoException ("REF not supported", VirtuosoException.NOTIMPLEMENTED);
   }
   public
   Blob
   getBlob(int parameterIndex) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      if (_wasNull = (objparams.elementAt(parameterIndex - 1) == null))
        return null;
      else
        return
           (Blob)
           objparams.elementAt(parameterIndex - 1);
   }
   public
   Clob
   getClob(int parameterIndex) throws VirtuosoException
   {
      if(parameterIndex < 1 || parameterIndex > parameters.capacity())
         throw new VirtuosoException("Index " + parameterIndex + " is not 1<n<" + parameters.capacity(),VirtuosoException.BADPARAM);
      if (_wasNull = (objparams.elementAt(parameterIndex - 1) == null))
        return null;
      else
        return
         (Clob)
         objparams.elementAt(parameterIndex - 1);
   }
   public Array getArray(int parameterIndex) throws VirtuosoException
   {
     throw new VirtuosoException ("ARRAY not supported", VirtuosoException.NOTIMPLEMENTED);
   }
   public java.sql.Date getDate(int parameterIndex, Calendar cal) throws VirtuosoException
   {
     java.sql.Date date = this.getDate(parameterIndex);
      if(cal != null && date != null)
        date = new java.sql.Date(VirtuosoTypes.timeToCal(date, cal));
      return date;
   }
   public java.sql.Time getTime(int parameterIndex, Calendar cal) throws VirtuosoException
   {
     java.sql.Time _time = this.getTime(parameterIndex);
      if(cal != null && _time != null)
        _time = new java.sql.Time(VirtuosoTypes.timeToCal(_time, cal));
      return _time;
   }
   public java.sql.Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException
   {
     java.sql.Timestamp _ts, val;
     _ts = val = this.getTimestamp(parameterIndex);
      if(cal != null && _ts != null)
        _ts = new java.sql.Timestamp(VirtuosoTypes.timeToCal(_ts, cal));
      if (_ts!=null)
        _ts.setNanos(val.getNanos());
      return _ts;
   }
   public void registerOutParameter(int parameterIndex, int sqlType, String typeName) throws VirtuosoException
   {
     throw new VirtuosoException ("UDTs not supported", VirtuosoException.NOTIMPLEMENTED);
   }
   public void registerOutParameter(String parameterName,
       int sqlType) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void registerOutParameter(String parameterName,
       int sqlType,
       int scale) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void registerOutParameter(String parameterName,
       int sqlType,
       String typeName) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public java.net.URL getURL(int parameterIndex) throws SQLException
     {
       throw new VirtuosoException ("DATALINK type not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void setURL(String parameterName, java.net.URL val) throws SQLException
     {
       throw new VirtuosoException ("DATALINK type not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void setNull(String parameterName, int sqlType) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void setBoolean(String parameterName, boolean x) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void setByte(String parameterName, byte x) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void setShort(String parameterName, short x) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void setInt(String parameterName, int x) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void setLong(String parameterName, long x) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void setFloat(String parameterName, float x) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void setDouble(String parameterName, double x) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void setString(String parameterName, String x) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void setBytes(String parameterName, byte[] x) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void setDate(String parameterName, java.sql.Date x) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void setTime(String parameterName, Time x) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void setTimestamp(String parameterName, Timestamp x) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void setObject(String parameterName, Object x, int targetSqlType,
       int scale) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void setObject(String parameterName, Object x,
       int targetSqlType) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void setObject(String parameterName, Object x) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void setDate(String parameterName, java.sql.Date x, Calendar cal) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void setTime(String parameterName, Time x, Calendar cal) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public void setNull(String parameterName, int sqlType, String typeName) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public String getString(String parameterName) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public boolean getBoolean(String parameterName) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public byte getByte(String parameterName) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public short getShort(String parameterName) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public int getInt(String parameterName) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public long getLong(String parameterName) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public float getFloat(String parameterName) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public double getDouble(String parameterName) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public byte[] getBytes(String parameterName) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public java.sql.Date getDate(String parameterName) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public Time getTime(String parameterName) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public Timestamp getTimestamp(String parameterName) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public Object getObject(String parameterName) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public BigDecimal getBigDecimal(String parameterName) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public Object getObject(String parameterName, Map map) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public Ref getRef(String parameterName) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public Blob getBlob(String parameterName) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public Clob getClob(String parameterName) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public Array getArray(String parameterName) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public java.sql.Date getDate(String parameterName, Calendar cal) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public Time getTime(String parameterName, Calendar cal) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException
     {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
     }
   public java.net.URL getURL(String parameterName) throws SQLException
     {
       throw new VirtuosoException ("DATALINK type not supported", VirtuosoException.NOTIMPLEMENTED);
     }
  private int findParam (String paramName) throws SQLException {
       throw new VirtuosoException ("Named parameters not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public RowId getRowId(int parameterIndex) throws SQLException
  {
    throw new VirtuosoException ("Method 'getRowId(parameterIndex)' not yet implemented", VirtuosoException.NOTIMPLEMENTED);
  }
  public RowId getRowId(String parameterName) throws SQLException
  {
    throw new VirtuosoException ("Method 'getRowId(parameterName)' not yet implemented", VirtuosoException.NOTIMPLEMENTED);
  }
  public void setRowId(String parameterName, RowId x) throws SQLException
  {
    throw new VirtuosoException ("Method 'setRowId(parameterName, x)' not yet implemented", VirtuosoException.NOTIMPLEMENTED);
  }
  public void setNString(String parameterName, String value)
            throws SQLException
  {
    setNString(findParam(parameterName), value);
  }
  public void setNCharacterStream(String parameterName, Reader value, long length)
            throws SQLException
  {
    setNCharacterStream(findParam(parameterName), value, length);
  }
  public void setNClob(String parameterName, NClob value) throws SQLException
  {
    setNClob(findParam(parameterName), value);
  }
  public void setClob(String parameterName, Reader reader, long length)
       throws SQLException
  {
    setClob(findParam(parameterName), reader, length);
  }
  public void setBlob(String parameterName, InputStream inputStream, long length)
        throws SQLException
  {
    setBlob(findParam(parameterName), inputStream, length);
  }
  public void setNClob(String parameterName, Reader reader, long length)
       throws SQLException
  {
    setNClob(findParam(parameterName), reader, length);
  }
  public NClob getNClob (int parameterIndex) throws SQLException
  {
    return new OPLHeapNClob(getString(parameterIndex));
  }
  public NClob getNClob (String parameterName) throws SQLException
  {
    return getNClob(findParam(parameterName));
  }
  public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException
  {
    throw new VirtuosoException ("Method 'setSQLXML(parameterName, xmlObject)' not yet implemented", VirtuosoException.NOTIMPLEMENTED);
  }
  public SQLXML getSQLXML(int parameterIndex) throws SQLException
  {
    throw new VirtuosoException ("Method 'getSQLXML(int parameterIndex)' not yet implemented", VirtuosoException.NOTIMPLEMENTED);
  }
  public SQLXML getSQLXML(String parameterName) throws SQLException
  {
    throw new VirtuosoException ("Method 'getSQLXML(String parameterName)' not yet implemented", VirtuosoException.NOTIMPLEMENTED);
  }
  public String getNString(int parameterIndex) throws SQLException
  {
    return getString(parameterIndex);
  }
  public String getNString(String parameterName) throws SQLException
  {
    return getNString(findParam(parameterName));
  }
  public java.io.Reader getNCharacterStream(int parameterIndex) throws SQLException
  {
    return (new OPLHeapNClob(getNString(parameterIndex))).getCharacterStream();
  }
  public java.io.Reader getNCharacterStream(String parameterName) throws SQLException
  {
    return getNCharacterStream(findParam(parameterName));
  }
  public java.io.Reader getCharacterStream(int parameterIndex) throws SQLException
  {
    return (new OPLHeapClob(getString(parameterIndex))).getCharacterStream();
  }
  public java.io.Reader getCharacterStream(String parameterName) throws SQLException
  {
    return getCharacterStream(findParam(parameterName));
  }
  public void setBlob (String parameterName, Blob x) throws SQLException
  {
    setBlob(findParam(parameterName), x);
  }
  public void setClob (String parameterName, Clob x) throws SQLException
  {
    setClob(findParam(parameterName), x);
  }
  public void setAsciiStream(String parameterName, java.io.InputStream x, long length)
 throws SQLException
  {
    setAsciiStream(findParam(parameterName), x, (int)length);
  }
  public void setBinaryStream(String parameterName, java.io.InputStream x,
    long length) throws SQLException
  {
    setBinaryStream(findParam(parameterName), x, (int)length);
  }
  public void setCharacterStream(String parameterName,
       java.io.Reader reader,
       long length) throws SQLException
  {
    setCharacterStream(findParam(parameterName), reader, (int)length);
  }
  public void setAsciiStream(String parameterName, java.io.InputStream x)
     throws SQLException
  {
    throw new VirtuosoException ("Method 'setAsciiStream(parameterName, x)' not yet implemented", VirtuosoException.NOTIMPLEMENTED);
  }
  public void setBinaryStream(String parameterName, java.io.InputStream x)
    throws SQLException
  {
    throw new VirtuosoException ("Method 'setBinaryStream(parameterName, x)' not yet implemented", VirtuosoException.NOTIMPLEMENTED);
  }
  public void setCharacterStream(String parameterName,
            java.io.Reader reader) throws SQLException
  {
    throw new VirtuosoException ("Method 'setCharacterStream(parameterName, reader)' not yet implemented", VirtuosoException.NOTIMPLEMENTED);
  }
  public void setNCharacterStream(String parameterName, Reader value) throws SQLException
  {
    throw new VirtuosoException ("Method 'setNCharacterStream(parameterName, value)' not yet implemented", VirtuosoException.NOTIMPLEMENTED);
  }
  public void setClob(String parameterName, Reader reader)
       throws SQLException
  {
    throw new VirtuosoException ("Method 'setClob(parameterName, reader)' not yet implemented", VirtuosoException.NOTIMPLEMENTED);
  }
  public void setBlob(String parameterName, InputStream inputStream)
        throws SQLException
  {
    throw new VirtuosoException ("Method 'setBlob(parameterName, inputStream)' not yet implemented", VirtuosoException.NOTIMPLEMENTED);
  }
  public void setNClob(String parameterName, Reader reader)
       throws SQLException
  {
    throw new VirtuosoException ("Method 'setNClob(parameterName, reader)' not yet implemented", VirtuosoException.NOTIMPLEMENTED);
  }
}
