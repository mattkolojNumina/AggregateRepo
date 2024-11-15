-- MariaDB dump 10.19  Distrib 10.6.7-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: rds
-- ------------------------------------------------------
-- Server version	10.6.7-MariaDB-2ubuntu1-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `_db`
--

DROP TABLE IF EXISTS `_db`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `_db` (
  `topic` char(32) NOT NULL DEFAULT '',
  `action` char(32) NOT NULL DEFAULT '',
  `server` char(32) NOT NULL DEFAULT '',
  `query` text NOT NULL,
  `perm` char(64) NOT NULL DEFAULT '',
  `project` char(32) NOT NULL DEFAULT '',
  `stamp` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`topic`,`action`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `_db`
--
-- ORDER BY:  `topic`,`action`

LOCK TABLES `_db` WRITE;
/*!40000 ALTER TABLE `_db` DISABLE KEYS */;
INSERT INTO `_db` VALUES ('adminLog','all','reader','SELECT * FROM _adminLog;','adminLogView','ui','2019-08-13 21:55:18');
INSERT INTO `_db` VALUES ('control','all','reader','SELECT * FROM controls ;','controlView','ui','2017-05-17 12:40:05');
INSERT INTO `_db` VALUES ('control','delete','writer','DELETE FROM controls \r\nWHERE host={{host}} \r\nAND zone={{zone}} \r\nAND name={{name}} ;\r\n','controlEdit','ui','2017-05-17 12:40:06');
INSERT INTO `_db` VALUES ('control','editable','reader','SELECT * FROM controls WHERE editable=\'yes\' ;','controlView','ui','2017-05-17 12:40:29');
INSERT INTO `_db` VALUES ('control','update','writer','INSERT INTO controls (host,zone,name,value,description)\r\nVALUES ({{host}},{{zone}},{{name}},{{value}},{{description}})\r\nON DUPLICATE KEY UPDATE\r\nvalue={{value}},\r\ndescription={{description}} ;','controlEdit','ui','2017-05-17 12:40:04');
INSERT INTO `_db` VALUES ('counters','all','reader','SELECT * FROM counters ;','reportView','ui','2019-08-14 01:12:58');
INSERT INTO `_db` VALUES ('counters','update','writer','UPDATE counters \r\nSET description={{description}} \r\nWHERE code={{code}} ;','reportEdit','ui','2019-08-13 22:26:06');
INSERT INTO `_db` VALUES ('event','all','reader','SELECT * FROM events ;','eventView','ui','2019-05-17 12:39:18');
INSERT INTO `_db` VALUES ('event','current','reader','SELECT *, TIMESTAMPDIFF(SECOND,start,NOW()) AS duration \r\nFROM events \r\nWHERE state=\'on\' ;','eventView','ui','2019-08-13 22:28:45');
INSERT INTO `_db` VALUES ('event','delete','writer','DELETE FROM events \r\nWHERE code={{code}} ;','eventEdit','ui','2017-05-17 12:39:41');
INSERT INTO `_db` VALUES ('event','recent','reader','SELECT l.code, e.description, e.severity, l.state, l.start, e.suggestedAction,\r\n  IF(e.state=\'on\',TIMESTAMPDIFF(SECOND,l.start,NOW()),l.duration) AS duration\r\nFROM eventLog as l\r\nLEFT OUTER JOIN `events` as e\r\nON e.code=l.code \r\nORDER BY l.start DESC\r\nLIMIT {{limit}} ;\r\n\r\n\r\n','eventView','ui','2019-08-13 22:30:00');
INSERT INTO `_db` VALUES ('event','report','reader','SELECT l.code,\r\n       e.description,\r\n       e.severity,\r\n       COUNT(l.code) AS totalQuantity,\r\n       SUM(l.duration) AS totalDuration\r\nFROM eventLog AS l\r\nLEFT OUTER JOIN events AS e\r\nON e.code=l.code\r\nWHERE l.start >= {{start}}\r\nAND l.start < {{end}}\r\nGROUP BY l.code\r\nORDER BY totalQuantity DESC ;\r\n\r\n','eventView','ui','2019-08-13 22:30:36');
INSERT INTO `_db` VALUES ('event','update','writer','INSERT INTO events (code,description,severity,notify,suggestedAction)\r\nVALUES ({{code}},{{description}},{{severity}},{{notify}},{{suggestedAction}})\r\nON DUPLICATE KEY UPDATE\r\ndescription={{description}},\r\nseverity={{severity}},\r\nnotify={{notify}},\r\nsuggestedAction={{suggestedAction}} ;','eventEdit','ui','2017-05-23 14:12:37');
INSERT INTO `_db` VALUES ('execute','all','reader','SELECT * FROM execute ORDER BY seq DESC ;','systemView','ui','2017-05-17 12:40:22');
INSERT INTO `_db` VALUES ('heart','beat','writer','REPLACE INTO runtime(name,value) VALUES(\'webstamp\',{{date}}) ;','','ui','2019-08-12 21:17:22');
INSERT INTO `_db` VALUES ('heart','listen','reader','SELECT * FROM runtime WHERE name=\'webstamp\' ;','','ui','2019-08-12 21:16:24');
INSERT INTO `_db` VALUES ('main','all','reader','SELECT * FROM _menu ORDER BY ordinal;','','ui','2019-08-14 01:36:04');
INSERT INTO `_db` VALUES ('notify','list','reader','SELECT individual FROM notificationIndividuals \r\nUNION SELECT DISTINCT groupName FROM notificationGroups ;','notifyView','ui','2017-05-23 14:00:03');
INSERT INTO `_db` VALUES ('notifyCarriers','all','reader','SELECT * FROM notificationCarriers ;','notifyView','ui','2017-05-22 22:49:16');
INSERT INTO `_db` VALUES ('notifyCarriers','delete','writer','DELETE FROM notificationCarriers \r\nWHERE carrier = {{carrier}} ;','notifyEdit','ui','2017-05-22 22:50:04');
INSERT INTO `_db` VALUES ('notifyCarriers','update','writer','INSERT INTO notificationCarriers ( \r\ncarrier,domain\r\n ) \r\nVALUES ( \r\n{{carrier}},{{domain}} \r\n ) \r\nON DUPLICATE KEY UPDATE  \r\ndomain={{domain}} ;','notifyEdit','ui','2017-05-23 13:46:17');
INSERT INTO `_db` VALUES ('notifyGroups','all','reader','SELECT * FROM notificationGroups ;','notifyView','ui','2017-05-22 22:28:36');
INSERT INTO `_db` VALUES ('notifyGroups','byIndividual','reader','SELECT * FROM notificstionGroups \r\nWHERE individual={{individual}} ;','notifyView','ui','2017-05-22 22:33:14');
INSERT INTO `_db` VALUES ('notifyGroups','delete','writer','DELETE FROM notificationGroups \r\nWHERE groupName={{group}} \r\nAND individual={{individual}} ;\r\n','notifyEdit','ui','2019-08-13 23:00:24');
INSERT INTO `_db` VALUES ('notifyGroups','update','writer','REPLACE INTO notificationGroups \r\n(groupName,individual) \r\nVALUES\r\n({{group}},{{individual}}) ;','notifyEdit','ui','2017-05-22 22:31:48');
INSERT INTO `_db` VALUES ('notifyIndividuals','all','reader','SELECT * FROM notificationIndividuals ;','notifyView','ui','2017-05-22 19:45:57');
INSERT INTO `_db` VALUES ('notifyIndividuals','delete','writer','DELETE FROM notificationIndividuals WHERE individual={{individual}} ;','notifyEdit','ui','2017-05-22 19:46:07');
INSERT INTO `_db` VALUES ('notifyIndividuals','test','writer','INSERT INTO notificationIndividuals ( \r\nindividual,email,phone,carrier,sendTest\r\n ) \r\nVALUES ( \r\n{{individual}},{{email}},{{phone}},{{carrier}},1\r\n ) \r\nON DUPLICATE KEY UPDATE \r\nemail={{email}}, \r\nphone={{phone}}, \r\ncarrier={{carrier}},\r\nsendTest=1 ;','notifyEdit','ui','2017-05-23 18:27:39');
INSERT INTO `_db` VALUES ('notifyIndividuals','update','writer','INSERT INTO notificationIndividuals ( \r\nindividual,email,phone,carrier\r\n ) \r\nVALUES ( \r\n{{individual}},{{email}},{{phone}},{{carrier}} \r\n ) \r\nON DUPLICATE KEY UPDATE \r\nemail={{email}}, \r\nphone={{phone}}, \r\ncarrier={{carrier}} ;','notifyEdit','ui','2017-05-22 19:46:16');
INSERT INTO `_db` VALUES ('perm','all','reader','SELECT * FROM _perm \r\nWHERE perm!=\'root\' AND perm!=\'always\' AND perm!=\'never\' \r\nORDER BY perm ;','userView','ui','2019-08-13 23:01:20');
INSERT INTO `_db` VALUES ('perm','delete','writer','DELETE FROM _perm \r\nWHERE perm={{perm}} \r\nAND perm!=\'root\' AND perm!=\'always\' AND perm!=\'never\' ;','userEdit','ui','2019-08-13 23:01:36');
INSERT INTO `_db` VALUES ('perm','forRole','reader','SELECT p.perm, p.description, IF(rp.role IS NULL,\'false\',\'true\') AS assigned \r\nFROM _perm AS p \r\nLEFT OUTER JOIN (SELECT * FROM _rolePerm WHERE role={{role}} AND role!=\'rds\') AS rp \r\nON p.perm=rp.perm \r\nWHERE p.perm!=\'root\' AND p.perm!=\'always\' AND p.perm!=\'never\' \r\nORDER BY p.perm ;','userView','ui','2019-08-13 23:52:50');
INSERT INTO `_db` VALUES ('perm','update','writer','INSERT INTO _perm (perm,description,enforced) \r\nVALUES ({{perm}},{{description}},IF({{enforced}}=\'\',enforced,{{enforced}})) \r\nON DUPLICATE KEY UPDATE \r\ndescription=IF({{perm}}=\'root\' OR {{perm}}=\'always\' OR {{perm}}=\'never\',description,{{description}}),\r\nenforced=IF({{perm}}=\'root\' OR {{perm}}=\'always\' OR {{perm}}=\'never\' OR {{enforced}}=\'\',enforced,{{enforced}}) ;','userEdit','ui','2019-08-13 23:53:00');
INSERT INTO `_db` VALUES ('prod','all','reader','CALL _proAll({{start}},{{end}}) ;','prodView','ui','2017-05-17 12:40:50');
INSERT INTO `_db` VALUES ('prod','proopdate','writer','UPDATE proOperations SET goal={{goal}} WHERE sequence={{sequence}} ;','prodConfig','ui','2019-08-13 23:23:54');
INSERT INTO `_db` VALUES ('prod','proops','reader','SELECT * FROM proOperations ;','prodConfig','ui','2019-08-13 23:23:55');
INSERT INTO `_db` VALUES ('reports','addItem','writer','REPLACE INTO reportItems (report,code,description) \r\nVALUES ({{report}}, {{code}}, {{description}}) ;','reportEdit','ui','2019-08-13 23:26:38');
INSERT INTO `_db` VALUES ('reports','all','reader','SELECT * FROM reports \r\nORDER BY ordinal, report ;','reportView','ui','2019-08-13 23:24:07');
INSERT INTO `_db` VALUES ('reports','allWithoutSqltable','reader','SELECT * FROM reports WHERE type<>\'sqltable\' \r\nORDER BY ordinal, report ;','reportView','ui','2019-08-13 23:26:18');
INSERT INTO `_db` VALUES ('reports','create','writer','INSERT INTO reports (report,title) VALUES ({{report}},{{report}}) ;','reportEdit','ui','2019-08-13 23:26:29');
INSERT INTO `_db` VALUES ('reports','destroy','writer','DELETE FROM reports WHERE report={{report}} ;','reportEdit','ui','2019-08-13 23:26:30');
INSERT INTO `_db` VALUES ('reports','destroyItem','writer','DELETE FROM reportItems WHERE report={{report}} ;','reportEdit','ui','2019-08-13 23:26:31');
INSERT INTO `_db` VALUES ('reports','itemCounts','reader','SELECT reportItems.ordinal, reportItems.description AS description, reportItems.code, SUM(counts.value) AS count \r\nFROM counts, reportItems \r\nWHERE report={{report}} \r\nAND counts.code=reportItems.code \r\nAND counts.stamp>={{start}} \r\nAND counts.stamp<{{end}} \r\nGROUP BY counts.code \r\nORDER BY reportItems.ordinal ASC, count DESC ;','reportView','ui','2019-08-13 23:24:21');
INSERT INTO `_db` VALUES ('reports','items','reader','SELECT r.report, r.ordinal, r.code, c.description \r\nFROM reportItems AS r LEFT JOIN counters AS c ON r.code=c.code \r\nWHERE r.report={{report}} ;','reportView','ui','2019-08-13 23:26:22');
INSERT INTO `_db` VALUES ('reports','itemTotalVal','reader','SELECT SUM(counts.value) AS totalVal \r\nFROM counts, reportItems \r\nWHERE report={{report}} \r\nAND counts.code=reportItems.code \r\nAND counts.stamp>={{start}} \r\nAND counts.stamp<{{end}} ;','reportView','ui','2019-08-13 23:26:23');
INSERT INTO `_db` VALUES ('reports','removeItem','writer','DELETE FROM reportItems \r\nWHERE report={{report}} AND code={{code}} ;','reportEdit','ui','2019-08-13 23:26:32');
INSERT INTO `_db` VALUES ('reports','update','writer','UPDATE reports \r\nSET type={{type}}, title={{title}}, ordinal={{ordinal}}, params={{params}} \r\nWHERE report={{report}} ;','reportEdit','ui','2019-08-13 23:26:33');
INSERT INTO `_db` VALUES ('reports','updateItem','writer','REPLACE INTO reportItems (report,code,description) \r\nVALUES ({{report}},{{code}},{{description}}) ;','reportEdit','ui','2019-08-13 23:26:34');
INSERT INTO `_db` VALUES ('role','all','reader','SELECT * FROM _role WHERE role!=\'rds\' \r\nORDER BY role ;','userView','ui','2019-08-13 23:53:27');
INSERT INTO `_db` VALUES ('role','delete','writer','DELETE FROM _role \r\nWHERE role={{role}} AND role!=\'rds\' AND role!=\'admin\' ;','userEdit','ui','2019-08-13 23:54:23');
INSERT INTO `_db` VALUES ('role','forUser','reader','SELECT r.role, r.description, IF(ur.user IS NULL,\'false\',\'true\') AS assigned \r\nFROM _role AS r \r\nLEFT OUTER JOIN (SELECT * FROM _userRole WHERE user={{user}} AND user!=\'rds\') AS ur \r\nON r.role=ur.role \r\nWHERE r.role!=\'rds\' \r\nORDER BY r.role ;\r\n','userView','ui','2019-08-13 23:54:51');
INSERT INTO `_db` VALUES ('role','update','writer','INSERT INTO _role (role,description)\r\nVALUES ({{role}},{{description}})\r\nON DUPLICATE KEY UPDATE \r\ndescription={{description}} ;\r\n\r\n','userEdit','ui','2019-08-13 23:55:25');
INSERT INTO `_db` VALUES ('rolePerm','delete','writer','DELETE FROM _rolePerm \r\nWHERE role={{role}} \r\nAND role!=\'rds\' ;','userEdit','ui','2019-08-13 23:55:45');
INSERT INTO `_db` VALUES ('rolePerm','deletePerm','writer','DELETE FROM _rolePerm \r\nWHERE role={{role}} \r\nAND perm={{perm}} \r\nAND role!=\'rds\' ;','userEdit','ui','2019-08-13 23:55:57');
INSERT INTO `_db` VALUES ('rolePerm','update','writer','INSERT INTO _rolePerm (role,perm) \r\nVALUES ({{role}},IF({{perm}}=\'root\' OR {{perm}}=\'always\' OR {{perm}}=\'never\',\'\',{{perm}})) \r\nON DUPLICATE KEY UPDATE \r\nperm=perm ;','userEdit','ui','2019-08-13 23:56:25');
INSERT INTO `_db` VALUES ('system','all','reader','SELECT *, TIMESTAMPDIFF(SECOND,lastStart,NOW()) AS elapsed FROM launch ;','systemView','ui','2017-05-17 12:40:20');
INSERT INTO `_db` VALUES ('system','execute','writer','INSERT INTO execute (host,command) VALUES ({{host}},{{command}}) ;','systemEdit','ui','2017-05-17 12:40:23');
INSERT INTO `_db` VALUES ('system','hosts','reader','SELECT DISTINCT host FROM launch ORDER BY host ;','systemView','ui','2017-05-17 12:40:22');
INSERT INTO `_db` VALUES ('system','restart','writer','UPDATE launch SET operation=\'trigger\' WHERE host={{host}} AND ordinal={{ordinal}} ;','systemEdit','ui','2017-05-17 12:40:25');
INSERT INTO `_db` VALUES ('tuning','all','reader','SELECT * FROM trak ;','tuningView','ui','2017-05-17 12:40:12');
INSERT INTO `_db` VALUES ('tuning','delete','writer','DELETE FROM trak \r\nWHERE host={{host}} \r\nAND name={{name}} \r\nAND register={{register}} ;\r\n','tuningEdit','ui','2017-05-17 12:40:13');
INSERT INTO `_db` VALUES ('tuning','update','writer','UPDATE trak \r\nSET put={{value}}, \r\nstate=\'write\' \r\nWHERE host={{host}} \r\nAND name={{name}} \r\nAND register={{register}} ;\r\n','tuningEdit','ui','2017-05-17 12:40:08');
INSERT INTO `_db` VALUES ('user','all','reader','SELECT * FROM _user \r\nWHERE user!=\'rds\' \r\nORDER BY user ;','userView','ui','2019-08-13 23:59:01');
INSERT INTO `_db` VALUES ('user','delete','writer','DELETE FROM _user \r\nWHERE user={{user}} \r\nAND user!=\'rds\' ;','userEdit','ui','2019-08-13 23:59:11');
INSERT INTO `_db` VALUES ('user','update','writer','INSERT INTO _user (user,name) \r\nVALUES ({{user}},{{name}}) \r\nON DUPLICATE KEY UPDATE \r\nname={{name}} ;','userEdit','ui','2019-08-13 23:59:27');
INSERT INTO `_db` VALUES ('user','updatePass','writer','UPDATE _user \r\nSET password=PASSWORD({{password}}) \r\nWHERE user={{user}} ;','userEdit','ui','2017-05-17 12:39:55');
INSERT INTO `_db` VALUES ('userRole','delete','writer','DELETE FROM _userRole \r\nWHERE user={{user}} \r\nAND user!=\'rds\' ;','userEdit','ui','2019-08-13 23:59:47');
INSERT INTO `_db` VALUES ('userRole','deleteRole','writer','DELETE FROM _userRole \r\nWHERE user={{user}} \r\nAND role={{role}} \r\nAND user!=\'rds\' ;','userEdit','ui','2019-08-13 23:59:57');
INSERT INTO `_db` VALUES ('userRole','update','writer','REPLACE INTO _userRole (user,role) \r\nVALUES ({{user}},{{role}}) ;','userEdit','ui','2017-05-17 12:39:57');
INSERT INTO `_db` VALUES ('voice','all','reader','SELECT\r\n p.*,\r\n IF(v.id IS NULL,\'\',v.id) AS device \r\nFROM proOperators AS p \r\nLEFT OUTER JOIN voiceDevices AS v \r\nON p.operatorID = v.operatorID ;','voiceView','ui','2018-10-03 21:44:20');
INSERT INTO `_db` VALUES ('voice','areas','reader','SELECT area \r\nFROM proAreas \r\nORDER BY area ;','voiceView','ui','2018-10-12 22:28:33');
INSERT INTO `_db` VALUES ('voice','checkId','reader','SELECT COUNT(*) AS num \r\nFROM proOperators \r\nWHERE operatorID={{id}} ;','voiceView','ui','2018-02-02 16:29:37');
INSERT INTO `_db` VALUES ('voice','detail','reader','SELECT * FROM proOperators \r\nWHERE operatorId={{id}} ;','voiceView','ui','2017-05-17 12:41:06');
INSERT INTO `_db` VALUES ('voice','getConfig','reader','SELECT * FROM voiceConfig \r\nWHERE operatorID={{id}} \r\nAND name={{name}} ;','voiceEdit','ui','2018-02-02 16:29:38');
INSERT INTO `_db` VALUES ('voice','getLanguages','reader','SELECT DISTINCT language FROM voiceLanguageParams ;','voiceView','ui','2018-10-01 20:09:19');
INSERT INTO `_db` VALUES ('voice','logoffPO','writer','UPDATE proOperators \r\nSET task=\'\', area=\'\' \r\nWHERE operatorID={{id}} ;','voiceEdit','ui','2018-02-02 16:29:39');
INSERT INTO `_db` VALUES ('voice','logoffPOL','writer','UPDATE proOperatorLog \r\nSET endTime=NOW() \r\nWHERE operatorID={{id}} \r\nAND endTime IS NULL ;','voiceEdit','ui','2018-02-02 16:29:39');
INSERT INTO `_db` VALUES ('voice','logoffVD','writer','UPDATE voiceDevices \r\nSET operatorID=\'\' \r\nWHERE operatorID={{id}} ;','voiceEdit','ui','2018-02-02 16:29:39');
INSERT INTO `_db` VALUES ('voice','newOpPO','writer','INSERT INTO proOperators \r\nSET operatorID={{id}}, \r\noperatorName={{name}}, \r\nvoiceLevel=\'beginner\' ;','voiceEdit','ui','2018-02-02 16:29:40');
INSERT INTO `_db` VALUES ('voice','newOpVC','writer','INSERT INTO voiceConfig \r\nSET operatorID={{id}}, \r\nname=\'new\', \r\nvalue=\'new\' ;','voiceEdit','ui','2018-02-02 16:29:40');
INSERT INTO `_db` VALUES ('voice','operations','reader','SELECT DISTINCT operation \r\nFROM proOperations \r\nORDER BY operation ;','voiceView','ui','2018-10-12 16:34:27');
INSERT INTO `_db` VALUES ('voice','operators','reader','SELECT DISTINCT operatorName \r\nFROM proOperators \r\nORDER BY operatorName ;','voiceView','ui','2018-10-12 16:33:00');
INSERT INTO `_db` VALUES ('voice','pair','writer','REPLACE INTO voicePairing(client,name,mac,pin) \r\nVALUES({{client}},{{name}},{{mac}},{{pin}}) ;','voiceEdit','ui','2018-09-21 19:41:03');
INSERT INTO `_db` VALUES ('voice','pairDel','writer','DELETE FROM voicePairing \r\nWHERE client={{client}}\r\n AND name={{name}} ;','voiceEdit','ui','2018-09-21 21:19:58');
INSERT INTO `_db` VALUES ('voice','pairings','reader','SELECT * FROM voicePairing ;','voiceView','ui','2018-09-19 22:14:12');
INSERT INTO `_db` VALUES ('voice','pairReload','writer','REPLACE INTO voiceConfig(operatorID,name,value) \r\nVALUES(\'pairing\',\'reload\',\'\') ;','voiceEdit','ui','2018-09-21 21:28:42');
INSERT INTO `_db` VALUES ('voice','pairUpdate','writer','REPLACE INTO voiceConfig(operatorID,name,value) \r\nVALUES(\'pairing\',\'update\',\'\') ;','voiceEdit','ui','2018-09-21 21:28:37');
INSERT INTO `_db` VALUES ('voice','setConfig','writer','REPLACE INTO voiceConfig \r\nSET operatorID={{id}}, \r\nname={{name}}, \r\nvalue={{value}} ;','voiceEdit','ui','2018-02-02 16:29:40');
INSERT INTO `_db` VALUES ('voice','talk','reader','SELECT\r\n d.*,\r\n v.description \r\nFROM voiceDialogs AS d \r\nLEFT OUTER JOIN voiceStates AS v \r\nON d.state=v.name \r\nWHERE operatorID={{id}} \r\nORDER BY seq DESC \r\nLIMIT 20 ;','voiceView','ui','2018-10-03 21:44:32');
INSERT INTO `_db` VALUES ('voice','tasks','reader','SELECT task \r\nFROM proTasks \r\nORDER BY task ;','voiceView','ui','2018-10-12 16:33:53');
INSERT INTO `_db` VALUES ('webObjects','all','reader','SELECT * FROM webObjects ;','diagView','ui','2019-08-14 00:34:49');
/*!40000 ALTER TABLE `_db` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-07-17  0:01:02
