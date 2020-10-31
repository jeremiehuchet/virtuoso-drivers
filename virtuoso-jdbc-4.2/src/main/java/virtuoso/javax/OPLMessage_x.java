package virtuoso.javax;
import java.text.MessageFormat;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
class OPLMessage_x extends openlink.util.BaseMessage {
  protected static final int errx_Physical_Connection_is_closed = 1;
  protected static final int errx_Connection_is_closed = 2;
  protected static final int errx_Unexpected_state_of_cache = 3;
  protected static final int errx_Connection_failed_loginTimeout_has_expired = 4;
  protected static final int errx_ConnectionPoolDataSource_is_closed = 5;
  protected static final int errx_Statement_is_closed = 6;
  protected static final int errx_ResultSet_is_closed = 7;
  protected static final int errx_Invalid_column_count = 8;
  protected static final int errx_Column_Index_out_of_range = 9;
  protected static final int errx_Unknown_type_of_parameter = 10;
  protected static final int errx_SQL_query_is_undefined = 11;
  protected static final int errx_Invalid_parameter_index_XX = 12;
  protected static final int errx_Invalid_column_name = 13;
  protected static final int errx_XX_was_called_when_the_insert_row_is_off = 14;
  protected static final int errx_Could_not_convert_parameter_to_XX = 15;
  protected static final int errx_Names_of_columns_are_not_found = 16;
  protected static final int errx_Could_not_set_XX_value_to_field = 17;
  protected static final int errx_Could_not_call_XX_when_the_cursor_on_the_insert_row = 18;
  protected static final int errx_Could_not_call_XX_on_a_TYPE_FORWARD_ONLY_result_set = 19;
  protected static final int errx_Could_not_call_XX_on_a_CONCUR_READ_ONLY_result_set = 20;
  protected static final int errx_No_row_is_currently_available = 21;
  protected static final int errx_Invalid_hex_number = 22;
  protected static final int errx_The_name_of_table_is_not_defined = 23;
  protected static final int errx_RowSetWriter_is_not_defined = 24;
  protected static final int errx_acceptChanges_Failed = 25;
  protected static final int errx_Invalid_key_columns = 26;
  protected static final int errx_Illegal_operation_on_non_inserted_row = 27;
  protected static final int errx_Invalid_row_number_for_XX = 28;
  protected static final int errx_Failed_to_insert_Row = 29;
  protected static final int errx_Invalid_cursor_position = 30;
  protected static final int errx_Unable_to_get_a_Connection = 31;
  protected static final int errx_RowSetMetaData_is_not_defined = 32;
  protected static final int errx_XX_can_not_determine_the_table_name = 33;
  protected static final int errx_XX_can_not_determine_the_keyCols = 34;
  protected static final int errx_Method_XX_not_yet_implemented = 35;
  protected static final int errx_Unable_to_unwrap_to_XX = 36;
  private static OPLMessage_x msg = new OPLMessage_x();
  private OPLMessage_x() {
    msgPrefix = "jdbcx.err.";
    init("virtuoso.javax.messages_x");
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
  protected static SQLFeatureNotSupportedException makeFExceptionV (int err_id, String p0)
  {
    Object params[] = { p0 };
    return new SQLFeatureNotSupportedException (err_Prefix + getMessage(err_id, params), S_GENERAL_ERR);
  }
  protected static SQLException makeExceptionV (int err_id, String p0)
  {
    Object params[] = { p0 };
    return new SQLException (err_Prefix + getMessage(err_id, params), S_GENERAL_ERR);
  }
  protected static SQLException makeExceptionV (int err_id, String p0, String p1)
  {
    Object params[] = { p0, p1 };
    return new SQLException (err_Prefix + getMessage(err_id, params), S_GENERAL_ERR);
  }
}
