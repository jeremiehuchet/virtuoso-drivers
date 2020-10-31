package virtuoso.jdbc4;
import java.sql.*;
public class VirtuosoFNSException extends SQLFeatureNotSupportedException
{
   public VirtuosoFNSException(String data, int vendor)
   {
      super(data,"42000",vendor);
   }
   public VirtuosoFNSException(String data, String sqlstate, int vendor)
   {
      super(data,sqlstate,vendor);
   }
   public VirtuosoFNSException(Exception e, int vendor)
   {
      super("General error","42000",vendor);
      initCause (e);
   }
}
