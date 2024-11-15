(
function()
  {
  angular
    .module('ui')
      .controller('VasController',vasController) ;

  angular
    .module('ui')
      .config(vasConfig) ;

  vasController.$inject = ['$scope','$routeParams','$interval','$timeout',
                           'Global','DbFactory'] ;
  
  function
  vasController($scope,$routeParams,$interval,$timeout,
                Global,DbFactory)
    {
    var periodic ;

    $scope.refresh = refresh ;
    $scope.selected = 0 ;
    $scope.permit = Global.permit ;
    $scope.codes = [] ;
    var descs = {} ;
    var thePod = '' ;
    var theVasBin = '' ;

    var bin = null ; 
    $scope.bin = {} ;
    $scope.binClear = binClear;
    $scope.binUpdate = binUpdate ;

    var vas = null ;
    $scope.vas = {} ;
    $scope.vasNew = vasNew ;
    $scope.vasUpdate = vasUpdate ;
    $scope.vasDelete = vasDelete ;

    function
    refresh()
      {
      if($scope.selected==0)
        {
        DbFactory
          .post({topic: 'vasBin', action: 'all'}) 
            .success(binSuccess)
            .error(function(){console.log('vas bin error')}) ; 
        DbFactory
          .post({topic: 'vas', action: 'all'}) 
            .success(choiceSuccess)
            .error(function(){console.log('vas error')}) ; 
        }
      if($scope.selected==1)
        {
        DbFactory
          .post({topic: 'vas', action: 'all'}) 
            .success(vasSuccess)
            .error(function(){console.log('vas error')}) ; 
        }
      }

    function
    choiceSuccess(data)
      {
      $scope.codes = data ;
      descs = {} ;
      for(var i=0 ; i<data.length ; i++)
        descs[data[i].vasCode]=data[i].vasDesc ;
      }

    function
    binNew()
      {
      $scope.bin.pod = '' ;
      $scope.bin.vasBin = '' ;
      $scope.bin.choice = '' ;
      }

    function
    binClear()
      {
      if($scope.bin.pod=='') return ;
      DbFactory
        .post({topic: 'vasBin', action: 'update',
               params: {pod:     $scope.bin.pod,
                        vasBin:  $scope.bin.vasBin,
                        vasCode: '',
                        vasDesc: '' }}) 
          .success(binUpdateSuccess)
          .error(function() {console.log('bin update error')}) ;
      binNew() ;
      $timeout(refresh,1000) ;
      }

    function
    binUpdate()
      {
      if($scope.bin.pod=='') return ;
      DbFactory
        .post({topic: 'vasBin', action: 'update',
               params: {pod:     $scope.bin.pod,
                        vasBin:  $scope.bin.vasBin,
                        vasCode: $scope.bin.choice,
                        vasDesc: descs[$scope.bin.choice]}}) 
          .success(binUpdateSuccess)
          .error(function() {console.log('bin update error')}) ;
      binNew() ;
      $timeout(refresh,1000) ;
      }

    function
    binUpdateSuccess()
      {
      }

    function
    binClick()
      {
      var data = bin.row(this).data() ;
      $scope.bin.pod     = data.pod ;
      $scope.bin.vasBin  = data.vasBin ;
      $scope.bin.choice   = data.vasCode;
      $scope.$apply() ;
      }

    function
    binSuccess(data)
      {
      var cols = [] ;
      var ref = "#bin" ;

      cols.push({title: "Pod", data:"pod"}) ;
      cols.push({title: "Bin", data:"vasBin"}) ;
      cols.push({title: "Code",        data:"vasCode"}) ;
      cols.push({title: "Description", data:"vasDesc"}) ;

      if(bin)
        {
        bin.clear() ;
        bin.rows.add(data) ;
        bin.draw() ;
        }
      else
        {
        bin = $(ref)
                  .DataTable({data: data, 
                              columns: cols,
                              order: [[0,'asc'],[1,'asc']],
                              scrollX: "100%",
                              scrollY: "500px",
                              scrollCollapse: true,
                              paging: false,
                              dom: 'lftBipr',
                              buttons: ['copy','print','excel','pdf']}) ;
      
        $(ref+' tbody').on('click','tr',binClick) ;
        $timeout(function(){bin.draw()},0) ;
        }

      binNew() ;
      choose() ;
      }

    function
    vasNew()
      {
      $scope.vas.vasCode = '' ;
      $scope.vas.vasDesc = '' ;
      }

    function
    vasUpdate()
      {
      DbFactory
        .post({topic: 'vas', action: 'update',
               params: {vasCode: $scope.vas.vasCode,
                        vasDesc: $scope.vas.vasDesc}}) 
          .success(vasUpdateSuccess)
          .error(function() {console.log('vas update error')}) ;
      $timeout(refresh,1000) ;
      }

    function
    vasUpdateSuccess()
      {
      }

    function
    vasDelete()
      {
      DbFactory
        .post({topic: 'vas', action: 'delete',
               params: {vasCode: $scope.vas.vasCode} }) 
          .error(function() {console.log('vas delete error')}) ;
      vasNew() ;
      $timeout(refresh,1000) ;
      }

    function
    vasClick()
      {
      var data = vas.row(this).data() ;
      $scope.vas.vasCode = data.vasCode ;
      $scope.vas.vasDesc = data.vasDesc ;
      $scope.$apply() ;
      }

    function
    vasSuccess(data)
      {
      var cols = [] ;
      var ref = "#vas" ;

      cols.push({title: "VAS Code", data:"vasCode"}) ;
      cols.push({title: "Description", data:"vasDesc"}) ;

      if(vas)
        {
        vas.clear() ;
        vas.rows.add(data) ;
        vas.draw() ;
        }
      else
        {
        vas = $(ref)
                  .DataTable({data: data, 
                              columns: cols,
                              order: [[0,'asc']],
                              scrollX: "100%",
                              scrollY: "500px",
                              scrollCollapse: true,
                              paging: false,
                              dom: 'lftBipr',
                              buttons: ['copy','print','excel','pdf']}) ;
      
        $(ref+' tbody').on('click','tr',vasClick) ;
        $timeout(function(){vas.draw()},0) ;
        }

      vasNew() ;
      }

    function
    choose()
      {
console.log(thePod+' '+theVasBin) ;
      if(thePod && theVasBin)
        {
        var data = bin.rows(function(idx,data,node)
                              {
                              if(data.pod==thePod)
                                if(data.vasBin==theVasBin)
                                  return true ;
                              return false ;
                              }).data()[0] ;
        if(data)
          {
          $scope.bin.pod = data.pod ;
          $scope.bin.vasBin = data.vasBin ;
          $scope.bin.choice = data.vasCode ;
          }
        thePod='' ;
        theVasBin='' ;
        }
      }

    function
    init()
      {
      Global.setTitle('Value Add') ;
      Global.recv('refresh',refresh,$scope) ;
      thePod = $routeParams.pod ;
      theVasBin = $routeParams.vasBin ;
      }

    init() ;
    }

  function
  vasConfig($routeProvider)
    {
    $routeProvider
      .when('/vas',{controller: 'VasController',
                     templateUrl: '/app/vas/vas.view.html'}) ;
    }

  }() 
) 
  
