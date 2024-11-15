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
-- Table structure for table `notificationCarriers`
--

DROP TABLE IF EXISTS `notificationCarriers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `notificationCarriers` (
  `carrier` char(32) NOT NULL,
  `domain` char(32) NOT NULL DEFAULT '',
  `stamp` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`carrier`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notificationCarriers`
--
-- ORDER BY:  `carrier`

LOCK TABLES `notificationCarriers` WRITE;
/*!40000 ALTER TABLE `notificationCarriers` DISABLE KEYS */;
INSERT INTO `notificationCarriers` VALUES ('alltel','message.alltel.com','2015-11-14 20:52:52');
INSERT INTO `notificationCarriers` VALUES ('att','txt.att.net','2015-11-14 20:52:52');
INSERT INTO `notificationCarriers` VALUES ('boostmobile','myboostmobile.com','2015-11-14 20:52:52');
INSERT INTO `notificationCarriers` VALUES ('cricket','sms.mycricket.com','2015-11-14 20:52:52');
INSERT INTO `notificationCarriers` VALUES ('nextel','messaging.nextel.com','2015-11-14 20:52:52');
INSERT INTO `notificationCarriers` VALUES ('ptel','ptel.com','2015-11-14 20:52:52');
INSERT INTO `notificationCarriers` VALUES ('qwest','qwestmp.com','2015-11-14 20:52:52');
INSERT INTO `notificationCarriers` VALUES ('sprint','messaging.sprintpcs.com','2015-11-14 20:52:52');
INSERT INTO `notificationCarriers` VALUES ('suncom','tms.suncom.com','2015-11-14 20:52:52');
INSERT INTO `notificationCarriers` VALUES ('tmobile','tmomail.net','2015-11-14 20:52:52');
INSERT INTO `notificationCarriers` VALUES ('uscellular','email.uscc.net','2015-11-14 20:52:52');
INSERT INTO `notificationCarriers` VALUES ('verizon','vtext.com','2015-11-14 20:52:52');
INSERT INTO `notificationCarriers` VALUES ('virginmobile','vmobl.com','2015-11-14 20:52:52');
/*!40000 ALTER TABLE `notificationCarriers` ENABLE KEYS */;
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
