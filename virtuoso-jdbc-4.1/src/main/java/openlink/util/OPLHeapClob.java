package openlink.util;
import java.sql.Clob;
import java.io.*;
import java.sql.SQLException;
public class OPLHeapClob implements Clob, Serializable {
  static final long serialVersionUID = 6296947263965593908L;
  private char[] blobData = null;
  private long blobLength = 0;
  private final static int buf_size = 0x8000;
  private Object lck;
  public OPLHeapClob() {
    lck = this;
    blobData = new char[1];
  }
  public OPLHeapClob(String b) {
    lck = this;
    blobData = b.toCharArray();
    blobLength = blobData.length;
  }
  public OPLHeapClob(char[] b) {
    lck = this;
    blobData = new char[b.length];
    System.arraycopy(b, 0, blobData, 0, b.length);
    blobLength = b.length;
  }
  public OPLHeapClob(Reader is) throws SQLException {
    lck = this;
    try {
      CharArrayWriter out = new CharArrayWriter(buf_size);
      BufferedReader in = new BufferedReader(is, buf_size);
      char[] tmp = new char[buf_size];
      int sz = in.read(tmp, 0, buf_size);
      while( sz != -1 ) {
   out.write(tmp, 0, sz);
 sz = in.read(tmp, 0, buf_size);
      }
      blobData = out.toCharArray();
      blobLength = blobData.length;
    } catch( IOException e ) {
      throw OPLMessage_u.makeException(e);
    }
  }
  public long length() throws SQLException {
    ensureOpen();
    synchronized(lck) {
      return blobLength;
    }
  }
  public String getSubString(long pos, int len) throws SQLException {
    ensureOpen();
    synchronized(lck) {
      pos--;
      if ( pos >= blobLength )
        throw OPLMessage_u.makeException(OPLMessage_u.erru_Invalid_start_position);
      if ( len > blobLength - pos )
        len = (int)(blobLength - pos);
      return new String(blobData, (int)pos, len);
    }
  }
  public Reader getCharacterStream() throws SQLException {
    ensureOpen();
    return new OPLHeapClob.BlobInputReader(lck);
  }
  public InputStream getAsciiStream() throws SQLException {
    ensureOpen();
    return new BlobInputStream(getCharacterStream());
  }
  public long position(String searchstr, long start) throws SQLException {
    ensureOpen();
    synchronized(lck) {
      if ( start < 1 )
        throw OPLMessage_u.makeException(OPLMessage_u.erru_Invalid_start_position);
      start--;
      boolean match;
      if (start > blobLength)
        return -1;
      for(int i=(int)start; i<blobLength; i++) {
        if( searchstr.length() > (blobLength-i) )
            break;
        if( blobData[i] == searchstr.charAt(0) ) {
       match = true;
     for(int j=1; j<searchstr.length(); j++) {
       if( blobData[i+j] != searchstr.charAt(j) ) {
    match = false;
   break;
              }
            }
     if( match )
        return i+1;
        }
      }
    }
    return -1;
  }
  public long position(Clob searchstr, long start) throws SQLException {
    ensureOpen();
    if( start < 1 )
        throw OPLMessage_u.makeException(OPLMessage_u.erru_Invalid_start_position);
    return position(searchstr.getSubString(0, (int)searchstr.length()), start);
  }
  public int setString(long pos, String str) throws SQLException {
    ensureOpen();
    return setString(pos, str, 0, str.length());
  }
  public int setString(long pos, String str, int offset, int len) throws SQLException {
    ensureOpen();
    synchronized (lck) {
      Writer os = new BlobOutputWriter(lck, pos);
      try {
        os.write(str, offset, len);
      } catch (IOException e) {
        OPLMessage_u.makeException(e);
      }
    }
    return len;
  }
  public java.io.OutputStream setAsciiStream(long pos) throws SQLException {
    ensureOpen();
    return new BlobOutputStream(new BlobOutputWriter(lck, pos));
  }
  public java.io.Writer setCharacterStream(long pos) throws SQLException {
    ensureOpen();
    return new BlobOutputWriter(lck, pos);
  }
  public void truncate(long len) throws SQLException {
    ensureOpen();
    synchronized(lck) {
      int newLen = (int)len;
      if (newLen < 0 || newLen > blobData.length)
         throw OPLMessage_u.makeException(OPLMessage_u.erru_Invalid_length);
      if (newLen < blobData.length) {
        char[] newbuf = new char[newLen];
        System.arraycopy(blobData, 0, newbuf, 0, newLen);
        blobData = newbuf;
      }
      blobLength = len;
    }
  }
  public void free() throws SQLException
  {
    synchronized (lck) {
        blobData = null;
    }
  }
  public Reader getCharacterStream(long pos, long length) throws SQLException
  {
    ensureOpen();
    return new OPLHeapClob.BlobInputReader(lck, pos, length);
  }
  private void ensureOpen() throws SQLException
  {
    if (blobData == null)
       throw OPLMessage_u.makeException(OPLMessage_u.erru_Blob_is_freed);
  }
  protected class BlobInputReader extends Reader {
    private boolean isClosed = false;
    private int pos = 0;
    private long length = 0;
    protected BlobInputReader(Object lck) {
      super(lck);
      length = blobData.length;
    }
    protected BlobInputReader(Object lck, long pos, long length) {
      super(lck);
      this.pos = (int)pos;
      this.length = length;
    }
    public int read() throws IOException {
      synchronized (lock) {
        ensureOpen();
        return (pos < length) ? blobData[pos++] : -1;
      }
    }
    public int read(char b[], int off, int len) throws IOException {
      synchronized (lock) {
 ensureOpen();
 if (b == null)
     throw new NullPointerException();
 if ((off < 0) || (off > b.length) || (len < 0) ||
     ((off + len) > b.length) || ((off + len) < 0))
     throw new IndexOutOfBoundsException();
 if (pos >= length)
     return -1;
 if (pos + len > length)
     len = (int)(length - pos);
        if (len <= 0)
           return 0;
 System.arraycopy(blobData, pos, b, off, len);
 pos += len;
 return len;
      }
    }
    public synchronized long skip(long n) throws IOException {
 synchronized (lock) {
     ensureOpen();
     if (pos + n > length)
  n = length - pos;
     if (n < 0)
  return 0;
     pos += n;
     return n;
 }
    }
    public boolean ready() throws IOException {
      synchronized (lock) {
        ensureOpen();
        return true;
      }
    }
    public void close() {
 isClosed = true;
    }
    private void ensureOpen() throws IOException {
        if (isClosed || blobData == null)
          throw new IOException(OPLMessage_u.getMessage(OPLMessage_u.erru_Stream_is_closed));
    }
  }
  protected class BlobInputStream extends InputStream {
    private static final int defBufSize = 32;
    private byte[] buf;
    private char[] cbuf;
    private int pos = 0;
    private int count = 0;
    private Reader in;
    BlobInputStream(Reader in) {
      buf = new byte[defBufSize];
      cbuf = new char[defBufSize / 4];
      this.in = in;
    }
    private void ensureOpen() throws IOException {
      if (in == null || blobData == null)
         throw new IOException(OPLMessage_u.getMessage(OPLMessage_u.erru_Stream_is_closed));
    }
    private void fill() throws IOException {
      count = pos = 0;
      int cnt;
      if ((cnt = in.read(cbuf)) == -1)
         return;
      byte[] tmp = (new String(cbuf)).getBytes();
      System.arraycopy(tmp, 0, buf, 0, tmp.length);
      count = tmp.length;
    }
    public synchronized int read() throws IOException {
      ensureOpen();
      if (pos >= count) {
          fill();
          if (pos >= count)
       return -1;
      }
      return buf[pos++] & 0xff;
    }
    private int read1(byte[] b, int off, int len) throws IOException {
      int avail = count - pos;
      if (avail <= 0) {
          fill();
          avail = count - pos;
          if (avail <= 0) return -1;
      }
      int cnt = (avail < len) ? avail : len;
      System.arraycopy(buf, pos, b, off, cnt);
      pos += cnt;
      return cnt;
    }
    public synchronized int read(byte b[], int off, int len)
   throws IOException
    {
      ensureOpen();
      if ((off | len | (off + len) | (b.length - (off + len))) < 0)
        throw new IndexOutOfBoundsException();
      else if (len == 0)
        return 0;
      int n = read1(b, off, len);
      if (n <= 0) return n;
      while (n < len) {
          int n1 = read1(b, off + n, len - n);
          if (n1 <= 0) break;
          n += n1;
      }
      return n;
    }
    public synchronized int available() throws IOException {
      ensureOpen();
      return (count - pos);
    }
    public synchronized void close() throws IOException
    {
      buf = null;
      cbuf = null;
      in.close();
      in = null;
    }
  }
  protected class BlobOutputWriter extends Writer {
    protected int count;
    private boolean isClosed = false;
    protected BlobOutputWriter(Object lck, long pos) {
      super(lck);
      count = (int)pos;
    }
    public void write(int c) throws IOException {
      ensureOpen();
      synchronized (lock) {
 int newcount = count + 1;
 if (newcount > blobData.length) {
    char newbuf[] = new char[Math.max(blobData.length + buf_size, newcount)];
    System.arraycopy(blobData, 0, newbuf, 0, count);
    blobData = newbuf;
        }
 blobData[count] = (char)c;
 count = newcount;
      }
    }
    public void write(char c[], int off, int len) throws IOException {
      ensureOpen();
      if ((off < 0) || (off > c.length) || (len < 0) ||
         ((off + len) > c.length) || ((off + len) < 0)) {
  throw new IndexOutOfBoundsException();
      } else if (len == 0) {
  return;
      }
      synchronized (lock) {
 int newcount = count + len;
 if (newcount > blobData.length) {
          char newbuf[] = new char[Math.max(blobData.length + buf_size, newcount)];
   System.arraycopy(blobData, 0, newbuf, 0, count);
   blobData = newbuf;
        }
 System.arraycopy(c, off, blobData, count, len);
 count = newcount;
      }
    }
    public void write(String str, int off, int len) throws IOException {
      ensureOpen();
      synchronized (lock) {
 int newcount = count + len;
 if (newcount > blobData.length) {
   char newbuf[] = new char[Math.max(blobData.length + buf_size, newcount)];
     System.arraycopy(blobData, 0, newbuf, 0, count);
   blobData = newbuf;
        }
 str.getChars(off, off + len, blobData, count);
 count = newcount;
      }
    }
    public void flush() { }
    public synchronized void close() {
      synchronized (lock) {
 isClosed = true;
      }
    }
    private void ensureOpen() throws IOException {
        if (isClosed || blobData == null)
          throw new IOException(OPLMessage_u.getMessage(OPLMessage_u.erru_Stream_is_closed ));
    }
  }
  protected class BlobOutputStream extends OutputStream {
    private Writer out;
    private boolean isClosed = false;
    protected BlobOutputStream(Writer out) {
      this.out = out;
    }
    public synchronized void write(int b) throws IOException {
      ensureOpen();
      byte[] tmp = {(byte)b};
      out.write(new String(tmp));
    }
    public void write(byte b[], int off, int len) throws IOException {
      ensureOpen();
      if (len == 0)
  return;
      out.write(new String(b, off, len));
    }
    public void close() throws IOException{
      isClosed = true;
      out.close();
    }
    private void ensureOpen() throws IOException {
      if (isClosed || blobData == null)
         throw new IOException(OPLMessage_u.getMessage(OPLMessage_u.erru_Stream_is_closed));
    }
  }
}
