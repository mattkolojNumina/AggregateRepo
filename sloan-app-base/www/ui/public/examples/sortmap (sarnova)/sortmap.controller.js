(function()
{
  angular
    .module('ui')
      .controller('SortmapController',sortmapController);

  angular
    .module('ui')
      .config(sortmapConfig);

  sortmapController.$inject = ['$scope','$interval','$timeout',
                               'Global','DbFactory'];
  
  function
  sortmapController($scope,$interval,$timeout,
                    Global,DbFactory)
  {
    var sortmapTable = null;
    
    $scope.edit = {};
    $scope.editNew = editNew;
    $scope.editUpdate = editUpdate;
    $scope.editDelete = editDelete;
    $scope.editDisabled = editDisabled;
    
    $scope.refresh = refresh;
    $scope.permit = Global.permit;
   
    function
    editDisabled()
    {
      if(!Global.permit('sortEdit')) return true;
//      if(!$scope.edit.carrierCode) return true;
//      if(!$scope.edit.lane) return true;
      return false;
    }
 
    function
    refresh()
    {
      Global.busyRefresh(true);
      DbFactory
        .post({topic: 'sortmap',
               action: 'all' })
        .success(sortmapSuccess)
        .error  (sortmapError); 
    }

    function
    editNew()
    { $scope.edit = {}; }
    
    function
    editUpdate()
    {
      DbFactory
        .post({topic: 'sortmap',
               action: 'update',
               params: {description:  ($scope.edit.description || ''),
                        sortLane:     ($scope.edit.sortLane    || ''),
                        enabled:      ($scope.edit.enabled     || '')
                        }})
        .success(function(){refresh();editNew();}) 
        .error(function(){console.log('update error');})
    }

    function
    editDelete()
    {
      DbFactory
        .post({topic: 'sortmap',
               action: 'delete',
               params: {carrierCode:   $scope.edit.carrierCode,
                        carrierService:$scope.edit.carrierService}})
        .success(function(){refresh();editNew();})
        .error(function(){console.log('update error');})
    }

    function
    editClick()
    {
      var data = sortmapTable.row(this).data();
      
      $scope.edit.carrier     = data.carrier;
      $scope.edit.service     = data.service;
      $scope.edit.description = data.description;
      $scope.edit.sortLane    = data.sortLane;
      $scope.edit.enabled     = data.enabled;
      
      $scope.$apply();
    }
    
    function
    sortmapSuccess(data)
    {
      var cols = [];
      var ref = "#sortmap";
      
      cols.push({title:"Carrier", data:"carrier"});            
      cols.push({title:"Service", data:"service"});
      cols.push({title:"Lane",    data:"sortLane",
                                  class:"dt-right"});
      
      if(sortmapTable)
      {
        sortmapTable.clear();
        sortmapTable.rows.add(data);
        sortmapTable.draw();
      } else {
        sortmapTable = $(ref)
          .DataTable({data: data, 
                      columns: cols,
                      order: [[0,'asc'],[1,'asc']],
                      scrollY: "425px",
                      scrollCollapse: true,
                      paging: false,
                      dom: 'lftipr'});
        $(ref+' tbody').on('click','tr',editClick);
        $timeout(function(){sortmapTable.draw()},0);
      }
      Global.busyRefresh(false);
    }

    function
    sortmapError()
    {
      console.log('error');
      Global.busyRefresh(false);
    }

    function
    init()
    {
      Global.setTitle('Sort Map');
      Global.recv('refresh',refresh,$scope);
      refresh();
    }
    
    init();
  }

  function
  sortmapConfig($routeProvider)
  {
    $routeProvider
      .when('/sortmap',{controller: 'SortmapController',
                        templateUrl: '/app/sortmap/sortmap.view.html'});
  }

}())
