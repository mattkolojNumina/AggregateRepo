(function () {
  angular
    .module('ui')
    .controller('HostLogController', hostLogController);

  angular
    .module('ui')
    .config(hostLogConfig);

  hostLogController.$inject = ['$scope', '$interval', 'Global', 'DbFactory', '$timeout', '$routeParams', '$mdDialog'];

  function
    hostLogController($scope, $interval, Global, DbFactory, $timeout, $routeParams, $mdDialog) {

    $scope.refresh = refresh;
    $scope.selected = 0;
    $scope.logDetails = {};
    $scope.logSeq = '';

    function errorRender(data) {
      if(data)
        return('&check;');
      else
        return('');
    }

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

    // // // // //
    // LOGS

    function refreshHostLog() {
      DbFactory.post({
        topic: 'hostLog',
        action: 'all'
      })
        .success(hostLogSuccess)
        .error(refreshError);
    }

    function refreshError(err) {
      console.log(err);
    }

    function hostLogSuccess(data) {
      buildHostLogTable(data);
    }

    var hostLog;

    function buildHostLogTable(data) {
      var cols = [];
      var ref = "#hostLog";

      cols.push({ title: "Log Seq", class: "dt-center", data: "logSeq" });
      cols.push({ title: "Message Type", class: "dt-center", data: "messageType" });
      cols.push({ title: "Ref Type", class: "dt-center", data: "refType" });
      cols.push({ title: "Ref Value", class: "dt-center", data: "refValue" });
      // cols.push({ title: "URL", class: "dt-center", data: "url" });
      // cols.push({ title: "Request", class: "dt-center", data: "request" });
      // cols.push({ title: "Response", class: "dt-center", data: "response" });
      cols.push({ title: "Error", class: "dt-center", data: "isError", render: errorRender });
      cols.push({ title: "Error Msg", class: "dt-center", data: "errorMessage" });
      cols.push({ title: "Stamp", class: "dt-center", data: "stamp", render:dateRender });
      cols.push({ title: "Acknowledge", class: "dt-center", data: null });

      if (hostLog) {
        hostLog.clear();
        hostLog.rows.add(data);
        hostLog.draw(false);
      } else {
        hostLog = $(ref).DataTable({
          data: data,
          columns: cols,
          rowCallback: acknowledgeCallback,
          order: [[0, 'desc']],
          scrollY: '440px',
          scrollCollapse: true,
          paging: true,
          deferRender: true,
          dom: 'lftipr',
          buttons: ['copy', 'print', 'excel', 'pdf']
        });
        $(ref+' tbody').on('click','button',ackClick);
        $(ref+' tbody').on('click','tr',logTableClick);
        $timeout(hostLog.draw, 0);
      }
    }

    function acknowledgeCallback(row,data,index) {
      if(Global.permit('hostLogAck'))
        $("td:eq(7)",row).html("<button class='tableButton'>Acknowledge</button>");
      else
        $("td:eq(7)",row).html("<button class='tableButton' disabled>Acknowledge</button>");
      // if(data.stamp)
      //   checkLeft(row,6,data.stamp);
    }

    function logTableClick() {
       var data = hostLog.row(this).data();
       if (data) {
          $scope.logSeq = data.logSeq;
          $scope.selected = 1;
          $scope.$apply();
          refresh();
       }
    }

    function ackClick() {
      var row = $(this).parents('tr');
      var data = hostLog.row(row).data();
      
      var dialog = $mdDialog.confirm()
        .title('Acknowledge Log?')
          .textContent('Acknowledge log?')
          .ariaLabel('Acknowledge log')
          .ok('Yes')
          .cancel('Cancel')
      
      $mdDialog
        .show(dialog)
        .then(function(){
          DbFactory.post({topic:'hostLog',
                          action:'ack',
                          params: {seq: data.logSeq}
                         })
            .success(ackSuccess)
            .error  (ackError);
        });
    }
    
    function ackSuccess() {
      refresh();
    }
    
    function ackError(err) {
      console.error(err);
    }

    // // // // //
    // DETAILS

    function refreshHostLogDetails() {
      if($scope.logSeq) {
        DbFactory.post({
          topic: 'hostLog',
          action: 'logDetails',
          params: {logSeq : $scope.logSeq}
        })
          .success(logDetailsSuccess)
          .error(refreshDetailsError);
      }
    }

    function refreshDetailsError(err) {
      console.log(err);
    }

    function logDetailsSuccess(data) {
      $scope.logDetails = data[0]
    }

    // // // // //
    // REFRESH

    function refresh() {
      if($scope.selected == 0)
        refreshHostLog();
      if($scope.selected == 1)
        refreshHostLogDetails();
    }

    // // // // //
    // INIT

    function
      init() {
      Global.setTitle('Host Log');
      refresh();
      Global.recv('refresh', refresh, $scope);
      periodic = $interval(refresh, 5000);
    }

    init();

    $scope.$on('$destroy', function () {
      $interval.cancel(periodic);
    });
  }

  function
    hostLogConfig($routeProvider) {
    $routeProvider
      .when('/hostLog', {
        controller: 'HostLogController',
        templateUrl: '/app/hostLog/hostLog.view.html'
      });
  }

}()) 