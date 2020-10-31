package virtuoso.jdbc4;
import javax.sql.DataSource;
import java.sql.*;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Properties;
import java.util.Enumeration;
import javax.naming.*;
public class VirtuosoDataSource implements DataSource, Referenceable, Serializable {
    protected String dataSourceName = "VirtuosoDataSourceName";
    protected String description;
    protected String serverName = "localhost";
    protected String portNumber = "1111";
    protected String databaseName;
    protected String user = "dba";
    protected String password = "dba";
    protected String charSet;
    protected int loginTimeout = 0;
    protected String pwdclear;
    protected int fbs = 0;
    protected int sendbs = 0;
    protected int recvbs = 0;
    protected boolean roundrobin = false;
    protected boolean usepstmtpool = false;
    protected int pstmtpoolsize = 0;
    protected transient java.io.PrintWriter logWriter;
    final static String n_dataSourceName = "dataSourceName";
    final static String n_description = "description";
    final static String n_serverName = "serverName";
    final static String n_portNumber = "portNumber";
    final static String n_databaseName = "databaseName";
    final static String n_user = "user";
    final static String n_password = "password";
    final static String n_charSet = "charSet";
    final static String n_loginTimeout = "loginTimeout";
    final static String n_pwdclear = "pwdclear";
    final static String n_fbs = "fbs";
    final static String n_sendbs = "sendbs";
    final static String n_recvbs = "recvbs";
    final static String n_roundrobin = "roundrobin";
    final static String n_usepstmtpool = "usepstmtpool";
    final static String n_pstmtpoolsize = "pstmtpoolsize";
  public VirtuosoDataSource ()
  {
  }
  protected void addProperties(Reference ref) {
    if (dataSourceName != null)
      ref.add(new StringRefAddr(VirtuosoDataSource.n_dataSourceName, dataSourceName));
    if (description != null)
      ref.add(new StringRefAddr(VirtuosoDataSource.n_description, description));
    if (serverName != null)
      ref.add(new StringRefAddr(VirtuosoDataSource.n_serverName, serverName));
    if (portNumber != null)
      ref.add(new StringRefAddr(VirtuosoDataSource.n_portNumber, portNumber));
    if (databaseName != null)
      ref.add(new StringRefAddr(VirtuosoDataSource.n_databaseName, databaseName));
    if (user != null)
      ref.add(new StringRefAddr(VirtuosoDataSource.n_user, user));
    if (password != null)
      ref.add(new StringRefAddr(VirtuosoDataSource.n_password, password));
    if (loginTimeout != 0)
      ref.add(new StringRefAddr(VirtuosoDataSource.n_loginTimeout, String.valueOf(loginTimeout)));
    if (charSet != null)
      ref.add(new StringRefAddr(VirtuosoDataSource.n_charSet, charSet));
    if (pwdclear != null)
      ref.add(new StringRefAddr(VirtuosoDataSource.n_pwdclear, pwdclear));
    if (fbs != 0)
      ref.add(new StringRefAddr(VirtuosoDataSource.n_fbs, String.valueOf(fbs)));
    if (sendbs != 0)
      ref.add(new StringRefAddr(VirtuosoDataSource.n_sendbs, String.valueOf(sendbs)));
    if (recvbs != 0)
      ref.add(new StringRefAddr(VirtuosoDataSource.n_recvbs, String.valueOf(recvbs)));
    ref.add(new StringRefAddr(VirtuosoDataSource.n_roundrobin, String.valueOf(roundrobin)));
    ref.add(new StringRefAddr(VirtuosoDataSource.n_usepstmtpool, String.valueOf(usepstmtpool)));
    if (pstmtpoolsize != 0)
      ref.add(new StringRefAddr(VirtuosoDataSource.n_pstmtpoolsize, String.valueOf(pstmtpoolsize)));
  }
  public Reference getReference() throws NamingException {
     Reference ref = new Reference(getClass().getName(), "virtuoso.jdbc4.VirtuosoDataSourceFactory", null);
     addProperties(ref);
     return ref;
  }
  protected Properties createConnProperties() {
    Properties prop = new Properties();
    String vhost = serverName;
    if (serverName.indexOf(':') == -1 &&
        serverName.indexOf(',') == -1 && portNumber != "1111")
      vhost += ":" + portNumber;
    prop.setProperty("_vhost", vhost);
    if (databaseName != null) prop.setProperty("database", databaseName);
    if (user != null) prop.setProperty("user", user);
    if (password != null) prop.setProperty("password", password);
    if (loginTimeout != 0) prop.setProperty("timeout", String.valueOf(loginTimeout));
    if (charSet != null) prop.setProperty("charset", charSet);
    if (pwdclear != null) prop.setProperty("pwdclear", pwdclear);
    if (fbs != 0) prop.setProperty("fbs", String.valueOf(fbs));
    if (sendbs != 0) prop.setProperty("sendbs", String.valueOf(sendbs));
    if (recvbs != 0) prop.setProperty("recvbs", String.valueOf(recvbs));
    if (roundrobin) prop.setProperty("roundrobin", "1");
    if (usepstmtpool) prop.setProperty("usepstmtpool", "1");
    if (pstmtpoolsize != 0) prop.setProperty("pstmtpoolsize", String.valueOf(pstmtpoolsize));
    return prop;
  }
  protected String create_url_key(String base_conn_url, Properties info) {
    String key;
    StringBuffer connKeyBuf = new StringBuffer(128);
    connKeyBuf.append(base_conn_url);
    for (Enumeration en = info.propertyNames(); en.hasMoreElements(); ) {
      key = (String)en.nextElement();
      connKeyBuf.append(key);
      connKeyBuf.append('=');
      connKeyBuf.append(info.getProperty(key));
      connKeyBuf.append('/');
    }
    return connKeyBuf.toString();
  }
  protected String create_url() {
    String url = "jdbc:virtuoso://" + serverName;
     if (serverName.indexOf(':') == -1 &&
         serverName.indexOf(',') == -1 && portNumber != "1111")
       url += ":" + portNumber;
    return url;
  }
  public Connection getConnection() throws SQLException
  {
     return getConnection (null, null);
  }
  public Connection getConnection(String username, String password)
    throws SQLException
  {
    String url = create_url();
    Properties info = createConnProperties();
    if (user != null)
        info.setProperty("user", user);
    if (password != null)
        info.setProperty("password", password);
    return new VirtuosoConnection (url, "localhost", 1111, info);
  }
  public PrintWriter getLogWriter() throws SQLException
  {
    return logWriter;
  }
  public void setLogWriter(PrintWriter out) throws SQLException
  {
    logWriter = out;
  }
  public void setLoginTimeout(int seconds) throws SQLException
  {
    loginTimeout = seconds;
  }
  public int getLoginTimeout() throws SQLException
  {
    return loginTimeout;
  }
  public String getDataSourceName() {
    return dataSourceName;
  }
  public void setDataSourceName(String parm) {
    dataSourceName = parm;
  }
  public void setDescription (String description)
  {
    this.description = description;
  }
  public String getDescription ()
  {
    return this.description;
  }
  public void setServerName (String serverName)
  {
    this.serverName = serverName;
  }
  public String getServerName ()
  {
    return serverName;
  }
  public int getPortNumber() {
    return Integer.parseInt(portNumber);
  }
  public void setPortNumber(int parm) {
    portNumber = String.valueOf(parm);
  }
  public void setUser (String user)
  {
    this.user = user;
  }
  public String getUser ()
  {
    return this.user;
  }
  public void setPassword (String passwd)
  {
    this.password = passwd;
  }
  public String getPassword ()
  {
    return this.password;
  }
  public void setDatabaseName (String name)
  {
    this.databaseName = name;
  }
  public String getDatabaseName ()
  {
    return databaseName;
  }
  public void setCharset (String name)
  {
    this.charSet = name;
  }
  public String getCharset ()
  {
    return this.charSet;
  }
  public void setPwdClear (String value)
  {
    this.pwdclear = value;
  }
  public String getPwdClear ()
  {
    return this.pwdclear;
  }
  public void setFbs (int value)
  {
    this.fbs = value;
  }
  public int getFbs ()
  {
    return this.fbs;
  }
  public void setSendbs (int value)
  {
    this.sendbs = value;
  }
  public int getSendbs ()
  {
    return this.sendbs;
  }
  public void setRecvbs (int value)
  {
    this.recvbs = value;
  }
  public int getRecvbs ()
  {
    return this.recvbs;
  }
  public void setRoundrobin (boolean value)
  {
    this.roundrobin = value;
  }
  public boolean getRoundrobin ()
  {
    return this.roundrobin;
  }
  public void setUsepstmtpool (boolean value)
  {
    this.usepstmtpool = value;
  }
  public boolean getUsepstmtpool ()
  {
    return this.usepstmtpool;
  }
  public void setPstmtpoolsize (int value)
  {
    this.pstmtpoolsize = value;
  }
  public int getPstmtpoolsize ()
  {
    return this.pstmtpoolsize;
  }
  public <T> T unwrap(java.lang.Class<T> iface) throws java.sql.SQLException
  {
    try {
      return iface.cast(this);
    } catch (ClassCastException cce) {
      throw new VirtuosoException("Unable to unwrap to "+iface.toString(), VirtuosoException.OK);
    }
  }
  public boolean isWrapperFor(java.lang.Class<?> iface) throws java.sql.SQLException
  {
    return iface.isInstance(this);
  }
}
