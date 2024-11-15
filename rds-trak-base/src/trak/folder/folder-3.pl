# folder 3 machine

# folder state 3 has no actual I/O. It is a state machine for 
# to hold paper in the knifeless ungripped pocket
#
# _f3_state_init
# zone_dp -- empty


#
# when we have a eye through the pocket, this should be clear in idle
# after a short timeout

# value on
# set box to empty
#
@code=qw(
   >r
   r@ on 
   RF_CRIMP_CLOSE r@ _zone_dp_off
   BOX_NONE RF_BOX r@ dp.register.set
   r> drop
);
Trk::ex_compile("_f3_state_init-enter","folder 3 state init",\@code) ;

@code=qw(
   >r
   r@ off
   r> drop
);
Trk::ex_compile("_f3_state_init-exit","folder 3 state init",\@code) ;

@code=qw(
   >r
   r@ _f3_state_init-enter
   r@ _zone_tick

   r@ _zone_elapsed@ reset_duration rp.value.get > if
      ERF_SETUP_FAIL r@ _zone_error_msg
      SF_FAULT  RF_STATE r@ dp.register.set
   else
     RF_NEXT r@ dp.register.get  _zone_state@ SF_IDLE = 
     RF_CRIMPA_ISOPEN r@ _zone_dp?  and
     RF_CRIMPB_ISOPEN r@ _zone_dp?  and
     RF_POCKET_EYE r@ _zone_dp? not
     RF_PURGE r@ _zone_dp?          or  and
     r@ _zone_elapsed@ folder_pause rp.value.get > and if
        r@ _f3_state_init-exit
        SF_IDLE r@ _zone_state!
     then
   then

   r> drop
);
Trk::ex_compile("_f3_state_init","folder 3 state init",\@code);



# _f3_state_idle
# zone_dp -- empty

# value off
# if pocket blocked -> FAULT
# else
# if box register now set 
   # set zone on
   # goto to full
@code=qw(
   >r
   r@ off
   RF_CRIMP_CLOSE r@ _zone_dp_off
   r> drop
);
Trk::ex_compile("_f3_state_idle-enter","folder 3 state idle",\@code);
@code=qw(
   >r
   r@ on
   RF_CRIMP_CLOSE r@ _zone_dp_on
   r> drop
);
Trk::ex_compile("_f3_state_idle-exit","folder 3 state idle",\@code);
@code=qw(
   >r

   r@ _f3_state_idle-enter

   RF_F2 r@ dp.register.get dp.state.get dup
   SF_EXTEND = not swap
   SF_RETRACT = not and
   RF_KNIFE_ISACTIVE r@ _zone_dp? not and
   RF_POCKET_EYE r@ _zone_dp?
   RF_PURGE r@ _zone_dp? or and
   RF_BOX r@ dp.register.get  BOX_NONE = not and if
      r@ _f3_state_idle-exit
      SF_CRIMP r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile("_f3_state_idle","folder 3 state idle",\@code);



# _f3_state_crimp
# zone_dp -- empty

# value on
# we are full. 
# wait for next to be available ... 
# if available
#  copy box there
#  clear box here
#  go to "extend"

@code=qw(
   >r
   r@ on
   r@ RF_CRIMP_CLOSE _zone_dp_on
   r> drop
);
Trk::ex_compile("_f3_state_crimp-enter","folder 3 state full",\@code);

@code=qw(
   >r
   RF_CRIMP_CLOSE r@ _zone_dp_on
   r> drop
);
Trk::ex_compile("_f3_state_crimp-exit","folder 3 state full",\@code);

@code=qw(
   >r
   r@ _f3_state_crimp-enter
   r@ _zone_tick

   r@ _zone_elapsed@ actuator_movement rp.value.get > if
      ERF_FOLDER_FAIL r@ _zone_error_msg
      SF_FAULT r@ _zone_state!
   else
      r@ _zone_elapsed@ paper_movement rp.value.get > 
      RF_CRIMPA_ISOPEN r@ _zone_dp? not and
      RF_CRIMPB_ISOPEN r@ _zone_dp? not and  if
         r@ _f3_state_crimp-exit
         SF_FULL r@ _zone_state!
      then
   then

   r> drop
);
Trk::ex_compile("_f3_state_crimp","folder 3 state full",\@code);


# _f3_state_full
# zone_dp -- empty

# value on
# we are full. 
# wait for next to be available ... 
# if available
#  copy box there
#  clear box here
#  go to "extend"

@code=qw(
   >r
   r@ on
   RF_CRIMP_CLOSE r@ _zone_dp_on
   r> drop
);
Trk::ex_compile("_f3_state_full-enter","folder 3 state full",\@code) ;
@code=qw(
   >r
   RF_BOX r@ dp.register.get  RF_BOX RF_NEXT r@ dp.register.get  dp.register.set
   BOX_NONE RF_BOX r@ dp.register.set
  r> drop
) ;
Trk::ex_compile("_f3_state_full-exit","folder 3 state full",\@code) ;

@code=qw(
   >r

   r@ _f3_state_full-enter

   RF_STATE  RF_NEXT r@ dp.register.get  dp.register.get SF_IDLE =
   RF_NEXT r@ dp.register.get  dp.carton.get BOX_NONE = and
   RF_HOLD r@ dp.register.get  dp.value.get not and  if
      r@ _f3_state_full-exit
      SF_EXTEND r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile("_f3_state_full","folder 3 state full",\@code) ;


# _f3_state_extend
# zone_dp -- empty

# still on
# look at next, wait for it to be full
# if fault goto fault
# if next full return to idle

@code=qw(
   >r
   r@ on
   RF_CRIMP_CLOSE r@ _zone_dp_on
   r> drop
) ;
Trk::ex_compile("_f3_state_extend-enter","folder 3 state extend",\@code);

@code=qw(
   >r
   r@ on
   RF_CRIMP_CLOSE r@ _zone_dp_off
   r> drop
);
Trk::ex_compile("_f3_state_extend-exit","folder 3 state extend",\@code) ;

@code=qw(
   >r

   r@ _f3_state_extend-enter

   RF_NEXT r@ dp.register.get _zone_state@  SF_FAULT = if
      SF_FAULT r@ _zone_state!
   then

   RF_NEXT r@ dp.register.get _zone_state@  SF_GRIP = if
      r@ _f3_state_extend-exit
      SF_IDLE r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile("_f3_state_extend","folder 3 state extend",\@code) ;


# _f3_state_fault
# zone_dp -- empty
# wait for reset

@code=qw(
   >r

   r@ on

   RF_RESET r@ dp.register.get dp.value.get if
      SF_INIT r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile("_f3_state_fault","folder 3 state fault",\@code);



# f3 machine
@code=qw(
   >r

   RF_RUN   r@ dp.register.get tst
   RF_DEBUG r@ dp.register.get tst not and  if
      r@ _zone_state@

      dup SF_INIT     = if r@ _f3_state_init     then
      dup SF_IDLE     = if r@ _f3_state_idle     then
      dup SF_CRIMP    = if r@ _f3_state_crimp    then
      dup SF_FULL     = if r@ _f3_state_full     then
      dup SF_EXTEND   = if r@ _f3_state_extend   then
      dup SF_FAULT    = if r@ _f3_state_fault    then
      drop
   then
   r> drop
);
Trk::ex_compile("f3_machine","f3 machine",\@code);

