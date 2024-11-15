(
function()
  {
  angular
    .module('ui')
      .factory('DbFactory',dbFactory) ;

  dbFactory.$inject = ['$http'] ;

  function
  dbFactory($http)
    {
    var factory = {} ;

    factory.post = function(params)
      {
      return $http.post('/db',params) ;
      }
   
    return factory ;
    }

  }() 
) 
