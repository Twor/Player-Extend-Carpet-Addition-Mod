package fengliu.peca.util.sql;

import fengliu.peca.PecaMod;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import net.minecraft.network.chat.Component;

/**
 * sql 连接
 */
public interface ISqlConnection {
    /**
     * 获取数据库地址
     *
     * @return 数据库地址
     */
    String getDBUrl();

    /**
     * 获取使用表名
     *
     * @return 表名
     */
    String getTableName();

    /**
     * 获取创建使用表 sql
     *
     * @return sql
     */
    String getCreateTableSql();

    interface Job {
        Object run(Statement statement) throws Exception;
    }

    /**
     * 以该配置执行 sql 语句
     *
     * @param job sql
     * @return 结果
     */
    default Object runSql(Job job) {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection(
                this.getDBUrl()
            );
            Statement statement = connection.createStatement();
            Object data = job.run(statement);
            statement.close();
            connection.close();
            return data;
        } catch (Exception e) {
            PecaMod.LOGGER.error(String.valueOf(e));
        }
        return false;
    }

    /**
     * 创建表
     *
     * @return 成功 true
     */
    default boolean createTable() {
        return (boolean) this.runSql(statement -> {
            statement.execute(
                this.getCreateTableSql().replace(
                    "CREATE TABLE",
                    "CREATE TABLE IF NOT EXISTS"
                )
            );
            PecaMod.LOGGER.info(
                Component.translatable(
                    String.format(
                        "peca.info.sql.not.exist.table.%s",
                        this.getTableName()
                    )
                ).getString()
            );
            return true;
        });
    }

    /**
     * 以该配置执行 sql 语句
     *
     * @param job sql
     * @return 结果
     */
    default Object executeSql(Job job) {
        if (!createTable()) {
            PecaMod.LOGGER.error(
                Component.translatable(
                    String.format(
                        "peca.info.sql.error.exist.table.%s",
                        this.getTableName()
                    )
                ).getString()
            );
            return false;
        }
        return runSql(job);
    }
}
