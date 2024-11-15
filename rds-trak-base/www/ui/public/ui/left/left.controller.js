(
function()
  {
  angular
    .module('ui')
      .controller('LeftController',leftController) ;

  leftController.$inject = ['$scope','$mdSidenav','Global'] ;
  
  function
  leftController($scope,$mdSidenav,Global)
    {
    $scope.leftToggle = leftToggle ;
    $scope.menus = [] ;

    function
    addMenu(title, href, perm)
      {
      if(perm=='' || Global.permit(perm))
        {
        var menu = {} ;
        menu.title = title ;
        menu.href  = href ;
        $scope.menus.push(menu) ;
        }
      }

    function
    setup()
      {
      $scope.menus = [] ;
      addMenu('Main',     '#/',       '') ;
      addMenu('Waves',    '#/wave',   'waveView') ;
      addMenu('Pods',     '#/pod',    'podView') ;
      addMenu('Items',    '#/item',   'itemView') ;
      addMenu('Devices',  '#/device', 'deviceView') ;
      addMenu('Value Add','#/vas',    'vasView') ;
      addMenu('Carton Sizes','#/cartonSize','cartonSizeView') ;
      addMenu('Sequence','#/sequence','sequenceView') ;
      addMenu('Reports',  '#/report', 'reportView') ;
      addMenu('Dashboard','#/dash',   '') ;
      addMenu('Events',   '#/event',  'eventView') ;
      addMenu('Documents','#/doc',    '') ;
      addMenu('Users',    '#/user',   'userView') ;
      addMenu('System',   '#/system', 'systemView') ;
      addMenu('Controls', '#/control','controlView') ;
      addMenu('Navaids',  '#/navaids','navaidView') ;
      }  

    function
    newUser(event,user)
      {
      setup() ;
      }

    function
    leftToggle()
      {
      $mdSidenav('left').toggle() ;
      }

    function
    init()
      {
      Global.recv('leftToggle',leftToggle,$scope) ;
      Global.recv('newUser',newUser,$scope) ;
      setup() ;
      }

    init() ;
    }

  }() 
) 
