package virtuoso.jdbc4;
import java.sql.*;
import java.net.*;
import java.io.*;
import java.util.*;
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
   private int trxisolation = Connection.TRANSACTION_REPEATABLE_READ;
   private boolean readOnly = false;
   protected int timeout = 60;
   protected int txn_timeout = 0;
   protected int fbs = VirtuosoTypes.DEFAULTPREFETCH;
   protected boolean utf8_execs = false;
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
 if (charset.indexOf("UTF-8") != -1)
 {
     this.charset = null;
     this.charset_utf8 = true;
 }
      }
      user = (String)prop.get("user");
      if(user == null || user.equals(""))
         user = "anonymous";
      password = (String)prop.get("password");
      if(password == null)
         password = "";
      if(prop.get("timeout") != null)
     timeout = Integer.parseInt(prop.getProperty("timeout"));
      pwdclear = (String)prop.get("pwdclear");
      if(prop.get("sendbs") != null)
     sendbs = Integer.parseInt(prop.getProperty("sendbs"));
      if(prop.get("recvbs") != null)
     recvbs = Integer.parseInt(prop.getProperty("recvbs"));
      if(prop.get("fbs") != null)
     fbs = Integer.parseInt(prop.getProperty("fbs"));
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
          if (e.getErrorCode() != VirtuosoException.IOERROR)
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
      if(db!=null) new VirtuosoStatement(this).executeQuery("use "+db);
      if (log_enable >= 0 && log_enable <= 3)
        new VirtuosoStatement(this).executeQuery("log_enable ("+log_enable+")");
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
  private void connect(String host, int port, int sendbs, int recvbs) throws VirtuosoException
   {
      try
      {
  socket = new Socket(host,port);
  socket.setSoTimeout(timeout*1000);
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
   int pwd_clear_code = (int) cdef_param (caller_id_opts, "SQL_ENCRYPTION_ON_PASSWORD", -1);
   switch (pwd_clear_code)
     {
       case 1: pwdclear = "cleartext"; break;
       case 2: pwdclear = "encrypt"; break;
       case 0: pwdclear = "digest"; break;
     }
        }
      removeFuture(future);
      Object[] args = new Object[3];
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
    timeout = (int) (cdef_param (client_defaults, "SQL_QUERY_TIMEOUT", timeout * 1000) / 1000);
    socket.setSoTimeout(timeout*1000);
    txn_timeout = (int) (cdef_param (client_defaults, "SQL_TXN_TIMEOUT", txn_timeout * 1000)/ 1000);
    trxisolation = (int) cdef_param (client_defaults, "SQL_TXN_ISOLATION", trxisolation);
    utf8_execs = cdef_param (client_defaults, "SQL_UTF8_EXECS", 0) != 0;
    if (!utf8_execs && cdef_param (client_defaults, "SQL_NO_CHAR_C_ESCAPE", 0) != 0)
      throw new VirtuosoException (
          "Not using UTF-8 encoding of SQL statements, " +
          "but processing character escapes also disabled",
          VirtuosoException.MISCERROR);
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
      catch(IOException e)
      {
         throw new VirtuosoException("Connection failed: " + e.getMessage(),VirtuosoException.IOERROR);
      }
   }
   protected void write_object(Object obj) throws IOException, VirtuosoException
   {
     if (VirtuosoFuture.rpc_log != null)
       {
  synchronized (VirtuosoFuture.rpc_log)
    {
      VirtuosoFuture.rpc_log.print ("(conn " + hashCode() + ") OUT ");
      VirtuosoFuture.rpc_log.println (obj != null ? obj.toString() : "<null>");
      VirtuosoFuture.rpc_log.flush();
    }
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
  synchronized (VirtuosoFuture.rpc_log)
    {
      VirtuosoFuture.rpc_log.print ("(conn " + hashCode() + ") IN ");
      VirtuosoFuture.rpc_log.println (_result != null ? _result.toString() : "<null>");
      VirtuosoFuture.rpc_log.flush();
    }
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
             synchronized (VirtuosoFuture.rpc_log)
               {
                 VirtuosoFuture.rpc_log.println ("(conn " + hashCode() + ") **** runtime2 " +
                     e.getClass().getName() + " in read_request");
                 e.printStackTrace(VirtuosoFuture.rpc_log);
   VirtuosoFuture.rpc_log.flush();
               }
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
      try
      {
         if(isClosed())
            throw new VirtuosoException("The connection is already closed.",VirtuosoException.DISCONNECTED);
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
     socket.setSoTimeout (timeout * 1000);
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
       System.err.println ("charsetBytes1(" + from + " , " + to);
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
   public boolean isValid(int timeout) throws SQLException
   {
     throw new VirtuosoFNSException ("isValid(timeout)  not supported", VirtuosoException.NOTIMPLEMENTED);
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
    throw new VirtuosoFNSException ("createArrayOf(typeName, elements)  not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public Struct createStruct(String typeName, Object[] attributes) throws SQLException
  {
    throw new VirtuosoFNSException ("createStruct(typeName, attributes)  not supported", VirtuosoException.NOTIMPLEMENTED);
  }
  public <T> T unwrap(java.lang.Class<T> iface) throws java.sql.SQLException
  {
    if(isClosed())
      throw new VirtuosoException("The connection is already closed.",VirtuosoException.DISCONNECTED);
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
        synchronized (VirtuosoFuture.rpc_log)
        {
     VirtuosoFuture.rpc_log.println ("VirtuosoConnection.getGlobalTransaction () (con=" + this.hashCode() + ") :" + global_transaction);
     VirtuosoFuture.rpc_log.flush();
        }
    }
        return global_transaction;
    }
    void setGlobalTransaction(boolean value) {
    if (VirtuosoFuture.rpc_log != null)
    {
        synchronized (VirtuosoFuture.rpc_log)
        {
     VirtuosoFuture.rpc_log.println ("VirtuosoConnection.getGlobalTransaction (" + value + ") (con=" + this.hashCode() + ") :" + global_transaction);
     VirtuosoFuture.rpc_log.flush();
        }
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
