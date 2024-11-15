(function()
{
  angular
    .module('ui')
      .controller('WavesController',wavesController);

  angular
    .module('ui')
      .config(wavesConfig);

  wavesController.$inject = ['$scope','$timeout','$routeParams','_',
                              '$mdDialog','$interval','Global','DbFactory'];
  
  function
  wavesController($scope,$timeout,$routeParams,_,$mdDialog,$interval,Global,DbFactory)
  {
    
    $scope.refresh = refresh;
    
    $scope.wave = {};
    $scope.waveSeq = "";
    $scope.lookup = lookup;
    $scope.selectWave = selectWave;  
    
    $scope.permit = Global.permit;
    
    var refreshCounter = 0;
    
    function
    refreshCount(n)
    {
      refreshCounter += n;
      if(refreshCounter>0)
        Global.busyRefresh(true);
      else
        Global.busyRefresh(false);
    }
    
    function
    refreshCount(n,name)
    {
      refreshCounter += n;
      if(refreshCounter>0)
        Global.busyRefresh(true);
      else
        Global.busyRefresh(false);
      if(true) //make true for logging
        console.log(name+": "+refreshCounter);
    }    
       
    
    // // // // //
    // TABLE HELPERS

    function checkRight(row, col, tip) {
      $('td:eq(' + col + ')', row)
        .html(tip ?
          '<div class="righttip">'
          + '&check;'
          + '<span class="righttiptext">'
          + simpleDate(tip)
          + '</span>'
          + '</div>'
          : '');
    }
    
    function
    simpleDate(data)
    {
      if(data==null)
        return '';
      var date = new Date(data);
      var today = new Date();
      //if(today.toDateString()==date.toDateString())
      //  return date.toLocaleTimeString();
      return date.toLocaleString();
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

    function
    executeError(err){
      console.log(err);
      refreshCount(-1,'executeError');		
    }
    
    function
    clearCount(){
      refreshCount(-1);
    }
	
    function
    executeSuccess()
    {
      refresh();
      refreshCount(-1,'executeSuccess');
    }		
    

    // // // // //
    // DATA RETRIEVAL
    
    // // //
    // TAB 0 wave Release
	
	$scope.showConfig=true;
	$scope.updateConfig = updateConfig;
	$scope.pauseRefresh = pauseRefresh;
	$scope.pauseWaveReleaseRefresh = false;
    
     
    
    function updateConfig() {
      var dialog = $mdDialog.confirm()
        .title('Update Release Configuration')
        .textContent('Are you sure you want to update configuration for wave release?')
        .ariaLabel('Update Release Configuration') 
        .ok('Confirm')
        .cancel('Cancel');
      
      $mdDialog
        .show(dialog)
        .then(function(){
          refreshCount(1,"updateConfig");
          DbFactory.post({topic: 'waves',
                          action: 'updateConfig',
                          params: {autoRelease: $scope.autoRelease?'yes':'no',
						           zoueRouteAutoReleaseTime: $scope.zoueRouteAutoReleaseTime.toLocaleTimeString('it-IT'),
								   cartPickAutoReleaseTime: $scope.cartPickAutoReleaseTime.toLocaleTimeString('it-IT'),
								   geekAutoReleaseTime: $scope.geekAutoReleaseTime.toLocaleTimeString('it-IT')}
                         })
            .success(updateConfigSuccess)
            .error(executeError);
			 $scope.working = true ;
			 $timeout(function() {
				$scope.pauseWaveReleaseRefresh = false; 
				$scope.working = false ;
				refresh();
			 },1000) ;
          }) ;
    }

    function updateConfigSuccess(){
      refreshCount(-1,"updateConfigSuccess");
    }    
    
    function pauseRefresh(){
      $scope.pauseWaveReleaseRefresh = true;
    }
	
    function refreshUnreleasedWaves() {
      DbFactory.post({topic: 'waves',
                      action: 'unreleased'
                     })
        .success(unreleasedWavesSuccess)
        .error  (executeError);
    }
    
    function unreleasedWavesSuccess(data) { 
	  for( let i=0; i<data.length; i++ ){
		data[i].numGeekTotalAndReceived = data[i].numGeek + '/' + data[i].numGeekReceived;
	  }
      buildUnreleasedWavesTable(data); 
    }
    
    var unreleasedWavesTable = null;
    
    function buildUnreleasedWavesTable(data) {
      var cols = [];
      var ref = "#unreleasedWaves";

      cols.push({title: "Batch ID",   data:"waveName",class:"dt-center"});
	  cols.push({title: "Demand Date",   data:"demandDate",class:"dt-center"});
	  cols.push({title: "Wave #",   data:"dailyWaveSeq",class:"dt-center"});
      cols.push({title: "Created",   data:"createStamp",class:"dt-center",render:dateRender});
      cols.push({title: "ZoneRoute Released",   data:"zoneRouteReleaseStamp",class:"dt-center",render:dateRender});
	  cols.push({title: "# ZoneRoute",   data:"numZoneRoute",class:"dt-center"});
      cols.push({title: "CartPick Released",   data:"cartPickReleaseStamp",class:"dt-center",render:dateRender});
	  cols.push({title: "# CartPick",   data:"numCartPick",class:"dt-center"});
	  cols.push({title: "Geek Released",   data:"geekReleaseStamp",class:"dt-center",render:dateRender});
	  cols.push({title: "# Geek",   data:"numGeekTotalAndReceived",class:"dt-center"});
      
      if(unreleasedWavesTable){
        unreleasedWavesTable.clear();
        unreleasedWavesTable.rows.add(data);
        unreleasedWavesTable.draw(false);
      } else {
        unreleasedWavesTable = $(ref)
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
							  rowCallback: unleasedWavesTableCallback,
                              scrollY: "700px",
                              scrollCollapse: true,
                              paging: true,
                              pagingType: "numbers",
                              pageLength: 100,
                              dom: 'lftBipr',
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','td',unleasedWavesTableClick);
		$(ref+' tbody').on('click','button',unreleasedWavesButtonClick);
        setTimeout(function(){unreleasedWavesTable.draw();},0);
      }
      //refreshCount(-1);
    }
	
    function unleasedWavesTableCallback(row,data,index) {				
      if( !data.zoneRouteReleaseStamp ){
		if( data.canReleaseZoneRoute==1 ){
			if(Global.permit('wavesEdit')){
			  $("td:eq(4)",row).html('<button class="tableButton">Release ZoneRoute</button>');
			} else {
			  $("td:eq(4)",row).html('<button class="tableButton" disabled>Release ZoneRoute</button>');
			}
		} else {
			$("td:eq(4)",row).html('<button class="tableButton" disabled>Can\'t release</button>');
		}
	  }
      if( !data.cartPickReleaseStamp ){
		if( data.canReleaseCartPick==1 ){
			if(Global.permit('wavesEdit')){
			  $("td:eq(6)",row).html('<button class="tableButton">Release CartPick</button>');
			} else {
			  $("td:eq(6)",row).html('<button class="tableButton" disabled>Release CartPick</button>');
			}
		} else {
			$("td:eq(6)",row).html('<button class="tableButton" disabled>Can\'t release</button>');
		}
	  }	
      if( !data.geekReleaseStamp ){
		if( data.canReleaseGeek==1 ){
			if(Global.permit('wavesEdit')){
			  $("td:eq(8)",row).html('<button class="tableButton">Release Geek</button>');
			} else {
			  $("td:eq(8)",row).html('<button class="tableButton" disabled>Release Geek</button>');
			}
		} else {
			$("td:eq(8)",row).html('<button class="tableButton" disabled>Can\'t release</button>');
		}
	  }	  
    }
	
    function unreleasedWavesButtonClick() {
      var row  = $(this).parents('tr');
      var data = unreleasedWavesTable.row(row).data();
      var col = $(this).parents('td')[0].cellIndex;

      if(data && data.waveSeq && !data.zoneRouteReleaseStamp && col==4)
        releaseWave(data.waveSeq, 'zoneRouteRelease', 'zoneRoute pick');
      if(data && data.waveSeq && !data.cartPickReleaseStamp && col==6)
        releaseWave(data.waveSeq, 'cartPickRelease', 'cart pick');
      if(data && data.waveSeq && !data.geekReleaseStamp && col==8)
        releaseWave(data.waveSeq, 'geekRelease', 'geek pick');
    }
	
    function releaseWave(waveSeq,statusType,pickType) {
      var dialog = $mdDialog.confirm()
      .title('Wave Release')
      .textContent('Are you sure you want to release wave ' + waveSeq + ' ' + pickType )
      .ariaLabel('Wave Release')
      .ok('Yes')
      .cancel('Cancel');

      $mdDialog
        .show(dialog)
        .then(function(){
		  var data = {};
		  data.waveSeq = waveSeq;
		  var user = Global.getUser().user;
		  DbFactory.post({
				topic: 'waves',
				action: 'insertStatus',
				params: {
					  statusType : statusType,
					  data : JSON.stringify(data),
					  appName: 'orderRelease',
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
	
    
    function unleasedWavesTableClick() {
      var data = unreleasedWavesTable.row(this).data();
      var idx = unreleasedWavesTable.cell(this).index().column;
      if( data.waveSeq ){
        if(idx==0){
          $scope.waveSeq = data.waveSeq;
		  $scope.lookupId = data.waveSeq;
          $scope.selected = 3;
          $scope.$apply() ;
          lookupSeq();				
        }
      }
    }
	
    function getReleaseConfig(){
      refreshCount(1,"getReleaseConfig");
      DbFactory.post({topic: 'waves',
                      action: 'getReleaseConfig'
                     })
        .success(getReleaseConfigSuccess)
        .error  (executeError);         
      
    }
    
    function getReleaseConfigSuccess(data){
      refreshCount(-1,"getReleaseConfigSuccess");
	  for( let i = 0 ; i< data.length; i++ ){
		if( data[i]['name'] == 'autoRelease' ) $scope.autoRelease = (data[i]['value'] == 'yes');
		if( data[i]['name'] == 'zoueRouteAutoReleaseTime' ) $scope.zoueRouteAutoReleaseTime = new Date("1970-01-01T" + data[i]['value']);
		if( data[i]['name'] == 'cartPickAutoReleaseTime' ) $scope.cartPickAutoReleaseTime = new Date("1970-01-01T" + data[i]['value']);
		if( data[i]['name'] == 'geekAutoReleaseTime' ) $scope.geekAutoReleaseTime = new Date("1970-01-01T" + data[i]['value']);  
	  }
	  console.log($scope.zoueRouteAutoReleaseTime);
    }	
	
    function refreshTab0(){
      if(!$scope.pauseWaveReleaseRefresh){
		refreshUnreleasedWaves();
		getReleaseConfig();
	  }
    }       
    
    // // //
    // TAB 1 started waves   
    function
    refreshStartedWaves()
    {
	  refreshCount(1,'refreshStartedWaves');	
      DbFactory.post({topic: 'waves',
                      action: 'started'
                     })
        .success(startedWavesSuccess)
        .error  (executeError);
    }
    
    function 
    startedWavesSuccess(data)
    { 
      buildStartedWavesTable(data); 
    }	
	
    var startedWavesTable = null;    
	
    function
    buildStartedWavesTable(data)
    {
      var cols = [];
      var ref = "#startedWaves";
      
      cols.push({title: "Batch ID",   data:"waveName",class:"dt-center"});
	  cols.push({title: "Demand Date",   data:"demandDate",class:"dt-center"});
	  cols.push({title: "Wave #",   data:"dailyWaveSeq",class:"dt-center"});
      cols.push({title: "Created",   data:"createStamp",class:"dt-center",render:dateRender});
      cols.push({title: "ZoneRoute Released",   data:"zoneRouteReleaseStamp",class:"dt-center",render:dateRender});
      cols.push({title: "CartPick Released",   data:"cartPickReleaseStamp",class:"dt-center",render:dateRender});
	  cols.push({title: "Geek Released",   data:"geekReleaseStamp",class:"dt-center",render:dateRender});  
      cols.push({title: "Picked",   data:"pickEndStamp",class:"dt-center",render:dateRender});     
      cols.push({title: "Labeled",   data:"labelStamp",class:"dt-center",render:dateRender});     	  
      
      
      if(startedWavesTable){
        startedWavesTable.clear();
        startedWavesTable.rows.add(data);
        startedWavesTable.draw(false);
      } else {
        startedWavesTable = $(ref)
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
                              dom: 'lftBipr',
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','td',startedWavesTableClick);
        setTimeout(function(){startedWavesTable.draw();},0);
      }
      refreshCount(-1,'buildStartedWavesTable');
    }
    
    function
    startedWavesTableClick()
    {
      var data = startedWavesTable.row(this).data();
      var idx = startedWavesTable.cell(this).index().column;
      if( idx==0 && data.waveSeq ){
		$scope.waveSeq = data.waveSeq;
		$scope.lookupId = data.waveSeq;
		$scope.selected = 3;
		$scope.$apply() ;
		lookupSeq();				
      }
    }  

    // // //
    // TAB 2 completed waves
	
    function toDueDate(date){
      return date.getFullYear() + 
        ('00' +(date.getMonth()+1)).slice(-2) +
        ('00' +(date.getDate())).slice(-2);
    }

    var completedAll = [];
    
    $scope.displayTodayCompleted = displayTodayCompleted;
    $scope.displayCompleted = displayCompleted;
	
    function
    displayCompleted()
    {
      refreshCount(1,'displayCompleted');
      if($scope.date1 && $scope.date2){
        $scope.d1 = new Date($scope.date1);
        $scope.d2 = new Date($scope.date2);
          DbFactory.post({topic: 'waves',
                  action: 'completedBetween',
                    params:{start: toDueDate($scope.d1),
                    end: toDueDate($scope.d2)}
                       })
          .success(completedWavesSuccess)
          .error  (executeError);
      }
      else if($scope.date1 && (!$scope.date2)){
        $scope.d1 = new Date($scope.date1);
          DbFactory.post({topic: 'waves',
                  action: 'completedAfter',
                    params:{start: toDueDate($scope.d1)}
                       })
          .success(completedWavesSuccess)
          .error  (executeError);		
      }
      else if($scope.date2 && (!$scope.date1)){
        $scope.d2 = new Date($scope.date2);
          DbFactory.post({topic: 'waves',
                  action: 'completedBefore',
                    params:{end: toDueDate($scope.d2)}
                       })
          .success(completedWavesSuccess)
          .error  (executeError);		
      }
      else{
        displayTodayCompleted();
      }			
    }
	
    function
    displayTodayCompleted()
    {
      DbFactory.post({topic: 'waves',
                      action: 'completedToday'
                     })
        .success(completedWavesSuccess)
        .error  (executeError);
    }
    
    function 
    completedWavesSuccess(data)
    { 
	    completedAll = data ; 
      buildCompletedWavesTable(completedWavesFilter()); 
    }
	
    function completedWavesFilter(){
      var filtered = [] ;	
      for(var i=0 ; i<completedAll.length ; i++)
      {
        filtered.push(completedAll[i]);	
      }
      return filtered;
    }	
	
    var completedWavesTable = null;
	
    function
    buildCompletedWavesTable(data)
    {
      var cols = [];
      var ref = "#completedWaves";
      
      cols.push({title: "Batch ID",   data:"waveName",class:"dt-center"});
	  cols.push({title: "Demand Date",   data:"demandDate",class:"dt-center"});
	  cols.push({title: "Wave #",   data:"dailyWaveSeq",class:"dt-center"});
      cols.push({title: "# Orders",  data:"numOrders", class:"dt-center"});
      cols.push({title: "# Cartons",     data:"numCartons", class:"dt-center"});
      cols.push({title: "Created",   data:"createdStamp",class:"dt-center",render:dateRender});
      cols.push({title: "Picked",     data:"pickedStamp",class:"dt-center",render:dateRender});	
      cols.push({title: "Labeled",     data:"labeledStamp",class:"dt-center",render:dateRender});		
      cols.push({title: "Completed",       data:"completeStamp",class:"dt-center",render:dateRender});          
      
      if(completedWavesTable){
        completedWavesTable.clear();
        completedWavesTable.rows.add(data);
        completedWavesTable.draw(false);
      } else {
        completedWavesTable = $(ref)
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
                              dom: 'lftBipr',
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','td',completedWavesTableClick);
        setTimeout(function(){completedWavesTable.draw();},0);
      }
      refreshCount(-1,'buildCompletedWavesTable');
    }
    
    function
    completedWavesTableClick()
    {
      var data = completedWavesTable.row(this).data();
      var idx = completedWavesTable.cell(this).index().column;
      if( data.waveSeq && idx==0 ){
          $scope.waveSeq = data.waveSeq;
		  $scope.lookupId = data.waveSeq;
          $scope.selected = 3;
          $scope.$apply() ;
          lookupSeq();				
      }
    }

    // // //
    // TAB 3 wave details  	  

    $scope.completeWave = completeWave;
	
	function completeWave(){
	  var dialog = $mdDialog.confirm()
	  .title('Mark Wave Complete')
	  .textContent('Are you sure you want to mark wave: ' + $scope.wave.waveName + ' complete?')
	  .ariaLabel('Mark Complete')
	  .ok('Yes')
	  .cancel('Cancel');
	  
	  $mdDialog
		.show(dialog)
		.then(function(){
		var data = {};
		data.waveSeq = $scope.wave.waveSeq;
		var user = Global.getUser().user;
		DbFactory.post({topic: 'orders',
						action: 'insertStatus',
						params: {
							  appName : 'statusApp',
							  statusType : 'waveComplete',
							  data : JSON.stringify(data),
							  operator : user
						}
						 });                      
		Global.busyRefresh(true);
		$timeout(function() { 
		   Global.busyRefresh(false);
		   $scope.waveSeq = $scope.wave.waveSeq;
		   lookupSeq();
		},2000) ;
	  });		
		
		
	}
	
    function
    lookup()
    {
      if(!$scope.lookupId) return;
      refreshCount(1,"lookup");
        DbFactory.post({topic: 'waves',
                        action: 'lookup',
              params: {waveSeq: $scope.lookupId,
			           waveName: '%'+$scope.lookupId+'%'}
                       })
          .success(populateWave)
          .error  (executeError);			
    }
	
    function
    lookupSeq()
    {
      if(!$scope.waveSeq) return;
      refreshCount(1,"lookup");
        DbFactory.post({topic: 'waves',
                        action: 'lookupSeq',
              params: {waveSeq: $scope.waveSeq}
                       })
          .success(populateWave)
          .error  (executeError);			
    }	
    
    function
    populateWave(data)
    {
      refreshCount(-1,"populateWave");
      if(data.length > 10){
        Global.showMessage('Too many matching records found!');				
        $scope.wave = {};
      } else if(data.length==0) {
        Global.showMessage('Not found!');				
        $scope.wave = {};
      } else if(data.length==1){
        prepareWave( data[0] );
        populateOrders($scope.wave.waveSeq);
		populateLines($scope.wave.waveSeq);
        populateCartons($scope.wave.waveSeq);
        populateCartonsFullcase($scope.wave.waveSeq);
        populateHistory($scope.wave.waveSeq);
      } else {
        wavesLookupDialog( data );
      }
    } 

    function wavesLookupDialog( data )
    {
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
	
    function selectWave(wave) 
    {				
      if (wave) 
      {			
        prepareWave( wave );
        populateOrders($scope.wave.waveSeq);
		populateLines($scope.wave.waveSeq);
        populateCartons($scope.wave.waveSeq);
        populateCartonsFullcase($scope.wave.waveSeq);
        populateHistory($scope.wave.waveSeq);
      }
      $mdDialog.hide();			
    } 

    // wave object
    function
    prepareWave( wave )
    {
      $scope.wave = wave;
      $scope.waveSeq = wave.waveSeq;
    }
    
    // wave orders
    function
    populateOrders( waveSeq )
    {
      DbFactory.post({topic: 'waves',
                      action: 'getOrders',
            params: {waveSeq: waveSeq}
                     })
        .success(populateOrdersSuccess)
        .error  (executeError);			
    }
	
    function
    populateOrdersSuccess(data)
    {
      buildwaveOrdersTable(data);
    }	
	
    var waveOrdersTable = null;	
	
    function
    buildwaveOrdersTable(data)
    {
      var cols = [];
      var ref = "#waveOrders";
      
      cols.push({title: "OrderId(invoice)",   data:"orderId",class:"dt-center"});
      cols.push({title: "Type",       data:"orderType",class:"dt-center"});
      cols.push({title: "Customer",   data:"customerNumber",class:"dt-center"});
      cols.push({title: "Truck",    data:"truckNumber",class:"dt-center"});
      cols.push({title: "Door",       data:"door",class:"dt-center"});
      cols.push({title: "Stop",       data:"stop",class:"dt-center"});
      cols.push({title: "Status",       data:"status",class:"dt-center"});     
      cols.push({title: "Released",   data:"releaseStamp",class:"dt-center",render:dateRender}); 
      cols.push({title: "Picked",   data:"pickEndStamp",class:"dt-center",render:dateRender});   
      cols.push({title: "Labeled",   data:"labelStamp",class:"dt-center",render:dateRender});   
      cols.push({title: "Completed",   data:"completeStamp",class:"dt-center",render:dateRender});   	  
      
      if(waveOrdersTable){
        waveOrdersTable.clear();
        waveOrdersTable.rows.add(data);
        waveOrdersTable.draw(false);
      } else {
        waveOrdersTable = $(ref)
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
                              dom: 'lftBipr',
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','td',waveOrdersTableClick);
        setTimeout(function(){waveOrdersTable.draw();},0);
      }
      refreshCount(0,"buildwaveOrdersTable");
    }

    function
    waveOrdersTableClick()
    {
      var data = waveOrdersTable.row(this).data();
      var idx = waveOrdersTable.cell(this).index().column;
      if( data.orderId ){
        if( idx == 0) {
          var orderId = data.orderId;
          window.location = '#/orders?orderId=' + orderId;	
        }     
      }
    }
	
    // wave lines
    function
    populateLines( waveSeq )
    {
      DbFactory.post({topic: 'waves',
                      action: 'getLines',
            params: {waveSeq: waveSeq}
                     })
        .success(populateLinesSuccess)
        .error  (executeError);			
    }
	
    function
    populateLinesSuccess(data)
    {
      buildwaveLinesTable(data);
    }	
	
    var waveLinesTable = null;	
	
    function
    buildwaveLinesTable(data)
    {
      var cols = [];
      var ref = "#waveLines";
      
      cols.push({title: "Line Seq",       data:"orderLineSeq",class:"dt-center"});
	  cols.push({title: "Order ID",       data:"orderId",class:"dt-center"});
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
      
      if(waveLinesTable){
        waveLinesTable.clear();
        waveLinesTable.rows.add(data);
        waveLinesTable.draw(false);
      } else {
        waveLinesTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              pageLength: 100,
                              order: [],
                              scrollY: "700px",
                              scrollCollapse: true,
                              paging: true,
                              pagingType: "numbers",
                              dom: 'lftBipr',
                              buttons: ['copy','print','excel','pdf']});
        setTimeout(function(){waveLinesTable.draw();},0);
      }
      refreshCount(0,"buildwaveLinesTable");
    }	

    // wave cartons
    function
    populateCartons( waveSeq )
    {
      DbFactory.post({topic: 'waves',
                      action: 'getCartons',
            params: {waveSeq: waveSeq}
                     })
        .success(populateCartonsSuccess)
        .error  (executeError);			
    }
	
    function
    populateCartonsSuccess(data)
    {
      buildWaveCartonsTable(data);
    }	
	
    var waveCartonsTable = null;	
	
    function
    buildWaveCartonsTable(data)
    {
      var cols = [];
      var ref = "#waveCartons";
      
      cols.push({title: "Carton Seq",   data:"cartonSeq",class:"dt-center"});
	  cols.push({title: "OrderId",       data:"orderId",class:"dt-center"});
      cols.push({title: "LPN",       data:"lpn",class:"dt-center"});
	  cols.push({title: "Tracking",       data:"trackingNumber",class:"dt-center"});
	  cols.push({title: "Type",       data:"cartonType",class:"dt-center"});
      cols.push({title: "Pick Area",   data:"pickType",class:"dt-center"});
	  cols.push({title: "Pick Started",   data:"pickStartStamp",class:"dt-center",render:dateRender});	  
      cols.push({title: "Picked",   data:"pickStamp",class:"dt-center",render:dateRender});
      cols.push({title: "Labeled",   data:"labelStamp",class:"dt-center",render:dateRender});           
      cols.push({title: "Sorted",   data:"shipStamp",class:"dt-center",render:dateRender}); 
	  cols.push({title: "Canceled",   data:"cancelStamp",class:"dt-center",render:dateRender}); 
	  //cols.push({title: "Cancel Carton", class: "dt-center", data: {}, render: (data) => 
	  //		  { return buttonRender(ref, data, 'ordersEdit','Cancel Carton', data.cartonSeq+'cancelCarton', messageCancelCarton, ( !data.labelStamp && !data.cancelStamp )) }});	  
      
      if(waveCartonsTable){
        waveCartonsTable.clear();
        waveCartonsTable.rows.add(data);
        waveCartonsTable.draw(false);
      } else {
        waveCartonsTable = $(ref)
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
							  rowCallback: waveRepackCartonsCallback,
                              scrollY: "700px",
                              scrollCollapse: true,
                              paging: true,
                              pagingType: "numbers",
                              dom: 'lftBipr',
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','td',waveCartonsTableClick);
		$(ref+' tbody').on('click','button',waveCartonsButtonClick);
        setTimeout(function(){waveCartonsTable.draw();},0);
      }
      refreshCount(0,"buildWaveCartonsTable");
    }
	
    function waveRepackCartonsCallback(row,data,index) {				
      if( !data.labelStamp && data.pickStamp && !data.cancelStamp ){
		if(Global.permit('ordersEdit')){
		  $("td:eq(8)",row).html('<button class="tableButton">Mark Labeled</button>');
		} else {
		  $("td:eq(8)",row).html('<button class="tableButton" disabled>Mark Labeled</button>');
		}
	  }
      if( !data.shipStamp && data.labelStamp && !data.cancelStamp ){
		if(Global.permit('ordersEdit')){
		  $("td:eq(9)",row).html('<button class="tableButton">Mark Sorted</button>');
		} else {
		  $("td:eq(9)",row).html('<button class="tableButton" disabled>Mark Sorted</button>');
		}
	  }	
      if( !data.cancelStamp && !data.pickStartStamp && data.pickType == 'ZoneRoute' && data.lpn && data.cartonType == 'TOTE' ){
		if(Global.permit('ordersEdit')){
		  $("td:eq(6)",row).html('<button class="tableButton">Reassign LPN</button>');
		} else {
		  $("td:eq(6)",row).html('<button class="tableButton" disabled>Reassign LPN</button>');
		}
	  }	  
      if( !data.cancelStamp && !data.labelStamp ){
		if(Global.permit('ordersEdit')){
		  $("td:eq(10)",row).html('<button class="tableButton">Cancel Carton</button>');
		} else {
		  $("td:eq(10)",row).html('<button class="tableButton" disabled>Cancel Carton</button>');
		}
	  }		  
    }

    function waveCartonsButtonClick() {
      var row  = $(this).parents('tr');
      var data = waveCartonsTable.row(row).data();
      var col = $(this).parents('td')[0].cellIndex;

      if(data && data.cartonSeq && !data.labelStamp && data.pickStamp && !data.cancelStamp && col==8)
        messageMarkCartonLabeled( data );
      if(data && data.cartonSeq && !data.shipStamp && data.labelStamp && !data.cancelStamp && col==9)
        messageMarkCartonSorted( data );
      if(data && data.cartonSeq && !data.cancelStamp && !data.pickStartStamp && col==6)
        messageReassignLpn( data );	
      if(data && data.cartonSeq && !data.cancelStamp && !data.labelStamp && col==10)
        messageCancelCarton( data );
    }	
    
    function
    waveCartonsTableClick()
    {
      var idx = waveCartonsTable.cell(this).index().column;		
      var data = waveCartonsTable.row(this).data() ;
      if( idx == 0 ){
        var seq = data.cartonSeq;
        window.location = '#/cartons?cartonSeq=' + seq;
      } 
    }
	
	function messageMarkCartonLabeled( carton ){
	  var dialog = $mdDialog.confirm()
	  .title('Mark Carton Labeled')
	  .textContent('Are you sure you want to mark carton: ' + carton.lpn + ' labeled?')
	  .ariaLabel('Label Carton')
	  .ok('Yes')
	  .cancel('Cancel');
	  
	  $mdDialog
		.show(dialog)
		.then(function(){
		var data = {};
		data.cartonSeq = carton.cartonSeq;
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
		   Global.busyRefresh(false);
		   populateCartons($scope.wave.waveSeq);
		},2000) ;
	  });
	}	
	
	function messageMarkCartonSorted( carton ){
	  var dialog = $mdDialog.confirm()
	  .title('Mark Carton Sorted')
	  .textContent('Are you sure you want to mark carton: ' + carton.lpn + ' sorted?')
	  .ariaLabel('Sort Carton')
	  .ok('Yes')
	  .cancel('Cancel');
	  
	  $mdDialog
		.show(dialog)
		.then(function(){
		var data = {};
		data.cartonSeq = carton.cartonSeq;
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
		   Global.busyRefresh(false);
		   populateCartons($scope.wave.waveSeq);
		},2000) ;
	  });
	}	

	function messageCancelCarton(data) {
		  $scope.cartonSeq = data.cartonSeq; 
		  $scope.orderId = data.orderId;
		  var dialog = $mdDialog.confirm()
				.title('Cancel Carton')
				.textContent('Are you sure you want to cancel cartonSeq '+$scope.cartonSeq+' in RDS?')
				.ok('Cancel Carton')
				.cancel('Cancel');
		  $mdDialog
				.show(dialog)
				.then(cancelCarton);
	}


	function cancelCarton(){
		  Global.busyRefresh(true);
		  var data = {};
		  data.cartonSeq = $scope.cartonSeq;
		  data.orderId = $scope.orderId;
		  var user = Global.getUser().user;
		  DbFactory.post({
				topic: 'orders',
				action: 'insertStatus',
				params: {
					  statusType : 'cancelCarton',
					  data : JSON.stringify(data),
					  appName: 'statusApp',
					  operator : user
				}
		  })
				.error(function (err) {
					  console.log(err); });

		  $timeout(function() {
			 Global.busyRefresh(false);
			 populateCartons($scope.wave.waveSeq);
		  },2000) ;
	}	
	
	function messageReassignLpn(data) {
		$scope.reassigncartonSeq = data.cartonSeq;
		$scope.cartonType = data.cartonType;
		$mdDialog.show({
			templateUrl: 'lpnReassign.html',
			clickOutsideToClose: false,
			scope: $scope,
			preserveScope: true,
			controller: function($scope) {},
			parent: angular.element(document.body),
		});	
		$timeout(function() { 
			cancelReassignLpn();
		},30000) ;				
	}
	
	$scope.doReassignLpn = doReassignLpn;
	$scope.cancelReassignLpn = cancelReassignLpn;		
	
	function cancelReassignLpn(){			
	  $mdDialog.cancel();
	}

	function doReassignLpn(){
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
		  } else if( $scope.cartonType == 'SMALL' ){
			  if( !$scope.newCartonLpn.includes('C1') ){
				 $mdDialog.hide();
				 Global.showMessage('Invalid LPN, expecting C1 lpn for SMALL box');
				 return;
			  }
		  } else if( $scope.cartonType == 'MEDIUM' ){
			  if( !$scope.newCartonLpn.includes('C2') ){
				 $mdDialog.hide();
				 Global.showMessage('Invalid LPN, expecting C2 lpn for MEDIUM box');
				 return;
			  }
		  } else if( $scope.cartonType == 'LARGE' ){
			  if( !$scope.newCartonLpn.includes('C3') ){
				 $mdDialog.hide();
				 Global.showMessage('Invalid LPN, expecting C3 lpn for LARGE box');
				 return;
			  }				  
		  }
		  Global.busyRefresh(true);
		  var data = {};
		  data.cartonSeq = $scope.reassigncartonSeq;
		  data.newCartonLpn = $scope.newCartonLpn;			  
		  var user = Global.getUser().user;
		  DbFactory.post({
				topic: 'orders',
				action: 'insertStatus',
				params: {
					  statusType : 'reassignLpn',
					  data : JSON.stringify(data),
					  appName: 'statusApp',
					  operator : user
				}
		  })
				.error(function (err) {
					  console.log(err); });
			$mdDialog.hide();
			Global.busyRefresh(true);
		  $timeout(function() {
			 Global.busyRefresh(false);
			 populateCartons($scope.wave.waveSeq);
		  },2000) ;
	}	

    // wave cartons
    function
    populateCartonsFullcase( waveSeq )
    {
      DbFactory.post({topic: 'waves',
                      action: 'getCartonsFullcase',
            params: {waveSeq: waveSeq}
                     })
        .success(populateCartonsFullcaseSuccess)
        .error  (executeError);			
    }
	
    function
    populateCartonsFullcaseSuccess(data)
    {
      buildWaveCartonsFullcaseTable(data);
    }	
	
    var waveCartonsFullcaseTable = null;	
	
    function
    buildWaveCartonsFullcaseTable(data)
    {
      var cols = [];
      var ref = "#waveCartonsFullcase";
      
      cols.push({title: "Carton Seq",   data:"cartonSeq",class:"dt-center"});
	    cols.push({title: "OrderId",       data:"orderId",class:"dt-center"});
	    cols.push({title: "Tracking",       data:"trackingNumber",class:"dt-center"});
      cols.push({title: "Pick Area",   data:"pickType",class:"dt-center"});           
      cols.push({title: "Sorted",   data:"shipStamp",class:"dt-center",render:dateRender});      
      
      if(waveCartonsFullcaseTable){
        waveCartonsFullcaseTable.clear();
        waveCartonsFullcaseTable.rows.add(data);
        waveCartonsFullcaseTable.draw(false);
      } else {
        waveCartonsFullcaseTable = $(ref)
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
                              dom: 'lftBipr',
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','td',waveCartonsFullcaseTableClick);
        setTimeout(function(){waveCartonsFullcaseTable.draw();},0);
      }
      refreshCount(0,"buildWaveCartonsFullcaseTable");
    }
    
    function
    waveCartonsFullcaseTableClick()
    {
      var idx = waveCartonsFullcaseTable.cell(this).index().column;		
      var data = waveCartonsFullcaseTable.row(this).data() ;
      if( idx == 0 ){
        var seq = data.cartonSeq;
        window.location = '#/cartons?cartonSeq=' + seq;
      } 
    }   

    //history
    function
    populateHistory( waveSeq )
    {
        DbFactory.post({topic: 'waves',
                        action: 'history',
              params: {waveSeq: waveSeq}
                       })
          .success(populateHistorySuccess)
          .error  (executeError);			
    }
    
    function
    populateHistorySuccess(data)
    {
      buildHistoryTable(data);
    }
	
    var historyTable = null;	
	
    function
    buildHistoryTable(data)
    {
      var cols = [];
      var ref = "#history";
      
      cols.push({title: "Code",   data:"code",class:"dt-center"});
      cols.push({title: "Description",     data:"message",class:"dt-center"});
      cols.push({title: "Stamp",			data:"stamp",		class:"dt-center",	type:"date",	render: dateRender});	
      
      if(historyTable){
        historyTable.clear();
        historyTable.rows.add(data);
        historyTable.draw(false);
      } else {
        historyTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              order: [],
                              scrollY: "700px",
                              scrollCollapse: true,
                              paging: true,
                              pagingType: "numbers",
                              dom: 'ltBipr',
                              lengthMenu: [ 50,100 ],
                              buttons: ['copy','print','excel','pdf']});
        setTimeout(function(){historyTable.draw();},0);
      }
    }	    

    
 
     
    // // // // //
    // SETUP AND ALL THAT

    function
    refresh()
    {
        switch($scope.selected){
          case 0:
            refreshTab0();
			break;		
          case 1:
            refreshStartedWaves();
            break;               
          case 3:
            lookupSeq();
            break;              
        }
    }

    function
    init()
    {
      Global.setTitle('Waves');
      Global.recv('refresh',refresh,$scope);
      periodic1 = $interval(refresh, 30000);
      //periodic2 = $interval(refreshProcessStatus, 1000);       
      if($routeParams.waveSeq){
        $scope.selected = 3;
        $scope.waveSeq = $routeParams.waveSeq;
        lookupSeq();
      }
      refresh();
    }

    init();
    
    $scope.$on('$destroy', function(){
      $interval.cancel(periodic1);
      //$interval.cancel(periodic2);
    });    

    //$scope.$on('$destroy', function(){
    //  $interval.cancel(periodic);
    //});
    
  }

  function
  wavesConfig($routeProvider)
  {
    $routeProvider
      .when('/waves', {controller: 'WavesController',
                        templateUrl: '/app/waves/waves.view.html'});
  }
  
}())
