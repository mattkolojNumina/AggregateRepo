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
-- Table structure for table `adminPermissions`
--

DROP TABLE IF EXISTS `adminPermissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `adminPermissions` (
  `action` char(64) NOT NULL DEFAULT '',
  `description` char(64) NOT NULL DEFAULT '',
  `level` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`action`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `adminPermissions`
--
-- ORDER BY:  `action`

LOCK TABLES `adminPermissions` WRITE;
/*!40000 ALTER TABLE `adminPermissions` DISABLE KEYS */;
INSERT INTO `adminPermissions` VALUES ('advanced process control','Modify processes and execute custom commands',7);
INSERT INTO `adminPermissions` VALUES ('calibrate dimensioner','Modify the dimensioner calibration settings',7);
INSERT INTO `adminPermissions` VALUES ('default action','The action level for unspecified actions',5);
INSERT INTO `adminPermissions` VALUES ('edit events','Modify event descriptions and settings',5);
INSERT INTO `adminPermissions` VALUES ('edit tables','Modify database tables',5);
INSERT INTO `adminPermissions` VALUES ('manage actions','Create, modify, or delete access control actions',9);
INSERT INTO `adminPermissions` VALUES ('manage users','Create, modify, or delete user accounts',9);
INSERT INTO `adminPermissions` VALUES ('modify control parameters','Update program-specific settings',9);
INSERT INTO `adminPermissions` VALUES ('modify trak io','Change the state of inputs and outputs',9);
INSERT INTO `adminPermissions` VALUES ('modify trak parameters','Update low-level tuning parameters',7);
INSERT INTO `adminPermissions` VALUES ('process control','Control system processes and restart/reboot',5);
INSERT INTO `adminPermissions` VALUES ('update trak io','Manually control inputs and outputs',9);
INSERT INTO `adminPermissions` VALUES ('view access control','Access the administrative controls panel',9);
INSERT INTO `adminPermissions` VALUES ('view control parameters','Access the control parameters panel',7);
INSERT INTO `adminPermissions` VALUES ('view report editor','Access the report editor panel',5);
INSERT INTO `adminPermissions` VALUES ('view trace messages','Access the trace messages panel',7);
INSERT INTO `adminPermissions` VALUES ('view tuning parameters','Access the tuning parameters panel',7);
/*!40000 ALTER TABLE `adminPermissions` ENABLE KEYS */;
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
