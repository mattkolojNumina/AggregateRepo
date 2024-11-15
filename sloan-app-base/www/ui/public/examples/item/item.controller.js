(
function()
  {
  angular
    .module('ui')
      .controller('ItemController',itemController) ;

  angular
    .module('ui')
      .config(itemConfig) ;

  itemController.$inject = ['$scope','$timeout','_',
                            'Global','DbFactory'] ;
  
  function
  itemController($scope,$timeout,_,Global,DbFactory)
    {
    var periodic ;

    $scope.refresh = refresh ;
    $scope.permit = Global.permit ;
    $scope.pod = '02P' ; 
    $scope.podName = 'Pod 02' ;
    $scope.podChange = podChange ;
    $scope.binClick = binClick ;
    $scope.bins = {} ;
    $scope.bin = {} ;
    $scope.items = [] ;
    $scope.itemClick = itemClick ;
    $scope.cartons = [] ;
    $scope.cartonHide = true ;
    $scope.item ;

    var itemList ;
    var itemTable ;
    var cartonTable ;

    function
    initBins()
      {
      $scope.bins = {} ;
      for(var n=1 ; n <= 48 ; n++)
        {
        var bin = n + '' ;
        if(bin.length<2) bin = '0' + bin ;
        $scope.bins[bin]={} ;
        $scope.bins[bin].name = $scope.pod+bin ;
        $scope.bins[bin].show = '' ;
        $scope.bins[bin].state = 0 ;
        $scope.bins[bin].border = 0 ;
        $scope.bins[bin].style={} ;
        }
      updateBins() ;
      }

    function
    updateBins()
      {
      for(var bin in $scope.bins)
        {
        var style = {} ;
        if($scope.bins[bin].border==1)
          style.border = 'dotted 4px yellow' ;
        if($scope.bins[bin].border==2)
          style.border = 'solid 4px yellow' ;
        switch($scope.bins[bin].state)
          {
          case 1:
            style.background = '#ccff66' ;
            break ;
          case 2:
            style.background = '#00ff00' ;
            break ;
          case 3:
            style.background = '#0099ff' ;
            break ;
          case 4:
            style.background = '#0000ff' ;
          case 98:
            style.color      = '#ffffff' ;
            style.background = '#ff0000' ;
            break ;
          case 99:
            style.color      = '#ffffff' ;
            style.background = '#202020' ;
            break ;
          }

        $scope.bins[bin].style = style ;
        }
      }

    function
    highlightBin(name)
      {
      for(var bin in $scope.bins)
        $scope.bins[bin].border=0 ;

      if(name)
        {
        if($scope.bins[name].carton)
          for(var bin in $scope.bins)
            if($scope.bins[name].carton==$scope.bins[bin].carton)
              $scope.bins[bin].border=1 ;

        $scope.bins[name].border=2 ;
        }
      updateBins() ;
      }

    function
    binClick(name)
      {
      highlightBin(name) ;
      } 

    function
    podChange()
      {
      $scope.podName = 'Pod '+$scope.pod.substring(0,2) ;
      $scope.bin = {} ;
      initBins() ;
      $scope.item = '' ;
      updateCartons() ;
      refresh() ;
      }

    function
    binSuccess(data)
      {
      for(var i=0  ; i<data.length ; i++)
        {
        var bin = data[i].cubby.substr(3) ;
        $scope.bins[bin].state = 0 ;
        $scope.bins[bin].carton = data[i].carton ;
        if($scope.bins[bin].carton)
          $scope.bins[bin].state = 1 ;
        if(data[i].putCompleteStamp)
          $scope.bins[bin].state = 2 ;
        if(!((data[i].putStatus=='ok')&&(data[i].putService=='in')))
          $scope.bins[bin].state = 98 ;
        if(!((data[i].packStatus=='ok')&&(data[i].packService=='in')))
          $scope.bins[bin].state = 98 ;
        if(data[i].enabled=='no')
          $scope.bins[bin].state = 99 ;
        }
      updateBins() ;
      }

    function
    itemSuccess(data)
      {
      var uniqSeqs  = _.uniqBy(data,function(a){return a.seq}) ;
      var uniqItems = _.uniqBy(data,function(a){return a.item}) ;

      itemList =  {} ;
      for(var i=0 ; i<uniqItems.length ; i++)
        {
        var item = {} ;
        item.item = uniqItems[i].item ;
        item.desc = uniqItems[i].itemDesc ;
        item.sku  = uniqItems[i].itemSku ;
        item.total = 0 ;
        item.put = 0 ;

        var thisItem = _.filter(uniqSeqs,
                                function(a){return a.item==item.item});
        var uniqCartons = _.uniqBy(thisItem,
                                   function(a){return a.carton;});

        var cartonList = {} ; 
        for(var c=0 ; c<uniqCartons.length ; c++)
          {
          var carton = {} ;
          carton.carton = uniqCartons[c].carton ;
          carton.cubby  = uniqCartons[c].cubby ;
          carton.cubbies = '' ;
          carton.total  = 0 ;
          carton.put    = 0 ; 
          cartonList[carton.carton] = carton ;
          }

        item.cartonList = cartonList ; 
        itemList[item.item] = item ;
        }

      for(var s=0 ; s<uniqSeqs.length ; s++)
        {
        var item   = uniqSeqs[s].item ;
        var carton = uniqSeqs[s].carton ;
        itemList[item].total++ ;
        itemList[item].cartonList[carton].total++ ; 
        if(uniqSeqs[s].putStamp)
          {
          itemList[item].put++ ;
          itemList[item].cartonList[carton].put++ ;
          }
        }

      for(var d=0 ; d<data.length ; d++)
        {
        var item   = data[d].item ;
        var carton = data[d].carton ;
        var cubby  = data[d].cubby ;
        var index = itemList[item].cartonList[carton].cubbies.indexOf(cubby) ; 
        if(index<0)
          {
          if(itemList[item].cartonList[carton].cubbies.length>0)
            itemList[item].cartonList[carton].cubbies += ' ' ;
          itemList[item].cartonList[carton].cubbies += cubby ;       
          }
        }

      $scope.items = [] ;
      for(var item in itemList)
        {
        itemList[item].cartons = [] ;
        for(var carton in itemList[item].cartonList)
          itemList[item].cartons.push(itemList[item].cartonList[carton]) ;
        $scope.items.push(itemList[item]) ;
        }

      var cols = [] ;
      var ref = "#item" ;

      cols.push({title:'Item', data:'item'}) ;
      cols.push({title:'Desc',  data:'desc'}) ;
      cols.push({title:'Sku',     data:'sku'}) ;
      cols.push({title:'Put',     data:'put',
                                  class:'dt-right'}) ;
      cols.push({title:'Total',   data:'total',
                                  class:'dt-right'}) ;
      
      if(itemTable)
        {
        itemTable.clear() ;
        itemTable.rows.add($scope.items) 
        itemTable.draw() ;
        }
      else
        {
        itemTable = $(ref)
          .DataTable({data: $scope.items,
                      columns: cols,
                      order: [[0,'asc']],
                      scrollCollapse: true,
                      paging: false, 
                      dom: 'lt',
                      buttons: ['copy','print','excel','pdf']}) ;
        $(ref+' tbody').on('click','td',itemClick) ;
        } 

      $scope.item = '' ;
      updateCartons() ;
      }

    function
    itemClick()
      {
      var row = itemTable.row(this).data() ;
      $scope.item = row.item ;
      updateCartons() ;
      $scope.$apply() ;
      }

    function
    updateCartons()
      {
      for(var bin in $scope.bins)
        $scope.bins[bin].show='' ;
      if($scope.item)
        {
        $scope.cartonHide=false ;
        $scope.cartons = itemList[$scope.item].cartons ;
        for(var i=0 ; i<$scope.cartons.length ; i++)
          {
          var bin = $scope.cartons[i].cubby.substr(3) ;
          $scope.bins[bin].show=$scope.bins[bin].name+' '
                               + $scope.cartons[i].put+'/'
                               +$scope.cartons[i].total ;
          }

        var cols = [] ;
        var ref = "#carton" ;

        cols.push({title:'Carton', data:'carton'}) ;
        cols.push({title:'Cubbies',  data:'cubbies'}) ;
        cols.push({title:'Put',     data:'put',
                                    class:'dt-right'}) ;
        cols.push({title:'Total',   data:'total',
                                    class:'dt-right'}) ;
      
        if(cartonTable)
          {
          cartonTable.clear() ;
          cartonTable.rows.add($scope.cartons) 
          cartonTable.draw() ;
          }
        else
          {
          cartonTable = $(ref)
            .DataTable({data: $scope.cartons,
                        columns: cols,
                        order: [[0,'asc']],
                        scrollCollapse: true,
                        paging: false, 
                        dom: 'lt',
                        buttons: ['copy','print','excel','pdf']}) ;
          } 
        $(ref+' tbody').on('click','td',cartonClick) ;
        }
      else
        $scope.cartonHide = true ; 
      }

    function
    cartonClick()
      {
      var row = cartonTable.row(this).data() ;
      if(row.cubbies.length>=5)
        highlightBin(row.cubbies.substr(3,2)) ;
      $scope.$apply() ;
      }

    function
    refresh()
      {
      DbFactory
        .post({topic:'bins', action:'byPod',
               params: {pod: $scope.pod}}) 
          .success(binSuccess)
          .error(function(){console.log('bin error')}) ;
      DbFactory
        .post({topic:'item', action:'byPod',
               params: {pod: $scope.pod}}) 
          .success(itemSuccess)
          .error(function(){console.log('item error')}) ;
      }

    function
    init()
      {
      Global.setTitle('Items') ; 
      Global.recv('refresh',refresh,$scope) ;
      initBins() ;
      refresh() ;
      }

    init() ;
    }

  function
  itemConfig($routeProvider)
    {
    $routeProvider
      .when('/item',{controller: 'ItemController',
                     templateUrl: '/app/item/item.view.html'}) ;
    }

  }() 
) 


