package virtuoso.jdbc4;
import java.sql.*;
import java.io.*;
public class VirtuosoBlob
  implements Blob, Clob
   ,NClob
{
   private boolean request = true;
   private long length, bh_page, ask, bh_current_page, bh_start_offset, bh_position;
   private byte[] buffer;
   private Reader rd;
   private InputStream is;
   private VirtuosoConnection connection;
   protected int dtp;
   protected long key_id = 0;
   protected long frag_no = 0;
   protected long dir_page = 0;
   protected long bh_timestamp = 0;
   protected Object pages = null;
   VirtuosoBlob(InputStream is, long length, long index) throws VirtuosoException
   {
      this.is = is;
      this.bh_page = index;
      this.length = length;
      rewind();
   }
   private void rewind ()
     {
       this.bh_current_page = this.bh_page;
       this.bh_start_offset = 0;
       this.bh_position = 0;
     }
   private long bh_offset()
     {
       return this.bh_start_offset;
     }
   VirtuosoBlob(byte[] array) throws VirtuosoException
   {
      this.buffer = array;
      this.request = false;
      this.length = array.length;
   }
   VirtuosoBlob() throws VirtuosoException
   {
   }
   VirtuosoBlob(Object obj, long index) throws VirtuosoException
   {
   }
   VirtuosoBlob(Reader rd, long length, long index) throws VirtuosoException
   {
      this.rd = rd;
      this.length = length;
      this.bh_page = index;
      rewind();
   }
   VirtuosoBlob(VirtuosoConnection connection, long ask, long index, long length) throws VirtuosoException
   {
      this.connection = connection;
      this.ask = ask;
      this.bh_page = index;
      this.length = length;
      this.dtp = VirtuosoTypes.DV_BLOB_HANDLE;
      rewind();
   }
   VirtuosoBlob(VirtuosoConnection connection, long ask, long index, long length, long __key_id, long __frag_no, long __dir_page, long __timestamp, Object __pages, int dtp) throws VirtuosoException
   {
      this.connection = connection;
      this.ask = ask;
      this.bh_page = index;
      this.length = length;
      this.dtp = dtp;
      this.key_id = __key_id;
      this.frag_no = __frag_no;
      this.dir_page = __dir_page;
      this.bh_timestamp = __timestamp;
      this.pages = __pages;
      rewind();
   }
   protected void setInputStream(InputStream is, long length)
   {
      this.is = is;
      this.length = length;
   }
   protected void setReader(Reader rd, long length)
   {
      this.rd = rd;
      this.length = length;
   }
   protected void setObject(Object obj)
   {
   }
   private void checkBlobError (openlink.util.Vector curr) throws VirtuosoException
     {
       if (curr.firstElement() instanceof Long &&
    ((Number) curr.firstElement()).longValue() == VirtuosoTypes.QA_ERROR)
  {
    throw new VirtuosoException(curr.elementAt(2).toString(),
        curr.elementAt(1).toString(),
        VirtuosoException.SQLERROR);
  }
     }
   public byte[] getBytes(long pos, int length) throws VirtuosoException
     {
       if (pos <= 0)
  throw new VirtuosoException ("Invalid param to the getBytes", "22023", VirtuosoException.BADPARAM);
       if(!request)
  {
           pos--;
    if(pos>buffer.length || pos+length>buffer.length)
      {
        return null;
      }
    byte[] array = new byte[length];
    System.arraycopy(buffer, (int)pos, array, 0, length);
    return array;
  }
       try
  {
    if(pos < 0 || length <= 0 || pos + length - 1 > this.length)
      {
        return null;
      }
    Long init_read_len = null;
    if (pos - 1 < bh_offset ())
      {
        rewind();
        init_read_len = new Long (pos - 1);
      }
    else if (pos - 1 > bh_offset ())
      init_read_len = new Long (pos - bh_offset() - 1);
    if (init_read_len != null)
      {
        openlink.util.Vector curr = null;
        synchronized (connection)
   {
     Object[] args = new Object[9];
     args[0] = new Long(this.bh_current_page);
     args[1] = init_read_len;
     args[2] = new Long(this.bh_position);
     args[3] = new Long(this.key_id);
     args[4] = new Long(this.frag_no);
     args[5] = new Long(this.dir_page);
     args[6] = this.pages;
     args[7] = this.dtp == VirtuosoTypes.DV_BLOB_WIDE_HANDLE ? new Long (1) : new Long(0);
     args[8] = new Long (this.bh_timestamp);
     VirtuosoFuture future = connection.getFuture(VirtuosoFuture.getdata,args, -1);
     curr = future.nextResult();
     curr = (openlink.util.Vector) curr.firstElement();
     connection.removeFuture (future);
   }
        if(!(curr instanceof openlink.util.Vector))
   {
     return null;
   }
        checkBlobError(curr);
        for (int inx = 0; inx < curr.size(); inx++)
   {
     Object val = curr.elementAt (inx);
     if (val instanceof openlink.util.Vector)
       {
         openlink.util.Vector vval = (openlink.util.Vector)val;
         this.bh_current_page = ((Number) vval.elementAt (1)).longValue();
         this.bh_position = ((Number) vval.elementAt (2)).longValue();
         break;
       }
     else if (val instanceof String)
       {
         String sval = (String)val;
         if (dtp == VirtuosoTypes.DV_BLOB_WIDE_HANDLE)
    {
      this.bh_start_offset += sval.length();
    }
         else
    {
      this.bh_start_offset += sval.getBytes("8859_1").length;
    }
       }
   }
      }
    ByteArrayOutputStream bo = new ByteArrayOutputStream();
    openlink.util.Vector curr = null;
    synchronized (connection)
      {
        Object[] args = new Object[9];
        args[0] = new Long(this.bh_current_page);
        args[1] = new Long(length);
        args[2] = new Long(this.bh_position);
               args[3] = new Long(this.key_id);
               args[4] = new Long(this.frag_no);
               args[5] = new Long(this.dir_page);
               args[6] = this.pages;
        args[7] = this.dtp == VirtuosoTypes.DV_BLOB_WIDE_HANDLE ? new Long (1) : new Long(0);
        args[8] = new Long (this.bh_timestamp);
        VirtuosoFuture future = connection.getFuture(VirtuosoFuture.getdata,args, -1);
        curr = future.nextResult();
        curr = (openlink.util.Vector) curr.firstElement();
        connection.removeFuture (future);
      }
    if(!(curr instanceof openlink.util.Vector))
      {
        return null;
      }
    checkBlobError(curr);
    for (int inx = 0; inx < curr.size(); inx++)
      {
        Object val = curr.elementAt (inx);
        if (val instanceof openlink.util.Vector)
   {
     openlink.util.Vector vval = (openlink.util.Vector)val;
     this.bh_current_page = ((Number) vval.elementAt (1)).longValue();
     this.bh_position = ((Number) vval.elementAt (2)).longValue();
   }
        else if (val instanceof String)
   {
     String sval = (String)val;
       {
         bo.write (sval.getBytes("8859_1"));
         this.bh_start_offset += sval.getBytes("8859_1").length;
       }
   }
      }
    byte [] ret = bo.toByteArray();
    return ret;
  }
       catch(IOException e)
  {
    throw new VirtuosoException("I/O error occurred : " + e.getMessage(),VirtuosoException.IOERROR);
  }
     }
   public String getSubString(long pos, int length) throws VirtuosoException
   {
     byte[] bytes = getBytes(pos,length);
     if (dtp == VirtuosoTypes.DV_BLOB_WIDE_HANDLE)
       {
  try
    {
      return new String(bytes, "UTF8");
    }
  catch (java.io.UnsupportedEncodingException e)
    {
      throw new VirtuosoException ("UTF8 not supported in getSubString", VirtuosoException.MISCERROR);
    }
       }
     else
       {
  try
    {
      return new String(bytes, "8859_1");
    }
  catch (Exception e)
    {
      throw new VirtuosoException ("8859-1 not supported in getSubString", VirtuosoException.MISCERROR);
    }
       }
   }
   public long length() throws VirtuosoException
   {
      return length;
   }
   public InputStream getBinaryStream() throws VirtuosoException
   {
      if(is != null)
         return is;
      if(buffer != null)
         return new ByteArrayInputStream(buffer);
      return new VirtuosoBlobStream(this);
   }
   public Reader getCharacterStream() throws VirtuosoException
   {
      if(rd != null)
         return rd;
      if(buffer != null)
         return new StringReader(new String(buffer));
      try
 {
   switch (dtp)
     {
       case VirtuosoTypes.DV_BLOB_WIDE:
    return new InputStreamReader (new VirtuosoBlobStream (this), "UTF8");
       case VirtuosoTypes.DV_BLOB_BIN:
    return new InputStreamReader (new VirtuosoBlobStream (this), "8859_1");
       default:
    return new InputStreamReader (new VirtuosoBlobStream (this),
        connection.charset != null ? connection.charset : "8859_1");
     }
 }
      catch (java.io.UnsupportedEncodingException e)
 {
   throw new VirtuosoException ("Unsupported charset encoding : " + e.getMessage(),
       VirtuosoException.CASTERROR);
 }
   }
   public InputStream getAsciiStream() throws VirtuosoException
   {
      if(is != null)
         return is;
      if(buffer != null)
         return new ByteArrayInputStream(buffer);
      return new VirtuosoClobStream(this);
   }
   public long position(byte pattern[], long start) throws VirtuosoException
   {
      if(!request)
      {
         if(start>=buffer.length || start+pattern.length>=buffer.length)
    return -1;
         boolean found = true; int i;
         for(i = (int)start-1;i < buffer.length && found;i++)
           found &= (buffer[i] == pattern[i]);
         if(found)
           return i;
  return -1;
      }
      try
      {
         VirtuosoBlobStream is = (VirtuosoBlobStream)this.getBinaryStream();
         is.skip(start - 1);
         byte[] _array = new byte[pattern.length];
         while(is.available() > _array.length)
         {
            if(is.read(_array,0,_array.length) == -1)
               throw new VirtuosoException("End of stream reached.",VirtuosoException.EOF);
            boolean found = true;
            for(int i = 0;i < _array.length && found;i++)
               found &= (_array[i] == pattern[i]);
            if(found)
               return is.pos;
         }
         return -1L;
      }
      catch(IOException e)
      {
         throw new VirtuosoException("I/O error occurred : " + e.getMessage(),VirtuosoException.IOERROR);
      }
   }
   public long position(Blob pattern, long start) throws VirtuosoException
   {
      try
      {
         VirtuosoBlobStream is = (VirtuosoBlobStream)pattern.getBinaryStream();
         is.skip(start - 1);
         byte[] _array = new byte[VirtuosoTypes.PAGELEN];
         while(is.available() > VirtuosoTypes.PAGELEN)
         {
            if(is.read(_array,0,VirtuosoTypes.PAGELEN) == -1)
               throw new VirtuosoException("End of stream reached.",VirtuosoException.EOF);
            long posi = position(_array,start += VirtuosoTypes.PAGELEN);
            if(posi != -1)
               return posi;
         }
         return -1L;
      }
      catch(IOException e)
      {
         throw new VirtuosoException("I/O error occurred : " + e.getMessage(),VirtuosoException.IOERROR);
      }
      catch(SQLException e)
      {
         throw new VirtuosoException("SQL error occurred : " + e.getMessage(),VirtuosoException.SQLERROR);
      }
   }
   public long position(String searchstr, long start) throws VirtuosoException
   {
      try
      {
         VirtuosoClobStream is = (VirtuosoClobStream)this.getAsciiStream();
         is.skip(start - 1);
         byte[] _array = new byte[searchstr.length()];
         while(is.available() > _array.length)
         {
            if(is.read(_array,0,_array.length) == -1)
               throw new VirtuosoException("End of stream reached.",VirtuosoException.EOF);
            if(searchstr.equals(new String(_array)))
               return is.pos;
         }
         return -1L;
      }
      catch(IOException e)
      {
         throw new VirtuosoException("I/O error occurred : " + e.getMessage(),VirtuosoException.IOERROR);
      }
   }
   public long position(Clob searchstr, long start) throws VirtuosoException
   {
      try
      {
         VirtuosoClobStream is = (VirtuosoClobStream)searchstr.getAsciiStream();
         is.skip(start - 1);
         byte[] _array = new byte[VirtuosoTypes.PAGELEN];
         while(is.available() > VirtuosoTypes.PAGELEN)
         {
            if(is.read(_array,0,VirtuosoTypes.PAGELEN) == -1)
               throw new VirtuosoException("End of stream reached.",VirtuosoException.EOF);
            long posi = position(new String(_array),start += VirtuosoTypes.PAGELEN);
            if(posi != -1)
               return posi;
         }
         return -1L;
      }
      catch(IOException e)
      {
         throw new VirtuosoException("I/O error occurred : " + e.getMessage(),VirtuosoException.IOERROR);
      }
      catch(SQLException e)
      {
         throw new VirtuosoException("SQL error occurred : " + e.getMessage(),VirtuosoException.SQLERROR);
      }
   }
   public int hashCode()
   {
      return (int)bh_page;
   }
   public String toString()
   {
      return "Blob " + length + "b";
   }
   public int setString(long pos, String str, int offset, int len) throws SQLException
     {
       throw new VirtuosoException ("Not implemented function", VirtuosoException.NOTIMPLEMENTED);
     }
   public int setString(long pos, String str) throws SQLException
     {
       return setString (pos, str, 0, str.length());
     }
   public OutputStream setAsciiStream(long pos) throws SQLException
     {
       throw new VirtuosoException ("Not implemented function", VirtuosoException.NOTIMPLEMENTED);
     }
   public Writer setCharacterStream(long pos) throws SQLException
     {
       throw new VirtuosoException ("Not implemented function", VirtuosoException.NOTIMPLEMENTED);
     }
   public void truncate(long len) throws SQLException
     {
       throw new VirtuosoException ("Not implemented function", VirtuosoException.NOTIMPLEMENTED);
     }
   public int setBytes(long pos, byte[] bytes) throws SQLException
     {
       return setBytes (pos, bytes, 0, bytes.length);
     }
   public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException
     {
       throw new VirtuosoException ("Not implemented function", VirtuosoException.NOTIMPLEMENTED);
     }
   public OutputStream setBinaryStream(long pos) throws SQLException
     {
       throw new VirtuosoException ("Not implemented function", VirtuosoException.NOTIMPLEMENTED);
     }
   public InputStream getBinaryStream(long pos, long len) throws SQLException
   {
       throw new VirtuosoException ("Not implemented function", VirtuosoException.NOTIMPLEMENTED);
   }
   public void free() throws SQLException
   {
       throw new VirtuosoException ("Not implemented function", VirtuosoException.NOTIMPLEMENTED);
   }
   public Reader getCharacterStream(long pos, long len) throws SQLException {
       throw new VirtuosoException ("Not implemented function", VirtuosoException.NOTIMPLEMENTED);
   }
}
