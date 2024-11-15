package Trk;

use strict;
use Carp;
use vars qw($VERSION @ISA @EXPORT @EXPORT_OK $AUTOLOAD);

require Exporter;
require DynaLoader;
require AutoLoader;

@ISA = qw(Exporter DynaLoader);
# Items to export into callers namespace by default. Note: do not export
# names by default without a very good reason. Use EXPORT_OK instead.
# Do not simply export all your public functions/methods/constants.
@EXPORT = qw(
	DATA_LEN
	NAME_LEN
	REG_N
);
$VERSION = '0.01';

sub AUTOLOAD {
    # This AUTOLOAD is used to 'autoload' constants from the constant()
    # XS function.  If a constant is not found then control is passed
    # to the AUTOLOAD in AutoLoader.

    my $constname;
    ($constname = $AUTOLOAD) =~ s/.*:://;
    croak "& not defined" if $constname eq 'constant';
    my $val = constant($constname, @_ ? $_[0] : 0);
    if ($! != 0) {
	if ($! =~ /Invalid/) {
	    $AutoLoader::AUTOLOAD = $AUTOLOAD;
	    goto &AutoLoader::AUTOLOAD;
	}
	else {
		croak "Your vendor has not defined Trk macro $constname";
	}
    }
    no strict 'refs';
    *$AUTOLOAD = sub () { $val };
    goto &$AUTOLOAD;
}

bootstrap Trk $VERSION;

# Preloaded methods go here.

# Autoload methods go after =cut, and are processed by the autosplit program.

1;
__END__
# Below is the stub of documentation for your module. You better edit it!

=head1 NAME

Trk - Perl extension for the Trak C-api in PERL.

=head1 SYNOPSIS

  use Trk;
  

=head1 DESCRIPTION


=head1 Exported constants

  DATA_LEN
  NAME_LEN
  REG_N


=head1 Exported functions

  void trak_start(void);
  void trak_stop(void);
  int trak_test(void);
  void dp_init(void) ;
  int dp_handle(char*name);
  int dp_set(int dp_handle,int new_value);
  int dp_get(int dp_handle);
  unsigned dp_counter(int dp_handle);
  int dp_registerset(int dp_handle,int which,int value);
  int dp_registerget(int dp_handle,int which,int*value);
  int dp_counter_control(int dp_handle,int value);
  int dp_setcounter(int dp_handle,unsigned value);
  int rp_adddriver(char*name,char*description);
  int rp_getdriver(char*name);
  int rp_adddevice(char*name,char*description,char*driver,char*driver_data);
  int rp_getdevice(char*name);
  int rp_new(char*name,char*driver,char*driver_data,char*description);
  int rp_handle(char*name);
  int rp_set(int rp_handle,unsigned new_value);
  unsigned rp_get(int rp_handle);
  int ev_insert(int dp_handle,unsigned act_statement,unsigned offset,int a,int b,int c,int d);
  int dp_adddriver(char*name,char*description);
  int dp_getdriver(char*name);
  int dp_adddevice(char*name,char*description,char*driver,char*device_data);
  int dp_getdevice(char*name);
  int dp_new(char*name,char*device,char*io_data,char*description);
  int dp_addtrailing(int dp_handle,int list_handle);
  int dp_addleading(int dp_handle,int list_handle);
  int ex_getstatement(char*name);
  int ex_getcmd(char*name);
  int ex_compile(char*name,char*description,int argc,char**argv);
  int ex_newlist(char*name,char*description,int statement);
  int ex_insert(int head,int add_list);


=head1 AUTHOR

Mark Olson, molson@numinagroup.com

=head1 SEE ALSO

perl(1).

=cut
