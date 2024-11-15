(function()
{
  angular
    .module('ui')
      .controller('HeadController',headController);
  
  headController.$inject = ['$scope','$mdDialog','$mdToast','DbFactory','$interval',
                            'Global','LoginFactory'];
  
  function
  headController($scope,$mdDialog,$mdToast,DbFactory,$interval,
                 Global,LoginFactory)
  {
    $scope.title = '';
    $scope.name = '';
    $scope.refresh = refresh;
    $scope.leftToggle = leftToggle;
    $scope.rightToggle = rightToggle;
    $scope.busyRefresh = false;
    $scope.showLogin = showLogin;
    $scope.errorCounter = 0;
    
    $scope.synchAlertOn = false;
    $scope.connectionAlertOn = false;
    
    var beatSeconds = 5;  //seconds between heartbeats
    var beatLeeway = 2;   //can miss this many heartbeats before triggering alert
    
    function
    setTitle(event,data)
    { $scope.title = data.title; }
    
    function
    busyRefresh(event,data)
    { $scope.busyRefresh = data.busy; }
    
    function
    refresh()
    { Global.send('refresh'); }
    
    function
    leftToggle()
    { Global.send('leftToggle'); }
    
    function
    rightToggle()
    { Global.send('rightToggle'); }
    
    function
    showMessage(event,data)
    {
      var toast = $mdToast
        .simple()
        .textContent(data.text)
        .hideDelay(3000)
        .parent('#body')
        .position('top right');
      $mdToast.show(toast);
    }
   
    function
    newUser(event,data)
    { $scope.name = data.name; }
   
    function
    reset(event)
    { LoginFactory.logout() ; }
 
    function
    showLogin(event)
    {
      var user = Global.getUser();
      if(!user.token){
        $mdDialog.show(
          {
          controller: 'LoginController',
          templateUrl: 'ui/login/login.dialog.html',
          targetEvent: event,
          openFrom: '#login',
          closeTo: '#login',
          clickOutsideToClose: true
          })
      } else {
        function
        cancel()
        {}
        
        function
        logout()
        { LoginFactory.logout(); }
        
        var dialog = $mdDialog.confirm()
          .title('Log out?') 
          .textContent('You are logged in as '+user.name) 
          .targetEvent(event)
          .openFrom('#login')
          .closeTo('#login')
          .ok('Log Out')
          .cancel('Cancel');
        $mdDialog
          .show(dialog)
          .then(logout,cancel);
      }
    }
    
    function
    keepalive()
    {
      heartbeat();
      heartlisten();
    }
    
    function
    heartbeat()
    {
      DbFactory.post({topic:'heart',
                      action:'beat',
                      params:{date: new Date()}
                      //not using SQL stamp in case of time zone mismatch
                     })
        .success(beatSuccess)
        .error  (beatError);
    }
    
    function
    beatSuccess()
    { cancelConnectionAlert(); }
    
    function
    beatError(err)
    { console.error(err); }

    function
    heartlisten()
    {
      DbFactory.post({topic:'heart',
                      action:'listen'
                     })
        .success(listenSuccess)
        .error  (listenError);
    }
    
    function
    listenSuccess(data)
    {
      $scope.errorCounter = 0;
      cancelConnectionAlert();
      var thisDate = new Date();
      var lastDate = new Date(data[0].value);
      if(thisDate - lastDate > 1000*(beatSeconds*beatLeeway+2)){
        //i.e. missing beatLeeway heartbeats, with 2s margin of error
        displaySynchAlert();
      } else {
        cancelSynchAlert();
      }
    }
    
    function
    listenError(err)
    {
      console.error(err);
      $scope.errorCounter++;
      if($scope.errorCounter>2){
        displayConnectionAlert();
      }
    }
    
    function
    displaySynchAlert()
    { $scope.synchAlertOn = true; }
    
    function
    cancelSynchAlert()
    { $scope.synchAlertOn = false; }
    
    function
    displayConnectionAlert()
    { $scope.connectionAlertOn = true; }
    
    function
    cancelConnectionAlert()
    { $scope.connectionAlertOn = false; }
    
    
    // // // // //
    
    function
    init()
    {
      Global.recv('setTitle',   setTitle,   $scope); 
      Global.recv('busyRefresh',busyRefresh,$scope);
      Global.recv('showMessage',showMessage,$scope);
      Global.recv('newUser',    newUser,    $scope);
      Global.recv('reset',      reset,      $scope);
      if($scope.name=='')
        LoginFactory.login('guest','guest');
      
      heartbeat();
      periodic = $interval(keepalive, 1000*beatSeconds);
    }
    
    init();
    
    $scope.$on('$destroy', function(){
      $interval.cancel(periodic);
    });
  }
  
}())
