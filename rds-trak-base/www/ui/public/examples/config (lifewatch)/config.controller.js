(function()
{
  angular
    .module('ui')
      .controller('ConfigController',configController);

  angular
    .module('ui')
      .config(configConfig);

  configController.$inject = ['$scope','$interval','$timeout',
                              'Global','DbFactory'];
  
  function
  configController($scope,$interval,$timeout,
                   Global,DbFactory)
  {
    
    var periodic;
    
    $scope.permit = Global.permit;
    
    var cartonsTable;
    var skusTable;
    var shippersTable;
    var locationsTable;
    
    $scope.cartonType;
    $scope.itemId;
    $scope.carrier; $scope.class;
    $scope.locationId;
    
    $scope.cartonEdit     = {};
    $scope.skuEdit        = {};
    $scope.shipperEdit    = {};
    $scope.locationEdit   = {};
    
    $scope.cartonNew      = cartonNew;
    $scope.cartonUpdate   = cartonUpdate;
    $scope.cartonDelete   = cartonDelete;
    
    $scope.skuNew         = skuNew;
    $scope.skuUpdate      = skuUpdate;
    $scope.skuDelete      = skuDelete;
    
    $scope.shipperNew     = shipperNew;
    $scope.shipperUpdate  = shipperUpdate;
    $scope.shipperDelete  = shipperDelete;
    
    $scope.locationNew    = locationNew;
    $scope.locationUpdate = locationUpdate;
    $scope.locationDelete = locationDelete;
    
    $scope.locationTypes  = ['box','config','forward','parts','reserve','secondary'];
    

    var refreshCounter = 0;

    function
    refreshCount(n,name)
    {
      refreshCounter += n;
      if(refreshCounter>0)
        Global.busyRefresh(true);
      else
        Global.busyRefresh(false);
      if(false){ //make true for logging
        console.log(name+": "+refreshCounter);
      }
    }
    
    
    // // // // //
    // EDIT ACTIONS
    
    // // //
    // CARTONS
    
    function
    cartonNew()
    {
      refreshCount(1,"cartonNew");
      DbFactory.post({topic: 'config', action: 'cartonNew',
                      params: {cartonType: $scope.cartonEdit.cartonType_new}
                     })
        .success(cartonSuccess)
        .error  (cartonError);
//      console.log("New carton "+$scope.cartonEdit.cartonType_new);
    }
    
    function
    cartonUpdate()
    {
      refreshCount(1,"cartonUpdate");
      DbFactory.post({topic: 'config', action: 'cartonUpdate',
                      params: {cartonType:    $scope.cartonType,
                               weight:        $scope.cartonEdit.weight,
                               length:        $scope.cartonEdit.length,
                               width:         $scope.cartonEdit.width,
                               height:        $scope.cartonEdit.height,
                               numKits:       $scope.cartonEdit.numKits,
                               numSmallKits:  $scope.cartonEdit.numSmallKits,
                               description:   $scope.cartonEdit.description}
                     })
        .success(cartonSuccess)
        .error  (cartonError);
//      console.log("Update carton "+$scope.cartonType+" with");
//      console.log($scope.cartonEdit);
    }
    
    function
    cartonDelete()
    {
      refreshCount(1,"cartonDelete");
      DbFactory.post({topic: 'config', action: 'cartonDelete',
                      params: {cartonType: $scope.cartonType}
                     })
        .success(cartonSuccess)
        .error  (cartonError);
//      console.log("Delete carton "+$scope.cartonType);
    }
    
    function
    cartonSuccess()
    {
      $scope.cartonType = '';
      $scope.cartonEdit = {};
      cartonsRefresh();
    }
    
    function
    cartonError(err)
    {
      console.log(err);
      refreshCount(-1,"cartonError");
    }
    
    // // //
    // SKUS
    
    function
    skuNew()
    {
      refreshCount(1,"skuNew");
      DbFactory.post({topic: 'config', action: 'skuNew',
                      params: {itemId: $scope.skuEdit.itemId_new}
                     })
        .success(skuSuccess)
        .error  (skuError);
//      console.log("New sku "+$scope.skuEdit.itemId_new);
    }
    
    function
    skuUpdate()
    {
      refreshCount(1,"skuUpdate");
      DbFactory.post({topic: 'config', action: 'skuUpdate',
                      params: {itemId:      $scope.itemId,
                               barcode:     $scope.skuEdit.barcode,
                               description: $scope.skuEdit.description,
                               weight:      $scope.skuEdit.weight,
                               isBox:       ($scope.skuEdit.isBox?"true":"false"),
                               isSubkit:    ($scope.skuEdit.isSubkit?"true":"false"),
                               isSerial:    ($scope.skuEdit.isSerial?"true":"false"),
                               notes:       $scope.skuEdit.notes}
                     })
        .success(skuSuccess)
        .error  (skuError);
//      console.log("Update sku "+$scope.itemId+" with");
//      console.log($scope.skuEdit);
    }
    
    function
    skuDelete()
    {
      refreshCount(1,"skuDelete");
      DbFactory.post({topic: 'config', action: 'skuDelete',
                      params: {itemId: $scope.itemId}
                     })
        .success(skuSuccess)
        .error  (skuError);
//      console.log("Delete sku "+$scope.itemId);
    }
    
    function
    skuSuccess()
    {
      $scope.itemId = '';
      $scope.skuEdit = {};
      skusRefresh();
    }
    
    function
    skuError(err)
    {
      console.log(err);
      refreshCount(-1,"skuError");
    }
    
    // // //
    // SHIPPERS
    
    function
    shipperNew()
    {
      refreshCount(1,"shipperNew");
      DbFactory.post({topic: 'config', action: 'shipperNew',
                      params: {carrier: $scope.shipperEdit.carrier_new,
                               class: $scope.shipperEdit.class_new}
                     })
        .success(shipperSuccess)
        .error  (shipperError);
//      console.log("New shipper "+$scope.shipperEdit.carrier_new+' '+$scope.shipperEdit.class_new);
    }
    
    function
    shipperUpdate()
    {
      refreshCount(1,"shipperUpdate");
      DbFactory.post({topic: 'config', action: 'shipperUpdate',
                      params: {carrier:     $scope.carrier,
                               class:       $scope.class,
                               sortLane:    $scope.shipperEdit.sortLane,
                               service:     $scope.shipperEdit.service,
                               description: $scope.shipperEdit.description}
                     })
        .success(shipperSuccess)
        .error  (shipperError);
//      console.log("Update shipper "+$scope.carrier+' '+$scope.class+" with");
//      console.log($scope.shipperEdit);
    }
    
    function
    shipperDelete()
    {
      refreshCount(1,"shipperDelete");
      DbFactory.post({topic: 'config', action: 'shipperDelete',
                      params: {carrier: $scope.carrier,
                               class: $scope.class}
                     })
        .success(shipperSuccess)
        .error  (shipperError);
//      console.log("Delete shipper "+$scope.carrier+' '+$scope.class);
    }
    
    function
    shipperSuccess()
    {
      $scope.carrier = '';
      $scope.class = '';
      $scope.shipperEdit = {};
      shippersRefresh();
    }
    
    function
    shipperError(err)
    {
      console.log(err);
      refreshCount(-1,"shipperError");
    }
    
    // // //
    // LOCATIONS
    
    function
    locationNew()
    {
      refreshCount(1,"locationNew");
      DbFactory.post({topic: 'config', action: 'locationNew',
                      params: {locationId: $scope.locationEdit.locationId_new}
                     })
        .success(locationSuccess)
        .error  (locationError);
//      console.log("New location "+$scope.locationEdit.locationId_new);
    }
    
    function
    locationUpdate()
    {
      refreshCount(1,"locationUpdate");
      DbFactory.post({topic: 'config', action: 'locationUpdate',
                      params: {locationId:   $scope.locationId,
                               locationType: $scope.locationEdit.locationType,
                               itemId:       $scope.locationEdit.itemId,
                               ordinal:      $scope.locationEdit.ordinal}
                     })
        .success(locationSuccess)
        .error  (locationError);
//      console.log("Update location "+$scope.locationId+" with");
//      console.log($scope.locationEdit);
    }
    
    function
    locationDelete()
    {
      refreshCount(1,"locationDelete");
      DbFactory.post({topic: 'config', action: 'locationDelete',
                      params: {locationId: $scope.locationId}
                     })
        .success(locationSuccess)
        .error  (locationError);
//      console.log("Delete location "+$scope.locationId);
    }
    
    function
    locationSuccess()
    {
      $scope.locationId = '';
      $scope.locationEdit = {};
      locationsRefresh();
    }
    
    function
    locationError(err)
    {
      console.log(err);
      refreshCount(-1,"locationError");
    }
    
    
    // // // // //
    // TABLES
    
    // // //
    // CARTONS
    
    function
    buildCartonsTable(data)
    {
      var cols = [];
      var ref = "#cartons";
      
      cols.push({title: "Type",         data:"cartonType"});
      cols.push({title: "Weight",       data:"weight",
                                        visible:false});
      cols.push({title: "Length",       data:"length",
                                        visible:false});
      cols.push({title: "Width",        data:"width",
                                        visible:false});
      cols.push({title: "Height",       data:"height",
                                        visible:false});
      cols.push({title: "Kits",         data:"numKits"});
      cols.push({title: "Small kits",   data:"numSmallKits"});
      cols.push({title: "Description",  data:"description"});
      
      if(cartonsTable)
      {
        cartonsTable.clear();
        cartonsTable.rows.add(data);
        cartonsTable.draw(false);
      } else {
        cartonsTable = $(ref)
                  .DataTable({data: data, 
                              columns: cols,
                              scrollY: "425px",
                              scrollCollapse: true,
                              paging: false,
                              dom: 'lftipr'});
        $(ref+' tbody').on('click','tr',cartonEditClick);
        setTimeout(function(){cartonsTable.draw();},0);
      }
      refreshCount(-1,"buildCartonsTable");
    }

    function
    cartonEditClick()
    {
      var data = cartonsTable.row(this).data();
      
      $scope.cartonType                = data.cartonType;
      $scope.cartonEdit.weight         = data.weight || 0;
      $scope.cartonEdit.length         = data.length || 0;
      $scope.cartonEdit.width          = data.width || 0;
      $scope.cartonEdit.height         = data.height || 0;
      $scope.cartonEdit.numKits        = data.numKits || 0;
      $scope.cartonEdit.numSmallKits   = data.numSmallKits || 0;
      $scope.cartonEdit.description    = data.description || '';
      $scope.cartonEdit.cartonType_new = '';
      
      $scope.$apply(); // really necessary?
    }
    
    // // //
    // SKUS
    
    function
    buildSkusTable(data)
    {
      var cols = [];
      var ref = "#skus";
      
      cols.push({title: "SKU",         data:"itemId"});
      cols.push({title: "Barcode",     data:"barcode",
                                       visible:false});
      cols.push({title: "Description", data:"description"});
      cols.push({title: "Weight",      data:"weight"});
      cols.push({title: "Box?",        data:"isBox",
                                       visible:false});
      cols.push({title: "Subkit?",     data:"isSubkit",
                                       visible:false});
      cols.push({title: "Serial?",     data:"isSerial",
                                       visible:false});
      cols.push({title: "Notes",       data:"notes"});
      
      if(skusTable)
      {
        skusTable.clear();
        skusTable.rows.add(data);
        skusTable.draw(false);
      } else {
        skusTable = $(ref)
                  .DataTable({data: data, 
                              columns: cols,
                              scrollY: "425px",
                              scrollCollapse: true,
                              paging: false,
                              dom: 'lftipr'});
        $(ref+' tbody').on('click','tr',skuEditClick);
        setTimeout(function(){skusTable.draw();},0);
      }
      refreshCount(-1,"buildSkusTable");
    }

    function
    skuEditClick()
    {
      var data = skusTable.row(this).data();
      
      $scope.itemId              = data.itemId;
      $scope.skuEdit.barcode     = data.barcode || '';
      $scope.skuEdit.description = data.description || '';
      $scope.skuEdit.weight      = data.weight || 0;
      $scope.skuEdit.isBox       = (data.isBox=="true")?true:false;
      $scope.skuEdit.isSubkit    = (data.isSubkit=="true")?true:false;
      $scope.skuEdit.isSerial    = (data.isSerial=="true")?true:false;
      $scope.skuEdit.notes       = data.notes || '';
      $scope.skuEdit.itemId_new  = '';
      
      $scope.$apply(); // really necessary?
    }
    
    // // //
    // SHIPPERS
    
    function
    buildShippersTable(data)
    {
      var cols = [];
      var ref = "#shippers";
      
      cols.push({title: "Carrier",     data:"carrier",
                                       visible:false});
      cols.push({title: "Class",       data:"class",
                                       visible:false});
      cols.push({title: "Description", data:"description"});
      cols.push({title: "Lane",        data:"sortLane"});
      cols.push({title: "Service",     data:"service"});
      
      if(shippersTable)
      {
        shippersTable.clear();
        shippersTable.rows.add(data);
        shippersTable.draw(false);
      } else {
        shippersTable = $(ref)
                  .DataTable({data: data, 
                              columns: cols,
                              scrollY: "425px",
                              scrollCollapse: true,
                              paging: false,
                              dom: 'lftipr'});
        $(ref+' tbody').on('click','tr',shipperEditClick);
        setTimeout(function(){shippersTable.draw();},0);
      }
      refreshCount(-1,"buildShippersTable");
    }

    function
    shipperEditClick()
    {
      var data = shippersTable.row(this).data();
      
      $scope.carrier                 = data.carrier;
      $scope.class                   = data.class;
      $scope.shipperEdit.sortLane    = data.sortLane || '';
      $scope.shipperEdit.service     = data.service || '';
      $scope.shipperEdit.description = data.description || '';
      $scope.shipperEdit.carrier_new = '';
      $scope.shipperEdit.class_new   = '';
      
      $scope.$apply(); // really necessary?
    }
    
    // // //
    // LOCATIONS
    
    function
    buildLocationsTable(data)
    {
      var cols = [];
      var ref = "#locations";
      
      cols.push({title: "Location", data:"locationId"});
      cols.push({title: "Type",     data:"locationType"});
      cols.push({title: "Item",     data:"itemId"});
      cols.push({title: "Ordinal",  data:"ordinal"});
      
      if(locationsTable)
      {
        locationsTable.clear();
        locationsTable.rows.add(data);
        locationsTable.draw(false);
      } else {
        locationsTable = $(ref)
                  .DataTable({data: data, 
                              columns: cols,
                              scrollY: "425px",
                              scrollCollapse: true,
                              paging: false,
                              dom: 'lftipr'});
        $(ref+' tbody').on('click','tr',locationEditClick);
        setTimeout(function(){locationsTable.draw();},0);
      }
      refreshCount(-1,"buildLocationsTable");
    }

    function
    locationEditClick()
    {
      var data = locationsTable.row(this).data();
      
      $scope.locationId                = data.locationId;
      $scope.locationEdit.locationType = data.locationType || 'forward';
      $scope.locationEdit.itemId       = data.itemId || '';
      $scope.locationEdit.ordinal      = data.ordinal || 0;
      
      $scope.$apply(); // really necessary?
    }
    
    
    // // // // //
    // REFRESHMENTS
    
    function
    refresh()
    {
      refreshCount(4,"refresh");
      cartonsRefresh();
      skusRefresh();
      shippersRefresh();
      locationsRefresh();
    }
    
    // // //
    // CARTONS
    
    function
    cartonsRefresh()
    {
      DbFactory.post({topic: 'config',
                      action: 'cartons'}) 
          .success(cartonsSuccess)
          .error  (cartonsError); 
    }
    
    function
    cartonsSuccess(data)
    {
      buildCartonsTable(data);
    }
    
    function
    cartonsError(err)
    {
      console.log(err);
      refreshCount(-1,"cartonsError");
    }
    
    // // //
    // SKUS
    
    function
    skusRefresh()
    {
      DbFactory.post({topic: 'config',
                      action: 'skus'}) 
          .success(skusSuccess)
          .error  (skusError); 
    }
    
    function
    skusSuccess(data)
    {
      buildSkusTable(data);
    }
    
    function
    skusError(err)
    {
      console.log(err);
      refreshCount(-1,"skusError");
    }
    
    // // //
    // SHIPPERS
    
    function
    shippersRefresh()
    {
      DbFactory.post({topic: 'config',
                      action: 'shippers'}) 
          .success(shippersSuccess)
          .error  (shippersError); 
    }
    
    function
    shippersSuccess(data)
    {
      buildShippersTable(data);
    }
    
    function
    shippersError(err)
    {
      console.log(err);
      refreshCount(-1,"shippersError");
    }
    
    // // //
    // LOCATIONS
    
    function
    locationsRefresh()
    {
      DbFactory.post({topic: 'config',
                      action: 'locations'}) 
          .success(locationsSuccess)
          .error  (locationsError); 
    }
    
    function
    locationsSuccess(data)
    {
      buildLocationsTable(data);
    }
    
    function
    locationsError(err)
    {
      console.log(err);
      refreshCount(-1,"locationsError");
    }
    
    
    // // // // //
    // INNIT?
    
    function
    init()
    {
      Global.setTitle('Configuration');
      Global.recv('refresh',refresh,$scope);
      refresh();
      periodic = $interval(refresh,30000);
    }
    
    $scope.$on('$destroy',function(){
      $interval.cancel(periodic);
    });
    
    init();
  }

  function
  configConfig($routeProvider)
  {
    $routeProvider
      .when('/config',{controller: 'ConfigController',
                       templateUrl: '/app/config/config.view.html'});
  }

}())