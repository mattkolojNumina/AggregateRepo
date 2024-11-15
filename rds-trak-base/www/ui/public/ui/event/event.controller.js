(function()
{
  angular
    .module('ui')
      .controller('EventController',eventController);
  
  angular
    .module('ui')
      .config(eventConfig);
  
  eventController.$inject = ['$scope','$interval','$timeout','$mdDialog',
                             'Global','DbFactory'];
  
  function
  eventController($scope,$interval,$timeout,$mdDialog,
                  Global,DbFactory)
  {
    var periodic;
    
    // tabs
    $scope.selected = 0;
    $scope.refresh = refresh;
    $scope.permit = Global.permit;
    $scope.info = info;
    
    function
    info(ev,suggest)
    {
      $mdDialog.show(
        $mdDialog.alert()
          .clickOutsideToClose(true)
          .title('Suggested Action: ')
          .textContent(suggest)
          .ariaLabel('Suggested Action')
          .ok('OK')
          .targetEvent(ev)
      );
    }
    
    function
    refresh()
    {
      if($scope.selected==0)
        currentRefresh();
      
      if($scope.selected==1)
        historyRefresh();
      
      if($scope.selected==2)
        reportRefresh();
      
      if($scope.selected==3)
        chooseRefresh();
    }
    
    
    // utils
    
    function
    durationRender(data,type,full,meta)
    {
      var show = '';
      var sec = data;
      var field;
     
      if(type!='display')
        return data;
      
      field = sec % 60;
      sec = Math.floor(sec/60);
      
      show = field + 's';
      
      if(sec>0){
        field = sec % 60;
        sec = Math.floor(sec/60);
        show = field + 'm ' + show; 
      }
      
      if(sec>0){
        field = sec % 24;
        sec = Math.floor(sec/24);
        show = field + 'h ' + show;
      }
      
      if(sec>0){
        field = sec;
        show = field + 'd ' + show; 
      }
      
      return show;
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
          var localTime = new Date(data) - tzoffset;
          if(!localTime) return '';
          var localISOTime = new Date(localTime).toISOString().slice(0,-1);
          return localISOTime;
        }
      } else {
        return '';
      }
    }
    
    
    
    // tab 0 - current
    
    $scope.current = [];
    
    function
    currentRefresh()
    {
      if($scope.selected==0){
        Global.busyRefresh(true);
        DbFactory.post({topic: 'event',
                        action: 'current'
                       })
          .success(currentSuccess)
          .error(currentError);
      }
    }
    
    function
    currentSuccess(data)
    {
      for(var i=0; i<data.length; i++){
        data[i].class='event-info';
        if(data[i].severity==0)
          data[i].class='event-alarm';
        if(data[i].severity==1)
          data[i].class='event-alert';
        if(data[i].severity==2)
          data[i].class='event-trace';
        data[i].duration = durationRender(data[i].duration,'display','','');
        if(data[i].suggestedAction)
          {
          var s = data[i].suggestedAction.data ;
          var suggest = ""  ;
          for(var j=0 ; j<s.length ; j++)
            suggest += String.fromCharCode(s[j]) ;
          data[i].suggestedAction = suggest ;
          }

      }
      $scope.current = data;
      Global.busyRefresh(false);
    }
    
    function
    currentError(err)
    { console.error(err); }
    
    
    // tab 1 - history
   
    var historyTable = null;
    
    function
    historyRefresh()
    {
      Global.busyRefresh(true);
      DbFactory.post({topic: 'event',
                      action: 'recent',
                      params: {limit: 100}
                     })
        .success(historySuccess)
        .error(historyError);
    }
    
    function
    historyCallback(row,data,index)
    {
      if(data.severity==0)
        $(row).css('background-color','#ffc0c0');
      else if (data.severity==1)
        $(row).css('background-color','#ffcc99');
      else if (data.severity==2)
        $(row).css('background-color','#ffff80');
      else
        $(row).css('background-color','#d0d0ff');
    }
    
    function
    historySuccess(data)
    {
      var cols = [];
      var ref = "#eventHistory";
      
      cols.push({title: "Code",        data:"code"});
      cols.push({title: "Description", data:"description",
                                       width: "80%"});
      cols.push({title: "Severity",    data:"severity",
                                       class:"dt-center"});
      cols.push({title: "Start",       data:"start",
                                       type:"date",
                                       render: dateRender,
                                       className: 'dt-body-nowrap'});
      cols.push({title: "Duration",    data:"duration",
                                       render: durationRender,
                                       className: 'dt-body-right dt-body-nowrap'});
      
      if(historyTable){
        historyTable.clear();
        historyTable.rows.add(data);
        historyTable.draw(false);
      } else {
        historyTable = $(ref)
          .DataTable({data: data,
                      columns: cols,
                      rowCallback: historyCallback,
                      order: [[3,'desc']],
                      scrollY: "550px",
                      scrollX: true,
                      scrollCollapse: true,
                      paging: true,
                      pageLength: 50,
                      dom: 'lftBipr',
                      buttons: ['copy',
                                'print',
                                {extend:'excel',exportOptions:{orthogonal:'exportExcel'}},
                                'pdf']});
      }
      Global.busyRefresh(false);
    }
    
    function
    historyError(err)
    {
      console.error(err);
      Global.busyRefresh(false);
    }
    
    
    // tab 2 - report
    
    var reportTable = null;
    
    $scope.reportStart = new Date();
    $scope.reportEnd = new Date();
    $scope.reported = false;
    $scope.changeStart = changeStart;
    $scope.changeEnd   = changeEnd;
    $scope.reportRefresh = reportRefresh;

    function
    setToday()
    {
      $scope.reportStart = new Date();
      $scope.reportStart.setHours(0);
      $scope.reportStart.setMinutes(0);
      $scope.reportStart.setSeconds(0);

      $scope.reportEnd = new Date();
      $scope.reportEnd.setHours(23);
      $scope.reportEnd.setMinutes(59);
      $scope.reportEnd.setSeconds(59);
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
    changeStart()
    { $scope.reported = false; }
    
    function
    changeEnd()
    { $scope.reported = false; }
    
    function
    reportRefresh()
    {
      Global.busyRefresh(true);
      DbFactory.post({topic:  'event',
                      action: 'report',
                      params: {start: mySQLdate($scope.reportStart),
                               end:   mySQLdate($scope.reportEnd)}
                     })
        .success(reportSuccess)
        .error(reportError);
    }
    
    function
    reportSuccess(data)
    {
      var cols = [];
      var ref = "#eventReport";
      
      cols.push({title: "Code",        data:"code"});
      cols.push({title: "Description", data:"description"});
      cols.push({title: "Severity",    data:"severity",
                                       type:"num",
                                       className:"dt-body-right"});
      cols.push({title: "Quantity",    data:"totalQuantity",
                                       className:"dt-body-right",
                                       type:"num"});
      cols.push({title: "Duration",    data:"totalDuration", 
                                       className:"dt-body-right",
                                       render: durationRender});

      if(reportTable){
        reportTable.clear();
        reportTable.rows.add(data);
        reportTable.draw(false);
      } else {
        reportTable = $(ref)
          .DataTable({data: data, 
                      columns: cols,
                      rowCallback: historyCallback,
                      order: [[2,'asc'],[3,'desc']],
                      scrollY: "400px",
                      scrollX: true,
                      scrollCollapse: true,
                      paging: false,
                      dom: 'lftBipr',
                      buttons: ['copy','print','excel','pdf']});
      }
      $scope.reported = true;
      Global.busyRefresh(false);
    }
    
    function
    reportError(err)
    {
      console.error(err);
      Global.busyRefresh(false);
    }
    
    // tab 3 - choose and edit
    
    var chooseTable = null;
    
    $scope.edit = {};
    $scope.editDisable = editDisable;
    $scope.editNew = editNew;
    $scope.editUpdate = editUpdate;
    $scope.editDelete = editDelete;
    $scope.notifies = [];
    
    function
    editDisable()
    { return !$scope.edit.code; }
    
    function
    editNew()
    { $scope.edit = {}; }
    
    function
    editUpdate()
    {
      Global.busyRefresh(true);
      DbFactory.post({topic: 'event',
                      action: 'update',
                      params: {code:        $scope.edit.code,
                               description: $scope.edit.description,
                               severity:    $scope.edit.severity,
                               notify:      ($scope.edit.notify || ''),
                               suggestedAction: ($scope.edit.suggestedAction||'')}
                     }) 
        .success(updateSuccess)
        .error(updateError)
    }

    function
    updateSuccess()
    {
      //console.log('update success');
      $timeout(refresh,1000);
    }

    function
    updateError(err)
    {
      console.error(err);
      Global.busyRefresh(false);
    }

    function
    editDelete()
    {
      Global.busyRefresh(true);
      DbFactory.post({topic: 'event',
                      action: 'delete',
                      params: {code: $scope.edit.code}
                     })
        .success(deleteSuccess)
        .error(deleteError)
      editNew();
    }

    function
    deleteSuccess()
    {
      //console.log('delete success');
      $timeout(refresh,1000);
    }

    function
    deleteError(err)
    {
      console.error(err);
      Global.busyRefresh(false);
    }

    function
    editClick()
    {
      var data = chooseTable.row(this).data();
      $scope.edit.code = data.code;
      $scope.edit.description = data.description;
      $scope.edit.severity = data.severity;
      $scope.edit.notify = data.notify;
      $scope.edit.suggestedAction = data.suggestedAction;
      $scope.$apply();
    }

    function
    chooseRefresh()
    {
      Global.busyRefresh(true);
      DbFactory.post({topic: 'event',
                      action: 'all'
                     })
        .success(chooseSuccess)
        .error(chooseError); 
      DbFactory.post({topic: 'notify',
                      action: 'list'
                     })
        .success(notifySuccess)
        .error(function(err){console.error(err);})
    }

    function
    notifySuccess(data)
    {
      $scope.notifies = _.map(data,'individual');
      $scope.notifies.unshift('');
      //console.log($scope.notifies);
    }

    function
    chooseSuccess(data)
    {
      var cols = [];
      var ref = "#eventChoose";

      cols.push({title: "Code",        data:"code"});
      cols.push({title: "Description", data:"description"});
      cols.push({title: "Severity",    data:"severity",
                                       visible: true,
                                       searchable: false });
      cols.push({title: "Notify",      data:"notify"});

      if(chooseTable){
        chooseTable.clear();
        chooseTable.rows.add(data);
        chooseTable.draw(false);
      } else {
        chooseTable = $(ref)
                  .DataTable({data: data, 
                              columns: cols,
                              scrollY: "425px",
                              scrollX: true,
                              scrollCollapse: true,
                              paging: false,
                              dom: 'lftipr'});
        $(ref+' tbody').on('click','tr',editClick);     
      }
      Global.busyRefresh(false);
    }

    function
    chooseError(err)
    {
      console.error(err);
      Global.busyRefresh(false);
    }

    function
    init()
    {
      Global.setTitle('Events');
      Global.recv('refresh',refresh,$scope);
      periodic = $interval(currentRefresh,5000);
      setToday();
    }

    $scope.$on('$destroy',function(){
      $interval.cancel(periodic);
    });

    init();
  }

  function
  eventConfig($routeProvider)
  {
    $routeProvider
      .when('/event',{controller: 'EventController',
                      templateUrl: '/ui/event/event.view.html'});
  }

}()) 
