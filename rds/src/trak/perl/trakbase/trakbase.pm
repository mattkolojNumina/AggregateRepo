package trakbase;

use strict;
use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

require Exporter;
require AutoLoader;

@ISA = qw(Exporter AutoLoader);
# Items to export into callers namespace by default. Note: do not export
# names by default without a very good reason. Use EXPORT_OK instead.
# Do not simply export all your public functions/methods/constants.
@EXPORT = qw(
  message,submessage,leading,trailing,dhandle
);
$VERSION = '0.01';


# Preloaded methods go here.


# Preloaded methods go here.

require Trk ;

sub message {
    my $msg = @_[0] ;
    print ".. $msg\n" ;
}
sub submessage {
    my $msg = @_[0] ;
    print "  .....  $msg\n" ;
}

Trk::dp_adddriver ("virtual","Virtual Driver") ;
Trk::dp_adddevice ("virtual","Virtual Device","virtual","unused") ;

Trk::rp_adddriver ("register","Register Driver") ;
Trk::rp_adddevice ("register","Register Device","register","unused") ;

Trk::dp_adddriver ("time","Timing Driver") ;
Trk::dp_adddevice ("time","Timing Device","time","unused") ;

my @code=( "-1" ) ;
Trk::ex_compile("NIL","Constant NIL",\@code) ;
Trk::ex_compile("nil","Constant NIL",\@code) ;

@code=qw( 1 swap dp.value.set ) ;
Trk::ex_compile("on","Turn dp on",\@code) ;

@code=qw( 0 swap dp.value.set ) ;
Trk::ex_compile("off","Turn dp off",\@code) ;

@code=qw( dp.value.get ) ;
Trk::ex_compile("tst","Shorthand for dp.value.get",\@code) ;

@code=qw( dp.counter.get ) ;
Trk::ex_compile("ctr","Shorthand for dp.counter.get",\@code) ;

@code=qw( evt.a on ) ;
Trk::ex_compile("delay_set","Event to set dp in arg a",\@code) ;

@code=qw( evt.a off ) ;
Trk::ex_compile("delay_clr","Event to clear dp in arg a",\@code) ;

@code=qw( a dp.carton.set ) ;
Trk::ex_compile("carton!","Store Carton",\@code) ;

@code=qw( a dp.carton.get ) ;
Trk::ex_compile("carton@","Fetch Carton",\@code) ;

@code = qw( a dp.state.set ) ;
Trk::ex_compile("state!","Store State",\@code) ;

@code = qw( a dp.state.get ) ;
Trk::ex_compile("state@","Fetch State",\@code) ;

@code = qw( 4 a dp.register.get  rp.value.get ) ;
Trk::ex_compile("on_preset@","Fetch Preset",\@code) ;

@code = qw( 5 a dp.register.get rp.value.get ) ;
Trk::ex_compile("off_preset@","Fetch Preset",\@code) ;

@code= qw( 6 a dp.register.get ) ;
Trk::ex_compile("eye_timer@","Fetch Timer",\@code) ;

@code = qw( a dp.partner.get ) ;
Trk::ex_compile("partner@","Fetch Partner",\@code) ;

@code=qw(swap 1 swap shl or) ;
Trk::ex_compile("bit_set","set a bit",\@code) ;

@code=qw(swap 1 swap shl neg and) ;
Trk::ex_compile("bit_clr","Clear a bit",\@code) ;

@code=qw(swap 1 swap shl and) ;
Trk::ex_compile("bit_tst?","test a bit",\@code) ;

@code=qw(  evt.a on ) ;
Trk::ex_compile("delay_set","Event to set dp in arg a",\@code) ;

@code=qw(  evt.a off ) ;
Trk::ex_compile("delay_clr","Event to clear dp in arg a",\@code) ;

@code=qw( evt.b evt.a dp.value.set) ;
Trk::ex_compile("delay_value","Event to set dp in arg a to b",\@code) ;

@code=qw( evt.b not evt.a dp.value.set) ;
Trk::ex_compile("delay_not","Event to set dp in arg a to not b",\@code) ;

sub leading {
    my $dp = @_[0] ;
    my $statement = @_[1] ;
    my $desc = @_[2] ;
    
    my $list ;
    my $state ;
    my $foo ;
    my $dp_h = dhandle($dp) ;
    my $stat = Trk::ex_getstatement($statement) ;
    if ($stat == -1) {
	die "Statement: " . $statement . " Not Found" ;
    }
    $list = Trk::ex_newlist("On: " . $desc,$stat) ;
    $foo = Trk::dp_addleading($dp_h,$list) ;
}

sub trailing {
    my $dp = @_[0] ;
    my $statement = @_[1] ;
    my $desc = @_[2] ;
    
    my $list ;
    my $state ;
    my $foo ;
    my $dp_h = dhandle($dp) ;
    my $stat = Trk::ex_getstatement($statement) ;
    if ($stat == -1) {
	die("Statement: " . $statement . " Not Found") ;
    }
    $list = Trk::ex_newlist("Off: " . $desc,$stat) ;
    $foo = Trk::dp_addtrailing($dp_h,$list) ;
}

sub dhandle {
    my $name = @_[0] ;
    
    my $handle = Trk::dp_handle($name) ;
    if ($_ == -1) {
	die "Failed to locate dp: " . $name ;
    }
    return $handle ;
}


# Autoload methods go after =cut, and are processed by the autosplit program.

1;
__END__
# Below is the stub of documentation for your module. You better edit it!

=head1 NAME

trakbase - Perl extension for Trak. 

=head1 SYNOPSIS

  require trakbase;
  trakbase::leading(dp_name,statement, description)
  trakbase::trailing(dp_name,statement, description)
  trakbase::dhandle --- (deprecated. Use dp::handle instead).

Action Words added for common use.
  on --- ( dp --- )  turns on stack on.
  off --- ( dp --- )  turns given dp off.
  tst --- ( dp --- b ) leaves value of given dp on stack.
  ctr@ --- ( dp --- counter ) leaves counter value on stack.
  delay_set --- code to put on dp. Turns on dp in parameter a. Used in events.
  delay_clr --- code turns dp off. Used in events. dp in parameter a.

  carton[@!] --- ( --- b) Assumes a is the dp. shorthand to get/set register 0 from
dp pointed to by register a.
  state[@!] --- as above, but for register 1.
  partner@ --- like above, but fetch register 2.
  

=head1 DESCRIPTION

This module installs a lot of helper actions into trak. This also assists in
setting up leading and trailing edge actions.

leading and trailing 

=head1 AUTHOR

Boi. M. I. Thor, thor@a.galaxy.far.far.away
Mark Olson marko@numinasys.com

=head1 SEE ALSO

perl(1).

=cut
