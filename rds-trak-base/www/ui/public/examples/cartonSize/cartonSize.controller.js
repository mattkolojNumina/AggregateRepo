(
function()
  {
  angular
    .module('ui')
      .controller('CartonSizeController',cartonSizeController) ;

  angular
    .module('ui')
      .config(cartonSizeConfig) ;

  cartonSizeController.$inject = ['$scope','$interval','$timeout',
                                  'Global','DbFactory'] ;
  
  function
  cartonSizeController($scope,$interval,$timeout,
                       Global,DbFactory)
    {
    var periodic ;

    $scope.refresh = refresh ;
    $scope.permit = Global.permit ;
 
    var carton = null ;

    $scope.carton = {} ;
    $scope.cartonNew = cartonNew ;
    $scope.cartonUpdate = cartonUpdate ;
    $scope.cartonDelete = cartonDelete ;

    function
    refresh()
      {
      DbFactory
        .post({topic: 'cartonSize', action: 'all'}) 
          .success(cartonSuccess)
          .error(function(){console.log('carton error')}) ; 
      }

    function
    cartonNew()
      {
      $scope.carton.cartonSize = '' ;
      $scope.carton.cubbies = '' ;
      $scope.carton.autobaggerEligible = '' ;
      }

    function
    cartonUpdate()
      {
      DbFactory
        .post({topic: 'cartonSize', action: 'update',
               params: {cartonSize: $scope.carton.cartonSize,
                        cubbies: $scope.carton.cubbies,
                        autobaggerEligible: $scope.carton.autobaggerEligible} })
          .success(cartonUpdateSuccess)
          .error(function() {console.log('carton update error')}) ;
      $timeout(refresh,1000) ;
      }

    function
    cartonUpdateSuccess()
      {
      }

    function
    cartonDelete()
      {
      DbFactory
        .post({topic: 'cartonSize', action: 'delete',
               params: {cartonSize: $scope.carton.cartonSize} }) 
          .error(function() {console.log('user role delete error')}) ;
      cartonNew() ;
      $timeout(refresh,1000) ;
      }

    function
    cartonClick()
      {
      var data = carton.row(this).data() ;
      $scope.carton.cartonSize = data.cartonSize ;
      $scope.carton.cubbies = data.cubbies ;
      $scope.carton.autobaggerEligible = data.autobaggerEligible ;
      $scope.$apply() ;
      }

    function
    cartonSuccess(data)
      {
      var cols = [] ;
      var ref = "#carton" ;

      cols.push({title: "Carton Size", data:"cartonSize"}) ;
      cols.push({title: "Cubbies", data:"cubbies",
                                   class:"dt-right"}) ;
      cols.push({title: "AutoBagger Eligible", data:"autobaggerEligible",
                                               class:"dt-center"}) ;

      if(carton)
        {
        carton.clear() ;
        carton.rows.add(data) ;
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
      
        $(ref+' tbody').on('click','tr',cartonClick) ;
        $timeout(function(){carton.draw()},0) ;
        }

      cartonNew() ;
      }

    function
    cartonCallback(row,data,index)
      {
      if(data.autobaggerEligible==1)
        $('td:eq(2)',row).html('yes') ;
      else
        $('td:eq(2)',row).html('no') ;
      }   
    
    function
    init()
      {
      Global.setTitle('Carton Sizes') ;
      Global.recv('refresh',refresh,$scope) ;
      refresh() ;
      }

    init() ;
    }

  function
  cartonSizeConfig($routeProvider)
    {
    $routeProvider
      .when('/cartonSize',{controller: 'CartonSizeController',
                     templateUrl: '/app/cartonSize/cartonSize.view.html'}) ;
    }

  }() 
) 
  
