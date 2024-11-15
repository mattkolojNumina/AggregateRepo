var mysql   = require('mysql') ;

var reader   = mysql.createPool({
  host:            'db',
  user:            'rds',
  password:        'rds',
  database:        'rds',
  port:            '3306',
  connectionLimit: 10
}) ;

var writer   = mysql.createPool({
  host:            'db',
  user:            'rds',
  password:        'rds',
  database:        'rds',
  port:            '3306',
  connectionLimit: 10
}) ;

module.exports.reader = reader ;
module.exports.writer = writer ;
