#!/bin/perl -I.

# CLIENT AREA Trak Controller

require DBI;
require Trk;
require trakbase;
require dp;
require rp;
require timer;

trakbase::message( 'Test AREA Trak Control' );

require "PREFIX_AREA_system.pl" ;
require "PREFIX_AREA_io_ecat.pl" ;
require "PREFIX_AREA_io_linx.pl" ;
require "PREFIX_AREA_transport.pl" ;
require "PREFIX_AREA_control.pl" ;

exit(1) ;

