/*
SQLyog Community v13.1.2 (64 bit)
MySQL - 10.1.44-MariaDB-0ubuntu0.18.04.1 : Database - rds
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
USE `rds`;

/*Data for the table `controls` */

INSERT INTO `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`,`stamp`) VALUES 
/* Do not allow customer to edit these */
('build','victory','port','10200','Victory Voice server port','no',NOW()),
('build','victory','reconnectTime','900','Num seconds to determine if operator reconnected','no',NOW()),
('build','victory','dumpDeviceLogs','false','Dumps device logs into victoryLog table','no',NOW()),
('build','victory','debugMsg','false','Traces \"debug\" messages in logs','no',NOW()),
('build','victory','traceJson','false','Traces output of server JSON for debugging purposes','no',NOW()),
('build','victory','enableGlobalDetailedLogging','false','Enables detailed loggin for all devices','no',NOW()),
('build','victory','checksum','38C903289D38BA7','','no',NOW()),
('build','victory','delayedLogout','false','Determines if logout/unreserve is controlled by status app','no',NOW()),
/* Allow customer to edit these */
('build','victory','skipFeature','true','Allows operators to press \"skip\" button','yes',NOW()),
('build','victory','noScanFeature','true','Allows operators to press \"no scan\" button','yes',NOW()),
('build','victory','locationScanRequired','true','Requires operators to scan the location of a pick','yes',NOW()),
('build','victory','showMessageButton','true','Allows operators to send messages to supervisor','yes',NOW()),
('build','victory','quitOnLogoff','false','Close app on logoff instead of return to login screen','yes',NOW()),
('build','victory','autoLogout','30','Number of minutes to automatically log an operator out','yes',NOW()),
('build','victory','confirmLogout','false','Prompt operator to confirm logout','yes',NOW()),
/* Keen voice control */
('build','victory','KASRVadTimeoutEndSilenceForGoodMatch','0.5','Timeout after this many seconds if good match','yes',NOW()),
('build','victory','KASRVadTimeoutEndSilenceForAnyMatch','0.5','Timeout after this many seconds if any match','yes',NOW()),
('build','victory','KASRVadTimeoutMaxDuration','30.0','Timeout after this many seconds regardless of what has been recognized','yes',NOW()),
('build','victory','KASRVadTimeoutForNoSpeech','5.0','Timeout after this many seconds even if nothing has been recognized','yes',NOW());

/*Table structure for table `victoryDevices` */

DROP TABLE IF EXISTS `victoryDevices`;

CREATE TABLE `victoryDevices` (
  `deviceID` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'unique deviceId assigned by victory',
  `deviceName` char(64) NOT NULL DEFAULT '' COMMENT 'optional name for device from settings app',
  `ipAddress` char(32) NOT NULL DEFAULT '' COMMENT 'device ip address',
  `apkVersion` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'apk version number',
  `voiceVersion` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'voice engine version',
  `releaseMode` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'release or debug mode',
  `buildModel` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'device''s hardware model',
  `androidVersion` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'android OS version',
  `deviceOSBuild` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'zebra OS patch number',
  `pairedDeviceList` char(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'all paired devices',
  `battery` int(2) NOT NULL DEFAULT 0 COMMENT 'device battery level',
  `rssi` char(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'device network signal strength',
  `bssid` char(32) NOT NULL DEFAULT '' COMMENT 'device MAC address',
  `stamp` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT 'last updated stamp',
  PRIMARY KEY (`deviceID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

/*Table structure for table `victoryErrors` */

DROP TABLE IF EXISTS `victoryErrors`;

CREATE TABLE `victoryErrors` (
  `seq` int(11) NOT NULL AUTO_INCREMENT,
  `errorType` char(32) NOT NULL COMMENT 'type of error',
  `error` text NOT NULL COMMENT 'provide error message, stack trace, json, ...',
  `task` char(32) NOT NULL DEFAULT '' COMMENT 'operator''s task',
  `area` char(32) NOT NULL DEFAULT '' COMMENT 'operator''s area',
  `location` char(32) NOT NULL DEFAULT '' COMMENT 'location param if being used',
  `deviceID` char(32) NOT NULL DEFAULT '' COMMENT 'device error occured on',
  `operatorID` char(32) NOT NULL DEFAULT '' COMMENT 'operator error occured on',
  `stamp` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`seq`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

/*Table structure for table `victoryInteractions` */

DROP TABLE IF EXISTS `victoryInteractions`;

CREATE TABLE `victoryInteractions` (
  `seq` INT(11) NOT NULL AUTO_INCREMENT,
  `operatorID` VARCHAR(32) NOT NULL DEFAULT '',
  `deviceID` VARCHAR(32) NOT NULL DEFAULT '',
  `task` VARCHAR(32) NOT NULL DEFAULT '',
  `area` VARCHAR(32) NOT NULL DEFAULT '',
  `screen` VARCHAR(64) NOT NULL DEFAULT '',
  `source` ENUM('user','device') NOT NULL DEFAULT 'device',
  `responseType` VARCHAR(32) NOT NULL DEFAULT '',
  `description` VARCHAR(255) NOT NULL DEFAULT '',
  `stamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`seq`),
  KEY `stamp` (`stamp`),
  KEY `op` (`operatorID`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

/*Table structure for table `victoryMessages` */

DROP TABLE IF EXISTS `victoryMessages`;

CREATE TABLE `victoryMessages` (
  `seq` INT(11) NOT NULL AUTO_INCREMENT,
  `message` VARCHAR(255) NOT NULL DEFAULT '',
  `toOperator` VARCHAR(64) NOT NULL DEFAULT '',
  `fromOperator` VARCHAR(64) NOT NULL DEFAULT '',
  `acknowledged` TINYINT(4) NOT NULL DEFAULT '0',
  `stamp` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`seq`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

/*Table structure for table `victoryParams` */

DROP TABLE IF EXISTS `victoryParams`;

CREATE TABLE `victoryParams` (
  `operatorID` CHAR(64) NOT NULL,
  `name` CHAR(64) NOT NULL DEFAULT '',
  `value` CHAR(255) NOT NULL DEFAULT '',
  `stamp` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`operatorID`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

/*Table structure for table `victoryPhrases` */

DROP TABLE IF EXISTS `victoryPhrases`;

CREATE TABLE `victoryPhrases` (
  `phrase` CHAR(32) NOT NULL,
  `english` CHAR(255) NOT NULL DEFAULT '',
  `spanish` CHAR(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`phrase`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

/*Data for the table `victoryPhrases` */

INSERT  INTO `victoryPhrases`(`phrase`,`english`,`spanish`) VALUES 
('ARE_YOU_SURE','Are you sure?',''),
('QUIT','Quit',''),
('FOOTER_OPERATOR','Operator ID:',''),
('FOOTER_LAST_RESPONSE','Last Response:',''),
('FOOTER_MESSAGE','Message',''),
('FOOTER_LOGOUT','Logout',''),
('FOOTER_AREA','Area',''),
('FOOTER_TASK','Task',''),
('START_PALLET','Start Pallet',''),
('START','Start',''),
('ADD_PALLET','Add Pallet',''),
('MOVE_CARTON','Move carton',''),
('MOVE_PALLET','Move Pallet',''),
('SPLIT','Split',''),
('JOIN','Join',''),
('FULL','Full',''),
('UNDO','Undo',''),
('CHANGE_TASK','Change Task',''),
('CHANGE_AREA','Change Area',''),
('SHORT','Short',''),
('NO_SCAN','No Scan',''),
('SKIP','Skip',''),
('CHANGE_LOCATION','Change Location',''),
('CLOSE_PALLET','Close Pallet',''),
('REPRINT','Reprint',''),
('OVERRIDE','Override',''),
('LANE','Lane',''),
('CARTON_BARCODE','Carton barcode',''),
('SCAN_CARTON','Scan carton',''),
('PALLET','Pallet',''),
('ALREADY_LOGGED_IN','* .. <operatorID> is already logged in elsewhere',''),
('CANCEL','Cancel',''),
('CANCELED_LOGIN','Log in cancelled',''),
('CONFIRM','Confirm',''),
('CONFIRM_LOGIN','Confirm I D <operatorID>',''),
('CONFIRM_LOGOFF','Confirm log off',''),
('CONFIRM_LOGOUT','Confirm log out',''),
('ENTER_AREA','Select an area',''),
('ENTER_NUMBER','Enter a number',''),
('ENTER_PASS','Enter password',''),
('ENTER_TASK','Select a task',''),
('ENTER_TEXT','Enter some text',''),
('ERROR','Error',''),
('ERROR_LOGIN','Error logging in',''),
('ERROR_LOADING_TASK','There was an error with that .. check your code',''),
('GO_TO_LOCATION_VOICE','Go to <location>',''),
('INVALID_AREA','Invalid area',''),
('INVALID_BARCODE','Invalid barcode',''),
('INVALID_CARTON','Invalid carton <scan>',''),
('INVALID_COMMAND','Invalid command',''),
('INVALID_DESTINATION','Invalid destination',''),
('INVALID_ITEM','Invalid item',''),
('INVALID_LANE','Invalid lane',''),
('INVALID_LOCATION','Invalid location',''),
('INVALID_OPERATOR','Invalid operator I D',''),
('INVALID_PALLET','Invalid pallet <scan>',''),
('INVALID_PASSWORD','Invalid password',''),
('INVALID_POSITION','Invalid position <scan>',''),
('INVALID_QTY','Invalid quantity',''),
('INVALID_TASK','Invalid task',''),
('LOGGING_IN','Logging in as <operatorID>',''),
('LOGIN_PROMPT','Please log in',''),
('PLEASE_WAIT','Please wait',''),
('READY','Ready',''),
('RECONNECTING','* .. <operatorID> is reconnecting',''),
('SCAN_BARCODE','Scan a barcode',''),
('SCAN_ITEM','Scan [sku]',''),
('SCAN_LOCATION','Scan Location',''),
('TASK_READY','<task> ready',''),
('WARNING_MESSAGE','* Ouch * Ouch * .. that hurt','* I * I * .. detentay por fahvore'),
('WELCOME','Welcome to Victory Voice',''),
('WELCOME_MESSAGE','Welcome to RDS Victory Voice',''),
('CARTON_LPN','Carton LPN',''),
('SKU','SKU',''),
('CONTAINER','Container',''),
('PALLET_POSITION','Pallet position',''),
('PALLET_BARCODE','Pallet barcode',''),
('OPERATOR_ID','Operator ID',''),
('INVALID_OPERATOR_TEXT','Invalid operator <scan>',''),
('CONFIRM_LOGIN_TEXT','Confirm ID <operatorID>',''),
('MESSAGE_SCREEN','Send message to supervisor',''),
('MESSAGE_ENTRY_HINT','Message',''),
('SCAN_CART_TEXT','Scan cart LPN',''),
('SCAN_CART_VOICE','Scan cart L P N',''),
('CART_BARCODE','Cart barcode',''),
('INVALID_CART','Invalid cart barcode',''),
('GO_TO_LOCATION_TEXT','Go to location <location>',''),
('LOCATION','Location',''),
('QTY','Qty',''),
('ITEM','Item',''),
('BARCODE','Barcode',''),
('ITEM_BARCODE','Item barcode',''),
('PICK_QTY_UOM','Pick <qty> <uom>',''),
('QUANTITY','Quantity',''),
('PICKING_COMPLETE','Picking complete',''),
('CONFIRM_SHORT','Confirm short',''),
('SLOT_BARCODE','Cart slot barcode',''),
('NO_PICKS','No picks',''),
('SCAN_PALLET_VOICE','Scan pallet',''),
('CARTON_LABEL','Carton label',''),
('MESSAGE_VOICE','Enter message',''),
('POSITION','Position',''),
('ORDER','Order',''),
('SCAN_LOT','Scan lot',''),
('START_PICKING','Start picking',''),
('SCAN_LOAD','Scan load ID',''),
('LOAD_ID','Load ID',''),
('PUTWALL','Putwall',''),
('PIGEONHOLE','Pigeon hole',''),
('SCAN_PIGEONHOLE','Scan pigeon hole',''),
('CONFIRM_SKU','Confirm SKU <sku>',''),
('PUT_QTY_UOM','Put <putQty> <uom>',''),
('CONFIRM_SKU_VOICE','Confirm <sku>',''),
('PALLET_COMPLETE','Pallet complete','');

/*Table structure for table `victoryScreens` */

DROP TABLE IF EXISTS `victoryScreens`;

CREATE TABLE `victoryScreens` (
  `name` CHAR(255) NOT NULL DEFAULT '' COMMENT 'Java Class Names',
  `description` CHAR(255) NOT NULL DEFAULT '' COMMENT 'some description for your own benefit',
  `grammar` CHAR(32) NOT NULL DEFAULT '' COMMENT 'use a number, or leave it blank. Non-numeric values are treated as blank',
  `enableSettings` ENUM('yes','no') NOT NULL DEFAULT 'yes' COMMENT 'yes/no: whether the settings panel is shown',
  `useScanner` ENUM('yes','no') NOT NULL DEFAULT 'no' COMMENT 'yes/no: whether we interpret scan responses',
  `textEntryType` ENUM('numeric','non-numeric','none') NOT NULL DEFAULT 'none' COMMENT 'type of text entry to use when drawing the screen',
  `titleText` CHAR(128) NOT NULL DEFAULT '' COMMENT 'brief description of the state/desired action',
  `promptText` CHAR(255) NOT NULL DEFAULT '' COMMENT 'name of a prompt that the app displays in the prompt box on screen load. joins with victoryPhrases USING (phrase)',
  `textEntryHint` CHAR(128) NOT NULL DEFAULT '' COMMENT 'hint that displays on the prompt box in a text entry. joins with victoryPhrases USING (phrase)',
  `phrase` CHAR(128) NOT NULL DEFAULT '' COMMENT 'name of a phrase that the app says on screen load. joins with victoryPhrases USING (phrase)',
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

/*Data for the table `victoryScreens` */

INSERT  INTO `victoryScreens`(`name`,`description`,`grammar`,`enableSettings`,`useScanner`,`textEntryType`,`titleText`,`promptText`,`textEntryHint`,`phrase`) VALUES 
('victoryApp.TaskSelectScreen','select a task based on proTasks table','','yes','no','none','ENTER_TASK','','','ENTER_TASK'),
('victoryApp.AreaSelectScreen','select an area','1','yes','no','none','ENTER_AREA','','','ENTER_AREA'),
('victoryApp.ConnectScreen','connect screen','','yes','no','none','','','',''),
('victoryApp.ConfirmLogoutScreen','confirm logout','','yes','no','none','CONFIRM_LOGOUT','ARE_YOU_SURE','','CONFIRM_LOGOUT'),
('victoryApp.LoginScreen','login screen','10','no','yes','non-numeric','WELCOME','LOGIN_PROMPT','OPERATOR_ID','LOGIN_PROMPT'),
('victoryApp.MessageScreen','operator can send a message to the RDS webpages','','yes','no','non-numeric','MESSAGE_SCREEN','','MESSAGE_ENTRY_HINT','MESSAGE_VOICE');

/*Table structure for table `victoryUserPreferences` */

DROP TABLE IF EXISTS `victoryUserPreferences`;

CREATE TABLE `victoryUserPreferences` (
  `operatorID` CHAR(64) NOT NULL,
  `name` CHAR(64) NOT NULL,
  `value` CHAR(255) DEFAULT '',
  `stamp` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`operatorID`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

/*Data for the table `victoryUserPreferences` */

INSERT  INTO `victoryUserPreferences`(`operatorID`,`name`,`value`,`stamp`) VALUES 
('606','language'    ,'english' ,NOW()),
('606','level'       ,'beginner',NOW()),
('606','operatorName','Operator',NOW()),
('606','pitch'       ,'1.000'   ,NOW()),
('606','rate'        ,'1.000'   ,NOW()),
('606','sensitivity' ,'0.35'    ,NOW()),
('606','volume'      ,'0.750'   ,NOW());
('default','language'    ,'english' ,NOW()),
('default','level'       ,'beginner',NOW()),
('default','operatorName','Operator',NOW()),
('default','pitch'       ,'1.000'   ,NOW()),
('default','rate'        ,'1.000'   ,NOW()),
('default','sensitivity' ,'0.35'    ,NOW()),
('default','volume'      ,'0.750'   ,NOW());

/*Table structure for table `victoryTasks` */

DROP TABLE IF EXISTS `victoryTasks`;

CREATE TABLE `victoryTasks` (
  `task` char(32) NOT NULL DEFAULT '' COMMENT 'task name maps to proTasks',
  `phrase` char(32) NOT NULL DEFAULT '' COMMENT 'victoryPhrase for multi-language support',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT 'show or hide tasks on taskSelectionScreen',
  `ordinal` int(3) DEFAULT 0 COMMENT 'order to show on taskSelectScreen',
  PRIMARY KEY (`task`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

/*Table structure for table `victoryLog` */

DROP TABLE IF EXISTS `victoryLog`;

CREATE TABLE `victoryLog` (
  `seq` int(11) NOT NULL AUTO_INCREMENT,
  `operatorID` varchar(32) NOT NULL DEFAULT '',
  `deviceID` varchar(32) NOT NULL DEFAULT '',
  `log` text NOT NULL,
  `stamp` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`seq`),
  KEY `stamp` (`stamp`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
