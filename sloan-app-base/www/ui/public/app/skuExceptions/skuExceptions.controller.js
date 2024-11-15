(function () { angular
    .module('ui')
      .controller('SkuExceptionsController',skuExceptionsController);

  angular
    .module('ui')
      .config(skuExceptionsConfig);

      skuExceptionsController.$inject = ['$scope','$timeout','$interval',
                                '$mdDialog',
                               'Global','DbFactory'];
  
  function
  skuExceptionsController($scope,$timeout,$interval, $mdDialog, Global,DbFactory)
    {
    $scope.refresh = refresh;
    $scope.permit = Global.permit;
    $scope.selected = 0 ;
	
    
    function
    updateSuccess()
    { $timeout(refresh,1000); }
    
    function
    updateError(err)
    {
      console.error(err);
      Global.busyRefresh(false);
    }

    Date.prototype.stdTimezoneOffset = function () {
        var jan = new Date(this.getFullYear(), 0, 1);
        var jul = new Date(this.getFullYear(), 6, 1);
        return Math.max(jan.getTimezoneOffset(), jul.getTimezoneOffset());
    } 

    Date.prototype.isDstObserved = function () {
        return this.getTimezoneOffset() < this.stdTimezoneOffset();
    }    
    
    function
    dateRender(data,type,full,meta)
    {
      if(data){
        if(type=='display'){
          var date = new Date(data);
          const localOffset = new Date().getTimezoneOffset(); // in minutes
          const localOffsetMillis = 60 * 1000 * localOffset;
          const stdOffset = new Date().stdTimezoneOffset(); // in minutes
          const stdOffsetMillis = 60 * 1000 * stdOffset;
          const serverOffset = 300;
          const serverOffsetMillis = 60 * 1000 * serverOffset;
          //const dtsOffset = date.isDstObserved()?60000*60:0;
          const dtsOffset = 0;
          var modifiedOffset = (stdOffsetMillis-dtsOffset-serverOffsetMillis);
          var modifiedDate = new Date(date.getTime()+modifiedOffset);
          return modifiedDate.toLocaleString('en-US',{year: 'numeric', month: 'numeric', day: 'numeric',hour: 'numeric',minute:'numeric' });
        }else{
          var tzoffset = (new Date()).getTimezoneOffset()*60000;
          var localTime = new Date(data) - tzoffset;
          if(!localTime) return '';
          var localISOTime = new Date(localTime).toISOString().slice(0,-1);
          return localISOTime;
        }
      }else{
        return '';
      } 
    }  



      function buttonRender(ref, data, perm, title, name, cb, enabled) {
        name = name.replace(/\s+/g, '');
        $(ref + ' tbody').off('click', '#' + name + 'button');
        $(ref + ' tbody').on('click', '#' + name + 'button', (e) => cb(data));
        if (enabled) {
              if (Global.permit(perm))
                    return '<button id="' + name + 'button">' + title + '</button>';
              else
                    return '<button disabled>' + title + '</button>';
        } else {
              return '';
        }
        
  }     


/************************************************************************************************************************************/
    var skuExceptionsTable = null;

    function
      buildSkuExceptionsTable(data) {
      var cols = [];
      var ref = "#skuExceptionsTable";

      cols.push({ title: "SKU", data: "sku", class: "dt-center" });
      cols.push({ title: "Exception", data: "exception", class: "dt-center" });
      cols.push({ title: "RDS Solution", data: "solution", class: "dt-center" });
      cols.push({ title: "Stamp", data: "stamp", class: "dt-center",render:dateRender });
      cols.push({title: "Acknowledge Action", class: "dt-center", data: {}, render: (data) => 
      { return buttonRender(ref, data, 'skuExceptionsPerm','Acknowledge', data.seq+'ack', messageAck, true)}});
      //cols.push({ title: "Acknowledge Stamp", data: "acknowledged", class: "dt-center",render:dateRender });

      if (skuExceptionsTable) {
        skuExceptionsTable.clear();
        skuExceptionsTable.rows.add(data);
        skuExceptionsTable.draw(false);
      }
      else {
        skuExceptionsTable
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
        $(ref + ' tbody').on('click', 'tr', skuExceptionsTableClick);
        $timeout(skuExceptionsTable.draw, 0);
      }

      Global.busyRefresh(false);
    }

    function
    skuExceptionsTableClick() {
      var data = skuExceptionsTable.row(this).data();
    }


    function
      refreshSkuExceptionsTable() {
      DbFactory.post({
        topic: 'skuExceptions',
        action: 'getSkuExceptionsTable'
      })
        .success(skuExceptionsTableSuccess)
        .error(fetchError);
    }

    function
    skuExceptionsTableSuccess(data) { buildSkuExceptionsTable(data); }

/************************************************************************************************************************************/
    function messageAck(data) {

      $scope.sku = data.sku;

      //User confirmation popup
      var dialog = $mdDialog.confirm()
          .title('Acknowledge - SKU: ' + data.sku + ' exception.')
          .textContent("Are you sure you want to acknowlege this SKU Exception?")
          .ok('Acknowledge')
          .cancel('Cancel');
        $mdDialog
          .show(dialog)
          .then(acknowledgeException);
    }

    function acknowledgeException(){
      Global.busyRefresh(true);
      DbFactory.post({
            topic: 'skuExceptions',
            action: 'exceptionAck',
            params: { 
                      sku: $scope.sku
                    }
      })
            .success(refresh)
            .error(function (err) { console.log(err); });
    }

    function
    fetchError(err)
    {
      console.error(err);
    }

    function refresh() {
      refreshSkuExceptionsTable();
    }


    function
    init()
      {
      Global.setTitle('SKU Exceptions');
      refresh();
      Global.recv('refresh',refresh,$scope);
      periodic = $interval(refresh, 30000);
      }

    init();

    $scope.$on('$destroy', function(){
      $interval.cancel(periodic);
    });
    
  
}

  function skuExceptionsConfig($routeProvider)
  {
    $routeProvider
      .when('/skuExceptions', {controller: 'SkuExceptionsController',
                        templateUrl: '/app/skuExceptions/skuExceptions.view.html'});
  }
  
}())
