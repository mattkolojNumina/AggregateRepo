/*
SQLyog Professional v12.07 (64 bit)
MySQL - 5.5.56-MariaDB : Database - rds
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`rds` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `rds`;

/* Procedure structure for procedure `_proAll` */

DELIMITER $$

/*!50003 CREATE DEFINER=`rds`@`%` PROCEDURE `_proAll`(IN fromDate DATETIME, toDate DATETIME)
BEGIN

CALL _proDurations(fromDate,toDate) ;
CALL _proTracks(fromDate,toDate) ;

SELECT a.operatorName AS operator,
       t.task,
       t.operation,
       t.`area`,
       SUM(t.quantity) AS `value`,
       SUM(d.duration) AS duration,
       SUM(t.quantity)/SUM(d.duration) AS rate,
       FLOOR(SUM(t.quantity)/SUM(d.duration)/(SUM(IF(g.goal IS NULL,-1.0,g.goal)*d.duration)/SUM(d.duration))*100.0) AS intRate,
       SUM(IF(g.goal IS NULL,-1.0,g.goal)*d.duration)/SUM(d.duration) AS standard
FROM tracks AS t
LEFT OUTER JOIN durations AS d
ON  d.operatorID=t.operatorID
AND d.task      =t.task
AND d.area      =t.area

JOIN proOperators AS a   
ON t.operatorID=a.operatorID 
LEFT OUTER JOIN proOperations AS g
ON g.task=t.task
AND g.operation=t.operation
AND g.`area`=t.`area` 
GROUP BY t.operatorID, t.task, t.operation, t.`area` ;

DROP TEMPORARY TABLE IF EXISTS durations ;
DROP TEMPORARY TABLE IF EXISTS tracks ;

END */$$
DELIMITER ;

/* Procedure structure for procedure `_proDurations` */

DELIMITER $$

/*!50003 CREATE DEFINER=`rds`@`%` PROCEDURE `_proDurations`(IN fromDate DATETIME, toDate DATETIME)
BEGIN
DROP TABLE IF EXISTS durations ;
CREATE TEMPORARY TABLE IF NOT EXISTS durations
   ( operatorID CHAR(32) NOT NULL, 
     task CHAR(32) NOT NULL, 
     `area` CHAR(64) NOT NULL, 
     startDate DATE NOT NULL, 
     duration FLOAT NOT NULL ) ;
TRUNCATE durations ;
 
INSERT INTO durations   
SELECT operatorID,
       task,
       `area`,
       DATE(startTime) AS startDate,
       SUM(UNIX_TIMESTAMP(LEAST(IFNULL(endTime,NOW()),toDate)) -    
       UNIX_TIMESTAMP(GREATEST(startTime,fromDate)))/3600.0 AS duration    
FROM proOperatorLog  
WHERE startTime < toDate 
AND ( (endTime IS NULL) OR (endtime>fromDate) )
GROUP BY operatorID, task, `area`, startDate ;
END */$$
DELIMITER ;

/* Procedure structure for procedure `_proTracks` */

DELIMITER $$

/*!50003 CREATE DEFINER=`rds`@`%` PROCEDURE `_proTracks`(IN fromDate DATETIME, toDate DATETIME)
BEGIN
DROP TEMPORARY TABLE IF EXISTS tracks ;
CREATE TEMPORARY TABLE IF NOT EXISTS tracks
  ( operatorID CHAR(32) NOT NULL,
    task       CHAR(32) NOT NULL,
    `area`     CHAR(64) NOT NULL,
    operation  CHAR(64) NOT NULL,
    startDate  DATE     NOT NULL,
    quantity   double   NOT NULL ) ;
TRUNCATE tracks ;
INSERT INTO tracks 
SELECT operatorID,
       task,
       `area`,
       operation,
       DATE(stamp) AS startDate,
       SUM(`value`) AS quantity
FROM proTracker
WHERE stamp >= fromDate
AND   stamp <= toDate
GROUP BY operatorID, task, `area`, operation, startDate ;
END */$$
DELIMITER ;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
