package virtuoso.jdbc4;
import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import java.math.*;
import openlink.util.*;
class VirtuosoOutputStream extends BufferedOutputStream
{
   private VirtuosoConnection connection;
   private byte[] tmp = new byte[16];
   private static final int DefaultBufferSize = 2048;
   VirtuosoOutputStream(VirtuosoConnection connection, OutputStream output) throws IOException
   {
      this (connection,output,DefaultBufferSize);
   }
   VirtuosoOutputStream(VirtuosoConnection connection, OutputStream output, int size) throws IOException
   {
     super (output, size);
     this.connection = connection;
   }
   VirtuosoOutputStream(VirtuosoConnection connection, Socket output) throws IOException
   {
      this (connection,output.getOutputStream());
   }
   VirtuosoOutputStream(VirtuosoConnection connection, Socket output, int size) throws IOException
   {
      this (connection,output.getOutputStream(),size);
   }
   protected boolean isClosed()
   {
      return (out == null);
   }
   protected void write_object(Object obj) throws IOException, VirtuosoException
   {
     int tag;
     switch(tag = whatIs(obj))
       {
  case VirtuosoTypes.DV_LIST_OF_POINTER:
        {
   LinkedList o = (LinkedList)obj;
   int length = o.size();
   write(VirtuosoTypes.DV_ARRAY_OF_POINTER);
   writeint(length);
   for(ListIterator it = o.listIterator(); it.hasNext(); )
     {
       write_object(it.next());
     }
   return;
        }
  case VirtuosoTypes.DV_ARRAY_OF_POINTER:
        {
   openlink.util.Vector o = (openlink.util.Vector)obj;
   int length = o.size();
   write(tag);
   writeint(length);
   for(int i = 0; i < length; i++)
     {
       write_object(o.elementAt(i));
     }
   return;
        }
  case VirtuosoTypes.DV_ARRAY_OF_LONG:
        {
   VectorOfLong o = (VectorOfLong)obj;
   int length = o.size();
   write(tag);
   writeint(length);
   for(int i = 0; i < length; i++)
     {
       writelongint(((Long)o.elementAt(i)).longValue());
     }
   return;
        }
  case VirtuosoTypes.DV_ARRAY_OF_LONG_PACKED:
        {
   openlink.util.Vector o = (openlink.util.Vector)obj;
   int length = o.size();
   write(tag);
   writeint(length);
   for(int i = 0; i < length; i++)
     {
       writeint(((Number)o.elementAt(i)).longValue());
     }
   return;
        }
  case VirtuosoTypes.DV_ARRAY_OF_DOUBLE:
        {
   VectorOfDouble o = (VectorOfDouble)obj;
   int length = o.size();
   write(tag);
   writeint(length);
   for(int i = 0; i < length; i++)
     {
       writerawdouble(((Double)o.elementAt(i)).doubleValue());
     }
   return;
        }
  case VirtuosoTypes.DV_ARRAY_OF_FLOAT:
        {
   VectorOfFloat o = (VectorOfFloat)obj;
   int length = o.size();
   write(tag);
   writeint(length);
   for(int i = 0; i < length; i++)
     {
       writerawfloat(((Float)o.elementAt(i)).floatValue());
     }
   return;
        }
  case VirtuosoTypes.DV_SHORT_STRING_SERIAL:
  case VirtuosoTypes.DV_STRING:
  case VirtuosoTypes.DV_C_STRING:
  case VirtuosoTypes.DV_WIDE:
  case VirtuosoTypes.DV_LONG_WIDE:
        {
   if (obj instanceof VirtuosoExplicitString)
     ((VirtuosoExplicitString)obj).write(this);
   else
     writestring((String)obj);
   return;
        }
  case VirtuosoTypes.DV_SINGLE_FLOAT:
        {
   writefloat(((Float)obj).floatValue());
   return;
        }
  case VirtuosoTypes.DV_DOUBLE_FLOAT:
        {
   writedouble(((Double)obj).doubleValue());
   return;
        }
  case VirtuosoTypes.DV_NULL:
  case VirtuosoTypes.DV_DB_NULL:
        {
   write(tag);
   return;
        }
  case VirtuosoTypes.DV_SHORT_CONT_STRING:
  case VirtuosoTypes.DV_LONG_CONT_STRING:
        {
   String o = (String)obj;
   write(o.getBytes(),0,o.length());
   return;
        }
  case VirtuosoTypes.DV_SHORT_INT:
        {
                 if (obj instanceof Boolean)
     writeint((((Boolean)obj).booleanValue()==true)?1:0);
                 else if (obj instanceof Byte)
     writeint(((Byte)obj).shortValue());
                 else
   writeint(((Number)obj).shortValue());
   return;
        }
  case VirtuosoTypes.DV_LONG_INT:
        {
   writeint(((Number)obj).longValue());
   return;
        }
  case VirtuosoTypes.DV_INT64:
        {
   writelong(((Number)obj).longValue());
   return;
        }
  case VirtuosoTypes.DV_DATETIME:
  case VirtuosoTypes.DV_TIMESTAMP_OBJ:
  case VirtuosoTypes.DV_TIMESTAMP:
  case VirtuosoTypes.DV_TIME:
  case VirtuosoTypes.DV_DATE:
        {
   writeDate(obj,tag);
   return;
        }
  case VirtuosoTypes.BYTEARRAY:
      throw new VirtuosoException ("invalid type", VirtuosoException.NOTIMPLEMENTED);
         case VirtuosoTypes.DV_BIN:
               {
                 byte [] bobj = (byte[]) obj;
   write(VirtuosoTypes.DV_BIN);
                 write(bobj.length);
   write(bobj, 0, bobj.length);
   return;
        }
         case VirtuosoTypes.DV_LONG_BIN:
               {
                 byte [] bobj = (byte[]) obj;
   write(VirtuosoTypes.DV_LONG_BIN);
                 writelongint(bobj.length);
   write(bobj, 0, bobj.length);
   return;
        }
  case VirtuosoTypes.DV_BLOB_BIN:
        {
   write(VirtuosoTypes.DV_BLOB_HANDLE);
   writelongint(1l);
   writelongint(((VirtuosoBlob)obj).hashCode());
   writelongint(((VirtuosoBlob)obj).length());
   writelongint(((VirtuosoBlob)obj).key_id);
   writelongint(((VirtuosoBlob)obj).frag_no);
   writelongint(((VirtuosoBlob)obj).dir_page);
   writelongint(((VirtuosoBlob)obj).bh_timestamp);
   write_object(((VirtuosoBlob)obj).pages);
   return;
        }
  case VirtuosoTypes.DV_BLOB:
        {
   write(VirtuosoTypes.DV_BLOB_HANDLE);
   writelongint(1l);
   writelongint(((VirtuosoBlob)obj).hashCode());
   writelongint(((VirtuosoBlob)obj).length());
   writelongint(((VirtuosoBlob)obj).key_id);
   writelongint(((VirtuosoBlob)obj).frag_no);
   writelongint(((VirtuosoBlob)obj).dir_page);
   writelongint(((VirtuosoBlob)obj).bh_timestamp);
   write_object(((VirtuosoBlob)obj).pages);
   return;
        }
  case VirtuosoTypes.DV_NUMERIC:
        {
                 if (obj instanceof Long)
                   writenumeric (BigDecimal.valueOf (((Long)obj).longValue()));
                 else
     writenumeric((BigDecimal)obj);
   return;
        }
  case VirtuosoTypes.DV_BOX_FLAGS:
        {
   VirtuosoExtendedString o = (VirtuosoExtendedString) obj;
   write (tag);
   writelongint (o.strType);
   write_object (o.str);
   return;
        }
  case VirtuosoTypes.DV_RDF:
        {
     writeRdfBox ((VirtuosoRdfBox) obj);
     return;
        }
  default:
      if (obj instanceof Serializable)
        {
   writeobject (obj);
        }
      else
        throw new VirtuosoException(
     "Tag " + tag +
     " not defined and the object " +
     obj.getClass().getName() +
     "is not serializable.",
     VirtuosoException.BADTAG);
       }
   }
   private void writefloat(float n) throws IOException
   {
      write(VirtuosoTypes.DV_SINGLE_FLOAT);
      writerawfloat(n);
   }
   private void writerawfloat(float m) throws IOException
   {
      writelongint(Float.floatToIntBits(m));
   }
   private void writedouble(double n) throws IOException
   {
      write(VirtuosoTypes.DV_DOUBLE_FLOAT);
      writerawdouble(n);
   }
   private void writerawdouble(double m) throws IOException
   {
      writerawlong(Double.doubleToLongBits(m));
   }
   private void writeint(long n) throws IOException
   {
      if((n >= -128) && (n < 128))
      {
         write(VirtuosoTypes.DV_SHORT_INT);
         write((byte)n);
      }
      else
      {
         write(VirtuosoTypes.DV_LONG_INT);
         writelongint(n);
      }
   }
   protected void writelongint(long data) throws IOException
   {
     tmp[0] = ((byte) ((data >> 24) & 0xFF));
     tmp[1] = ((byte) ((data >> 16) & 0xFF));
     tmp[2] = ((byte) ((data >> 8) & 0xFF));
     tmp[3] = ((byte) (data & 0xFF));
     write(tmp, 0, 4);
   }
   protected void writelongint(int data) throws IOException
   {
     tmp[0] = ((byte) ((data >> 24) & 0xFF));
     tmp[1] = ((byte) ((data >> 16) & 0xFF));
     tmp[2] = ((byte) ((data >> 8) & 0xFF));
     tmp[3] = ((byte) (data & 0xFF));
     write(tmp, 0, 4);
   }
   protected void writeshort (short data) throws IOException
   {
     tmp[0] = ((byte) ((data >> 8) & 0xFF));
     tmp[1] = ((byte) (data & 0xFF));
     write(tmp, 0, 2);
   }
   protected void writelong(long data) throws IOException
   {
     write(VirtuosoTypes.DV_INT64);
     writerawlong(data);
   }
   protected void writerawlong(long data) throws IOException
   {
     tmp[0] = ((byte) ((data >> 56) & 0xFF));
     tmp[1] = ((byte) ((data >> 48) & 0xFF));
     tmp[2] = ((byte) ((data >> 40) & 0xFF));
     tmp[3] = ((byte) ((data >> 32) & 0xFF));
     tmp[4] = ((byte) ((data >> 24) & 0xFF));
     tmp[5] = ((byte) ((data >> 16) & 0xFF));
     tmp[6] = ((byte) ((data >> 8) & 0xFF));
     tmp[7] = ((byte) (data & 0xFF));
     write(tmp, 0, 8);
   }
   private void writenumeric(BigDecimal bd) throws IOException, VirtuosoException
   {
      write(VirtuosoTypes.DV_NUMERIC);
      if(bd.doubleValue() == Double.NaN)
      {
         write(3);
         write(0x8);
         write(0);
         write(VirtuosoTypes.DV_NULL);
         return;
      }
      else
         if(bd.doubleValue() == Double.NEGATIVE_INFINITY)
         {
            write(3);
            write(0x11);
            write(0);
            write(VirtuosoTypes.DV_NULL);
            return;
         }
         else
            if(bd.doubleValue() == Double.POSITIVE_INFINITY)
            {
               write(3);
               write(0x10);
               write(0);
               write(VirtuosoTypes.DV_NULL);
               return;
            }
      int flags = ((bd.signum() == -1) ? 1 : 0);
      if(bd.signum() == -1)
         bd = bd.negate();
      String bcd = bd.toString();
      int len = bcd.length();
      if (len - 1 > 40)
 throw new VirtuosoException ("Numeric " + bcd + " too large to transfer to Virtuoso as numeric",
  VirtuosoException.BADPARAM);
      int numbefdot = bcd.indexOf('.');
      int i = 0;
      if(numbefdot < 0)
         numbefdot = len;
      if((numbefdot & 0x1) == 0x1)
         flags |= 0x4;
      int numaftdot = len - numbefdot - 1;
      if(numaftdot < 0)
         numaftdot = 0;
      if((numaftdot & 0x1) == 0x1)
         flags |= 0x2;
      write(((len + (((flags & 0x2) == 0x2) ? 1 : 0) + (((flags & 0x4) == 0x4) ? 1 : 0)) >> 1) + 2);
      write(flags);
      write((numbefdot + (((flags & 0x4) == 0x4) ? 1 : 0)) >> 1);
      if((flags & 0x04) == 0x04)
         write(bcd.charAt(i++) - '0');
      for(;i < numbefdot;i += 2)
         write(((bcd.charAt(i) - '0') << 4) | (bcd.charAt(i + 1) - '0'));
      if(numaftdot == 0)
         return;
      for(i++;i <= numaftdot + numbefdot - (((flags & 0x2) == 0x2)?1:0);i += 2)
         write(((bcd.charAt(i) - '0') << 4) | (bcd.charAt(i + 1) - '0'));
      if((flags & 0x02) == 0x02)
         write((bcd.charAt(i++) - '0') << 4);
   }
   private void writeRdfBox (VirtuosoRdfBox rb) throws IOException, VirtuosoException
   {
      int flags = 0;
      write (VirtuosoTypes.DV_RDF);
      if (rb.rb_ro_id != 0)
 flags |= VirtuosoRdfBox.RBS_OUTLINED;
      if (rb.rb_ro_id > 0xffffffffL)
 flags |= VirtuosoRdfBox.RBS_64;
      if (VirtuosoRdfBox.RDF_BOX_DEFAULT_LANG != rb.rb_lang)
 flags |= VirtuosoRdfBox.RBS_HAS_LANG;
      if (VirtuosoRdfBox.RDF_BOX_DEFAULT_TYPE != rb.rb_type)
 flags |= VirtuosoRdfBox.RBS_HAS_TYPE;
      if (rb.rb_is_complete)
 flags |= VirtuosoRdfBox.RBS_COMPLETE;
      write (flags);
      write_object (rb.rb_box);
      if (rb.rb_ro_id != 0)
      {
 if (rb.rb_ro_id > 0xffffffffL)
 {
   writerawlong(rb.rb_ro_id);
 }
 else
 {
   writelongint (rb.rb_ro_id);
 }
      }
      if (VirtuosoRdfBox.RDF_BOX_DEFAULT_TYPE != rb.rb_type)
 writeshort (rb.rb_type);
      if (VirtuosoRdfBox.RDF_BOX_DEFAULT_LANG != rb.rb_lang)
 writeshort (rb.rb_lang);
   }
   private void writeobject(Object obj) throws IOException
   {
     ByteArrayOutputStream bos = new ByteArrayOutputStream ();
     ObjectOutputStream oos = new ObjectOutputStream (bos);
     oos.writeObject (obj);
     oos.flush();
     byte[] bytes = bos.toByteArray();
     oos = null;
     bos = null;
     write (VirtuosoTypes.DV_OBJECT);
     writelongint (VirtuosoTypes.UDT_JAVA_CLIENT_OBJECT_ID);
     if (bytes.length > 255)
       {
  write (VirtuosoTypes.DV_LONG_BIN);
  writelongint (bytes.length);
       }
     else
       {
  write (VirtuosoTypes.DV_BIN);
  write (bytes.length);
       }
     write (bytes, 0, bytes.length);
     bytes = null;
   }
   private void writestring(String string) throws IOException
   {
     int len = string.length();
     byte[] bytes = new byte [len];
     for (int i = 0; i < len; i++)
       bytes[i] = (byte)string.charAt(i);
     if(len < 256)
      {
         write(VirtuosoTypes.DV_SHORT_STRING_SERIAL);
         write(len);
      }
     else
      {
         write(VirtuosoTypes.DV_STRING);
         writelongint(len);
      }
     write(bytes, 0, len);
   }
   private void writeDate(Object obj, int tag) throws IOException
   {
      int temp, tz;
      int _year, _month, _day, _hour, _minute, _second, _frac, yday;
      java.util.Date date = (java.util.Date)obj;
      GregorianCalendar cal = new GregorianCalendar();
      cal.setTime((java.util.Date)obj);
      if (!(obj instanceof java.sql.Time))
 {
   _year = cal.get (Calendar.YEAR);
   _month = cal.get (Calendar.MONTH) + 1;
   _day = cal.get (Calendar.DAY_OF_MONTH);
   yday = date2num(_year, _month, _day);
 }
      else
 yday = _year = _month = _day = 0;
      if (!(obj instanceof java.sql.Date))
 {
   _hour = cal.get(Calendar.HOUR_OF_DAY);
   _minute = cal.get (Calendar.MINUTE);
   _second = cal.get (Calendar.SECOND);
   if (obj instanceof java.sql.Timestamp)
     _frac = ((Timestamp)obj).getNanos();
   else
     _frac = 0;
 }
      else
 {
   _hour = _minute = _second = _frac = 0;
 }
      tz = cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET);
      tz = tz / (60*1000);
      if(tz != 0)
 {
   int day = yday;
   int sec = 0;
   sec = VirtuosoInputStream.time_to_sec (0, _hour, _minute, _second);
   sec -= 60 * tz;
   if (sec < 0)
     {
       day = day - (1 + ((-sec) / VirtuosoInputStream.SPERDAY));
       sec = sec % VirtuosoInputStream.SPERDAY;
       if (sec == 0)
  day++;
       sec = VirtuosoInputStream.SPERDAY + sec;
     }
   else
     {
       day = day + sec / VirtuosoInputStream.SPERDAY;
       sec = sec % VirtuosoInputStream.SPERDAY;
     }
   int dummy_day = sec / VirtuosoInputStream.SPERDAY;
   _hour = (sec - (dummy_day * VirtuosoInputStream.SPERDAY)) / (60 * 60);
   _minute = (sec - (dummy_day * VirtuosoInputStream.SPERDAY) - (_hour * 60 * 60)) / 60;
   _second = sec % 60;
   yday = day;
 }
      write(VirtuosoTypes.DV_DATETIME);
      write(yday >> 16);
      write(yday >> 8);
      write(yday);
      write(_hour);
      yday = _second;
      write(((_minute << 2) & 0xfc) | ((yday >> 4) & 0x3));
      temp = _frac;
      write((yday << 4) | ((temp >> 16) & 0xf));
      write(temp >> 8);
      write(temp);
      if (tz < 0)
 tz = 65536 + tz;
      int tz_bytes[] = new int[2];
      tz_bytes[0] = (tz >> 8);
      tz_bytes[1] = tz & 0xFF;
      byte type = (byte) VirtuosoTypes.DT_TYPE_DATETIME;
      if (obj instanceof java.sql.Time)
 type = (byte) VirtuosoTypes.DT_TYPE_TIME;
      if (obj instanceof java.sql.Date)
 type = (byte) VirtuosoTypes.DT_TYPE_DATE;
      tz_bytes[0] &= 0x07;
      tz_bytes[0] |= (type << 5);
      write(tz_bytes[0]);
      write(tz_bytes[1]);
   }
   static final int cumdays_in_month[] = { 0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334 };
   public static int date2num(int year, int month, int day)
   {
      int julian_days = (year - 1) * 365 + ((year - 1) >> 2);
      if(year > 1582 ||
   ((year == 1582)
    && (month > 10 ||
      ((month == 10)
       && (day > 14)))))
         julian_days -= 10;
      if(year > 1582)
      {
         julian_days += (((year - 1) / 400) - (1582 / 400));
         julian_days -= (((year - 1) / 100) - (1582 / 100));
         if(((1582 % 100) == 0) && ((1582 % 400) != 0))
            julian_days--;
      }
      julian_days += cumdays_in_month[month - 1];
      julian_days += day;
      if(days_in_february(year) == 29 && month > 2)
         julian_days++;
      return julian_days;
   }
   public static int days_in_february(int year)
   {
      int day;
      if((year > 1582) ||
   ((year == 1582)
    &&
    (10 == 1 ||
     ((10 == 2)
      && (14 >= 28)))))
         day = ((year & 3) != 0) ? 28 : (((year % 100) == 0 && (year % 400) != 0) ? 28 : 29);
      else
         day = ((year & 3) != 0) ? 28 : 29;
      if(year == 4)
         day--;
      return day;
   }
   private int whatIs(Object obj)
   {
      if(obj == null)
 return VirtuosoTypes.DV_NULL;
      if(obj instanceof VirtuosoNullParameter)
         return VirtuosoTypes.DV_DB_NULL;
      if(obj instanceof VectorOfLong)
         return VirtuosoTypes.DV_ARRAY_OF_LONG;
      if(obj instanceof VectorOfDouble)
         return VirtuosoTypes.DV_ARRAY_OF_DOUBLE;
      if(obj instanceof VectorOfFloat)
         return VirtuosoTypes.DV_ARRAY_OF_FLOAT;
      if(obj instanceof LinkedList)
         return VirtuosoTypes.DV_LIST_OF_POINTER;
      if(obj instanceof openlink.util.Vector)
         return VirtuosoTypes.DV_ARRAY_OF_POINTER;
      if(obj instanceof Short)
         return VirtuosoTypes.DV_SHORT_INT;
      if(obj instanceof Boolean)
         return VirtuosoTypes.DV_SHORT_INT;
      if(obj instanceof Byte)
         return VirtuosoTypes.DV_SHORT_INT;
      if(obj instanceof Long)
      {
         Long o = (Long)obj;
         if (o.longValue () >= -128 && o.longValue() < 128)
           return VirtuosoTypes.DV_SHORT_INT;
         else if (o.longValue () >= (long) Integer.MIN_VALUE && o.longValue() <= (long) Integer.MAX_VALUE)
    return VirtuosoTypes.DV_LONG_INT;
         else
           return VirtuosoTypes.DV_INT64;
      }
      if(obj instanceof Integer)
      {
         Integer o = (Integer)obj;
         return ((o.longValue() >= -128) && (o.longValue() < 128)) ? VirtuosoTypes.DV_SHORT_INT : VirtuosoTypes.DV_LONG_INT;
      }
      if(obj instanceof Float)
         return VirtuosoTypes.DV_SINGLE_FLOAT;
      if(obj instanceof Double)
         return VirtuosoTypes.DV_DOUBLE_FLOAT;
      if(obj instanceof String)
      {
         String o = (String)obj;
         return (o.length() < 256) ? VirtuosoTypes.DV_SHORT_STRING_SERIAL : VirtuosoTypes.DV_STRING;
      }
      if(obj instanceof BigDecimal)
         return VirtuosoTypes.DV_NUMERIC;
      if(obj instanceof byte[])
         return ((byte [])obj).length < 256 ? VirtuosoTypes.DV_BIN : VirtuosoTypes.DV_LONG_BIN;
      if(obj instanceof java.sql.Date)
         return VirtuosoTypes.DV_DATE;
      if(obj instanceof java.sql.Time)
         return VirtuosoTypes.DV_TIME;
      if(obj instanceof java.sql.Timestamp)
         return VirtuosoTypes.DV_TIMESTAMP;
      if(obj instanceof java.util.Date)
         return VirtuosoTypes.DV_DATETIME;
      if(obj instanceof Clob)
         return VirtuosoTypes.DV_BLOB;
      if(obj instanceof Blob)
         return VirtuosoTypes.DV_BLOB_BIN;
      if (obj instanceof VirtuosoExplicitString)
 {
   VirtuosoExplicitString sobj = (VirtuosoExplicitString)obj;
   return ((VirtuosoExplicitString)obj).getDtp();
 }
      if (obj instanceof VirtuosoExtendedString)
 return VirtuosoTypes.DV_BOX_FLAGS;
      if (obj instanceof VirtuosoRdfBox)
 return VirtuosoTypes.DV_RDF;
      return 0;
   }
}
