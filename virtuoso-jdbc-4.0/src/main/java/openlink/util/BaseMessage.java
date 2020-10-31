package openlink.util;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.sql.SQLException;
abstract public class BaseMessage {
  public static final String RESBUNDLE_NOTFOUND = "HY000:Could not found resource file '";
  public static final String NO_MESSAGE = "HY000:Can not retrieve message for code : ";
  public static final String S_GENERAL_ERR = "HY000";
  protected ResourceBundle rb;
  protected String defaultMessage;
  protected String msgPrefix;
    public static final String err_Prefix = "[OpenLink][OPLJDBC4]";
  protected void init(String resourceFile) {
    defaultMessage = RESBUNDLE_NOTFOUND + (resourceFile != null ? resourceFile : "null") + "'";
    if (resourceFile == null)
      return;
    try {
        rb = ResourceBundle.getBundle(resourceFile);
    } catch(MissingResourceException e) { }
      catch(ClassFormatError e) { }
  }
  protected String getBundle(String s) {
    if (rb != null)
      try {
        return rb.getString(s);
      } catch (MissingResourceException e) {
        return NO_MESSAGE + s;
      } catch (ClassFormatError e) {
        return NO_MESSAGE + s;
      }
    return defaultMessage;
  }
  public static SQLException makeException (Exception e)
  {
    return new SQLException(err_Prefix + "Error :" + e.toString(), S_GENERAL_ERR);
  }
}
