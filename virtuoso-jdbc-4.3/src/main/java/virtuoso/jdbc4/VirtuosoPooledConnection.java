package virtuoso.jdbc4;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.DatabaseMetaData;
import java.sql.SQLWarning;
import java.sql.SQLException;
import java.util.*;
import javax.sql.PooledConnection;
import javax.sql.ConnectionEventListener;
import javax.sql.ConnectionEvent;
import javax.sql.StatementEventListener;
public class VirtuosoPooledConnection implements PooledConnection, Cloneable {
  private LinkedList<ConnectionEventListener> listeners = null;
  private ConnectionWrapper connWrapper = null;
  private VirtuosoConnection conn;
  private boolean sendEvent = true;
  protected String connURL;
  protected int hashConnURL;
  protected long tmClosed;
  protected VirtuosoPooledConnection(VirtuosoConnection _conn, String _connURL)
  {
    conn = _conn;
    conn.pooled_connection = this;
    connURL = _connURL;
    hashConnURL = connURL.hashCode();
    tmClosed = System.currentTimeMillis();
  }
  protected VirtuosoPooledConnection(VirtuosoConnection _conn, String _connURL, VirtuosoConnectionPoolDataSource listener) {
    this(_conn, _connURL);
    init(listener);
  }
  protected void init(VirtuosoConnectionPoolDataSource listener) {
    listeners = new LinkedList<ConnectionEventListener>();
    addConnectionEventListener(listener);
    conn.pooled_connection = this;
  }
  public synchronized void finalize () throws Throwable {
    close();
  }
  protected synchronized Object clone() {
    try {
      VirtuosoPooledConnection v = (VirtuosoPooledConnection)super.clone();
      v.listeners = null;
      v.connWrapper = null;
      v.conn = conn;
      v.sendEvent = true;
      v.connURL = connURL;
      v.hashConnURL = hashConnURL;
      v.tmClosed = tmClosed;
      return v;
    } catch (CloneNotSupportedException e) {
      throw new InternalError();
    }
  }
  protected VirtuosoPooledConnection reuse() {
    connWrapper.reset();
    VirtuosoPooledConnection pconn = (VirtuosoPooledConnection)this.clone();
    listeners.clear();
    this.connWrapper = null;
    this.conn.pooled_connection = null;
    this.conn.xa_connection = null;
    this.conn = null;
    this.connURL = null;
    pconn.tmClosed = System.currentTimeMillis();
    pconn.conn.pooled_connection = pconn;
    pconn.conn.clearFutures();
    return pconn;
  }
  public void addConnectionEventListener(ConnectionEventListener parm) {
    synchronized(listeners) {
      listeners.add(parm);
    }
  }
  public void removeConnectionEventListener(ConnectionEventListener parm) {
    synchronized(listeners) {
      listeners.remove(parm);
    }
  }
  public synchronized void close() throws java.sql.SQLException {
    SQLException ex = null;
    if (connWrapper != null) {
      try {
        connWrapper.closeAll();
      } catch(SQLException e) {
        ex = e;
      }
      connWrapper = null;
    }
    if (conn != null) {
      if (!conn.isClosed()) {
        try {
          conn.close();
        } catch(SQLException e) {
          ex = e;
        }
      }
      conn.pooled_connection = null;
      conn.xa_connection = null;
    }
    conn = null;
    sendErrorEvent(new VirtuosoException("Physical Connection is closed", VirtuosoException.OK));
    if (ex != null)
      throw ex;
  }
  @Deprecated
  public void closeAll() throws java.sql.SQLException {
  }
  public Connection getConnection() throws java.sql.SQLException {
    if (conn == null) {
       SQLException ex = (SQLException)(new VirtuosoException("Physical Connection is closed", VirtuosoException.OK));
       sendErrorEvent(ex);
       throw ex;
    }
    if (connWrapper != null) {
      sendEvent = false;
      connWrapper.reset();
      connWrapper.close();
      sendEvent = true;
    }
    connWrapper = new ConnectionWrapper(conn, this);
    return connWrapper;
  }
  public VirtuosoConnection getVirtuosoConnection() throws java.sql.SQLException
  {
    if (conn == null) {
       SQLException ex = (SQLException)(new VirtuosoException("Connection is closed", VirtuosoException.OK));
       sendErrorEvent(ex);
       throw ex;
    }
    return conn;
  }
  public boolean isConnectionLost(int timeout_sec)
  {
    if (conn == null) {
       return true;
    }
    return conn.isClosed() || conn.isConnectionLost(timeout_sec);
  }
  public void addStatementEventListener(StatementEventListener listener)
  {
  }
  public void removeStatementEventListener(StatementEventListener listener)
  {
  }
  protected boolean isClosed() {
    return conn == null;
  }
  protected void sendCloseEvent() {
     if (!sendEvent)
       return;
     if (listeners == null)
        return;
     ConnectionEvent ev = new ConnectionEvent((PooledConnection)this);
     LinkedList tmpList;
     synchronized(listeners) {
       tmpList = (LinkedList)listeners.clone();
     }
     for (Iterator i = tmpList.iterator(); i.hasNext(); )
         ((ConnectionEventListener)(i.next())).connectionClosed(ev);
     tmpList.clear();
  }
  protected void sendErrorEvent(SQLException ex) {
     if (listeners == null)
        return;
     ConnectionEvent ev = new ConnectionEvent((PooledConnection)this, ex);
     LinkedList tmpList;
     synchronized(listeners) {
       tmpList = (LinkedList)listeners.clone();
     }
     for (Iterator i = tmpList.iterator(); i.hasNext(); )
         ((ConnectionEventListener)(i.next())).connectionErrorOccurred(ev);
     tmpList.clear();
  }
}
