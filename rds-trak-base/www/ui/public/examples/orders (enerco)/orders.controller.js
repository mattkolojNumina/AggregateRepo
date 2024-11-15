(function()
{
  angular
    .module('ui')
      .controller('OrdersController',ordersController);

  angular
    .module('ui')
      .config(ordersConfig);

  ordersController.$inject = ['$scope','$timeout','$interval','Global','DbFactory'];
  
  function
  ordersController($scope,$timeout,$interval,Global,DbFactory)
  {
    
    $scope.refresh = refresh;
    
    $scope.order = {};
    $scope.storedId = '';
    $scope.orderId = "";
    $scope.lookup = lookup;
    
    $scope.permit = Global.permit;
    
    $scope.barShown = 'tool';
    $scope.showCancel = showCancel;
    $scope.showRelease = showRelease;
    $scope.barReset = barReset;
    
    $scope.orderCancel = orderCancel;
    $scope.orderRelease = orderRelease;
    
    $scope.priorityPlus = priorityPlus;
    $scope.priorityMinus = priorityMinus;
    
    
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
    
    
    $scope.orderRefreshEnabled = true;
    $scope.pauseRefreshSeconds = 120;
    $scope.pauseRefresh = pauseRefresh;
    
    function
    pauseRefresh()
    {
      $scope.orderRefreshEnabled = false;
      setTimeout(function(){$scope.orderRefreshEnabled = true},
                 (1000*$scope.pauseRefreshSeconds));
    }
    
    
    // // // // //
    // ORDER COMMANDS
    
    function
    showCancel()
    { $scope.barShown = 'cancel'; }
    
    function
    showRelease()
    { $scope.barShown = 'release'; }
    
    function
    barReset()
    { $scope.barShown = 'tool'; }
    
    function
    orderUpdateError(err)
    { 
      console.log(err);
      refreshCount(-3);
//      console.log("orderUpdateError: "+refreshCounter);
    }
    
    function
    orderCancel()
    {
      if($scope.storedId){
        refreshCount(3);
        DbFactory.post({topic: 'order',
                        action: 'setStatus',
                        params: {orderID: $scope.storedId,
                                 status: 'cancelled'}
                       })
          .success(refreshDetails)
          .error  (orderUpdateError);
      }
      barReset();
    }
    
    function
    orderRelease()
    {
      if($scope.storedId){
        refreshCount(3);
        DbFactory.post({topic: 'order',
                        action: 'setStatus',
                        params: {orderID: $scope.storedId,
                                 status: 'released'}
                       })
          .success(refreshDetails)
          .error  (orderUpdateError);
      }
      barReset();
    }
    
    function
    priorityPlus()
    {
      if($scope.storedId){
        refreshCount(3);
        DbFactory.post({topic: 'order',
                        action: 'priorityPlus',
                        params: {orderID: $scope.storedId}
                       })
          .success(refreshDetails)
          .error  (orderUpdateError);
      }
    }
    
    function
    priorityMinus()
    {
      if($scope.storedId){
        refreshCount(3);
        DbFactory.post({topic: 'order',
                        action: 'priorityMinus',
                        params: {orderID: $scope.storedId}
                       })
          .success(refreshDetails)
          .error  (orderUpdateError);
      }
    }
    
    
    // // // // //
    // TABLE HELPERS
    
    function
    simpleDate(data)
    {
      if(data==null)
        return '';
      var date = new Date(data);
      var today = new Date();
      if(today.toDateString()==date.toDateString())
        return date.toLocaleTimeString();
      return date.toLocaleString();
    }

    function
    milestonesCallback(columnsSkipped)
    {
      return (function(row,data,index)
      {
        var downloadHtml  = data.downloadStamp?
            ('<div class="tooltip"><b>&check;</b><span class="tooltiptext">'
             +simpleDate(data.downloadStamp)+'</span></div>'):'';
        $('td:eq('+(columnsSkipped+0)+')',row).html(downloadHtml);
        
        var releasedHtml  = data.releasedStamp?
            ('<div class="tooltip"><b>&check;</b><span class="tooltiptext">'
             +simpleDate(data.releasedStamp)+'</span></div>'):'';
        $('td:eq('+(columnsSkipped+1)+')',row).html(releasedHtml);
        
        var startedHtml   = data.startedStamp?
            ('<div class="tooltip"><b>&check;</b><span class="tooltiptext">'
             +simpleDate(data.startedStamp)+'</span></div>'):'';
        $('td:eq('+(columnsSkipped+2)+')',row).html(startedHtml);
        
        var completedHtml = data.completedStamp?
            ('<div class="tooltip"><b>&check;</b><span class="tooltiptext">'
             +simpleDate(data.completedStamp)+'</span></div>'):'';
        $('td:eq('+(columnsSkipped+3)+')',row).html(completedHtml);
      });
    }
    
    
    // // // // //
    // TABLE
    
    function
    dateRender(data,type,full,meta)
    {
      if(type!='display')
        return data;
      
      var date = new Date(data);
      return date.toLocaleString();
    }
    
    // // //
    // ORDERS-ALL
    
    var ordersAllTable = null;
    
    function
    buildOrdersAllTable(data)
    {
      var cols = [];
      var ref = "#ordersAll";
      
      cols.push({title: "Order number", data:"orderID"});
      cols.push({title: "Order type",   data:"orderType"});
      cols.push({title: "Status",       data:"status"});
      cols.push({title: "Downloaded",   data:"downloadStamp"});
      cols.push({title: "Released",     data:"releasedStamp"});
      cols.push({title: "Started",      data:"startedStamp"});
      cols.push({title: "Completed",    data:"completedStamp"});
      //cols.push({title: "Stamp",        data:"stamp", render: dateRender});
      
      if(ordersAllTable){
        ordersAllTable.clear();
        ordersAllTable.rows.add(data);
        ordersAllTable.draw();
      } else {
        ordersAllTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              rowCallback: milestonesCallback(3),
                              order: [],
                              scrollY: "500px",
                              scrollCollapse: true,
                              paging: false,
                              dom: 'ltB',
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','tr',selectClickOrdersAll);
        setTimeout(function(){ordersAllTable.draw();},0);
      }
      refreshCount(-1);
//      console.log("buildOrdersAllTable: "+refreshCounter);
    }
    
    function
    selectClickOrdersAll()
    {
      var data = ordersAllTable.row(this).data();
      
      if(data.orderID){
        $scope.orderId = data.orderID;
        $scope.selected = 7;
        lookup();
      }
    }
    
    // // //
    // ORDERS-UNASSIGNED
    
    var ordersUnassignedTable = null;
    
    function
    buildOrdersUnassignedTable(data)
    {
      var cols = [];
      var ref = "#ordersUnassigned";
      
      cols.push({title: "Order number", data:"orderID"});
      cols.push({title: "Priority",     data:"priority"});
      cols.push({title: "Customer",     data:"customer"});
      cols.push({title: "Downloaded",   data:"downloadStamp"});
      cols.push({title: "Released",     data:"releasedStamp"});
      cols.push({title: "Started",      data:"startedStamp"});
      cols.push({title: "Completed",    data:"completedStamp"});
      //cols.push({title: "Stamp",        data:"stamp", render: dateRender});
      
      if(ordersUnassignedTable){
        ordersUnassignedTable.clear();
        ordersUnassignedTable.rows.add(data);
        ordersUnassignedTable.draw();
      } else {
        ordersUnassignedTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              rowCallback: milestonesCallback(3),
                              order: [],
                              scrollY: "500px",
                              scrollCollapse: true,
                              paging: false,
                              dom: 'ltB',
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','tr',selectClickOrdersUnassigned);
        setTimeout(function(){ordersUnassignedTable.draw();},0);
      }
      refreshCount(-1);
//      console.log("buildOrdersUnassignedTable: "+refreshCounter);
    }
    
    function
    selectClickOrdersUnassigned()
    {
      var data = ordersUnassignedTable.row(this).data();
      
      if(data.orderID){
        $scope.orderId = data.orderID;
        $scope.selected = 7;
        lookup();
      }
    }
    
    // // //
    // ORDERS-IN PROCESS
    
    var ordersInProcessTable = null;
    
    function
    buildOrdersInProcessTable(data)
    {
      var cols = [];
      var ref = "#ordersInProcess";
      
      cols.push({title: "Order number", data:"orderID"});
      cols.push({title: "Order type",   data:"orderType"});
      cols.push({title: "Status",       data:"status"});
//      cols.push({title: "Error type",        data:"errorType"});
      cols.push({title: "Downloaded",   data:"downloadStamp"});
      cols.push({title: "Released",     data:"releasedStamp"});
      cols.push({title: "Started",      data:"startedStamp"});
      cols.push({title: "Completed",    data:"completedStamp"});
      //cols.push({title: "Stamp",        data:"stamp", render: dateRender});
      
      if(ordersInProcessTable){
        ordersInProcessTable.clear();
        ordersInProcessTable.rows.add(data);
        ordersInProcessTable.draw();
      } else {
        ordersInProcessTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              rowCallback: milestonesCallback(3),
                              order: [],
                              scrollY: "500px",
                              scrollCollapse: true,
                              paging: false,
                              dom: 'ltB',
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','tr',selectClickOrdersInProcess);
        setTimeout(function(){ordersInProcessTable.draw();},0);
      }
      refreshCount(-1);
//      console.log("buildOrdersInProcessTable: "+refreshCounter);
    }
    
    function
    selectClickOrdersInProcess()
    {
      var data = ordersInProcessTable.row(this).data();
      
      if(data.orderID){
        $scope.orderId = data.orderID;
        $scope.selected = 7;
        lookup();
      }
    }
    
    // // //
    // ORDERS-ERROR
    
    var ordersErrorTable = null;
    
    function
    buildOrdersErrorTable(data)
    {
      var cols = [];
      var ref = "#ordersError";
      
      cols.push({title: "Order number", data:"orderID"});
      cols.push({title: "Order type",   data:"orderType"});
      cols.push({title: "Error type",       data:"errorType"});
      cols.push({title: "Downloaded",   data:"downloadStamp"});
      cols.push({title: "Released",     data:"releasedStamp"});
      cols.push({title: "Started",      data:"startedStamp"});
      cols.push({title: "Completed",    data:"completedStamp"});
      //cols.push({title: "Stamp",        data:"stamp", render: dateRender});
      
      if(ordersErrorTable){
        ordersErrorTable.clear();
        ordersErrorTable.rows.add(data);
        ordersErrorTable.draw();
      } else {
        ordersErrorTable = $(ref)
                  .DataTable({data: data, 
                              columns: cols,
                              rowCallback: milestonesCallback(3),
                              order: [],
                              scrollY: "500px",
                              scrollCollapse: true,
                              paging: false,
                              dom: 'ltB',
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','tr',selectClickOrdersError);
        setTimeout(function(){ordersErrorTable.draw();},0);
      }
      refreshCount(-1);
//      console.log("buildOrdersErrorTable: "+refreshCounter);
    }
    
    function
    selectClickOrdersError()
    {
      var data = ordersErrorTable.row(this).data();
      
      if(data.orderID){
        $scope.orderId = data.orderID;
        $scope.selected = 7;
        lookup();
      }
    }
    
    // // //
    // ORDERS-AGED
    
    var ordersAgedTable = null;
    
    function
    buildOrdersAgedTable(data)
    {
      var cols = [];
      var ref = "#ordersAged";
      
      cols.push({title: "Order number", data:"orderID"});
      cols.push({title: "Order type",   data:"orderType"});
      cols.push({title: "Status",       data:"status"});
      cols.push({title: "Downloaded",   data:"downloadStamp"});
      cols.push({title: "Released",     data:"releasedStamp"});
      cols.push({title: "Started",      data:"startedStamp"});
      cols.push({title: "Completed",    data:"completedStamp"});
      //cols.push({title: "Stamp",        data:"stamp", render: dateRender});
      
      if(ordersAgedTable){
        ordersAgedTable.clear();
        ordersAgedTable.rows.add(data);
        ordersAgedTable.draw();
      } else {
        ordersAgedTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              rowCallback: milestonesCallback(3),
                              order: [],
                              scrollY: "500px",
                              scrollCollapse: true,
                              paging: false,
                              dom: 'ltB',
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','tr',selectClickOrdersAged);
        setTimeout(function(){ordersAgedTable.draw();},0);
      }
      refreshCount(-1);
//      console.log("buildOrdersAgedTable: "+refreshCounter);
    }
    
    function
    selectClickOrdersAged()
    {
      var data = ordersAgedTable.row(this).data();
      
      if(data.orderID){
        $scope.orderId = data.orderID;
        $scope.selected = 7;
        lookup();
      }
    }
    
    // // //
    // ORDERS-CANCELLED
    
    var ordersCancelledTable = null;
    
    function
    buildOrdersCancelledTable(data)
    {
      var cols = [];
      var ref = "#ordersCancelled";
      
      cols.push({title: "Order number", data:"orderID"});
      cols.push({title: "Order type",   data:"orderType"});
      cols.push({title: "Error type",   data:"errorType"});
      cols.push({title: "Downloaded",   data:"downloadStamp"});
      cols.push({title: "Released",     data:"releasedStamp"});
      cols.push({title: "Started",      data:"startedStamp"});
      cols.push({title: "Completed",    data:"completedStamp"});
      //cols.push({title: "Stamp",        data:"stamp", render: dateRender});
      
      if(ordersCancelledTable){
        ordersCancelledTable.clear();
        ordersCancelledTable.rows.add(data);
        ordersCancelledTable.draw();
      } else {
        ordersCancelledTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              rowCallback: milestonesCallback(3),
                              order: [],
                              scrollY: "500px",
                              scrollCollapse: true,
                              paging: false,
                              dom: 'ltB',
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','tr',selectClickOrdersCancelled);
        setTimeout(function(){ordersCancelledTable.draw();},0);
      }
      refreshCount(-1);
//      console.log("buildOrdersCancelledTable: "+refreshCounter);
    }
    
    function
    selectClickOrdersCancelled()
    {
      var data = ordersCancelledTable.row(this).data();
      
      if(data.orderID){
        $scope.orderId = data.orderID;
        $scope.selected = 7;
        lookup();
      }
    }
    
    // // //
    // ORDERS-COMPLETE
    
    var ordersCompleteTable = null;
    
    function
    buildOrdersCompleteTable(data)
    {
      var cols = [];
      var ref = "#ordersComplete";
      
      cols.push({title: "Order number", data:"orderID"});
      cols.push({title: "Order type",   data:"orderType"});
//      cols.push({title: "Status",       data:"status"});
      cols.push({title: "Downloaded",   data:"downloadStamp"});
      cols.push({title: "Released",     data:"releasedStamp"});
      cols.push({title: "Started",      data:"startedStamp"});
      cols.push({title: "Completed",    data:"completedStamp"});
      //cols.push({title: "Stamp",        data:"stamp", render: dateRender});
      
      if(ordersCompleteTable){
        ordersCompleteTable.clear();
        ordersCompleteTable.rows.add(data);
        ordersCompleteTable.draw();
      } else {
        ordersCompleteTable = $(ref)
                  .DataTable({data: data, 
                              columns: cols,
                              rowCallback: milestonesCallback(2),
                              order: [],
                              scrollY: "500px",
                              scrollCollapse: true,
                              paging: false,
                              dom: 'ltB',
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','tr',selectClickOrdersComplete);
        setTimeout(function(){ordersCompleteTable.draw();},0);
      }
      refreshCount(-1);
//      console.log("buildOrdersCompleteTable: "+refreshCounter);
    }
    
    function
    selectClickOrdersComplete()
    {
      var data = ordersCompleteTable.row(this).data();
      
      if(data.orderID){
        $scope.orderId = data.orderID;
        $scope.selected = 7;
        lookup();
      }
    }
    
    // // //
    // PALLETS
    
    var palletsTable = null;
    
    function
    buildPalletsTable(data)
    {
      var cols = [];
      var ref = "#pallets";

      cols.push({title: "Pallet LPN",     data:"palletLpn"});
      cols.push({title: "Pallet number",  data:"palletNumber"});
      cols.push({title: "Pallet weight",  data:"weight"});
      cols.push({title: "Built",          data:"built"});
      cols.push({title: "Confirmed",      data:"confirmed"});

      if(palletsTable){
         palletsTable.clear();
         palletsTable.rows.add(data);
         palletsTable.draw();
      } else {
        palletsTable = $(ref)
          .DataTable({data: data,
                      columns: cols,
                      rowCallback: palletCallback,
                      order: [],
                      scrollY: '220px',
                      scrollCollapse: true,
                      paging: false,
                      dom: 'ltB',
                      buttons: ['copy','print','excel','pdf']});
        setTimeout(function(){palletsTable.draw();},0);
      }
      refreshCount(-1);
//      console.log("buildPalletsTable: "+refreshCounter);
    }
    
    function
    palletCallback(row,data,index)
    {
      if(data.built){
        $('td:eq(3)',row).html('<div style="text-align: center"><b>&check;</b></div>');
      } else {
        $('td:eq(3)',row).html('');
      }
      if(data.confirmed){
        $('td:eq(4)',row).html('<div style="text-align: center"><b>&check;</b></div>');
      } else {
        $('td:eq(4)',row).html('');
      }
    }
    
    // // //
    // CARTONS
    
    var cartonsTable = null;
    
    function
    buildCartonsTable(data)
    {
      var cols = [];
      var ref = "#cartons";

      cols.push({title: "Carton Seq",   data:"cartonSeq"});
      cols.push({title: "LPN",          data:"cartonLpn"});
      cols.push({title: "Type",         data:"cartonType"});
      cols.push({title: "palletLpn",    data:"shipPalletLpn"});
      cols.push({title: "Picked",       data:"picked"});
      cols.push({title: "Audit",        data:"audit"});
      cols.push({title: "Packed",       data:"packed"});
      cols.push({title: "Sorted",       data:"sorted"});
      cols.push({title: "Confirmed",    data:"confirmed"});
      cols.push({title: "Completed",    data:"completed"});
      //cols.push({title: "Length",       data:"length"});
      //cols.push({title: "Width",        data:"width"});
      //cols.push({title: "Height",       data:"height"});
      //cols.push({title: "Est. weight",  data:"estimatedWeight"});
      //cols.push({title: "Stamp",        data:"stamp", render:dateRender});

      if(cartonsTable){
        cartonsTable.clear();
        cartonsTable.rows.add(data);
        cartonsTable.draw();
      } else {
        cartonsTable = $(ref)
          .DataTable({data: data, 
                      columns: cols,
                      rowCallback: orderCartonCallback,
                      order: [],
                      scrollY: '220px',
                      scrollCollapse: true,
                      paging: false,
                      dom: 'ltB',
                      buttons: ['copy','print','excel','pdf']});
        setTimeout(function(){cartonsTable.draw();},0);
      }
      refreshCount(-1);
//      console.log("buildCartonsTable: "+refreshCounter);
    }
    
    function
    orderCartonCallback(row,data,index)
    {
      if(data.picked){
        $('td:eq(4)',row).html('<div style="text-align: left"><b>&check;</b></div>');
      } else {
        $('td:eq(4)',row).html('');
      }
      if(data.audit){
        $('td:eq(5)',row).html('<div style="text-align: left"><b>&check;</b></div>');
      } else {
        $('td:eq(5)',row).html('');
      }
      if(data.packed){
        $('td:eq(6)',row).html('<div style="text-align: left"><b>&check;</b></div>');
      } else {
        $('td:eq(6)',row).html('');
      }
      if(data.sorted){
        $('td:eq(7)',row).html('<div style="text-align: left"><b>&check;</b></div>');
      } else {
        $('td:eq(7)',row).html('');
      }
      if(data.confirmed){
        $('td:eq(8)',row).html('<div style="text-align: left"><b>&check;</b></div>');
      } else {
        $('td:eq(8)',row).html('');
      }
      if(data.completed){
        $('td:eq(9)',row).html('<div style="text-align: left"><b>&check;</b></div>');
      } else {
        $('td:eq(9)',row).html('');
      }
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
      cols.push({title: "LPN",      data:"cartonLpn"});
      cols.push({title: "SKU",      data:"sku"});
      cols.push({title: "Location", data:"location"});
      cols.push({title: "Qty",      data:"qty"});
      cols.push({title: "Picked Qty", data:"actQty"});
      cols.push({title: "Picked",   data:"picked"});
      cols.push({title: "Uploaded", data:"uploaded"});
      cols.push({title: "Qty",      data:"qty"});
      //cols.push({title: "Stamp",    data:"stamp", render:dateRender});

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
        $('td:eq(6)',row).html('<div style="text-align: left"><b>&check;</b></div>');
      } else {
        $('td:eq(6)',row).html('');
      }
      if(data.uploaded){
        $('td:eq(7)',row).html('<div style="text-align: left"><b>&check;</b></div>');
      } else {
        $('td:eq(7)',row).html('');
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
      
      cols.push({title: "Stamp",      data:"stamp",
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
//      console.log("buildhistoryTable: "+refreshCounter);
    }
    

    // // // // //
    // DATA RETRIEVAL
    
    // // //
    // ORDERS-ALL

    function
    refreshOrdersAll()
    {
      DbFactory.post({topic: 'order',
                      action: 'all'
                     })
        .success(ordersAllSuccess)
        .error  (ordersAllError);
    }
    
    function 
    ordersAllSuccess(data)
    { buildOrdersAllTable(data); }
    
    function
    ordersAllError(err)
    {
      console.log(err);
      refreshCount(-1);
//      console.log("ordersAllError: "+refreshCounter);
    }
    
    // // //
    // ORDERS-UNASSIGNED

    function
    refreshOrdersUnassigned()
    {
      DbFactory.post({topic: 'order',
                      action: 'unassigned'
                     })
        .success(ordersUnassignedSuccess)
        .error  (ordersUnassignedError);
    }
    
    function 
    ordersUnassignedSuccess(data)
    { buildOrdersUnassignedTable(data); }
    
    function
    ordersUnassignedError(err)
    {
      console.log(err);
      refreshCount(-1);
//      console.log("ordersUnassignedError: "+refreshCounter);
    }
    
    // // //
    // ORDERS-IN PROCESS
    
    function
    refreshOrdersInProcess()
    {
      DbFactory.post({topic: 'order',
                      action: 'inProcess'
                     })
        .success(ordersInProcessSuccess)
        .error  (ordersInProcessError);
    }
    
    function 
    ordersInProcessSuccess(data)
    { buildOrdersInProcessTable(data); }
    
    function
    ordersInProcessError(err)
    {
      console.log(err);
      refreshCount(-1);
//      console.log("ordersInProcessError: "+refreshCounter);
    }
    
    // // //
    // ORDERS-ERROR

    function
    refreshOrdersError()
    {
      DbFactory.post({topic: 'order',
                      action: 'error'
                     })
        .success(ordersErrorSuccess)
        .error  (ordersErrorError);
    }
    
    function 
    ordersErrorSuccess(data)
    { buildOrdersErrorTable(data); }
    
    function
    ordersErrorError(err)
    {
      console.log(err);
      refreshCount(-1);
//      console.log("ordersErrorError: "+refreshCounter);
    }
    
    // // //
    // ORDERS-AGED
    
    function
    refreshOrdersAged()
    {
      DbFactory.post({topic: 'order',
                      action: 'aged'
                     })
        .success(ordersAgedSuccess)
        .error  (ordersAgedError);
    }
    
    function 
    ordersAgedSuccess(data)
    { buildOrdersAgedTable(data); }
    
    function
    ordersAgedError(err)
    {
      console.log(err);
      refreshCount(-1);
//      console.log("ordersAgedError: "+refreshCounter);
    }
    
    // // //
    // ORDERS-CANCELLED
    
    function
    refreshOrdersCancelled()
    {
      DbFactory.post({topic: 'order',
                      action: 'cancelled'
                     })
        .success(ordersCancelledSuccess)
        .error  (ordersCancelledError);
    }
    
    function 
    ordersCancelledSuccess(data)
    { buildOrdersCancelledTable(data); }
    
    function
    ordersCancelledError(err)
    {
      console.log(err);
      refreshCount(-1);
//      console.log("ordersCancelledError: "+refreshCounter);
    }
    
    // // //
    // ORDERS-COMPLETE
    
    function
    refreshOrdersComplete()
    {
      DbFactory.post({topic: 'order',
                      action: 'complete'
                     })
        .success(ordersCompleteSuccess)
        .error  (ordersCompleteError);
    }
    
    function 
    ordersCompleteSuccess(data)
    { buildOrdersCompleteTable(data); }
    
    function
    ordersCompleteError(err)
    {
      console.log(err);
      refreshCount(-1);
//      console.log("ordersCompleteError: "+refreshCounter);
    }
    
    // // //
    // DETAILS
    
    function
    someError(err)
    { console.log(err); }
    
    function
    lookupSuccess(order)
    {
      if(order[0]){
        $scope.order = order[0];
        $scope.storedId = $scope.order.orderID;
      } else {
        alert("That order was not found.");
      }
      refreshPallets($scope.storedId);
      refreshCartons($scope.storedId);
      refreshPicks($scope.storedId);
      refreshLog($scope.storedId);
    }
    
    function
    lookup()
    {
      refreshCount(4);
//      console.log("lookup: "+refreshCounter);
      DbFactory.post({topic: 'order',
                      action: 'lookup',
                      params: {orderID: $scope.orderId}
                     })
        .success(lookupSuccess)
        .error  (refreshOrderError);
//      storeCartonSeq();
//      $scope.lpn = "";
    }
    
    function
    refreshOrderError(err)
    { 
      $scope.order = {};
      console.log(err);
      refreshCount(-4);
//      console.log("refreshOrderError: "+refreshCounter);
    }

    function 
    refreshOrderSuccess(order)                
    {
      $scope.order = order[0];
      refreshPallets(order[0].orderID);
      refreshCartons(order[0].orderID);
      refreshPicks(order[0].orderID);
      refreshLog(order[0].orderID);
    }
    
    // // //
    // PALLETS/CARTONS/PICKS/HISTORY
    
    // PALLETS
    
    function
    palletSuccess(pallets)
    { buildPalletsTable(pallets); }
    
    function
    palletError(err)
    {
      if(palletsTable){
         palletsTable.clear();
      }
      console.log(err);
      refreshCount(-1);
//      console.log("palletError: "+refreshCounter);
    }

    function
    refreshPallets(orderId)
    {
      if(orderId){
        DbFactory.post({topic: 'order',
                        action: 'pallets',
                        params: {orderId: orderId}})
          .success(palletSuccess)
          .error  (palletError);
      } else {
        refreshCount(-1);
//        console.log("refreshPallets: "+refreshCounter);
      }
    }
    
    // CARTONS
    
    function
    cartonSuccess(cartons)
    { buildCartonsTable(cartons); }
    
    function
    cartonError(err)
    {
      if(cartonsTable){
        cartonsTable.clear();
      }
      console.log(err);
      refreshCount(-1);
//      console.log("cartonError: "+refreshCounter);
    }

    function
    refreshCartons(orderId)
    {
      if(orderId){
        DbFactory.post({topic: 'order',
                        action: 'cartons',
                        params: {orderID: orderId}})
          .success(cartonSuccess)
          .error  (cartonError);
      } else {
        refreshCount(-1);
//        console.log("refreshCartons: "+refreshCounter);
      }
    }
    
    // PICKS
    
    function
    picksSuccess(picks)
    { buildPicksTable(picks); }

    function
    picksError(err)  
    {
      if(picksTable){
        picksTable.clear();
      }
      console.log(err);
      refreshCount(-1);
//      console.log("picksError: "+refreshCounter);
    }

    function
    refreshPicks(orderId)
    {
      if(orderId){
        DbFactory.post({topic: 'order',
                        action: 'picks',
                        params: {orderID: orderId}})
          .success(picksSuccess)
          .error  (picksError);
      } else {
        refreshCount(-1);
//        console.log("refreshLogs: "+refreshCounter);
      }
    }
    
    // HISTORY
    
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
    refreshLog(orderId)
    {
      if(orderId){
        DbFactory.post({topic: 'order',
                        action: 'history',
                        params: {orderID: orderId}})
          .success(logSuccess)
          .error  (logError);
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
      refreshCount(11);
//      console.log("refresh: "+refreshCounter);
      refreshOrders();
      refreshDetails();
    }

    function
    refreshOrders()
    {
      if($scope.orderRefreshEnabled){
        refreshOrdersAll();
        refreshOrdersUnassigned();
        refreshOrdersInProcess();
        refreshOrdersError();
        refreshOrdersAged();
        refreshOrdersCancelled();
        refreshOrdersComplete();
      } else {
        refreshCount(-7);
      }
    }

    function 
    refreshDetails()
    {
      if($scope.storedId){
        DbFactory.post({topic: 'order',
                        action: 'lookup',
                        params: {orderID: $scope.storedId}
                       })
          .success(refreshOrderSuccess)
          .error  (refreshOrderError);
      } else {
        refreshCount(-4);
//        console.log("refreshDetails: "+refreshCounter);
      }
    }

    function
    init()
    {
      Global.setTitle('Orders');
      refresh();
      Global.recv('refresh',refresh,$scope);
      periodic = $interval(refresh, 15000);
    }

    init();

    $scope.$on('$destroy', function(){
      $interval.cancel(periodic);
    });
    
  }

  function
  ordersConfig($routeProvider)
  {
    $routeProvider
      .when('/orders', {controller: 'OrdersController',
                        templateUrl: '/app/orders/orders.view.html'});
  }
  
}())
