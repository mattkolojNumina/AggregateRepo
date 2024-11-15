(function()
{
  angular
    .module('ui')
      .controller('LinesController',linesController);

  angular
    .module('ui')
      .config(linesConfig);

  linesController.$inject = ['$scope','$timeout','$routeParams',
                              '$mdDialog','$interval','Global','DbFactory'];
  
  function linesController($scope,$timeout,$routeParams,$mdDialog,$interval,Global,DbFactory) {
    
    $scope.refresh = refresh;
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

    // TAB 0 started lines

    var startedAll = [];
	
    function refreshStartedLines() {
      DbFactory.post({topic: 'lines',
                      action: 'started'
                     })
        .success(startedLinesSuccess)
        .error  (executeError);
    }
    
    function startedLinesSuccess(data) { 
	  startedAll = data ; 
      buildStartedLinesTable(startedLinesFilter()); 
    }
	
    function startedLinesFilter() {
      var filtered = [] ;	
      for(var i=0 ; i<startedAll.length ; i++)
      {
        filtered.push(startedAll[i]);
      }
      return filtered;
    }	
	
    var startedLinesTable = null;	
	
    function
    buildStartedLinesTable(data)
    {
      var cols = [];
      var ref = "#startedLine";
      
      cols.push({title: "Line Seq",       data:"orderLineSeq",class:"dt-center"});
	  cols.push({title: "Batch ID",       data:"waveName",class:"dt-center"});
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
      
      if(startedLinesTable){
        startedLinesTable.clear();
        startedLinesTable.rows.add(data);
        startedLinesTable.draw(false);
      } else {
        startedLinesTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              order: [],
                              scrollY: "700px",
                              scrollCollapse: true,
                              paging: true,
                              pagingType: "numbers",
                              pageLength: 100,
                              dom: 'lftBipr',
                              buttons: ['copy','print','excel','pdf']});
        setTimeout(function(){startedLinesTable.draw();},0);
      }
      refreshCount(-1, "build started lines table");
    }
    
	
    // // //
    // TAB 1 completed lines
	
    var completedAll = [];
	
    $scope.displayTodayCompleted = displayTodayCompleted;
    $scope.displayCompleted = displayCompleted;
	
    function displayCompleted() {
      if($scope.date1 && $scope.date2){
        $scope.d1 = new Date($scope.date1);
        $scope.d2 = new Date($scope.date2);
          DbFactory.post({topic: 'lines',
                  action: 'completedBetween',
                    params:{start: toDueDate($scope.d1),
                    end: toDueDate($scope.d2)}
                       })
          .success(completedLinesSuccess)
          .error  (executeError);
      } else if($scope.date1 && (!$scope.date2)){
        $scope.d1 = new Date($scope.date1);
          DbFactory.post({topic: 'lines',
                  action: 'completedAfter',
                    params:{start: toDueDate($scope.d1)}
                       })
          .success(completedLinesSuccess)
          .error  (executeError);		
      } else if($scope.date2 && (!$scope.date1)){
        $scope.d2 = new Date($scope.date2);
          DbFactory.post({topic: 'lines',
                  action: 'completedBefore',
                    params:{end: toDueDate($scope.d2)}
                       })
          .success(completedLinesSuccess)
          .error  (executeError);		
      } else{
        displayTodayCompleted();
      }			
    }
	
    function displayTodayCompleted() {
      DbFactory.post({topic: 'lines',
                      action: 'completedToday'
                     })
        .success(completedLinesSuccess)
        .error  (executeError);
    }
    
    function completedLinesSuccess(data) { 
	    completedAll = data ; 
      buildCompletedLinesTable(completedLinesFilter()); 
    }
	
    function completedLinesFilter() {
      var filtered = [] ;	
      for(var i=0 ; i<completedAll.length ; i++)
      {
        filtered.push(completedAll[i]);	
      }
      return filtered;
    }	
	
    var completedLinesTable = null;
    
    function buildCompletedLinesTable(data) {
      var cols = [];
      var ref = "#completedLine";
      
      cols.push({title: "Line Seq",       data:"orderLineSeq",class:"dt-center"});
	  cols.push({title: "Batch ID",       data:"waveName",class:"dt-center"});
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
      
      if(completedLinesTable){
        completedLinesTable.clear();
        completedLinesTable.rows.add(data);
        completedLinesTable.draw(false);
      } else {
        completedLinesTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              order: [],
                              scrollY: "700px",
                              scrollCollapse: true,
                              paging: true,
                              pagingType: "numbers",
                              pageLength: 100,
                              dom: 'lftBipr',
                              buttons: ['copy','print','excel','pdf']});
        setTimeout(function(){completedLinesTable.draw();},0);
      }
      refreshCount(-1, "build completed lines table");
    }	
    
    // // // // //
    // SETUP AND ALL THAT

    function
    refresh()
    {
      switch($scope.selected){				
        case 0:
          refreshStartedLines();
          break;									
        default:
          refreshCount(0, "default tab rendered");
      }
    }

    function
    init()
    {
      Global.setTitle('Lines');
      Global.recv('refresh',refresh,$scope);
      periodic = $interval(refresh, 30000); 
      refresh();
    }

    init();

    $scope.$on('$destroy', function(){
      $interval.cancel(periodic);
    });
    
  }

  function
  linesConfig($routeProvider)
  {
    $routeProvider
      .when('/lines', {controller: 'LinesController',
                        templateUrl: '/app/lines/lines.view.html'});
  }
  
}())

