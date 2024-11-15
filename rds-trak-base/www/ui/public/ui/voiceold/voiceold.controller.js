(function()
{
  angular
    .module('ui')
      .controller('VoiceoldController',voiceoldController);
  
  angular
    .module('ui')
      .config(voiceoldConfig);
  
  voiceoldController.$inject = ['$scope','$interval','Global','DbFactory','$timeout'];
  
  function
  voiceoldController($scope,$interval,Global,DbFactory,$timeout)
  {
    $scope.selected = 0;
    $scope.refresh = refresh;
    $scope.id = '';
    $scope.detail = {};
    $scope.lookup  = '';
    $scope.doLookup = doLookup;
    $scope.permit = Global.permit;
    $scope.newOperator    = newOperator;
    $scope.logoffOperator = logoffOperator;
    $scope.editOperator   = editOperator;
    $scope.deleteOperator = deleteOperator;
    $scope.goEdit = function(){$scope.selected = 2;}
    $scope.getEditValues = getEditValues;
    $scope.deleteButton = 'preliminary';
    $scope.beginDelete = beginDelete;
    $scope.cancelDelete = cancelDelete;
    
    $scope.storedFirstTalk = {};
    
    $scope.opData = {};
    var opDataStr = {}; //same as opData but in strings
    
    var periodic;
    var operator;
    var talk;
    
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
    
    function
    beginDelete()
    {
      $scope.deleteButton = 'really';
      setTimeout(function(){$scope.deleteButton = 'reallyReally';},100);
    }
    
    function
    cancelDelete()
    {
      $scope.deleteButton = 'preliminary';
    }
    
    
    // // // // //
    // OPERATOR FUNCTIONS
    
    function
    newOperator(opid)
    {
      DbFactory.post({topic: 'voice',
                      action: 'checkId',
                      params: {id: opid}
                     })
      .then(function(op){
        if(op.data[0].num>0) throw 'id conflict';
      })
      .then(function(){
        DbFactory.post({topic: 'voice',
                        action: 'newOpPO',
                        params: {id: opid,
                                 name: ('operator '+opid)}
                       });
      })
      .then(function(){
        DbFactory.post({topic: 'voice',
                        action: 'newOpVC',
                        params: {id: opid}
                       });
      })
      .then(function(){
        alert('Operator '+opid+' created');
        $timeout(function(){getEditValues(opid);},1000);
      })
      .catch(function(err){
        if(err=='id conflict'){
          alert('An operator with that ID already exists');
        } else {
          console.log(err);
        }
      });
    }
    
    function
    logoffOperator(opid)
    {
      DbFactory.post({topic: 'voice',
                      action: 'logoffPO',
                      params: {id: opid}
                     })
      .then(function(){
        DbFactory.post({topic: 'voice',
                        action: 'logoffVD',
                        params: {id: opid}
                       })
      })
      .then(function(){
        DbFactory.post({topic: 'voice',
                        action: 'logoffPOL',
                        params: {id: opid}
                       })
      })
      .then(function(){
        alert('Operator '+opid+' logged off');
      })
      .catch(function(err){
        console.log(err);
      });
    }
    
    function
    editOperator()
    {
      convertInputToSend();
      setTimeout(sendInput,500);
      // I don't see how this delay could be necessary, but
      // that in itself is an okay reason to leave it in
    }
    
    function
    deleteOperator(opid)
    {
      refreshCount(1,"deleteOperator");
      setOpConfig(opid,'delete','delete');
      cancelDelete();
      $scope.selected--;
      refresh0();
    }
    
    
    // // // // //
    // HELPER FUNCTIONS
    
    function
    setOpConfig(opid,name,value)
    {
      DbFactory.post({topic: 'voice',
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
    { refreshCount(-1,"setOpConfigSuccess"); }
    
    function
    setOpConfigError(err)
    {
      console.log(err);
      refreshCount(-1,"setOpConfigError");
    }
    
    
    function
    getOpConfig(opid,name)
    {
      function
      callback(value)
      { opDataStr[name] = value[0]?(value[0].value):''; }
      
      return DbFactory.post({topic: 'voice',
                             action: 'getConfig',
                             params: {id: opid,
                                      name: name}
                            })
        .success(callback)
        .error  (function(err){console.log(err);});
    }
    
    function
    blankOpData()
    { $scope.opData = {}; }
    
    function
    getEditValues(opid)
    {
      refreshCount(1,"getEditValues");
      $scope.thisOpID = opid;
      
             getOpConfig(opid,'userName')        .then(function(){
      return getOpConfig(opid,'grammar');})      .then(function(){
      return getOpConfig(opid,'soundFilePath');}).then(function(){
      return getOpConfig(opid,'pdaName');})      .then(function(){
      return getOpConfig(opid,'headsetName');})  .then(function(){
      return getOpConfig(opid,'micGain');})      .then(function(){
      return getOpConfig(opid,'phoneVolume');})  .then(function(){
      return getOpConfig(opid,'acousticModel');}).then(function(){
      return getOpConfig(opid,'agc');})          .then(function(){
      return getOpConfig(opid,'bargeIn');})      .then(function(){
      return getOpConfig(opid,'advanced');})     .then(function(){
             convertInputForForm();})
      
      // Have I mentioned recently HOW MUCH I HATE ASYNCHRONOUS FUNCTIONS
    }
    
    function
    convertInputForForm()
    {
      $scope.opData.userName       = opDataStr.userName;
      $scope.opData.grammar        = opDataStr.grammar;
      $scope.opData.soundFilePath  = opDataStr.soundFilePath;
      $scope.opData.pdaName        = opDataStr.pdaName;
      $scope.opData.headsetName    = opDataStr.headsetName;
      $scope.opData.micGain        = parseInt(opDataStr.micGain, 10);
      $scope.opData.phoneVolume    = parseInt(opDataStr.phoneVolume, 10);
      $scope.opData.acousticModel  = opDataStr.acousticModel;
      $scope.opData.agc            = (opDataStr.agc == 'true');
      $scope.opData.bargeIn        = (opDataStr.bargeIn == 'true');
      $scope.opData.advanced       = (opDataStr.advanced == 'advanced');
      
      $scope.goEdit();
      refreshCount(-1,"convertInputForForm");
    }
    
    function
    convertInputToSend()
    {
      opDataStr.userName       = $scope.opData.userName;
      opDataStr.grammar        = $scope.opData.grammar;
      opDataStr.soundFilePath  = $scope.opData.soundFilePath;
      opDataStr.pdaName        = $scope.opData.pdaName;
      opDataStr.headsetName    = $scope.opData.headsetName;
      opDataStr.micGain        = String($scope.opData.micGain);
      opDataStr.phoneVolume    = String($scope.opData.phoneVolume);
      opDataStr.acousticModel  = $scope.opData.acousticModel;
      opDataStr.agc            = ($scope.opData.agc?'true':'false');
      opDataStr.bargeIn        = ($scope.opData.bargeIn?'true':'false');
      opDataStr.advanced       = ($scope.opData.advanced?'advanced':'beginner');
    }
    
    function
    sendInput()
    {
      refreshCount(12,"sendInput");
      setOpConfig($scope.thisOpID, 'userName',      opDataStr.userName);
      setOpConfig($scope.thisOpID, 'grammar',       opDataStr.grammar);
      setOpConfig($scope.thisOpID, 'soundFilePath', opDataStr.soundFilePath);
      setOpConfig($scope.thisOpID, 'pdaName',       opDataStr.pdaName);
      setOpConfig($scope.thisOpID, 'headsetName',   opDataStr.headsetName);
      setOpConfig($scope.thisOpID, 'micGain',       opDataStr.micGain);
      setOpConfig($scope.thisOpID, 'phoneVolume',   opDataStr.phoneVolume);
      setOpConfig($scope.thisOpID, 'acousticModel', opDataStr.acousticModel);
      setOpConfig($scope.thisOpID, 'agc',           opDataStr.agc);
      setOpConfig($scope.thisOpID, 'bargeIn',       opDataStr.bargeIn);
      setOpConfig($scope.thisOpID, 'advanced',      opDataStr.advanced);
      setOpConfig($scope.thisOpID, 'update',        'update');
    }
    
    
    // // // // //
    // OTHER STUFF
    
    function
    operatorCallback(row,data,index)
    { }
    
    function
    operatorSuccess(data)
    {
      var cols = [];
      var ref = "#operator";
      
      cols.push({title: "ID",     data:"operatorID"});
      cols.push({title: "Name",   data:"operatorName"});
      cols.push({title: "Task",   data:"task"});
      cols.push({title: "Area",   data:"area"});
      cols.push({title: "Device", data:"device"});
      
      if(operator){
        operator.clear();
        operator.rows.add(data);
        operator.draw();
      } else {
        operator = $(ref).DataTable({data: data, 
                                     columns: cols,
                                     order: [],
                                     rowCallback: operatorCallback,
                                     scrollY: '440px',
                                     scrollCollapse: true,
                                     paging: false,
                                     dom: 'ltB',
                                     buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','tr',operatorClick);
      }
      refreshCount(-1,"operatorSuccess");
    }
    
    function
    operatorClick()
    {
      var data = operator.row(this).data();
      $scope.id = data.operatorID;
      $scope.selected=1;
    }
    
    function
    doLookup()
    {
      $scope.id = $scope.lookup;
      $scope.lookup = '';
      refresh();
    }
     
    function
    talkCallback(row,data,index)
    {
      if(data.code<0)
        $('td:eq(2)',row).html('<img src="images/headphones.png">');
      else
        $('td:eq(2)',row).html('<img src="images/operator.png">');
    }
    
    function
    talkSuccess(data)
    {
      if(data[0] && (data[0].seq!=$scope.storedFirstTalk)){
        $scope.storedFirstTalk = data[0].seq;
        
        var cols = [];
        var ref = "#talk";
        
        cols.push({title: "Time",   data:"stamp",
                                    render:stampRender});
        cols.push({title: "State",  data:"description"});
        cols.push({title: "",       data:"code"});
        cols.push({title: "Dialog", data:"text"});
        
        if(talk){
          talk.clear();
          talk.rows.add(data);
          talk.draw();
        } else {
          talk = $(ref).DataTable({data: data, 
                                   columns: cols,
                                   rowCallback: talkCallback,
                                   order: [],
                                   scrollY: '360px',
                                   scrollCollapse: true,
                                   paging: false,
                                   dom: 'ltB',
                                   buttons: ['copy','print','excel','pdf']});
        }
      }
      refreshCount(-1,"talkSuccess");
    }
    
    function
    detailSuccess(data)
    {
      $scope.detail = data[0];
      refreshCount(-1,"detailSuccess");
    }
    
    function
    refresh0()
    {
      refreshCount(1,"refresh0");
      DbFactory.post({topic: 'voice',
                      action: 'all'
                     })
        .success(operatorSuccess)
        .error  (refreshError); 
    }
    
    function
    refresh1()
    {
      if($scope.id!=''){
        refreshCount(2,"refresh1");
        DbFactory.post({topic: 'voice',
                        action: 'talk',
                        params: {id: $scope.id}
                       })
          .success(talkSuccess)
          .error  (refreshError);
        DbFactory.post({topic: 'voice',
                        action: 'detail',
                        params: {id: $scope.id}
                       })
          .success(detailSuccess)
          .error  (refreshError);
      }
    }
    
    function
    refreshError(err)
    {
      console.log(err);
      refreshCount(-1,"refreshError");
    }
    
    function
    refresh()
    {
      if($scope.selected==0)
        refresh0();
      if($scope.selected==1)
        refresh1();
    }
    
    function
    autoRefresh()
    {
      if($scope.selected==1)
        refresh1();
    }
    
    function
    init()
    {
      Global.setTitle('Voice Operators');
      Global.recv('refresh',refresh,$scope);
      periodic = $interval(autoRefresh,3000);
      
      refresh();
    }
    
    $scope.$on('$destroy',function(){
      $interval.cancel(periodic);
    });
    
    init();
  }
  
  function
  voiceoldConfig($routeProvider)
  {
    $routeProvider
      .when('/voiceold',{controller: 'VoiceoldController',
                      templateUrl: '/ui/voiceold/voiceold.view.html'});
  }

}()) 
