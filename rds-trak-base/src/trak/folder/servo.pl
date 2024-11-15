# --- Servo

# The folder has a servo. WE introduce some helper words and a servo def


require "zonetrak_common.pl" ;


my ( $SVR_POWERUP, $SVR_INIT, $SVR_IDLE, $SVR_MOVING, $SVR_FAULT)
   = ( 0, 1, 2, 3, 99 );
Trk::const_set( "SVR_POWERUP",$SVR_POWERUP );
Trk::const_set( "SVR_INIT",   $SVR_INIT );
Trk::const_set( "SVR_IDLE",   $SVR_IDLE );
Trk::const_set( "SVR_MOVING", $SVR_MOVING );
Trk::const_set( "SVR_FAULT",  $SVR_FAULT );


my ( $REGS_SETP, $REGS_STATE, $REGS_COMPARE, $REGS_HOLD, $REGS_OUT0,
     $REGS_OUT1, $REGS_OUT2, $REGS_OUT3, $REGS_OUT4, $REGS_OUT5,
     $REGS_DRIVE, $REGS_BUSY, $REGS_FAULT, $REGS_INP, $REGS_AREA,
     $REGS_SVON, $REGS_ESTOP, $REGS_RESET, $REGS_SETUP, $REGS_DEBUG,
     $REGS_GO, $REGS_HOLDDP, $REGS_SETON, $REGS_SVRE, $REGS_ESTOPIN,
     $REGS_FAULTDP )
   = ( 0,  1,  2,  3,  4,
       5,  6,  7,  8, 10,
      11, 12, 13, 14, 15,
      16, 17, 18, 19, 20,
      21, 22, 23, 24, 25,
      26 );
Trk::const_set( "REGS_SETP",    $REGS_SETP );
Trk::const_set( "REGS_STATE",   $REGS_STATE );
Trk::const_set( "REGS_COMPARE", $REGS_COMPARE );
Trk::const_set( "REGS_HOLD",    $REGS_HOLD );
Trk::const_set( "REGS_OUT0",    $REGS_OUT0 );
Trk::const_set( "REGS_OUT1",    $REGS_OUT1 );
Trk::const_set( "REGS_OUT2",    $REGS_OUT2 );
Trk::const_set( "REGS_OUT3",    $REGS_OUT3 );
Trk::const_set( "REGS_OUT4",    $REGS_OUT4 );
Trk::const_set( "REGS_OUT5",    $REGS_OUT5 );
Trk::const_set( "REGS_DRIVE",   $REGS_DRIVE );
Trk::const_set( "REGS_BUSY",    $REGS_BUSY );
Trk::const_set( "REGS_FAULT",   $REGS_FAULT );
Trk::const_set( "REGS_INP",     $REGS_INP );
Trk::const_set( "REGS_AREA",    $REGS_AREA );
Trk::const_set( "REGS_SVON",    $REGS_SVON );
Trk::const_set( "REGS_ESTOP",   $REGS_ESTOP );
Trk::const_set( "REGS_RESET",   $REGS_RESET );
Trk::const_set( "REGS_SETUP",   $REGS_SETUP );
Trk::const_set( "REGS_DEBUG",   $REGS_DEBUG );
Trk::const_set( "REGS_GO",      $REGS_GO );
Trk::const_set( "REGS_HOLDDP",  $REGS_HOLDDP );
Trk::const_set( "REGS_SETON",   $REGS_SETON );
Trk::const_set( "REGS_SVRE",    $REGS_SVRE );
Trk::const_set( "REGS_ESTOPIN", $REGS_ESTOPIN );
Trk::const_set( "REGS_FAULTDP", $REGS_FAULTDP );



# value dp servo-encode-out ---- 
@code=qw(
   >r
   dup 1 and       REGS_OUT0 r@ dp.register.get dp.value.set
   1 shr dup 1 and REGS_OUT1 r@ dp.register.get dp.value.set
   1 shr dup 1 and REGS_OUT2 r@ dp.register.get dp.value.set
   1 shr dup 1 and REGS_OUT3 r@ dp.register.get dp.value.set
   1 shr dup 1 and REGS_OUT4 r@ dp.register.get dp.value.set
   1 shr     1 and REGS_OUT5 r@ dp.register.get dp.value.set
   r> drop
);
Trk::ex_compile( 'servo-encode-out', '0-63 -> 6 outputs', \@code );

# value dp servo-send
@code=qw(
   >r
   r@ servo-encode-out
   REGS_SETUP r@ _zone_dp_off
   tm_1ms &delay_set 5 REGS_DRIVE r@ dp.register.get 0 0 0 ev_insert
   r> drop
);
Trk::ex_compile('servo-send','0-63 -> 6 outputs',\@code) ;

@code=qw(
   >r
   REGS_DRIVE r@ _zone_dp_off
   r> drop
);
Trk::ex_compile('servo-hold','0-63 -> 6 outputs',\@code) ;

@code=qw(
   >r
   REGS_SVON r@ _zone_dp_on
   r> drop
);
Trk::ex_compile('servo-ready','0-63 -> 6 outputs',\@code) ;


@code=qw(
   >r
   REGS_SETUP r@ _zone_dp_on
   REGS_DRIVE r@ _zone_dp_off
   REGS_RESET r@ _zone_dp_off
   r> drop
);
Trk::ex_compile('servo-setup','0-63 -> 6 outputs',\@code) ;


@code=qw(
   REGS_BUSY swap _zone_dp?
);
Trk::ex_compile('servo-busy?','0-63 -> 6 outputs',\@code) ;

@code=qw(
   >r
   REGS_FAULT r@ _zone_dp? not
   r> drop
);

Trk::ex_compile( 'servo-fault?', '0-63 -> 6 outputs', \@code );

@code=qw(
   >r
   REGS_INP  r@ _zone_dp?
   r> drop
);
Trk::ex_compile( 'servo-done?', '0-63 -> 6 outputs', \@code );

@code=qw(
   >r
   REGS_SVON r@ _zone_dp_on
   REGS_ESTOP r@ _zone_dp_on
   REGS_RESET r@ _zone_dp_off
   r> drop
);
Trk::ex_compile( 'servo-on', '0-63 -> 6 outputs', \@code );

@code=qw(
   >r
   0 r@ servo-encode-out
   0 REGS_SETP r@ dp.register.set
   0 REGS_COMPARE r@ dp.register.set
   REGS_RESET r@ dp.register.get pulse
   r> drop
);
Trk::ex_compile( 'servo-reset', '0-63 -> 6 outputs', \@code );



@code=qw(
   >r
   r@ on
   r@ servo-on
   r@ _zone_tick

   SVR_INIT r@ _zone_state!

   r> drop
);
Trk::ex_compile( '_svro_state_powerup', 'init servo', \@code );



#
@code=qw(
   >r
   r@ on

   r@ servo-hold
   SVR_IDLE r@ _zone_state!
   r> drop
);
Trk::ex_compile( '_svro_state_init', 'init servo', \@code );



@code=qw(
   >r
   r@ off
   r@ servo-on

   r@ servo-fault? if
      r@ on
      SVR_FAULT r@ _zone_state!
      REGS_FAULTDP r@ _zone_dp_on
   else
      REGS_SETP r@ dp.register.get
      REGS_COMPARE r@ dp.register.get = not if
         REGS_SETP r@ dp.register.get r@ servo-send
         SVR_MOVING r@ _zone_state!
      then
   then

   r> drop
);
Trk::ex_compile('_svro_state_idle','init servo',\@code) ;



@code=qw(
   >r
   r@ on
   r@ _zone_tick

   r@ servo-fault? if
      r@ on
      SVR_FAULT r@ _zone_state!
      REGS_FAULTDP r@ _zone_dp_on
   else
      r@ _zone_elapsed@ 75 >
      REGS_INP r@ _zone_dp? and
      REGS_BUSY r@ _zone_dp? not and if
         REGS_SETP r@ dp.register.get  REGS_COMPARE r@ dp.register.set
         r@ servo-hold
         SVR_IDLE r@ _zone_state!
      then
   then
   r> drop
);
Trk::ex_compile( '_svro_state_moving', 'init servo', \@code );

@code=qw(
   >r
   r@ on
   REGS_FAULTDP r@ _zone_dp? not if
      r@ servo-reset
      SVR_INIT r@ _zone_state!
   then
   r> drop
);
Trk::ex_compile('_svro_state_fault','init servo',\@code) ;



@code=qw(
   >r
   REGS_DEBUG r@ _zone_dp? not if
      r@ _zone_state@

      dup SVR_POWERUP = if r@ _svro_state_powerup  then
      dup SVR_INIT    = if r@ _svro_state_init     then
      dup SVR_MOVING  = if r@ _svro_state_moving   then
      dup SVR_IDLE    = if r@ _svro_state_idle     then
      dup SVR_FAULT   = if r@ _svro_state_fault    then
      drop
   then
   r> drop
);
Trk::ex_compile('_svro_machine','machine for servo',\@code) ;



#
sub addServo {
   my ( $name, $data ) = @_;

   my $dp_h = dp::virtual( $name, $name );

   my $dp_hold =  dp::virtual( $name . '_hold',  'hold for '  . $name );
   my $dp_debug = dp::virtual( $name . '_debug', 'debug for ' . $name );
   my $dp_fault = dp::virtual( $name . '_fault', 'fault for ' . $name );
   Trk::dp_registerset( $dp_h, $REGS_HOLDDP,  $dp_hold );
   Trk::dp_registerset( $dp_h, $REGS_FAULTDP, $dp_fault );
   Trk::dp_registerset( $dp_h, $REGS_DEBUG,   $dp_debug );

   Trk::dp_registerset( $dp_h, $REGS_OUT0,   dp::handle( $data->{ 'out0' } ) );
   Trk::dp_registerset( $dp_h, $REGS_OUT1,   dp::handle( $data->{ 'out1' } ) );
   Trk::dp_registerset( $dp_h, $REGS_OUT2,   dp::handle( $data->{ 'out2' } ) );
   Trk::dp_registerset( $dp_h, $REGS_OUT3,   dp::handle( $data->{ 'out3' } ) );
   Trk::dp_registerset( $dp_h, $REGS_OUT4,   dp::handle( $data->{ 'out4' } ) );
   Trk::dp_registerset( $dp_h, $REGS_OUT5,   dp::handle( $data->{ 'out5' } ) );

   Trk::dp_registerset( $dp_h, $REGS_DRIVE,  dp::handle( $data->{ 'drive' } ) );
   Trk::dp_registerset( $dp_h, $REGS_BUSY,   dp::handle( $data->{ 'busy' } ) );

   Trk::dp_registerset( $dp_h, $REGS_FAULT,  dp::handle( $data->{ 'fault' } ) );
   Trk::dp_registerset( $dp_h, $REGS_INP,    dp::handle( $data->{ 'inp' } ) );
   Trk::dp_registerset( $dp_h, $REGS_AREA,   dp::handle( $data->{ 'area' } ) );

   Trk::dp_registerset( $dp_h, $REGS_SVON,   dp::handle( $data->{ 'svon' } ) );
   Trk::dp_registerset( $dp_h, $REGS_ESTOP,  dp::handle( $data->{ 'estop' } ) );
   Trk::dp_registerset( $dp_h, $REGS_RESET,  dp::handle( $data->{ 'reset' } ) );

   Trk::dp_registerset( $dp_h, $REGS_HOLD,   dp::handle( $data->{ 'hold' } ) );
   Trk::dp_registerset( $dp_h, $REGS_SETUP,  dp::handle( $data->{ 'setup'} ) );

   Trk::dp_registerset( $dp_h, $REGS_SETON,  dp::handle( $data->{ 'seton' } ) );
   Trk::dp_registerset( $dp_h, $REGS_SVRE,   dp::handle( $data->{ 'svre' } ) );
   Trk::dp_registerset( $dp_h, $REGS_ESTOPIN,dp::handle( $data->{ 'estop_in' } ) );

   my @stub = ( $name, '_svro_machine' );
   Trk::ex_compile( $name . '_machine', 'machine for ' . $name, \@stub );
   trakbase::leading(  'tm_10ms', $name . '_machine', 'machine for servo' );
   trakbase::trailing( 'tm_10ms', $name . '_machine', 'machine for servo');
   my $foo = pop( @stub );

}
