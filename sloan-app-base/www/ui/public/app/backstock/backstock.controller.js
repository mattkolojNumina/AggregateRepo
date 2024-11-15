(function() {

// -----------------------------------------------------------------


    var screenName = "backstock";
    var screenTitle = "Backstock";
    var refreshTime = 0;

    var tabs = [
        {
            title: "Backstock",
            tables: [
                {
                    id: "backstock",
                    action: "all",
                    columns: [
                        {title: "Location", data: "location", align: "center"},
                        {title: "SKU", data: "sku", align: "center"},
                        {title: "Quantity", data: "qty", align: "center"},
                        {title: "Downloaded", data: "downloadStamp", align: "center", render: "stampRenderer"}
                    ]
                }
            ]
        }
    ];

// -----------------------------------------------------------------

    angular
        .module('ui')
        .controller(screenName + 'Controller', controller);

    angular
        .module('ui')
        .config(config);

    controller.$inject = ['$scope', '$interval', '$timeout', 'Global', 'DbFactory'];

    var filterValue = "";

    function
    controller($scope, $interval, $timeout, Global, DbFactory) {
        var periodic;
        var dataTables = [];

        // tabs
        $scope.tabIdx = 0;
        $scope.subtabIdx = 0;
        $scope.refresh = refresh;
        $scope.permit = Global.permit;
        $scope.edit = {};
        $scope.editUpdate = editUpdate;
        $scope.editNew = editNew;
        $scope.editDelete = editDelete;

        function
        stampRenderer(data, type, full, meta) {
            if (type != 'display')
                return data;
            if (data == null || data == '')
                return '';

            var date = new Date(data);
            var today = new Date();
            if (today.toDateString() == date.toDateString())
                return date.toLocaleTimeString();
            return date.toLocaleString();
        }

        function
        onError(err) {
            console.error(err);
        }

        function
        refresh() {
            var tabIdx = $scope.tabIdx;
            var subtabIdx = $scope.subtabIdx;
            if (tabs[tabIdx].tables.length - 1 < subtabIdx) subtabIdx = 0;
            if (typeof tabs[tabIdx].tables[subtabIdx].filterColumn != 'undefined') {
                var paramList = {};
                paramList[tabs[tabIdx].tables[subtabIdx].filterColumn] = filterValue;
                DbFactory.post({
                    topic: screenName,
                    action: tabs[tabIdx].tables[subtabIdx].action,
                    params: paramList
                })
                    .success(onSuccess)
                    .error(onError);
            } else {
                DbFactory.post({
                    topic: screenName,
                    action: tabs[tabIdx].tables[subtabIdx].action
                })
                    .success(onSuccess)
                    .error(onError);
            }
        }


        function
        onSuccess(data) {
            $scope.working = false;
            var cols = [];
            var tabIdx = $scope.tabIdx;
            var subtabIdx = $scope.subtabIdx;
            if (tabs[tabIdx].tables.length - 1 < subtabIdx) subtabIdx = 0;
            var ref = "#" + tabs[tabIdx].tables[subtabIdx].id;

            for (const col of tabs[tabIdx].tables[subtabIdx].columns) {
                if (typeof col.align === 'undefined') col.align = 'center';
                if (col.render) {
                    if (col.render == 'stampRenderer')
                        cols.push({title: col.title, data: col.data, class: 'dt-' + col.align, render: stampRenderer});
                    else
                        cols.push({title: col.title, data: col.data, class: 'dt-' + col.align});
                } else
                    cols.push({title: col.title, data: col.data, class: 'dt-' + col.align});
            }

            if (typeof dataTables[tabIdx] != 'undefined' &&
                typeof dataTables[tabIdx][subtabIdx] != 'undefined') {
                dataTables[tabIdx][subtabIdx].clear();
                dataTables[tabIdx][subtabIdx].rows.add(data);
                dataTables[tabIdx][subtabIdx].draw();
            } else {
                if (typeof dataTables[tabIdx] == 'undefined')
                    dataTables[tabIdx] = [];
                dataTables[tabIdx][subtabIdx]
                    = $(ref)
                    .DataTable({
                        data: data,
                        columns: cols,
                        order: [],
                        scrollY: "550px",
                        scrollX: true,
                        scrollCollapse: true,
                        paging: true,
                        pageLength: 30,
                        dom: 'lftBipr',
                        buttons: ['copy',
                            'print',
                            {extend: 'excel', exportOptions: {orthogonal: 'exportExcel'}},
                            'pdf']
                    });
                $timeout(function () {
                    dataTables[tabIdx][subtabIdx].draw()
                }, 0);
                if (tabs[tabIdx].tables[subtabIdx].edit)
                    $(ref + ' tbody').on('click', 'tr', editClick);
                if (tabs[tabIdx].tables[subtabIdx].linkTab)
                    $(ref + ' tbody').on('click', 'td', onClick);
            }
        }

        function
        onClick() {
            var tabIdx = $scope.tabIdx;
            var subtabIdx = $scope.subtabIdx;
            if (tabs[tabIdx].tables.length - 1 < subtabIdx) subtabIdx = 0;
            if (tabs[tabIdx].tables[subtabIdx].linkTab) {
                var row = dataTables[tabIdx][subtabIdx].row(this).data();
                var column = tabs[tabIdx].tables[subtabIdx].linkColumn;
                // $scope[tabs[tabs[tabIdx][subtabIdx].linkTab].filterColumn] = row[column];
                filterValue = row[column];
                $scope.tabIdx = tabs[tabIdx].tables[subtabIdx].linkTab;
                $scope.$apply();
                if (typeof tabs[tabs[tabIdx].tables[subtabIdx].linkTab].details != 'undefined')
                    for (const d of tabs[tabs[tabIdx].tables[subtabIdx].linkTab].details) {
                        $scope[d.data] = row[d.data];
                    }
            }
        }

        function
        editClick() {
            var tabIdx = $scope.tabIdx;
            var row = dataTables[tabIdx][subtabIdx].row(this).data();
            for (const col of tabs[tabIdx][subtabIdx].edit.columns) {
                var id = tabs[tabIdx][subtabIdx].id;
                $scope.edit[id][col.data] = row[col.data];
            }
            $scope.$apply();
        }

        function
        editNew() {
            var tabIdx = $scope.tabIdx;
            var subtabIdx = $scope.subtabIdx;
            var id = tabs[tabIdx][subtabIdx].id;
            $scope.edit[id] = {};
        }

        function
        editUpdate() {
            Global.busyRefresh(true);
            var paramList = {};
            for (const col of tabs[$scope.tabIdx][$scope.subtabIdx].columns) {
                paramList[col.data] = $scope.edit[tabs[$scope.tabIdx][$scope.subtabIdx].id][col.data];
            }
            DbFactory.post({
                topic: screenName,
                action: tabs[$scope.tabIdx][$scope.subtabIdx].edit.updateAction,
                params: paramList
            })
                .success(editUpdateSuccess)
                .error(editUpdateError)
        }

        function
        editUpdateSuccess() {
            $timeout(refresh, 1000);
            Global.busyRefresh(false);
        }

        function
        editUpdateError(err) {
            Global.busyRefresh(false);
            console.error(err);
        }

        function
        editDelete() {
            Global.busyRefresh(true);
            var paramList = {};
            for (const col of tabs[$scope.tabIdx][$scope.subtabIdx].columns) {
                paramList[col.data] = $scope.edit[tabs[$scope.tabIdx][$scope.subtabIdx].id][col.data];
            }
            DbFactory.post({
                topic: screenName,
                action: tabs[$scope.tabIdx][$scope.subtabIdx].edit.deleteAction,
                params: paramList
            })
                .success(editDeleteSuccess)
                .error(editDeleteError)
            editNew();
        }

        function
        editDeleteSuccess() {
            $timeout(refresh, 1000);
            Global.busyRefresh(false);
        }

        function
        editDeleteError(err) {
            Global.busyRefresh(false);
            console.error(err);
        }

        function
        init() {
            Global.setTitle(screenTitle);
            Global.recv('refresh', refresh, $scope);
            $scope.working = true;
            if (refreshTime > 0)
                periodic = $interval(refresh, refreshTime);
        }

        refresh();

        $scope.$on('$destroy', function () {
            $interval.cancel(periodic);
        });
        for (const tab of tabs)
            for (const table of tab.tables) {
                $scope.edit[table.id] = {};
                if (table.edit) {
                    for (const col of table.edit.columns) {
                        if (col.type === "list") {
                            if (col.listData) {
                                var itemList = [];
                                col.listData.forEach(function (item) {
                                    itemList.push('' + item);
                                });
                                $scope[col.data + 'List'] = itemList;
                            } else {
                                DbFactory.post({
                                    topic: screenName,
                                    action: col.listAction
                                })
                                    .success(function (data) {
                                        var itemList = [];
                                        data.forEach(function (row) {
                                            itemList.push('' + row[col.data]);
                                        });
                                        $scope[col.data + 'List'] = itemList;
                                    })
                                    .error(function (err) {
                                        console.error(err);
                                    })
                            }
                        }
                    }
                }
            }
        init();
    }

    function
    config($routeProvider) {

        var html = "";
        html += "<div class=\"container-fluid\" ng-cloak>\n";
        html += "<md-content ng-show='working'>\n" +
            "        <div layout=\"row\">\n" +
            "            <span flex></span>\n" +
            "            <div layout=\"column\" layout-align=\"center center\">\n" +
            "                <h2>WORKING</h2>\n" +
            "                <md-progress-circular md-mode=\"indeterminate\"></md-progress-circular>\n" +
            "                <h3></h3>\n" +
            "            </div>\n" +
            "            <span flex></span>\n" +
            "        </div>\n" +
            "</md-content>\n"
        html += "  <md-content ng-hide=\"working\">\n";

        var tabIdx = 0;
        if (tabs.length > 1)
            html += "    <md-tabs md-dynamic-height md-selected=\"tabIdx\">\n";
        for (const tab of tabs) {

            if (tabs.length > 1)
                html += "      <md-tab label=\"" + tabs[tabIdx].title + "\" md-on-select=\"refresh()\">\n";
            html += "        <md-content class=\"md-padding\">\n";

            if (typeof tab.details != 'undefined') {
                html += "        <table>\n";
                for (const d of tab.details) {
                    html += "          <tr>\n";
                    html += "          <td class='detail-field'>" + d.title + "</td>\n";
                    html += "          <td class='detail-data'>{{" + d.data + "}}</td>\n";
                    html += "          </tr>\n";
                }
                html += "        </table>\n";
            }

            var subtabIdx = 0;
            if (tab.tables.length > 1) {
                html += "        <md-tabs md-dynamic-height md-selected=\"subtabIdx\">\n";
            }
            for (const table of tab.tables) {
                if (tab.tables.length > 1) {
                    html += "          <md-tab label=\"" + table.title + "\" md-on-select=\"refresh()\">\n";
                    html += "            <md-content class=\"md-padding\">\n";
                }
                if (typeof table.edit != 'undefined') {
                    html += "            <div layout=\"row\">";
                    html += "            <div flex=65>";
                }
                html += "            <md-content class=\"md-padding\">\n";
                html += "            <table id=\"" + table.id + "\" class=\"display compact\" width=\"100%\"></table>\n";
                html += "            </md-content>\n";
                if (typeof table.edit != 'undefined') {
                    html += "          </div>";
                    html += "          <div flex=35>";
                    html += "              <md-toolbar>";
                    html += "                <div class=\"md-toolbar-tools\">";
                    html += "                  <span flex></span>";
                    if (table.edit.newDelete === true) {
                        html += "                  <md-button aria-label=\"New\"";
                        html += "                             ng-click=\"editNew()\">New</md-button>";
                    }
                    html += "                  <md-button aria-label=\"Update\"";
                    html += "                             ng-readonly=\"editDisable()\"";
                    html += "                             ng-click=\"editUpdate()\">Save</md-button>";
                    if (table.edit.newDelete === true) {
                        html += "                  <md-button aria-label=\"Delete\"";
                        html += "                             ng-readonly=\"editDisable()\"";
                        html += "                             ng-click=\"editDelete()\">Delete</md-button>";
                    }
                    html += "                </div>";
                    html += "              </md-toolbar>";
                    html += "              <md-content class=\"md-padding\">";

                    for (const c of table.edit.columns) {
                        html += "                <div layout=\"column\">";
                        html += "                  <div>";
                        html += "                    <md-input-container md-no-float class=\"md-block\" >";
                        html += "                      <label>" + c.title + "</label>";
                        if (c.type && c.type == "list") {
                            html += "                      <md-select ng-model=\"edit." + table.id + "." + c.data + "\">";
                            html += "                        <md-option ng-repeat=\"" + c.data + " in " + c.data + "List\"";
                            html += "                                   ng-value=\"" + c.data + "\">";
                            html += "                          {{" + c.data + "}}";
                            html += "                      </md-select>";
                        } else {
                            html += "                      <input ng-model=\"edit." + table.id + "." + c.data + "\">";
                        }
                        html += "                    </md-input-container>";
                        html += "                  </div>";
                        /*
                                html += "                  <div>";
                                html += "                    <md-input-container md-no-float class=\"md-block\">";
                                html += "                      <label>Notify</label>";
                                html += "                      <md-select ng-model=\"edit.notify\">";
                                html += "                        <md-option ng-repeat=\"notify in notifies\"";
                                html += "                                   ng-value=\"notify\">";
                                html += "                          {{notify}}";
                                html += "                        </md-option>";
                                html += "                      </md-select>";
                                html += "                    </md-input-container>";
                                html += "                  </div>";
                        */
                        html += "                </div>";
                    } // for c of columns
                    html += "              </md-content>";

                    html += "          </div>";
                    html += "          </div>";
                } // if edit != undefined
                html += "            </md-content>\n";
                if (tab.tables.length > 1)
                    html += "          </md-tab>\n";
                subtabIdx++;
            } // for table of tables
            if (tab.tables.length > 1)
                html += "        </md-tabs>\n";
            if (tabs.length > 1)
                html += "      </md-tab>\n";
            tabIdx++;
        } // for tab of tabs
        if (tabs.length > 1)
            html += "    </md-tabs>\n";
        html += "  </md-content>\n";
        html += "</div>";

        // console.log("html -------------------\n" + html + "\n-------------------\n\n");

        $routeProvider
            .when('/' + screenName,
                {
                    controller: screenName + 'Controller',
                    // templateUrl: '/app/'+screenName+'/'+screenName+'.view.html'
                    template: html
                });
    }

}())
