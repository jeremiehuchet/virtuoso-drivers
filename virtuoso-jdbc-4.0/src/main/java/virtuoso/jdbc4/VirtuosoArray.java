package virtuoso.jdbc4;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
public class VirtuosoArray implements Array {
    ArrayList<Object> data;
    String typeName;
    int typeCode;
    private static final HashMap<String,Integer> types = new HashMap<String,Integer>();
    static {
        types.put("varchar", Types.VARCHAR);
        types.put("character", Types.CHAR);
        types.put("nvarchar", Types.VARCHAR);
        types.put("char", Types.CHAR);
        types.put("nchar", Types.CHAR);
        types.put("numeric", Types.NUMERIC);
        types.put("decimal", Types.DECIMAL);
        types.put("integer", Types.INTEGER);
        types.put("int", Types.INTEGER);
        types.put("smallint", Types.SMALLINT);
        types.put("float", Types.FLOAT);
        types.put("real", Types.REAL);
        types.put("double", Types.DOUBLE);
        types.put("varbinary", Types.VARBINARY);
        types.put("timestamp", Types.TIMESTAMP);
        types.put("datetime", Types.TIMESTAMP);
        types.put("date", Types.DATE);
        types.put("time", Types.TIME);
        types.put("any", Types.OTHER);
    }
    public VirtuosoArray(VirtuosoConnection conn, String typeName, Object[] arr_data) throws VirtuosoException{
        this.typeName = typeName;
        Integer typeId = types.get(typeName.toLowerCase());
        typeCode = typeId!=null?typeId.intValue():Types.OTHER;
        if (arr_data!=null) {
            this.data = new ArrayList<Object>(arr_data.length);
            for(int i=0; i< arr_data.length; i++) {
                Object x = VirtuosoTypes.mapJavaTypeToSqlType(arr_data[i], typeCode);
                if (x instanceof String)
                {
                    switch(typeCode){
                        case Types.CHAR:
                        case Types.VARCHAR:
                        case Types.LONGVARCHAR:
                        case Types.CLOB:
                        case Types.OTHER:
                            this.data.add(new VirtuosoExplicitString((String)x, VirtuosoTypes.DV_ANY,conn));
                            break;
                        case Types.NCHAR:
                        case Types.NVARCHAR:
                        case Types.LONGNVARCHAR:
                        case Types.NCLOB:
                            this.data.add(new VirtuosoExplicitString((String)x, VirtuosoTypes.DV_WIDE,conn));
                            break;
                        default:
                            this.data.add(x);
                            break;
                    }
                }
                else {
                    this.data.add(x);
                }
            }
        }
    }
    public String getBaseTypeName() throws SQLException {
        return typeName;
    }
    public int getBaseType() throws SQLException {
        return typeCode;
    }
    public Object getArray() throws SQLException {
        return data.toArray();
    }
    public Object getArray(Map<String, Class<?>> map) throws SQLException {
        return getArray();
    }
    public Object getArray(long index, int count) throws SQLException {
        Object[] slice = new Object[count];
        for (int i = 0; i < count; i++)
            slice[i] = data.get((int) index + i - 1);
        return slice;
    }
    public Object getArray(long index, int count, Map<String, Class<?>> map) throws SQLException {
        return getArray(index, count);
    }
    public ResultSet getResultSet() throws SQLException {
        throw new VirtuosoFNSException ("getResultSet  not supported", VirtuosoException.NOTIMPLEMENTED);
    }
    public ResultSet getResultSet(Map<String, Class<?>> map) throws SQLException {
        return getResultSet();
    }
    public ResultSet getResultSet(long index, int count) throws SQLException {
        throw new VirtuosoFNSException ("getResultSet  not supported", VirtuosoException.NOTIMPLEMENTED);
    }
    public ResultSet getResultSet(long index, int count, Map<String, Class<?>> map) throws SQLException {
     return getResultSet(index, count);
    }
    @Override
    public void free() throws SQLException {
        data = null;
    }
}
