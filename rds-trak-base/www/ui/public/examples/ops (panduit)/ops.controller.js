(function()
{
  angular
    .module('ui')
      .controller('OpsController',opsController);

  angular
    .module('ui')
      .config(opsConfig);

  opsController.$inject = ['$scope','$interval','$timeout',
                           'Global','DbFactory'];

  function
  opsController($scope,$interval,$timeout,
                Global,DbFactory)
  {
    var periodic;
    var configTable = null;

    $scope.refresh = refresh;
    $scope.permit = Global.permit;

    $scope.selected = 0;
    $scope.pick1Button     = '';
    $scope.pick2Button     = '';
    $scope.pick3Button     = '';
    $scope.pickMergeButton = '';
    $scope.bridgeButton    = '';
    $scope.lLoopButton     = '';

    $scope.pick1Start      = pick1Start;
    $scope.pick2Start      = pick2Start;
    $scope.pick3Start      = pick3Start;
    $scope.pickMergeStart  = pickMergeStart;
    $scope.bridgeStart     = bridgeStart;
    $scope.lLoopStart      = lLoopStart;

    $scope.pick1Latest     = undefined;
    $scope.pick2Latest     = undefined;
    $scope.pick3Latest     = undefined;
    $scope.pickMergeLatest = undefined;
    $scope.bridgeLatest    = undefined;
    $scope.lLoopLatest     = undefined;

    $scope.pick1Stop       = pick1Stop;
    $scope.pick2Stop       = pick2Stop;
    $scope.pick3Stop       = pick3Stop;
    $scope.pickMergeStop   = pickMergeStop;
    $scope.bridgeStop      = bridgeStop;
    $scope.lLoopStop       = lLoopStop;

    $scope.disableAllPack  = disableAllPack;

    $scope.allStart        = allStart;
    $scope.allStop         = allStop;

    $scope.edit            = {};
    $scope.commitTune      = commitTune;

    $scope.configTable;
    $scope.packconfigSaveNew = packconfigSaveNew;
    $scope.packconfigLoad    = packconfigLoad;
    $scope.packconfigDelete  = packconfigDelete;
    $scope.editDisabled      = editDisabled;
    
    $scope.hLoopShow  = hLoopShow;
    $scope.hLoopHide  = hLoopHide;
    $scope.hLoopShown = false;
    
    $scope.jLoopShow  = jLoopShow;
    $scope.jLoopHide  = jLoopHide;
    $scope.jLoopShown = false;
    
    $scope.kLoopShow  = kLoopShow;
    $scope.kLoopHide  = kLoopHide;
    $scope.kLoopShown = false;
    

    // // // // //
    // START/STOP FUNCTIONS

    // // //
    // START

    function
    disableAllPack(which)
    {
      var d = new Date();
      var name = "Autosave "+//dateStr.toUTCString();
          d.toLocaleString()
      var callback = function(){disableAllPackInner(which);};
      packconfigSave(name,callback);
    }

    function
    disableAllPackInner(which)
    {
      DbFactory.post({topic: 'ops',
                      action: 'packDisable',
                      params: {sorter: which},
                      log: 'disable all pack lanes on sorter ' + which
                    })
        .success(refreshConfigWithButtonReset)
        .error(function(err){console.log(err);});

      console.log("disable " + which);
    }
    
    function
    hLoopShow()
    { $scope.hLoopShown = true; }
    
    function
    hLoopHide()
    { $scope.hLoopShown = false; }
    
    function
    jLoopShow()
    { $scope.jLoopShown = true; }
    
    function
    jLoopHide()
    { $scope.jLoopShown = false; }
    
    function
    kLoopShow()
    { $scope.kLoopShown = true; }
    
    function
    kLoopHide()
    { $scope.kLoopShown = false; }
    
    function
    pick1Start()
    {
      DbFactory.post({topic: 'ops',
                      action: 'runstop',
                      params: {arg1: 'pick1',
                               arg2: 'start'}})
        .catch(function(err){console.log(err);});
    }

    function
    pick2Start()
    {
      DbFactory.post({topic: 'ops',
                      action: 'runstop',
                      params: {arg1: 'pick2',
                               arg2: 'start'}})
        .catch(function(err){console.log(err);});
    }

    function
    pick3Start()
    {
      DbFactory.post({topic: 'ops',
                      action: 'runstop',
                      params: {arg1: 'pick3',
                               arg2: 'start'}})
        .catch(function(err){console.log(err);});
    }

    function
    pickMergeStart()
    {
      DbFactory.post({topic: 'ops',
                      action: 'runstop',
                      params: {arg1: 'pickmerge',
                               arg2: 'start'}})
        .catch(function(err){console.log(err);});
    }

    function
    bridgeStart()
    {
      DbFactory.post({topic: 'ops',
                      action: 'runstop',
                      params: {arg1: 'bridge',
                               arg2: 'start'}})
        .catch(function(err){console.log(err);});
    }

    function
    lLoopStart()
    {
      DbFactory.post({topic: 'ops',
                      action: 'runstop',
                      params: {arg1: 'loop',
                               arg2: 'start'}})
        .catch(function(err){console.log(err);});
    }

    function
    allStart()
    {
      pick1Start();
      pick2Start();
      pick3Start();
      pickMergeStart();
      bridgeStart();
      lLoopStart();
    }

    // // //
    // STOP

    function
    pick1Stop()
    {
      DbFactory.post({topic: 'ops',
                      action: 'runstop',
                      params: {arg1: 'pick1',
                               arg2: 'stop'}})
        .catch(function(err){console.log(err);});
    }

    function
    pick2Stop()
    {
      DbFactory.post({topic: 'ops',
                      action: 'runstop',
                      params: {arg1: 'pick2',
                               arg2: 'stop'}})
        .catch(function(err){console.log(err);});
    }

    function
    pick3Stop()
    {
      DbFactory.post({topic: 'ops',
                      action: 'runstop',
                      params: {arg1: 'pick3',
                               arg2: 'stop'}})
        .catch(function(err){console.log(err);});
    }

    function
    pickMergeStop()
    {
      DbFactory.post({topic: 'ops',
                      action: 'runstop',
                      params: {arg1: 'pickmerge',
                               arg2: 'stop'}})
        .catch(function(err){console.log(err);});
    }

    function
    bridgeStop()
    {
      DbFactory.post({topic: 'ops',
                      action: 'runstop',
                      params: {arg1: 'bridge',
                               arg2: 'stop'}})
        .catch(function(err){console.log(err);});
    }

    function
    lLoopStop()
    {
      DbFactory.post({topic: 'ops',
                      action: 'runstop',
                      params: {arg1: 'loop',
                               arg2: 'stop'}})
        .catch(function(err){console.log(err);});
    }

    function
    allStop()
    {
      pick1Stop();
      pick2Stop();
      pick3Stop();
      pickMergeStop();
      bridgeStop();
      lLoopStop();
    }


    // // // // //
    // TUNING FUNCTIONS

    function
    commitTune()
    {
      sendAutoRelease();
      sendAutoExpress() ;
      sendLiveLoad();
      sendRecircTrigger();
      sendStagingFill();
      sendWaveCommit() ;
      sendWavePnA() ;
      sendWaveStd() ;
      sendDuct() ;
      sendHLoop() ;
      sendJLoop() ;
      sendKLoop() ;
      sendWaveFail() ;
      sendHFail() ;
      sendKFail() ;
      sendJFail() ;
      sendShipFail() ;
      sendLFail() ;
      for (var i=1 ; i <= 9 ; i++) {
        sendShipScanFail(i) ;
      }
      sendLoopScanFail(7) ;
      sendLoopScanFail(8) ;
      sendLoopScanFail(9) ;
    }

    function
    sendShipScanFail(which)
    {
      var variable = 'ship' + which + 'Fail' ;
      if (which == 1) {
          sendConsec('shipLaneScan_' + which,$scope.edit.ship1Fail.toString()) ;
      } else if (which == 2) {
        sendConsec('shipLaneScan_' + which,$scope.edit.ship2Fail.toString()) ;
      } else if (which == 3) {
        sendConsec('shipLaneScan_' + which,$scope.edit.ship3Fail.toString()) ;
      } else if (which == 4) {
        sendConsec('shipLaneScan_' + which,$scope.edit.ship4Fail.toString()) ;
      } else if (which == 5) {
        sendConsec('shipLaneScan_' + which,$scope.edit.ship5Fail.toString()) ;
      } else if (which == 6) {
        sendConsec('shipLaneScan_' + which,$scope.edit.ship6Fail.toString()) ;
      } else if (which == 7) {
        sendConsec('shipLaneScan_' + which,$scope.edit.ship7Fail.toString()) ;
      } else if (which == 8) {
        sendConsec('shipLaneScan_' + which,$scope.edit.ship8Fail.toString()) ;
      } else if (which == 9) {
        sendConsec('shipLaneScan_' + which,$scope.edit.ship9Fail.toString()) ;
      }

    }

    function
    sendLoopScanFail(which)
    {
      var variable = 'ship' + which + 'Fail' ;
      if (which == 7) {
          sendConsec('loopLaneScan_' + which,$scope.edit.loop7Fail.toString()) ;
      } else if (which == 8) {
        sendConsec('loopLaneScan_' + which,$scope.edit.loop8Fail.toString()) ;
      } else if (which == 9) {
        sendConsec('loopLaneScan_' + which,$scope.edit.loop9Fail.toString()) ;
      }
    }

    function
    sendConsec(name,value)
    {
      DbFactory.post({topic: 'ops',
                          action: 'tune',
                          log: 'ops tune page parameter saved',
                          params: {zone: 'consec',
                                   name: name,
                                   value: value}})
            .catch(function(err){console.log(err);});

    }
    function
    sendWaveFail()
    {
      sendConsec('waveSorterScan',$scope.edit.waveFail.toString()) ;
    }

    function
    sendHFail()
    {
      sendConsec('h-loopSorterScan',$scope.edit.hscanFail.toString()) ;
    }
    function
    sendKFail()
    {
      sendConsec('k-loopSorterScan',$scope.edit.kscanFail.toString()) ;
    }
    function
    sendJFail()
    {
        sendConsec('j-loopSorterScan',$scope.edit.jscanFail.toString()) ;
    }
    function
    sendShipFail()
    {
      sendConsec('shipSorterScan',$scope.edit.shipFail.toString()) ;
    }
    function
    sendLFail()
    {
      sendConsec('loopSorterScan',$scope.edit.lscanFail.toString()) ;
    }
    function
    sendHLoop()
    {
      DbFactory.post({topic: 'ops',
                          action: 'tune',
                          params: {zone: 'h-sort',
                                   name: 'max',
                                   value: $scope.edit.hLength.toString()}})
            .catch(function(err){console.log(err);});

    }

    function
    sendJLoop()
    {
      DbFactory.post({topic: 'ops',
                          action: 'tune',
                          params: {zone: 'j-sort',
                                   name: 'max',
                                   value: $scope.edit.jLength.toString()}})
            .catch(function(err){console.log(err);});

    }
    function
    sendKLoop()
    {
      DbFactory.post({topic: 'ops',
                          action: 'tune',
                          params: {zone: 'k-sort',
                                   name: 'max',
                                   value: $scope.edit.kLength.toString()}})
            .catch(function(err){console.log(err);});

    }

    function
    sendDuct()
    {
      DbFactory.post({topic: 'ops',
                          action: 'tune',
                          params: {zone: 'l-loop',
                                   name: 'duct_enable',
                                   value: $scope.edit.ductEnable?'true':'false'}})
            .catch(function(err){console.log(err);});

    }

    function
    sendWavePnA()
    {
      DbFactory.post({topic: 'ops',
                      action: 'tune',
                      params: {zone: 'wv-sort',
                               name: 'print_apply_lane',
                               value: $scope.edit.printApply.toString()}})
        .catch(function(err){console.log(err);});

    }
    function
    sendWaveStd()
    {
      DbFactory.post({topic: 'ops',
                      action: 'tune',
                      params: {zone: 'wv-sort',
                               name: 'shipping_lane',
                               value: $scope.edit.standardLane.toString()}})
        .catch(function(err){console.log(err);});

    }
    function
    sendWaveCommit()
    {
      DbFactory.post({topic: 'ops',
                      action: 'tune',
                      params: {zone: 'wv-sort',
                               name: 'emergency_lane',
                               value: $scope.edit.committed.toString()}})
        .catch(function(err){console.log(err);});

    }

    function
    sendAutoRelease()
    {
      DbFactory.post({topic: 'ops',
                      action: 'tune',
                      params: {zone: 'system',
                               name: 'autoRelease',
                               value: $scope.edit.autoRelease?'on':'off'}})
        .catch(function(err){console.log(err);});
    }

    function
    sendAutoExpress()
    {
      DbFactory.post({topic: 'ops',
                      action: 'tune',
                      params: {zone: 'wave',
                               name: 'express',
                               value: $scope.edit.autoExpress?'on':'off'}})
        .catch(function(err){console.log(err);});
    }

    function
    sendLiveLoad()
    {
      DbFactory.post({topic: 'ops',
                      action: 'tune',
                      params: {zone: 'system',
                               name: 'liveLoadEnabled',
                               value: $scope.edit.liveLoad?'yes':'no'}})
        .catch(function(err){console.log(err);});
    }

    function
    sendRecircTrigger()
    {
      DbFactory.post({topic: 'ops',
                      action: 'tune',
                      params: {zone: 'shp-sort',
                               name: 'recircTrigger',
                               value: $scope.edit.recircTrigger.toString()}})
        .catch(function(err){console.log(err);});
    }

    function
    sendStagingFill()
    {
      DbFactory.post({topic: 'ops',
                      action: 'tune',
                      params: {zone: 'wave',
                               name: 'stagingFill',
                               value: $scope.edit.stagingFill.toString()}})
        .catch(function(err){console.log(err);});
    }


    // // // // //
    // DATA RETRIEVAL

    // // //
    // START/STOP

    function
    getSystemStatus()
    {
      DbFactory.post({topic: 'ops',
                      action: 'status',
                      params: {name: 'pick1_run'}})
        .success(pick1StatusSuccess)
        .error(function(err){console.log(err);});

      DbFactory.post({topic: 'ops',
                      action: 'status',
                      params: {name: 'pick2_run'}})
        .success(pick2StatusSuccess)
        .error(function(err){console.log(err);});

      DbFactory.post({topic: 'ops',
                      action: 'status',
                      params: {name: 'pick3_run'}})
        .success(pick3StatusSuccess)
        .error(function(err){console.log(err);});

      DbFactory.post({topic: 'ops',
                      action: 'status',
                      params: {name: 'pickmerge_run'}})
        .success(pickMergeStatusSuccess)
        .error(function(err){console.log(err);});

      DbFactory.post({topic: 'ops',
                      action: 'status',
                      params: {name: 'bridge_run'}})
        .success(bridgeStatusSuccess)
        .error(function(err){console.log(err);});

      DbFactory.post({topic: 'ops',
                      action: 'status',
                      params: {name: 'loop_run'}})
        .success(lLoopStatusSuccess)
        .error(function(err){console.log(err);});
    }

    function
    pick1StatusSuccess(data)
    {
      if(data[0].value=='run'){
        $scope.pick1Button =     'stop';
      } else if(data[0].value=='stop'){
        $scope.pick1Button =     'start';
      } else {
        $scope.pick1Button =     '';
        if(data[0].value!=$scope.pick1Latest)
          console.log("pick1 had unknown status '"+data[0].value+"'");
      }
      $scope.pick1Latest = data[0].value;
    }

    function
    pick2StatusSuccess(data)
    {
      if(data[0].value=='run'){
        $scope.pick2Button =     'stop';
      } else if(data[0].value=='stop'){
        $scope.pick2Button =     'start';
      } else {
        $scope.pick2Button =     '';
        if(data[0].value!=$scope.pick2Latest)
          console.log("pick2 had unknown status '"+data[0].value+"'");
      }
      $scope.pick2Latest = data[0].value;
    }

    function
    pick3StatusSuccess(data)
    {
      if(data[0].value=='run'){
        $scope.pick3Button =     'stop';
      } else if(data[0].value=='stop'){
        $scope.pick3Button =     'start';
      } else {
        $scope.pick3Button =     '';
        if(data[0].value!=$scope.pick3Latest)
          console.log("pick3 had unknown status '"+data[0].value+"'");
      }
      $scope.pick3Latest = data[0].value;
    }

    function
    pickMergeStatusSuccess(data)
    {
      if(data[0].value=='run'){
        $scope.pickMergeButton = 'stop';
      } else if(data[0].value=='stop'){
        $scope.pickMergeButton = 'start';
      } else {
        $scope.pickMergeButton = '';
        if(data[0].value!=$scope.pickMergeLatest)
          console.log("pickMerge had unknown status '"+data[0].value+"'");
      }
      $scope.pickMergeLatest = data[0].value;
    }

    function
    bridgeStatusSuccess(data)
    {
      if(data[0].value=='run'){
        $scope.bridgeButton =    'stop';
      } else if(data[0].value=='stop'){
        $scope.bridgeButton =    'start';
      } else {
        $scope.bridgeButton =    '';
        if(data[0].value!=$scope.bridgeLatest)
          console.log("bridge had unknown status '"+data[0].value+"'");
      }
      $scope.bridgeLatest = data[0].value;
    }

    function
    lLoopStatusSuccess(data)
    {
      if(data[0].value=='run'){
        $scope.lLoopButton =     'stop';
      } else if(data[0].value=='stop'){
        $scope.lLoopButton =     'start';
      } else {
        $scope.lLoopButton =     '';
        if(data[0].value!=$scope.lLoopLatest)
          console.log("lLoop had unknown status '"+data[0].value+"'");
      }
      $scope.lLoopLatest = data[0].value;
    }

    // // //
    // TUNING

    function
    getControls()
    {
      findAutoRelease();
      findAutoExpress();
      findLiveLoad();
      findRecircTrigger();
      findStagingFill();
      findPnA() ;
      findCommit() ;
      findStandard() ;
      findDuct() ;
      findHLength() ;
      findJLength() ;
      findKLength() ;

      findWaveFail() ;
      findHFail() ;
      findJFail() ;
      findKFail() ;
      findShipFail() ;
      findLoopFail() ;
      findShipScanFail1() ;
      findShipScanFail2() ;
      findShipScanFail3() ;
      findShipScanFail4() ;
      findShipScanFail5() ;
      findShipScanFail6() ;
      findShipScanFail7() ;
      findShipScanFail8() ;
      findShipScanFail9() ;
      findLoopScanFail7() ;
      findLoopScanFail8() ;
      findLoopScanFail9() ;
    }


    function
    findLoopScanFail7()
    {
      DbFactory.post({topic: 'ops',
                      action: 'read',
                      params: {zone: 'consec',
                               name: 'loopLaneScan_7'}})
        .success(findLoopScanFail7Success)
        .error(function(err){console.log(err);});
    }
    function
    findLoopScanFail7Success(data)
    { $scope.edit.loop7Fail = parseInt(data[0].value); }

    function
    findLoopScanFail8()
    {
      DbFactory.post({topic: 'ops',
                      action: 'read',
                      params: {zone: 'consec',
                               name: 'loopLaneScan_8'}})
        .success(findLoopScanFail8Success)
        .error(function(err){console.log(err);});
    }
    function
    findLoopScanFail8Success(data)
    { $scope.edit.loop8Fail = parseInt(data[0].value); }


    function
    findLoopScanFail9()
    {
      DbFactory.post({topic: 'ops',
                      action: 'read',
                      params: {zone: 'consec',
                               name: 'loopLaneScan_9'}})
        .success(findLoopScanFail9Success)
        .error(function(err){console.log(err);});
    }
    function
    findLoopScanFail9Success(data)
    { $scope.edit.loop9Fail = parseInt(data[0].value); }



    function
    findShipScanFail1()
    {
      DbFactory.post({topic: 'ops',
                      action: 'read',
                      params: {zone: 'consec',
                               name: 'shipLaneScan_1'}})
        .success(findShipScanFail1Success)
        .error(function(err){console.log(err);});
    }
    function
    findShipScanFail1Success(data)
    { $scope.edit.ship1Fail = parseInt(data[0].value); }

    function
    findShipScanFail2()
    {
      DbFactory.post({topic: 'ops',
                      action: 'read',
                      params: {zone: 'consec',
                               name: 'shipLaneScan_2'}})
        .success(findShipScanFail2Success)
        .error(function(err){console.log(err);});
    }
    function
    findShipScanFail2Success(data)
    { $scope.edit.ship2Fail = parseInt(data[0].value); }



    function
    findShipScanFail3()
    {
      DbFactory.post({topic: 'ops',
                      action: 'read',
                      params: {zone: 'consec',
                               name: 'shipLaneScan_3'}})
        .success(findShipScanFail3Success)
        .error(function(err){console.log(err);});
    }
    function
    findShipScanFail3Success(data)
    { $scope.edit.ship3Fail = parseInt(data[0].value); }

    function
    findShipScanFail4()
    {
      DbFactory.post({topic: 'ops',
                      action: 'read',
                      params: {zone: 'consec',
                               name: 'shipLaneScan_4'}})
        .success(findShipScanFail4Success)
        .error(function(err){console.log(err);});
    }
    function
    findShipScanFail4Success(data)
    { $scope.edit.ship4Fail = parseInt(data[0].value); }

    function
    findShipScanFail5()
    {
      DbFactory.post({topic: 'ops',
                      action: 'read',
                      params: {zone: 'consec',
                               name: 'shipLaneScan_5'}})
        .success(findShipScanFail5Success)
        .error(function(err){console.log(err);});
    }
    function
    findShipScanFail5Success(data)
    { $scope.edit.ship5Fail = parseInt(data[0].value); }

    function
    findShipScanFail6()
    {
      DbFactory.post({topic: 'ops',
                      action: 'read',
                      params: {zone: 'consec',
                               name: 'shipLaneScan_6'}})
        .success(findShipScanFail6Success)
        .error(function(err){console.log(err);});
    }
    function
    findShipScanFail6Success(data)
    { $scope.edit.ship6Fail = parseInt(data[0].value); }

    function
    findShipScanFail7()
    {
      DbFactory.post({topic: 'ops',
                      action: 'read',
                      params: {zone: 'consec',
                               name: 'shipLaneScan_7'}})
        .success(findShipScanFail7Success)
        .error(function(err){console.log(err);});
    }
    function
    findShipScanFail7Success(data)
    { $scope.edit.ship7Fail = parseInt(data[0].value); }

    function
    findShipScanFail8()
    {
      DbFactory.post({topic: 'ops',
                      action: 'read',
                      params: {zone: 'consec',
                               name: 'shipLaneScan_8'}})
        .success(findShipScanFail8Success)
        .error(function(err){console.log(err);});
    }
    function
    findShipScanFail8Success(data)
    { $scope.edit.ship8Fail = parseInt(data[0].value); }

    function
    findShipScanFail9()
    {
      DbFactory.post({topic: 'ops',
                      action: 'read',
                      params: {zone: 'consec',
                               name: 'shipLaneScan_9'}})
        .success(findShipScanFail9Success)
        .error(function(err){console.log(err);});
    }
    function
    findShipScanFail9Success(data)
    { $scope.edit.ship9Fail = parseInt(data[0].value); }



    function
    findWaveFail()
    {
      DbFactory.post({topic: 'ops',
                      action: 'read',
                      params: {zone: 'consec',
                               name: 'waveSorterScan'}})
        .success(findWaveFailSuccess)
        .error(function(err){console.log(err);});
    }

    function
    findWaveFailSuccess(data)
    { $scope.edit.waveFail = parseInt(data[0].value); }

    function
    findHFail()
    {
      DbFactory.post({topic: 'ops',
                      action: 'read',
                      params: {zone: 'consec',
                               name: 'h-loopSorterScan'}})
        .success(findHFailSuccess)
        .error(function(err){console.log(err);});

    }
    function
    findHFailSuccess(data)
    { $scope.edit.hscanFail = parseInt(data[0].value); }

    function
    findJFail()
    {
      DbFactory.post({topic: 'ops',
                    action: 'read',
                    params: {zone: 'consec',
                             name: 'j-loopSorterScan'}})
      .success(findJFailSuccess)
      .error(function(err){console.log(err);});

    }
    function
    findJFailSuccess(data)
    { $scope.edit.jscanFail = parseInt(data[0].value); }

    function
    findKFail()
    {
      DbFactory.post({topic: 'ops',
                    action: 'read',
                    params: {zone: 'consec',
                             name: 'k-loopSorterScan'}})
      .success(findKFailSuccess)
      .error(function(err){console.log(err);});

    }
    function
    findKFailSuccess(data)
    { $scope.edit.kscanFail = parseInt(data[0].value); }

    function
    findShipFail()
    {
      DbFactory.post({topic: 'ops',
                    action: 'read',
                    params: {zone: 'consec',
                             name: 'shipSorterScan'}})
      .success(findShipFailSuccess)
      .error(function(err){console.log(err);});

    }
    function
    findShipFailSuccess(data)
    { $scope.edit.shipFail = parseInt(data[0].value); }

    function
    findLoopFail()
    {
      DbFactory.post({topic: 'ops',
                    action: 'read',
                    params: {zone: 'consec',
                             name: 'loopSorterScan'}})
      .success(findLoopFailSuccess)
      .error(function(err){console.log(err);});

    }
    function
    findLoopFailSuccess(data)
    { $scope.edit.lscanFail = parseInt(data[0].value); }

    function
    findHLength()
    {
      DbFactory.post({topic: 'ops',
                      action: 'read',
                      params: {zone: 'h-sort',
                               name: 'max'}})
        .success(findHLengthSuccess)
        .error(function(err){console.log(err);});
    }

    function
    findJLength()
    {
      DbFactory.post({topic: 'ops',
                      action: 'read',
                      params: {zone: 'j-sort',
                               name: 'max'}})
        .success(findJLengthSuccess)
        .error(function(err){console.log(err);});
    }

    function
    findKLength()
    {
      DbFactory.post({topic: 'ops',
                      action: 'read',
                      params: {zone: 'k-sort',
                               name: 'max'}})
        .success(findKLengthSuccess)
        .error(function(err){console.log(err);});
    }


    function
    findDuct()
    {
      DbFactory.post({topic: 'ops',
                      action: 'read',
                      params: {zone: 'l-loop',
                               name: 'duct_enable'}})
        .success(findDuctSuccess)
        .error(function(err){console.log(err);});
    }

    function
    findPnA()
    {
      DbFactory.post({topic: 'ops',
                      action: 'read',
                      params: {zone: 'wv-sort',
                               name: 'print_apply_lane'}})
        .success(findPnASuccess)
        .error(function(err){console.log(err);});
    }

    function
    findCommit()
    {
      DbFactory.post({topic: 'ops',
                      action: 'read',
                      params: {zone: 'wv-sort',
                               name: 'emergency_lane'}})
        .success(findCommitSuccess)
        .error(function(err){console.log(err);});
    }

    function
    findStandard()
    {
      DbFactory.post({topic: 'ops',
                      action: 'read',
                      params: {zone: 'wv-sort',
                               name: 'shipping_lane'}})
        .success(findStandardSuccess)
        .error(function(err){console.log(err);});
    }


    function
    findAutoRelease()
    {
      DbFactory.post({topic: 'ops',
                      action: 'read',
                      params: {zone: 'system',
                               name: 'autoRelease'}})
        .success(findAutoReleaseSuccess)
        .error(function(err){console.log(err);});
    }
    function
    findAutoExpress()
    {
      DbFactory.post({topic: 'ops',
                      action: 'read',
                      params: {zone: 'wave',
                               name: 'express'}})
        .success(findAutoExpressSuccess)
        .error(function(err){console.log(err);});
    }
    function
    findLiveLoad()
    {
      DbFactory.post({topic: 'ops',
                      action: 'read',
                      params: {zone: 'system',
                               name: 'liveLoadEnabled'}})
        .success(findLiveLoadSuccess)
        .error(function(err){console.log(err);});
    }

    function
    findRecircTrigger()
    {
      DbFactory.post({topic: 'ops',
                      action: 'read',
                      params: {zone: 'shp-sort',
                               name: 'recircTrigger'}})
        .success(findRecircTriggerSuccess)
        .error(function(err){console.log(err);});
    }

    function
    findStagingFill()
    {
      DbFactory.post({topic: 'ops',
                      action: 'read',
                      params: {zone: 'wave',
                               name: 'stagingFill'}})
        .success(findStagingFillSuccess)
        .error(function(err){console.log(err);});
    }

    function
    findAutoReleaseSuccess(data)
    { $scope.edit.autoRelease = (data[0].value=='on')?true:false; }

    function
    findAutoExpressSuccess(data)
    { $scope.edit.autoExpress = (data[0].value=='on')?true:false; }

    function
    findLiveLoadSuccess(data)
    { $scope.edit.liveLoad = (data[0].value=='yes' || data[0].value=='true')?true:false; }

    function
    findRecircTriggerSuccess(data)
    { $scope.edit.recircTrigger = parseInt(data[0].value); }

    function
    findStagingFillSuccess(data)
    { $scope.edit.stagingFill = parseInt(data[0].value); }

    function
    findPnASuccess(data)
    { $scope.edit.printApply = parseInt(data[0].value); }

    function
    findCommitSuccess(data)
    { $scope.edit.committed = parseInt(data[0].value); }

    function
    findStandardSuccess(data)
    { $scope.edit.standardLane = parseInt(data[0].value); }

    function
    findDuctSuccess(data)
    { $scope.edit.ductEnable = (data[0].value=='yes' || data[0].value=='true')?true:false; }

    function
    findHLengthSuccess(data)
    { $scope.edit.hLength = parseInt(data[0].value); }
    function
    findJLengthSuccess(data)
    { $scope.edit.jLength = parseInt(data[0].value); }
    function
    findKLengthSuccess(data)
    { $scope.edit.kLength = parseInt(data[0].value); }

    // // // // //
    // PACK CONFIG

    function
    editDisabled()
    {
      return !($scope.configTable && $scope.configTable.configName);
    }

    function
    packconfigSaveNew()
    { packconfigSave($scope.configTable.configName,refreshConfig); }

    function
    packconfigSave(name,callback)
    {
      console.log('save ' + name);
      var logData = 'saved new pack configuration ' + name ;
      DbFactory.post({topic: 'packConfig',
                      action: 'saveNew',
                      log: logData,
                      params: {configName: name}
                     })
        .success(callback)
        .error(function(err){console.log(err);});
    }

    function
    packconfigLoad()
    {
      var name = $scope.configTable.configName;
      console.log('load ' + name);
      var logData = 'loaded stored pack configuration ' + name ;
      DbFactory.post({topic:'packConfig',
                      action:'load',
                      log: logData,
                      params: {configName: $scope.configTable.configName}
                     })
        .success(refreshConfig)
        .error(function(err){console.log(err);});
    }

    function
    packconfigDelete()
    {
      var name = $scope.configTable.configName;
      var logData = 'deleted pack configuration ' + name ;
      console.log('delete ' + name);
      DbFactory.post({topic:'packConfig',
                      action:'delete',
                      log: logData,
                      params: {configName: $scope.configTable.configName}
                     })
        .success(refreshConfig)
        .error(function(err){console.log(err);});
    }

    function
    refreshConfig()
    {
      DbFactory.post({topic:'packConfig',
                      action:'all'
                     })
        .success(configSuccess)
        .error(function(err){console.log(err);});
    }
    
    function
    refreshConfigWithButtonReset()
    {
      DbFactory.post({topic:'packConfig',
                      action:'all'
                     })
        .success(function(data){
          configSuccess(data);
          hLoopHide();
          jLoopHide();
          kLoopHide();
        })
        .error(function(err){console.log(err);});
    }

    function
    configSuccess(data)
    {
      var cols = [];
      var ref = "#configTable";
      cols.push({title: "Config Name",  data:"configName",
                                        class:'dt-center'});
      cols.push({title: "Lane Count",   data:"activeCount",
                                        class:'dt-center'});
      cols.push({title: "Changed",      data:"stamp",
                                        render:stampRender,
                                        class:'dt-center'});

      if(configTable) {
        configTable.clear();
        configTable.rows.add(data);
        configTable.draw();
      } else {
        configTable = $(ref)
          .DataTable({data: data,
                      columns: cols,
                      scrollY: "1100px",
                      scrollCollapse: true,
                      paging: false,
                      dom: 'ltBfipr',
                      buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','tr',packConfigClick);
        $timeout(function(){configTable.draw();},0);
        $scope.configTable = configTable;
      }
    }

    function
    packConfigClick() {
      var data = configTable.row(this).data();
      console.log('click ' + data.configName);
      $scope.configTable.configName = data.configName;

      $scope.$apply();
    }

    function
    stampRender(data,type,full,meta)
    {
      if(type!='display')
        return data;

      if(data==null)
        return '';

      var date = new Date(data);
      var today = new Date();
      if(today.toDateString()==date.toDateString())
        return date.toLocaleTimeString();

      return date.toLocaleString();
    }


    // // // // //
    // LANE CAPACITY

    var waveLaneCapsTable;
    var shipLaneCapsTable;

    $scope.laneEdit = {};
//    $scope.editNew = editNew;

    $scope.waveLane_editDisable = waveLane_editDisable;
    $scope.waveLane_editUpdate  = waveLane_editUpdate;
//    $scope.waveLane_editDelete  = waveLane_editDelete;

    $scope.shipLane_editDisable = shipLane_editDisable;
    $scope.shipLane_editUpdate  = shipLane_editUpdate;
//    $scope.shipLane_editDelete  = shipLane_editDelete;

    /*function
    editNew()
    { $scope.laneEdit = {}; }*/

    function
    laneUpdateSuccess()
    {
      Global.busyRefresh(false);
      $timeout(refresh,1000);
    }

    function
    laneUpdateError(err)
    {
      console.log(err);
      Global.busyRefresh(false);
    }

    // // //
    // WAVE LANES

    function
    refreshWaveLaneCaps()
    {
      DbFactory.post({topic:'waveLane',
                      action:'capacities'
                     })
        .success(waveLaneCapsSuccess)
        .error(function(err){console.log(err);});
    }

    function
    waveLaneCapsSuccess(data)
    {
      var cols = [];
      var ref = "#waveLaneCaps";
      cols.push({title: "Lane",     data:"lane",
                                    class:'dt-center'});
      cols.push({title: "Capacity", data:"cartonCapacity",
                                    class:'dt-center'});

      if(waveLaneCapsTable) {
        waveLaneCapsTable.clear();
        waveLaneCapsTable.rows.add(data);
        waveLaneCapsTable.draw();
      } else {
        waveLaneCapsTable = $(ref)
          .DataTable({data: data,
                      columns: cols,
                      scrollY: "1100px",
                      scrollCollapse: true,
                      paging: false,
                      dom: 'ltBfipr',
                      buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','tr',waveLaneCapsClick);
        $timeout(function(){waveLaneCapsTable.draw();},0);
        $scope.waveLaneCapsTable = waveLaneCapsTable;
      }
    }

    function
    waveLaneCapsClick()
    {
      var data = waveLaneCapsTable.row(this).data();
      $scope.laneEdit.waveLane = data.lane;
      $scope.laneEdit.waveLaneCapacity = data.cartonCapacity;
      $scope.$apply();
    }

    function
    waveLane_editDisable()
    { return !$scope.laneEdit.waveLane; }

    function
    waveLane_editUpdate()
    {
      if($scope.laneEdit.waveLane){
        var logData = 'lane ' + $scope.laneEdit.shipLane
                        + ' capacity set to  ' +
                        $scope.laneEdit.shipLaneCapacity ;

        Global.busyRefresh(true);
        DbFactory.post({topic: 'waveLane',
                        action: 'update',
                        log: logData,
                        params: {lane: $scope.laneEdit.waveLane,
                                 capacity: $scope.laneEdit.waveLaneCapacity || 0}
                       })
          .success(laneUpdateSuccess)
          .error(laneUpdateError)
      }
    }

    /*function
    waveLane_editDelete()
    {
      if($scope.laneEdit.waveLane){
        Global.busyRefresh(true);
        DbFactory.post({topic: 'waveLane',
                        action: 'delete',
                        params: {lane: $scope.laneEdit.waveLane}
                       })
          .success(laneUpdateSuccess)
          .error(laneUpdateError)
        editNew();
      }
    }*/

    // // //
    // SHIP LANES

    function
    refreshShipLaneCaps()
    {
      DbFactory.post({topic:'shipLane',
                      action:'capacities'
                     })
        .success(shipLaneCapsSuccess)
        .error(function(err){console.log(err);});
    }

    function
    shipLaneCapsSuccess(data)
    {
      var cols = [];
      var ref = "#shipLaneCaps";
      cols.push({title: "Lane",     data:"lane",
                                    class:'dt-center'});
      cols.push({title: "Capacity", data:"capacity",
                                    class:'dt-center'});

      if(shipLaneCapsTable) {
        shipLaneCapsTable.clear();
        shipLaneCapsTable.rows.add(data);
        shipLaneCapsTable.draw();
      } else {
        shipLaneCapsTable = $(ref)
          .DataTable({data: data,
                      columns: cols,
                      scrollY: "1100px",
                      scrollCollapse: true,
                      paging: false,
                      dom: 'ltBfipr',
                      buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','tr',shipLaneCapsClick);
        $timeout(function(){shipLaneCapsTable.draw();},0);
        $scope.shipLaneCapsTable = shipLaneCapsTable;
      }
    }

    function
    shipLaneCapsClick()
    {
      var data = shipLaneCapsTable.row(this).data();
      $scope.laneEdit.shipLane = data.lane;
      $scope.laneEdit.shipLaneCapacity = data.capacity;
      $scope.$apply();
    }

    function
    shipLane_editDisable()
    { return !$scope.laneEdit.shipLane; }

    function
    shipLane_editUpdate()
    {
      if($scope.laneEdit.shipLane){
        var logData = 'lane ' + $scope.laneEdit.shipLane
                        + ' capacity set to  ' +
                        $scope.laneEdit.shipLaneCapacity ;
        Global.busyRefresh(true);
        DbFactory.post({topic: 'shipLane',
                        action: 'update',
                        log: logData,
                        params: {lane: $scope.laneEdit.shipLane,
                                 capacity: $scope.laneEdit.shipLaneCapacity || 0}
                       })
          .success(laneUpdateSuccess)
          .error(laneUpdateError)
      }
    }

    /*function
    shipLane_editDelete()
    {
      if($scope.laneEdit.shipLane){
        Global.busyRefresh(true);
        DbFactory.post({topic: 'shipLane',
                        action: 'delete',
                        params: {lane: $scope.laneEdit.shipLane}
                       })
          .success(laneUpdateSuccess)
          .error(laneUpdateError)
        editNew();
      }
    }*/

    // // // // //

    function
    refresh()
    {
      if($scope.selected < 2)
        getSystemStatus();
      if($scope.selected==2)
        refreshConfig();
      if($scope.selected==3)
        refreshWaveLaneCaps();
      if($scope.selected==4)
        refreshShipLaneCaps();
    }

    function
    init()
    {
      Global.setTitle('Ops');
      Global.recv('refresh',refresh,$scope);
      periodic = $interval(refresh,1000);
      getControls();
    }

    $scope.$on('$destroy',function(){
      $interval.cancel(periodic);
    });

    init();
  }

  function
  opsConfig($routeProvider)
  {
    $routeProvider
      .when('/ops',{controller: 'OpsController',
                    templateUrl: '/app/ops/ops.view.html'});
  }

}())
