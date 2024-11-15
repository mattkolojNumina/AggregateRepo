(function()
{
  angular
    .module('ui')
      .controller('VoiceController',voiceController);
  
  angular
    .module('ui')
      .config(voiceConfig);
  
  voiceController.$inject = ['$scope','$interval','Global','DbFactory','$timeout','$routeParams'];
  
  function
  voiceController($scope,$interval,Global,DbFactory,$timeout,$routeParams)
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
    $scope.logoffOperator = logoffOperator;
    $scope.editOperator = editOperator;
    $scope.deleteOperator = deleteOperator;
    $scope.goEdit = function(){$scope.selected = 2;}
    $scope.getEditValues = getEditValues;
    $scope.deleteButton = 'preliminary';
    $scope.beginDelete = beginDelete;
    $scope.endDelete = endDelete;
    
    $scope.pairNew = pairNew;
    $scope.pairUpdate = pairUpdate;
    $scope.pairDelete = pairDelete;
    $scope.pairsCommit = pairsCommit;
    $scope.changed = false;
    
    $scope.barShown = 'tool';
    $scope.showReload = showReload;
    $scope.barReset = barReset;
    $scope.reloadPairing = reloadPairing;
    $scope.opIsUnchanged = true;
    $scope.changeOp = function(){$scope.opIsUnchanged = false;}
    
    $scope.opData = {};
    var opDataStr = {}; //same as opData but in strings
    
    var configsTrue = [];
    var configsMod = [];
    
    var periodic;
    var operator;
    var talk;
    
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
    // OPERATOR COMMANDS
    
    function
    newOperator(opid)
    {
      refreshCount(1,"newOperator");
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
        // This delay allows the backend program to set up the new user
        $timeout(function(){
          getEditValues(opid);
          refreshCount(-1,"newOperator_");
        },creationDelay);
      })
      .catch(function(err){
        if(err=='id conflict'){
          alert('An operator with that ID already exists');
        } else {
          console.error(err);
        }
        refreshCount(-1,"newOperator__");
      });
    }
    
    function
    logoffOperator(opid)
    {
      refreshCount(1,"logoffOperator");
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
        refreshCount(-1,"logoffOperator_");
      })
      .catch(function(err){
        console.error(err);
        refreshCount(-1,"logoffOperator__");
      });
    }
    
    function
    editOperator()
    {
      refreshCount(1,"editOperator");
      convertInputToSend();
      $timeout(function(){
        sendInput();
        $scope.opIsUnchanged = true;
        refreshCount(-1,"editOperator_");
      },500);
      // I don't see how this delay could be necessary, but
      // that in itself is an okay reason to leave it in
    }
    
    function
    beginDelete()
    {
      $scope.deleteButton = 'really';
      $timeout(function(){$scope.deleteButton = 'reallyReally';},100);
    }
    
    function
    endDelete()
    {
      $scope.deleteButton = 'preliminary';
    }
    
    function
    deleteOperator(opid)
    {
      refreshCount(1,"deleteOperator");
      setOpConfig(opid,'delete','delete');
      endDelete();
      $scope.opData = {};
      $scope.thisOpID = '';
      $scope.selected--;
      refreshVoiceOperators();
    }
    
    
    // // // // //
    // CONFIGURATION
    
    // // //
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
      console.error(err);
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
        .error  (function(err){console.error(err);});
    }
    
    function
    blankOpData()
    { $scope.opData = {}; }
    
    // // //
    // GET / SEND
    
    function
    getLanguages()
    {
      refreshCount(1,"getLanguages");
      DbFactory.post({topic: 'voice',
                      action: 'getLanguages'
                     })
        .success(langSuccess)
        .error  (langError);
    }
    
    function
    langSuccess(langs)
    {
      $scope.langChoices = langs;
      refreshCount(-1,"langSuccess");
    }
    
    function
    langError(err)
    {
      console.error(err);
      refreshCount(-1,"langError");
    }
    
    function
    getEditValues(opid)
    {
      refreshCount(1,"getEditValues");
      $scope.thisOpID = opid;
      $scope.opIsUnchanged = true;
      
             getOpConfig(opid,'userName')         .then(function(){
//      return getOpConfig(opid,'grammar');})       .then(function(){
//      return getOpConfig(opid,'soundFilePath');}) .then(function(){
      return getOpConfig(opid,'pdaName');})       .then(function(){
      return getOpConfig(opid,'headsetName');})   .then(function(){
      return getOpConfig(opid,'micGain');})       .then(function(){
      return getOpConfig(opid,'phoneVolume');})   .then(function(){
//      return getOpConfig(opid,'acousticModel');}) .then(function(){
      return getOpConfig(opid,'agc');})           .then(function(){
      return getOpConfig(opid,'bargeIn');})       .then(function(){
      return getOpConfig(opid,'advanced');})      .then(function(){
      return getOpConfig(opid,'language');})      .then(function(){
             convertInputForForm();})
      
      // Have I mentioned recently HOW MUCH I HATE ASYNCHRONOUS FUNCTIONS
    }
    
    function
    convertInputForForm()
    {
      $scope.opData.userName        = opDataStr.userName;
//      $scope.opData.grammar         = opDataStr.grammar;
//      $scope.opData.soundFilePath   = opDataStr.soundFilePath;
      $scope.opData.pdaName         = opDataStr.pdaName;
      $scope.opData.headsetName     = opDataStr.headsetName;
      $scope.opData.micGain         = parseInt(opDataStr.micGain, 10);
      $scope.opData.phoneVolume     = parseInt(opDataStr.phoneVolume, 10);
//      $scope.opData.acousticModel   = opDataStr.acousticModel;
      $scope.opData.agc             = (opDataStr.agc == 'true');
      $scope.opData.bargeIn         = (opDataStr.bargeIn == 'true');
      $scope.opData.advanced        = (opDataStr.advanced == 'advanced');
      $scope.opData.language        = opDataStr.language;
      
      $scope.goEdit();
      refreshCount(-1,"convertInputForForm");
    }
    
    function
    convertInputToSend()
    {
      opDataStr.userName       = $scope.opData.userName;
//      opDataStr.grammar        = $scope.opData.grammar;
//      opDataStr.soundFilePath  = $scope.opData.soundFilePath;
      opDataStr.pdaName        = $scope.opData.pdaName;
      opDataStr.headsetName    = $scope.opData.headsetName;
      opDataStr.micGain        = String($scope.opData.micGain);
      opDataStr.phoneVolume    = String($scope.opData.phoneVolume);
//      opDataStr.acousticModel  = $scope.opData.acousticModel;
      opDataStr.agc            = ($scope.opData.agc?'true':'false');
      opDataStr.bargeIn        = ($scope.opData.bargeIn?'true':'false');
      opDataStr.advanced       = ($scope.opData.advanced?'advanced':'beginner');
      opDataStr.language       = $scope.opData.language;
    }
    
    function
    sendInput()
    {
      refreshCount(10,"sendInput");
      setOpConfig($scope.thisOpID, 'userName',      opDataStr.userName);
//      setOpConfig($scope.thisOpID, 'grammar',       opDataStr.grammar);
//      setOpConfig($scope.thisOpID, 'soundFilePath', opDataStr.soundFilePath);
      setOpConfig($scope.thisOpID, 'pdaName',       opDataStr.pdaName);
      setOpConfig($scope.thisOpID, 'headsetName',   opDataStr.headsetName);
      setOpConfig($scope.thisOpID, 'micGain',       opDataStr.micGain);
      setOpConfig($scope.thisOpID, 'phoneVolume',   opDataStr.phoneVolume);
//      setOpConfig($scope.thisOpID, 'acousticModel', opDataStr.acousticModel);
      setOpConfig($scope.thisOpID, 'agc',           opDataStr.agc);
      setOpConfig($scope.thisOpID, 'bargeIn',       opDataStr.bargeIn);
      setOpConfig($scope.thisOpID, 'advanced',      opDataStr.advanced);
      setOpConfig($scope.thisOpID, 'language',      opDataStr.language);
      setOpConfig($scope.thisOpID, 'update',        'update');
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
      cols.push({title: "Area",   data:"area",
                                  class:"dt-center"});
      cols.push({title: "Device", data:"device",
                                  class:"dt-center"});
      
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
                                     dom: 'lftB',
                                     buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','tr',operatorClick);
        $timeout(operator.draw,0);
      }
      refreshCount(-1,"buildOperatorTable");
    }
    
    function
    operatorClick()
    {
      var data = operator.row(this).data();
      if(data && data.operatorID){
        $scope.id = data.operatorID;
        $scope.selected=1;
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
      refreshCount(-1,"buildTalkTable");
    }
    
    function
    talkCallback(row,data,index)
    {
      if(data.code<0)
        $('td:eq(2)',row).html('<img src="images/headphones.png">');
      else
        $('td:eq(2)',row).html('<img src="images/operator.png">');
    }
    
    // // //
    // PAIRING
    
    var pairsTable = null;
    
    function
    buildPairsTable(data)
    {
      var cols = [];
      var ref = "#pairsTable";
      
      cols.push({title: "Name",   data:"name",
                                  class:"dt-center"});
      cols.push({title: "Client", data:"client",
                                  class:"dt-center"});
	  cols.push({title: "MAC",    data:"mac",
                                  class:"dt-center"});
      cols.push({title: "PIN",    data:"pin",
                                  class:"dt-center"});
      
      if(pairsTable){
        pairsTable.clear();
        pairsTable.rows.add(data);
        pairsTable.draw(false);
      } else {
        pairsTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              order: [[0,'asc']],
                              scrollY: "500px",
                              scrollCollapse: true,
                              paging: false,
                              dom: 'lftipr'}); //ltB?
        $(ref+' tbody').on('click','tr',pairsClick);
        $timeout(pairsTable.draw,0);
      }
      refreshCount(-1,"buildPairsTable");
    }
      
    function
    pairsClick()
    {
      var data = pairsTable.row(this).data();
      $scope.pair = data;
      $scope.$apply();
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
      if($scope.selected==0)
        refreshVoiceOperators();
      if($scope.selected==1)
        refreshOperatorDetails();
      if($scope.selected==2 && $scope.thisOpID)
        getEditValues($scope.thisOpID);
      if($scope.selected==3)
        refreshPairing();
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
      console.error(err);
      refreshCount(-1,"refreshError");
    }
    
    // // //
    // VOICE OPS
    
    function
    refreshVoiceOperators()
    {
      refreshCount(1,"refreshVoiceOperators");
      DbFactory.post({topic: 'voice',
                      action: 'all'
                     })
        .success(operatorSuccess)
        .error  (refreshError); 
    }
    
    function
    operatorSuccess(data)
    { buildOperatorTable(data); }
    
    // // //
    // OPERATOR DETAILS
    
    function
    refreshOperatorDetails()
    {
      if($scope.id!=''){
        refreshCount(2,"refreshOperatorDetails");
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
    talkSuccess(data)
    { buildTalkTable(data); }
    
    function
    detailSuccess(data)
    {
      $scope.detail = data[0];
      refreshCount(-1,"detailSuccess");
    }
    
    // // //
    // PAIRING
    
    function
    refreshPairing()
    {
      refreshCount(1,"refreshPairing");
      configsTrue = [];
      configsMod = [];
      $scope.changed = false;
      DbFactory.post({topic: 'voice',
                      action: 'pairings'
                     })
        .success(refreshPairingSuccess)
        .error  (refreshError);
    }
    
    function
    refreshPairingSuccess(data)
    {
      configsTrue = data;
      configsMod = JSON.parse(JSON.stringify(configsTrue));
      buildPairsTable(configsMod);
    }
    
    
    // // // // //
    // PAIRING COMMANDS
    
    function
    pairNew()
    {
      $scope.pair = {};
    }
    
    function
    pairUpdate()
    {
      refreshCount(1,"pairUpdate");
      $scope.changed = true;
      var neu = true;
      for(var i = 0; i < configsMod.length; i++){
        if(configsMod[i].name==$scope.pair.name && configsMod[i].client==$scope.pair.client){
          neu = false;
          configsMod[i].mac = $scope.pair.mac;
          configsMod[i].pin = $scope.pair.pin;
          break;
        }
      }
      if(neu && !!$scope.pair.name && !!$scope.pair.client){
        configsMod.push({
          name:   $scope.pair.name,
          client: $scope.pair.client,
          mac:    $scope.pair.mac || '',
          pin:    $scope.pair.pin || ''
        });
      }
      pairNew();
      buildPairsTable(configsMod);
    }
    
    function
    pairDelete()
    {
      refreshCount(1,"pairDelete");
      $scope.changed = true;
      for(var j = 0; j < configsMod.length; j++){
        if(configsMod[j].name==$scope.pair.name && configsMod[j].client==$scope.pair.client){
          configsMod.splice(j,1);
          break;
        }
      }
      pairNew();
      buildPairsTable(configsMod);
    }
    
    function
    pairsCommit()
    {
      refreshCount(1,"pairsCommit");
      for(var k = 0; k<configsMod.length; k++){
        var write = true;
        var ck = configsMod[k];
        if(!ck.client || !ck.name)
          write = false;
        for(var l = 0; l<configsTrue.length; l++){
          var cl = configsTrue[l];
          if(ck.client==cl.client &&
             ck.name==cl.name &&
             ck.mac==cl.mac &&
             ck.pin==cl.pin){
            write = false;
          }
          if(!write) break;
        }
        if(write){
          refreshCount(1,"pairsCommit_");
          DbFactory.post({topic: 'voice',
                          action: 'pair',
                          params: {client: ck.client,
                                   name: ck.name,
                                   mac: ck.mac || '',
                                   pin: ck.pin || ''}
                         })
            .success(pairSuccess)
            .error  (pairError);
        }
      }
      for(var m = 0; m<configsTrue.length; m++){
        var deletThis = true;
        var cm = configsTrue[m];
        for(var n = 0; n<configsMod.length; n++){
          var cn = configsMod[n];
          if(cm.client==cn.client && cm.name==cn.name)
            deletThis = false;
        }
        if(deletThis){
          refreshCount(1,"pairsCommit__");
          DbFactory.post({topic: 'voice',
                          action: 'pairDel',
                          params: {client: cm.client,
                                   name: cm.name}
                         })
            .success(pairSuccess)
            .error  (pairError);
        }
      }
      refreshCount(-1,"pairsCommit___");
    }
    
    function
    pairSuccess()
    {
      refreshCount(-1,"pairSuccess");
      if(refreshCounter==0){
        refreshCount(1,"pairSuccess_");
        DbFactory.post({topic: 'voice',
                        action: 'pairUpdate'
                       })
          .success(pairSuccessSuccess)
          .error  (pairError);
      }
    }
    
    function
    pairSuccessSuccess()
    {
      refreshPairing();
      refreshCount(-1,"pairSuccessSuccess");
    }
    
    function
    pairError(err)
    {
      console.error(err);
      refreshCount(-1,"pairError");
    }
    
    function
    showReload()
    {
      $scope.barShown = 'reload';
    }
    
    function
    barReset()
    {
      $scope.barShown = 'tool';
    }
    
    function
    reloadPairing()
    {
      refreshCount(1,'reloadPairing');
      barReset();
      DbFactory.post({topic: 'voice',
                      action: 'pairReload'
                     })
        .success(reloadPairingSuccess)
        .error  (reloadPairingError);
    }
    
    function
    reloadPairingSuccess()
    {
      $timeout(function(){
        refreshPairing();
        refreshCount(-1,'reloadPairingSuccess');
      },1500);
    }
    
    function
    reloadPairingError(err)
    {
      console.error(err);
      Global.showMessage("Reload error");
      refreshPairing();
      refreshCount(-1,'reloadPairingError');
    }
    
    
    // // // // //
    // INIT
    
    function
    init()
    {
      Global.setTitle('Voice Operators');
      Global.recv('refresh',refresh,$scope);
      periodic = $interval(mildRefresh,4000);
      
      if($routeParams.operator){
        $scope.lookup = $routeParams.operator;
        doLookup();
        $scope.selected = 1;
      }
      
      refresh();
      refreshPairing();
      getLanguages();
    }
    
    $scope.$on('$destroy',function(){
      $interval.cancel(periodic);
    });
    
    init();
  }
  
  function
  voiceConfig($routeProvider)
  {
    $routeProvider
      .when('/voice',{controller: 'VoiceController',
                      templateUrl: '/ui/voice/voice.view.html'});
  }

}()) 
