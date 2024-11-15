(
function()
  {
  angular
    .module('ui')
      .controller('WaveController',waveController) ;

  angular
    .module('ui')
      .config(waveConfig) ;

  waveController.$inject = ['$scope','$timeout','$routeParams','$mdDialog',
                            'Global','DbFactory'] ;

  function
  waveController($scope,$timeout,$routeParams,$mdDialog,Global,DbFactory)
    {
    var wave ;
    var waveData ;
    var delivery ;
    var waveHistory ;
    var deliveriesData ;
    var deliveries ;
    var allCarton ;
    var pickCarton ;
    var shipCarton ;
    var deliveryHistory ;
    var cartonHistory ;
    var stage ;

    $scope.permit = Global.permit ;
    $scope.selected ;
    $scope.refresh = refresh ;
    $scope.wave = '';
    $scope.waveDetail = {};
    $scope.waveLookup = '' ;
    $scope.doWaveLookup = doWaveLookup ;
    $scope.stepSelect = 0 ;
    $scope.steps = [] ;
    $scope.display = display ;
    $scope.delStepSelect = 0 ;
    $scope.delSteps = [] ;
    $scope.deliveriesDisplay = deliveriesDisplay ;
    $scope.deliveryId = '' ;
    $scope.subOrder = '' ;
    $scope.deliveryDetail = {} ;
    $scope.seq ='';
    $scope.cartonDetail ;
    $scope.deliveryLookup ;
    $scope.doDeliveryLookup = doDeliveryLookup ;
    $scope.cartonLookup ;
    $scope.doCartonLookup = doCartonLookup ;
    $scope.manualReject = manualReject ;
    $scope.packingCompleted = packingCompleted ;
    $scope.cartonIgnore = cartonIgnore ;

    function
    smallRender(data,type,full,meta)
      {
      if(type!='display')
        return data ;

      if(data==null)
        return '' ;

      var date = new Date(data) ;

      var M = '' + (date.getMonth() + 1) ;
      if(M.length<2) M = '0'+M ;

      var D = '' + (date.getDate()) ;
      if(D.length<2) D = '0'+D ;

      var h = '' + (date.getHours()) ;
      if(h.length<2) h = '0'+h ;

      var m = '' + (date.getMinutes()) ;
      if(m.length<2) m = '0'+m ;

      var small = M + '/' + D + ' ' + h + ':' + m ;
      return small ;
      }

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
    simpleDate(data)
      {
      if(data==null)
        return '' ;
      var date = new Date(data) ;
      var today = new Date() ;
      if(today.toDateString()==date.toDateString())
        return date.toLocaleTimeString() ;
      return date.toLocaleString() ;
      }

    function
    checkRight(row,col,tip)
      {
      $('td:eq('+col+')',row)
        .html( '<div class="righttip">'
             + '&check;'
             + '<span class="righttiptext">'
             + simpleDate(tip)
             + '</span>'
             + '</div>' )  ;
      }

    function
    checkLeft(row,col,tip)
      {
      $('td:eq('+col+')',row)
        .html( '<div class="lefttip">'
             + '&check;'
             + '<span class="lefttiptext">'
             + simpleDate(tip)
             + '</span>'
             + '</div>' )  ;
      }

    function
    doWaveLookup()
      {
      if($scope.waveLookup)
        {
        $scope.wave = $scope.waveLookup ;
        $scope.waveLookup = null ;
        $scope.refresh() ;
        }
      }


    function
    waveSuccess(data)
      {
      waveData = data ;
      display() ;
      }

    function
    initSteps()
      {
      $scope.steps.push({id:0, name:'All waves'}) ;
      $scope.steps.push({id:1, name:'Created, Authorization not Requested'}) ;
      $scope.steps.push({id:2, name:'Authorization Requested'}) ;
      $scope.steps.push({id:3, name:'Pick Authorized, not yet Started'}) ;
      $scope.steps.push({id:4, name:'Pick Started, not yet Complete'}) ;
      $scope.steps.push({id:5, name:'Pick Complete'}) ;
      }


    function
    waveFilter(input)
      {
      var output = [] ;
      for(var i=0 ; i<input.length ; i++)
        {
        var keep = true ;

        if($scope.stepSelect==1)
          if(!(input[i].created && !input[i].pickingAuthRequested))
            keep = false ;

        if($scope.stepSelect==2)
          if(!(input[i].pickingAuthRequested && !input[i].pickingAuthorized))
            keep = false ;

        if($scope.stepSelect==3)
          if(!(input[i].pickingAuthorized && !input[i].pickingStarted))
            keep = false ;

        if($scope.stepSelect==4)
          if(!(input[i].pickingStarted && !input[i].pickingCompleted))
            keep = false ;

        if($scope.stepSelect==5)
          if(!input[i].pickingCompleted)
            keep = false  ;
        if(keep)
          output.push(input[i]) ;
        }
      return output ;
      }

    function
    display()
      {
      var data = waveFilter(waveData) ;

      for(var i=0 ; i<data.length ; i++)
        {
        if(data[i].pickingAuthorized)
          data[i].authorization = "Authorized" ;
        else if(data[i].pickingAuthRequested)
          data[i].authorization = "Requested" ;
        else
          data[i].authorization = "Waiting" ;

        if(data[i].pickingCompleted)
          data[i].picking = "Completed" ;
        else if(data[i].pickingStarted)
          data[i].picking = "Started" ;
        else
          data[i].picking = "Waiting" ;

       if(data[i].lengthSet)
         data[i].lengthView = Number(data[i].estLength).toFixed(2)  ;
       else
         data[i].lengthView = "Not Available" ;
        }

      var cols = [] ;
      var ref = "#wave" ;

      cols.push({title:'Wave',            data:'waveShow'}) ;
      cols.push({title:'Created',         data:'created',
                                          render:smallRender,
                                          class: 'dt-center'}) ;
      cols.push({title:'Authorization',   data:'authorization',
                                          class: 'dt-center'}) ;
      cols.push({title:'Picking',         data:'picking',
                                          class: 'dt-center'}) ;
      cols.push({title:'Released',        data:'waveReleased',
                                          render: smallRender,
                                          class: 'dt-center'}) ;
      cols.push({title:'Length Set',      data:'lengthView',
                                          class: 'dt-right'}) ;
      cols.push({title:'Comment',      data:'comment',
                                          class: 'dt-right'}) ;

      if(wave)
        {
        wave.clear() ;
        wave.rows.add(data)
        wave.draw() ;
        }
      else
        {
        wave = $(ref)
          .DataTable({data: data,
                      columns: cols,
                      rowCallback: waveCallback,
                      order: [[0,'asc']],
                      scrollX: "100%",
                      scrollY: "500px",
                      scrollCollapse: true,
                      paging: false,
                      dom: 'lftBipr',
                      buttons: ['copy','print','excel','pdf']}) ;
        $(ref+' tbody').on('click','td',waveClick) ;
        $timeout(function(){wave.draw()},0) ;
        }
      }

    function
    waveCallback(row,data,index)
      {
      if(data.authorization=='Authorized')
        $("td:eq(2)",row).css('background-color','#99ff99') ;
      if(data.authorization=='Requested')
        $("td:eq(2)",row).css('background-color','#ffff33') ;

      if(data.picking=='Completed')
        $("td:eq(3)",row).css('background-color','#99ff99') ;
      if(data.picking=='Started')
        $("td:eq(3)",row).css('background-color','#ffff33') ;
      }

    function
    waveClick()
      {
      var row = wave.row(this).data() ;
      $scope.wave = row.wave ;
      $scope.selected = 1 ;
      $scope.$apply() ;
      }

    function
    deliverySuccess(data)
      {
      var cols = [] ;
      var ref = "#delivery" ;

      for(var i=0 ; i<data.length ; i++)
        {
        if(data[i].pickingCompleted)
          data[i].picking = 'Completed' ;
        else if(data[i].pickingStarted)
          data[i].picking = 'Started' ;
        else
          data[i].picking = 'Waiting' ;

        if(data[i].packingCompleted)
          data[i].packing = 'Completed' ;
        else
          data[i].packing = 'Waiting' ;
        }

      cols.push({title:'Delivery', data:'delivery'}) ;
 //     cols.push({title:'Wave', data:'wave'}) ;
      cols.push({title:'Created', data:'created',
                                  render:smallRender,
                                  class:'dt-center'}) ;
      cols.push({title:'Picking', data:'picking',
                                  class:'dt-center'}) ;
      cols.push({title:'Packing', data:'packing',
                                  class:'dt-center'}) ;
      cols.push({title:'Pallet Pack', data:'isPalletPack',
                                      class:'dt-center'}) ;
      cols.push({title:'Live Load', data:'isLiveLoad',
                                    class:'dt-center'}) ;
      cols.push({title:'Parcel', data:'isParcel',
                                 class:'dt-center'}) ;
      cols.push({title:'Commited', data:'isCommittedOrder',
                                 class:'dt-center'}) ;
      cols.push({title:'Length', data:'deliveryLength',
                                 class:'dt-right'}) ;
      cols.push({title:'Changed', data:'stamp',
                                  render:smallRender,
                                  class:'dt-center'}) ;
      cols.push({title:'Comment', data:'comment',
                                 class:'dt-right'}) ;

      if(delivery)
        {
        delivery.clear() ;
        delivery.rows.add(data) ;
        delivery.draw() ;
        }
      else
        {
        delivery = $(ref)
          .DataTable({data: data,
                      columns: cols,
                      rowCallback: deliveryCallback,
                      order: [[0,'asc'],[1,'asc']],
                      scrollX: "100%",
                      scrollY: "300px",
                      scrollCollapse: true,
                      paging: false,
                      dom: 'lftBipr',
                      buttons: ['copy','print','excel','pdf']}) ;
        $(ref+' tbody').on('click','td',deliveryClick) ;
        $timeout(function(){delivery.draw()},0) ;
        }
      }

    function
    deliveryCallback(row,data,index)
      {
      if(data.picking=='Completed')
        $("td:eq(2)",row).css('background-color','#99ff99') ;
      if(data.picking=='Started')
        $("td:eq(2)",row).css('background-color','#ffff33') ;

      if(data.packing=='Completed')
        $("td:eq(3)",row).css('background-color','#99ff99') ;
      }

    function
    deliveryClick()
      {
      var row = delivery.row(this).data() ;
      $scope.deliverySeq = row.seq ;
      $scope.selected = 3 ;
      $scope.$apply() ;
      }

    function
    waveHistorySuccess(data)
      {
      var cols = [] ;
      var ref = "#waveHistory" ;

      cols.push({title:'Code', data:'code'}) ;
      cols.push({title:'Description', data:'description'}) ;
      cols.push({title:'Stamp', data:'stamp',
                                render: stampRender}) ;

      if(waveHistory)
        {
        waveHistory.clear() ;
        waveHistory.rows.add(data) ;
        waveHistory.draw() ;
        }
      else
        {
        waveHistory = $(ref)
          .DataTable({data: data,
                      columns: cols,
                      scrollX: "100%",
                      scrollY: "300px",
                      scrollCollapse: true,
                      paging: false,
                      dom: 'lftBipr',
                      buttons: ['copy','print','excel','pdf']}) ;
        $timeout(function(){waveHistory.draw()},0) ;
        }
      }

    function
    waveDetailSuccess(data)
      {
      $scope.waveDetail = data[0] ;

      if(!$scope.waveDetail)
        Global.showMessage("Wave Not Found") ;

      if($scope.wave)
        {
        DbFactory
          .post({topic: 'delivery', action: 'byWave',
                 params: {wave: $scope.wave}})
            .success(deliverySuccess)
            .error(function(){console.log('delivery detail error');}) ;

        DbFactory
          .post({topic: 'wave', action:'log',
                 params: {id: $scope.wave}})
            .success(waveHistorySuccess)
            .error(function(){console.log('wave history error');}) ;
        }
      }

    function
    initDelSteps()
      {
      $scope.delSteps.push({id:0, name:'All Deliveries'}) ;
      $scope.delSteps.push({id:1, name:'Created, Picking Not Started'}) ;
      $scope.delSteps.push({id:2, name:'Picking Started, not yet Completed'}) ;
      $scope.delSteps.push({id:3, name:'Picking Completed, not yet Packed'}) ;
      $scope.delSteps.push({id:4, name:'Packing Completed'}) ;
      }

    function
    deliveriesFilter(input)
      {
      var output = [] ;
      for(var i=0 ; i<input.length ; i++)
        {
        var keep = true ;

        if($scope.delStepSelect==1)
          if(!(input[i].created && !input[i].pickingStarted))
            keep = false ;

        if($scope.delStepSelect==2)
          if(!(input[i].pickingStarted && !input[i].pickingCompleted))
            keep = false ;

        if($scope.delStepSelect==3)
          if(!(input[i].pickingCompleted && !input[i].packingCompleted))
            keep = false ;

        if($scope.delStepSelect==4)
          if(!input[i].packingCompleted)
            keep = false  ;

        if(keep)
          output.push(input[i]) ;
        }

      return output ;
      }

    function
    deliveriesSuccess(data)
      {
      deliveriesData = data ;
      deliveriesDisplay() ;
      }

    function
    deliveriesDisplay()
      {
      var data = deliveriesFilter(deliveriesData) ;

      for(var i=0 ; i<data.length ; i++)
        {
        if(data[i].pickingCompleted)
          data[i].picking = "Completed" ;
        else if(data[i].pickingStarted)
          data[i].picking = "Started" ;
        else
          data[i].picking = "Waiting" ;

        if(data[i].packingCompleted)
          data[i].packing = "Completed" ;
        else
          data[i].packing = "Waiting" ;
        }

      var cols = [] ;
      var ref = "#deliveries" ;

      cols.push({title:'Delivery',    data:'delivery'}) ;
      cols.push({title:'Wave',        data:'wave'}) ;
      cols.push({title:'Created',     data:'created',
                                      render:smallRender,
                                      class: 'dt-center'}) ;
      cols.push({title:'Picking',     data:'picking',
                                      class: 'dt-center'}) ;
      cols.push({title:'Packing',     data:'packing',
                                      class: 'dt-center'}) ;
      cols.push({title:'Pallet Pack', data:'isPalletPack',
                                      class: 'dt-center'}) ;
      cols.push({title:'Live Load',   data:'isLiveLoad',
                                      class: 'dt-center'}) ;
      cols.push({title:'Parcel',      data: 'isParcel',
                                      class: 'dt-center'}) ;
      cols.push({title:'Commited',    data:'isCommittedOrder',
                                      class: 'dt-center'}) ;
      cols.push({title:'Changed',     data:'stamp',
                                      render:smallRender,
                                      class: 'dt-center'}) ;
      cols.push({title:'Comment',     data:'comment',
                                      class: 'dt-center'}) ;

      if(deliveries)
        {
        deliveries.clear() ;
        deliveries.rows.add(data)
        deliveries.draw() ;
        }
      else
        {
        deliveries = $(ref)
          .DataTable({data: data,
                      columns: cols,
                      rowCallback: deliveriesCallback,
                      order: [[0,'asc']],
                      scrollX: "100%",
                      scrollY: "500px",
                      scrollCollapse: true,
                      paging: true,
                      pageLength: 25,
                      dom: 'lftBipr',
                      buttons: ['copy','print','excel','pdf']}) ;
        $(ref+' tbody').on('click','td',deliveriesClick) ;
        $timeout(function(){deliveries.draw()},0) ;
        }
      }

    function
    deliveriesCallback(row,data,index)
      {
      if(data.picking=='Completed')
        $("td:eq(3)",row).css('background-color','#99ff99') ;
      if(data.picking=='Started')
        $("td:eq(3)",row).css('background-color','#ffff33') ;

      if(data.packing=='Completed')
        $("td:eq(4)",row).css('background-color','#99ff99') ;
      }

    function
    deliveriesClick()
      {
      var row = deliveries.row(this).data() ;
      $scope.deliverySeq = row.seq ;
      $scope.selected = 3 ;
      $scope.$apply() ;
      }

    function
    deliveryDetailSuccess(data)
      {
      $scope.deliveryDetail = data[0] ;

      if(!$scope.deliveryDetail)
        Global.showMessage("Delivery Not Found") ;

      if($scope.deliverySeq)
        {
        DbFactory //new
          .post({topic: 'cartonContainer', action: 'byDelivery',
                 params: {deliverySeq: $scope.deliverySeq}})
            .success(allCartonSuccess)
            .error(function(err){console.log(err);}) ;

        DbFactory
          .post({topic: 'carton', action: 'byDelivery',
                 params: {deliverySeq: $scope.deliverySeq}})
            .success(pickCartonSuccess)
            .error(function(){console.log('pickCarton byDelivery error');}) ;

        DbFactory //new
          .post({topic: 'container', action: 'byDelivery',
                 params: {deliverySeq: $scope.deliverySeq}})
            .success(shipCartonSuccess)
            .error(function(){console.log('shipCarton byDelivery error');}) ;

        DbFactory
          .post({topic: 'delivery', action: 'log',
                 params: {id: $scope.deliverySeq}})
            .success(deliveryHistorySuccess)
            .error(function(){console.log('delivery history error');}) ;
        }
      }

    function
    deliveryHistorySuccess(data)
      {
      var cols = [] ;
      var ref = "#deliveryHistory" ;

      cols.push({title:'Code', data:'code'}) ;
      cols.push({title:'Description', data:'description'}) ;
      cols.push({title:'Stamp', data:'stamp',
                                render: stampRender}) ;

      if(deliveryHistory)
        {
        deliveryHistory.clear() ;
        deliveryHistory.rows.add(data) ;
        deliveryHistory.draw() ;
        }
      else
        {
        deliveryHistory = $(ref)
          .DataTable({data: data,
                      columns: cols,
                      scrollX: "100%",
                      scrollY: "300px",
                      scrollCollapse: true,
                      paging: false,
                      dom: 'lftBipr',
                      buttons: ['copy','print','excel','pdf']}) ;
        $timeout(function(){deliveryHistory.draw()},0) ;
        }
      }

    function
    doDeliveryLookup()
      {
//      console.log('lookup: ',$scope.deliveryLookup) ;

      DbFactory
        .post({topic:'delivery', action:'seq',
               params: {delivery: $scope.deliveryLookup}})
          .success(sequenceSuccess)
          .error(function(){console.log('delivery seq error');}) ;
      $scope.deliveryLookup = '' ;
      }

    function
    sequenceSuccess(data)
      {
//console.log(data[0].seq) ;
      if(data[0].seq)
        {
        $scope.deliverySeq = data[0].seq ;
        refresh() ;
//        console.log('found: ',$scope.deliverySeq) ;
        }
      else
        Global.showMessage("Delivery Not Found") ;
      }

    function
    allCartonSuccess(data)
      {
      var cols = [] ;
      var ref = "#allCarton" ;

      cols.push({title:'LPN',           data:'lpn',
                                        class:'dt-center'}) ;
      cols.push({title:'toteLPN',       data:'toteLPN',
                                        class:'dt-center'}) ;
      cols.push({title:'SubOrder',      data: 'subOrder',
                                        class:'dt-center'}) ;
      cols.push({title:'Pick?',        data:'isPick',
                                        class:'dt-center'}) ;
      cols.push({title:'P. to D.?',  data:'isPickToDock',
                                        class:'dt-center'}) ;
      cols.push({title:'Manual Reject', data:'manualRejectSet',
                                        render:smallRender,
                                        class:'dt-center'}) ;
      cols.push({title:'Created',       data:'created',
                                        class:'dt-center',
                                        render:smallRender}) ;
      cols.push({title:'Shipped',       data:'shipped',
                                        class:'dt-center',
                                        render:smallRender}) ;
      cols.push({title:'Stamp',         data:'stamp',
                                        class:'dt-center',
                                        render:smallRender}) ;
      cols.push({title:'Comment',       data:'comment',
                                        class:'dt-left'}) ;
      cols.push({title:'seq',           data:'seq',
                                        class:'dt-left',
                                        visible:false}) ;
      cols.push({title:'Ignore',    data:'seq',
                                        class:'dt-center'}) ;

      if(allCarton)
        {
        allCarton.clear() ;
        allCarton.rows.add(data) ;
        allCarton.draw() ;
        }
      else
        {
        allCarton = $(ref)
          .DataTable({data: data,
                      columns: cols,
                      order:[[5,'asc']],
                      rowCallback: ignoreCallback,
                      scrollX: "100%",
                      scrollY: "400px",
                      scrollCollapse: true,
                      paging: false,
                      dom: 'lftBipr',
                      buttons: ['copy','print','excel','pdf']}) ;
        $(ref+' tbody').on('click','button',ignoreClick) ;
        $(ref+' tbody').on('click','td',allCartonClick) ;
        $timeout(function(){allCarton.draw()},0) ;
        }
      }

    function
    ignoreCallback(row,data,index)
      {
      if(data.isPick=='true')
        {
        if(Global.permit('cartonEdit'))
          $("td:eq(9)",row).html("<button>Ignore</button>") ;
        else
          $("td:eq(9)",row).html("<button disabled>Ignore</button>") ;
        }
      else
        $("td:eq(9)",row).html("") ;
      }

    function
    ignoreClick()
      {
      var row = $(this).parents('tr') ;
      var data = allCarton.row(row).data() ;
      cartonIgnore(data.seq);
      }

    function
    allCartonClick() //new
      {
      var row = allCarton.row(this).data() ;
      var col = allCarton.cell(this).index().column ;

      if(col!=10)
        {
        if(row.isPick == 'true')
          {
          $scope.seq = row.seq ;
          $scope.selected = 4 ;
          $scope.$apply() ;
          }
        else
          {
          window.location = '#/ship?shipLPN='+row.lpn ;
          }
        }
      }

    function
    pickCartonSuccess(data)
      {
      var cols = [] ;
      var ref = "#pickCarton" ;

      cols.push({title:'Pick LPN', data:'pickLPN'}) ;
      cols.push({title:'Pick To Dock', data:'isPickToDock',
                                       class:'dt-center'}) ;
      cols.push({title:'Print-Apply', data:'isPrintApply',
                                      class:'dt-center'}) ;
      cols.push({title:'SubOrder', data: 'subOrder',
                                    class:'dt-center'})
      cols.push({title:'Length',data:'length',
                                class:'dt-right'}) ;
      cols.push({title:'Created', data:'created',
                                  render: smallRender,
                                  class:'dt-center'}) ;
      cols.push({title:'To Storage', data:'sentToStorage',
                                  render:smallRender,
                                     class:'dt-center'}) ;
      cols.push({title:'Over Bridge',data:'sentAcrossBridge',
                                    render:smallRender,
                                     class:'dt-center'}) ;
      cols.push({title:'Packed', data:'packed',
                                  render: smallRender,
                                  class:'dt-center'}) ;
      cols.push({title:'Changed', data:'stamp',
                                  render: smallRender,
                                  class:'dt-center'}) ;
      cols.push({title:'Comment', data:'comment',
                                  class:'dt-center'}) ;

      if(pickCarton)
        {
        pickCarton.clear() ;
        pickCarton.rows.add(data) ;
        pickCarton.draw() ;
        }
      else
        {
        pickCarton = $(ref)
          .DataTable({data: data,
                      columns: cols,
                      rowCallback: pickCartonCallback,
                      scrollX: "100%",
                      scrollY: "400px",
                      scrollCollapse: true,
                      paging: false,
                      dom: 'lftBipr',
                      buttons: ['copy','print','excel','pdf']}) ;
        $(ref+' tbody').on('click','td',pickCartonClick) ;
        $timeout(function(){pickCarton.draw()},0) ;
        }
      }

    function
    pickCartonCallback(row,data,index)
      {
      /*if(data.sentToStorage)
        checkLeft(row,5,data.sentToStorage) ;
      if(data.sentAcrossBridge)
        checkLeft(row,6,data.sentAcrossBridge) ;
      if(data.deleted)
        checkLeft(row,7,data.deleted) ;*/
      }

    function
    pickCartonClick()
      {
      var row = pickCarton.row(this).data() ;
      $scope.seq = row.seq ;
      $scope.selected = 4 ;
      $scope.$apply() ;
      }

    function
    shipCartonSuccess(data)
      {
      var cols = [] ;
      var ref = "#shipCarton" ;

      cols.push({title:'Ship LPN', data:'shipLPN'}) ;
      cols.push({title:'Tote LPN', data:'toteLPN'}) ;
      cols.push({title:'Pick To Dock', data:'isPickToDock',
                                       class:'dt-center'}) ;
      cols.push({title:'Manual Reject',data:'manualRejectSet',
                                      render: smallRender,
                                       class:'dt-center'}) ;
      cols.push({title:'Created', data:'created',
                                  render: smallRender,
                                  class:'dt-center'}) ;
      cols.push({title:'Shipped', data:'shipped',
                                  class:'dt-center',
                                  render: smallRender}) ;
      cols.push({title:'Changed', data:'stamp',
                                  render: smallRender,
                                  class:'dt-center'}) ;
      cols.push({title:'seq',     data:'seq', visible: false}) ;

      if(shipCarton)
        {
        shipCarton.clear() ;
        shipCarton.rows.add(data) ;
        shipCarton.draw() ;
        }
      else
        {
        shipCarton = $(ref)
          .DataTable({data: data,
                      columns: cols,
                      rowCallback: shipCartonCallback,
                      scrollX: "100%",
                      scrollY: "400px",
                      scrollCollapse: true,
                      paging: false,
                      dom: 'lftBipr',
                      buttons: ['copy','print','excel','pdf']}) ;
        $(ref+' tbody').on('click','td',shipCartonClick) ;
        $timeout(function(){shipCarton.draw()},0) ;
        }
      }

    function
    shipCartonCallback(row,data,index)
      {
      /*if(data.manualRejectSet)
        checkLeft(row,3,data.manualRejectSet) ;
      if(data.shipped)
        checkLeft(row,4,data.shipped) ;
      if(data.stamp)
        checkLeft(row,5,data.stamp) ;*/
      }

    function
    shipCartonClick() //new
      {
      var row = shipCarton.row(this).data() ;
      window.location = '#/ship?shipLPN='+row.shipLPN ;
      }

    function
    cartonDetailSuccess(data)
      {
      $scope.cartonDetail = data[0] ;

      if(!$scope.cartonDetail)
        Global.showMessage("Carton Not Found") ;

      $scope.seq = $scope.cartonDetail.seq ;

      DbFactory
        .post({topic: 'pickCarton', action: 'log',
               params: {id: $scope.seq}})
          .success(cartonHistorySuccess)
          .error(function(){console.log('carton history error');}) ;
      }

    function
    cartonHistorySuccess(data)
      {
      var cols = [] ;
      var ref = "#cartonHistory" ;

      cols.push({title:'Code',        data:'code'}) ;
      cols.push({title:'Description', data:'description'}) ;
      cols.push({title:'Stamp',       data:'stamp',
                                      render: stampRender}) ;

      if(cartonHistory)
        {
        cartonHistory.clear() ;
        cartonHistory.rows.add(data) ;
        cartonHistory.draw() ;
        }
      else
        {
        cartonHistory = $(ref)
          .DataTable({data: data,
                      columns: cols,
                      scrollX: "100%",
                      scrollY: "300px",
                      scrollCollapse: true,
                      paging: false,
                      dom: 'lftBipr',
                      buttons: ['copy','print','excel','pdf']}) ;
        $timeout(function(){cartonHistory.draw()},0) ;
        }
      }

    function
    doCartonLookup()
      {
      if($scope.cartonLookup)
        {
        DbFactory
          .post({topic: 'carton', action: 'byLPN',
                 params: {lpn: $scope.cartonLookup}})
            .success(cartonDetailSuccess)
            .error(function(){console.log('carton detail error')}) ;
        $scope.cartonLookup = '' ;
        }
      }

    function
    stageSuccess(data)
      {
      var cols = [] ;
      var ref = "#stage" ;

      for (var i=0 ; i < data.length ; i++) {
        data[i].delcomp = data[i].deliveryCompleteCount
              + " of "
              + data[i].deliveryCount ;
        if (data[i].waveShow==-1) {
          data[i].waveShow = "unassigned" ;
        }
      }

      cols.push({title:'Lane',         data:'lane'}) ;
      cols.push({title:'State',        data:'state'}) ;
      cols.push({title:'Wave',         data:'waveShow'}) ;
      cols.push({title:'Count',        data:'cartonCount',
                                       class:'dt-right'}) ;
      cols.push({title:'Span',         data:'cartonSpan',
                                       class:'dt-right'}) ;
      cols.push({title:'Full %',       data:'fill',
                                       class:'dt-right'}) ;
      cols.push({title: 'Order Comp.', data:'delcomp',
                                       class:'dt-right'}) ;
      cols.push({title: 'Last Sort', data:'lastSort',
                                       render:smallRender,
                                        class:'dt-center'}) ;
      cols.push({title:'Release',      data:null,defaultContent:"",
                                       class:'dt-center'});
      cols.push({title:'Disable',      data:null,defaultContent:"",
                                       class:'dt-center'});

      if(stage)
        {
        stage.clear() ;
        stage.rows.add(data) ;
        stage.draw() ;
        }
      else
        {
        stage = $(ref)
          .DataTable({data: data,
                      columns: cols,
                      rowCallback: stageCallback,
                      order: [[0,'asc']],
                      scrollX: "100%",
                      scrollY: "500px",
                      scrollCollapse: true,
                      paging: false,
                      dom: 'lftBipr',
                      buttons: ['copy','print','excel','pdf']}) ;
        $(ref+' tbody').on('click','button',stageClick) ;
        $timeout(function(){stage.draw()},0) ;
        }
      }

    function
    stageCallback(row,data,index)
      {
 //     if(data.state!='idle')
        {
        if(Global.permit('waveEdit'))
          $("td:eq(8)",row).html("<button>Release</button>") ;
        else
          $("td:eq(8)",row).html("<button disabled>Release</button>") ;
        }

        if(data.disabled==null)
          {
          $("td:eq(9)",row).css('background-color','#99ff99') ;
          if(Global.permit('stageLaneAble'))
            $("td:eq(9)",row).html("<button>Disable</button>") ;
          else
            $("td:eq(9)",row).html("<button disabled>Disable</button>") ;
          }
        else
          {
          $("td:eq(9)",row).css('background-color','#ff9999') ;
          if(Global.permit('stageLaneAble'))
            $("td:eq(9)",row).html("<button>Enable</button>") ;
          else
            $("td:eq(9)",row).html("<button disabled>Enable</button>") ;
          }
      }

    function
    stageClick()
      {
      var row = $(this).parents('tr') ;
      var cell = $(this).parents('td') ;
      var col = stage.cell(cell).index().column ;
      var data = stage.row(row).data() ;

      if(col==8)
        {
        var dialog = $mdDialog.confirm()
          .title('Release Stage Lane')
          .textContent('Release stage lane '+data.lane+'?')
          .ariaLabel('Release lane '+data.lane)
          .ok('Release')
          .cancel('Cancel')

        $mdDialog
          .show(dialog)
          .then(function()
            {
            DbFactory
              .post({topic:'stage', action:'release',
                     params: {lane: data.lane}})
              .success(refresh)
              .error(function(){console.log('stage release error');}) ;
            }) ;
        }
      else if(col==9)
        {
        if(data.disabled==null)
          {
          DbFactory
            .post({topic: 'stage', action:'disable',
                   params: {lane: data.lane}})
            .success(refresh)
            .error(function(){console.log('stage lane disable error');}) ;
          }
        else
          {
          DbFactory
            .post({topic: 'stage', action:'enable',
                   params: {lane: data.lane}})
            .success(refresh)
            .error(function(){console.log('stage lane enable error');}) ;
          }
        }
      }

    function
    packingCompleted(flag)
      {
      if(flag)
        {
        DbFactory
          .post({topic:'delivery', action:'packSet',
                 params: {seq: $scope.deliveryDetail.seq}})
            .success(refresh)
            .error(function(){console.log('pack set error');}) ;
        }
      else
        {
        DbFactory
          .post({topic:'delivery', action:'packClr',
                 params: {seq: $scope.deliveryDetail.seq}})
            .success(refresh)
            .error(function(){console.log('pack set error');}) ;
        }

      }

    function
    manualReject(flag)
      {
      if(flag)
        {
        DbFactory
          .post({topic:'carton', action:'rejectSet',
                 params: {seq: $scope.seq}})
            .success(refresh)
            .error(function(){console.log('reject set error');}) ;
        }
      else
        {
        DbFactory
          .post({topic:'carton', action:'rejectClr',
                 params: {seq: $scope.seq}})
            .success(refresh)
            .error(function(){console.log('reject set error');}) ;
        }
      }

    function
    cartonIgnore(sequence)
      {
      DbFactory
        .post({topic:'carton', action:'ignore',
               params: {seq: sequence}})
          .success(refresh)
          .error(function(){console.log('ignore error');}) ;
      }

    function
    refresh()
      {
      if($scope.selected==0)
        {
        DbFactory
          .post({topic: 'wave', action: 'all'})
            .success(waveSuccess)
            .error(function(){console.log('wave error')}) ;
        }
      if($scope.selected==1)
        {
        if($scope.wave && ($scope.wave != ''))
          {
          DbFactory
            .post({topic: 'wave', action: 'detail',
                   params: {wave: $scope.wave}})
              .success(waveDetailSuccess)
              .error(function(){console.log('wave detail error')}) ;
          }
        }
      if($scope.selected==2)
        {
        DbFactory
          .post({topic: 'delivery', action: 'all'})
          .success(deliveriesSuccess)
          .error(function(){console.log('deliveries error')}) ;
        }
      if($scope.selected==3)
        {
        if($scope.deliverySeq)
          {
          DbFactory
            .post({topic: 'delivery', action: 'detail',
                   params: {seq: $scope.deliverySeq}})
              .success(deliveryDetailSuccess)
              .error(function(){console.log('delivery detail error')}) ;
          }
        }
      if($scope.selected==4)
        {
        if($scope.seq)
          {
          DbFactory
            .post({topic: 'carton', action: 'detail',
                   params: {seq: $scope.seq}})
              .success(cartonDetailSuccess)
              .error(function(){console.log('carton detail error')}) ;
          }
        }
      if($scope.selected==5)
        {
        DbFactory
          .post({topic: 'stage', action: 'all'})
            .success(stageSuccess)
            .error(function(){console.log('stage error');}) ;
        }
      }

    function
    init()
      {
      Global.setTitle('Waves') ;
      Global.recv('refresh',refresh,$scope) ;
      initSteps() ;
      initDelSteps() ;

      if($routeParams.wave)
        {
        $scope.wave = $routeParams.wave ;
        $scope.select = 1 ;
        }

      if($routeParams.pickLPN)
        {
        $scope.cartonLookup = $routeParams.pickLPN ;
        $scope.selected = 4 ;
        setTimeout(doCartonLookup,500) ;
        }

      }

    init() ;
    }

  function
  waveConfig($routeProvider)
    {
    $routeProvider
      .when('/wave',{controller: 'WaveController',
                     templateUrl: '/app/wave/wave.view.html'}) ;
    }

  }()
)
