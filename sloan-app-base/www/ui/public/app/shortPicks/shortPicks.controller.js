(function () {
	angular
		.module('ui')
		.controller('ShortPicksController',shortPicksController);

	angular
		.module('ui')
		.config(shortPicksConfig);

	shortPicksController.$inject = ['$scope', '$interval', '$timeout',
		'$mdDialog', '$routeParams',
		'Global', 'DbFactory','_'
	];

	function shortPicksController($scope, $interval, $timeout, $mdDialog, $routeParams,
                  Global, DbFactory,_) {

		$scope.refresh = refresh;
		$scope.permit = Global.permit;
		$scope.selected = 0;

		Date.prototype.stdTimezoneOffset = function () {
			var jan = new Date(this.getFullYear(), 0, 1);
			var jul = new Date(this.getFullYear(), 6, 1);
			return Math.max(jan.getTimezoneOffset(), jul.getTimezoneOffset());
		}

		Date.prototype.isDstObserved = function () {
			return this.getTimezoneOffset() < this.stdTimezoneOffset();
		}

		function checkMarkRender(data,type,full,meta){
			if(data==null)
				return '';
			if(type!=='display')
				return data;
			return data >0 ? '&check;' : '';
		}

		function dateRender(data,type,full,meta) {
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

		function executeError(err) {
			console.log(err);
			Global.busyRefresh(false);
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

		function insertStatus(statusType, data) {
			const user = Global.getUser().user;
			DbFactory.post({
				topic: 'shortPicks',
				action: 'insertStatus',
				params: {
					statusType : statusType,
					data : JSON.stringify(data),
					appName : 'statusApp',
					operator : user
				}
			}).error(function (err) {console.log(statusType, err)});
		}

/************************************************************************************************************************************/
        //Tab0 Unassigned short picks

		var UnassignedShortPicksAll = [];
		$scope.UnassignedShortPicksSelectAll = UnassignedShortPicksSelectAll;
		$scope.UnassignedShortPicksSelectNone = UnassignedShortPicksSelectNone;
		$scope.UnassignedShortPicksSelectShown = UnassignedShortPicksSelectShown;
		$scope.UnassignedShortPicksMarkPickedMass = UnassignedShortPicksMarkPickedMass;
		$scope.UnassignedShortPicksShipShortMass = UnassignedShortPicksShipShortMass;
		$scope.UnassignedShortPicksAssignPickerMass = UnassignedShortPicksAssignPickerMass;

		$scope.UnassignedSkuList = false;
		$scope.UnassignedSkuListButton = 'Show Chasepick List';
		$scope.UnassignedSkuListSwitch = UnassignedSkuListSwitch;

		$scope.UnassignedShortPicksClearFilters = UnassignedShortPicksClearFilters;
		$scope.UnassignedShortPicksShowFilters = true;
		$scope.UnassignedShortPicksFiltered = 0 ;
		$scope.UnassignedShortPicksTotal = 0 ;
		$scope.UnassignedShortPicksSelected = 0 ;
		$scope.UnassignedShortPicksOrderIdFilter = '';
		$scope.UnassignedShortPicksSKUFilter = '';
		$scope.UnassignedShortPicksPickTypeFilter = 'All';
		$scope.UnassignedShortPicksDailyWaveFilter = 'All';
		$scope.massAssignPicks = [];

		$scope.updateAutoAssign = updateAutoAssign;

		function updateAutoAssign() {
          DbFactory.post({topic: 'shortPicks',
                          action: 'updateAutoAssign',
                          params: {value: $scope.autoAssign?'yes':'no'}
                         })
            .success(updateAutoAssignSuccess)
            .error(executeError);
		}

		function updateAutoAssignSuccess(){
		  console.log("updateAutoAssignSuccess");
		}


		function UnassignedShortPicksSelectAll() {
		  for(var i=0 ; i<UnassignedShortPicksAll.length ; i++)
			UnassignedShortPicksAll[i].selected = true ;
		  buildUnassignedShortPicksTable(UnassignedShortPicksFilter());
		}

		function UnassignedShortPicksSelectNone() {
		  for(var i=0 ; i<UnassignedShortPicksAll.length ; i++)
			UnassignedShortPicksAll[i].selected = false ;
		  buildUnassignedShortPicksTable(UnassignedShortPicksFilter());
		}

		function UnassignedShortPicksSelectShown() {
		  for (var i = 0; i < UnassignedShortPicksAll.length; i++)
			if (UnassignedShortPicksAll[i].shown)
			  UnassignedShortPicksAll[i].selected = true;
		  buildUnassignedShortPicksTable(UnassignedShortPicksFilter());
		}

		function UnassignedShortPicksClearFilters() {
		  $scope.UnassignedShortPicksOrderIdFilter = '';
		  $scope.UnassignedShortPicksSKUFilter = '';
		  $scope.UnassignedShortPicksPickTypeFilter = 'All';
		  $scope.UnassignedShortPicksDailyWaveFilter = 'All';
		  refresh();
		}

		function UnassignedSkuListSwitch(){
		  if(!$scope.UnassignedSkuList){
			$scope.UnassignedSkuList = true;
			$scope.UnassignedSkuListButton = 'Hide Chasepick List';
			refresh();
		  } else {
			$scope.UnassignedSkuList = false;
			$scope.UnassignedSkuListButton = 'Show Chasepick List';
			refresh();
		  }
		}

		function UnassignedShortPicksFilter() {
		  var filtered = [] ;
		  $scope.UnassignedShortPicksTotal = 0 ;
		  $scope.UnassignedShortPicksSelected = 0 ;
		  $scope.UnassignedShortPicksFiltered = 0 ;
		  for(var i=0 ; i<UnassignedShortPicksAll.length ; i++)
		  {
			var include = true;
			UnassignedShortPicksAll[i].shown = false;
			if( $scope.UnassignedShortPicksOrderIdFilter!='' && !UnassignedShortPicksAll[i].orderId.includes($scope.UnassignedShortPicksOrderIdFilter) )
			  include = false;
			if( $scope.UnassignedShortPicksSKUFilter!='' && $scope.UnassignedShortPicksSKUFilter.toUpperCase() !=UnassignedShortPicksAll[i].sku )
			  include = false;
			if( $scope.UnassignedShortPicksPickTypeFilter!='All' && !$scope.UnassignedShortPicksPickTypeFilter.includes(UnassignedShortPicksAll[i].pickType) )
			  include = false;
			if( $scope.UnassignedShortPicksDailyWaveFilter!='All' && $scope.UnassignedShortPicksDailyWaveFilter !=UnassignedShortPicksAll[i].dailyWaveSeq )
			  include = false;
			if (include) {
			  UnassignedShortPicksAll[i].shown = true;
			  filtered.push(UnassignedShortPicksAll[i]);
			  $scope.UnassignedShortPicksFiltered++;
			}
			if (UnassignedShortPicksAll[i].selected)
			  $scope.UnassignedShortPicksSelected++;
			$scope.UnassignedShortPicksTotal++;
		  }
		  return filtered;
		}

		function UnassignedShortPicksMarkPickedMass(ev){
		  var count = 0 ;
		  for(var s = 0 ; s<UnassignedShortPicksAll.length ; s++)
			if(UnassignedShortPicksAll[s].selected)
			  count++ ;

			const dialog
				= $mdDialog.confirm()
				.title("Mark picked")
				.textContent("Are you sure you want to mark " + count + " picks as picked?")
				.ariaLabel('Mark picked')
				.targetEvent(ev)
				.ok('Mark picked')
				.cancel('Cancel');

			$mdDialog
			 .show(dialog)
			 .then(function()
			{
			  for(var i=0 ; i<UnassignedShortPicksAll.length ; i++)
			  {
				if(UnassignedShortPicksAll[i].selected && UnassignedShortPicksAll[i].uom==='CA')
				{
				  const data = {
					  pickSeq: UnassignedShortPicksAll[i].pickSeq
				  };
				  console.log('mark picked' + data.pickSeq);
				  insertStatus('dashboardPick', data)
				}
			  }
			  $scope.working = true ;
			  $timeout(function()
			  {
				 refresh() ;
				 $scope.working=false ;
			   },2000 * (1 + count/50) ) ;
			}) ;
		}

		function UnassignedShortPicksShipShortMass(ev){
		  var count = 0 ;
		  for(var s = 0 ; s<UnassignedShortPicksAll.length ; s++)
			if(UnassignedShortPicksAll[s].selected)
			  count++ ;

		  var dialog
		  = $mdDialog.confirm()
			 .title("Ship short")
			 .textContent("Are you sure you want to ship short "+count+" picks?")
			 .ariaLabel('Ship short')
			 .targetEvent(ev)
			 .ok('Ship short')
			 .cancel('Cancel') ;

		  $mdDialog
			 .show(dialog)
			 .then(function()
			{
			  for(var i=0 ; i<UnassignedShortPicksAll.length ; i++)
			  {
				if(UnassignedShortPicksAll[i].selected )
				{
				  const data = {
					  pickSeq: UnassignedShortPicksAll[i].pickSeq
				  }
				  console.log('ship short' + data.pickSeq);
				  insertStatus('shipShort', data)
				}
			  }
			  $scope.working = true ;
			  $timeout(function()
			  {
				 refresh() ;
				 $scope.working=false ;
			   },2000 * (1 + count/50) ) ;
			}) ;

		}

		function UnassignedShortPicksAssignPickerMass(ev){
		  var count = 0 ;
		  for(var s = 0 ; s<UnassignedShortPicksAll.length ; s++)
			if(UnassignedShortPicksAll[s].selected)
			  count++ ;
		  $scope.massAssignPicks = UnassignedShortPicksAll;

			$mdDialog.show({
				templateUrl: 'massAssign.html',
				clickOutsideToClose: false,
				scope: $scope,
				preserveScope: true,
				controller: function($scope) {},
				parent: angular.element(document.body),
			});
			$timeout(function() {
				cancelMassAssign();
			},60000) ;
		}

	    $scope.doMassAssignChasePick = doMassAssignChasePick;
		$scope.doMassAssignChasePickForSku = doMassAssignChasePickForSku;
	    $scope.cancelMassAssign = cancelMassAssign;
		$scope.SearchPicker = '';
		$scope.clearSearchPicker = clearSearchPicker;

		function clearSearchPicker(){
			$scope.SearchPicker = '';
		}

		function cancelMassAssign(){
		  $mdDialog.cancel();
		}

		function doMassAssignChasePick(){
			var count = 0 ;
			for(var s = 0 ; s<$scope.massAssignPicks.length ; s++)
			if($scope.massAssignPicks[s].selected)
			  count++ ;
			for(var i=0 ; i<$scope.massAssignPicks.length ; i++)
			  {
				if($scope.massAssignPicks[i].selected )
				{
				  const data = {
					  pickSeq: $scope.massAssignPicks[i].pickSeq,
					  chasePickOperatorId: $scope.chasePickOperator
				  }
					insertStatus('assignChasePick', data)
				}
			  }
			$mdDialog.hide();
			$scope.working = true ;
			$timeout(function()
			  {
				 refresh() ;
				 $scope.working=false ;
			   },2000 * (1 + count/50) ) ;

		}

		function doMassAssignChasePickForSku(){
			var count = 0 ;
			for(var s = 0 ; s<$scope.massAssignPicks.length ; s++)
				if($scope.massAssignPicks[s].selected)
					count++ ;
			var user = Global.getUser().user;
			for(var i=0 ; i<$scope.massAssignPicks.length ; i++)
			{
				if($scope.massAssignPicks[i].selected )
				{
					const data = {
						sku: $scope.massAssignPicks[i].sku,
						pickType: $scope.massAssignPicks[i].pickType,
						chasePickOperatorId: $scope.chasePickOperator
					}
					insertStatus('assignChasePickForSku', data)
				}
			}
			$mdDialog.hide();
			$scope.working = true ;
			$timeout(function()
			{
				refresh() ;
				$scope.working=false ;
			},2000 * (1 + count/50) ) ;

		}

		var UnassignedShortPicksTable = null;

		function buildUnassignedShortPicksTable(data) {
			  var cols = [];
			  var ref = "#UnassignedShortPicksTable";

			  cols.push({title: "Selected", 		data:"selected", 		class:"dt-center"}) ;
			  cols.push({ title: "SKU", class: "dt-center", data: "sku"});
			  cols.push({ title: "Desc", class: "dt-center", data: "description"});
			  cols.push({ title: "Barcode", class: "dt-center", data: "barcode"});
			  cols.push({ title: "Type", class: "dt-center", data: "pickType"});
			  cols.push({ title: "Invoice", class: "dt-center", data: "orderId"});
			  cols.push({ title: "Wave", class: "dt-center", data: "dailyWaveSeq"});
			  cols.push({ title: "Demand Date", class: "dt-center", data: "demandDate"});
			  cols.push({ title: "LPN #1", class: "dt-center", data: "lpn"});
			  cols.push({ title: "LPN #2", class: "dt-center", data: "trackingNumber"});
			  cols.push({ title: "Last Seen", class: "dt-center", data: "lastPositionLogical"});
			  cols.push({title: "Assign Chase Picker", class: "dt-center", data: {}, render: (data) =>
			  { return buttonRender(ref, data, 'shortPicksEdit','Assign Chase Picker', data.pickSeq+'assignChasePick', messageAssignChasePicker, true) }});

			  if (UnassignedShortPicksTable) {
				   UnassignedShortPicksTable.clear();
				   UnassignedShortPicksTable.rows.add(data);
				   UnassignedShortPicksTable.draw(false);
			  } else {
				   UnassignedShortPicksTable = $(ref)
						  .DataTable({
								data: data,
								rowCallback: UnassignedShortPicksCallback(),
								columns: cols,
								order: [],
								scrollY: "550px",
								scrollX: true,
								scrollCollapse: true,
								paging: true,
								pageLength: 50,
								dom: 'lftBipr',
                              	buttons: ['copy','print','excel','pdf']
						  });
					$(ref+' tbody').on('click','td',UnassignedShortPicksTableClick);
					$timeout(UnassignedShortPicksTable.draw, 0);
			  }
			  Global.busyRefresh(false);
		}

		function UnassignedShortPicksCallback() {
		  return (function(row,data,index)
		  {
			if(data.selected) {
			  $(row).css('background-color','#e0ffe0') ;
			  $("td:eq(0)",row).html("&#10004") ;
			} else {
			  $(row).css('background-color','') ;
			  $("td:eq(0)",row).html("") ;
			}
		  });
		}

		function UnassignedShortPicksTableClick() {
		  var data = UnassignedShortPicksTable.row(this).data();
		  var idx = UnassignedShortPicksTable.cell(this).index().column;
		  if( data.pickSeq ){
			if(idx==0){
			  data.selected = !data.selected ;
			  UnassignedShortPicksTable.row(this).data(data).draw(false) ;
			  $scope.UnassignedShortPicksSelected = 0;
			  for(var i=0 ; i<UnassignedShortPicksAll.length ; i++) {
				if( (UnassignedShortPicksAll[i].pickSeq==data.pickSeq) )
				  UnassignedShortPicksAll[i].selected = data.selected ;
				if(UnassignedShortPicksAll[i].selected)
				  $scope.UnassignedShortPicksSelected++;
			  }
			  $scope.$apply() ;
			}
		  }
		}

		function messageShortShip(data) {
			  $scope.pickSeq = data.pickSeq;
			  $scope.sku = data.sku;
			  $scope.location = data.location;
			  $scope.uom = data.uom;

			  let item = '[' + $scope.sku +'/' +  $scope.uom + ']';

			  var dialog = $mdDialog.confirm()
					.title('Ship Short')
					.textContent('Are you sure you want to ship short: ' + item +'?')
					.ok('Ship Short')
					.cancel('Cancel');
			  $mdDialog
					.show(dialog)
					.then(shortShipRecord);
		}

		function shortShipRecord(){
			  Global.busyRefresh(true);
			  const data = {
				  pickSeq: $scope.pickSeq
			  };
			  insertStatus('shipShort', data)
			  $timeout(function() {
				 refresh();
			  },2000) ;
		}

		function messageShortShipCarton(data) {
			$scope.cartonSeq = data.cartonSeq;
			const dialog = $mdDialog.confirm()
				.title('Ship Short')
				.textContent('Are you sure you want to ship short all chase and open picks in: ' + data.lpn +'?')
				.ok('Ship Short')
				.cancel('Cancel');
			$mdDialog
				.show(dialog)
				.then(shortShipCarton);
		}

		function shortShipCarton(){
			Global.busyRefresh(true);
			const data = {
				cartonSeq: $scope.cartonSeq
			};
			insertStatus('shipShortCarton', data)
			$timeout(function() {
				refresh();
			},2000) ;
		}

		function messageAssignChasePicker(data) {
			$scope.pickSeq = data.pickSeq;
			$mdDialog.show({
				templateUrl: 'pickerAssign.html',
				clickOutsideToClose: false,
				scope: $scope,
				preserveScope: true,
				controller: function($scope) {},
				parent: angular.element(document.body),
			});
			$timeout(function() {
				cancelAssign();
			},60000) ;
		}

		function messageAssignChasePickerChasePickListVersion(data) {
			$scope.sku = data.sku;
			$scope.pickType = data.pickType;
			$mdDialog.show({
				templateUrl: 'pickerAssignForSku.html',
				clickOutsideToClose: false,
				scope: $scope,
				preserveScope: true,
				controller: function($scope) {},
				parent: angular.element(document.body),
			});
			$timeout(function() {
				cancelAssign();
			},60000) ;
		}

	    $scope.doAssignChasePick = doAssignChasePick;
		$scope.doAssignChasePickForSku = doAssignChasePickForSku;
	    $scope.cancelAssign = cancelAssign;

		function cancelAssign(){
		  $mdDialog.cancel();
		}

		function doAssignChasePick(){
			  Global.busyRefresh(true);
			  const data = {
				pickSeq: $scope.pickSeq,
				chasePickOperatorId: $scope.chasePickOperator
			  };
			  insertStatus('assignChasePick', data)
			  $mdDialog.hide();
			  $scope.working = true ;
			  $timeout(function() {
				 $scope.working=false ;
				 refresh();
			  },2000) ;
		}

		function doAssignChasePickForSku(){
			Global.busyRefresh(true);
			const data = {
				sku: $scope.sku,
				pickType: $scope.pickType,
				chasePickOperatorId: $scope.chasePickOperator
			};
			insertStatus('assignChasePickForSku', data)
			$mdDialog.hide();
			$scope.working = true ;
			$timeout(function() {
				$scope.working=false ;
				refresh();
			},2000) ;
		}

		function
			  refreshUnassignedShortPicks() {
			  Global.busyRefresh(true);
			  DbFactory.post({
					topic: 'shortPicks',
					action: 'getUnassignedShortPicks'
			  })
					.success(UnassignedShortPicksSuccess)
					.error(executeError);
		}

		function UnassignedShortPicksSuccess(data) {
			  UnassignedShortPicksAll = data ;
			  for(var i=0 ; i<UnassignedShortPicksAll.length ; i++){
				UnassignedShortPicksAll[i].selected=false ;
			  }
			  buildUnassignedShortPicksTable(UnassignedShortPicksFilter());
		}

		function getOperatorList(){
			  Global.busyRefresh(true);
			  DbFactory.post({
					topic: 'shortPicks',
					action: 'getOperatorList'
			  })
					.success(getOperatorListSuccess)
					.error(executeError);
		}

		function getOperatorListSuccess(data){
			$scope.chasePickerList = _.uniq(_.map(data,'operatorId')) ;
			Global.busyRefresh(false);
		}

////////////////////////////////////////////////////////////////////////////////////////
/////////Unassigned Sku List

	    var UnassignedSkuListsAll = [];
		$scope.UnassignedSkuListClearFilters = UnassignedSkuListClearFilters;
		$scope.UnassignedSkuListSelectShown = UnassignedSkuListSelectShown;
		$scope.UnassignedSkuListSelectNone = UnassignedSkuListSelectNone;
		$scope.UnassignedSkuListAssignPickerMass = UnassignedSkuListAssignPickerMass;
		$scope.UnassignedSkuListTotal = 0 ;
		$scope.UnassignedSkuListSelected = 0 ;
		$scope.UnassignedSkuListFiltered = 0 ;
		$scope.UnassignedSkuListSKUFilter = '';
		$scope.UnassignedSkuListPickTypeFilter = 'All';
        $scope.UnassignedDailyWaveFilter = 'All';
        $scope.unassignedWaveSeqs = [];

		function UnassignedSkuListClearFilters() {
		  $scope.UnassignedSkuListSKUFilter = '';
		  $scope.UnassignedSkuListPickTypeFilter = 'All';
          $scope.UnassignedDailyWaveFilter = 'All';
		  refresh();
		}

		function UnassignedSkuListSelectNone() {
			for(var i=0 ; i<UnassignedSkuListsAll.length ; i++)
				UnassignedSkuListsAll[i].selected = false ;
			buildUnassignedSkuListTable(UnassignedSkuListFilter());
		}

		function UnassignedSkuListSelectShown() {
			for (var i = 0; i < UnassignedSkuListsAll.length; i++)
				if (UnassignedSkuListsAll[i].shown)
					UnassignedSkuListsAll[i].selected = true;
			buildUnassignedSkuListTable(UnassignedSkuListFilter());
		}

		function UnassignedSkuListFilter() {
		  var filtered = [] ;
		  $scope.UnassignedSkuListTotal = 0 ;
		  $scope.UnassignedSkuListSelected = 0 ;
		  $scope.UnassignedSkuListFiltered = 0 ;
		  for(var i=0 ; i<UnassignedSkuListsAll.length ; i++)
		  {
			var include = true;
			UnassignedSkuListsAll[i].shown = false;
			if( $scope.UnassignedSkuListSKUFilter!=='' && $scope.UnassignedSkuListSKUFilter.toUpperCase() !== UnassignedSkuListsAll[i].sku )
			  include = false;
			if( $scope.UnassignedSkuListPickTypeFilter!=='All' && !$scope.UnassignedSkuListPickTypeFilter.includes(UnassignedSkuListsAll[i].pickType) )
			  include = false;
			if( $scope.UnassignedDailyWaveFilter!=='All' && $scope.UnassignedDailyWaveFilter !== UnassignedSkuListsAll[i].dailyWaveSeq )
			  include = false;
			if (include) {
			  UnassignedSkuListsAll[i].shown = true;
			  filtered.push(UnassignedSkuListsAll[i]);
			  $scope.UnassignedSkuListFiltered++;
			}
		    if (UnassignedSkuListsAll[i].selected)
			  $scope.UnassignedSkuListSelected++;
		    $scope.UnassignedSkuListTotal++;
		  }
		  return filtered;
		}

		function UnassignedSkuListAssignPickerMass(ev){
			var count = 0 ;
			for(var s = 0 ; s<UnassignedSkuListsAll.length ; s++)
				if(UnassignedSkuListsAll[s].selected)
					count++;
			var user = Global.getUser().user ;
			$scope.massAssignPicks = UnassignedSkuListsAll;

			$mdDialog.show({
				templateUrl: 'massAssignForSku.html',
				clickOutsideToClose: false,
				scope: $scope,
				preserveScope: true,
				controller: function($scope) {},
				parent: angular.element(document.body),
			});
			$timeout(function() {
				cancelMassAssign();
			},60000) ;

			var dialog
				= $mdDialog.confirm()
				.title("Ship short")
				.textContent("Are you sure you want to ship short "+count+" skus?")
				.ariaLabel('Ship short')
				.targetEvent(ev)
				.ok('Ship short')
				.cancel('Cancel') ;
		}

		function
			  refreshUnassignedSkuList() {
			  Global.busyRefresh(true);
			  DbFactory.post({
					topic: 'shortPicks',
					action: 'getUnassignedSkuList'
			  })
					.success(UnassignedSkuListSuccess)
					.error(executeError);
		}

		function UnassignedSkuListSuccess(data) {
			  UnassignedSkuListsAll = data ;
			  $scope.unassignedWaveSeqs = _.uniq(_.map(data,'dailyWaveSeq')) ;
			  for(var i=0 ; i<UnassignedSkuListsAll.length ; i++){
				  UnassignedSkuListsAll[i].selected=false ;
			  }
			  buildUnassignedSkuListTable(UnassignedSkuListFilter());
		}

		var UnassignedSkuListTable = null;

		function buildUnassignedSkuListTable(data) {
			  var cols = [];
			  var ref = "#UnassignedSkuListTable";

			  cols.push({ title: "Selected", 		data:"selected", 		class:"dt-center"}) ;
              cols.push({ title: "Wave", class: "dt-center", data: "dailyWaveSeq"});
			  cols.push({ title: "SKU", class: "dt-center", data: "sku"});
			  cols.push({ title: "Desc", class: "dt-center", data: "description"});
			  cols.push({ title: "Barcode", class: "dt-center", data: "barcode"});
			  cols.push({ title: "Type", class: "dt-center", data: "pickType"});
	          cols.push({ title: "Required", class: "dt-center", data: "requiredQty"});
			  cols.push({ title: "Assign Chase Picker", class: "dt-center", data: {}, render: (data) =>
				{ return buttonRender(ref, data, 'shortPicksEdit','Assign Chase Picker', data.pickSeq+'assignChasePick', messageAssignChasePickerChasePickListVersion, true) }});

			  if (UnassignedSkuListTable) {
				   UnassignedSkuListTable.clear();
				   UnassignedSkuListTable.rows.add(data);
				   UnassignedSkuListTable.draw(false);
			  } else {
				   UnassignedSkuListTable = $(ref)
						  .DataTable({
								data: data,
								columns: cols,
							    rowCallback: UnassignedSkuListCallback(),
								order: [],
								scrollY: "550px",
								scrollX: true,
								scrollCollapse: true,
								paging: true,
								pageLength: 50,
								dom: 'lftBipr',
                              	buttons: ['copy','print','excel','pdf']
						  });
				  $(ref+' tbody').on('click','td',UnassignedSkuListTableClick);
				  $timeout(UnassignedSkuListTable.draw, 0);
			  }
			  Global.busyRefresh(false);
		}

		function UnassignedSkuListCallback() {
			return (function(row,data,index)
			{
				if(data.selected) {
					$(row).css('background-color','#e0ffe0') ;
					$("td:eq(0)",row).html("&#10004") ;
				} else {
					$(row).css('background-color','') ;
					$("td:eq(0)",row).html("") ;
				}
			});
		}

		function UnassignedSkuListTableClick() {
			var data = UnassignedSkuListTable.row(this).data();
			var idx = UnassignedSkuListTable.cell(this).index().column;
			if( data.sku ){
				if(idx==0){
					data.selected = !data.selected ;
					UnassignedSkuListTable.row(this).data(data).draw(false) ;
					$scope.UnassignedSkuListSelected = 0;
					for(var i=0 ; i<UnassignedSkuListsAll.length ; i++) {
						if( (UnassignedSkuListsAll[i].sku==data.sku) )
							UnassignedSkuListsAll[i].selected = data.selected ;
						if(UnassignedSkuListsAll[i].selected)
							$scope.UnassignedSkuListSelected++;
					}
					$scope.$apply() ;
				}
			}
		}

		function getAutoAssignValue(){
		  DbFactory.post({topic: 'shortPicks',
						  action: 'getAutoAssignValue'
						 })
			.success(getAutoAssignValueSuccess)
			.error  (executeError);

		}

		function getAutoAssignValueSuccess(data){
		  for( let i = 0 ; i< data.length; i++ ){
			if( data[i]['name'] == 'autoAssignChasePicker' ) $scope.autoAssign = (data[i]['value'] == 'yes');
		  }
		}


		$scope.refreshTab0 = refreshTab0;

		function refreshTab0(){
			console.log("refresh tab0");
			refreshUnassignedSkuList();
			refreshUnassignedShortPicks();
			getOperatorList();
			getAutoAssignValue();
		}

////////////////////////////////////////////////////////////////////////////////////////
////////Tab1 Assigned Short Picks		

		var AssignedShortPicksAll = [];
		$scope.AssignedShortPicksSelectAll = AssignedShortPicksSelectAll;
		$scope.AssignedShortPicksSelectNone = AssignedShortPicksSelectNone;
		$scope.AssignedShortPicksSelectShown = AssignedShortPicksSelectShown;
		$scope.AssignedShortPicksMarkPickedMass = AssignedShortPicksMarkPickedMass;
		$scope.AssignedShortPicksShipShortMass = AssignedShortPicksShipShortMass;

		$scope.AssignedSkuList = false;
		$scope.AssignedSkuListButton = 'Show Chasepick List';
		$scope.AssignedSkuListSwitch = AssignedSkuListSwitch;


		$scope.AssignedShortPicksClearFilters = AssignedShortPicksClearFilters;
		$scope.AssignedShortPicksShowFilters = true;
		$scope.AssignedShortPicksFiltered = 0 ;
		$scope.AssignedShortPicksTotal = 0 ;
		$scope.AssignedShortPicksSelected = 0 ;
		$scope.AssignedShortPicksOrderIdFilter = '';
		$scope.AssignedShortPicksPickTypeFilter = 'All';
		$scope.AssignedShortPicksDailyWaveFilter = 'All';
		$scope.AssignedShortPicksSKUFilter = '';
		//$scope.AssignedShortPicksChaseOperatorIdFilter = Global.getUser().user;
		$scope.AssignedShortPicksChaseOperatorIdFilter = '';


		function AssignedShortPicksSelectAll() {
		  for(var i=0 ; i<AssignedShortPicksAll.length ; i++)
			AssignedShortPicksAll[i].selected = true ;
		  buildAssignedShortPicksTable(AssignedShortPicksFilter());
		}

		function AssignedShortPicksSelectNone() {
		  for(var i=0 ; i<AssignedShortPicksAll.length ; i++)
			AssignedShortPicksAll[i].selected = false ;
		  buildAssignedShortPicksTable(AssignedShortPicksFilter());
		}

		function AssignedShortPicksSelectShown() {
		  for (var i = 0; i < AssignedShortPicksAll.length; i++)
			if (AssignedShortPicksAll[i].shown)
			  AssignedShortPicksAll[i].selected = true;
		  buildAssignedShortPicksTable(AssignedShortPicksFilter());
		}

		function AssignedShortPicksClearFilters() {
		  $scope.AssignedShortPicksOrderIdFilter = '';
		  $scope.AssignedShortPicksSKUFilter = '';
		  $scope.AssignedShortPicksPickTypeFilter = 'All';
		  $scope.AssignedShortPicksDailyWaveFilter = 'All';
		  //$scope.AssignedShortPicksChaseOperatorIdFilter = Global.getUser().user;
		  $scope.AssignedShortPicksChaseOperatorIdFilter = '';
		  refresh();
		}

		function AssignedSkuListSwitch(){
		  if(!$scope.AssignedSkuList){
			$scope.AssignedSkuList = true;
			$scope.AssignedSkuListButton = 'Hide Chasepick List';
			refresh();
		  } else {
			$scope.AssignedSkuList = false;
			$scope.AssignedSkuListButton = 'Show Chasepick List';
			refresh();
		  }
		}

		function AssignedShortPicksFilter() {
		  var filtered = [] ;
		  $scope.AssignedShortPicksTotal = 0 ;
		  $scope.AssignedShortPicksSelected = 0 ;
		  $scope.AssignedShortPicksFiltered = 0 ;
		  for(var i=0 ; i<AssignedShortPicksAll.length ; i++)
		  {
			var include = true;
			AssignedShortPicksAll[i].shown = false;
			if( $scope.AssignedShortPicksOrderIdFilter!='' && !AssignedShortPicksAll[i].orderId.includes($scope.AssignedShortPicksOrderIdFilter) )
			  include = false;
			if( $scope.AssignedShortPicksSKUFilter!='' && $scope.AssignedShortPicksSKUFilter.toUpperCase() !=AssignedShortPicksAll[i].sku )
			  include = false;
			if( $scope.AssignedShortPicksPickTypeFilter!='All' && !$scope.AssignedShortPicksPickTypeFilter.includes(AssignedShortPicksAll[i].pickType) )
			  include = false;
			if( $scope.AssignedShortPicksChaseOperatorIdFilter!='' && $scope.AssignedShortPicksChaseOperatorIdFilter != AssignedShortPicksAll[i].chasePickOperatorId )
			  include = false;
			if( $scope.AssignedShortPicksDailyWaveFilter!='All' && $scope.AssignedShortPicksDailyWaveFilter !=AssignedShortPicksAll[i].dailyWaveSeq )
			  include = false;
			if (include) {
			  AssignedShortPicksAll[i].shown = true;
			  filtered.push(AssignedShortPicksAll[i]);
			  $scope.AssignedShortPicksFiltered++;
			}
			if (AssignedShortPicksAll[i].selected)
			  $scope.AssignedShortPicksSelected++;
			$scope.AssignedShortPicksTotal++;
		  }
		  return filtered;
		}

		function AssignedShortPicksMarkPickedMass(ev){
		  var count = 0 ;
		  for(var s = 0 ; s<AssignedShortPicksAll.length ; s++)
			if(AssignedShortPicksAll[s].selected)
			  count++ ;

		  var dialog
		  = $mdDialog.confirm()
			 .title("Mark picked")
			 .textContent("Are you sure you want to mark "+count+" picks as picked?")
			 .ariaLabel('Mark picked')
			 .targetEvent(ev)
			 .ok('Mark picked')
			 .cancel('Cancel') ;

		  $mdDialog
			 .show(dialog)
			 .then(function()
			{
			  for(var i=0 ; i<AssignedShortPicksAll.length ; i++)
			  {
				if(AssignedShortPicksAll[i].selected)
				{
				  const data = {
					  pickSeq: AssignedShortPicksAll[i].pickSeq
				  };
				  console.log('mark picked' + data.pickSeq);
				  insertStatus('chasePicked', data)
				}
			  }
			  $scope.working = true ;
			  $timeout(function()
			  {
				 refresh() ;
				 $scope.working=false ;
			   },2000 * (1 + count/50) ) ;
			}) ;
		}

		function AssignedShortPicksShipShortMass(ev){
		  var count = 0 ;
		  for(var s = 0 ; s<AssignedShortPicksAll.length ; s++)
			if(AssignedShortPicksAll[s].selected)
			  count++ ;

		  var dialog
		  = $mdDialog.confirm()
			 .title("Ship short")
			 .textContent("Are you sure you want to ship short "+count+" picks?")
			 .ariaLabel('Ship short')
			 .targetEvent(ev)
			 .ok('Ship short')
			 .cancel('Cancel') ;

		  $mdDialog
			 .show(dialog)
			 .then(function()
			{
			  for(var i=0 ; i<AssignedShortPicksAll.length ; i++)
			  {
				if(AssignedShortPicksAll[i].selected )
				{
				  const data = {
					  pickSeq: AssignedShortPicksAll[i].pickSeq
				  };
				  console.log('ship short' + data.pickSeq);
				  insertStatus('shipShort', data)
				}
			  }
			  $scope.working = true ;
			  $timeout(function()
			  {
				 refresh() ;
				 $scope.working=false ;
			   },2000 * (1 + count/50) ) ;
			}) ;

		}

		var AssignedShortPicksTable = null;

		function buildAssignedShortPicksTable(data) {
			  var cols = [];
			  var ref = "#AssignedShortPicksTable";

			  cols.push({title: "Selected", 		data:"selected", 		class:"dt-center"}) ;
			  cols.push({ title: "SKU", class: "dt-center", data: "sku"});
			  cols.push({ title: "UOM", class: "dt-center", data: "uom"});
			  cols.push({ title: "Desc", class: "dt-center", data: "description"});
			  cols.push({ title: "Barcode", class: "dt-center", data: "barcode"});
			  cols.push({ title: "Type", class: "dt-center", data: "pickType"});
			  cols.push({ title: "Invoice", class: "dt-center", data: "orderId"});
			  cols.push({ title: "Wave", class: "dt-center", data: "dailyWaveSeq"});
			  cols.push({ title: "Demand Date", class: "dt-center", data: "demandDate"});
			  cols.push({ title: "LPN #1", class: "dt-center", data: "lpn"});
			  cols.push({ title: "LPN #2", class: "dt-center", data: "trackingNumber"});
			  cols.push({ title: "Last Seen", class: "dt-center", data: "lastPositionLogical"});
			  cols.push({ title: "Chase Picker", class: "dt-center", data: "chasePickOperatorId"});
			  cols.push({title: "Mark Picked", class: "dt-center", data: {}, render: (data) =>
			  { return buttonRender(ref, data, 'shortPicksEdit','Mark Picked', data.pickSeq+'markPicked', messageChasePicked, true) }});
			  cols.push({title: "Not Found", class: "dt-center", data: {}, render: (data) =>
			  { return buttonRender(ref, data, 'shortPicksEdit','Not Found', data.pickSeq+'notFound', messageNotFound, true) }});
			  cols.push({title: "Chase Location", class: "dt-center", data: {},
					render: (data) => {
						const chaseLocation = data.chasePickLocation;
						let displayContent = chaseLocation ? `<span>${chaseLocation}</span>` : '';
						return buttonRender(
							ref, data, 'shortPicksEdit',
							displayContent || 'Select Location',
							data.pickSeq + 'selectLocation',
							() => messageLocation(data, 'picks'),
							true
						);
					}
			  });

			  if (AssignedShortPicksTable) {
				   AssignedShortPicksTable.clear();
				   AssignedShortPicksTable.rows.add(data);
				   AssignedShortPicksTable.draw(false);
			  } else {
				   AssignedShortPicksTable = $(ref)
						  .DataTable({
								data: data,
								rowCallback: AssignedShortPicksCallback(),
								columns: cols,
								order: [],
								scrollY: "550px",
								scrollX: true,
								scrollCollapse: true,
								paging: true,
								pageLength: 50,
								dom: 'lftBipr',
                              	buttons: ['copy','print','excel','pdf']
						  });
					$(ref+' tbody').on('click','td',AssignedShortPicksTableClick);
					$timeout(AssignedShortPicksTable.draw, 0);
			  }
			  Global.busyRefresh(false);
		}

		function AssignedShortPicksCallback() {
		  return (function(row,data,index)
		  {
			if(data.selected) {
			  $(row).css('background-color','#e0ffe0') ;
			  $("td:eq(0)",row).html("&#10004") ;
			} else {
			  $(row).css('background-color','') ;
			  $("td:eq(0)",row).html("") ;
			}
		  });
		}

		function AssignedShortPicksTableClick() {
		  var data = AssignedShortPicksTable.row(this).data();
		  var idx = AssignedShortPicksTable.cell(this).index().column;
		  if( data.pickSeq ){
			if(idx==0){
			  data.selected = !data.selected ;
			  AssignedShortPicksTable.row(this).data(data).draw(false) ;
			  $scope.AssignedShortPicksSelected = 0;
			  for(var i=0 ; i<AssignedShortPicksAll.length ; i++) {
				if( (AssignedShortPicksAll[i].pickSeq==data.pickSeq) )
				  AssignedShortPicksAll[i].selected = data.selected ;
				if(AssignedShortPicksAll[i].selected)
				  $scope.AssignedShortPicksSelected++;
			  }
			  $scope.$apply() ;
			}
		  }
		}

		function messageChasePicked(data) {
			  $scope.pickSeq = data.pickSeq;
			  $scope.sku = data.sku;
			  $scope.location = data.location;
			  $scope.uom = data.uom;

			  let item = '[' + $scope.sku +'/' +  $scope.uom + ']';

			  const dialog = $mdDialog.confirm()
					.title('Mark Picked')
					.textContent('Are you sure you want to mark: 1 ' + item +' as picked outside of RDS?')
					.ok('Mark Picked')
					.cancel('Cancel');
			  $mdDialog
					.show(dialog)
					.then(chasePickedRecord);
		}

		function chasePickedRecord(){
			  Global.busyRefresh(true);
			  const data = {
				  pickSeq: $scope.pickSeq
			  };
			  insertStatus('chasePicked', data)
			  $timeout(function() {
				 refresh();
			  },2000) ;
		}

		function messageChasePickedForSku(data) {
			$scope.sku = data.sku;
			$scope.pickType = data.pickType;
			$scope.chasePickOperatorId = data.chasePickOperatorId;

			const dialog = $mdDialog.confirm()
				.title('Mark Picked')
				.textContent('Are you sure you want to mark: 1 ' + $scope.sku +' as picked outside of RDS?')
				.ok('Mark Picked')
				.cancel('Cancel');
			$mdDialog
				.show(dialog)
				.then(chasePickedSku);
		}

		function chasePickedSku(){
			Global.busyRefresh(true);
			const data = {
				sku: $scope.sku,
				pickType: $scope.pickType,
				chasePickOperatorId: $scope.chasePickOperatorId
			}
			insertStatus('chasePickedForSku', data)
			$timeout(function() {
				refresh();
			},2000) ;
		}

		function messageNotFound(data) {
			$scope.pickSeq = data.pickSeq;
			$scope.sku = data.sku;
			$scope.location = data.location;
			$scope.uom = data.uom;

			const item = '[' + $scope.sku +'/' +  $scope.uom + ']';

			const dialog = $mdDialog.confirm()
				.title('Mark Not Found')
				.textContent('Are you sure you want to mark: 1 ' + item +' as not found?')
				.ok('Mark Not Found')
				.cancel('Cancel');
			$mdDialog
				.show(dialog)
				.then(notFoundRecord);
		}

		function notFoundRecord(){
			Global.busyRefresh(true);
			const data = {
				pickSeq: $scope.pickSeq
			};
			insertStatus('chasePickedNotFound', data)
			$timeout(function() {
				refresh();
			},2000) ;
		}

		function messageNotFoundForSku(data) {
			$scope.sku = data.sku;
			$scope.pickType = data.pickType;
			$scope.chasePickOperatorId = data.chasePickOperatorId;
			const qty = data.requiredQty;

			const dialog = $mdDialog.confirm()
				.title('Mark Not Found')
				.textContent('Are you sure you want to mark: ' + qty + ' ' + $scope.sku +' as not found?')
				.ok('Mark Not Found')
				.cancel('Cancel');
			$mdDialog
				.show(dialog)
				.then(notFoundRecordForSku);
		}

		function notFoundRecordForSku(){
			Global.busyRefresh(true);
			const data = {
				sku: $scope.sku,
				pickType: $scope.pickType,
				chasePickOperatorId: $scope.chasePickOperatorId
			};
			insertStatus('chasePickedNotFoundForSku', data)
			$timeout(function() {
				refresh();
			},2000) ;
		}

		$scope.backStock = '';
		$scope.clearBackStockLocation = clearBackStockLocation;

		function clearBackStockLocation(){
			$scope.backStock = '';
		}

		function messageLocation(data, type) {
			$scope.workingLocation = true;
			$scope.backStockEmpty = false;
			$scope.sku = data.sku;
			$scope.pickSeq = data.pickSeq;
		    $scope.locationType = type;
			$scope.currentChasePickLocation = data.chasePickLocation;
			$scope.chasePickOperatorId = data.chasePickOperatorId;
			DbFactory.post({
				topic: 'shortPicks',
				action: 'getBackStockLocations',
				params: {
					sku: $scope.sku
				}
			})
				.success(BackStockLocationSuccess)
				.error(executeError);
			$mdDialog.show({
				templateUrl: 'selectLocation.html',
				clickOutsideToClose: false,
				scope: $scope,
				preserveScope: true,
				controller: function($scope) {},
				parent: angular.element(document.body),
			});
			$timeout(function() {
				cancelBackStock();
			},30000) ;
		}

		$scope.backStockLocationList = '';

		function BackStockLocationSuccess(data) {
			$scope.workingLocation = false;
			if(data && data.length > 0) {
				$scope.backStockLocationList = _.uniq(_.map(data, 'location'));
			}
			else
				$scope.backStockEmpty = true;
		}

		$scope.cancelBackStock = cancelBackStock;

		function cancelBackStock() {
			$mdDialog.cancel()
		}

		$scope.doBackStockLocation = doBackStockLocation;

		function doBackStockLocation() {
			if ($scope.locationType === 'picks') {
				const data = {
					pickSeq: $scope.pickSeq,
					chasePickLocation: $scope.chasePickLocation
				}
				insertStatus('assignChasePickLocationForRecord', data);
			}
			else if ($scope.locationType === 'sku') {
				const data = {
					sku: $scope.sku,
					chasePickOperatorId: $scope.chasePickOperatorId,
					currentChasePickLocation: $scope.currentChasePickLocation,
					newChasePickLocation: $scope.chasePickLocation
				}
				insertStatus('assignChasePickLocationForSku', data);
			}
			$mdDialog.hide();
			$scope.working = true ;
			$timeout(function() {
				refresh() ;
				$scope.working=false ;
			},2000 ) ;
		}

		function
			  refreshAssignedShortPicks() {
			  Global.busyRefresh(true);
			  DbFactory.post({
					topic: 'shortPicks',
					action: 'getAssignedShortPicks'
			  })
					.success(AssignedShortPicksSuccess)
					.error(executeError);
		}

		function AssignedShortPicksSuccess(data) {
			  AssignedShortPicksAll = data ;
			  for(var i=0 ; i<AssignedShortPicksAll.length ; i++){
				AssignedShortPicksAll[i].selected=false ;
			  }
			  buildAssignedShortPicksTable(AssignedShortPicksFilter());
		}

////////////////////////////////////////////////////////////////////////////////////////
/////////Assigned Sku List

	    var AssignedSkuListsAll = [];
		$scope.AssignedSkuListClearFilters = AssignedSkuListClearFilters;
		$scope.AssignedSkuListSKUFilter = '';
		$scope.AssignedSkuListChaseOperatorIdFilter = '';
		$scope.AssignedSkuListPickTypeFilter = 'All';
        $scope.AssignedDailyWaveFilter = 'All';
        $scope.AssignedWaveSeqs = [];

		function AssignedSkuListClearFilters() {
		  $scope.AssignedSkuListSKUFilter = '';
		  $scope.AssignedSkuListChaseOperatorIdFilter = '';
		  $scope.AssignedSkuListPickTypeFilter = 'All';
		  $scope.AssignedDailyWaveFilter = 'All';
		  refresh();
		}

		function AssignedSkuListFilter() {
		  var filtered = [] ;
		  for(var i=0 ; i<AssignedSkuListsAll.length ; i++)
		  {
			var include = true;
			AssignedSkuListsAll[i].shown = false;
			if( $scope.AssignedSkuListSKUFilter!='' && $scope.AssignedSkuListSKUFilter.toUpperCase() !=AssignedSkuListsAll[i].sku )
			  include = false;
			if( $scope.AssignedSkuListChaseOperatorIdFilter!='' && $scope.AssignedSkuListChaseOperatorIdFilter != AssignedSkuListsAll[i].chasePickOperatorId )
			  include = false;
			if( $scope.AssignedSkuListPickTypeFilter!='All' && !$scope.AssignedSkuListPickTypeFilter.includes(AssignedSkuListsAll[i].pickType) )
			  include = false;
			if( $scope.AssignedDailyWaveFilter!='All' && $scope.AssignedDailyWaveFilter != AssignedSkuListsAll[i].dailyWaveSeq )
			  include = false;
			if (include) {
			  AssignedSkuListsAll[i].shown = true;
			  filtered.push(AssignedSkuListsAll[i]);
			}
		  }
		  return filtered;
		}

		function
			  refreshAssignedSkuList() {
			  Global.busyRefresh(true);
			  DbFactory.post({
					topic: 'shortPicks',
					action: 'getAssignedSkuList'
			  })
					.success(AssignedSkuListSuccess)
					.error(executeError);
		}

		function AssignedSkuListSuccess(data) {
			  AssignedSkuListsAll = data ;
			  $scope.assignedWaveSeqs = _.uniq(_.map(data,'dailyWaveSeq')) ;
			  buildAssignedSkuListTable(AssignedSkuListFilter());
		}

		var AssignedSkuListTable = null;

		function buildAssignedSkuListTable(data) {
			  var cols = [];
			  var ref = "#AssignedSkuListTable";

			  cols.push({ title: "Wave", class: "dt-center", data: "dailyWaveSeq"});
			  cols.push({ title: "SKU", class: "dt-center", data: "sku"});
			  cols.push({ title: "Desc", class: "dt-center", data: "description"});
			  cols.push({ title: "Barcode", class: "dt-center", data: "barcode"});
			  cols.push({ title: "Type", class: "dt-center", data: "pickType"});
	          cols.push({ title: "Required", class: "dt-center", data: "requiredQty"});
			  cols.push({ title: "Chase Picker", class: "dt-center", data: "chasePickOperatorId"});
			  cols.push({title: "Mark Picked", class: "dt-center", data: {}, render: (data) =>
				{ return buttonRender(ref, data, 'shortPicksEdit','Mark Picked', data.wave+data.sku+data.type+'markPicked', messageChasePickedForSku, true) }});
			  cols.push({title: "Not Found", class: "dt-center", data: {}, render: (data) =>
				{ return buttonRender(ref, data, 'shortPicksEdit','Not Found', data.wave+data.sku+data.type+'notFound', messageNotFoundForSku, true) }});
			  cols.push({title: "Chase Location", class: "dt-center", data: {},
					render: (data) => {
						const chaseLocation = data.chasePickLocation;
						let displayContent = chaseLocation ? `<span>${chaseLocation}</span>` : '';
						return buttonRender(
							ref, data, 'shortPicksEdit',
							displayContent || 'Select Location',
							data.sku + data.chasePickLocation + 'selectLocation',
							() => messageLocation(data, 'sku'),
							true
						);
					}
			  });

			  if (AssignedSkuListTable) {
				   AssignedSkuListTable.clear();
				   AssignedSkuListTable.rows.add(data);
				   AssignedSkuListTable.draw(false);
			  } else {
				   AssignedSkuListTable = $(ref)
						  .DataTable({
								data: data,
								columns: cols,
								order: [],
								scrollY: "550px",
								scrollX: true,
								scrollCollapse: true,
								paging: true,
								pageLength: 50,
								dom: 'lftBipr',
                              	buttons: ['copy','print','excel','pdf']
						  });
					$timeout(AssignedSkuListTable.draw, 0);
			  }
			  Global.busyRefresh(false);
		}

		function refreshTab1(){
			console.log("refresh tab1");
			refreshAssignedShortPicks();
			refreshAssignedSkuList();
		}

////////////////////////////////////////////////////////////////////////////////////////
////////Tab2 Picked Chase Picks		

		var PickedChasePicksAll = [];
		$scope.PickedChasePicksSelectAll = PickedChasePicksSelectAll;
		$scope.PickedChasePicksSelectNone = PickedChasePicksSelectNone;
		$scope.PickedChasePicksSelectShown = PickedChasePicksSelectShown;
		$scope.PickedChasePicksMarkPutMass = PickedChasePicksMarkPutMass;


		$scope.PickedChasePicksClearFilters = PickedChasePicksClearFilters;
		$scope.PickedChasePicksShowFilters = true;
		$scope.PickedChasePicksFiltered = 0 ;
		$scope.PickedChasePicksTotal = 0 ;
		$scope.PickedChasePicksSelected = 0 ;
		$scope.pickedChasePicksLastSeenFilter = 'All';


		function PickedChasePicksSelectAll() {
		  for(var i=0 ; i<PickedChasePicksAll.length ; i++)
			PickedChasePicksAll[i].selected = true ;
		  buildPickedChasePicksTable(PickedChasePicksFilter());
		}

		function PickedChasePicksSelectNone() {
		  for(var i=0 ; i<PickedChasePicksAll.length ; i++)
			PickedChasePicksAll[i].selected = false ;
		  buildPickedChasePicksTable(PickedChasePicksFilter());
		}

		function PickedChasePicksSelectShown() {
		  for (var i = 0; i < PickedChasePicksAll.length; i++)
			if (PickedChasePicksAll[i].shown)
			  PickedChasePicksAll[i].selected = true;
		  buildPickedChasePicksTable(PickedChasePicksFilter());
		}

		function PickedChasePicksClearFilters() {
          $scope.pickedChasePicksLastSeenFilter = 'All';
		  refresh();
		}

		function PickedChasePicksFilter() {
		  var filtered = [] ;
		  $scope.PickedChasePicksTotal = 0 ;
		  $scope.PickedChasePicksSelected = 0 ;
		  $scope.PickedChasePicksFiltered = 0 ;
		  for(var i=0 ; i<PickedChasePicksAll.length ; i++)
		  {
			var include = true;
			PickedChasePicksAll[i].shown = false;
			if( $scope.pickedChasePicksLastSeenFilter!=='All' && !PickedChasePicksAll[i].lastPositionLogical?.includes($scope.pickedChasePicksLastSeenFilter) )
			  include = false;
			if (include) {
			  PickedChasePicksAll[i].shown = true;
			  filtered.push(PickedChasePicksAll[i]);
			  $scope.PickedChasePicksFiltered++;
			}
			if (PickedChasePicksAll[i].selected)
			  $scope.PickedChasePicksSelected++;
			$scope.PickedChasePicksTotal++;
		  }
		  return filtered;
		}

		function PickedChasePicksMarkPutMass(ev){
		  var count = 0 ;
		  for(var s = 0 ; s<PickedChasePicksAll.length ; s++)
			if(PickedChasePicksAll[s].selected)
			  count++ ;

		  var dialog
		  = $mdDialog.confirm()
			 .title("Mark put in short carton")
			 .textContent("Are you sure you want to mark "+count+" picks as put in short carton?")
			 .ariaLabel('Mark put')
			 .targetEvent(ev)
			 .ok('Mark put')
			 .cancel('Cancel') ;

		  $mdDialog
			 .show(dialog)
			 .then(function()
			{
			  for(var i=0 ; i<PickedChasePicksAll.length ; i++) {
				if(PickedChasePicksAll[i].selected) {
				  const data = {
					  pickSeq: PickedChasePicksAll[i].pickSeq
				  };
				  console.log('mark picked' + data.pickSeq);
				  insertStatus('chasePickPut', data)
				}
			  }
			  $scope.working = true ;
			  $timeout(function()
			  {
				 refresh() ;
				 $scope.working=false ;
			   },2000 * (1 + count/50) ) ;
			}) ;
		}

		var PickedChasePicksTable = null;

		function buildPickedChasePicksTable(data) {
			  var cols = [];
			  var ref = "#PickedChasePicksTable";

			  cols.push({title: "Selected", 		data:"selected", 		class:"dt-center"}) ;
			  cols.push({ title: "SKU", class: "dt-center", data: "sku"});
			  cols.push({ title: "UOM", class: "dt-center", data: "uom"});
			  cols.push({ title: "Type", class: "dt-center", data: "pickType"});
			  cols.push({ title: "Invoice", class: "dt-center", data: "orderId"});
			  cols.push({ title: "Wave", class: "dt-center", data: "dailyWaveSeq"});
			  cols.push({ title: "Demand Date", class: "dt-center", data: "demandDate"});
			  cols.push({ title: "LPN #1", class: "dt-center", data: "lpn"});
			  cols.push({ title: "LPN #2", class: "dt-center", data: "trackingNumber"});
			  cols.push({ title: "Operator", class: "dt-center", data: "chasePickOperatorId"});
			  cols.push({ title: "Chase Location", class: "dt-center", data: "chasePickLocation"});
			  cols.push({ title: "Last Seen", class: "dt-center", data: "lastPositionLogical"});
			  cols.push({ title: "Not Found", class: "dt-center", data: "notFound", render:checkMarkRender});
			  cols.push({title: "Mark Put", class: "dt-center", data: {}, render: (data) =>
			  { return buttonRender(ref, data, 'shortPicksEdit','Mark Put', data.pickSeq+'markPut', messageMarkPut, true) }});
			  cols.push({title: "Short Ship", class: "dt-center", data: {}, render: (data) =>
			  { return buttonRender(ref, data, 'shortPicksShortShip', 'Allow Short Ship', data.pickSeq + 'shipShort', messageShortShip, true) }});


			  if (PickedChasePicksTable) {
				   PickedChasePicksTable.clear();
				   PickedChasePicksTable.rows.add(data);
				   PickedChasePicksTable.draw(false);
			  } else {
				   PickedChasePicksTable = $(ref)
						  .DataTable({
								data: data,
								rowCallback: PickedChasePicksCallback(),
								columns: cols,
								order: [],
								scrollY: "550px",
								scrollX: true,
								scrollCollapse: true,
								paging: true,
								pageLength: 50,
								dom: 'lftBipr',
                              	buttons: ['copy','print','excel','pdf']
						  });
					$(ref+' tbody').on('click','td',PickedChasePicksTableClick);
					$timeout(PickedChasePicksTable.draw, 0);
			  }
			  Global.busyRefresh(false);
		}

		function PickedChasePicksCallback() {
		  return (function(row,data,index)
		  {
			if(data.selected) {
			  $(row).css('background-color','#e0ffe0') ;
			  $("td:eq(0)",row).html("&#10004") ;
			} else {
			  $(row).css('background-color','') ;
			  $("td:eq(0)",row).html("") ;
			}
		  });
		}

		function PickedChasePicksTableClick() {
		  var data = PickedChasePicksTable.row(this).data();
		  var idx = PickedChasePicksTable.cell(this).index().column;
		  if( data.pickSeq ){
			if(idx==0){
			  data.selected = !data.selected ;
			  PickedChasePicksTable.row(this).data(data).draw(false) ;
			  $scope.PickedChasePicksSelected = 0;
			  for(var i=0 ; i<PickedChasePicksAll.length ; i++) {
				if( (PickedChasePicksAll[i].pickSeq==data.pickSeq) )
				  PickedChasePicksAll[i].selected = data.selected ;
				if(PickedChasePicksAll[i].selected)
				  $scope.PickedChasePicksSelected++;
			  }
			  $scope.$apply() ;
			}
		  }
		}

		function messageMarkPut(data) {
			  $scope.pickSeq = data.pickSeq;
			  $scope.sku = data.sku;
			  $scope.location = data.location;
			  $scope.uom = data.uom;

			  let item = '[' + $scope.sku +'/' +  $scope.uom + ']';

			  var dialog = $mdDialog.confirm()
					.title('Mark Put')
					.textContent('Are you sure you want to mark: 1 ' + item +' as put in ' + data.lpn + '?')
					.ok('Mark Put')
					.cancel('Cancel');
			  $mdDialog
					.show(dialog)
					.then(markPutRecord);
		}


		function markPutRecord(){
			  Global.busyRefresh(true);
			  const data = {
				  pickSeq: $scope.pickSeq
			  };
			  insertStatus('chasePickPut', data)
			  $timeout(function() {
				 refresh();
			  },2000) ;
		}


		function
			  refreshPickedChasePicks() {
			  Global.busyRefresh(true);
			  DbFactory.post({
					topic: 'shortPicks',
					action: 'getPickedChasePicks'
			  })
					.success(PickedChasePicksSuccess)
					.error(executeError);
		}

		function PickedChasePicksSuccess(data) {
			  PickedChasePicksAll = data ;
			  for(var i=0 ; i<PickedChasePicksAll.length ; i++){
				PickedChasePicksAll[i].selected=false ;
			  }
			  buildPickedChasePicksTable(PickedChasePicksFilter());
		}

		function refreshTab2(){
			console.log("refresh tab2");
			refreshPickedChasePicks();
		}

/**************************************************************************************/
// Tab3 Short Cartons


		var ShortCartonsTable = null;

		function buildShortCartonsTable(data) {
			  var cols = [];
			  var ref = "#shortCartons";

			  cols.push({ title: "LPN #1", class: "dt-center", data: "lpn"});
			  cols.push({ title: "LPN #2", class: "dt-center", data: "trackingNumber"});
			  cols.push({ title: "Type", class: "dt-center", data: "pickType"});
			  cols.push({ title: "Carton Type", class: "dt-center", data: "cartonType"});
			  cols.push({ title: "Invoice", class: "dt-center", data: "orderId"});
			  cols.push({ title: "Wave", class: "dt-center", data: "dailyWaveSeq"});
			  cols.push({ title: "Demand Date", class: "dt-center", data: "demandDate"});
			  cols.push({ title: "Last Seen", class: "dt-center", data: "lastPositionLogical"});
			  cols.push({title: "Assign LPN", class: "dt-center", data: {}, render: (data) =>
			  { return buttonRender(ref, data, 'shortPicksEdit','Assign LPN', data.cartonSeq+'assignLpn', messageAssingLpn, data.lpn==data.trackingNumber) }});
			  cols.push({title: "Short Ship", class: "dt-center", data: {}, render: (data) =>
			  { return buttonRender(ref, data, 'shortPicksShortShip','Allow Short Ship', data.cartonSeq+'shipShort', messageShortShipCarton, true) }});


			  if (ShortCartonsTable) {
				   ShortCartonsTable.clear();
				   ShortCartonsTable.rows.add(data);
				   ShortCartonsTable.draw(false);
			  } else {
				   ShortCartonsTable = $(ref)
						  .DataTable({
								data: data,
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
								scrollY: "550px",
								scrollX: true,
								scrollCollapse: true,
								paging: true,
								pageLength: 50,
								dom: 'lftipr'
						  });
					$(ref+' tbody').on('click','td',shortCartonsClick) ;
					$timeout(ShortCartonsTable.draw, 0);
			  }
			  Global.busyRefresh(false);
		}

		function shortCartonsClick() {
		  var idx = ShortCartonsTable.cell(this).index().column;
		  var data = ShortCartonsTable.row(this).data() ;
		  if( idx == 0 ){
			var cartonSeq = data.cartonSeq;
			window.location = '#/cartons?cartonSeq=' + cartonSeq;
		  }
		}

		function messageAssingLpn(data) {
			$scope.cartonSeq = data.cartonSeq;
			$scope.cartonType = data.cartonType;
			$mdDialog.show({
				templateUrl: 'lpnAssign.html',
				clickOutsideToClose: false,
				scope: $scope,
				preserveScope: true,
				controller: function($scope) {},
				parent: angular.element(document.body),
			});
			$timeout(function() {
				cancelAssignLpn();
			},30000) ;
		}

	    $scope.doAssignLpn = doAssignLpn;
	    $scope.cancelAssignLpn = cancelAssignLpn;

		function cancelAssignLpn(){
		  $mdDialog.cancel();
		}

		function doAssignLpn(){
			  if( $scope.cartonType == 'TOTE' ){
				 if( !$scope.newCartonLpn.includes('TT') ){
					 $mdDialog.hide();
					 Global.showMessage('Invalid LPN, expecting TT lpn for tote');
					 return;
				 }
			  } else if( $scope.cartonType == 'EXPORT' ){
				  if( !$scope.newCartonLpn.includes('C4') ){
					 $mdDialog.hide();
					 Global.showMessage('Invalid LPN, expecting C4 lpn for export box');
					 return;
				  }

			  } else {
				  if( !$scope.newCartonLpn.includes('C1') && !$scope.newCartonLpn.includes('C2') && !$scope.newCartonLpn.includes('C3') ){
					 $mdDialog.hide();
					 Global.showMessage('Invalid LPN, expecting C1, C2 or C3 lpn for box');
					 return;
				  }
			  }
			  Global.busyRefresh(true);
			  const data = {
				  cartonSeq: $scope.cartonSeq,
				  newCartonLpn: $scope.newCartonLpn
			  };
			  insertStatus('assignLpn', data)
			  $mdDialog.hide();
			  $scope.working = true ;
			  $timeout(function() {
				 $scope.working=false ;
				 refresh();
			  },2000) ;
		}

		function
			  refreshShortCartons() {
			  Global.busyRefresh(true);
			  DbFactory.post({
					topic: 'shortPicks',
					action: 'getShortCartons'
			  })
					.success(ShortCartonsSuccess)
					.error(executeError);
		}

		function ShortCartonsSuccess(data) {
			  buildShortCartonsTable(data);
		}

		function refreshTab3(){
			console.log("refresh tab3");
			refreshShortCartons();
		}

/************************************************************************************************************************************/

/**************************************************************************************/
// tab4 Short Picks by SKU

		$scope.showMarkOutInfo = showMarkOutInfo;
		$scope.closeMarkOutInfo = closeMarkOutInfo;

		function showMarkOutInfo(event) {
			$mdDialog.show({
				templateUrl: 'markOutInfo.html',
				clickOutsideToClose: true,
				scope: $scope,
				preserveScope: true,
				controller: function($scope) {},
				parent: angular.element(document.body),
			});
		}

		function closeMarkOutInfo(){
			$mdDialog.cancel();
		}

		var ShortPicksBySkuTable = null;

		function buildShortPicksBySkuTable(data) {
			  var cols = [];
			  var ref = "#shortPicksBySku";

			  cols.push({ title: "SKU", class: "dt-center", data: "sku"});
			  cols.push({ title: "Type", class: "dt-center", data: "pickType"});
			  cols.push({ title: "Qty", class: "dt-center", data: "qty"});
			  cols.push({title: "Mark Out", class: "dt-center", data: {}, render: (data) =>
			  { return buttonRender(ref, data, 'shortPicksMarkOut','Mark Out', data.sku+'markOut', messageMarkout, true) }});

			  if (ShortPicksBySkuTable) {
				   ShortPicksBySkuTable.clear();
				   ShortPicksBySkuTable.rows.add(data);
				   ShortPicksBySkuTable.draw(false);
			  } else {
				   ShortPicksBySkuTable = $(ref)
						  .DataTable({
								data: data,
								columns: cols,
								order: [],
								scrollY: "550px",
								scrollX: true,
								scrollCollapse: true,
								paging: true,
								pageLength: 50,
								dom: 'lftpr'
						  });
					$timeout(ShortPicksBySkuTable.draw, 0);
			  }
			  Global.busyRefresh(false);
		}

		function messageMarkout(data) {
			  $scope.sku = data.sku;

			  let item = $scope.sku;

			  var dialog = $mdDialog.confirm()
					.title('Mark Out')
					.textContent('Are you sure you want to mark out sku: ' + item +'?')
					.ok('Mark Out')
					.cancel('Cancel');
			  $mdDialog
					.show(dialog)
					.then(markOutRecord);
		}


		function markOutRecord(){
			  Global.busyRefresh(true);
			  const data = {
				  sku: $scope.sku
			  };
			  insertStatus('markOut', data)
              $scope.working = true;
			  $timeout(function() {
				 refresh();
				 $scope.working = false;
			  },2000) ;
		}


		function
			  refreshShortPicksBySku() {
			  Global.busyRefresh(true);
			  DbFactory.post({
					topic: 'shortPicks',
					action: 'getShortPicksBySku'
			  })
					.success(ShortPicksBySkuSuccess)
					.error(executeError);
		}

		function ShortPicksBySkuSuccess(data) {
			  buildShortPicksBySkuTable(data);
		}

		var MarkOutSkusTable = null;

		function buildMarkOutSkusTable(data) {
			  var cols = [];
			  var ref = "#markOutSkus";

			  cols.push({ title: "SKU", class: "dt-center", data: "sku"});
			  cols.push({ title: "Operator", class: "dt-center", data: "operator"});
			  cols.push({ title: "Data/Time", class: "dt-center", data: "stamp", render: dateRender});
			  cols.push({title: "Clear Mark Out", class: "dt-center", data: {}, render: (data) =>
			  { return buttonRender(ref, data, 'shortPicksMarkOut','Clear Mark Out', data.sku+'clearMarkOut', messageClearMarkout, true) }});

			  if (MarkOutSkusTable) {
				   MarkOutSkusTable.clear();
				   MarkOutSkusTable.rows.add(data);
				   MarkOutSkusTable.draw(false);
			  } else {
				   MarkOutSkusTable = $(ref)
						  .DataTable({
								data: data,
								columns: cols,
								order: [],
								scrollY: "550px",
								scrollX: true,
								scrollCollapse: true,
								paging: true,
								pageLength: 50,
								dom: 'lftpr'
						  });
					$timeout(MarkOutSkusTable.draw, 0);
			  }
			  Global.busyRefresh(false);
		}

		function messageClearMarkout(data) {
			  $scope.markOutSku = data.sku;

			  let item = $scope.markOutSku;

			  var dialog = $mdDialog.confirm()
					.title('Clear Mark Out')
					.textContent('Are you sure you want to clear mark out sku: ' + item +'? This will only affect future download order lines.')
					.ok('Clear Mark Out')
					.cancel('Cancel');
			  $mdDialog
					.show(dialog)
					.then(clearMarkOutRecord);
		}


		function clearMarkOutRecord(){
			  Global.busyRefresh(true);
			  const data = {
				  sku: $scope.markOutSku
			  };
			  insertStatus('clearMarkOut', data)
              $scope.working = true;
			  $timeout(function() {
				 refresh();
				 $scope.working = false;
			  },2000) ;
		}


		function
			  refreshMarkOutSkus() {
			  Global.busyRefresh(true);
			  DbFactory.post({
					topic: 'shortPicks',
					action: 'getMarkOutSkus'
			  })
					.success(MarkOutSkusSuccess)
					.error(executeError);
		}

		function MarkOutSkusSuccess(data) {
			  buildMarkOutSkusTable(data);
		}

		function refreshTab4(){
			console.log("refresh tab4");
			refreshShortPicksBySku();
			refreshMarkOutSkus();
		}

		//================================================================================================================================================
		// TAB 5: CfgDepartments
		$scope.CfgDepartments = {};
		$scope.CfgDepartmentsReadonly = true;
		$scope.CfgDepartmentsNewEntry = false;
		$scope.CfgDepartmentsNew = CfgDepartmentsNew;
		$scope.updateDefaultChasePicker = updateDefaultChasePicker;
		$scope.clearDefaultChasePicker = clearDefaultChasePicker;

		function CfgDepartmentsNew() {
		  $scope.CfgDepartments.rdsPickZone = '';
		  $scope.CfgDepartments.defaultChasePicker = '';
		}

		function updateDefaultChasePicker() {
		  doCfgDepartmentsUpdate();
		}

		function doCfgDepartmentsUpdate() {
		  var dialog =
			$mdDialog.confirm()
			  .title("Set default chase picker for pick Type " + $scope.CfgDepartments.rdsPickZone)
			  .textContent('Are you sure you want to set default chase picker for the selected pick type?')
			  .ariaLabel('Update')
			  .ok('Update')
			  .cancel('Cancel') ;
		  $mdDialog
			.show(dialog)
			.then(function() {
				  DbFactory
					.post({
							 topic:'shortPicks',
							 action:'updateDefaultChasePicker',
							 params: $scope.CfgDepartments
						 })
					.error(executeError);
				$scope.working = true ;
				$timeout(function() {
				  refreshTab5() ; CfgDepartmentsNew(); $scope.working=false ;
				},2000) ;
			  }) ;
		}

		function clearDefaultChasePicker() {
			doCfgDepartmentsClear();
		}

		function doCfgDepartmentsClear() {
			var dialog =
				$mdDialog.confirm()
					.title("Clear the default chase picker for pick Type " + $scope.CfgDepartments.rdsPickZone)
					.textContent('Are you sure you want to clear the default chase picker for the selected pick type?')
					.ariaLabel('Clear')
					.ok('Clear')
					.cancel('Cancel') ;
			$mdDialog
				.show(dialog)
				.then(function() {
					DbFactory
						.post({
							topic:'shortPicks',
							action:'clearDefaultChasePicker',
							params: $scope.CfgDepartments
						})
						.error(executeError);
					$scope.working = true ;
					$timeout(function() {
						refreshTab5() ; CfgDepartmentsNew(); $scope.working=false ;
					},2000) ;
				}) ;
		}

		function refreshcfgDepartmentss() {
		  DbFactory.post({
						 topic: 'shortPicks',
						 action: 'getDefaultChasePicker'
						 })
				   .success(getcfgDepartmentssSuccess)
				   .error(executeError);
		}

		function getcfgDepartmentssSuccess(data){
		  buildcfgDepartmentssTable(data);
		}

		var cfgDepartmentssTable = null;

		function buildcfgDepartmentssTable(data){
		  var cols = [];
		  var ref = "#cfgDepartments";

		  cols.push({title: 'Pick Type',data:'rdsPickZone',class:'dt-center'});
		  cols.push({title: 'Default Chase Picker',data:'defaultChasePicker',class:'dt-center'});

		  if(cfgDepartmentssTable){
			cfgDepartmentssTable.clear();
			cfgDepartmentssTable.rows.add(data);
			cfgDepartmentssTable.draw(false);
		  } else {
			cfgDepartmentssTable = $(ref)
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
						  scrollX: true,
						  scrollY: '425px',
						  scrollCollapse: true,
						  pagingType: 'numbers',
						  paging: true,
						  dom: 'lftriBp',
						  lengthMenu: [25,50,100],
						  buttons: ['copy','print','excel','pdf']});
			$(ref+' tbody').on('click','td',cfgDepartmentssTableClick);
			setTimeout(function(){cfgDepartmentssTable.draw();},0);
		  }
		}

		function cfgDepartmentssTableClick() {
		  var data = cfgDepartmentssTable.row(this).data();
		  if( data ){
			  $scope.CfgDepartments = data;
			  $scope.CfgDepartmentsReadonly = true;
			  $scope.CfgDepartmentsNewEntry = false;
		  }
		  $scope.$apply();
		}

		$scope.refreshTab5 = refreshTab5;

		function refreshTab5() {
		  refreshcfgDepartmentss();
		  getOperatorList();
		}

/************************************************************************************************************************************/


		function refresh() {
		  switch($scope.selected){
			case 0:
			  refreshTab0();
			  break;
			case 1:
			  refreshTab1();
			  break;
			case 2:
			  refreshTab2();
			  break;
			case 3:
			  refreshTab3();
			  break;
			case 4:
			  refreshTab4();
			  break;
			case 5:
			  refreshTab5();
			  break;
		  }
		}

		function
			  init() {
			  Global.setTitle('Short Picks');
			  Global.recv('refresh', refresh, $scope);
			  refresh();
		}

		init();

    }

      function
            shortPicksConfig($routeProvider) {
            $routeProvider
                  .when('/shortPicks', {
                        controller: 'ShortPicksController',
                        templateUrl: '/app/shortPicks/shortPicks.view.html'
                  });
      }

}())
