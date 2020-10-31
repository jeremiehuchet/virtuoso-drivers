package virtuoso.jdbc4;
import java.sql.*;
import java.sql.RowIdLifetime;
public class VirtuosoDatabaseMetaData implements DatabaseMetaData
{
   private VirtuosoConnection connection;
   VirtuosoDatabaseMetaData(VirtuosoConnection connection)
   {
      this.connection = connection;
   }
   public String getURL() throws VirtuosoException
   {
      return connection.getURL();
   }
   public String getUserName() throws VirtuosoException
   {
      return connection.getUserName();
   }
   public boolean isReadOnly() throws VirtuosoException
   {
      return false;
   }
   public boolean nullsAreSortedHigh() throws VirtuosoException
   {
      return true;
   }
   public boolean nullsAreSortedLow() throws VirtuosoException
   {
      return false;
   }
   public boolean nullsAreSortedAtStart() throws VirtuosoException
   {
      return false;
   }
   public boolean nullsAreSortedAtEnd() throws VirtuosoException
   {
      return false;
   }
   public String getDatabaseProductName() throws VirtuosoException
   {
      return new String("OpenLink Virtuoso VDBMS");
   }
   public String getDatabaseProductVersion() throws VirtuosoException
   {
      return connection.getVersion();
   }
   public String getDriverName() throws VirtuosoException
   {
      return new String("OpenLink Virtuoso JDBC pure Java");
   }
   public String getDriverVersion() throws VirtuosoException
   {
      return new String(virtuoso.jdbc4.Driver.major + "." + virtuoso.jdbc4.Driver.minor + " (for Java 2 platform)");
   }
   public int getDriverMajorVersion()
   {
      return virtuoso.jdbc4.Driver.major;
   }
   public int getDriverMinorVersion()
   {
      return virtuoso.jdbc4.Driver.minor;
   }
   public boolean usesLocalFiles() throws VirtuosoException
   {
      return true;
   }
   public boolean usesLocalFilePerTable() throws VirtuosoException
   {
      return false;
   }
   public boolean supportsMixedCaseIdentifiers() throws VirtuosoException
   {
      return (connection.getCase() == 0);
   }
   public boolean storesUpperCaseIdentifiers() throws VirtuosoException
   {
      return (connection.getCase() == 1);
   }
   public boolean storesLowerCaseIdentifiers() throws VirtuosoException
   {
      return false;
   }
   public boolean storesMixedCaseIdentifiers() throws VirtuosoException
   {
      return (connection.getCase() == 2);
   }
   public boolean supportsMixedCaseQuotedIdentifiers() throws VirtuosoException
   {
      return true;
   }
   public boolean storesUpperCaseQuotedIdentifiers() throws VirtuosoException
   {
      return false;
   }
   public boolean storesLowerCaseQuotedIdentifiers() throws VirtuosoException
   {
      return false;
   }
   public boolean storesMixedCaseQuotedIdentifiers() throws VirtuosoException
   {
      return true;
   }
   public String getIdentifierQuoteString() throws VirtuosoException
   {
      return new String("\"");
   }
   public String getSQLKeywords() throws VirtuosoException
   {
      return new String("CHAR,INT,NAME,STRING,INTNUM,APPROXNUM,AMMSC,PARAMETER,AS,OR,AND,NOT,UMINUS,ALL," + "AMMSC,ANY,ATTACH,AS,ASC,AUTHORIZATION,BETWEEN,BY,CHARACTER,CHECK,CLOSE," + "COMMIT,CONTINUE,CREATE,CURRENT,CURSOR,DECIMAL,DECLARE,DEFAULT,DELETE,DESC," + "DISTINCT,DOUBLE,DROP,ESCAPE,EXISTS,FETCH,FLOAT,FOR,FOREIGN,FOUND,FROM,GOTO,GO," + "GRANT,GROUP,HAVING,IN,INDEX,INDICATOR,INSERT,INTEGER,INTO,IS,KEY,LANGUAGE," + "LIKE,NULLX,NUMERIC,OF,ON,OPEN,OPTION,ORDER,PRECISION,PRIMARY,PRIVILEGES,PROCEDURE," + "PUBLIC,REAL,REFERENCES,ROLLBACK,SCHEMA,SELECT,SET,SMALLINT,SOME,SQLCODE,SQLERROR," + "TABLE,TO,UNION,UNIQUE,UPDATE,USER,VALUES,VIEW,WHENEVER,WHERE,WITH,WORK," + "CONTIGUOUS,OBJECT_ID,UNDER,CLUSTERED,VARCHAR,VARBINARY,LONG,REPLACING,SOFT," + "SHUTDOWN,CHECKPOINT,BACKUP,REPLICATION,SYNC,ALTER,ADD,RENAME,DISCONNECT," + "BEFORE,AFTER,INSTEAD,TRIGGER,REFERENCING,OLD,PROCEDURE,FUNCTION,OUT,INOUT," + "HANDLER,IF,THEN,ELSE,ELSEIF,WHILE,BEGINX,ENDX,EQUALS,RETURN,CALL,RETURNS,DO," + "EXCLUSIVE,PREFETCH,SQLSTATE,FOUND,REVOKE,PASSWORD,OFF,LOGX,SQLSTATE,TIMESTAMP," + "DATE,DATETIME,TIME,EXECUTE,OWNER,BEGIN_FN_X,BEGIN_OJ_X,CONVERT,CASE,WHEN,THEN," + "IDENTITY,LEFT,RIGHT,FULL,OUTER,JOIN,USE,");
   }
   public String getNumericFunctions() throws VirtuosoException
   {
      return new String("MOD,ABS,SIGN,ACOS,ASIN,ATAN,COS,SIN,TAN,COT,DEGREES,RADIANS," + "EXP,LOG,LOG10,SQRT,ATAN2,POWER,CEILING,FLOOR,PI,RAND");
   }
   public String getStringFunctions() throws VirtuosoException
   {
      return new String("CONCAT,LEFT,LTRIM,LENGTH,LCASE,REPEAT,RIGHT,RTRIM,SUBSTRING,UCASE,ASCII,CHAR," + "SPACE,LOCATE,LOCATE_2");
   }
   public String getSystemFunctions() throws VirtuosoException
   {
      return new String("USERNAME,DBNAME,IFNULL");
   }
   public String getTimeDateFunctions() throws VirtuosoException
   {
      return new String("NOW,CURDATE,DAYOFMONTH,DAYOFWEEK,DAYOFYEAR,MONTH,QUARTER,WEEK,YEAR,CURTIME," + "HOUR,MINUTE,SECOND,DAYNAME,MONTHNAME");
   }
   public String getSearchStringEscape() throws VirtuosoException
   {
      return new String("\\");
   }
   public String getExtraNameCharacters() throws VirtuosoException
   {
      return new String("");
   }
   public boolean supportsAlterTableWithAddColumn() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsAlterTableWithDropColumn() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsColumnAliasing() throws VirtuosoException
   {
      return true;
   }
   public boolean nullPlusNonNullIsNull() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsConvert() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsConvert(int fromType, int toType) throws VirtuosoException
   {
      switch(fromType)
      {
         case Types.CHAR:
         case Types.VARCHAR:
         case Types.LONGVARCHAR:
            return true;
         case Types.BIT:
         case Types.TINYINT:
         case Types.SMALLINT:
         case Types.INTEGER:
         case Types.BIGINT:
         case Types.FLOAT:
         case Types.REAL:
         case Types.DOUBLE:
         case Types.NUMERIC:
         case Types.DECIMAL:
            switch(toType)
            {
               case Types.CHAR:
               case Types.VARCHAR:
               case Types.LONGVARCHAR:
               case Types.BIT:
               case Types.TINYINT:
               case Types.SMALLINT:
               case Types.INTEGER:
               case Types.BIGINT:
               case Types.FLOAT:
               case Types.REAL:
               case Types.DOUBLE:
               case Types.NUMERIC:
               case Types.DECIMAL:
                  return true;
            }
            ;
            return false;
         case Types.BLOB:
         case Types.CLOB:
            switch(toType)
            {
               case Types.CHAR:
               case Types.VARCHAR:
               case Types.LONGVARCHAR:
               case Types.BLOB:
               case Types.CLOB:
                  return true;
            }
            ;
            return false;
         default:
            switch(toType)
            {
               case Types.CHAR:
               case Types.VARCHAR:
               case Types.LONGVARCHAR:
                  return true;
            }
            ;
            return false;
      }
   }
   public boolean supportsTableCorrelationNames() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsDifferentTableCorrelationNames() throws VirtuosoException
   {
      return false;
   }
   public boolean supportsExpressionsInOrderBy() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsOrderByUnrelated() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsGroupBy() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsGroupByUnrelated() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsGroupByBeyondSelect() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsLikeEscapeClause() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsMultipleResultSets() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsMultipleTransactions() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsNonNullableColumns() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsMinimumSQLGrammar() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsCoreSQLGrammar() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsExtendedSQLGrammar() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsANSI92EntryLevelSQL() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsANSI92IntermediateSQL() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsANSI92FullSQL() throws VirtuosoException
   {
      return false;
   }
   public boolean supportsIntegrityEnhancementFacility() throws SQLException
   {
      return true;
   }
   public boolean supportsOuterJoins() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsFullOuterJoins() throws VirtuosoException
   {
      return false;
   }
   public boolean supportsLimitedOuterJoins() throws VirtuosoException
   {
      return true;
   }
   public String getSchemaTerm() throws VirtuosoException
   {
      return new String("OWNER");
   }
   public String getProcedureTerm() throws VirtuosoException
   {
      return new String("PROCEDURE");
   }
   public String getCatalogTerm() throws VirtuosoException
   {
      return new String("QUALIFIER");
   }
   public boolean isCatalogAtStart() throws VirtuosoException
   {
      return true;
   }
   public String getCatalogSeparator() throws VirtuosoException
   {
      return new String(".");
   }
   public boolean supportsSchemasInDataManipulation() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsSchemasInProcedureCalls() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsSchemasInTableDefinitions() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsSchemasInIndexDefinitions() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsSchemasInPrivilegeDefinitions() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsCatalogsInDataManipulation() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsCatalogsInProcedureCalls() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsCatalogsInTableDefinitions() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsCatalogsInIndexDefinitions() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsCatalogsInPrivilegeDefinitions() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsPositionedDelete() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsPositionedUpdate() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsSelectForUpdate() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsStoredProcedures() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsSubqueriesInComparisons() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsSubqueriesInExists() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsSubqueriesInIns() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsSubqueriesInQuantifieds() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsCorrelatedSubqueries() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsUnion() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsUnionAll() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsOpenCursorsAcrossCommit() throws VirtuosoException
   {
      return false;
   }
   public boolean supportsOpenCursorsAcrossRollback() throws VirtuosoException
   {
      return false;
   }
   public boolean supportsOpenStatementsAcrossCommit() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsOpenStatementsAcrossRollback() throws VirtuosoException
   {
      return true;
   }
   public int getMaxBinaryLiteralLength() throws VirtuosoException
   {
      return 0;
   }
   public int getMaxCharLiteralLength() throws VirtuosoException
   {
      return 0;
   }
   public int getMaxColumnNameLength() throws VirtuosoException
   {
      return 100;
   }
   public int getMaxColumnsInGroupBy() throws VirtuosoException
   {
      return 0;
   }
   public int getMaxColumnsInIndex() throws VirtuosoException
   {
      return 0;
   }
   public int getMaxColumnsInOrderBy() throws VirtuosoException
   {
      return 0;
   }
   public int getMaxColumnsInSelect() throws VirtuosoException
   {
      return 0;
   }
   public int getMaxColumnsInTable() throws VirtuosoException
   {
      return 200;
   }
   public int getMaxConnections() throws VirtuosoException
   {
      return 0;
   }
   public int getMaxCursorNameLength() throws VirtuosoException
   {
      return 100;
   }
   public int getMaxIndexLength() throws VirtuosoException
   {
      return 1300;
   }
   public int getMaxSchemaNameLength() throws VirtuosoException
   {
      return 100;
   }
   public int getMaxProcedureNameLength() throws VirtuosoException
   {
      return 100;
   }
   public int getMaxCatalogNameLength() throws VirtuosoException
   {
      return 100;
   }
   public int getMaxRowSize() throws VirtuosoException
   {
      return 2000;
   }
   public boolean doesMaxRowSizeIncludeBlobs() throws VirtuosoException
   {
      return false;
   }
   public int getMaxStatementLength() throws VirtuosoException
   {
      return 0;
   }
   public int getMaxStatements() throws VirtuosoException
   {
      return 0;
   }
   public int getMaxTableNameLength() throws VirtuosoException
   {
      return 100;
   }
   public int getMaxTablesInSelect() throws VirtuosoException
   {
      return 0;
   }
   public int getMaxUserNameLength() throws VirtuosoException
   {
      return 100;
   }
   public int getDefaultTransactionIsolation() throws VirtuosoException
   {
      return Connection.TRANSACTION_REPEATABLE_READ;
   }
   public boolean supportsTransactions() throws VirtuosoException
   {
      return true;
   }
   public boolean supportsTransactionIsolationLevel(int level) throws VirtuosoException
   {
      if(level == Connection.TRANSACTION_READ_UNCOMMITTED
   || level == Connection.TRANSACTION_READ_COMMITTED
   || level == Connection.TRANSACTION_REPEATABLE_READ
   || level == Connection.TRANSACTION_SERIALIZABLE)
         return true;
      return false;
   }
   public boolean supportsDataDefinitionAndDataManipulationTransactions() throws VirtuosoException
   {
      return false;
   }
   public boolean supportsDataManipulationTransactionsOnly() throws VirtuosoException
   {
      return false;
   }
   public boolean dataDefinitionCausesTransactionCommit() throws VirtuosoException
   {
      return true;
   }
   public boolean dataDefinitionIgnoredInTransactions() throws VirtuosoException
   {
      return false;
   }
   public boolean allProceduresAreCallable() throws VirtuosoException
   {
      return false;
   }
   public boolean allTablesAreSelectable() throws VirtuosoException
   {
      return false;
   }
   private static final String getProceduresCaseMode0 =
       "SELECT " +
         "name_part (\\P_NAME, 0) AS PROCEDURE_CAT VARCHAR(128)," +
  "name_part (\\P_NAME, 1) AS PROCEDURE_SCHEM VARCHAR(128)," +
  "name_part (\\P_NAME, 2) AS PROCEDURE_NAME VARCHAR(128)," +
  "\\P_N_IN AS RES1," +
  "\\P_N_OUT AS RES2," +
  "\\P_N_R_SETS AS RES3," +
  "\\P_COMMENT AS REMARKS VARCHAR(254)," +
  "either(isnull(P_TYPE),0,P_TYPE) AS PROCEDURE_TYPE SMALLINT " +
       "FROM DB.DBA.SYS_PROCEDURES " +
       "WHERE " +
         "name_part (\\P_NAME, 0) like ? AND " +
  "name_part (\\P_NAME, 1) like ? AND " +
  "name_part (\\P_NAME, 2) like ? AND " +
  "__proc_exists (\\P_NAME) is not null " +
       "ORDER BY P_QUAL, P_NAME";
   private static final String getProceduresCaseMode2 =
       "SELECT " +
         "name_part (\\P_NAME, 0) AS PROCEDURE_CAT VARCHAR(128)," +
  "name_part (\\P_NAME, 1) AS PROCEDURE_SCHEM VARCHAR(128)," +
  "name_part (\\P_NAME, 2) AS PROCEDURE_NAME VARCHAR(128)," +
  "\\P_N_IN AS RES1," +
  "\\P_N_OUT AS RES2," +
  "\\P_N_R_SETS AS RES3," +
  "\\P_COMMENT AS REMARKS VARCHAR(254)," +
  "either(isnull(P_TYPE),0,P_TYPE) AS PROCEDURE_TYPE SMALLINT " +
       "FROM DB.DBA.SYS_PROCEDURES " +
       "WHERE " +
         "upper (name_part (\\P_NAME, 0)) like upper(?) AND " +
  "upper (name_part (\\P_NAME, 1)) like upper(?) AND " +
  "upper (name_part (\\P_NAME, 2)) like upper(?) AND " +
  "__proc_exists (\\P_NAME) is not null " +
       "ORDER BY P_QUAL, P_NAME";
   private static final String getWideProceduresCaseMode0 =
       "SELECT " +
         "charset_recode (name_part (\\P_NAME, 0), 'UTF-8', '_WIDE_') AS PROCEDURE_CAT NVARCHAR(128)," +
  "charset_recode (name_part (\\P_NAME, 1), 'UTF-8', '_WIDE_') AS PROCEDURE_SCHEM NVARCHAR(128)," +
  "chatset_recode (name_part (\\P_NAME, 2), 'UTF-8', '_WIDE_') AS PROCEDURE_NAME NVARCHAR(128)," +
  "\\P_N_IN AS RES1," +
  "\\P_N_OUT AS RES2," +
  "\\P_N_R_SETS AS RES3," +
  "\\P_COMMENT AS REMARKS VARCHAR(254)," +
  "either(isnull(P_TYPE),0,P_TYPE) AS PROCEDURE_TYPE SMALLINT " +
       "FROM DB.DBA.SYS_PROCEDURES " +
       "WHERE " +
         "name_part (\\P_NAME, 0) like ? AND " +
  "name_part (\\P_NAME, 1) like ? AND " +
  "name_part (\\P_NAME, 2) like ? AND " +
  "__proc_exists (\\P_NAME) is not null " +
       "ORDER BY P_QUAL, P_NAME";
   private static final String getWideProceduresCaseMode2 =
       "SELECT " +
         "charset_recode (name_part (\\P_NAME, 0), 'UTF-8', '_WIDE_') AS PROCEDURE_CAT NVARCHAR(128)," +
  "charset_recode (name_part (\\P_NAME, 1), 'UTF-8', '_WIDE_') AS PROCEDURE_SCHEM NVARCHAR(128)," +
  "charset_recode (name_part (\\P_NAME, 2), 'UTF-8', '_WIDE_') AS PROCEDURE_NAME NVARCHAR(128)," +
  "\\P_N_IN AS RES1," +
  "\\P_N_OUT AS RES2," +
  "\\P_N_R_SETS AS RES3," +
  "\\P_COMMENT AS REMARKS VARCHAR(254)," +
  "either(isnull(P_TYPE),0,P_TYPE) AS PROCEDURE_TYPE SMALLINT " +
       "FROM DB.DBA.SYS_PROCEDURES " +
       "WHERE " +
         "charset_recode (upper (charset_recode (name_part (\\P_NAME, 0), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
  " like charset_recode (upper(charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') AND " +
  "charset_recode (upper (charset_recode (name_part (\\P_NAME, 1), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
  " like charset_recode (upper(charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') AND " +
  "charset_recode (upper (charset_recode (name_part (\\P_NAME, 2), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
  " like charset_recode (upper(charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') AND " +
  "__proc_exists (\\P_NAME) is not null " +
       "ORDER BY P_QUAL, P_NAME";
   public ResultSet getProcedures(String catalog, String schemaPattern,
       String procedureNamePattern) throws SQLException
   {
      if(catalog == null)
         catalog = "%";
      if(schemaPattern == null)
         schemaPattern = "%";
      if(procedureNamePattern == null)
         procedureNamePattern = "%";
      VirtuosoPreparedStatement ps;
      if (connection.utf8_execs)
 ps = (VirtuosoPreparedStatement)
     connection.prepareStatement ((connection.getCase() == 2) ?
  getWideProceduresCaseMode2 :
  getWideProceduresCaseMode0);
      else
 ps = (VirtuosoPreparedStatement)
     connection.prepareStatement ((connection.getCase() == 2) ?
  getProceduresCaseMode2 :
  getProceduresCaseMode0);
      ps.setString (1, connection.escapeSQLString (catalog).toParamString());
      ps.setString (2, connection.escapeSQLString (schemaPattern).toParamString());
      ps.setString (3, connection.escapeSQLString (procedureNamePattern).toParamString());
      ResultSet rs = ps.executeQuery();
      return rs;
   }
   public ResultSet getProcedureColumns(String catalog, String schemaPattern,
       String procedureNamePattern, String columnNamePattern) throws SQLException
   {
      if(catalog == null)
         catalog = "%";
      if(schemaPattern == null)
         schemaPattern = "%";
      if(procedureNamePattern == null)
         procedureNamePattern = "%";
      if(columnNamePattern == null)
         columnNamePattern = "%";
      VirtuosoPreparedStatement ps = (VirtuosoPreparedStatement)
   (connection.utf8_execs ?
       connection.prepareStatement("DB.DBA.SQL_PROCEDURE_COLUMNSW (?, ?, ?, ?, ?, ?)") :
       connection.prepareStatement("DB.DBA.SQL_PROCEDURE_COLUMNS (?, ?, ?, ?, ?, ?)"));
      ps.setString (1, connection.escapeSQLString(catalog).toParamString());
      ps.setString (2, connection.escapeSQLString(schemaPattern).toParamString());
      ps.setString (3, connection.escapeSQLString(procedureNamePattern).toParamString());
      ps.setString (4, connection.escapeSQLString(columnNamePattern).toParamString());
      ps.setInt(5, connection.getCase());
      ps.setInt(6, 1);
      VirtuosoResultSet rs = (VirtuosoResultSet)ps.executeQuery();
      rs.metaData.setColumnName(8, "PRECISION");
      rs.metaData.setColumnName(9, "LENGTH");
      rs.metaData.setColumnName(10, "SCALE");
      rs.metaData.setColumnName(11, "RADIX");
      return rs;
   }
   private static final String getWideTablesCaseMode0 =
       "SELECT " +
         "charset_recode (name_part(\\KEY_TABLE,0), 'UTF-8', '_WIDE_') AS \\TABLE_CAT NVARCHAR(128)," +
  "charset_recode (name_part(\\KEY_TABLE,1), 'UTF-8', '_WIDE_') AS \\TABLE_SCHEM NVARCHAR(128)," +
  "charset_recode (name_part(\\KEY_TABLE,2), 'UTF-8', '_WIDE_') AS \\TABLE_NAME NVARCHAR(128)," +
         "table_type(\\KEY_TABLE)  AS \\TABLE_TYPE VARCHAR(128)," +
  "NULL AS \\REMARKS VARCHAR(254) " +
  ",NULL AS \\TYPE_CAT  VARCHAR(128) " +
  ",NULL AS \\TYPE_SCHEM VARCHAR(128) " +
  ",NULL AS \\TYPE_NAME VARCHAR(128) " +
  ",NULL AS \\SELF_REFERENCING_COL_NAME VARCHAR(128) " +
  ",NULL AS \\REF_GENERATION VARCHAR(128) " +
       "FROM DB.DBA.SYS_KEYS " +
       "WHERE " +
         "__any_grants(\\KEY_TABLE) AND " +
  "name_part(\\KEY_TABLE,0) LIKE ? AND " +
  "name_part(\\KEY_TABLE,1) LIKE ? AND " +
  "name_part(\\KEY_TABLE,2) LIKE ? AND " +
  "locate (concat ('G', table_type (\\KEY_TABLE)), ?) > 0 AND " +
  "\\KEY_IS_MAIN = 1 AND " +
  "\\KEY_MIGRATE_TO IS NULL " +
       "ORDER BY 4, 2, 3";
   private static final String getWideTablesCaseMode2 =
       "SELECT " +
         "charset_recode (name_part(\\KEY_TABLE,0), 'UTF-8', '_WIDE_') AS \\TABLE_CAT NVARCHAR(128)," +
  "charset_recode (name_part(\\KEY_TABLE,1), 'UTF-8', '_WIDE_') AS \\TABLE_SCHEM NVARCHAR(128)," +
  "charset_recode (name_part(\\KEY_TABLE,2), 'UTF-8', '_WIDE_') AS \\TABLE_NAME NVARCHAR(128)," +
         "table_type(\\KEY_TABLE)  AS \\TABLE_TYPE VARCHAR(128)," +
  "NULL AS \\REMARKS VARCHAR(254) " +
  ",NULL AS \\TYPE_CAT  VARCHAR(128) " +
  ",NULL AS \\TYPE_SCHEM VARCHAR(128) " +
  ",NULL AS \\TYPE_NAME VARCHAR(128) " +
  ",NULL AS \\SELF_REFERENCING_COL_NAME VARCHAR(128) " +
  ",NULL AS \\REF_GENERATION VARCHAR(128) " +
       "FROM DB.DBA.SYS_KEYS " +
       "WHERE " +
         "__any_grants(\\KEY_TABLE) AND " +
  "charset_recode (UPPER(charset_recode (name_part(\\KEY_TABLE,0), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
  " LIKE charset_recode (UPPER(charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') AND " +
  "charset_recode (UPPER(charset_recode (name_part(\\KEY_TABLE,1), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
  " LIKE charset_recode (UPPER(charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') AND " +
  "charset_recode (UPPER(charset_recode (name_part(\\KEY_TABLE,2), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
  " LIKE charset_recode (UPPER(charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') AND " +
  "locate (concat ('G', table_type (\\KEY_TABLE)), ?) > 0 AND " +
  "\\KEY_IS_MAIN = 1 AND " +
  "\\KEY_MIGRATE_TO IS NULL " +
       "ORDER BY 4, 2, 3";
   private static final String getTablesCaseMode0 =
       "SELECT " +
         "name_part(\\KEY_TABLE,0) AS \\TABLE_CAT VARCHAR(128)," +
  "name_part(\\KEY_TABLE,1) AS \\TABLE_SCHEM VARCHAR(128)," +
  "name_part(\\KEY_TABLE,2) AS \\TABLE_NAME VARCHAR(128)," +
         "table_type(\\KEY_TABLE)  AS \\TABLE_TYPE VARCHAR(128)," +
  "NULL AS \\REMARKS VARCHAR(254) " +
  ",NULL AS \\TYPE_CAT  VARCHAR(128) " +
  ",NULL AS \\TYPE_SCHEM VARCHAR(128) " +
  ",NULL AS \\TYPE_NAME VARCHAR(128) " +
  ",NULL AS \\SELF_REFERENCING_COL_NAME VARCHAR(128) " +
  ",NULL AS \\REF_GENERATION VARCHAR(128) " +
       "FROM DB.DBA.SYS_KEYS " +
       "WHERE " +
         "__any_grants(\\KEY_TABLE) AND " +
  "name_part(\\KEY_TABLE,0) LIKE ? AND " +
  "name_part(\\KEY_TABLE,1) LIKE ? AND " +
  "name_part(\\KEY_TABLE,2) LIKE ? AND " +
  "locate (concat ('G', table_type (\\KEY_TABLE)), ?) > 0 AND " +
  "\\KEY_IS_MAIN = 1 AND " +
  "\\KEY_MIGRATE_TO IS NULL " +
       "ORDER BY 4, 2, 3";
   private static final String getTablesCaseMode2 =
       "SELECT " +
         "name_part(\\KEY_TABLE,0) AS \\TABLE_CAT VARCHAR(128)," +
  "name_part(\\KEY_TABLE,1) AS \\TABLE_SCHEM VARCHAR(128)," +
  "name_part(\\KEY_TABLE,2) AS \\TABLE_NAME VARCHAR(128)," +
         "table_type(\\KEY_TABLE)  AS \\TABLE_TYPE VARCHAR(128)," +
  "NULL AS \\REMARKS VARCHAR(254) " +
  ",NULL AS \\TYPE_CAT  VARCHAR(128) " +
  ",NULL AS \\TYPE_SCHEM VARCHAR(128) " +
  ",NULL AS \\TYPE_NAME VARCHAR(128) " +
  ",NULL AS \\SELF_REFERENCING_COL_NAME VARCHAR(128) " +
  ",NULL AS \\REF_GENERATION VARCHAR(128) " +
       "FROM DB.DBA.SYS_KEYS " +
       "WHERE " +
         "__any_grants(\\KEY_TABLE) AND " +
  "UPPER(name_part(\\KEY_TABLE,0)) LIKE UPPER(?) AND " +
  "UPPER(name_part(\\KEY_TABLE,1)) LIKE UPPER(?) AND " +
  "UPPER(name_part(\\KEY_TABLE,2)) LIKE UPPER(?) AND " +
  "locate (concat ('G', table_type (\\KEY_TABLE)), ?) > 0 AND " +
  "\\KEY_IS_MAIN = 1 AND " +
  "\\KEY_MIGRATE_TO IS NULL " +
       "ORDER BY 4, 2, 3";
   public ResultSet getTables(String catalog, String schemaPattern,
       String tableNamePattern, String types[]) throws SQLException
   {
      if(catalog != null && catalog.equals("%") && schemaPattern == null && tableNamePattern == null)
 return getCatalogs();
      if(schemaPattern != null && schemaPattern.equals("%") && catalog == null && tableNamePattern == null)
 return getSchemas();
      if(types!=null && types[0].equals("%") && catalog == null &&
   schemaPattern == null && tableNamePattern == null)
 return getTableTypes();
      StringBuffer typ = new StringBuffer();
      if(types != null)
         for(int i = 0;i < types.length;i++)
  {
    if (types[i].equals("TABLE"))
      typ.append("GTABLE");
    else if (types[i].equals("VIEW"))
      typ.append("GVIEW");
    else if (types[i].equals("SYSTEM TABLE"))
      typ.append("GSYSTEM TABLE");
  }
      if (typ.length() == 0)
 typ.append("GTABLEGVIEWGSYSTEM TABLE");
      if (catalog == null)
 catalog = "%";
      if (schemaPattern == null)
 schemaPattern = "%";
      if (tableNamePattern == null)
 tableNamePattern = "%";
      VirtuosoPreparedStatement ps;
      if (connection.utf8_execs)
 ps = (VirtuosoPreparedStatement) connection.prepareStatement(
     (connection.getCase() == 2) ?
     getWideTablesCaseMode2 :
     getWideTablesCaseMode0);
      else
 ps = (VirtuosoPreparedStatement) connection.prepareStatement(
     (connection.getCase() == 2) ?
     getTablesCaseMode2 :
     getTablesCaseMode0);
      ps.setString(1,connection.escapeSQLString(catalog).toParamString());
      ps.setString(2,connection.escapeSQLString(schemaPattern).toParamString());
      ps.setString(3,connection.escapeSQLString (tableNamePattern).toParamString());
      ps.setString(4,connection.escapeSQLString(typ.toString()).toParamString());
      ResultSet rs = ps.executeQuery();
      return rs;
   }
   private static final String getSchemasText =
 "select distinct" +
 " name_part(KEY_TABLE, 1) AS \\TABLE_SCHEM VARCHAR(128)" +
 ", null AS \\TABLE_CAT VARCHAR(128)" +
 "from DB.DBA.SYS_KEYS";
   private static final String getWideSchemasText =
 "select distinct" +
 " charset_recode (name_part(KEY_TABLE, 1), 'UTF-8', '_WIDE_') AS \\TABLE_SCHEM NVARCHAR(128)" +
 ", null AS \\TABLE_CAT NVARCHAR(128)" +
 "from DB.DBA.SYS_KEYS";
   public ResultSet getSchemas() throws SQLException
   {
      Statement st = connection.createStatement();
      ResultSet rs = st.executeQuery(connection.utf8_execs ? getWideSchemasText : getSchemasText);
      return rs;
   }
   private static final String getCatalogsText =
 "select" +
 " distinct name_part(KEY_TABLE,0) AS \\TABLE_CAT VARCHAR(128)" +
 "from DB.DBA.SYS_KEYS order by 1";
   private static final String getWideCatalogsText =
 "select" +
 " distinct charset_recode (name_part(KEY_TABLE,0), 'UTF-8', '_WIDE_') AS \\TABLE_CAT NVARCHAR(128)" +
 "from DB.DBA.SYS_KEYS order by 1";
   public ResultSet getCatalogs() throws SQLException
   {
      Statement st = connection.createStatement();
      ResultSet rs = st.executeQuery(connection.utf8_execs ? getWideCatalogsText : getCatalogsText);
      return rs;
   }
   private static final String getTableTypes_text =
    "select distinct" +
    " table_type (KEY_TABLE)" +
    "   AS \\TABLE_TYPE VARCHAR(128)," +
    " NULL AS \\REMARKS VARCHAR(254) " +
    "from DB.DBA.SYS_KEYS";
   public ResultSet getTableTypes() throws SQLException
   {
     return connection.createStatement().executeQuery(getTableTypes_text);
   }
   private static final String getColumsText_case0 =
       "SELECT " +
         "name_part(k.KEY_TABLE,0) AS \\TABLE_CAT VARCHAR(128), " +
  "name_part(k.KEY_TABLE,1) AS \\TABLE_SCHEM VARCHAR(128), " +
  "name_part(k.KEY_TABLE,2) AS \\TABLE_NAME VARCHAR(128), " +
  "c.\"COLUMN\" AS \\COLUMN_NAME VARCHAR(128), " +
  "dv_to_sql_type(c.COL_DTP) AS \\DATA_TYPE SMALLINT, " +
  "dv_type_title(c.COL_DTP) AS \\TYPE_NAME VARCHAR(128), " +
  "c.COL_PREC AS \\COLUMN_SIZE INTEGER, " +
  "NULL AS \\BUFFER_LENGTH INTEGER, " +
  "c.COL_SCALE AS \\DECIMAL_DIGITS SMALLINT, " +
  "2 AS \\NUM_PREC_RADIX SMALLINT, " +
  "either(isnull(c.COL_NULLABLE),2,c.COL_NULLABLE) AS \\NULLABLE SMALLINT, " +
  "NULL AS \\REMARKS VARCHAR(254), " +
  "NULL AS \\COLUMN_DEF VARCHAR(128), " +
  "NULL AS \\SQL_DATA_TYPE INTEGER, " +
  "NULL AS \\SQL_DATETIME_SUB INTEGER, " +
  "NULL AS \\CHAR_OCTET_LENGTH INTEGER, " +
  "NULL AS \\ORDINAL_POSITION INTEGER, " +
  "NULL AS \\IS_NULLABLE VARCHAR(10) " +
  ",NULL AS \\SCOPE_CATLOG VARCHAR(128) " +
  ",NULL AS \\SCOPE_SCHEMA VARCHAR(128) " +
  ",NULL AS \\SCOPE_TABLE VARCHAR(128) " +
  ",NULL AS \\SOURCE_DATA_TYPE SMALLINT " +
       "FROM " +
         "DB.DBA.SYS_KEYS k, " +
  "DB.DBA.SYS_KEY_PARTS kp, " +
  "DB.DBA.SYS_COLS c " +
       "WHERE " +
         "name_part(k.KEY_TABLE,0) LIKE ? " +
  "AND name_part(k.KEY_TABLE,1) LIKE ? " +
  "AND name_part(k.KEY_TABLE,2) LIKE ? " +
  "AND c.\"COLUMN\" LIKE ? " +
  "AND c.\"COLUMN\" <> '_IDN' " +
  "AND k.KEY_IS_MAIN = 1 " +
  "AND k.KEY_MIGRATE_TO is null " +
  "AND kp.KP_KEY_ID = k.KEY_ID " +
  "AND COL_ID = KP_COL " +
       "ORDER BY k.KEY_TABLE, c.COL_ID";
   private static final String getColumsText_case2 =
       "SELECT " +
         "name_part(k.KEY_TABLE,0) AS \\TABLE_CAT VARCHAR(128), " +
  "name_part(k.KEY_TABLE,1) AS \\TABLE_SCHEM VARCHAR(128), " +
  "name_part(k.KEY_TABLE,2) AS \\TABLE_NAME VARCHAR(128)," +
  "c.\"COLUMN\" AS \\COLUMN_NAME VARCHAR(128), " +
  "dv_to_sql_type(c.COL_DTP) AS \\DATA_TYPE SMALLINT," +
  "dv_type_title(c.COL_DTP) AS \\TYPE_NAME VARCHAR(128), " +
  "c.COL_PREC AS \\COLUMN_SIZE INTEGER, " +
  "NULL AS \\BUFFER_LENGTH INTEGER, " +
  "c.COL_SCALE AS \\DECIMAL_DIGITS SMALLINT," +
  "2 AS \\NUM_PREC_RADIX SMALLINT, " +
  "either(isnull(c.COL_NULLABLE),2,c.COL_NULLABLE) AS \\NULLABLE SMALLINT," +
  "NULL AS \\REMARKS VARCHAR(254), " +
  "NULL AS \\COLUMN_DEF VARCHAR(128), " +
  "NULL AS \\SQL_DATA_TYPE INTEGER, " +
  "NULL AS \\SQL_DATETIME_SUB INTEGER," +
  "NULL AS \\CHAR_OCTET_LENGTH INTEGER, " +
  "NULL AS \\ORDINAL_POSITION INTEGER, " +
  "NULL AS \\IS_NULLABLE VARCHAR(10) " +
  ",NULL AS \\SCOPE_CATLOG VARCHAR(128) " +
  ",NULL AS \\SCOPE_SCHEMA VARCHAR(128) " +
  ",NULL AS \\SCOPE_TABLE VARCHAR(128) " +
  ",NULL AS \\SOURCE_DATA_TYPE SMALLINT " +
       "FROM " +
         "DB.DBA.SYS_KEYS k, " +
  "DB.DBA.SYS_KEY_PARTS kp, " +
  "DB.DBA.SYS_COLS c " +
       "WHERE " +
         "upper(name_part(k.KEY_TABLE,0)) LIKE upper(?) " +
  "AND upper(name_part(k.KEY_TABLE,1)) LIKE upper(?) " +
  "AND upper(name_part(k.KEY_TABLE,2)) LIKE upper(?) " +
  "AND upper (c.\"COLUMN\") LIKE upper(?) " +
  "AND c.\"COLUMN\" <> '_IDN' " +
  "AND k.KEY_IS_MAIN = 1 " +
  "AND k.KEY_MIGRATE_TO is null " +
  "AND kp.KP_KEY_ID = k.KEY_ID " +
  "AND COL_ID = KP_COL " +
       "ORDER BY k.KEY_TABLE, c.COL_ID";
   private static final String getWideColumsText_case0 =
       "SELECT " +
         "charset_recode (name_part(k.KEY_TABLE,0), 'UTF-8', '_WIDE_') AS \\TABLE_CAT NVARCHAR(128), " +
  "charset_recode (name_part(k.KEY_TABLE,1), 'UTF-8', '_WIDE_') AS \\TABLE_SCHEM NVARCHAR(128), " +
  "charset_recode (name_part(k.KEY_TABLE,2), 'UTF-8', '_WIDE_') AS \\TABLE_NAME NVARCHAR(128), " +
  "charset_recode (c.\"COLUMN\", 'UTF-8', '_WIDE_') AS \\COLUMN_NAME NVARCHAR(128), " +
  "dv_to_sql_type(c.COL_DTP) AS \\DATA_TYPE SMALLINT, " +
  "dv_type_title(c.COL_DTP) AS \\TYPE_NAME VARCHAR(128), " +
  "c.COL_PREC AS \\COLUMN_SIZE INTEGER, " +
  "NULL AS \\BUFFER_LENGTH INTEGER, " +
  "c.COL_SCALE AS \\DECIMAL_DIGITS SMALLINT, " +
  "2 AS \\NUM_PREC_RADIX SMALLINT, " +
  "either(isnull(c.COL_NULLABLE),2,c.COL_NULLABLE) AS \\NULLABLE SMALLINT, " +
  "NULL AS \\REMARKS VARCHAR(254), " +
  "NULL AS \\COLUMN_DEF VARCHAR(128), " +
  "NULL AS \\SQL_DATA_TYPE INTEGER, " +
  "NULL AS \\SQL_DATETIME_SUB INTEGER, " +
  "NULL AS \\CHAR_OCTET_LENGTH INTEGER, " +
  "NULL AS \\ORDINAL_POSITION INTEGER, " +
  "NULL AS \\IS_NULLABLE VARCHAR(10) " +
  ",NULL AS \\SCOPE_CATLOG VARCHAR(128) " +
  ",NULL AS \\SCOPE_SCHEMA VARCHAR(128) " +
  ",NULL AS \\SCOPE_TABLE VARCHAR(128) " +
  ",NULL AS \\SOURCE_DATA_TYPE SMALLINT " +
       "FROM " +
         "DB.DBA.SYS_KEYS k, " +
  "DB.DBA.SYS_KEY_PARTS kp, " +
  "DB.DBA.SYS_COLS c " +
       "WHERE " +
         "name_part(k.KEY_TABLE,0) LIKE ? " +
  "AND name_part(k.KEY_TABLE,1) LIKE ? " +
  "AND name_part(k.KEY_TABLE,2) LIKE ? " +
  "AND c.\"COLUMN\" LIKE ? " +
  "AND c.\"COLUMN\" <> '_IDN' " +
  "AND k.KEY_IS_MAIN = 1 " +
  "AND k.KEY_MIGRATE_TO is null " +
  "AND kp.KP_KEY_ID = k.KEY_ID " +
  "AND COL_ID = KP_COL " +
       "ORDER BY k.KEY_TABLE, c.COL_ID";
   private static final String getWideColumsText_case2 =
       "SELECT " +
         "charset_recode (name_part(k.KEY_TABLE,0), 'UTF-8', '_WIDE_') AS \\TABLE_CAT NVARCHAR(128), " +
  "charset_recode (name_part(k.KEY_TABLE,1), 'UTF-8', '_WIDE_') AS \\TABLE_SCHEM NVARCHAR(128), " +
  "charset_recode (name_part(k.KEY_TABLE,2), 'UTF-8', '_WIDE_') AS \\TABLE_NAME NVARCHAR(128), " +
  "charset_recode (c.\"COLUMN\", 'UTF-8', '_WIDE_') AS \\COLUMN_NAME NVARCHAR(128), " +
  "dv_to_sql_type(c.COL_DTP) AS \\DATA_TYPE SMALLINT," +
  "dv_type_title(c.COL_DTP) AS \\TYPE_NAME VARCHAR(128), " +
  "c.COL_PREC AS \\COLUMN_SIZE INTEGER, " +
  "NULL AS \\BUFFER_LENGTH INTEGER, " +
  "c.COL_SCALE AS \\DECIMAL_DIGITS SMALLINT," +
  "2 AS \\NUM_PREC_RADIX SMALLINT, " +
  "either(isnull(c.COL_NULLABLE),2,c.COL_NULLABLE) AS \\NULLABLE SMALLINT," +
  "NULL AS \\REMARKS VARCHAR(254), " +
  "NULL AS \\COLUMN_DEF VARCHAR(128), " +
  "NULL AS \\SQL_DATA_TYPE INTEGER, " +
  "NULL AS \\SQL_DATETIME_SUB INTEGER," +
  "NULL AS \\CHAR_OCTET_LENGTH INTEGER, " +
  "NULL AS \\ORDINAL_POSITION INTEGER, " +
  "NULL AS \\IS_NULLABLE VARCHAR(10) " +
  ",NULL AS \\SCOPE_CATLOG VARCHAR(128) " +
  ",NULL AS \\SCOPE_SCHEMA VARCHAR(128) " +
  ",NULL AS \\SCOPE_TABLE VARCHAR(128) " +
  ",NULL AS \\SOURCE_DATA_TYPE SMALLINT " +
       "FROM " +
         "DB.DBA.SYS_KEYS k, " +
  "DB.DBA.SYS_KEY_PARTS kp, " +
  "DB.DBA.SYS_COLS c " +
       "WHERE " +
         "charset_recode (upper(charset_recode (name_part(k.KEY_TABLE,0), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
  " LIKE charset_recode (upper(charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') " +
  "AND charset_recode (upper(charset_recode (name_part(k.KEY_TABLE,1), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
  " LIKE charset_recode (upper(charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') " +
  "AND charset_recode (upper(charset_recode (name_part(k.KEY_TABLE,2), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
  " LIKE charset_recode (upper(charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') " +
  "AND charset_recode (upper (charset_recode (c.\"COLUMN\", 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
  " LIKE charset_recode (upper(charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') " +
  "AND c.\"COLUMN\" <> '_IDN' " +
  "AND k.KEY_IS_MAIN = 1 " +
  "AND k.KEY_MIGRATE_TO is null " +
  "AND kp.KP_KEY_ID = k.KEY_ID " +
  "AND COL_ID = KP_COL " +
       "ORDER BY k.KEY_TABLE, c.COL_ID";
   public ResultSet getColumns(String catalog, String schemaPattern,
       String tableNamePattern, String columnNamePattern) throws SQLException
   {
      if(catalog == null)
         catalog = "";
      if(schemaPattern == null)
         schemaPattern = "";
      if(tableNamePattern == null)
         tableNamePattern = "";
      if(columnNamePattern == null)
         columnNamePattern = "";
      catalog = (catalog.equals("")) ? "%" : catalog;
      schemaPattern = (schemaPattern.equals("")) ? "%" : schemaPattern;
      tableNamePattern = (tableNamePattern.equals("")) ? "%" : tableNamePattern;
      columnNamePattern = (columnNamePattern.equals("")) ? "%" : columnNamePattern;
      VirtuosoPreparedStatement ps;
      if (connection.utf8_execs)
 ps = (VirtuosoPreparedStatement)connection.prepareStatement(
     (connection.getCase() == 2) ? getWideColumsText_case2 : getWideColumsText_case0);
      else
 ps = (VirtuosoPreparedStatement)connection.prepareStatement(
     (connection.getCase() == 2) ? getColumsText_case2 : getColumsText_case0);
      ps.setString(1,connection.escapeSQLString (catalog).toParamString());
      ps.setString(2,connection.escapeSQLString (schemaPattern).toParamString());
      ps.setString(3,connection.escapeSQLString (tableNamePattern).toParamString());
      ps.setString(4,connection.escapeSQLString (columnNamePattern).toParamString());
      ResultSet rs = ps.executeQuery();
      return rs;
   }
   public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) throws SQLException
   {
      if(catalog == null)
         catalog = "%";
      if(schema == null)
         schema = "%";
      if(table == null)
         table = "%";
      if(columnNamePattern == null)
         columnNamePattern = "%";
      VirtuosoPreparedStatement ps = (VirtuosoPreparedStatement)
   (connection.utf8_execs ?
       connection.prepareStatement("DB.DBA.column_privileges_utf8(?,?,?,?)") :
       connection.prepareStatement("DB.DBA.column_privileges(?,?,?,?)"));
      ps.setString (1, connection.escapeSQLString(catalog).toParamString());
      ps.setString (2, connection.escapeSQLString(schema).toParamString());
      ps.setString (3, connection.escapeSQLString(table).toParamString());
      ps.setString (4, connection.escapeSQLString(columnNamePattern).toParamString());
      return ps.executeQuery();
   }
   public ResultSet getTablePrivileges(String catalog, String schemaPattern,
 String tableNamePattern) throws VirtuosoException
   {
      if(catalog == null)
         catalog = "%";
      if(schemaPattern == null)
         schemaPattern = "%";
      if(tableNamePattern == null)
         tableNamePattern = "%";
      VirtuosoPreparedStatement ps = (VirtuosoPreparedStatement)
   connection.prepareStatement("DB.DBA.table_privileges(?,?,?)");
      ps.setString (1, connection.escapeSQLString(catalog).toParamString());
      ps.setString (2, connection.escapeSQLString(schemaPattern).toParamString());
      ps.setString (3, connection.escapeSQLString(tableNamePattern).toParamString());
      return ps.executeQuery();
   }
   public static final int VARCHAR_UNSPEC_SIZE = 4080;
   public static final String getWideBestRowIdText_case0 =
    "select" +
    " 0 AS \\SCOPE SMALLINT," +
    " charset_recode (SYS_COLS.\\COLUMN, 'UTF-8', '_WIDE_') AS COLUMN_NAME NVARCHAR(128)," +
    " dv_to_sql_type(SYS_COLS.COL_DTP) AS DATA_TYPE SMALLINT," +
    " dv_type_title(SYS_COLS.COL_DTP) AS TYPE_NAME VARCHAR(128)," +
    " case SYS_COLS.COL_PREC when 0 then " + VARCHAR_UNSPEC_SIZE + " else SYS_COLS.COL_PREC end AS COLUMN_SIZE INTEGER,\n" +
    " SYS_COLS.COL_PREC AS BUFFER_LENGTH INTEGER," +
    " SYS_COLS.COL_SCALE AS DECIMAL_DIGITS SMALLINT," +
    " 1 AS PSEUDO_COLUMN SMALLINT " +
    "from DB.DBA.SYS_KEYS SYS_KEYS, DB.DBA.SYS_KEY_PARTS SYS_KEY_PARTS," +
    " DB.DBA.SYS_COLS SYS_COLS " +
    " where name_part(SYS_KEYS.KEY_TABLE,0) like ?" +
    "  and __any_grants (KEY_TABLE) " +
    "  and name_part(SYS_KEYS.KEY_TABLE,1) like ?" +
    "  and name_part(SYS_KEYS.KEY_TABLE,2) like ?" +
    "  and SYS_KEYS.KEY_IS_MAIN = 1" +
    "  and SYS_KEYS.KEY_MIGRATE_TO is NULL" +
    "  and SYS_KEY_PARTS.KP_KEY_ID = SYS_KEYS.KEY_ID" +
    "  and SYS_KEY_PARTS.KP_NTH < SYS_KEYS.KEY_DECL_PARTS" +
    "  and SYS_COLS.COL_ID = SYS_KEY_PARTS.KP_COL " +
    " order by SYS_KEYS.KEY_TABLE, SYS_KEY_PARTS.KP_NTH";
   public static final String getWideBestRowIdText_case2 =
    "select" +
    " 0 AS \\SCOPE SMALLINT," +
    " charset_recode (SYS_COLS.\\COLUMN, 'UTF-8', '_WIDE_') AS COLUMN_NAME NVARCHAR(128)," +
    " dv_to_sql_type(SYS_COLS.COL_DTP) AS DATA_TYPE SMALLINT," +
    " dv_type_title(SYS_COLS.COL_DTP) AS TYPE_NAME VARCHAR(128)," +
    " case SYS_COLS.COL_PREC when 0 then " + VARCHAR_UNSPEC_SIZE + " else SYS_COLS.COL_PREC end AS COLUMN_SIZE INTEGER,\n" +
    " SYS_COLS.COL_PREC AS BUFFER_LENGTH INTEGER," +
    " SYS_COLS.COL_SCALE AS DECIMAL_DIGITS SMALLINT," +
    " 1 AS PSEUDO_COLUMN SMALLINT " +
    "from DB.DBA.SYS_KEYS SYS_KEYS, DB.DBA.SYS_KEY_PARTS SYS_KEY_PARTS," +
    " DB.DBA.SYS_COLS SYS_COLS " +
    " where charset_recode (upper(charset_recode (name_part(SYS_KEYS.KEY_TABLE,0), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8        ') like charset_recode (upper(charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
    "  and __any_grants (KEY_TABLE) " +
    "  and charset_recode (upper(charset_recode (name_part(SYS_KEYS.KEY_TABLE,1), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8'        ) like charset_recode (upper(charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
    "  and charset_recode (upper(charset_recode (name_part(SYS_KEYS.KEY_TABLE,2), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8'        ) like charset_recode (upper(charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
    "  and SYS_KEYS.KEY_IS_MAIN = 1" +
    "  and SYS_KEYS.KEY_MIGRATE_TO is NULL" +
    "  and SYS_KEY_PARTS.KP_KEY_ID = SYS_KEYS.KEY_ID" +
    "  and SYS_KEY_PARTS.KP_NTH < SYS_KEYS.KEY_DECL_PARTS" +
    "  and SYS_COLS.COL_ID = SYS_KEY_PARTS.KP_COL " +
    " order by SYS_KEYS.KEY_TABLE, SYS_KEY_PARTS.KP_NTH";
   public static final String getBestRowIdText_case0 =
    "select" +
    " 0 AS \\SCOPE SMALLINT," +
    " SYS_COLS.\\COLUMN AS COLUMN_NAME VARCHAR(128)," +
    " dv_to_sql_type(SYS_COLS.COL_DTP) AS DATA_TYPE SMALLINT," +
    " dv_type_title(SYS_COLS.COL_DTP) AS TYPE_NAME VARCHAR(128)," +
    " case SYS_COLS.COL_PREC when 0 then " + VARCHAR_UNSPEC_SIZE + " else SYS_COLS.COL_PREC end AS COLUMN_SIZE INTEGER,\n" +
    " SYS_COLS.COL_PREC AS BUFFER_LENGTH INTEGER," +
    " SYS_COLS.COL_SCALE AS DECIMAL_DIGITS SMALLINT," +
    " 1 AS PSEUDO_COLUMN SMALLINT " +
    "from DB.DBA.SYS_KEYS SYS_KEYS, DB.DBA.SYS_KEY_PARTS SYS_KEY_PARTS," +
    " DB.DBA.SYS_COLS SYS_COLS " +
    " where name_part(SYS_KEYS.KEY_TABLE,0) like ?" +
    "  and __any_grants (KEY_TABLE) " +
    "  and name_part(SYS_KEYS.KEY_TABLE,1) like ?" +
    "  and name_part(SYS_KEYS.KEY_TABLE,2) like ?" +
    "  and SYS_KEYS.KEY_IS_MAIN = 1" +
    "  and SYS_KEYS.KEY_MIGRATE_TO is NULL" +
    "  and SYS_KEY_PARTS.KP_KEY_ID = SYS_KEYS.KEY_ID" +
    "  and SYS_KEY_PARTS.KP_NTH < SYS_KEYS.KEY_DECL_PARTS" +
    "  and SYS_COLS.COL_ID = SYS_KEY_PARTS.KP_COL " +
    " order by SYS_KEYS.KEY_TABLE, SYS_KEY_PARTS.KP_NTH";
   public static final String getBestRowIdText_case2 =
    "select" +
    " 0 AS \\SCOPE SMALLINT," +
    " SYS_COLS.\\COLUMN AS COLUMN_NAME VARCHAR(128)," +
    " dv_to_sql_type(SYS_COLS.COL_DTP) AS DATA_TYPE SMALLINT," +
    " dv_type_title(SYS_COLS.COL_DTP) AS TYPE_NAME VARCHAR(128)," +
    " case SYS_COLS.COL_PREC when 0 then " + VARCHAR_UNSPEC_SIZE + " else SYS_COLS.COL_PREC end AS COLUMN_SIZE INTEGER,\n" +
    " SYS_COLS.COL_PREC AS BUFFER_LENGTH INTEGER," +
    " SYS_COLS.COL_SCALE AS DECIMAL_DIGITS SMALLINT," +
    " 1 AS PSEUDO_COLUMN SMALLINT " +
    "from DB.DBA.SYS_KEYS SYS_KEYS, DB.DBA.SYS_KEY_PARTS SYS_KEY_PARTS," +
    " DB.DBA.SYS_COLS SYS_COLS " +
    " where upper(name_part(SYS_KEYS.KEY_TABLE,0)) like upper(?)" +
    "  and __any_grants (KEY_TABLE) " +
    "  and upper(name_part(SYS_KEYS.KEY_TABLE,1)) like upper(?)" +
    "  and upper(name_part(SYS_KEYS.KEY_TABLE,2)) like upper(?)" +
    "  and SYS_KEYS.KEY_IS_MAIN = 1" +
    "  and SYS_KEYS.KEY_MIGRATE_TO is NULL" +
    "  and SYS_KEY_PARTS.KP_KEY_ID = SYS_KEYS.KEY_ID" +
    "  and SYS_KEY_PARTS.KP_NTH < SYS_KEYS.KEY_DECL_PARTS" +
    "  and SYS_COLS.COL_ID = SYS_KEY_PARTS.KP_COL " +
    " order by SYS_KEYS.KEY_TABLE, SYS_KEY_PARTS.KP_NTH";
   public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) throws VirtuosoException
   {
      if(catalog == null)
         catalog = "";
      if(schema == null)
         schema = "";
      if(table == null)
         table = "";
      catalog = (catalog.equals("")) ? "%" : catalog;
      schema = (schema.equals("")) ? "%" : schema;
      table = (table.equals("")) ? "%" : table;
      VirtuosoPreparedStatement ps;
      if (connection.utf8_execs)
 ps = (VirtuosoPreparedStatement)connection.prepareStatement(
     (connection.getCase() == 2) ? getWideBestRowIdText_case2 : getWideBestRowIdText_case0);
      else
 ps = (VirtuosoPreparedStatement)connection.prepareStatement(
     (connection.getCase() == 2) ? getBestRowIdText_case2 : getBestRowIdText_case0);
      ps.setString(1,connection.escapeSQLString (catalog).toParamString());
      ps.setString(2,connection.escapeSQLString (schema).toParamString());
      ps.setString(3,connection.escapeSQLString (table).toParamString());
      ResultSet rs = ps.executeQuery();
      return rs;
   }
   public static final String getWideBestVersionColsText_case0 =
    "select" +
          " null as \\SCOPE smallint," +
          " charset_recode (\\COLUMN, 'UTF-8', '_WIDE_') as COLUMN_NAME nvarchar(128)," +
          " dv_to_sql_type(COL_DTP) as DATA_TYPE smallint," +
          " dv_type_title(COL_DTP) as TYPE_NAME varchar(128)," +
          " case COL_PREC when 0 then " + VARCHAR_UNSPEC_SIZE + " else COL_PREC end AS COLUMN_SIZE INTEGER,\n" +
          " COL_PREC as BUFFER_LENGTH integer," +
          " COL_SCALE as DECIMAL_DIGITS smallint," +
          " 1 as PSEUDO_COLUMN smallint " +
          "from DB.DBA.SYS_COLS " +
          "where \\COL_DTP = 128" +
          "  and name_part(\\TABLE,0) like ?" +
          "  and name_part(\\TABLE,1) like ?" +
          "  and name_part(\\TABLE,2) like ? " +
          "order by \\TABLE, \\COL_ID";
   public static final String getWideBestVersionColsText_case2 =
    "select" +
          " NULL as \\SCOPE smallint," +
          " charset_recode (\\COLUMN, 'UTF-8', '_WIDE_') as COLUMN_NAME nvarchar(128)," +
          " dv_to_sql_type(COL_DTP) as DATA_TYPE smallint," +
          " dv_type_title(COL_DTP) as TYPE_NAME varchar(128)," +
          " case COL_PREC when 0 then " + VARCHAR_UNSPEC_SIZE + " else COL_PREC end AS COLUMN_SIZE INTEGER,\n" +
          " COL_PREC as BUFFER_LENGTH integer," +
          " COL_SCALE as DECIMAL_DIGITS smallint," +
          " 1 as PSEUDO_COLUMN smallint " +
          "from DB.DBA.SYS_COLS " +
          "where \\COL_DTP = 128" +
          "  and charset_recode (upper(charset_recode (name_part(\\TABLE,0), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') lik        e charset_recode (upper(charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
          "  and charset_recode (upper(charset_recode (name_part(\\TABLE,1), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') lik        e charset_recode (upper(charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
          "  and charset_recode (upper(charset_recode (name_part(\\TABLE,2), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') lik        e charset_recode (upper(charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') " +
          "order by \\TABLE, \\COL_ID";
   public static final String getBestVersionColsText_case0 =
    "select" +
          " null as \\SCOPE smallint," +
          " \\COLUMN as COLUMN_NAME varchar(128)," +
          " dv_to_sql_type(COL_DTP) as DATA_TYPE smallint," +
          " dv_type_title(COL_DTP) as TYPE_NAME varchar(128)," +
          " case COL_PREC when 0 then " + VARCHAR_UNSPEC_SIZE + " else COL_PREC end AS COLUMN_SIZE INTEGER,\n" +
          " COL_PREC as BUFFER_LENGTH integer," +
          " COL_SCALE as DECIMAL_DIGITS smallint," +
          " 1 as PSEUDO_COLUMN smallint " +
          "from DB.DBA.SYS_COLS " +
          "where \\COL_DTP = 128" +
          "  and name_part(\\TABLE,0) like ?" +
          "  and name_part(\\TABLE,1) like ?" +
          "  and name_part(\\TABLE,2) like ? " +
          "order by \\TABLE, \\COL_ID";
   public static final String getBestVersionColsText_case2 =
    "select" +
          " null as \\SCOPE smallint," +
          " \\COLUMN as COLUMN_NAME varchar(128)," +
          " dv_to_sql_type(COL_DTP) as DATA_TYPE smallint," +
          " dv_type_title(COL_DTP) as TYPE_NAME varchar(128)," +
          " case COL_PREC when 0 then " + VARCHAR_UNSPEC_SIZE + " else COL_PREC end AS COLUMN_SIZE INTEGER,\n" +
          " COL_PREC as BUFFER_LENGTH integer," +
          " COL_SCALE as DECIMAL_DIGITS smallint," +
          " 1 as PSEUDO_COLUMN smallint " +
          "from DB.DBA.SYS_COLS " +
          "where \\COL_DTP = 128" +
          "  and upper(name_part(\\TABLE,0)) like upper(?)" +
          "  and upper(name_part(\\TABLE,1)) like upper(?)" +
          "  and upper(name_part(\\TABLE,2)) like upper(?) " +
          "order by \\TABLE, \\COL_ID";
   public ResultSet getVersionColumns(String catalog, String schema, String table) throws VirtuosoException
   {
      if(catalog == null)
         catalog = "";
      if(schema == null)
         schema = "";
      if(table == null)
         table = "";
      catalog = (catalog.equals("")) ? "%" : catalog;
      schema = (schema.equals("")) ? "%" : schema;
      table = (table.equals("")) ? "%" : table;
      VirtuosoPreparedStatement ps;
      if (connection.utf8_execs)
 ps = (VirtuosoPreparedStatement)connection.prepareStatement(
     (connection.getCase() == 2) ? getWideBestVersionColsText_case2 : getWideBestVersionColsText_case0);
      else
 ps = (VirtuosoPreparedStatement)connection.prepareStatement(
     (connection.getCase() == 2) ? getBestVersionColsText_case2 : getBestVersionColsText_case0);
      ps.setString(1,connection.escapeSQLString (catalog).toParamString());
      ps.setString(2,connection.escapeSQLString (schema).toParamString());
      ps.setString(3,connection.escapeSQLString (table).toParamString());
      ResultSet rs = ps.executeQuery();
      return rs;
   }
   private static final String get_pk_case0 =
       "SELECT " +
         "name_part(v1.KEY_TABLE,0) AS \\TABLE_CAT VARCHAR(128), " +
  "name_part(v1.KEY_TABLE,1) AS \\TABLE_SCHEM VARCHAR(128), " +
  "name_part(v1.KEY_TABLE,2) AS \\TABLE_NAME VARCHAR(128), " +
  "DB.DBA.SYS_COLS.\"COLUMN\" AS \\COLUMN_NAME VARCHAR(128), " +
  "(kp.KP_NTH+1) AS \\KEY_SEQ SMALLINT, " +
  "name_part (v1.KEY_NAME, 2) AS \\PK_NAME VARCHAR(128) " +
       "FROM " +
         "DB.DBA.SYS_KEYS v1, " +
  "DB.DBA.SYS_KEYS v2, " +
  "DB.DBA.SYS_KEY_PARTS kp, " +
  "DB.DBA.SYS_COLS " +
       "WHERE " +
         "name_part(v1.KEY_TABLE,0) LIKE ? " +
  "AND name_part(v1.KEY_TABLE,1) LIKE ? " +
  "AND name_part(v1.KEY_TABLE,2) LIKE ? " +
  "AND __any_grants (v1.KEY_TABLE) " +
  "AND v1.KEY_IS_MAIN = 1 " +
  "AND v1.KEY_MIGRATE_TO is NULL " +
  "AND v1.KEY_SUPER_ID = v2.KEY_ID " +
  "AND kp.KP_KEY_ID = v1.KEY_ID " +
  "AND kp.KP_NTH < v1.KEY_DECL_PARTS " +
  "AND DB.DBA.SYS_COLS.COL_ID = kp.KP_COL " +
  "AND DB.DBA.SYS_COLS.\"COLUMN\" <> '_IDN' " +
       "ORDER BY v1.KEY_TABLE, kp.KP_NTH";
   private static final String get_pk_case2 =
       "SELECT " +
         "name_part(v1.KEY_TABLE,0) AS \\TABLE_CAT VARCHAR(128), " +
  "name_part(v1.KEY_TABLE,1) AS \\TABLE_SCHEM VARCHAR(128), " +
  "name_part(v1.KEY_TABLE,2) AS \\TABLE_NAME VARCHAR(128), " +
  "DB.DBA.SYS_COLS.\"COLUMN\" AS \\COLUMN_NAME VARCHAR(128), " +
  "(kp.KP_NTH+1) AS \\KEY_SEQ SMALLINT, " +
  "name_part (v1.KEY_NAME, 2) AS \\PK_NAME VARCHAR(128) " +
       "FROM " +
         "DB.DBA.SYS_KEYS v1, " +
  "DB.DBA.SYS_KEYS v2, " +
  "DB.DBA.SYS_KEY_PARTS kp, " +
  "DB.DBA.SYS_COLS " +
       "WHERE " +
         "upper (name_part(v1.KEY_TABLE,0)) LIKE upper (?) " +
  "AND upper (name_part(v1.KEY_TABLE,1)) LIKE upper (?) " +
  "AND upper (name_part(v1.KEY_TABLE,2)) LIKE upper (?) " +
  "AND __any_grants (v1.KEY_TABLE) " +
  "AND v1.KEY_IS_MAIN = 1 " +
  "AND v1.KEY_MIGRATE_TO is NULL " +
  "AND v1.KEY_SUPER_ID = v2.KEY_ID " +
  "AND kp.KP_KEY_ID = v1.KEY_ID " +
  "AND kp.KP_NTH < v1.KEY_DECL_PARTS " +
  "AND DB.DBA.SYS_COLS.COL_ID = kp.KP_COL " +
  "AND DB.DBA.SYS_COLS.\"COLUMN\" <> '_IDN' " +
       "ORDER BY v1.KEY_TABLE, kp.KP_NTH";
   private static final String get_wide_pk_case0 =
       "SELECT " +
         "charset_recode (name_part(v1.KEY_TABLE,0), 'UTF-8', '_WIDE_') AS \\TABLE_CAT NVARCHAR(128), " +
  "charset_recode (name_part(v1.KEY_TABLE,1), 'UTF-8', '_WIDE_') AS \\TABLE_SCHEM NVARCHAR(128), " +
  "charset_recode (name_part(v1.KEY_TABLE,2), 'UTF-8', '_WIDE_') AS \\TABLE_NAME NVARCHAR(128), " +
  "charset_recode (DB.DBA.SYS_COLS.\"COLUMN\", 'UTF-8', '_WIDE_') AS \\COLUMN_NAME NVARCHAR(128), " +
  "(kp.KP_NTH+1) AS \\KEY_SEQ SMALLINT, " +
  "charset_recode (name_part (v1.KEY_NAME, 2), 'UTF-8', '_WIDE_') AS \\PK_NAME VARCHAR(128) " +
       "FROM " +
         "DB.DBA.SYS_KEYS v1, " +
  "DB.DBA.SYS_KEYS v2, " +
  "DB.DBA.SYS_KEY_PARTS kp, " +
  "DB.DBA.SYS_COLS " +
       "WHERE " +
         "name_part(v1.KEY_TABLE,0) LIKE ? " +
  "AND name_part(v1.KEY_TABLE,1) LIKE ? " +
  "AND name_part(v1.KEY_TABLE,2) LIKE ? " +
  "AND __any_grants (v1.KEY_TABLE) " +
  "AND v1.KEY_IS_MAIN = 1 " +
  "AND v1.KEY_MIGRATE_TO is NULL " +
  "AND v1.KEY_SUPER_ID = v2.KEY_ID " +
  "AND kp.KP_KEY_ID = v1.KEY_ID " +
  "AND kp.KP_NTH < v1.KEY_DECL_PARTS " +
  "AND DB.DBA.SYS_COLS.COL_ID = kp.KP_COL " +
  "AND DB.DBA.SYS_COLS.\"COLUMN\" <> '_IDN' " +
       "ORDER BY v1.KEY_TABLE, kp.KP_NTH";
   private static final String get_wide_pk_case2 =
       "SELECT " +
         "charset_recode (name_part(v1.KEY_TABLE,0), 'UTF-8', '_WIDE_') AS \\TABLE_CAT NVARCHAR(128), " +
  "charset_recode (name_part(v1.KEY_TABLE,1), 'UTF-8', '_WIDE_') AS \\TABLE_SCHEM NVARCHAR(128), " +
  "charset_recode (name_part(v1.KEY_TABLE,2), 'UTF-8', '_WIDE_') AS \\TABLE_NAME NVARCHAR(128), " +
  "charset_recode (DB.DBA.SYS_COLS.\"COLUMN\", 'UTF-8', '_WIDE_') AS \\COLUMN_NAME NVARCHAR(128), " +
  "(kp.KP_NTH+1) AS \\KEY_SEQ SMALLINT, " +
  "charset_recode (name_part (v1.KEY_NAME, 2), 'UTF-8', '_WIDE_') AS \\PK_NAME VARCHAR(128) " +
       "FROM " +
         "DB.DBA.SYS_KEYS v1, " +
  "DB.DBA.SYS_KEYS v2, " +
  "DB.DBA.SYS_KEY_PARTS kp, " +
  "DB.DBA.SYS_COLS " +
       "WHERE " +
         "charset_recode (upper (charset_recode (name_part(v1.KEY_TABLE,0), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')"+
  " LIKE charset_recode (upper (charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') " +
  "AND charset_recode (upper (charset_recode (name_part(v1.KEY_TABLE,1), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
  " LIKE charset_recode (upper (charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') " +
  "AND charset_recode (upper (charset_recode (name_part(v1.KEY_TABLE,2), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
  " LIKE charset_recode (upper (charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') " +
  "AND __any_grants (v1.KEY_TABLE) " +
  "AND v1.KEY_IS_MAIN = 1 " +
  "AND v1.KEY_MIGRATE_TO is NULL " +
  "AND v1.KEY_SUPER_ID = v2.KEY_ID " +
  "AND kp.KP_KEY_ID = v1.KEY_ID " +
  "AND kp.KP_NTH < v1.KEY_DECL_PARTS " +
  "AND DB.DBA.SYS_COLS.COL_ID = kp.KP_COL " +
  "AND DB.DBA.SYS_COLS.\"COLUMN\" <> '_IDN' " +
       "ORDER BY v1.KEY_TABLE, kp.KP_NTH";
   public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException
   {
      if(catalog == null)
         catalog = "";
      if(schema == null)
         schema = "";
      if(table == null)
         table = "";
      catalog = catalog.equals("") ? "%" : catalog;
      schema = schema.equals("") ? "%" : schema;
      table = table.equals("") ? "%" : table;
      VirtuosoPreparedStatement ps;
      if (connection.utf8_execs)
 ps = (VirtuosoPreparedStatement)
     connection.prepareStatement((connection.getCase() == 2) ? get_wide_pk_case2 : get_wide_pk_case0);
      else
 ps = (VirtuosoPreparedStatement)
     connection.prepareStatement((connection.getCase() == 2) ? get_pk_case2 : get_pk_case0);
      ps.setString(1, connection.escapeSQLString (catalog).toParamString());
      ps.setString(2, connection.escapeSQLString (schema).toParamString());
      ps.setString(3, connection.escapeSQLString (table).toParamString());
      ResultSet rs = ps.executeQuery();
      return rs;
   }
   private static final String imp_keys_case0 =
       "SELECT " +
         "name_part (PK_TABLE, 0) as PKTABLE_CAT varchar (128), " +
  "name_part (PK_TABLE, 1) as PKTABLE_SCHEM varchar (128), " +
  "name_part (PK_TABLE, 2) as PKTABLE_NAME varchar (128), " +
  "PKCOLUMN_NAME, " +
  "name_part (FK_TABLE, 0) as FKTABLE_CAT varchar (128), " +
  "name_part (FK_TABLE, 1) as FKTABLE_SCHEM varchar (128), " +
  "name_part (FK_TABLE, 2) as FKTABLE_NAME varchar (128), " +
  "FKCOLUMN_NAME, " +
  "KEY_SEQ+1 AS KEY_SEQ, " +
  "UPDATE_RULE, " +
  "DELETE_RULE, " +
  "FK_NAME, " +
  "PK_NAME, " +
  "7 AS DEFERRABILITY " +
       "FROM " +
         "DB.DBA.SYS_FOREIGN_KEYS SYS_FOREIGN_KEYS " +
       "WHERE " +
         "name_part (FK_TABLE, 0) LIKE ? " +
  "AND name_part (FK_TABLE, 1) LIKE ? " +
  "AND name_part (FK_TABLE, 2) LIKE ? " +
       "ORDER BY 5,6,7,10";
   private static final String imp_keys_case2 =
       "SELECT " +
         "name_part (PK_TABLE, 0) as PKTABLE_CAT varchar (128), " +
  "name_part (PK_TABLE, 1) as PKTABLE_SCHEM varchar (128), " +
  "name_part (PK_TABLE, 2) as PKTABLE_NAME varchar (128), " +
  "PKCOLUMN_NAME, " +
  "name_part (FK_TABLE, 0) as FKTABLE_CAT varchar (128), " +
  "name_part (FK_TABLE, 1) as FKTABLE_SCHEM varchar (128), " +
  "name_part (FK_TABLE, 2) as FKTABLE_NAME varchar (128), " +
  "FKCOLUMN_NAME, " +
  "KEY_SEQ+1 AS KEY_SEQ, " +
  "UPDATE_RULE, " +
  "DELETE_RULE, " +
  "FK_NAME, " +
  "PK_NAME, " +
  "7 AS DEFERRABILITY " +
       "FROM " +
         "DB.DBA.SYS_FOREIGN_KEYS SYS_FOREIGN_KEYS " +
       "WHERE " +
         "upper (name_part (FK_TABLE, 0)) LIKE upper (?) " +
  "AND upper (name_part (FK_TABLE, 1)) LIKE upper (?) " +
  "AND upper (name_part (FK_TABLE, 2)) LIKE upper (?) " +
       "ORDER BY 5,6,7,10";
   private static final String imp_wide_keys_case0 =
       "SELECT " +
         "charset_recode (name_part (PK_TABLE, 0), 'UTF-8', '_WIDE_') as PKTABLE_CAT nvarchar (128), " +
  "charset_recode (name_part (PK_TABLE, 1), 'UTF-8', '_WIDE_') as PKTABLE_SCHEM nvarchar (128), " +
  "charset_recode (name_part (PK_TABLE, 2), 'UTF-8', '_WIDE_') as PKTABLE_NAME nvarchar (128), " +
  "charset_recode (PKCOLUMN_NAME, 'UTF-8', '_WIDE_') as PKCOLUMN_NAME nvarchar (128), " +
  "charset_recode (name_part (FK_TABLE, 0), 'UTF-8', '_WIDE_') as FKTABLE_CAT nvarchar (128), " +
  "charset_recode (name_part (FK_TABLE, 1), 'UTF-8', '_WIDE_') as FKTABLE_SCHEM nvarchar (128), " +
  "charset_recode (name_part (FK_TABLE, 2), 'UTF-8', '_WIDE_') as FKTABLE_NAME nvarchar (128), " +
  "charset_recode (FKCOLUMN_NAME, 'UTF-8', '_WIDE_') as FKCOLUMN_NAME nvarchar (128), " +
  "KEY_SEQ+1 AS KEY_SEQ, " +
  "UPDATE_RULE, " +
  "DELETE_RULE, " +
  "charset_recode (FK_NAME, 'UTF-8', '_WIDE_') as FK_NAME nvarchar (128), " +
  "charset_recode (PK_NAME, 'UTF-8', '_WIDE_') as PK_NAME nvarchar (128), " +
  "7 AS DEFERRABILITY " +
       "FROM " +
         "DB.DBA.SYS_FOREIGN_KEYS SYS_FOREIGN_KEYS " +
       "WHERE " +
         "name_part (FK_TABLE, 0) LIKE ? " +
  "AND name_part (FK_TABLE, 1) LIKE ? " +
  "AND name_part (FK_TABLE, 2) LIKE ? " +
       "ORDER BY 5,6,7,10";
   private static final String imp_wide_keys_case2 =
       "SELECT " +
         "charset_recode (name_part (PK_TABLE, 0), 'UTF-8', '_WIDE_') as PKTABLE_CAT nvarchar (128), " +
  "charset_recode (name_part (PK_TABLE, 1), 'UTF-8', '_WIDE_') as PKTABLE_SCHEM nvarchar (128), " +
  "charset_recode (name_part (PK_TABLE, 2), 'UTF-8', '_WIDE_') as PKTABLE_NAME nvarchar (128), " +
  "charset_recode (PKCOLUMN_NAME, 'UTF-8', '_WIDE_') as PKCOLUMN_NAME nvarchar (128), " +
  "charset_recode (name_part (FK_TABLE, 0), 'UTF-8', '_WIDE_') as FKTABLE_CAT nvarchar (128), " +
  "charset_recode (name_part (FK_TABLE, 1), 'UTF-8', '_WIDE_') as FKTABLE_SCHEM nvarchar (128), " +
  "charset_recode (name_part (FK_TABLE, 2), 'UTF-8', '_WIDE_') as FKTABLE_NAME nvarchar (128), " +
  "charset_recode (FKCOLUMN_NAME, 'UTF-8', '_WIDE_') as FKCOLUMN_NAME nvarchar (128), " +
  "KEY_SEQ+1 AS KEY_SEQ, " +
  "UPDATE_RULE, " +
  "DELETE_RULE, " +
  "charset_recode (FK_NAME, 'UTF-8', '_WIDE_') as FK_NAME nvarchar (128), " +
  "charset_recode (PK_NAME, 'UTF-8', '_WIDE_') as PK_NAME nvarchar (128), " +
  "7 AS DEFERRABILITY " +
       "FROM " +
         "DB.DBA.SYS_FOREIGN_KEYS SYS_FOREIGN_KEYS " +
       "WHERE " +
         "charset_recode (upper (charset_recode (name_part (FK_TABLE, 0), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
  " LIKE charset_recode (upper (charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') " +
         "AND charset_recode (upper (charset_recode (name_part (FK_TABLE, 1), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
  " LIKE charset_recode (upper (charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') " +
         "AND charset_recode (upper (charset_recode (name_part (FK_TABLE, 2), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
  " LIKE charset_recode (upper (charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') " +
       "ORDER BY 5,6,7,10";
   public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException
   {
      if(catalog == null)
         catalog = "";
      if(schema == null)
         schema = "";
      if(table == null)
         table = "";
      catalog = catalog.equals("") ? "%" : catalog;
      schema = schema.equals("") ? "%" : schema;
      table = table.equals("") ? "%" : table;
      VirtuosoPreparedStatement ps;
      if (connection.utf8_execs)
 ps = (VirtuosoPreparedStatement)
     connection.prepareStatement((connection.getCase() == 2) ? imp_wide_keys_case2 : imp_wide_keys_case0);
      else
 ps = (VirtuosoPreparedStatement)
     connection.prepareStatement((connection.getCase() == 2) ? imp_keys_case2 : imp_keys_case0);
      ps.setString(1, connection.escapeSQLString (catalog).toParamString());
      ps.setString(2, connection.escapeSQLString (schema).toParamString());
      ps.setString(3, connection.escapeSQLString (table).toParamString());
      ResultSet rs = ps.executeQuery();
      return rs;
   }
   private static final String exp_keys_mode0 =
       "SELECT " +
         "name_part (PK_TABLE, 0) as PKTABLE_CAT varchar (128), " +
  "name_part (PK_TABLE, 1) as PKTABLE_SCHEM varchar (128), " +
  "name_part (PK_TABLE, 2) as PKTABLE_NAME varchar (128), " +
  "PKCOLUMN_NAME, " +
  "name_part (FK_TABLE, 0) as FKTABLE_CAT varchar (128), " +
  "name_part (FK_TABLE, 1) as FKTABLE_SCHEM varchar (128), " +
  "name_part (FK_TABLE, 2) as FKTABLE_NAME varchar (128), " +
  "FKCOLUMN_NAME, " +
  "KEY_SEQ+1 AS KEY_SEQ, " +
  "UPDATE_RULE, " +
  "DELETE_RULE, " +
  "FK_NAME, " +
  "PK_NAME, " +
  "7 AS DEFERRABILITY " +
       "FROM DB.DBA.SYS_FOREIGN_KEYS SYS_FOREIGN_KEYS " +
       "WHERE " +
         "name_part (PK_TABLE, 0) LIKE ? " +
  "AND name_part (PK_TABLE, 1) LIKE ? " +
  "AND name_part (PK_TABLE, 2) LIKE ? " +
       "ORDER BY 1,2,3,10";
   private static final String exp_keys_mode2 =
       "SELECT " +
         "name_part (PK_TABLE, 0) as PKTABLE_CAT varchar (128), " +
  "name_part (PK_TABLE, 1) as PKTABLE_SCHEM varchar (128), " +
  "name_part (PK_TABLE, 2) as PKTABLE_NAME varchar (128), " +
  "PKCOLUMN_NAME, " +
  "name_part (FK_TABLE, 0) as FKTABLE_CAT varchar (128), " +
  "name_part (FK_TABLE, 1) as FKTABLE_SCHEM varchar (128), " +
  "name_part (FK_TABLE, 2) as FKTABLE_NAME varchar (128), " +
  "FKCOLUMN_NAME, " +
  "KEY_SEQ+1 AS KEY_SEQ, " +
  "UPDATE_RULE, " +
  "DELETE_RULE, " +
  "FK_NAME, " +
  "PK_NAME, " +
  "7 AS DEFERRABILITY " +
       "FROM DB.DBA.SYS_FOREIGN_KEYS SYS_FOREIGN_KEYS " +
       "WHERE " +
         "upper (name_part (PK_TABLE, 0)) LIKE upper (?) " +
  "AND upper (name_part (PK_TABLE, 1)) LIKE upper (?) " +
  "AND upper (name_part (PK_TABLE, 2)) LIKE upper (?) " +
       "ORDER BY 1,2,3,10";
   private static final String exp_wide_keys_mode0 =
       "SELECT " +
         "charset_recode (name_part (PK_TABLE, 0), 'UTF-8', '_WIDE_') as PKTABLE_CAT nvarchar (128), " +
  "charset_recode (name_part (PK_TABLE, 1), 'UTF-8', '_WIDE_') as PKTABLE_SCHEM nvarchar (128), " +
  "charset_recode (name_part (PK_TABLE, 2), 'UTF-8', '_WIDE_') as PKTABLE_NAME nvarchar (128), " +
  "charset_recode (PKCOLUMN_NAME, 'UTF-8', '_WIDE_') as PKCOLUMN_NAME nvarchar (128), " +
  "charset_recode (name_part (FK_TABLE, 0), 'UTF-8', '_WIDE_') as FKTABLE_CAT nvarchar (128), " +
  "charset_recode (name_part (FK_TABLE, 1), 'UTF-8', '_WIDE_') as FKTABLE_SCHEM nvarchar (128), " +
  "charset_recode (name_part (FK_TABLE, 2), 'UTF-8', '_WIDE_') as FKTABLE_NAME nvarchar (128), " +
  "charset_recode (FKCOLUMN_NAME, 'UTF-8', '_WIDE_') as FKCOLUMN_NAME nvarchar (128), " +
  "KEY_SEQ+1 AS KEY_SEQ, " +
  "UPDATE_RULE, " +
  "DELETE_RULE, " +
  "charset_recode (FK_NAME, 'UTF-8', '_WIDE_') as FK_NAME nvarchar (128), " +
  "charset_recode (PK_NAME, 'UTF-8', '_WIDE_') as PK_NAME nvarchar (128), " +
  "7 AS DEFERRABILITY " +
       "FROM DB.DBA.SYS_FOREIGN_KEYS SYS_FOREIGN_KEYS " +
       "WHERE " +
         "name_part (PK_TABLE, 0) LIKE ? " +
  "AND name_part (PK_TABLE, 1) LIKE ? " +
  "AND name_part (PK_TABLE, 2) LIKE ? " +
       "ORDER BY 1,2,3,10";
   private static final String exp_wide_keys_mode2 =
       "SELECT " +
         "charset_recode (name_part (PK_TABLE, 0), 'UTF-8', '_WIDE_') as PKTABLE_CAT nvarchar (128), " +
  "charset_recode (name_part (PK_TABLE, 1), 'UTF-8', '_WIDE_') as PKTABLE_SCHEM nvarchar (128), " +
  "charset_recode (name_part (PK_TABLE, 2), 'UTF-8', '_WIDE_') as PKTABLE_NAME nvarchar (128), " +
  "charset_recode (PKCOLUMN_NAME, 'UTF-8', '_WIDE_') as PKCOLUMN_NAME nvarchar (128), " +
  "charset_recode (name_part (FK_TABLE, 0), 'UTF-8', '_WIDE_') as FKTABLE_CAT nvarchar (128), " +
  "charset_recode (name_part (FK_TABLE, 1), 'UTF-8', '_WIDE_') as FKTABLE_SCHEM nvarchar (128), " +
  "charset_recode (name_part (FK_TABLE, 2), 'UTF-8', '_WIDE_') as FKTABLE_NAME nvarchar (128), " +
  "charset_recode (FKCOLUMN_NAME, 'UTF-8', '_WIDE_') as FKCOLUMN_NAME nvarchar (128), " +
  "KEY_SEQ+1 AS KEY_SEQ, " +
  "UPDATE_RULE, " +
  "DELETE_RULE, " +
  "charset_recode (FK_NAME, 'UTF-8', '_WIDE_') as FK_NAME nvarchar (128), " +
  "charset_recode (PK_NAME, 'UTF-8', '_WIDE_') as PK_NAME nvarchar (128), " +
  "7 AS DEFERRABILITY " +
       "FROM DB.DBA.SYS_FOREIGN_KEYS SYS_FOREIGN_KEYS " +
       "WHERE " +
         "charset_recode (upper (charset_recode (name_part (PK_TABLE, 0), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
  " LIKE charset_recode (upper (charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') " +
         "AND charset_recode (upper (charset_recode (name_part (PK_TABLE, 1), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
  " LIKE charset_recode (upper (charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') " +
         "AND charset_recode (upper (charset_recode (name_part (PK_TABLE, 2), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
  " LIKE charset_recode (upper (charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') " +
       "ORDER BY 1,2,3,10";
   public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException
   {
      if(catalog == null)
         catalog = "";
      if(schema == null)
         schema = "";
      if(table == null)
         table = "";
      catalog = catalog.equals("") ? "%" : catalog;
      schema = schema.equals("") ? "%" : schema;
      table = table.equals("") ? "%" : table;
      VirtuosoPreparedStatement ps;
      if (connection.utf8_execs)
 ps = (VirtuosoPreparedStatement)
     connection.prepareStatement((connection.getCase() == 2) ? exp_wide_keys_mode2 : exp_wide_keys_mode0);
      else
 ps = (VirtuosoPreparedStatement)
     connection.prepareStatement((connection.getCase() == 2) ? exp_keys_mode2 : exp_keys_mode0);
      ps.setString(1, connection.escapeSQLString (catalog).toParamString());
      ps.setString(2, connection.escapeSQLString (schema).toParamString());
      ps.setString(3, connection.escapeSQLString (table).toParamString());
      ResultSet rs = ps.executeQuery();
      return rs;
   }
    public static final String fk_text_casemode_0 =
    "select" +
    " name_part (PK_TABLE, 0) as PKTABLE_CAT varchar (128)," +
    " name_part (PK_TABLE, 1) as PKTABLE_SCHEM varchar (128)," +
    " name_part (PK_TABLE, 2) as PKTABLE_NAME varchar (128)," +
    " PKCOLUMN_NAME," +
    " name_part (FK_TABLE, 0) as FKTABLE_CAT varchar (128)," +
    " name_part (FK_TABLE, 1) as FKTABLE_SCHEM varchar (128)," +
    " name_part (FK_TABLE, 2) as FKTABLE_NAME varchar (128)," +
    " FKCOLUMN_NAME," +
    " (KEY_SEQ + 1) as KEY_SEQ SMALLINT," +
    " (case UPDATE_RULE when 0 then 3 when 1 then 0 when 3 then 4 end) as UPDATE_RULE smallint," +
    " (case DELETE_RULE when 0 then 3 when 1 then 0 when 3 then 4 end) as DELETE_RULE smallint," +
    " FK_NAME," +
    " PK_NAME, " +
    " NULL as DEFERRABILITY " +
    "from DB.DBA.SYS_FOREIGN_KEYS SYS_FOREIGN_KEYS " +
    "where name_part (PK_TABLE, 0) like ?" +
    "  and name_part (PK_TABLE, 1) like ?" +
    "  and name_part (PK_TABLE, 2) like ?" +
    "  and name_part (FK_TABLE, 0) like ?" +
    "  and name_part (FK_TABLE, 1) like ?" +
    "  and name_part (FK_TABLE, 2) like ? " +
    "order by 1, 2, 3, 5, 6, 7, 9";
    public static final String fk_text_casemode_2 =
    "select" +
    " name_part (PK_TABLE, 0) as PKTABLE_CAT varchar (128)," +
    " name_part (PK_TABLE, 1) as PKTABLE_SCHEM varchar (128)," +
    " name_part (PK_TABLE, 2) as PKTABLE_NAME varchar (128)," +
    " PKCOLUMN_NAME," +
    " name_part (FK_TABLE, 0) as FKTABLE_CAT varchar (128)," +
    " name_part (FK_TABLE, 1) as FKTABLE_SCHEM varchar (128)," +
    " name_part (FK_TABLE, 2) as FKTABLE_NAME varchar (128)," +
    " FKCOLUMN_NAME," +
    " (KEY_SEQ + 1) as KEY_SEQ SMALLINT," +
    " (case UPDATE_RULE when 0 then 3 when 1 then 0 when 3 then 4 end) as UPDATE_RULE smallint," +
    " (case DELETE_RULE when 0 then 3 when 1 then 0 when 3 then 4 end) as DELETE_RULE smallint," +
    " FK_NAME," +
    " PK_NAME, " +
    " NULL as DEFERRABILITY " +
    "from DB.DBA.SYS_FOREIGN_KEYS SYS_FOREIGN_KEYS " +
    "where upper (name_part (PK_TABLE, 0)) like upper (?)" +
    "  and upper (name_part (PK_TABLE, 1)) like upper (?)" +
    "  and upper (name_part (PK_TABLE, 2)) like upper (?)" +
    "  and upper (name_part (FK_TABLE, 0)) like upper (?)" +
    "  and upper (name_part (FK_TABLE, 1)) like upper (?)" +
    "  and upper (name_part (FK_TABLE, 2)) like upper (?) " +
    "order by 1, 2, 3, 5, 6, 7, 9";
    public static final String fk_textw_casemode_0 =
    "select" +
    " charset_recode (name_part (PK_TABLE, 0), 'UTF-8', '_WIDE_') as PKTABLE_CAT nvarchar (128)," +
    " charset_recode (name_part (PK_TABLE, 1), 'UTF-8', '_WIDE_') as PKTABLE_SCHEM nvarchar (128)," +
    " charset_recode (name_part (PK_TABLE, 2), 'UTF-8', '_WIDE_') as PKTABLE_NAME nvarchar (128)," +
    " charset_recode (PKCOLUMN_NAME, 'UTF-8', '_WIDE_') as PKCOLUMN_NAME nvarchar (128)," +
    " charset_recode (name_part (FK_TABLE, 0), 'UTF-8', '_WIDE_') as FKTABLE_CAT nvarchar (128)," +
    " charset_recode (name_part (FK_TABLE, 1), 'UTF-8', '_WIDE_') as FKTABLE_SCHEM nvarchar (128)," +
    " charset_recode (name_part (FK_TABLE, 2), 'UTF-8', '_WIDE_') as FKTABLE_NAME nvarchar (128)," +
    " charset_recode (FKCOLUMN_NAME, 'UTF-8', '_WIDE_') as FKCOLUMN_NAME nvarchar (128)," +
    " (KEY_SEQ + 1) as KEY_SEQ SMALLINT," +
    " (case UPDATE_RULE when 0 then 3 when 1 then 0 when 3 then 4 end) as UPDATE_RULE smallint," +
    " (case DELETE_RULE when 0 then 3 when 1 then 0 when 3 then 4 end) as DELETE_RULE smallint," +
    " charset_recode (FK_NAME, 'UTF-8', '_WIDE_') as FK_NAME nvarchar (128)," +
    " charset_recode (PK_NAME, 'UTF-8', '_WIDE_') as PK_NAME nvarchar (128), " +
    " NULL as DEFERRABILITY " +
    "from DB.DBA.SYS_FOREIGN_KEYS SYS_FOREIGN_KEYS " +
    "where name_part (PK_TABLE, 0) like ?" +
    "  and name_part (PK_TABLE, 1) like ?" +
    "  and name_part (PK_TABLE, 2) like ?" +
    "  and name_part (FK_TABLE, 0) like ?" +
    "  and name_part (FK_TABLE, 1) like ?" +
    "  and name_part (FK_TABLE, 2) like ? " +
    "order by 1, 2, 3, 5, 6, 7, 9";
    public static final String fk_textw_casemode_2 =
    "select" +
    " charset_recode (name_part (PK_TABLE, 0), 'UTF-8', '_WIDE_') as PKTABLE_CAT nvarchar (128)," +
    " charset_recode (name_part (PK_TABLE, 1), 'UTF-8', '_WIDE_') as PKTABLE_SCHEM nvarchar (128)," +
    " charset_recode (name_part (PK_TABLE, 2), 'UTF-8', '_WIDE_') as PKTABLE_NAME nvarchar (128)," +
    " charset_recode (PKCOLUMN_NAME, 'UTF-8', '_WIDE_') as PKCOLUMN_NAME nvarchar (128)," +
    " charset_recode (name_part (FK_TABLE, 0), 'UTF-8', '_WIDE_') as FKTABLE_CAT nvarchar (128)," +
    " charset_recode (name_part (FK_TABLE, 1), 'UTF-8', '_WIDE_') as FKTABLE_SCHEM nvarchar (128)," +
    " charset_recode (name_part (FK_TABLE, 2), 'UTF-8', '_WIDE_') as FKTABLE_NAME nvarchar (128)," +
    " charset_recode (FKCOLUMN_NAME, 'UTF-8', '_WIDE_') as FKCOLUMN_NAME nvarchar (128)," +
    " (KEY_SEQ + 1) as KEY_SEQ SMALLINT," +
    " (case UPDATE_RULE when 0 then 3 when 1 then 0 when 3 then 4 end) as UPDATE_RULE smallint," +
    " (case DELETE_RULE when 0 then 3 when 1 then 0 when 3 then 4 end) as DELETE_RULE smallint," +
    " charset_recode (FK_NAME, 'UTF-8', '_WIDE_') as FK_NAME nvarchar (128)," +
    " charset_recode (PK_NAME, 'UTF-8', '_WIDE_') as PK_NAME nvarchar (128), " +
    " NULL as DEFERRABILITY " +
    "from DB.DBA.SYS_FOREIGN_KEYS SYS_FOREIGN_KEYS " +
    "where charset_recode (upper (charset_recode (name_part (PK_TABLE, 0), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') like charset_recode (upper (charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
    "  and charset_recode (upper (charset_recode (name_part (PK_TABLE, 1), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') like charset_recode (upper (charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
    "  and charset_recode (upper (charset_recode (name_part (PK_TABLE, 2), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') like charset_recode (upper (charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
    "  and charset_recode (upper (charset_recode (name_part (FK_TABLE, 0), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') like charset_recode (upper (charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
    "  and charset_recode (upper (charset_recode (name_part (FK_TABLE, 1), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') like charset_recode (upper (charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
    "  and charset_recode (upper (charset_recode (name_part (FK_TABLE, 2), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') like charset_recode (upper (charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') " +
    "order by 1, 2, 3, 5, 6, 7, 9";
   public ResultSet getCrossReference(String primaryCatalog, String primarySchema, String primaryTable, String foreignCatalog, String foreignSchema, String foreignTable) throws VirtuosoException
   {
      if(primaryCatalog == null)
         primaryCatalog = "";
      if(primarySchema == null)
         primarySchema = "";
      if(primaryTable == null)
         primaryTable = "";
      if(foreignCatalog == null)
         foreignCatalog = "";
      if(foreignSchema == null)
         foreignSchema = "";
      if(foreignTable == null)
         foreignTable = "";
      primaryCatalog = primaryCatalog.equals("") ? "%" : primaryCatalog;
      primarySchema = primarySchema.equals("") ? "%" : primarySchema;
      primaryTable = primaryTable.equals("") ? "%" : primaryTable;
      foreignCatalog = foreignCatalog.equals("") ? "%" : foreignCatalog;
      foreignSchema = foreignSchema.equals("") ? "%" : foreignSchema;
      foreignTable = foreignTable.equals("") ? "%" : foreignTable;
      VirtuosoPreparedStatement ps;
      if (connection.utf8_execs)
 ps = (VirtuosoPreparedStatement)
     connection.prepareStatement((connection.getCase() == 2) ? fk_textw_casemode_2 : fk_textw_casemode_0);
      else
 ps = (VirtuosoPreparedStatement)
     connection.prepareStatement((connection.getCase() == 2) ? fk_text_casemode_2 : fk_text_casemode_0);
      ps.setString(1, connection.escapeSQLString (primaryCatalog).toParamString());
      ps.setString(2, connection.escapeSQLString (primarySchema).toParamString());
      ps.setString(3, connection.escapeSQLString (primaryTable).toParamString());
      ps.setString(4, connection.escapeSQLString (foreignCatalog).toParamString());
      ps.setString(5, connection.escapeSQLString (foreignSchema).toParamString());
      ps.setString(6, connection.escapeSQLString (foreignTable).toParamString());
      ResultSet rs = ps.executeQuery();
      return rs;
   }
   public ResultSet getTypeInfo() throws SQLException
   {
      CallableStatement cs = connection.prepareCall("DB.DBA.gettypeinfojdbc(?)");
      cs.setInt(1, 0);
      ResultSet rs = cs.executeQuery();
      return rs;
   }
    private static final int SQL_INDEX_OBJECT_ID_STR = 8;
    public static final String sql_statistics_text_casemode_0 =
    "select" +
    " name_part(SYS_KEYS.KEY_TABLE,0) AS \\TABLE_CAT VARCHAR(128)," +
    " name_part(SYS_KEYS.KEY_TABLE,1) AS \\TABLE_SCHEM VARCHAR(128)," +
    " name_part(SYS_KEYS.KEY_TABLE,2) AS \\TABLE_NAME VARCHAR(128)," +
    " iszero(SYS_KEYS.KEY_IS_UNIQUE) AS \\NON_UNIQUE SMALLINT," +
    " name_part (SYS_KEYS.KEY_TABLE, 0) AS \\INDEX_QUALIFIER VARCHAR(128)," +
    " name_part (SYS_KEYS.KEY_NAME, 2) AS \\INDEX_NAME VARCHAR(128)," +
    " ((SYS_KEYS.KEY_IS_OBJECT_ID*" + SQL_INDEX_OBJECT_ID_STR + ") + " +
    "(3-(2*iszero(SYS_KEYS.KEY_CLUSTER_ON_ID)))) AS \\TYPE SMALLINT," +
    " (SYS_KEY_PARTS.KP_NTH+1) AS \\ORDINAL_POSITION SMALLINT," +
    " SYS_COLS.\\COLUMN AS \\COLUMN_NAME VARCHAR(128)," +
    " NULL AS \\ASC_OR_DESC CHAR(1)," +
    " NULL AS \\CARDINALITY INTEGER," +
    " NULL AS \\PAGES INTEGER," +
    " NULL AS \\FILTER_CONDITION VARCHAR(128) " +
    "from DB.DBA.SYS_KEYS SYS_KEYS, DB.DBA.SYS_KEY_PARTS SYS_KEY_PARTS," +
    " DB.DBA.SYS_COLS SYS_COLS " +
    "where name_part(SYS_KEYS.KEY_TABLE,0) like ?" +
    "  and __any_grants (SYS_KEYS.KEY_TABLE) " +
    "  and name_part(SYS_KEYS.KEY_TABLE,1) like ?" +
    "  and name_part(SYS_KEYS.KEY_TABLE,2) like ?" +
    "  and SYS_KEYS.KEY_IS_UNIQUE >= ?" +
    "  and SYS_KEYS.KEY_MIGRATE_TO is NULL" +
    "  and SYS_KEY_PARTS.KP_KEY_ID = SYS_KEYS.KEY_ID" +
    "  and SYS_KEY_PARTS.KP_NTH < SYS_KEYS.KEY_DECL_PARTS" +
    "  and SYS_COLS.COL_ID = SYS_KEY_PARTS.KP_COL" +
    "  and SYS_COLS.\\COLUMN <> '_IDN' " +
    "order by SYS_KEYS.KEY_TABLE, SYS_KEYS.KEY_NAME, SYS_KEY_PARTS.KP_NTH";
    public static final String sql_statistics_text_casemode_2 =
    "select" +
    " name_part(SYS_KEYS.KEY_TABLE,0) AS \\TABLE_CAT VARCHAR(128)," +
    " name_part(SYS_KEYS.KEY_TABLE,1) AS \\TABLE_SCHEM VARCHAR(128)," +
    " name_part(SYS_KEYS.KEY_TABLE,2) AS \\TABLE_NAME VARCHAR(128)," +
    " iszero(SYS_KEYS.KEY_IS_UNIQUE) AS \\NON_UNIQUE SMALLINT," +
    " name_part (SYS_KEYS.KEY_TABLE, 0) AS \\INDEX_QUALIFIER VARCHAR(128)," +
    " name_part (SYS_KEYS.KEY_NAME, 2) AS \\INDEX_NAME VARCHAR(128)," +
    " ((SYS_KEYS.KEY_IS_OBJECT_ID*" + SQL_INDEX_OBJECT_ID_STR + ") + " +
    "(3-(2*iszero(SYS_KEYS.KEY_CLUSTER_ON_ID)))) AS \\TYPE SMALLINT," +
    " (SYS_KEY_PARTS.KP_NTH+1) AS \\ORDINAL_POSITION SMALLINT," +
    " SYS_COLS.\\COLUMN AS \\COLUMN_NAME VARCHAR(128)," +
    " NULL AS \\ASC_OR_DESC CHAR(1)," +
    " NULL AS \\CARDINALITY INTEGER," +
    " NULL AS \\PAGES INTEGER," +
    " NULL AS \\FILTER_CONDITION VARCHAR(128) " +
    "from DB.DBA.SYS_KEYS SYS_KEYS, DB.DBA.SYS_KEY_PARTS SYS_KEY_PARTS," +
    " DB.DBA.SYS_COLS SYS_COLS " +
    "where upper(name_part(SYS_KEYS.KEY_TABLE,0)) like upper(?)" +
    "  and __any_grants (SYS_KEYS.KEY_TABLE) " +
    "  and upper(name_part(SYS_KEYS.KEY_TABLE,1)) like upper(?)" +
    "  and upper(name_part(SYS_KEYS.KEY_TABLE,2)) like upper(?)" +
    "  and SYS_KEYS.KEY_IS_UNIQUE >= ?" +
    "  and SYS_KEYS.KEY_MIGRATE_TO is NULL" +
    "  and SYS_KEY_PARTS.KP_KEY_ID = SYS_KEYS.KEY_ID" +
    "  and SYS_KEY_PARTS.KP_NTH < SYS_KEYS.KEY_DECL_PARTS" +
    "  and SYS_COLS.COL_ID = SYS_KEY_PARTS.KP_COL" +
    "  and SYS_COLS.\\COLUMN <> '_IDN' " +
    "order by SYS_KEYS.KEY_TABLE, SYS_KEYS.KEY_NAME, SYS_KEY_PARTS.KP_NTH";
    public static final String sql_statistics_textw_casemode_0 =
    "select" +
    " charset_recode (name_part(SYS_KEYS.KEY_TABLE,0), 'UTF-8', '_WIDE_') AS \\TABLE_CAT NVARCHAR(128)," +
    " charset_recode (name_part(SYS_KEYS.KEY_TABLE,1), 'UTF-8', '_WIDE_') AS \\TABLE_SCHEM NVARCHAR(128)," +
    " charset_recode (name_part(SYS_KEYS.KEY_TABLE,2), 'UTF-8', '_WIDE_') AS \\TABLE_NAME NVARCHAR(128)," +
    " iszero(SYS_KEYS.KEY_IS_UNIQUE) AS \\NON_UNIQUE SMALLINT," +
    " charset_recode (name_part (SYS_KEYS.KEY_TABLE, 0), 'UTF-8', '_WIDE_') AS \\INDEX_QUALIFIER NVARCHAR(128)," +
    " charset_recode (name_part (SYS_KEYS.KEY_NAME, 2), 'UTF-8', '_WIDE_') AS \\INDEX_NAME NVARCHAR(128)," +
    " ((SYS_KEYS.KEY_IS_OBJECT_ID*" + SQL_INDEX_OBJECT_ID_STR + ") + " +
    "(3-(2*iszero(SYS_KEYS.KEY_CLUSTER_ON_ID)))) AS \\TYPE SMALLINT," +
    " (SYS_KEY_PARTS.KP_NTH+1) AS \\ORDINAL_POSITION SMALLINT," +
    " charset_recode (SYS_COLS.\\COLUMN, 'UTF-8', '_WIDE_') AS \\COLUMN_NAME NVARCHAR(128)," +
    " NULL AS \\ASC_OR_DESC CHAR(1)," +
    " NULL AS \\CARDINALITY INTEGER," +
    " NULL AS \\PAGES INTEGER," +
    " NULL AS \\FILTER_CONDITION VARCHAR(128) " +
    "from DB.DBA.SYS_KEYS SYS_KEYS, DB.DBA.SYS_KEY_PARTS SYS_KEY_PARTS," +
    " DB.DBA.SYS_COLS SYS_COLS " +
    "where name_part(SYS_KEYS.KEY_TABLE,0) like ?" +
    "  and __any_grants (SYS_KEYS.KEY_TABLE) " +
    "  and name_part(SYS_KEYS.KEY_TABLE,1) like ?" +
    "  and name_part(SYS_KEYS.KEY_TABLE,2) like ?" +
    "  and SYS_KEYS.KEY_IS_UNIQUE >= ?" +
    "  and SYS_KEYS.KEY_MIGRATE_TO is NULL" +
    "  and SYS_KEY_PARTS.KP_KEY_ID = SYS_KEYS.KEY_ID" +
    "  and SYS_KEY_PARTS.KP_NTH < SYS_KEYS.KEY_DECL_PARTS" +
    "  and SYS_COLS.COL_ID = SYS_KEY_PARTS.KP_COL" +
    "  and SYS_COLS.\\COLUMN <> '_IDN' " +
    "order by SYS_KEYS.KEY_TABLE, SYS_KEYS.KEY_NAME, SYS_KEY_PARTS.KP_NTH";
    public static final String sql_statistics_textw_casemode_2 =
    "select" +
    " charset_recode (name_part(SYS_KEYS.KEY_TABLE,0), 'UTF-8', '_WIDE_') AS TABLE_CAT NVARCHAR(128)," +
    " charset_recode (name_part(SYS_KEYS.KEY_TABLE,1), 'UTF-8', '_WIDE_') AS \\TABLE_SCHEM NVARCHAR(128)," +
    " charset_recode (name_part(SYS_KEYS.KEY_TABLE,2), 'UTF-8', '_WIDE_') AS \\TABLE_NAME NVARCHAR(128)," +
    " iszero(SYS_KEYS.KEY_IS_UNIQUE) AS \\NON_UNIQUE SMALLINT," +
    " charset_recode (name_part (SYS_KEYS.KEY_TABLE, 0), 'UTF-8', '_WIDE_') AS \\INDEX_QUALIFIER NVARCHAR(128)," +
    " charset_recode (name_part (SYS_KEYS.KEY_NAME, 2), 'UTF-8', '_WIDE_') AS \\INDEX_NAME NVARCHAR(128)," +
    " ((SYS_KEYS.KEY_IS_OBJECT_ID*" + SQL_INDEX_OBJECT_ID_STR + ") + " +
    "(3-(2*iszero(SYS_KEYS.KEY_CLUSTER_ON_ID)))) AS \\TYPE SMALLINT," +
    " (SYS_KEY_PARTS.KP_NTH+1) AS \\ORDINAL_POSITION SMALLINT," +
    " charset_recode (SYS_COLS.\\COLUMN, 'UTF-8', '_WIDE_') AS \\COLUMN_NAME NVARCHAR(128)," +
    " NULL AS \\ASC_OR_DESC CHAR(1)," +
    " NULL AS \\CARDINALITY INTEGER," +
    " NULL AS \\PAGES INTEGER," +
    " NULL AS \\FILTER_CONDITION VARCHAR(128) " +
    "from DB.DBA.SYS_KEYS SYS_KEYS, DB.DBA.SYS_KEY_PARTS SYS_KEY_PARTS," +
    " DB.DBA.SYS_COLS SYS_COLS " +
    "where charset_recode (upper(charset_recode (name_part(SYS_KEYS.KEY_TABLE,0), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') like charset_recode (upper(charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
    "  and __any_grants (SYS_KEYS.KEY_TABLE) " +
    "  and charset_recode (upper(charset_recode (name_part(SYS_KEYS.KEY_TABLE,1), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') like charset_recode (upper(charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
    "  and charset_recode (upper(charset_recode (name_part(SYS_KEYS.KEY_TABLE,2), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') like charset_recode (upper(charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
    "  and SYS_KEYS.KEY_IS_UNIQUE >= ?" +
    "  and SYS_KEYS.KEY_MIGRATE_TO is NULL" +
    "  and SYS_KEY_PARTS.KP_KEY_ID = SYS_KEYS.KEY_ID" +
    "  and SYS_KEY_PARTS.KP_NTH < SYS_KEYS.KEY_DECL_PARTS" +
    "  and SYS_COLS.COL_ID = SYS_KEY_PARTS.KP_COL" +
    "  and SYS_COLS.\\COLUMN <> '_IDN' " +
    "order by SYS_KEYS.KEY_TABLE, SYS_KEYS.KEY_NAME, SYS_KEY_PARTS.KP_NTH";
   public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) throws VirtuosoException
   {
      if(catalog == null)
         catalog = "";
      if(schema == null)
         schema = "";
      if(table == null)
         table = "";
      catalog = catalog.equals("") ? "%" : catalog;
      schema = schema.equals("") ? "%" : schema;
      table = table.equals("") ? "%" : table;
      VirtuosoPreparedStatement ps;
      if (connection.utf8_execs)
 ps = (VirtuosoPreparedStatement)
     connection.prepareStatement((connection.getCase() == 2) ? sql_statistics_textw_casemode_2 : sql_statistics_textw_casemode_0);
      else
 ps = (VirtuosoPreparedStatement)
     connection.prepareStatement((connection.getCase() == 2) ? sql_statistics_text_casemode_2 : sql_statistics_text_casemode_0);
      ps.setString(1, connection.escapeSQLString (catalog).toParamString());
      ps.setString(2, connection.escapeSQLString (schema).toParamString());
      ps.setString(3, connection.escapeSQLString (table).toParamString());
      ps.setInt(4, unique ? 0 : 1 );
      ResultSet rs = ps.executeQuery();
      return rs;
   }
   public Connection getConnection() throws VirtuosoException {
 return connection;
   }
   public boolean supportsResultSetType(int type) throws VirtuosoException
   {
      switch(type)
      {
         case VirtuosoResultSet.TYPE_FORWARD_ONLY:
            return true;
         case VirtuosoResultSet.TYPE_SCROLL_INSENSITIVE:
            return true;
         case VirtuosoResultSet.TYPE_SCROLL_SENSITIVE:
            return true;
      }
      ;
      return false;
   }
   public boolean supportsResultSetConcurrency(int type, int concurrency) throws VirtuosoException
   {
      return supportsResultSetType(type);
   }
   public boolean ownUpdatesAreVisible(int type) throws VirtuosoException
   {
      return true;
   }
   public boolean ownDeletesAreVisible(int type) throws VirtuosoException
   {
      return true;
   }
   public boolean ownInsertsAreVisible(int type) throws VirtuosoException
   {
      return true;
   }
   public boolean othersUpdatesAreVisible(int type) throws VirtuosoException
   {
      return true;
   }
   public boolean othersDeletesAreVisible(int type) throws VirtuosoException
   {
      return true;
   }
   public boolean othersInsertsAreVisible(int type) throws VirtuosoException
   {
      return true;
   }
   public boolean updatesAreDetected(int type) throws VirtuosoException
   {
      return true;
   }
   public boolean deletesAreDetected(int type) throws VirtuosoException
   {
      return true;
   }
   public boolean insertsAreDetected(int type) throws VirtuosoException
   {
      return true;
   }
   public boolean supportsBatchUpdates() throws VirtuosoException
   {
      return true;
   }
   public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) throws VirtuosoException
   {
     String [] col_names = {
      "TYPE_CAT",
      "TYPE_SCHEM",
      "TYPE_NAME",
      "CLASS_NAME",
      "DATA_TYPE",
      "REMARKS",
      "BASE_TYPE" };
     int [] col_dtps = {
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_SHORT_INT };
     return new VirtuosoResultSet (connection, col_names, col_dtps);
   }
   public boolean supportsSavepoints() throws SQLException
     {
       return false;
     }
   public boolean supportsNamedParameters() throws SQLException
     {
       return false;
     }
   public boolean supportsMultipleOpenResults() throws SQLException
     {
       return false;
     }
   public boolean supportsGetGeneratedKeys() throws SQLException
     {
       return false;
     }
   public ResultSet getSuperTypes(String catalog, String schemaPattern,
       String typeNamePattern) throws SQLException
     {
     String [] col_names = {
      "TYPE_CAT",
      "TYPE_SCHEM",
      "TYPE_NAME",
      "SUPERTYPE_CAT",
      "SUPERTYPE_SCHEM",
      "SUPERTYPE_NAME"
      };
     int [] col_dtps = {
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_STRING
       };
     return new VirtuosoResultSet (connection, col_names, col_dtps);
     }
   public ResultSet getSuperTables(String catalog, String schemaPattern,
       String tableNamePattern) throws SQLException
     {
     String [] col_names = {
      "TYPE_CAT",
      "TYPE_SCHEM",
      "TYPE_NAME",
      "SUPERTABLE_NAME"
      };
     int [] col_dtps = {
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_STRING
       };
     return new VirtuosoResultSet (connection, col_names, col_dtps);
     }
   public ResultSet getAttributes(String catalog, String schemaPattern,
       String typeNamePattern, String attributeNamePattern) throws SQLException
     {
     String [] col_names = {
      "TYPE_CAT",
      "TYPE_SCHEM",
      "TYPE_NAME",
      "ATTR_NAME",
      "DATA_TYPE",
      "ATTR_TYPE_NAME",
      "ATTR_SIZE",
      "DECIMAL_DIGITS",
      "NUM_PREC_RADIX",
      "NULLABLE",
      "REMARKS",
      "ATTR_DEF",
      "SQL_DATA_TYPE",
      "SQL_DATETIME_SUB",
      "CHAR_OCTET_LENGTH",
      "ORDINAL_POSITION",
      "IS_NULLABLE",
      "SCOPE_CATALOG",
      "SCOPE_SCHEMA",
      "SCOPE_TABLE",
      "SOURCE_DATA_TYPE"
      };
     int [] col_dtps = {
      VirtuosoTypes.DV_STRING,
      VirtuosoTypes.DV_STRING,
      VirtuosoTypes.DV_STRING,
      VirtuosoTypes.DV_STRING,
      VirtuosoTypes.DV_SHORT_INT,
      VirtuosoTypes.DV_STRING,
      VirtuosoTypes.DV_LONG_INT,
      VirtuosoTypes.DV_LONG_INT,
      VirtuosoTypes.DV_LONG_INT,
      VirtuosoTypes.DV_LONG_INT,
      VirtuosoTypes.DV_STRING,
      VirtuosoTypes.DV_STRING,
      VirtuosoTypes.DV_LONG_INT,
      VirtuosoTypes.DV_LONG_INT,
      VirtuosoTypes.DV_LONG_INT,
      VirtuosoTypes.DV_LONG_INT,
      VirtuosoTypes.DV_STRING,
      VirtuosoTypes.DV_STRING,
      VirtuosoTypes.DV_STRING,
      VirtuosoTypes.DV_STRING,
      VirtuosoTypes.DV_SHORT_INT
       };
     return new VirtuosoResultSet (connection, col_names, col_dtps);
     }
   public boolean supportsResultSetHoldability(int holdability) throws SQLException
     {
       return (holdability == ResultSet.CLOSE_CURSORS_AT_COMMIT);
     }
   public int getResultSetHoldability() throws SQLException
     {
       return ResultSet.CLOSE_CURSORS_AT_COMMIT;
     }
   public int getDatabaseMajorVersion() throws SQLException
     {
       return 3;
     }
   public int getDatabaseMinorVersion() throws SQLException
     {
       return 0;
     }
   public int getJDBCMajorVersion() throws SQLException
     {
       return 3;
     }
   public int getJDBCMinorVersion() throws SQLException
     {
       return 0;
     }
   public int getSQLStateType() throws SQLException
     {
       return sqlStateXOpen;
     }
   public boolean locatorsUpdateCopy() throws SQLException
     {
       return false;
     }
   public boolean supportsStatementPooling() throws SQLException
     {
       return true;
     }
  public RowIdLifetime getRowIdLifetime() throws SQLException
  {
    return RowIdLifetime.ROWID_UNSUPPORTED;
  }
   private static final String getWideSchemasCaseMode0 =
       "SELECT DISTINCT " +
         "charset_recode (name_part(\\KEY_TABLE,1), 'UTF-8', '_WIDE_') AS \\TABLE_SCHEM NVARCHAR(128)," +
  "charset_recode (name_part(\\KEY_TABLE,0), 'UTF-8', '_WIDE_') AS \\TABLE_CAT NVARCHAR(128) " +
       "FROM DB.DBA.SYS_KEYS " +
       "WHERE " +
  "name_part(\\KEY_TABLE,0) LIKE ? AND " +
  "name_part(\\KEY_TABLE,1) LIKE ? " +
       "ORDER BY 1, 2";
   private static final String getWideSchemasCaseMode2 =
       "SELECT DISTINCT " +
         "charset_recode (name_part(\\KEY_TABLE,1), 'UTF-8', '_WIDE_') AS \\TABLE_SCHEM NVARCHAR(128)," +
  "charset_recode (name_part(\\KEY_TABLE,0), 'UTF-8', '_WIDE_') AS \\TABLE_CAT NVARCHAR(128) " +
       "FROM DB.DBA.SYS_KEYS " +
       "WHERE " +
  "charset_recode (UPPER(charset_recode (name_part(\\KEY_TABLE,0), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
  " LIKE charset_recode (UPPER(charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') AND " +
  "charset_recode (UPPER(charset_recode (name_part(\\KEY_TABLE,1), 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8')" +
  " LIKE charset_recode (UPPER(charset_recode (?, 'UTF-8', '_WIDE_')), '_WIDE_', 'UTF-8') " +
       "ORDER BY 1, 2";
   private static final String getSchemasCaseMode0 =
       "SELECT DISTINCT " +
         "name_part(\\KEY_TABLE,1) AS \\TABLE_SCHEM VARCHAR(128)," +
  "name_part(\\KEY_TABLE,0) AS \\TABLE_CAT VARCHAR(128) " +
       "FROM DB.DBA.SYS_KEYS " +
       "WHERE " +
  "name_part(\\KEY_TABLE,0) LIKE ? AND " +
  "name_part(\\KEY_TABLE,1) LIKE ? " +
       "ORDER BY 1, 2";
   private static final String getSchemasCaseMode2 =
       "SELECT DISTINCT " +
         "name_part(\\KEY_TABLE,1) AS \\TABLE_SCHEM VARCHAR(128)," +
  "name_part(\\KEY_TABLE,0) AS \\TABLE_CAT VARCHAR(128) " +
       "FROM DB.DBA.SYS_KEYS " +
       "WHERE " +
  "UPPER(name_part(\\KEY_TABLE,0)) LIKE UPPER(?) AND " +
  "UPPER(name_part(\\KEY_TABLE,1)) LIKE UPPER(?) " +
       "ORDER BY 1, 2";
  public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException
  {
      if (catalog == null)
 catalog = "%";
      if (schemaPattern == null)
 schemaPattern = "%";
      VirtuosoPreparedStatement ps;
      if (connection.utf8_execs)
 ps = (VirtuosoPreparedStatement) connection.prepareStatement(
     (connection.getCase() == 2) ?
     getWideSchemasCaseMode2 :
     getWideSchemasCaseMode0);
      else
 ps = (VirtuosoPreparedStatement) connection.prepareStatement(
     (connection.getCase() == 2) ?
     getSchemasCaseMode2 :
     getSchemasCaseMode0);
      ps.setString(1,connection.escapeSQLString(catalog).toParamString());
      ps.setString(2,connection.escapeSQLString(schemaPattern).toParamString());
      ResultSet rs = ps.executeQuery();
      return rs;
  }
  public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException
  {
    return true;
  }
  public boolean autoCommitFailureClosesAllResultSets() throws SQLException
  {
    return false;
  }
  public ResultSet getClientInfoProperties() throws SQLException
  {
     String [] col_names = {
      "NAME",
      "MAX_LEN",
      "DEFAULT_VALUE",
      "DESCRIPTION"
      };
     int [] col_dtps = {
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_LONG_INT,
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_STRING
       };
     return new VirtuosoResultSet (connection, col_names, col_dtps);
  }
  public ResultSet getFunctions(String catalog, String schemaPattern,
       String functionNamePattern) throws SQLException
  {
     String [] col_names = {
      "FUNCTION_CAT",
      "FUNCTION_SCHEM",
      "FUNCTION_NAME",
      "REMARKS",
      "FUNCTION_TYPE",
      "SPECIFIC_NAME"
      };
     int [] col_dtps = {
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_STRING
       };
     return new VirtuosoResultSet (connection, col_names, col_dtps);
  }
  public ResultSet getFunctionColumns(String catalog,
      String schemaPattern,
      String functionNamePattern,
      String columnNamePattern) throws SQLException
  {
     String [] col_names = {
       "FUNCTION_CAT",
       "FUNCTION_SCHEM",
       "FUNCTION_NAME",
       "COLUMN_NAME",
       "COLUMN_TYPE",
       "DATA_TYPE",
       "TYPE_NAME",
       "PRECISION",
       "LENGTH",
       "SCALE",
       "RADIX",
       "NULLABLE",
       "REMARKS",
       "CHAR_OCTET_LENGTH",
       "ORDINAL_POSITION",
       "IS_NULLABLE",
       "SPECIFIC_NAME"
      };
     int [] col_dtps = {
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_SHORT_INT,
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_LONG_INT,
       VirtuosoTypes.DV_LONG_INT,
       VirtuosoTypes.DV_SHORT_INT,
       VirtuosoTypes.DV_SHORT_INT,
       VirtuosoTypes.DV_SHORT_INT,
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_LONG_INT,
       VirtuosoTypes.DV_LONG_INT,
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_STRING
       };
     return new VirtuosoResultSet (connection, col_names, col_dtps);
  }
  public <T> T unwrap(java.lang.Class<T> iface) throws java.sql.SQLException
  {
    try {
      return iface.cast(this);
    } catch (ClassCastException cce) {
      throw new VirtuosoException ("Unable to unwrap to "+iface.toString(), "22023", VirtuosoException.BADPARAM);
    }
  }
  public boolean isWrapperFor(java.lang.Class<?> iface) throws java.sql.SQLException
  {
    return iface.isInstance(this);
  }
  public ResultSet getPseudoColumns(String catalog,
                         String schemaPattern,
                         String tableNamePattern,
                         String columnNamePattern) throws SQLException
  {
     String [] col_names = {
      "TABLE_CAT",
      "TABLE_SCHEM",
      "TABLE_NAME",
      "COLUMN_NAME",
      "DATA_TYPE",
      "COLUMN_SIZE",
      "DECIMAL_DIGITS",
      "NUM_PREC_RADIX",
      "COLUMN_USAGE",
      "REMARKS",
      "CHAR_OCTET_LENGTH",
      "IS_NULLABLE"
      };
     int [] col_dtps = {
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_LONG_INT,
       VirtuosoTypes.DV_LONG_INT,
       VirtuosoTypes.DV_LONG_INT,
       VirtuosoTypes.DV_LONG_INT,
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_STRING,
       VirtuosoTypes.DV_LONG_INT,
       VirtuosoTypes.DV_STRING
       };
     return new VirtuosoResultSet (connection, col_names, col_dtps);
  }
  public boolean generatedKeyAlwaysReturned() throws SQLException
  {
    return false;
  }
}
