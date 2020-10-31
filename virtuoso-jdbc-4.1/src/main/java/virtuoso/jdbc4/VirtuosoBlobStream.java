package virtuoso.jdbc4;
import java.io.*;
class VirtuosoBlobStream extends InputStream
{
   private VirtuosoBlob blob;
   protected long pos = 1;
   VirtuosoBlobStream(VirtuosoBlob blob)
   {
      this.blob = blob;
   }
   public int read() throws IOException
   {
      try
      {
         return (pos > blob.length()) ? -1 : (blob.getBytes(pos++,1))[0];
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
         if((pos + len) > blob.length())
         {
    int to_read = (int)(blob.length() - pos + 1);
    if (to_read > 0)
      bytes = blob.getBytes(pos,to_read);
    if (bytes != null)
      {
        System.arraycopy(bytes,0,b,off,to_read);
        pos = blob.length() + 1;
        return bytes.length;
      }
    else
      return -1;
         }
  bytes = blob.getBytes(pos,len);
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
         if((pos + n) > blob.length())
         {
            long _prov = blob.length() - pos + 1;
            pos = blob.length();
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
         return (int)(blob.length() - pos + 1);
      }
      catch(Exception e)
      {
         throw new IOException(e.getMessage());
      }
   }
   public void reset ()
     {
       pos = 1;
     }
}
