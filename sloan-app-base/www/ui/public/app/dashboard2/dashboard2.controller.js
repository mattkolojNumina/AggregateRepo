(function()
{
  angular
    .module('ui')
      .controller('Dashboard2Controller',dashboard2Controller);

  angular
    .module('ui')
      .config(dashboard2Config);

  dashboard2Controller.$inject = ['$scope','$timeout','$routeParams',
                              '$mdDialog','$interval','Global','DbFactory'];
  
  function dashboard2Controller($scope,$timeout,$routeParams,$mdDialog,$interval,Global,DbFactory) {
    
    var periodic;

    $scope.refresh = refresh;
    $scope.permit = Global.permit;

    $scope.today = {};
    $scope.geek = {};
    $scope.zoneRoute = {};
    $scope.duCarts = {};
    $scope.pqCarts = {};
    $scope.dailyWaveSeq = "All";
    $scope.waveSeqs = [];
    $scope.dzr = $scope.dzr || {};
    $scope.shorts = {};
	
    // // // // //
    // TABLE HELPERS  
    var refreshCounter = 0;
    
    function refreshCount(n,name) {
      refreshCounter += n;
      if(refreshCounter>0)
        Global.busyRefresh(true);
      else
        Global.busyRefresh(false);
      if(false) //make true for logging
        console.log(name+": "+refreshCounter);
    } 

    function executeError(err) {
      console.log(err);
      refreshCount(-1, "error");		
    }

    function executeErrorToday(err) {
      console.log(err);
      refreshCount(-1, "error");		
    }

    function executeErrorGeek(err) {
      console.log(err);
      refreshCount(-1, "error");		
    }

    function executeErrorZR(err) {
      console.log(err);
      refreshCount(-1, "error");		
    }

    function executeErrorDU(err) {
      console.log(err);
      refreshCount(-1, "error");		
    }

    function executeErrorPQ(err) {
      console.log(err);
      refreshCount(-1, "error");		
    }

    function executeErrorShorts(err) {
      console.log(err);
      refreshCount(-1, "error");		
    }

    // // // // //
    // DATA RETRIEVAL
    //======================================================================================================================================
    // // //
    // TAB 0 Demand Today
    
    function refreshDemandToday() {
      DbFactory.post({topic: 'dashboard',
                      action: 'getWaves'
                    })
        .success(setWaves)
        .error  (executeError);

      DbFactory.post({topic: 'dashboard2',
                      action: 'todayRevised',
                      params: {dailySeq: $scope.dailyWaveSeq}
                     })
        .success(demandTodaySuccess)
        .error  (executeErrorToday);

      DbFactory.post({topic: 'dashboard2',
                      action: 'geekRevised',
                      params: {dailySeq: $scope.dailyWaveSeq}
                    })
        .success(demandGeekSuccess)
        .error  (executeErrorGeek);

      DbFactory.post({topic: 'dashboard2',
                        action: 'zoneRouteTodayRevised',
                        params: {dailySeq: $scope.dailyWaveSeq}
                      })
        .success(demandTodayZoneRouteSuccess)
        .error  (executeErrorZR);

      DbFactory.post({topic: 'dashboard2',
                        action: 'duCartsRevised',
                        params: {dailySeq: $scope.dailyWaveSeq}
                      })
        .success(demandDUCartsSuccess)
        .error  (executeErrorDU);

      DbFactory.post({topic: 'dashboard2',
                        action: 'pqCartsRevised',
                        params: {dailySeq: $scope.dailyWaveSeq}
                      })
        .success(demandPQCartsSuccess)
        .error  (executeErrorPQ);

      DbFactory.post({topic: 'dashboard2',
                        action: 'shortPicks',
                        params: {dailySeq: $scope.dailyWaveSeq}
                      })
        .success(demandShortsSuccess)
        .error  (executeErrorShorts);
    }

    function setWaves(data) {
      var waves = [];
      for(var i=0; i<data.length;i++) {
        waves.push(data[i].dailyWaveSeq);
      }
      $scope.waveSeqs = waves;
    }
    
    function demandTodaySuccess(data) {
      if($scope.dailyWaveSeq==='All') {
        $scope.today = data[0];
      }
      else {
        $scope.today = data[1];
      }
      $scope.today.demandDate = new Date()
    }

    function demandShortsSuccess(data) {
      if($scope.dailyWaveSeq==='All') {
        $scope.shorts = data[0];
      }
      else {
        $scope.shorts = data[1];
      }
    }

    function demandGeekSuccess(data) {
      if($scope.dailyWaveSeq==='All') {
        $scope.geek = data[0];
      }
      else {
        $scope.geek = data[1];
      }
    }

    function demandTodayZoneRouteSuccess(data) {
      if($scope.dailyWaveSeq==='All') {
        $scope.zoneRoute = data[0];
      }
      else {
        $scope.zoneRoute = data[1];
      }
    }

    function demandDUCartsSuccess(data) {
      if($scope.dailyWaveSeq==='All') {
        $scope.duCarts = data[0];
      }
      else {
        $scope.duCarts = data[1];
      }
    }

    function demandPQCartsSuccess(data) {
      if($scope.dailyWaveSeq==='All') {
        $scope.pqCarts = data[0];
        $scope.pqCarts.numPQCartons = data[0].numLiquidCartons + data[0].numPerishableCartons;
      }
      else {
        $scope.pqCarts = data[1];
        $scope.pqCarts.numPQCartons = data[1].numLiquidCartons + data[1].numPerishableCartons;
      }
    }
    
    //======================================================================================================================================
    // // //
    // TAB 1 Demand Zone Route

    function refreshDemandZoneRoute() {
      if($scope.dailyWaveSeq==='All') {
      DbFactory.post({topic: 'dashboard',
                      action: 'zr'
                     })
        .success(demandZoneRouteAllSuccess)
        .error  (executeErrorZRTab);
      }
      else {
        DbFactory.post({topic: 'dashboard',
                        action: 'zrByWave',
                        params: {dailySeq: $scope.dailyWaveSeq}
                      })
        .success(demandZoneRouteSuccess)
        .error  (executeErrorZRTab);
      }
    }

    function executeErrorZRTab(err) {
      console.log(err);
      refreshCount(-1, "error");		
    }
    
    function demandZoneRouteSuccess(data) {
      $scope.dzr = data;
    }

    function demandZoneRouteAllSuccess(data) {
      $scope.dzr = [];
      $scope.dzr[0] = {0:0};
      $scope.dzr = $scope.dzr.concat(data);
    }
    
    // // // // //
    // SETUP AND ALL THAT

    function
    refresh()
    {
      switch($scope.selected){
        case 0:
          refreshDemandToday();
          break;				
        case 1:
          refreshDemandZoneRoute();
          break;					
        default:
          refreshCount(0, "default tab rendered");
      }
    }

    function
    init()
    {
      Global.setTitle('Dashboard');
      Global.recv('refresh',refresh,$scope);
      periodic = $interval(refresh, 30000); 
      refresh();
    }

    init();

    $scope.$on('$destroy', function(){
      $interval.cancel(periodic);
    });
    
  }

  function
  dashboard2Config($routeProvider)
  {
    $routeProvider
      .when('/dashboard2', {controller: 'Dashboard2Controller',
                        templateUrl: '/app/dashboard2/dashboard2.view.html'});
  }
  
}())

