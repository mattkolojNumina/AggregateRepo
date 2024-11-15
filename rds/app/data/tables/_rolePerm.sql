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
-- Table structure for table `_rolePerm`
--

DROP TABLE IF EXISTS `_rolePerm`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `_rolePerm` (
  `role` char(64) NOT NULL DEFAULT '',
  `perm` char(64) NOT NULL DEFAULT '',
  `stamp` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`role`,`perm`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `_rolePerm`
--
-- ORDER BY:  `role`,`perm`

LOCK TABLES `_rolePerm` WRITE;
/*!40000 ALTER TABLE `_rolePerm` DISABLE KEYS */;
INSERT INTO `_rolePerm` VALUES ('admin','adminLogView','2019-08-14 01:10:04');
INSERT INTO `_rolePerm` VALUES ('admin','controlEdit','2017-01-27 18:52:42');
INSERT INTO `_rolePerm` VALUES ('admin','controlView','2017-01-27 18:52:42');
INSERT INTO `_rolePerm` VALUES ('admin','deviceView','2019-08-14 04:18:08');
INSERT INTO `_rolePerm` VALUES ('admin','diagView','2019-08-14 01:10:04');
INSERT INTO `_rolePerm` VALUES ('admin','docView','2019-08-14 01:43:30');
INSERT INTO `_rolePerm` VALUES ('admin','eventEdit','2017-01-27 18:52:42');
INSERT INTO `_rolePerm` VALUES ('admin','eventView','2017-01-27 18:52:42');
INSERT INTO `_rolePerm` VALUES ('admin','notifyEdit','2019-08-14 01:10:04');
INSERT INTO `_rolePerm` VALUES ('admin','notifyView','2019-08-14 01:10:04');
INSERT INTO `_rolePerm` VALUES ('admin','prodConfig','2019-08-14 01:10:04');
INSERT INTO `_rolePerm` VALUES ('admin','prodView','2019-08-14 01:10:04');
INSERT INTO `_rolePerm` VALUES ('admin','reportEdit','2019-08-14 01:10:04');
INSERT INTO `_rolePerm` VALUES ('admin','reportView','2019-08-14 01:10:04');
INSERT INTO `_rolePerm` VALUES ('admin','systemEdit','2019-08-14 01:10:04');
INSERT INTO `_rolePerm` VALUES ('admin','systemView','2019-08-14 01:10:04');
INSERT INTO `_rolePerm` VALUES ('admin','tuningEdit','2019-08-14 01:10:04');
INSERT INTO `_rolePerm` VALUES ('admin','tuningView','2019-08-14 01:10:04');
INSERT INTO `_rolePerm` VALUES ('admin','userEdit','2017-01-27 18:52:42');
INSERT INTO `_rolePerm` VALUES ('admin','userFire','2017-01-27 18:52:42');
INSERT INTO `_rolePerm` VALUES ('admin','userKill','2017-01-27 18:52:42');
INSERT INTO `_rolePerm` VALUES ('admin','userView','2017-01-27 18:52:42');
INSERT INTO `_rolePerm` VALUES ('admin','voiceEdit','2019-08-14 01:10:04');
INSERT INTO `_rolePerm` VALUES ('admin','voiceView','2019-08-14 01:10:04');
INSERT INTO `_rolePerm` VALUES ('picker','diagView','2019-08-14 03:51:35');
INSERT INTO `_rolePerm` VALUES ('picker','docView','2019-08-14 03:51:35');
INSERT INTO `_rolePerm` VALUES ('picker','eventView','2019-08-14 03:51:35');
INSERT INTO `_rolePerm` VALUES ('picker','reportView','2019-08-14 03:51:35');
INSERT INTO `_rolePerm` VALUES ('rds','root','2019-08-13 23:49:30');
/*!40000 ALTER TABLE `_rolePerm` ENABLE KEYS */;
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
