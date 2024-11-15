-- MariaDB dump 10.19  Distrib 10.6.11-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: rds
-- ------------------------------------------------------
-- Server version	10.6.11-MariaDB-0ubuntu0.22.04.1-log

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
-- Table structure for table `fmsRobots`
--

DROP TABLE IF EXISTS `fmsRobots`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fmsRobots` (
  `id` int(11) NOT NULL,
  `isConnected` enum('true','false','err') NOT NULL DEFAULT 'false',
  `state` int(11) NOT NULL DEFAULT 0,
  `battery` double NOT NULL DEFAULT 0,
  `currentTask` char(64) NOT NULL DEFAULT '',
  `sourceLocation` char(64) NOT NULL DEFAULT '',
  `destinationLocation` char(64) NOT NULL DEFAULT '',
  `number` int(11) NOT NULL DEFAULT 0,
  `type` char(32) NOT NULL DEFAULT '',
  `robotName` char(32) NOT NULL DEFAULT '',
  `viewName` char(32) NOT NULL DEFAULT '',
  `x` double NOT NULL DEFAULT 0,
  `y` double NOT NULL DEFAULT 0,
  `z` double NOT NULL DEFAULT 0,
  `w` double NOT NULL DEFAULT 0,
  `workHours` char(32) NOT NULL DEFAULT '',
  `description` char(128) NOT NULL DEFAULT '',
  `stamp` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fmsRobots`
--

LOCK TABLES `fmsRobots` WRITE;
/*!40000 ALTER TABLE `fmsRobots` DISABLE KEYS */;
INSERT INTO `fmsRobots` VALUES (0,'true',5,73.93333333333761,'waitspot_2','','',1,'amr','193.193.193.20','AMR 1',105.20531463623047,8.866056442260742,0.0017869738354413982,0.9999984033609811,'24 Hours','The robot is in Idle state','2022-09-28 12:06:32');
/*!40000 ALTER TABLE `fmsRobots` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-12-08 10:53:58
