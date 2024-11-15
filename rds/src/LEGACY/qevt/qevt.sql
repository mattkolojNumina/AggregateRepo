#
# Table structure for table 'event'
#
CREATE TABLE event (
  code char(16) NOT NULL default '',
  state enum('on','off') NOT NULL default 'off',
  start timestamp(14) NOT NULL,
  description char(80) NOT NULL default '',
  zone char(16) NOT NULL default 'default',
  severity int(11) NOT NULL default '0',
  PRIMARY KEY  (code)
) TYPE=MyISAM;

#
# Table structure for table 'event_log'
#
CREATE TABLE event_log (
  code char(16) NOT NULL default '',
  state enum('on','off') NOT NULL default 'off',
  start timestamp(14) NOT NULL,
  duration int(11) NOT NULL default '0',
  KEY code (code),
  KEY start (start)
) TYPE=MyISAM;

