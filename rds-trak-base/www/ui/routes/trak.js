var express = require('express');
var trak    = require('../rds/trak') ;
var perm    = require('../api/perm') ;
var secret  = require('../api/secret') ;
var jwt     = require('jsonwebtoken') ;
var _       = require('lodash') ;
var router  = express.Router();

router.post('/',do_trak) ;

var trak_count = -1 ;
var dp_handles = {} ;
var rp_handles = {} ;

function
check_trak()
  {
  var count = trak.dpCounterGet(0) ;
  if(count!=trak_count)
    {
    dp_handles = {} ;
    rp_handles = {} ;
    trak_count = count ;
    }
  }

function
dp_handle(name)
  {
  check_trak() ;

  if(dp_handles[name]==undefined)
    dp_handles[name] = trak.dpHandle(name) ;

  return dp_handles[name] ;
  }

function
rp_handle(name)
  {
  check_trak() ;

  if(rp_handles[name]==undefined)
    rp_handles[name] = trak.rpHandle(name) ;

  return rp_handles[name] ;
  }

function 
do_trak(req,res,next) 
  {
  var topic  = req.body.topic   ;
  var action = req.body.action  ;
  var params = req.body.params ;
  var admlog = req.body.log ;
  var token  = req.headers.token ;

  if(topic && action)
    {
    user = 'guest' ;
    if(token)
      {
      try
        {
        decode = jwt.verify(token,secret.get()) ;
        }
      catch(error)
        {
        var err = new Error('bad token') ;
        err.status = 401 ;
        return next(err) ;
        }
      user = decode.user ;
      } 

    console.log('user '+user+' topic '+topic+' action '+action) ;
   
    perm.getPerms(user,function(err,perms)
      {
      if(!err)
        {
        if(true)
          {
          if((topic=='dp')&&(action=='list'))
            {
            var dps = [] ;
            var handle = 0 ;

            while(true)
              {
              var dp = trak.dp(handle++) ;
              if(dp.name=='')
                break ;
              dps.push(dp) ;
              }

            res.send(dps) ;
            }
          else if((topic=='rp')&&(action=='list'))
            {
            var rps = [] ;
            var handle = 0 ;

            while(true)
              {
              var rp = trak.rp(handle++) ;
              if(rp.name=='')
                break ;
              rps.push(rp) ;
              }

            res.send(rps) ;
            }
          else if((topic=='dp')&&(action=='full'))
            {
            var dp = {} ;
console.log(params) ;
            if(params.handle==undefined)
              dp.handle = dp_handle(params.name) ;
            else
              dp.handle = params.handle ;
            if(typeof(dp.handle)=="string")
              dp.handle = parseInt(dp.handle) ;
            if(params.name==undefined)
              {
              var info = trak.dp(dp.handle) ;
              dp.name = info.name ;
              }
            else
              dp.name = params.name ; 
            var full = trak.dpFull(dp.handle) ;
            res.send(full) ;
            }
          else if((topic=='dp')&&(action=='get'))
            {
            var dp = {} ;
console.log(params) ;
            if(params.handle==undefined)
              dp.handle = dp_handle(params.name) ;
            else
              dp.handle = params.handle ;
            if(typeof(dp.handle)=="string")
              dp.handle = parseInt(dp.handle) ;
            if(params.name==undefined)
              {
              var info = trak.dp(dp.handle) ;
              dp.name = info.name ;
              }
            else
              dp.name = params.name ; 
            dp.value  = trak.dpValueGet(dp.handle) ;
            res.send(dp) ;
            }
          else if((topic=='dp')&&(action=='set'))
            {
            var dp = {} ;
            if(params.handle==undefined)
              dp.handle = dp_handle(params.name) ;
            else
              dp.handle = params.handle ;
            if(typeof(dp.handle)=="string")
              dp.handle = parseInt(dp.handle) ;
            if(params.name==undefined)
              {
              var info = trak.dp(dp.handle) ;
              dp.name = info.name ;
              }
            else
              dp.name = params.name ; 
            dp.error  = trak.dpValueSet(dp.handle,parseInt(params.value)) ;
            res.send(dp) ;
            }
          else if((topic=='trak')&&(action=='list'))
            {
            var list = params ;

            for(var i=0 ; i<list.length ; i++)
              {
              if(list[i].type=='dp')
                {
                if(list[i].handle==undefined)
                  list[i].handle = dp_handle(list[i].name) ;
                if(typeof(list[i].handle)=="string")
                  list[i].handle = parseInt(list[i].handle) ;
                if(list[i].name==undefined)
                  {
                  var info = trak.dp(list[i].handle) ;
                  list[i].name = info.name ;
                  }
                if(list[i].element=='value')
                  list[i].value = trak.dpValueGet(list[i].handle) ;
                else if(list[i].element=='counter')
                  list[i].value = trak.dpCounterGet(list[i].handle) ;
                else if(list[i].element=='register')
                  {
                  list[i].value 
                    = trak.dpRegisterGet(list[i].handle,
                                         parseInt(list[i].register)) ;
                  }
                }
              else if(list[i].type=='rp')
                {
                list[i].handle = rp_handle(list[i].name) ;
                if(list[i].element=='value')
                  list[i].value = trak.rpValueGet(list[i].handle) ;
                }
              } 
            res.send(list) ;
            }
          else
            {
            var err = new Error('invalid topic:action') ;
            err.status = 404 ;
            return next(err) ;
            }
          }
        else
          {
          var err = new Error('requires perm '+need) ;
          err.status = 401 ;
          return next(err) ;
          }
        }
      else 
        return next(err) ;
      }) ;
    }
  else
    {
    var err = new Error('missing topic or action') ;
    err.status = 404 ;
    return next(err) ;
    }
  } ;

module.exports = router;
