-- MySQL dump 10.16  Distrib 10.1.34-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: rds
-- ------------------------------------------------------
-- Server version	10.1.34-MariaDB-0ubuntu0.18.04.1

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
  `block` int(11) NOT NULL DEFAULT '0',
  `ordinal` int(11) NOT NULL DEFAULT '0',
  `link` char(64) NOT NULL DEFAULT '',
  `icon` char(64) NOT NULL DEFAULT '',
  `color` char(64) NOT NULL DEFAULT '',
  `description` char(255) NOT NULL DEFAULT '',
  `perm` char(64) NOT NULL DEFAULT '',
  `stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`title`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `_menu`
--

LOCK TABLES `_menu` WRITE;
/*!40000 ALTER TABLE `_menu` DISABLE KEYS */;
INSERT INTO `_menu` VALUES ('Users',3,0,'#/user','people.svg','#009933','Maintain user and group permissions.','userView','2019-05-02 21:34:38'),('Config',2,0,'#/config','settings_by_freepik.svg','#4169E1','Review, edit, or update configurations.','configView','2019-05-02 21:35:37'),('Controls',2,0,'#/control','settings.svg','#6600cc','View and set system parameters.','controlView','2019-05-02 21:36:32'),('Production',1,3,'#/prod','report.svg','#999966','See picker productivity','prodView','2019-05-02 22:05:13'),('ProdConfig',2,0,'#/prodConfig','options.svg','#999966','Configure production users and standards.','prodEdit','2019-05-02 21:38:41'),('System',3,0,'#/system','power.svg','#0033cc','Monitor, start, and stop processes and systems.','systemView','2019-05-02 21:39:40'),('Tuning',2,0,'#/tuning','dial.svg','#882266','View and set tuning parameters','tuningView','2019-05-02 21:40:18'),('Events',1,0,'#/event','ring.svg','#ff0066','System and process events and alerts','eventView','2019-05-02 21:41:28'),('Diagnostics',1,1,'#/diag?rds=candy','scope.svg','#ee3300','Graphical diagnostics','diagView','2019-05-02 22:04:40'),('Waves',0,5,'#/wave','sea.svg','#204090','View and manage SpeedPick and BulkPick','waveView','2019-05-02 22:09:42'),('Demand',0,4,'#/demand','package.svg','#406080','View the number of cartons required to complete current orders.','ordersView','2019-05-02 22:09:33'),('Lookup',0,3,'#/cartonDetails','box.svg','#806040','View the details of a carton.','cartonView','2019-05-02 22:09:29'),('Release',0,0,'#/release','play.svg','#208020','Release downloaded orders to start picking.','releaseView','2019-05-02 21:45:13'),('Carts',0,2,'#/carts','grids.svg','#DD853F','View carts and their contents.','cartsView','2019-05-02 22:09:25'),('Voice',1,2,'#/voice','voicepick.svg','#1E90FF','Review and lookup voice operators.','voiceView','2019-05-02 22:04:56'),('Orders',0,1,'#/orders','item.svg','#A04040','View and manage orders.','ordersView','2019-05-02 22:09:22'),('Recent',0,6,'#/cartonHistory','conveyor.svg','#408040','View current processing at the print-apply.','cartonView','2019-05-02 22:09:44'),('Replenish',1,0,'#/replen','restock.svg','#0000A0','Items needing replenishment','replenView','2019-05-02 21:49:47');
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

-- Dump completed on 2019-05-02 17:15:16
-- MySQL dump 10.16  Distrib 10.1.34-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: rds
-- ------------------------------------------------------
-- Server version	10.1.34-MariaDB-0ubuntu0.18.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `_db`
--
-- WHERE:  topic='main'

LOCK TABLES `_db` WRITE;
/*!40000 ALTER TABLE `_db` DISABLE KEYS */;
REPLACE INTO `_db` VALUES ('main','all','reader','SELECT *\r\nFROM _menu\r\nORDER BY ordinal ;','','ui','2019-05-02 22:13:05');
/*!40000 ALTER TABLE `_db` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-05-02 17:15:33
