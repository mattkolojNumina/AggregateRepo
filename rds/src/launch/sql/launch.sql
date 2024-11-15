-- MySQL dump 10.14  Distrib 5.5.38-MariaDB, for Linux (i686)
--
-- Host: localhost    Database: rds
-- ------------------------------------------------------
-- Server version	5.5.38-MariaDB-log

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
-- Table structure for table `launch`
--

DROP TABLE IF EXISTS `launch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `launch` (
  `host` char(32) NOT NULL DEFAULT 'localhost',
  `ordinal` int(11) NOT NULL DEFAULT '0',
  `nickName` char(32) NOT NULL DEFAULT '',
  `displayName` char(64) NOT NULL DEFAULT '',
  `traceName` char(8) NOT NULL DEFAULT '',
  `home` char(64) NOT NULL DEFAULT '',
  `process` char(32) NOT NULL DEFAULT '',
  `args` char(255) NOT NULL DEFAULT '',
  `delayAfter` int(11) NOT NULL DEFAULT '0',
  `termDelay` int(11) NOT NULL DEFAULT '0',
  `pid` int(11) NOT NULL DEFAULT '-1',
  `mode` enum('daemon','manual','startonce') NOT NULL DEFAULT 'manual',
  `operation` enum('trigger','idle') NOT NULL DEFAULT 'idle',
  `count` int(11) NOT NULL DEFAULT '0',
  `throttled` enum('yes','no') NOT NULL DEFAULT 'no',
  `lastStart` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`host`,`ordinal`),
  KEY `status` (`throttled`,`lastStart`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `launch`
--
-- ORDER BY:  `host`,`ordinal`

LOCK TABLES `launch` WRITE;
/*!40000 ALTER TABLE `launch` DISABLE KEYS */;
INSERT INTO `launch` VALUES ('build',1,'trace','trace logger','','/home/rds/bin','trn_logd','/home/rds/log/trn.log',0,0,2605,'daemon','idle',1,'no','2016-03-30 09:28:51','2016-03-30 14:28:51');
INSERT INTO `launch` VALUES ('build',2,'webtron','web trace daemon','','/home/rds/bin','webtron','',0,0,2606,'daemon','idle',1,'no','2016-03-30 09:28:51','2016-03-30 14:28:51');
INSERT INTO `launch` VALUES ('build',3,'ctr','counter daemon','','/home/rds/bin','ctrd','',0,0,2607,'daemon','idle',1,'no','2016-03-30 09:28:51','2016-03-30 14:28:51');
INSERT INTO `launch` VALUES ('build',4,'evt','eventing daemon','','/home/rds/bin','evtd','',0,0,2608,'daemon','idle',1,'no','2016-03-30 09:28:51','2016-03-30 14:28:51');
INSERT INTO `launch` VALUES ('build',5,'history','history daemon','','/home/rds/bin','histd','',0,0,2609,'daemon','idle',1,'no','2016-03-30 09:28:51','2016-03-30 14:28:51');
INSERT INTO `launch` VALUES ('build',10,'execute','command execution','','/home/rds/bin','execute','',0,0,2610,'daemon','idle',1,'no','2016-03-30 09:28:51','2016-03-30 14:28:51');
INSERT INTO `launch` VALUES ('build',100,'trak','trak loader','trak','/home/rds/app/bin','/bin/bash','rc.trak',5,0,-1,'manual','idle',0,'no','2014-08-04 19:09:51','2014-08-07 13:52:56');
INSERT INTO `launch` VALUES ('build',101,'trakd','trak daemon','','/home/rds/bin','trakd','',0,0,-1,'manual','idle',0,'no','2014-08-07 08:52:29','2014-08-07 13:58:47');
INSERT INTO `launch` VALUES ('build',102,'trakload','register point loader','','/home/rds/bin','/bin/bash','rpload',0,0,-1,'manual','idle',0,'no','2014-08-04 19:09:51','2014-08-07 13:52:56');
INSERT INTO `launch` VALUES ('build',103,'traktrace','trak tracing','','/home/rds/bin','traktrace','',0,0,-1,'manual','idle',0,'no','2014-08-07 08:52:28','2014-08-07 13:58:47');
/*!40000 ALTER TABLE `launch` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-03-30 11:43:06
