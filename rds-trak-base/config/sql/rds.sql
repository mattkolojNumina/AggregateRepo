-- MySQL dump 10.13  Distrib 5.1.35, for pc-linux-gnu (i686)
--
-- Host: db    Database: rds
-- ------------------------------------------------------
-- Server version	5.1.35-enterprise-commercial-classic

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `admin`
--

DROP TABLE IF EXISTS `admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `admin` (
  `username` char(16) NOT NULL DEFAULT '',
  `password` char(41) NOT NULL DEFAULT '',
  `level` int(11) NOT NULL DEFAULT '0',
  `expiration` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`username`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin`
--

LOCK TABLES `admin` WRITE;
/*!40000 ALTER TABLE `admin` DISABLE KEYS */;
INSERT INTO `admin` VALUES ('rds','*60C03F3E675ACB80108C34E8199ED2A0F8CDDDE5',9,5);
/*!40000 ALTER TABLE `admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `adminLog`
--

DROP TABLE IF EXISTS `adminLog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `adminLog` (
  `user` char(32) NOT NULL DEFAULT '',
  `description` char(128) NOT NULL DEFAULT '',
  `stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `stamp` (`stamp`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `adminLog`
--

LOCK TABLES `adminLog` WRITE;
/*!40000 ALTER TABLE `adminLog` DISABLE KEYS */;
/*!40000 ALTER TABLE `adminLog` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `adminPermissions`
--

DROP TABLE IF EXISTS `adminPermissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `adminPermissions` (
  `action` char(32) NOT NULL DEFAULT '',
  `description` char(64) NOT NULL DEFAULT '',
  `level` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`action`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `adminPermissions`
--

LOCK TABLES `adminPermissions` WRITE;
/*!40000 ALTER TABLE `adminPermissions` DISABLE KEYS */;
INSERT INTO `adminPermissions` VALUES ('process control','Manage application restart settings',7);
INSERT INTO `adminPermissions` VALUES ('advanced process control','Execute high-level commands (restart/reboot/etc.)',7);
INSERT INTO `adminPermissions` VALUES ('default action','The action level for unspecified actions',5);
INSERT INTO `adminPermissions` VALUES ('modify trak parameters','Update low-level tuning parameters',7);
INSERT INTO `adminPermissions` VALUES ('modify control parameters','Update program-specific settings',9);
INSERT INTO `adminPermissions` VALUES ('modify trak io','Change the state of inputs and outputs',9);
INSERT INTO `adminPermissions` VALUES ('manage users','Create, modify, or delete user accounts',9);
INSERT INTO `adminPermissions` VALUES ('view access control','Access the administrative controls panel',9);
INSERT INTO `adminPermissions` VALUES ('manage actions','Create, modify, or delete access control actions',9);
INSERT INTO `adminPermissions` VALUES ('view control parameters','Access the control parameters panel',7);
INSERT INTO `adminPermissions` VALUES ('view tuning parameters','Access the tuning parameters panel',7);
INSERT INTO `adminPermissions` VALUES ('view report editor','Access the report editor panel',5);
/*!40000 ALTER TABLE `adminPermissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cartonLog`
--

DROP TABLE IF EXISTS `cartonLog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cartonLog` (
  `sequence` int(11) NOT NULL AUTO_INCREMENT,
  `id` char(32) NOT NULL DEFAULT '',
  `code` char(32) NOT NULL DEFAULT '',
  `description` char(64) NOT NULL DEFAULT '',
  `stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `msec` int(3) unsigned zerofill NOT NULL DEFAULT '000',
  PRIMARY KEY (`sequence`),
  KEY `id` (`id`),
  KEY `stamp_msec` (`stamp`,`msec`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cartonLog`
--

LOCK TABLES `cartonLog` WRITE;
/*!40000 ALTER TABLE `cartonLog` DISABLE KEYS */;
/*!40000 ALTER TABLE `cartonLog` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `controls`
--

DROP TABLE IF EXISTS `controls`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `controls` (
  `zone` char(32) NOT NULL DEFAULT '',
  `name` char(32) NOT NULL DEFAULT '',
  `value` char(32) NOT NULL DEFAULT '',
  `description` char(80) NOT NULL DEFAULT '',
  `editable` enum('yes','no') NOT NULL DEFAULT 'yes',
  `stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`zone`,`name`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `controls`
--

LOCK TABLES `controls` WRITE;
/*!40000 ALTER TABLE `controls` DISABLE KEYS */;
/*!40000 ALTER TABLE `controls` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `counters`
--

DROP TABLE IF EXISTS `counters`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `counters` (
  `code` char(64) NOT NULL DEFAULT '',
  `description` char(64) NOT NULL DEFAULT '',
  `stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`code`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `counters`
--

LOCK TABLES `counters` WRITE;
/*!40000 ALTER TABLE `counters` DISABLE KEYS */;
/*!40000 ALTER TABLE `counters` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `counts`
--

DROP TABLE IF EXISTS `counts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `counts` (
  `code` char(32) NOT NULL DEFAULT '',
  `stamp` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `msec` int(3) unsigned zerofill NOT NULL DEFAULT '000',
  `value` double(17,0) NOT NULL DEFAULT '0',
  KEY `code_stamp` (`code`,`stamp`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `counts`
--

LOCK TABLES `counts` WRITE;
/*!40000 ALTER TABLE `counts` DISABLE KEYS */;
/*!40000 ALTER TABLE `counts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dashboard`
--

DROP TABLE IF EXISTS `dashboard`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dashboard` (
  `id` int(11) NOT NULL DEFAULT '0',
  `zone` char(32) NOT NULL DEFAULT '',
  `object` char(64) NOT NULL DEFAULT '',
  `params` char(128) NOT NULL DEFAULT '',
  KEY `zone_object` (`zone`,`object`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dashboard`
--

LOCK TABLES `dashboard` WRITE;
/*!40000 ALTER TABLE `dashboard` DISABLE KEYS */;
INSERT INTO `dashboard` VALUES (100,'panel','rdsDashboard.panel.ReportPanel','');
INSERT INTO `dashboard` VALUES (101,'panel','rdsDashboard.panel.EventPanel','');
INSERT INTO `dashboard` VALUES (102,'panel','rdsDashboard.panel.TracePanel','');
INSERT INTO `dashboard` VALUES (103,'panel','rdsDashboard.panel.ProcessPanel','');
INSERT INTO `dashboard` VALUES (202,'panel','rdsDashboard.panel.TuningPanel','');
INSERT INTO `dashboard` VALUES (203,'panel','rdsDashboard.panel.ConfigPanel','');
INSERT INTO `dashboard` VALUES (204,'panel','rdsDashboard.panel.AdminPanel','');
INSERT INTO `dashboard` VALUES (201,'panel','rdsDashboard.panel.ReportEditorPanel','');
INSERT INTO `dashboard` VALUES (199,'panel','rdsDashboard.panel.LinkPanel','');
/*!40000 ALTER TABLE `dashboard` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dashboardLinks`
--

DROP TABLE IF EXISTS `dashboardLinks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dashboardLinks` (
  `ordinal` int(11) NOT NULL DEFAULT '0',
  `type` enum('header','link','separator') NOT NULL DEFAULT 'link',
  `title` varchar(32) NOT NULL DEFAULT '',
  `description` varchar(255) NOT NULL DEFAULT '',
  `link` varchar(128) NOT NULL DEFAULT '',
  PRIMARY KEY (`ordinal`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dashboardLinks`
--

LOCK TABLES `dashboardLinks` WRITE;
/*!40000 ALTER TABLE `dashboardLinks` DISABLE KEYS */;
INSERT INTO `dashboardLinks` VALUES (12,'link','User\'s Guide','Guide to the RDS system and the web-based dashboard','/download/guide.pdf');
INSERT INTO `dashboardLinks` VALUES (11,'header','Documentation','','');
INSERT INTO `dashboardLinks` VALUES (20,'separator','','','');
INSERT INTO `dashboardLinks` VALUES (21,'header','Administration','','');
INSERT INTO `dashboardLinks` VALUES (22,'link','Database Admin','Database maintenance tool','/rds/pma/index.php');
/*!40000 ALTER TABLE `dashboardLinks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eventLog`
--

DROP TABLE IF EXISTS `eventLog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `eventLog` (
  `code` char(32) NOT NULL DEFAULT '',
  `state` enum('on','off') NOT NULL DEFAULT 'off',
  `start` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `duration` int(11) NOT NULL DEFAULT '0',
  KEY `code` (`code`),
  KEY `start` (`start`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eventLog`
--

LOCK TABLES `eventLog` WRITE;
/*!40000 ALTER TABLE `eventLog` DISABLE KEYS */;
/*!40000 ALTER TABLE `eventLog` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `events`
--

DROP TABLE IF EXISTS `events`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `events` (
  `code` char(32) NOT NULL DEFAULT '',
  `description` char(80) NOT NULL DEFAULT '',
  `severity` int(11) NOT NULL DEFAULT '0',
  `state` enum('on','off') NOT NULL DEFAULT 'off',
  `start` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`code`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `events`
--

LOCK TABLES `events` WRITE;
/*!40000 ALTER TABLE `events` DISABLE KEYS */;
/*!40000 ALTER TABLE `events` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `execute`
--

DROP TABLE IF EXISTS `execute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `execute` (
  `seq` int(11) NOT NULL AUTO_INCREMENT,
  `host` char(48) NOT NULL DEFAULT 'localhost',
  `command` char(255) NOT NULL DEFAULT '',
  `completed` enum('true','false') NOT NULL DEFAULT 'false',
  `stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`seq`),
  KEY `completed` (`host`,`completed`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `execute`
--

LOCK TABLES `execute` WRITE;
/*!40000 ALTER TABLE `execute` DISABLE KEYS */;
/*!40000 ALTER TABLE `execute` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `launch`
--

DROP TABLE IF EXISTS `launch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `launch` (
  `host` char(32) NOT NULL DEFAULT 'localhost',
  `ordinal` int(11) NOT NULL DEFAULT '0',
  `nickName` char(24) NOT NULL DEFAULT '',
  `displayName` char(24) NOT NULL DEFAULT '',
  `traceName` char(8) NOT NULL DEFAULT '',
  `home` char(64) NOT NULL DEFAULT '',
  `process` char(32) NOT NULL DEFAULT '',
  `args` char(80) NOT NULL DEFAULT '',
  `delayAfter` int(11) NOT NULL DEFAULT '0',
  `termDelay` int(11) NOT NULL DEFAULT '0',
  `pid` int(11) NOT NULL DEFAULT '-1',
  `mode` enum('daemon','manual','startonce') NOT NULL DEFAULT 'manual',
  `operation` enum('trigger','idle') NOT NULL DEFAULT 'idle',
  `count` int(11) NOT NULL DEFAULT '0',
  `throttled` enum('yes','no') NOT NULL DEFAULT 'no',
  `lastStart` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `host` (`host`,`ordinal`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `launch`
--

LOCK TABLES `launch` WRITE;
/*!40000 ALTER TABLE `launch` DISABLE KEYS */;
INSERT INTO `launch` VALUES ('build',3,'ctr','counter daemon','','/home/rds/bin','ctrd','',0,0,3859,'daemon','idle',1,'no','2011-02-09 18:15:29','2011-02-10 00:15:29');
INSERT INTO `launch` VALUES ('build',4,'evt','eventing daemon','','/home/rds/bin','evtd','',0,0,3860,'daemon','idle',1,'no','2011-02-09 18:15:29','2011-02-10 00:15:29');
INSERT INTO `launch` VALUES ('build',1,'trace','trace logger','','/home/rds/bin','trn_logd','/home/rds/log/trn.log',0,0,3857,'daemon','idle',1,'no','2011-02-09 18:15:29','2011-02-10 00:15:29');
INSERT INTO `launch` VALUES ('build',2,'webtron','web trace daemon','','/home/rds/bin','webtron','',0,0,3858,'daemon','idle',1,'no','2011-02-09 18:15:29','2011-02-10 00:15:29');
INSERT INTO `launch` VALUES ('build',100,'trak','trak loader','trak','/home/rds/app/bin','/bin/bash','rc.trak start',5,0,-1,'manual','idle',0,'no','2011-09-27 15:53:52','2011-09-27 21:15:54');
INSERT INTO `launch` VALUES ('build',101,'trakd','trak daemon','','/home/rds/bin','trakd','',0,0,-1,'manual','idle',0,'no','2011-09-27 15:53:52','2011-09-27 21:16:37');
INSERT INTO `launch` VALUES ('build',102,'trakload','register point loader','','/home/rds/bin','/bin/bash','rpload',0,0,-1,'manual','idle',0,'no','2011-09-27 15:53:52','2011-09-27 21:15:54');
INSERT INTO `launch` VALUES ('build',103,'traktrace','trak tracing','','/home/rds/bin','traktrace','',0,0,-1,'manual','idle',0,'no','2011-09-27 15:53:52','2011-09-27 21:16:37');
INSERT INTO `launch` VALUES ('build',10,'execute','command execution','','/home/rds/bin','execute','',0,0,3862,'daemon','idle',1,'no','2011-02-09 18:15:29','2011-02-10 00:15:29');
INSERT INTO `launch` VALUES ('build',5,'history','history daemon','','/home/rds/bin','histd','',0,0,3861,'daemon','idle',1,'no','2011-02-09 18:15:29','2011-02-10 00:15:29');
/*!40000 ALTER TABLE `launch` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `messages`
--

DROP TABLE IF EXISTS `messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `messages` (
  `message` char(80) NOT NULL DEFAULT '',
  `severity` enum('low','medium','high') NOT NULL DEFAULT 'high',
  `stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`message`),
  KEY `stamp` (`stamp`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `messages`
--

LOCK TABLES `messages` WRITE;
/*!40000 ALTER TABLE `messages` DISABLE KEYS */;
/*!40000 ALTER TABLE `messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reportItems`
--

DROP TABLE IF EXISTS `reportItems`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `reportItems` (
  `report` char(32) NOT NULL DEFAULT '',
  `ordinal` int(11) NOT NULL DEFAULT '0',
  `code` char(64) NOT NULL DEFAULT '',
  `description` char(128) NOT NULL DEFAULT '',
  PRIMARY KEY (`report`,`code`),
  KEY `view` (`report`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reportItems`
--

LOCK TABLES `reportItems` WRITE;
/*!40000 ALTER TABLE `reportItems` DISABLE KEYS */;
/*!40000 ALTER TABLE `reportItems` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reports`
--

DROP TABLE IF EXISTS `reports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `reports` (
  `report` varchar(32) NOT NULL DEFAULT 'report',
  `type` varchar(16) NOT NULL DEFAULT 'table',
  `title` varchar(64) NOT NULL DEFAULT '',
  `ordinal` int(11) NOT NULL DEFAULT '0',
  `params` varchar(255) NOT NULL DEFAULT '',
  `stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`report`),
  KEY `stamp` (`stamp`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reports`
--

LOCK TABLES `reports` WRITE;
/*!40000 ALTER TABLE `reports` DISABLE KEYS */;
/*!40000 ALTER TABLE `reports` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `runtime`
--

DROP TABLE IF EXISTS `runtime`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `runtime` (
  `name` char(32) NOT NULL DEFAULT '',
  `value` varchar(255) NOT NULL DEFAULT '',
  `stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `runtime`
--

LOCK TABLES `runtime` WRITE;
/*!40000 ALTER TABLE `runtime` DISABLE KEYS */;
/*!40000 ALTER TABLE `runtime` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `trak`
--

DROP TABLE IF EXISTS `trak`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trak` (
  `zone` enum('rp','dp') NOT NULL DEFAULT 'rp',
  `name` char(32) NOT NULL DEFAULT '',
  `register` int(11) NOT NULL DEFAULT '-1',
  `area` char(32) NOT NULL DEFAULT '',
  `get` int(11) DEFAULT NULL,
  `put` int(11) DEFAULT NULL,
  `standard` int(11) DEFAULT NULL,
  `state` enum('idle','write','standard','save') NOT NULL DEFAULT 'idle',
  `description` char(64) DEFAULT NULL,
  `handle` int(11) NOT NULL DEFAULT '-1',
  `stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`name`,`register`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trak`
--

LOCK TABLES `trak` WRITE;
/*!40000 ALTER TABLE `trak` DISABLE KEYS */;
/*!40000 ALTER TABLE `trak` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `webObjects`
--

DROP TABLE IF EXISTS `webObjects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `webObjects` (
  `name` char(32) NOT NULL DEFAULT '',
  `area` char(32) NOT NULL DEFAULT 'main',
  `type` char(32) NOT NULL DEFAULT '',
  `value` char(32) NOT NULL DEFAULT '',
  `hint` char(64) NOT NULL DEFAULT '',
  `stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`name`),
  KEY `stamp` (`stamp`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `webObjects`
--

LOCK TABLES `webObjects` WRITE;
/*!40000 ALTER TABLE `webObjects` DISABLE KEYS */;
/*!40000 ALTER TABLE `webObjects` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2011-11-14 22:16:21
