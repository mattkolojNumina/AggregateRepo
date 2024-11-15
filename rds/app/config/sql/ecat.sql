-- MySQL dump 10.13  Distrib 5.1.35, for pc-linux-gnu (i686)
--
-- Host: db    Database: rds
-- ------------------------------------------------------
-- Server version	5.1.35-enterprise-commercial-classic

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
-- Table structure for table `ethercatDevices`
--

DROP TABLE IF EXISTS `ethercatDevices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ethercatDevices` (
  `vendor` char(48) NOT NULL DEFAULT '',
  `vendorID` int(11) NOT NULL DEFAULT '0',
  `device` char(24) NOT NULL DEFAULT '',
  `deviceCode` int(11) DEFAULT NULL,
  `deviceAddress` int(11) DEFAULT NULL,
  `deviceType` char(24) DEFAULT NULL,
  `pointCount` int(11) NOT NULL DEFAULT '2',
  `description` char(80) DEFAULT NULL,
  `stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`vendor`,`vendorID`,`device`),
  KEY `NewIndex1` (`vendorID`,`deviceCode`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ethercatDevices`
--

LOCK TABLES `ethercatDevices` WRITE;
/*!40000 ALTER TABLE `ethercatDevices` DISABLE KEYS */;
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL5001',327757906,6144,'Measuring',1,'EL5001 1Ch. SSI Encoder','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL5001-0010',327757906,6144,'Measuring',1,'EL5001-0010 1Ch. SSI Monitor','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL5101',334311506,6144,'Measuring',1,'EL5101-1001 1Ch. Encoder 5V','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL5151',337588306,6144,'Measuring',1,'EL5151 1Ch. Inc. Encoder','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3001',196685906,4096,'AnaIn',2,'EL3001 1Ch. Ana. Input +/-10V','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3002',196751442,4096,'AnaIn',4,'EL3002 2Ch. Ana. Input +/-10V','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3004',196882514,4096,'AnaIn',8,'EL3004 4Ch. Ana. Input +/-10V','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3008',197144658,4096,'AnaIn',16,'EL3008 8Ch. Ana. Input +/-10V','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3041',199307346,4096,'AnaIn',2,'EL3041 1Ch. Ana. Input 0-20mA','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3042',199372882,4096,'AnaIn',4,'EL3042 2Ch. Ana. Input 0-20mA','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3044',199503954,4096,'AnaIn',8,'EL3044 4Ch. Ana. Input 0-20mA','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3048',199766098,4096,'AnaIn',16,'EL3048 8Ch. Ana. Input 0-20mA','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3051',199962706,4096,'AnaIn',2,'EL3051 1Ch. Ana. Input 4-20mA','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3052',200028242,4096,'AnaIn',4,'EL3052 2Ch. Ana. Input 4-20mA','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3054',200159314,4096,'AnaIn',8,'EL3054 4Ch. Ana. Input 4-20mA','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3058',200421458,4096,'AnaIn',16,'EL3058 8Ch. Ana. Input 4-20mA','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3061',200618066,4096,'AnaIn',2,'EL3061 1Ch. Ana. Input 0-10V','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3062',200683602,4096,'AnaIn',4,'EL3062 2Ch. Ana. Input 0-10V','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3062-0030',200683602,4096,'AnaIn',4,'EL3062-0030 2Ch. Ana. Input 0-30 V','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3064',200814674,4096,'AnaIn',8,'EL3064 4Ch. Ana. Input 0-10V','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3068',201076818,4096,'AnaIn',16,'EL3068 8Ch. Ana. Input 0-10V','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3102',203305042,6144,'AnaIn',2,'EL3102 2Ch. Ana. Input +/-10V, DIFF','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3112',203960402,6144,'AnaIn',2,'EL3112 2Ch. Ana. Input 0-20mA, DIFF','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3122',204615762,6144,'AnaIn',2,'EL3122 2Ch. Ana. Input 4-20mA, DIFF','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3142',205926482,6144,'AnaIn',2,'EL3142 2Ch. Ana. Input 0-20mA','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3142-0010',205926482,6144,'AnaIn',3,'EL3142-0010 2Ch. Ana. Input +/-10mA','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3152',206581842,6144,'AnaIn',2,'EL3152 2Ch. Ana. Input 4-20mA','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3162',207237202,6144,'AnaIn',2,'EL3162 2Ch. Ana. Input 0-10V','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3201',209793106,4096,'AnaIn',1,'EL3201 1Ch. Ana. Input PT100 (RTD)','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3201-0010',209793106,4096,'AnaIn',1,'EL3201-0010 1Ch. Ana. Input PT100 (RTD), High Precision','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3201-0020',209793106,4096,'AnaIn',1,'EL3201-0020 1Ch. Ana. Input PT100 (RTD), High Precision, calibrated','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3202',209858642,4096,'AnaIn',2,'EL3202 2Ch. Ana. Input PT100 (RTD)','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3204',209989714,4096,'AnaIn',4,'EL3204 4Ch. Ana. Input PT100 (RTD)','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3311',217002066,4096,'AnaIn',1,'EL3311 1Ch. Ana. Input Thermocouple (TC)','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3312',217067602,4096,'AnaIn',2,'EL3312 2Ch. Ana. Input Thermocouple (TC)','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3314',217198674,4096,'AnaIn',4,'EL3314 4Ch. Ana. Input Thermocouple (TC)','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3403',223031378,4096,'AnaIn',3,'EL3403 3Ch. Power Measuring','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3403-0010',223031378,4096,'AnaIn',3,'EL3403-0010 3Ch. Power Measuring, 5A','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3602',236073042,4096,'AnaIn',2,'EL3602 2Ch. Ana. Input +/-10Volt, Diff. 24bit','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3602-0010',236073042,4096,'AnaIn',2,'EL3602-0010 2Ch. Ana. Input +/-75mV, Diff. 24bit','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3612',236728402,4096,'AnaIn',2,'EL3612 2Ch. Ana. Input 0-20 mA, Diff. 24bit','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL9800',642265170,6144,'EvaBoardDemo',4,'EL9800 8 Bit MCI-Demo with DC','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL9800 4Port',642265170,3840,'EvaBoard',4,'EL9800 32 Ch. Dig. Output (4Port, DC)','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'AX2000-B110',131096596,4362,'Drive',0,'AX2000-B110 EtherCAT Drive','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EM7004',459027538,4096,'DriveAxisModules',10,'EM7004 4-Axis Interface Unit','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL4732',310128722,4096,'AnaOutFast',4,'EL4732 2Ch. Ana. Output +/-10V, Oversample','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL9010',-1,-1,'System',0,'EL9010 End Terminal','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL9011',-1,-1,'System',0,'EL9011 End Terminal','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL9100',-1,-1,'System',0,'EL9100 Power Supplier with LED (24V)','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL9110',597045330,4096,'System',1,'EL9110 Power Supplier with LED (24V, Diagnostics)','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL9150',-1,-1,'System',0,'EL9150 Power Supplier with LED(230V)','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL9160',600322130,4096,'System',1,'EL9160 Power Supplier with LED (230V, Diagnostics)','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL9180',-1,-1,'System',0,'EL9180 Potential Connection, 2 x 24-230V, 2 x PE','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL9185',-1,-1,'System',0,'EL9185 Potential Connection, 4 x 24-230V','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL9186',-1,-1,'System',0,'EL9186 Potential Connection, 8 x 24V','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL9187',-1,-1,'System',0,'EL9187 Potential Connection, 8 x Ground','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL9190',-1,-1,'System',0,'EL9190 Power Supplier','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL9195',-1,-1,'System',0,'EL9195 Potential Connection, 2 x Shield','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL9200',-1,-1,'System',0,'EL9200 Power Supplier with LED (24V, Fuse)','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL9210',603598930,4096,'System',2,'EL9210 Power Supplier with LED (24V, Fuse, Diagnostics)','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL9250',-1,-1,'System',0,'EL9150 Power Supplier with LED(230V, Fuse)','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL9260',606875730,4096,'System',2,'EL9260 Power Supplier with LED (230V, Fuse, Diagnostics)','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL9290',-1,-1,'System',0,'EL9290 Power Supplier (Fuse)','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL9400',-1,-1,'System',0,'EL9400 E-Bus Power Supplier','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL9410',616706130,4096,'System',2,'EL9410 E-Bus Power Supplier  (Diagnostics)','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL1502',98447442,24576,'DigIn',3,'EL1502 2Ch. +/- Counter 24V, 100kHz','2009-08-11 13:59:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL1512',99102802,24576,'DigIn',3,'EL1512 2Ch. +/- Counter 24V, 1kHz','2009-08-11 13:59:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EP1008-0001',66076754,4096,'FielldbusTerminalEP',8,'EP1008-0001 8 Ch. Dig. Input 24V, 3ms, M8','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EP1008-0002',66076754,4096,'FielldbusTerminalEP',8,'EP1008-0002 8 Ch. Dig. Input 24V, 3ms, M12','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EP1018-0001',66732114,4096,'FielldbusTerminalEP',8,'EP1018-0001 8 Ch. Dig. Input 24V, 10µs, M8','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EP1018-0002',66732114,4096,'FielldbusTerminalEP',8,'EP1018-0002 8 Ch. Dig. Input 24V, 10µs, M12','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EP1258-0001',82460754,4096,'FielldbusTerminalEP',14,'EP1258-0001 8 Ch. Dig. Input 24V, 10µs, DC Latch, M8','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EP1258-0002',82460754,4096,'FielldbusTerminalEP',14,'EP1258-0002 8 Ch. Dig. Input 24V, 10µs, DC Latch, M12','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EP1816-0008',119029842,4096,'FielldbusTerminalEP',2,'EP1816-0008 16 Ch. Dig. Input 24V, 10µs, D-SUB25','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EP2001-1000',131154002,3840,'FielldbusTerminalEP',2,'EP2001-1000 8 Ch. Dig. Output 24V, 0,5A, M8','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EP2008-0001',131612754,3840,'FielldbusTerminalEP',8,'EP2008-0001 8 Ch. Dig. Output 24V, 0,5A, M8','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EP2008-0002',131612754,3840,'FielldbusTerminalEP',8,'EP2008-0002 8 Ch. Dig. Output 24V, 0,5A, M12','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EP2308-0001	',151273554,3840,'FielldbusTerminalEP',4,'EP2308-0001 4 Ch. Dig. In, 3ms, 4 Ch. Dig. Out 24V, 0,5A, M8','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EP2308-0002	',151273554,3840,'FielldbusTerminalEP',4,'EP2308-0002 4 CH. Dig. In, 3ms, 4 Ch. Dig. Out 24V, 0,5A, M12','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EP2316-0008',151797842,4096,'FielldbusTerminalEP',2,'EP2316-0008 8 Ch. Dig. In, 10µs, 8Ch. Dig. Out 24V, 0,5A, Diagnostic, D-SUB25','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EP2318-0001	',151928914,3840,'FielldbusTerminalEP',4,'EP2318-0001 4 Ch. Dig. In, 10µs, 4 Ch. Dig. Out 24V, 0,5A, M8','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EP2318-0002	',151928914,3840,'FielldbusTerminalEP',4,'EP2318-0002 4 Ch. Dig. In, 10µs, 4 Ch. Dig. Out 24V, 0,5A, M12','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EP2338-0001',153239634,3840,'FielldbusTerminalEP',8,'EP2338-0001 8 Ch. Dig. Input/Output 24V, 0,5A, M8','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EP2338-0002',153239634,3840,'FielldbusTerminalEP',8,'EP2338-0002 8 Ch. Dig. Input/Output 24V, 0,5A, M12','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EP2816-0008',184565842,4096,'FielldbusTerminalEP',3,'EP2816-0008 16 Ch. Dig. Output 24V, 0,5A, Diagnostic, D-SUB25','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EP2816-0010',184565842,4096,'FielldbusTerminalEP',3,'EP2816-0010 16 Ch. Dig. Output 24V, 0,5A, Diagnostic, D-SUB9','2009-08-10 20:34:08');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL6001',393293906,6144,'Communication',1,'EL6001 Interface (RS232) (15 Byte)','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL6021',394604626,6144,'Communication',3,'EL6021 Interface (RS422/485)','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL6080',398471250,11776,'Communication',2,'EL6080 EtherCAT Memory Terminal (128kB)','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL6224',407908434,4096,'Communication',4,'EL6224 (IO Link Master)','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL6601',432615506,6144,'Communication',0,'EL6601 1 Port Switch (Ethernet)','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL6614',433467474,4096,'Communication',0,'EL6614 4 Port Switch (Ethernet, CoE)','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL6688',438317138,4096,'Communication',3,'EL6688 - External Synchronisation Interface (IEEE1588)','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL6690',438448210,4096,'Communication',1,'EL6690 EtherCAT Bridge terminal (Secondary)','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL6692',438579282,4096,'Communication',1,'EL6692 EtherCAT Bridge terminal (Secondary)','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL6720',440414290,4096,'Communication',0,'EL6720 Lightbus Master','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL6731',441135186,6144,'Communication',0,'EL6731 Profibus Master (12 MBaud)','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL6731-0100',441135186,4096,'Communication',0,'EL6731 PROFIBUS DP Master (Free Run)','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL6731-0010',441135186,6144,'Communication',1,'EL6731-0010 Profibus Slave (12 MBaud)','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL6731-1003',441135186,4096,'Customer',0,'EL6731-1003 PROFIBUS Master','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL6740-0010',441725010,6144,'Communication',2,'EL6740 InterBus Slave','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL6751',442445906,6144,'Communication',0,'EL6751 CANopen Master','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL6751-0010',442445906,4096,'Communication',0,'EL6751-0010 CANopen Slave','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL6752',442511442,6144,'Communication',0,'EL6752 DeviceNet Master','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL6752-0010',442511442,4096,'Communication',1,'EL6752-0010 DeviceNet Slave','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL6851',448999506,4096,'Communication',9,'EL6851 DMX Master','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'BK1120',73411618,7168,'TermBk',0,'BK1120 EtherCAT Fieldbus coupler','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'AX2000-B120',131096596,4362,'Drive',1,'AX2000-B120 EtherCAT Drive (E-Bus)','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EK1100',72100946,-1,'SystemBk',0,'EK1100 EtherCAT Coupler (2A E-Bus)','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EK0000',-1,-1,'SystemBk',0,'EK0000 Unknown Coupler Terminal','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EK1110',72756306,-1,'System',0,'EK1110 EtherCAT extension','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EK1200',-1,-1,'SystemBk',0,'EK1200 E-Bus Coupler Terminal','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EK1210',-1,-1,'System',0,'EK1210 E-Bus Extension Terminal','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL0000',-1,-1,'System',0,'EL0000 Unknown Terminal','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL1002',65679442,24576,'DigIn',2,'EL1002 2Ch. Dig. Input 24V, 3ms','2009-08-11 13:59:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL1004',65810514,24576,'DigIn',4,'EL1004 4Ch. Dig. Input 24V, 3ms','2009-08-11 13:59:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL1012',66334802,24576,'DigIn',2,'EL1012 2Ch. Dig. Input 24V, 10µs','2009-08-11 13:59:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL1014',66465874,24576,'DigIn',4,'EL1014 4Ch. Dig. Input 24V, 10µs','2009-08-11 13:59:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL2002',131215442,28672,'DigOut',2,'EL2002 2Ch. Dig. Output 24V, 0.5A','2009-08-11 18:23:47');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL2004',131346514,28672,'DigOut',4,'EL2004 4Ch. Dig. Output 24V, 0.5A','2009-08-11 18:23:47');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL2032',133181522,28672,'DigOut',2,'EL2032 2Ch. Dig. Output 24V, 2A, Diagnostic','2009-08-11 18:23:47');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL4102',268841042,6144,'AnaOut',2,'EL4102 2Ch. Ana. Output  0-10V, 16bit','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL4112',269496402,6144,'AnaOut',2,'EL4112 2Ch. Ana. Output  0-20mA, 16bit','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL4132',270807122,6144,'AnaOut',2,'EL4132 2Ch. Ana. Output +/-10V, 16bit','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'FM5001',327758946,6144,'Misc',1,'FM5001-B100 Encoder','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL1004-0010',65810514,24576,'DigIn',4,'EL1004-0010 4Ch. Dig. Input 24V, isolated, 3ms','2009-08-11 13:59:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL1008',66072658,24576,'DigIn',8,'EL1008 8Ch. Dig. Input 24V, 3ms','2009-08-11 13:59:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL1014-0010',66465874,24576,'DigIn',4,'EL1014-0010 4Ch. Dig. Input 24V, isolated, 10µs','2009-08-11 13:59:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL1018',66728018,24576,'DigIn',8,'EL1018 8Ch. Dig. Input 24V, 10µs','2009-08-11 13:59:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL1024',67121234,24576,'DigIn',4,'EL1024 4Ch. Dig. Input 24V, Type 2, 3ms','2009-08-11 13:59:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL1034',67776594,24576,'DigIn',4,'EL1034 4Ch. Dig. Input 24V, potential-free, 10µs','2009-08-11 13:59:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL1084',71053394,24576,'DigIn',4,'EL1084 4Ch. Dig. Input 24V, 3ms, negative','2009-08-11 13:59:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL1088',71315538,24576,'DigIn',8,'EL1088 8Ch. Dig. Input 24V, 3ms, negative','2009-08-11 13:59:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL1094',71708754,24576,'DigIn',4,'EL1094 4Ch. Dig. Input 24V, 10µs, negative','2009-08-11 13:59:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL1098',71970898,24576,'DigIn',8,'EL1098 8Ch. Dig. Input 24V, 10µs, negative','2009-08-11 13:59:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL1104',72364114,24576,'DigIn',4,'EL1104 4Ch. Dig. Input 24V, 3ms, Sensor Power','2009-08-11 13:59:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL1114',73019474,24576,'DigIn',4,'EL1114 4Ch. Dig. Input 24V, 10µs, Sensor Power','2009-08-11 13:59:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL1124',73674834,24576,'DigIn',4,'EL1124 4Ch. Dig. Input 5V, 10µs, Sensor Power','2009-08-11 13:59:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL1134',74330194,24576,'DigIn',4,'EL1134 4Ch. Dig. Input 48V, 10µs, Sensor Power','2009-08-11 13:59:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL1144',74985554,24576,'DigIn',4,'EL1144 4Ch. Dig. Input 12V, 10µs, Sensor Power','2009-08-11 13:59:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL1202',78786642,24576,'DigIn',2,'EL1202 2Ch. Fast Dig. Input 24V, 1µs','2009-08-11 13:59:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL1202-0100',78786642,24576,'DigIn',5,'EL1202-0100 2Ch. Fast Dig. Input 24V, 1µs, DC Latch','2009-08-11 13:59:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL1252',82063442,24576,'DigIn',9,'EL1252 2Ch. Fast Dig. Input 24V, 1µs, DC Latch','2009-08-11 13:59:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL1262',82718802,24576,'DigIn',21,'EL1262 2Ch. Dig. Input 24V, 1µs, DC Oversample','2009-08-11 13:59:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL1702',111554642,24576,'DigIn',2,'EL1702 2Ch. Dig. Input 120V/230V AC, 10ms','2009-08-11 13:59:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL1712',112210002,24576,'DigIn',2,'EL1712 2Ch. Dig. Input 120V AC/DC, 10ms','2009-08-11 13:59:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL1722',112865362,24576,'DigIn',2,'EL1722 2Ch. Dig. Input 120V/230V AC, 10ms, no power contacts','2009-08-11 13:59:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'FB1111 Dig. In/Out',72812642,3842,'FB1XXX',2,'FB1111 16 Ch. Dig. In-/Output 2xMII','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'FB1111 Dig. In',72812642,4096,'FB1XXX',4,'FB1111 32 Ch. Dig. Input 2xMII','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'FB1111 Dig. Out',72812642,3840,'FB1XXX',4,'FB1111 32 Ch. Dig. Output 2xMII','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'FB1111 SPI-Slave',72812642,4096,'FB1XXX',1,'FB1111 SPI-Slave 2xMII','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'FB1111 MCI8 DC',72812642,4096,'FB1XXX',1,'FB1111 MCI8 DC 2xMII','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'FB1111 MCI16 DC',72812642,4096,'FB1XXX',1,'FB1111 MCI16 DC 2xMII','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL2008',131608658,28672,'DigOut',8,'EL2008 8Ch. Dig. Output 24V, 0.5A','2009-08-11 18:23:47');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL2022',132526162,28672,'DigOut',2,'EL2022 2Ch. Dig. Output 24V, 2A','2009-08-11 18:23:47');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL2024',132657234,28672,'DigOut',4,'EL2024 4Ch. Dig. Output 24V, 2A','2009-08-11 18:23:47');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL2024-0010',132657234,28672,'DigOut',4,'EL2024-0010 4Ch. Dig. Output 12V, 2A','2009-08-11 18:23:47');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL2034',133312594,28672,'DigOut',4,'EL2034 4Ch. Dig. Output 24V, 2A, Diagnostic','2009-08-11 18:23:47');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL2042',133836882,28672,'DigOut',2,'EL2042 2Ch. Dig. Output 24V, 4A','2009-08-11 18:23:47');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL2084',136589394,28672,'DigOut',4,'EL2084 4Ch. Dig. Output 24V, 0.5A, switching to negative','2009-08-11 18:23:47');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL2088',136851538,28672,'DigOut',8,'EL2088 8Ch. Dig. Output 24V, 0.5A, switching to negative','2009-08-11 18:23:47');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL2124',139210834,28672,'DigOut',4,'EL2124 4Ch. Dig. Output 5V, 20mA','2009-08-11 18:23:47');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL2202',144322642,28672,'DigOut',2,'EL2202 2Ch. Dig. Output 24V, 0.5A','2009-08-11 18:23:47');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL2202-0100',144322642,28672,'DigOut',2,'EL2202-0100 2Ch. Dig. Output 24V, 0.5A, DC Sync','2009-08-11 18:23:47');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL2252',147599442,28672,'DigOut',5,'EL2252 2Ch. Dig. Output 24V, 0.5A, DC Time Stamp','2009-08-11 18:23:47');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL2262',148254802,28672,'DigOut',20,'EL2262 2Ch. Dig. Output 24V, 1µs, DC Oversample','2009-08-11 18:23:47');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL2502',163983442,28672,'DigOut',4,'EL2502 2Ch. PWM Output, 24V','2009-08-11 18:23:47');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL2521',165228626,28672,'DigOut',6,'EL2521 1Ch. Pulse Train Output','2009-08-11 18:23:47');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL2521-0024',165228626,28672,'DigOut',6,'EL2521-0024 1Ch. Pulse Train 24V DC Output','2009-08-11 18:23:47');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL2521-1001',165228626,28672,'DigOut',6,'EL2521-1001 1Ch. Pulse Train Output','2009-08-11 18:23:47');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL2602',170537042,28672,'DigOut',2,'EL2602 2Ch. Relay Output, NO (230V AC / 30V DC)','2009-08-11 18:23:47');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL2612',171192402,28672,'DigOut',2,'EL2612 2Ch. Relay Output, CO (125V AC / 30V DC)','2009-08-11 18:23:47');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL2622',171847762,28672,'DigOut',2,'EL2622 2Ch. Relay Output, NO (230V AC / 30V DC)','2009-08-11 18:23:47');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL2624',171978834,28672,'DigOut',4,'EL2624 4Ch. Relay Output, NO (125V AC / 30V DC)','2009-08-11 18:23:47');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EK1101',72166482,4096,'SystemBk',1,'EK1101 EtherCAT Coupler (2A E-Bus, ID switch)','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EK1122',73542738,-1,'System',0,'EK1122 2 port EtherCAT junction','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EK1501',98380882,4096,'SystemBk',1,'EK1501 EtherCAT Coupler (2A E-Bus, FX-MultiMode, ID switch)','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EK1521',99691602,-1,'System',0,'EK1521 1 port EtherCAT junction (FX-MultiMode)','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'CX1100-0004',72114226,-1,'SystemBk',0,'CX1100-0004 EtherCAT Power supply (2A E-Bus)','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL4001',262221906,4096,'AnaOut',1,'EL4001 1Ch. Ana. Output 0-10V, 12bit','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL4002',262287442,4096,'AnaOut',2,'EL4002 2Ch. Ana. Output 0-10V, 12bit','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL4004',262418514,4096,'AnaOut',4,'EL4004 4Ch. Ana. Output 0-10V, 12bit','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL4008',262680658,4096,'AnaOut',8,'EL4008 8Ch. Ana. Output 0-10V, 12bit','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL4011',262877266,4096,'AnaOut',1,'EL4011 1Ch. Ana. Output 0-20mA, 12bit','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL4012',262942802,4096,'AnaOut',2,'EL4012 2Ch. Ana. Output 0-20mA, 12bit','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL4014',263073874,4096,'AnaOut',4,'EL4014 4Ch. Ana. Output 0-20mA, 12bit','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL4018',263336018,4096,'AnaOut',8,'EL4018 8Ch. Ana. Output 0-20mA, 12bit','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL4021',263532626,4096,'AnaOut',1,'EL4021 1Ch. Ana. Output 4-20mA, 12bit','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL4022',263598162,4096,'AnaOut',2,'EL4022 2Ch. Ana. Output 4-20mA, 12bit','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL4024',263729234,4096,'AnaOut',4,'EL4024 4Ch. Ana. Output 4-20mA, 12bit','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL4028',263991378,4096,'AnaOut',8,'EL4028 8Ch. Ana. Output 4-20mA, 12bit','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL4031',264187986,4096,'AnaOut',1,'EL4031 1Ch. Ana. Output +/-10V, 12bit','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL4032',264253522,4096,'AnaOut',2,'EL4032 2Ch. Ana. Output +/-10V, 12bit','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL4034',264384594,4096,'AnaOut',4,'EL4034 4Ch. Ana. Output +/-10V, 12bit','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL4038',264646738,4096,'AnaOut',8,'EL4038 8Ch. Ana. Output +/-10V, 12bit','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL4112-0010',269496402,6144,'AnaOut',2,'EL4112-0010 2Ch. Ana. Output +/-10mA, 16bit','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL4122',270151762,6144,'AnaOut',2,'EL4122 2Ch. Ana. Output  4-20mA, 16bit','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3702',242626642,4096,'AnaInFast',5,'EL3702 2Ch. Ana. Input +/-10V, DIFF, Oversample','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'EL3742',245248082,4096,'AnaInFast',5,'EL3742 2Ch. Ana. Input 0-20mA, 16bit, DIFF, Oversample','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'BK1250',81931298,7168,'System',0,'BK1250 Coupler between E-bus and K-bus Terminals','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'IL2300-B110',150745236,7168,'TermBk',0,'IL2300-B110 EtherCAT IP link coupler','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'IL2301-B110',150810772,7168,'TermBk',0,'IL2301-B110 EtherCAT IP link coupler','2009-08-10 20:34:09');
INSERT INTO `ethercatDevices` VALUES ('Beckhoff Automation GmbH',2,'IL2302-B110',150876308,7168,'TermBk',0,'IL2302-B110 EtherCAT IP link coupler','2009-08-10 20:34:09');
/*!40000 ALTER TABLE `ethercatDevices` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ethercatNet`
--

DROP TABLE IF EXISTS `ethercatNet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ethercatNet` (
  `address` int(11) NOT NULL DEFAULT '0',
  `vendor` char(48) NOT NULL DEFAULT '',
  `device` char(24) NOT NULL DEFAULT '',
  `status` enum('active','inactive','virtual') NOT NULL DEFAULT 'active',
  `stem` char(12) NOT NULL DEFAULT 'default',
  `stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`address`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ethercatNet`
--

LOCK TABLES `ethercatNet` WRITE;
/*!40000 ALTER TABLE `ethercatNet` DISABLE KEYS */;
INSERT INTO `ethercatNet` VALUES (0,'beckhoff','EK1100','inactive','default','2010-10-13 16:00:25');
INSERT INTO `ethercatNet` VALUES (1,'beckhoff','EL1018','active','default','2011-09-26 23:04:34');
INSERT INTO `ethercatNet` VALUES (2,'beckhoff','EL2008','active','default','2011-09-27 21:13:56');
/*!40000 ALTER TABLE `ethercatNet` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ethercatVendors`
--

DROP TABLE IF EXISTS `ethercatVendors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ethercatVendors` (
  `vendorName` char(48) NOT NULL DEFAULT '',
  `vendorNickname` char(16) NOT NULL DEFAULT '',
  `stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`vendorName`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ethercatVendors`
--

LOCK TABLES `ethercatVendors` WRITE;
/*!40000 ALTER TABLE `ethercatVendors` DISABLE KEYS */;
INSERT INTO `ethercatVendors` VALUES ('Beckhoff Automation GmbH','beckhoff','2009-08-10 19:08:44');
/*!40000 ALTER TABLE `ethercatVendors` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2011-11-14 22:03:04
