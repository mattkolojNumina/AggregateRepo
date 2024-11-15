(function()
{
  angular
    .module('ui')
      .controller('VictoryController',victoryController);
  
  angular
    .module('ui')
      .config(victoryConfig);
  
  victoryController.$inject = ['$scope','$interval','Global','DbFactory','$timeout','$routeParams','$mdDialog'];
  
  function
  victoryController($scope,$interval,Global,DbFactory,$timeout,$routeParams,$mdDialog)
  {
    var creationDelay = 1000; // how long to wait for backend program - speed may vary by system
    
    $scope.selected = 0;
    $scope.refresh = refresh;
    $scope.id = '';
    $scope.detail = {};
    $scope.lookup  = '';
    $scope.doLookup = doLookup;
    $scope.permit = Global.permit;
    $scope.newOperator = newOperator;
    $scope.editOperator = editOperator;
    $scope.deleteOperator = deleteOperator;
    $scope.logoffOperator = logoffOperator;
    $scope.goEdit = function(){$scope.selected = 2;}
    $scope.getEditValues = getEditValues;
    $scope.deleteButton = 'preliminary';
    $scope.beginDelete = beginDelete;
    $scope.endDelete = endDelete;
    $scope.displayTimeRange = displayTimeRange;
    $scope.sendMessage = sendMessage;

    $scope.gangingLevels = [1,2,3,4,5];

    $scope.message = {};
    
    $scope.opIsUnchanged = true;
    $scope.changeOp = function(){$scope.opIsUnchanged = false;}
    $scope.changeGangingLevel = function(){$scope.opIsUnchanged = false;}

    $scope.opData = {};
    var opDataStr = {}; //same as opData but in strings
    
    var configsTrue = [];
    var configsMod = [];
    
    var periodic;
    var operator;
    var devices;
    var talk;
    
    // // // // //
    // OPERATOR COMMANDS
    
    function
    newOperator(opid)
    {
      DbFactory.post({topic: 'victory',
                      action: 'checkId',
                      params: {id: opid}
                     })
      .then(function(op){
        if(op.data[0].num>0) throw 'id conflict';
      })
      .then(function(){
        DbFactory.post({topic: 'victory',
                        action: 'newOpPO',
                        params: {id: opid,
                                 name: (opid)}
                       });
      })
      .then(function(){
        DbFactory.post({topic: 'victory',
                        action: 'newOpVC',
                        params: {id: opid}
                       });
      })
      .then(function(){
        alert('Operator '+opid+' created');
        // This delay allows the backend program to set up the new user
        $timeout(function(){
          getEditValues(opid);
        },creationDelay);
      })
      .catch(function(err){
        //no time to change the existing structure,
        //so still calling it an 'err' even though it's really now just a lookup
        if(err=='id conflict'){
          $timeout(function(){
            getEditValues(opid);
          },creationDelay);
        } else {
          console.log(err);
        }
      });
    }

    function
    logoffOperator(opid)
    {
      DbFactory.post({topic: 'victory',
                      action: 'logoffPO',
                      params: {id: opid}
                     })
      .then(function(){
        DbFactory.post({topic: 'victory',
                        action: 'logoffVD',
                        params: {id: opid}
                       })
      })
      .then(function(){
        DbFactory.post({topic: 'victory',
                        action: 'logoffPOL',
                        params: {id: opid}
                       })
      })
      .then(function(){
        DbFactory.post({topic: 'victory',
                        action: 'logoffApp',
                        params: {id: opid}
                       })
      })
      .then(function(){
        alert('Operator '+opid+' logged off');
        refresh();
      })
      .catch(function(err){
        console.error(err);
        refresh();
      });
    }
        
    function
    editOperator()
    {
      convertInputToSend();
      $timeout(function(){
        sendInput();
        $scope.opIsUnchanged = true;
      },500);
    }
    
    function
    beginDelete()
    {
      $scope.deleteButton = 'really';
      $timeout(function(){$scope.deleteButton = 'reallyReally';},100);
      refresh();
    }
    
    function
    endDelete()
    {
      $scope.deleteButton = 'preliminary';
      refresh();

    }
    
    function
    deleteOperator(opid)
    {
      DbFactory.post({
           topic:  "victory",
           action: "delete",
           params: {opid: opid}
      });
      endDelete();
      $scope.opData = {};
      $scope.thisOpID = '';
      $scope.selected=0;
      refreshVictoryOperators();
    }
    
    // // // // //
    // Time range

    function
      toMySQLDateTime(date) {
      return date.getFullYear() + '-' +
        ('00' + (date.getMonth() + 1)).slice(-2) + '-' +
        ('00' + date.getDate()).slice(-2) + ' ' +
        ('00' + date.getHours()).slice(-2) + ':' +
        ('00' + date.getMinutes()).slice(-2) + ':' +
        ('00' + date.getSeconds()).slice(-2);
    }

    function
      displayTimeRange() {
      $scope.d1 = new Date($scope.date1);
      $scope.d2 = new Date($scope.date2);

        DbFactory.post({
          topic: 'victory',
          action: 'victoryTimeRange',
          params: {
            id: $scope.id,
            start: toMySQLDateTime($scope.d1),
            end: toMySQLDateTime($scope.d2)
          }
        })
          .success(totalValSuccess)
          .error(someError);
    
    }

    function totalValSuccess(data) {
      buildTalkTable(data);
    }

    function
      someError(err) {
      console.error(err);
    }
	
	function setToday(){
		$scope.date1 = new Date();
		$scope.date1.setHours(0);
		$scope.date1.setMinutes(0);
		$scope.date1.setSeconds(0);

		$scope.date2 = new Date();
		$scope.date2.setHours(23);
		$scope.date2.setMinutes(59);
		$scope.date2.setSeconds(59);
	}

    // // //
    // HELPER FUNCTIONS

    function
    setOpName(opid,value)
    {
      DbFactory.post({topic: 'victory',
                      action: 'setName',
                      params: {id: opid,
                               value: value}
                     })
        .success(setOpConfigSuccess)
        .catch  (setOpConfigError);
    }
   
    function
    setOpConfig(opid,name,value)
    {
      DbFactory.post({topic: 'victory',
                      action: 'setConfig',
                      params: {id: opid,
                               name: name,
                               value: value}
                     })
        .success(setOpConfigSuccess)
        .catch  (setOpConfigError);
    }
    
    function
    setOpConfigSuccess()
    { }
    
    function
    setOpConfigError(err)
    {
      console.log(err);
    }
	
    function
    setOpDetail(opid,upcScanProbability,randomQc,gangingLevel,allowReverse)
    {
      DbFactory.post({topic: 'victory',
                      action: 'setDetail',
                      params: {id: opid,
                               upcScanProbability: upcScanProbability,
                               randomQc: randomQc,
                               gangingLevel: gangingLevel,
                               allowReverse: allowReverse}
                     })
        .success(setOpConfigSuccess)
        .catch  (setOpConfigError);
    }	
    
    function
    getOpConfig(opid,name)
    {
      function
      callback(value)
      { opDataStr[name] = value[0]?(value[0].value):''; }
      
      return DbFactory.post({topic: 'victory',
                             action: 'getConfig',
                             params: {id: opid,
                                      name: name}
                            })
        .success(callback)
        .error  (function(err){console.log(err);});
    }
	
    function
    getOpDetail(opid)
    {
      function
      callback(value) {
        opDataStr.upcScanProbability = value[0].upcScanProbability;
        opDataStr.randomQc           = value[0].randomQc;
        opDataStr.gangingLevel       = value[0].gangingLevel;
        opDataStr.allowReverse       = value[0].allowReverse;
      }
      
      return DbFactory.post({topic: 'victory',
                             action: 'detail',
                             params: {id: opid}
                            })
        .success(callback)
        .error  (function(err){console.log(err);});
    }	
    
    function
    blankOpData()
    { $scope.opData = {}; }
    
    // // //
    // GET / SEND
    
    function
    getLanguages()
    {
      DbFactory.post({topic: 'victory',
                      action: 'getLanguages'
                     })
        .success(langSuccess)
        .error  (langError);
    }
    
    function
    langSuccess(data)
    {
      const langs = [];
      for(i = 0; i < Object.keys(data).length; i++) {
        langs.push({language: data[0].COLUMN_NAME})
      }
      $scope.langChoices = langs;
    }
    
    function
    langError(err)
    {
      console.log(err);
    }
    
    function
    getEditValues(opid)
    {
      $scope.thisOpID = opid;
      $scope.opIsUnchanged = true;
      
             getOpConfig(opid,'sensitivity')      .then(function(){
      return getOpConfig(opid,'volume');})        .then(function(){
      return getOpConfig(opid,'rate');})          .then(function(){
      return getOpConfig(opid,'pitch');})         .then(function(){
      return getOpConfig(opid,'level');})         .then(function(){
      return getOpConfig(opid,'language');})      .then(function(){
	    return getOpDetail(opid);})      .then(function(){
      convertInputForForm();})
  
      $scope.opData.userName = opid
      $scope.selected=2;
      refresh();
    }
    
    function
    convertInputForForm()
    {
      //console.log("converting input");
      $scope.opData.micGain         = parseFloat(opDataStr.sensitivity);
      $scope.opData.phoneVolume     = parseFloat(opDataStr.volume);
      $scope.opData.speed           = parseFloat(opDataStr.rate);
      //console.log("input: " + opDataStr.rate + ", output: " + $scope.opData.speed);
      $scope.opData.pitch           = parseFloat(opDataStr.pitch);
      $scope.opData.advanced        = (opDataStr.level == 'advanced');
      $scope.opData.language        = opDataStr.language;
      $scope.opData.gangingLevel    = opDataStr.gangingLevel;
      $scope.opData.upcScanProbability = opDataStr.upcScanProbability;
	    $scope.opData.randomQc        = opDataStr.randomQc;
	    $scope.opData.allowReverse    = opDataStr.allowReverse == 1;
      
      $scope.goEdit();
    }
    
    function
    convertInputToSend()
    {
      opDataStr.userName       = $scope.opData.userName;
      opDataStr.sensitivity    = String($scope.opData.micGain);
      opDataStr.volume         = String($scope.opData.phoneVolume);
      opDataStr.rate           = String($scope.opData.speed);
      opDataStr.pitch          = String($scope.opData.pitch);
      opDataStr.level          = ($scope.opData.advanced?'advanced':'beginner');
      opDataStr.language       = $scope.opData.language;
      opDataStr.gangingLevel   = $scope.opData.gangingLevel;
      opDataStr.upcScanProbability = $scope.opData.upcScanProbability;
      opDataStr.randomQc       = $scope.opData.randomQc;
	    opDataStr.allowReverse   = $scope.opData.allowReverse?1:0;
    }
    
    function
    sendInput()
    {
      setOpName($scope.thisOpID, opDataStr.userName); 
      setOpConfig($scope.thisOpID, 'operatorName',  opDataStr.userName); 
      setOpConfig($scope.thisOpID, 'volume',        opDataStr.volume);
      setOpConfig($scope.thisOpID, 'rate',          opDataStr.rate);
      setOpConfig($scope.thisOpID, 'pitch',         opDataStr.pitch);
      setOpConfig($scope.thisOpID, 'sensitivity',   opDataStr.sensitivity);
      setOpConfig($scope.thisOpID, 'level',         opDataStr.level);
      setOpConfig($scope.thisOpID, 'language',      opDataStr.language);
	    setOpDetail($scope.thisOpID, opDataStr.upcScanProbability, opDataStr.randomQc, opDataStr.gangingLevel, opDataStr.allowReverse);
      refresh();
    }
    
    
    // // // // //
    // TABLE
    
    // // //
    // RENDERERS
    
    function
    stampRender(data,type,full,meta)
    {
      if(type!='display')
        return data;
    
      var date = new Date(data);
      var today = new Date();
      if(today.toDateString()==date.toDateString())
        return date.toLocaleTimeString();
      
      return date.toLocaleString();
    }
    
    function
    boolSortRender(data,type,full,meta)
    {
      if(type!='sort' && type!='type')
        return data;
      return !!data;
    }
    
    // // //
    // OPERATOR
    
    function
    buildOperatorTable(data)
    {
      var cols = [];
      var ref = "#operator";
      
      cols.push({title: "ID",     data:"operatorID",
                                  class:"dt-center"});
      cols.push({title: "Name",   data:"operatorName",
                                  class:"dt-left"});
      cols.push({title: "Task",   data:"task",
                                  class:"dt-center",
                                  render:boolSortRender});
      cols.push({title: "Area",         data:"area",
                                  class:"dt-center"});
      cols.push({title: "Device",       data:"device",
                                  class:"dt-center"});
      // cols.push({title: "Workstation",  data:"terminal",
      //                             class:"dt-center"});
      
      if(operator){
        operator.clear();
        operator.rows.add(data);
        operator.draw(false);
      } else {
        operator = $(ref).DataTable({data: data, 
                                     columns: cols,
                                     order: [[2,'desc'],[1,'asc']],
                                     scrollY: '500px',
                                     scrollCollapse: true,
                                     paging: false,
                                     dom: 'lftBipr',
                                     buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','tr',operatorClick);
        $timeout(operator.draw,0);
      }
    }
    
    function
    operatorClick()
    {
      var data = operator.row(this).data();
      if(data && data.operatorID){
        $scope.id = data.operatorID;
        $scope.selected=1;
        refresh();
      }
    }

    // // //
    // DEVICES

    function
    buildDevicesTable(data)
    {
      var cols = [];
      var ref = "#devices";
      
      cols.push({title: "Device ID",      data:"deviceID",
                                          class:"dt-center"});
      cols.push({title: "Device Name",    data:"deviceName",
                                          class:"dt-center"});
      cols.push({title: "Device Model",   data:"buildModel",
                                          class:"dt-center"});
      cols.push({title: "OS Version",     data:"androidVersion",
                                          class:"dt-center"});
      cols.push({title: "OS Build",       data:"deviceOSBuild",
                                          class:"dt-center"});
      cols.push({title: "App Version",    data:"apkVersion",
                                          class:"dt-center"});
      cols.push({title: "Paired Devices", data:"pairedDeviceList",
                                          class:"dt-center"});
      cols.push({title: "Battery %",      data:"battery",
                                          class:"dt-center"});
      cols.push({title: "RSSI",           data:"rssi",
                                          class:"dt-center"});
      cols.push({title: "BSSID",          data:"bssid",
                                          class:"dt-center"});
      cols.push({title: "IP",             data:"ipAddress",
                                          class:"dt-center"});

      if(devices){
        devices.clear();
        devices.rows.add(data);
        devices.draw(false);
      } else {
        devices = $(ref).DataTable({data: data, 
                                     columns: cols,
                                     rowCallback: devicesCallback,
                                     order: [[0,'desc']],
                                     scrollY: '500px',
                                     scrollCollapse: true,
                                     paging: false,
                                     dom: 'lftBipr',
                                     buttons: ['copy','print','excel','pdf']});
        $timeout(devices.draw,0);
      }
    }

    function
    devicesCallback(row,data,index)
    {
      if((data.battery)){ 
        var percent = data.battery;
        var bar = percent;
        if(bar>100) bar=100;
        var color='red';
        if(percent>20)
          color='yellow';
        if(percent>70)
          color='green';
        var html = '<div class="w3-border w3-dark-grey">'
                 +   '<div class="w2-container w3-'+color+' w3-center"'
                 +     'style="height:24px;width:'+bar+'%">'
                 +     percent + '%'
                 +   '</div>' 
                 + '</div>';
        $('td:eq(7)',row).html(html);
      } 
      else {
        $('td:eq(7)',row).html('');
      }
      if((data.rssi && data.rssi!='0')){ 
        var percent = data.rssi.substring(1);
        var bar = percent;
        if(bar>80) bar=80;
        var color='red';
        if(percent<80)
          color='yellow';
        if(percent<67)
          color='green';
        var html = '<div class="w3-border w3-dark-grey">'
                 +   '<div class="w2-container w3-'+color+' w3-center"'
                 +     'style="height:24px;width:100%">'
                 +     '-' + percent
                 +   '</div>' 
                 + '</div>';
        $('td:eq(8)',row).html(html);
      }
      else {
        $('td:eq(8)',row).html('');
      }
    }
    
    // // //
    // TALK
    
    function
    buildTalkTable(data)
    {
      var cols = [];
      var ref = "#talk";

      cols.push({title: "Time",   data:"stamp",
                                  render:stampRender,
                                  class:"dt-center"});
      cols.push({title: "State",  data:"description"});
      cols.push({title: "",       data:"code",
                                  class:"dt-center"});
      cols.push({title: "Dialog", data:"text",
                                  class:"dt-left"});

      if(talk){
        talk.clear();
        talk.rows.add(data);
        talk.draw(false);
      } else {
        talk = $(ref).DataTable({data: data, 
                                 columns: cols,
                                 rowCallback: talkCallback,
//                                 order: [[0,'desc']],
                                 order: [], //use SQL order by sequence number
                                 scrollY: '440px',
                                 scrollCollapse: true,
                                 paging: false,
                                 dom: 'ltB',
                                 buttons: ['copy','print','excel','pdf']});
        $timeout(talk.draw,0);
      }
    }
    
    function
    talkCallback(row,data,index)
    {
      if(data.code=="")
        $('td:eq(2)',row).html('<img src="images/headphones.png">');
      else
        $('td:eq(2)',row).html('<img src="images/operator.png">');
    }
    
    // // //
    // MESSAGES
    
    var messagesTable = null;
    
    function
    buildMessagesTable(data)
    {
      var cols = [];
      var ref = "#messagesTable";
      
      cols.push({title: "Message", data:"message",
                                  class:"dt-center"});
 	    cols.push({title: "Sender",    data:"fromOperator",
                                  class:"dt-center"});
      cols.push({title: "Recipient",    data:"toOperator",
                                  class:"dt-center"});
      cols.push({title: "Timestamp",    data:"stamp",
                                  class:"dt-center",
                                  render: stampRender});    
      cols.push({title: "Acknowledge",   data: {},
                                  class: "dt-center",
                                  render:  (data) => {
                                        return buttonRender(ref, data, 'victoryEdit',
                                        'Acknowledge', data.seq+'ack',messageAck, true)},
                                  class:"dt-center"});

      if(messagesTable){
        messagesTable.clear();
        messagesTable.rows.add(data);
        messagesTable.draw(false);
      } else {
        messagesTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              order: [[3,'desc']],
                              scrollY: "500px",
                              scrollCollapse: true,
                              paging: false,
                              dom: 'lftipr'}); //ltB?
      }
      Global.busyRefresh(false);
    }

    function 
    buttonRender(ref, data, perm, title, name, cb, enabled) {
      name = name.replace(/\s+/g, '');
      $(ref + ' tbody').off('click', '#' + name + 'button');
      $(ref + ' tbody').on('click', '#' + name + 'button', (e) => cb(data));
      if (enabled) {
        if (Global.permit(perm))
          return '<button id="' + name + 'button">' + title + '</button>';
        else
          return '<button disabled>' + title + '</button>';
      } else {
        return '';
      }
    }      

    function
    messageAck(data) {
      $scope.message = data.message;
      $scope.seq = data.seq;

      //User confirmation popup
      var dialog = $mdDialog.confirm()
          .title('Acknowledge') 
          .textContent('Are you sure you want to acknowlege: \"' + $scope.message + '\"?') 
          .ok('Acknowledge')
          .cancel('Cancel');
        $mdDialog
          .show(dialog)
          .then(acknowledgeRecord);
    }

    function
    acknowledgeRecord()
    {
      Global.busyRefresh(true);
      DbFactory.post({topic: 'victory',
                      action: 'messageAck',
                      params: { seq: $scope.seq }})
        .success(acknowlegeSuccess)
        .error(function(err){console.log(err);});
    }

    function
    acknowlegeSuccess()
    { 
      $scope.message = {};
      refreshMessages(); 
    }

    // // // // //
    // DATA RETRIEVAL / REFRESH
    
    function
    mildRefresh()
    {
      if($scope.selected<2)
        refresh();
    }
    
    function
    refresh()
    {
      Global.busyRefresh(true);
      if($scope.selected==0)
        refreshVictoryOperators();
      if($scope.selected==1)
        refreshOperatorDetails();
      if($scope.selected==2)
        refreshMessages();
      if($scope.selected==4)
        refreshVictoryDevices();
      Global.busyRefresh(false);
    }
    
    function
    doLookup()
    {
      $scope.id = $scope.lookup;
      $scope.lookup = '';
      refresh();
    }
    
    function
    refreshError(err)
    {
      console.log(err);
    }
    
    // // //
    // VOICE OPS
    
    function
    refreshVictoryOperators()
    {
      DbFactory.post({topic: 'victory',
                      action: 'all'
                     })
        .success(operatorSuccess)
        .error  (refreshError); 
    }
    
    function
    operatorSuccess(data)
    { buildOperatorTable(data); }

    // // //
    // DEVICES

    function
    refreshVictoryDevices()
    {
      DbFactory.post({topic: 'victory',
                      action: 'devices'
                     })
        .success(devicesSuccess)
        .error  (refreshError); 
    }

    function
    devicesSuccess(data) { 
      buildDevicesTable(data); 
    }
    
    // // //
    // OPERATOR DETAILS
    
    function
    refreshOperatorDetails()
    {
      if($scope.id!=''){
        // DbFactory.post({topic: 'victory',
        //                 action: 'talk',
        //                 params: {id: $scope.id}
        //                })
        //   .success(talkSuccess)
        //   .error  (refreshError);
        displayTimeRange();

        DbFactory.post({topic: 'victory',
                        action: 'detail',
                        params: {id: $scope.id}
                       })
          .success(detailSuccess)
          .error  (refreshError);
      }
    }
    
    function
    talkSuccess(data)
    { buildTalkTable(data); }
    
    function
    detailSuccess(data)
    {
      $scope.detail = data[0];
    }

    // // // // //
    // MESSAGES

    function
    refreshMessages() {
      DbFactory.post({topic: 'victory',
                      action: 'messages'
                     })
        .success(messagesSuccess)
        .error  (refreshError);

    }

    function
    messagesSuccess(data)
    { 
      buildMessagesTable(data);
    }

    function
    messageSendSuccess(data)
    { 
      refresh();
    }

    function 
    sendMessage() {
      DbFactory.post({topic: 'victory',
                      action: 'sendMsg',
                      params: {
                        msg: $scope.message.message,
                        to: $scope.message.to 
                      }
                     })
        .success(messageSendSuccess)
        .error  (refreshError);
    }



    // // // // //
    //Update allowReverse and Random Audit Percentage

    function proOperatorsUpdate() {
            Global.busyRefresh(true);
            DbFactory.post({
                  topic: 'victory',
                  action: 'updateProOperators',
                  params: {
                        id: $scope.id,
                        upcScanProbabilityPercentage: $scope.cartonTypes.cartonType,
                        allowReverse: enabled == "Yes" ? 1 : 0
                  }
            })
      
                  .success(updateSuccess)
                  .error(updateError);
    }
    
    function
      updateSuccess() { $timeout(refresh, 1000); }
    
    function
      updateError(err) {
      console.error(err);
      Global.busyRefresh(false);
    }

         
    // // // // //
    // INIT
    
    function
    init()
    {
      Global.setTitle('Victory Operators');
      // Global.setAutoRefresh(5000);
      Global.recv('refresh', refresh, $scope);

      //Refresh the page every 5 seconds
      periodic = $interval(refresh,5000);
      setToday();	
      $scope.$on('$destroy',function(){
        $interval.cancel(periodic);
      });
    
      if($routeParams.operator){
        $scope.lookup = $routeParams.operator;
        doLookup();
        $scope.selected = 1;
      }
      
      refresh();
      refreshMessages();
      getLanguages();
    }
    
    init();
  }
  
  function
  victoryConfig($routeProvider)
  {
    $routeProvider
      .when('/victory',{controller: 'VictoryController',
                      templateUrl: '/ui/victory/victory.view.html'});
  }

}()) 
