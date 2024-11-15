(
function()
  {
  angular
    .module('ui',['ngMaterial',
                  'ngRoute',
                  'mdPickers']) ;

  angular
    .module('ui')
      .config(uiConfig) ;

  angular
    .module('ui')
      .constant('_',window._) ;

  uiConfig.$inject = ['$mdThemingProvider'] ;

  function
  uiConfig($mdThemingProvider)
    {
    $mdThemingProvider
      .theme('default')
//        .primaryPalette('blue') 
//        .accentPalette('lime') 
;
    }

  }() 
) 
