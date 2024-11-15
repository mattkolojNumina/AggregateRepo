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
-- Table structure for table `dashboard`
--

DROP TABLE IF EXISTS `dashboard`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dashboard` (
  `id` int(11) NOT NULL DEFAULT '0',
  `zone` char(32) NOT NULL DEFAULT '',
  `object` char(64) NOT NULL DEFAULT '',
  `params` char(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`,`zone`),
  KEY `zone_object` (`zone`,`object`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dashboard`
--
-- ORDER BY:  `id`,`zone`

LOCK TABLES `dashboard` WRITE;
/*!40000 ALTER TABLE `dashboard` DISABLE KEYS */;
INSERT INTO `dashboard` VALUES (11,'panel','rdsDashboard.panel.EventPanel','');
INSERT INTO `dashboard` VALUES (51,'panel','rdsDashboard.panel.ReportPanel','');
INSERT INTO `dashboard` VALUES (99,'panel','rdsDashboard.panel.LinkPanel','');
INSERT INTO `dashboard` VALUES (501,'panel','rdsDashboard.panel.ReportEditorPanel','');
INSERT INTO `dashboard` VALUES (599,'panel','rdsDashboard.panel.ProcessPanel','');
INSERT INTO `dashboard` VALUES (701,'panel','rdsDashboard.panel.ConfigPanel','');
INSERT INTO `dashboard` VALUES (702,'panel','rdsDashboard.panel.TuningPanel','');
INSERT INTO `dashboard` VALUES (799,'panel','rdsDashboard.panel.TracePanel','');
INSERT INTO `dashboard` VALUES (999,'panel','rdsDashboard.panel.AdminPanel','');
INSERT INTO `dashboard` VALUES (1000,'panel','rdsDashboard.panel.InputOutputPanel','');
/*!40000 ALTER TABLE `dashboard` ENABLE KEYS */;
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
