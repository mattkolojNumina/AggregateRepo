(function()
{
  angular
    .module('ui')
      .controller('DeviceController',deviceController);
  
  angular
    .module('ui')
      .config(deviceConfig);
  
  deviceController.$inject = ['$scope','$timeout','$interval',
                            'Global','DbFactory'];
  
  function
  deviceController($scope,$timeout,$interval,Global,DbFactory)
  {
    var periodic;
    $scope.devices = [];
    
    /*
     * For each tile, the developer may choose between 'titled'
     * and 'unitary' display styles.
     *
     * Titled looks a bit cooler, but you have to come up with
     * both a title and a description, neither one of which is 
     * more than a few words.
     *
     * Unitary displays no title, so a great deal more room is
     * made available for the description, which is put in a
     * correspondingly slightly larger font.
     */
    
    function
    addDevice(title, href, icon, background, desc, tooltip, style)
    {
      var device = {};
      device.title = title; // only used in 'titled' style
      device.href = href;
      device.icon = "icons/"+icon;
      device.background = background;
      device.desc = desc;
      device.tooltip = tooltip;
      device.style = style;
      $scope.devices.push(device);
    }
    
    function
    setup()
      {
      $scope.devices=[];
      
      // Unitary examples
      
      
      /*
      addDevice('','/device/Keyence Power Supply.pdf','pdf.svg','#c33',
             'Keyence Power Supply',
             'Electrical drawing for scanner power supply',
             'unitary');
      */
 
      // Titled example

      /*
      addDevice('ATOP Programs','/device/PTLSetup.zip','zip.svg','#cc3',
              'Utility Programs',
              'An archive file containing ATOP utility programs',
              'titled');
      */ 
      
      addDevice('Demo','http://172.17.1.99','web.svg','#228',
             'Demo Device',
             'Demo device',
             'titled');

      }
    
    function
    init()
    {
      Global.setTitle('Devices');
      Global.recv('refresh',setup,$scope);
      setup();
    }
    
    init();
  }
  
  function
  deviceConfig($routeProvider)
  {
    $routeProvider
      .when('/device',{controller: 'DeviceController',
                    templateUrl: '/ui/device/device.view.html'});
  }
  
}())
