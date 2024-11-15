# f2 
# folder 2 machine

# machine f2: formax exit, bail, and knife
#  init -> idle -> wait -> block -> settle -> square
#     -> full -> extend -> retract -> idle
#

# _f2_state_init
#
#  zone_dp -- empty

# turn value on
# turn formax offf
# put output dps in known states
#    -> knife up
#    -> bailer down
# wait for next to get to idle

@code=qw(
   >r
   r@ on 
   RF_FMAX_RUN       r@ _zone_dp_off
   RF_KNIFE_EXTEND   r@ _zone_dp_off
   RF_KNIFE_RETRACT  r@ _zone_dp_on
   RF_BAIL_EXTEND    r@ _zone_dp_off
   BOX_NONE RF_BOX   r@ dp.register.set
   r> drop
);
Trk::ex_compile("_f2_state_init-entry","folder 2 state init",\@code) ;

@code=qw(
   >r
   RF_KNIFE_EXTEND   r@ _zone_dp_off
   RF_KNIFE_RETRACT  r@ _zone_dp_on
   BOX_NONE RF_BOX   r@ dp.register.set
   0        RF_COUNT r@ dp.register.set
   0        RF_TOTAL r@ dp.register.set
   r> drop
);
Trk::ex_compile("_f2_state_init-exit","folder 2 state init",\@code) ;

@code=qw(
   >r
   r@ _f2_state_init-entry

   r@ _zone_tick
   r@ _zone_elapsed@ reset_duration rp.value.get > if
      ERF_SETUP_FAIL r@ _zone_error_msg
      SF_FAULT  RF_STATE r@ dp.register.set
   else
      RF_BAIL_ISHOME  r@ _zone_dp?
      RF_KNIFE_ISHOME r@ _zone_dp?     and
      RF_POCKET_EYE   r@ _zone_dp? not
      RF_PURGE        r@ _zone_dp?     or and
      RF_STATE  RF_NEXT r@ dp.register.get  dp.register.get SF_IDLE =  and  if
         r@ _f2_state_init-exit
         SF_IDLE  RF_STATE r@ dp.register.set
      then
   then
   r> drop
);
Trk::ex_compile("_f2_state_init","folder 2 state init",\@code) ;



# _f2_state_idle 
# zone_dp -- empty
# wait for printer to activate ... A print job should not be sent
# when this machine value is not off. Write box number and pages to be
# printed/collated into carton and count registers.

# turn value off
# stop formax

# wait for box register
# if box register 
#  -> state WAIT

@code=qw(
   >r
   r@ off
   RF_FMAX_RUN r@ _zone_dp_off
   r> drop
);
Trk::ex_compile("_f2_state_idle-entry","folder 2 state idle",\@code) ;

@code=qw( 
   >r
   r@ _f2_state_idle-entry

   RF_BOX r@ dp.register.get BOX_NONE = not if
      RF_BOX r@ dp.register.get BOX_ANON = if
        SF_SETTLE r@ _zone_state!
      else
        SF_WAIT r@ _zone_state!
      then
   then

   r> drop
);
Trk::ex_compile("_f2_state_idle","folder 2 state idle",\@code) ;



# _f2_state_wait
# zone_dp -- empty

# turn value on (should be on from prior state
# turn formax on ... paper is on the way

# make sure of knife and bailer position, knife up and bailer down
# wait (have for timeout)

# if you see paper, increment page count goto state block
@code=qw(
   >r
   r@ on 
   RF_FMAX_RUN      r@ dp.register.get on
   RF_KNIFE_EXTEND  r@ _zone_dp_off
   RF_KNIFE_RETRACT r@ _zone_dp_on
   RF_BAIL_EXTEND   r@ _zone_dp_off
   r> drop
);
Trk::ex_compile("_f2_state_wait-entry","folder 2 state wait",\@code) ;

@code=qw(
   >r
   RF_COUNT r@ dp.register.get
   1 +
   RF_COUNT r@ dp.register.set
   r> drop
);
Trk::ex_compile("_f2_state_wait-exit","folder 2 state wait",\@code) ;

@code=qw(
   >r
   r@ _f2_state_wait-entry
   r@ _zone_tick

   r@ _zone_elapsed@ printer_wait rp.value.get > if
      ERF_PRINTER_TIMEOUT r@ _zone_error_msg
      SF_FAULT            r@ _zone_state!
   else
      RF_EYE_OUT r@ dp.register.get dp.value.get if
         r@ _zone_elapsed@ paper_early rp.value.get < if
            ERF_PAPER_MISFEED r@ _zone_error_msg
            SF_FAULT r@ _zone_state!
         else
            r@ _f2_state_wait-exit
            SF_BLOCK r@ _zone_state!
         then
      then
   then
   r> drop
);
Trk::ex_compile("_f2_state_wait","folder 2 state wait",\@code) ;



# _f2_state_block
# zone_dp -- empty

# value on
# formax on 
# if eye stays blocked, paper jam
# When eye clears 
#  if count = pages -> settle
#  otherwise wait for more paper

@code=qw(
   >r
   r@ on
   RF_FMAX_RUN r@ _zone_dp_on
   r> drop
);
Trk::ex_compile("_f2_state_block-entry","folder 2 state block",\@code) ;

@code=qw(
   >r
   r@ on 
   RF_FMAX_RUN r@ _zone_dp_on
   r@ _zone_tick

   r@ _zone_elapsed@ paper_movement rp.value.get > if
      ERF_PAPER_JAM r@ _zone_error_msg
      SF_FAULT r@ _zone_state!
   else 
      RF_EYE_OUT r@ dp.register.get dp.value.get not if
         RF_COUNT r@ dp.register.get  RF_TOTAL r@ dp.register.get =
         RF_COUNT r@ dp.register.get  page_batch rp.value.get % 0 = or  if
            SF_SETTLE r@ _zone_state!
         else
            SF_WAIT r@ _zone_state!
         then
      then
   then

   r> drop
);
Trk::ex_compile("_f2_state_block","folder 2 state block",\@code) ;



# _f2_state_settle
# zone_dp -- empty

# value on
# formax done.
# After short timeout waiting for paper -> 
# extend bailer and goto state SQUARE

@code=qw(
   >r
   r@ on
   r> drop
);
Trk::ex_compile("_f2_state_settle-entry","folder 2 state settle",\@code) ;

@code=qw(
   >r
   RF_BAIL_EXTEND r@ _zone_dp_on
   r> drop
);
Trk::ex_compile("_f2_state_settle-exit","folder 2 state settle",\@code) ;

@code=qw(
   >r
   r@ _f2_state_settle-entry
   r@ _zone_tick

   r@ _zone_elapsed@  folder_settle rp.value.get > if
      r@ _f2_state_settle-exit
      SF_SQUARE r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile("_f2_state_settle","folder 2 state settle",\@code) ;



# _f2_state_square
# zone_dp -- empty

# value on
# formax off
# check for timeout
# if waited min time and bailer moved
#   goto state full full

@code=qw(
   >r
   r@ on
   RF_BAIL_EXTEND  r@ _zone_dp_on
   r> drop
);
Trk::ex_compile("_f2_state_square-entry","folder 2 state square",\@code) ;

@code=qw(
   >r
   r@ _f2_state_square-entry
   r@ _zone_tick

   r@ _zone_elapsed@ actuator_movement rp.value.get > if
     ERF_FOLDER_FAIL r@ _zone_error_msg
     SF_FAULT r@ _zone_state!
   else
     r@ _zone_elapsed@ folder_settle rp.value.get >
     RF_BAIL_ISACTIVE r@ _zone_dp? and if
        SF_FULL r@ _zone_state!
     then 
   then

   r> drop
);
Trk::ex_compile("_f2_state_square","folder 2 state square",\@code) ;



# _f2_state_full
# zone_dp -- empty

# value on
# formax off
# wait til next and not held
# then 
#   knife the paper
#   goto -> extend

@code=qw(
   >r
   r@ on 
   RF_COUNT r@ dp.register.get  RF_TOTAL r@ dp.register.get =  if
      RF_FMAX_RUN r@ _zone_dp_off
   else
      RF_FMAX_RUN r@ _zone_dp_on
   then
   r> drop
);
Trk::ex_compile("_f2_state_full-entry","folder 2 state full",\@code) ;

@code=qw(
   >r
   RF_BAIL_EXTEND   r@ _zone_dp_off
   RF_KNIFE_EXTEND  r@ _zone_dp_on
   RF_KNIFE_RETRACT r@ _zone_dp_off
   r> drop
);
Trk::ex_compile("_f2_state_full-exit","folder 2 state full",\@code) ;

@code=qw(
   >r
   r@ _f2_state_full-entry

   RF_STATE  RF_NEXT r@ dp.register.get  dp.register.get SF_IDLE =
   RF_NEXT r@ dp.register.get  dp.carton.get BOX_NONE = and
   RF_POCKET_EYE r@ _zone_dp? not  and
   RF_HOLD r@ dp.register.get dp.value.get not and if
      RF_BOX r@ dp.register.get BOX_ANON  = not
      RF_POCKET_EYE r@ _zone_dp? and if
         ERF_POCKET_FAULT r@ _zone_error_msg
         SF_FAULT         r@ _zone_state!
      else
         r@ _f2_state_full-exit
         SF_EXTEND r@ _zone_state!
      then
   then

   r> drop
);
Trk::ex_compile("_f2_state_full","folder 2 state full",\@code) ;



# _f2_state_extend
# zone_dp -- empty

# check movement timeout
#  - fault if 
# if knife down
#   return 

@code=qw(
   >r
   r@ on
   RF_COUNT r@ dp.register.get  RF_TOTAL r@ dp.register.get =  if
     RF_FMAX_RUN r@ _zone_dp_off
   else
     RF_FMAX_RUN r@ _zone_dp_on
   then
   r> drop
);
Trk::ex_compile("_f2_state_extend-enter","folder 2 state extend",\@code) ;

@code=qw(
   >r
   RF_KNIFE_EXTEND  r@ _zone_dp_off
   RF_KNIFE_RETRACT r@ _zone_dp_on
   r> drop
);
Trk::ex_compile("_f2_state_extend-exit","folder 2 state extend",\@code) ;

@code=qw(
   >r
   r@ _f2_state_extend-enter
   r@ _zone_tick

   r@ _zone_elapsed@ actuator_movement rp.value.get > if
     ERF_FOLDER_FAIL r@ _zone_error_msg
     SF_FAULT r@ _zone_state!
   else
      RF_KNIFE_ISACTIVE r@ _zone_dp? if
         r@ _f2_state_extend-exit
         SF_RETRACT r@ _zone_state!
      then
   then
 
   r> drop
);
Trk::ex_compile("_f2_state_extend","folder 2 state extend",\@code) ;



# _f2_state_retract
# zone_dp -- empty

@code=qw(
   >r
   r@ on
   RF_COUNT r@ dp.register.get  RF_TOTAL r@ dp.register.get =  if
     RF_FMAX_RUN r@ _zone_dp_off
   else
     RF_FMAX_RUN r@ _zone_dp_on
   then
   r> drop
) ;
Trk::ex_compile("_f2_state_retract-enter","folder 2 state retract",\@code) ;

@code=qw(
   >r
   r@ _f2_state_retract-enter
   r@ _zone_tick

   r@ _zone_elapsed@ actuator_movement rp.value.get > if
      ERF_FOLDER_FAIL r@ _zone_error_msg
      SF_FAULT r@ _zone_state!
   else
      r@ _zone_elapsed@ purge_grip_wait rp.value.get >
      RF_KNIFE_ISHOME r@ _zone_dp? and if
         RF_BOX r@ dp.register.get
            RF_BOX  RF_NEXT r@ dp.register.get  dp.register.set
         RF_COUNT r@ dp.register.get  RF_TOTAL r@ dp.register.get =  if
            0        RF_COUNT r@ dp.register.set
            0        RF_TOTAL r@ dp.register.set
            BOX_NONE RF_BOX   r@ dp.register.set
            SF_IDLE  r@ _zone_state!
         else
            SF_WAIT r@ _zone_state!
         then
      then
   then

   r> drop
);
Trk::ex_compile("_f2_state_retract","folder 2 state retract",\@code) ;



# _f2_state_fault
# zone_dp -- empty

@code=qw(
   >r
   r@ on 
   RF_FMAX_RUN r@ _zone_dp_off
   RF_KNIFE_EXTEND  r@ _zone_dp_off
   RF_KNIFE_RETRACT r@ _zone_dp_off
   r> drop
) ;
Trk::ex_compile("_f2_state_fault-enter","folder 2 state fault",\@code) ;
@code=qw(
   >r
   r@ _f2_state_fault-enter

   RF_RESET r@ dp.register.get dp.value.get if
      SF_INIT r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile("_f2_state_fault","folder 2 state fault",\@code) ;



# f2 machine
# empty --- empty
#
@code=qw(
   >r 

   RF_RUN         r@ dp.register.get tst
   RF_FMAX_ENABLE r@ dp.register.get      dp.value.set

   RF_RUN   r@ dp.register.get tst
   RF_DEBUG r@ dp.register.get tst not and  if
      r@ _zone_state@

      dup SF_INIT     = if r@ _f2_state_init     then
      dup SF_IDLE     = if r@ _f2_state_idle     then
      dup SF_WAIT     = if r@ _f2_state_wait     then
      dup SF_BLOCK    = if r@ _f2_state_block    then
      dup SF_SETTLE   = if r@ _f2_state_settle   then
      dup SF_SQUARE   = if r@ _f2_state_square   then
      dup SF_FULL     = if r@ _f2_state_full     then
      dup SF_EXTEND   = if r@ _f2_state_extend   then
      dup SF_RETRACT  = if r@ _f2_state_retract  then
      dup SF_FAULT    = if r@ _f2_state_fault    then
      drop
   then
   r> drop
);
Trk::ex_compile("f2_machine","folder 2 machine",\@code) ;

