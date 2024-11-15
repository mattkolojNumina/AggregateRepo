/*Table structure for table `_adminLog` */

CREATE TABLE IF NOT EXISTS `_adminLog` (
  `user` char(32) NOT NULL DEFAULT '',
  `description` varchar(255) NOT NULL DEFAULT '',
  `stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `stamp` (`stamp`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

/*Data for the table `_adminLog` */

insert  into `_adminLog`(`user`,`description`,`stamp`) values ('rds','Add to role admin permission deviceView','2019-08-13 23:18:08'),('rds','Update role admin with description Administrator','2019-08-13 23:18:08');

/*Table structure for table `_db` */

CREATE TABLE IF NOT EXISTS `_db` (
  `topic` char(32) NOT NULL DEFAULT '',
  `action` char(32) NOT NULL DEFAULT '',
  `server` char(32) NOT NULL DEFAULT '',
  `query` text NOT NULL,
  `perm` char(64) NOT NULL DEFAULT '',
  `project` char(32) NOT NULL DEFAULT '',
  `stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`topic`,`action`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

/*Data for the table `_db` */

insert  into `_db`(`topic`,`action`,`server`,`query`,`perm`,`project`,`stamp`) values ('event','all','reader','SELECT * FROM events ;','eventView','ui','2019-05-17 07:39:18'),('event','current','reader','SELECT *, TIMESTAMPDIFF(SECOND,start,NOW()) AS duration \r\nFROM events \r\nWHERE state=\'on\' ;','eventView','ui','2019-08-13 17:28:45'),('perm','update','writer','INSERT INTO _perm (perm,description,enforced) \r\nVALUES ({{perm}},{{description}},IF({{enforced}}=\'\',enforced,{{enforced}})) \r\nON DUPLICATE KEY UPDATE \r\ndescription=IF({{perm}}=\'root\' OR {{perm}}=\'always\' OR {{perm}}=\'never\',description,{{description}}),\r\nenforced=IF({{perm}}=\'root\' OR {{perm}}=\'always\' OR {{perm}}=\'never\' OR {{enforced}}=\'\',enforced,{{enforced}}) ;','userEdit','ui','2019-08-13 18:53:00'),('event','recent','reader','SELECT l.code, e.description, e.severity, l.state, l.start, e.suggestedAction,\r\n  IF(e.state=\'on\',TIMESTAMPDIFF(SECOND,l.start,NOW()),l.duration) AS duration\r\nFROM eventLog as l\r\nLEFT OUTER JOIN `events` as e\r\nON e.code=l.code \r\nORDER BY l.start DESC\r\nLIMIT {{limit}} ;\r\n\r\n\r\n','eventView','ui','2019-08-13 17:30:00'),('event','report','reader','SELECT l.code,\r\n       e.description,\r\n       e.severity,\r\n       COUNT(l.code) AS totalQuantity,\r\n       SUM(l.duration) AS totalDuration\r\nFROM eventLog AS l\r\nLEFT OUTER JOIN events AS e\r\nON e.code=l.code\r\nWHERE l.start >= {{start}}\r\nAND l.start < {{end}}\r\nGROUP BY l.code\r\nORDER BY totalQuantity DESC ;\r\n\r\n','eventView','ui','2019-08-13 17:30:36'),('event','update','writer','INSERT INTO events (code,description,severity,notify,suggestedAction)\r\nVALUES ({{code}},{{description}},{{severity}},{{notify}},{{suggestedAction}})\r\nON DUPLICATE KEY UPDATE\r\ndescription={{description}},\r\nseverity={{severity}},\r\nnotify={{notify}},\r\nsuggestedAction={{suggestedAction}} ;','eventEdit','ui','2017-05-23 09:12:37'),('event','delete','writer','DELETE FROM events \r\nWHERE code={{code}} ;','eventEdit','ui','2017-05-17 07:39:41'),('user','all','reader','SELECT * FROM _user \r\nWHERE user!=\'rds\' \r\nORDER BY user ;','userView','ui','2019-08-13 18:59:01'),('role','all','reader','SELECT * FROM _role WHERE role!=\'rds\' \r\nORDER BY role ;','userView','ui','2019-08-13 18:53:27'),('perm','all','reader','SELECT * FROM _perm \r\nWHERE perm!=\'root\' AND perm!=\'always\' AND perm!=\'never\' \r\nORDER BY perm ;','userView','ui','2019-08-13 18:01:20'),('role','forUser','reader','SELECT r.role, r.description, IF(ur.user IS NULL,\'false\',\'true\') AS assigned \r\nFROM _role AS r \r\nLEFT OUTER JOIN (SELECT * FROM _userRole WHERE user={{user}} AND user!=\'rds\') AS ur \r\nON r.role=ur.role \r\nWHERE r.role!=\'rds\' \r\nORDER BY r.role ;\r\n','userView','ui','2019-08-13 18:54:51'),('perm','forRole','reader','SELECT p.perm, p.description, IF(rp.role IS NULL,\'false\',\'true\') AS assigned \r\nFROM _perm AS p \r\nLEFT OUTER JOIN (SELECT * FROM _rolePerm WHERE role={{role}} AND role!=\'rds\') AS rp \r\nON p.perm=rp.perm \r\nWHERE p.perm!=\'root\' AND p.perm!=\'always\' AND p.perm!=\'never\' \r\nORDER BY p.perm ;','userView','ui','2019-08-13 18:52:50'),('user','update','writer','INSERT INTO _user (user,name) \r\nVALUES ({{user}},{{name}}) \r\nON DUPLICATE KEY UPDATE \r\nname={{name}} ;','userEdit','ui','2019-08-13 18:59:27'),('user','updatePass','writer','UPDATE _user \r\nSET password=PASSWORD({{password}}) \r\nWHERE user={{user}} ;','userEdit','ui','2017-05-17 07:39:55'),('userRole','deleteRole','writer','DELETE FROM _userRole \r\nWHERE user={{user}} \r\nAND role={{role}} \r\nAND user!=\'rds\' ;','userEdit','ui','2019-08-13 18:59:57'),('userRole','update','writer','REPLACE INTO _userRole (user,role) \r\nVALUES ({{user}},{{role}}) ;','userEdit','ui','2017-05-17 07:39:57'),('userRole','delete','writer','DELETE FROM _userRole \r\nWHERE user={{user}} \r\nAND user!=\'rds\' ;','userEdit','ui','2019-08-13 18:59:47'),('user','delete','writer','DELETE FROM _user \r\nWHERE user={{user}} \r\nAND user!=\'rds\' ;','userEdit','ui','2019-08-13 18:59:11'),('role','update','writer','INSERT INTO _role (role,description)\r\nVALUES ({{role}},{{description}})\r\nON DUPLICATE KEY UPDATE \r\ndescription={{description}} ;\r\n\r\n','userEdit','ui','2019-08-13 18:55:25'),('role','delete','writer','DELETE FROM _role \r\nWHERE role={{role}} AND role!=\'rds\' AND role!=\'admin\' ;','userEdit','ui','2019-08-13 18:54:23'),('rolePerm','deletePerm','writer','DELETE FROM _rolePerm \r\nWHERE role={{role}} \r\nAND perm={{perm}} \r\nAND role!=\'rds\' ;','userEdit','ui','2019-08-13 18:55:57'),('rolePerm','update','writer','INSERT INTO _rolePerm (role,perm) \r\nVALUES ({{role}},IF({{perm}}=\'root\' OR {{perm}}=\'always\' OR {{perm}}=\'never\',\'\',{{perm}})) \r\nON DUPLICATE KEY UPDATE \r\nperm=perm ;','userEdit','ui','2019-08-13 18:56:25'),('rolePerm','delete','writer','DELETE FROM _rolePerm \r\nWHERE role={{role}} \r\nAND role!=\'rds\' ;','userEdit','ui','2019-08-13 18:55:45'),('perm','delete','writer','DELETE FROM _perm \r\nWHERE perm={{perm}} \r\nAND perm!=\'root\' AND perm!=\'always\' AND perm!=\'never\' ;','userEdit','ui','2019-08-13 18:01:36'),('control','update','writer','INSERT INTO controls (host,zone,name,value,description)\r\nVALUES ({{host}},{{zone}},{{name}},{{value}},{{description}})\r\nON DUPLICATE KEY UPDATE\r\nvalue={{value}},\r\ndescription={{description}} ;','controlEdit','ui','2017-05-17 07:40:04'),('control','all','reader','SELECT * FROM controls ;','controlView','ui','2017-05-17 07:40:05'),('control','delete','writer','DELETE FROM controls \r\nWHERE host={{host}} \r\nAND zone={{zone}} \r\nAND name={{name}} ;\r\n','controlEdit','ui','2017-05-17 07:40:06'),('reports','all','reader','SELECT * FROM reports \r\nORDER BY ordinal, report ;','reportView','ui','2019-08-13 18:24:07'),('tuning','update','writer','UPDATE trak \r\nSET put={{value}}, \r\nstate=\'write\' \r\nWHERE host={{host}} \r\nAND name={{name}} \r\nAND register={{register}} ;\r\n','tuningEdit','ui','2017-05-17 07:40:08'),('reports','allWithoutSqltable','reader','SELECT * FROM reports WHERE type<>\'sqltable\' \r\nORDER BY ordinal, report ;','reportView','ui','2019-08-13 18:26:18'),('tuning','all','reader','SELECT * FROM trak ;','tuningView','ui','2017-05-17 07:40:12'),('tuning','delete','writer','DELETE FROM trak \r\nWHERE host={{host}} \r\nAND name={{name}} \r\nAND register={{register}} ;\r\n','tuningEdit','ui','2017-05-17 07:40:13'),('main','all','reader','SELECT * FROM _menu ORDER BY ordinal;','','ui','2019-08-13 20:36:04'),('system','all','reader','SELECT *, TIMESTAMPDIFF(SECOND,lastStart,NOW()) AS elapsed FROM launch ;','systemView','ui','2017-05-17 07:40:20'),('execute','all','reader','SELECT * FROM execute ORDER BY seq DESC ;','systemView','ui','2017-05-17 07:40:22'),('system','hosts','reader','SELECT DISTINCT host FROM launch ORDER BY host ;','systemView','ui','2017-05-17 07:40:22'),('system','execute','writer','INSERT INTO execute (host,command) VALUES ({{host}},{{command}}) ;','systemEdit','ui','2017-05-17 07:40:23'),('system','restart','writer','UPDATE launch SET operation=\'trigger\' WHERE host={{host}} AND ordinal={{ordinal}} ;','systemEdit','ui','2017-05-17 07:40:25'),('control','editable','reader','SELECT * FROM controls WHERE editable=\'yes\' ;','controlView','ui','2017-05-17 07:40:29'),('reports','items','reader','SELECT r.report, r.ordinal, r.code, c.description \r\nFROM reportItems AS r LEFT JOIN counters AS c ON r.code=c.code \r\nWHERE r.report={{report}} ;','reportView','ui','2019-08-13 18:26:22'),('reports','create','writer','INSERT INTO reports (report,title) VALUES ({{report}},{{report}}) ;','reportEdit','ui','2019-08-13 18:26:29'),('reports','update','writer','UPDATE reports \r\nSET type={{type}}, title={{title}}, ordinal={{ordinal}}, params={{params}} \r\nWHERE report={{report}} ;','reportEdit','ui','2019-08-13 18:26:33'),('reports','destroy','writer','DELETE FROM reports WHERE report={{report}} ;','reportEdit','ui','2019-08-13 18:26:30'),('reports','destroyItem','writer','DELETE FROM reportItems WHERE report={{report}} ;','reportEdit','ui','2019-08-13 18:26:31'),('reports','addItem','writer','REPLACE INTO reportItems (report,code,description) \r\nVALUES ({{report}}, {{code}}, {{description}}) ;','reportEdit','ui','2019-08-13 18:26:38'),('reports','removeItem','writer','DELETE FROM reportItems \r\nWHERE report={{report}} AND code={{code}} ;','reportEdit','ui','2019-08-13 18:26:32'),('webObjects','all','reader','SELECT * FROM webObjects ;','diagView','ui','2019-08-13 19:34:49'),('adminLog','all','reader','SELECT * FROM _adminLog;','adminLogView','ui','2019-08-13 16:55:18'),('counters','update','writer','UPDATE counters \r\nSET description={{description}} \r\nWHERE code={{code}} ;','reportEdit','ui','2019-08-13 17:26:06'),('reports','itemTotalVal','reader','SELECT SUM(counts.value) AS totalVal \r\nFROM counts, reportItems \r\nWHERE report={{report}} \r\nAND counts.code=reportItems.code \r\nAND counts.stamp>={{start}} \r\nAND counts.stamp<{{end}} ;','reportView','ui','2019-08-13 18:26:23'),('reports','itemCounts','reader','SELECT reportItems.ordinal, reportItems.description AS description, reportItems.code, SUM(counts.value) AS count \r\nFROM counts, reportItems \r\nWHERE report={{report}} \r\nAND counts.code=reportItems.code \r\nAND counts.stamp>={{start}} \r\nAND counts.stamp<{{end}} \r\nGROUP BY counts.code \r\nORDER BY reportItems.ordinal ASC, count DESC ;','reportView','ui','2019-08-13 18:24:21'),('prod','all','reader','CALL _proAll({{start}},{{end}}) ;','prodView','ui','2017-05-17 07:40:50'),('voice','getConfig','reader','SELECT * FROM voiceConfig \r\nWHERE operatorID={{id}} \r\nAND name={{name}} ;','voiceEdit','ui','2018-02-02 10:29:38'),('voice','talk','reader','SELECT\r\n d.*,\r\n v.description \r\nFROM voiceDialogs AS d \r\nLEFT OUTER JOIN voiceStates AS v \r\nON d.state=v.name \r\nWHERE operatorID={{id}} \r\nORDER BY seq DESC \r\nLIMIT 20 ;','voiceView','ui','2018-10-03 16:44:32'),('voice','detail','reader','SELECT * FROM proOperators \r\nWHERE operatorId={{id}} ;','voiceView','ui','2017-05-17 07:41:06'),('counters','all','reader','SELECT * FROM counters ;','reportView','ui','2019-08-13 20:12:58'),('reports','updateItem','writer','REPLACE INTO reportItems (report,code,description) \r\nVALUES ({{report}},{{code}},{{description}}) ;','reportEdit','ui','2019-08-13 18:26:34'),('notifyIndividuals','all','reader','SELECT * FROM notificationIndividuals ;','notifyView','ui','2017-05-22 14:45:57'),('notifyIndividuals','delete','writer','DELETE FROM notificationIndividuals WHERE individual={{individual}} ;','notifyEdit','ui','2017-05-22 14:46:07'),('notifyIndividuals','update','writer','INSERT INTO notificationIndividuals ( \r\nindividual,email,phone,carrier\r\n ) \r\nVALUES ( \r\n{{individual}},{{email}},{{phone}},{{carrier}} \r\n ) \r\nON DUPLICATE KEY UPDATE \r\nemail={{email}}, \r\nphone={{phone}}, \r\ncarrier={{carrier}} ;','notifyEdit','ui','2017-05-22 14:46:16'),('notifyGroups','all','reader','SELECT * FROM notificationGroups ;','notifyView','ui','2017-05-22 17:28:36'),('notifyGroups','delete','writer','DELETE FROM notificationGroups \r\nWHERE groupName={{group}} \r\nAND individual={{individual}} ;\r\n','notifyEdit','ui','2019-08-13 18:00:24'),('notifyGroups','update','writer','REPLACE INTO notificationGroups \r\n(groupName,individual) \r\nVALUES\r\n({{group}},{{individual}}) ;','notifyEdit','ui','2017-05-22 17:31:48'),('notifyGroups','byIndividual','reader','SELECT * FROM notificstionGroups \r\nWHERE individual={{individual}} ;','notifyView','ui','2017-05-22 17:33:14'),('notifyCarriers','all','reader','SELECT * FROM notificationCarriers ;','notifyView','ui','2017-05-22 17:49:16'),('notifyCarriers','delete','writer','DELETE FROM notificationCarriers \r\nWHERE carrier = {{carrier}} ;','notifyEdit','ui','2017-05-22 17:50:04'),('notifyCarriers','update','writer','INSERT INTO notificationCarriers ( \r\ncarrier,domain\r\n ) \r\nVALUES ( \r\n{{carrier}},{{domain}} \r\n ) \r\nON DUPLICATE KEY UPDATE  \r\ndomain={{domain}} ;','notifyEdit','ui','2017-05-23 08:46:17'),('notify','list','reader','SELECT individual FROM notificationIndividuals \r\nUNION SELECT DISTINCT groupName FROM notificationGroups ;','notifyView','ui','2017-05-23 09:00:03'),('notifyIndividuals','test','writer','INSERT INTO notificationIndividuals ( \r\nindividual,email,phone,carrier,sendTest\r\n ) \r\nVALUES ( \r\n{{individual}},{{email}},{{phone}},{{carrier}},1\r\n ) \r\nON DUPLICATE KEY UPDATE \r\nemail={{email}}, \r\nphone={{phone}}, \r\ncarrier={{carrier}},\r\nsendTest=1 ;','notifyEdit','ui','2017-05-23 13:27:39'),('heart','listen','reader','SELECT * FROM runtime WHERE name=\'webstamp\' ;','','ui','2019-08-12 16:16:24'),('heart','beat','writer','REPLACE INTO runtime(name,value) VALUES(\'webstamp\',{{date}}) ;','','ui','2019-08-12 16:17:22'),('voice','checkId','reader','SELECT COUNT(*) AS num \r\nFROM proOperators \r\nWHERE operatorID={{id}} ;','voiceView','ui','2018-02-02 10:29:37'),('voice','all','reader','SELECT\r\n p.*,\r\n IF(v.id IS NULL,\'\',v.id) AS device \r\nFROM proOperators AS p \r\nLEFT OUTER JOIN voiceDevices AS v \r\nON p.operatorID = v.operatorID ;','voiceView','ui','2018-10-03 16:44:20'),('prod','proopdate','writer','UPDATE proOperations SET goal={{goal}} WHERE sequence={{sequence}} ;','prodConfig','ui','2019-08-13 18:23:54'),('prod','proops','reader','SELECT * FROM proOperations ;','prodConfig','ui','2019-08-13 18:23:55'),('voice','logoffPO','writer','UPDATE proOperators \r\nSET task=\'\', area=\'\' \r\nWHERE operatorID={{id}} ;','voiceEdit','ui','2018-02-02 10:29:39'),('voice','logoffPOL','writer','UPDATE proOperatorLog \r\nSET endTime=NOW() \r\nWHERE operatorID={{id}} \r\nAND endTime IS NULL ;','voiceEdit','ui','2018-02-02 10:29:39'),('voice','logoffVD','writer','UPDATE voiceDevices \r\nSET operatorID=\'\' \r\nWHERE operatorID={{id}} ;','voiceEdit','ui','2018-02-02 10:29:39'),('voice','newOpPO','writer','INSERT INTO proOperators \r\nSET operatorID={{id}}, \r\noperatorName={{name}}, \r\nvoiceLevel=\'beginner\' ;','voiceEdit','ui','2018-02-02 10:29:40'),('voice','newOpVC','writer','INSERT INTO voiceConfig \r\nSET operatorID={{id}}, \r\nname=\'new\', \r\nvalue=\'new\' ;','voiceEdit','ui','2018-02-02 10:29:40'),('voice','setConfig','writer','REPLACE INTO voiceConfig \r\nSET operatorID={{id}}, \r\nname={{name}}, \r\nvalue={{value}} ;','voiceEdit','ui','2018-02-02 10:29:40'),('voice','pairings','reader','SELECT * FROM voicePairing ;','voiceView','ui','2018-09-19 17:14:12'),('voice','pair','writer','REPLACE INTO voicePairing(client,name,mac,pin) \r\nVALUES({{client}},{{name}},{{mac}},{{pin}}) ;','voiceEdit','ui','2018-09-21 14:41:03'),('voice','pairUpdate','writer','REPLACE INTO voiceConfig(operatorID,name,value) \r\nVALUES(\'pairing\',\'update\',\'\') ;','voiceEdit','ui','2018-09-21 16:28:37'),('voice','pairReload','writer','REPLACE INTO voiceConfig(operatorID,name,value) \r\nVALUES(\'pairing\',\'reload\',\'\') ;','voiceEdit','ui','2018-09-21 16:28:42'),('voice','pairDel','writer','DELETE FROM voicePairing \r\nWHERE client={{client}}\r\n AND name={{name}} ;','voiceEdit','ui','2018-09-21 16:19:58'),('voice','getLanguages','reader','SELECT DISTINCT language FROM voiceLanguageParams ;','voiceView','ui','2018-10-01 15:09:19'),('voice','operators','reader','SELECT DISTINCT operatorName \r\nFROM proOperators \r\nORDER BY operatorName ;','voiceView','ui','2018-10-12 11:33:00'),('voice','tasks','reader','SELECT task \r\nFROM proTasks \r\nORDER BY task ;','voiceView','ui','2018-10-12 11:33:53'),('voice','operations','reader','SELECT DISTINCT operation \r\nFROM proOperations \r\nORDER BY operation ;','voiceView','ui','2018-10-12 11:34:27'),('voice','areas','reader','SELECT area \r\nFROM proAreas \r\nORDER BY area ;','voiceView','ui','2018-10-12 17:28:33');

/*Table structure for table `_menu` */

CREATE TABLE IF NOT EXISTS `_menu` (
  `title` char(32) NOT NULL,
  `block` int(11) NOT NULL DEFAULT '0',
  `ordinal` int(11) NOT NULL DEFAULT '0',
  `link` char(64) NOT NULL DEFAULT '',
  `icon` char(64) NOT NULL DEFAULT '',
  `color` char(64) NOT NULL DEFAULT '',
  `description` char(255) NOT NULL DEFAULT '',
  `perm` char(64) NOT NULL DEFAULT '',
  `stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`title`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4;

/*Data for the table `_menu` */

insert  into `_menu`(`title`,`block`,`ordinal`,`link`,`icon`,`color`,`description`,`perm`,`stamp`) values ('Users',3,0,'#/user','people.svg','#009933','Maintain user and group permissions.','userView','2019-05-02 17:34:38'),('Voice_legacy',1,4,'#/voiceold','voicepick.svg','#1E90FF','Review and lookup voice operators.','voiceView','2019-08-13 22:47:26'),('Controls',2,0,'#/control','settings.svg','#6600cc','View and set system parameters.','controlView','2019-05-02 17:36:32'),('Production',1,5,'#/productivity','progress.svg','#999966','See picker productivity','prodView','2019-08-13 22:47:11'),('Reports',1,6,'#/report','report.svg','#009900','View system reports.','reportView','2019-08-13 22:47:11'),('System',3,0,'#/system','power.svg','#0033cc','Monitor, start, and stop processes and systems.','systemView','2019-05-02 17:39:40'),('Tuning',2,0,'#/tuning','dial.svg','#882266','View and set tuning parameters','tuningView','2019-05-02 17:40:18'),('Events',1,0,'#/event','ring.svg','#ff0066','System and process events and alerts','eventView','2019-05-02 17:41:28'),('Diagnostics',1,1,'#/diag?rds=candy','scope.svg','#ee3300','Graphical diagnostics','diagView','2019-05-02 18:04:40'),('Voice',1,3,'#/voice','voicepick.svg','#1E90FF','Review and lookup voice operators.','voiceView','2019-08-13 20:38:13'),('Devices',4,0,'#/device','computer.svg','#AABBCC','Devices','deviceView','2019-08-13 23:17:45'),('Docs',3,0,'#/doc','books.svg','#404050','Documentation.','docView','2019-08-13 20:42:46'),('Notify',1,2,'#/notify','notify.svg','#990022','Maintain notifications.','notifyView','2019-08-13 20:38:49');

/*Table structure for table `_perm` */

CREATE TABLE IF NOT EXISTS `_perm` (
  `perm` char(64) NOT NULL,
  `description` char(80) NOT NULL DEFAULT '',
  `enforced` enum('yes','no') DEFAULT 'yes',
  `stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`perm`),
  KEY `enforced` (`enforced`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

/*Data for the table `_perm` */

insert  into `_perm`(`perm`,`description`,`enforced`,`stamp`) values ('eventView','view events','no','2017-01-18 11:56:26'),('eventEdit','edit events','yes','2019-08-13 19:36:30'),('userView','view users','yes','2017-05-24 14:28:36'),('userEdit','edit users','yes','2017-05-24 14:28:47'),('notifyView','view notifications','no','2017-05-22 11:48:22'),('reportEdit','create and edit reports','no','2017-05-22 11:55:09'),('controlView','view control parameters','yes','2019-08-13 19:36:11'),('controlEdit','edit control parameters','yes','2019-08-13 19:36:15'),('tuningView','view tuning parameters','yes','2019-08-13 19:36:56'),('tuningEdit','edit tuning parameters','yes','2019-08-13 19:36:54'),('systemView','view system processes','yes','2019-08-13 19:36:53'),('systemEdit','edit system processes','yes','2019-08-13 19:36:50'),('diagView','view graphical diagnostics','no','2017-03-03 12:55:20'),('docView','view documents','yes','2019-08-13 20:43:09'),('deviceView','view devices','yes','2019-08-13 23:17:58'),('voiceView','view voice operators details and history','no','2017-03-07 15:53:13'),('prodView','view productivity reports','no','2017-03-07 15:53:35'),('reportView','view reports','no','2017-03-07 15:53:45'),('voiceEdit','edit voice operators','yes','2019-08-13 19:37:01'),('notifyEdit','edit notifications','yes','2019-08-13 19:36:32'),('adminLogView','view change logs','yes','2019-08-13 19:33:41'),('never','actions never allowed','yes','2019-08-13 18:21:16'),('always','actions always allowed','no','2019-08-13 18:21:16'),('root','UNLIMITED POWER *mad cackling*','yes','2019-08-13 18:21:16'),('prodConfig','configure productivity','yes','2019-08-13 19:35:42');

/*Table structure for table `_role` */

CREATE TABLE IF NOT EXISTS `_role` (
  `role` char(64) NOT NULL,
  `description` char(80) NOT NULL DEFAULT '',
  `stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`role`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

/*Data for the table `_role` */

insert  into `_role`(`role`,`description`,`stamp`) values ('admin','Administrator','2017-01-19 15:09:34'),('picker','Picker( View only )','2019-08-13 22:51:35'),('rds','rds','2019-08-12 16:20:56');

/*Table structure for table `_rolePerm` */

CREATE TABLE IF NOT EXISTS `_rolePerm` (
  `role` char(64) NOT NULL DEFAULT '',
  `perm` char(64) NOT NULL DEFAULT '',
  `stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`role`,`perm`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

/*Data for the table `_rolePerm` */

insert  into `_rolePerm`(`role`,`perm`,`stamp`) values ('admin','diagView','2019-08-13 20:10:04'),('admin','prodConfig','2019-08-13 20:10:04'),('admin','deviceView','2019-08-13 23:18:08'),('admin','prodView','2019-08-13 20:10:04'),('admin','adminLogView','2019-08-13 20:10:04'),('admin','eventView','2017-01-27 12:52:42'),('admin','reportView','2019-08-13 20:10:04'),('admin','userView','2017-01-27 12:52:42'),('admin','eventEdit','2017-01-27 12:52:42'),('admin','userEdit','2017-01-27 12:52:42'),('admin','userKill','2017-01-27 12:52:42'),('admin','userFire','2017-01-27 12:52:42'),('admin','reportEdit','2019-08-13 20:10:04'),('admin','notifyView','2019-08-13 20:10:04'),('admin','controlView','2017-01-27 12:52:42'),('admin','controlEdit','2017-01-27 12:52:42'),('admin','notifyEdit','2019-08-13 20:10:04'),('admin','systemEdit','2019-08-13 20:10:04'),('admin','systemView','2019-08-13 20:10:04'),('admin','tuningEdit','2019-08-13 20:10:04'),('picker','reportView','2019-08-13 22:51:35'),('picker','eventView','2019-08-13 22:51:35'),('picker','docView','2019-08-13 22:51:35'),('picker','diagView','2019-08-13 22:51:35'),('admin','docView','2019-08-13 20:43:30'),('admin','voiceView','2019-08-13 20:10:04'),('admin','voiceEdit','2019-08-13 20:10:04'),('admin','tuningView','2019-08-13 20:10:04'),('rds','root','2019-08-13 18:49:30');

/*Table structure for table `_user` */

CREATE TABLE IF NOT EXISTS `_user` (
  `user` char(64) NOT NULL,
  `name` char(64) NOT NULL DEFAULT '',
  `password` char(64) NOT NULL DEFAULT '',
  `stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

/*Data for the table `_user` */

insert  into `_user`(`user`,`name`,`password`,`stamp`) values ('test','test user','*94BDCEBE19083CE2A1F959FD02F964C7AF4CFC29','2019-08-13 22:51:55'),('rds','rds','*3D3AA71394F4DBBB71A496A2DA57AF2A94D92FF1','2019-08-12 16:20:56');

/*Table structure for table `_userRole` */

CREATE TABLE IF NOT EXISTS `_userRole` (
  `user` char(64) NOT NULL DEFAULT '',
  `role` char(64) NOT NULL DEFAULT '',
  `stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user`,`role`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

/*Data for the table `_userRole` */

insert  into `_userRole`(`user`,`role`,`stamp`) values ('test','picker','2019-08-13 22:51:55'),('rds','rds','2019-08-13 19:48:08');

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
