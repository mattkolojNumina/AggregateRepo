# ------ f o l d e r -----
#

# remove formax entrance. Three machines
#
# machine f2: formax exit, bail, and knife
#  init -> idle -> wait -> block -> settle -> square
#     -> full -> extend -> retract -> idle
#
# machine f3: pocket
#  init -> idle -> crimp -> full -> extend -> idle
#
# machine f4: grip (note idle is out and open
#  init -> idle -> retract -> -> grip -> extend -> 
#            xfer_grab -> xfer_release -> full -> 
#            lower -> release -> close -> 
#            raise -> idle
#
require "zonetrak_common.pl" ;


my ( $SF_INIT, $SF_IDLE, $SF_WAIT, $SF_BLOCK, $SF_SETTLE,
     $SF_SQUARE, $SF_FULL, $SF_EXTEND, $SF_RETRACT, $SF_GRIP,
     $SF_RELEASE, $SF_CRIMP, $SF_XFER_GRAB, $SF_RAISE, $SF_XFER,
     $SF_CLOSE, $SF_XFER_RELEASE, $SF_LOWER, $SF_SETSERVO, $SF_SETSLIDE,
     $SF_DONE, $SF_SLIDEIN, $SF_SLIDEOUT, $SF_FAULT )
   = (  0,  1,  2,  3,  4,
        5,  6,  7,  8,  9,
       10, 11, 12, 13, 14,
       15, 16, 17, 18, 19,
       20, 21, 22, 99 );
Trk::const_set( "SF_INIT",        $SF_INIT );
Trk::const_set( "SF_IDLE",        $SF_IDLE );
Trk::const_set( "SF_WAIT",        $SF_WAIT );
Trk::const_set( "SF_BLOCK",       $SF_BLOCK );
Trk::const_set( "SF_SETTLE",      $SF_SETTLE );
Trk::const_set( "SF_SQUARE",      $SF_SQUARE );
Trk::const_set( "SF_FULL",        $SF_FULL );
Trk::const_set( "SF_EXTEND",      $SF_EXTEND );
Trk::const_set( "SF_RETRACT",     $SF_RETRACT );
Trk::const_set( "SF_GRIP",        $SF_GRIP );
Trk::const_set( "SF_RELEASE",     $SF_RELEASE );
Trk::const_set( "SF_CRIMP",       $SF_CRIMP );
Trk::const_set( "SF_XFER_GRAB",   $SF_XFER_GRAB );
Trk::const_set( "SF_RAISE",       $SF_RAISE );
Trk::const_set( "SF_XFER",        $SF_XFER );
Trk::const_set( "SF_CLOSE",       $SF_CLOSE );
Trk::const_set( "SF_XFER_RELEASE",$SF_XFER_RELEASE );
Trk::const_set( "SF_LOWER",       $SF_LOWER );
Trk::const_set( "SF_SETSERVO",    $SF_SETSERVO );
Trk::const_set( "SF_SETSLIDE",    $SF_SETSLIDE );
Trk::const_set( "SF_DONE",        $SF_DONE );
Trk::const_set( "SF_SLIDEIN",     $SF_SLIDEIN );
Trk::const_set( "SF_SLIDEOUT",    $SF_SLIDEOUT );
Trk::const_set( "SF_FAULT",       $SF_FAULT );


my ( $RF_BOX, $RF_STATE, $RF_NEXT, $RF_TOTAL,
     $RF_COUNT, $RF_EYE_IN, $RF_EYE_OUT, $RF_KNIFE_ISHOME,
     $RF_KNIFE_ISACTIVE, $RF_TIMER, $RF_BAIL_ISHOME, $RF_BAIL_ISACTIVE,
     $RF_SLIDE_ISHOME, $RF_SLIDE_ISACTIVE, $RF_GRIP_OPEN, $RF_KNIFE_EXTEND,
     $RF_KNIFE_RETRACT, $RF_BAIL_EXTEND, $RF_SLIDE_EXTEND, $RF_SLIDE_RETRACT,
     $RF_XGRIP_CLOSE, $RF_GO, $RF_CHUTE, $RF_RUN,
     $RF_DEBUG, $RF_HOLD, $RF_RESET, $RF_FMAX_ENABLE,
     $RF_FMAX_RUN, $RF_POCKET_EYE, $RF_INTERLOCK1, $RF_INTERLOCK2,
     $RF_CRIMP_CLOSE, $RF_CRIMPA_ISOPEN, $RF_CRIMPB_ISOPEN, $RF_SERVO,
     $RF_YGRIP_CLOSE, $RF_YGRIP_ISOPEN, $RF_XGRIP_ISOPEN, $RF_F2,
     $RF_F3, $RF_F4, $RF_XYPAPER, $RF_SERVO_FAULT,
     $RF_PURGE )
   = (  0,  1,  2,  3,
        4,  5,  6,  7,
        8,  9, 10, 11,
       12, 13, 14, 15,
       16, 17, 18, 19,
       20, 21, 22, 23,
       24, 25, 26, 27,
       28, 29, 30, 31,
       32, 33, 34, 35,
       36, 37, 38, 39,
       40, 41, 42, 43,
       44 );
Trk::const_set( "RF_BOX",              $RF_BOX );
Trk::const_set( "RF_STATE",            $RF_STATE );
Trk::const_set( "RF_TIMER",            $RF_TIMER );
Trk::const_set( "RF_NEXT",             $RF_NEXT );
Trk::const_set( "RF_TOTAL",            $RF_TOTAL );
Trk::const_set( "RF_COUNT",            $RF_COUNT );
Trk::const_set( "RF_EYE_IN",           $RF_EYE_IN );
Trk::const_set( "RF_EYE_OUT",          $RF_EYE_OUT );
Trk::const_set( "RF_KNIFE_ISHOME",     $RF_KNIFE_ISHOME );
Trk::const_set( "RF_KNIFE_ISACTIVE",   $RF_KNIFE_ISACTIVE );
Trk::const_set( "RF_KNIFE_EXTEND",     $RF_KNIFE_EXTEND );
Trk::const_set( "RF_KNIFE_RETRACT",    $RF_KNIFE_RETRACT );
Trk::const_set( "RF_BAIL_ISHOME",      $RF_BAIL_ISHOME );
Trk::const_set( "RF_BAIL_ISACTIVE",    $RF_BAIL_ISACTIVE );
Trk::const_set( "RF_BAIL_EXTEND",      $RF_BAIL_EXTEND );
Trk::const_set( "RF_SLIDE_ISHOME",     $RF_SLIDE_ISHOME );
Trk::const_set( "RF_SLIDE_ISACTIVE",   $RF_SLIDE_ISACTIVE );
Trk::const_set( "RF_SLIDE_EXTEND",     $RF_SLIDE_EXTEND );
Trk::const_set( "RF_SLIDE_RETRACT",    $RF_SLIDE_RETRACT );
Trk::const_set( "RF_XGRIP_ISOPEN",     $RF_XGRIP_ISOPEN );
Trk::const_set( "RF_XGRIP_CLOSE",      $RF_XGRIP_CLOSE );
Trk::const_set( "RF_YGRIP_ISOPEN",     $RF_YGRIP_ISOPEN );
Trk::const_set( "RF_YGRIP_CLOSE",      $RF_YGRIP_CLOSE );
Trk::const_set( "RF_CRIMPA_ISOPEN",    $RF_CRIMPA_ISOPEN );
Trk::const_set( "RF_CRIMPB_ISOPEN",    $RF_CRIMPB_ISOPEN );
Trk::const_set( "RF_CRIMP_CLOSE",      $RF_CRIMP_CLOSE );
Trk::const_set( "RF_XYPAPER",          $RF_XYPAPER );
Trk::const_set( "RF_SERVO_FAULT",      $RF_SERVO_FAULT );
Trk::const_set( "RF_PURGE",            $RF_PURGE );

Trk::const_set( "RF_GO",               $RF_GO );
Trk::const_set( "RF_CHUTE",            $RF_CHUTE );
Trk::const_set( "RF_RUN",              $RF_RUN );
Trk::const_set( "RF_DEBUG",            $RF_DEBUG );
Trk::const_set( "RF_HOLD",             $RF_HOLD );
Trk::const_set( "RF_RESET",            $RF_RESET );
Trk::const_set( "RF_FMAX_ENABLE",      $RF_FMAX_ENABLE );
Trk::const_set( "RF_FMAX_RUN",         $RF_FMAX_RUN );
Trk::const_set( "RF_POCKET_EYE",       $RF_POCKET_EYE );
Trk::const_set( "RF_ILK-1",            $RF_INTERLOCK1 );
Trk::const_set( "RF_ILK-2",            $RF_INTERLOCK2 );
Trk::const_set( "RF_SERVO",            $RF_SERVO );
Trk::const_set( "RF_F2",               $RF_F2 );
Trk::const_set( "RF_F3",               $RF_F3 );
Trk::const_set( "RF_F4",               $RF_F4 );


my ( $BOX_NONE, $BOX_ANON ) = ( -1, -2 );
Trk::const_set( "BOX_NONE", $BOX_NONE );
Trk::const_set( "BOX_ANON", $BOX_ANON );


my ( $ERF_PAPER_JAM, $ERF_PAPER_LOST, $ERF_PRINTER_TIMEOUT,
     $ERF_FOLDER_FAIL, $ERF_POCKET_FAULT, $ERF_PAPER_MISFEED,
     $ERF_CHUTE_JAM, $ERF_SERVO_FAULT, $ERF_SETUP_FAIL,
     $ERF_YGRIP_FAIL )
   = ( 100, 101, 102,
       103, 104, 105,
       106, 107, 108,
       109 );
Trk::const_set( 'ERF_PAPER_JAM',      $ERF_PAPER_JAM );
Trk::const_set( 'ERF_PAPER_LOST',     $ERF_PAPER_LOST );
Trk::const_set( 'ERF_PRINTER_TIMEOUT',$ERF_PRINTER_TIMEOUT );
Trk::const_set( 'ERF_FOLDER_FAIL',    $ERF_FOLDER_FAIL );
Trk::const_set( 'ERF_POCKET_FAULT',   $ERF_POCKET_FAULT );
Trk::const_set( 'ERF_PAPER_MISFEED',  $ERF_PAPER_MISFEED );
Trk::const_set( 'ERF_CHUTE_JAM',      $ERF_CHUTE_JAM );
Trk::const_set( 'ERF_SERVO_FAULT',    $ERF_SERVO_FAULT );
Trk::const_set( 'ERF_SETUP_FAIL',     $ERF_SETUP_FAIL );
Trk::const_set( 'ERF_YGRIP_FAIL',     $ERF_YGRIP_FAIL );


rp::const( 'folder_settle',    10 );
rp::const( 'folder_down',      40 );
rp::const( 'folder_up',        10 );
rp::const( 'folder_grip',      20 );
rp::const( 'folder_pause',     100 );
rp::const( 'printer_wait',     1600 );
rp::const( 'paper_movement',   50 );
rp::const( 'actuator_movement',150 );
rp::const( 'reset_duration',   500 );
rp::const( 'paper_drop',       1000 );
rp::const( 'paper_early',      5 );  #ank: was 15
rp::const( 'purge_grip_wait',  25 );
rp::const( 'crimp_duration',   50 );
rp::const( 'page_batch',       5 );


# this file got too large. 
# find the f2 through f4 in the files below
require "folder-2.pl";
require "folder-3.pl";
require "folder-4.pl";


# addFolder
#
sub addFolder {
   my ( $name, $data ) = @_;
   my $d2  = dp::virtual( $name . '.2',      $name . ' machine 2' );
   my $hd2 = dp::virtual( $name . '.2_hold', $name . ' machine 2 hold' );
   my $dd2 = dp::virtual( $name . '.2_debug',$name . ' machine 2 debug' );
   my $d3  = dp::virtual( $name . '.3',      $name . ' machine 3' );
   my $hd3 = dp::virtual( $name . '.3_hold', $name . ' machine 3 hold' );
   my $dd3 = dp::virtual( $name . '.3_debug',$name . ' machine 3 debug' );
   my $d4  = dp::virtual( $name . '.4',      $name . ' machine 4' );
   my $hd4 = dp::virtual( $name . '.4_hold', $name . ' machine 4 hold');
   my $dd4 = dp::virtual( $name . '.4_debug',$name . ' machine 4 debug');
   my $d5  = dp::virtual( $name . '.4_go',   $name . ' release paper into box');

   Trk::dp_registerset( $d2, $RF_NEXT, $d3 );
   Trk::dp_registerset( $d3, $RF_NEXT, $d4 );

   Trk::dp_registerset( $d2, $RF_DEBUG, $dd2 );
   Trk::dp_registerset( $d3, $RF_DEBUG, $dd3 );
   Trk::dp_registerset( $d4, $RF_DEBUG, $dd4 );

   Trk::dp_registerset( $d2, $RF_HOLD, $hd2 );
   Trk::dp_registerset( $d3, $RF_HOLD, $hd3 );
   Trk::dp_registerset( $d4, $RF_HOLD, $hd4 );

   Trk::dp_registerset( $d4, $RF_GO, $d5 );

   my $run = $data->{'run'};
   my $reset = $data->{'reset'};
   #printf("1\n") ;

   Trk::dp_registerset( $d2, $RF_RUN, dp::handle( $data->{'run'} ) );
   Trk::dp_registerset( $d3, $RF_RUN, dp::handle( $data->{'run'} ) );
   Trk::dp_registerset( $d4, $RF_RUN, dp::handle( $data->{'run'} ) );

   Trk::dp_registerset( $d2, $RF_RESET, dp::handle( $data->{'reset'} ) );
   Trk::dp_registerset( $d3, $RF_RESET, dp::handle( $data->{'reset'} ) );
   Trk::dp_registerset( $d4, $RF_RESET, dp::handle( $data->{'reset'} ) );

   Trk::dp_registerset( $d2, $RF_PURGE, dp::handle( $data->{'purge'} ) );
   Trk::dp_registerset( $d3, $RF_PURGE, dp::handle( $data->{'purge'} ) );
   Trk::dp_registerset( $d4, $RF_PURGE, dp::handle( $data->{'purge'} ) );
   #printf("2\n") ;

   Trk::dp_registerset( $d2, $RF_POCKET_EYE, dp::handle($data->{'pocketEye'}) );
   Trk::dp_registerset( $d3, $RF_POCKET_EYE, dp::handle($data->{'pocketEye'}) );
   Trk::dp_registerset( $d4, $RF_POCKET_EYE, dp::handle($data->{'pocketEye'}) );
   #printf("3\n") ;

   Trk::dp_registerset($d2,$RF_FMAX_ENABLE,dp::handle($data->{'formaxOk'}));
   Trk::dp_registerset($d2,$RF_FMAX_RUN,dp::handle($data->{'formaxRun'}));
   Trk::dp_registerset($d2,$RF_EYE_OUT,dp::handle($data->{'eyeOut'}));
   Trk::dp_registerset($d2,$RF_KNIFE_ISHOME,dp::handle($data->{'knifeIsHome'}));
   Trk::dp_registerset($d2,$RF_KNIFE_ISACTIVE,dp::handle($data->{'knifeIsActive'}));
   Trk::dp_registerset($d2,$RF_KNIFE_EXTEND,dp::handle($data->{'knifeExtend'})) ;
   Trk::dp_registerset($d2,$RF_KNIFE_RETRACT,dp::handle($data->{'knifeRetract'}));
   Trk::dp_registerset($d2,$RF_BAIL_ISHOME,dp::handle($data->{'bailIsHome'})) ;
   Trk::dp_registerset($d2,$RF_BAIL_ISACTIVE,dp::handle($data->{'bailIsActive'}));
   Trk::dp_registerset($d2,$RF_BAIL_EXTEND,dp::handle($data->{'bailExtend'})) ;
   #printf("4\n") ;

   Trk::dp_registerset($d3,$RF_KNIFE_ISHOME,dp::handle($data->{'knifeIsHome'})) ;
   Trk::dp_registerset($d3,$RF_KNIFE_ISACTIVE,dp::handle($data->{'knifeIsActive'}));
   Trk::dp_registerset($d3,$RF_CRIMPA_ISOPEN,dp::handle($data->{'crimpaIsOpen'}));
   Trk::dp_registerset($d3,$RF_CRIMPB_ISOPEN,dp::handle($data->{'crimpbIsOpen'}));
   Trk::dp_registerset($d3,$RF_CRIMP_CLOSE,dp::handle($data->{'crimp'}));
   #printf("5\n") ;

   Trk::dp_registerset($d4,$RF_KNIFE_ISHOME,dp::handle($data->{'knifeIsHome'}));
   Trk::dp_registerset($d4,$RF_KNIFE_ISACTIVE,dp::handle($data->{'knifeIsActive'}));
   Trk::dp_registerset($d4,$RF_CRIMPA_ISOPEN,dp::handle($data->{'crimpaIsOpen'}));
   Trk::dp_registerset($d4,$RF_CRIMPB_ISOPEN,dp::handle($data->{'crimpbIsOpen'}));
   Trk::dp_registerset($d4,$RF_SLIDE_ISHOME,dp::handle($data->{'slideIsHome'}));
   Trk::dp_registerset($d4,$RF_SLIDE_ISACTIVE,dp::handle($data->{'slideIsActive'}));
   Trk::dp_registerset($d4,$RF_SLIDE_EXTEND,dp::handle($data->{'slideExtend'}));
   Trk::dp_registerset($d4,$RF_SLIDE_RETRACT,dp::handle($data->{'slideRetract'}));
   Trk::dp_registerset($d4,$RF_XGRIP_ISOPEN,dp::handle($data->{'xGripIsOpen'}));
   Trk::dp_registerset($d4,$RF_XGRIP_CLOSE,dp::handle($data->{'xGripClose'}));
   Trk::dp_registerset($d4,$RF_YGRIP_ISOPEN,dp::handle($data->{'yGripIsOpen'}));
   Trk::dp_registerset($d4,$RF_YGRIP_CLOSE,dp::handle($data->{'yGripClose'}));
   Trk::dp_registerset($d4,$RF_XYPAPER,dp::handle($data->{'xyPaper'}));
   Trk::dp_registerset($d4,$RF_SERVO,dp::handle($data->{'servo'}));
   Trk::dp_registerset($d4,$RF_SERVO_FAULT,dp::handle($data->{'servoFault'}));
   #printf("6\n") ;

   Trk::dp_registerset( $d2, $RF_F2, $d2 );
   Trk::dp_registerset( $d2, $RF_F3, $d3 );
   Trk::dp_registerset( $d2, $RF_F4, $d4 );
   Trk::dp_registerset( $d3, $RF_F2, $d2 );
   Trk::dp_registerset( $d3, $RF_F3, $d3 );
   Trk::dp_registerset( $d3, $RF_F4, $d4 );
   Trk::dp_registerset( $d4, $RF_F2, $d2 );
   Trk::dp_registerset( $d4, $RF_F3, $d3 );
   Trk::dp_registerset( $d4, $RF_F4, $d4 );
   #printf("7\n");

   my @stub = ( $name . '.2', 'f2_machine',
                $name . '.3', 'f3_machine',
                $name . '.4', 'f4_machine' );
   Trk::ex_compile($name."_machine",$name." machine",\@stub);
   trakbase::leading("tm_10ms",$name."_machine",$name." machine") ;
   trakbase::trailing("tm_10ms",$name."_machine",$name." machine") ;
   my $dummy = pop(@stub) ;
}
