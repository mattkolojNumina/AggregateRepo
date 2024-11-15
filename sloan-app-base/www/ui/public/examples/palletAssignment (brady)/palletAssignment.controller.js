(function()
  {
  angular
    .module('ui')
      .controller('PalletAssignmentController',palletAssignmentController);

  angular
    .module('ui')
      .config(palletAssignmentConfig);

  palletAssignmentController.$inject = ['$scope','$interval','$timeout',
                                        'Global','DbFactory'];
  
  function
  palletAssignmentController($scope,$interval,$timeout,
                               Global,DbFactory)
  {
//    var periodic;
    
    var chooseTable = null;
    
    $scope.edit = {};
    $scope.destinations = [];
    $scope.editNew = editNew;
    $scope.editUpdate = editUpdate;
    $scope.editUnassign = editUnassign;
    
    $scope.creating = true; //as opposed to editing
    
    $scope.refresh = refresh;
    $scope.permit = Global.permit;
    
    
    // utils 
    
    /*function
    dateRender(data,type,full,meta)
    {
      if(type!='display')
        return data;

      var date = new Date(data);
      return date.toLocaleString();
    }*/
    
    
    // // // // //
    // DATA RETRIEVAL
    
    function
    loadLocations()
    {
      Global.busyRefresh(true);
      DbFactory.post({topic: 'floorLocation',
                      action: 'all'
                     })
        .success(locationsSuccess)
        .error  (locationsError);
    }
    
    function
    loadDestinations()
    {
      Global.busyRefresh(true);
      DbFactory.post({topic: 'palletDestination',
                      action: 'all'
                     })
        .success(destinationsSuccess)
        .error  (destinationsError);
    }
    
    
    // // // // //
    // EDITING
    
    function
    editNew()
    {
      $scope.edit = {};
      $scope.creating = true;
    }
    
    function
    editUpdate()
    {
      Global.busyRefresh(true);
      DbFactory.post({topic: 'floorLocation',
                      action: 'update',
                      params: {floorLocation:  ($scope.edit.floorLocation || null),
                               destinationSeq:  $scope.edit.destinationSeq,
                               palletID:        $scope.edit.palletID}
                     })
        .success(updateSuccess)
        .error  (updateError)
    }
    
    function
    updateSuccess()
    {
      console.log('update success');
      $timeout(loadLocations,1000);
    }
    
    function
    updateError()
    {
      console.log('update error');
      Global.busyRefresh(false);
    }
    
    function
    editUnassign()
    {
      Global.busyRefresh(true);
      DbFactory.post({topic: 'floorLocation',
                      action: 'unassign',
                      params: {floorLocation: $scope.edit.floorLocation}
                     })
        .success(unassignSuccess)
        .error  (unassignError)
      editNew();
    }
    
    function
    unassignSuccess()
    {
      console.log('unassign success');
      $timeout(loadLocations,1000);
    }

    function
    unassignError()
    {
      console.log('unassign error');
      Global.busyRefresh(false);
    }

    function
    editClick()
    {
      var data = chooseTable.row(this).data();
      
      $scope.edit.floorLocation  = data.floorLocation,
      $scope.edit.destinationSeq = data.destinationSeq,
      $scope.edit.palletID       = data.palletID
      
      $scope.$apply();
    }
    
    function
    destinationsSuccess(data)
    { $scope.destinations = data; }
    
    function
    locationsSuccess(data)
    {
      // TODO show floor locations table
      
      /*var cols = [];
      var ref = "#destChoose";
      
      cols.push({title:"Sequence",        data:"seq",             visible: false});
      cols.push({title:"Location",        data:"location"});
      cols.push({title:"Description",     data:"description"});
      cols.push({title:"Pallet ID",       data:"palletID",        visible: false});
      cols.push({title:"incotm2",         data:"incotm2"});
      cols.push({title:"Ship to",         data:"shipTo",          visible: false});
      cols.push({title:"Country",         data:"countryCode"});
      cols.push({title:"Name",            data:"name",            visible: false});
      cols.push({title:"City",            data:"city"});
      cols.push({title:"Carrier",         data:"carrierCode",     visible: false});
      cols.push({title:"Carrier service", data:"carrierService",  visible: false});
      cols.push({title:"Purchase order",  data:"purchaseOrderNo", visible: false});
      cols.push({title:"Stamp",           data:"stamp",           visible: false,
                                          render: dateRender});
      
      if(chooseTable){
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
        $timeout(function(){chooseTable.draw()},0);
      }*/
      Global.busyRefresh(false);
    }

    function
    destinationsError()
    {
      console.log('error');
      Global.busyRefresh(false);
    }

    function
    locationsError()
    {
      console.log('error');
      Global.busyRefresh(false);
    }


    function
    init()
    {
      Global.setTitle('Pallet Assignment');
//      Global.recv('refresh',refresh,$scope);
//      refresh();
//      periodic = $interval(currentRefresh,5000);
    }
    
//    $scope.$on('$destroy',function(){
//      $interval.cancel(periodic);
//    });
    
    init();
    }

  function
  palletAssignmentConfig($routeProvider)
  {
    $routeProvider
      .when('/palletAssignment',{controller: 'PalletAssignmentController',
                                   templateUrl: '/app/palletAssignment/palletAssignment.view.html'});
  }

}())