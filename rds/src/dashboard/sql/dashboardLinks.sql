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
-- Table structure for table `dashboardLinks`
--

DROP TABLE IF EXISTS `dashboardLinks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dashboardLinks` (
  `ordinal` int(11) NOT NULL DEFAULT '0',
  `type` enum('header','link','separator') NOT NULL DEFAULT 'link',
  `title` char(32) NOT NULL DEFAULT '',
  `description` varchar(255) NOT NULL DEFAULT '',
  `link` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`ordinal`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dashboardLinks`
--
-- ORDER BY:  `ordinal`

LOCK TABLES `dashboardLinks` WRITE;
/*!40000 ALTER TABLE `dashboardLinks` DISABLE KEYS */;
INSERT INTO `dashboardLinks` VALUES (11,'header','Documentation','','');
INSERT INTO `dashboardLinks` VALUES (12,'link','User\'s Guide','Guide to the RDS system and the web-based dashboard','/download/guide.pdf');
INSERT INTO `dashboardLinks` VALUES (20,'separator','','','');
INSERT INTO `dashboardLinks` VALUES (21,'header','Administration','','');
INSERT INTO `dashboardLinks` VALUES (22,'link','Database Admin','Database maintenance tool','/rds/pma/index.php');
/*!40000 ALTER TABLE `dashboardLinks` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-03-30 11:55:01
