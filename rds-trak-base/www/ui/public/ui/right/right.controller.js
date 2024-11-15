(
function()
  {
  angular
    .module('ui')
      .controller('RightController',rightController) ;

  rightController.$inject = ['$scope','$mdSidenav','Global'] ;
  
  function
  rightController($scope,$mdSidenav,Global)
    {
    $scope.rightToggle = rightToggle ;

    function
    rightToggle()
      {
      $mdSidenav('right').toggle() ;
      }

    function
    init()
      {
      Global.recv('rightToggle',rightToggle,$scope) ;
      }

    init() ;
    }

  }() 
) 
