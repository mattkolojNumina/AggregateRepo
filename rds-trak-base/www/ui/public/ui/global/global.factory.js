(
function()
  {
  angular
    .module('ui')
      .factory('Global',['$rootScope','$location',globalFactory]) ;

  globalFactory.$inject = ['$rootScope','$location'] ;

  function
  globalFactory($rootScope,$location)
    {
    var factory = {} ;
    var user = null ;

    factory.send = function(msg,data)
      {
      data = data || {} ;
      $rootScope.$emit(msg,data) ;
      }

    factory.recv = function(msg,func,scope)
      {
      var unbind = $rootScope.$on(msg,func) ;
      if(scope)
        scope.$on('$destroy',unbind) ;
      }

    
    factory.busyRefresh = function(busy)
      {
      factory.send('busyRefresh',{busy: busy}) ;
      }

    factory.setTitle = function(title)
      {
      factory.send('setTitle',{title: title}) ;
      }

    factory.showMessage = function(text)
      {
      factory.send('showMessage',{text: text}) ;
      }

    factory.home = function()
      {
      $location.path('/') ;
      }

    factory.setUser = function(newUser)
      {
      user = newUser ;
      factory.send('newUser',user) ;
      }

    factory.getUser = function()
      {
      return user ;
      }

    factory.permit = function(perm)
      {
      if(user && user.perms)
        for(var i=0 ; i<user.perms.length ; i++)
          if(perm == user.perms[i] || user.perms[i] == 'root')
            return true ;
      return false ;
      }

    factory.reset = function()
      {
      factory.send('reset',{}) ;
      }
 
    return factory ;
    }

  }() 
) 
