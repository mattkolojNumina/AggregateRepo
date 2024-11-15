package Ersc;

use 5.026001;
use strict;
use warnings;

require Exporter;
use AutoLoader qw(AUTOLOAD);

our @ISA = qw(Exporter);

# Items to export into callers namespace by default. Note: do not export
# names by default without a very good reason. Use EXPORT_OK instead.
# Do not simply export all your public functions/methods/constants.

# This allows declaration	use Ersc ':all';
# If you do not need this, moving things directly into @EXPORT or @EXPORT_OK
# will save memory.
our %EXPORT_TAGS = ( 'all' => [ qw(
	
) ] );

our @EXPORT_OK = ( @{ $EXPORT_TAGS{'all'} } );

our @EXPORT = qw(
  addModule	
);

our $VERSION = '0.01';


# Preloaded methods go here.


require Trk;
require trakbase;
require dp;
require rp;
require timer;

Trk::dp_adddriver('io:ersc','extern ersc') ;

sub addModule 
  {
    my ($name,$ip) = @_ ;
 
    my $dev = $name .'_dev' ;
    Trk::dp_adddevice($dev,
                   $name.' device',
                   'io:ersc',
                   $ip) ;
    Trk::dp_new($name.'_pe_lf',$dev,'pe_lf','left pe ' . $name) ;
    Trk::dp_new($name.'_pe_rt',$dev,'pe_rt','right pe ' . $name) ;
    Trk::dp_new($name.'_io_i1r',$dev,'io_i1r','control input 1 right ' . $name) ;
    Trk::dp_new($name.'_io_i2r',$dev,'io_i2r','control input 2 right ' . $name) ;
    Trk::dp_new($name.'_io_or',$dev,'io_or','control output right  ' . $name) ;
    Trk::dp_new($name.'_io_i1l',$dev,'io_i1l','control input 1 left ' . $name) ;
    Trk::dp_new($name.'_io_i2l',$dev,'io_i2l','control input 2 left ' . $name) ;
    Trk::dp_new($name.'_io_ol',$dev,'io_ol','control output left  ' . $name) ;
    Trk::dp_new($name.'_mtr_lf',$dev,'mtr_lf','motor left ' . $name) ;
    Trk::dp_new($name.'_mtr_rt',$dev,'mtr_rt','mtor right ' . $name) ;
    Trk::dp_new($name.'_mtr_lf_r',$dev,'mtr_lf_r','motor left control ' . $name) ;
    Trk::dp_new($name.'_mtr_rt_r',$dev,'mtr_rt_r','motor right control ' . $name) ;
    Trk::dp_new($name.'_mtr_lf_bk',$dev,'mtr_lf_bk','brake via servo ' . $name) ;
    Trk::dp_new($name.'_mtr_rt_bk',$dev,'mtr_rt_bk','brake via servo ' . $name) ;
    Trk::dp_new($name.'_mtr_lf_f?',$dev,'mtr_lf_flt','motor left fault' . $name) ;
    Trk::dp_new($name.'_mtr_rt_f?',$dev,'mtr_rt_flt','motor right fault' . $name) ;
    Trk::dp_new($name.'_reset',$dev,'reset','reset card ' . $name) ;
    Trk::dp_new($name.'_comm',$dev,'comm','comm with card (1=fault) ' . $name) ;
    
  }

# Autoload methods go after =cut, and are processed by the autosplit program.

1;
__END__
# Below is stub documentation for your module. You'd better edit it!

=head1 NAME

Ersc - Perl extension for adding ERSC (ConveyControl LINX cards) to Trak
perl modules

=head1 SYNOPSIS

  use Ersc;
  addModule(name,ip)

=head1 DESCRIPTION

Each module adds modules in plc io mode for ERSC. 

Need to add function addZPA for adding interface dps to modules in ZPA
mode.

=head2 EXPORT

None by default.



=head1 SEE ALSO

Trak docs


=head1 AUTHOR

RDS Developer Mark Olson, E<lt>rds@E<gt>

=head1 COPYRIGHT AND LICENSE

Copyright (C) 2019 by RDS Developer

This library is free software; you can redistribute it and/or modify
it under the same terms as Perl itself, either Perl version 5.26.1 or,
at your option, any later version of Perl 5 you may have available.


=cut
