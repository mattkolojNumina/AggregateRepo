#!/bin/perl -I.
#
# P8IO control words

require Trk;
require dp;

my @code;

Trk::dp_adddriver('extern:p8io','external driver for Panther P8 i/o');

sub create_p8io {
   my ($name, $p8io_data) = @_;

   # create the device
   my $ip = $p8io_data->{'ip'};
   Trk::dp_adddevice( $name.'_dev', $name.' i/o device', 'extern:p8io',
         'ip='.$ip );

   define_io($name,'ok',    {report=>both});
   define_io($name,'home',  {report=>both});
   define_io($name,'lotar', {report=>lead});
   define_io($name,'data',  {report=>both});
   define_io($name,'label', {report=>both});
   define_io($name,'ribbon',{report=>both});

   define_io($name,'tamp',  {report=>lead,oneshot=>true});
   define_io($name,'reset', {report=>lead,oneshot=>true});
}

sub define_io {
   my ($base,$type,$extra) = @_;

   my $ppi = $extra->{'ppi'};
   if ($ppi eq '') { $ppi = 'tm_1ms'; }

   my $dp = $base.'_'.$type;
   my $h = Trk::dp_new( $dp, $base.'_dev', $type, $name.' '.$type );

   if($extra->{'report'} eq 'lead') {
      traksort::report_ld($dp,$ppi);
   } elsif($extra->{'report'} eq 'trail') {
      traksort::report_trl($dp,$ppi);
   } elsif($extra->{'report'} eq 'both') {
      traksort::report_ld($dp,$ppi);
      traksort::report_trl($dp,$ppi);
   }

   if($extra->{'oneshot'} ne '') {
      my @stub = ($dp,"pulse");
      Trk::ex_compile($dp."_oneshot",$dp." oneshot",\@stub);
      trakbase::leading($dp,$dp."_oneshot",$dp." oneshot");
      my $dummy = pop(@stub);
   }
}

# return a true value to indicate successful initialization
1;
