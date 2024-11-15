(
function()
  {
  angular
    .module('ui')
      .controller('WaveController',waveController) ;

  angular
    .module('ui')
      .config(waveConfig) ;

  waveController.$inject = ['$scope','$routeParams',
                            'Global','DbFactory'] ;
  
  function
  waveController($scope,$routeParams,Global,DbFactory)
    {
    var periodic ;
    var wave ;
    var wavedata ;
    var ewave ;
    var carton ;
    var item ;
    var lineitem ;
    var vas ;

    $scope.selected ;
    $scope.refresh = refresh ;
    $scope.wave = null ;
    $scope.waveDetail ;
    $scope.waveLookup ;
    $scope.doWaveLookup = doWaveLookup ;
    $scope.stepSelect = 0 ;
    $scope.steps = [] ;
    $scope.maxMinutes = 60 ;
    $scope.display = display ;
    $scope.carton = null ;
    $scope.cartonDetail ;
    $scope.cartonLookup ;
    $scope.doCartonLookup = doCartonLookup ;

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
    waveCallback(row,data,index)
      {
      if(data.downloadStamp)
        $('td:eq(3)',row).html('<div class="tooltip">'
                              +'&check;'
                              +'<span class="tooltiptext">'      
                              +simpleDate(data.downloadStamp)
                              +'</span>'
                              +'</div>') ;

      if(data.passConsistencyCheck==1)
        $('td:eq(4)',row).html('&check;') ;
      else
        $('td:eq(4)',row).html('') ;
      
      if(data.assignedStamp)
        $('td:eq(5)',row).html('<div class="tooltip">'
                              +'&check;'
                              +'<span class="tooltiptext">'      
                              +simpleDate(data.assignedStamp)
                              +'</span>'
                              +'</div>') ;

      if(data.putCompleteStamp)
        $('td:eq(6)',row).html('<div class="tooltip">'
                              +'&check;'
                              +'<span class="tooltiptext">'      
                              +simpleDate(data.putCompleteStamp)
                              +'</span>'
                              +'</div>') ;

      if(data.packCompleteStamp)
        $('td:eq(7)',row).html('<div class="tooltip">'
                              +'&check;'
                              +'<span class="tooltiptext">'      
                              +simpleDate(data.packCompleteStamp)
                              +'</span>'
                              +'</div>') ;

      if(data.forcedclosedStamp)
        $('td:eq(8)',row).html('<div class="tooltip">'
                              +'&check;'
                              +'<span class="tooltiptext">'      
                              +simpleDate(data.forcedclosedStamp)
                              +'</span>'
                              +'</div>') ;

      if(data.clearedStamp)
        $('td:eq(9)',row).html('<div class="tooltip">'
                              +'&check;'
                              +'<span class="tooltiptext">'      
                              +simpleDate(data.clearedStamp)
                              +'</span>'
                              +'</div>') ;
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
      $scope.steps.push({id:1, name:'Failed consistency checks'}) ;
      $scope.steps.push({id:2, name:'Not yet assigned to a pod'}) ;
      $scope.steps.push({id:3, name:'Currently assigned to a pod'}) ;
      $scope.steps.push({id:4, name:'Put complete'}) ;
      $scope.steps.push({id:5, name:'Pack complete'}) ;
      $scope.steps.push({id:6, name:'Late completing the Put process'}) ;
      $scope.steps.push({id:7, name:'Late completing the Pack process'}) ;
      }
    function
    waveFilter(input)
      {
      var output = [] ;
      for(var i=0 ; i<input.length ; i++)
        {
        var keep = true ;

        if($scope.stepSelect==1)
          if(input[i].passConsistencyCheck==1)
            keep = false ;

        if($scope.stepSelect==2)
          if(input[i].assignedStamp)
            keep = false ;

        if($scope.stepSelect==3)
          if(!(input[i].assignedStamp && !(input[i].putCompleteStamp)))
            keep = false ;

        if($scope.stepSelect==4)
          if(!input[i].putCompleteStamp)
            keep = false ;

        if($scope.stepSelect==5)
          if(!input[i].packCompleteStamp)
            keep = false ;

        if($scope.stepSelect==6)
          {
          keep = false ;
          if(input[i].assignedStamp)
            if(!input[i].putCompleteStamp)
              {
              var now = new Date() ;
              var start = new Date(input[i].assignedStamp) ;
              var minutes = (now-start)/1000/60 ;
              if(minutes > $scope.maxMinutes)
                keep = true ;
              }
          }

        if($scope.stepSelect==7)
          {
          keep = false ;
          if(input[i].putCompleteStamp)
            if(!input[i].packCompleteStamp)
              {
              var now = new Date() ;
              var start = new Date(input[i].putCompleteStamp) ;
              var minutes = (now-start)/1000/60 ;
              if(minutes > $scope.maxMinutes)
                keep = true ;
              }
          }

        if(keep)
          output.push(input[i]) ;
        }
      return output ;
      }

    function
    display()
      {
      var data = waveFilter(waveData) ;

      var cols = [] ;
      var ref = "#wave" ;

      cols.push({title:'Pack Wave', data:'packWave'}) ;
      cols.push({title:'Pick Wave', data:'pickWave'}) ;
      cols.push({title:'Pod',       data:'pod'}) ;
      cols.push({title:'Download',  data:'downloadStamp',
                                    class: 'dt-center'}) ;
      cols.push({title:'Consistent',data:'passConsistencyCheck',
                                    class: 'dt-center'}) ;
      cols.push({title:'Assign',    data:'assignedStamp',
                                    class: 'dt-center'}) ;
      cols.push({title:'Put',       data:'putCompleteStamp',
                                    class: 'dt-center'}) ;
      cols.push({title:'Pack',      data:'packCompleteStamp',
                                    class: 'dt-center'}) ;
      cols.push({title:'Forced Closed',     data:'forcedclosedStamp',
                                    class: 'dt-center'}) ;
      cols.push({title:'Cleared',   data:'clearedStamp',
                                    class: 'dt-center'}) ;
      
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
        } 
      }

    function
    waveClick()
      {
      var row = wave.row(this).data() ;
      $scope.wave = row.packWave ;
      $scope.selected = 2 ;
      $scope.$apply() ;
      }

    function
    ewaveSuccess(data)
      {
      var cols = [] ;
      var ref = "#ewave" ;

      cols.push({title:'Pick Wave', data:'pickWave'}) ;
      cols.push({title:'Filename',  data:'filename'}) ;
      cols.push({title:'Error',     data:'error'}) ;
      cols.push({title:'Processed',  data:'processedStamp',
                                    class: 'dt-center'}) ;
      
      if(ewave)
        {
        ewave.clear() ;
        ewave.rows.add(data) 
        ewave.draw() ;
        }
      else
        {
        ewave = $(ref)
          .DataTable({data: data,
                      columns: cols,
                      rowCallback: ewaveCallback,
                      order: [[0,'asc']],
                      scrollX: "100%",
                      scrollY: "500px",
                      scrollCollapse: true,
                      paging: false, 
                      dom: 'lftBipr',
                      buttons: ['copy','print','excel','pdf']}) ;
        $(ref+' tbody').on('click','td',waveClick) ;
        } 
      }

    function
    ewaveCallback(row,data,index)
      {
      if(data.processedStamp)
        $('td:eq(3)',row).html('<div class="tooltip">'
                              +'&check;'
                              +'<span class="tooltiptext">'      
                              +simpleDate(data.processedStamp)
                              +'</span>'
                              +'</div>') ;
     }

    function
    waveDetailSuccess(data)
      {
      $scope.waveDetail = data[0] ;

      if($scope.wave)
        {
        DbFactory
          .post({topic: 'carton', action: 'byPack',
                 params: {wave: $scope.wave}})
            .success(cartonSuccess)
            .error(function(){console.log('carton detail error')}) ;
        }
      } 

    function
    cartonDetailSuccess(data)
      {
      $scope.cartonDetail = data[0] ;
      if($scope.carton)
        {
        DbFactory
          .post({topic: 'item', action: 'byCarton',
                 params: {carton: $scope.carton}})
            .success(itemSuccess)
            .error(function(){console.log('item detail error')}) ;
        DbFactory
          .post({topic: 'lineitem', action: 'byCarton',
                 params: {carton: $scope.carton}})
            .success(lineitemSuccess)
            .error(function(){console.log('lineitem detail error')}) ;
        DbFactory
          .post({topic: 'vas', action: 'byCarton',
                 params: {carton: $scope.carton}})
            .success(vasSuccess)
            .error(function(){console.log('vas detail error')}) ;
        }
      } 

    function
    doWaveLookup()
      {
      if($scope.waveLookup)
        {
        $scope.wave = $scope.waveLookup ;
console.log($scope.wave) ;
        $scope.waveLookup = null ;
        $scope.refresh() ;
        }
      }

    function
    doCartonLookup()
      {
      if($scope.cartonLookup)
        {
        $scope.carton = $scope.cartonLookup ;
console.log($scope.carton) ;
        $scope.cartonLookup = null ;
        $scope.refresh() ;
        }
      }

    function
    refresh()
      {
      if($scope.selected==0)
        {
        DbFactory
          .post({topic: 'wave', action: 'all'})
            .success(waveSuccess)
            .error(function(err){console.log(err);}) ;
        } 
      if($scope.selected==1)
        {
        DbFactory
          .post({topic: 'wave', action: 'error'})
            .success(ewaveSuccess)
            .error(function(err){console.log(err);}) ;
        } 
      if($scope.selected==2)
        {
        if($scope.wave)
         {
          DbFactory
            .post({topic: 'wave', action: 'detail',
                   params: {wave: $scope.wave}})
              .success(waveDetailSuccess)
              .error(function(err){console.log(err);}) ;
          }
        }
      if($scope.selected==3)
        {
        if($scope.carton)
          {
          DbFactory
            .post({topic: 'carton', action: 'detail',
                   params: {carton: $scope.carton}})
              .success(cartonDetailSuccess)
              .error(function(err){console.log(err);}) ;
          }
        } 
      }

    function
    cartonSuccess(data)
      {
      var cols = [] ;
      var ref = "#carton" ;

      cols.push({title:'Carton',    data:'carton'}) ;
      cols.push({title:'Size',      data:'cartonSize'}) ;
      cols.push({title:'Cubbies',   data:'cubbies',
                                    class: 'dt-right'}) ;
      cols.push({title:'AutoBag',   data:'autobaggerEligible',
                                    class: 'dt-center'}) ;
      cols.push({title:'Packslip',  data:'packslipRequired',
                                    class: 'dt-center'}) ;
      cols.push({title:'Put %',     data:'putPercent',
                                    class: 'dt-right'}) ;
      cols.push({title:'Download',  data:'downloadStamp',
                                    class: 'dt-center'}) ;
      cols.push({title:'Assign',    data:'assignedStamp',
                                    class: 'dt-center'}) ;
      cols.push({title:'Put Done',  data:'putCompleteStamp',
                                    class: 'dt-center'}) ;
      cols.push({title:'Pack Start',data:'packStartStamp',
                                    class: 'dt-center'}) ;
      cols.push({title:'Pack Done', data:'packCompleteStamp',
                                    class: 'dt-center'}) ;

      if(carton)
        {
        carton.clear() ;
        carton.rows.add(data) 
        carton.draw() ;
        }
      else
        {
        carton = $(ref)
          .DataTable({data: data,
                      columns: cols,
                      rowCallback: cartonCallback,
                      order: [[0,'asc']],
                      scrollX: "100%",
                      scrollY: "500px",
                      scrollCollapse: true,
                      paging: false, 
                      dom: 'lftBipr',
                      buttons: ['copy','print','excel','pdf']}) ;
        $(ref+' tbody').on('click','td',cartonClick) ;
        } 
      }

    function
    cartonCallback(row,data,index)
      {
      if(data.autoBaggerEligible==1)
        $('td:eq(3)',row).html('&check;') ;
      else
        $('td:eq(3)',row).html('') ;

      if(data.packslipRequired==1)
        $('td:eq(4)',row).html('&check;') ;
      else
        $('td:eq(4)',row).html('') ;
      
      if(data.downloadStamp)
        $('td:eq(6)',row).html('<div class="tooltip">'
                              +'&check;'
                              +'<span class="tooltiptext">'      
                              +simpleDate(data.downloadStamp)
                              +'</span>'
                              +'</div>') ;

      if(data.assignedStamp)
        $('td:eq(7)',row).html('<div class="tooltip">'
                              +'&check;'
                              +'<span class="tooltiptext">'      
                              +simpleDate(data.assignedStamp)
                              +'</span>'
                              +'</div>') ;

      if(data.putCompleteStamp)
        $('td:eq(8)',row).html('<div class="tooltip">'
                              +'&check;'
                              +'<span class="tooltiptext">'      
                              +simpleDate(data.putCompleteStamp)
                              +'</span>'
                              +'</div>') ;

      if(data.packStartStamp)
        $('td:eq(9)',row).html('<div class="tooltip">'
                              +'&check;'
                              +'<span class="tooltiptext">'      
                              +simpleDate(data.packStartStamp)
                              +'</span>'
                              +'</div>') ;

      if(data.packCompleteStamp)
        $('td:eq(10)',row).html('<div class="tooltip">'
                              +'&check;'
                              +'<span class="tooltiptext">'      
                              +simpleDate(data.packCompleteStamp)
                              +'</span>'
                              +'</div>') ;

      }

    function
    cartonClick()
      {
      var row = carton.row(this).data() ;
      $scope.carton = row.carton ;
      $scope.selected = 3 ;
      $scope.$apply() ;
      }

    function
    itemSuccess(data)
      {
      var cols = [] ;
      var ref = "#item" ;

      cols.push({title:'Seq',       data:'seq',
                                    class:'dt-right'}) ;
      cols.push({title:'Item',      data:'item'}) ;
      cols.push({title:'Line Item', data:'lineItem'}) ;
      cols.push({title:'SKU',       data:'itemSku'}) ;
      cols.push({title:'Description', data:'itemDesc'}) ;
      cols.push({title:'Delicate',  data:'delicate',
                                    class:'dt-center'}) ;
      cols.push({title:'Canceled',  data:'canceled',
                                    class:'dt-center'}) ;
      cols.push({title:'Download',  data:'downloadStamp',
                                    class:'dt-center'}) ;
      cols.push({title:'Put',       data:'putStamp',
                                    class:'dt-center'}) ;

      if(item)
        {
        item.clear() ;
        item.rows.add(data) 
        item.draw() ;
        }
      else
        {
        item = $(ref)
          .DataTable({data: data,
                      columns: cols,
                      rowCallback: itemCallback,
                      order: [[0,'asc']],
                      scrollX: "100%",
                      scrollY: "500px",
                      scrollCollapse: true,
                      paging: false, 
                      dom: 'lftBipr',
                      buttons: ['copy','print','excel','pdf']}) ;
        $(ref+' tbody').on('click','td',itemClick) ;
        } 
      }

    function
    itemCallback(row,data,index)
      {
      if(data.delicate==1)
        $('td:eq(5)',row).html('&check;') ;
      else
        $('td:eq(5)',row).html('') ;
      
      if(data.canceled==1)
        $('td:eq(6)',row).html('&check;') ;
      else
        $('td:eq(6)',row).html('') ;
      
      if(data.downloadStamp)
        $('td:eq(7)',row).html('<div class="tooltip">'
                              +'&check;'
                              +'<span class="tooltiptext">'      
                              +simpleDate(data.downloadStamp)
                              +'</span>'
                              +'</div>') ;

      if(data.putStamp)
        $('td:eq(8)',row).html('<div class="tooltip">'
                              +'&check;'
                              +'<span class="tooltiptext">'      
                              +simpleDate(data.putStamp)
                              +'</span>'
                              +'</div>') ;

      }

    function
    itemClick()
      {
      var row = item.row(this).data() ;
      }

    function
    lineitemSuccess(data)
      {
      var cols = [] ;
      var ref = "#lineitem" ;

      cols.push({title:'Seq',       data:'seq',
                                    class:'dt-right'}) ;
      cols.push({title:'Item',      data:'item'}) ;
      cols.push({title:'Line Item', data:'lineItem'}) ;
      cols.push({title:'SKU',       data:'itemSku'}) ;
      cols.push({title:'Description', data:'itemDesc'}) ;
      cols.push({title:'Qty',       data:'qty',
                                    class:'dt-center'}) ;
      cols.push({title:'Put Qty',   data:'putQty',
                                    class:'dt-center'}) ;
      cols.push({title:'Delicate',  data:'delicate',
                                    class:'dt-center'}) ;
      cols.push({title:'VAS',       data:'vas',
                                    class:'dt-center'}) ;
      cols.push({title:'Download',  data:'downloadStamp',
                                    class:'dt-center'}) ;
      cols.push({title:'Put Complete', data:'putCompleteStamp',
                                    class:'dt-center'}) ;

      if(lineitem)
        {
        lineitem.clear() ;
        lineitem.rows.add(data) 
        lineitem.draw() ;
        }
      else
        {
        lineitem = $(ref)
          .DataTable({data: data,
                      columns: cols,
                      rowCallback: lineitemCallback,
                      order: [[0,'asc']],
                      scrollX: "100%",
                      scrollY: "500px",
                      scrollCollapse: true,
                      paging: false, 
                      dom: 'lftBipr',
                      buttons: ['copy','print','excel','pdf']}) ;
        $(ref+' tbody').on('click','td',lineitemClick) ;
        } 
      }

    function
    lineitemCallback(row,data,index)
      {
      if(data.delicate==1)
        $('td:eq(7)',row).html('&check;') ;
      else
        $('td:eq(7)',row).html('') ;
      
      if(data.vas==1)
        $('td:eq(8)',row).html('&check;') ;
      else
        $('td:eq(8)',row).html('') ;
      
      if(data.downloadStamp)
        $('td:eq(9)',row).html('<div class="tooltip">'
                              +'&check;'
                              +'<span class="tooltiptext">'      
                              +simpleDate(data.downloadStamp)
                              +'</span>'
                              +'</div>') ;

      if(data.putCompleteStamp)
        $('td:eq(10)',row).html('<div class="tooltip">'
                              +'&check;'
                              +'<span class="tooltiptext">'      
                              +simpleDate(data.putCompleteStamp)
                              +'</span>'
                              +'</div>') ;

      }

    function
    lineitemClick()
      {
      var row = lineitem.row(this).data() ;
      }

    function
    vasSuccess(data)
      {
      var cols = [] ;
      var ref = "#vas" ;

      cols.push({title:'Item',      data:'item'}) ;
      cols.push({title:'Code',      data:'vasCode'}) ;
      cols.push({title:'Description',data:'vasDesc'}) ;
      cols.push({title:'Level',     data:'cartonLevelVas',
                                    class:'dt-center'}) ;
      cols.push({title:'Side',      data:'putSide',
                                    class:'dt-center'}) ; 
      cols.push({title:'Download',  data:'downloadStamp',
                                    class:'dt-center'}) ;

      if(vas)
        {
        vas.clear() ;
        vas.rows.add(data) 
        vas.draw() ;
        }
      else
        {
        vas = $(ref)
          .DataTable({data: data,
                      columns: cols,
                      rowCallback: vasCallback,
                      order: [[0,'asc']],
                      scrollX: "100%",
                      scrollY: "500px",
                      scrollCollapse: true,
                      paging: false, 
                      dom: 'lftBipr',
                      buttons: ['copy','print','excel','pdf']}) ;
        $(ref+' tbody').on('click','td',vasClick) ;
        } 
      }

    function
    vasCallback(row,data,index)
      {
      if((data.cartonLevelVas==0)&&(data.itemLevelVas==0))
        $('td:eq(3)',row).html('') ;
      if((data.cartonLevelVas==1)&&(data.itemLevelVas==0))
        $('td:eq(3)',row).html('Carton') ;
      if((data.cartonLevelVas==0)&&(data.itemLevelVas==1))
        $('td:eq(3)',row).html('Item') ;
      if((data.cartonLevelVas==1)&&(data.itemLevelVas==1))
        $('td:eq(3)',row).html('Both') ;
      
      if((data.putSide==0)&&(data.packSide==0))
        $('td:eq(4)',row).html('') ;
      if((data.putSide==1)&&(data.packSide==0))
        $('td:eq(4)',row).html('Put') ;
      if((data.putSide==0)&&(data.packSide==1))
        $('td:eq(4)',row).html('Pack') ;
      if((data.putSide==1)&&(data.packSide==1))
        $('td:eq(4)',row).html('Both') ;

      if(data.downloadStamp)
        $('td:eq(5)',row).html('<div class="tooltip">'
                              +'&check;'
                              +'<span class="tooltiptext">'      
                              +simpleDate(data.downloadStamp)
                              +'</span>'
                              +'</div>') ;

      if(data.pickStamp)
        $('td:eq(6)',row).html('<div class="tooltip">'
                              +'&check;'
                              +'<span class="tooltiptext">'      
                              +simpleDate(data.picktamp)
                              +'</span>'
                              +'</div>') ;

      }

    function
    vasClick()
      {
      var row = lineitem.row(this).data() ;
      }


    function
    init()
      {
      Global.setTitle('Waves') ; 
      Global.recv('refresh',refresh,$scope) ;
      initSteps() ;

      if($routeParams.packWave)
        {
        $scope.wave = $routeParams.packWave ;
        $scope.selected = 2 ;
        }
      if($routeParams.carton)
        {
        $scope.carton = $routeParams.carton ;
        $scope.selected = 3 ;
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
