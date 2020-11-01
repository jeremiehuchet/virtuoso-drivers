package openlink.util;
import java.text.MessageFormat;
import java.sql.SQLException;
class OPLMessage_u extends openlink.util.BaseMessage {
  protected static final int erru_Stream_is_closed = 1;
  protected static final int erru_Invalid_start_position = 2;
  protected static final int erru_Invalid_length = 3;
  protected static final int erru_Blob_is_freed = 4;
  private static OPLMessage_u msg = new OPLMessage_u();
  private OPLMessage_u() {
    msgPrefix = "jdbcu.err.";
    init("openlink.util.messages_u");
  }
  protected static String getMessage(int err_id) {
    return msg.getBundle(msg.msgPrefix + err_id);
  }
  protected static String getMessage(int err_id, Object[] params) {
     return MessageFormat.format(getMessage(err_id), params);
  }
  protected static SQLException makeException (int err_id)
  {
    return new SQLException (err_Prefix + getMessage(err_id), S_GENERAL_ERR);
  }
}
