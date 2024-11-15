(function () {
  angular
    .module('ui')
    .controller('GeekController', geekController);

  angular
    .module('ui')
    .config(geekConfig);

  geekController.$inject = ['$scope', '$interval', 'Global', 'DbFactory', '$timeout', '$routeParams', '$mdDialog'];

  

  function
    geekController($scope, $interval, Global, DbFactory, $timeout, $routeParams, $mdDialog) {

    $scope.refresh = refresh;
    $scope.permit = Global.permit;
    $scope.selected = 0 ;
    $scope.oldSelected = 0;

    $scope.refreshGeekPutaways = refreshGeekPutaways;
    $scope.changeGeekPutaways = changeGeekPutaways;

    $scope.refreshGeekStocktake = refreshGeekStocktake;
    $scope.changeGeekStockatakes = changeGeekStockatakes;

    $scope.refreshGeekOutbound = refreshGeekOutbound;
    $scope.changeGeekOutbounds = changeGeekOutbounds;

    $scope.trackingNumber;
    $scope.lookupByTrackingNumber = lookupByTrackingNumber;

    $scope.LPN;
    $scope.lookupByLPN = lookupByLPN;

    function simpleDate(data) {
      var date = new Date(data);
      return date.toLocaleString();
    }

    function
    toMySQLDateTime(date)
    {
      return    date.getFullYear() + '-' +
        ('00' +(date.getMonth()+1)).slice(-2) + '-' +
        ('00' + date.getDate()).slice(-2) + ' ' +
        ('00' + date.getHours()).slice(-2) + ':' +
        ('00' + date.getMinutes()).slice(-2) + ':' +
        ('00' + date.getSeconds()).slice(-2);
    }

    function dateRender(data, type, full, meta) {
      if (data) {
        if (type == 'display') {
          var date = new Date(data);
          return date.toLocaleString();
        }
        else {
          var tzoffset = (new Date()).getTimezoneOffset() * 60000;
          var localTime = new Date(data) - tzoffset;
          if (!localTime) return '';
          var localISOTime = new Date(localTime).toISOString().slice(0, -1);
          return localISOTime;
        }
      }
      else {
        return '';
      }
    }

    function checkMarkRender(data,type,full,meta){
      if(data==null)
        return '';		
      if(type!='display')
        return data;
      return data >0 ? '&check;' : '';      
    } 

    function refreshError(err) {
      console.log(err);
    }

    function lookupByTrackingNumber() {
	  console.log('look up tracking number:' + $scope.trackingNumber);
      pickConfDetailsUpdate();
    }

    function lookupByLPN() {
	  console.log('look up LPN:' + $scope.LPN);
      pickConfDetailsUpdateLPN();
    }

    function
      pickConfDetailsUpdate() {
      DbFactory
        .post({
          topic: 'geek',
          action: 'pickConfDetailsByTrackingNumber',
          params: {
            trackingNumber: $scope.trackingNumber
          }
        })
        .success(pickConfDetailsUpdateSuccess)
        .error(refreshError);
    }

    function
      pickConfDetailsUpdateLPN() {
      DbFactory
        .post({
          topic: 'geek',
          action: 'pickConfDetailsByLPN',
          params: {
            LPN: $scope.LPN
          }
        })
        .success(pickConfDetailsUpdateSuccess)
        .error(refreshError);
    }

      function
      pickConfDetailsUpdateSuccess(data) {
      if(data.length < 1) {
        Global.showMessage('Not found!');
        $scope.searchResult = {};
      } else if(data.length > 0){
        $scope.searchResult = data[0];
        $scope.trackingNumber = data[0].out_order_code;
        console.log("data "+data);
        refreshGeekOutboundDetails();
      } 	  
    }

    // tab 0 - Geek Putaway Conf 
    
    var geekPutawayTable = null;
    
    function
    refreshGeekPutaways() {
      DbFactory.post({topic: 'geek',
                        action: 'putawayConfSku' })
        .success(geekPutawaySuccess)
        .error  (refreshError); 
    }

    function 
    changeGeekPutaways() {
      $scope.d1 = new Date($scope.date1);
      $scope.d2 = new Date($scope.date2);

      DbFactory.post({
        topic: 'geek',
        action: 'putawayConfSkuRange',
        params: { start: toMySQLDateTime($scope.d1),
                  end: toMySQLDateTime($scope.d2) }
      })
        .success(geekPutawaySuccess)
        .error(refreshError);
    }

    function
    geekPutawaySuccess(data) {
      var cols = [];
      var ref = "#geekPutaways";
      
      cols.push({title: "PutawayId",  class:"dt-center", data:"receipt_code"});
      cols.push({title: "SKU",  class:"dt-center", data:"sku_code"});
      cols.push({title: "Planned Qty",  class:"dt-center", data:"plan_amount"});
      cols.push({title: "Putaway Qty",  class:"dt-center", data:"amount"});
      cols.push({title: "Processed",  class:"dt-center", data:"processed"});
      cols.push({title: "Confirm Stamp",  class:"dt-center", data:"completion_time", render:dateRender});
      
      if(geekPutawayTable)
        {
        geekPutawayTable.clear();
        geekPutawayTable.rows.add(data);
        geekPutawayTable.draw(false);
        } 
      else 
        {
        geekPutawayTable 
          = $(ref)
           .DataTable({data: data, 
                       columns: cols,
                        rowCallback: putawaysCallBack,
                       order: [],
                       scrollY: "550px",
                       scrollX: true,
                       scrollCollapse: true,
                       paging: true,
                       dom: 'lftBipr',
                       buttons: ['copy',
                                 'print',
                                 {extend:'excel',exportOptions:{orthogonal:'exportExcel'}},
                                 'pdf']});
        }
    }

    function 
    putawaysCallBack(row,data,index) {
    
    }
    
    // tab 1 - Geek Stocktake Conf 
    
    var geekStocktakeTable = null;
    
    function
    refreshGeekStocktake() {
      DbFactory.post({topic: 'geek',
                        action: 'internalFeedbackSku' })
        .success(geekStocktakeSuccess)
        .error  (refreshError); 
    }

    function 
    changeGeekStockatakes() {
      $scope.d3 = new Date($scope.date3);
      $scope.d4 = new Date($scope.date4);

      DbFactory.post({
        topic: 'geek',
        action: 'internalFeedbackSkuRange',
        params: { start: toMySQLDateTime($scope.d3),
                  end: toMySQLDateTime($scope.d4) }
      })
        .success(geekStocktakeSuccess)
        .error(refreshError);
    }
    
    function
    geekStocktakeSuccess(data) {
      var cols = [];
      var ref = "#geekStocktake";
      
      cols.push({title: "StocktakeId",  class:"dt-center", data:"stocktake_code"});
      cols.push({title: "SKU",  class:"dt-center", data:"sku_code"});
      cols.push({title: "Qty",  class:"dt-center", data:"amount"});
      cols.push({title: "Reason",  class:"dt-center", data:"reason_code"});
      cols.push({title: "Operator",  class:"dt-center", data:"operator"});
      cols.push({title: "workstationId",  class:"dt-center", data:"workstation_no"});
      cols.push({title: "Processed",  class:"dt-center", data:"processed"});
      cols.push({title: "Confirm Stamp",  class:"dt-center", data:"stamp", render:dateRender});
      
      
      if(geekStocktakeTable)
        {
        geekStocktakeTable.clear();
        geekStocktakeTable.rows.add(data);
        geekStocktakeTable.draw(false);
        } 
      else 
        {
        geekStocktakeTable 
          = $(ref)
           .DataTable({data: data, 
                       columns: cols,
                        rowCallback: stocktakeCallBack,
                       order: [],
                       scrollY: "550px",
                       scrollX: true,
                       scrollCollapse: true,
                       paging: true,
                       dom: 'lftBipr',
                       buttons: ['copy',
                                 'print',
                                 {extend:'excel',exportOptions:{orthogonal:'exportExcel'}},
                                 'pdf']});
        }
    }

    function 
    stocktakeCallBack(row,data,index) {
    
    }

    // tab 2 - Geek Outbound Conf 
    
    var geekOutboundTable = null;
    
    function
    refreshGeekOutbound() {
      DbFactory.post({topic: 'geek',
                        action: 'pickConfs' })
        .success(geekOutboundSuccess)
        .error  (refreshError); 
    }

    function 
    changeGeekOutbounds() {
      $scope.d5 = new Date($scope.date5);
      $scope.d6 = new Date($scope.date6);

      DbFactory.post({
        topic: 'geek',
        action: 'pickConfsSkuRange',
        params: { start: toMySQLDateTime($scope.d5),
                  end: toMySQLDateTime($scope.d6) }
      })
        .success(geekOutboundSuccess)
        .error(refreshError);
    }
    
    function
    geekOutboundSuccess(data) {
      var cols = [];
      var ref = "#geekOutbounds";
      
      cols.push({title: "Tracking No.",  class:"dt-center", data:"out_order_code"});
      cols.push({title: "Container LPN",  class:"dt-center", data:"container_code"});
      cols.push({title: "Picker",  class:"dt-center", data:"picker"});
      cols.push({title: "Short",  class:"dt-center", data:"lack_flag", render:checkMarkRender});
      cols.push({title: "Processed",  class:"dt-center", data:"processed"});
      cols.push({title: "Finish Stamp",  class:"dt-center", data:"finish_date", render:dateRender});
      
      
      if(geekOutboundTable)
        {
        geekOutboundTable.clear();
        geekOutboundTable.rows.add(data);
        geekOutboundTable.draw(false);
        } 
      else 
        {
        geekOutboundTable 
          = $(ref)
           .DataTable({data: data, 
                        columns: cols,
                        order: [],
                        scrollY: "550px",
                        scrollX: true,
                        scrollCollapse: true,
                        paging: true,
                        dom: 'lftBipr',
                        buttons: ['copy', 'print', 'excel', 'pdf']
                      });
                      $(ref + ' tbody').on('click', 'td', outboundCallBack);
                      $timeout(geekOutboundTable.draw, 0);
        }
    }

    function 
    outboundCallBack() {
      console.log(geekOutboundTable.row(this));
      var data = geekOutboundTable.row(this).data();
      if (data != null) {
        
        $scope.trackingNumber = data.out_order_code;
        $scope.LPN = data.container_code;
        console.log("$scope.trackingNumber: " + $scope.trackingNumber);
        console.log("$scope.LPN: " + $scope.LPN);
        $scope.selected = 3;
        $scope.oldselected = 2;
        lookupByTrackingNumber();
      }
    }

    // tab 3 - Geek Outbound Conf Details
    var geekOutboundDetailsTable = null;

    function
    refreshGeekOutboundDetails() {
      if( $scope.trackingNumber != null){
        DbFactory.post({topic: 'geek',
                        action: 'pickConfDetailsByTrackingNumber',
                        params: {trackingNumber: $scope.trackingNumber} })
        .success(geekOutboundDetailsSuccess)
        .error  (refreshError); 
      }
     
    }

        function
      geekOutboundDetailsSuccess(data) {
      var cols = [];
      var ref = "#geekOutboundDetailsTable";

      cols.push({ title: "SKU", data: "sku_code", class: "dt-center" });
      cols.push({ title: "Plan amount", data: "plan_amount", class: "dt-center" });
      cols.push({ title: "Pickedup amount", data: "pickup_amount", class: "dt-center" });
	    cols.push({ title: "Picker", data: "picker", class: "dt-center" });

      if (geekOutboundDetailsTable) {
        geekOutboundDetailsTable.clear();
        geekOutboundDetailsTable.rows.add(data);
        geekOutboundDetailsTable.draw(false);
      }
      else {
        geekOutboundDetailsTable
          = $(ref)
            .DataTable({
              data: data,
              columns: cols,
              order: [],
              scrollY: "500px",
              scrollCollapse: true,
              paging: false,
              dom: 'ltBip',
              buttons: ['copy', 'print', 'excel', 'pdf']
            });
        $timeout(geekOutboundDetailsTable.draw, 0);
      }

      Global.busyRefresh(false);
    }


    // // // // //
    // REFRESH

    function refresh() {
      if($scope.selected==0)
        refreshGeekPutaways();
      if($scope.selected==1)
        refreshGeekStocktake();
      if($scope.selected==2)
        refreshGeekOutbound();
      if($scope.selected==3)
        refreshGeekOutboundDetails();
    }

    // // // // //
    // INIT

    function
      init() {
      Global.setTitle('Geek');
      Global.recv('refresh', refresh, $scope);
      // periodic = $interval(refresh, 2000);

      if($routeParams.trackingNumber) {
          $scope.selected = 4;
          $scope.trackingNumber = $routeParams.trackingNumber;
          lookupByTrackingNumber();
      }

      if($routeParams.LPN) {
          $scope.selected = 4;
          $scope.LPN = $routeParams.LPN;
          lookupByLPN();
      }
    }

    init();

    $scope.$on('$destroy', function () {
      $interval.cancel(periodic);
    });
  }

  function
    geekConfig($routeProvider) {
    $routeProvider
      .when('/geek', {
        controller: 'GeekController',
        templateUrl: '/app/geek/geek.view.html'
      });
  }

}()) 