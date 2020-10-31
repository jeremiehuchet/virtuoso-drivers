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
  private LinkedList<Object> listeners = null;
  private LinkedList<Object> pStmtsPool = null;
  private ConnectionWrapper connWrapper = null;
  private VirtuosoConnection conn;
  private boolean sendEvent = true;
  private int maxStatements = 0;
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
    listeners = new LinkedList<Object>();
    addConnectionEventListener(listener);
    maxStatements = listener.getMaxStatements();
    conn.pooled_connection = this;
  }
  public synchronized void finalize () throws Throwable {
    try {
      close();
    } catch(Exception e) { }
    listeners.clear();
  }
  protected synchronized Object clone() {
    try {
      VirtuosoPooledConnection v = (VirtuosoPooledConnection)super.clone();
      v.listeners = null;
      v.connWrapper = null;
      v.conn = conn;
      v.pStmtsPool = null;
      v.sendEvent = true;
      v.maxStatements = 0;
      v.connURL = connURL;
      v.hashConnURL = hashConnURL;
      v.tmClosed = tmClosed;
      return v;
    } catch (CloneNotSupportedException e) {
      throw new InternalError();
    }
  }
  protected VirtuosoPooledConnection reuse() {
    LinkedList<Object> StmtsPool = connWrapper.reset();
    VirtuosoPooledConnection pconn = (VirtuosoPooledConnection)this.clone();
    listeners.clear();
    this.connWrapper = null;
    this.conn.pooled_connection = null;
    this.conn.xa_connection = null;
    this.conn = null;
    this.pStmtsPool = null;
    this.connURL = null;
    pconn.tmClosed = System.currentTimeMillis();
    pconn.pStmtsPool = StmtsPool;
    pconn.conn.pooled_connection = pconn;
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
  public void close() throws java.sql.SQLException {
    SQLException ex = null;
    if (connWrapper != null) {
      try {
        connWrapper.closeAll();
      } catch(SQLException e) {
        ex = e;
      }
      connWrapper = null;
    }
    if (conn != null)
      {
        conn.pooled_connection = null;
        conn.xa_connection = null;
      }
    conn = null;
    if (pStmtsPool != null)
      pStmtsPool.clear();
    pStmtsPool = null;
    sendErrorEvent(new VirtuosoException("Physical Connection is closed", VirtuosoException.OK));
    if (ex != null)
      throw ex;
  }
  public void closeAll() throws java.sql.SQLException {
    if (connWrapper != null) {
      connWrapper.clearStmtsCache();
    }
  }
  public Connection getConnection() throws java.sql.SQLException {
    if (conn == null) {
       SQLException ex = (SQLException)(new VirtuosoException("Physical Connection is closed", VirtuosoException.OK));
       sendErrorEvent(ex);
       throw ex;
    }
    if (connWrapper != null) {
      sendEvent = false;
      pStmtsPool = connWrapper.reset();
      connWrapper.close();
      sendEvent = true;
    }
    connWrapper = new ConnectionWrapper(conn, this, pStmtsPool, maxStatements);
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
