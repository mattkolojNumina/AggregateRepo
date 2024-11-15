(function()
{
  angular
    .module('ui')
      .controller('MainController',mainController);
  
  angular
    .module('ui')
      .config(mainConfig);
  
  mainController.$inject = ['$scope','$timeout','$interval',
                            'Global','DbFactory'];
  
  function
  mainController($scope,$timeout,$interval,Global,DbFactory)
    {
    var periodic;
    $scope.tiles ;

    function
    addTile(group, title, href, icon, background, tooltip, perm, openInNewTab)
      {
      if(perm=='' || Global.permit(perm))
        {
        var tile = {};
        tile.title = title;
        tile.href = href;
        tile.icon = "icons/"+icon;
        tile.background = background;
        tile.tooltip = tooltip;
        if(openInNewTab) tile.target = "_blank";
        else target = undefined;
        $scope.tiles[group].push(tile);
        }
      }
    
    function
    setup()
      {
      DbFactory
        .post({topic: 'main',
               action: 'all'}) 
          .success(loadTiles)
          .error(function(){console.log('menu all error');}) ;
      }
    
    function
    loadTiles(data)
      {
      var groups = 5 ;
      $scope.tiles = null ;
      $scope.tiles= new Array(5) ;
      for(var g=0 ; g<groups ; g++)
        {
        $scope.tiles[g] = null ;
        $scope.tiles[g] = [] ;
        }

      for(var i=0 ; i<data.length ; i++)
        addTile(data[i].block,
                data[i].title,
                data[i].link,
                data[i].icon,
                data[i].color,
                data[i].description,
                data[i].perm,
                data[i].openInNewTab) ;
      }
    
    function
    init()
      {
      Global.setTitle('Main'); 
      Global.recv('refresh',setup,$scope);
      Global.recv('newUser',setup,$scope) ;
      setup();
      }
    
    init();
    }
  
  function
  mainConfig($routeProvider)
  {
    $routeProvider
      .when('/',{controller: 'MainController',
                 templateUrl: '/ui/main/main.view.html'});
  }
  
}())
