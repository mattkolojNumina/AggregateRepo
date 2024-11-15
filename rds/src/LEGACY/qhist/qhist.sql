#
#  qhist - create table (has indexes and all fields are NOT NULL)
#

CREATE TABLE `carton_log` (
  `carton` char(16) NOT NULL default '',
  `code` char(16) NOT NULL default '',
  `description` char(128) NOT NULL default '',
  `stamp` timestamp(14) NOT NULL,
  KEY `carton` (`carton`),
  KEY `stamp` (`stamp`)
) TYPE=MyISAM;

