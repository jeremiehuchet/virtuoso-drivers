package virtuoso.jdbc4;
import java.io.*;
class VirtuosoClobStream extends InputStream
{
   private VirtuosoBlob clob;
   protected long pos = 1;
   VirtuosoClobStream(VirtuosoBlob clob)
   {
      this.clob = clob;
   }
   public int read() throws IOException
   {
      try
      {
         return (pos > clob.length()) ? -1 : ((clob.getSubString(pos++,1)).getBytes("ASCII"))[0];
      }
      catch(Exception e)
      {
         throw new IOException(e.getMessage());
      }
   }
   public int read(byte b[], int off, int len) throws IOException
   {
      try
      {
 byte[] bytes = null;
         if(len <= 0)
            return 0;
         if((pos + len) > clob.length())
         {
     int to_read = (int)(clob.length() - pos + 1);
     if (to_read > 0)
       bytes = clob.getSubString (pos, to_read).getBytes("ASCII");
     if (bytes != null)
       {
  System.arraycopy(bytes,0,b,off,to_read);
  pos = clob.length() + 1;
  return bytes.length;
       }
     else
       return -1;
         }
  bytes = clob.getSubString(pos,len).getBytes("ASCII");
  if (bytes != null)
    {
      System.arraycopy(bytes,0,b,off,len);
      pos += bytes.length;
      return bytes.length;
    }
  else return -1;
      }
      catch(Exception e)
      {
         throw new IOException(e.getMessage());
      }
   }
   public long skip(long n) throws IOException
   {
      try
      {
         if(n <= 0)
            return 0;
         if((pos + n) > clob.length())
         {
            long _prov = clob.length() - pos;
            pos = clob.length();
            return _prov;
         }
         pos += n;
         return n;
      }
      catch(Exception e)
      {
         throw new IOException(e.getMessage());
      }
   }
   public int available() throws IOException
   {
      try
      {
         return (int)(clob.length() - pos + 1);
      }
      catch(Exception e)
      {
         throw new IOException(e.getMessage());
      }
   }
}
