(function()
{
  angular
    .module('ui')
      .controller('OrdersController',ordersController);

  angular
    .module('ui')
      .config(ordersConfig);

  ordersController.$inject = ['$scope','$timeout','$routeParams',
                              '$mdDialog','$interval','Global','DbFactory'];
  
  function ordersController($scope,$timeout,$routeParams,$mdDialog,$interval,Global,DbFactory) {
    
    $scope.refresh = refresh;
    $scope.order = {};
    $scope.storedId = '';
    $scope.orderId = "";
    $scope.permit = Global.permit;
	
    // // // // //
    // TABLE HELPERS  
    var refreshCounter = 0;
    
    function refreshCount(n,name) {
      refreshCounter += n;
      if(refreshCounter>0)
        Global.busyRefresh(true);
      else
        Global.busyRefresh(false);
      if(true) //make true for logging
        console.log(name+": "+refreshCounter);
    } 
    
    function simpleDate(data) {
      if(data==null)
        return '';
      var date = new Date(data);
      var today = new Date();
      //if(today.toDateString()==date.toDateString())
      //  return date.toLocaleTimeString();
      return date.toLocaleString();
    }
    
    function
    dateRender_old(data,type,full,meta)
    {
      if(data==null)
        return '';		
      if(type!='display')
        return data;
      
      var date = new Date(data);
	    return date.toLocaleString('en-US',{year: 'numeric', month: 'numeric', day: 'numeric',hour: 'numeric',minute:'numeric' });
      //return date.toLocaleString();
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

    function checkMarkRender(data,type,full,meta){
      if(data==null)
        return '';		
      if(type!='display')
        return data;
      return data >0 ? '&check;' : '';      
    } 

    function buttonRender(ref, data, perm, title, name, cb, enabled) {
        //name = name.replace(/\s+/g, '');
      //console.log("butoon: " + JSON.stringify(data));
      $(ref + ' tbody').off('click', '#' + name + 'button');
      $(ref + ' tbody').on('click', '#' + name + 'button', (e) => cb(data,title));
      if (enabled) { 
        if (Global.permit(perm))
          return '<button id="' + name+ 'button">' + title + '</button>';
        else
          return '<button disabled>' + title + '</button>';
      } else {
          return '';
      }
    }    

    function executeError(err) {
        console.log(err);
        refreshCount(-1, "error");		
    }
	
    function clearCount() {
      refreshCount(-1, "clear ct");
    }
	
    function executeSuccess() {
      refresh();
      refreshCount(-1,'executeSuccess');
    }	

    function toDueDate(date){
      return date.getFullYear() + 
        ('00' +(date.getMonth()+1)).slice(-2) +
        ('00' +(date.getDate())).slice(-2);
    }    

    // // // // //
    // DATA RETRIEVAL
    //======================================================================================================================================
    // // //
    // TAB 0 unreleased orders

    var unreleasedAll = [];
    
    function refreshUnreleasedOrders() {
      DbFactory.post({topic: 'orders',
                      action: 'unreleased'
                     })
        .success(unreleasedOrdersSuccess)
        .error  (executeError);
    }
    
    function unreleasedOrdersSuccess(data) { 
	    unreleasedAll = data ; 
      buildUnreleasedOrdersTable(unreleasedOrdersFilter()); 
    }  
	
    function unreleasedOrdersFilter(){
      var filtered = [] ;
      for(var i=0 ; i<unreleasedAll.length ; i++)
      {
        filtered.push(unreleasedAll[i]);
      }
      return filtered;
    }	

    var unreleasedOrdersTable = null;
    
    function buildUnreleasedOrdersTable(data) {
      var cols = [];
      var ref = "#unreleasedOrder";

      cols.push({title: "Order ID", 		data:"orderId", 		class:"dt-center"}) ;
      cols.push({title: "Batch ID",   data:"waveName",class:"dt-center"});
	  cols.push({title: "Carton Type",   data:"orderType",class:"dt-center"});
	  cols.push({title: "Demand Date",   data:"demandDate",class:"dt-center"});
	  cols.push({title: "# Cartons",   data:"numCartons",class:"dt-center"});
      cols.push({title: "Downloaded",   data:"downloadStamp",class:"dt-center",render:dateRender});
      cols.push({title: "Prepared",   data:"prepareStamp",class:"dt-center",render:dateRender});
      cols.push({title: "Cartonized",   data:"cartonizeStamp",class:"dt-center",render:dateRender});
      
      if(unreleasedOrdersTable){
        unreleasedOrdersTable.clear();
        unreleasedOrdersTable.rows.add(data);
        unreleasedOrdersTable.draw(false);
      } else {
        unreleasedOrdersTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              columnDefs: [
                                {                                
                                targets: [0],
                                render: function (data, type, row, meta) 
                                {											
                                  if(data && data !== '') {
                                    data = '<a style="cursor: pointer;"> <strong>' + data + '</strong>';								
                                    return data;
                                  } else {
                                    data = '';
                                    return data;
                                  }
                                }
                                              }
                              ],
                              order: [],
                              scrollY: "700px",
                              scrollCollapse: true,
                              paging: true,
                              pagingType: "numbers",
                              pageLength: 100,
                              dom: 'lftBipr',
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','td',unleasedOrdersTableClick);
        setTimeout(function(){unreleasedOrdersTable.draw();},0);
      }
      //refreshCount(-1);
    }
    
    function unleasedOrdersTableClick() {
      var data = unreleasedOrdersTable.row(this).data();
      var idx = unreleasedOrdersTable.cell(this).index().column;
      if( data.orderId ){
        if(idx==0){
          // console.log("here");
          $scope.orderId = data.orderId;
          $scope.selected = 4;
          $scope.$apply() ;
          lookup();				
        }
      }
    }
    
    //======================================================================================================================================
    // // //
    // TAB 1 started orders

    var startedAll = [];
	
    function refreshStartedOrders() {
      DbFactory.post({topic: 'orders',
                      action: 'started'
                     })
        .success(startedOrdersSuccess)
        .error  (executeError);
    }
    
    function startedOrdersSuccess(data) { 
	  startedAll = data ; 
      buildStartedOrdersTable(startedOrdersFilter()); 
    }
	
    function startedOrdersFilter() {
      var filtered = [] ;	
      for(var i=0 ; i<startedAll.length ; i++)
      {
        filtered.push(startedAll[i]);
      }
      return filtered;
    }	
	
    var startedOrdersTable = null;	
	
    function
    buildStartedOrdersTable(data)
    {
      var cols = [];
      var ref = "#startedOrder";
      
      cols.push({title: "Order ID", 		data:"orderId", 		class:"dt-center"}) ;
      cols.push({title: "Batch ID",   data:"waveName",class:"dt-center"});
	  cols.push({title: "Carton Type",   data:"orderType",class:"dt-center"});
	  cols.push({title: "Demand Date",   data:"demandDate",class:"dt-center"});
	  cols.push({title: "# Cartons",   data:"numCartons",class:"dt-center"});
	  cols.push({title: "# Picked",   data:"numCartonsPicked",class:"dt-center"});
	  cols.push({title: "# Labeled",   data:"numCartonsLabeled",class:"dt-center"});
	  cols.push({title: "# Sorted",   data:"numCartonsSorted",class:"dt-center"});
      cols.push({title: "Downloaded",   data:"downloadStamp",class:"dt-center",render:dateRender});
      cols.push({title: "Released",     data:"releaseStamp",class:"dt-center",render:dateRender});
      cols.push({title: "Pick Start",       data:"pickStartStamp",class:"dt-center",render:dateRender});
      cols.push({title: "Pick End",       data:"pickEndStamp",class:"dt-center",render:dateRender});
      cols.push({title: "Labeled",   data:"labelStamp",class:"dt-center",render:dateRender});	  			  
      
      if(startedOrdersTable){
        startedOrdersTable.clear();
        startedOrdersTable.rows.add(data);
        startedOrdersTable.draw(false);
      } else {
        startedOrdersTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              columnDefs: [
                                {                                
                                targets: [0],
                                render: function (data, type, row, meta) 
                                {											
                                  if(data && data !== '') {
                                    data = '<a style="cursor: pointer;"> <strong>' + data + '</strong>';								
                                    return data;
                                  } else {
                                    data = '';
                                    return data;
                                  }
                                }
                                              }
                              ],
                              order: [],
                              scrollY: "700px",
                              scrollCollapse: true,
                              paging: true,
                              pagingType: "numbers",
                              pageLength: 100,
                              dom: 'lftBipr',
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','td',startedOrdersTableClick);
        setTimeout(function(){startedOrdersTable.draw();},0);
      }
      refreshCount(-1, "build started orders table");
    }
    
    function startedOrdersTableClick()
    {
      var data = startedOrdersTable.row(this).data();
      var idx = startedOrdersTable.cell(this).index().column;
      if( data.orderId ){
        if(idx==0){
          $scope.orderId = data.orderId;
          $scope.selected = 4;
          $scope.$apply() ;
          lookup();				
        } 
      }
    }
	
    // // //
    // TAB 3 completed orders
	
    var completedAll = [];
	
    $scope.displayTodayCompleted = displayTodayCompleted;
    $scope.displayCompleted = displayCompleted;
	
    function displayCompleted() {
      if($scope.date1 && $scope.date2){
        $scope.d1 = new Date($scope.date1);
        $scope.d2 = new Date($scope.date2);
          DbFactory.post({topic: 'orders',
                  action: 'completedBetween',
                    params:{start: toDueDate($scope.d1),
                    end: toDueDate($scope.d2)}
                       })
          .success(completedOrdersSuccess)
          .error  (executeError);
      } else if($scope.date1 && (!$scope.date2)){
        $scope.d1 = new Date($scope.date1);
          DbFactory.post({topic: 'orders',
                  action: 'completedAfter',
                    params:{start: toDueDate($scope.d1)}
                       })
          .success(completedOrdersSuccess)
          .error  (executeError);		
      } else if($scope.date2 && (!$scope.date1)){
        $scope.d2 = new Date($scope.date2);
          DbFactory.post({topic: 'orders',
                  action: 'completedBefore',
                    params:{end: toDueDate($scope.d2)}
                       })
          .success(completedOrdersSuccess)
          .error  (executeError);		
      } else{
        displayTodayCompleted();
      }			
    }
	
    function displayTodayCompleted() {
      DbFactory.post({topic: 'orders',
                      action: 'completedToday'
                     })
        .success(completedOrdersSuccess)
        .error  (executeError);
    }
    
    function completedOrdersSuccess(data) { 
	    completedAll = data ; 
      buildCompletedOrdersTable(completedOrdersFilter()); 
    }
	
    function completedOrdersFilter() {
      var filtered = [] ;	
      for(var i=0 ; i<completedAll.length ; i++)
      {
        filtered.push(completedAll[i]);	
      }
      return filtered;
    }	
	
    var completedOrdersTable = null;
    
    function buildCompletedOrdersTable(data) {
      var cols = [];
      var ref = "#completedOrder";
      
      cols.push({title: "Order ID", 		data:"orderId", 		class:"dt-center"}) ;
      cols.push({title: "Batch ID",   data:"waveName",class:"dt-center"});
	  cols.push({title: "Carton Type",   data:"orderType",class:"dt-center"});
	  cols.push({title: "Demand Date",   data:"demandDate",class:"dt-center"});
	  cols.push({title: "# Cartons",   data:"numCartons",class:"dt-center"});
      cols.push({title: "Downloaded",   data:"downloadStamp",class:"dt-center",render:dateRender});
      cols.push({title: "Released",     data:"releaseStamp",class:"dt-center",render:dateRender});
      cols.push({title: "Pick Start",       data:"pickStartStamp",class:"dt-center",render:dateRender});
      cols.push({title: "Pick End",       data:"pickEndStamp",class:"dt-center",render:dateRender});
      cols.push({title: "Labeled",   data:"labelStamp",class:"dt-center",render:dateRender});	  
      cols.push({title: "Completed",      data:"completeStamp",class:"dt-center",render:dateRender}); 
      
      if(completedOrdersTable){
        completedOrdersTable.clear();
        completedOrdersTable.rows.add(data);
        completedOrdersTable.draw(false);
      } else {
        completedOrdersTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              columnDefs: [
                                {                                
                                targets: [0],
                                render: function (data, type, row, meta) 
                                {											
                                  if(data && data !== '') {
                                    data = '<a style="cursor: pointer;"> <strong>' + data + '</strong>';								
                                    return data;
                                  } else {
                                    data = '';
                                    return data;
                                  }
                                }
                                              }
                              ],
                              order: [],
                              scrollY: "700px",
                              scrollCollapse: true,
                              paging: true,
                              pagingType: "numbers",
                              pageLength: 100,
                              dom: 'lftBipr',
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','td',completedOrdersTableClick);
        setTimeout(function(){completedOrdersTable.draw();},0);
      }
      refreshCount(-1, "build completed orders table");
    }
    
    function completedOrdersTableClick() {
      var data = completedOrdersTable.row(this).data();
      var idx = completedOrdersTable.cell(this).index().column;
      if( data.orderId ){
        if(idx==0){
          $scope.orderId = data.orderId;
          $scope.selected = 4;
          $scope.$apply() ;
          lookup();				
        } 
      }
    }	
	
	
    // // //
    // TAB 3 error orders

    var errorAll = [];

    function refreshErrorOrders() {
      DbFactory.post({topic: 'orders',
                      action: 'error'
                     })
        .success(errorOrdersSuccess)
        .error  (executeError);
    }
    
    function errorOrdersSuccess(data) {
	    errorAll = data ; 
      buildErrorOrdersTable(errorOrdersFilter()); 
    }
	
    function errorOrdersFilter() {
      var filtered = [] ;	
      for(var i=0 ; i<errorAll.length ; i++)
      {
        filtered.push(errorAll[i]);
      }
      return filtered;
    }	
	
    var errorOrdersTable = null;
	
    function buildErrorOrdersTable(data) {
      var cols = [];
      var ref = "#errorOrder";
      
      cols.push({title: "Order ID",   data:"orderId",class:"dt-center"});
      cols.push({title: "Batch ID",   data:"waveName",class:"dt-center"});
	  cols.push({title: "Carton Type",   data:"orderType",class:"dt-center"});
	  cols.push({title: "Demand Date",   data:"demandDate",class:"dt-center"});
      cols.push({title: "Downloaded",   data:"downloadStamp",class:"dt-center",render:dateRender});
      cols.push({title: "Last Activity",   data:"stamp",class:"dt-center",render:dateRender});
      cols.push({title: "Error",     data:"errorMsg"}); 
      cols.push({title: "Retry",     data:null, class:"dt-center"});   	  
      
      if(errorOrdersTable){
        errorOrdersTable.clear();
        errorOrdersTable.rows.add(data);
        errorOrdersTable.draw(false);
      } else {
        errorOrdersTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
							  rowCallback: errorsCallback,
                              columnDefs: [
                                {                                
                                targets: [0],
                                render: function (data, type, row, meta) 
                                {											
                                  if(data && data !== '') {
                                    data = '<a style="cursor: pointer;"> <strong>' + data + '</strong>';								
                                    return data;
                                  } else {
                                    data = '';
                                    return data;
                                  }
                                }
                                              }
                              ],
                              order: [],
                              scrollY: "700px",
                              scrollCollapse: true,
                              paging: true,
                              pagingType: "numbers",
                              pageLength: 100,
                              dom: 'lftBipr',
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','button',errorOrdersButtonClick);
        $(ref+' tbody').on('click','td',errorOrdersTableClick);
        setTimeout(function(){errorOrdersTable.draw();},0);
      }
      refreshCount(-1, "build errors order table");
    }
    
    function errorsCallback(row,data,index) {
      if(Global.permit('ordersEdit')){
        $("td:eq(7)",row).html('<button class="tableButton">Retry</button>');
      } else {
        $("td:eq(7)",row).html('<button class="tableButton" disabled>Retry</button>');
      }
    }

    function errorOrdersButtonClick() {
      var row  = $(this).parents('tr');
      var data = errorOrdersTable.row(row).data();
      var col = $(this).parents('td')[0].cellIndex;

      if(data && data.orderId && col==7)
        doRetryOrder(data.orderId);
    }

    function doRetryOrder(orderId) {
      var dialog = $mdDialog.confirm()
      .title('Retry Order')
      .textContent('Are you sure you want to retry order '+orderId+'?')
      .ariaLabel('Retry Order')
      .ok('Yes')
      .cancel('Cancel');

      $mdDialog
        .show(dialog)
        .then(function(){
		  var data = {};
		  data.orderId = orderId;
		  var user = Global.getUser().user;
		  DbFactory.post({
				topic: 'orders',
				action: 'insertStatus',
				params: {
					  statusType : 'retryOrder',
					  data : JSON.stringify(data),
					  appName: 'statusApp',
					  operator : user
				}
		  })
         .error  (executeError);
         $scope.working = true ;
         $timeout(function() {
            refresh();
            $scope.working = false ;
         },2000) ;
      });
    }	
	
    function errorOrdersTableClick() {
      var data = errorOrdersTable.row(this).data();
      var idx = errorOrdersTable.cell(this).index().column;
      if( data.orderId ){
        if(idx==0){
          $scope.orderId = data.orderId;
          $scope.selected = 4;
          $scope.$apply() ;
          lookup();				
        } 
      }
    }	
	
	// // //
	// TAB 6 order detail
    $scope.lookup = lookup;
    $scope.lookupOrderId= lookupOrderId;
	$scope.selectOrder = selectOrder;
	$scope.cancelOrder = cancelOrder;
	$scope.editLine = editLine;	
	$scope.editingLines = false;
	
    function lookup() {
      refreshCount(1, "lookup orders");
        DbFactory.post({topic: 'orders',
                        action: 'lookup',
              params: {orderId: $scope.orderId}
                       })
          .success(populateOrder)
          .error  (executeError);			
    }
    
    function lookupOrderId() {
      refreshCount(1, "lookup orders");
        DbFactory.post({topic: 'orders',
                        action: 'lookupOrderId',
              params: {orderId: '%'+$scope.orderId+'%'}
                       })
          .success(populateOrder)
          .error  (executeError);			
    }
	
	function cancelOrder() {
      var dialog = $mdDialog.confirm()
      .title('Cancel Order')
      .textContent('Are you sure you want to cancel order ' + $scope.order.orderId + '?' )
      .ariaLabel('Cancel Order')
      .ok('Yes')
      .cancel('Cancel');

      $mdDialog
        .show(dialog)
        .then(function(){
		  var data = {};
		  data.orderId = $scope.order.orderId;
		  var user = Global.getUser().user;
		  DbFactory.post({
				topic: 'orders',
				action: 'insertStatus',
				params: {
					  statusType : 'cancelOrder',
					  data : JSON.stringify(data),
					  appName: 'statusApp',
					  operator : user
				}
		  })			
         .error  (executeError);
         $scope.working = true ;
         $timeout(function() {
		    $scope.working = false ;
		    refresh();
         },2000) ;
      });		
		
	}
	
    function populateOrder(data) {
      refreshCount(-1, "populate order");
      if(data.length > 10){
        Global.showMessage('Too many matching records found!');				
        $scope.order = {};
      } else if(data.length==0) {
        Global.showMessage('Not found!');
        $scope.order = {};
      } else if(data.length==1){
        $scope.orderId = data[0].orderId;
        prepareOrder( data[0] );
        populatePicks($scope.order.orderId);
        populateLines($scope.order.orderId);
        populateCartons($scope.order.orderId);
        populateFullcases($scope.order.orderId);
        populateOrderHistory($scope.order.orderId);
      } else {
          ordersLookupDialog( data );
      }
    }
	
    function ordersLookupDialog( data ) {
      $scope.candidates = data;
      $mdDialog.show({
        templateUrl: 'lookup.html',
        clickOutsideToClose: true,
        scope: $scope,
        preserveScope: true,
        controller: function($scope) {},
        parent: angular.element(document.body),
      })
    }	
	
    function selectOrder(order)  {				
      if (order) {
        $scope.orderId = order.orderId;        
        prepareOrder( order );
        populatePicks($scope.order.orderId);
        populateLines($scope.order.orderId);
        populateCartons($scope.order.orderId);
        populateFullcases($scope.order.orderId);
        populateOrderHistory($scope.order.orderId); 
      }
      $mdDialog.hide();			
    }
		
    // order object
    function prepareOrder( order ) {
      $scope.order = order;
    }
    
    // order picks
    function populatePicks( orderId ) {
      DbFactory.post({topic: 'orders',
                      action: 'picks',
            params: {orderId: orderId}
                     })
        .success(populatePicksSuccess)
        .error  (executeError);			
    }
	
    function populatePicksSuccess(data) {
      buildPicksTable(data);
    }		
	
    var picksTable = null;
	
    function buildPicksTable(data) {
      var cols = [];
      var ref = "#picks";
      
      cols.push({title: "Line Seq",       data:"orderLineSeq",class:"dt-center"});
      cols.push({title: "Pick Seq",       data:"pickSeq",class:"dt-center"});
      cols.push({title: "Carton Seq",       data:"cartonSeq",class:"dt-center"});
      cols.push({title: "Pick Type",       data:"pickType",class:"dt-center"});
      cols.push({title: "SKU",   data:"sku",class:"dt-center"});
      cols.push({title: "UOM",   data:"uom",class:"dt-center"});
	  cols.push({title: "Base UOM",   data:"baseUom",class:"dt-center"});
      cols.push({title: "Picker",  data:"pickOperatorId",class:"dt-center"});
      cols.push({title: "Picked",      data:"pickStamp",class:"dt-center",render:dateRender});
      cols.push({title: "Picked Short",      data:"shortStamp",class:"dt-center",render:dateRender});	  
      //cols.push({title: "ship Short", data:null,class:"dt-center"});   
      //cols.push({title: "Mark Picked", data:null,class:"dt-center"});   
      
      if(picksTable){
        picksTable.clear();
        picksTable.rows.add(data);
        picksTable.draw(false);
      } else {
        picksTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              order: [],
                              rowCallback: picksCallback,
                              scrollY: "700px",
                              scrollCollapse: true,
                              paging: true,
                              pagingType: "numbers",
                              dom: 'ftBipr',
                              lengthMenu: [ 50,100 ],
                              pageLength: 100,
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','button',picksButtonClick);
        setTimeout(function(){picksTable.draw();},0);
      }
    }

    function picksCallback(row,data,index) {
      if(Global.permit('ordersEdit') && !data.pickStamp ){
        $("td:eq(10)",row).html('<button class="tableButton">Short Pick</button>');
      } else {
        $("td:eq(10)",row).html('<button class="tableButton" disabled>Short Pick</button>');
      }
      if(Global.permit('ordersEdit') && !data.pickStamp && data.readyForPick==1 && data.canceled==0 && data.pickOperatorId==''){
        $("td:eq(11)",row).html('<button class="tableButton">Mark Picked</button>');
      } else {
        $("td:eq(11)",row).html('<button class="tableButton" disabled>Mark Picked</button>');
      }
    }

    function picksButtonClick() {
      var row  = $(this).parents('tr');
      var data = picksTable.row(row).data();
      var col = $(this).parents('td')[0].cellIndex;

      if(data && data.pickSeq && col==10)
        doShortPick(data.pickSeq, data.sku, data.uom);
      if(data && data.pickSeq && col==11)
        doMarkPicked(data.pickSeq, data.sku, data.uom);
    }

    function doShortPick(pickSeq,sku,uom) {
      var dialog = $mdDialog.confirm()
      .title('Confirm Short')
      .textContent('Are you sure you want to short 1 '+sku+' '+uom+'?')
      .ariaLabel('Confirm Short')
      .ok('Yes')
      .cancel('Cancel');

      $mdDialog
        .show(dialog)
        .then(function(){
          DbFactory.post({topic: 'orders',
                            action: 'dashboardShort',
                            params: {pickSeq: pickSeq}
                          })
         .error  (executeError);
         $scope.working = true ;
         $timeout(function() {
            lookup();
            $scope.working = false ;
         },2000) ;
      });
    }

    function doMarkPicked(pickSeq,sku,uom) {
      var dialog = $mdDialog.confirm()
      .title('Mark Picked')
      .textContent('Are you sure you want to mark 1 '+sku+' '+uom+' as picked?')
      .ariaLabel('Mark Picked')
      .ok('Yes')
      .cancel('Cancel');

      $mdDialog
        .show(dialog)
        .then(function(){
          DbFactory.post({topic: 'orders',
                            action: 'dashboardPick',
                            params: {pickSeq: pickSeq}
                          })
         .error  (executeError);
         $scope.working = true ;
         $timeout(function() {
            lookup();
            $scope.working = false ;
         },2000) ;
      });
    }

    // order lines
    function populateLines( orderId ) {
      DbFactory.post({topic: 'orders',
                      action: 'lines',
            params: {orderId: orderId}
                     })
        .success(populateLinesSuccess)
        .error  (executeError);			
    }
    
    function populateLinesSuccess(data) {
      buildLinesTable(data);
    }	

    var linesTable = null;	
	
    function buildLinesTable(data) {
      var cols = [];
      var ref = "#lines";
      
      cols.push({title: "Line Seq",       data:"orderLineSeq",class:"dt-center"});
	  cols.push({title: "Page ID",       data:"pageId",class:"dt-center"});
      cols.push({title: "Line ID",       data:"lineId",class:"dt-center"});
      cols.push({title: "Status",       data:"status",class:"dt-center"});
      cols.push({title: "SKU",   data:"sku",class:"dt-center"});
      cols.push({title: "UOM",   data:"uom",class:"dt-center"});
	  cols.push({title: "Location",   data:"location",class:"dt-center"});
      cols.push({title: "QTY",   data:"qty",class:"dt-center"});
      cols.push({title: "Actual qty",       data:"actQty",class:"dt-center"});
	  cols.push({title: "Picked",   data:"pickStamp",class:"dt-center",render:dateRender});
	  cols.push({title: "Uploaded",   data:"uploadStamp",class:"dt-center",render:dateRender});
      
      if(linesTable){
        linesTable.clear();
        linesTable.rows.add(data);
        linesTable.draw(false);
      } else {
        linesTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,                             
                              order: [],
                              scrollY: "700px",
                              scrollCollapse: true,
                              paging: true,
                              pagingType: "numbers",
                              dom: 'ftBipr',
                              lengthMenu: [ 50,100 ],
                              pageLength: 100,
                              buttons: ['copy','print','excel','pdf']});                              
        setTimeout(function(){linesTable.draw();},0);
      }
    }

    // order cartons
    function populateCartons( orderId ) {
      DbFactory.post({topic: 'orders',
                      action: 'cartons',
            params: {orderId: orderId}
                     })
        .success(populateCartonsSuccess)
        .error  (executeError);			
    }
	
    function populateCartonsSuccess(data) {
      buildCartonsTable(data);
    }		
	
    var cartonsTable = null;	
	
    function buildCartonsTable(data) {
      var cols = [];
      var ref = "#cartons";
      
      cols.push({title: "Carton Seq",   data:"cartonSeq",class:"dt-center"});
      cols.push({title: "LPN",       data:"lpn",class:"dt-center"});
	  cols.push({title: "Tracking",       data:"trackingNumber",class:"dt-center"});
	  cols.push({title: "Type",       data:"cartonType",class:"dt-center"});
      cols.push({title: "Pick Area",   data:"pickType",class:"dt-center"});
      cols.push({title: "Picked",   data:"pickStamp",class:"dt-center",render:dateRender});
      cols.push({title: "Labeled",   data:"labelStamp",class:"dt-center",render:dateRender});           
      cols.push({title: "Sorted",   data:"shipStamp",class:"dt-center",render:dateRender});	      
      
      if(cartonsTable){
        cartonsTable.clear();
        cartonsTable.rows.add(data);
        cartonsTable.draw(false);
      } else {
        cartonsTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              columnDefs: [
                                {                                
                                targets: [0],
                                render: function (data, type, row, meta) 
                                {											
                                  if(data && data !== '') {
                                    data = '<a style="cursor: pointer;"> <strong>' + data + '</strong>';								
                                    return data;
                                  } else {
                                    data = '';
                                    return data;
                                  }
                                }
                                              }
                              ],                              
                              order: [],
                              scrollY: "700px",
                              scrollCollapse: true,
                              paging: true,
                              pagingType: "numbers",
                              dom: 'ftBipr',
                              lengthMenu: [ 50,100 ],
                              pageLength: 100,
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','td',orderCartonsClick) ;
        setTimeout(function(){cartonsTable.draw();},0);
      }
    }	

    function orderCartonsClick() {
      var idx = cartonsTable.cell(this).index().column;		
      var data = cartonsTable.row(this).data() ;
      if( idx == 0 ){
        var cartonSeq = data.cartonSeq;
        window.location = '#/cartons?cartonSeq=' + cartonSeq; 
      } 
    }	

    // order fullcases 
    function populateFullcases( orderId ) {
      DbFactory.post({topic: 'orders',
                      action: 'fullcases',
            params: {orderId: orderId}
                     })
        .success(populateFullcasesSuccess)
        .error  (executeError);			
    }
	
    function populateFullcasesSuccess(data) {
      buildFullcasesTable(data);
    }		
	
    var fullcasesTable = null;	
	
    function buildFullcasesTable(data) {
      var cols = [];
      var ref = "#fullcases";
      
      cols.push({title: "Carton Seq",   data:"cartonSeq",class:"dt-center"});
      cols.push({title: "Tracking",       data:"trackingNumber",class:"dt-center"});
      cols.push({title: "Pick Area",   data:"pickType",class:"dt-center"});
      cols.push({title: "Sorted",   data:"shipStamp",class:"dt-center",render:dateRender});	      
      
      if(fullcasesTable){
        fullcasesTable.clear();
        fullcasesTable.rows.add(data);
        fullcasesTable.draw(false);
      } else {
        fullcasesTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              columnDefs: [
                                {                                
                                targets: [0],
                                render: function (data, type, row, meta) 
                                {											
                                  if(data && data !== '') {
                                    data = '<a style="cursor: pointer;"> <strong>' + data + '</strong>';								
                                    return data;
                                  } else {
                                    data = '';
                                    return data;
                                  }
                                }
                                              }
                              ],                              
                              order: [],
                              scrollY: "700px",
                              scrollCollapse: true,
                              paging: true,
                              pagingType: "numbers",
                              dom: 'ftBipr',
                              lengthMenu: [ 50,100 ],
                              pageLength: 100,
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','td',orderFullcasesClick) ;
        setTimeout(function(){fullcasesTable.draw();},0);
      }
    }	

    function orderFullcasesClick() {
      var idx = fullcasesTable.cell(this).index().column;		
      var data = fullcasesTable.row(this).data() ;
      if( idx == 0 ){
        var cartonSeq = data.cartonSeq;
        window.location = '#/cartons?cartonSeq=' + cartonSeq; 
      } 
    }	

    //order history
    function populateOrderHistory( orderId ) {
        DbFactory.post({topic: 'orders',
                        action: 'history',
              params: {orderId: orderId}
                       })
          .success(populateOrderHistorySuccess)
          .error  (executeError);			
    }
	
    //order history
    function populateOrderHistory( orderId ) {
        DbFactory.post({topic: 'orders',
                        action: 'history',
              params: {orderId: orderId}
                       })
          .success(populateOrderHistorySuccess)
          .error  (executeError);			
    }
	
    function populateOrderHistorySuccess(data) {
      buildOrderHistoryTable(data);
    }
	
    var orderHistoryTable = null;	
	
    function buildOrderHistoryTable(data) {
      var cols = [];
      var ref = "#orderHistory";
      
      cols.push({title: "Code",   data:"code",class:"dt-center"});
      cols.push({title: "Description",     data:"message",class:"dt-center"});
      cols.push({title: "Stamp",			data:"stamp",		class:"dt-center",	type:"date",	render: dateRender});	
      
      if(orderHistoryTable){
        orderHistoryTable.clear();
        orderHistoryTable.rows.add(data);
        orderHistoryTable.draw(false);
      } else {
        orderHistoryTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              order: [],
                              scrollY: "700px",
                              scrollCollapse: true,
                              paging: true,
                              pagingType: "numbers",
                              dom: 'ltBipr',
                              lengthMenu: [ 50,100 ],
                              pageLength: 100,
                              buttons: ['copy','print','excel','pdf']});
        setTimeout(function(){orderHistoryTable.draw();},0);
      }
    }
	
	function editLine(){
		$scope.editingLines = true;
		$scope.hasChange = false;
		populateEditLines($scope.order.orderId);
	}
	
    // order editEditLines
	
	
	var editLinesAll = [];
	
    function populateEditLines( orderId ) {
      DbFactory.post({topic: 'orders',
                      action: 'lines',
            params: {orderId: orderId}
                     })
        .success(populateEditLinesSuccess)
        .error  (executeError);			
    }
    
    function populateEditLinesSuccess(data) {
	  editLinesAll = data;
	  for( var i=0;i<editLinesAll.length;i++){
		  editLinesAll[i].newQty = '';
		  editLinesAll[i].changed = false;
	  }
      buildEditLinesTable(editLinesAll);
    }	

    var editLinesTable = null;	
	
    function buildEditLinesTable(data) {
      var cols = [];
      var ref = "#editLines";
      
      cols.push({title: "Line Seq",       data:"orderLineSeq",class:"dt-center"});
	  cols.push({title: "Page ID",       data:"pageId",class:"dt-center"});
      cols.push({title: "Line ID",       data:"lineId",class:"dt-center"});
      cols.push({title: "Status",       data:"status",class:"dt-center"});
      cols.push({title: "SKU",   data:"sku",class:"dt-center"});
      cols.push({title: "UOM",   data:"uom",class:"dt-center"});
	  cols.push({title: "Location",   data:"location",class:"dt-center"});
      cols.push({title: "QTY",   data:"qty",class:"dt-center"});
      cols.push({title: "Picked qty",       data:"actQty",class:"dt-center"});
	  cols.push({title: "Labeled qty",       data:"labeledQty",class:"dt-center"});
	  cols.push({title: "Change Qty", data:null,class:"dt-center"});
	  cols.push({title: "New Qty", data:"newQty",class:"dt-center"});

      
      if(editLinesTable){
        editLinesTable.clear();
        editLinesTable.rows.add(data);
        editLinesTable.draw(false);
      } else {
        editLinesTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,                             
                              order: [],
							  rowCallback: editLinesCallback,
                              scrollY: "700px",
                              scrollCollapse: true,
                              paging: true,
                              pagingType: "numbers",
                              dom: 'ftBipr',
                              lengthMenu: [ 50,100 ],
                              pageLength: 100,
                              buttons: ['copy','print','excel','pdf']});       
        $(ref+' tbody').on('click','button',editLinesButtonClick);							  
        setTimeout(function(){editLinesTable.draw();},0);
      }
    }	
	
    function editLinesCallback(row,data,index) {
	  if(data.changed) {
		  $(row).css('background-color','#e0ffe0') ;
	  } else {
		  $(row).css('background-color','') ;
      } 		
      if(Global.permit('ordersEdit') && !data.labelStamp ){
        $("td:eq(10)",row).html('<button class="tableButton">Change Qty</button>');
      } else {
        $("td:eq(10)",row).html('<button class="tableButton" disabled>Change Qty</button>');
      }
    }

    function editLinesButtonClick() {
      var row  = $(this).parents('tr');
      var data = editLinesTable.row(row).data();
      var col = $(this).parents('td')[0].cellIndex;

      if(data && data.orderLineSeq && col==10)
        changeQty(data);
    }	
	
	function changeQty(data) {
		$scope.selectedLine = data.orderId+'/'+data.pageId+'/'+data.lineId+'/'+data.sku+' '+data.uom;
		$scope.originQty = data.qty;
		$scope.newQty = data.qty;
		$scope.labeledQty = data.labeledQty;
		$scope.selectLine = data;
		$mdDialog.show({
			templateUrl: 'changeQty.html',
			clickOutsideToClose: false,
			scope: $scope,
			preserveScope: true,
			controller: function($scope) {},
			parent: angular.element(document.body),
		});	
		$timeout(function() { 
			cancelChangeQty();
		},30000) ;				
	}

	$scope.doChangeQty = doChangeQty;
	$scope.cancelChangeQty = cancelChangeQty;	
    $scope.updateOrderLineQty = updateOrderLineQty;	
		
	function cancelChangeQty(){			
	  $mdDialog.cancel();
	}

    function doChangeQty(){
	  $scope.changedLine = 0;
	  for(var i=0 ; i<editLinesAll.length ; i++) {
		if( (editLinesAll[i].orderLineSeq==$scope.selectLine.orderLineSeq) ){
		  if( $scope.newQty != $scope.selectLine.qty ){
			  editLinesAll[i].newQty = $scope.newQty ;
			  editLinesAll[i].changed = true;
			  $scope.hasChange = true;
		  }
		}
		if(editLinesAll[i].changed)
			$scope.changedLine++;
	  } 
      $mdDialog.hide();	  
      buildEditLinesTable(editLinesAll);	  
	}
	
	function updateOrderLineQty(){
      var dialog = $mdDialog.confirm()
      .title('Update Order Line Qty')
      .textContent('Update qty for ' + $scope.changedLine+' lines.')
      .ariaLabel('Update Order Line Qty')
      .ok('Yes')
      .cancel('Cancel');

      $mdDialog
        .show(dialog)
        .then(function(){
		  var data = {};
		  data.orderId = $scope.order.orderId;
		  data.changedLines = [];
		  for( var i=0 ; i<editLinesAll.length ; i++ ){
			  if( editLinesAll[i].changed ){
				  let line = {};
				  line.orderLineSeq = editLinesAll[i].orderLineSeq;
				  line.qty = editLinesAll[i].qty;
				  line.newQty = editLinesAll[i].newQty;
				  data.changedLines.push(line);
			  }
		  }
		  var user = Global.getUser().user;
		  DbFactory.post({
				topic: 'orders',
				action: 'insertStatus',
				params: {
					  statusType : 'updateOrderLineQty',
					  data : JSON.stringify(data),
					  appName: 'statusApp',
					  operator : user
				}
		  })
         .error  (executeError);
         $scope.working = true ;
         $timeout(function() {
            refresh();
            $scope.working = false ;
			$scope.editingLines = false;
         },2000) ;
      });		
		
		
	}
    
    // // // // //
    // SETUP AND ALL THAT

    function
    refresh()
    {
      switch($scope.selected){
        case 0:
          refreshUnreleasedOrders();
          break;				
        case 1:
          refreshStartedOrders();
          break;				
        case 3:
          refreshErrorOrders();
          break;					
        default:
          refreshCount(0, "default tab rendered");
      }
    }

    function
    init()
    {
      Global.setTitle('Orders');
      Global.recv('refresh',refresh,$scope);
      periodic = $interval(refresh, 30000); 
      if($routeParams.orderId){
        $scope.selected = 4;
        $scope.orderId = $routeParams.orderId;
        lookup();
      } 
      refresh();
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

