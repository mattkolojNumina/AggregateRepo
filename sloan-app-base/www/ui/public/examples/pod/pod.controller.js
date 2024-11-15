(
function()
  {
  angular
    .module('ui')
      .controller('PodController',podController) ;

  angular
    .module('ui')
      .config(podConfig) ;

  podController.$inject = ['$scope','$mdDialog',
                            'Global','DbFactory'] ;
  
  function
  podController($scope,$mdDialog,Global,DbFactory)
    {
    var periodic ;
    var lastBin = '' ;
    var lastVas = '' ;

    $scope.refresh = refresh ;
    $scope.permit = Global.permit ;
    $scope.pod = '02P' ; 
    $scope.podName = 'Pod 02' ;
    $scope.podChange = podChange ;
    $scope.podRestart = podRestart ;
    $scope.podClear = podClear ;
    $scope.binClick = binClick ;
    $scope.bins = {} ;
    $scope.bin = {} ;
    $scope.showBin = false ;
    $scope.vasClick = vasClick ;
    $scope.vases = {} ;
    $scope.vas = {} ;
    $scope.showVas = false ;
    $scope.enableBin = enableBin ;
    $scope.enableVas = enableVas ;

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
            break ;
          case 97:
            style.background = '#ff9933' ;
            break ;
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
        $scope.showBin = true ;
        }
      else
        $scope.showBin = false ;
      updateBins() ;
      }

    function
    binClick(name)
      {
      lastVas = ''  ;
      highlightVas() ;
      lastBin = name ;
      highlightBin(name) ;

      $scope.bin.cubby = $scope.pod + name ;
      $scope.bin.carton = $scope.bins[name].carton ;
      $scope.bin.packWave = $scope.bins[name].packWave ;
      $scope.bin.idle     = $scope.bins[name].idle ;
      $scope.bin.cubbies = $scope.bins[name].cubbies ;
      $scope.bin.cubbySeq = $scope.bins[name].cubbySeq ;
      $scope.bin.cartonSize = $scope.bins[name].cartonSize ;
      $scope.bin.enabled = $scope.bins[name].enabled ;
      $scope.bin.putWall = $scope.bins[name].putWall ;
      $scope.bin.putDevice = $scope.bins[name].putDevice ; 
      $scope.bin.putStatus = $scope.bins[name].putStatus ;
      $scope.bin.putService = $scope.bins[name].putService
      $scope.bin.packDevice = $scope.bins[name].packDevice ;
      $scope.bin.packStatus = $scope.bins[name].packStatus ;
      $scope.bin.packService = $scope.bins[name].packService ;
      } 

    function
    initVases()
      {
      $scope.vases = {} ;
      for(var n=1 ; n <= 5 ; n++)
        {
        var vas = 'v'+n ;
        $scope.vases[vas]={} ;
        $scope.vases[vas].bin = n ;
        $scope.vases[vas].name ='V'+n ;
        $scope.vases[vas].state = 0 ;
        $scope.vases[vas].border = 0 ;
        $scope.vases[vas].style={} ;
        }
      updateVases() ;
      }

    function
    updateVases()
      {
      for(var vas in $scope.vases)
        {
        var style = {} ;
        if($scope.vases[vas].border==1)
          style.border = 'solid 4px yellow' ;
        switch($scope.vases[vas].state)
          {
          case 1:
            style.background = '#a0a0ff' ;
            break ;
          case 98:
            style.color      = '#ffffff' ;
            style.background = '#ff0000' ;
            break ;
          case 99:
            style.color      = '#ffffff' ;
            style.background = '#202020' ;
            break ;
          }

        $scope.vases[vas].style = style ;
        }
      }

    function
    highlightVas(name)
      {
      for(var vas in $scope.vases)
        $scope.vases[vas].border=0 ;

      if(name)
        {
        $scope.vases[name].border=1 ;
        $scope.showVas = true ;
        }
      else
        $scope.showVas = false ;
      updateVases() ;
      }

    function
    vasClick(name)
      {
      lastBin ='' ;
      highlightBin() ;
      lastVas = name ;
      highlightVas(name) ;

      $scope.vas.name = $scope.vases[name].name ;
      $scope.vas.bin        = $scope.vases[name].bin ;
      $scope.vas.vasCode    = $scope.vases[name].vasCode ;
      $scope.vas.vasDesc    = $scope.vases[name].vasDesc ;
      $scope.vas.enabled    = $scope.vases[name].enabled ;
      $scope.vas.vasDevice = $scope.vases[name].vasDevice ;
      $scope.vas.vasStatus = $scope.vases[name].vasStatus ;
      $scope.vas.vasService = $scope.vases[name].vasService ;
      } 

    function
    vasSuccess(data)
      {
      for(var i=0  ; i<data.length ; i++)
        {
        var vas = 'v'+data[i].vasBin ;
        $scope.vases[vas].state = 0 ;
        $scope.vases[vas].vasCode = data[i].vasCode ;
        $scope.vases[vas].vasDesc = data[i].vasDesc ;
        if($scope.vases[vas].vasCode)
          $scope.vases[vas].state = 1 ;
        if(!((data[i].vasStatus=='ok')&&(data[i].vasService=='in')))
          $scope.vases[vas].state = 98 ;
        if(data[i].enabled=='no')
          $scope.vases[vas].state = 99 ;
        $scope.vases[vas].enabled   = data[i].enabled ;
        $scope.vases[vas].vasDevice = data[i].vasDevice ;
        $scope.vases[vas].vasStatus = data[i].vasStatus ;
        $scope.vases[vas].vasService = data[i].vasService ;
        }
      updateVases() ;
      if(lastVas!='') vasClick(lastVas) ;
      }

    function
    enableBin(ev)
      {
      var dialog = $mdDialog.confirm()
        .title('Enable/Disable Cubby '+$scope.bin.cubby)
        .textContent('Enable or Disable the cubby from being assigned')
        .ariaLabel('Enable/Disable '+$scope.bin.cubby)
        .targetEvent(ev)
        .ok('Enable')
        .cancel('Disable')

      $mdDialog
        .show(dialog)
        .then(function()
                {
                console.log('enable '+$scope.bin.cubby);
                DbFactory
                  .post({topic: 'bins', action:'updateEnabled',
                         params: {cubby: $scope.bin.cubby,
                                  enabled: 'yes'}}) 
                  .success(refresh) ;
                },
              function()
                {
                console.log('disable '+$scope.bin.cubby);
                DbFactory
                  .post({topic: 'bins', action:'updateEnabled',
                         params: {cubby: $scope.bin.cubby,
                                  enabled: 'no'}}) 
                  .success(refresh) ;
                }) ;
      }

    function
    enableVas(ev)
      {
      var dialog = $mdDialog.confirm()
        .title('Enable/Disable VAS bin '+$scope.vas.name)
        .textContent('Enable or Disable the VAS bin')
        .ariaLabel('Enable/Disable '+$scope.vas.name)
        .targetEvent(ev)
        .ok('Enable')
        .cancel('Disable')

      $mdDialog
        .show(dialog)
        .then(function()
                {
                console.log('enable '+$scope.vas.bin);
                DbFactory
                  .post({topic: 'vasBin', action:'updateEnabled',
                         params: {pod: $scope.pod,
                                  vasBin: $scope.vas.bin,
                                  enabled: 'yes'}}) 
                  .success(refresh) ;
                },
              function()
                {
                console.log('disable '+$scope.bin.cubby);
                DbFactory
                  .post({topic: 'vasBin', action:'updateEnabled',
                         params: {pod: $scope.pod,
                                  vasBin: $scope.vas.bin,
                                  enabled: 'no'}}) 
                  .success(refresh) ;
                }) ;
      }

    function
    podChange()
      {
      $scope.podName = $scope.pod ;
      $scope.bin = {} ;
      initBins() ;
      initVases() ;
      refresh() ;
      }

    function
    podRestart(ev)
      {
      var user = Global.getUser().name ;
      var dialog = $mdDialog.confirm()
        .title('Restart Pod '+$scope.podName)
        .textContent('Do you really want to restart Pod '+$scope.podName+'?')
        .ariaLabel('Restart Pod '+$scope.podName)
        .targetEvent(ev)
        .ok('Yes, restart')
        .cancel('No, do not restart')

      $mdDialog
        .show(dialog)
        .then(function()
                {
                DbFactory
                 .post({topic:'pod', action:'restart',
                        params:{pod: $scope.pod, 
                                user: user}}) ;
                }) ;
      }

    function
    podClear(ev)
      {
      var user = Global.getUser().name ;
      var dialog = $mdDialog.confirm()
        .title('Clear Pod '+$scope.podName)
        .textContent('Do you really want to clear Pod '+$scope.podName+'?')
        .ariaLabel('Clear Pod '+$scope.podName)
        .targetEvent(ev)
        .ok('Yes, clear')
        .cancel('No, do not clear')

      $mdDialog
        .show(dialog)
        .then(function()
                {
                DbFactory
                 .post({topic:'pod', action:'clear',
                        params:{pod: $scope.pod, 
                                user: user}}) ;
                }) ;
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
    binSuccess(data)
      {
      for(var i=0  ; i<data.length ; i++)
        {
        var bin = data[i].cubby.substr(3) ;
        $scope.bins[bin].state = 0 ;
        $scope.bins[bin].carton = data[i].carton ;
        $scope.bins[bin].packWave = data[i].packWave ;
        $scope.bins[bin].cubbies = data[i].cubbies ;
        $scope.bins[bin].cubbySeq = data[i].cubbySeq ;
        $scope.bins[bin].cartonSize = data[i].cartonSize ;
 
        if($scope.bins[bin].carton)
          $scope.bins[bin].state = 1 ;
        if(data[i].putCompleteStamp)
          $scope.bins[bin].state = 2 ;
        if(data[i].packStartedStamp) 
          $scope.bins[bin].state = 3 ;
        if(data[i].packCompleteStamp)
          $scope.bins[bin].state = 4 ;

        var now = new Date() ;
        var last = new Date(data[i].lastStamp) ;
        var idle = (now - last) ;
        idle = idle /(1000*60)

        if($scope.bins[bin].state != 0)
          idle = 0 ;
        $scope.bins[bin].idle = idle ;
        var maxIdle = 60 ;
        if((!$scope.bins[bin].carton) && (idle > maxIdle))
          $scope.bins[bin].state = 97 ; 
        if(!((data[i].putStatus=='ok')&&(data[i].putService=='in')))
          $scope.bins[bin].state = 98 ;
        if(!((data[i].packStatus=='ok')&&(data[i].packService=='in')))
          $scope.bins[bin].state = 98 ;
        if(data[i].enabled=='no')
          $scope.bins[bin].state = 99 ;
        $scope.bins[bin].enabled   = data[i].enabled ;
        $scope.bins[bin].putWall   = data[i].putWall ;
        $scope.bins[bin].putDevice = data[i].putDevice ;
        $scope.bins[bin].putStatus = data[i].putStatus ;
        $scope.bins[bin].putService = data[i].putService ;
        $scope.bins[bin].packDevice = data[i].packDevice ;
        $scope.bins[bin].packStatus = data[i].packStatus ;
        $scope.bins[bin].packService = data[i].packService ;
        }
      updateBins() ;
      if(lastBin!='') binClick(lastBin) ;
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
        .post({topic:'vases', action:'byPod',
               params: {pod: $scope.pod}})
          .success(vasSuccess)
          .error(function(){console.log('vas error')}) ;
      }

    function
    init()
      {
      Global.setTitle('Pods') ; 
      Global.recv('refresh',refresh,$scope) ;
      initBins() ;
      initVases() ;
      refresh() ;
      }

    init() ;
    }

  function
  podConfig($routeProvider)
    {
    $routeProvider
      .when('/pod',{controller: 'PodController',
                     templateUrl: '/app/pod/pod.view.html'}) ;
    }

  }() 
) 
