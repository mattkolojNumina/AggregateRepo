(
function()
  {
  angular
    .module('ui')
      .controller('FootController',footController) ;

  footController.$inject = ['$scope'] ;
  
  function
  footController($scope)
    {
    $scope.title = 'foot' ;

    function
    init()
      {
      }

    init() ;
    }

  }() 
) 
