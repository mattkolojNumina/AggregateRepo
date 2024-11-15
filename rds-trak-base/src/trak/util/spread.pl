# ------ s p r e a d -----
#
#  spread_timer returns the name of one of ten 10ms timers
#

my $spread_next = 0 ;
sub spread_timer
   {
   @letters = ('a','b','c','d','e','f','g','h','i','j') ;
   $spread_next = $spread_next + 1 ;
   if($spread_next<0) {$spread_next=0} ;
   if($spread_next>9) {$spread_next=0} ;
   return 'tm_10ms_'.@letters[$spread_next] ; 
   }

# spread
#    reg 1 - next to toggle
dp::virtual('spread','spread') ;

# spread_array
#    reg 0 to 9 - spread registers
dp::virtual('spread_array','spread array') ;

dp::virtual('tm_10ms_a','spread A') ;
dp::virtual('tm_10ms_b','spread B') ;
dp::virtual('tm_10ms_c','spread C') ;
dp::virtual('tm_10ms_d','spread D') ;
dp::virtual('tm_10ms_e','spread E') ;
dp::virtual('tm_10ms_f','spread F') ;
dp::virtual('tm_10ms_g','spread G') ;
dp::virtual('tm_10ms_h','spread H') ;
dp::virtual('tm_10ms_i','spread I') ;
dp::virtual('tm_10ms_j','spread J') ;

Trk::dp_registerset(dp::handle('spread_array'),0,dp::handle('tm_10ms_a')) ;
Trk::dp_registerset(dp::handle('spread_array'),1,dp::handle('tm_10ms_b')) ;
Trk::dp_registerset(dp::handle('spread_array'),2,dp::handle('tm_10ms_c')) ;
Trk::dp_registerset(dp::handle('spread_array'),3,dp::handle('tm_10ms_d')) ;
Trk::dp_registerset(dp::handle('spread_array'),4,dp::handle('tm_10ms_e')) ;
Trk::dp_registerset(dp::handle('spread_array'),5,dp::handle('tm_10ms_f')) ;
Trk::dp_registerset(dp::handle('spread_array'),6,dp::handle('tm_10ms_g')) ;
Trk::dp_registerset(dp::handle('spread_array'),7,dp::handle('tm_10ms_h')) ;
Trk::dp_registerset(dp::handle('spread_array'),8,dp::handle('tm_10ms_i')) ;
Trk::dp_registerset(dp::handle('spread_array'),9,dp::handle('tm_10ms_j')) ;

# spread_tick ( -- ) [evt]
@code=qw(
   tm_1ms tst if
      1 spread dp.register.get 
      1 +
      dup 9 > if drop 0 then
      dup 0 < if drop 0 then
      1 spread dp.register.set

      1 spread dp.register.get
      0 max 9 min
      spread_array dp.register.get on
   else
      1 spread dp.register.get 5 + 10 %
      spread_array dp.register.get off
   then
   ) ;
Trk::ex_compile('spread_tick','spread tick',\@code) ;
trakbase::leading ('tm_1ms','spread_tick','spread tick') ;
trakbase::trailing('tm_1ms','spread_tick','spread tick') ;

1 ;

