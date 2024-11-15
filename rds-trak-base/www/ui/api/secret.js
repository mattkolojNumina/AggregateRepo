var uuid = require('node-uuid') ;
var path = require('path') ;
var fs   = require('fs') ;

var secret = '' ;

function
set()
  {
  var secret_file = ".secret" ;
  fs.readFile(secret_file,function(err,data){
    if(err)
      {
      secret = uuid.v4() ;
      fs.writeFile(secret_file,secret,function(err){
        if(err)
          console.log('error writing secret ',err) ;
        }) ;
      }
    else
      secret = data ;
    }) ;
  }

function
get()
  {
  return secret ;
  }

module.exports.set = set ;
module.exports.get = get ; 
