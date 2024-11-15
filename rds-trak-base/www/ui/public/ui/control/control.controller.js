(function()
{
  angular
    .module('ui')
      .controller('ControlController',controlController);
  
  angular
    .module('ui')
      .config(controlConfig);
  
  controlController.$inject = ['$scope','$interval','$timeout',
                               'Global','DbFactory'];
  
  function
  controlController($scope,$interval,$timeout,
                    Global,DbFactory)
  {
    $scope.refresh = refresh;
    $scope.permit = Global.permit;
    
    $scope.control = {};
    $scope.controlNew = controlNew;
    $scope.controlUpdate = controlUpdate;
    $scope.controlDelete = controlDelete;
    
    
    // // // // //
    // ACTIONS
    
    function
    controlUpdate()
    {
      Global.busyRefresh(true);
      DbFactory.post({topic: 'control',
                      action: 'update',
                      params: {host:        $scope.control.host,
                               zone:        $scope.control.zone,
                               name:        $scope.control.name,
                               value:       $scope.control.value,
                               description: $scope.control.description}
                     })
        .success(updateSuccess)
        .error(updateError)
    }
    
    function
    updateSuccess()
    { $timeout(refresh,1000); }
    
    function
    updateError(err)
    {
      console.error(err);
      Global.busyRefresh(false);
    }
    
    function
    controlDelete()
    {
      Global.busyRefresh(true);
      DbFactory.post({topic: 'control',
                      action: 'delete',
                      params: {host: $scope.control.host,
                               zone: $scope.control.zone,
                               name: $scope.control.name}
                     })
        .success(deleteSuccess)
        .error(deleteError)
      controlNew();
    }
    
    function
    deleteSuccess()
    { $timeout(refresh,1000); }
    
    function
    deleteError(err)
    {
      console.error(err);
      Global.busyRefresh(false);
    }
    
    function
    controlNew()
    { $scope.control = {}; }
    
    
    // // // // //
    // TABLE
    
    // // //
    // CONTROL
    
    var controlTable = null;
    
    function
    buildControlTable(data)
    {
      var cols = [];
      var ref = "#controlTable";
      
      cols.push({title: "Host",        data:"host"});
      cols.push({title: "Zone",        data:"zone"});
      cols.push({title: "Name",        data:"name"});
      cols.push({title: "Value",       data:"value"});
      cols.push({title: "Description", data:"description"});
      
      if(controlTable){
        controlTable.clear();
        controlTable.rows.add(data);
        controlTable.draw(false);
      } else {
        controlTable = $(ref)
                  .DataTable({data: data, 
                              columns: cols,
                              scrollY: "550px",
                              scrollX: true,
                              scrollCollapse: true,
                              paging: true,
                              pageLength: 50,
                              dom: 'lftipr'});
        $(ref+' tbody').on('click','tr',controlClick);
        $timeout(controlTable.draw,0);
      }
      Global.busyRefresh(false);
    }
    
    function
    controlClick()
    {
      var data = controlTable.row(this).data();
      if(data){
        $scope.control.host = data.host;
        $scope.control.zone = data.zone;
        $scope.control.name = data.name;
        $scope.control.value = data.redacted=='true'?'':data.value;
        $scope.control.description = data.description;
        $scope.$apply();
      }
    }
    
    
    // // // // //
    // DATA RETRIEVAL
    
    function
    controlRefresh()
    {
      Global.busyRefresh(true);
      DbFactory.post({topic: 'control',
                      action: 'editable'
                     })
        .success(controlSuccess)
        .error(controlError); 
    }
    
    function
    controlSuccess(data)
    { buildControlTable(data); }
    
    function
    controlError(err)
    {
      console.error(err);
      Global.busyRefresh(false);
    }
    
    function
    refresh()
    {
      controlRefresh();
    }
    
    
    // // // // //
    // INIT
    
    function
    init()
    {
      Global.setTitle('Control Parameters');
      Global.recv('refresh',refresh,$scope);
      refresh();
    }
    
    init();
  }
  
  function
  controlConfig($routeProvider)
  {
    $routeProvider
      .when('/control',{controller: 'ControlController',
                        templateUrl: '/ui/control/control.view.html'});
  }
  
}())
