(function () {
   angular
      .module('ui')
      .controller('CartonsController', cartonsController);

   angular
      .module('ui')
      .config(cartonsConfig);

   cartonsController.$inject = ['$scope', '$interval', '$timeout',
      '$mdDialog', '$routeParams',
      'Global', 'DbFactory'
   ];

   function
      cartonsController($scope, $interval, $timeout, $mdDialog, $routeParams,
         Global, DbFactory) {

      $scope.refresh = refresh;
      $scope.permit = Global.permit;
      $scope.selected = 0;
      $scope.carton = {};
      $scope.cartonInfo = {};
      $scope.carton.lpn = "";
      $scope.carton.orderId = "";
      $scope.carton.stamp = "";
      $scope.carton.lookupByLpn = "";
      $scope.cartonSeq = -1;
	  $scope.selectCarton = selectCarton;
	  $scope.labelCarton = labelCarton;
	  $scope.sortCarton = sortCarton;
	  $scope.auditCarton = auditCarton;
	  $scope.cancelInGeek = cancelInGeek;


      $scope.outsideLPN = "";


      $scope.lpnSearchDetails = lpnSearchDetails;
      $scope.lpnBySearch = "";
      $scope.cartonSeqToSearch = "";


      $scope.cartonLpn = "";
      $scope.lookupByLpn = lookupByLpn;

      //Date
      $scope.toDate = new Date();
      $scope.fromDate = new Date();
      $scope.changeStart = changeStart;
      $scope.changeEnd = changeEnd;
      $scope.display = display;

      $scope.goToOrdersPage = goToOrdersPage;
      $scope.goToPalletsPage = goToPalletsPage;

      function
      goToOrdersPage()
      {
         var data = $scope.cartonInfo.orderId;
         //console.log("Order: " + data);

         if(data) {
            window.location = "#/orders?orderId=" + data;
         }
      }

      function 
      goToPalletsPage() {
         var data = $scope.cartonInfo.palletSeq;
         //console.log("Pallet Seq: " + data);

         if(data) {
            window.location = "#/pallets?palletSeq=" + data;
         }
      }



      function
         changeStart() {
         if ($scope.fromDate.getTime() > $scope.toDate.getTime()) {
            $scope.toDate = new Date();
            $scope.toDate.setTime($scope.fromDate.getTime() + 1000);
         }
         $scope.reported = false;
      }

      function
         changeEnd() {
         if ($scope.fromDate.getTime() > $scope.toDate.getTime()) {
            $scope.fromDate = new Date();
            $scope.fromDate.setTime($scope.toDate.getTime() - 1000);
         }
         $scope.reported = false;
      }

      function
         display() {
         completeUpdate();
      }

      function
         dateRender_old(data) {
         if (data == null)
            return '';
         var date = new Date(data);
         var today = new Date();
         return date.toLocaleString('en-US', { year: "2-digit", month: "2-digit", day: "2-digit", hour12: true, hour: "2-digit", minute: "2-digit", second: "2-digit" });
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


      //FUNCTIONS/METHODS

      function lpnSearchDetails(){
 
         if ($scope.lpnBySearch !== "" || $scope.lpnBySearch !== $scope.carton.lpn){
            $scope.carton.lpn = $scope.lpnBySearch;
            lookupByLpn();
            document.getElementById("lpnSearch").value = "";
         }
      }

      function lookupByLpn() {
         infoUpdate();
      }
	  
	  function lookupBySeq(){
         DbFactory
            .post({
               topic: 'cartons',
               action: 'lookupSeq',
               params: {
                  input: $scope.cartonSeq
               }
            })
            .success(infoSuccess)
            .error(infoError);		  
	  }


      function
         infoUpdate() {
	     Global.busyRefresh(true);
         DbFactory
            .post({
               topic: 'cartons',
               action: 'info',
               params: {
                  input: '%'+$scope.lpnBySearch+'%'
               }
            })
            .success(infoSuccess)
            .error(infoError);
      }

      function infoSuccess(data) {
         Global.busyRefresh(false);
		 if(data.length > 10){
			Global.showMessage('Too many matching records found!');			
			$scope.cartonInfo = {};
			$scope.cartonSeq = -1;
		  } else if(data.length==0) {
			Global.showMessage('No carton found!');				
			$scope.cartonInfo = {};
			$scope.cartonSeq = -1;
		  } else if(data.length==1){
            $scope.cartonInfo = data[0];
            updateItems();
            updateHistory();
		  } else {
			cartonsLookupDialog( data );
		  }		 
      }
	  
		function cartonsLookupDialog( data )
		{
		  $scope.candidates = data;
		  $mdDialog.show({
			templateUrl: 'lookupCarton.html',
			clickOutsideToClose: true,
			scope: $scope,
			preserveScope: true,
			controller: function($scope) {},
			parent: angular.element(document.body),
		  })
		}	
		
		function selectCarton(carton) {				
		  if (carton) {			
			$scope.cartonInfo = carton;
            updateItems();
            updateHistory();
		  }
		  $mdDialog.hide();			
		}	  
		
		function labelCarton(){
		  var dialog = $mdDialog.confirm()
		  .title('Mark Carton Labeled')
		  .textContent('Are you sure you want to mark carton: ' + $scope.cartonInfo.lpn + ' labeled?')
		  .ariaLabel('Label Carton')
		  .ok('Yes')
		  .cancel('Cancel');
		  
		  $mdDialog
			.show(dialog)
			.then(function(){
			var data = {};
			data.cartonSeq = $scope.cartonInfo.cartonSeq;
	        var user = Global.getUser().user;
			DbFactory.post({topic: 'orders',
							action: 'insertStatus',
							params: {
								  appName : 'statusApp',
								  statusType : 'cartonLabel',
								  data : JSON.stringify(data),
								  operator : user
							}
							 });                      
			Global.busyRefresh(true);
			$timeout(function() { 
			   $scope.cartonSeq = $scope.cartonInfo.cartonSeq;
			   lookupBySeq();	
			   Global.busyRefresh(false);
			},2000) ;
		  });
		}	
		
		function sortCarton(){
		  var dialog = $mdDialog.confirm()
		  .title('Mark Carton Sorted')
		  .textContent('Are you sure you want to mark carton: ' + $scope.cartonInfo.lpn + ' sorted?')
		  .ariaLabel('Sort Carton')
		  .ok('Yes')
		  .cancel('Cancel');
		  
		  $mdDialog
			.show(dialog)
			.then(function(){
			var data = {};
			data.cartonSeq = $scope.cartonInfo.cartonSeq;
	        var user = Global.getUser().user;
			DbFactory.post({topic: 'orders',
							action: 'insertStatus',
							params: {
								  appName : 'statusApp',
								  statusType : 'cartonShip',
								  data : JSON.stringify(data),
								  operator : user
							}
							 });                      
			Global.busyRefresh(true);
			$timeout(function() { 
			   $scope.cartonSeq = $scope.cartonInfo.cartonSeq;
			   lookupBySeq();	
			   Global.busyRefresh(false);
			},2000) ;
		  });
		}		

		function auditCarton(){
		  var dialog = $mdDialog.confirm()
		  .title('Mark Carton Requiring Audit')
		  .textContent('Are you sure you want to mark carton: ' + $scope.cartonInfo.lpn + ' requiring audit?')
		  .ariaLabel('Mark Carton Requiring Audit')
		  .ok('Yes')
		  .cancel('Cancel');
		  
		  $mdDialog
			.show(dialog)
			.then(function(){
			var data = {};
			data.cartonSeq = $scope.cartonInfo.cartonSeq;
	        var user = Global.getUser().user;
			DbFactory.post({topic: 'orders',
							action: 'insertStatus',
							params: {
								  appName : 'statusApp',
								  statusType : 'requireAudit',
								  data : JSON.stringify(data),
								  operator : user
							}
							 });                      
			Global.busyRefresh(true);
			$timeout(function() { 
			   $scope.cartonSeq = $scope.cartonInfo.cartonSeq;
			   lookupBySeq();	
			   Global.busyRefresh(false);
			},2000) ;
		  });
		}

		function cancelInGeek(){
		  var dialog = $mdDialog.confirm()
		  .title('Cancel processing in Geek')
		  .textContent('Are you sure you want to cancel processing cartonSeq: ' + $scope.cartonInfo.cartonSeq + ' in Geek?')
		  .ariaLabel('Cancel processing in Geek')
		  .ok('Yes')
		  .cancel('Cancel');
		  
		  $mdDialog
			.show(dialog)
			.then(function(){
			var data = {};
			data.cartonSeq = $scope.cartonInfo.cartonSeq;
	        var user = Global.getUser().user;
			DbFactory.post({topic: 'cartons',
							action: 'shortGeekCartonPicks',
							params: {
								  cartonSeq : $scope.cartonInfo.cartonSeq
							}
							 });  
			DbFactory.post({topic: 'cartons',
							action: 'shortGeekCarton',
							params: {
								  cartonSeq : $scope.cartonInfo.cartonSeq
							}
							 }); 							 
			Global.busyRefresh(true);
			$timeout(function() { 
			   $scope.cartonSeq = $scope.cartonInfo.cartonSeq;
			   lookupBySeq();	
			   Global.busyRefresh(false);
			},2000) ;
		  });
		}		

/************************************************************************************************************************************/ 

      function
         updateItems() {
         DbFactory.post({
            topic: "cartons",
            action: "getItems",
            params: { cartonSeq: $scope.cartonInfo.cartonSeq }
         })
            .success(buildItemTable)
            .error((err) => { console.log(err) });
      }

      
      // items table

      var itemsTable = null;

      function
         buildItemTable(data) {
         var ref = "#itemsTable";
         var cols = [];

         cols.push({ title: "SKU", data: "sku", class: "dt-center" });
         cols.push({ title: "UoM", data: "uom", class: "dt-center" });
         cols.push({ title: "Qty", data: "qty", class: "dt-center" });
         //cols.push({ title: "Pick Status", data: "", class: "dt-center" });
         cols.push({ title: "Picked", data: "pickedFlag", class: "dt-center" });
         cols.push({ title: "Short Picked", data: "shortPickedFlag", class: "dt-center" });
         cols.push({ title: "Canceled", data: "canceledFlag", class: "dt-center" });
         cols.push({ title: "Picked By Operator", data: "pickOperatorId", class: "dt-center" });

         if (itemsTable) {
            itemsTable.clear();
            itemsTable.rows.add(data);
            itemsTable.draw(false);
         } else {
            itemsTable = $(ref)
               .DataTable({
                  data: data,
                  columns: cols,
                  //rowCallback: PalletsCallback,
                  order: [],
                  scrollY: "550px",
                  scrollX: true,
                  scrollCollapse: true,
                  paging: false,
                  dom: 'lftBipr',
                  buttons: ['copy',
                     'print',
                     { extend: 'excel', exportOptions: { orthogonal: 'exportExcel' } },
                     'pdf']
               });
            setTimeout(function () { itemsTable.draw(); }, 0);
         }
         Global.busyRefresh(false);
      }

/************************************************************************************************************************************/ 
      function
      updateHistory() {
         DbFactory.post({
            topic: "cartons",
            action: "getHistory",
            params: { id: $scope.cartonInfo.cartonSeq } 
         })
         .success(buildHistoryTable)
         .error((err) => { console.log(err) });
      }

      
      // history table

      var historyTable = null;

      function
      buildHistoryTable(data) {
         var ref = "#historyTable";
         var cols = [];

         cols.push({
            title: "Time",
            data: "stamp",
            class: "dt-center",
            render: dateRender
         });

         cols.push({
            title: "Message",
            data: "message",
            class: "dt-center"
         });


         if(historyTable) {
            historyTable.clear();
            historyTable.rows.add(data);
            historyTable.draw(false);
         } else {
            historyTable = $(ref)
               .DataTable({
                  data: data, 
                  columns: cols,
                  order: [],
                  scrollY: "550px",
                  scrollX: true,
                  scrollCollapse: true,
                  paging: false,
                  dom: 'lftBipr',
                  buttons: ['copy',
                            'print',
                            {extend:'excel',exportOptions:{orthogonal:'exportExcel'}},
                            'pdf']
               });
            setTimeout(function(){historyTable.draw();},0);
         }
         Global.busyRefresh(false);  
      }


      function
         infoError(err) {
		 Global.busyRefresh(false);
            console.log("info error")
         console.log(err);
      }


      // // // // //
      // TABLES


/************************************************************************************************************************************/ 
      var allCartonsTable = null;

      function
         buildallCartonsTable(data) {
         var cols = [];
         var ref = "#allCartonsTable";

         cols.push({ title: "Carton ID", data: "lpn", class: "dt-center" });
         cols.push({ title: "Shipment ID", data: "orderId", class: "dt-center" });
         cols.push({ title: "Dimension", data: "dimensions", class: "dt-center" });
         cols.push({ title: "Stamp", data: "stamp", render: dateRender, class: "dt-center" });

         if (allCartonsTable) {
            allCartonsTable.clear();
            allCartonsTable.rows.add(data);
            allCartonsTable.draw(false);
         } else {
            allCartonsTable = $(ref)
               .DataTable({
                  data: data,
                  columns: cols,
                  scrollY: "550px",
                  scrollX: true,
                  scrollCollapse: true,
                  paging: true,
                  pageLength: 50,
                  dom: 'lftipr'
               });
            $(ref + ' tbody').on('click', 'tr', allCartonsClick);
            $timeout(allCartonsTable.draw, 0);
         }
         Global.busyRefresh(false);

      }


      function
         allCartonsClick() {
         //console.log("clicked");

         var data = allCartonsTable.row(this).data();
         if (data) {
            $scope.carton.lpn = data.lpn;
            $scope.carton.orderId = data.orderId;
            $scope.carton.dimension = data.dimesions;
            $scope.carton.stamp = data.stamp;
            $scope.selected = 1;
            refresh();
         }

      }

      // // //


      // // //
      // SLAM

      function
         allCartonsUpdate() {
         DbFactory.post({
            topic: 'cartons',
            action: 'allCartons'
         })
            .success(allCartonsSuccess)
            .error(allCartonsError);
      }

      function
         allCartonsSuccess(cartons) {
         buildallCartonsTable(cartons);
         refresh();
      }

      function
         allCartonsError(err) {
         console.error(err);
         refresh();
      }

      function
      detailsRefresh() {
		 if( $scope.cartonSeq && $scope.cartonSeq> 0 )
			 lookupBySeq();
		  
		 /*
         $scope.cartonLpn = $scope.carton.lpn;

         if($scope.outsideLPN != "") {

            $scope.cartonLpn = $scope.outsideLPN;
            $scope.carton.lpn = $scope.cartonLpn;
            $scope.outsideLPN = "";
         }



         lookupByLpn();
		 */
      }

      // // // // //
      // SETUP AND ALL THAT

      function
         refresh() {
            if($scope.selected == 0){
               detailsRefresh();
            }
            
      }

      function
         init() {
         Global.setTitle('Cartons');
         Global.recv('refresh', refresh, $scope);
         if($routeParams.cartonSeq) {
            $scope.selected = 0;
            $scope.cartonSeq = $routeParams.cartonSeq;
            lookupBySeq();
         }


      }

      init();

   }

   function
      cartonsConfig($routeProvider) {
      $routeProvider
         .when('/cartons', {
            controller: 'CartonsController',
            templateUrl: '/app/cartons/cartons.view.html'
         });
   }

}())
