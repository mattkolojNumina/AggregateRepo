-- MariaDB dump 10.19  Distrib 10.6.16-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: rds
-- ------------------------------------------------------
-- Server version	10.6.16-MariaDB-0ubuntu0.22.04.1-log

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
-- Table structure for table `_menu`
--

DROP TABLE IF EXISTS `_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `_menu` (
  `title` char(32) NOT NULL,
  `block` int(11) NOT NULL DEFAULT 0,
  `ordinal` int(11) NOT NULL DEFAULT 0,
  `link` char(64) NOT NULL DEFAULT '',
  `icon` char(64) NOT NULL DEFAULT '',
  `color` char(64) NOT NULL DEFAULT '',
  `description` char(255) NOT NULL DEFAULT '',
  `perm` char(64) NOT NULL DEFAULT '',
  `stamp` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `openInNewTab` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`title`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `_menu`
--
-- ORDER BY:  `title`

LOCK TABLES `_menu` WRITE;
/*!40000 ALTER TABLE `_menu` DISABLE KEYS */;
INSERT INTO `_menu` VALUES ('Controls',2,0,'#/control','settings.svg','#6600cc','View and set system parameters.','controlView','2019-05-02 22:36:32',0);
INSERT INTO `_menu` VALUES ('Devices',4,0,'#/device','computer.svg','#AABBCC','Devices','deviceView','2019-08-14 04:17:45',0);
INSERT INTO `_menu` VALUES ('Diagnostics',1,1,'#/diag?rds=demo','scope.svg','#ee3300','Graphical diagnostics','diagView','2020-01-30 16:46:32',1);
INSERT INTO `_menu` VALUES ('Docs',3,0,'#/doc','books.svg','#404050','Documentation.','docView','2019-08-14 01:42:46',0);
INSERT INTO `_menu` VALUES ('Events',1,0,'#/event','ring.svg','#ff0066','System and process events and alerts','eventView','2020-01-30 16:46:30',0);
INSERT INTO `_menu` VALUES ('Notify',1,2,'#/notify','notify.svg','#990022','Maintain notifications.','notifyView','2019-08-14 01:38:49',0);
INSERT INTO `_menu` VALUES ('Reports',1,6,'#/report','report.svg','#009900','View system reports.','reportView','2019-08-14 03:47:11',0);
INSERT INTO `_menu` VALUES ('System',3,0,'#/system','power.svg','#0033cc','Monitor, start, and stop processes and systems.','systemView','2019-05-02 22:39:40',0);
INSERT INTO `_menu` VALUES ('Trak',2,1,'#/trak','railway.svg','#C02020','Trak Viewer','trakView','2020-01-03 19:56:51',0);
INSERT INTO `_menu` VALUES ('Tuning',2,0,'#/tuning','dial.svg','#882266','View and set tuning parameters','tuningView','2019-05-02 22:40:18',0);
INSERT INTO `_menu` VALUES ('Users',3,0,'#/user','people.svg','#009933','Maintain user and group permissions.','userView','2019-05-02 22:34:38',0);
/*!40000 ALTER TABLE `_menu` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-06-05 13:37:47
