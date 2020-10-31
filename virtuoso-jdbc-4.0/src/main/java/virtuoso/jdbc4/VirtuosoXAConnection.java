package virtuoso.jdbc4;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.XAConnection;
import javax.sql.ConnectionEventListener;
import javax.sql.StatementEventListener;
import javax.transaction.xa.XAResource;
public class VirtuosoXAConnection implements XAConnection {
    private VirtuosoPooledConnection pconn;
    private VirtuosoXAResource resource;
    protected VirtuosoXAConnection(VirtuosoPooledConnection connection, String server, int port) throws SQLException
    {
        pconn = connection;
        pconn.getVirtuosoConnection().xa_connection = this;
        resource = new VirtuosoXAResource(pconn, server, port);
 if (VirtuosoFuture.rpc_log != null)
 {
     synchronized (VirtuosoFuture.rpc_log)
     {
  VirtuosoFuture.rpc_log.println ("new VirtuosoXAConnection (connection=" + connection.hashCode() + ") :" + hashCode() + ")");
  VirtuosoFuture.rpc_log.flush();
     }
 }
    }
    public XAResource getXAResource() throws SQLException
    {
      return (XAResource) getVirtuosoXAResource();
    }
    VirtuosoXAResource getVirtuosoXAResource() throws VirtuosoException
    {
     if (pconn.isClosed())
        throw new VirtuosoException("Connection is closed.",VirtuosoException.DISCONNECTED);
     if (VirtuosoFuture.rpc_log != null)
       {
  synchronized (VirtuosoFuture.rpc_log)
    {
      VirtuosoFuture.rpc_log.println ("VirtuosoXAConnection.getVirtuosoXAResource () ret " + resource.hashCode() + " :" + hashCode());
      VirtuosoFuture.rpc_log.flush();
    }
       }
      return resource;
    }
  public Connection getConnection() throws SQLException {
    if (pconn.isClosed())
      throw new VirtuosoException("Connection is closed.",VirtuosoException.DISCONNECTED);
    ConnectionWrapper conn = (ConnectionWrapper)pconn.getConnection();
    conn.setXAResource(resource);
    return (Connection)conn;
  }
  public void close() throws SQLException {
    pconn.close();
  }
  public void addConnectionEventListener(ConnectionEventListener listener) {
    pconn.addConnectionEventListener(listener);
  }
  public void removeConnectionEventListener(ConnectionEventListener listener) {
    pconn.removeConnectionEventListener(listener);
  }
  public void addStatementEventListener(StatementEventListener listener)
  {
  }
  public void removeStatementEventListener(StatementEventListener listener)
  {
  }
}
