(
function()
  {
  angular
    .module('ui')
      .controller('LoginController',loginController) ;

  loginController.$inject = ['$scope','$location','$mdDialog',
                             'Global','LoginFactory'] ;
  
  function
  loginController($scope,$location,$mdDialog,Global,LoginFactory)
    {
    $scope.login = {} ;
    $scope.cancel = cancel;
    $scope.login  = login ;
    $scope.logout = logout ;

    function
    cancel() 
      {
      $mdDialog.cancel();
      };

    function
    login()
      {
      LoginFactory
        .login($scope.login.username,$scope.login.password)  
      $mdDialog.hide();
      }

    function
    logout()
      {
      LoginFactory
        .logout() ;
      $mdDialog.hide() ;
      }

    function
    init()
      {
      }

    init() ;
    }

  }() 
) 
