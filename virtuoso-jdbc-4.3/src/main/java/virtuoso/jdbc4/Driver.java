package virtuoso.jdbc4;
import java.sql.*;
import java.util.*;
public class Driver implements java.sql.Driver
{
   static
   {
      try
      {
         DriverManager.registerDriver(new Driver());
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }
   protected static final int major = 3;
   protected static final int minor = 117;
   private String host = "localhost";
   private String port = "1111";
   private String user, password, database, charset, pwdclear;
   private Integer timeout, log_enable;
   private String keystore_cert, keystore_pass, keystore_path;
   private String ssl_provider;
   private Integer fbs, sendbs, recvbs;
   private final String VirtPrefix = "jdbc:virtuoso://";
   public Driver() throws SQLException
   {
     try
       {
  String log_file = System.getProperty(
      "JDBC4_LOG"
      );
  if (log_file != null)
    {
      System.err.println ("RPC logfile=" + log_file);
      try
        {
          VirtuosoFuture.rpc_log = new java.io.PrintWriter(
               new java.io.FileOutputStream(log_file), true);
        }
      catch (Exception e)
        {
   VirtuosoFuture.rpc_log = new java.io.PrintWriter(System.out, true);
        }
    }
       }
     catch (Exception e)
       {
         VirtuosoFuture.rpc_log = null;
       }
   }
   public Connection connect(String url, Properties info) throws VirtuosoException
   {
      try
      {
         if(acceptsURL(url))
         {
            Properties props = urlToInfo(url, info);
            return new VirtuosoConnection(url,host,Integer.parseInt(port),props);
         }
      }
      catch(NumberFormatException e)
      {
         throw new VirtuosoException("Wrong port number : " + e.getMessage(),VirtuosoException.BADFORMAT);
      }
      return null;
   }
   public boolean acceptsURL(String url) throws VirtuosoException
   {
     if (url.startsWith (VirtPrefix))
       return true;
     return false;
   }
   protected Properties urlToInfo(String url, Properties _info)
   {
    host = "localhost";
    port = "1111";
    fbs = new Integer(VirtuosoTypes.DEFAULTPREFETCH);
    sendbs = new Integer(32768);
    recvbs = new Integer(32768);
    Properties props = new Properties();
    for (Enumeration en = _info.propertyNames(); en.hasMoreElements(); ) {
      String key = (String)en.nextElement();
      String property = (String)_info.getProperty(key);
      props.setProperty(key.toLowerCase(), property);
    }
    char inQuote = '\0';
    String attr = null;
    StringBuffer buff = new StringBuffer();
    String part = url.substring(VirtPrefix.length());
    boolean isFirst = true;
    for (int i = 0; i < part.length(); i++) {
      char c = part.charAt(i);
      switch (c) {
        case '\'':
        case '"':
          if (inQuote == c)
            inQuote = '\0';
          else if (inQuote == '\0')
            inQuote = c;
          break;
        case '/':
   if (inQuote == '\0') {
            String val = buff.toString().trim();
            if (attr == null) {
              attr = val;
              val = "";
            }
     if (attr != null && attr.length() > 0) {
              if (isFirst) {
                isFirst = false;
  props.setProperty("_vhost", attr);
              } else {
         props.setProperty(attr.toLowerCase(), val);
              }
     }
     attr = null;
     buff.setLength(0);
   } else {
     buff.append(c);
   }
   break;
        case '=':
   if (inQuote == '\0') {
     attr = buff.toString().trim();
     buff.setLength(0);
   } else {
     buff.append(c);
   }
   break;
        default:
   buff.append(c);
   break;
      }
    }
    String val = buff.toString().trim();
    if (attr == null) {
      attr = val;
      val = "";
    }
    if (attr != null && attr.length() > 0) {
      if (isFirst)
        props.put("_vhost", attr);
      else
        props.put(attr.toLowerCase(), val);
    }
    char fsep = System.getProperty("file.separator").charAt(0);
    val = props.getProperty("kpath");
    if (val != null) {
      if (fsep != '\\') {
        val = val.replace('\\', fsep);
        props.put("kpath", val);
      }
    }
    val = props.getProperty("ts");
    if (val != null) {
      if (fsep != '\\') {
        val = val.replace('\\', fsep);
        props.put("ts", val);
      }
    }
    val = props.getProperty("ssl");
    if (val != null) {
        props.setProperty("ssl", "1");
    }
    val = props.getProperty("pwdtype");
    if (val != null)
      props.setProperty("pwdclear", val);
    val = props.getProperty("uid");
    if (val != null)
      props.setProperty("user", val);
    val = props.getProperty("pwd");
    if (val != null)
      props.setProperty("password", val);
    val = props.getProperty("ts");
    if (val != null)
      props.setProperty("truststorepath", val);
    val = props.getProperty("tspass");
    if (val != null)
      props.setProperty("truststorepass", val);
    val = props.getProperty("kpath");
    if (val != null)
      props.setProperty("keystorepath", val);
    val = props.getProperty("pass");
    if (val != null)
      props.setProperty("keystorepass", val);
    val = props.getProperty("kpass");
    if (val != null)
      props.setProperty("keystorepass", val);
    return props;
   }
   public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws VirtuosoException
   {
      Vector pinfo = new Vector();
      DriverPropertyInfo pr;
      if(acceptsURL(url))
      {
         if(info.get("user") == null)
         {
            pr = new DriverPropertyInfo("user",null);
            pr.required = true;
            pinfo.add(pr);
         }
         if(info.get("password") == null)
         {
            pr = new DriverPropertyInfo("password",null);
            pr.required = true;
            pinfo.add(pr);
         }
         if(info.get("database") == null)
         {
            pr = new DriverPropertyInfo("database",null);
            pr.required = false;
            pinfo.add(pr);
         }
         if(info.get("cert") == null)
         {
            pr = new DriverPropertyInfo("cert",null);
            pr.required = false;
            pinfo.add(pr);
         }
         if(info.get("keystorepass") == null)
         {
            pr = new DriverPropertyInfo("keystorepass",null);
            pr.required = false;
            pinfo.add(pr);
         }
         if(info.get("keystorepath") == null)
         {
            pr = new DriverPropertyInfo("keystorepath",null);
            pr.required = false;
            pinfo.add(pr);
         }
         if(info.get("provider") == null)
         {
            pr = new DriverPropertyInfo("provider",null);
            pr.required = false;
            pinfo.add(pr);
         }
         if(info.get("truststorepass") == null)
         {
            pr = new DriverPropertyInfo("truststorepass",null);
            pr.required = false;
            pinfo.add(pr);
         }
         if(info.get("truststorepath") == null)
         {
            pr = new DriverPropertyInfo("truststorepath",null);
            pr.required = false;
            pinfo.add(pr);
         }
         DriverPropertyInfo drv_info[] = new DriverPropertyInfo[pinfo.size()];
         pinfo.copyInto(drv_info);
         return drv_info;
      }
      pr = new DriverPropertyInfo("url",url);
      pr.required = true;
      pinfo.add(pr);
      if(info.get("user") == null)
      {
         pr = new DriverPropertyInfo("user",null);
         pr.required = true;
         pinfo.add(pr);
      }
      if(info.get("password") == null)
      {
         pr = new DriverPropertyInfo("password",null);
         pr.required = true;
         pinfo.add(pr);
      }
      if(info.get("database") == null)
      {
         pr = new DriverPropertyInfo("database",null);
         pr.required = false;
         pinfo.add(pr);
      }
      if(info.get("fbs") == null)
      {
         pr = new DriverPropertyInfo("fbs",null);
         pr.required = false;
         pinfo.add(pr);
      }
      if(info.get("sendbs") == null)
      {
         pr = new DriverPropertyInfo("sendbs",null);
         pr.required = false;
         pinfo.add(pr);
      }
      if(info.get("recvbs") == null)
      {
         pr = new DriverPropertyInfo("recvbs",null);
         pr.required = false;
         pinfo.add(pr);
      }
      if(info.get("roundrobin") == null)
      {
         pr = new DriverPropertyInfo("roundrobin",null);
         pr.required = false;
         pinfo.add(pr);
      }
      if(info.get("cert") == null)
      {
         pr = new DriverPropertyInfo("cert",null);
         pr.required = false;
         pinfo.add(pr);
      }
      if(info.get("keystorepass") == null)
      {
         pr = new DriverPropertyInfo("keystorepass",null);
         pr.required = false;
         pinfo.add(pr);
      }
      if(info.get("keystorepath") == null)
      {
         pr = new DriverPropertyInfo("keystorepath",null);
         pr.required = false;
         pinfo.add(pr);
      }
      if(info.get("provider") == null)
      {
         pr = new DriverPropertyInfo("provider",null);
         pr.required = false;
         pinfo.add(pr);
      }
      if(info.get("truststorepass") == null)
      {
         pr = new DriverPropertyInfo("truststorepass",null);
         pr.required = false;
         pinfo.add(pr);
      }
      if(info.get("truststorepath") == null)
      {
         pr = new DriverPropertyInfo("truststorepath",null);
         pr.required = false;
         pinfo.add(pr);
      }
      if(info.get("usepstmtpool") == null)
      {
         pr = new DriverPropertyInfo("usepstmtpool",null);
         pr.required = false;
         pinfo.add(pr);
      }
      if(info.get("pstmtpoolsize") == null)
      {
         pr = new DriverPropertyInfo("pstmtpoolsize",null);
         pr.required = false;
         pinfo.add(pr);
      }
      DriverPropertyInfo drv_info[] = new DriverPropertyInfo[pinfo.size()];
      pinfo.copyInto(drv_info);
      return drv_info;
   }
   public int getMajorVersion()
   {
      return major;
   }
   public int getMinorVersion()
   {
      return minor;
   }
   public boolean jdbcCompliant()
   {
      return true;
   }
   public static void main(String args[])
   {
      System.out.println("OpenLink Virtuoso(TM) Driver with SSL support for JDBC(TM) Version " + 4.3 + " [Build " + major + "." + minor + "]");
   }
   public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException
   {
     throw new VirtuosoFNSException ("getParentLogger()  not supported", VirtuosoException.NOTIMPLEMENTED);
   }
}
