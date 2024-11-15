#!/bin/perl -I.

# CLIENT AREA Transportation

require "zonetrak_common.pl"; 
require "zonetrak_oneshot.pl"; 
require "zonetrak_zone.pl";
require "zonetrak_belt.pl";
require "zonetrak_divert.pl";
require "zonetrak_angle_divert.pl";
require "zonetrak_handoff.pl";


trakbase::submessage('Transport for CLIENT');
$area='area';

# ----- APPLICATION -----
trakbase::submessage('Transport for APPLICATION');
# zone_def('1000a-1',{'lax_mode'=>'true','slug_mode'=>'true'});


1;
