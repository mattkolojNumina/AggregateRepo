(function()
{
  angular
    .module('ui')
      .controller('SystemController',systemController);
  
  angular
    .module('ui')
      .config(systemConfig);
  
  systemController.$inject = ['$scope','$timeout','$mdDialog','_',
                              'Global','DbFactory'];
  
  function
  systemController($scope,$timeout,$mdDialog,_,
                   Global,DbFactory)
  {
    var periodic;
    
    // tabs
    $scope.selected = 0;
    $scope.refresh = refresh;
    $scope.permit = Global.permit;
    
    // hosts
    $scope.host = '';
    $scope.hosts = [];
    
    // buttons
    $scope.reboot = reboot;
    $scope.shutdown = shutdown;
    $scope.command = '';
    $scope.execute = execute;
    
    function
    someError(err)
    { console.error(err); }
    
    function
    reboot(ev)
    {
      var dialog = $mdDialog.confirm()
        .title('Reboot '+$scope.host)
        .textContent('Are you sure you want to reboot '+$scope.host+'?')
        .ariaLabel('Reboot '+$scope.host) 
        .targetEvent(ev)
        .ok('Reboot')
        .cancel('Cancel');
      
      $mdDialog
        .show(dialog)
        .then(function(){
          //console.log('reboot '+$scope.host);
          DbFactory.post({topic: 'system',
                          action: 'execute',
                          params: {host: $scope.host,
                                   command: 'sudo /sbin/reboot'}
                         })
            .success(executeSuccess)
            .error  (someError);
      });
    }
    
    function
    executeSuccess()
    { $timeout(refresh,1000); }
    
    function
    shutdown(ev)
    {
      var dialog = $mdDialog.confirm()
        .title('Shut down '+$scope.host)
        .textContent('Are you sure you want to shut down '+$scope.host+'?')
        .ariaLabel('Shut down '+$scope.host) 
        .targetEvent(ev)
        .ok('Shut down')
        .cancel('Cancel');
      
      $mdDialog
        .show(dialog)
        .then(function(){
          //console.log('poweroff '+$scope.host);
          DbFactory.post({topic: 'system',
                          action: 'execute',
                          params: {host: $scope.host,
                                   command: 'sudo /sbin/poweroff'}
                         })
            .success(executeSuccess)
            .error  (someError);
      });
    }
    
    function
    execute()
    {
      //console.log('command '+$scope.command);
      DbFactory.post({topic: 'system',
                      action: 'execute',
                      params: {host: $scope.host,
                               command: $scope.command}
                     })
        .success(executeSuccess)
        .error  (someError);
      $scope.command = ''; 
    }
    
    function
    refresh()
    {
      if($scope.selected==0)
        systemRefresh();
      if($scope.selected==1)
        commandRefresh();
    }
    
    function
    dateRender(data,type,full,meta)
    {
      if(data){
        if(type=='display'){
          var date = new Date(data);
          return date.toLocaleString();
        } else {
          var tzoffset = (new Date()).getTimezoneOffset()*60000;
          var localISOTime = (new Date(new Date(data) - tzoffset)).toISOString().slice(0,-1);
          return localISOTime;
        }
      } else {
        return '';
      }
    }
    
    
    // tab 0 - system
    
    var systemTable = null;
    
    function
    systemRefresh()
    {
      Global.busyRefresh(true);
      DbFactory.post({topic: 'system',
                      action: 'all'
                     })
        .success(systemSuccess)
        .error  (systemError); 
    }
    
    function
    systemSuccess(data)
    {
      var cols = [];
      var ref = "#systemTable";
      
      cols.push({title: "Host",        data:"host"});
      cols.push({title: "Ordinal",     data:"ordinal",
                                       class:"dt-right"});
      cols.push({title: "Description", data:"displayName"});
      cols.push({title: "Mode",        data:"mode"});
      cols.push({title: "PID",         data:"pid",
                                       class:"dt-right"});
      cols.push({title: "Count",       data:"count",
                                       class:"dt-right"});
      cols.push({title: "Last Start",  data:"lastStart",
                                       render: dateRender});
      cols.push({title: "Operation",   data:null,
                                       defaultContent: "",
                                       class:"dt-center"});
      
      if(systemTable){
        systemTable.clear();
        systemTable.rows.add(data);
        systemTable.draw(false);
      } else {
        systemTable = $(ref)
                  .DataTable({data: data, 
                              columns: cols,
                              rowCallback: systemCallback,
                              order: [[0,'asc'],[1,'asc']],
                              scrollY: "550px",
                              scrollX: true,
                              scrollCollapse: true,
                              paging: false,
                              dom: 'lftBipr',
                              buttons: ['copy',
                                        'print',
                                        {extend:'excel',exportOptions:{orthogonal:'exportExcel'}},
                                        'pdf']});
        $(ref+' tbody').on('click','button',systemClick);
      }
      Global.busyRefresh(false);
    }
    
    function
    systemClick()
    {
      var row  = $(this).parents('tr');
      var data = systemTable.row(row).data();
      //console.log('restart host '+data.host+" ordinal "+data.ordinal); 
      DbFactory.post({topic: 'system',
                      action: 'restart',
                      params: {host: data.host,
                               ordinal: data.ordinal}
                     })
        .success(executeSuccess)
        .error  (someError);
    }
    
    function
    systemCallback(row,data,index)
    {
      if(data.pid>=0)
        $(row).css('font-weight','bold');
      if(data.throttled=='yes')
        $(row).css('background-color','pink');
      else if(data.elapsed<300)
        $(row).css('background-color','yellow');
    
      if(data.mode=='daemon'){
        if(Global.permit('systemEdit'))
          $("td:eq(7)",row).html('<button class="tableButton" style="font-weight: normal">Restart</button>');
        else
          $("td:eq(7)",row).html('<button class="tableButton" style="font-weight: normal" disabled>Restart</button>');
      }
    }
    
    function
    systemError(err)
    {
      console.error(err);
      Global.busyRefresh(false);
    }
    
    
    // tab 1 - command
    
    var commandTable = null;
    
    function
    commandRefresh()
    {
      Global.busyRefresh(true);
      DbFactory.post({topic: 'system',
                      action: 'hosts'
                     })
        .success(hostSuccess)
        .error  (someError); 
      DbFactory.post({topic: 'execute', 
                      action: 'all'
                     })
        .success(commandSuccess)
        .error  (commandError); 
    }
    
    function
    commandSuccess(data)
    {
      var cols = [];
      var ref = "#commandTable";
      
      cols.push({title: "Sequence",    data:"seq",
                                       className: "dt-body-right"});
      cols.push({title: "Host",        data:"host"});
      cols.push({title: "Command",     data:"command"});
      cols.push({title: "Completed",   data:"completed",
                                       class:"dt-center"});
      cols.push({title: "Stamp",       data:"stamp",
                                       render:dateRender});
      
      if(commandTable){
        commandTable.clear();
        commandTable.rows.add(data);
        commandTable.draw(false);
      } else {
        commandTable = $(ref)
                  .DataTable({data: data, 
                              columns: cols,
                              order: [[0,'desc']],
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
      Global.busyRefresh(false);
    }
    
    function
    commandError(err)
    {
      console.error(err);
      Global.busyRefresh(false);
    }
    
    function
    hostSuccess(data)
    {
      $scope.hosts = _.map(data,'host');
      if(_.indexOf($scope.hosts,$scope.host)==-1 && $scope.hosts[0])
        $scope.host = $scope.hosts[0];
    }
    
    function
    init()
    {
      Global.setTitle('System Processes');
      Global.recv('refresh',refresh,$scope);
    }
    
    init();
  }
  
  function
  systemConfig($routeProvider)
  {
    $routeProvider
      .when('/system',{controller: 'SystemController',
                       templateUrl: '/ui/system/system.view.html'});
  }
  
}())