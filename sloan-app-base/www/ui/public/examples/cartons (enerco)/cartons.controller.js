(function()
  {
  angular
    .module('ui')
      .controller('CartonsController',cartonsController);

  angular
    .module('ui')
      .config(cartonsConfig);

  cartonsController.$inject = ['$scope','$timeout','$interval','Global','DbFactory'];
  
  function
  cartonsController($scope,$timeout,$interval,Global,DbFactory)
  {
    
    $scope.refresh = refresh;
    $scope.xpals = [];

    $scope.carton = {};
    $scope.storedSeq = '';
    $scope.lpn = "";
    $scope.cartonSeq = "";
    $scope.lookup = lookup;
    
    $scope.permit = Global.permit;
    
    var refreshCounter = 0;
    
    function
    refreshCount(n)
    {
      refreshCounter += n;
      if(refreshCounter>0)
        Global.busyRefresh(true);
      else
        Global.busyRefresh(false);
    }
    
    $scope.cartonRefreshEnabled = true;
    $scope.pauseRefreshSeconds = 120;
    $scope.pauseRefresh = pauseRefresh;
    
    function
    pauseRefresh()
    {
      $scope.cartonRefreshEnabled = false;
      setTimeout(function(){$scope.cartonRefreshEnabled = true},
                 (1000*$scope.pauseRefreshSeconds));
    }
    
    
    
    // // // // //
    // TABLE
    
    // // //
    // RECENT
    
    var recentTable = null;
    
    function
    dateRender(data,type,full,meta)
    {
      if(type!='display')
        return data;
      
      var date = new Date(data);
      return date.toLocaleString();
    }
    
    function
    buildRecentTable(data)
    {
      var cols = [];
      var ref = "#recents";
      
//      cols.push({title: "Sequence",       data:"seq"});
      cols.push({title: "Box",            data:"box"});
//      cols.push({title: "Carton sequence",data:"cartonSeq"});
      cols.push({title: "Barcode",        data:"barcode"});
//      cols.push({title: "Length",         data:"length"});
//      cols.push({title: "Width",          data:"width"});
//      cols.push({title: "Height",         data:"height"});
      cols.push({title: "Size",           data:"size"});
      cols.push({title: "Weight",         data:"weight"});
//      cols.push({title: "Xact",           data:"xact"});
      cols.push({title: "Tracking number",data:"trackingNumber"});
      cols.push({title: "UCC",data:"UCC"});
      cols.push({title: "Status",         data:"status"});
      cols.push({title: "Description",    data:"description"});
      cols.push({title: "Started",        data:"startStamp",  render: dateRender});
      cols.push({title: "Decided",        data:"decideStamp", render: dateRender});
      
      if(recentTable){
        recentTable.clear();
        recentTable.rows.add(data);
        recentTable.draw();
      } else {
        recentTable = $(ref)
                  .DataTable({data: data, 
                              columns: cols,
                              order: [],
                              scrollY: "500px",
                              scrollCollapse: true,
                              paging: false,
                              dom: 'ltB',
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','tr',selectClickRecent);
        setTimeout(function(){recentTable.draw();},0);
      }
      refreshCount(-1);
//      console.log("buildRecentTable: "+refreshCounter);
    }
    
    function
    selectClickRecent()
    {
      var data = recentTable.row(this).data();
      
      if(data.barcode){
        $scope.lpn = data.barcode;
      }
      if(data.cartonSeq){
        $scope.cartonSeq = data.cartonSeq;
        $scope.selected+=2;
        lookupSeq();
      } else if(data.barcode){
        $scope.selected+=2;
        lookup();
      }
    }
    
    // // //
    // SORTER
    
    var sorterTable = null;
    
    function
    dateRender(data,type,full,meta)
    {
      if(type!='display')
        return data;
      
      var date = new Date(data);
      return date.toLocaleString();
    }
    
    function
    buildSorterTable(data)
    {
      var cols = [];
      var ref = "#sorters";
      
//      cols.push({title: "Sequence",       data:"seq"});
      cols.push({title: "Box",            data:"box"});
      //cols.push({title: "Carton sequence",data:"cartonSeq"});
      cols.push({title: "Barcode",        data:"barcode"});
      cols.push({title: "Order ID",       data:"orderID"});
      cols.push({title: "Carrier",        data:"carrier"});
      cols.push({title: "Lane",           data:"lane"});
      //cols.push({title: "Status",         data:"status"});
      cols.push({title: "Description",    data:"description"});
      cols.push({title: "Started",        data:"startStamp",  render: dateRender});
      
      if(sorterTable){
        sorterTable.clear();
        sorterTable.rows.add(data);
        sorterTable.draw();
      } else {
        sorterTable = $(ref)
                  .DataTable({data: data, 
                              columns: cols,
                              order: [],
                              scrollY: "500px",
                              scrollCollapse: true,
                              paging: false,
                              dom: 'ltB',
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','tr',selectClickSorter);
        setTimeout(function(){sorterTable.draw();},0);
      }
      refreshCount(-1);
//      console.log("buildSorterTable: "+refreshCounter);
    }
    
    function
    selectClickSorter()
    {
      var data = sorterTable.row(this).data();
      
      if(data.barcode){
        $scope.lpn = data.barcode;
      }
      if(data.cartonSeq){
        $scope.cartonSeq = data.cartonSeq;
        $scope.selected++;
        lookupSeq();
      } else if(data.barcode){
        $scope.selected++;
        lookup();
      }
    }
    
    // // //
    // HISTORY
    
    var historyTable = null;
    
    function
    buildHistoryTable(data)
    {
      var cols = [];
      var ref = "#history";

      cols.push({title: "Stamp",   data:"stamp",
                                   render:dateRender});
      cols.push({title: "Description",data:"description"});

      if(historyTable){
        historyTable.clear();
        historyTable.rows.add(data);
        historyTable.draw();
      } else {
        historyTable = $(ref)
          .DataTable({data: data, 
                      columns: cols,
                      order: [],
                      scrollY: '220px',
                      scrollCollapse: true,
                      paging: false,
                      dom: 'ltB',
                      buttons: ['copy','print','excel','pdf']});
        setTimeout(function(){historyTable.draw();},0);
      }
      refreshCount(-1);
//      console.log("buildHistoryTable: "+refreshCounter);
    }
    
    // // //
    // PICKS
    
    var picksTable = null;
    
    function
    buildPicksTable(data)
    {
      var cols = [];
      var ref = "#picks";
      
      cols.push({title: "Pick ID",  data:"pickID"});
      cols.push({title: "SKU",      data:"sku"});
      //cols.push({title: "UPC",      data:"upc"});
      cols.push({title: "Location", data:"location"});
      cols.push({title: "Qty", data:"qty"});
      cols.push({title: "Picked Qty", data:"actQty"});
      cols.push({title: "Picked", data:"picked"});
      cols.push({title: "Uploaded", data:"uploaded"});
      cols.push({title: "Stamp",    data:"stamp",
                                    render:dateRender});

      if(picksTable){
        picksTable.clear();
        picksTable.rows.add(data);
        picksTable.draw();
      } else {
        picksTable = $(ref)
          .DataTable({data: data, 
                      columns: cols,
                      rowCallback: pickCallback,
                      order: [],
                      scrollY: '220px',
                      scrollCollapse: true,
                      paging: false,
                      dom: 'ltB',
                      buttons: ['copy','print','excel','pdf']});
        setTimeout(function(){picksTable.draw();},0);
      }
      refreshCount(-1);
//      console.log("buildpicksTable: "+refreshCounter);
    }
    function
    pickCallback(row,data,index)
    {
      if(data.picked){
        $('td:eq(5)',row).html('<div style="text-align: left"><b>&check;</b></div>');
      } else {
        $('td:eq(5)',row).html('');
      }
      if(data.uploaded){
        $('td:eq(6)',row).html('<div style="text-align: left"><b>&check;</b></div>');
      } else {
        $('td:eq(6)',row).html('');
      }
    }
 

    // // // // //
    // DATA RETRIEVAL

    // // //
    // XPAL (RECENT)

    function
    refreshXpal()
    {
      DbFactory.post({topic: 'carton',
                      action: 'xpal'
                     })
        .success(xpalSuccess)
        .error  (xpalError);
    }
    
    function 
    xpalSuccess(cartons)
    { buildRecentTable(cartons); }
    
    function
    xpalError(err)
    {
      console.log(err);
      refreshCount(-1);
//      console.log("xpalError: "+refreshCounter);
    }

    // // //
    // SORTER

    function
    refreshSorter()
    {
      DbFactory.post({topic: 'carton',
                      action: 'sorter'
                     })
        .success(sorterSuccess)
        .error  (sorterError);
    }
    
    function 
    sorterSuccess(cartons)
    { buildSorterTable(cartons); }
    
    function
    sorterError(err)
    {
      console.log(err);
      refreshCount(-1);
//      console.log("xpalError: "+refreshCounter);
    }
    
    // // //
    // DETAILS
    
    function
    someError(err)
    { console.log(err); }
    
    function
    lookupSuccess(carton)
    {
      if(carton[0]){
        $scope.carton = carton[0];
        $scope.storedSeq = $scope.carton.cartonSeq;
      } else {
        alert("That carton was not found.");
      }
//      storeGValue("cartonDetails","cartonSeq",$scope.storedLpn);
      refreshPicks($scope.storedSeq);
      refreshLogs($scope.storedSeq);
    }
    
    function
    lookupError(err)
    {
      $scope.carton = {};
//      $scope.picks = [];
//      $scope.logs = [];    
      console.log(err);
    }
    
    function
    lookup()
    {
      refreshCount(2);
//      console.log("lookup: "+refreshCounter);
      DbFactory.post({topic: 'carton',
                      action: 'lookup',
                      params: {barcode: $scope.lpn}
                     })
        .success(lookupSuccess)
        .error  (lookupError);
//      storeCartonSeq();
//      $scope.lpn = "";
    }
    
    function
    lookupSeq()
    {
      refreshCount(2);
//      console.log("lookup: "+refreshCounter);
      DbFactory.post({topic: 'carton',
                      action: 'lookupSeq',
                      params: {seq: $scope.cartonSeq}
                     })
        .success(lookupSuccess)
        .error  (lookupError);
//      storeCartonSeq();
//      $scope.lpn = "";
    }
    
    function
    refreshCartonError(err)
    { 
      $scope.carton = {};
      console.log(err);
      refreshCount(-1);
//      console.log("refreshCartonError: "+refreshCounter);
    }

    function 
    refreshCartonSuccess(carton)                
    {
      $scope.carton = carton[0];
      refreshPicks(carton[0].cartonSeq);
      refreshLogs(carton[0].cartonSeq);
    }
    
    // // //
    // HISTORY/PICKS
    
    function
    logSuccess(logs)
    { buildHistoryTable(logs); }
    
    function
    logError(err)
    {
      if(historyTable){
        historyTable.clear();
      }
      console.log(err);
      refreshCount(-1);
//      console.log("logError: "+refreshCounter);
    }

    function
    refreshLogs(cartonSeq)
    {
      if(cartonSeq){
        DbFactory.post({topic: 'carton',
                        action: 'historySeq',
                        params: {seq: cartonSeq}})
          .success(logSuccess)
          .error  (logError);
      } else {
        refreshCount(-1);
//        console.log("refreshLogs: "+refreshCounter);
      }
    }

    function
    picksSuccess(picks)
    { buildPicksTable(picks); }

    function
    picksError(data,status,headers,config)  
    {
      if(picksTable){
        picksTable.clear();
      }
      console.log(err);
      refreshCount(-1);
//      console.log("picksError: "+refreshCounter);
    }

    function
    refreshPicks(cartonSeq)
    {
      if(cartonSeq){
        DbFactory.post({topic: 'carton',
                        action: 'picksSeq',
                        params: {seq: cartonSeq}})
          .success(picksSuccess)
          .error  (picksError);
      } else {
        refreshCount(-1);
//        console.log("refreshLogs: "+refreshCounter);
      }
    }
    
    
    // // // // //
    // SETUP AND ALL THAT

    function
    refresh()
    {
      if($scope.cartonRefreshEnabled){
        refreshCount(3);
//        console.log("refresh: "+refreshCounter);
        refreshRecents();
        refreshDetails();
      }
    }

    function
    refreshRecents()
    {
      refreshXpal();
      refreshSorter();
    }

    function 
    refreshDetails()
    {
      if($scope.storedSeq){
        DbFactory.post({topic: 'carton',
                        action: 'lookupSeq',
                        params: {seq: $scope.storedSeq}
                       })
          .success(refreshCartonSuccess)
          .error  (refreshCartonError);
      } else {
        refreshCount(-1);
//        console.log("refreshDetails: "+refreshCounter);
      }
    }

    function
    init()
    {
      Global.setTitle('Cartons');
      refresh();
      Global.recv('refresh',refresh,$scope);
      periodic = $interval(refresh, 5000);
    }

    init();

    $scope.$on('$destroy', function(){
      $interval.cancel(periodic);
    });
    
  }

  function
  cartonsConfig($routeProvider)
  {
    $routeProvider
      .when('/cartons', {controller: 'CartonsController',
                         templateUrl: '/app/cartons/cartons.view.html'});
  }
  
}())
