#
#   qctr - create tables 
#

CREATE TABLE counter (
  zone char(16) NOT NULL default '',
  code char(16) NOT NULL default '',
  value int(11) NOT NULL default '0',
  description char(64) default NULL,
  stamp timestamp(14) NOT NULL,
  PRIMARY KEY  (zone,code)
) TYPE=MyISAM;

CREATE TABLE counter_log (
  zone char(16) NOT NULL default '',
  code char(16) NOT NULL default '',
  value int(11) NOT NULL default '0',
  description char(64) default NULL,
  stamp timestamp(14) NOT NULL
) TYPE=MyISAM;
