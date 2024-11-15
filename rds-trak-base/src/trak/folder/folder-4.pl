# folder 4 machine

# machine f4: grip (note idle is out and open
#  init -> moveslideout -> (if timeout) moveslide in (repeat if fail) -> 
#  setgrip -> setservo -> setslide   -> 
#  done -> retract -> idle -> grip -> extend -> 
#            xfer_grab -> xfer_release -> full -> 
#            lower -> release -> close -> 
#            raise -> (done)
#



# _f4_state_init
# zone_dp -- empty
# set grip

# open grip put slide to pocket
# when they are there, go to idle and set box to none

@code=qw(
   >r
   r@ on
   RF_XGRIP_CLOSE r@ _zone_dp_off
   RF_YGRIP_CLOSE r@ _zone_dp_on
   r> drop
) ;
Trk::ex_compile("_f4_state_init-enter","folder 4 state init",\@code) ;

@code=qw(
   >r
   r@ _f4_state_init-enter

   r@ _zone_tick
   r@ _zone_elapsed@ actuator_movement rp.value.get > if
      ERF_SETUP_FAIL r@ _zone_error_msg
      SF_FAULT  RF_STATE r@ dp.register.set
   else
     RF_YGRIP_ISOPEN r@ _zone_dp? not
     RF_XGRIP_ISOPEN r@ _zone_dp?     and if
       RF_SLIDE_ISHOME   r@ _zone_dp?
       RF_SLIDE_ISACTIVE r@ _zone_dp? or if
          RF_YGRIP_CLOSE r@ _zone_dp_off
          SF_SETSERVO    r@ _zone_state!
       else
          SF_SLIDEIN r@ _zone_state!
       then
     then
   then

   r> drop
);
Trk::ex_compile("_f4_state_init","folder 4 state init",\@code) ;

# _f4_state_slidein
# zone_dp -- empty

#
# now move servo into position
# when they are there, go to idle and set box to none

@code=qw(
   >r
   r@ on

   RF_SLIDE_EXTEND  r@ _zone_dp_off
   RF_SLIDE_RETRACT r@ _zone_dp_on
   RF_XGRIP_CLOSE   r@ _zone_dp_off
   RF_YGRIP_CLOSE   r@ _zone_dp_on
   r> drop
) ;
Trk::ex_compile("_f4_state_slidein-enter","folder 4 state slidein",\@code) ;
@code=qw(
   >r
   r@ _f4_state_slidein-enter
   r@ _zone_tick

   r@ _zone_elapsed@ actuator_movement rp.value.get > if
      SF_SLIDEOUT r@ _zone_state! 
   else
      RF_SLIDE_ISHOME r@ _zone_dp? 
      RF_YGRIP_ISOPEN r@ _zone_dp? not and
      RF_XGRIP_ISOPEN r@ _zone_dp?     and  if
         RF_YGRIP_CLOSE  r@ _zone_dp_off
         SF_SETSERVO r@ _zone_state!
      then
   then
   r> drop
);
Trk::ex_compile("_f4_state_slidein","folder 4 state slidein",\@code) ;
# _f4_state_slideout
# zone_dp -- empty


#
# now move servo into position
# when they are there, go to idle and set box to none

@code=qw(
   >r
   r@ on
   RF_SLIDE_EXTEND  r@ _zone_dp_on
   RF_SLIDE_RETRACT r@ _zone_dp_off
   RF_XGRIP_CLOSE   r@ _zone_dp_off
   RF_YGRIP_CLOSE   r@ _zone_dp_on
   r> drop
) ;
Trk::ex_compile("_f4_state_slideout-enter","folder 4 state slideout",\@code) ;

@code=qw(
   >r
   r@ _f4_state_slideout-enter
   r@ _zone_tick

   r@ _zone_elapsed@ actuator_movement rp.value.get > if
      ERF_SETUP_FAIL r@ _zone_error_msg
      SF_FAULT r@ _zone_state! 
   else
      RF_SLIDE_ISACTIVE r@ _zone_dp? 
      RF_YGRIP_ISOPEN   r@ _zone_dp? not and
      RF_XGRIP_ISOPEN   r@ _zone_dp?     and  if
         SF_SETSERVO r@ _zone_state!
      then
   then
   r> drop
);
Trk::ex_compile("_f4_state_slideout","folder 4 state slideout",\@code) ;


# _f4_state_setservo
# zone_dp -- empty

# 
# now move servo into position
# when they are there, go to idle and set box to none
# do not use (ank):  RF_SERVO r@ dp.register.get servo-on
# do not use (ank):  0 RF_SERVO r@ dp.register.get servo-send

@code=qw(
   >r
   r@ on
   0 RF_SERVO r@ dp.register.get dp.carton.set
   SVR_POWERUP RF_SERVO r@ dp.register.get dp.state.set
   RF_XGRIP_CLOSE r@ _zone_dp_off
   RF_YGRIP_CLOSE r@ _zone_dp_on
   r> drop
);
Trk::ex_compile("_f4_state_setservo-enter","folder 4 state setservo",\@code) ;

# do not use (ank):  RF_SERVO r@ dp.register.get servo-hold
@code=qw(
   >r
   RF_YGRIP_CLOSE  r@ _zone_dp_off
   r> drop
);
Trk::ex_compile("_f4_state_setservo-exit","folder 4 state setservo",\@code) ;
@code=qw(
   >r
   r@ _f4_state_setservo-enter

   RF_SERVO        r@ _zone_dp? not
   RF_YGRIP_ISOPEN r@ _zone_dp? not and
   RF_XGRIP_ISOPEN r@ _zone_dp?     and  if
      r@ _f4_state_setservo-exit
      SF_SETSLIDE r@ _zone_state!
   then
   r> drop
);
Trk::ex_compile("_f4_state_setservo","folder 4 state setservo",\@code) ;


# _f4_state_setslide
# zone_dp -- empty
# now move slide out

# open grip put slide to pocket
# when they are there, go to idle and set box to none

@code=qw(
   >r
   r@ on
   RF_SLIDE_EXTEND  r@ _zone_dp_on
   RF_SLIDE_RETRACT r@ _zone_dp_off
   RF_XGRIP_CLOSE   r@ _zone_dp_off
   RF_YGRIP_CLOSE   r@ _zone_dp_off
   r> drop
);
Trk::ex_compile("_f4_state_setslide-enter","folder 4 state setslide",\@code) ;
@code=qw(
   >r
   r@ _f4_state_setslide-enter

   r@ _zone_tick
   r@ _zone_elapsed@ actuator_movement rp.value.get > if
      ERF_SETUP_FAIL r@ _zone_error_msg
      SF_FAULT  RF_STATE r@ dp.register.set
   else

     RF_SLIDE_ISACTIVE r@ _zone_dp?
     RF_YGRIP_ISOPEN   r@ _zone_dp? and
     RF_XGRIP_ISOPEN   r@ _zone_dp? and  if
        SF_DONE r@ _zone_state!
     then
   then
   r> drop
);
Trk::ex_compile("_f4_state_setslide","folder 4 state setslide",\@code) ;



# _f4_state_done
# zone_dp -- empty

# value off ... wait for "box"
# if box set
#    clear go
#    go to state GRIP
#    y grip open
#    x grip open
# servo is home

# AHM 2016-06-01 : add logic to hold slide out until crimp is closed
#   (unless there is no paper to process)

@code=qw(
   >r
   BOX_NONE RF_BOX  r@ dp.register.set
   RF_GO            r@ _zone_dp_off
   RF_XGRIP_CLOSE   r@ _zone_dp_off
   RF_YGRIP_CLOSE   r@ _zone_dp_off
   r@ _zone_tick

   RF_CRIMPA_ISOPEN r@ _zone_dp? not
   RF_CRIMPB_ISOPEN r@ _zone_dp? not and
   RF_BOX RF_F2 r@ dp.register.get dp.register.get  BOX_NONE =
   RF_BOX RF_F3 r@ dp.register.get dp.register.get  BOX_NONE = and  or if

      r@ _zone_elapsed@ folder_grip rp.value.get > if
         RF_YGRIP_ISOPEN r@ _zone_dp? if
            SF_RETRACT       r@ _zone_state!
         else
            ERF_YGRIP_FAIL r@ _zone_error_msg
            SF_FAULT r@ _zone_state!
         then
      then

   then

   r> drop
);
Trk::ex_compile("_f4_state_done","folder 4 state done",\@code) ;



# _f4_state_retract
# zone_dp -- empty

# value on
#  check actuator timeout
#  if time + not grip open 
#     move slide out
#     goto state EXTEND

@code=qw(
   >r
   r@ on
   RF_SLIDE_EXTEND  r@ _zone_dp_off
   RF_SLIDE_RETRACT r@ _zone_dp_on
   RF_XGRIP_CLOSE   r@ _zone_dp_off
   RF_YGRIP_CLOSE   r@ _zone_dp_off
   r> drop
);
Trk::ex_compile("_f4_state_retract-enter","folder 4 state retract",\@code) ;

@code=qw(
   >r
   RF_SLIDE_EXTEND  r@ _zone_dp_off
   RF_SLIDE_RETRACT r@ _zone_dp_on
   RF_XGRIP_CLOSE   r@ _zone_dp_off
   RF_YGRIP_CLOSE   r@ _zone_dp_off
   r> drop
);
Trk::ex_compile("_f4_state_retract-exit","folder 4 state retract",\@code) ;

@code=qw(
   >r
   r@ _f4_state_retract-enter
   r@ _zone_tick

   r@ _zone_elapsed@ actuator_movement rp.value.get > if
      ERF_FOLDER_FAIL r@ _zone_error_msg
      SF_FAULT r@ _zone_state!
   else
      RF_SLIDE_ISHOME r@ _zone_dp? 
      RF_XGRIP_ISOPEN r@ _zone_dp? and if
         r@ _f4_state_retract-exit
         SF_IDLE r@ _zone_state!
      then
   then
   r> drop
);
Trk::ex_compile("_f4_state_retract","folder 4 state retract",\@code) ;



# _f4_state_idle
# zone_dp -- empty

# value on
#  check actuator timeout
#  if time + not grip open 
#     move slide out
#     goto state EXTEND

@code=qw(
   >r
   r@ off
   r> drop
);
Trk::ex_compile("_f4_state_idle-enter","folder 4 state idle",\@code) ;

@code=qw(
   >r
   RF_SLIDE_EXTEND  r@ _zone_dp_off
   RF_SLIDE_RETRACT r@ _zone_dp_on
   RF_XGRIP_CLOSE   r@ _zone_dp_on
   RF_YGRIP_CLOSE   r@ _zone_dp_off
   r> drop
);
Trk::ex_compile("_f4_state_idle-exit","folder 4 state idle",\@code) ;

@code=qw(
   >r
   r@ _f4_state_idle-enter

   RF_F2 r@ dp.register.get dp.state.get dup
   SF_EXTEND = not swap
   SF_RETRACT = not and
   RF_KNIFE_ISHOME r@ _zone_dp?       and
   RF_POCKET_EYE r@ _zone_dp?
   RF_PURGE      r@ _zone_dp?      or and
   RF_BOX r@ dp.register.get BOX_NONE = not and if
      r@ _f4_state_idle-exit
      SF_GRIP r@ _zone_state!
   then
   r> drop
);
Trk::ex_compile("_f4_state_idle","folder 4 state idle",\@code) ;



# _f4_state_grip
# zone_dp -- empty

# value on
#  check actuator timeout
#  if time + not grip open 
#     move slide out
#     goto state EXTEND

@code=qw(
   >r
   r@ on
   RF_SLIDE_EXTEND   r@ _zone_dp_off
   RF_SLIDE_RETRACT  r@ _zone_dp_on
   RF_XGRIP_CLOSE    r@ _zone_dp_on
   RF_YGRIP_CLOSE    r@ _zone_dp_off
   r> drop
);
Trk::ex_compile("_f4_state_grip-enter","folder 4 state init",\@code) ;

@code=qw(
   >r
   RF_SLIDE_EXTEND   r@ _zone_dp_on
   RF_SLIDE_RETRACT  r@ _zone_dp_off
   RF_YGRIP_CLOSE    r@ _zone_dp_off
   RF_XGRIP_CLOSE    r@ _zone_dp_on
   r> drop
);
Trk::ex_compile("_f4_state_grip-exit","folder 4 state init",\@code) ;

@code=qw(
   >r
   r@ _f4_state_grip-enter
   r@ _zone_tick

   r@ _zone_elapsed@ actuator_movement rp.value.get > if
      ERF_FOLDER_FAIL r@ _zone_error_msg
      SF_FAULT        r@ _zone_state!
   else
      RF_XGRIP_ISOPEN r@ _zone_dp? not
      RF_SLIDE_ISHOME r@ _zone_dp?     and  if
         r@ _f4_state_grip-exit
         SF_EXTEND r@ _zone_state!
      then
   then

   r> drop
);
Trk::ex_compile("_f4_state_grip","folder 4 state grip",\@code) ;



# _f4_state_extend
# zone_dp -- empty

# value on
# check timeout
# if slide out
#  then goto state FULL

@code=qw(
   >r
   r@ on

   RF_SLIDE_EXTEND   r@ _zone_dp_on
   RF_SLIDE_RETRACT  r@ _zone_dp_off
   RF_XGRIP_CLOSE    r@ _zone_dp_on
   RF_YGRIP_CLOSE    r@ _zone_dp_off
   r> drop
) ;
Trk::ex_compile("_f4_state_extend-enter","folder 4 state init",\@code) ;
@code=qw(
   >r
   RF_YGRIP_CLOSE    r@ _zone_dp_on
   r> drop
);
Trk::ex_compile("_f4_state_extend-exit","folder 4 state init",\@code) ;

@code=qw(
   >r
   r@ _f4_state_extend-enter
   r@ _zone_tick

   r@ _zone_elapsed@ actuator_movement rp.value.get > if
     ERF_FOLDER_FAIL r@ _zone_error_msg
     SF_FAULT r@ _zone_state!
   else
      RF_XYPAPER r@ _zone_dp? 
      RF_PURGE r@ _zone_dp? or
      RF_SLIDE_ISACTIVE r@ _zone_dp? and
      r@ _zone_elapsed@ folder_pause rp.value.get > and if
         r@ _f4_state_extend-exit
         SF_XFER_GRAB r@ _zone_state!
      then
   then
   r> drop
);
Trk::ex_compile("_f4_state_extend","folder 4 state extend",\@code) ;



# _f4_state_xfer_grab
# zone_dp -- empty

# value on
#  check actuator timeout
#  if time + not grip open 
#     move slide out
#     goto state EXTEND

@code=qw(
   >r
   r@ on 
   RF_SLIDE_EXTEND  r@ _zone_dp_on
   RF_SLIDE_RETRACT r@ _zone_dp_off
   RF_XGRIP_CLOSE   r@ _zone_dp_on
   RF_YGRIP_CLOSE   r@ _zone_dp_on
   r> drop
);
Trk::ex_compile("_f4_state_xfer_grab-enter","folder 4 state init",\@code) ;
@code=qw(
   >r
   RF_XGRIP_CLOSE   r@ _zone_dp_off
   RF_YGRIP_CLOSE   r@ _zone_dp_on
   r> drop
);
Trk::ex_compile("_f4_state_xfer_grab-exit","folder 4 state init",\@code) ;

@code=qw(
   >r
   r@ _f4_state_xfer_grab-enter
   r@ _zone_tick

   r@ _zone_elapsed@ actuator_movement rp.value.get > if
      ERF_FOLDER_FAIL r@ _zone_error_msg
      SF_FAULT        r@ _zone_state!
   else
      RF_XYPAPER        r@ _zone_dp?
      RF_PURGE          r@ _zone_dp?     or
      RF_YGRIP_ISOPEN   r@ _zone_dp? not and
      RF_SLIDE_ISACTIVE r@ _zone_dp?     and  if
         r@ _zone_elapsed@ folder_grip rp.value.get > if
            r@ _f4_state_xfer_grab-exit
            SF_XFER_RELEASE r@ _zone_state!
         then
      then
   then
   r> drop
);
Trk::ex_compile("_f4_state_xfer_grab","folder 4 state grip",\@code) ;



# _f4_state_xfer_release
# zone_dp -- empty

# value on
#  check actuator timeout
#  if time + not grip open 
#     move slide out
#     goto state EXTEND

@code=qw(
   >r
   r@ on
   RF_SLIDE_EXTEND  r@ _zone_dp_on
   RF_SLIDE_RETRACT r@ _zone_dp_off
   RF_XGRIP_CLOSE   r@ _zone_dp_off
   RF_YGRIP_CLOSE   r@ _zone_dp_on
   r> drop
);
Trk::ex_compile("_f4_state_xfer_release-enter","folder 4 state init",\@code) ;
@code=qw(
   >r
   RF_XGRIP_CLOSE    r@ _zone_dp_off
   RF_YGRIP_CLOSE    r@ _zone_dp_on
   RF_SLIDE_EXTEND   r@ _zone_dp_on
   RF_SLIDE_RETRACT  r@ _zone_dp_off
   r> drop
);
Trk::ex_compile("_f4_state_xfer_release-exit","folder 4 state init",\@code) ;

@code=qw(
   >r
   r@ _f4_state_xfer_release-enter
   r@ _zone_tick

   r@ _zone_elapsed@ actuator_movement rp.value.get > if
      ERF_FOLDER_FAIL r@ _zone_error_msg
      SF_FAULT        r@ _zone_state!
   else
      RF_XGRIP_ISOPEN   r@ _zone_dp?
      RF_SLIDE_ISACTIVE r@ _zone_dp?     and  if
         r@ _f4_state_xfer_release-exit
         SF_FULL r@ _zone_state!
      then
   then
   r> drop
);
Trk::ex_compile("_f4_state_xfer_release","folder 4 state grip",\@code) ;



# _f4_state_full
#  ... wait for _go to be set
# zone_dp -- empty

# value on
# no timeout
# wait for go (and not held)
# if go and !held
#  release grip
#  turn "go" off
#  goto state RELEASE

@code=qw(
   >r
   r@ on
   r> drop
);
Trk::ex_compile("_f4_state_full-enter","folder 4 state init",\@code) ;

@code=qw(
   >r
   RF_XGRIP_CLOSE r@ _zone_dp_off
   RF_GO          r@ dp.register.get off
   r> drop
);
Trk::ex_compile("_f4_state_full-exit","folder 4 state init",\@code) ;

@code=qw(
   >r
   r@ _f4_state_full-enter

   RF_GO   r@ _zone_dp?
   RF_HOLD r@ _zone_dp? not and  if
      r@ _f4_state_full-exit
      SF_LOWER r@ _zone_state!
   then
   r> drop
);
Trk::ex_compile("_f4_state_full","folder 4 state full",\@code) ;



# _f4_state_release
# zone_dp -- empty

# value still on 
# check actuator timeout
# if grip open and chute paper blocked
#  return slide
#  set box to none
#  reset "go"
#  goto state RETRACT

@code=qw(
   >r
   r@ on
   RF_SLIDE_RETRACT r@ _zone_dp_off
   RF_SLIDE_EXTEND  r@ _zone_dp_on
   RF_XGRIP_CLOSE   r@ _zone_dp_off
   RF_YGRIP_CLOSE   r@ _zone_dp_on
   RF_PURGE r@ _zone_dp? if
      1 RF_SERVO r@ dp.register.get dp.carton.set
   else
      r@ dp.carton.get 1000 % bx.data.get
      RF_SERVO r@ dp.register.get dp.carton.set
   then
   r> drop
);
Trk::ex_compile("_f4_state_lower-enter","folder 4 state init",\@code) ;

@code=qw(
   >r
   RF_SLIDE_RETRACT r@ _zone_dp_off
   RF_SLIDE_EXTEND  r@ _zone_dp_on
   RF_XGRIP_CLOSE   r@ _zone_dp_off
   RF_YGRIP_CLOSE   r@ _zone_dp_on
   BOX_NONE RF_BOX  r@ dp.register.set
   RF_GO r@ dp.register.get off
   r> drop
);
Trk::ex_compile("_f4_state_lower-exit","folder 4 state init",\@code) ;

@code=qw(
   >r
   r@ _f4_state_lower-enter
   r@ _zone_tick

   r@ _zone_elapsed@ actuator_movement rp.value.get > if
      ERF_FOLDER_FAIL r@ _zone_error_msg
      SF_FAULT        r@ _zone_state!
   else
      RF_SERVO        r@ _zone_dp? not
      r@ _zone_elapsed@ purge_grip_wait rp.value.get > and
      RF_YGRIP_ISOPEN r@ _zone_dp? not and
      RF_XGRIP_ISOPEN r@ _zone_dp?     and  if
         r@ _f4_state_lower-exit
         SF_RELEASE r@ _zone_state!
      then
   then
   r> drop
);
Trk::ex_compile("_f4_state_lower","folder 4 state release",\@code) ;



# _f4_state_release
# zone_dp -- empty

# value still on 
# check actuator timeout
# if grip open and chute paper blocked
#  return slide
#  set box to none
#  reset "go"
#  goto state RETRACT

# TODO  get drop position from box

@code=qw(
   >r
   r@ on
   RF_SLIDE_RETRACT r@ _zone_dp_off
   RF_SLIDE_EXTEND  r@ _zone_dp_on
   RF_XGRIP_CLOSE   r@ _zone_dp_off
   RF_YGRIP_CLOSE   r@ _zone_dp_off
   r> drop
);
Trk::ex_compile("_f4_state_release-enter","folder 4 state init",\@code) ;
@code=qw(
   >r
   RF_SLIDE_RETRACT r@ _zone_dp_off
   RF_SLIDE_EXTEND  r@ _zone_dp_on
   BOX_NONE RF_BOX  r@ dp.register.set

   RF_GO r@ dp.register.get off
   r> drop
);
Trk::ex_compile("_f4_state_release-exit","folder 4 state init",\@code) ;

@code=qw(
   >r
   r@ _f4_state_release-enter
   r@ _zone_tick

   r@ _zone_elapsed@ actuator_movement rp.value.get > if
      ERF_FOLDER_FAIL r@ _zone_error_msg
      SF_FAULT        r@ _zone_state!
   else
      r@ _zone_elapsed@ purge_grip_wait rp.value.get > if
         RF_YGRIP_ISOPEN r@ _zone_dp?  if
            r@ _f4_state_release-exit
            SF_RAISE r@ _zone_state!
         then
      then
   then
   r> drop
);
Trk::ex_compile("_f4_state_release","folder 4 state release",\@code) ;



# _f4_state_raise
# zone_dp -- empty

# check actuator timeout
# if slide home
# goto state IDLE

@code=qw(
   >r
   r@ on 
   RF_SLIDE_RETRACT r@ _zone_dp_off
   RF_SLIDE_EXTEND  r@ _zone_dp_on
   RF_XGRIP_CLOSE   r@ _zone_dp_off
   RF_YGRIP_CLOSE   r@ _zone_dp_on
   0 RF_SERVO r@ dp.register.get dp.carton.set
   r> drop
);
Trk::ex_compile("_f4_state_raise-enter","folder 4 state init",\@code) ;

@code=qw(
   >r
   RF_SLIDE_RETRACT r@ _zone_dp_off
   RF_SLIDE_EXTEND  r@ _zone_dp_on
   RF_XGRIP_CLOSE   r@ _zone_dp_off
   RF_YGRIP_CLOSE   r@ _zone_dp_off
   BOX_NONE RF_BOX  r@ dp.register.set
   RF_GO            r@ dp.register.get off
   r> drop
);
Trk::ex_compile("_f4_state_raise-exit","folder 4 state init",\@code) ;

@code=qw(
   >r
   r@ _f4_state_raise-enter
   r@ _zone_tick

   r@ _zone_elapsed@ actuator_movement rp.value.get > if
      ERF_FOLDER_FAIL r@ _zone_error_msg
      SF_FAULT        r@ _zone_state!
   else
      RF_XYPAPER r@ _zone_dp? not
      r@ _zone_elapsed@ folder_down rp.value.get >  and
      RF_SERVO   r@ _zone_dp? not and  if
         r@ _f4_state_raise-exit
         SF_DONE r@ _zone_state!
      then
   then
   r> drop
);
Trk::ex_compile("_f4_state_raise","folder 4 state retract",\@code) ;



# _f4_state_fault
# zone_dp -- empty
@code=qw(
   >r
   r@ on
   RF_GO r@ dp.register.get off
   RF_SLIDE_EXTEND  r@ _zone_dp_off
   RF_SLIDE_RETRACT r@ _zone_dp_off

   RF_RESET r@ dp.register.get dp.value.get if
      SF_INIT RF_STATE r@ dp.register.set
   then
   r> drop
);
Trk::ex_compile("_f4_state_fault","folder 4 state fault",\@code) ;



# f4 machine
# zone_dp -- empty
@code=qw(
   >r
   RF_RUN   r@ dp.register.get tst 
   RF_DEBUG r@ dp.register.get tst not and  if

      RF_STATE r@ dp.register.get  SF_FAULT = not
      RF_SERVO_FAULT r@ _zone_dp? and if
         ERF_SERVO_FAULT r@ _zone_error_msg
         SF_FAULT r@  _zone_state!
      then
      RF_STATE r@ dp.register.get

      dup SF_INIT         = if r@ _f4_state_init         then
      dup SF_SETSERVO     = if r@ _f4_state_setservo     then
      dup SF_SETSLIDE     = if r@ _f4_state_setslide     then
      dup SF_DONE         = if r@ _f4_state_done         then
      dup SF_RETRACT      = if r@ _f4_state_retract      then
      dup SF_IDLE         = if r@ _f4_state_idle         then
      dup SF_GRIP         = if r@ _f4_state_grip         then
      dup SF_EXTEND       = if r@ _f4_state_extend       then
      dup SF_XFER_GRAB    = if r@ _f4_state_xfer_grab    then
      dup SF_XFER_RELEASE = if r@ _f4_state_xfer_release then
      dup SF_FULL         = if r@ _f4_state_full         then
      dup SF_LOWER        = if r@ _f4_state_lower        then
      dup SF_RELEASE      = if r@ _f4_state_release      then
      dup SF_RAISE        = if r@ _f4_state_raise        then
      dup SF_SLIDEIN      = if r@ _f4_state_slidein      then
      dup SF_SLIDEOUT     = if r@ _f4_state_slideout     then
      dup SF_FAULT        = if r@ _f4_state_fault        then
      drop
   then
   r> drop
);
Trk::ex_compile("f4_machine","f4 machine",\@code) ;
