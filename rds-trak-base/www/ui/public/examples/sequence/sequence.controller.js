(
function()
  {
  angular
    .module('ui')
      .controller('SequenceController',sequenceController) ;

  angular
    .module('ui')
      .config(sequenceConfig) ;

  sequenceController.$inject = ['$scope','$routeParams','$interval','$timeout',
                                  'Global','DbFactory'] ;
  
  function
  sequenceController($scope,$routeParams,$interval,$timeout,
                       Global,DbFactory)
    {
    var periodic ;
    var theCubby = '' ;
    var sequence = null ;

    $scope.refresh = refresh ;
    $scope.permit = Global.permit ;
    $scope.sequence = {} ;
    $scope.sequenceUpdate = sequenceUpdate ;

    function
    refresh()
      {
      DbFactory
        .post({topic: 'sequence', action: 'all'}) 
          .success(sequenceSuccess)
          .error(function(){console.log('sequence error')}) ; 
      }

    function
    sequenceNew()
      {
      $scope.sequence.cubby = '' ;
      $scope.sequence.sequence = '' ;
      }

    function
    sequenceUpdate()
      {
      DbFactory
        .post({topic: 'sequence', action: 'update',
               params: {cubby: $scope.sequence.cubby,
                        cubbySeq: $scope.sequence.sequence}})
          .success(sequenceUpdateSuccess)
          .error(function() {console.log('sequence update error')}) ;
      $timeout(refresh,1000) ;
      }

    function
    sequenceUpdateSuccess()
      {
      }

    function
    sequenceClick()
      {
      var data = sequence.row(this).data() ;
      $scope.sequence.cubby = data.cubby ;
      $scope.sequence.sequence = data.cubbySeq ;
      $scope.$apply() ;
      }

    function
    sequenceSuccess(data)
      {
      var cols = [] ;
      var ref = "#sequence" ;

      cols.push({title: "Cubby", data:"cubby"}) ;
      cols.push({title: "Sequence", data:"cubbySeq",
                                   class:"dt-right"}) ;

      if(sequence)
        {
        sequence.clear() ;
        sequence.rows.add(data) ;
        sequence.draw() ;
        }
      else
        {
        sequence = $(ref)
                  .DataTable({data: data, 
                              columns: cols,
                              order: [[0,'asc']],
                              scrollX: "100%",
                              scrollY: "500px",
                              scrollCollapse: true,
                              paging: false,
                              dom: 'lftBipr',
                              buttons: ['copy','print','excel','pdf']}) ;
      
        $(ref+' tbody').on('click','tr',sequenceClick) ;
        $timeout(function(){sequence.draw()},0) ;
        }

      sequenceNew() ;
      choose() ;
      }

    function
    choose()
      {
      if(theCubby)
        {
        var data 
          = sequence
              .rows(function(idx,data,node)
                {
                if(data.cubby==theCubby)
                  return true ;
                return false ;
                })
              .data()[0] ;
        if(data)
          {
          $scope.sequence.cubby = data.cubby ;
          $scope.sequence.sequence = data.cubbySeq ;
          }
        theCubby='' ;
        }
      }

    function
    init()
      {
      Global.setTitle('Cubby Sequence') ;
      Global.recv('refresh',refresh,$scope) ;
      theCubby = $routeParams.cubby ;
      refresh() ;
      }

    init() ;
    }

  function
  sequenceConfig($routeProvider)
    {
    $routeProvider
      .when('/sequence',{controller: 'SequenceController',
                     templateUrl: '/app/sequence/sequence.view.html'}) ;
    }

  }() 
) 
  
