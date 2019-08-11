package club.vtuka.vicWeb.helper;

import club.vtuka.vicWeb.util.CollectionUtil;
import club.vtuka.vicWeb.util.PropsUtil;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class DataBaseHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataBaseHelper.class);

    private static final QueryRunner QUERY_RUNNER;

    private static final ThreadLocal<Connection> CONNECTION_HOLDER;

    private static final BasicDataSource DATA_SOURCE;

    static {
        QUERY_RUNNER = new QueryRunner();

        CONNECTION_HOLDER = new ThreadLocal<Connection>();

        Properties config = PropsUtil.loadProps("config.properties");
        String driver = config.getProperty("jdbc.driver");
        String url = config.getProperty("jdbc.url");
        String username = config.getProperty("jdbc.username");
        String password = config.getProperty("jdbc.password");

        /**
         * 利用dbcp2实现池化数据库
         */
        DATA_SOURCE = new BasicDataSource();
        DATA_SOURCE.setDriverClassName(driver);
        DATA_SOURCE.setUrl(url);
        DATA_SOURCE.setUsername(username);
        DATA_SOURCE.setPassword(password);
    }

    private static Connection getConnection(){

        Connection connection = CONNECTION_HOLDER.get();
        if(connection == null){
            try {
                connection = DATA_SOURCE.getConnection();
            } catch (SQLException e) {
                LOGGER.error("get connection failure",e);
                throw new RuntimeException(e);
            } finally {
                CONNECTION_HOLDER.set(connection);
            }
        }
        return connection;
    }

    /*使用dbcp2来管理数据库连接，无需手动关闭
    public static void closeConnection(){
        Connection connection = CONNECTION_HOLDER.get();
        if(connection != null){
            try {
                connection.close();
            } catch (SQLException e) {
                LOGGER.error("close connection failure",e);
                throw new RuntimeException(e);
            } finally {
                CONNECTION_HOLDER.remove();
            }
        }
    }
    */

    public static <T> List<T> queryEntityList(Class<T> entity, String sql,Object... params){
        List<T> entityList;
        try {
            Connection connection = getConnection();
            entityList = QUERY_RUNNER.query(connection, sql, new BeanListHandler<T>(entity), params);
        } catch (SQLException e) {
            LOGGER.error("query entity list failure",e);
            throw new RuntimeException(e);
        }
        return entityList;
    }

    public static <T> T queryEntity(Class<T> entityClass, String sql, Object ... params){
        T entity;
        try {
            Connection connection = getConnection();
            entity = QUERY_RUNNER.query(connection,sql,new BeanHandler<T>(entityClass),params);
        } catch (SQLException e) {
            LOGGER.error("query entity failure",e);
            throw new RuntimeException(e);
        }
        return entity;
    }

    public static List<Map<String,Object>> executeQuery(String sql, Object ... params){
        List<Map<String, Object>> mapList;
        try {
            Connection connection = getConnection();
            mapList = QUERY_RUNNER.query(connection, sql, new MapListHandler(), params);
        } catch (SQLException e) {
            LOGGER.error("query entity failure",e);
            throw new RuntimeException(e);
        }
        return mapList;
    }

    /**
     * 依靠QueryRunner的update实现insert、update、delete
     * @param sql
     * @param params
     * @return
     */
    public static int executeUpdate(String sql, Object ... params){
        int result;
        try {
            Connection connection = getConnection();
            result = QUERY_RUNNER.update(connection, sql, params);
        } catch (SQLException e) {
            LOGGER.error("update entity failure",e);
            throw new RuntimeException(e);
        }
        return result;
    }

    public static <T> boolean insertEntity(Class<T> entityClass, Map<String,Object> fieldMap){
        if(CollectionUtil.isEmpty(fieldMap)){
            LOGGER.error("can not insert entity: fieldMap is empty");
            return false;
        }

        String insertSql = "insert into " + getTableName(entityClass);
        StringBuilder column = new StringBuilder("(");
        StringBuilder value = new StringBuilder("(");
        for(Map.Entry<String,Object> field:fieldMap.entrySet()){
            column.append(field.getKey()).append(",");
            value.append("?,");
        }
        column.replace(column.lastIndexOf(","),column.length(),")");
        value.replace(value.lastIndexOf(","),value.length(),")");
        insertSql += column + " values " + value;
        Object[] params = fieldMap.values().toArray();
        return executeUpdate(insertSql,params) == 1;
    }

    public static <T> boolean updateEntity(Class<T> entityClass, long id,Map<String,Object> fieldMap){
        if(CollectionUtil.isEmpty(fieldMap)){
            LOGGER.error("can not update entity: fieldMap is empty");
            return false;
        }

        String updateSql = "update " + getTableName(entityClass) + " set ";
        StringBuilder value = new StringBuilder();
        for(Map.Entry<String,Object> field:fieldMap.entrySet()){
            value.append(field.getKey()).append("=?, ");
        }
        updateSql += value.substring(0,value.lastIndexOf(",")) + " where id = ?";

        List<Object> paramList = new ArrayList<Object>();
        paramList.addAll(fieldMap.values());
        paramList.add(id);
        Object[] params = paramList.toArray();
        return executeUpdate(updateSql,params) == 1;
    }

    public static <T> boolean deleteEntity(Class<T> entityClass, long id){
        String deleteSql = "delete from " + getTableName(entityClass) + " where id = ?";
        return executeUpdate(deleteSql,id) == 1;
    }

    public static void executeSqlFile(String file){
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String sql;
        try{
            while(StringUtils.isNotEmpty(sql = reader.readLine())){
                DataBaseHelper.executeUpdate(sql);
            }
        }catch(Exception e){
            LOGGER.error("execute sql file failure",e);
            throw new RuntimeException(e);
        }

    }

    private static String getTableName(Class<?> entityClass){
        return entityClass.getSimpleName();
    }
}
