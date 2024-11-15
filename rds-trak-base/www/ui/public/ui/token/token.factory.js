(
function()
  {
  angular
    .module('ui')
      .factory('TokenFactory',tokenFactory) ;

  angular
    .module('ui')
      .config(tokenConfig) ;

  tokenFactory.$inject = ['$q','Global'] ;

  function
  tokenFactory($q,Global)
    {
    var factory = {} ;

    factory.request = function(req)
      {
      var user = Global.getUser() ;
      if(user && user.loggedIn && user.token)
        {
        req.headers.token = user.token ;
        }
      return req ;
      }

    factory.requestError = function(req)
      {
      return req ;
      }

    factory.response = function(res)
      {
      return res ;
      }

    factory.responseError = function(res)
      {
      if(res.status==401)
        Global.showMessage('You do not have permission to do that.') ;
      else
        Global.showMessage('A server error occurred') ;        

      Global.home() ;
      Global.reset() ;

      return $q.reject(res) ;
      }

   
    return factory ;
    }

  function
  tokenConfig($httpProvider)
    {
    $httpProvider
      .interceptors
        .push('TokenFactory') ;
    }

  }() 
) 
