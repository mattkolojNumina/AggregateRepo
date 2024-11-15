(function()
{
  angular
    .module('ui')
      .controller('UserController',userController);
  
  angular
    .module('ui')
      .config(userConfig);
  
  userController.$inject = ['$scope','$interval',
                            'Global','DbFactory'];
  
  function
  userController($scope,$interval,
                 Global,DbFactory)
  {
    var periodic;
    
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
    
    // tabs
    $scope.selected = 0;
    $scope.refresh = refresh;
    $scope.permit = Global.permit;
    
    function
    refresh()
    {
      if($scope.selected==0)
        userRefresh();
      
      if($scope.selected==1)
        roleRefresh();
      
      if($scope.selected==2)
        permRefresh();
	
	  if($scope.selected==3)
        adminLogRefresh();
    }
    
    
    // // // // //
    // USERS
    
    var userTable = null;
    
    $scope.user = {};
    $scope.userRoles = [];
    $scope.userNew = userNew;
    $scope.userUpdate = userUpdate;
    $scope.userDelete = userDelete;
    $scope.userLock = false;
    
    function
    userRefresh()
    {
      refreshCount(1,"userRefresh");
      DbFactory.post({topic: 'user',
                      action: 'all'
                     })
        .success(userSuccess)
        .error  (userError);
    }
    
    function
    userSuccess(data)
    {
      var cols = [];
      var ref = "#user";
      
      cols.push({title: "User", data:"user"});
      cols.push({title: "Name", data:"name"});
      
      if(userTable){
        userTable.clear();
        userTable.rows.add(data);
        userTable.draw(false);
      } else {
        userTable = $(ref)
                  .DataTable({data: data,
                              columns: cols,
                              order: [[0,'desc']],
                              scrollY: "425px",
                              scrollCollapse: true,
                              paging: false,
                              dom: 'lftBipr',
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','tr',userClick);
      }
      userNew();
      refreshCount(-1,"userSuccess");
    }
    
    function
    userError(err)
    {
      console.error(err);
      refreshCount(-1,"userError");
    }
    
    function
    userNew()
    {
      refreshCount(1,"userNew");
      
      $scope.user.user = '';
      $scope.user.name = '';
      $scope.user.password = '';
      $scope.userLock = false;
      
      DbFactory.post({topic: 'role',
                      action: 'all'
                     })
        .success(userRoleSuccess)
        .error  (userRoleError);
    }
    
    function
    userRoleSuccess(data) 
    {
      $scope.userRoles = data;
      for(var i=0; i<$scope.userRoles.length; i++)
        $scope.userRoles[i].on = ($scope.userRoles[i].assigned=='true');
      refreshCount(-1,"userRoleSuccess");
    }
    
    function
    userRoleError(err)
    {
      console.error(err);
      refreshCount(-1,"userRoleError");
    }
    
    function
    userUpdate()
    {
      refreshCount(1,"userUpdate");
      DbFactory.post({topic: 'user',
                      action: 'update',
                      params: {user: $scope.user.user,
                               name: $scope.user.name},
                      log: 'Update user '+$scope.user.user+' name '+$scope.user.name
                     })
        .success(userUpdateSuccess)
        .error  (userUpdateError);
    }
    
    function
    userUpdateSuccess()
    {
      if($scope.user.password){
        refreshCount(1,"userUpdateSuccess_password");
        DbFactory.post({topic: 'user',
                        action: 'updatePass',
                        params: {user: $scope.user.user,
                                 password: $scope.user.password},
                        log: 'Update user '+$scope.user.user+' password (redacted)'
                       })
          .success(userUpdateSuccessSuccess)
          .error  (userUpdateSuccessError);
      }
      
      for(var i=0; i<$scope.userRoles.length; i++){
        if($scope.userRoles[i].on && $scope.userRoles[i].assigned!='true'){
          refreshCount(1,"userUpdateSuccess_update");
          DbFactory.post({topic: 'userRole',
                          action: 'update',
                          params: {user: $scope.user.user,
                                   role: $scope.userRoles[i].role},
                          log: 'Add to user '+$scope.user.user+' role '+$scope.userRoles[i].role
                         })
            .success(userUpdateSuccessSuccess)
            .error  (userUpdateSuccessError);
        } else if(!$scope.userRoles[i].on && $scope.userRoles[i].assigned=='true'){
          refreshCount(1,"userUpdateSuccess_delete");
          DbFactory.post({topic: 'userRole',
                          action: 'deleteRole',
                          params: {user: $scope.user.user,
                                   role: $scope.userRoles[i].role},
                          log: 'Remove from user '+$scope.user.user+' role '+$scope.userRoles[i].role
                         })
            .success(userUpdateSuccessSuccess)
            .error  (userUpdateSuccessError);
        }
      }
      refreshCount(-1,"userUpdateSuccess");
    }
    
    function
    userUpdateError(err)
    {
      console.error(err);
      refreshCount(-1,"userUpdateError");
      refresh();
    }
    
    function
    userUpdateSuccessSuccess()
    {
      refreshCount(-1,"userUpdateSuccessSuccess");
      if(refreshCounter==0)
        refresh();
    }
    
    function
    userUpdateSuccessError(err)
    {
      console.error(err);
      refreshCount(-1,"userUpdateSuccessError");
      if(refreshCounter==0)
        refresh();
    }
    
    function
    userDelete()
    {
      refreshCount(2,"userDelete");
      DbFactory.post({topic: 'userRole',
                      action: 'delete',
                      params: {user: $scope.user.user}
                     })
        .success(userDeleteSuccess)
        .error  (userDeleteError);
      DbFactory.post({topic: 'user',
                      action: 'delete',
                      params: {user: $scope.user.user},
                      log: 'Delete user '+$scope.user.user
                     }) 
        .success(userDeleteSuccess)
        .error  (userDeleteError);
      
      userNew();
    }
    
    function
    userDeleteSuccess()
    {
      refreshCount(-1,"userDeleteSuccess");
      if(refreshCounter==0)
        refresh();
    }
    
    function
    userDeleteError(err)
    {
      console.error(err);
      refreshCount(-1,"userDeleteError");
      if(refreshCounter==0)
        refresh();
    }
    
    function
    userClick()
    {
      refreshCount(1,"userClick");
      
      var data = userTable.row(this).data();
      $scope.user.user = data.user;
      $scope.user.name = data.name;
      $scope.user.password = '';
      $scope.userLock = true;
      $scope.$apply();
      
      DbFactory.post({topic: 'role',
                      action: 'forUser',
                      params: {user: $scope.user.user}
                     }) 
        .success(userRoleSuccess)
        .error  (userRoleError); 
    }
    
    
    // // // // //
    // ROLES
    
    var roleTable = null;
    
    $scope.role = {};
    $scope.rolePerms = [];
    $scope.roleNew = roleNew;
    $scope.roleUpdate = roleUpdate;
    $scope.roleDelete = roleDelete;
    $scope.roleLock = false;
    
    function
    roleRefresh()
    {
      refreshCount(1,"roleRefresh");
      DbFactory.post({topic: 'role',
                      action: 'all'
                     })
        .success(roleSuccess)
        .error  (roleError);
    }
    
    function
    roleSuccess(data)
    {
      var cols = [];
      var ref = "#role";
      
      cols.push({title: "Role",        data:"role"});
      cols.push({title: "Description", data:"description"});
      
      if(roleTable){
        roleTable.clear();
        roleTable.rows.add(data);
        roleTable.draw(false);
      } else {
        roleTable = $(ref)
                  .DataTable({data: data, 
                              columns: cols,
                              order: [[0,'desc']],
                              scrollY: "425px",
                              scrollCollapse: true,
                              paging: false,
                              dom: 'lftBipr',
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','tr',roleClick);
      }
      roleNew();
      refreshCount(-1,"roleSuccess");
    }
    
    function
    roleError(err)
    {
      console.error(err);
      refreshCount(-1,"roleError");
    }
    
    function
    roleNew()
    {
      refreshCount(1,"roleNew");
      $scope.role.role = '';
      $scope.role.description = '';
      $scope.roleLock = false;
      
      DbFactory.post({topic: 'perm',
                      action: 'all'
                     })
        .success(rolePermSuccess)
        .error  (rolePermError); 
    }
    
    function
    rolePermSuccess(data) 
    {
      $scope.rolePerms = data;
      for(var i=0; i<$scope.rolePerms.length; i++)
        $scope.rolePerms[i].on = ($scope.rolePerms[i].assigned=='true');
      refreshCount(-1,"rolePermSuccess");
    }
    
    function
    rolePermError(err)
    {
      console.error(err);
      refreshCount(-1,"rolePermError");
    }
    
    function
    roleUpdate()
    {
      refreshCount(1,"roleUpdate");
      DbFactory.post({topic: 'role',
                      action: 'update',
                      params: {role: $scope.role.role,
                               description: $scope.role.description},
                      log: 'Update role '+$scope.role.role+' with description '+$scope.role.description
                     })
        .success(roleUpdateSuccess)
        .error  (roleUpdateError);
    }
    
    function
    roleUpdateSuccess()
    {
      for(var i=0; i<$scope.rolePerms.length; i++){
        if($scope.rolePerms[i].on && $scope.rolePerms[i].assigned!='true'){
          refreshCount(1,"roleUpdateSuccess_update");
          DbFactory.post({topic: 'rolePerm',
                          action: 'update',
                          params: {role: $scope.role.role,
                                   perm: $scope.rolePerms[i].perm},
                          log: 'Add to role '+$scope.role.role+' permission '+$scope.rolePerms[i].perm
                         })
            .success(roleUpdateSuccessSuccess)
            .error  (roleUpdateSuccessError);
        } else if(!$scope.rolePerms[i].on && $scope.rolePerms[i].assigned=='true'){
          refreshCount(1,"roleUpdateSuccess_delete");
          DbFactory.post({topic: 'rolePerm',
                          action: 'deletePerm',
                          params: {role: $scope.role.role,
                                   perm: $scope.rolePerms[i].perm},
                          log: 'Remove from role '+$scope.role.role+' permission '+$scope.rolePerms[i].perm
                         })
            .success(roleUpdateSuccessSuccess)
            .error  (roleUpdateSuccessError);
        }
      }
      refreshCount(-1,"roleUpdateSuccess");
    }
    
    function
    roleUpdateError(err)
    {
      console.error(err);
      refreshCount(-1,"roleUpdateError");
    }
    
    function
    roleUpdateSuccessSuccess()
    {
      refreshCount(-1,"roleUpdateSuccessSuccess");
      if(refreshCounter==0)
        refresh();
    }
    
    function
    roleUpdateSuccessError(err)
    {
      console.error(err);
      refreshCount(-1,"roleUpdateSuccessError");
      if(refreshCounter==0)
        refresh();
    }
    
    function
    roleDelete()
    {
      refreshCount(2,"roleDelete");
      DbFactory.post({topic: 'rolePerm',
                      action: 'delete',
                      params: {role: $scope.role.role}
                     })
        .success(roleDeleteSuccess)
        .error  (roleDeleteError);
      DbFactory.post({topic: 'role',
                      action: 'delete',
                      params: {role: $scope.role.role},
                      log: 'Delete role '+$scope.role.role
                     })
        .success(roleDeleteSuccess)
        .error  (roleDeleteError);
      
      roleNew();
    }
    
    function
    roleDeleteSuccess()
    {
      refreshCount(-1,"roleDeleteSuccess");
      if(refreshCounter==0)
        refresh();
    }
    
    function
    roleDeleteError(err)
    {
      console.error(err);
      refreshCount(-1,"roleDeleteError");
      if(refreshCounter==0)
        refresh();
    }
    
    function
    roleClick()
    {
      refreshCount(1,"roleClick");
      
      var data = roleTable.row(this).data();
      $scope.role.role = data.role;
      $scope.role.description = data.description;
      $scope.roleLock = true;
      $scope.$apply();
      
      DbFactory.post({topic: 'perm',
                      action: 'forRole',
                      params: {role: $scope.role.role}
                     })
        .success(rolePermSuccess)
        .error  (rolePermError); 
    }
    
    
    // // // // //
    // PERMISSIONS
    
    var permTable = null;
    
    $scope.perm = {};
    $scope.permNew = permNew;
    $scope.permUpdate = permUpdate;
    $scope.permDelete = permDelete;
    $scope.permLock = false;
    
    $scope.answers = [
      { id: 1, answer: 'yes' },
      { id: 0, answer: 'no'  } ];
    
    function
    permRefresh()
    {
      refreshCount(1,"permRefresh");
      DbFactory.post({topic: 'perm',
                      action: 'all'
                     })
        .success(permSuccess)
        .error  (permError);
    }
    
    function
    permSuccess(data)
    {
      var cols = [];
      var ref = "#perm";
      
      cols.push({title: "Permission",   data:"perm"});
      cols.push({title: "Description",  data:"description"});
      cols.push({title: "Enforced",     data:"enforced"});
      
      if(permTable){
        permTable.clear();
        permTable.rows.add(data);
        permTable.draw(false);
      } else {
        permTable = $(ref)
                  .DataTable({data: data, 
                              columns: cols,
                              order: [],
                              scrollY: "425px",
                              scrollCollapse: true,
                              paging: false,
                              dom: 'lftBipr',
                              buttons: ['copy','print','excel','pdf']});
        $(ref+' tbody').on('click','tr',permClick);
      }
      permNew();
      refreshCount(-1,"permSuccess");
    }
    
    function
    permError(err)
    {
      console.error(err);
      refreshCount(-1,"permError");
    }
    
    function
    permNew()
    {
      $scope.perm.perm = '';
      $scope.perm.description = '';
      $scope.perm.enforced = '';
      $scope.permLock = false;
    }
    
    function
    permUpdate()
    {
      refreshCount(1,"permUpdate");
      DbFactory.post({topic: 'perm',
                      action: 'update',
                      params: {perm: $scope.perm.perm,
                               description: $scope.perm.description,
                               enforced: ($scope.perm.enforced || '')},
                      log: 'Update permission '+$scope.perm.perm+' with description '+
                            $scope.perm.description+' and enforced '+$scope.perm.enforced
                     })
        .success(permUpdateSuccess)
        .error  (permUpdateError);
    }
    
    function
    permUpdateSuccess()
    {
      refreshCount(-1,"permUpdateSuccess");
      if(refreshCounter==0)
        refresh();
    }
    
    function
    permUpdateError(err)
    {
      console.error(err);
      refreshCount(-1,"permUpdateError");
      if(refreshCounter==0)
        refresh();
    }
    
    function
    permDelete()
    {
      refreshCount(1,"permDelete");
      DbFactory.post({topic: 'perm',
                      action: 'delete',
                      params: {perm: $scope.perm.perm},
                      log: 'Delete permission '+$scope.perm.perm
                     })
        .success(permDeleteSuccess)
        .error  (permDeleteError);
      
      permNew();
    }
    
    function
    permDeleteSuccess()
    {
      refreshCount(-1,"permDeleteSuccess");
      if(refreshCounter==0)
        refresh();
    }
    
    function
    permDeleteError(err)
    {
      console.error(err);
      refreshCount(-1,"permDeleteError");
      if(refreshCounter==0)
        refresh();
    }
    
    function
    permClick()
    {
      var data = permTable.row(this).data();
      $scope.perm.perm = data.perm;
      $scope.perm.description = data.description;
      $scope.perm.enforced = data.enforced;
      $scope.permLock = true;
      $scope.$apply();
    }

    // // // // //
    // ADMIN LOG
    
    var adminLogTable = null;
    
    function
    adminLogRefresh()
    {
      refreshCount(1,"adminLogRefresh");
      DbFactory.post({topic: 'adminLog',
                      action: 'all'
                     })
        .success(adminLogSuccess)
        .error  (adminLogError);
    }
    
    function
    adminLogSuccess(data)
    {
      var cols = [];
      var ref = "#adminLogTable";
      
      cols.push({title: "User",   		data:"user",class:"dt-center"});
      cols.push({title: "Description",  data:"description",class:"dt-center"});
	  cols.push({title: "Date/Time",    data:"stamp",   type:"date",	render: dateRender,class:"dt-center"});
      
      if(adminLogTable){
        adminLogTable.clear();
        adminLogTable.rows.add(data);
        adminLogTable.draw(false);
      } else {
        adminLogTable = $(ref)
                  .DataTable({data: data, 
                              columns: cols,
                              order: [[0,'asc']],
                              scrollY: "600px",
                              scrollCollapse: true,
                              paging: true,
                              dom: 'lftipr'});
      }
      refreshCount(-1,"adminLogSuccess");
    }
    
    function
    adminLogError(err)
    {
      console.error(err);
      refreshCount(-1,"adminLogError");
    }

	
	function dateRender(data,type,full,meta) {
		if(type!='display')
			return data;

		/* The code below displays Epoch (1970, January) 
		var date = new Date(data);
		return date.toLocaleString();
		*/
		
		var date = new Date(data);			
		return (date.getTime() > 0)? date.toLocaleString() : date = ''; /* If DateTime is Epoch (1970, January), don't display */
	}    
    
    // // // // //
    
    function
    init()
    {
      Global.setTitle('Users');
      Global.recv('refresh',refresh,$scope);
    }
    
    init();
  }
  
  function
  userConfig($routeProvider)
  {
    $routeProvider
      .when('/user',{controller: 'UserController',
                     templateUrl: '/ui/user/user.view.html'});
  }
  
}())
