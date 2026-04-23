package fengliu.peca.util.sql;

public class SqlUtil {

    /**
     * Sanitize a string value for safe use in SQL queries by escaping single quotes.
     * This is a basic mitigation for SQL injection.
     * Note: A full migration to PreparedStatements would be more robust.
     *
     * @param value the string to sanitize
     * @return the sanitized string with single quotes escaped
     */
    public static String sanitize(String value) {
        return value.replace("'", "''");
    }

    public static class BuildSqlHelper {

        protected String sql;
        protected boolean whereIn = false;
        protected boolean firstIn = true;

        public BuildSqlHelper(String baseSql) {
            this.sql = baseSql + " ";
        }

        public BuildSqlHelper where() {
            if (whereIn) {
                return this;
            }

            this.whereIn = true;
            this.sql += "WHERE ";
            return this;
        }

        interface Add {
            void add();
        }

        private BuildSqlHelper add(Add add, String sql) {
            if (!this.whereIn) {
                this.where();
                this.sql += sql + " ";
                this.whereIn = true;
            } else {
                add.add();
            }

            return this;
        }

        public BuildSqlHelper like(String t, String sql) {
            return this.and(t + " LIKE " + sql);
        }

        public BuildSqlHelper and(String sql) {
            return this.add(() -> this.sql += "AND " + sql + " ", sql);
        }

        public BuildSqlHelper or(String sql) {
            return this.add(() -> this.sql += "OR " + sql + " ", sql);
        }

        public String build() {
            return this.sql;
        }
    }
}
