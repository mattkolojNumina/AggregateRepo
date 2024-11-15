(function()
  {
  angular
    .module('ui')
      .controller('RecentController',recentController);
  
  angular
    .module('ui')
      .config(recentConfig);
  
  recentController.$inject = ['$scope','$routeParams','$timeout','$interval',
                              'Global','DbFactory'];
  
  function
  recentController($scope,$routeParams,$timeout,$interval,
                   Global,DbFactory)
    {
    var periodic;
    
    // tabs
    $scope.selected = 0;
    $scope.refresh = refresh;
    $scope.permit = Global.permit;
    
    function
    someError(err)
      { 
      console.error(err); 
      }
    
    function
    refresh()
      {
      if($scope.selected==0)
        eastPNARefresh();
      if($scope.selected==1)
        westPNARefresh();
      if($scope.selected==2)
        sortRefresh() ; 
      }
    
    // tab 0 - eastPNA 
    
    var eastPNA = null;
   
    function
    eastPNARefresh()
      {
      DbFactory
        .post({topic: 'recent',
               action: 'eastPNA' })
        .success(eastPNASuccess)
        .error  (someError); 
      }
    
    function
    eastPNASuccess(data)
      {
      var cols = [];
      var ref = "#eastPNA";
      
      cols.push({title: "LPN",data:"lpn",class:"dt-center"});
      cols.push({title: "Description",data:"description",class:"dt-left"});
      cols.push({title: "Stamp",  data:"stamp",render:dateRender,class:"dt-center"}) ;
      
      if(eastPNA)
        {
        eastPNA.clear();
        eastPNA.rows.add(data);
        eastPNA.draw(false);
        } 
      else 
        {
        eastPNA 
          = $(ref)
           .DataTable({data: data, 
                       columns: cols,
                       order: [],
                       rowCallback: eastPNACallback,
                       scrollY: "550px",
                       scrollX: true,
                       scrollCollapse: true,
                       paging: false,
                       dom: 'lftBipr',
                       buttons: ['copy',
                                 'print',
                                 {extend:'excel',
                                  exportOptions:{orthogonal:'exportExcel'}},
                                 'pdf']});
        $(ref+' tbody').on('click','tr',eastPNAClick) ;
        $timeout(eastPNA.draw,1) ; 
        }
      }
   
    function
    eastPNAClick()
      {
      var data = eastPNA.row(this).data() ;
      $scope.$apply() ;
      }

    function
    eastPNACallback(row,data,index)
      {
      if(data.status==-1)
        $(row).css('color','#F00') ;
      else if(data.status==1)
        $(row).css('color','#080') ;
      else
        $(row).css('color','#000') ;
      }


    // tab 1 - westPNA 
    
    var westPNA = null;
   
    function
    westPNARefresh()
      {
      DbFactory
        .post({topic: 'recent',
               action: 'westPNA' })
        .success(westPNASuccess)
        .error  (someError); 
      }
    
    function
    westPNASuccess(data)
      {
      var cols = [];
      var ref = "#westPNA";
      
      cols.push({title: "LPN",data:"lpn",class:"dt-center"});
      cols.push({title: "Description",data:"description",class:"dt-left"});
      cols.push({title: "Stamp",  data:"stamp",render:dateRender,class:"dt-center"}) ;
      
      if(westPNA)
        {
        westPNA.clear();
        westPNA.rows.add(data);
        westPNA.draw(false);
        } 
      else 
        {
        westPNA 
          = $(ref)
           .DataTable({data: data, 
                       columns: cols,
                       order: [],
                       rowCallback: westPNACallback,
                       scrollY: "550px",
                       scrollX: true,
                       scrollCollapse: true,
                       paging: false,
                       dom: 'lftBipr',
                       buttons: ['copy',
                                 'print',
                                 {extend:'excel',
                                  exportOptions:{orthogonal:'exportExcel'}},
                                 'pdf']});
        $(ref+' tbody').on('click','tr',westPNAClick) ;
        $timeout(westPNA.draw,1) ; 
        }
      }
   
    function
    westPNAClick()
      {
      var data = westPNA.row(this).data() ;
      $scope.$apply() ;
      }

    function
    westPNACallback(row,data,index)
      {
      if(data.status==-1)
        $(row).css('color','#F00') ;
      else if(data.status==1)
        $(row).css('color','#080') ;
      else
        $(row).css('color','#000') ;
      }

    // tab 2 - sort 
    
    var sort = null;
   
    function
    sortRefresh()
      {
      DbFactory
        .post({topic: 'recent',
               action: 'sort' })
        .success(sortSuccess)
        .error  (someError); 
      }
    
    function
    sortSuccess(data)
      {
      var cols = [];
      var ref = "#sort";
     
      cols.push({title: "Window",   data:"box",class:"dt-center"}) ; 
      cols.push({title: "Status",   data:"status",class:"dt-center"}) ;
      cols.push({title: "Description",data:"description",class:"dt-left"});
      cols.push({title: "Tracking",data:"trackingNumber",class:"dt-center"});
      cols.push({title: "UPC",     data:"upc",class:"dt-center"}) ;
      cols.push({title: "Truck",   data:"truck",class:"dt-center"}) ; 
      cols.push({title: "Door",   data:"door",class:"dt-center"}) ; 
      cols.push({title: "Lane",   data:"lane",class:"dt-center"}) ; 
      cols.push({title: "Stamp",  data:"stamp",render:dateRender,class:"dt-center"}) ;
      
      if(sort)
        {
        sort.clear();
        sort.rows.add(data);
        sort.draw(false);
        } 
      else 
        {
        sort 
          = $(ref)
           .DataTable({data: data, 
                       columns: cols,
                       order: [],
                       rowCallback: sortCallback,
                       scrollY: "550px",
                       scrollX: true,
                       scrollCollapse: true,
                       paging: false,
                       dom: 'lftBipr',
                       buttons: ['copy',
                                 'print',
                                 {extend:'excel',
                                  exportOptions:{orthogonal:'exportExcel'}},
                                 'pdf']});
        $(ref+' tbody').on('click','tr',sortClick) ;
        $timeout(sort.draw,1) ; 
        }
      }
   
    function
    sortClick()
      {
      var data = sort.row(this).data() ;
      var cartonSeq = data.cartonSeq ;
      if(cartonSeq!=0)
        window.location='#/cartons?cartonSeq='+cartonSeq ;
      }

    function
    sortCallback(row,data,index)
      {
      if(data.status.substring(0,2)=='Er')
        $(row).css('color','#F00') ;
      else if(data.status.substring(0,2)=='Sc')
        $(row).css('color','#DA1') ;
      else if(data.status.substring(0,2)=='Re')
        $(row).css('color','#269') ;
      else if(data.status.substring(0,2)=='So')
        $(row).css('color','#080') ;
      else
        $(row).css('color','#000') ;
      if(data.code<0)
        $(row).find('td:eq(2)').css('color','#F00') ; 
      }

    function
    dateConvert(old) 
      {
      if (old == null)
        return '';
      var date = new Date(old);
      var today = new Date();
      if (today.toDateString() == date.toDateString())
        return date.toLocaleTimeString();
      return date.toLocaleString();
      }
    
    function
    dateRender(data,type,full,meta)
      {
      if(data)
        {
        if(type=='display')
          {
          var date = new Date(data);
          return date.toLocaleString();
          }
        else
          {
          var tzoffset = (new Date()).getTimezoneOffset()*60000;
          var localTime = new Date(data) - tzoffset;
          if(!localTime) return '';
          var iso = new Date(localTime).toISOString() ;
          iso = iso.replace('T',' ') ;
          iso = iso.replace('Z','') ;
          iso = iso.slice(0,-4) ;
          return iso ;
          }
        }
      else
        {
        return '';
        }
      }

    function
    init()
      {
      Global.setTitle('Recent View');
      Global.recv('refresh',refresh,$scope);
      periodic = $interval(refresh,1000) ;
      }
   
    $scope.$on('$destroy',function(){$interval.cancel(periodic);}) ;
 
    init();
    }
  
  function
  recentConfig($routeProvider)
    {
    $routeProvider
      .when('/recent',{controller: 'RecentController',
                     templateUrl: '/app/recent/recent.view.html'});
    }
  
  }())
