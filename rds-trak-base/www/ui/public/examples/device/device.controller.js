(
function()
  {
  angular
    .module('ui')
      .controller('DeviceController',deviceController) ;

  angular
    .module('ui')
      .config(deviceConfig) ;

  deviceController.$inject = ['$scope','$interval','$timeout',
                              'Global','DbFactory'] ;
  
  function
  deviceController($scope,$interval,$timeout,
                   Global,DbFactory)
    {
    var periodic ;

    $scope.refresh = refresh ;
    $scope.permit = Global.permit ;
 
    var device = null ;

    function
    stampRender(data,type,full,meta)
      {
      if(type!='display')
        return data ;

      if(data==null)
        return '' ;
    
      var date = new Date(data) ;
      var today = new Date() ;
      if(today.toDateString()==date.toDateString())
        return date.toLocaleTimeString() ;

      return date.toLocaleString() ;
      }

    function
    refresh()
      {
      DbFactory
        .post({topic: 'device', action: 'all'}) 
          .success(deviceSuccess)
          .error(function(){console.log('device error')}) ; 
      }

    function
    deviceSuccess(data)
      {
      var cols = [] ;
      var ref = "#device" ;

      cols.push({title:"Name",        data:"name"}) ;
      cols.push({title:"Controller?", data:"is_controller"}) ;
      cols.push({title:"Type",        data:"device_type"}) ;
      cols.push({title:"Comment",     data:"comment"}) ;
      cols.push({title:"Last Message",data:"last_msg"}) ;
      cols.push({title:"Status",      data:"status"}) ;
      cols.push({title:"Service",     data:"service"}) ;
      cols.push({title:"Stamp",       data:"stamp",
                                      render:stampRender}) ;

      if(device)
        {
        device.clear() ;
        device.rows.add(data) ;
        device.draw() ;
        }
      else
        {
        device = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              rowCallback: deviceCallback,
                              order: [[0,'asc']],
                              scrollY: "500px",
                              scrollCollapse: true,
                              paging: false,
                              dom: 'lftBipr',
                              buttons: ['copy','print','excel','pdf']}) ;
        $(ref+' tbody').on('click','tr',deviceClick) ;
        $timeout(function(){
          device.draw() ;
          },0) ;
        }
      }

    function
    deviceCallback(row,data,index)
      {
      }   
   
    function
    deviceClick()
      {
      }
 
    function
    init()
      {
      Global.setTitle('Devices') ;
      Global.recv('refresh',refresh,$scope) ;
      refresh() ;
      }

    init() ;
    }

  function
  deviceConfig($routeProvider)
    {
    $routeProvider
      .when('/device',{controller: 'DeviceController',
                     templateUrl: '/app/device/device.view.html'}) ;
    }

  }() 
) 
  
