(
function()
  {
  angular
    .module('ui')
      .controller('NavaidsController',navaidsController) ;

  angular
    .module('ui')
      .config(navaidsConfig) ;

  navaidsController.$inject = ['$scope','$interval','$timeout',
                              'Global','DbFactory'] ;
  
  function
  navaidsController($scope,$interval,$timeout,
                   Global,DbFactory)
    {
    var periodic ;

    $scope.refresh = refresh ;
    $scope.permit = Global.permit ;
 
    var navaids = null ;

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
    refresh()
      {
      DbFactory
        .post({topic: 'navaids', action: 'all'}) 
          .success(navaidsSuccess)
          .error(function(){console.log('navaids error')}) ; 
      }

    function
    navaidsSuccess(data)
      {
      var cols = [] ;
      var ref = "#navaids" ;

      cols.push({title:"Name",        data:"name"}) ;
      cols.push({title:"Type",        data:"type"}) ;

      if(navaids)
        {
        navaids.clear() ;
        navaids.rows.add(data) ;
        navaids.draw() ;
        }
      else
        {
        navaids = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              rowCallback: navaidsCallback,
                              order: [[0,'asc']],
                              scrollY: "200px",
                              scrollCollapse: true,
                              paging: true,
                              dom: 'plftBir',
                              buttons: ['copy','print','excel','pdf']}) ;
        $(ref+' tbody').on('click','tr',navaidsClick) ;
        $timeout(function(){
          navaids.draw() ;
          },0) ;
        }
      }

    function
    navaidsCallback(row,data,index)
      {
      }   
   
    function
    navaidsClick()
      {
         console.log( "clicked something" );
      }
 
    function
    init()
      {
      Global.setTitle('Navaids') ;
      Global.recv('refresh',refresh,$scope) ;
      refresh() ;
      }

    init() ;
    }

  function
  navaidsConfig($routeProvider)
    {
    $routeProvider
      .when('/navaids',{controller: 'NavaidsController',
                     templateUrl: '/app/navaids/navaids.view.html'}) ;
    }

  }() 
) 
  
