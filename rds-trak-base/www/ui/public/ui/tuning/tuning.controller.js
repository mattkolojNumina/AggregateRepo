(function()
{
  angular
    .module('ui')
      .controller('TuningController',tuningController);
  
  angular
    .module('ui')
      .config(tuningConfig);
  
  tuningController.$inject = ['$scope','$interval','$timeout','Global','DbFactory'];
  
  function
  tuningController($scope,$interval,$timeout,Global,DbFactory)
  {
    $scope.refresh = refresh;
    $scope.permit = Global.permit;
    
    $scope.tuning = {};
    $scope.tuningNew = tuningNew;
    $scope.tuningUpdate = tuningUpdate;
    $scope.tuningDelete = tuningDelete;
    
    
    // // // // //
    // ACTIONS
    
    function
    tuningUpdate()
    {
      Global.busyRefresh(true);
      DbFactory.post({topic: 'tuning',
                      action: 'update',
                      params: {host:     $scope.tuning.host,
                               name:     $scope.tuning.name,
                               register: $scope.tuning.register,
                               value:    $scope.tuning.value}
                     })
        .success(updateSuccess)
        .error  (updateError);
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
    tuningDelete()
    {
      Global.busyRefresh(true);
      DbFactory.post({topic: 'tuning',
                      action: 'delete',
                      params: {host:     $scope.tuning.host,
                               name:     $scope.tuning.name,
                               register: $scope.tuning.register}
                     })
        .success(deleteSuccess)
        .error  (deleteError)
      tuningNew();
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
    tuningNew()
    { $scope.tuning = {}; }
    
    
    // // // // //
    // TABLE
    
    /*function
    dateRender(data,type,full,meta)
    {
      if(data){
        if(type=='display'){
          var date = new Date(data);
          return date.toLocaleString();
        } else {
          var tzoffset = (new Date()).getTimezoneOffset()*60000;
          var localISOTime = (new Date(new Date(data) - tzoffset)).toISOString().slice(0,-1);
          return localISOTime;
        }
      } else {
        return '';
      }
    }*/
    
    // // //
    // TUNING
    
    var tuningTable = null;
    
    function
    buildTuningTable(data)
    {
      var cols = [];
      var ref = "#tuningTable";
      
      cols.push({title: "Host",         data:"host",
                                        class:"dt-center"});
      cols.push({title: "Zone",         data:"zone",
                                        class:"dt-center"});
      cols.push({title: "Name",         data:"name"});
      cols.push({title: "Area",         data:"area",
                                        class:"dt-center"});
      cols.push({title: "Value",        data:"get",
                                        class:"dt-right"});
      cols.push({title: "Standard",     data:"standard",
                                        class:"dt-right"});
      cols.push({title: "Description",  data:"description",
                                        class:"dt-left"});
      
      if(tuningTable){
        tuningTable.clear();
        tuningTable.rows.add(data);
        tuningTable.draw(false);
      } else {
        tuningTable = $(ref)
          .DataTable({data: data,
                      columns: cols,
                      scrollY: "550px",
                      scrollX: true,
                      scrollCollapse: true,
                      paging: true,
                      pageLength: 50,
                      dom: 'lftipr'});
        $(ref+' tbody').on('click','tr',tuningClick);
        $timeout(tuningTable.draw,0);
      }
      Global.busyRefresh(false);
    }
    
    function
    tuningClick()
    {
      var data = tuningTable.row(this).data();
      if(data){
        $scope.tuning.host        = data.host;
        $scope.tuning.zone        = data.zone;
        $scope.tuning.name        = data.name;
        $scope.tuning.register    = data.register;
        $scope.tuning.area        = data.area;
        $scope.tuning.get         = data.get;
        $scope.tuning.put         = data.put;
        $scope.tuning.standard    = data.standard;
        $scope.tuning.state       = data.state;
        $scope.tuning.description = data.description;
        $scope.tuning.handle      = data.handle;
        $scope.tuning.stamp       = data.stamp;
        $scope.tuning.value       = data.get;
        $scope.$apply();
      }
    }
    
    
    // // // // //
    // DATA RETRIEVAL
    
    function
    refresh()
    {
      Global.busyRefresh(true);
      DbFactory.post({topic: 'tuning',
                      action: 'all'
                     }) 
        .success(tuningSuccess)
        .error  (tuningError); 
    }
    
    function
    tuningSuccess(data)
    { buildTuningTable(data); }
    
    function
    tuningError(err)
    {
      console.error(err);
      Global.busyRefresh(false);
    }
    
    
    // // // // //
    // INIT
    
    function
    init()
    {
      Global.setTitle('Tuning');
      Global.recv('refresh',refresh,$scope);
      refresh();
    }
    
    init();
  }
  
  function
  tuningConfig($routeProvider)
  {
    $routeProvider
      .when('/tuning',{controller: 'TuningController',
                       templateUrl: '/ui/tuning/tuning.view.html'});
  }
  
}())
