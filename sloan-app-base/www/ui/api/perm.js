var connect = require('../api/connect') ;
var _       = require('lodash') ;

function
getPerms(user,callback)
  {
  var sql = "SELECT DISTINCT p.perm "
          + "FROM _user AS u "
          + "JOIN _userRole as ur "
          + "ON u.user=ur.user "
          + "JOIN _role AS r "
          + "ON ur.role=r.role "
          + "JOIN _rolePerm AS rp "
          + "ON r.role=rp.role "
          + "JOIN _perm AS p "
          + "ON rp.perm=p.perm "
          + "WHERE u.user='" + user + "' "
          + "UNION DISTINCT "
          + "SELECT perm "
          + "FROM _perm "
          + "WHERE enforced='no' " ;
  connect.reader.query(sql,function(err,rows,fields)
    {
    callback(err,_.map(rows,'perm')) ;
    }) ;
  }

module.exports.getPerms = getPerms ;
