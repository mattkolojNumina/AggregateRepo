(function()
{
  angular
    .module('ui')
      .controller('SupportController',supportController);

  angular
    .module('ui')
      .config(supportConfig);

  supportController.$inject = ['$scope','$timeout','$routeParams',
                              '$mdDialog','$interval','Global','DbFactory'];
  
  function supportController($scope,$timeout,$routeParams,$mdDialog,$interval,Global,DbFactory) {

    function init() {
      Global.setTitle('Support');
    }

    init();

    $scope.$on('$destroy', function(){
      $interval.cancel(periodic);
    });
    
  }

  function supportConfig($routeProvider) {
    $routeProvider
      .when('/support', {controller: 'SupportController',
                        templateUrl: '/ui/support/support.view.html'});
  }
  
}())
