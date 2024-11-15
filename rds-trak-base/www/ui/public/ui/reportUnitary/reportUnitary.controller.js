(function()
{
  angular
    .module('ui')
      .controller('ReportUnitaryController',reportUnitaryController);
  
  angular
    .module('ui')
      .config(reportUnitaryConfig);
  
  reportUnitaryController.$inject = ['$scope','$compile','$timeout','Global','DbFactory','_'];
  
  function
  reportUnitaryController($scope,$compile,$timeout,Global,DbFactory,_)
  {
    $scope.permit = Global.permit;
    
    $scope.view_reports = [];
    $scope.displayReport = displayReport;
    $scope.itemCounts = [];
    $scope.currentStyle = '';
    
    $scope.createReport = createReport;
    $scope.destroyReport = destroyReport;
    $scope.updateReport = updateReport;
    $scope.updateItem = updateItem;
    $scope.updateCounter = updateCounter;
    $scope.deselect = deselect;
    $scope.deselectCounter = deselectCounter;
    $scope.edit_report = {};
    $scope.edit_reports = [];
    $scope.countersAll = [];
    $scope.countersHierarchical = [];
    $scope.types = ['table','piechart','trendchart','tree'];
    var theReport = '';
    
    $scope.childrenShown = {};
    $scope.showChild = showChild;
    
    
    // // //
    
    var refreshCounter = 0;
    
    function
    refreshCount(n,name)
    {
      refreshCounter += n;
      if(refreshCounter>0)
        Global.busyRefresh(true);
      else
        Global.busyRefresh(false);
      if(false) //make true for logging
        console.log(name+": "+refreshCounter);
    }
    
    function
    someError(err)
    {
      console.error(err);
      refreshCount(-1,"someError");
    }
    
    
    // // // // //
    // VIEW - DATE/TIME
    
    function
    toMySQLDateTime(date)
    {
      return    date.getFullYear() + '-' +
        ('00' +(date.getMonth()+1)).slice(-2) + '-' +
        ('00' + date.getDate()).slice(-2) + ' ' +
        ('00' + date.getHours()).slice(-2) + ':' +
        ('00' + date.getMinutes()).slice(-2) + ':' +
        ('00' + date.getSeconds()).slice(-2);
    }
    
    
    // // // // //
    // VIEW - TABLE
    
    var table;
    
    function
    craftTable(data)
    {
      var ref = '#tableChart';
      var topMessage =
          toMySQLDateTime($scope.date1).slice(0,16)+' to '+
          toMySQLDateTime($scope.date2).slice(0,16);
      
      var cols = [];
      
      cols.push({title: "Item",     data:"description"});
      cols.push({title: "Count",    data:"count",
                                    class:"dt-right"});
      cols.push({title: "Percent",  data:"percent",
                                    visible:false,
                                    class:"dt-right"});
      
      if(table){
        table.clear();
        table.rows.add(data);
        table.draw(false);
      } else {
        table = $(ref).DataTable({data: data,
                                  columns: cols,
                                  order: [],
                                  scrollY: '400px',
                                  scrollX: true,
                                  scrollCollapse: true,
                                  paging: false,
                                  dom: 'lftBipr',
                                  buttons: [
                                    {
                                      extend: 'copy',
                                      messageTop: topMessage
                                    },
                                    {
                                      extend: 'print',
                                      messageTop: topMessage
                                    },
                                    {
                                      extend: 'excel',
                                      messageTop: topMessage
                                    },
                                    {
                                      extend: 'pdf',
                                      messageTop: topMessage
                                    }
                                  ]});
        $timeout(table.draw,0);
      }
      refreshCount(-1,"craftTable");
    }
    
    
    // // // // //
    // VIEW - DISPLAY
    
    function
    displayReport()
    {
      if(!$scope.selectedReport) return;
      
      refreshCount(1,"displayReport");
      $scope.d1 = new Date($scope.date1);
      $scope.d2 = new Date($scope.date2);
      
      if($scope.selectedReport.type == 'table' ||
         $scope.selectedReport.type == 'piechart' ||
         $scope.selectedReport.type == 'tree'){
        
        DbFactory.post({topic: 'reports',
                        action: 'itemTotalVal',
                        params: {report: $scope.selectedReport.report,
                                 start: toMySQLDateTime($scope.d1),
                                 end: toMySQLDateTime($scope.d2)}
                       })
          .success(totalValSuccess)
          .error  (someError);
      } else if($scope.selectedReport.type == 'trendchart'){
        $scope.d1.setHours(0);
        $scope.d1.setMinutes(0);
        $scope.d1.setSeconds(0);
        $scope.d2.setHours(0);
        $scope.d2.setMinutes(0);
        $scope.d2.setSeconds(0);
        
        DbFactory.post({topic: 'reports',
                        action: 'items',
                        params: {report: $scope.selectedReport.report}
                       })
          .success(buildTrendChart)
          .error  (someError);
      } else if($scope.selectedReport.type == 'sqltable') {
       $scope.currentStyle = $scope.selectedReport.type;
        DbFactory.post({topic: 'reports',
                        action: $scope.selectedReport.report,
                        params: {
                                 start: toMySQLDateTime($scope.d1),
                                 end: toMySQLDateTime($scope.d2)}
                       })
          .success(sqltableSuccess)
          .error  (someError);
      } else {
        refreshCount(-1,"displayReport");
        console.error("Unrecognized report type: "+$scope.selectedReport.type);
      }
    }
    
    function
    totalValSuccess(val)
    {
      $scope.itemTotalVal = val[0].totalVal;
      
      DbFactory.post({topic: 'reports',
                      action: 'itemCounts',
                      params: {report: $scope.selectedReport.report,
                               start: toMySQLDateTime($scope.d1),
                               end: toMySQLDateTime($scope.d2)}
                     })
        .success(countsSuccess)
        .error  (someError);
    }
    
    function
    countsSuccess(itemCounts)
    {
      $scope.itemCounts = itemCounts;
      $scope.currentStyle = $scope.selectedReport.type;
      for(var i = 0; i < $scope.itemCounts.length; i++){
        //convert to rounded float
        $scope.itemCounts[i].percent =
          Math.round($scope.itemCounts[i].count*1000.0/$scope.itemTotalVal)/10;
      }
      
      if($scope.selectedReport.type == 'table'){
        craftTable($scope.itemCounts);
      } else if($scope.selectedReport.type == 'piechart') {
        bakePie($scope.itemCounts);
      } else if($scope.selectedReport.type == 'tree') {
        plantTree($scope.itemCounts);
      }
    }

    // // // // //
    // VIEW - SQLTABLE

    var sqldataTable = null;
    function
    sqltableSuccess(data)
    {
     var topMessage =
          toMySQLDateTime($scope.date1).slice(0,16)+' to '+
          toMySQLDateTime($scope.date2).slice(0,16);
      var cols = [];
      var ref = "#sqldataTable";

     if( !data || data.length == 0 )
        return;
     var keys = Object.keys(data[0]);
     for( var i= 0; i<keys.length; i++ ){
        var col = {};
        col['title'] = keys[i];
        col['data'] = keys[i];
        col['class'] = "dt-center";
        cols.push( col );
     }

      if(sqldataTable){
        sqldataTable.clear();
        sqldataTable.rows.add(data);
        sqldataTable.draw(false);
      } else {
        sqldataTable = $(ref).DataTable({data: data,
                                  columns: cols,
                                  order: [],
                                  scrollY: '400px',
                                  scrollX: true,
                                  scrollCollapse: true,
                                  paging: false,
                                  dom: 'lftBipr',
                                  buttons: [
                                    {
                                      extend: 'copy',
                                      messageTop: topMessage
                                    },
                                    {
                                      extend: 'print',
                                      messageTop: topMessage
                                    },
                                    {
                                      extend: 'excel',
                                      messageTop: topMessage
                                    },
                                    {
                                      extend: 'pdf',
                                      messageTop: topMessage
                                    }
                                  ]});
        $timeout(sqldataTable.draw,0);
      }
      refreshCount(-1,"sqltableSuccess");
    }
    
    // // // // //
    // VIEW - TREND
    
    var trend1 = '',
        trendArray = [],
        trendItems = [];
    
    function
    plusDay(date)
    {
      var newDate = new Date(date);
      newDate.setDate(date.getDate()+1);
      return newDate;
    }
    
    function
    isDateBefore(date1,date2)
    { return (date2 - date1 > 0); }
    
    function
    trendFormatDate(sqldate)
    { return sqldate.slice(0,10); }
    
    function
    countsCB(start,stop)
    {
      var cb = function(counts){
        trendArray[0].push(trendFormatDate(start));
        for(var l = 1; l < trendArray.length; l++){
          var posVal = false;
          for(var m = 0; m < counts.length; m++){
            if(counts[m].code == trendArray[l][0]){
              trendArray[l].push(counts[m].count);
              posVal = true;
            }
          }
          if(!posVal){
            trendArray[l].push(0);
          }
        }
      }
      return cb;
    }
    
    function
    buildTrendChart(items)
    {
      trendItems = [];
      for(var j = 0; j < items.length; j++){
        trendItems.push(items[j].code);
      }
      
      trendArray = [['x']];
      for(var k = 0; k < trendItems.length; k++){
        trendArray.push([trendItems[k]]);
      }
      
      do {
        var start_ = toMySQLDateTime($scope.d1),
            stop_  = toMySQLDateTime(plusDay($scope.d1));
        
        DbFactory.post({topic: 'reports',
                        action: 'itemCounts',
                        params: {report: $scope.selectedReport.report,
                                 start: start_,
                                 end: stop_}})
          .success(countsCB(start_,stop_))
          .error  (someError);
        $scope.d1 = plusDay($scope.d1);
      } while(isDateBefore($scope.d1,$scope.d2));
      
      $timeout(function(){
        createTrend('#trend1','Report');
        $scope.currentStyle = 'trendchart';
        refreshCount(-1,"buildTrendChart");
        $scope.$apply();
      },1500);
    }
    
    function
    createTrend(_bind,_title)
    {
      return c3.generate({
        bindto: _bind,
        data: {
          x: 'x',
          columns: trendArray
        },
        axis: {
          x: {
            type: 'timeseries',
            tick: {
              format: function(x){return x.toLocaleDateString();}
            }
          }
        }
      });
    }
    
    
    // // // // //
    // VIEW - PIE, COLORS
    
    var pie1 = '',
        pieArray = [],
        colorObj = {},
        dataColors = [ //for more fun with colors: colorbrewer2.org
      '#377eb8',
      '#4daf4a',
      '#e31a1c',
      '#999999',
      '#984ea3',
      '#e6e688',
      '#b15928',
      '#f781bf',
      '#ff7f00',
      '#a6cee3',
      '#ffff33',
      '#b2df8a',
      '#fb9a99',
      '#fdbf6f',
      '#cab2d6'
    ];
    
    function
    randColor()
    { return '#'+Math.floor(Math.random()*16777215).toString(16); }
    
    function
    bakePie(data)
    {
      pie1 = createPie('#pie1',$scope.selectedReport.title,data);
      refreshCount(-1,"bakePie");
    }
    
    function
    createPieArray(data)
    {
      pieArray = [];
      for(var j = 0; j < data.length; j++){
        pieArray.push([data[j].description,
                       data[j].count])
      }
      colorObj = {};
      for(var k = 0; k < data.length; k++){
        colorObj[data[k].description] =
          ((k<dataColors.length)?dataColors[k]:randColor());
      }
    }
    
    function
    createPie(_bind,_title,data)
    {
      createPieArray(data);
      return c3.generate({
        bindto: _bind,
        data: {
          columns: pieArray,
          type: 'donut',
          colors: colorObj
        },
        donut: {
          title: _title,
          label: {
            format: function(value,ratio,id) {
              return value;
            }
          }
        },
      });
    }
    
    
    // // // // //
    // VIEW - TREE
    
    function
    plantTree(data)
    {
      $("#grove").html('');
      $("#bush").html('');
      $("#underbrush").html('');
      var dataHierarchical = magic('',0,data,data);
      displayTree('grove',dataHierarchical);
      displayShrub('bush',dataHierarchical);
      displayTwig('underbrush',dataHierarchical);
      refreshCount(-1,"plantTree");
    }
    
    function
    displayTree(ref,data)
    {
      for(var v = 0; v < data.length; v++){
        var dv = data[v];
        var markup = '';
        var offset = '';
        for(var w = 0; w < dv.depth; w++){
          offset += '&nbsp;&nbsp;&nbsp;&nbsp;';
        }
        markup +=
          '<div style="border-top: 1px dotted">' +
            '<span>' +
              offset +
              '<span>' +
                dv.description +
                '<md-tooltip md-delay="500">' +
                dv.code +
                '</md-tooltip>' +
              '</span>' +
            '</span>' +
            '<div id="DISPLAY_' +
              simpl(dv.code) +
              '">' +
            '</div>' +
          '</div>';
        $("#"+ref).append($compile(markup)($scope));
        displayTree("DISPLAY_"+simpl(dv.code),dv.seedlings);
      }
    }
    
    function
    displayShrub(ref,data)
    {
      for(var y = 0; y < data.length; y++){
        var dy = data[y];
        var markup = '';
        markup +=
          '<div style="text-align: right; border-top: 1px dotted">' +
            '<span>' +
              dy.count.toLocaleString('en') +
            '</span>' +
            '<div id="DISPLAYSHRUB_' +
              simpl(dy.code) +
              '">' +
            '</div>' +
          '</div>';
        $("#"+ref).append($compile(markup)($scope));
        displayShrub("DISPLAYSHRUB_"+simpl(dy.code),dy.seedlings);
      }
    }
    
    function
    displayTwig(ref,data)
    {
      for(var z = 0; z < data.length; z++){
        var dz = data[z];
        var markup = '';
        var disPct = dz.pct.length>0?('&nbsp;('+dz.pct+')'):'&nbsp;';
        markup +=
          '<div style="text-align: right; border-top: 1px dotted">' +
            '<span>&nbsp;' +
              dz.pct +
            '</span>' +
            '<div id="DISPLAYTWIG_' +
              simpl(dz.code) +
              '">' +
            '</div>' +
          '</div>';
        $("#"+ref).append($compile(markup)($scope));
        displayTwig("DISPLAYTWIG_"+simpl(dz.code),dz.seedlings);
      }
    }
    
    
    // // // // //
    // VIEW - PREP
    
    function
    listReports()
    {
      refreshCount(1,"listReports");
      DbFactory.post({topic: 'reports',
                      action: 'all'
                     })
        .success(listReportsSuccess)
        .error  (someError);
    }
    
    function
    listReportsSuccess(reports)
    {
      $scope.view_reports = reports;
      for(var n = 0; n < $scope.view_reports.length; n++){
        $scope.view_reports[n].title = $scope.view_reports[n].title || "(untitled report)";
      }
      refreshCount(-1,"listReportsSuccess");
    }
    
    function
    initDates()
    {
      $scope.date1 = new Date();
      $scope.date1.setHours(0);
      $scope.date1.setMinutes(0);
      $scope.date1.setSeconds(0);
      
      $scope.date2 = new Date();
      $scope.date2.setHours(23);
      $scope.date2.setMinutes(59);
      $scope.date2.setSeconds(59);
    }
    
    
    // // // // //
    // EDIT - BLAH
    
    function
    hideLoading()
    { $scope.hideItems = true; }
    
    function
    showLoading()
    {
      if($scope.countersHierarchical){
        displayCounters('thicket',$scope.countersHierarchical);
      }
      $scope.hideItems = false;
    }
    
    function
    displayCounters(ref,data)
    {
      for(var p = 0; p < data.length; p++){
        var dp = data[p];
        var markup = '';
        var offset = '';
        if(dp.depth>0){
          offset += '&nbsp;&nbsp;&nbsp;';
          for(var r = 1; r < dp.depth; r++){
            offset += '&nbsp;&nbsp;&nbsp;';
          }
        }
        markup +=
          '<div>' +
            '<span>' +
              offset +
              '<input type="checkbox" ng-model="countersAll[' +
              dp.index +
              '].selected" ng-change="updateItem(countersAll[' +
              dp.index +
              '])">&nbsp;' +
              '<span ng-click="showChild(\'' +
              simpl(dp.code) +
              '\')">' +
                dp.description +
                '<md-tooltip md-delay="500">' +
                dp.code +
                '</md-tooltip>' +
                '<span ng-if="' +
                (dp.seedlings.length>0?'true':'false') +
                '">&nbsp;' +
                  '<span ng-show="childrenShown[\'' +
                  simpl(dp.code) +
                  '\']">&#9660;</span>' +
                  '<span ng-hide="childrenShown[\'' +
                  simpl(dp.code) +
                  '\']">&#9654;</span>' +
                '</span>' +
              '</span>' +
            '</span>' +
            '<div id="' +
              simpl(dp.code) +
              '" ng-show="childrenShown[\'' +
              simpl(dp.code) +
              '\']">' +
            '</div>' +
          '</div>';
        $("#"+ref).append($compile(markup)($scope));
        displayCounters(simpl(dp.code),dp.seedlings);
      }
    }
    
    function
    simpl(text)
    { return text.replace(/\/| /g,''); }
    
    function
    showChild(code)
    { $scope.childrenShown[code] = !$scope.childrenShown[code]; }
    
    function
    deselect()
    {
      $scope.edit_report = {title: ''};
      $scope.countersAll = [];
      theReport = '';
    }
    
    
    // // // // //
    // EDIT - REPORT FUNCTIONS
    
    function
    createReport(newName)
    {
      refreshCount(1,"createReport");
      theReport = newName;
      DbFactory.post({topic: 'reports',
                      action: 'create',
                      params: {report: newName}
                     })
        .success(reportFunctionSuccess)
        .error  (reportFunctionError);
    }
    
    function
    destroyReport()
    {
      theReport='';
      if($scope.edit_report.report){
        refreshCount(1,"destroyReport");
        DbFactory.post({topic: 'reports',
                        action: 'destroy',
                        params: {report: $scope.edit_report.report}
                       })
          .success(reportFunctionSuccess)
          .error  (reportFunctionError);
        DbFactory.post({topic: 'reports',
                        action: 'destroyItem',
                        params: {report: $scope.edit_report.report}
                       })
          .success(reportFunctionSuccess)
          .error  (reportFunctionError);
      }
    }
    
    function
    updateReport()
    {
      theReport = $scope.edit_report.report;
      
      if($scope.edit_report.report){
        refreshCount(1,"updateReport");
        DbFactory.post({topic: 'reports',
                        action: 'update',
                        params: {report:  $scope.edit_report.report,
                                 type:    $scope.edit_report.type    || '',
                                 title:   $scope.edit_report.title   || '',
                                 ordinal: $scope.edit_report.ordinal || 0,
                                 params:  $scope.edit_report.params  || ''}})
        
          .success(reportFunctionSuccess)
          .error  (reportFunctionError);
      }
    }
    
    function
    reportFunctionSuccess()
    {
      listReports();
      refreshReports();
      refreshCount(-1,"reportFunctionSuccess");
    }
    
    function
    reportFunctionError(err)
    {
      console.error(err);
      refreshReports();
      refreshCount(-1,"reportFunctionError");
    }
    
    function
    updateItem(item)
    {
       if($scope.edit_report.report){
         refreshCount(1,"updateItem");
         if(item.selected){
           DbFactory.post({topic: 'reports',
                           action: 'updateItem',
                           params: {report: $scope.edit_report.report,
                                    code:   item.code        || '',
                                    description:  item.description || ''}
                          })
             .success(updateItemSuccess)
             .error  (updateItemError);
         } else {
           DbFactory.post({topic: 'reports',
                           action: 'removeItem',
                           params: {report: $scope.edit_report.report,
                                    code:   item.code || ''}
                          })
             .success(updateItemSuccess)
             .error  (updateItemError);
         }
       }
    }
    
    function
    updateItemSuccess()
    {
      refreshCountersAll();
      refreshCount(-1,"updateItemSuccess");
    }
    
    function
    updateItemError(err)
    {
      console.error(err);
      alert("Error updating item");
      refreshReports();
      refreshCount(-1,"updateItemError");
    }
    
    
    // // // // //
    // EDIT - COUNTER FUNCTIONS
    
    function
    deselectCounter()
    {
      $scope.counter = {};
    }
    
    function
    updateCounter()
    {
      if($scope.counter.code){
        refreshCount(1,"updateCounter");
        DbFactory.post({topic: 'counters',
                        action: 'update',
                        params: {code:        $scope.counter.code,
                                 description: $scope.counter.description || ''}
                       })
          .success(counterFunctionSuccess)
          .error  (counterFunctionError);
      }
    }
    
    function
    counterFunctionSuccess()
    {
      $scope.counter = {};
      refreshCountersEditable();
      refreshCount(-1,"counterFunctionSuccess");
    }
    
    function
    counterFunctionError(err)
    {
      console.error(err);
      refreshCount(-1,"counterFunctionError");
    }
    
    
    // // // // //
    // EDIT - TABLE
    
    // // //
    // REPORTS - CHOOSE
    
    var chooseTable = null;
    
    function
    buildReportsChooseTable(data)
    {
      $scope.childrenShown = {};
      
      var cols = [];
      var ref = "#reportChoose";
      
      cols.push({title:"Report", data:"report"});
      
      if(chooseTable){
        chooseTable.clear();
        chooseTable.rows.add(data);
        chooseTable.draw(false);
      } else {
        chooseTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              scrollY: "550px",
                              scrollX: true,
                              scrollCollapse: true,
                              paging: false,
                              dom: 'ltipr'});
        $(ref+' tbody').on('click','tr',editClick);
        $timeout(chooseTable.draw,0);
      }
      refreshCount(-1,"buildReportsChooseTable");
    }
    
    function
    editClick()
    {
      var data = chooseTable.row(this).data();
      $scope.edit_report = data || {};
      refreshCountersAll();
    }
    
    // // //
    // COUNTERS - EDITABLE
    
    var countersEditableTable = null;
    
    function
    buildCountersEditableTable(data)
    {
      var cols = [];
      var ref = "#counterChoose";
      
      cols.push({title:"Code",        data:"code"});
      cols.push({title:"Description", data:"description"});
      
      if(countersEditableTable){
        countersEditableTable.clear();
        countersEditableTable.rows.add(data);
        countersEditableTable.draw(false);
      } else {
        countersEditableTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              scrollY: "550px",
                              scrollX: true,
                              scrollCollapse: true,
                              paging: false,
                              dom: 'lftipr'});
        $(ref+' tbody').on('click','tr',counterEditClick);
        $timeout(countersEditableTable.draw,0);
      }
      refreshCount(-1,"buildCountersEditableTable");
    }
    
    function
    counterEditClick()
    {
      var data = countersEditableTable.row(this).data();
      $scope.counter = data || {};
      $scope.$apply();
    }
    
    
    // // // // //
    // EDIT - DATA RETRIEVAL
    
    // // //
    // REPORTS
    
    function
    refreshReports()
    {
      refreshCount(1,"refreshReports");
      $scope.edit_reports = [];
      deselect();
      
      DbFactory.post({topic: 'reports',
                      action: 'allWithoutSqltable'})
        .success(refreshReportsSuccess)
        .error  (refreshReportsError);
    }
    
    function
    refreshReportsSuccess(reports)
    {
      $scope.edit_reports = reports;
      
      if(theReport != ''){
        for(var i=0; i<reports.length; i++){
          if(reports[i].report==theReport)
            $scope.edit_report = reports[i];
        }
        theReport = '';
      }
      
      buildReportsChooseTable($scope.edit_reports);
    }
    
    function
    refreshReportsError(err)
    {
      $scope.edit_reports = [];
      refreshCount(-1,"refreshReportsError");
    }
    
    // // //
    // COUNTERS - EDITABLE
    
    function
    refreshCountersEditable()
    {
      refreshCount(1,"refreshCountersEditable");
      DbFactory.post({topic: 'counters',
                      action: 'all'
                     })
        .success(refreshCountersEditableSuccess)
        .error  (refreshCountersEditableError);
    }
    
    function
    refreshCountersEditableSuccess(counters)
    { buildCountersEditableTable(counters); }
    
    function
    refreshCountersEditableError(err)
    {
      console.error(err);
      refreshCount(-1,"refreshCountersEditableError");
    }
    
    // // //
    // COUNTERS - ALL
    
    function
    refreshCountersAll()
    {
      refreshCount(1,"refreshCountersAll");
      hideLoading();
      
      $scope.countersAll = [];
      DbFactory.post({topic: 'counters',
                      action: 'all'
                     })
        .success(refreshCountersAllSuccess)
        .error  (countersAllError);
    }
    
    function
    refreshCountersAllSuccess(counters)
    {
      $scope.countersAll = counters;
      $scope.countersHierarchical = [];
      
      if($scope.edit_report.report){
        DbFactory.post({topic: 'reports',
                        action: 'items',
                        params: {report: $scope.edit_report.report}
                       })
          .success(specifyReportCounters)
          .error  (countersAllError);
      } else {
        doMagicOnCounters();
      }
    }
    
    function
    specifyReportCounters(selects)
    {
      for(var i=0; i<$scope.countersAll.length; i++){
        var selected = false;
        for(var s=0; s<selects.length; s++){
          if(selects[s].code==$scope.countersAll[i].code)
            selected = true;
        }
        $scope.countersAll[i].selected = selected;
        $scope.countersAll[i].index = i;
      }
      doMagicOnCounters();
    }
    
    function
    doMagicOnCounters()
    {
      $("#thicket").html('');
      $scope.countersHierarchical = magic('',0,$scope.countersAll,$scope.countersAll);
      showLoading();
      refreshCount(-1,"doMagicOnCounters");
    }
    
    function
    magic(prefix,depth,data,dataImmutable)
    {
      if(data.length==0) return [];
      
      var names = [];
      for(var i = 0; i < data.length; i++){
        names.push(nextToken(data[i].code,prefix.length,depth,dataImmutable));
      }
      names = _.uniq(names);
      for(var t = 0; t < names.length; t++){
        if(names[t]=='xXx_delete_me_xXx'){
          names.splice(t,1);
          continue;
        }
      }
      
      var saplings = [];
      for(var k = 0; k < names.length; k++){
        var longName = prefix+names[k];
        var sprouts = findChildren(longName,dataImmutable);
        var sapCount = getCount(longName,dataImmutable);
        var parCount = getCount(prefix,dataImmutable);
        var sapPct = depth==0?'':
            (parCount==0?
             '-&nbsp;%':
             (100.0*sapCount/parCount).toFixed(1)+'%');
        
        saplings.push({
          nameShort: names[k],
          code: longName,
          depth: depth,
          index: findIndex(longName,dataImmutable),
          description: getDescription(longName,dataImmutable),
          selectable: isSelectable(longName,dataImmutable),
          selected: isSelected(longName,dataImmutable),
          count: sapCount,
          pct: sapPct,
          seedlings: magic(longName,depth+1,sprouts,dataImmutable)
        })
      }
      return saplings;
    }
    
    function
    nextToken(code,prefixLength,depth,dataImmutable)
    {
      var c0 = code.slice(prefixLength);
      if(depth==0){
        for(var j = 1; j < c0.length; j++){ //starts at 1 because some codes begin with a slash
          var c1 = code.slice(0,prefixLength+j);
          if(c0.charAt(j)=='/'){
            if(isRealCounter(c1,dataImmutable)){
              return c0.slice(0,j);
            }
          }
        }
        return c0;
      } else {
        for(var j = 0; j < c0.length; j++){
          var c1 = code.slice(0,prefixLength+j);
          if(c0.charAt(j)!='/' && j==0)
            return 'xXx_delete_me_xXx'; //a real child would begin with a slash
          if(c0.charAt(j)=='/' && j>0){
            if(isRealCounter(c1,dataImmutable)){
              return c0.slice(0,j);
            }
          }
        }
        return c0;
      }
    }
    
    function
    isRealCounter(str,source)
    {
      for(var u = 0; u < source.length; u++){
        if(source[u].code==str)
          return true;
      }
      return false;
    }
    
    function
    findChildren(bigPrefix,source)
    {
      var children = [];
      for(var l = 0; l < source.length; l++){
        var cl = source[l];
        if(cl.code.startsWith(bigPrefix) && cl.code.length > bigPrefix.length)
          children.push(cl);
      }
      return children;
    }
    
    function
    getDescription(name,source)
    {
      for(var o = 0; o < source.length; o++){
        if(source[o].code==name)
          return source[o].description;
      }
      return '';
    }
    
    function
    isSelectable(name,source)
    {
      for(var m = 0; m < source.length; m++){
        if(source[m].code==name)
          return true;
      }
      return false;
    }
    
    function
    isSelected(name,source)
    {
      for(var n = 0; n < source.length; n++){
        if(source[n].code==name)
          return source[n].selected;
      }
      return null;
    }
    
    function
    findIndex(name,source)
    {
      for(var q = 0; q < source.length; q++){
        if(source[q].code==name)
          return source[q].index;
      }
      return null;
    }
    
    function
    getCount(name,source)
    {
      for(var x = 0; x < source.length; x++){
        if(source[x].code==name)
          return source[x].count;
      }
      return null;
    }
    
    function
    countersAllError(err)
    {
      console.error(err);
      showLoading();
      refreshCount(-1,"countersAllError");
    }
    
    
    // // // // //
    // REFRESHMENTS, INNIT?
    
    function
    edit_refresh()
    {
      refreshReports();
      refreshCountersEditable();
    }
    
    function
    init()
    {
      //IE portability
      if(!String.prototype.startsWith){
        String.prototype.startsWith = function(searchString, position){
          position = position || 0;
          return this.indexOf(searchString, position)===position;
        };
      }
      
      Global.setTitle('Reports');
      Global.recv('refresh',edit_refresh,$scope);
      initDates();
      listReports();
      edit_refresh();
    }
    
    init();
    
  }
  
  function
  reportUnitaryConfig($routeProvider)
  {
    $routeProvider
      .when('/report', {controller: 'ReportUnitaryController',
                        templateUrl: '/ui/reportUnitary/reportUnitary.view.html'});
  }
  
}())
