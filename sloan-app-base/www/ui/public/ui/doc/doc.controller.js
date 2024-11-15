(function()
{
  angular
    .module('ui')
      .controller('DocController',docController);
  
  angular
    .module('ui')
      .config(docConfig);
  
  docController.$inject = ['$scope','$timeout','$interval',
                            'Global','DbFactory'];
  
  function
  docController($scope,$timeout,$interval,Global,DbFactory)
  {
    var periodic;
    $scope.docs = [];
    
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
    addDoc(title, href, icon, background, desc, tooltip, style)
    {
      var doc = {};
      doc.title = title; // only used in 'titled' style
      doc.href = href;
      // Note that the files are in ui/public/doc, not ui/public/ui/doc
      doc.icon = "icons/"+icon;
      doc.background = background;
      doc.desc = desc;
      doc.tooltip = tooltip;
      doc.style = style;
      $scope.docs.push(doc);
    }
    
    function
    setup()
      {
      $scope.docs=[];
      
      // Unitary examples
      
      
      /*
      addDoc('','/doc/Keyence Power Supply.pdf','pdf.svg','#c33',
             'Keyence Power Supply',
             'Electrical drawing for scanner power supply',
             'unitary');
      */
 
      // Titled example

      /*
      addDoc('ATOP Programs','/doc/PTLSetup.zip','zip.svg','#cc3',
              'Utility Programs',
              'An archive file containing ATOP utility programs',
              'titled');
      */ 
      
      addDoc('ATOP Install','/doc/ATOPInstallation.pdf','pdf.svg','#44a',
             'Installation Instructions',
             'A document describing how to install ATOP devices',
             'titled');
      
      addDoc('PTL Setup','/doc/PTLSetup.zip','zip.svg','#a44',
             'PTL Setup Programs',
             'An archive of programs to setup ATOP networks.',
             'titled');
      
      addDoc('PTL Addresses','/doc/PTLAddressesNetworkInfo.xlsx','xlsx.svg','#4a4',
             'PTL Address Information',
             'A spreadsheet of address information',
             'titled');
      
       
      }
    
    function
    init()
    {
      Global.setTitle('Documents');
      Global.recv('refresh',setup,$scope);
      setup();
    }
    
    init();
  }
  
  function
  docConfig($routeProvider)
  {
    $routeProvider
      .when('/doc',{controller: 'DocController',
                    templateUrl: '/ui/doc/doc.view.html'});
  }
  
}())
