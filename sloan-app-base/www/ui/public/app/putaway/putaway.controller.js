(function () {
  angular
    .module('ui')
    .controller('PutawayController', putawayController);

  angular
    .module('ui')
    .config(putawayConfig);

  putawayController.$inject = ['$scope', '$interval', 'Global', 'DbFactory', '$timeout', '$routeParams', '$mdDialog'];

  function
    putawayController($scope, $interval, Global, DbFactory, $timeout, $routeParams, $mdDialog) {

    $scope.refresh = refresh;

    function simpleDate(data) {
      var date = new Date(data);
      return date.toLocaleString();
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

    function checkLeft(row,col,tip) {
      $('td:eq('+col+')',row)
        .html(tip?
               '<div class="lefttip">'
             + '<b>&check;</b>'
             + '<span class="lefttiptext">'
             + simpleDate(tip)
             + '</span>'
             + '</div>'
             : '');
    }

    function refreshPutaways() {
      DbFactory.post({
        topic: 'putaway',
        action: 'all'
      })
        .success(putawaySuccess)
        .error(refreshError);
    }

    function refreshError(err) {
      console.log(err);
    }

    function putawaySuccess(data) {
      buildPutawayTable(data);
    }

    var putaways;

    function buildPutawayTable(data) {
      var cols = [];
      var ref = "#putaways";

      cols.push({ title: "Order Code", class: "dt-center", data: "putawayOrderCode" });
      cols.push({ title: "Pallet Code", class: "dt-center", data: "palletCode" });
      cols.push({ title: "SKU", class: "dt-center", data: "sku" });
      cols.push({ title: "UOM", class: "dt-center", data: "uom" });
      cols.push({ title: "Qty", class: "dt-center", data: "qty" });
      cols.push({ title: "Shelf Qty", class: "dt-center", data: "shelfQty" });
      cols.push({ title: "Status", class: "dt-center", data: "status" });
      cols.push({ title: "Download Stamp", class: "dt-center", data: "downloadStamp" });
      cols.push({ title: "Confirm Stamp", class: "dt-center", data: "confirmStamp" });
      cols.push({ title: "Cancel Stamp", class: "dt-center", data: "cancelStamp" });
      cols.push({ title: "Error Msg", class: "dt-center", data: "errorMsg" });

      if (putaways) {
        putaways.clear();
        putaways.rows.add(data);
        putaways.draw(false);
      } else {
        putaways = $(ref).DataTable({
          data: data,
          columns: cols,
          rowCallback: putawaysCallBack,
          order: [[0, 'desc']],
          scrollY: '440px',
          scrollCollapse: true,
          paging: true,
          deferRender: true,
          dom: 'lftipr',
          buttons: ['copy', 'print', 'excel', 'pdf']
        });
        setTimeout(function () { putaways.draw(); }, 0);
      }
      Global.busyRefresh(false);
    }

    function putawaysCallBack(row,data,index) {
      if(data.downloadStamp)
        checkLeft(row,7,data.downloadStamp);
      if(data.confirmStamp)
        checkLeft(row,8,data.confirmStamp);
      if(data.cancelStamp)
        checkLeft(row,9,data.cancelStamp);
    }

    // // // // //
    // REFRESH

    function refresh() {
      refreshPutaways();
    }

    // // // // //
    // INIT

    function
      init() {
      Global.setTitle('Putaways');
      refresh();
      Global.recv('refresh', refresh, $scope);
      periodic = $interval(refresh, 2000);
    }

    init();

    $scope.$on('$destroy', function () {
      $interval.cancel(periodic);
    });
  }

  function
    putawayConfig($routeProvider) {
    $routeProvider
      .when('/putaway', {
        controller: 'PutawayController',
        templateUrl: '/app/putaway/putaway.view.html'
      });
  }

}()) 