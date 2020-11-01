package virtuoso.jdbc4;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.math.*;
import openlink.util.*;
class VirtuosoInputStream extends BufferedInputStream
{
   private VirtuosoConnection connection;
   private byte[] tmp = new byte[16];
    public int read () throws IOException
      {
 int c = super.read();
 if (c == -1)
   throw new IOException ("Connection to the server lost");
 return c;
      }
    public int read(byte[] b, int off, int len) throws IOException
      {
 int c = super.read (b, off, len);
 if (c == -1)
   throw new IOException ("Connection to the server lost");
 return c;
      }
    private static final int DefaultBufferSize = 2048;
   VirtuosoInputStream(VirtuosoConnection connection, InputStream input) throws IOException
   {
      this (connection,input,DefaultBufferSize);
   }
   VirtuosoInputStream(VirtuosoConnection connection, InputStream input, int size) throws IOException
   {
     super (input, size);
      this.connection = connection;
   }
   VirtuosoInputStream(VirtuosoConnection connection, Socket input) throws IOException
   {
      this (connection,input.getInputStream());
   }
   VirtuosoInputStream(VirtuosoConnection connection, Socket input, int size) throws IOException
   {
      this (connection,input.getInputStream(),size);
   }
   protected boolean isClosed()
   {
      return (in == null);
   }
   protected Object read_object() throws IOException, EOFException, VirtuosoException
   {
     int tag = read();
     Object res;
     try
       {
         switch(tag)
           {
             case VirtuosoTypes.DV_NULL:
                   {
                     return new Short((short)0);
                   }
             case VirtuosoTypes.DV_DB_NULL:
                   {
                     return null;
                   }
             case VirtuosoTypes.DV_ARRAY_OF_POINTER:
             case VirtuosoTypes.DV_LIST_OF_POINTER:
                   {
                     int n = readint();
                     Object[] array = new Object[(int)n];
                     for(int i = 0;i < n;i++)
                       array[i] = read_object();
       res = new openlink.util.Vector(array);
                     return res;
                   }
             case VirtuosoTypes.DV_ARRAY_OF_LONG:
                   {
                     int n = readint();
                     Object[] array = new Object[(int)n];
                     for(int i = 0;i < n;i++)
                       array[i] = new Long(readlongint());
                     res = new VectorOfLong(array);
                     return res;
                   }
             case VirtuosoTypes.DV_ARRAY_OF_LONG_PACKED:
                   {
                     int n = readint();
                     Object[] array = new Object[(int)n];
                     for(int i = 0;i < n;i++)
                       array[i] = new Long(readlongint());
                     res = new VectorOfLong(array);
                     return res;
                   }
             case VirtuosoTypes.DV_ARRAY_OF_DOUBLE:
                   {
                     int n = readint();
                     Object[] array = new Object[(int)n];
                     for(int i = 0;i < n;i++)
                       array[i] = new Double(readdouble());
                     res = new VectorOfDouble(array);
                     return res;
                   }
             case VirtuosoTypes.DV_ARRAY_OF_FLOAT:
                   {
                     int n = readint();
                     Object[] array = new Object[(int)n];
                     for(int i = 0;i < n;i++)
                       array[i] = new Float(readfloat());
                     res = new VectorOfFloat(array);
                     return res;
                   }
             case VirtuosoTypes.DV_LONG_WIDE:
                   {
                     int n = readlongint();
                     byte[] array = new byte[(int)n];
                     for(int i = read(array,0,(int)n) ; i != n ; i+=read(array,i,(int)n-i));
                     return convByte2UTF(array);
                   }
             case VirtuosoTypes.DV_WIDE:
                   {
                     int n = readshortint();
                     byte[] array = new byte[n];
                     for(int i = read(array,0,(int)n) ; i != n ; i+=read(array,i,(int)n-i));
       return convByte2UTF(array);
                   }
             case VirtuosoTypes.DV_C_STRING:
             case VirtuosoTypes.DV_STRING:
             case VirtuosoTypes.DV_LONG_CONT_STRING:
                   {
                     int n = readlongint();
                     byte[] array = new byte[(int)n];
                     for(int i = read(array,0,(int)n) ; i != n ; i+=read(array,i,(int)n-i));
       if (connection.charset_utf8)
           return convByte2UTF(array);
       else
           return convByte2Ascii(array);
                   }
      case VirtuosoTypes.DV_BOX_FLAGS:
     {
                     int flags = readlongint();
       Object str = read_object ();
                     res = new VirtuosoExtendedString ((String) str, flags);
                     return res;
     }
             case VirtuosoTypes.DV_LONG_BIN:
                   {
                     int n = readlongint();
                     byte[] array = new byte[(int)n];
                     for(int i = read(array,0,(int)n) ; i != n ; i+=read(array,i,(int)n-i));
                     return array;
                   }
             case VirtuosoTypes.DV_C_SHORT:
             case VirtuosoTypes.DV_SHORT_STRING_SERIAL:
             case VirtuosoTypes.DV_SHORT_CONT_STRING:
                   {
                     int n = readshortint();
                     byte[] array = new byte[n];
                     for(int i = read(array,0,(int)n) ; i != n ; i+=read(array,i,(int)n-i));
       if (connection.charset_utf8)
           return convByte2UTF(array);
       else
           return convByte2Ascii(array);
                   }
             case VirtuosoTypes.DV_BIN:
                   {
                     int n = readshortint();
                     byte[] array = new byte[n];
                     for(int i = read(array,0,(int)n) ; i != n ; i+=read(array,i,(int)n-i));
                     return array;
                   }
             case VirtuosoTypes.DV_SINGLE_FLOAT:
                   {
                     res = new Float(readfloat());
                     return res;
                   }
             case VirtuosoTypes.DV_DOUBLE_FLOAT:
                   {
                     res = new Double(readdouble());
                     return res;
                   }
             case VirtuosoTypes.DV_SHORT_INT:
                   {
       int ret = readshortint();
       if (ret > 127)
         ret = ret - 256;
                     res = new Short((short)ret);
                     return res;
                   }
             case VirtuosoTypes.DV_LONG_INT:
                   {
                     res = new Integer(readlongint());
                     return res;
                   }
             case VirtuosoTypes.DV_DATETIME:
             case VirtuosoTypes.DV_TIMESTAMP_OBJ:
             case VirtuosoTypes.DV_TIMESTAMP:
             case VirtuosoTypes.DV_TIME:
             case VirtuosoTypes.DV_DATE:
                   {
                     res = readDate(tag);
                     return res;
                   }
             case VirtuosoTypes.DV_BLOB_HANDLE:
             case VirtuosoTypes.DV_BLOB_WIDE_HANDLE:
                   {
                     res = new VirtuosoBlob(connection,readlongint(),readlongint(),readlongint(),readlongint(),readlongint(),readlongint(), readlongint(), read_object(), tag);
                     return res;
                   }
             case VirtuosoTypes.DV_NUMERIC:
                   {
                     res = readNumeric();
                     return res;
                   }
      case VirtuosoTypes.DV_OBJECT:
     {
       res = readObject();
       return res;
     }
             case VirtuosoTypes.DV_STRING_SESSION:
                   {
       int flags = read();
       ByteArrayOutputStream os = new ByteArrayOutputStream ();
       do
         {
    int part_tag = read ();
    if (part_tag != VirtuosoTypes.DV_STRING &&
     part_tag != VirtuosoTypes.DV_SHORT_STRING_SERIAL)
      {
        throw new VirtuosoException (
         "Invalid data (tag=" + part_tag + ") in deserializing a string session",
         "42000",
         VirtuosoException.BADTAG);
      }
    int n = (part_tag == VirtuosoTypes.DV_STRING) ? readlongint() : read ();
    if (n > 0)
      {
        byte[] array = new byte[(int)n];
        for(int i = read(array,0,(int)n) ; i != n ; i+=read(array,i,(int)n-i));
        os.write (array, 0, n);
      }
    else
      break;
         }
       while (true);
                     res = os.toString ((flags & 0x1) != 0 ? "UTF-8" : "8859_1");
                     return res;
                   }
             case VirtuosoTypes.DV_IRI_ID:
                   {
                     res = new Integer(readlongint());
                     return res;
                   }
             case VirtuosoTypes.DV_IRI_ID_8:
             case VirtuosoTypes.DV_INT64:
                   {
                     res = new Long(readlong());
                     return res;
                   }
      case VirtuosoTypes.DV_RDF:
     {
       res = readRdfBox ();
                     return res;
                   }
             default:
                 throw new VirtuosoException("Tag " + tag + " not defined.",VirtuosoException.BADTAG);
           }
       }
     catch (ClassCastException e)
       {
         if (VirtuosoFuture.rpc_log != null)
           {
                 VirtuosoFuture.rpc_log.println ("  **(conn " + connection.hashCode() + ") **** runtime " +
                     e.getClass().getName() + " encountered while reading tag " + tag);
                 e.printStackTrace(VirtuosoFuture.rpc_log);
           }
           throw new VirtuosoException(e.getClass().getName() + ":" + e.getMessage(),VirtuosoException.IOERROR);
       }
   }
   private final String convByte2UTF(byte[] data) throws IOException {
        int utflen = data.length;
        char[] c_arr = new char[utflen];
        char bad_char = '?';
        int c, c2, c3;
        int count = 0;
        int ch_count=0;
        while (count < utflen) {
            c = (int) data[count] & 0xff;
            if (c > 127) break;
            count++;
            c_arr[ch_count++]=(char)c;
        }
        while (count < utflen) {
            c = (int) data[count] & 0xff;
            switch (c >> 4) {
                case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                    count++;
                    c_arr[ch_count++]=(char)c;
                    break;
                case 12: case 13:
                    count += 2;
                    if (count > utflen) {
                        c_arr[ch_count++]=(char)c;
                    } else {
                        c2 = (int) data[count-1];
                        if ((c2 & 0xC0) != 0x80)
                          c_arr[ch_count++] = bad_char;
                        else
                          c_arr[ch_count++] = (char)(((c & 0x1F) << 6) | (c2 & 0x3F));
                    }
                    break;
                case 14:
                    count += 3;
                    if (count > utflen) {
                        c_arr[ch_count++]=(char)c;
                    } else {
                        c2 = (int) data[count-2];
                        c3 = (int) data[count-1];
                        if (((c2 & 0xC0) != 0x80) || ((c3 & 0xC0) != 0x80))
                          c_arr[ch_count++] = bad_char;
                        else
                          c_arr[ch_count++] = (char)(((c & 0x0F) << 12) |
                                                    ((c2 & 0x3F) << 6) |
                                                    ((c3 & 0x3F) << 0));
                    }
                    break;
                default:
                    count++;
                    c_arr[ch_count++] = bad_char;
            }
        }
        return new String(c_arr, 0, ch_count);
    }
   private final String convByte2Ascii(byte[] data) throws IOException {
        int len = data.length;
        char[] c_arr = new char[len];
        for(int i=0; i < len; i++)
          c_arr[i] = (char)(data[i] & 0xff);
        return new String(c_arr, 0, len);
    }
   private int readint() throws IOException
   {
      return (read() == VirtuosoTypes.DV_SHORT_INT) ? readshortint() : readlongint();
   }
   private int readshortint() throws IOException
   {
      return read();
   }
   private short readshort() throws IOException
   {
      int retVal;
      for(int i = read(tmp,0, 2) ; i != 2 ; i+=read(tmp,i,2-i));
      retVal = ((int) tmp[0] & 0xFF) << 8;
      return (short)(retVal | ((int) tmp[1] & 0xFF));
   }
   private int readlongint() throws IOException
   {
      int retVal;
      for(int i = read(tmp,0, 4) ; i != 4 ; i+=read(tmp,i,4-i));
      retVal = ((int) tmp[0] & 0xFF) << 24;
      retVal |= ((int) tmp[1] & 0xFF) << 16;
      retVal |= ((int) tmp[2] & 0xFF) << 8;
      return retVal | ((int) tmp[3] & 0xFF);
   }
   private long readlong() throws IOException
   {
      long retVal;
      for(int i = read(tmp,0, 8) ; i != 8 ; i+=read(tmp,i,8-i));
      retVal = ((long) tmp[0] & 0xFF) << 56;
      retVal |= ((long) tmp[1] & 0xFF) << 48;
      retVal |= ((long) tmp[2] & 0xFF) << 40;
      retVal |= ((long) tmp[3] & 0xFF) << 32;
      retVal |= ((long) tmp[4] & 0xFF) << 24;
      retVal |= ((long) tmp[5] & 0xFF) << 16;
      retVal |= ((long) tmp[6] & 0xFF) << 8;
      return retVal | ((long) tmp[7] & 0xFF);
   }
   private float readfloat() throws IOException
   {
      return Float.intBitsToFloat(readlongint());
   }
   private double readdouble() throws IOException
   {
     return Double.longBitsToDouble (readlong());
   }
   private BigDecimal readNumeric() throws IOException, VirtuosoException
   {
      int n = readshortint(), i = 0;
      int len, scale, rp, ep;
      boolean isneg, isinvalid;
      byte array[] = new byte[n + 2];
      byte dp[] = new byte[n << 1];
      BigDecimal bd;
      if (array!=null && dp!=null)
      {
        array[0] = (byte)VirtuosoTypes.DV_NUMERIC;
 array[1] = (byte)n;
 for(int inx = read(array,2,n) ; inx != n ; inx+=read(array,inx + 2,n-inx));
      }
      else
        return null;
      dp[0] = (byte) '0';
      len = array[3] << 1;
      scale = (array[1] - array[3] - 2) << 1;
      isneg = ((array[2] & 0x1) == 0x1) ? true : false;
      isinvalid = ((array[2] & 0x18) == 0x18) ? true : false;
      switch(array[2] & 0x18)
      {
        case 0x8:
            {
       return new BigDecimal(Double.NaN);
            }
 case 0x10:
            {
               if(isneg)
                 return new BigDecimal(Double.NEGATIVE_INFINITY);
               return new BigDecimal(Double.POSITIVE_INFINITY);
            }
      };
      rp = 4;
      ep = 2 + array[1];
      if ((array[2] & 0x04) == 0x04)
      {
        dp[i++] = (byte)((array[rp ++] & 0x0f) + '0');
 len --;
      }
      if ((array[2] & 0x02) == 0x02)
        scale --;
      while(rp < ep)
      {
        if (i == len)
   dp[i++] = (byte) '.';
        dp[i++] = (byte)(((array[rp] >> 4) & 0x0f) + '0');
        if (i == len)
   dp[i++] = (byte) '.';
 dp[i++] = (byte)((array[rp++] & 0x0f) + '0');
      }
      bd = new BigDecimal( ((isneg) ? "-" : "") + new String(dp, 0,
          (rp != 4) ? (i - (((array[2] & 0x02) == 0x02) ? 1 : 0)) : 1));
      return bd;
   }
   private VirtuosoRdfBox readRdfBox () throws IOException, VirtuosoException
   {
      int flags = read ();
      Object box = null;
      short type = VirtuosoRdfBox.RDF_BOX_DEFAULT_TYPE;
      short lang = VirtuosoRdfBox.RDF_BOX_DEFAULT_LANG;
      boolean is_complete = false;
      long ro_id = 0L;
      boolean id_only = false;
      VirtuosoRdfBox rb;
      if (0 != (flags & VirtuosoRdfBox.RBS_EXT_TYPE))
      {
        int ID_ONLY = VirtuosoRdfBox.RBS_HAS_LANG | VirtuosoRdfBox.RBS_HAS_TYPE;
        if ((flags & ID_ONLY) == ID_ONLY) {
            id_only = true;
        } else if ((flags & VirtuosoRdfBox.RBS_HAS_LANG)!=0){
            lang = readshort();
        } else {
            type = readshort();
        }
        if (0 != (flags & VirtuosoRdfBox.RBS_64))
            ro_id = readlong();
        else
            ro_id = readlongint();
        if (0 != (flags & VirtuosoRdfBox.RBS_COMPLETE)){
            is_complete = true;
            box = read_object ();
            if (type == VirtuosoRdfBox.RDF_BOX_GEO_TYPE) {
                if (box instanceof String && ((String)box).length()>5 && ((String)box).substring(0,5).equalsIgnoreCase("point")) {
                    String data = ((String)box).substring(6);
                    try {
                        box = new VirtuosoPoint(data.substring(0, data.length()-1));
                    } catch (Exception e){
                        throw new VirtuosoException(e, VirtuosoException.IOERROR);
                    }
                }
            }
        }
      } else {
        if (0 != (flags & VirtuosoRdfBox.RBS_CHKSUM))
        {
   throw new VirtuosoException ("Invalid rdf box received", "42000", VirtuosoException.MISCERROR);
        }
        if (0 != (flags & VirtuosoRdfBox.RBS_SKIP_DTP))
        {
   int n = readshortint();
   byte[] array = new byte[n];
   for(int i = read(array,0,(int)n) ; i != n ; i+=read(array,i,(int)n-i));
   if (connection.charset_utf8)
       box = convByte2UTF(array);
   else
       box = convByte2Ascii(array);
        }
        else
          box = read_object ();
        if (0 != (flags & VirtuosoRdfBox.RBS_OUTLINED))
        {
   if (0 != (flags & VirtuosoRdfBox.RBS_64))
     ro_id = readlong();
   else
     ro_id = readlongint ();
        }
        if (0 != (flags & VirtuosoRdfBox.RBS_COMPLETE))
   is_complete = true;
        if (0 != (flags & VirtuosoRdfBox.RBS_HAS_TYPE))
   type = readshort ();
        else
   type = VirtuosoRdfBox.RDF_BOX_DEFAULT_TYPE;
        if (0 != (flags & VirtuosoRdfBox.RBS_HAS_LANG))
   lang = readshort ();
        else
   lang = VirtuosoRdfBox.RDF_BOX_DEFAULT_LANG;
      }
      rb = new VirtuosoRdfBox(this.connection, box, is_complete, id_only, type, lang, ro_id);
      return rb;
   }
   private Object readObject() throws IOException, VirtuosoException
   {
     int obj_id = readlongint();
     Object obj = read_object ();
     if (obj instanceof String)
       {
  try
    {
      java.io.ByteArrayInputStream bis = new ByteArrayInputStream (((String)obj).getBytes ("8859_1"));
      ObjectInputStream ois = new ObjectInputStream (bis);
      obj = ois.readObject();
    }
  catch (Exception e)
    {
      obj = null;
    }
       }
     return obj;
   }
   private Object readDate(int tag) throws IOException
   {
      java.util.Calendar cal_dat = new java.util.GregorianCalendar ();
      int d0 = read();
      int day = d0 << 16 | read() << 8 | read();
      int hour = read();
      int temp = read();
      int minute = temp >> 2;
      int second = (((temp & 0x3) << 4) | ((temp = read()) >> 4));
      int fraction = (((temp & 0xf) << 16) | (read() << 8) | read());
      int tz_bytes[] = new int[2], tz_interm;
      day = day | ((d0 & 0x80)!=0 ? 0xff000000 : 0);
      int tzless = hour >> 7;
      hour &= 0x1F;
      tz_bytes[0] = read();
      tz_bytes[1] = read();
      int tz = (((int)(tz_bytes[0] & 0x07)) << 8) | tz_bytes[1];
      int type = tz_bytes[0] >> 5;
      if ((tz_bytes[0] & 0x4) != 0)
        {
          tz_interm = tz_bytes[0] & 0x07;
   tz_interm |= 0xF8;
        }
      else
        tz_interm = tz_bytes[0] & 0x03;
      tz = ((int)(tz_interm << 8)) | tz_bytes[1];
      if (tz > 32767)
 tz -= 65536;
      return new DateObject(day, hour, minute, second, fraction, tz, type);
   }
}
