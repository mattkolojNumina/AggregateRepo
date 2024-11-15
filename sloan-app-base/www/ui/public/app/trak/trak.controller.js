(function()
  {
  angular
    .module('ui')
      .controller('TrakController',trakController);
  
  angular
    .module('ui')
      .config(trakConfig);
  
  trakController.$inject = ['$scope','$timeout','Global','TrakFactory'];
  
  function
  trakController($scope,$timeout,Global,TrakFactory)
    {
    var periodic;
    
    // tabs
    $scope.selected = 0;
    $scope.refresh = refresh;
    $scope.permit = Global.permit;
    
    function
    someError(err)
      { 
      console.error(err); 
      }
    
    function
    refresh()
      {
      if($scope.selected==0)
        dpRefresh();
      if($scope.selected==1)
        rpRefresh() ;
      }
    
    // tab 0 - DP 
    
    var dpTable = null;
    
    function
    dpRefresh()
      {
      TrakFactory.post({topic: 'dp',
                        action: 'list' })
        .success(dpSuccess)
        .error  (someError); 
      }
    
    function
    dpSuccess(data)
      {
      var cols = [];
      var ref = "#dpTable";
      
      cols.push({title: "name",       data:"name"});
      cols.push({title: "Value",      data:"value",  class:"dt-right"});
      cols.push({title: "Counter",    data:"counter",class:"dt-right"});
      cols.push({title: "Description",data:"description"});
      
      if(dpTable)
        {
        dpTable.clear();
        dpTable.rows.add(data);
        dpTable.draw(false);
        } 
      else 
        {
        dpTable 
          = $(ref)
           .DataTable({data: data, 
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
                                 'pdf']});
        }
      }
    
    // tab 1 - RP 
    
    var rpTable = null;
    
    function
    rpRefresh()
      {
      TrakFactory.post({topic: 'rp',
                        action: 'list' })
        .success(rpSuccess)
        .error  (someError); 
      }
    
    function
    rpSuccess(data)
      {
      var cols = [];
      var ref = "#rpTable";
      
      cols.push({title: "name",       data:"name"});
      cols.push({title: "Value",      data:"value",  class:"dt-right"});
      cols.push({title: "Description",data:"description"});
      
      if(rpTable)
        {
        rpTable.clear();
        rpTable.rows.add(data);
        rpTable.draw(false);
        } 
      else 
        {
        rpTable 
          = $(ref)
           .DataTable({data: data, 
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
                                 'pdf']});
        }
      }

    function
    init()
      {
      Global.setTitle('Trak View');
      Global.recv('refresh',refresh,$scope);
      }
    
    init();
    }
  
  function
  trakConfig($routeProvider)
    {
    $routeProvider
      .when('/trak',{controller: 'TrakController',
                     templateUrl: '/app/trak/trak.view.html'});
    }
  
  }())
