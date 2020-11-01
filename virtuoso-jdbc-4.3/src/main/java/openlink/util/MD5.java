package openlink.util;
class MD5State {
  int state[];
  int count[];
  byte buffer[];
  public MD5State() {
    buffer = new byte[64];
    count = new int[2];
    state = new int[4];
    state[0] = 0x67452301;
    state[1] = 0xefcdab89;
    state[2] = 0x98badcfe;
    state[3] = 0x10325476;
    count[0] = count[1] = 0;
  }
  public MD5State (MD5State from) {
    this();
    int i;
    for (i = 0; i < buffer.length; i++)
      this.buffer[i] = from.buffer[i];
    for (i = 0; i < state.length; i++)
      this.state[i] = from.state[i];
    for (i = 0; i < count.length; i++)
      this.count[i] = from.count[i];
  }
};
public class MD5 {
  MD5State state;
  MD5State finals;
  static byte padding[] = {
    (byte) 0x80, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
  };
  public synchronized void Init () {
    state = new MD5State();
    finals = null;
  }
  public MD5 () {
    this.Init();
  }
  public MD5 (Object ob) {
    this();
    Update(ob.toString());
  }
  private int rotate_left (int x, int n) {
    return (x << n) | (x >>> (32 - n));
  }
  private int uadd (int a, int b) {
    long aa, bb;
    aa = ((long) a) & 0xffffffffL;
    bb = ((long) b) & 0xffffffffL;
    aa += bb;
    return (int) (aa & 0xffffffffL);
  }
  private int uadd (int a, int b, int c) {
    return uadd(uadd(a, b), c);
  }
  private int uadd (int a, int b, int c, int d) {
    return uadd(uadd(a, b, c), d);
  }
  private int FF (int a, int b, int c, int d, int x, int s, int ac) {
    a = uadd(a, ((b & c) | (~b & d)), x, ac);
    return uadd(rotate_left(a, s), b);
  }
  private int GG (int a, int b, int c, int d, int x, int s, int ac) {
    a = uadd(a, ((b & d) | (c & ~d)), x, ac);
    return uadd(rotate_left(a, s), b);
  }
  private int HH (int a, int b, int c, int d, int x, int s, int ac) {
    a = uadd(a, (b ^ c ^ d), x, ac);
    return uadd(rotate_left(a, s) , b);
  }
  private int II (int a, int b, int c, int d, int x, int s, int ac) {
    a = uadd(a, (c ^ (b | ~d)), x, ac);
    return uadd(rotate_left(a, s), b);
  }
  private int[] Decode (byte buffer[], int len, int shift) {
    int out[];
    int i, j;
    out = new int[16];
    for (i = j = 0; j < len; i++, j += 4) {
      out[i] = ((int) (buffer[j + shift] & 0xff)) |
 (((int) (buffer[j + 1 + shift] & 0xff)) << 8) |
 (((int) (buffer[j + 2 + shift] & 0xff)) << 16) |
 (((int) (buffer[j + 3 + shift] & 0xff)) << 24);
    }
    return out;
  }
  private void Transform (MD5State state, byte buffer[], int shift) {
    int
      a = state.state[0],
      b = state.state[1],
      c = state.state[2],
      d = state.state[3],
      x[];
    x = Decode(buffer, 64, shift);
    a = FF (a, b, c, d, x[ 0], 7, 0xd76aa478);
    d = FF (d, a, b, c, x[ 1], 12, 0xe8c7b756);
    c = FF (c, d, a, b, x[ 2], 17, 0x242070db);
    b = FF (b, c, d, a, x[ 3], 22, 0xc1bdceee);
    a = FF (a, b, c, d, x[ 4], 7, 0xf57c0faf);
    d = FF (d, a, b, c, x[ 5], 12, 0x4787c62a);
    c = FF (c, d, a, b, x[ 6], 17, 0xa8304613);
    b = FF (b, c, d, a, x[ 7], 22, 0xfd469501);
    a = FF (a, b, c, d, x[ 8], 7, 0x698098d8);
    d = FF (d, a, b, c, x[ 9], 12, 0x8b44f7af);
    c = FF (c, d, a, b, x[10], 17, 0xffff5bb1);
    b = FF (b, c, d, a, x[11], 22, 0x895cd7be);
    a = FF (a, b, c, d, x[12], 7, 0x6b901122);
    d = FF (d, a, b, c, x[13], 12, 0xfd987193);
    c = FF (c, d, a, b, x[14], 17, 0xa679438e);
    b = FF (b, c, d, a, x[15], 22, 0x49b40821);
    a = GG (a, b, c, d, x[ 1], 5, 0xf61e2562);
    d = GG (d, a, b, c, x[ 6], 9, 0xc040b340);
    c = GG (c, d, a, b, x[11], 14, 0x265e5a51);
    b = GG (b, c, d, a, x[ 0], 20, 0xe9b6c7aa);
    a = GG (a, b, c, d, x[ 5], 5, 0xd62f105d);
    d = GG (d, a, b, c, x[10], 9, 0x2441453);
    c = GG (c, d, a, b, x[15], 14, 0xd8a1e681);
    b = GG (b, c, d, a, x[ 4], 20, 0xe7d3fbc8);
    a = GG (a, b, c, d, x[ 9], 5, 0x21e1cde6);
    d = GG (d, a, b, c, x[14], 9, 0xc33707d6);
    c = GG (c, d, a, b, x[ 3], 14, 0xf4d50d87);
    b = GG (b, c, d, a, x[ 8], 20, 0x455a14ed);
    a = GG (a, b, c, d, x[13], 5, 0xa9e3e905);
    d = GG (d, a, b, c, x[ 2], 9, 0xfcefa3f8);
    c = GG (c, d, a, b, x[ 7], 14, 0x676f02d9);
    b = GG (b, c, d, a, x[12], 20, 0x8d2a4c8a);
    a = HH (a, b, c, d, x[ 5], 4, 0xfffa3942);
    d = HH (d, a, b, c, x[ 8], 11, 0x8771f681);
    c = HH (c, d, a, b, x[11], 16, 0x6d9d6122);
    b = HH (b, c, d, a, x[14], 23, 0xfde5380c);
    a = HH (a, b, c, d, x[ 1], 4, 0xa4beea44);
    d = HH (d, a, b, c, x[ 4], 11, 0x4bdecfa9);
    c = HH (c, d, a, b, x[ 7], 16, 0xf6bb4b60);
    b = HH (b, c, d, a, x[10], 23, 0xbebfbc70);
    a = HH (a, b, c, d, x[13], 4, 0x289b7ec6);
    d = HH (d, a, b, c, x[ 0], 11, 0xeaa127fa);
    c = HH (c, d, a, b, x[ 3], 16, 0xd4ef3085);
    b = HH (b, c, d, a, x[ 6], 23, 0x4881d05);
    a = HH (a, b, c, d, x[ 9], 4, 0xd9d4d039);
    d = HH (d, a, b, c, x[12], 11, 0xe6db99e5);
    c = HH (c, d, a, b, x[15], 16, 0x1fa27cf8);
    b = HH (b, c, d, a, x[ 2], 23, 0xc4ac5665);
    a = II (a, b, c, d, x[ 0], 6, 0xf4292244);
    d = II (d, a, b, c, x[ 7], 10, 0x432aff97);
    c = II (c, d, a, b, x[14], 15, 0xab9423a7);
    b = II (b, c, d, a, x[ 5], 21, 0xfc93a039);
    a = II (a, b, c, d, x[12], 6, 0x655b59c3);
    d = II (d, a, b, c, x[ 3], 10, 0x8f0ccc92);
    c = II (c, d, a, b, x[10], 15, 0xffeff47d);
    b = II (b, c, d, a, x[ 1], 21, 0x85845dd1);
    a = II (a, b, c, d, x[ 8], 6, 0x6fa87e4f);
    d = II (d, a, b, c, x[15], 10, 0xfe2ce6e0);
    c = II (c, d, a, b, x[ 6], 15, 0xa3014314);
    b = II (b, c, d, a, x[13], 21, 0x4e0811a1);
    a = II (a, b, c, d, x[ 4], 6, 0xf7537e82);
    d = II (d, a, b, c, x[11], 10, 0xbd3af235);
    c = II (c, d, a, b, x[ 2], 15, 0x2ad7d2bb);
    b = II (b, c, d, a, x[ 9], 21, 0xeb86d391);
    state.state[0] += a;
    state.state[1] += b;
    state.state[2] += c;
    state.state[3] += d;
  }
  public void Update (MD5State stat, byte buffer[], int offset, int length) {
    int index, partlen, i, start;
    finals = null;
    if ((length - offset)> buffer.length)
      length = buffer.length - offset;
    index = (int) (stat.count[0] >>> 3) & 0x3f;
    if ((stat.count[0] += (length << 3)) <
 (length << 3))
      stat.count[1]++;
    stat.count[1] += length >>> 29;
    partlen = 64 - index;
    if (length >= partlen) {
      for (i = 0; i < partlen; i++)
 stat.buffer[i + index] = buffer[i + offset];
      Transform(stat, stat.buffer, 0);
      for (i = partlen; (i + 63) < length; i+= 64)
 Transform(stat, buffer, i);
      index = 0;
    } else
      i = 0;
    if (i < length) {
      start = i;
      for (; i < length; i++)
 stat.buffer[index + i - start] = buffer[i + offset];
    }
  }
  public void Update (byte buffer[], int offset, int length) {
      Update(this.state, buffer, offset, length);
  }
  public void Update (byte buffer[], int length) {
      Update(this.state, buffer, 0, length);
  }
  public void Update (byte buffer[]) {
      Update(buffer, 0, buffer.length);
  }
  public void Update (byte b) {
    byte buffer[] = new byte[1];
    buffer[0] = b;
    Update(buffer, 1);
  }
  public void Update (String s) {
    byte chars[];
    try
      {
 chars = s.getBytes ("8859_1");
 Update(chars, chars.length);
      }
    catch (java.io.UnsupportedEncodingException e)
      {
      }
  }
  public void Update (int i) {
      Update((byte) (i & 0xff));
  }
  private byte[] Encode (int input[], int len) {
    int i, j;
    byte out[];
    out = new byte[len];
    for (i = j = 0; j < len; i++, j += 4) {
      out[j] = (byte) (input[i] & 0xff);
      out[j + 1] = (byte) ((input[i] >>> 8) & 0xff);
      out[j + 2] = (byte) ((input[i] >>> 16) & 0xff);
      out[j + 3] = (byte) ((input[i] >>> 24) & 0xff);
    }
    return out;
  }
  public synchronized byte[] Final () {
    byte bits[];
    int index, padlen;
    MD5State fin;
    if (finals == null) {
      fin = new MD5State(state);
      bits = Encode(fin.count, 8);
      index = (int) ((fin.count[0] >>> 3) & 0x3f);
      padlen = (index < 56) ? (56 - index) : (120 - index);
      Update(fin, padding, 0, padlen);
      Update(fin, bits, 0, 8);
      finals = fin;
    }
    return Encode(finals.state, 16);
  }
  public static String asHex (byte hash[]) {
    StringBuffer buf = new StringBuffer(hash.length * 2);
    int i;
    for (i = 0; i < hash.length; i++) {
      if (((int) hash[i] & 0xff) < 0x10)
 buf.append("0");
      buf.append(Long.toString((int) hash[i] & 0xff, 16));
    }
    return buf.toString();
  }
  public String asHex () {
    return asHex(this.Final());
  }
  public static String md5_digest (String user, String password, String ses_name)
    {
      MD5 md5 = new MD5();
      try
 {
   byte uid_bytes [] = user.getBytes ("8859_1");
   byte pwd_bytes [] = password.getBytes ("8859_1");
   byte ses_bytes [] = ses_name.getBytes ("8859_1");
   md5.Init();
   md5.Update(ses_bytes, ses_bytes.length);
   md5.Update(uid_bytes, uid_bytes.length);
   md5.Update(pwd_bytes, pwd_bytes.length);
   return new String (md5.Final (), "8859_1");
 }
      catch (java.io.UnsupportedEncodingException e)
 {
 }
      return null;
    }
  private static final String spass1 = "7rLrT7iG3kWWLuSDYdS/KIXO8JF86h12KyCTG1Mh0qxWdSZ6ezHRST0UuGl6xkbMgsXj4+eZbXNyYijRmoaaJm+hQCWSOW+0OHGCnYWB4upxi0Fogdu0gb+q4VFzyUFknEpZPg==";
  private static final String spass2 = "PCuJhpWX5eApg2mRs0bvSIdfwSDUa0kjiSdd76ORgXYyhtLbHm4Uq6afLbfROLi5pDpjKVS9Vr9aZo+F3IpyZ6Zn6m/Xf1PRtq3jdseJht4VSduxHrpocKVdRh3LixXKr6Ue6A==";
  private static byte the_pass[] = new String ("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx").getBytes();
  private static void calculate_pass ()
    {
      byte pass1[] = spass1.getBytes();
      byte pass2[] = spass2.getBytes();
      synchronized (the_pass)
 {
   if (the_pass[0] == 'x')
     {
       int inx;
       for (inx = 0; inx < pass2.length; inx++)
  {
    the_pass[inx] = (byte) (pass1[inx] ^ pass2[inx]);
    if (the_pass[inx] == 0)
      the_pass[inx] = pass1[inx];
  }
       the_pass[136] = 0;
     }
 }
    }
  private static void xx_encrypt_passwd (byte thing[], int thing_ofs, byte user_name[])
    {
      MD5 md5 = new MD5();
      byte md5_res[];
      int md5_inx;
      int inx;
      calculate_pass ();
      md5.Init ();
      if (user_name != null && user_name[0] != 0)
 md5.Update (user_name, user_name.length);
      md5.Update (the_pass, the_pass.length);
      md5_res = md5.Final ();
      for (md5_inx = 0, inx = thing_ofs; inx < thing.length; inx++, md5_inx = (++md5_inx) % md5_res.length)
 thing[inx] = (byte) (thing[inx] ^ md5_res[md5_inx]);
    }
  public static String pwd_magic_encrypt (String user_name, String password)
    {
      try
 {
   byte passwd_bytes[] = password.getBytes("8859_1");
   byte uname_bytes[] = user_name.getBytes("8859_1");
   byte pwd[] = new byte[passwd_bytes.length + 1];
   pwd[0] = 0;
   System.arraycopy (passwd_bytes, 0, pwd, 1, passwd_bytes.length);
   xx_encrypt_passwd (pwd, 1, uname_bytes);
   return new String (pwd, "8859_1");
 }
      catch (java.io.UnsupportedEncodingException e)
 {
 }
      return null;
    }
}
