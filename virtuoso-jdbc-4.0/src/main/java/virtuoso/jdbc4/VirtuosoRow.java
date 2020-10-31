package virtuoso.jdbc4;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.math.*;
import java.io.*;
import openlink.util.*;
import java.lang.reflect.Method;
class VirtuosoRow
{
   private openlink.util.Vector content;
   protected int maxCol;
   private VirtuosoResultSet resultSet;
   VirtuosoRow(VirtuosoResultSet resultSet, openlink.util.Vector args) throws VirtuosoException
   {
      this.resultSet = resultSet;
      content = args;
      maxCol = resultSet.metaData.getColumnCount();
   }
   protected void getContent(Object anArray[])
   {
      for(int i = 0;i < anArray.length && i < maxCol;i++)
         anArray[i] = content.elementAt(i);
   }
   protected InputStream getAsciiStream(int column) throws VirtuosoException
   {
      if(column < 1 || column > maxCol)
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + maxCol,VirtuosoException.BADPARAM);
      Object obj = content.elementAt(column - 1);
      resultSet.wasNull((obj != null)?false:true);
      if(obj != null && obj instanceof VirtuosoBlob)
         return ((VirtuosoBlob)obj).getAsciiStream();
      try
        {
          if(obj != null && obj instanceof String)
            return new ByteArrayInputStream (((String)obj).getBytes ("8859_1"));
 }
      catch (java.io.UnsupportedEncodingException e)
        {
   throw new VirtuosoException (e, VirtuosoException.CASTERROR);
 }
      if(obj != null && obj instanceof byte[])
         return new ByteArrayInputStream((byte[])obj);
      return null;
   }
   protected InputStream getBinaryStream(int column) throws VirtuosoException
   {
      if(column < 1 || column > maxCol)
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + maxCol,VirtuosoException.BADPARAM);
      Object obj = content.elementAt(column - 1);
      resultSet.wasNull((obj != null)?false:true);
      if(obj != null && obj instanceof VirtuosoBlob)
         return ((VirtuosoBlob)obj).getBinaryStream();
      try
        {
          if(obj != null && obj instanceof String)
            return new ByteArrayInputStream (((String)obj).getBytes ("8859_1"));
 }
      catch (java.io.UnsupportedEncodingException e)
        {
   throw new VirtuosoException (e, VirtuosoException.CASTERROR);
 }
      if(obj != null && obj instanceof byte[])
         return new ByteArrayInputStream((byte[])obj);
      return null;
   }
   protected Reader getCharacterStream(int column) throws VirtuosoException
   {
      if(column < 1 || column > maxCol)
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + maxCol,VirtuosoException.BADPARAM);
      Object obj = content.elementAt(column - 1);
      resultSet.wasNull((obj != null)?false:true);
      if(obj != null && obj instanceof VirtuosoBlob)
         return ((VirtuosoBlob)obj).getCharacterStream();
      if(obj != null && obj instanceof String)
 {
   String str = (String)obj;
   VirtuosoStatement stmt = (VirtuosoStatement)resultSet.getStatement();
   if (stmt.connection.charset != null && obj instanceof String)
     {
       int dtp = resultSet.metaData.getColumnDtp (column);
       switch (dtp)
  {
    case VirtuosoTypes.DV_SHORT_STRING_SERIAL:
    case VirtuosoTypes.DV_STRING:
    case VirtuosoTypes.DV_STRICT_STRING:
        try
   {
     str = stmt.connection.uncharsetBytes ((String)obj);
   }
        catch (Exception e)
   {
     str = (String)obj;
   }
  }
     }
   return new StringReader(str);
 }
      if(obj != null && obj instanceof char[])
         return new CharArrayReader((char[])obj);
      return null;
   }
   protected BigDecimal getBigDecimal(int column) throws VirtuosoException
   {
      if(column < 1 || column > maxCol)
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + maxCol,VirtuosoException.BADPARAM);
      Object obj = content.elementAt(column - 1);
      if(obj != null)
      {
         resultSet.wasNull(false);
         if(obj instanceof BigDecimal)
            return (BigDecimal)obj;
         else
            if(obj instanceof Number)
               try
               {
                  return new BigDecimal(((Number)obj).doubleValue());
               }
               catch(ClassCastException e)
               {
                  throw new VirtuosoException("Column does not contain a number.",VirtuosoException.CASTERROR);
               }
            else
               try
               {
                  return new BigDecimal(obj.toString());
               }
               catch(NumberFormatException e)
               {
                  throw new VirtuosoException(obj.toString() + " is not a number.",VirtuosoException.BADFORMAT);
               }
      }
      resultSet.wasNull(true);
      return null;
   }
   protected boolean getBoolean(int column) throws VirtuosoException
   {
      if(column < 1 || column > maxCol)
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + maxCol,VirtuosoException.BADPARAM);
      Object obj = content.elementAt(column - 1);
      if(obj != null)
      {
         resultSet.wasNull(false);
         if (obj instanceof Number)
           return (((Number)obj).intValue() == 0)?false:true;
         if (obj instanceof String)
           return (java.lang.Integer.parseInt((String)obj) == 0)?false:true;
         if (obj instanceof byte[])
           return (java.lang.Integer.parseInt(new String((byte[])obj)) == 0)?false:true;
         return new Boolean(obj.toString()).booleanValue();
      }
      resultSet.wasNull(true);
      return false;
   }
   protected byte getByte(int column) throws VirtuosoException
   {
      if(column < 1 || column > maxCol)
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + maxCol,VirtuosoException.BADPARAM);
      Object obj = content.elementAt(column - 1);
      if(obj != null)
      {
         resultSet.wasNull(false);
         if(obj instanceof Number)
            try
            {
               return ((Number)obj).byteValue();
            }
            catch(ClassCastException e)
            {
               throw new VirtuosoException("Column does not contain a byte.",VirtuosoException.CASTERROR);
            }
         else
            try
            {
               return new Byte(obj.toString()).byteValue();
            }
            catch(NumberFormatException e)
            {
               throw new VirtuosoException(obj.toString() + " is not a byte.",VirtuosoException.BADFORMAT);
            }
      }
      resultSet.wasNull(true);
      return 0;
   }
   protected byte[] getBytes(int column) throws VirtuosoException
   {
      if(column < 1 || column > maxCol)
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + maxCol,VirtuosoException.BADPARAM);
      Object obj = content.elementAt(column - 1);
      if(obj != null)
      {
         resultSet.wasNull(false);
         if (obj instanceof VirtuosoBlob)
           {
             VirtuosoBlob blob = (VirtuosoBlob) obj;
             if (blob.length() > Integer.MAX_VALUE)
               throw new VirtuosoException (
                    "Will not return more than " +
                     Integer.MAX_VALUE +
                     " for a BLOB column. Use getBinaryStream() instead",
               VirtuosoException.ERRORONTYPE);
      return blob.getBytes(1, (int) blob.length());
    }
         else if (obj instanceof byte[])
           return (byte[])obj;
         else if (obj instanceof java.lang.String)
           {
              try {
                return ((String)obj).getBytes ("8859_1");
              } catch (UnsupportedEncodingException e) { return null; }
           }
         else
           throw new VirtuosoException ("getBytes() return undefined on a value of type " + obj.getClass(),
             VirtuosoException.ERRORONTYPE);
      }
      resultSet.wasNull(true);
      return null;
   }
   protected double getDouble(int column) throws VirtuosoException
   {
      if(column < 1 || column > maxCol)
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + maxCol,VirtuosoException.BADPARAM);
      Object obj = content.elementAt(column - 1);
      if(obj != null)
      {
         resultSet.wasNull(false);
         if(obj instanceof Number)
            try
            {
               return ((Number)obj).doubleValue();
            }
            catch(ClassCastException e)
            {
               throw new VirtuosoException("Column does not contain a double.",VirtuosoException.CASTERROR);
            }
         else
            try
            {
               return new Double(obj.toString()).doubleValue();
            }
            catch(NumberFormatException e)
            {
               throw new VirtuosoException(obj.toString() + " is not a double.",VirtuosoException.BADFORMAT);
            }
      }
      resultSet.wasNull(true);
      return 0.0d;
   }
   protected java.sql.Date getDate(int column) throws VirtuosoException
     {
       if(column < 1 || column > maxCol)
  throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + maxCol,VirtuosoException.BADPARAM);
       Object obj = content.elementAt(column - 1);
       if(obj != null)
  {
    resultSet.wasNull(false);
    if(obj instanceof java.sql.Date)
      return java.sql.Date.valueOf (((java.sql.Date)obj).toString());
    else if (obj instanceof String)
      return java.sql.Date.valueOf ((String)obj);
    else
      return java.sql.Date.valueOf (((java.util.Date)obj).toString());
  }
       resultSet.wasNull(true);
       return null;
     }
   protected java.sql.Time getTime(int column) throws VirtuosoException
   {
      if(column < 1 || column > maxCol)
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + maxCol,VirtuosoException.BADPARAM);
      Object obj = content.elementAt(column - 1);
      if(obj != null)
      {
         resultSet.wasNull(false);
         if(obj instanceof java.sql.Time)
            return java.sql.Time.valueOf (((java.sql.Time)obj).toString());
         else if (obj instanceof java.util.Date)
           {
             java.util.Date dt = (java.util.Date) obj;
             java.sql.Time tm = new java.sql.Time (dt.getTime());
             return java.sql.Time.valueOf(tm.toString());
           }
         else if(obj instanceof String)
           {
             return java.sql.Time.valueOf((String)obj);
           }
         else return null;
      }
      resultSet.wasNull(true);
      return null;
   }
   protected java.sql.Timestamp getTimestamp(int column) throws VirtuosoException
     {
       if(column < 1 || column > maxCol)
  throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + maxCol,VirtuosoException.BADPARAM);
       Object obj = content.elementAt(column - 1);
       if(obj != null)
  {
    resultSet.wasNull(false);
    if(obj instanceof java.sql.Timestamp)
      {
        return (java.sql.Timestamp)obj;
      }
    else if (obj instanceof java.sql.Date)
      {
        java.sql.Date dt = (java.sql.Date)obj;
        return new java.sql.Timestamp (dt.getTime());
      }
    else if (obj instanceof java.sql.Time)
      {
        java.sql.Time tm = (java.sql.Time)obj;
        return new java.sql.Timestamp (tm.getTime());
      }
    else if(obj instanceof String)
      {
        java.sql.Timestamp ts;
        ts = Timestamp.valueOf((String)obj);
        return ts;
      }
    return null;
  }
       resultSet.wasNull(true);
       return null;
     }
   protected float getFloat(int column) throws VirtuosoException
   {
      if(column < 1 || column > maxCol)
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + maxCol,VirtuosoException.BADPARAM);
      Object obj = content.elementAt(column - 1);
      if(obj != null)
      {
         resultSet.wasNull(false);
         if(obj instanceof Number)
            try
            {
               return ((Number)obj).floatValue();
            }
            catch(ClassCastException e)
            {
               throw new VirtuosoException("Column does not contain a float.",VirtuosoException.CASTERROR);
            }
         else
            try
            {
               return new Float(obj.toString()).floatValue();
            }
            catch(NumberFormatException e)
            {
               throw new VirtuosoException(obj.toString() + " is not a float.",VirtuosoException.BADFORMAT);
            }
      }
      resultSet.wasNull(true);
      return 0.0f;
   }
   protected int getInt(int column) throws VirtuosoException
   {
      if(column < 1 || column > maxCol)
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + maxCol,VirtuosoException.BADPARAM);
      Object obj = content.elementAt(column - 1);
      if(obj != null)
      {
         resultSet.wasNull(false);
         if(obj instanceof Number)
            try
            {
               return ((Number)obj).intValue();
            }
            catch(ClassCastException e)
            {
               throw new VirtuosoException("Column does not contain an int.",VirtuosoException.CASTERROR);
            }
         else
            try
            {
               return new Integer(obj.toString()).intValue();
            }
            catch(NumberFormatException e)
            {
               throw new VirtuosoException(obj.toString() + " is not an int.",VirtuosoException.BADFORMAT);
            }
      }
      resultSet.wasNull(true);
      return 0;
   }
   protected long getLong(int column) throws VirtuosoException
   {
      if(column < 1 || column > maxCol)
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + maxCol,VirtuosoException.BADPARAM);
      Object obj = content.elementAt(column - 1);
      if(obj != null)
      {
         resultSet.wasNull(false);
         if(obj instanceof Number)
            try
            {
               return ((Number)obj).longValue();
            }
            catch(ClassCastException e)
            {
               throw new VirtuosoException("Column does not contain a long.",VirtuosoException.CASTERROR);
            }
         else
            try
            {
               return new Long(obj.toString()).longValue();
            }
            catch(NumberFormatException e)
            {
               throw new VirtuosoException(obj.toString() + " is not a long.",VirtuosoException.BADFORMAT);
            }
      }
      resultSet.wasNull(true);
      return 0l;
   }
   protected short getShort(int column) throws VirtuosoException
   {
      if(column < 1 || column > maxCol)
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + maxCol,VirtuosoException.BADPARAM);
      Object obj = content.elementAt(column - 1);
      if(obj != null)
      {
         resultSet.wasNull(false);
         if(obj instanceof Number)
            try
            {
               return ((Number)obj).shortValue();
            }
            catch(ClassCastException e)
            {
               throw new VirtuosoException("Column does not contain a short.",VirtuosoException.CASTERROR);
            }
         else
            try
            {
               return new Short(obj.toString()).shortValue();
            }
            catch(NumberFormatException e)
            {
               throw new VirtuosoException(obj.toString() + " is not a short.",VirtuosoException.BADFORMAT);
            }
      }
      resultSet.wasNull(true);
      return 0;
   }
   protected String getString(int column) throws VirtuosoException
   {
      if(column < 1 || column > maxCol)
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + maxCol,VirtuosoException.BADPARAM);
      Object obj = content.elementAt(column - 1);
      if(obj != null)
      {
  int dtp = resultSet.metaData.getColumnDtp (column);
         resultSet.wasNull(false);
  VirtuosoStatement stmt = (VirtuosoStatement)resultSet.getStatement();
  if (stmt.connection.charset != null && obj instanceof String)
    {
      switch (dtp)
        {
   case VirtuosoTypes.DV_SHORT_STRING_SERIAL:
   case VirtuosoTypes.DV_STRING:
   case VirtuosoTypes.DV_STRICT_STRING:
       try
         {
    return stmt.connection.uncharsetBytes ((String)obj);
         }
       catch (Exception e)
         {
    return (String)obj;
         }
        }
    }
  if (obj instanceof java.sql.Timestamp)
    {
      java.sql.Timestamp ts = (java.sql.Timestamp) obj;
      switch (dtp)
        {
   case VirtuosoTypes.DV_DATE:
       return (new java.sql.Date (ts.getTime())).toString();
   case VirtuosoTypes.DV_TIME:
       return (new java.sql.Time (ts.getTime())).toString();
        }
    }
         return obj.toString();
      }
      resultSet.wasNull(true);
      return null;
   }
   protected VirtuosoBlob getBlob(int column) throws VirtuosoException
   {
      if(column < 1 || column > maxCol)
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + maxCol,VirtuosoException.BADPARAM);
      VirtuosoBlob obj = (content.elementAt(column - 1) instanceof String) ?
         new VirtuosoBlob(((String)content.elementAt(column - 1)).getBytes()) :
  (VirtuosoBlob)(content.elementAt(column - 1));
      if(obj != null)
      {
         resultSet.wasNull(false);
         return obj;
      }
      resultSet.wasNull(true);
      return null;
   }
   protected
   VirtuosoBlob
   getClob(int column) throws VirtuosoException
   {
      if(column < 1 || column > maxCol)
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + maxCol,VirtuosoException.BADPARAM);
      VirtuosoBlob obj = (content.elementAt(column - 1) instanceof String) ?
         new VirtuosoBlob(((String)content.elementAt(column - 1)).getBytes()) :
  (VirtuosoBlob)(content.elementAt(column - 1));
      if(obj != null)
      {
         resultSet.wasNull(false);
         return obj;
      }
      resultSet.wasNull(true);
      return null;
   }
   protected Object createDOMT (Object data) throws VirtuosoException
     {
       Object db = null;
       Method parse_mtd = null;
       try
         {
    Class<?> dbfact_cls = Class.forName ("javax.xml.parsers.DocumentBuilderFactory");
    Object dbfact;
    Method newInstMtd, newDbMtd;
    newInstMtd = dbfact_cls.getDeclaredMethod ("newInstance", new Class [0]);
    newDbMtd = dbfact_cls.getDeclaredMethod ("newDocumentBuilder", new Class [0]);
    dbfact = newInstMtd.invoke (null, new Object [0]);
    db = newDbMtd.invoke (dbfact, new Object [0]);
    Class<?> db_cls = Class.forName ("javax.xml.parsers.DocumentBuilder");
    Class<?> parse_args = Class.forName ("java.io.InputStream");
    parse_mtd = db_cls.getDeclaredMethod ("parse", parse_args);
         }
       catch (Throwable t)
         {
    db = null;
         }
       if (db == null || parse_mtd == null)
    return data;
       try
         {
    if (data instanceof String)
      {
        Object [] args = { new ByteArrayInputStream ( ((String)data).getBytes("UTF-8")) };
        data = parse_mtd.invoke (db, args );
      }
    else if (data instanceof VirtuosoBlob)
      {
        Object [] args = { ((VirtuosoBlob)data).getBinaryStream () };
        data = parse_mtd.invoke (db, args);
      }
         }
       catch (Exception e)
         {
    throw new VirtuosoException (e, VirtuosoException.CASTERROR);
         }
       return data;
   }
   protected Object getObject(int column) throws VirtuosoException
   {
      if(column < 1 || column > maxCol)
         throw new VirtuosoException("Bad column number : " + column + " not in 1<n<" + maxCol,VirtuosoException.BADPARAM);
      Object obj = content.elementAt(column - 1);
      if(obj != null)
      {
         resultSet.wasNull(false);
  if (resultSet.metaData.isXml (column))
    {
        obj = createDOMT (obj);
    }
  else if(obj instanceof java.sql.Date)
    {
      obj = java.sql.Date.valueOf (((java.sql.Date)obj).toString());
    }
         else if(obj instanceof java.sql.Time)
           {
             obj = java.sql.Time.valueOf (((java.sql.Time)obj).toString());
           }
         return obj;
      }
      resultSet.wasNull(true);
      return null;
   }
   protected int getRow()
   {
      return (maxCol != content.size()) ? ((Number)content.elementAt(maxCol)).intValue() : 0;
   }
   protected String getRef()
   {
      if (maxCol != content.size())
 {
   openlink.util.Vector v1 = (openlink.util.Vector)content.elementAt(maxCol);
   openlink.util.Vector v2 = (openlink.util.Vector)v1.elementAt (1);
   Object ret = v2.elementAt (0);
   return ret.toString();
 }
      else
 return "";
   }
   protected openlink.util.Vector getBookmark()
   {
     if (maxCol != content.size())
       return (openlink.util.Vector)content.elementAt(maxCol);
     else
       return content;
   }
   public void finalize() throws Throwable
   {
      if(content != null)
      {
         content.removeAllElements();
         content = null;
      }
      resultSet = null;
   }
   public String toString ()
     {
       if (content == null)
  return super.toString();
      StringBuffer buf = new StringBuffer();
      buf.append("{ROW ");
      buf.append (content.toString());
      buf.append("}");
      return buf.toString();
     }
}
