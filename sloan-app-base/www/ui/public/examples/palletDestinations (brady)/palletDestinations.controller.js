(function()
  {
  angular
    .module('ui')
      .controller('PalletDestinationsController',palletDestinationsController);

  angular
    .module('ui')
      .config(palletDestinationsConfig);

  palletDestinationsController.$inject = ['$scope','$interval','$timeout',
                                          'Global','DbFactory'];
  
  function
  palletDestinationsController($scope,$interval,$timeout,
                               Global,DbFactory)
  {
//    var periodic;
    
    var chooseTable = null;
    
    $scope.edit = {};
    $scope.editNew = editNew;
    $scope.editUpdate = editUpdate;
    $scope.editDelete = editDelete;
    
    $scope.refresh = refresh;
    $scope.permit = Global.permit;
    
    
    // utils 

    function
    dateRender(data,type,full,meta)
    {
      if(type!='display')
        return data;

      var date = new Date(data);
      return date.toLocaleString();
    }
    
    
    // // // // //
    // DATA RETRIEVAL
    
    function
    refresh()
    {
      Global.busyRefresh(true);
      DbFactory.post({topic: 'palletDestination',
                      action: 'all'
                     })
        .success(chooseSuccess)
        .error  (chooseError); 
    }

    function
    editNew()
    { $scope.edit = {}; }
    
    function
    editUpdate()
    {
      Global.busyRefresh(true);
      DbFactory.post({topic: 'palletDestination',
                      action: 'update',
                      params: {seq:            ($scope.edit.seq || null),
                               destination:     $scope.edit.destination,
                               countryCode:     $scope.edit.countryCode,
                               incotm2:         $scope.edit.incotm2,
                               name:            $scope.edit.name,
                               shipTo:          $scope.edit.shipTo,
                               regionCode:      $scope.edit.regionCode,
                               city:            $scope.edit.city,
                               purchaseOrderNo: $scope.edit.purchaseOrderNo,
                               expectedCartons: $scope.edit.expectedCartons}
                     })
          .success(updateSuccess)
          .error(updateError)
    }

    function
    updateSuccess()
    {
      console.log('update success');
      $timeout(refresh,1000);
    }

    function
    updateError()
    {
      console.log('update error');
      Global.busyRefresh(false);
    }

    function
    editDelete()
    {
      Global.busyRefresh(true);
      DbFactory.post({topic: 'palletDestination',
                      action: 'delete',
                      params: {seq: $scope.edit.seq}
                     })
        .success(deleteSuccess)
        .error(deleteError)
      editNew();
    }

    function
    deleteSuccess()
    {
      console.log('delete success');
      $timeout(refresh,1000);
    }

    function
    deleteError()
    {
      console.log('delete error');
      Global.busyRefresh(false);
    }

    function
    editClick()
    {
      var data = chooseTable.row(this).data();
      
      $scope.edit.seq             = data.seq,
      $scope.edit.destination     = data.destination,
      $scope.edit.countryCode     = data.countryCode,
      $scope.edit.incotm2         = data.incotm2,
      $scope.edit.name            = data.name,
      $scope.edit.shipTo          = data.shipTo,
      $scope.edit.regionCode      = data.regionCode,
      $scope.edit.city            = data.city,
      $scope.edit.purchaseOrderNo = data.purchaseOrderNo,
      $scope.edit.expectedCartons = data.expectedCartons
      
      $scope.$apply();
    }
    
    function
    chooseSuccess(data)
    {
      var cols = [];
      var ref = "#destChoose";
      
      cols.push({title:"Sequence",        data:"seq",             visible: false});
      cols.push({title:"Destination",     data:"destination"});
      cols.push({title:"Country",         data:"countryCode"});
      cols.push({title:"incotm2",         data:"incotm2"});
      cols.push({title:"Name",            data:"name",            visible: false});
      cols.push({title:"Ship to",         data:"shipTo",          visible: false});
      cols.push({title:"Region",          data:"regionCode",      visible: false});
      cols.push({title:"City",            data:"city",            visible: false});
      cols.push({title:"Purchase order",  data:"purchaseOrderNo", visible: false});
      cols.push({title:"Expected cartons",data:"expectedCartons", visible: false});
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
      }
      Global.busyRefresh(false);
    }

    function
    chooseError()
    {
      console.log('error');
      Global.busyRefresh(false);
    }


    function
    init()
    {
      Global.setTitle('Pallet Destinations');
      Global.recv('refresh',refresh,$scope);
      refresh();
//      periodic = $interval(currentRefresh,5000);
    }
    
//    $scope.$on('$destroy',function(){
//      $interval.cancel(periodic);
//    });
    
    init();
    }

  function
  palletDestinationsConfig($routeProvider)
  {
    $routeProvider
      .when('/palletDestinations',{controller: 'PalletDestinationsController',
                                   templateUrl: '/app/palletDestinations/palletDestinations.view.html'});
  }

}())