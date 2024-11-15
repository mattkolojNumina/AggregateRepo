(
function()
  {
  angular
    .module('ui')
      .controller('DocController',docController) ;

  angular
    .module('ui')
      .config(docConfig) ;

  docController.$inject = ['$scope','$timeout','$interval',
                            'Global','DbFactory'] ;
  
  function
  docController($scope,$timeout,$interval,Global,DbFactory)
    {
    var periodic ;
    $scope.docs = [] ;

    function
    addDoc(title, href, icon, background, desc, tooltip)
      {
      var doc = {} ;
      doc.title = title ;
      doc.href = href ;
      doc.icon = "icons/"+icon ;
      doc.background = background ;
      doc.desc = desc ;
      doc.tooltip = tooltip ;
      $scope.docs.push(doc) ;
      }

    function
    setup()
      {
      $scope.docs=[] ;

      addDoc('ATOP Install','/doc/ATOPInstallation.pdf','pdf.svg','#3333cc',
              'Installation Instructions', 
              'A document describing how to install ATOP devices') ;

      addDoc('ATOP Programs','/doc/PTLSetup.zip','zip.svg','#cc3333',
              'Utility Programs', 
              'An archive file containing ATOP utility programs') ;

      addDoc('ATOP Network','/doc/PTLAddressesNetworkInfo.xlsx',
             'xlsx.svg','#33cc33',
             'Network Information', 
             'A spreadsheet containing PTL addresses and network information') ;

      }

    function
    init()
      {
      Global.setTitle('Documents') ; 
      Global.recv('refresh',setup,$scope) ;
      setup() ;
      }

    init() ;
    }

  function
  docConfig($routeProvider)
    {
    $routeProvider
      .when('/doc',{controller: 'DocController',
                    templateUrl: '/app/doc/doc.view.html'}) ;
    }

  }() 
) 
