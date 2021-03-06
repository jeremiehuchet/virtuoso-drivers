package virtuoso.jdbc4;
import java.sql.SQLException;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.naming.*;
public class VirtuosoXADataSource
    extends VirtuosoConnectionPoolDataSource
    implements XADataSource {
    public VirtuosoXADataSource()
    {
      dataSourceName = "VirtuosoXADataSource";
      if (VirtuosoFuture.rpc_log != null)
       {
      VirtuosoFuture.rpc_log.println ("new VirtuosoXADataSource () :" + hashCode());
       }
    }
    public Reference getReference() throws NamingException
    {
      Reference ref = new Reference(getClass().getName(), "virtuoso.jdbc4.VirtuosoDataSourceFactory", null);
      addProperties(ref);
      return ref;
    }
    public XAConnection getXAConnection() throws SQLException
    {
      if (VirtuosoFuture.rpc_log != null)
       {
      VirtuosoFuture.rpc_log.println ("VirtuosoXADataSource.getXAConnection () :" + hashCode());
       }
      return getXAConnection(null, null);
    }
    public XAConnection getXAConnection(String user, String password)
        throws SQLException
    {
      if (VirtuosoFuture.rpc_log != null)
       {
      VirtuosoFuture.rpc_log.println ("VirtuosoXADataSource.getXAConnection (user=" + user + ", pass=" + password + ") :" + hashCode());
       }
      return new VirtuosoXAConnection((VirtuosoPooledConnection)getPooledConnection(user, password), getServerName(), getPortNumber());
    }
}
