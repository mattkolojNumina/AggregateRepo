(function()
{
  angular
    .module('ui')
      .controller('LaneConfigureController',laneConfigureController);

  angular
    .module('ui')
      .config(laneConfigureConfig);

  laneConfigureController.$inject = ['$scope','$interval','$timeout',
                                     'Global','DbFactory'];
  
  function
  laneConfigureController($scope,$interval,$timeout,
                          Global,DbFactory)
  {
    
    var periodic;
    var chooseTable = null;
    
    $scope.permit = Global.permit;
    
    $scope.edit = {};
    $scope.editUpdate = editUpdate;
//    $scope.statuses = ['idle','inactive','reverse','assigned','active','full'];
    $scope.laneTypes = ['pallet','disabled'];
    
    
    // // // // //
    // CHOOSE & EDIT
    
    function
    editUpdate()
    {
      Global.busyRefresh(true);
      DbFactory.post({topic: 'lane', action: 'setType',
                      params: {sorterLane:  $scope.sorterLane,
                               laneType:    $scope.edit.laneType}
                     }) 
        .success(updateSuccess)
        .error  (updateError);
    }
    
    function
    updateSuccess()
    {
//      console.log('update success');
//      $timeout(refresh,1000);
      refresh();
    }
    
    function
    updateError(err)
    {
      console.log(err);
      Global.busyRefresh(false);
    }
    
    
    function
    editClick()
    {
      var data = chooseTable.row(this).data();
      $scope.sorterLane = data.sorterLane;
      $scope.edit.laneType = data.laneType;
      $scope.$apply(); // really necessary?
    }
    
    
    // // // // //
    // REFRESHMENTS
    
    function
    refresh()
    {
      refreshLanes();
    }
    
    function
    refreshLanes()
    {
      Global.busyRefresh(true);
      DbFactory.post({topic: 'lane',
                      action: 'pallet'}) 
          .success(lanesSuccess)
          .error  (lanesError); 
    }
    
    function
    lanesSuccess(data)
    {
      var cols = [];
      var ref = "#laneChoose";
      
      cols.push({title: "Lane",   data:"sorterLane"});
      cols.push({title: "Type",   data:"laneType"});
      cols.push({title: "Status", data:"status"});
      
      if(chooseTable)
      {
        chooseTable.clear();
        chooseTable.rows.add(data);
        chooseTable.draw();
      } else {
        chooseTable = $(ref)
                  .DataTable({data: data, 
                              columns: cols,
                              scrollY: "425px",
                              scrollCollapse: true,
                              paging: false,
                              dom: 'lftipr'});
        $(ref+' tbody').on('click','tr',editClick);     
      }
      Global.busyRefresh(false);
    }
    
    function
    lanesError(err)
    {
      console.log(err);
      Global.busyRefresh(false);
    }
    
    
    // // // // //
    // INIT
    
    function
    init()
    {
      Global.setTitle('Lane Configuration');
      Global.recv('refresh',refresh,$scope);
      refresh();
      periodic = $interval(refresh,5000);
    }
    
    $scope.$on('$destroy',function(){
      $interval.cancel(periodic);
    });
    
    init();
  }

  function
  laneConfigureConfig($routeProvider)
  {
    $routeProvider
      .when('/laneConfigure',{controller: 'LaneConfigureController',
                              templateUrl: '/app/laneConfigure/laneConfigure.view.html'});
  }

}())