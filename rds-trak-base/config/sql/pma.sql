# This sql file creates 'pma' user with password 'pma' in MySQL (that must
# be referred as a controluser in phpMyAdmin's file config.inc.php).
# The sql file works in MySQL 5+ and must be executed as a 'root' MySQL user.

GRANT USAGE ON mysql.* TO 'pma'@'%' IDENTIFIED BY 'pma';
GRANT SELECT (
    Host, User, Select_priv, Insert_priv, Update_priv, Delete_priv,
    Create_priv, Drop_priv, Reload_priv, Shutdown_priv, Process_priv,
    File_priv, Grant_priv, References_priv, Index_priv, Alter_priv,
    Show_db_priv, Super_priv, Create_tmp_table_priv, Lock_tables_priv,
    Execute_priv, Repl_slave_priv, Repl_client_priv
    ) ON mysql.user TO 'pma'@'%';
GRANT SELECT ON mysql.db TO 'pma'@'%';
GRANT SELECT ON mysql.host TO 'pma'@'%';
GRANT SELECT (Host, Db, User, Table_name, Table_priv, Column_priv)
    ON mysql.tables_priv TO 'pma'@'%';


