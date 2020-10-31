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
   protected static final int minor = 45;
   private String host = "localhost";
   private String port = "1111";
   private String user, password, database, charset, pwdclear;
   private Integer timeout, log_enable;
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
   VirtuosoFuture.rpc_log = new java.io.PrintStream (
       new java.io.BufferedOutputStream (
         new java.io.FileOutputStream (log_file), 4096));
        }
      catch (Exception e)
        {
   VirtuosoFuture.rpc_log = System.out;
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
    val = props.getProperty("kpath");
    if (val != null) {
      char fsep = System.getProperty("file.separator").charAt(0);
      if (fsep != '\\') {
        val = val.replace('\\', fsep);
        props.put("kpath", val);
      }
    }
    val = props.getProperty("ssl");
    if (val != null) {
      if (props.getProperty("cert")==null)
        props.setProperty("cert", "");
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
    val = props.getProperty("cert");
    if (val != null)
      props.setProperty("certificate", val);
    val = props.getProperty("pass");
    if (val != null)
      props.setProperty("keystorepass", val);
    val = props.getProperty("kpath");
    if (val != null)
      props.setProperty("keystorepath", val);
    return props;
   }
   public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws VirtuosoException
   {
      if(acceptsURL(url))
      {
         DriverPropertyInfo[] pinfo = new DriverPropertyInfo[7];
         if(info.get("user") == null)
         {
            pinfo[0] = new DriverPropertyInfo("user",null);
            pinfo[0].required = true;
         }
         if(info.get("password") == null)
         {
            pinfo[1] = new DriverPropertyInfo("password",null);
            pinfo[1].required = true;
         }
         if(info.get("database") == null)
         {
            pinfo[2] = new DriverPropertyInfo("database",null);
            pinfo[2].required = false;
         }
         return pinfo;
      }
      DriverPropertyInfo[] pinfo = new DriverPropertyInfo[8];
      pinfo[0] = new DriverPropertyInfo("url",url);
      pinfo[0].required = true;
      if(info.get("user") == null)
      {
         pinfo[1] = new DriverPropertyInfo("user",null);
         pinfo[1].required = true;
      }
      if(info.get("password") == null)
      {
         pinfo[2] = new DriverPropertyInfo("password",null);
         pinfo[2].required = true;
      }
      if(info.get("database") == null)
      {
         pinfo[3] = new DriverPropertyInfo("database",null);
         pinfo[3].required = false;
      }
      if(info.get("fbs") == null)
      {
         pinfo[3] = new DriverPropertyInfo("fbs",null);
         pinfo[3].required = false;
      }
      if(info.get("sendbs") == null)
      {
         pinfo[3] = new DriverPropertyInfo("sendbs",null);
         pinfo[3].required = false;
      }
      if(info.get("recvbs") == null)
      {
         pinfo[3] = new DriverPropertyInfo("recvbs",null);
         pinfo[3].required = false;
      }
      if(info.get("roundrobin") == null)
      {
         pinfo[3] = new DriverPropertyInfo("roundrobin",null);
         pinfo[3].required = false;
      }
      if(info.get("usepstmtpool") == null)
      {
         pinfo[3] = new DriverPropertyInfo("usepstmtpool",null);
         pinfo[3].required = false;
      }
      if(info.get("pstmtpoolsize") == null)
      {
         pinfo[3] = new DriverPropertyInfo("pstmtpoolsize",null);
         pinfo[3].required = false;
      }
      return pinfo;
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
      System.out.println("OpenLink Virtuoso(TM) Driver for JDBC(TM) Version " + "4.x" + " [Build " + major + "." + minor + "]");
   }
}
