package virtuoso.javax;
import javax.sql.RowSetMetaData;
import java.sql.*;
import java.io.Serializable;
public class OPLRowSetMetaData implements RowSetMetaData, Serializable {
  private static final long serialVersionUID = 4491018005954285242L;
  private Coldesc[] desc;
  public OPLRowSetMetaData(ResultSetMetaData rsmd) throws SQLException {
    int count = rsmd.getColumnCount();
    setColumnCount(count);
    for(int i = 0; i < count; i++){
        int col = i + 1;
        String v;
        desc[i].columnLabel = rsmd.getColumnLabel(col);
        desc[i].columnName = rsmd.getColumnName(col);
        v = rsmd.getSchemaName(col);
          desc[i].schemaName = ( v != null ? v : "");
        v = rsmd.getTableName(col);
          desc[i].tableName = ( v != null ? v : "");
        v = rsmd.getCatalogName(col);
        desc[i].catalogName = ( v != null ? v : "");
        desc[i].typeName = rsmd.getColumnTypeName(col);
        desc[i].type = rsmd.getColumnType(col);
        desc[i].precision = rsmd.getPrecision(col);
        desc[i].scale = rsmd.getScale(col);
        desc[i].displaySize = rsmd.getColumnDisplaySize(col);
        desc[i].isAutoIncrement = rsmd.isAutoIncrement(col);
        desc[i].isCaseSensitive = rsmd.isCaseSensitive(col);
        desc[i].isCurrency = rsmd.isCurrency(col);
        desc[i].nullable = rsmd.isNullable(col);
        desc[i].isSigned = rsmd.isSigned(col);
        desc[i].isSearchable = rsmd.isSearchable(col);
        desc[i].isDefinitelyWritable = rsmd.isDefinitelyWritable(col);
        desc[i].isReadOnly = rsmd.isReadOnly(col);
        desc[i].isWritable = rsmd.isWritable(col);
    }
  }
  public void setAutoIncrement(int column, boolean property) throws java.sql.SQLException {
    check_index(column).isAutoIncrement = property;
  }
  public void setCaseSensitive(int column, boolean property) throws java.sql.SQLException {
    check_index(column).isCaseSensitive = property;
  }
  public void setCatalogName(int column, String catalogName) throws java.sql.SQLException {
    if (catalogName != null)
      check_index(column).catalogName = catalogName;
    else
      check_index(column).catalogName = "";
  }
  public void setColumnCount(int columnCount) throws SQLException {
    if (columnCount <= 0)
       throw OPLMessage_x.makeException(OPLMessage_x.errx_Invalid_column_count);
    desc = new Coldesc[columnCount];
    for(int i = 0; i < columnCount; i++)
       desc[i] = new Coldesc();
  }
  public void setColumnDisplaySize(int column, int size) throws java.sql.SQLException {
    check_index(column).displaySize = size;
  }
  public void setColumnLabel(int column, String label) throws java.sql.SQLException {
    check_index(column).columnLabel = label;
  }
  public void setColumnName(int column, String columnName) throws java.sql.SQLException {
    check_index(column).columnName = columnName;
  }
  public void setColumnType(int column, int SQLType) throws java.sql.SQLException {
    check_index(column).type = SQLType;
  }
  public void setColumnTypeName(int column, String typeName) throws java.sql.SQLException {
    check_index(column).typeName = typeName;
  }
  public void setCurrency(int column, boolean property) throws java.sql.SQLException {
    check_index(column).isCurrency = property;
  }
  public void setNullable(int column, int property) throws java.sql.SQLException {
    check_index(column).nullable = property;
  }
  public void setPrecision(int column, int precision) throws java.sql.SQLException {
    check_index(column).precision = precision;
  }
  public void setScale(int column, int scale) throws java.sql.SQLException {
    check_index(column).scale = scale;
  }
  public void setSchemaName(int column, String schemaName) throws java.sql.SQLException {
    if (schemaName != null)
      check_index(column).schemaName = schemaName;
    else
      check_index(column).schemaName = "";
  }
  public void setSearchable(int column, boolean property) throws java.sql.SQLException {
    check_index(column).isSearchable = property;
  }
  public void setSigned(int column, boolean property) throws java.sql.SQLException {
    check_index(column).isSigned = property;
  }
  public void setTableName(int column, String tableName) throws java.sql.SQLException {
    if (tableName != null)
      check_index(column).tableName = tableName;
    else
      check_index(column).tableName = "";
  }
  public int getColumnCount() throws SQLException {
    return (desc != null ? desc.length : 0);
  }
  public boolean isAutoIncrement(int column) throws SQLException {
    return check_index(column).isAutoIncrement;
  }
  public boolean isCaseSensitive(int column) throws SQLException {
    return check_index(column).isCaseSensitive;
  }
  public boolean isSearchable(int column) throws SQLException {
    return check_index(column).isSearchable;
  }
  public boolean isCurrency(int column) throws SQLException {
    return check_index(column).isCurrency;
  }
  public int isNullable(int column) throws SQLException {
    return check_index(column).nullable;
  }
  public boolean isSigned(int column) throws SQLException {
    return check_index(column).isSigned;
  }
  public int getColumnDisplaySize(int column) throws SQLException {
    return check_index(column).displaySize;
  }
  public String getColumnLabel(int column) throws SQLException {
    return check_index(column).columnLabel;
  }
  public String getColumnName(int column) throws SQLException {
    return check_index(column).columnName;
  }
  public String getSchemaName(int column) throws SQLException {
    return check_index(column).schemaName;
  }
  public int getPrecision(int column) throws SQLException {
    return check_index(column).precision;
  }
  public int getScale(int column) throws SQLException {
    return check_index(column).scale;
  }
  public String getTableName(int column) throws SQLException {
    return check_index(column).tableName;
  }
  public String getCatalogName(int column) throws SQLException {
    return check_index(column).catalogName;
  }
  public int getColumnType(int column) throws SQLException {
    return check_index(column).type;
  }
  public String getColumnTypeName(int column) throws SQLException {
    return check_index(column).typeName;
  }
  public boolean isReadOnly(int column) throws SQLException {
    return check_index(column).isReadOnly;
  }
  public boolean isWritable(int column) throws SQLException {
    return check_index(column).isWritable;
  }
  public boolean isDefinitelyWritable(int column) throws SQLException {
    return check_index(column).isDefinitelyWritable;
  }
  public String getColumnClassName(int column) throws SQLException {
    return null;
  }
  public <T> T unwrap(java.lang.Class<T> iface) throws java.sql.SQLException
  {
    try {
      return iface.cast(this);
    } catch (ClassCastException cce) {
      throw OPLMessage_x.makeExceptionV(OPLMessage_x.errx_Unable_to_unwrap_to_XX, iface.toString());
    }
  }
  public boolean isWrapperFor(java.lang.Class<?> iface) throws java.sql.SQLException
  {
    return iface.isInstance(this);
  }
  private Coldesc check_index(int column) throws SQLException
  {
    if (desc==null || column < 1 || column > desc.length)
          throw OPLMessage_x.makeException(OPLMessage_x.errx_Column_Index_out_of_range);
    return desc[column - 1];
  }
  private class Coldesc implements Serializable {
    private String columnLabel;
    private String columnName;
    private String schemaName;
    private String tableName;
    private String catalogName;
    private String typeName;
    private int type;
    private int precision;
    private int scale;
    private int displaySize;
    private int nullable;
    private boolean isAutoIncrement;
    private boolean isCaseSensitive;
    private boolean isCurrency;
    private boolean isSigned;
    private boolean isSearchable;
    private boolean isDefinitelyWritable;
    private boolean isReadOnly;
    private boolean isWritable;
  }
}
