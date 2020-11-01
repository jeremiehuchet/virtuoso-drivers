package virtuoso.jdbc4;
import java.sql.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.security.*;
import java.security.cert.*;
import javax.net.ssl.*;
import openlink.util.*;
import java.util.Vector;
import openlink.util.OPLHeapNClob;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
public class VirtuosoConnection implements Connection
{
   private Socket socket;
   private VirtuosoInputStream in;
   private VirtuosoOutputStream out;
   private Hashtable<Integer,VirtuosoFuture> futures;
   private int req_no, con_no;
   private static int global_con_no = 0;
   protected String qualifier;
   private String version;
   private int _case;
   protected openlink.util.Vector client_defaults;
   protected openlink.util.Vector client_charset;
   protected Hashtable<Character,Byte> client_charset_hash;
   protected SQLWarning warning = null;
   private String peer_name;
   private boolean auto_commit = true;
   private boolean global_transaction = false;
   private String url;
   private String user, password, pwdclear;
   private String cert_alias;
   private String keystore_path, keystore_pass;
   private String truststore_path, truststore_pass;
   private String ssl_provider;
   private boolean use_ssl;
   private String con_delegate;
   private int trxisolation = Connection.TRANSACTION_REPEATABLE_READ;
   private boolean readOnly = false;
   protected int timeout_def = 60*1000;
   protected int timeout = 0;
   protected int txn_timeout = 0;
   protected int fbs = VirtuosoTypes.DEFAULTPREFETCH;
   protected boolean utf8_execs = false;
   protected int timezoneless_datetimes = 0;
   protected VirtuosoPooledConnection pooled_connection = null;
   protected VirtuosoXAConnection xa_connection = null;
   protected String charset;
   protected boolean charset_utf8 = false;
   protected Hashtable<Integer,String> rdf_type_hash = null;
   protected Hashtable<Integer,String> rdf_lang_hash = null;
   protected Hashtable<String,Integer> rdf_type_rev = null;
   protected Hashtable<String,Integer> rdf_lang_rev = null;
  private LRUCache<String,VirtuosoPreparedStatement> pStatementCache;
  private boolean useCachePrepStatements = false;
  private Vector<VhostRec> hostList = new Vector<VhostRec>();
   protected boolean rdf_type_loaded = false;
   protected boolean rdf_lang_loaded = false;
   private static final SQLPermission SET_NETWORK_TIMEOUT_PERM = new SQLPermission("setNetworkTimeout");
   private static final SQLPermission ABORT_PERM = new SQLPermission("abort");
  private boolean useRoundRobin;
   protected class VhostRec
   {
     protected String host;
     protected int port;
     protected VhostRec(String _host, String _port) throws VirtuosoException
     {
       host = _host;
       try {
         port = Integer.parseInt(_port);
       } catch(NumberFormatException e) {
         throw new VirtuosoException("Wrong port number : " + e.getMessage(),VirtuosoException.BADFORMAT);
       }
     }
     protected VhostRec(String _host, int _port) throws VirtuosoException
     {
       host = _host;
       port = _port;
     }
   }
   protected Vector<VhostRec> parse_vhost(String vhost, String _host, int _port) throws VirtuosoException
   {
     Vector<VhostRec> hostlist = new Vector<VhostRec>();
     String port = Integer.toString(_port);
     String attr = null;
     StringBuffer buff = new StringBuffer();
     for (int i = 0; i < vhost.length(); i++) {
       char c = vhost.charAt(i);
       switch (c) {
         case ',':
           String val = buff.toString().trim();
           if (attr == null) {
             attr = val;
             val = port;
           }
    if (attr != null && attr.length() > 0)
              hostlist.add(new VhostRec(attr, val));
    attr = null;
    buff.setLength(0);
    break;
         case ':':
    attr = buff.toString().trim();
    buff.setLength(0);
    break;
         default:
    buff.append(c);
    break;
       }
     }
     String val = buff.toString().trim();
     if (attr == null) {
       attr = val;
       val = port;
     }
     if (attr != null && attr.length() > 0) {
       hostlist.add(new VhostRec(attr, val));
     }
     if (hostlist.size() == 0)
       hostlist.add(new VhostRec(_host, _port));
     return hostlist;
   }
   private synchronized int getNextRoundRobinHostIndex()
   {
     int indexRange = hostList.size();
     return (int)(Math.random() * indexRange);
   }
   VirtuosoConnection(String url, String host, int port, Properties prop) throws VirtuosoException
   {
      int sendbs = 32768;
      int recvbs = 32768;
      hostList = parse_vhost(prop.getProperty("_vhost", ""), host, port);
      this.req_no = 0;
      this.url = url;
      this.con_no = global_con_no++;
      if (prop.get("charset") != null)
      {
 charset = (String)prop.get("charset");
 if (charset.toUpperCase().indexOf("UTF-8") != -1)
 {
     this.charset = null;
     this.charset_utf8 = true;
 }
      }
      user = (String)prop.get("user");
      if(user == null || user.equals(""))
         user = "";
      password = (String)prop.get("password");
      if (password == null)
         password = "";
      timeout = getIntAttr(prop, "timeout", timeout)*1000;
      pwdclear = (String)prop.get("pwdclear");
      sendbs = getIntAttr(prop, "sendbs", sendbs);
      if (sendbs <= 0)
          sendbs = 32768;
      recvbs = getIntAttr(prop, "recvbs", recvbs);
      if (recvbs <= 0)
          recvbs = 32768;
      fbs = getIntAttr(prop, "fbs", fbs);
      if (fbs <= 0)
          fbs = VirtuosoTypes.DEFAULTPREFETCH;;
      truststore_path = (String)prop.get("truststorepath");
      truststore_pass = (String)prop.get("truststorepass");
      keystore_pass = (String)prop.get("keystorepass");
      keystore_path = (String)prop.get("keystorepath");
      ssl_provider = (String)prop.get("provider");
      cert_alias = (String)prop.get("cert");
      use_ssl = getBoolAttr(prop, "ssl", false);
      con_delegate = (String)prop.get("delegate");
      if(pwdclear == null)
         pwdclear = "0";
      futures = new Hashtable<Integer,VirtuosoFuture>();
      rdf_type_hash = new Hashtable<Integer,String> ();
      rdf_lang_hash = new Hashtable<Integer,String> ();
      rdf_type_rev = new Hashtable<String,Integer> ();
      rdf_lang_rev = new Hashtable<String,Integer> ();
      useCachePrepStatements = getBoolAttr(prop, "usepstmtpool", false);
      int poolSize = getIntAttr(prop, "pstmtpoolsize", 25);
      createCaches(poolSize);
      useRoundRobin = getBoolAttr(prop, "roundrobin", false);
      if (hostList.size() <= 1)
        useRoundRobin = false;
      connect(host,port,(String)prop.get("database"), sendbs, recvbs, (prop.get("log_enable") != null ? (Integer.parseInt(prop.getProperty("log_enable"))) : -1));
   }
   public synchronized boolean isConnectionLost(int timeout_sec)
   {
     ResultSet rs = null;
     Statement st = null;
     try{
        st = createStatement();
 st.setQueryTimeout(timeout_sec);
        rs = st.executeQuery("select 1");
        return false;
     } catch (Exception e ) {
        return true;
     } finally {
       if (rs!=null)
         try{
           rs.close();
         } catch(Exception e){}
       if (st!=null)
         try{
           st.close();
         } catch(Exception e){}
     }
   }
   protected int getIntAttr(java.util.Properties info, String key, int def)
   {
     int ret = def;
     String val = info.getProperty(key);
     try {
 if (val != null && val.length() > 0)
   ret = Integer.parseInt(val);
     } catch (NumberFormatException e) {
 ret = def;
     }
     return ret;
   }
   protected boolean getBoolAttr(java.util.Properties info, String key, boolean def)
   {
     boolean ret = def;
     String val = info.getProperty(key);
     if (val != null && val.length() > 0) {
       char c = val.charAt(0);
       return (c == 'Y' || c == 'y' || c == '1');
     } else {
       return def;
     }
   }
   private void connect(String host, int port,String db, int sendbs, int recvbs, int log_enable) throws VirtuosoException
   {
      int hostIndex = 0;
      int startIndex = 0;
      if (hostList.size() > 1 && useRoundRobin)
        startIndex = hostIndex = getNextRoundRobinHostIndex();
      while(true)
      {
        try {
          if (hostList.size() == 0) {
            connect(host, port, sendbs, recvbs);
          } else {
            VhostRec v = (VhostRec)hostList.elementAt(hostIndex);
            connect(v.host, v.port, sendbs, recvbs);
          }
          break;
        } catch (VirtuosoException e) {
          int erc = e.getErrorCode();
          if (erc != VirtuosoException.IOERROR && erc != VirtuosoException.NOLICENCE)
            throw e;
          hostIndex++;
          if (useRoundRobin) {
            if (hostList.size() == hostIndex)
              hostIndex = 0;
            if (hostIndex == startIndex)
              throw e;
          }
          else if (hostList.size() == hostIndex) {
            throw e;
          }
        }
      }
      if(db!=null)
        try {
          new VirtuosoStatement(this).executeQuery("use "+db);
        } catch (VirtuosoException ve) {
          throw new VirtuosoException(ve, "Could not execute 'use "+db+"'", VirtuosoException.SQLERROR);
        }
      if (log_enable >= 0 && log_enable <= 3)
        try {
          new VirtuosoStatement(this).executeQuery("log_enable ("+log_enable+")");
        } catch (VirtuosoException ve) {
          throw new VirtuosoException(ve, "Could not execute 'log_enable("+log_enable+")'", VirtuosoException.SQLERROR);
        }
   }
   private long cdef_param (openlink.util.Vector cdefs, String name, long deflt)
     {
       int len = cdefs != null ? cdefs.size() : 0;
       int inx;
       for (inx = 0; inx < len; inx += 2)
  if (name.equals ((String) cdefs.elementAt (inx)))
    {
      return (((Number)cdefs.elementAt (inx + 1)).longValue());
    }
       return deflt;
     }
   private Object[] fill_login_info_array ()
   {
       Object[] ret = new Object[7];
       ret[0] = new String ("JDBC");
       ret[1] = new Integer (0);
       ret[2] = new String ("");
       ret[3] = System.getProperty("os.name");
       ret[4] = new String ("");
       ret[5] = new Integer (0);
       ret[6] = new String (con_delegate != null ? con_delegate : "");
       return ret;
   }
    private Collection getCertificates(InputStream fis)
     throws CertificateException
    {
        CertificateFactory cf;
        cf = CertificateFactory.getInstance("X.509");
        return cf.generateCertificates(fis);
    }
  private void connect(String host, int port, int sendbs, int recvbs) throws VirtuosoException
   {
      String fname = null;
      try
      {
        if(use_ssl || truststore_path != null || keystore_path != null)
   {
               if (ssl_provider != null && ssl_provider.length() != 0) {
  Security.addProvider((Provider)(Class.forName(ssl_provider).newInstance()));
       }
               SSLContext ssl_ctx = SSLContext.getInstance("TLS");
               X509TrustManager tm = new VirtX509TrustManager();
  KeyManager []km = null;
               TrustManager[] tma = null;
               KeyStore tks = null;
               if (truststore_path.length() > 0) {
                   InputStream fis = null;
                   String keys_pwd = (truststore_pass != null) ? truststore_pass : "";
                   String alg = TrustManagerFactory.getDefaultAlgorithm();
                   TrustManagerFactory tmf = TrustManagerFactory.getInstance(alg);
                   tks = KeyStore.getInstance("JKS");
                   try {
                     fname = truststore_path;
                     fis = new FileInputStream(truststore_path);
                     if (truststore_path.endsWith(".pem") || truststore_path.endsWith(".crt") || truststore_path.endsWith(".p7b"))
                       {
                         tks.load(null);
                         Collection certs = getCertificates(fis);
                         if (certs!=null)
                           {
                             int i=0;
                             for(Iterator it=certs.iterator(); it.hasNext();)
                             {
                               tks.setCertificateEntry("cert"+i, (java.security.cert.Certificate) it.next());
                               i++;
                             }
                           }
       }
     else
                       tks.load(fis, keys_pwd.toCharArray());
                   } finally {
                     if (fis!=null)
                       fis.close();
                   }
                   tmf.init(tks);
                   tma = tmf.getTrustManagers();
               } else {
                   tma = new TrustManager[]{tm};
               }
               if (keystore_path.length() > 0 && keystore_pass.length() > 0) {
                   String keys_file = (keystore_path != null) ? keystore_path : System.getProperty("user.home") + System.getProperty("file.separator");
                   String keys_pwd = (keystore_pass != null) ? keystore_pass : "";
                   fname = keys_file;
                   km = new KeyManager[]{new VirtX509KeyManager(cert_alias, keys_file, keys_pwd, tks)};
       }
               ssl_ctx.init(km, tma, new SecureRandom());
               socket = ((SSLSocketFactory) ssl_ctx.getSocketFactory()).createSocket(host, port);
     ((SSLSocket)socket).startHandshake();
   }
 else
  socket = new Socket(host,port);
  if (timeout > 0)
    socket.setSoTimeout(timeout);
  socket.setTcpNoDelay(true);
         socket.setReceiveBufferSize(recvbs);
         socket.setSendBufferSize(sendbs);
         in = new VirtuosoInputStream(this,socket, recvbs);
  out = new VirtuosoOutputStream(this,socket, sendbs);
  synchronized (this)
    {
      Object [] caller_id_args = new Object[1];
      caller_id_args[0] = null;
      VirtuosoFuture future = getFuture(VirtuosoFuture.callerid,caller_id_args, timeout);
      openlink.util.Vector result_future = (openlink.util.Vector)future.nextResult().firstElement();
      peer_name = (String)(result_future.elementAt(1));
      if (result_future.size() > 2)
        {
   openlink.util.Vector caller_id_opts = (openlink.util.Vector)result_future.elementAt(2);
   int pwd_clear_code = (int) cdef_param (caller_id_opts, "SQL_ENCRYPTION_ON_PASSWORD", 3);
   switch (pwd_clear_code)
     {
       case 1: pwdclear = "cleartext"; break;
       case 2: pwdclear = "encrypt"; break;
       case 0: pwdclear = "digest"; break;
     }
        }
      removeFuture(future);
      Object[] args = new Object[4];
      args[0] = user;
      if (pwdclear != null && pwdclear.equals ("cleartext"))
        {
   args[1] = password;
        }
      else if (pwdclear != null && pwdclear.equals ("encrypt"))
        {
   args[1] = MD5.pwd_magic_encrypt (user, password);
        }
      else
        {
   args[1] = MD5.md5_digest (user, password, peer_name);
        }
      args[2] = VirtuosoTypes.version;
      args[3] = new openlink.util.Vector (fill_login_info_array ());
      future = getFuture(VirtuosoFuture.scon,args, this.timeout);
      result_future = (openlink.util.Vector)future.nextResult();
      if(!(result_future.firstElement() instanceof Short))
        {
   result_future = (openlink.util.Vector)result_future.firstElement();
   switch(((Number)result_future.firstElement()).shortValue())
     {
       case VirtuosoTypes.QA_LOGIN:
    qualifier = (String)result_future.elementAt(1);
    version = (String)result_future.elementAt(2);
                         int con_db_gen = Integer.parseInt (version.substring(6));
                         if (con_db_gen < 2303)
                    {
        throw new VirtuosoException (
          "Old server version", VirtuosoException.MISCERROR);
                    }
    _case = ((Number)result_future.elementAt(3)).intValue();
    if (result_future.size() > 3)
      client_defaults = (openlink.util.Vector)(result_future.elementAt (4));
    else
      client_defaults = null;
    Object obj = null;
    if (result_future.size() > 4)
      obj = result_future.elementAt (5);
    if (obj instanceof openlink.util.Vector)
      {
        client_charset = (openlink.util.Vector)obj;
        String table = (String)client_charset.elementAt (1);
        client_charset_hash = new Hashtable<Character,Byte> (256);
        for (int i = 0; i < 255; i++)
          {
     if (i < table.length())
       {
         client_charset_hash.put (
      new Character (table.charAt(i)),
      new Byte ((byte) (i + 1)));
       }
     else
       {
         client_charset_hash.put (
      new Character ((char) (i + 1)),
      new Byte ((byte) (i + 1)));
       }
          }
      }
    else
      client_charset = null;
    if (timeout <= 0) {
      timeout = (int) (cdef_param (client_defaults, "SQL_QUERY_TIMEOUT", timeout_def));
    }
                         if (timeout > 0)
      socket.setSoTimeout(timeout);
    if (txn_timeout <= 0) {
      txn_timeout = (int) (cdef_param (client_defaults, "SQL_TXN_TIMEOUT", txn_timeout * 1000)/1000);
    }
    trxisolation = (int) cdef_param (client_defaults, "SQL_TXN_ISOLATION", trxisolation);
    utf8_execs = cdef_param (client_defaults, "SQL_UTF8_EXECS", 0) != 0;
    if (!utf8_execs && cdef_param (client_defaults, "SQL_NO_CHAR_C_ESCAPE", 0) != 0)
      throw new VirtuosoException (
          "Not using UTF-8 encoding of SQL statements, " +
          "but processing character escapes also disabled",
          VirtuosoException.MISCERROR);
    timezoneless_datetimes = (int) cdef_param (client_defaults, "SQL_TIMEZONELESS_DATETIMES", 0);
    break;
       case VirtuosoTypes.QA_ERROR:
    removeFuture(future);
    throw new VirtuosoException((String)result_future.elementAt(1) + " " + (String)result_future.elementAt(2),VirtuosoException.NOLICENCE);
       default:
    removeFuture(future);
    throw new VirtuosoException(result_future.toString(),VirtuosoException.UNKNOWN);
     }
   ;
        }
      else
        {
   removeFuture(future);
   throw new VirtuosoException("Bad login.",VirtuosoException.BADLOGIN);
        }
      removeFuture(future);
    }
      }
      catch(NoClassDefFoundError e)
      {
         throw new VirtuosoException("Class not found: " + e.getMessage(),VirtuosoException.MISCERROR);
      }
      catch(FileNotFoundException e)
      {
         throw new VirtuosoException("Connection failed: "+ e.getMessage(),VirtuosoException.IOERROR);
      }
      catch(IOException e)
      {
         throw new VirtuosoException("Connection failed: ["+(fname!=null?fname:"")+"] "+e.getMessage(),VirtuosoException.IOERROR);
      }
      catch(ClassNotFoundException e)
      {
         throw new VirtuosoException("Class not found: " + e.getMessage(),VirtuosoException.MISCERROR);
      }
      catch(InstantiationException e)
      {
         throw new VirtuosoException("Class cannot be created: " + e.getMessage(),VirtuosoException.MISCERROR);
      }
      catch(IllegalAccessException e)
      {
         throw new VirtuosoException("Class cannot be accessed: " + e.getMessage(),VirtuosoException.MISCERROR);
      }
      catch(NoSuchAlgorithmException e)
      {
         throw new VirtuosoException("Encryption failed: " + e.getMessage(),VirtuosoException.MISCERROR);
      }
      catch(KeyStoreException e)
      {
         throw new VirtuosoException("Encryption failed: " + e.getMessage(),VirtuosoException.MISCERROR);
      }
      catch(KeyManagementException e)
      {
         throw new VirtuosoException("Encryption failed: " + e.getMessage(),VirtuosoException.MISCERROR);
      }
      catch(CertificateException e)
      {
         throw new VirtuosoException("Encryption failed: " + e.getMessage(),VirtuosoException.MISCERROR);
      }
      catch(UnrecoverableKeyException e)
      {
         throw new VirtuosoException("Encryption failed: ["+(fname!=null?fname:"") +"]" + e.getMessage(),VirtuosoException.MISCERROR);
      }
   }
   protected void write_object(Object obj) throws IOException, VirtuosoException
   {
     if (VirtuosoFuture.rpc_log != null)
       {
      VirtuosoFuture.rpc_log.print ("  >> (conn " + hashCode() + ") OUT ");
      VirtuosoFuture.rpc_log.println (obj != null ? obj.toString() : "<null>");
       }
    try {
        out.write_object(obj);
        out.flush();
    } catch (IOException ex) {
        if (pooled_connection != null) {
            VirtuosoException vex =
                new VirtuosoException(
                    "Connection failed: " + ex.getMessage(),
                    VirtuosoException.IOERROR);
            pooled_connection.sendErrorEvent(vex);
            throw vex;
        } else {
            throw ex;
        }
    } catch (VirtuosoException ex) {
        if (pooled_connection != null) {
            int code = ex.getErrorCode();
            if (code == VirtuosoException.DISCONNECTED
                || code == VirtuosoException.IOERROR) {
             pooled_connection.sendErrorEvent(ex);
            }
        }
        throw ex;
    }
   }
   protected void write_bytes(byte [] bytes) throws IOException, VirtuosoException
   {
    try {
        for (int k = 0; k < bytes.length; k++)
            out.write(bytes[k]);
        out.flush();
    } catch (IOException ex) {
        if (pooled_connection != null) {
            VirtuosoException vex =
                new VirtuosoException(
                    "Connection failed: " + ex.getMessage(),
                    VirtuosoException.IOERROR);
            pooled_connection.sendErrorEvent(vex);
            throw vex;
        } else {
            throw ex;
        }
    }
   }
   protected VirtuosoFuture getFuture(String rpcname, Object[] args, int timeout)
       throws IOException, VirtuosoException
   {
     VirtuosoFuture fut = null;
     int this_req_no;
     if (futures == null)
       throw new VirtuosoException ("Activity on a closed connection", "IM001", VirtuosoException.SQLERROR);
     synchronized (futures)
       {
  this_req_no = req_no;
  req_no += 1;
       }
     fut = new VirtuosoFuture(this,rpcname,args,this_req_no, timeout);
     futures.put(new Integer(this_req_no),fut);
     return fut;
   }
   protected void clearFutures()
   {
     if (futures != null)
        synchronized (futures)
        {
   futures.clear();
        }
   }
   protected void removeFuture(VirtuosoFuture fut)
   {
     if (futures != null)
       futures.remove(new Integer(fut.hashCode()));
   }
   protected boolean read_request() throws IOException, VirtuosoException
   {
     if (futures == null)
       throw new VirtuosoException ("Activity on a closed connection", "IM001", VirtuosoException.SQLERROR);
     Object _result;
     try {
        _result = in.read_object();
     } catch (IOException ex) {
        if (pooled_connection != null) {
            VirtuosoException vex =
                new VirtuosoException(
                    "Connection failed: " + ex.getMessage(),
                    VirtuosoException.IOERROR);
            pooled_connection.sendErrorEvent(vex);
            throw vex;
        } else {
            throw ex;
        }
     } catch (VirtuosoException ex) {
        if (pooled_connection != null) {
            int code = ex.getErrorCode();
            if (code == VirtuosoException.DISCONNECTED
                || code == VirtuosoException.IOERROR) {
                pooled_connection.sendErrorEvent(ex);
            }
        }
        throw ex;
     }
     if (VirtuosoFuture.rpc_log != null)
       {
      VirtuosoFuture.rpc_log.print ("  << (conn " + hashCode() + ") IN ");
      VirtuosoFuture.rpc_log.println (_result != null ? _result.toString() : "<null>");
       }
     try
       {
  openlink.util.Vector result = (openlink.util.Vector)_result;
  Object tag = result.firstElement();
  if(((Short)tag).shortValue() != VirtuosoTypes.DA_FUTURE_ANSWER && ((Short)tag).shortValue() != VirtuosoTypes.DA_FUTURE_PARTIAL_ANSWER)
    return false;
  VirtuosoFuture fut = (VirtuosoFuture)futures.get(new Integer(((Number)result.elementAt(1)).intValue()));
  if(fut == null)
    return false;
  fut.putResult(result.elementAt(2));
  fut.complete(((Short)tag).shortValue() == VirtuosoTypes.DA_FUTURE_ANSWER);
  return true;
       }
     catch (ClassCastException e)
       {
         if (VirtuosoFuture.rpc_log != null)
           {
                 VirtuosoFuture.rpc_log.println ("  **(conn " + hashCode() + ") **** runtime2 " +
                     e.getClass().getName() + " in read_request");
                 e.printStackTrace(VirtuosoFuture.rpc_log);
           }
         throw new Error (e.getClass().getName() + ":" + e.getMessage());
       }
   }
   protected String getURL()
   {
      return url;
   }
   protected String getUserName()
   {
      return user;
   }
   protected String getQualifierName()
   {
      return qualifier;
   }
   protected String getVersion()
   {
      return version;
   }
   protected int getVersionNum ()
     {
       try
  {
    return (new Integer (version.substring (6, 10))).intValue();
  }
       catch (Exception e)
  {
    return 1619;
  }
     }
   protected int getCase()
   {
      return _case;
   }
   protected int getTimeout()
   {
      return timeout;
   }
   public void clearWarnings() throws VirtuosoException
   {
       warning = null;
   }
   public void close() throws VirtuosoException
   {
      if (isClosed())
        return;
      try
      {
         synchronized(this) {
           if(!in.isClosed())
           {
             in.close();
             in = null;
           }
           if(!out.isClosed())
           {
             out.close();
             out = null;
           }
           if(socket != null)
           {
             socket.close();
             socket = null;
           }
           pStatementCache.clear();
           user = url = password = null;
           futures = null;
           pooled_connection = null;
           xa_connection = null;
         }
      }
      catch(IOException e)
      {
      }
   }
   public synchronized void commit() throws VirtuosoException
   {
      if (global_transaction)
 throw new VirtuosoException("Cannot commit while in global transaction.", VirtuosoException.BADPARAM);
      try
      {
 Object[] args = new Object[2];
 args[0] = new Long(VirtuosoTypes.SQL_COMMIT);
 args[1] = null;
 VirtuosoFuture fut = getFuture(VirtuosoFuture.transaction,args, this.timeout);
 openlink.util.Vector trsres = fut.nextResult();
 Object _err = (trsres == null) ? null: ((openlink.util.Vector)trsres).firstElement();
 if (_err instanceof openlink.util.Vector)
   {
     openlink.util.Vector err = (openlink.util.Vector) _err;
     throw new VirtuosoException ((String) (err.elementAt (2)),
  (String) (err.elementAt (1)), VirtuosoException.SQLERROR);
   }
 removeFuture(fut);
      }
      catch(IOException e)
      {
         throw new VirtuosoException("Connection failed: " + e.getMessage(),VirtuosoException.IOERROR);
      }
   }
   public Statement createStatement() throws VirtuosoException
   {
      return createStatement(VirtuosoResultSet.TYPE_FORWARD_ONLY,VirtuosoResultSet.CONCUR_READ_ONLY);
   }
   public boolean getAutoCommit() throws VirtuosoException
   {
      return auto_commit;
   }
   public DatabaseMetaData getMetaData() throws VirtuosoException
   {
      return new VirtuosoDatabaseMetaData(this);
   }
   public SQLWarning getWarnings() throws VirtuosoException
   {
      return warning;
   }
   public void setTransactionIsolation(int level) throws VirtuosoException
   {
      if(level == Connection.TRANSACTION_READ_UNCOMMITTED || level == Connection.TRANSACTION_READ_COMMITTED || level == Connection.TRANSACTION_REPEATABLE_READ || level == Connection.TRANSACTION_SERIALIZABLE)
         trxisolation = level;
      else
         throw new VirtuosoException("Bad parameters.",VirtuosoException.BADPARAM);
   }
   public int getTransactionIsolation() throws VirtuosoException
   {
      return trxisolation;
   }
   public boolean isClosed()
   {
      return
         (socket == null)
         || (in == null || in.isClosed())
         || (out == null || out.isClosed())
         ;
   }
   public CallableStatement prepareCall(String sql) throws VirtuosoException
   {
      return prepareCall(sql,VirtuosoResultSet.TYPE_FORWARD_ONLY,VirtuosoResultSet.CONCUR_READ_ONLY);
   }
   public PreparedStatement prepareStatement(String sql) throws VirtuosoException
   {
      return prepareStatement(sql,VirtuosoResultSet.TYPE_FORWARD_ONLY,VirtuosoResultSet.CONCUR_READ_ONLY);
   }
   public synchronized void rollback() throws VirtuosoException
   {
      if (global_transaction)
 throw new VirtuosoException("Cannot rollback while in global transaction.", VirtuosoException.BADPARAM);
      try
      {
         Object[] args = new Object[2];
         args[0] = new Long(VirtuosoTypes.SQL_ROLLBACK);
         args[1] = null;
         VirtuosoFuture fut = getFuture(VirtuosoFuture.transaction,args, this.timeout);
         openlink.util.Vector trsres = fut.nextResult();
  Object _err = (trsres == null) ? null: ((openlink.util.Vector)trsres).firstElement();
  if (_err instanceof openlink.util.Vector)
    {
      openlink.util.Vector err = (openlink.util.Vector) _err;
      throw new VirtuosoException ((String) (err.elementAt (2)),
   (String) (err.elementAt (1)), VirtuosoException.SQLERROR);
    }
         if(fut!=null) removeFuture(fut);
      }
      catch(IOException e)
      {
         throw new VirtuosoException("Connection failed: " + e.getMessage(),VirtuosoException.IOERROR);
      }
   }
   public void setAutoCommit(boolean autoCommit) throws VirtuosoException
   {
      if (autoCommit && global_transaction)
 throw new VirtuosoException("Cannot set autocommit mode while in global transaction.", VirtuosoException.BADPARAM);
      this.auto_commit = autoCommit;
   }
   public Statement createStatement(int resultSetType, int resultSetConcurrency) throws VirtuosoException
   {
      return new VirtuosoStatement(this,resultSetType,resultSetConcurrency);
   }
   public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws VirtuosoException
   {
      return new VirtuosoCallableStatement(this,sql,resultSetType,resultSetConcurrency);
   }
   public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws VirtuosoException
   {
     if (useCachePrepStatements) {
       VirtuosoPreparedStatement ps = null;
       synchronized(pStatementCache) {
         ps = pStatementCache.remove(""+resultSetType+"#"
                                       +resultSetConcurrency+"#"
                  +sql);
         if (ps != null) {
           ps.setClosed(false);
           ps.clearParameters();
         } else {
           ps = new VirtuosoPreparedStatement(this, sql, resultSetType,
             resultSetConcurrency);
           ps.isCached = true;
         }
       }
       return ps;
     }
     else
     {
       return new VirtuosoPreparedStatement(this,sql,resultSetType,resultSetConcurrency);
     }
   }
   public int hashCode()
   {
      return con_no;
   }
   public void finalize() throws Throwable
   {
      close();
   }
   public boolean isReadOnly() throws VirtuosoException
   {
      return readOnly;
   }
   public void setReadOnly(boolean readOnly) throws VirtuosoException
   {
     this.readOnly = readOnly;
   }
   public String nativeSQL(String sql) throws VirtuosoException
   {
      return "";
   }
   public void setCatalog(String catalog) throws VirtuosoException
   {
      VirtuosoStatement st = null;
      if (catalog!=null) {
        try {
          st = new VirtuosoStatement(this);
          st.executeQuery("use "+catalog);
          qualifier = catalog;
        } finally {
          if (st!=null) {
            try {
              st.close();
            } catch (Exception e) {}
          }
        }
      }
   }
   public String getCatalog() throws VirtuosoException
   {
      return qualifier;
   }
   public java.util.Map<String, Class<?>> getTypeMap() throws VirtuosoException
   {
      return null;
   }
   public void setTypeMap(java.util.Map<String,Class<?>> map) throws VirtuosoException
   {
   }
   protected void setSocketTimeout (int timeout) throws VirtuosoException
     {
      try
 {
   if (timeout != -1)
     socket.setSoTimeout (timeout);
 }
      catch (java.net.SocketException e)
 {
   throw new VirtuosoException ("Unable to set socket timeout : " + e.getMessage(),
       "S1000", VirtuosoException.MISCERROR);
 }
     }
   protected VirtuosoExplicitString escapeSQL (String sql) throws VirtuosoException
     {
       VirtuosoExplicitString sql1;
       if (this.charset != null)
  {
    byte [] bytes = charsetBytes(sql);
    sql1 = new VirtuosoExplicitString (bytes, VirtuosoTypes.DV_STRING);
    return sql1;
  }
       if (this.charset_utf8)
       {
   sql1 = new VirtuosoExplicitString (sql, VirtuosoTypes.DV_STRING, this);
   return sql1;
       }
       if (this.utf8_execs)
  {
    try
      {
        byte [] bytes = (new String ("\n--utf8_execs=yes\n" + sql)).getBytes("UTF8");
        sql1 = new VirtuosoExplicitString (bytes, VirtuosoTypes.DV_STRING);
      }
    catch (java.io.UnsupportedEncodingException e)
      {
        sql1 = new VirtuosoExplicitString ("\n--utf8_execs=yes\n" + sql,
     VirtuosoTypes.DV_STRING, this);
      }
  }
       else
  {
    sql1 = new VirtuosoExplicitString ("", VirtuosoTypes.DV_STRING, null);
    sql1.cli_wide_to_escaped (sql, this.client_charset_hash);
  }
       return sql1;
     }
   protected VirtuosoExplicitString escapeSQLString (String sql) throws VirtuosoException
     {
       VirtuosoExplicitString sql1;
       if (this.charset != null)
  {
    byte [] bytes = charsetBytes(sql);
    sql1 = new VirtuosoExplicitString (bytes, VirtuosoTypes.DV_STRING);
    return sql1;
  }
       if (this.utf8_execs)
  {
    try
      {
        byte [] bytes = sql.getBytes("UTF8");
        sql1 = new VirtuosoExplicitString (bytes, VirtuosoTypes.DV_STRING);
      }
    catch (java.io.UnsupportedEncodingException e)
      {
        sql1 = new VirtuosoExplicitString (sql,
     VirtuosoTypes.DV_STRING, this);
      }
  }
       else
  {
    sql1 = new VirtuosoExplicitString ("", VirtuosoTypes.DV_STRING, null);
    sql1.cli_wide_to_escaped (sql, this.client_charset_hash);
  }
       return sql1;
     }
   protected byte[] charsetBytes1(String source, String from, String to) throws VirtuosoException
    {
       byte ans[] = new byte[0];
       ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream( source.length() );
       try
  {
    OutputStreamWriter outputWriter = new OutputStreamWriter(byteArrayOutputStream, from);
    outputWriter.write(source, 0, source.length());
    outputWriter.flush();
    byte[] bytes = byteArrayOutputStream.toByteArray();
    ans = bytes;
  }
       catch (Exception e)
  {
    throw new VirtuosoException (
        "InternationalizationHelper: UnsupportedEncodingException: " + e,
        VirtuosoException.CASTERROR);
  }
       return ans;
    }
   protected byte[] charsetBytes(String source) throws VirtuosoException
     {
       if (source == null)
  return null;
       return charsetBytes1(source, this.charset, "8859_1");
     }
   protected String uncharsetBytes(String source) throws VirtuosoException
     {
       if (source == null)
  return null;
       ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream( source.length() );
       try
  {
    OutputStreamWriter outputWriter = new OutputStreamWriter(byteArrayOutputStream, "8859_1");
    outputWriter.write(source, 0, source.length());
    outputWriter.flush();
    byte[] bytes = byteArrayOutputStream.toByteArray();
    BufferedReader bufferedReader =
        (new BufferedReader( new InputStreamReader( new ByteArrayInputStream(bytes), this.charset)));
    StringBuffer buf = new StringBuffer();
    char cbuf [] = new char[4096];
    int read;
    while (0 < (read = bufferedReader.read (cbuf)))
      buf.append (cbuf, 0, read);
    return buf.toString();
  }
       catch (Exception e)
  {
    throw new VirtuosoException (
        "InternationalizationHelper: UnsupportedEncodingException: " + e,
        VirtuosoException.CASTERROR);
  }
     }
   protected void checkHoldability (int holdability) throws SQLException
     {
       if (holdability == ResultSet.HOLD_CURSORS_OVER_COMMIT)
         throw new VirtuosoException ("Unable to hold cursors over commit", "IM001",
     VirtuosoException.NOTIMPLEMENTED);
       else if (holdability != ResultSet.CLOSE_CURSORS_AT_COMMIT)
         throw new VirtuosoException ("Invalid holdability value", "22023",
     VirtuosoException.BADPARAM);
     }
   public void setHoldability (int holdability) throws SQLException
     {
       checkHoldability (holdability);
     }
   public int getHoldability() throws SQLException
     {
       return ResultSet.CLOSE_CURSORS_AT_COMMIT;
     }
   public Savepoint setSavepoint() throws SQLException
     {
       throw new VirtuosoException ("Savepoints not supported", "IM001",
         VirtuosoException.NOTIMPLEMENTED);
     }
   public Savepoint setSavepoint(String name) throws SQLException
     {
       throw new VirtuosoException ("Savepoints not supported", "IM001",
         VirtuosoException.NOTIMPLEMENTED);
     }
   public void rollback(Savepoint savepoint) throws SQLException
     {
       throw new VirtuosoException ("Savepoints not supported", "IM001",
         VirtuosoException.NOTIMPLEMENTED);
     }
   public void releaseSavepoint(Savepoint savepoint) throws SQLException
     {
       throw new VirtuosoException ("Savepoints not supported", "IM001",
         VirtuosoException.NOTIMPLEMENTED);
     }
   public Statement createStatement(int resultSetType,
       int resultSetConcurrency,
       int resultSetHoldability) throws SQLException
     {
       checkHoldability (resultSetHoldability);
       return createStatement (resultSetType, resultSetConcurrency);
     }
   public PreparedStatement prepareStatement(String sql,
       int resultSetType,
       int resultSetConcurrency,
       int resultSetHoldability) throws SQLException
     {
       checkHoldability (resultSetHoldability);
       return prepareStatement (sql, resultSetType, resultSetConcurrency);
     }
   public CallableStatement prepareCall(String sql,
       int resultSetType,
       int resultSetConcurrency,
       int resultSetHoldability) throws SQLException
     {
       checkHoldability (resultSetHoldability);
       return prepareCall (sql, resultSetType, resultSetConcurrency);
     }
   public PreparedStatement prepareStatement(String sql,
       int autoGeneratedKeys) throws SQLException
     {
       return prepareStatement (sql);
     }
   public PreparedStatement prepareStatement(String sql,
       int[] columnIndexes) throws SQLException
     {
       return prepareStatement (sql);
     }
   public PreparedStatement prepareStatement(String sql,
       String[] columnNames) throws SQLException
     {
       return prepareStatement (sql);
     }
   synchronized void checkClosed() throws SQLException
   {
        if (isClosed())
            throw new VirtuosoException("The connection is already closed.",VirtuosoException.DISCONNECTED);
    }
  public Clob createClob() throws SQLException
  {
    return new VirtuosoBlob();
  }
  public Blob createBlob() throws SQLException
  {
    return new VirtuosoBlob();
  }
  public NClob createNClob() throws SQLException
  {
    return new VirtuosoBlob();
  }
  public SQLXML createSQLXML() throws SQLException
  {
     throw new VirtuosoFNSException ("createSQLXML()  not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public boolean isValid(int _timeout) throws SQLException
  {
    if (isClosed())
      return false;
    boolean isLost = true;
    try {
      try {
        isLost = isConnectionLost(_timeout);
      } catch (Throwable t) {
        try {
          abortInternal();
        } catch (Throwable ignoreThrown) {
        }
        return false;
      }
    } catch (Throwable t) {
      return false;
    }
    return !isLost;
  }
  public void setClientInfo(String name, String value) throws SQLClientInfoException
  {
    Map<String, ClientInfoStatus> fail = new HashMap<String, ClientInfoStatus>();
    fail.put(name, ClientInfoStatus.REASON_UNKNOWN_PROPERTY);
    throw new SQLClientInfoException("ClientInfo property not supported", fail);
  }
  public void setClientInfo(Properties properties) throws SQLClientInfoException
  {
    if (properties == null || properties.size() == 0)
      return;
    Map<String, ClientInfoStatus> fail = new HashMap<String, ClientInfoStatus>();
    Iterator<String> i = properties.stringPropertyNames().iterator();
    while (i.hasNext()) {
      fail.put(i.next(), ClientInfoStatus.REASON_UNKNOWN_PROPERTY);
    }
    throw new SQLClientInfoException("ClientInfo property not supported", fail);
  }
  public String getClientInfo(String name) throws SQLException
  {
    return null;
  }
  public Properties getClientInfo() throws SQLException
  {
    return null;
  }
  public Array createArrayOf(String typeName, Object[] elements) throws SQLException
  {
      checkClosed();
      if (typeName == null)
          throw new VirtuosoException("typeName is null.",VirtuosoException.MISCERROR);
      if (elements == null)
          return null;
      return new VirtuosoArray(this, typeName, elements);
  }
  public Struct createStruct(String typeName, Object[] attributes) throws SQLException
  {
    throw new VirtuosoFNSException ("createStruct(typeName, attributes)  not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public <T> T unwrap(java.lang.Class<T> iface) throws java.sql.SQLException
  {
    try {
      return iface.cast(this);
    } catch (ClassCastException cce) {
      throw new VirtuosoException ("Unable to unwrap to "+iface.toString(), "22023", VirtuosoException.BADPARAM);
    }
  }
  public boolean isWrapperFor(java.lang.Class<?> iface) throws java.sql.SQLException
  {
    if(isClosed())
      throw new VirtuosoException("The connection is closed.",VirtuosoException.DISCONNECTED);
    return iface.isInstance(this);
  }
  public void setSchema(String schema) throws java.sql.SQLException
  {
    if(isClosed())
      throw new VirtuosoException("The connection is closed.",VirtuosoException.DISCONNECTED);
  }
  public String getSchema() throws java.sql.SQLException
  {
    if(isClosed())
      throw new VirtuosoException("The connection is closed.",VirtuosoException.DISCONNECTED);
    return null;
  }
  public void abort(java.util.concurrent.Executor executor) throws java.sql.SQLException
  {
    SecurityManager sec = System.getSecurityManager();
    if (sec != null)
      sec.checkPermission(ABORT_PERM);
    if (executor == null)
      throw new VirtuosoException ("Executor cannot be null",
                    VirtuosoException.BADPARAM);
    executor.execute(new Runnable()
    {
      public void run() {
        try {
          abortInternal();
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }
    });
  }
  public void setNetworkTimeout(java.util.concurrent.Executor executor,
      final int milliseconds) throws java.sql.SQLException
  {
    SecurityManager sec = System.getSecurityManager();
    if (sec != null)
      sec.checkPermission(SET_NETWORK_TIMEOUT_PERM);
    if (executor == null)
      throw new VirtuosoException ("Executor cannot be null",
                    VirtuosoException.BADPARAM);
    if(isClosed())
      throw new VirtuosoException("The connection is closed.",VirtuosoException.DISCONNECTED);
    executor.execute(new Runnable()
    {
      public void run() {
          try {
            setSocketTimeout(milliseconds);
          } catch (SQLException e) {
            throw new RuntimeException(e);
          }
      }
    });
  }
  public int getNetworkTimeout() throws java.sql.SQLException
  {
    if(isClosed())
      throw new VirtuosoException("The connection is closed.",VirtuosoException.DISCONNECTED);
    return timeout;
  }
  private void abortInternal() throws java.sql.SQLException
  {
    if (isClosed())
      return;
    try {
        close();
    } catch (Throwable t) {
    }
  }
  private void createCaches(int cacheSize)
  {
    pStatementCache = new LRUCache<String,VirtuosoPreparedStatement>(cacheSize) {
 protected boolean removeEldestEntry(java.util.Map.Entry eldest) {
   if (this.maxSize <= 1) {
     return false;
   }
   boolean remove = super.removeEldestEntry(eldest);
   if (remove) {
     VirtuosoPreparedStatement ps =
         (VirtuosoPreparedStatement)eldest.getValue();
     ps.isCached = false;
     ps.setClosed(false);
     try {
       ps.close();
     } catch (SQLException ex) {
     }
   }
   return remove;
 }
    };
  }
  protected void recacheStmt(VirtuosoPreparedStatement ps) throws SQLException
  {
    if (ps.isPoolable()) {
      synchronized (pStatementCache) {
        pStatementCache.put(""+ps.getResultSetType()+"#"
                +ps.getResultSetConcurrency()+"#"
                +ps.sql, ps);
      }
    }
  }
    boolean getGlobalTransaction() {
    if (VirtuosoFuture.rpc_log != null)
    {
     VirtuosoFuture.rpc_log.println ("VirtuosoConnection.getGlobalTransaction () (con=" + this.hashCode() + ") :" + global_transaction);
    }
        return global_transaction;
    }
    void setGlobalTransaction(boolean value) {
    if (VirtuosoFuture.rpc_log != null)
    {
     VirtuosoFuture.rpc_log.println ("VirtuosoConnection.getGlobalTransaction (" + value + ") (con=" + this.hashCode() + ") :" + global_transaction);
    }
        global_transaction = value;
    }
    protected void setWarning (SQLWarning warn)
    {
 warn.setNextWarning (warning);
 warning = warn;
    }
    protected VirtuosoException notify_error (Throwable e)
    {
 VirtuosoException vex;
 if (!(e instanceof VirtuosoException))
 {
     vex = new VirtuosoException(e.getMessage(), VirtuosoException.IOERROR);
     vex.initCause (e);
 }
 else
     vex = (VirtuosoException) e;
        if (pooled_connection != null && isCriticalError(vex)) {
            pooled_connection.sendErrorEvent(vex);
 }
 return vex;
    }
    public static boolean isCriticalError(SQLException ex)
    {
      if (ex == null)
        return false;
      String SQLstate = ex.getSQLState();
      if (SQLstate != null && SQLstate.startsWith("08")
          && SQLstate != "08C04"
          && SQLstate != "08C03"
          && SQLstate != "08001"
          && SQLstate != "08003"
          && SQLstate != "08006"
          && SQLstate != "08007"
          )
        return true;
      int vendor = ex.getErrorCode();
      if (vendor == VirtuosoException.DISCONNECTED
          || vendor == VirtuosoException.IOERROR
          || vendor == VirtuosoException.BADLOGIN
          || vendor == VirtuosoException.BADTAG
          || vendor == VirtuosoException.CLOSED
          || vendor == VirtuosoException.EOF
          || vendor == VirtuosoException.NOLICENCE
          || vendor == VirtuosoException.UNKNOWN)
        return true;
      else
        return false;
    }
}
class VirtX509TrustManager implements X509TrustManager
{
  public boolean isClientTrusted(java.security.cert.X509Certificate[] chain)
    {
      return true;
    }
  public boolean isServerTrusted(java.security.cert.X509Certificate[] chain)
    {
      return true;
    }
  public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
    {
    }
  public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
    {
    }
  public X509Certificate[] getAcceptedIssuers()
    {
      return null;
    }
}
class VirtX509KeyManager extends X509ExtendedKeyManager {
    X509KeyManager defaultKeyManager;
    String defAlias;
    KeyStore tks;
    ArrayList<X509Certificate> certs = new ArrayList<X509Certificate>(32);
    public VirtX509KeyManager(String cert_alias, String keys_file, String keys_pwd, KeyStore tks)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException
    {
        KeyManager[] km;
        KeyStore ks;
        if (keys_file.endsWith(".p12") || keys_file.endsWith(".pfx"))
            ks = KeyStore.getInstance("PKCS12");
        else
            ks = KeyStore.getInstance("JKS");
        InputStream is = null;
        try {
          is = new FileInputStream(keys_file);
          ks.load(is, keys_pwd.toCharArray());
        } finally {
          if (is!=null)
            is.close();
        }
        if (cert_alias == null)
          {
            String alias = null;
            Enumeration<String> en = ks.aliases();
            while(en.hasMoreElements()) {
                alias = en.nextElement();
                ks.isKeyEntry(alias);
                break;
            }
            defAlias = alias;
          }
        else
          {
            if (!ks.containsAlias(cert_alias))
              throw new KeyStoreException("Could not found alias:["+cert_alias+"] in KeyStore :"+keys_file);
            defAlias = cert_alias;
          }
        certs.add((X509Certificate) ks.getCertificate(defAlias));
        if (tks!=null) {
            for(Enumeration<String> en = tks.aliases(); en.hasMoreElements(); ) {
                String alias = en.nextElement();
                certs.add((X509Certificate) tks.getCertificate(alias));
            }
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, keys_pwd.toCharArray());
        defaultKeyManager = (X509KeyManager)kmf.getKeyManagers()[0];
    }
    public String[] getClientAliases(String s, Principal[] principals) {
        return defaultKeyManager.getClientAliases(s, principals);
    }
    public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket)
    {
        return defAlias;
    }
    public String[] getServerAliases(String s, Principal[] principals) {
        return defaultKeyManager.getServerAliases(s, principals);
    }
    public String chooseServerAlias(String s, Principal[] principals, Socket socket) {
        return defaultKeyManager.chooseServerAlias(s, principals, socket);
    }
    public X509Certificate[] getCertificateChain(String s) {
        return defaultKeyManager.getCertificateChain(s);
    }
    public PrivateKey getPrivateKey(String s) {
        return defaultKeyManager.getPrivateKey(s);
    }
}
