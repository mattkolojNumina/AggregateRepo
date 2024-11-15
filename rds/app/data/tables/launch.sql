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
-- Table structure for table `launch`
--

DROP TABLE IF EXISTS `launch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `launch` (
  `host` char(32) NOT NULL DEFAULT 'localhost',
  `ordinal` int(11) NOT NULL DEFAULT 0,
  `nickName` char(32) NOT NULL DEFAULT '',
  `displayName` char(64) NOT NULL DEFAULT '',
  `traceName` char(8) NOT NULL DEFAULT '',
  `home` char(64) NOT NULL DEFAULT '',
  `process` char(32) NOT NULL DEFAULT '',
  `args` char(255) NOT NULL DEFAULT '',
  `delayAfter` int(11) NOT NULL DEFAULT 0,
  `termDelay` int(11) NOT NULL DEFAULT 0,
  `pid` int(11) NOT NULL DEFAULT -1,
  `mode` enum('daemon','manual','startonce') NOT NULL DEFAULT 'manual',
  `operation` enum('trigger','idle') NOT NULL DEFAULT 'idle',
  `count` int(11) NOT NULL DEFAULT 0,
  `throttled` enum('yes','no') NOT NULL DEFAULT 'no',
  `lastStart` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `stamp` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
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
INSERT INTO `launch` VALUES ('build',1,'trace','trace logger','','/home/rds/bin','trn_logd','/home/rds/log/trn.log',0,0,1108,'daemon','idle',1,'no','2022-07-15 06:24:48','2022-07-15 11:24:48');
INSERT INTO `launch` VALUES ('build',2,'ctr','counter daemon','','/home/rds/bin','ctrd','',0,0,1109,'daemon','idle',1,'no','2022-07-15 06:24:48','2022-07-15 11:24:48');
INSERT INTO `launch` VALUES ('build',3,'evt','eventing daemon','','/home/rds/bin','evtd','',0,0,1110,'daemon','idle',1,'no','2022-07-15 06:24:48','2022-07-15 11:24:48');
INSERT INTO `launch` VALUES ('build',4,'history','history daemon','','/home/rds/bin','histd','',0,0,1111,'daemon','idle',1,'no','2022-07-15 06:24:48','2022-07-15 11:24:48');
INSERT INTO `launch` VALUES ('build',9,'notifier','notificer','notifier','/home/rds/bin','java','notifier.Notifier',0,0,-1,'manual','idle',0,'no','0000-00-00 00:00:00','2021-01-08 22:31:57');
INSERT INTO `launch` VALUES ('build',10,'execute','command execution','','/home/rds/bin','execute','',0,0,1112,'daemon','idle',1,'no','2022-07-15 06:24:48','2022-07-15 11:24:48');
INSERT INTO `launch` VALUES ('build',99,'web server','web server','web','/home/rds/app/www/ui','node','/home/rds/app/www/ui/bin/www',0,0,-1,'manual','idle',0,'no','0000-00-00 00:00:00','2021-01-08 22:32:32');
INSERT INTO `launch` VALUES ('build',100,'trak','trak loader','trak','/home/rds/app/bin','/bin/bash','rc.trak',30,0,-1,'manual','idle',0,'no','0000-00-00 00:00:00','2021-01-08 22:31:57');
INSERT INTO `launch` VALUES ('build',101,'trakd','trak daemon','','/home/rds/bin','trakd','',1,0,-1,'manual','idle',0,'no','0000-00-00 00:00:00','2021-01-08 22:31:57');
INSERT INTO `launch` VALUES ('build',102,'trakload','register point loader','','/home/rds/bin','/bin/bash','rpload',0,0,-1,'manual','idle',0,'no','0000-00-00 00:00:00','2021-01-08 22:31:57');
INSERT INTO `launch` VALUES ('build',103,'traktrace','trak tracing','','/home/rds/bin','traktrace','',0,0,-1,'manual','idle',0,'no','0000-00-00 00:00:00','2021-01-08 22:31:57');
INSERT INTO `launch` VALUES ('build',110,'engine','trak engine','','/home/rds/bin','engine','',1,0,-1,'manual','idle',0,'no','0000-00-00 00:00:00','2021-01-08 22:31:57');
INSERT INTO `launch` VALUES ('build',111,'ecattrak','trak ethercat driver','','/home/rds/bin','ecattrak','',1,0,-1,'manual','idle',0,'no','0000-00-00 00:00:00','2021-01-08 22:31:57');
INSERT INTO `launch` VALUES ('build',112,'linxtrak','id linx driver','','/home/rds/bin','linxtrak','192.168.1.100',1,0,-1,'manual','idle',0,'no','0000-00-00 00:00:00','2021-01-08 22:31:57');
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

-- Dump completed on 2022-07-17  0:01:02
