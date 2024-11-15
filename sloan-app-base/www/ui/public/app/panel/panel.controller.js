(function()
  {
  angular
    .module('ui')
      .controller('PanelController',panelController);
  
  angular
    .module('ui')
      .config(panelConfig);
  
  panelController.$inject = ['$scope','$timeout','$interval',
                             'Global','DbFactory'];
  
  function
  panelController($scope,$timeout,$interval,
                  Global,DbFactory)
    {
    var periodic;
    var panel = 1 ;
    var host ;
    
    // tabs
    $scope.selected = 0;
    $scope.refresh = refresh;
    $scope.permit = Global.permit;
    $scope.state = "" ;
    $scope.trashState = "" ;
    $scope.start = start ;
    $scope.startTrash = startTrash ;
    $scope.stopTrash = stopTrash ;
    $scope.resetTrash = resetTrash ;
    $scope.stopBoom = stopBoom ;
    $scope.startBoom = startBoom ;
    $scope.stop  = stop ;
    $scope.reset = reset ;
   
    function
    start()
      {
      console.log('start') ;
      put('ext'+panel+'_start') ;
      }

    function
    startTrash()
      {
      console.log('start') ;
      put('ext2_trash_start') ;
      }

    function
    startBoom()
      {
      console.log('start') ;
      put('ext1_boom_start') ;
      }

    function
    stop()
      {
      console.log('stop') ;
      put('ext'+panel+'_stop') ;
      }

    function
    stopTrash()
      {
      console.log('stop') ;
      put('ext2_trash_stop') ;
      }
      
    function
    stopBoom()
      {
      console.log('stop') ;
      put('ext1_boom_stop') ;
      }

    function
    reset()
      {
      console.log('reset') ;
      put('ext'+panel+'_reset') ;
      }

    function
    resetTrash()
      {
      console.log('reset') ;
      put('ext2_trash_reset') ;
      }
      


    function
    put(name)
      {
      DbFactory
        .post({topic: 'panel',
               action: 'put',
               params: {put: 1,
                        host: 'org-trk'+panel,
                        name: name}})
         .error(someError) ;
      }

    function
    someError(err)
      { 
      console.error(err); 
      }
    
    function
    refresh()
      {
      panel = $scope.selected + 1 ;
      host = 'org-trk'+panel ;
      panelRefresh(host) ;
      }
    
    function
    panelRefresh(host)
      {
      DbFactory.post({topic: 'panel',
                      action: 'all',
                      params: {host: host }})
        .success(panelSuccess)
        .error  (someError); 
      }
    
    function
    panelSuccess(data)
      {
      for(var i=0 ; i<data.length ; i++)
        {
        if(data[i].name=='cp'+panel)
          $scope.state = states(data[i].get) ;

        if(data[i].name=='sorter')
          $scope.state = states(data[i].get) ;

        if(data[i].name=='downline')
          $scope.state2 = states(data[i].get) ;
        
        if(data[i].name=='trash')
          $scope.trashState = trashStates(data[i].get) ;
        }
      }

    function
    states(s)
      {
      switch(s)
        {
        case 0: return "Initializing" ; break ;
        case 1: return "Checking"     ; break ;
        case 2: return "Idle"         ; break ;
        case 3: return "Starting"     ; break ;
        case 4: return "Running"      ; break ;
        case 5: return "Stopping"     ; break ;
        case 6: return "Faulted"      ; break ;
        case 7: return "Faulted"      ; break ;
        }
      }

    function
    trashStates(s)
      {
      switch(s)
        {
        case 0: return "Stopped"      ; break ;
        case 1: return "Running"      ; break ;
        }
      }

    function
    init()
      {
      Global.setTitle('Panel Controls');
      Global.recv('refresh',refresh,$scope);
      periodic = $interval(refresh,500) ;
      }
   
    $scope.$on('$destroy',function(){$interval.cancel(periodic);}) ;
 
    init();
    }
  
  function
  panelConfig($routeProvider)
    {
    $routeProvider
      .when('/panel',{controller: 'PanelController',
                     templateUrl: '/app/panel/panel.view.html'});
    }
  
  }())
