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
-- Table structure for table `runtime`
--

DROP TABLE IF EXISTS `runtime`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `runtime` (
  `name` char(255) NOT NULL DEFAULT '',
  `value` char(255) NOT NULL DEFAULT '',
  `stamp` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `runtime`
--
-- ORDER BY:  `name`

LOCK TABLES `runtime` WRITE;
/*!40000 ALTER TABLE `runtime` DISABLE KEYS */;
INSERT INTO `runtime` VALUES ('/build/app_daily','2024-06-04 04:00:02','2024-06-04 04:00:02');
INSERT INTO `runtime` VALUES ('/build/app_hourly','2024-06-04 18:00:01','2024-06-04 18:00:01');
INSERT INTO `runtime` VALUES ('/build/app_weekly','2022-07-10 00:01:03','2022-07-10 05:01:03');
INSERT INTO `runtime` VALUES ('/build/rds_daily','2024-06-04 03:00:01','2024-06-04 03:00:01');
INSERT INTO `runtime` VALUES ('/glb-trak1/app_daily','2024-06-05 09:00:02','2024-06-05 09:00:02');
INSERT INTO `runtime` VALUES ('/glb-trak1/app_hourly','2024-06-05 10:00:01','2024-06-05 15:00:01');
INSERT INTO `runtime` VALUES ('/glb-trak1/rds_daily','2024-06-05 08:00:01','2024-06-05 08:00:01');
INSERT INTO `runtime` VALUES ('webstamp','2024-06-05T15:18:23.059Z','2024-06-05 15:18:23');
/*!40000 ALTER TABLE `runtime` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-06-05 13:37:48
