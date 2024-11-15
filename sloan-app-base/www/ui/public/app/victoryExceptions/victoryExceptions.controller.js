(function () { angular
    .module('ui')
      .controller('VictoryExceptionsController',victoryExceptionsController);

  angular
    .module('ui')
      .config(victoryExceptionsConfig);

      victoryExceptionsController.$inject = ['$scope','$timeout','$interval',
                                '$mdDialog',
                               'Global','DbFactory'];
  
  function
  victoryExceptionsController($scope,$timeout,$interval, $mdDialog, Global,DbFactory)
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
    var badLocationLabelTable = null;

    function
      buildBadLocationLabelTable(data) {
      var cols = [];
      var ref = "#badLocationLabelTable";

      cols.push({ title: "SKU", data: "sku", class: "dt-center" });
      cols.push({ title: "UOM", data: "uom", class: "dt-center" });
      cols.push({ title: "Location", data: "location", class: "dt-center" });
      cols.push({ title: "QTY", data: "qty", class: "dt-center" });
      cols.push({ title: "Operator ID", data: "operatorId", class: "dt-center" });
      cols.push({ title: "Stamp", data: "stamp", class: "dt-center",render:dateRender });
      cols.push({title: "Acknowledge Action", class: "dt-center", data: {}, render: (data) => 
      { return buttonRender(ref, data, 'victoryExceptionsPerm','Acknowledge', data.seq+'ack', messageAck, true)}});
      //cols.push({ title: "Acknowledge Stamp", data: "acknowledged", class: "dt-center",render:dateRender });

      if (badLocationLabelTable) {
        badLocationLabelTable.clear();
        badLocationLabelTable.rows.add(data);
        badLocationLabelTable.draw(false);
      }
      else {
        badLocationLabelTable
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
        $(ref + ' tbody').on('click', 'tr', badLocationLabelTableClick);
        $timeout(badLocationLabelTable.draw, 0);
      }

      Global.busyRefresh(false);
    }

    function
    badLocationLabelTableClick() {
      var data = badLocationLabelTable.row(this).data();

    }



    function
      refreshBadLocationLabelTable() {
      DbFactory.post({
        topic: 'victoryExceptions',
        action: 'getBadLocationLabelTable'
      })
        .success(badLocationLabelTableSuccess)
        .error(fetchError);
    }

    function
    badLocationLabelTableSuccess(data) { buildBadLocationLabelTable(data); }

/************************************************************************************************************************************/
var noProductUPCTable = null;

function
  buildNoProductUPCTable(data) {
  var cols = [];
  var ref = "#noProductUPCTable";

  cols.push({ title: "SKU", data: "sku", class: "dt-center" });
  cols.push({ title: "UOM", data: "uom", class: "dt-center" });
  cols.push({ title: "Location", data: "location", class: "dt-center" });
  cols.push({ title: "QTY", data: "qty", class: "dt-center" });
  cols.push({ title: "Operator ID", data: "operatorId", class: "dt-center" });
  cols.push({ title: "Stamp", data: "stamp", class: "dt-center",render:dateRender });
  cols.push({title: "Acknowledge Action", class: "dt-center", data: {}, render: (data) => 
  { return buttonRender(ref, data, 'victoryExceptionsPerm','Acknowledge', data.seq+'ack', messageAck, true)}});
  //cols.push({ title: "Acknowledge Stamp", data: "acknowledged", class: "dt-center",render:dateRender });

  if (noProductUPCTable) {
    noProductUPCTable.clear();
    noProductUPCTable.rows.add(data);
    noProductUPCTable.draw(false);
  }
  else {
    noProductUPCTable
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
    $(ref + ' tbody').on('click', 'tr', noProductUPCTableClick);
    $timeout(noProductUPCTable.draw, 0);
  }

  Global.busyRefresh(false);
}

function
noProductUPCTableClick() {
  var data = noProductUPCTable.row(this).data();

}



function
  refreshNoProductUPCTable() {
  DbFactory.post({
    topic: 'victoryExceptions',
    action: 'getNoProductUPCTable'
  })
    .success(noProductUPCTableSuccess)
    .error(fetchError);
}

function
noProductUPCTableSuccess(data) { buildNoProductUPCTable(data); }

/************************************************************************************************************************************/
var damagedProductTable = null;

function
  buildDamagedProductTable(data) {
  var cols = [];
  var ref = "#damagedProductTable";

  cols.push({ title: "SKU", data: "sku", class: "dt-center" });
  cols.push({ title: "UOM", data: "uom", class: "dt-center" });
  cols.push({ title: "Location", data: "location", class: "dt-center" });
  cols.push({ title: "QTY", data: "qty", class: "dt-center" });
  cols.push({ title: "Operator ID", data: "operatorId", class: "dt-center" });
  cols.push({ title: "Stamp", data: "stamp", class: "dt-center",render:dateRender });
  cols.push({title: "Acknowledge Action", class: "dt-center", data: {}, render: (data) => 
  { return buttonRender(ref, data, 'victoryExceptionsPerm','Acknowledge', data.seq+'ack', messageAck, true)}});
  //cols.push({ title: "Acknowledge Stamp", data: "acknowledged", class: "dt-center",render:dateRender });

  if (damagedProductTable) {
    damagedProductTable.clear();
    damagedProductTable.rows.add(data);
    damagedProductTable.draw(false);
  }
  else {
    damagedProductTable
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
    $(ref + ' tbody').on('click', 'tr', damagedProductTableClick);
    $timeout(damagedProductTable.draw, 0);
  }

  Global.busyRefresh(false);
}

function
damagedProductTableClick() {
  var data = damagedProductTable.row(this).data();

}



function
  refreshDamagedProductTable() {
  DbFactory.post({
    topic: 'victoryExceptions',
    action: 'getDamagedProductTable'
  })
    .success(damagedProductTableSuccess)
    .error(fetchError);
}

function
damagedProductTableSuccess(data) { buildDamagedProductTable(data); }

/************************************************************************************************************************************/
var emptySlotTable = null;

function
  buildEmptySlotTable(data) {
  var cols = [];
  var ref = "#emptySlotTable";

  cols.push({ title: "SKU", data: "sku", class: "dt-center" });
  cols.push({ title: "UOM", data: "uom", class: "dt-center" });
  cols.push({ title: "Location", data: "location", class: "dt-center" });
  cols.push({ title: "QTY", data: "qty", class: "dt-center" });
  cols.push({ title: "Operator ID", data: "operatorId", class: "dt-center" });
  cols.push({ title: "Stamp", data: "stamp", class: "dt-center",render:dateRender });
  cols.push({title: "Acknowledge Action", class: "dt-center", data: {}, render: (data) => 
  { return buttonRender(ref, data, 'victoryExceptionsPerm','Acknowledge', data.seq+'ack', messageAck, true)}});
  //cols.push({ title: "Acknowledge Stamp", data: "acknowledged", class: "dt-center",render:dateRender });

  if (emptySlotTable) {
    emptySlotTable.clear();
    emptySlotTable.rows.add(data);
    emptySlotTable.draw(false);
  }
  else {
    emptySlotTable
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
    $(ref + ' tbody').on('click', 'tr', emptySlotTableClick);
    $timeout(emptySlotTable.draw, 0);
  }

  Global.busyRefresh(false);
}

function
emptySlotTableClick() {
  var data = emptySlotTable.row(this).data();

}



function
  refreshEmptySlotTable() {
  DbFactory.post({
    topic: 'victoryExceptions',
    action: 'getEmptySlotTable'
  })
    .success(emptySlotTableSuccess)
    .error(fetchError);
}

function
emptySlotTableSuccess(data) { buildEmptySlotTable(data); }


/************************************************************************************************************************************/
    function messageAck(data) {

      $scope.seq = data.seq;

      //User confirmation popup
      var dialog = $mdDialog.confirm()
          .title('Acknowledge - SKU: ' + data.sku + ' - UOM: ' + data.uom + ' - Location: ' + data.location)
          .textContent("Are you sure you want to acknowlege this Victory Exception?")
          .ok('Acknowledge')
          .cancel('Cancel');
        $mdDialog
          .show(dialog)
          .then(acknowledgeException);
    }

    function acknowledgeException(){
      Global.busyRefresh(true);
      DbFactory.post({
            topic: 'victoryExceptions',
            action: 'exceptionAck',
            params: { 
                      seq: $scope.seq
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
      if($scope.selected==0)
        refreshBadLocationLabelTable();
      if($scope.selected==1)
	      refreshNoProductUPCTable();
      if($scope.selected==2)
	      refreshDamagedProductTable();
      if($scope.selected==3)
	      refreshEmptySlotTable();
    }


    function
    init()
      {
      Global.setTitle('Victory Exceptions');
      refresh();
      Global.recv('refresh',refresh,$scope);
      periodic = $interval(refresh, 30000);
      }

    init();

    $scope.$on('$destroy', function(){
      $interval.cancel(periodic);
    });
    
  
}

  function victoryExceptionsConfig($routeProvider)
  {
    $routeProvider
      .when('/victoryExceptions', {controller: 'VictoryExceptionsController',
                        templateUrl: '/app/victoryExceptions/victoryExceptions.view.html'});
  }
  
}())
