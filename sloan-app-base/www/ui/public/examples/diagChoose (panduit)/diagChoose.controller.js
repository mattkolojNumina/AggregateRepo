(function()
{
  angular
    .module('ui')
      .controller('DiagChooseController',diagChooseController);

  angular
    .module('ui')
      .config(diagChooseConfig);

  diagChooseController.$inject = ['$scope','Global','DbFactory'];

  function
  diagChooseController($scope,Global,DbFactory)
  {

    $scope.tiles = [];

    function
    addTile(title, href, icon, background, tooltip)
    {
      var tile = {};
      tile.title = title;
      tile.href = href;
      tile.icon = "icons/"+icon;
      tile.background = background;
      tile.tooltip = tooltip;
      $scope.tiles.push(tile);
    }

    function
    setup()
    {
      $scope.tiles=[];



      addTile('Overview','#/diag?rds=OverallLayout','scope.svg','#2E258F',
              'Graphical diagnostics: Overview');

      addTile('Pick One','#/diag?rds=PickMod1','scope.svg','#73246D',
              'Graphical diagnostics: Pick Module 1');

      addTile('Pick Two','#/diag?rds=PickMod2','scope.svg','#73246D',
              'Graphical diagnostics: Pick Module 2');

      addTile('Pick Three','#/diag?rds=PickMod3','scope.svg','#73246D',
              'Graphical diagnostics: Pick Module 3');

      addTile('Picking 6-1','#/diag?rds=6to1Merge','scope.svg','#73246D',
              'Graphical diagnostics: 6-1 Merge Picking to Wave');

      addTile('Pick Flow','#/diag?rds=PickSystem','scope.svg','#73246D',
              'Graphical diagnostics: Pick Flow');

      addTile('Duct Area','#/diag?rds=DuctArea','scope.svg','#73246D',
              'Graphical diagnostics: Duct');

      addTile('Wave Sorter','#/diag?rds=WaveSystem','scope.svg','#358F94',
              'Graphical diagnostics: Wave Sort and Staging');

      addTile('Bridge to Shipping','#/diag?rds=Bridge','scope.svg','#0A615D',
              'Graphical diagnostics: Bridge from Wave to Shipping');

      addTile('Ship Sorter','#/diag?rds=ShipSorter','scope.svg','#248536',
              'Graphical diagnostics: Shipping Sorter');

      addTile('H - Loop','#/diag?rds=HLoop','scope.svg','#2D4C7D',
              'Graphical diagnostics: H-Loop Pack Sorter');

      addTile('J - Loop','#/diag?rds=JLoop','scope.svg','#2D4C7D',
              'Graphical diagnostics: J-Loop Pack Sorter');

      addTile('K - Loop','#/diag?rds=KLoop','scope.svg','#2D4C7D',
              'Graphical diagnostics: K-Loop Pack Sorter');

      addTile('Ship Staging','#/diag?rds=ShipStaging','scope.svg','#248536',
              'Graphical diagnostics: Ship/Pack Stage Lanes');

      addTile('Pack Takeaway','#/diag?rds=PackTkwy','scope.svg','#2D4C7D',
              'Graphical diagnostics: Pack Takeway');

      addTile('L-Loop','#/diag?rds=LLoop','scope.svg','#248536',
                      'Graphical diagnostics: L-Loop Sorter');
    }

    function
    init()
    {
      Global.setTitle('Choose Diagnostic Area');
//      Global.recv('refresh',refresh,$scope);
//      refresh();
      setup();
    }

    init();
  }

  function
  diagChooseConfig($routeProvider)
  {
    $routeProvider
      .when('/diagChoose',{controller: 'DiagChooseController',
                           templateUrl: '/app/diagChoose/diagChoose.view.html'});
  }

}())
