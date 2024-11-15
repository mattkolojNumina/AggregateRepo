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
-- Table structure for table `fmsTasks`
--

DROP TABLE IF EXISTS `fmsTasks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fmsTasks` (
  `seq` int(11) NOT NULL AUTO_INCREMENT,
  `fleetType` char(32) NOT NULL DEFAULT '',
  `taskId` char(32) NOT NULL DEFAULT '',
  `priority` int(11) NOT NULL DEFAULT 0,
  `sourceLocation` char(32) DEFAULT '',
  `destinationLocation` char(32) DEFAULT NULL,
  `deadline` char(32) DEFAULT NULL,
  `dependsOn` char(32) DEFAULT NULL,
  `baggage` char(32) DEFAULT NULL,
  `taskType` enum('LOAD','MOVE','UNLOAD','COMPLETE','END') NOT NULL DEFAULT 'MOVE',
  `sent` enum('no','yes','err') NOT NULL DEFAULT 'no',
  `robotId` char(32) NOT NULL DEFAULT '',
  `robotState` int(11) NOT NULL DEFAULT 0,
  `taskStatus` int(11) NOT NULL DEFAULT 0,
  `subtaskStatus` int(11) NOT NULL DEFAULT 0,
  `stamp` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`seq`),
  KEY `sent` (`sent`),
  KEY `fleetType` (`fleetType`),
  KEY `taskId` (`taskId`,`sourceLocation`,`destinationLocation`)
) ENGINE=InnoDB AUTO_INCREMENT=1356 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fmsTasks`
--

LOCK TABLES `fmsTasks` WRITE;
/*!40000 ALTER TABLE `fmsTasks` DISABLE KEYS */;
/* INSERT INTO `fmsTasks` VALUES (1355,'5',1,'L1',NULL,'2023-12-07 16:21:54',NULL,NULL,'LOAD','yes','amr01',0,1,0,'2022-12-07 22:22:31'); */
/*!40000 ALTER TABLE `fmsTasks` ENABLE KEYS */;
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
