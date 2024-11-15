(
function()
  {
  angular
    .module('ui')
      .factory('TrakFactory',trakFactory) ;

  trakFactory.$inject = ['$http'] ;

  function
  trakFactory($http)
    {
    var factory = {} ;

    factory.post = function(params)
      {
      return $http.post('/trak',params) ;
      }
   
    return factory ;
    }

  }() 
) 
