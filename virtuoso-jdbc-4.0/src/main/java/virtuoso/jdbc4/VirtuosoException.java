package virtuoso.jdbc4;
import java.sql.*;
public class VirtuosoException extends SQLException
{
   public static final int OK = 0;
   public static final int DISCONNECTED = -1;
   public static final int ILLJDBCURL = -2;
   public static final int IOERROR = -3;
   public static final int BADPARAM = -4;
   public static final int BADLOGIN = -5;
   public static final int TIMEOUT = -6;
   public static final int NOTIMPLEMENTED = -7;
   public static final int SQLERROR = -8;
   public static final int BADTAG = -9;
   public static final int CASTERROR = -10;
   public static final int BADFORMAT = -11;
   public static final int ERRORONTYPE = -12;
   public static final int CLOSED = -13;
   public static final int EOF = -14;
   public static final int NOLICENCE = -15;
   public static final int UNKNOWN = -16;
   public static final int MISCERROR = -17;
   public VirtuosoException(String data, int vendor)
   {
      super(data,"42000",vendor);
   }
   public VirtuosoException(String data, String sqlstate, int vendor)
   {
      super(data,sqlstate,vendor);
   }
   public VirtuosoException(Exception e, int vendor)
   {
      super("General error","42000",vendor);
      initCause (e);
   }
}
