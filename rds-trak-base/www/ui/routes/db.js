var express = require('express');
var connect = require('../api/connect');
var perm    = require('../api/perm');
var secret  = require('../api/secret');
var jwt     = require('jsonwebtoken');
var _       = require('lodash');
var router  = express.Router();

router.post('/',db);

function 
db(req,res,next) 
{
  var topic      = req.body.topic;
  var action     = req.body.action;
  var params     = req.body.params;
  var admlog     = req.body.log;
  var hasRealLog = !!admlog;
  var token      = req.headers.token;
  
  var logAllWrites = true; // false: logs only actions with log parameter
  var sendTraceMessages = false;

  if(topic && action){
    user = 'guest';
    // check for an authenticated user
    if(token){
      try {
        decode = jwt.verify(token,secret.get());
      } catch(error){
        console.log('bad token');
        var err = new Error('bad token');
        err.status = 401;
//        return res.json({tokenError: true, error: err});
        return next(err);
      }
      user = decode.user;
    }
    
    // send trace message
    if(sendTraceMessages){
      if(topic!='heart'){
        console.log('user: '+user+', topic: '+topic+', action: '+action);
      } else if(action=='beat') {
//        console.log(Math.floor(Date.now()/5000)%2?' <3':'<3');
      }
    }
    
    // get user's (or guest's) permissions
    perm.getPerms(user,
      function(err,perms)
      {
        if(!err){
          // find requested query template in _db
          connect.reader.query("SELECT * FROM _db "
                               +"WHERE topic='" + topic + "' "
                               +"AND action='" + action + "' ",
            function(err,rows0,fields) 
            {
              if(!err){
                if(rows0.length>0){
                  // choose writable or read-only database connexion
                  var server = (rows0[0].server=='writer')? connect.writer:connect.reader;
                  // get main query
                  var query = rows0[0].query || '';
                  // prepare log query
                  if(!hasRealLog)
                    admlog = topic+'/'+action;
                  var logQuery = "INSERT INTO _adminLog " +
                                 "SET user='"+user+"', " +
                                 "description='"+admlog+"'";
                  // get required permission
                  var need = rows0[0].perm || '';
                  // check required permission against list of available permissions
                  if((need=='') || (_.indexOf(perms,'root')>=0) || (_.indexOf(perms,need)>=0)){
                    var params = req.body.params || '{}';
                    // customize query template with user-provided parameters
                    for(var param in params){
                      query = query.replace(new RegExp('{{'+param+'}}','g'),
                                            server.escape(params[param]));
                      query = query.replace(new RegExp('##'+param+'##','g'),
                                            params[param]);
                    }
                    // send query
                    server.query(query,
                      function(err,rows1,fields)
                      {
                        if(!err){
                          if(hasRealLog || (rows0[0].server=='writer' && logAllWrites && admlog!='heart/beat'))
                            // send log query
                            connect.writer.query(logQuery,
                              function(e,r,f)
                              {
                                res.send(rows1);
                              })
                          else
                            res.send(rows1);
                        } else {
                          return next(err);
                        }
                      });
                  } else {
                    var err = new Error('requires perm '+need);
                    err.status = 401;
                    return next(err);
                  }
                } else {
                  var err = new Error('unknown topic '+topic+' action '+action);
                  err.status = 404;
                  return next(err);
                }
              } else
                return next(err);
            });
        } else {
          return next(err);
        }
      });
  } else {
    var err = new Error('missing topic or action');
    err.status = 404;
    return next(err);
  }
};

module.exports = router;
