package condition;

use strict;
use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

require Exporter;
require AutoLoader;

@ISA = qw(Exporter AutoLoader);
# Items to export into callers namespace by default. Note: do not export
# names by default without a very good reason. Use EXPORT_OK instead.
# Do not simply export all your public functions/methods/constants.
@EXPORT = qw(
	     create
	     );

$VERSION = '0.01';


# Preloaded methods go here.


require Trk ;
require trakbase ;
require dp ;
require rp ;

my $CONDITION_INIT = 0 ;
my $CONDITION_OFF = 4 ;
Trk::const_set('CONDITION_INIT',$CONDITION_INIT) ;
Trk::const_set('CONDITION_ON_HOLD',1) ;
Trk::const_set('CONDITION_ON',2) ;
Trk::const_set('CONDITION_OFF_HOLD',3) ;
Trk::const_set('CONDITION_OFF',$CONDITION_OFF) ;

my $CONDITION_REG_STATE = 1 ;
my $CONDITION_REG_PARTNER = 2 ;
my $CONDITION_REG_REVERSE = 3 ;
my $CONDITION_REG_TIMER = 4 ;
my $CONDITION_REG_OFF_VAL = 5 ;
my $CONDITION_REG_ON_VAL = 6 ;

Trk::const_set('CONDITION_REG_STATE',$CONDITION_REG_STATE) ;
Trk::const_set('CONDITION_REG_PARTNER',$CONDITION_REG_PARTNER) ;
Trk::const_set('CONDITION_REG_REVERSE',$CONDITION_REG_REVERSE) ;
Trk::const_set('CONDITION_REG_TIMER',$CONDITION_REG_TIMER) ;
Trk::const_set('CONDITION_REG_OFF_VAL',$CONDITION_REG_OFF_VAL) ;
Trk::const_set('CONDITION_REG_ON_VAL',$CONDITION_REG_ON_VAL) ;

rp::const('global_off',10,'global off debounce') ;
rp::const('global_on',10,'global on debounce') ;


my @code ;


#( dp --- bool (bool, true/false if reversed or not)
@code = qw( CONDITION_REG_REVERSE swap dp.register.get ) ;
Trk::ex_compile('cond.rev@','is the dead timed input reversed',\@code) ;
#( dp --- state  return state )
@code = qw (CONDITION_REG_STATE swap dp.register.get) ;
Trk::ex_compile('cond.state@','get deadtime state',\@code) ;
#( value dp ---  -> store state )
@code = qw (CONDITION_REG_STATE swap dp.register.set) ;
Trk::ex_compile('cond.state!','set deadtime state',\@code) ;
#( dp --- partner  return partner )
@code = qw (CONDITION_REG_PARTNER swap dp.register.get) ;
Trk::ex_compile('cond.partner@','get deadtime state',\@code) ;
#( dp --- timer  return timer )
@code = qw (CONDITION_REG_TIMER swap dp.register.get) ;
Trk::ex_compile('cond.timer@','get deadtime state',\@code) ;

@code = qw (CONDITION_REG_ON_VAL swap dp.register.get rp.value.get ) ;
Trk::ex_compile('cond.on@','get deadtime state',\@code) ;
@code = qw (CONDITION_REG_OFF_VAL swap dp.register.get rp.value.get ) ;
Trk::ex_compile('cond.off@','get deadtime state',\@code) ;

# forward declarations required.
@code=qw(1 drop) ;
Trk::ex_compile("dead_on_evt","Event for deadtime On",\@code) ;
Trk::ex_compile("dead_off_evt","Event for Deadtime Off",\@code) ;

#
#
# deadtime events and edge code
#
#

# evt.a - dp
# evt.b - partner
# state should be CONDITION_ON_HOLD
@code = qw( 
  evt.a tst if  
    CONDITION_ON evt.a cond.state!
    evt.b evt.a cond.rev@ if off else on then
  else
    CONDITION_OFF_HOLD evt.a cond.state!
    evt.b evt.a cond.rev@ if on else off then
    evt.a cond.timer@ &dead_off_evt evt.a cond.off@ 
       evt.a evt.b 0 0 ev_insert
  then
) ;
Trk::ex_compile("dead_on_evt","Event for deadtime On",\@code) ;

# evt.a - dp
# evt.b - partner
# state should be CONDITION_OFF_HOLD

@code = qw( 
evt.a tst if 
    CONDITION_ON_HOLD evt.a cond.state!
    evt.b evt.a cond.rev@ if off else on then
    evt.a cond.timer@ &dead_on_evt evt.a cond.on@ 
       evt.a evt.b 0 0 ev_insert
else
    CONDITION_OFF evt.a cond.state!
    evt.b evt.a cond.rev@ if on else off then
then
) ;
Trk::ex_compile("dead_off_evt","Event for Deadtime Off",\@code) ;

#Trk::const_set('CONDITION_ON_HOLD',1) ;
#Trk::const_set('CONDITION_ON',2) ;
#Trk::const_set('CONDITION_OFF_HOLD',3) ;
#Trk::const_set('CONDITION_OFF',4) ;

 #  r@ CONDITION_ON_HOLD = if 
 #     edge.dp cond.partner@ 
 #        edge.dp cond.rev@ if off else on then
 #  then
 #  r@ CONDITION_OFF_HOLD if 
 #     edge.dp cond.partner@
 #        edge.dp cond.rev@ if on else off then
 #  then


@code = qw( 
   edge.dp cond.state@ >r
   r@ CONDITION_OFF = r@ CONDITION_INIT = or if 
      edge.dp cond.partner@ 
          edge.dp cond.rev@ if off else on then 
      CONDITION_ON_HOLD edge.dp cond.state!
      edge.dp cond.timer@ &dead_on_evt edge.dp cond.on@ 
         edge.dp edge.dp cond.partner@ 0 0 ev_insert
   then
   r> drop
  ) ;
Trk::ex_compile("dead_leading","Action on valid leading edge",\@code) ;

#
#   r@ CONDITION_ON_HOLD = if 
#      edge.dp cond.partner@ 
#         edge.dp cond.rev@ if off else on then
#   then
#   r@ CONDITION_OFF_HOLD if 
#      edge.dp cond.partner@
#         edge.dp cond.rev@ if on else off then
#   then

@code=qw(
   edge.dp cond.state@ >r
   r@ CONDITION_ON = r@ CONDITION_INIT  = or if 
      edge.dp cond.partner@ 
          edge.dp cond.rev@ if on else off then 
      CONDITION_OFF_HOLD edge.dp cond.state!
      edge.dp cond.timer@ &dead_off_evt edge.dp cond.off@ 
         edge.dp edge.dp cond.partner@ 0 0 ev_insert
   then
   
   r> drop
) ;
Trk::ex_compile("dead_trailing","Action on Trailing Edge",\@code) ;

@code=qw(
  evt.a tst if
    CONDITION_ON evt.a cond.state!
    evt.b evt.a cond.rev@ if off else on then
  else
    CONDITION_OFF evt.a cond.state!
    evt.b evt.a cond.rev@ if on else off then
  then  
) ;
Trk::ex_compile("idle_dead","setup for deadtime",\@code) ;



#
#  debounce edge and event code
#
#
#



#Trk::const_set('CONDITION_INIT',$CONDITION_INIT) ;
#Trk::const_set('CONDITION_ON_HOLD',1) ;
#Trk::const_set('CONDITION_ON',2) ;
#Trk::const_set('CONDITION_OFF_HOLD',3) ;
#Trk::const_set('CONDITION_OFF',$CONDITION_OFF) ;


# a -> driving dp
# b -> synthetic dp
# c -> value
# d -> counter

@code=qw(
   evt.a cond.state@ CONDITION_OFF_HOLD = if
      evt.b evt.a cond.rev@ if on else off then
      CONDITION_OFF evt.a cond.state!
   then
) ;
Trk::ex_compile("dbc_trl_evt","Debounce Event",\@code) ;

@code=qw(
   evt.a cond.state@ CONDITION_ON_HOLD = if
      evt.b evt.a cond.rev@ if off else on then
      CONDITION_ON evt.a cond.state!
   then
) ;

Trk::ex_compile("dbc_lead_evt","Debounce Event",\@code) ;

@code=qw(
  evt.c evt.a dp.counter.get =  ifbreak
  evt.d if
    dbc_lead_evt    
  else
    dbc_trl_evt    
  then
) ;
Trk::ex_compile("dbc_event","Debounce Event",\@code) ;

@code=qw(
  edge.dp cond.state@ >r
  r@ CONDITION_OFF =  if
    1 drop
  then
  r@ CONDITION_INIT =  if
    CONDITION_OFF edge.dp cond.state!
    edge.dp cond.partner@ edge.dp cond.rev@ if on else off then
  then  
  r@ CONDITION_OFF_HOLD = if
    CONDITION_OFF edge.dp cond.state!
  then  
  r@ CONDITION_ON_HOLD =  if
    CONDITION_OFF edge.dp cond.state!
  then  
  r@ CONDITION_ON = if
    CONDITION_OFF_HOLD edge.dp cond.state!
    edge.dp cond.timer@ &dbc_event edge.dp cond.off@
      edge.dp dup cond.partner@ edge.counter edge.value ev_insert
  then  
  
  r> drop 
) ;
Trk::ex_compile("dbc_trailing","Debounce Trailing Edge",\@code) ;

@code=qw(
  edge.dp cond.state@ >r
  r@ CONDITION_ON = if 
    1 drop  
  then  
  r@ CONDITION_INIT = if
    CONDITION_ON edge.dp cond.state! 
    edge.dp cond.partner@ edge.dp cond.rev@ if on else off then
  then  
  r@ CONDITION_ON_HOLD = if
    CONDITION_ON edge.dp cond.state! 
  then  
  r@ CONDITION_OFF_HOLD = if
    CONDITION_ON edge.dp cond.state! 
  then
  r@ CONDITION_OFF = if
    CONDITION_ON_HOLD edge.dp cond.state!
    edge.dp cond.timer@ &dbc_event edge.dp cond.on@
      edge.dp dup cond.partner@ edge.counter edge.value ev_insert
  then  
  
  r> drop 
) ;
Trk::ex_compile("dbc_leading","Debounce Leading Edge",\@code) ;


#my $CONDITION_REG_STATE = 1 ;
#my $CONDITION_REG_PARTNER = 2 ;
#my $CONDITION_REG_REVERSE = 3 ;
#my $CONDITION_REG_TIMER = 4 ;
#my $CONDITION_REG_OFF_VAL = 5 ;
#my $CONDITION_REG_ON_VAL = 6 ;

sub create {
   my ($name,$dp,$data) = @_ ;

   my $n_h = Trk::dp_handle($name) ;

   my $type ;
   if ($data->{'type'} eq '') {
        $type = 'dead' ;
   }  else {
      $type = $data->{'type'} ;
   }

  
   my $desc ;
   if ($data->{'desc'} eq '') {
     $desc = sprintf("%s: [%s] driving [%s]",$type,$dp,$name) ;
   }  else {
      $desc = $data->{'desc'} ;
   }
   
   if ($n_h == -1) {
     $n_h = dp::virtual($name,$desc) ;
   } 
   my $dp_h = Trk::dp_handle($dp) ;
   if ($dp_h == -1) {
     die("ouch: " . $dp . " not found required to alias dead timer") ;
   }
   Trk::dp_registerset($dp_h,$CONDITION_REG_STATE,$CONDITION_INIT) ;
   Trk::dp_registerset($dp_h,$CONDITION_REG_PARTNER,$n_h) ;
   if ($data->{'reverse'} ne '') {
     Trk::dp_registerset($dp_h,$CONDITION_REG_REVERSE,1) ;
     my $ex = Trk::ex_getstatement('idle_dead') ;  
     Trk::ev_insert(dp::handle('tm_1ms'),$ex,1,$dp_h,$n_h,0,0) ;
   }  else {
     Trk::dp_registerset($dp_h,$CONDITION_REG_REVERSE,0) ;
     Trk::dp_registerset($dp_h,$CONDITION_REG_STATE,$CONDITION_OFF) ;
   }

   if ($data->{'timer'} ne '') {
      Trk::dp_registerset($dp_h,$CONDITION_REG_TIMER,
        dp::handle($data->{'timer'})) ;
   } else {
      Trk::dp_registerset($dp_h,$CONDITION_REG_TIMER,
        dp::handle('tm_1ms')) ;
   }

   
   if ($data->{'off'} ne '') {
     if ($data->{'reverse'} eq '') {
        Trk::dp_registerset($dp_h,$CONDITION_REG_OFF_VAL,
            rp::handle($data->{'off'})) ;
     } else {
        Trk::dp_registerset($dp_h,$CONDITION_REG_OFF_VAL,
            rp::handle($data->{'on'})) ;
     }
   } else {
     if ($data->{'reverse'} eq '') {
       Trk::dp_registerset($dp_h,$CONDITION_REG_OFF_VAL,
            rp::handle('global_off')) ;
     } else {
       Trk::dp_registerset($dp_h,$CONDITION_REG_OFF_VAL,
            rp::handle('global_on')) ;
     }
   }

   if ($data->{'on'} ne '') {
     if ($data->{'reverse'} eq '') {
       Trk::dp_registerset($dp_h,$CONDITION_REG_ON_VAL,
            rp::handle($data->{'on'})) ;
     } else {
       Trk::dp_registerset($dp_h,$CONDITION_REG_ON_VAL,
          rp::handle($data->{'off'})) ;
     }
   } else {
     if ($data->{'reverse'} eq '') {
       Trk::dp_registerset($dp_h,$CONDITION_REG_ON_VAL,
          rp::handle('global_on')) ;
     } else {
       Trk::dp_registerset($dp_h,$CONDITION_REG_ON_VAL,
          rp::handle('global_off')) ;
     }
   }
   
   if ($type eq 'debounce') {
      trakbase::leading($dp,"dbc_leading","dbc: " . $dp) ;
      trakbase::trailing($dp,"dbc_trailing","dbc: " . $dp ) ;
   } else { # default is dead
      trakbase::leading($dp,"dead_leading","dead: " . $dp) ;
      trakbase::trailing($dp,"dead_trailing","dead: " . $dp ) ;
   }
   return $n_h ; 
}


# Autoloaded methods go after =cut, and are processed by the autosplit program.

1;
__END__
# Below is the stub of documentation for your module. You better edit it!

=head1 NAME

dead - Perl extension to add dead timed inputs to Trak



=head1 SYNOPSIS

  require condition;

 condition::create(name,input dp,data)

   data is a hash  with the following keys
   
 * timer -> defaults dp tm_1ms 
 * off -> off rp (default 'global_off')
 * on  -> on rp (default 'global_on')
 * reverse -> if reverses linked (name) dp make non-empty if reversed desired
 * type -> default to dead, legal values currently "dead" "debounce"
 * desc -> description


=head1 DESCRIPTION

Use require not use, for inclusion. Use will re-evaluate the perl module each
time a use statement is seen. Require loads it once, and a "require" of
a previously "required" module will do nothing.

This modoule (dead) creates code "statements" in Trak to handle dead time 
conditioning of data points. An I/O point with dead time conditioning will 
change state with the input i/o dp, but then until the dead time has expired
will no longer change state until the dead time has passed. This is similar
(but not the same) as debounced I/O. Note: the input "name" data point, i.e.,
the new conditioned I/O *is* created by this call.

For the input dp, some registers are used. Please do not use these for
another purpose. We use register 1,2,4,5,6 (state,partner,4,5,6).
state (register 1) holds a state variable. partner holds the index of the
conditioned data point. Register 4 contains the On preset. The On preset
 is an index to the register containing the
on preset. Register 5 contains the Off preset.
Register 6 contains the index of the timer dp.


=head1 AUTHOR

Mark Olson marko@numinasys.com

=head1 SEE ALSO

timer,debounce,Trk,perl(1).

=cut

