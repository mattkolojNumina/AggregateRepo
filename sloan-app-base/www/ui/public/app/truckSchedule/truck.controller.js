(
  function() {
	  angular
      .module('ui')
      .controller('truckController',truckController);

	  angular
      .module('ui')
      .config(truckConfig);

  truckController.$inject = ['$scope','$timeout','$routeParams','$mdDialog','$interval','_','Global','DbFactory'];

  function truckController($scope,$timeout,$routeParams,$mdDialog,$interval,_,Global,DbFactory) {
    $scope.refresh = refresh;
    $scope.permit = Global.permit;
    $scope.tinyintList = [0,1];

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
    
    function dateRender_old(data,type,full,meta) {
      if(data==null)
        return '';		
      if(type!='display')
        return data;
      
      var date = new Date(data);
	  //return date.toLocaleString('en-US',{year: 'numeric', month: 'numeric', day: 'numeric',hour: 'numeric',minute:'numeric' });
      return date.toLocaleString();
    }
    
    function dateRender(data,type,full,meta) {
      if(data){
        if(type=='display'){
          var date = new Date(data);
          return date.toLocaleString();
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
  
    //================================================================================================================================================
    // TAB 0: CfgTruckSchedule
    $scope.CfgTruckSchedule = {};
	$scope.CfgTruckScheduleBackup = {};
    $scope.CfgTruckScheduleReadonly = true;
    $scope.CfgTruckScheduleNewEntry = false;
    $scope.CfgTruckScheduleNew = CfgTruckScheduleNew;
    $scope.CfgTruckScheduleUpdate = CfgTruckScheduleUpdate;
    $scope.CfgTruckScheduleDelete = CfgTruckScheduleDelete;
	$scope.doorList = [];

    function CfgTruckScheduleNew() {
      $scope.CfgTruckSchedule.truckNumber = '';
	  $scope.CfgTruckSchedule.door = '';
      $scope.CfgTruckScheduleNewEntry = true;
      $scope.CfgTruckScheduleReadonly = false;
    }

    function CfgTruckScheduleDelete() {
      var dialog = 
        $mdDialog.confirm()
          .title('Delete Truck Schedule')
          .textContent('Are you sure you want to delete the selected truck schedule?')
          .ariaLabel('Delete Truck Schedule')
          .ok('Delete')
          .cancel('Cancel') ;
      $mdDialog
        .show(dialog)
        .then(function() {
              refreshCount(1,'CfgTruckScheduleDelete');
              DbFactory
                .post({
                         topic:'truck',
                         action:'cfgTruckScheduleDelete',
                         params: $scope.CfgTruckSchedule
                     })
                .success(executeSuccess)
                .error(executeError); 
            $scope.working = true ;
            $timeout(function() { 
              refresh() ; CfgTruckScheduleNew(); $scope.working=false ;
            },1000) ;
          }) ;
    }

    function CfgTruckScheduleUpdate() {
      if($scope.CfgTruckScheduleNewEntry)
        doCfgTruckScheduleCreate();
      else
        doCfgTruckScheduleUpdate();
    }

    function doCfgTruckScheduleUpdate() {
      var dialog = 
        $mdDialog.confirm()
          .title('Update truck schedule')
          .textContent('Are you sure you want to update the selected truck schedule?')
          .ariaLabel('Update truck schedule')
          .ok('Update')
          .cancel('Cancel') ;
      $mdDialog
        .show(dialog)
        .then(function() {
		    if( $scope.CfgTruckSchedule.door != $scope.oldDoor ){	
              refreshCount(1,'CfgTruckScheduleUpdate');
              DbFactory
                .post({
                         topic:'truck',
                         action:'cfgTruckScheduleUpdate',
                         params: $scope.CfgTruckSchedule
                     })
                .success(executeSuccess)
                .error(executeError);
		    }
            $scope.working = true ;
            $timeout(function() { 
                refresh() ; CfgTruckScheduleNew(); $scope.working=false ;
            },1000) ;
          }) ;
    }

    function doCfgTruckScheduleCreate() {
      var dialog = 
        $mdDialog.confirm()
          .title('Create truck schedule')
          .textContent('Are you sure you want to create new truck schedule?')
          .ariaLabel('Create truck schedule')
          .ok('Create')
          .cancel('Cancel') ;
      $mdDialog
        .show(dialog)
        .then(function() {
              refreshCount(1,'CfgTruckScheduleCreate');
              DbFactory
                .post({
                         topic:'truck',
                         action:'cfgTruckScheduleCreate',
                         params: $scope.CfgTruckSchedule
                     })
                .success(executeSuccess)
                .error(executeError); 
            $scope.working = true ;
            $timeout(function() { 
              refresh() ; CfgTruckScheduleNew(); $scope.working=false ;
            },2000) ;
          }) ;
    }
    
    function refreshcfgTruckSchedules() {
      refreshCount(1,'refreshcfgTruckSchedules');
      DbFactory.post({
                     topic: 'truck',
                     action: 'getcfgTruckSchedule'
                     })
               .success(getcfgTruckSchedulesSuccess)
               .error(executeError);
    }

    function getcfgTruckSchedulesSuccess(data){ 
      buildcfgTruckSchedulesTable(data); 
    }

    var cfgTruckSchedulesTable = null;

    function buildcfgTruckSchedulesTable(data){
      var cols = [];
      var ref = "#CfgTruckSchedule";

      cols.push({title: 'Truck Number',data:'truckNumber',class:'dt-center'});
	  cols.push({title: 'Door',data:'door',class:'dt-center'});
      
      if(cfgTruckSchedulesTable){
        cfgTruckSchedulesTable.clear();
        cfgTruckSchedulesTable.rows.add(data);
        cfgTruckSchedulesTable.draw(false);
      } else {
        cfgTruckSchedulesTable = $(ref)
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
        $(ref+' tbody').on('click','td',cfgTruckSchedulesTableClick);
        setTimeout(function(){cfgTruckSchedulesTable.draw();},0);
      }
      refreshCount(-1,'buildcfgTruckSchedulesTable')
    }

    function cfgTruckSchedulesTableClick() {
      var data = cfgTruckSchedulesTable.row(this).data();
	  if( data ){
		  $scope.CfgTruckSchedule = data;
		  $scope.oldDoor = data.door;
		  $scope.CfgTruckScheduleReadonly = true;
		  $scope.CfgTruckScheduleNewEntry = false;
	  }
      $scope.$apply();
    }
	
	function getDoorList(){
	  refreshCount(1,'getDoorList');
	  DbFactory.post({
			topic: 'truck',
			action: 'getDoorList'
	  })
			.success(getDoorListSuccess)
			.error(executeError);			
	}
	
	function getDoorListSuccess(data){
		$scope.doorList = _.uniq(_.map(data,'door')) ;
		refreshCount(-1,'getDoorListSuccess');
	}	
    
    $scope.refreshTab0 = refreshTab0;

    function refreshTab0() {
	  getDoorList();
      refreshcfgTruckSchedules();
    }	  

    //================================================================================================================================================
    // TAB 1: CfgDefaultTruckSchedule
    $scope.CfgDefaultTruckSchedule = {};
    $scope.CfgDefaultTruckScheduleReadonly = true;
    $scope.CfgDefaultTruckScheduleNewEntry = false;
    $scope.CfgDefaultTruckScheduleNew = CfgDefaultTruckScheduleNew;
    $scope.CfgDefaultTruckScheduleUpdate = CfgDefaultTruckScheduleUpdate;
    $scope.CfgDefaultTruckScheduleDelete = CfgDefaultTruckScheduleDelete;
	var daysOfWeek = ['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'];
	let d = new Date();
    let day = d.getDay();
	$scope.dayOfWeekFilter = daysOfWeek[day];

    function CfgDefaultTruckScheduleNew() {
	  $scope.CfgDefaultTruckSchedule.dayOfWeek = $scope.dayOfWeekFilter;
      $scope.CfgDefaultTruckSchedule.truckNumber = '';
	  $scope.CfgDefaultTruckSchedule.door = '';
      $scope.CfgDefaultTruckScheduleNewEntry = true;
      $scope.CfgDefaultTruckScheduleReadonly = false;
    }

    function CfgDefaultTruckScheduleDelete() {
      var dialog = 
        $mdDialog.confirm()
          .title('Delete Truck Schedule')
          .textContent('Are you sure you want to delete the selected truck schedule?')
          .ariaLabel('Delete Truck Schedule')
          .ok('Delete')
          .cancel('Cancel') ;
      $mdDialog
        .show(dialog)
        .then(function() {
              refreshCount(1,'CfgDefaultTruckScheduleDelete');
              DbFactory
                .post({
                         topic:'truck',
                         action:'cfgDefaultTruckScheduleDelete',
                         params: $scope.CfgDefaultTruckSchedule
                     })
                .success(loadDefaultSchedule)
                .error(executeError); 
            $scope.working = true ;
            $timeout(function() { 
              refresh() ; CfgDefaultTruckScheduleNew(); $scope.working=false ;
            },1000) ;
          }) ;
    }

    function CfgDefaultTruckScheduleUpdate() {
      if($scope.CfgDefaultTruckScheduleNewEntry)
        doCfgDefaultTruckScheduleCreate();
      else
        doCfgDefaultTruckScheduleUpdate();
    }

    function doCfgDefaultTruckScheduleUpdate() {
      var dialog = 
        $mdDialog.confirm()
          .title('Update truck schedule')
          .textContent('Are you sure you want to update the selected truck schedule?')
          .ariaLabel('Update truck schedule')
          .ok('Update')
          .cancel('Cancel') ;
      $mdDialog
        .show(dialog)
        .then(function() {
              refreshCount(1,'CfgDefaultTruckScheduleUpdate');
              DbFactory
                .post({
                         topic:'truck',
                         action:'cfgDefaultTruckScheduleUpdate',
                         params: $scope.CfgDefaultTruckSchedule
                     })
                .success(loadDefaultSchedule)
                .error(executeError); 
            $scope.working = true ;
            $timeout(function() { 
              refresh() ; CfgDefaultTruckScheduleNew(); $scope.working=false ;
            },2000) ;
          }) ;
    }

    function doCfgDefaultTruckScheduleCreate() {
      var dialog = 
        $mdDialog.confirm()
          .title('Create truck schedule')
          .textContent('Are you sure you want to create new truck schedule?')
          .ariaLabel('Create truck schedule')
          .ok('Create')
          .cancel('Cancel') ;
      $mdDialog
        .show(dialog)
        .then(function() {
              refreshCount(1,'CfgDefaultTruckScheduleCreate');
              DbFactory
                .post({
                         topic:'truck',
                         action:'cfgDefaultTruckScheduleCreate',
                         params: $scope.CfgDefaultTruckSchedule
                     })
                .success(loadDefaultSchedule)
                .error(executeError); 
            $scope.working = true ;
            $timeout(function() { 
              refresh() ; CfgDefaultTruckScheduleNew(); $scope.working=false ;
            },2000) ;
          }) ;
    }
	
	function loadDefaultSchedule(){
	  refreshCount(0,'loadDefaultSchedule');
	  let d = new Date();
      let day = d.getDay();
	  DbFactory
		.post({
				 topic:'truck',
				 action:'loadDefaultSchedule',
				 params: { 'dayOfWeekFilter': daysOfWeek[day] }
			 })
		.success(executeSuccess)
        .error(executeError); 		
	}
    
    function refreshcfgDefaultTruckSchedule() {
      refreshCount(1,'refreshcfgDefaultTruckSchedule');
      DbFactory.post({
                     topic: 'truck',
                     action: 'getcfgDefaultTruckSchedule'
                     })
               .success(getcfgDefaultTruckScheduleSuccess)
               .error(executeError);
    }

    function getcfgDefaultTruckScheduleSuccess(data){ 
      buildcfgDefaultTruckScheduleTable(cfgDefaultTruckScheduleFilter(data)); 
    }
	
	function cfgDefaultTruckScheduleFilter(data){
      var filtered = [] ;
      for(var i=0 ; i<data.length ; i++)
      {
	    if(data[i].dayOfWeek == $scope.dayOfWeekFilter)
           filtered.push(data[i]);
      }
      return filtered;
	}

    var cfgDefaultTruckScheduleTable = null;

    function buildcfgDefaultTruckScheduleTable(data){
      var cols = [];
      var ref = "#CfgDefaultTruckSchedule";

      cols.push({title: 'Day',data:'dayOfWeek',class:'dt-center'});
      cols.push({title: 'Truck Number',data:'truckNumber',class:'dt-center'});
	  cols.push({title: 'Door',data:'door',class:'dt-center'});
      
      if(cfgDefaultTruckScheduleTable){
        cfgDefaultTruckScheduleTable.clear();
        cfgDefaultTruckScheduleTable.rows.add(data);
        cfgDefaultTruckScheduleTable.draw(false);
      } else {
        cfgDefaultTruckScheduleTable = $(ref)
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
        $(ref+' tbody').on('click','td',cfgDefaultTruckScheduleTableClick);
        setTimeout(function(){cfgDefaultTruckScheduleTable.draw();},0);
      }
      refreshCount(-1,'buildcfgDefaultTruckScheduleTable')
    }

    function cfgDefaultTruckScheduleTableClick() {
      var data = cfgDefaultTruckScheduleTable.row(this).data();
	  if( data ){
		  $scope.CfgDefaultTruckSchedule = data;
		  $scope.CfgDefaultTruckScheduleBackup = data;
		  $scope.CfgDefaultTruckScheduleReadonly = true;
		  $scope.CfgDefaultTruckScheduleNewEntry = false;
	  }
      $scope.$apply();
    }
	
	function getDoorList(){
	  refreshCount(1,'getDoorList');
	  DbFactory.post({
			topic: 'truck',
			action: 'getDoorList'
	  })
			.success(getDoorListSuccess)
			.error(executeError);			
	}
	
	function getDoorListSuccess(data){
		$scope.doorList = _.uniq(_.map(data,'door')) ;
		refreshCount(-1,'getDoorListSuccess');
	}	
    
    $scope.refreshTab1 = refreshTab1;

    function refreshTab1() {
	  getDoorList();
      refreshcfgDefaultTruckSchedule();
    }	
	
    //================================================================================================================================================
    // TAB 2: CfgPhysicalLane
    $scope.CfgPhysicalLane = {};
    $scope.CfgPhysicalLaneReadonly = true;
    $scope.CfgPhysicalLaneNewEntry = false;
    $scope.CfgPhysicalLaneUpdate = CfgPhysicalLaneUpdate;

    function CfgPhysicalLaneUpdate() {
        doCfgPhysicalLaneUpdate();
    }

    function doCfgPhysicalLaneUpdate() {
      var dialog = 
        $mdDialog.confirm()
          .title('Update sorter lane')
          .textContent('Are you sure you want to update the selected sorter lane?')
          .ariaLabel('Update sorter lane')
          .ok('Update')
          .cancel('Cancel') ;
      $mdDialog
        .show(dialog)
        .then(function() {
              refreshCount(1,'CfgPhysicalLaneUpdate');
              DbFactory
                .post({
                         topic:'truck',
                         action:'cfgPhysicalLaneUpdate',
                         params: $scope.CfgPhysicalLane
                     })
                .success(executeSuccess)
                .error(executeError); 
            $scope.working = true ;
            $timeout(function() { 
              refresh() ; $scope.CfgPhysicalLane = {}; $scope.working=false ;
            },2000) ;
          }) ;
    }

    
    function refreshcfgPhysicalLanes() {
      refreshCount(1,'refreshcfgPhysicalLanes');
      DbFactory.post({
                     topic: 'truck',
                     action: 'getcfgPhysicalLane'
                     })
               .success(getcfgPhysicalLanesSuccess)
               .error(executeError);
    }

    function getcfgPhysicalLanesSuccess(data){ 
      buildcfgPhysicalLanesTable(data); 
    }

    var cfgPhysicalLanesTable = null;

    function buildcfgPhysicalLanesTable(data){
      var cols = [];
      var ref = "#cfgPhysicalLanes";

	  cols.push({title: 'Lane',data:'physical',class:'dt-center'});
	  cols.push({title: 'Enabled',data:'enabled',class:'dt-center',render:checkMarkRender});
      
      if(cfgPhysicalLanesTable){
        cfgPhysicalLanesTable.clear();
        cfgPhysicalLanesTable.rows.add(data);
        cfgPhysicalLanesTable.draw(false);
      } else {
        cfgPhysicalLanesTable = $(ref)
          .DataTable({data: data,
                      columns: cols,
                      order: [],
                      scrollX: true,
                      scrollY: '425px',
                      scrollCollapse: true,
                      pagingType: 'numbers',
                      paging: true,
                      dom: 'lftriBp',
                      lengthMenu: [25,50,100],
                      buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','td',cfgPhysicalLanesTableClick);
        setTimeout(function(){cfgPhysicalLanesTable.draw();},0);
      }
      refreshCount(-1,'buildcfgPhysicalLanesTable')
    }

    function cfgPhysicalLanesTableClick() {
      var data = cfgPhysicalLanesTable.row(this).data();
	  if(data){
		  $scope.CfgPhysicalLane = data;
		  $scope.CfgPhysicalLaneReadonly = true;
		  $scope.CfgPhysicalLaneNewEntry = false;
	  }
      $scope.$apply();
    }
    
    $scope.refreshTab2 = refreshTab2;

    function refreshTab2() {
      refreshcfgPhysicalLanes();
    }	
	
    //================================================================================================================================================
    // TAB 3: CfgDoor
    $scope.CfgDoor = {};
    $scope.CfgDoorReadonly = true;
    $scope.CfgDoorNewEntry = false;
    $scope.CfgDoorUpdate = CfgDoorUpdate;
	$scope.sorterLaneList = [];

    function CfgDoorUpdate() {
        doCfgDoorUpdate();
    }

    function doCfgDoorUpdate() {
      var dialog = 
        $mdDialog.confirm()
          .title('Update door')
          .textContent('Are you sure you want to update the selected door?')
          .ariaLabel('Update door')
          .ok('Update')
          .cancel('Cancel') ;
      $mdDialog
        .show(dialog)
        .then(function() {
              refreshCount(1,'CfgDoorUpdate');
              DbFactory
                .post({
                         topic:'truck',
                         action:'cfgDoorUpdate',
                         params: $scope.CfgDoor
                     })
                .success(executeSuccess)
                .error(executeError); 
            $scope.working = true ;
            $timeout(function() { 
              refresh() ; $scope.CfgDoor = {}; $scope.working=false ;
            },2000) ;
          }) ;
    }

    
    function refreshcfgDoors() {
      refreshCount(1,'refreshcfgDoors');
      DbFactory.post({
                     topic: 'truck',
                     action: 'getcfgDoor'
                     })
               .success(getcfgDoorsSuccess)
               .error(executeError);
    }

    function getcfgDoorsSuccess(data){ 
      buildcfgDoorsTable(data); 
    }

    var cfgDoorsTable = null;

    function buildcfgDoorsTable(data){
      var cols = [];
      var ref = "#cfgDoors";

	  cols.push({title: 'Door',data:'door',class:'dt-center'});
	  cols.push({title: 'Lane',data:'lane',class:'dt-center'});
      
      if(cfgDoorsTable){
        cfgDoorsTable.clear();
        cfgDoorsTable.rows.add(data);
        cfgDoorsTable.draw(false);
      } else {
        cfgDoorsTable = $(ref)
          .DataTable({data: data,
                      columns: cols,
                      order: [],
                      scrollX: true,
                      scrollY: '425px',
                      scrollCollapse: true,
                      pagingType: 'numbers',
                      paging: true,
                      dom: 'lftriBp',
                      lengthMenu: [25,50,100],
                      buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','td',cfgDoorsTableClick);
        setTimeout(function(){cfgDoorsTable.draw();},0);
      }
      refreshCount(-1,'buildcfgDoorsTable')
    }

    function cfgDoorsTableClick() {
      var data = cfgDoorsTable.row(this).data();
	  if(data){
		  $scope.CfgDoor = data;
		  $scope.CfgDoorReadonly = true;
		  $scope.CfgDoorNewEntry = false;
	  }
      $scope.$apply();
    }
    
	function getSorterLaneList(){
	  refreshCount(1,'getSorterLaneList');
	  DbFactory.post({
			topic: 'truck',
			action: 'getSorterLaneList'
	  })
			.success(getSorterLaneListSuccess)
			.error(executeError);			
	}
	
	function getSorterLaneListSuccess(data){
		$scope.sorterLaneList = _.uniq(_.map(data,'physical')) ;
		refreshCount(-1,'getSorterLaneListSuccess');
	}	
	
    $scope.refreshTab3 = refreshTab3;

    function refreshTab3() {
      refreshcfgDoors();
	  getSorterLaneList();
    }	
    

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
      }
    }
    var periodic;
    function init() {
      Global.setTitle('Truck Schedule Config');
      Global.recv('refresh',refresh,$scope);
      refresh();
    }

    init();
  }

  function truckConfig($routeProvider) {
    $routeProvider
      .when('/truck', {controller: 'truckController',templateUrl: '/app/truckSchedule/truck.view.html'});
  }
}())
