SET PASSWORD FOR root@localhost = PASSWORD( 'numina' );
DELETE FROM mysql.user WHERE password = '';
FLUSH PRIVILEGES;
CREATE DATABASE rds;
GRANT RELOAD ON *.* TO rds IDENTIFIED BY 'rds';
GRANT ALL ON rds.* TO rds;
GRANT SELECT ON rds.* TO view IDENTIFIED BY 'view';
