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
-- Table structure for table `_perm`
--

DROP TABLE IF EXISTS `_perm`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `_perm` (
  `perm` char(64) NOT NULL,
  `description` char(80) NOT NULL DEFAULT '',
  `enforced` enum('yes','no') DEFAULT 'yes',
  `stamp` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`perm`),
  KEY `enforced` (`enforced`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `_perm`
--
-- ORDER BY:  `perm`

LOCK TABLES `_perm` WRITE;
/*!40000 ALTER TABLE `_perm` DISABLE KEYS */;
INSERT INTO `_perm` VALUES ('adminLogView','view change logs','yes','2019-08-14 00:33:41');
INSERT INTO `_perm` VALUES ('always','actions always allowed','no','2019-08-13 23:21:16');
INSERT INTO `_perm` VALUES ('controlEdit','edit control parameters','yes','2019-08-14 00:36:15');
INSERT INTO `_perm` VALUES ('controlView','view control parameters','yes','2019-08-14 00:36:11');
INSERT INTO `_perm` VALUES ('deviceView','view devices','yes','2019-08-14 04:17:58');
INSERT INTO `_perm` VALUES ('diagView','view graphical diagnostics','no','2017-03-03 18:55:20');
INSERT INTO `_perm` VALUES ('docView','view documents','yes','2019-08-14 01:43:09');
INSERT INTO `_perm` VALUES ('eventEdit','edit events','yes','2019-08-14 00:36:30');
INSERT INTO `_perm` VALUES ('eventView','view events','no','2017-01-18 17:56:26');
INSERT INTO `_perm` VALUES ('never','actions never allowed','yes','2019-08-13 23:21:16');
INSERT INTO `_perm` VALUES ('notifyEdit','edit notifications','yes','2019-08-14 00:36:32');
INSERT INTO `_perm` VALUES ('notifyView','view notifications','no','2017-05-22 16:48:22');
INSERT INTO `_perm` VALUES ('prodConfig','configure productivity','yes','2019-08-14 00:35:42');
INSERT INTO `_perm` VALUES ('prodView','view productivity reports','no','2017-03-07 21:53:35');
INSERT INTO `_perm` VALUES ('reportEdit','create and edit reports','no','2017-05-22 16:55:09');
INSERT INTO `_perm` VALUES ('reportView','view reports','no','2017-03-07 21:53:45');
INSERT INTO `_perm` VALUES ('root','UNLIMITED POWER *mad cackling*','yes','2019-08-13 23:21:16');
INSERT INTO `_perm` VALUES ('systemEdit','edit system processes','yes','2019-08-14 00:36:50');
INSERT INTO `_perm` VALUES ('systemView','view system processes','yes','2019-08-14 00:36:53');
INSERT INTO `_perm` VALUES ('trakView','view trak values','no','2020-01-03 19:54:57');
INSERT INTO `_perm` VALUES ('tuningEdit','edit tuning parameters','yes','2019-08-14 00:36:54');
INSERT INTO `_perm` VALUES ('tuningView','view tuning parameters','yes','2019-08-14 00:36:56');
INSERT INTO `_perm` VALUES ('userEdit','edit users','yes','2017-05-24 19:28:47');
INSERT INTO `_perm` VALUES ('userView','view users','yes','2017-05-24 19:28:36');
INSERT INTO `_perm` VALUES ('voiceEdit','edit voice operators','yes','2019-08-14 00:37:01');
INSERT INTO `_perm` VALUES ('voiceView','view voice operators details and history','no','2017-03-07 21:53:13');
/*!40000 ALTER TABLE `_perm` ENABLE KEYS */;
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
