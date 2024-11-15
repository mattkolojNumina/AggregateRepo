(function()
{
  angular
    .module('ui')
      .controller('CartsController',cartsController);

  angular
    .module('ui')
      .config(cartsConfig);

  cartsController.$inject = ['$scope','$timeout','$routeParams',
                              '$mdDialog','$interval','Global','DbFactory'];
  
  function cartsController($scope,$timeout,$routeParams,$mdDialog,$interval,Global,DbFactory) {
    
    $scope.refresh = refresh;
    $scope.cart = {};
    $scope.cartSeq = -1;
	$scope.cartId = "";
    $scope.lookup = lookup;
    $scope.lookupCartId= lookupCartId;
	$scope.selectCart = selectCart;
	$scope.completeCart = completeCart;
    $scope.permit = Global.permit;
	
    // // // // //
    // TABLE HELPERS  
    var refreshCounter = 0;
    
    function refreshCount(n,name) {
      refreshCounter += n;
      if(refreshCounter>0)
        Global.busyRefresh(true);
      else
        Global.busyRefresh(false);
      if(true) //make true for logging
        console.log(name+": "+refreshCounter);
    } 
    
    function simpleDate(data) {
      if(data==null)
        return '';
      var date = new Date(data);
      var today = new Date();
      //if(today.toDateString()==date.toDateString())
      //  return date.toLocaleTimeString();
      return date.toLocaleString();
    }
    
    function
    dateRender_old(data,type,full,meta)
    {
      if(data==null)
        return '';		
      if(type!='display')
        return data;
      
      var date = new Date(data);
	    return date.toLocaleString('en-US',{year: 'numeric', month: 'numeric', day: 'numeric',hour: 'numeric',minute:'numeric' });
      //return date.toLocaleString();
    }
    
    Date.prototype.stdTimezoneOffset = function () {
        var jan = new Date(this.getFullYear(), 0, 1);
        var jul = new Date(this.getFullYear(), 6, 1);
        return Math.max(jan.getTimezoneOffset(), jul.getTimezoneOffset());
    } 

    Date.prototype.isDstObserved = function () {
        return this.getTimezoneOffset() < this.stdTimezoneOffset();
    }    
    
    function
    dateRender(data,type,full,meta)
    {
      if(data){
        if(type=='display'){
          var date = new Date(data);
          const localOffset = new Date().getTimezoneOffset(); // in minutes
          const localOffsetMillis = 60 * 1000 * localOffset;
          const stdOffset = new Date().stdTimezoneOffset(); // in minutes
          const stdOffsetMillis = 60 * 1000 * stdOffset;
          const serverOffset = 300;
          const serverOffsetMillis = 60 * 1000 * serverOffset;
          //const dtsOffset = date.isDstObserved()?60000*60:0;
          const dtsOffset = 0;
          var modifiedOffset = (stdOffsetMillis-dtsOffset-serverOffsetMillis);
          var modifiedDate = new Date(date.getTime()+modifiedOffset);
          return modifiedDate.toLocaleString('en-US',{year: 'numeric', month: 'numeric', day: 'numeric',hour: 'numeric',minute:'numeric' });
        }else{
          var tzoffset = (new Date()).getTimezoneOffset()*60000;
          var localTime = new Date(data) - tzoffset;
          if(!localTime) return '';
          var localISOTime = new Date(localTime).toISOString().slice(0,-1);
          return localISOTime;
        }
      }else{
        return '';
      } 
    }  

    function checkMarkRender(data,type,full,meta){
      if(data==null)
        return '';		
      if(type!='display')
        return data;
      return data >0 ? '&check;' : '';      
    } 

    function buttonRender(ref, data, perm, title, name, cb, enabled) {
        //name = name.replace(/\s+/g, '');
      //console.log("butoon: " + JSON.stringify(data));
      $(ref + ' tbody').off('click', '#' + name + 'button');
      $(ref + ' tbody').on('click', '#' + name + 'button', (e) => cb(data,title));
      if (enabled) { 
        if (Global.permit(perm))
          return '<button id="' + name+ 'button">' + title + '</button>';
        else
          return '<button disabled>' + title + '</button>';
      } else {
          return '';
      }
    }    

    function executeError(err) {
        console.log(err);
        refreshCount(-1, "error");		
    }
	
    function clearCount() {
      refreshCount(-1, "clear ct");
    }
	
    function executeSuccess() {
      refresh();
      refreshCount(-1,'executeSuccess');
    }	

    function toDueDate(date){
      return date.getFullYear() + 
        ('00' +(date.getMonth()+1)).slice(-2) +
        ('00' +(date.getDate())).slice(-2);
    }    

    // // // // //
    // DATA RETRIEVAL
    //======================================================================================================================================
    // // //
    // TAB 0 open carts

    var openCartsAll = [];
    
    function refreshOpenCarts() {
      DbFactory.post({topic: 'carts',
                      action: 'openCarts'
                     })
        .success(openCartsSuccess)
        .error  (executeError);
    }
    
    function openCartsSuccess(data) { 
	    openCartsAll = data ; 
      buildOpenCartsTable(openCartsFilter()); 
    }  
	
    function openCartsFilter(){
      var filtered = [] ;
      for(var i=0 ; i<openCartsAll.length ; i++)
      {
        filtered.push(openCartsAll[i]);
      }
      return filtered;
    }	

    var openCartsTable = null;
    
    function buildOpenCartsTable(data) {
      var cols = [];
      var ref = "#openCarts";

      cols.push({title: "Cart Seq", 		data:"cartSeq", 		class:"dt-center"}) ;
      cols.push({title: "Cart ID",   data:"cartId",class:"dt-center"});
	  cols.push({title: "Cart Type",   data:"cartType",class:"dt-center"});
	  cols.push({title: "# Cartons",   data:"numCartons",class:"dt-center"});
      cols.push({title: "Created",   data:"createStamp",class:"dt-center",render:dateRender});
      cols.push({title: "Built",   data:"buildStamp",class:"dt-center",render:dateRender});
      cols.push({title: "Pick Start",   data:"pickStartStamp",class:"dt-center",render:dateRender});
      
      if(openCartsTable){
        openCartsTable.clear();
        openCartsTable.rows.add(data);
        openCartsTable.draw(false);
      } else {
        openCartsTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              columnDefs: [
                                {                                
                                targets: [0],
                                render: function (data, type, row, meta) 
                                {											
                                  if(data && data !== '') {
                                    data = '<a style="cursor: pointer;"> <strong>' + data + '</strong>';								
                                    return data;
                                  } else {
                                    data = '';
                                    return data;
                                  }
                                }
                                              }
                              ],
                              order: [],
                              scrollY: "700px",
                              scrollCollapse: true,
                              paging: true,
                              pagingType: "numbers",
                              pageLength: 100,
                              dom: 'lftBipr',
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','td',openCartsTableClick);
        setTimeout(function(){openCartsTable.draw();},0);
      }
      //refreshCount(-1);
    }
    
    function openCartsTableClick() {
      var data = openCartsTable.row(this).data();
      var idx = openCartsTable.cell(this).index().column;
      if( data.cartId ){
        if(idx==0){
          // console.log("here");
          $scope.cartSeq = data.cartSeq;
		  $scope.cartId = data.cartId;
          $scope.selected = 1;
          $scope.$apply() ;
          lookup();				
        }
      }
    }
    	
	
	// // //
	// TAB 1 cart detail
	
    function completeCart() {
      var dialog = $mdDialog.confirm()
      .title('Complete Cart')
      .textContent('Are you sure you want to complete cart '+$scope.cart.cartId+'?')
      .ariaLabel('Complete Cart')
      .ok('Yes')
      .cancel('Cancel');

      $mdDialog
        .show(dialog)
        .then(function(){
		  var data = {};
		  data.cartSeq = $scope.cartSeq;
		  var user = Global.getUser().user;
		  DbFactory.post({
				topic: 'carts',
				action: 'insertStatus',
				params: {
					  statusType : 'completeCart',
					  data : JSON.stringify(data),
					  appName: 'statusApp',
					  operator : user
				}
		  })
         .error  (executeError);
         $scope.working = true ;
         $timeout(function() {
            lookup();
            $scope.working = false ;
         },2000) ;
      });
    }	
	
    function lookup() {
      refreshCount(1, "lookup carts");
        DbFactory.post({topic: 'carts',
                        action: 'lookup',
              params: {cartSeq: $scope.cartSeq}
                       })
          .success(populateCart)
          .error  (executeError);			
    }
    
    function lookupCartId() {
      refreshCount(1, "lookup carts");
        DbFactory.post({topic: 'carts',
                        action: 'lookupCartId',
              params: {cartId: '%'+$scope.cartId+'%'}
                       })
          .success(populateCart)
          .error  (executeError);			
    }
	
    function populateCart(data) {
      refreshCount(-1, "populate order");
      if(data.length > 10){
        Global.showMessage('Too many matching records found!');				
        $scope.cart = {};
      } else if(data.length==0) {
        Global.showMessage('Not found!');
        $scope.cart = {};
      } else if(data.length==1){
        $scope.cartSeq = data[0].cartSeq;
        prepareCart( data[0] );
        populateCartons($scope.cart.cartSeq);
        populateCartHistory($scope.cart.cartSeq);
      } else {
          cartsLookupDialog( data );
      }
    }
	
    function cartsLookupDialog( data ) {
      $scope.candidates = data;
      $mdDialog.show({
        templateUrl: 'lookup.html',
        clickOutsideToClose: true,
        scope: $scope,
        preserveScope: true,
        controller: function($scope) {},
        parent: angular.element(document.body),
      })
    }	
	
    function selectCart(cart)  {				
      if (cart) {
        $scope.cartSeq = cart.cartSeq;        
        prepareCart( cart );
        populateCartons($scope.cart.cartSeq);
        populateCartHistory($scope.cart.cartSeq); 
      }
      $mdDialog.hide();			
    }
		
    // cart object
    function prepareCart( cart ) {
      $scope.cart = cart;
    }
    
    // cart cartons
    function populateCartons( cartSeq ) {
      DbFactory.post({topic: 'carts',
                      action: 'cartons',
            params: {cartSeq: cartSeq}
                     })
        .success(populateCartonsSuccess)
        .error  (executeError);			
    }
	
    function populateCartonsSuccess(data) {
      buildCartonsTable(data);
    }		
	
    var cartonsTable = null;	
	
    function buildCartonsTable(data) {
      var cols = [];
      var ref = "#cartons";
      
      cols.push({title: "Carton Seq",   data:"cartonSeq",class:"dt-center"});
      cols.push({title: "LPN",       data:"lpn",class:"dt-center"});
	  cols.push({title: "Tracking",       data:"trackingNumber",class:"dt-center"});
	  cols.push({title: "Type",       data:"cartonType",class:"dt-center"});
      cols.push({title: "Pick Area",   data:"pickType",class:"dt-center"});
	  cols.push({title: "Slot",   data:"cartSlot",class:"dt-center"});
      cols.push({title: "Picked",   data:"pickStamp",class:"dt-center",render:dateRender});      
      
      if(cartonsTable){
        cartonsTable.clear();
        cartonsTable.rows.add(data);
        cartonsTable.draw(false);
      } else {
        cartonsTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              columnDefs: [
                                {                                
                                targets: [0],
                                render: function (data, type, row, meta) 
                                {											
                                  if(data && data !== '') {
                                    data = '<a style="cursor: pointer;"> <strong>' + data + '</strong>';								
                                    return data;
                                  } else {
                                    data = '';
                                    return data;
                                  }
                                }
                                              }
                              ],                              
                              order: [],
                              scrollY: "700px",
                              scrollCollapse: true,
                              paging: true,
                              pagingType: "numbers",
                              dom: 'ftBipr',
                              lengthMenu: [ 50,100 ],
                              pageLength: 100,
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','td',cartCartonsClick) ;
        setTimeout(function(){cartonsTable.draw();},0);
      }
    }	

    function cartCartonsClick() {
      var idx = cartonsTable.cell(this).index().column;		
      var data = cartonsTable.row(this).data() ;
      if( idx == 0 ){
        var cartonSeq = data.cartonSeq;
        window.location = '#/cartons?cartonSeq=' + cartonSeq; 
      } 
    }	

    //order history
    function populateCartHistory( cartSeq ) {
        DbFactory.post({topic: 'carts',
                        action: 'history',
              params: {cartSeq: cartSeq}
                       })
          .success(populateCartHistorySuccess)
          .error  (executeError);			
    }
	
    function populateCartHistorySuccess(data) {
      buildOrderHistoryTable(data);
    }
	
    var cartHistoryTable = null;	
	
    function buildOrderHistoryTable(data) {
      var cols = [];
      var ref = "#history";
      
      cols.push({title: "Code",   data:"code",class:"dt-center"});
      cols.push({title: "Description",     data:"message",class:"dt-center"});
      cols.push({title: "Stamp",			data:"stamp",		class:"dt-center",	type:"date",	render: dateRender});	
      
      if(cartHistoryTable){
        cartHistoryTable.clear();
        cartHistoryTable.rows.add(data);
        cartHistoryTable.draw(false);
      } else {
        cartHistoryTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              order: [],
                              scrollY: "700px",
                              scrollCollapse: true,
                              paging: true,
                              pagingType: "numbers",
                              dom: 'ltBipr',
                              lengthMenu: [ 50,100 ],
                              pageLength: 100,
                              buttons: ['copy','print','excel','pdf']});
        setTimeout(function(){cartHistoryTable.draw();},0);
      }
    }
    
    // // // // //
    // SETUP AND ALL THAT

    function
    refresh()
    {
      switch($scope.selected){
        case 0:
          refreshOpenCarts();
          break;									
        default:
          refreshCount(0, "default tab rendered");
      }
    }

    function
    init()
    {
      Global.setTitle('Carts');
      Global.recv('refresh',refresh,$scope);
      periodic = $interval(refresh, 30000); 
      if($routeParams.cartSeq){
        $scope.selected = 1;
        $scope.cartSeq = $routeParams.cartSeq;
        lookup();
      } 
      refresh();
    }

    init();

    $scope.$on('$destroy', function(){
      $interval.cancel(periodic);
    });
    
  }

  function
  cartsConfig($routeProvider)
  {
    $routeProvider
      .when('/carts', {controller: 'CartsController',
                        templateUrl: '/app/carts/carts.view.html'});
  }
  
}())

