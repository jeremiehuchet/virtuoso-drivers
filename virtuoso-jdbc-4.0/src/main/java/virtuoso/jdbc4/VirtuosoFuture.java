package virtuoso.jdbc4;
import java.io.*;
import openlink.util.*;
class VirtuosoFuture
{
   protected static final String callerid = "caller_identification";
   protected static final String scon = "SCON";
   protected static final String exec = "EXEC";
   protected static final String close = "FRST";
   protected static final String fetch = "FTCH";
   protected static final String prepare = "PREP";
   protected static final String transaction = "TRXC";
   protected static final String getdata = "GETDA";
   protected static final String extendedfetch = "EXTF";
   protected static final String cancel = "CANCEL";
   protected static final String tp_transaction = "TPTRX";
   private int req_no;
   private VirtuosoConnection connection;
   private openlink.util.Vector results = new openlink.util.Vector(5);
   private boolean is_complete = false;
   protected static PrintWriter rpc_log = null;
   VirtuosoFuture(VirtuosoConnection connection, String rpcname, Object[] args, int req_no, int timeout)
       throws IOException, VirtuosoException
   {
      this.connection = connection;
      this.req_no = req_no;
      connection.setSocketTimeout(timeout);
      send_message(rpcname,args);
   }
   protected void send_message(String rpcname, Object[] args) throws IOException, VirtuosoException
   {
      Object[] vector = new Object[5];
      if(args != null)
      {
         openlink.util.Vector v = new openlink.util.Vector(args);
         vector[4] = v;
      }
      else
         vector[4] = null;
      vector[3] = rpcname;
      vector[2] = null;
      vector[1] = new Integer(req_no);
      vector[0] = new Integer(VirtuosoTypes.DA_FUTURE_REQUEST);
      connection.write_object(new openlink.util.Vector(vector));
   }
   protected void putResult(Object res)
   {
      results.addElement(res);
   }
   protected openlink.util.Vector nextResult() throws VirtuosoException
   {
      try
      {
        while(results.isEmpty())
          connection.read_request();
         openlink.util.Vector vect = (openlink.util.Vector)results.firstElement();
         results.removeElementAt(0);
         return vect;
      }
      catch(IOException e)
      {
 sendCancelFuture();
         throw new VirtuosoException("Virtuoso Communications Link Failure (timeout) : " + e.getMessage(),
      VirtuosoException.IOERROR);
      }
   }
   protected void sendCancelFuture () throws VirtuosoException
     {
       int ver = connection.getVersionNum();
       try
  {
    Object[] args = new Object[0];
    connection.removeFuture (connection.getFuture (VirtuosoFuture.cancel, args, 0));
  }
       catch (IOException e)
  {
  }
       catch (VirtuosoException e2)
  {
    throw e2;
  }
     }
   protected void complete(boolean isComplete)
   {
      is_complete = isComplete;
   }
   public int hashCode()
   {
      return req_no;
   }
   public boolean equals(Object obj)
   {
      if(obj != null && (obj instanceof VirtuosoFuture))
         return ((VirtuosoFuture)obj).req_no == req_no;
      return false;
   }
}
