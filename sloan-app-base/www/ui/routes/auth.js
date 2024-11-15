var express = require('express');
var connect = require('../api/connect') ;
var perm    = require('../api/perm') ;
var secret  = require('../api/secret') ;
var jwt     = require('jsonwebtoken') ;
var _       = require('lodash') ;
var router  = express.Router();

router.post('/',     auth) ;
router.post('/perms',perms) ;

function 
auth(req,res,next) 
  {
  var user     = req.body.user   ;
  var password = req.body.password  ;

  if(user && password)
    {
    connect.reader.query("SELECT user, name FROM _user "
                        +"WHERE user='" + user + "' "
                        +"AND   password=PASSWORD('" + password + "') ",
                        function(err,rows,fields) 
      {
      if(!err)
        {
        if(rows.length>0)
          {
          var name = rows[0].name ;
          var token = jwt.sign({user: user},
                               secret.get(),
                               {expiresIn: (7*24*60*60)}) ;
          perm.getPerms(user,function(err,perms)
            {
            if(!err)
              {
              res.json({loggedIn: true,
                        failed: false,
                        user: user,
                        token: token,
                        perms: perms,
                        name: name}) ;
              }
            else
              return next(err) ;
            }) ;
          }
        else
          {
          perm.getPerms('guest',function(err,perms)
            {
            if(!err)
              {
              var failed = (user != undefined) && (user != 'guest') ;
              res.json({loggedIn: false,
                        failed: failed,
                        user: 'guest',
                        token: null,
                        perms: perms,
                        name: 'Guest'}) ;
              }
            else
              return next(err) ;
            }) ;
          }
        }
      else 
        return next(err) ;
      }) ;
    }
  else
    return next(err) ; 
  } ;

function 
perms(req,res,next) 
  {
  var user='guest' ;
  var token = req.headers.token ;
  if(token)
    {
    var decode = jwt.verify(token,secret.get()) ;
    if(decode.user)
      user = decode.user ;
    }

  perm.getPerms(user,function(err,perms)
    {
    if(!err)
      res.send(perms) ;
    else
      return next(err) ;
    }) ;
  } ;

module.exports = router;
