package openlink.util;
import java.sql.Blob;
import java.sql.SQLException;
import java.io.*;
public class OPLHeapBlob implements Blob, Serializable {
  static final long serialVersionUID = -6793193829176495886L;
  private byte[] blobData = null;
  private long blobLength = 0;
  private final static int buf_size = 0x8000;
  private Object lck;
  public OPLHeapBlob() {
    lck = this;
    blobData = new byte[1];
  }
  public OPLHeapBlob(byte[] b) {
    this(b, 0, b.length);
  }
  public OPLHeapBlob(byte[] b, int off, int len) {
    lck = this;
    blobData = new byte[len];
    System.arraycopy(b, off, blobData, 0, len);
    blobLength = len;
  }
  public OPLHeapBlob(InputStream is) throws SQLException {
    lck = this;
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream(buf_size);
      BufferedInputStream in = new BufferedInputStream(is, buf_size);
      byte[] tmp = new byte[buf_size];
      int sz = in.read(tmp, 0, buf_size);
      while( sz != -1 ) {
   out.write(tmp, 0, sz);
 sz = in.read(tmp, 0, buf_size);
      }
      blobData = out.toByteArray();
      blobLength = blobData.length;
    } catch( IOException e ) {
      throw OPLMessage_u.makeException(e);
    }
  }
  public long length() throws SQLException {
    ensureOpen();
    synchronized (lck) {
      return blobLength;
    }
  }
  public byte[] getBytes(long pos, int len) throws SQLException {
    ensureOpen();
    synchronized (lck) {
      pos--;
      if( pos >= blobLength )
        throw OPLMessage_u.makeException(OPLMessage_u.erru_Invalid_start_position);
      if( len > blobLength - pos )
        len = (int)(blobLength - pos);
      byte[] tmp = new byte[len];
      System.arraycopy(blobData, (int)pos, tmp, 0, len);
      return tmp;
    }
  }
  public InputStream getBinaryStream() throws SQLException {
    ensureOpen();
    return new BlobInputStream(lck);
  }
  public long position(byte[] pattern, long start) throws SQLException {
    ensureOpen();
    synchronized (lck) {
      if( start < 1 )
        throw OPLMessage_u.makeException(OPLMessage_u.erru_Invalid_start_position);
      start--;
      boolean match;
      if (start > blobLength)
        return -1;
      for(int i=(int)start; i<blobLength; i++) {
        if( pattern.length > (blobLength-i) )
            break;
        if( blobData[i] == pattern[0] ) {
       match = true;
     for(int j=1; j<pattern.length; j++) {
       if( blobData[i+j] != pattern[j] ) {
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
  public long position(Blob pattern, long start) throws SQLException {
    ensureOpen();
    if( start < 1 )
        throw OPLMessage_u.makeException(OPLMessage_u.erru_Invalid_start_position);
    return position(pattern.getBytes(0, (int)pattern.length()), start);
  }
  public int setBytes(long pos, byte[] bytes) throws SQLException {
    ensureOpen();
    return setBytes(pos, bytes, 0, bytes.length);
  }
  public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
    ensureOpen();
    synchronized (lck) {
      OutputStream os = new BlobOutputStream(lck, pos);
      try {
       os.write(bytes, offset, len);
      } catch (IOException e) {
        OPLMessage_u.makeException(e);
      }
    }
    return len;
  }
  public java.io.OutputStream setBinaryStream(long pos) throws SQLException {
    ensureOpen();
    return new BlobOutputStream(lck, pos);
  }
  public void truncate(long len) throws SQLException {
    ensureOpen();
    synchronized (lck) {
      int newLen = (int)len;
      if( newLen < 0 || newLen > blobLength)
        throw OPLMessage_u.makeException(OPLMessage_u.erru_Invalid_length);
      if (newLen < blobData.length) {
        byte newbuf[] = new byte[newLen];
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
  public InputStream getBinaryStream(long pos, long length) throws SQLException
  {
    ensureOpen();
    return new BlobInputStream(lck, pos, length);
  }
  private void ensureOpen() throws SQLException
  {
    if (blobData == null)
       throw OPLMessage_u.makeException(OPLMessage_u.erru_Blob_is_freed);
  }
  protected class BlobInputStream extends InputStream {
    private boolean isClosed = false;
    private int pos = 0;
    private long length;
    private Object lock;
    protected BlobInputStream(Object lck) {
      this.lock = lck;
      length = blobLength;
    }
    protected BlobInputStream(Object lck, long pos, long length) {
      this.lock = lck;
      this.pos = (int)pos;
      this.length = length;
    }
    public int read() throws IOException {
      ensureOpen();
      synchronized (lock) {
        return (pos < length) ? (blobData[pos++] & 0xff) : -1;
      }
    }
    public int read(byte b[], int off, int len) throws IOException {
      ensureOpen();
      synchronized (lock) {
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
    public long skip(long n) throws IOException {
      ensureOpen();
      synchronized (lock) {
 if (pos + n > length)
     n = length - pos;
 if (n < 0)
     return 0;
 pos += n;
 return n;
      }
    }
    public void close() {
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
    protected int count;
    private boolean isClosed = false;
    private Object lock;
    protected BlobOutputStream(Object lck, long pos) {
      this.lock = lck;
      count = (int)pos;
    }
    public void write(int b) throws IOException {
      ensureOpen();
      synchronized (lock) {
        int newcount = count + 1;
        if (newcount > blobData.length) {
    byte newbuf[] = new byte[Math.max(blobData.length + buf_size, newcount)];
   System.arraycopy(blobData, 0, newbuf, 0, count);
   blobData = newbuf;
        }
        blobData[count] = (byte)b;
        count = newcount;
        blobLength+=1;
      }
    }
    public void write(byte b[], int off, int len) throws IOException {
      ensureOpen();
      synchronized (lock) {
 if ((off < 0) || (off > b.length) || (len < 0) ||
            ((off + len) > b.length) || ((off + len) < 0)) {
     throw new IndexOutOfBoundsException();
 } else if (len == 0) {
     return;
 }
        int newcount = count + len;
        if (newcount > blobData.length) {
            byte newbuf[] = new byte[Math.max(blobData.length + buf_size, newcount)];
            System.arraycopy(blobData, 0, newbuf, 0, count);
            blobData = newbuf;
        }
        System.arraycopy(b, off, blobData, count, len);
        count = newcount;
        blobLength += len;
      }
    }
    public void close() {
      synchronized (lock) {
 isClosed = true;
      }
    }
    private void ensureOpen() throws IOException {
        if (isClosed || blobData == null)
          throw new IOException(OPLMessage_u.getMessage(OPLMessage_u.erru_Stream_is_closed ));
    }
  }
}
