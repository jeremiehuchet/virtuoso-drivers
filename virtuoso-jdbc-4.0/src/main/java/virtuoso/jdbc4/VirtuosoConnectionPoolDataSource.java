package virtuoso.jdbc4;
import java.lang.reflect.*;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Connection;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.List;
import java.util.TreeSet;
import javax.sql.*;
import javax.naming.*;
public class VirtuosoConnectionPoolDataSource
    extends VirtuosoDataSource
    implements ConnectionPoolDataSource, ConnectionEventListener {
    protected final static String n_minPoolSize = "minPoolSize";
    protected final static String n_maxPoolSize = "maxPoolSize";
    protected final static String n_initialPoolSize = "initialPoolSize";
    protected final static String n_maxIdleTime = "maxIdleTime";
    protected final static String n_propertyCycle = "propertyCycle";
    protected final static String n_maxStatements = "maxStatements";
    private int minPoolSize = 0;
    private int maxPoolSize = 0;
    private int initialPoolSize = 0;
    private int maxIdleTime = 0;
    private int propertyCycle = 0;
    private int maxStatements = 0;
    private ConnCache connPool;
    private boolean isInitialized = false;
    private boolean isClosed = false;
    private Object initLock ;
    private TreeSet<Object> propQueue;
    private long propEnforceTime = 0;
  public synchronized void finalize () throws Throwable {
    close ();
  }
  public VirtuosoConnectionPoolDataSource() {
    dataSourceName = "VirtuosoConnectionPoolDataSourceName";
    initLock = new Object();
    connPool = new ConnCache(this);
    propQueue = new TreeSet<Object>( new Comparator<Object>() {
          public int compare(Object a, Object b) {
            long a_time = ((NewProperty)a).enforceTime;
            long b_time = ((NewProperty)b).enforceTime;
            if (a_time == b_time)
              return 0;
            else if (a_time > b_time)
              return +1;
            else
              return -1;
          }
        });
  }
  protected void checkPool() {
    if (isClosed)
       return;
    connPool.checkPool();
  }
  protected void checkPropQueue() {
    if (isClosed || propEnforceTime == 0)
       return;
    long curTime = System.currentTimeMillis();
    NewProperty prop;
    synchronized(propQueue) {
      while(propEnforceTime != 0 && propEnforceTime < curTime) {
        try {
          prop = (NewProperty)(propQueue.first());
        } catch (NoSuchElementException e) {
          propEnforceTime = 0;
          break;
        }
        propQueue.remove(prop);
        try {
          prop.fld.setInt(this, prop.arg);
        } catch (Exception e) {
        }
        try {
          prop = (NewProperty)(propQueue.first());
          propEnforceTime = prop.enforceTime;
        } catch (NoSuchElementException e) {
          propEnforceTime = 0;
          break;
        }
      }
    }
  }
  public void close() throws SQLException {
    if (isClosed)
      return;
    isClosed = true;
    connPool.clear();
    initLock = null;
    propQueue.clear();
  }
  public void connectionClosed(ConnectionEvent event) {
    try {
      Object source = event.getSource();
      if (source instanceof VirtuosoPooledConnection)
        connPool.reusePooledConnection((VirtuosoPooledConnection)event.getSource());
    } catch(SQLException e) { }
  }
  public void connectionErrorOccurred(ConnectionEvent event) {
    try {
      Object source = event.getSource();
      if (source instanceof VirtuosoPooledConnection)
        connPool.closePooledConnection((VirtuosoPooledConnection)event.getSource());
    } catch(SQLException e) { }
  }
  protected void addProperties(Reference ref) {
    super.addProperties(ref);
    ref.add(new StringRefAddr(VirtuosoConnectionPoolDataSource.n_minPoolSize, String.valueOf(minPoolSize)));
    ref.add(new StringRefAddr(VirtuosoConnectionPoolDataSource.n_maxPoolSize, String.valueOf(maxPoolSize)));
    ref.add(new StringRefAddr(VirtuosoConnectionPoolDataSource.n_initialPoolSize, String.valueOf(initialPoolSize)));
    ref.add(new StringRefAddr(VirtuosoConnectionPoolDataSource.n_maxIdleTime, String.valueOf(maxIdleTime)));
    ref.add(new StringRefAddr(VirtuosoConnectionPoolDataSource.n_propertyCycle, String.valueOf(propertyCycle)));
    ref.add(new StringRefAddr(VirtuosoConnectionPoolDataSource.n_maxStatements, String.valueOf(maxStatements)));
  }
  public Reference getReference() throws NamingException {
     Reference ref = new Reference(getClass().getName(), "virtuoso.jdbc4.VirtuosoDataSourceFactory", null);
     addProperties(ref);
     return ref;
  }
  public void fill() throws java.sql.SQLException {
    check_close();
    Properties info = createConnProperties();
    String connKey = create_url_key(create_url(), info);
    synchronized(initLock) {
      if (!isInitialized) {
        isInitialized = true;
        if (initialPoolSize != 0) {
          OpenHelper initThread = new OpenHelper(initialPoolSize, info);
          initThread.start();
          try {
            initThread.join();
          }
          catch (InterruptedException e) {}
        }
        VirtuosoPoolManager.getInstance().addPool(this);
      }
    }
  }
  public Connection getConnection() throws java.sql.SQLException {
    return getPooledConnection().getConnection();
  }
  public Connection getConnection(String user, String password) throws java.sql.SQLException {
    return getPooledConnection(user, password).getConnection();
  }
  public PooledConnection getPooledConnection() throws java.sql.SQLException {
    return getPooledConnection(null, null);
  }
  public PooledConnection getPooledConnection(String _user, String _password)
      throws java.sql.SQLException
  {
    check_close();
    String conn_url = create_url();
    Properties info = createConnProperties();
    if (_user != null)
        info.setProperty("user", _user);
    if (_password != null)
        info.setProperty("password", _password);
    String connKey = create_url_key(conn_url, info);
    Connection conn;
    synchronized(initLock) {
      if (!isInitialized) {
        isInitialized = true;
        if (initialPoolSize != 0) {
          OpenHelper initThread = new OpenHelper(initialPoolSize, info);
          initThread.start();
          try {
             initThread.join();
          } catch(InterruptedException e) {}
        }
        VirtuosoPoolManager.getInstance().addPool(this);
      }
    }
    return connPool.getPooledConnection(info, connKey, conn_url);
  }
  public int getMinPoolSize() {
    return minPoolSize;
  }
  public void setMinPoolSize(int parm) throws SQLException
  {
    try {
      Field fld = getClass().getDeclaredField(this.n_minPoolSize);
      setField(fld, parm);
    } catch (Exception e) {
      throw new VirtuosoException("Error: "+e.toString(), VirtuosoException.OK);
    }
  }
  public int getMaxPoolSize() {
    return maxPoolSize;
  }
  public void setMaxPoolSize(int parm) throws SQLException
  {
    try {
      Field fld = getClass().getDeclaredField(this.n_maxPoolSize);
      setField(fld, parm);
    } catch (Exception e) {
      throw new VirtuosoException("Error: "+e.toString(), VirtuosoException.OK);
    }
  }
  public int getInitialPoolSize() {
    return initialPoolSize;
  }
  public void setInitialPoolSize(int parm) throws SQLException
  {
    try {
      Field fld = getClass().getDeclaredField(this.n_initialPoolSize);
      setField(fld, parm);
    } catch (Exception e) {
      throw new VirtuosoException("Error: "+e.toString(), VirtuosoException.OK);
    }
  }
  public int getMaxIdleTime() {
    return maxIdleTime;
  }
  public void setMaxIdleTime(int parm) throws SQLException
  {
    try {
      Field fld = getClass().getDeclaredField(this.n_maxIdleTime);
      setField(fld, parm);
    } catch (Exception e) {
      throw new VirtuosoException("Error: "+e.toString(), VirtuosoException.OK);
    }
  }
  public int getPropertyCycle() {
    return propertyCycle;
  }
  public void setPropertyCycle(int parm) {
    propertyCycle = parm;
  }
  public int getMaxStatements() {
    return maxStatements;
  }
  public void setMaxStatements(int parm) throws SQLException
  {
    try {
      Field fld = getClass().getDeclaredField(this.n_maxStatements);
      setField(fld, parm);
    } catch (Exception e) {
      throw new VirtuosoException("Error: "+e.toString(), VirtuosoException.OK);
    }
  }
  private void setField(Field fld, int parm) throws Exception {
    if (propertyCycle == 0)
      fld.setInt(this, parm);
    else
      synchronized(propQueue) {
        propQueue.add(new NewProperty(fld, parm));
        propEnforceTime = ((NewProperty)propQueue.first()).enforceTime;
      }
  }
  private void check_close() throws SQLException
  {
    if (isClosed)
      throw new VirtuosoException("ConnectionPoolDataSource is closed", VirtuosoException.OK);
  }
  private class NewProperty {
    protected long enforceTime;
    protected Field fld;
    protected int arg;
    protected NewProperty(Field _fld, int _arg) {
      fld = _fld;
      arg = _arg;
      enforceTime = System.currentTimeMillis() + propertyCycle * 1000L;
    }
  }
  private class OpenHelper extends Thread {
    private String conn_url;
    private Properties info;
    private String connKey;
    private int count;
    protected OpenHelper(int _count, Properties _info) {
      count = _count;
      info = _info;
      conn_url = create_url();
      connKey = create_url_key(conn_url, info);
      setName("Virtuoso OpenHelper");
    }
    public void run() {
      if (connPool.cacheSize >= count || (maxPoolSize != 0 && connPool.cacheSize > maxPoolSize))
        return;
      for(int i = 0; i < count; i++)
        try {
          VirtuosoConnection conn = new VirtuosoConnection (conn_url, "localhost", 1111, info);
          connPool.addPooledConnection(new VirtuosoPooledConnection(conn, connKey));
          if (connPool.cacheSize >= count || (maxPoolSize != 0 && connPool.cacheSize > maxPoolSize))
            return;
        } catch (Exception e) {
        }
    }
  }
  private class CloseHelper extends Thread {
    private ArrayList connList;
    private PooledConnection pconn;
    private CloseHelper() {
      setName("Virtuoso CloseHelper");
    }
    protected CloseHelper(ArrayList _connList) {
      this();
      connList = _connList;
    }
    protected CloseHelper(PooledConnection _pconn) {
      this();
      pconn = _pconn;
    }
    public void run() {
      if (connList != null) {
        for(Iterator i = connList.iterator(); i.hasNext(); )
          try {
            ((VirtuosoPooledConnection)i.next()).close();
          } catch (Exception e) { }
        connList.clear();
      } else {
        try {
          pconn.close();
        } catch (Exception e) {
        }
      }
    }
  }
  private class ConnCache {
    private LinkedList<Object> unUsed = new LinkedList<Object>();
    private HashMap<Object,Object> in_Use = new HashMap<Object,Object>(32);
    private int cacheSize = 0;
    private VirtuosoConnectionPoolDataSource cpds;
    private ConnCache(VirtuosoConnectionPoolDataSource _cpds) {
      cpds = _cpds;
    }
    private void addPooledConnection(VirtuosoPooledConnection pconn) {
      synchronized(this) {
        connPool.unUsed.addLast(pconn);
        connPool.cacheSize++;
        notifyAll();
      }
    }
    private void clear() throws SQLException {
      VirtuosoPooledConnection pconn;
      synchronized (this) {
        for(Iterator iterator = in_Use.keySet().iterator(); iterator.hasNext(); ) {
          pconn = (VirtuosoPooledConnection)iterator.next();
          pconn.removeConnectionEventListener(cpds);
          cacheSize--;
          try {
            pconn.close();
          } catch (Exception e) {}
        }
        in_Use.clear();
        for(Iterator iterator = unUsed.iterator(); iterator.hasNext(); ) {
          pconn = (VirtuosoPooledConnection)iterator.next();
          cacheSize--;
          try {
            pconn.close();
          } catch (Exception e) {}
        }
        unUsed.clear();
      }
    }
    private void reusePooledConnection(VirtuosoPooledConnection pconn) throws SQLException {
      if (pconn == null)
        return;
      boolean reUsed = true;
      pconn.removeConnectionEventListener(cpds);
      synchronized(this) {
        VirtuosoPooledConnection pooledConn = null;
        if ((pooledConn = (VirtuosoPooledConnection)in_Use.remove(pconn)) == null) {
          throw new VirtuosoException("Unexpected state of cache", VirtuosoException.OK);
        }
        if (maxPoolSize != 0 && cacheSize > maxPoolSize) {
          cacheSize--;
          reUsed = false;
        } else {
          pooledConn = pconn.reuse();
          unUsed.addFirst(pooledConn);
          notifyAll();
        }
      }
      if ( !reUsed) {
        CloseHelper helpThread = new CloseHelper(pconn);
        helpThread.start();
      }
    }
    private void closePooledConnection(VirtuosoPooledConnection pconn) throws SQLException {
      pconn.removeConnectionEventListener(cpds);
      synchronized(this) {
        VirtuosoPooledConnection pooledConn;
        if ((pooledConn = (VirtuosoPooledConnection)in_Use.remove(pconn)) == null)
          throw new VirtuosoException("Unexpected state of cache", VirtuosoException.OK);
        cacheSize--;
      }
      pconn.close();
    }
    private VirtuosoPooledConnection lookup(String _Key) {
      VirtuosoPooledConnection pooledConn;
      int _hashKey = _Key.hashCode();
      for(Iterator iterator = unUsed.iterator(); iterator.hasNext(); ) {
        pooledConn = (VirtuosoPooledConnection)iterator.next();
        if (pooledConn.hashConnURL == _hashKey && pooledConn.connURL.equals(_Key)) {
            iterator.remove();
            return pooledConn;
        }
      }
      return null;
    }
    private PooledConnection getPooledConnection(Properties info,
                                                 String connKey,
                                                 String conn_url)
        throws java.sql.SQLException
    {
      VirtuosoPooledConnection pconn = null;
      VirtuosoConnection conn;
      synchronized(this) {
        if (!unUsed.isEmpty() && (pconn = lookup(connKey)) != null) {
          pconn.init(cpds);
          in_Use.put(pconn, pconn);
          return pconn;
        }
      }
      if (maxPoolSize == 0 || cacheSize < maxPoolSize) {
        synchronized(this) {
          conn = new VirtuosoConnection (conn_url, "localhost", 1111, info);
          cacheSize++;
          pconn = new VirtuosoPooledConnection(conn, connKey, cpds);
          in_Use.put(pconn, pconn);
        }
        return pconn;
      } else {
        long start = System.currentTimeMillis();
        long _timeout = loginTimeout * 1000L;
        Thread thr = Thread.currentThread();
        while (pconn == null) {
          synchronized(this) {
            if (!unUsed.isEmpty() && (pconn = lookup(connKey)) != null) {
              pconn.init(cpds);
              in_Use.put(pconn, pconn);
              return pconn;
            }
            try {
              if (loginTimeout > 0) {
                wait(_timeout);
                _timeout -= (System.currentTimeMillis() - start);
                if (_timeout < 0) {
                  throw new VirtuosoException("Connection failed loginTimeout has expired", VirtuosoException.TIMEOUT);
                }
              } else {
                wait();
              }
            } catch (InterruptedException e) {
            }
          }
        }
      }
      return null;
    }
    private void checkPool() {
      VirtuosoPooledConnection pooledConn;
      ArrayList<Object> closeTmp = null;
      ListIterator l_iter;
      if (maxIdleTime != 0) {
        long minTime = System.currentTimeMillis() - maxIdleTime * 1000L;
        synchronized(this) {
          int count = unUsed.size();
          closeTmp = new ArrayList<Object>(count);
          for(l_iter = unUsed.listIterator(); l_iter.hasPrevious(); ) {
            pooledConn = (VirtuosoPooledConnection)l_iter.previous();
            if (pooledConn.tmClosed < minTime) {
               closeTmp.add(pooledConn);
               l_iter.remove();
               cacheSize--;
            }
          }
        }
      }
      if (maxPoolSize != 0 && cacheSize > maxPoolSize) {
        synchronized(this) {
          int count = cacheSize - maxPoolSize;
          closeTmp = new ArrayList<Object>(count);
          for(l_iter = unUsed.listIterator(); l_iter.hasPrevious() && count > 0; count--) {
            closeTmp.add(l_iter.previous());
            l_iter.remove();
            cacheSize--;
          }
        }
      }
      if (closeTmp != null && closeTmp.size() > 0) {
        CloseHelper helpThread = new CloseHelper(closeTmp);
        helpThread.start();
      }
      if (minPoolSize != 0 && cacheSize < minPoolSize) {
        Properties info = createConnProperties();
        int count = minPoolSize - cacheSize;
        OpenHelper helpThread = new OpenHelper(count, info);
        helpThread.start();
      }
    }
  }
}
