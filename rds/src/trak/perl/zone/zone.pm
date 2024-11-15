package zone;

use strict;
use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

require Exporter;
require AutoLoader;

@ISA = qw(Exporter AutoLoader);
# Items to export into callers namespace by default. Note: do not export
# names by default without a very good reason. Use EXPORT_OK instead.
# Do not simply export all your public functions/methods/constants.
@EXPORT = qw(
  horn, link
);
$VERSION = '0.01';


# Preloaded methods go here.

require timer ;
require trakbase ;

my @code ;
# 

@code=qw( 1 swap dp.register.get ) ;
Trk::ex_compile("zone.state@","Start a Zone",\@code) ;
@code=qw( 1 swap dp.register.set ) ;
Trk::ex_compile("zone.state!","Start a Zone",\@code) ;
@code=qw( 2 swap dp.register.get) ;
Trk::ex_compile("zone.error@","Start a Zone",\@code) ;
@code=qw( 5 swap dp.register.get) ;
Trk::ex_compile("zone.reset.dp@","Zone Reset DP",\@code) ;

@code=qw( zone.error@ 1 and ) ;
Trk::ex_compile("zone.air?","Test zone air",\@code) ;
@code=qw( zone.error@ 2 and 1 shr ) ;
Trk::ex_compile("zone.overload?","Test zone air",\@code) ;
@code=qw( zone.error@ 4 and 2 shr ) ;
Trk::ex_compile("zone.jam?","Test zone air",\@code) ;
@code=qw( zone.error@ 8 and 3 shr ) ;
Trk::ex_compile("zone.estop?","Test zone air",\@code) ;

@code=qw( 4 swap dp.register.get) ; 
Trk::ex_compile("zone.counter@","Start a Zone",\@code) ;
@code=qw( 4 swap dp.register.set) ; 
Trk::ex_compile("zone.counter!","Start a Zone",\@code) ;
@code=qw( dup zone.counter@ 1 + swap zone.counter! ) ;
Trk::ex_compile("zone.counter++","Start a Zone",\@code) ;

# if error is not zero with a zone.error! then the zone is 
# turned off. On the trailing edge of the zone, 
# all sorts of motors and so on will be turning off.

@code=qw( 
        dup >r 2 swap dp.register.set 
        r@ zone.error@ 0 = not 
           if  r@ off  r@ zone.reset.dp@ off  3 r@ zone.state!  then
        r> drop
        ) ;
Trk::ex_compile("zone.error!","Start a Zone",\@code) ;



# user words. These words are used on edges by code all over.

@code=qw(
        >r 
        r@ zone.error@ if r> drop then
        r@ zone.state@ 0 = 
        if
          0 r@ zone.counter!
          1 r@ zone.state!
          r@ off
        then 
        r> drop
        ) ;
Trk::ex_compile("zone.start","Start a Zone",\@code) ;
@code=qw(
        >r
        r@ zone.error@ not
        if 
        0 r@ zone.state!
        then
        r> off 
        ) ;
Trk::ex_compile("zone.stop","Start a Zone",\@code) ;
@code=qw(
        dup off 
        dup 0 swap zone.state!
        dup 0 swap zone.error!
        zone.reset.dp@ on
        ) ;
Trk::ex_compile("zone.reset","Start a Zone",\@code) ;


# errors are a bit flag as more than one may exist at once.
@code=qw(
        dup zone.error@
        8 or swap zone.error!
        ) ;
Trk::ex_compile("zone.estop","Start a Zone",\@code) ;


# e-stops a little special. We want to change light behaviour when
# they are reset.

@code=qw(
        dup zone.error@
        2 or swap zone.error!
        ) ;
Trk::ex_compile("zone.overload","Start a Zone",\@code) ;

@code=qw(
        dup zone.error@
        4 or swap zone.error!
        ) ;
Trk::ex_compile("zone.jam","Start a Zone",\@code) ;

@code=qw(
        dup zone.error@
        1 or swap zone.error!
        ) ;
Trk::ex_compile("zone.air","Start a Zone",\@code) ;

@code=qw(
        a.set
        a zone.state@ 1 =
        if
          a zone.counter@ 10 = 
          if  
             a on 2 a zone.state! 0   
          else
          a zone.counter++
          then 
        then
        ) ;
Trk::ex_compile("zone.starter","Start Test for Zone",\@code) ;


sub link {
   my $name = @_[0] ;
   my $desc = @_[1] ;

   Trk::dp_new($name,"virtual","blank",$desc) ;
   Trk::dp_new($name . ".reset","virtual","blank",$desc) ;
   my $h = trakbase::dhandle($name) ;
   my $r_h = trakbase::dhandle($name . ".reset") ;
   Trk::dp_registerset($h,5,$r_h) ;

   my @code=($name,"zone.starter") ;
   Trk::ex_compile($name . ".zone.start",
        "Delayed startup for " .$name,\@code) ;
   trakbase::leading("tm_500ms",$name . ".zone.start",
       "Start Delay for zone: " . $name) ;
   my $foo = pop(@code) ; 
}

@code=qw(
        a.set
        partner@ zone.state@ 1 = 
        if
        b a dp.value.set 
        else
        a off
        then 
         ) ;
Trk::ex_compile("zone.horn","toggle horn if state=1",\@code) ;


sub horn {
  my $zone =@_[0] ;
  my $output = @_[1] ;

  my $h = trakbase::dhandle($output) ;
  my $z_h = trakbase::dhandle($zone) ;

  Trk::dp_registerset($h,2,$z_h) ;

  my @code=($output,"zone.horn") ;
  my $statement = $output . "-horn" ;
  Trk::ex_compile($statement,"horn for zone " . $zone,\@code) ;
  trakbase::leading("tm_500ms",$statement,"Horn on/off") ;
  trakbase::trailing("tm_500ms",$statement,"Horn on/off") ;
   my $foo = pop(@code) ; 
}


# Autoload methods go after =cut, and are processed by the autosplit program.

1;
__END__
# Below is the stub of documentation for your module. You better edit it!

=head1 NAME

zone - Perl extension for zone in Trak.


=head1 SYNOPSIS

  use zone;
 
  zone::link


=head1 DESCRIPTION

coming.

soon.

=head1 AUTHOR

Mark Olson. marko@numinasys.com

=head1 SEE ALSO

perl(1).

=cut
