(
function()
  {
  angular
    .module('ui')
      .controller('DashController',dashController) ;

  angular
    .module('ui')
      .config(dashConfig) ;

  dashController.$inject = ['$scope','$timeout','$interval',
                            'Global','DbFactory'] ;
  
  function
  dashController($scope,$timeout,$interval,Global,DbFactory)
    {
    var periodic ;
    var put ;
    var pack ;

    $scope.waves = [] ;
    
    function
    refreshSuccess(data,status,headers,config) 
      {
      var topic = config.data.topic ;
      var action = config.data.action ;

      for(var c=0 ; c<$scope.charts.length ; c++)
        {
        if( ($scope.charts[c].topic ==topic ) &&
            ($scope.charts[c].action==action)   )
          {
          if($scope.charts[c].type=='gauge')
            {
            var value = data[0].value ;
            $scope.charts[c].chart.load(
              {
              columns: [['data',value]]
              } ) ;
            }

          if($scope.charts[c].type=='bar')
            {
            var columns = [[]] ;
            for(var key in data[0])
              {
              var line = [] ;
              line.push(key) ;
              line.push(data[0][key]) ;
              columns.push(line) ;
              }
            $scope.charts[c].chart.load(
              {
              columns: columns
              } ) ;
            }
          }
        }
      }


    function
    podSuccess(data)
      {
      $scope.waves = data ;
      }

    function
    putSuccess(data)
      {
      var rate = data[0].rate ;
      put.chart.load( { columns: [['rate',rate]] } ) ;
      }

    function
    packSuccess(data)
      {
      var rate = data[0].rate ;
      pack.chart.load( { columns: [['rate',rate]] } ) ;
      }

    function
    refresh()
      {
      DbFactory
        .post({topic: 'stat', action: 'active'})
        .success(podSuccess) ;
      if(put.chart==null)
        put.chart = c3.generate(put.config) ;
      DbFactory
        .post({topic: 'stat', action: 'puts'})
        .success(putSuccess) ;
      if(pack.chart==null)
        pack.chart = c3.generate(pack.config) ;
      DbFactory
        .post({topic: 'stat', action:'packs'})
        .success(packSuccess) ;
      }

    function
    addGauge(id,title,topic,action)
      {
      var chart = {} ;
      chart.id    = id ;
      chart.title = title ;
      chart.topic = topic ;
      chart.action = action ;
      chart.type = 'gauge' ;

      chart.chart  = null ;
      chart.config =
        {
        bindto: '#'+id,
        data: { columns: [['rate','50']],
                type:    'gauge' },
        gauge: { label: {format: function(value,ratio){return value;}}},
        color: { pattern: ['#ff0000','#f97600','#f6c600','#60b044'],
                 threshold: { values: [25,50,75,100] } }
        } ;
      return chart ;
      }

    function
    setup()
      {
      put = addGauge("put","Puts/Hour","stat","puts") ;
      put.config.gauge.units = 'pph' ;
      put.config.gauge.min   =  0 ;
      put.config.gauge.max   = 1000 ;
      put.config.color= { pattern: ['#ff0000','#f97600','#f6c600','#60b044'],
                          threshold: { values: [200,500,750,900] } }


      pack = addGauge("pack","Packs/Hour","stat","packs") ;
      pack.config.gauge.units = 'pph' ;
      pack.config.gauge.min   = 0 ;
      pack.config.gauge.max   = 1000 ;
      pack.config.color= { pattern: ['#ff0000','#f97600','#f6c600','#60b044'],
                          threshold: { values: [200,500,750,900] } }
      }

    function
    init()
      {
      Global.setTitle('Dashboard') ; 
      Global.recv('refresh',refresh,$scope) ;
      setup() ;
      $timeout(refresh,100);
      periodic = $interval(refresh,5000) ;
      }

    $scope.$on('$destroy',function()
      {
      $interval.cancel(periodic) ;
      }) ;

    init() ;
    }

  function
  dashConfig($routeProvider)
    {
    $routeProvider
      .when('/dash',{controller: 'DashController',
                     templateUrl: '/ui/dash/dash.view.html'}) ;
    }

  }() 
) 
