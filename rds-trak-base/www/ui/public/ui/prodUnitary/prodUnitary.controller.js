(function()
{
  angular
    .module('ui')
      .controller('ProdUnitaryController',prodUnitaryController);
  
  angular
    .module('ui')
      .config(prodUnitaryConfig);
  
  prodUnitaryController.$inject = ['$scope','_','Global','DbFactory','$timeout'];
  
  function
  prodUnitaryController($scope,_,Global,DbFactory,$timeout)
  {
    var getSelectorsDynamically = false;
    
    $scope.report = report;
    $scope.reported = false;
    $scope.changeStart = changeStart;
    $scope.changeEnd = changeEnd;
    $scope.startDate;
    $scope.endDate;

    $scope.operatorSelect; 
    $scope.operatorChoices = [];
    $scope.taskSelect;
    $scope.taskChoices = [];
    $scope.operationSelect;
    $scope.operationChoices = [];
    $scope.areaSelect = '';
    $scope.areaChoices = [];
    $scope.display = display;
    
    var result;
    
    var all = '-- all --';
    
    $scope.proop = {};
    $scope.proopUpdate = proopUpdate;
    
    $scope.permit = Global.permit;
    
    var refreshCounter = 0;
    
    function
    refreshCount(n,name)
    {
      refreshCounter += n;
      if(refreshCounter>0)
        Global.busyRefresh(true);
      else
        Global.busyRefresh(false);
      if(false) //make true for logging
        console.log(name+": "+refreshCounter);
    }
    
    
    // // // // //
    // REPORTS - PROCESSING
    
    function
    prodFilter(input)
    {
      var output = [];
        for(var i=0; i<input.length; i++){
          var keep = true;
          
          if($scope.operatorSelect!=all)
            if($scope.operatorSelect!=input[i].operator)
              keep=false;
          
          if($scope.taskSelect!=all)
            if($scope.taskSelect!=input[i].task)
              keep=false;
          
          if($scope.operationSelect!=all)
            if($scope.operationSelect!=input[i].operation)
              keep=false;
          
          if($scope.areaSelect!=all)
            if($scope.areaSelect!=input[i].area)
              keep=false;
          
          if(keep)
            output.push(input[i]);
        }
      return output;
    }
    
    function
    loadSelectors(data)
    {
      $scope.operatorChoices = _.sortedUniq(_.sortBy(_.map(data,'operator')));
      $scope.operatorChoices.unshift(all);
      $scope.operatorSelect = $scope.operatorChoices[0];
      
      $scope.taskChoices = _.uniq(_.map(data,'task'));
      $scope.taskChoices.unshift(all);
      $scope.taskSelect = $scope.taskChoices[0];
      
      $scope.operationChoices = _.uniq(_.map(data,'operation'));
      $scope.operationChoices.unshift(all);
      $scope.operationSelect = $scope.operationChoices[0];
      
      $scope.areaChoices = _.uniq(_.map(data,'area'));
      $scope.areaChoices.unshift(all);
      $scope.areaSelect = $scope.areaChoices[0];
    }
    
    // // //
    // STATIC SELECTORS
    
    // OPERATORS
    
    function
    getOperators()
    {
      refreshCount(1,"getOperators");
      DbFactory.post({topic: "voice",
                      action: "operators"
                     })
        .success(getOperatorsSuccess)
        .error  (getOperatorsError);
    }
    
    function
    getOperatorsSuccess(data)
    {
      $scope.operatorChoices = [];
      for(var j=0; j < data.length; j++){
        $scope.operatorChoices.push(data[j].operatorName);
      }
      $scope.operatorChoices.unshift(all);
      $scope.operatorSelect = $scope.operatorChoices[0];
      refreshCount(-1,"getOperatorsSuccess");
    }
    
    function
    getOperatorsError(err)
    {
      console.error(err);
      refreshCount(-1,"getOperatorsError");
    }
    
    // TASKS
    
    function
    getTasks()
    {
      refreshCount(1,"getTasks");
      DbFactory.post({topic: "voice",
                      action: "tasks"
                     })
        .success(getTasksSuccess)
        .error  (getTasksError);
    }
    
    function
    getTasksSuccess(data)
    {
      $scope.taskChoices = [];
      for(var k=0; k < data.length; k++){
        $scope.taskChoices.push(data[k].task);
      }
      $scope.taskChoices.unshift(all);
      $scope.taskSelect = $scope.taskChoices[0];
      refreshCount(-1,"getTasksSuccess");
    }
    
    function
    getTasksError(err)
    {
      console.error(err);
      refreshCount(-1,"getTasksError");
    }
    
    // OPERATIONS
    
    function
    getOperations()
    {
      refreshCount(1,"getOperations");
      DbFactory.post({topic: "voice",
                      action: "operations"
                     })
        .success(getOperationsSuccess)
        .error  (getOperationsError);
    }
    
    function
    getOperationsSuccess(data)
    {
      $scope.operationChoices = [];
      for(var l=0; l < data.length; l++){
        $scope.operationChoices.push(data[l].operation);
      }
      $scope.operationChoices.unshift(all);
      $scope.operationSelect = $scope.operationChoices[0];
      refreshCount(-1,"getOperationsSuccess");
    }
    
    function
    getOperationsError(err)
    {
      console.error(err);
      refreshCount(-1,"getOperationsError");
    }
    
    // AREAS
    
    function
    getAreas()
    {
      refreshCount(1,"getAreas");
      DbFactory.post({topic: "voice",
                      action: "areas"
                     })
        .success(getAreasSuccess)
        .error  (getAreasError);
    }
    
    function
    getAreasSuccess(data)
    {
      $scope.areaChoices = [];
      for(var m=0; m < data.length; m++){
        $scope.areaChoices.push(data[m].area);
      }
      $scope.areaChoices.unshift(all);
      $scope.areaSelect = $scope.areaChoices[0];
      refreshCount(-1,"getAreasSuccess");
    }
    
    function
    getAreasError(err)
    {
      console.error(err);
      refreshCount(-1,"getAreasError");
    }
    
    
    // // // // //
    // REPORTS - TABLE
    
    var prod;
    
    function
    display()
    {
      var data = prodFilter(result);
      
      var cols = [];
      var ref = "#prod";
      
      cols.push({title: "Operator", data:"operator"});
      cols.push({title: "Task",     data:"task",
                                    class:"dt-center"});
      cols.push({title: "Operation",data:"operation",
                                    class:"dt-center"});
      cols.push({title: "Area",     data:"area",
                                    class:"dt-center"});
      cols.push({title: "Total",    data:"value",
                                    className:"dt-right"});
      cols.push({title: "Duration", data:"duration",
                                    className:"dt-right",
                                    render: $.fn.dataTable.render.number(',','.',1,'')});
      cols.push({title: "Rate",     data:"rate",
                                    className:"dt-right",
                                    render: $.fn.dataTable.render.number(',','.',1,'')});
      cols.push({title: "Standard", data:"standard",
                                    className:"dt-right"});
      cols.push({title: "",         data:"intRate",
                                    width: "15%" });
      
      if(prod){
        prod.clear();
        prod.rows.add(data);
        prod.draw(false);
      } else {
        prod = $(ref).DataTable({data: data,
                                 columns: cols,
                                 rowCallback: prodCallback,
                                 scrollY: '400px',
                                 scrollX: true,
                                 scrollCollapse: true ,
                                 paging: false,
                                 dom: 'ltBipr',
                                 buttons: ['copy','print','excel','pdf']});
      }
    }
    
    function
    prodCallback(row,data,index)
    {
      if((data.standard) && (data.standard>0)){ 
        var percent = data.intRate;
        var bar = percent;
        if(bar>100) bar=100;
        var color='red';
        if(percent>60)
          color='yellow';
        if(percent>90)
          color='green';
        var html = '<div class="w3-border w3-dark-grey">'
                 +   '<div class="w2-container w3-'+color+' w3-center"'
                 +     'style="height:24px;width:'+bar+'%">'
                 +     percent + '%'
                 +   '</div>' 
                 + '</div>';
        $('td:eq(8)',row).html(html);
      } else {
        $('td:eq(7)',row).html('');
        $('td:eq(8)',row).html('');
      }
    }
    
    
    // // // // //
    // REPORTS - DATA RETRIEVAL
    
    function
    mySQLdate(w)
    {
      var offset = (new Date()).getTimezoneOffset() * 60 * 1000;
      var iso = (new Date(w - offset)).toISOString();
      iso = iso.replace('T',' ') 
      iso = iso.substring(0,iso.lastIndexOf('.'));
      return iso;
    }
    
    function
    report()
    {
      refreshCount(1,'report');
      DbFactory.post({topic: 'prod',
                      action: 'all',
                      params: {start: mySQLdate($scope.startDate),
                               end:   mySQLdate($scope.endDate)}
                     })
        .success(prodSuccess)
        .error(prodError);
    }
    
    function
    prodSuccess(data)
    {
      $scope.reported = true;
      result = data[0];
      if(getSelectorsDynamically)
        loadSelectors(result);
      display();
      refreshCount(-1,"prodSuccess");
    }
    
    function
    prodError(err)
    {
      console.error(err);
      refreshCount(-1,"prodError");
    }
    
    function
    changeStart()
    {
      if($scope.startDate.getTime() > $scope.endDate.getTime()){
        $scope.endDate = new Date();
        $scope.endDate.setTime($scope.startDate.getTime()+1000);
      }
      $scope.reported = false;
    }
    
    function
    changeEnd()
    {
      if($scope.startDate.getTime() > $scope.endDate.getTime()){
        $scope.startDate = new Date();
        $scope.startDate.setTime($scope.endDate.getTime()-1000);
      }
      $scope.reported = false;
    }
    
    function
    setToday()
    {
      $scope.startDate = new Date();
      $scope.startDate.setHours(0);
      $scope.startDate.setMinutes(0);
      $scope.startDate.setSeconds(0);
      
      $scope.endDate = new Date();
      $scope.endDate.setHours(23);
      $scope.endDate.setMinutes(59);
      $scope.endDate.setSeconds(59);
    }
    
    
    // // // // //
    // CONFIGURE - ACTION
    
    function
    proopUpdate()
    {
      refreshCount(1,"proopUpdate");
      DbFactory.post({topic: 'prod',
                      action: 'proopdate',
                      params: {sequence: $scope.proop.sequence,
                               goal:     $scope.proop.goal}
                     })
        .success(updateSuccess)
        .error  (updateError)
    }
    
    function
    updateSuccess()
    { $timeout(finishingRefresh,800); }
    
    function
    updateError(err)
    {
      console.error(err);
      refreshCount(-1,"updateError");
    }
    
    
    // // // // //
    // CONFIGURE - TABLE
    
    var proopsTable = null;
    
    function
    buildProopsTable(greg)
    {
      var cols = [];
      var ref = "#proopsTable";
      
      cols.push({title: "Task",       data:"task"});
      cols.push({title: "Operation",  data:"operation"});
      cols.push({title: "Area",       data:"area"});
      cols.push({title: "Goal",       data:"goal",
                                      class:"dt-right"});
      
      if(proopsTable){
        proopsTable.clear();
        proopsTable.rows.add(greg);
        proopsTable.draw(false);
      } else {
        proopsTable = $(ref)
                  .DataTable({data: greg,
                              columns: cols,
                              scrollY: "550px",
                              scrollX: true,
                              scrollCollapse: true,
                              paging: false,
                              dom: 'lftipr'});
        $(ref+' tbody').on('click','tr',proopsClick);
        $timeout(proopsTable.draw,0);
      }
      refreshCount(-1,"buildProopsTable");
    }
    
    /*function
    proopsBlank()
    {
      $scope.proop = {};
    }*/
    
    function
    proopsClick()
    {
      var data = proopsTable.row(this).data();
      $scope.proop = data;
      $scope.$apply();
    }
    
    
    // // // // //
    // CONFIGURE - DATA RETRIEVAL
    
    function
    proopsRefresh()
    {
      refreshCount(1,"proopsRefresh");
      DbFactory.post({topic: 'prod',
                      action: 'proops'
                     })
        .success(proopsSuccess)
        .error  (proopsError);
    }
    
    function
    proopsSuccess(data)
    { buildProopsTable(data); }
    
    function
    proopsError(err)
    {
      console.error(err);
      refreshCount(-1,"proopsError");
    }
    
    function
    refresh()
    { proopsRefresh(); }
    
    function
    finishingRefresh()
    {
      refresh();
      refreshCount(-1,"finishingRefresh");
    }
    
    
    // // // // //
    // INIT
    
    function
    init()
    {
      Global.setTitle('Productivity');
      Global.recv('refresh',refresh,$scope);
      setToday();
      
      if(!getSelectorsDynamically){
        getOperators();
        getTasks();
        getOperations();
        getAreas();
      }
      
      refresh();
    }
    
    init();
  }
  
  function
  prodUnitaryConfig($routeProvider)
  {
    $routeProvider
      .when('/productivity',{controller:  'ProdUnitaryController',
                             templateUrl: '/ui/prodUnitary/prodUnitary.view.html'});
  }
  
}())
