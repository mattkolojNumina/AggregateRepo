(
function()
  {
  angular
    .module('ui')
      .factory('LoginFactory',loginFactory) ;

  loginFactory.$inject = ['$http','Global'] ;

  function
  loginFactory($http,Global)
    {
    var factory = {} ;

    function
    loginSuccess(data,status,headers,config)
      {
      Global.setUser(data) ;
      if(data.loggedIn)
        Global.showMessage('Logged in as '+data.name) ;
      else
        if(data.failed)
           Global.showMessage('Login failed') ;
      }

    function
    loginError(err)
      {
      console.log(err) ;
      }

    factory.login = function(user,password)
      {
      $http
        .post('/auth',
              {user: user,
              password: password}) 
          .success(loginSuccess) 
          .error(loginError) ;
      }

    factory.logout = function()
      {
      factory.login('guest','guest') ;
      Global.showMessage('Logged out') ;
      Global.home() ;
      }

    factory.perms = function(token)
      {
      return $http.post('/auth/perms',
                        {token: token}) ;
      }

    return factory ;
    }

  }() 
) 
