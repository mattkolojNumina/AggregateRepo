(function()
{
  angular
    .module('ui')
      .controller('ProdController',prodController);

  angular
    .module('ui')
      .config(prodConfig);

  prodController.$inject = ['$scope','_','Global','DbFactory'];
  
  function
  prodController($scope,_,Global,DbFactory)
  {
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
    
    var prod;
    var result;
    
    var all = '-- all --';
    
    function
    prodFilter(input)
    {
      var output = [];
      for(var i=0; i<input.length; i++){
        var keep = true;
        
        if($scope.operatorSelect!=all &&
           $scope.operatorSelect!=input[i].operator)
            keep=false;
        
        if($scope.taskSelect!=all &&
           $scope.taskSelect!=input[i].task)
            keep=false;
        
        if($scope.operationSelect!=all &&
           $scope.operationSelect!=input[i].operation)
            keep=false;
        
        if($scope.areaSelect!=all &&
           $scope.areaSelect!=input[i].area)
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
    
    function
    display() 
    {
      var data = prodFilter(result);
      
      var cols = [];
      var ref = "#prod";
      
      cols.push({title: "Operator", data:"operator"});
      cols.push({title: "Task",     data:"task"});
      cols.push({title: "Operation",data:"operation"});
      cols.push({title: "Area",     data:"area"});
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
        prod.draw();
      } else {
        prod = $(ref).DataTable({data: data,
                                 columns: cols,
                                 rowCallback: prodCallback,
                                 scrollY: '400px',
                                 scrollCollapse: true ,
                                 paging: false,
                                 dom: 'ltBipr',
                                 buttons: ['copy','print','excel','pdf']});
      }
	  Global.busyRefresh(false);
    }
    
    function
    prodSuccess(data)
    {
      $scope.reported = true;
      result = data[0];
      loadSelectors(result);
      display(); 
    }
    
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
	  Global.busyRefresh(true);
      DbFactory.post({topic: 'prod',
                      action: 'all',
                      params: {start: mySQLdate($scope.startDate),
                               end: mySQLdate($scope.endDate)}
                     })
        .success(prodSuccess)
        .error(function(err){
			console.log(err);
			Global.busyRefresh(false);
		});
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
    
    function
    refresh()
    { }
    
    function
    init()
    {
      Global.setTitle('Productivity');
      Global.recv('refresh',refresh,$scope);
      setToday();
      
      refresh();
    }
    
    init();
  }
  
  function
  prodConfig($routeProvider)
  {
    $routeProvider
      .when('/prod',{controller:  'ProdController',
                     templateUrl: '/ui/prod/prod.view.html'});
  }

}())