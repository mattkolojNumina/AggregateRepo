(function()
{
  angular
    .module('ui')
      .controller('WaveController',waveController);

  angular
    .module('ui')
      .config(waveConfig);

  waveController.$inject = ['$scope','$routeParams','$interval',
                            'Global','DbFactory'];
  
  function
  waveController($scope,$routeParams,$interval,Global,DbFactory)
  {
    $scope.refresh = refresh;
    
    $scope.permit = Global.permit;
    
    $scope.wave = {};
    $scope.waveSeq = 0;
    $scope.waveSeqStored = 0;
    $scope.waveRWave = '';
    $scope.waveRWaveStored = '';
    $scope.waveExpShipDate = '';
    $scope.waveExpShipDateStored = '';
//    $scope.lookup_waveSeq = lookup_waveSeq;
    $scope.lookup_waveRWave = lookup_waveRWave;
    
    $scope.order = {};
    $scope.orderNum = null;
    $scope.orderNumStored = null;
    $scope.lookup_orderNum = lookup_orderNum;
    
    $scope.carton = {};
    $scope.cartonSeq = 0;
    $scope.cartonSeqStored = 0;
    $scope.cartonLpn = '';
    $scope.cartonLpnStored = '';
//    $scope.lookup_cartonSeq = lookup_cartonSeq;
    $scope.lookup_cartonLpn = lookup_cartonLpn;
    
    $scope.barShown = 'tool';
    $scope.showCancel = showCancel;
    $scope.showRelease = showRelease;
    $scope.barReset = barReset;
    
    $scope.waveCancel = waveCancel;
    $scope.waveRelease = waveRelease;
    
    var refreshCounter = 0;
    
    function
    refreshCount(n,name)
    {
      refreshCounter += n;
      if(refreshCounter>0)
        Global.busyRefresh(true);
      else
        Global.busyRefresh(false);
      if(false) //make true for logging
        console.log(name+": "+refreshCounter);
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
    
    function
    formatExpShip(esd)
    { return esd.toString().replace(/-/g,'').slice(0,8); }
    
    // // // // //
    // WAVE COMMANDS
    
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
    waveUpdateError(err)
    { 
      console.log(err);
      refreshCount(-1,"waveUpdateError");
    }
    
    function
    waveCancel()
    {
      if($scope.waveSeqStored && $scope.waveRWaveStored && $scope.waveExpShipDateStored){
//        console.log("Cancel wave "+$scope.waveSeqStored);
        refreshCount(1,"waveCancel");
        DbFactory.post({topic: 'wave',
                        action: 'cancel',
                        params: {seq: $scope.waveSeqStored}
                       })
          .success(refreshWaveDetails)
          .error  (waveUpdateError);
        DbFactory.post({topic: 'wave',
                        action: 'cancel_up',
                        params: {rWave: $scope.waveRWaveStored,
                                 expShipDate: formatExpShip($scope.waveExpShipDateStored)}
                       })
          .catch(someError);
      }
      barReset();
    }
    
    function
    waveRelease()
    {
      if($scope.waveSeqStored){
//        console.log("Release wave "+$scope.waveSeqStored);
        refreshCount(1,"waveRelease");
        DbFactory.post({topic: 'wave',
                        action: 'release',
                        params: {seq: $scope.waveSeqStored}
                       })
          .success(refreshWaveDetails)
          .error  (waveUpdateError);
      }
      barReset();
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
    waveMilestonesCallback(columnsSkipped,includeDownload)
    {
      var x = includeDownload?1:0;
      
      return (function(row,data,index)
      {
        if(includeDownload){
          var downloadHtml = data.downloadStamp?
              ('<div class="lefttip"><b>&check;</b><span class="lefttiptext">'
               +simpleDate(data.downloadStamp)+'</span></div>'):'';
          $('td:eq('+(columnsSkipped+0)+')',row).html(downloadHtml);
        }
        
        var releasedHtml = data.releasedStamp?
            ('<div class="lefttip"><b>&check;</b><span class="lefttiptext">'
             +simpleDate(data.releasedStamp)+'</span></div>'):'';
        $('td:eq('+(columnsSkipped+0+x)+')',row).html(releasedHtml);
        
        var pickedHtml = data.pickCompleteStamp?
            ('<div class="lefttip"><b>&check;</b><span class="lefttiptext">'
             +simpleDate(data.pickCompleteStamp)+'</span></div>'):'';
        $('td:eq('+(columnsSkipped+1+x)+')',row).html(pickedHtml);
        
        var packedHtml = data.packCompleteStamp?
            ('<div class="lefttip"><b>&check;</b><span class="lefttiptext">'
             +simpleDate(data.packCompleteStamp)+'</span></div>'):'';
        $('td:eq('+(columnsSkipped+2+x)+')',row).html(packedHtml);
        
        var shippedHtml = data.shipCompleteStamp?
            ('<div class="lefttip"><b>&check;</b><span class="lefttiptext">'
             +simpleDate(data.shipCompleteStamp)+'</span></div>'):'';
        $('td:eq('+(columnsSkipped+3+x)+')',row).html(shippedHtml);
        
        var canceledHtml = data.canceledStamp?
            ('<div class="lefttip"><b>&check;</b><span class="lefttiptext">'
             +simpleDate(data.canceledStamp)+'</span></div>'):'';
        $('td:eq('+(columnsSkipped+4+x)+')',row).html(canceledHtml);
      });
    }
    
    function
    orderMilestonesCallback(columnsSkipped)
    {
      return (function(row,data,index)
      {
        var clubHtml = data.isClubOrder?
            ('<div><b>&check;</b></div>'):'';
        $('td:eq('+(columnsSkipped+0)+')',row).html(clubHtml);
        
        var pickedHtml = data.pickCompleteStamp?
            ('<div class="lefttip"><b>&check;</b><span class="lefttiptext">'
             +simpleDate(data.pickCompleteStamp)+'</span></div>'):'';
        $('td:eq('+(columnsSkipped+1)+')',row).html(pickedHtml);
        
        var packedHtml = data.packCompleteStamp?
            ('<div class="lefttip"><b>&check;</b><span class="lefttiptext">'
             +simpleDate(data.packCompleteStamp)+'</span></div>'):'';
        $('td:eq('+(columnsSkipped+2)+')',row).html(packedHtml);
        
        var shippedHtml = data.shipCompleteStamp?
            ('<div class="lefttip"><b>&check;</b><span class="lefttiptext">'
             +simpleDate(data.shipCompleteStamp)+'</span></div>'):'';
        $('td:eq('+(columnsSkipped+3)+')',row).html(shippedHtml);
      });
    }
    
    function
    cartonMilestonesCallback(columnsSkipped)
    {
      return (function(row,data,index)
      {
        var pickedHtml = data.pickCompleteStamp?
            ('<div class="lefttip"><b>&check;</b><span class="lefttiptext">'
             +simpleDate(data.pickCompleteStamp)+'</span></div>'):'';
        $('td:eq('+(columnsSkipped+0)+')',row).html(pickedHtml);
        
        var packedHtml = data.packStamp?
            ('<div class="lefttip"><b>&check;</b><span class="lefttiptext">'
             +simpleDate(data.packStamp)+'</span></div>'):'';
        $('td:eq('+(columnsSkipped+1)+')',row).html(packedHtml);
        
        var shippedHtml = data.shipStamp?
            ('<div class="lefttip"><b>&check;</b><span class="lefttiptext">'
             +simpleDate(data.shipStamp)+'</span></div>'):'';
        $('td:eq('+(columnsSkipped+2)+')',row).html(shippedHtml);
      });
    }

    function
    itemMilestonesCallback(columnsSkipped)
    {
      return (function(row,data,index)
      {
        var pickedHtml = data.picked?
            ('<div><b>&check;</b></div>'):'';
        $('td:eq('+(columnsSkipped+0)+')',row).html(pickedHtml);
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
    
    function
    dateRenderShort(data,type,full,meta)
    {
      if(type!='display')
        return data;
      
      var date = new Date(data);
      return date.toDateString();
    }
    
    // // //
    // WAVES-ALL
    
    var wavesAllTable = null;
    
    function
    buildWavesAllTable(data)
    {
      var cols = [];
      var ref = "#wavesAll";
      
      cols.push({title: "Wave",         data:"rWave"});
      cols.push({title: "Ship date",    data:"expShipDate",
                                        render:dateRenderShort});
      cols.push({title: "Type",         data:"rWaveType"});
      cols.push({title: "Description",  data:"rWaveDesc"});
      cols.push({title: "Downloaded",   data:"downloadStamp",
                                        class:'dt-center'});
      cols.push({title: "Released",     data:"releasedStamp",
                                        class:'dt-center'});
      cols.push({title: "Picked",       data:"pickCompleteStamp",
                                        class:'dt-center'});
      cols.push({title: "Packed",       data:"packCompleteStamp",
                                        class:'dt-center'});
      cols.push({title: "Shipped",      data:"shipCompleteStamp",
                                        class:'dt-center'});
      cols.push({title: "Canceled",     data:"canceledStamp",
                                        class:'dt-center'});
      cols.push({title: "Wave Seq",     data:"seq",
                                        visible:false});
      
      if(wavesAllTable){
        wavesAllTable.clear();
        wavesAllTable.rows.add(data);
        wavesAllTable.draw(false);
      } else {
        wavesAllTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              rowCallback: waveMilestonesCallback(4,true),
                              order: [],
                              scrollY: "500px",
                              scrollCollapse: true,
                              paging: false,
                              dom: 'ltB',
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','tr',selectClickWavesAll);
        setTimeout(function(){wavesAllTable.draw();},0);
      }
      refreshCount(-1,"buildWavesAllTable");
    }
    
    function
    selectClickWavesAll()
    {
      var data = wavesAllTable.row(this).data();
      
      if(data.seq){
        $scope.waveSeq = data.seq;
        $scope.selected = 5;
        lookup_waveSeq();
      }
    }
    
    // // //
    // WAVES-RELEASED
    
    var wavesReleasedTable = null;
    
    function
    buildWavesReleasedTable(data)
    {
      var cols = [];
      var ref = "#wavesReleased";
      
      cols.push({title: "Wave",         data:"rWave"});
      cols.push({title: "Ship date",    data:"expShipDate",
                                        render:dateRenderShort});
      cols.push({title: "Type",         data:"rWaveType"});
      cols.push({title: "Description",  data:"rWaveDesc"});
      cols.push({title: "Released",     data:"releasedStamp",
                                        class:'dt-center'});
      cols.push({title: "Picked",       data:"pickCompleteStamp",
                                        class:'dt-center'});
      cols.push({title: "Packed",       data:"packCompleteStamp",
                                        class:'dt-center'});
      cols.push({title: "Shipped",      data:"shipCompleteStamp",
                                        class:'dt-center'});
      cols.push({title: "Canceled",     data:"canceledStamp",
                                        class:'dt-center'});
      cols.push({title: "Wave Seq",     data:"seq",
                                        visible:false});
      
      if(wavesReleasedTable){
        wavesReleasedTable.clear();
        wavesReleasedTable.rows.add(data);
        wavesReleasedTable.draw(false);
      } else {
        wavesReleasedTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              rowCallback: waveMilestonesCallback(4,false),
                              order: [],
                              scrollY: "500px",
                              scrollCollapse: true,
                              paging: false,
                              dom: 'ltB',
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','tr',selectClickWavesReleased);
        setTimeout(function(){wavesReleasedTable.draw();},0);
      }
      refreshCount(-1,"buildWavesReleasedTable");
    }
    
    function
    selectClickWavesReleased()
    {
      var data = wavesReleasedTable.row(this).data();
      
      if(data.seq){
        $scope.waveSeq = data.seq;
        $scope.selected = 5;
        lookup_waveSeq();
      }
    }
    
    // // //
    // WAVES-NOT RELEASED
    
    var wavesNotReleasedTable = null;
    
    function
    buildWavesNotReleasedTable(data)
    {
      var cols = [];
      var ref = "#wavesNotReleased";
      
      cols.push({title: "Wave",         data:"rWave"});
      cols.push({title: "Ship date",    data:"expShipDate",
                                        render:dateRenderShort});
      cols.push({title: "Type",         data:"rWaveType"});
      cols.push({title: "Description",  data:"rWaveDesc"});
      cols.push({title: "Released",     data:"releasedStamp",
                                        class:'dt-center'});
      cols.push({title: "Picked",       data:"pickCompleteStamp",
                                        class:'dt-center'});
      cols.push({title: "Packed",       data:"packCompleteStamp",
                                        class:'dt-center'});
      cols.push({title: "Shipped",      data:"shipCompleteStamp",
                                        class:'dt-center'});
      cols.push({title: "Canceled",     data:"canceledStamp",
                                        class:'dt-center'});
      cols.push({title: "Wave Seq",     data:"seq",
                                        visible:false});
      
      if(wavesNotReleasedTable){
        wavesNotReleasedTable.clear();
        wavesNotReleasedTable.rows.add(data);
        wavesNotReleasedTable.draw(false);
      } else {
        wavesNotReleasedTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              rowCallback: waveMilestonesCallback(4,false),
                              order: [],
                              scrollY: "500px",
                              scrollCollapse: true,
                              paging: false,
                              dom: 'ltB',
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','tr',selectClickWavesNotReleased);
        setTimeout(function(){wavesNotReleasedTable.draw();},0);
      }
      refreshCount(-1,"buildWavesNotReleasedTable");
    }
    
    function
    selectClickWavesNotReleased()
    {
      var data = wavesNotReleasedTable.row(this).data();
      
      if(data.seq){
        $scope.waveSeq = data.seq;
        $scope.selected = 5;
        lookup_waveSeq();
      }
    }
    
    // // //
    // WAVES-ERROR
    
    var wavesErrorTable = null;
    
    function
    buildWavesErrorTable(data)
    {
      var cols = [];
      var ref = "#wavesError";
      
      cols.push({title: "Wave",         data:"rWave"});
      cols.push({title: "Ship date",    data:"expShipDate",
                                        render:dateRenderShort});
      cols.push({title: "Type",         data:"rWaveType"});
      cols.push({title: "Description",  data:"rWaveDesc"});
      cols.push({title: "Released",     data:"releasedStamp",
                                        class:'dt-center'});
      cols.push({title: "Picked",       data:"pickCompleteStamp",
                                        class:'dt-center'});
      cols.push({title: "Packed",       data:"packCompleteStamp",
                                        class:'dt-center'});
      cols.push({title: "Shipped",      data:"shipCompleteStamp",
                                        class:'dt-center'});
      cols.push({title: "Canceled",     data:"canceledStamp",
                                        class:'dt-center'});
      cols.push({title: "Wave Seq",     data:"seq",
                                        visible:false});
      
      if(wavesErrorTable){
        wavesErrorTable.clear();
        wavesErrorTable.rows.add(data);
        wavesErrorTable.draw(false);
      } else {
        wavesErrorTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              rowCallback: waveMilestonesCallback(4,false),
                              order: [],
                              scrollY: "500px",
                              scrollCollapse: true,
                              paging: false,
                              dom: 'ltB',
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','tr',selectClickWavesError);
        setTimeout(function(){wavesErrorTable.draw();},0);
      }
      refreshCount(-1,"buildWavesErrorTable");
    }
    
    function
    selectClickWavesError()
    {
      var data = wavesErrorTable.row(this).data();
      
      if(data.seq){
        $scope.waveSeq = data.seq;
        $scope.selected = 5;
        lookup_waveSeq();
      }
    }
    
    // // //
    // WAVES-CANCELED
    
    var wavesCanceledTable = null;
    
    function
    buildWavesCanceledTable(data)
    {
      var cols = [];
      var ref = "#wavesCanceled";
      
      cols.push({title: "Wave",         data:"rWave"});
      cols.push({title: "Ship date",    data:"expShipDate",
                                        render:dateRenderShort});
      cols.push({title: "Type",         data:"rWaveType"});
      cols.push({title: "Description",  data:"rWaveDesc"});
      cols.push({title: "Released",     data:"releasedStamp",
                                        class:'dt-center'});
      cols.push({title: "Picked",       data:"pickCompleteStamp",
                                        class:'dt-center'});
      cols.push({title: "Packed",       data:"packCompleteStamp",
                                        class:'dt-center'});
      cols.push({title: "Shipped",      data:"shipCompleteStamp",
                                        class:'dt-center'});
      cols.push({title: "Canceled",     data:"canceledStamp",
                                        class:'dt-center'});
      cols.push({title: "Wave Seq",     data:"seq",
                                        visible:false});
      
      if(wavesCanceledTable){
        wavesCanceledTable.clear();
        wavesCanceledTable.rows.add(data);
        wavesCanceledTable.draw(false);
      } else {
        wavesCanceledTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              rowCallback: waveMilestonesCallback(4,false),
                              order: [],
                              scrollY: "500px",
                              scrollCollapse: true,
                              paging: false,
                              dom: 'ltB',
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','tr',selectClickWavesCanceled);
        setTimeout(function(){wavesCanceledTable.draw();},0);
      }
      refreshCount(-1,"buildWavesCanceledTable");
    }
    
    function
    selectClickWavesCanceled()
    {
      var data = wavesCanceledTable.row(this).data();
      
      if(data.seq){
        $scope.waveSeq = data.seq;
        $scope.selected = 5;
        lookup_waveSeq();
      }
    }
    
    // // //
    // ORDERS
    
    var ordersTable = null;
    
    function
    buildOrdersTable(data)
    {
      var cols = [];
      var ref = "#orders";
      
      cols.push({title: "Order",    data:"orderNum"});
      cols.push({title: "Priority", data:"orderPriority"});
      cols.push({title: "Club?",    data:"isClubOrder",
                                    class:'dt-center'});
      cols.push({title: "Picked",   data:"pickCompleteStamp",
                                    class:'dt-center'});
      cols.push({title: "Packed",   data:"packCompleteStamp",
                                    class:'dt-center'});
      cols.push({title: "Shipped",  data:"shipCompleteStamp",
                                    class:'dt-center'});
      
      if(ordersTable){
        ordersTable.clear();
        ordersTable.rows.add(data);
        ordersTable.draw(false);
      } else {
        ordersTable = $(ref)
                  .DataTable({data: data, 
                              columns: cols,
                              rowCallback: orderMilestonesCallback(2),
                              order: [],
                              scrollY: "220px",
                              scrollCollapse: true,
                              paging: false,
                              dom: 'ltB',
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','tr',selectClickOrders);
        setTimeout(function(){ordersTable.draw();},0);
      }
      refreshCount(-1,"buildOrdersTable");
    }
    
    function
    selectClickOrders()
    {
      var data = ordersTable.row(this).data();
      
      if(data.orderNum){
        $scope.orderNum = data.orderNum;
        $scope.selected = 6;
        lookup_orderNum();
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
      
      cols.push({title: "LPN",        data:"carton"});
      cols.push({title: "Picked",     data:"pickCompleteStamp",
                                      class:'dt-center'});
      cols.push({title: "Packed",     data:"packStamp",
                                      class:'dt-center'});
      cols.push({title: "Shipped",    data:"shipStamp",
                                      class:'dt-center'});
      cols.push({title: "Carton Seq", data:"cartonSeq",
                                      visible:false});
      if(cartonsTable){
        cartonsTable.clear();
        cartonsTable.rows.add(data);
        cartonsTable.draw(false);
      } else {
        cartonsTable = $(ref)
          .DataTable({data: data, 
                      columns: cols,
                      rowCallback: cartonMilestonesCallback(1),
                      order: [],
                      scrollY: '220px',
                      scrollCollapse: true,
                      paging: false,
                      dom: 'ltB',
                      buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','tr',selectClickCartons);
        setTimeout(function(){cartonsTable.draw();},0);
      }
      refreshCount(-1,"buildCartonsTable");
    }
    
    function
    selectClickCartons()
    {
      var data = cartonsTable.row(this).data();
      
      if(data.cartonSeq){
        $scope.cartonSeq = data.cartonSeq;
        $scope.selected = 7;
        lookup_cartonSeq();
      }
    }
    
    // // //
    // ITEMS
    
    var itemsTable = null;
    
    function
    buildItemsTable(data)
    {
      var cols = [];
      var ref = "#items";
      
      cols.push({title: "SKU",        data:"sku"});
      cols.push({title: "EAN",        data:"ean"});
      cols.push({title: "Qty",        data:"qty"});
      cols.push({title: "Picked qty", data:"qtyPicked"});
      cols.push({title: "Picked",     data:"picked",
                                      class:'dt-center'});
      
      if(itemsTable){
        itemsTable.clear();
        itemsTable.rows.add(data);
        itemsTable.draw(false);
      } else {
        itemsTable = $(ref)
          .DataTable({data: data,
                      columns: cols,
                      rowCallback: itemMilestonesCallback(4),
                      order: [],
                      scrollY: '220px',
                      scrollCollapse: true,
                      paging: false,
                      dom: 'ltB',
                      buttons: ['copy','print','excel','pdf']});
        setTimeout(function(){itemsTable.draw();},0);
      }
      refreshCount(-1,"buildItemsTable");
    }
    
    // // //
    // HISTORY
    
    var historyTable = null;
    
    function
    buildHistoryTable(data)
    {
      var cols = [];
      var ref = "#history";
      
      cols.push({title: "Code",         data:"code"});
      cols.push({title: "Description",  data:"description"});
      cols.push({title: "Stamp",        data:"stamp",
                                        render:dateRender});
      
      if(historyTable){
        historyTable.clear();
        historyTable.rows.add(data);
        historyTable.draw(false);
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
      refreshCount(-1,"buildHistoryTable");
    }
    
    
    // // // // //
    // DATA RETRIEVAL
    
    function
    someError(err)
    { console.log(err); }
    
    // // //
    // WAVES-ALL

    function
    refreshWavesAll()
    {
      DbFactory.post({topic: 'wave',
                      action: 'all'
                     })
        .success(wavesAllSuccess)
        .error  (wavesAllError);
    }
    
    function 
    wavesAllSuccess(data)
    { buildWavesAllTable(data); }
    
    function
    wavesAllError(err)
    {
      console.log(err);
      refreshCount(-1,"wavesAllError");
    }
    
    // // //
    // WAVES-RELEASED

    function
    refreshWavesReleased()
    {
      DbFactory.post({topic: 'wave',
                      action: 'released'
                     })
        .success(wavesReleasedSuccess)
        .error  (wavesReleasedError);
    }
    
    function 
    wavesReleasedSuccess(data)
    { buildWavesReleasedTable(data); }
    
    function
    wavesReleasedError(err)
    {
      console.log(err);
      refreshCount(-1,"wavesReleasedError");
    }
    
    // // //
    // WAVES-NOT RELEASED

    function
    refreshWavesNotReleased()
    {
      DbFactory.post({topic: 'wave',
                      action: 'notReleased'
                     })
        .success(wavesNotReleasedSuccess)
        .error  (wavesNotReleasedError);
    }
    
    function 
    wavesNotReleasedSuccess(data)
    { buildWavesNotReleasedTable(data); }
    
    function
    wavesNotReleasedError(err)
    {
      console.log(err);
      refreshCount(-1,"wavesNotReleasedError");
    }
    
    // // //
    // WAVES-ERROR

    function
    refreshWavesError()
    {
      DbFactory.post({topic: 'wave',
                      action: 'error'
                     })
        .success(wavesErrorSuccess)
        .error  (wavesErrorError);
    }
    
    function 
    wavesErrorSuccess(data)
    { buildWavesErrorTable(data); }
    
    function
    wavesErrorError(err)
    {
      console.log(err);
      refreshCount(-1,"wavesErrorError");
    }
    
    // // //
    // WAVES-CANCELED

    function
    refreshWavesCanceled()
    {
      DbFactory.post({topic: 'wave',
                      action: 'canceled'
                     })
        .success(wavesCanceledSuccess)
        .error  (wavesCanceledError);
    }
    
    function 
    wavesCanceledSuccess(data)
    { buildWavesCanceledTable(data); }
    
    function
    wavesCanceledError(err)
    {
      console.log(err);
      refreshCount(-1,"wavesCanceledError");
    }
    
    // // //
    // LOOKUPS
    
    function
    lookup_waveSeq()
    {
      refreshCount(1,"lookup_waveSeq");
      DbFactory.post({topic: 'wave',
                      action: 'lookupSeq',
                      params: {seq: $scope.waveSeq}
                     })
        .success(waveLookupSuccess)
        .error  (refreshWaveError);
    }
    
    function
    lookup_waveRWave(withDate)
    {
      refreshCount(1,"lookup_waveRWave");
      if(withDate){
        DbFactory.post({topic: 'wave',
                        action: 'lookupRWave',
                        params: {rWave: $scope.waveRWave,
                                 expShipDate: $scope.waveExpShipDate}
                       })
          .success(waveLookupSuccess)
          .error  (refreshWaveError);
      } else {
        DbFactory.post({topic: 'wave',
                        action: 'lookupRWaveNoDate',
                        params: {rWave: $scope.waveRWave}
                       })
          .success(waveLookupSuccess)
          .error  (refreshWaveError);
      }
    }
    
    function
    waveLookupSuccess(wave)
    {
      if(wave[0]){
        $scope.wave = wave[0];
        $scope.waveSeqStored = $scope.wave.seq;
        $scope.waveRWaveStored = $scope.wave.rWave;
        $scope.waveExpShipDateStored = $scope.wave.expShipDate;
      } else {
        alert("That wave was not found.");
      }
      refreshOrders($scope.waveRWaveStored,
                    $scope.waveExpShipDateStored);
    }
    
    function
    refreshWaveError(err)
    { 
      $scope.wave = {};
      console.log(err);
      refreshCount(-1,"refreshWaveError");
    }
    
    //
    
    function
    lookup_orderNum()
    {
      refreshCount(1,"lookup_orderNum");
      DbFactory.post({topic: 'order',
                      action: 'lookup',
                      params: {orderNum: $scope.orderNum}
                     })
        .success(orderLookupSuccess)
        .error  (refreshOrderError);
    }
    
    function
    orderLookupSuccess(order)
    {
      if(order[0]){
        $scope.order = order[0];
        $scope.orderNumStored = $scope.order.orderNum;
      } else {
        alert("That order was not found.");
      }
      refreshCartons($scope.orderNumStored);
    }
    
    function
    refreshOrderError(err)
    { 
      $scope.order = {};
      console.log(err);
      refreshCount(-1,"refreshOrderError");
    }
    
    //
    
    function
    lookup_cartonSeq()
    {
      refreshCount(2,"lookup_cartonSeq");
      DbFactory.post({topic: 'carton',
                      action: 'lookup',
                      params: {cartonSeq: $scope.cartonSeq}
                     })
        .success(cartonLookupSuccess)
        .error  (refreshCartonError);
    }
    
    function
    lookup_cartonLpn()
    {
      refreshCount(2,"lookup_cartonLpn");
      DbFactory.post({topic: 'carton',
                      action: 'lookupLpn',
                      params: {carton: $scope.cartonLpn}
                     })
        .success(cartonLookupSuccess)
        .error  (refreshCartonError);
    }
    
    function
    cartonLookupSuccess(carton)
    {
      if(carton[0]){
        $scope.carton = carton[0];
        $scope.cartonSeqStored = $scope.carton.cartonSeq;
        $scope.cartonLpnStored = $scope.carton.carton;
      } else {
        alert("That carton was not found.");
      }
      refreshItems($scope.cartonLpnStored);
      refreshHistory($scope.cartonLpnStored);
    }
    
    function
    refreshCartonError(err)
    { 
      $scope.carton = {};
      console.log(err);
      refreshCount(-2,"refreshCartonError");
    }
    
    // // //
    // ORDERS/CARTONS/ITEMS/HISTORY
    
    // ORDERS

    function
    refreshOrders(rWave,expShip)
    {
      if(rWave && expShip){
        DbFactory.post({topic: 'wave',
                        action: 'orders',
                        params: {rWave: rWave,
                                 expShipDate: formatExpShip(expShip)}
                       })
          .success(orderSuccess)
          .error  (orderError);
      } else {
        refreshCount(-1,"refreshOrders");
      }
    }
    
    function
    orderSuccess(orders)
    { buildOrdersTable(orders); }
    
    function
    orderError(err)
    {
      if(ordersTable){
         ordersTable.clear();
      }
      console.log(err);
      refreshCount(-1,"orderError");
    }
    
    // CARTONS
    
    function
    refreshCartons(orderNum)
    {
      if(orderNum){
        DbFactory.post({topic: 'order',
                        action: 'cartons',
                        params: {orderNum: orderNum}})
          .success(cartonSuccess)
          .error  (cartonError);
      } else {
        refreshCount(-1,"refreshCartons");
      }
    }
    
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
      refreshCount(-1,"cartonError");
    }
    
    // ITEMS
    
    function
    refreshItems(cartonLpn)
    {
      if(cartonLpn){
        DbFactory.post({topic: 'carton',
                        action: 'items',
                        params: {carton: cartonLpn}})
          .success(itemsSuccess)
          .error  (itemsError);
      } else {
        refreshCount(-1,"refreshItems");
      }
    }
    
    function
    itemsSuccess(items)
    { buildItemsTable(items); }
    
    function
    itemsError(err)
    {
      if(itemsTable){
        itemsTable.clear();
      }
      console.log(err);
      refreshCount(-1,"itemsError");
    }
    
    // HISTORY
    
    function
    refreshHistory(cartonLpn)
    {
      if(cartonLpn){
        DbFactory.post({topic: 'carton',
                        action: 'history',
                        params: {carton: cartonLpn}})
          .success(historySuccess)
          .error  (historyError);
      } else {
        refreshCount(-1,"refreshHistory");
      }
    }
    
    function
    historySuccess(history)
    { buildHistoryTable(history); }
    
    function
    historyError(err)
    {
      if(historyTable){
        historyTable.clear();
      }
      console.log(err);
      refreshCount(-1,"historyError");
    }
    
    
    // // // // //
    // SETUP AND ALL THAT
    
    function
    refresh()
    {
      refreshCount(9,"refresh");
      refreshWaves(); //5
      refreshDetails(); //4
    }
    
    function
    refreshWaves()
    {
      if($scope.orderRefreshEnabled){
        refreshWavesAll();
        refreshWavesReleased();
        refreshWavesNotReleased();
        refreshWavesError();
        refreshWavesCanceled();
      } else {
        refreshCount(-5);
      }
    }
    
    function 
    refreshDetails()
    {
      refreshWaveDetails();   //1
      refreshOrderDetails();  //1
      refreshCartonDetails(); //2
    }
    
    function 
    refreshWaveDetails()
    {
      if($scope.waveSeqStored){
        DbFactory.post({topic: 'wave',
                        action: 'lookupSeq',
                        params: {seq: $scope.waveSeqStored}
                       })
          .success(waveLookupSuccess)
          .error  (refreshWaveError);
      } else {
        refreshCount(-1,"refreshWaveDetails");
      }
    }
    
    function 
    refreshOrderDetails()
    {
      if($scope.orderNumStored){
        DbFactory.post({topic: 'order',
                        action: 'lookup',
                        params: {orderNum: $scope.orderNumStored}
                       })
          .success(orderLookupSuccess)
          .error  (refreshOrderError);
      } else {
        refreshCount(-1,"refreshOrderDetails");
      }
    }
    
    function 
    refreshCartonDetails()
    {
      if($scope.cartonSeqStored){
        DbFactory.post({topic: 'carton',
                        action: 'lookup',
                        params: {cartonSeq: $scope.cartonSeqStored}
                       })
          .success(cartonLookupSuccess)
          .error  (refreshCartonError);
      } else {
        refreshCount(-2,"refreshCartonDetails");
      }
    }
    
    // // // // //
    
    function
    init()
    {
      Global.setTitle('Waves');
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
  waveConfig($routeProvider)
  {
    $routeProvider
      .when('/wave',{controller: 'WaveController',
                     templateUrl: '/app/wave/wave.view.html'});
  }
  
}())