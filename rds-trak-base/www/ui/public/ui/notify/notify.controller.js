(function()
{
  angular
    .module('ui')
      .controller('NotifyController',notifyController);
  
  angular
    .module('ui')
      .config(notifyConfig);
  
  notifyController.$inject = ['$scope','$interval','$timeout','_',
                              'Global','DbFactory'];
  
  function
  notifyController($scope,$interval,$timeout,_,
                   Global,DbFactory)
  {
    var periodic;
    
    // TODO: Fix the lazy deletion command
    
    // tabs
    $scope.selected = 0;
    $scope.refresh = refresh;
    $scope.permit = Global.permit;
    
    function
    refresh()
    {
      individualRefresh();
      groupRefresh();
      carrierRefresh();
    }
    
    function
    someError(err)
    { console.error(err); }
    
    // tab 0 - individual
    
    var individualTable = null;
    
    $scope.individual = {};
    $scope.individuals = [];
    $scope.individualNew = individualNew;
    $scope.individualUpdate = individualUpdate;
    $scope.individualTest = individualTest;
    $scope.individualDelete = individualDelete;
    
    function
    individualNew()
    { $scope.individual = {}; }
    
    function
    individualUpdate()
    {
      DbFactory.post({topic: 'notifyIndividuals',
                      action: 'update',
                      params: {individual: $scope.individual.individual,
                               email:      ($scope.individual.email ||''),
                               phone:      ($scope.individual.phone ||''),
                               carrier:    ($scope.individual.carrier ||'')}
                     })
        .success(individualUpdateSuccess)
        .error  (someError);
      individualNew();
    }
    
    function
    individualTest()
    {
      DbFactory.post({topic: 'notifyIndividuals',
                      action: 'test',
                      params: {individual: $scope.individual.individual,
                               email:      ($scope.individual.email ||''),
                               phone:      ($scope.individual.phone ||''),
                               carrier:    ($scope.individual.carrier ||'')}
                     })
        .success(individualUpdateSuccess)
        .error  (someError);
      individualNew();
    }
    
    function
    individualUpdateSuccess()
    {
      $timeout(refresh,1000);
    }
    
    function
    individualDelete()
    {
      DbFactory.post({topic: 'notifyIndividuals',
                      action: 'delete',
                      params: {individual: $scope.individual.individual}
                     })
        .success(individualDeleteSuccess)
        .error  (someError);
      DbFactory.post({topic: 'notifyIndividuals',
                      action: 'deleteLinks',
                      params: {individual: $scope.individual.individual}
                     })
        .error  (someError);
      individualNew();
    }
    
    function
    individualDeleteSuccess()
    {
      $timeout(refresh,1000);
    }
    
    function
    individualClick()
    {
      var data = individualTable.row(this).data();
      if(data){
        $scope.individual.individual = data.individual;
        $scope.individual.email = data.email;
        $scope.individual.phone = data.phone;
        $scope.individual.carrier = data.carrier;
        $scope.$apply();
      }
    }
    
    function
    individualRefresh()
    {
      DbFactory.post({topic: 'notifyIndividuals',
                      action: 'all'
                     })
        .success(individualSuccess)
        .error  (someError);
    }
    
    function
    individualSuccess(data)
    {
      var cols = [];
      var ref = "#individualChoose";
      
      cols.push({title: "Individual", data:"individual"});
      cols.push({title: "Email",      data:"email"});
      cols.push({title: "Phone",      data:"phone"});
      cols.push({title: "Carrier",    data:"carrier"});
      
      if(individualTable){
        individualTable.clear();
        individualTable.rows.add(data);
        individualTable.draw(false);
      } else {
        individualTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              scrollY: "550px",
                              scrollX: true,
                              scrollCollapse: true,
                              paging: false,
                              dom: 'lftipr'});
        $(ref+' tbody').on('click','tr',individualClick);
        $timeout(individualTable.draw,0);
      }
      $scope.individuals = _.map(data,'individual');
    }
    
    
    // tab 1 - group
    
    var groupTable = null;
    
    $scope.group = {};
    $scope.groupNew = groupNew;
    $scope.groupUpdate = groupUpdate;
    $scope.groupDelete = groupDelete;
    $scope.groupDisabled = groupDisabled;
    
    function
    groupDisabled()
    {
      if(!$scope.group.groupName || !$scope.group.individual) return true;
      return false;
    }
    
    function
    groupNew()
    { $scope.group = {}; }
    
    function
    groupUpdate()
    {
      DbFactory.post({topic: 'notifyGroups',
                      action: 'update',
                      params: {group:      $scope.group.groupName,
                               individual: $scope.group.individual}
                     })
        .success(groupUpdateSuccess)
        .error  (someError);
      groupNew();
    }
    
    function
    groupUpdateSuccess()
    {
      $timeout(refresh,1000);
    }
    
    function
    groupDelete()
    {
      DbFactory.post({topic: 'notifyGroups',
                      action: 'delete',
                      params: {group:      $scope.group.groupName,
                               individual: $scope.group.individual}
                     })
        .success(groupDeleteSuccess)
        .error  (someError);
      groupNew();
    }
    
    function
    groupDeleteSuccess()
    {
      $timeout(refresh,1000);
    }
    
    function
    groupClick()
    {
      var data = groupTable.row(this).data();
      if(data){
        $scope.group.groupName = data.groupName;
        $scope.group.individual = data.individual;
        $scope.$apply();
      }
    }
    
    function
    groupRefresh()
    {
      DbFactory.post({topic: 'notifyGroups',
                      action: 'all'
                     })
        .success(groupSuccess)
        .error  (someError);
    }
    
    function
    groupSuccess(data)
    {
      var cols = [];
      var ref = "#groupChoose";
      
      cols.push({title: "Group",      data:"groupName"});
      cols.push({title: "Individual", data:"individual"});
      
      if(groupTable){
        groupTable.clear();
        groupTable.rows.add(data);
        groupTable.draw(false);
      } else {
        groupTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              scrollY: "550px",
                              scrollX: true,
                              scrollCollapse: true,
                              paging: false,
                              dom: 'lftipr'});
        $(ref+' tbody').on('click','tr',groupClick);
        $timeout(groupTable.draw,0);
      }
    }
    
    // tab 2 - carrier
    
    var carrierTable = null;
    
    $scope.carrier = {};
    $scope.carriers = [];
    $scope.carrierNew = carrierNew;
    $scope.carrierUpdate = carrierUpdate;
    $scope.carrierDelete = carrierDelete;
    $scope.carrierDisabled = carrierDisabled;
    
    function
    carrierDisabled()
    { return !$scope.carrier.carrier; }
    
    function
    carrierNew()
    { $scope.carrier = {}; }
    
    function
    carrierUpdate()
    {
      DbFactory.post({topic: 'notifyCarrierss',
                      action: 'update',
                      params: {carrier: $scope.carrier.carrier,
                               domain:  $scope.carrier.domain}
                     })
        .success(carrierUpdateSuccess)
        .error  (someError);
      carrierNew();
    }
    
    function
    carrierUpdateSuccess()
    {
      $timeout(refresh,1000);
    }
    
    function
    carrierDelete()
    {
      DbFactory.post({topic: 'notifyCarriers',
                      action: 'delete',
                      params: {carrier: $scope.carrier.carrier}
                     })
        .success(carrierDeleteSuccess)
        .error  (someError);
      carrierNew();
    }
    
    function
    carrierDeleteSuccess()
    {
      $timeout(refresh,1000);
    }
    
    function
    carrierClick()
    {
      var data = carrierTable.row(this).data();
      $scope.carrier.carrier = data.carrier;
      $scope.carrier.domain = data.domain;
      $scope.$apply();
    }
    
    function
    carrierRefresh()
    {
      DbFactory.post({topic: 'notifyCarriers',
                      action: 'all'
                     })
        .success(carrierSuccess)
        .error  (someError);
    }
    
    function
    carrierSuccess(data)
    {
      var cols = [];
      var ref = "#carrierChoose";
      
      cols.push({title: "Carrier", data:"carrier"});
      cols.push({title: "Domain",  data:"domain"});
      
      if(carrierTable){
        carrierTable.clear();
        carrierTable.rows.add(data);
        carrierTable.draw(false);
      } else {
        carrierTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              scrollY: "550px",
                              scrollX: true,
                              scrollCollapse: true,
                              paging: false,
                              dom: 'lftipr'});
        $(ref+' tbody').on('click','tr',carrierClick);
        $timeout(carrierTable.draw,0);
      }
      $scope.carriers = _.map(data,"carrier");
      $scope.carriers.unshift('');
    }
    
    function
    init()
    {
      Global.setTitle('Notifications');
      Global.recv('refresh',refresh,$scope);
    }
    
    init();
  }
  
  function
  notifyConfig($routeProvider)
  {
    $routeProvider
      .when('/notify',{controller: 'NotifyController',
                       templateUrl: '/ui/notify/notify.view.html'});
  }
  
}())