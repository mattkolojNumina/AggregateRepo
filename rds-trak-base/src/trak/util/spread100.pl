# ------ s p r e a d  1 0 0 -----
#
#  spread100_timer returns the name of one of ten 100ms timers
#

my $spread100_next = 0 ;
sub spread100_timer
   {
   @letters = ('a','b','c','d','e','f','g','h','i','j') ;
   $spread100_next = $spread100_next + 1 ;
   if($spread100_next<0) {$spread100_next=0} ;
   if($spread100_next>9) {$spread100_next=0} ;
   return 'tm_100ms_'.@letters[$spread100_next] ; 
   }

# spread100
#    reg 1 - next to toggle
dp::virtual('spread100','spread 100') ;

# spread_array
#    reg 0 to 9 - spread registers
dp::virtual('spread100_array','spread 100 array') ;

dp::virtual('tm_100ms_a','spread 100 A') ;
dp::virtual('tm_100ms_b','spread 100 B') ;
dp::virtual('tm_100ms_c','spread 100 C') ;
dp::virtual('tm_100ms_d','spread 100 D') ;
dp::virtual('tm_100ms_e','spread 100 E') ;
dp::virtual('tm_100ms_f','spread 100 F') ;
dp::virtual('tm_100ms_g','spread 100 G') ;
dp::virtual('tm_100ms_h','spread 100 H') ;
dp::virtual('tm_100ms_i','spread 100 I') ;
dp::virtual('tm_100ms_j','spread 100 J') ;

Trk::dp_registerset(dp::handle('spread100_array'),0,dp::handle('tm_100ms_a')) ;
Trk::dp_registerset(dp::handle('spread100_array'),1,dp::handle('tm_100ms_b')) ;
Trk::dp_registerset(dp::handle('spread100_array'),2,dp::handle('tm_100ms_c')) ;
Trk::dp_registerset(dp::handle('spread100_array'),3,dp::handle('tm_100ms_d')) ;
Trk::dp_registerset(dp::handle('spread100_array'),4,dp::handle('tm_100ms_e')) ;
Trk::dp_registerset(dp::handle('spread100_array'),5,dp::handle('tm_100ms_f')) ;
Trk::dp_registerset(dp::handle('spread100_array'),6,dp::handle('tm_100ms_g')) ;
Trk::dp_registerset(dp::handle('spread100_array'),7,dp::handle('tm_100ms_h')) ;
Trk::dp_registerset(dp::handle('spread100_array'),8,dp::handle('tm_100ms_i')) ;
Trk::dp_registerset(dp::handle('spread100_array'),9,dp::handle('tm_100ms_j')) ;

# spread100_tick ( -- ) [evt]
@code=qw(
   tm_10ms tst if
      1 spread100 dp.register.get 
      1 +
      dup 9 > if drop 0 then
      dup 0 < if drop 0 then
      1 spread100 dp.register.set

      1 spread100 dp.register.get
      0 max 9 min
      spread100_array dp.register.get on
   else
      1 spread100 dp.register.get 5 + 10 %
      spread100_array dp.register.get off
   then
   ) ;
Trk::ex_compile('spread100_tick','spread 100 tick',\@code) ;
trakbase::leading ('tm_10ms','spread100_tick','spread 100 tick') ;
trakbase::trailing('tm_10ms','spread100_tick','spread 100 tick') ;

1 ;

