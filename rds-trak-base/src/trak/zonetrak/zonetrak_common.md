# zonetrak_common.pl

## Dependencies

| #!/bin/perl -I. | Includes current directory in Perl compilation |
| --- | --- |
| require "spread.pl"; | Distribution of 10ms timers |
| my @code; | Array variable “code” |
| Trk::include( "zonetrak.h", Register Type ); | Includes C Definitions |
| use constant { Constants } ; | Links C Definitions to Perl Constants |

## Messages

What is this?

\# prototypes

@code=qw( 1 drop );

Trk::ex_compile( '\_zone_mode?', 'zone mode set?', \\@code );

mb_npost?

\# ----- M e s s a g e s -----

#

\# \_zone_create_msg ( dp -- )

\# utility to send a notification message on box creation

\# sends:

\# MSG_ZONE_CREATE zone 0 box msec

\# note that the third field is merely a placeholder to keep the zone

\# message formats consistent

@code=qw(

\>r

&nbsp;

tm_1ms dp.counter.get

REG_BOX r@ dp.register.get

0

r@

MSG_ZONE_CREATE

5 mb_npost

&nbsp;

r> drop

);

Trk::ex_compile( "\_zone_create_msg", "zone create msg", \\@code );

&nbsp;

\# \_zone_state_msg ( dp -- )

\# utility to send a notification message on state change

\# sends:

\# MSG_ZONE_STATE zone state box msec

@code=qw(

\>r

&nbsp;

tm_1ms dp.counter.get

REG_BOX r@ dp.register.get

REG_STATE r@ dp.register.get

r@

MSG_ZONE_STATE

5 mb_npost

&nbsp;

r> drop

);

Trk::ex_compile( "\_zone_state_msg", "zone state msg", \\@code );

&nbsp;

\# \_zone_choice_msg ( dp -- )

\# utility to send a notification message on selection (e.g. merge input

\# lane or divert destination)

\# sends:

\# MSG_ZONE_CHOICE zone choice box msec

@code=qw(

\>r

&nbsp;

tm_1ms dp.counter.get

REG_BOX r@ dp.register.get

REG_CHOICE r@ dp.register.get

r@

MSG_ZONE_CHOICE

5 mb_npost

&nbsp;

r> drop

);

Trk::ex_compile( "\_zone_choice_msg", "zone choice msg", \\@code );

&nbsp;

\# \_zone_err_msg ( code dp -- )

\# utility to send a notification message on error/jam detection

\# sends:

\# MSG_ZONE_ERR zone code box msec

@code=qw(

\>r

&nbsp;

MODE_LAX r@ \_zone_mode? if

drop

else

&nbsp;

tm_1ms dp.counter.get swap

REG_BOX r@ dp.register.get swap

r@

MSG_ZONE_ERR

5 mb_npost

&nbsp;

then

&nbsp;

r> drop

);

Trk::ex_compile( "\_zone_error_msg", "zone error msg", \\@code );

&nbsp;

\# \_zone_reset_msg ( dp -- )

\# utility to send a notification message on zone reset

\# sends:

\# MSG_ZONE_RESET zone 0 0 msec

@code=qw(

\>r

&nbsp;

tm_1ms dp.counter.get

0

0

r@

MSG_ZONE_RESET

5 mb_npost

&nbsp;

r> drop

);

Trk::ex_compile( "\_zone_reset_msg", "zone reset msg", \\@code );

## Utilities (? – is, @ - get, ! - set )

### Summary

| **Word** | **Description \[DEFAULT\]** | **Input - I1 I2 …** | **Output** |
| --- | --- | --- | --- |
| **?** | **Is Value Functions** |     |     |
| \_zone_dp? | Returns dp(register) value \[OFF\] | register dp | DPvalue(dp(reg)) |
| \_zone_dp_not? | Returns NOT dp(register) value \[OFF\] | register dp | NOT DPvalue(dp(register)) |
| \_zone_mode? | Returns if mode is set in bitmask | mode dp | if dp(REG_MODE) and 1 << mode |
| \_zone_next_avail? | Returns if next zone is available \[OFF\] | dp  | NOT DPvalue(dp(REGNEXT)) |
| \_zone_run? | Returns if area run is on \[ON\] | dp  | DPValue(dp(REG_RUN) |
| \_zone_fault? | Returns if zone or area fault occurred | dp  | DPValue(dp(REG_FAULT)) or DPValue(dp(REG_ZONEFAULT)) |
| \_zone_box_valid? | Returns if dp(REG_BOX) is valid | dp  | if 0 < dp(REG_BOX) < 1000 |
| \_zone_slug_release? | Returns if zone should slug release | dp  | If dp(MODE) = MODE_SLUG and dp(REG_BOX) = NONE and dp(REG_TIMER) > dp(REG_TM_SLUG) |
| **@** | **Get Value Functions** |     |     |
| \_zone_rp@ | Returns rp in dp(register) \[0\] | register dp | RPvalue(dp(register)) |
| \_zone_box@ | Returns dp(REG_BOX) | dp  | dp(REG_BOX) |
| \_zone_elapsed@ | Returns dp(REG_TIMER) | dp  | dp(REG_TIMER) |
| \_zone_state@ | Returns dp(STATE) | dp  | dp(REG_STATE) |
| \_zone_first@ | Returns dp(FIRST) \[1\] | dp  | dp(FIRST) |
| \_zone_last@ | Returns dp(LAST) \[999\] | dp  | dp(LAST) |
| \_zone_box_dir@ | Get destination from Box Data and Convert to Local Direction \[DIR_NONE\] | dp  | (<sub>1</sub>BOXDATA(dp(REG_BOX)) >> (<sub>2</sub>3 \* dp(REG_OFFSET))<sub>2</sub>)<sub>1</sub> and 111<sub>2</sub> |
| \_zone_dest@ | Return dp for in register for dir input | dir dp | dp  |
| **!** | **Set Value Functions** |     |     |
| \_zone_box! | Set dp(REG_BOX) = val |     | val dp |
| \_zone_state! | Set dp(REG_STATE) = val<br><br>If there is a state change, clear dp(REG_TIMER), and dp \_zone_state_msg |     | val dp |
| Helper | Description |     | Input |
| \_zone_dp_on | dp(register) on |     | register dp |
| \_zone_dp_off | dp(register) off |     | register dp |
| \_zone_set_output | dp(REG_OUTPUT) on |     | dp  |
| \_zone_tick | dp(REG_TIMER) = dp(REG_TIMER) + 1 |     | dp  |
| \_zone_timer_clr | dp(TIMER) = 0 |     | dp  |
| \_zone_motor_next | ?   |     | dp  |
| \_zone_box_new | Based on dp(REG_CURRENT) + 1, dp(FIRST), dp(LAST): Set dp(REG_BOX) to dp(REG_CURRENT) + 1 or dp(FIRST) |     | dp  |
| \_zone_box_pass | dp(NEXT)(BOX) = dp(REG_BOX) or BOX_ANON, dp(BOX) = BOX_NONE |     | dp  |

### Psuedocode

#### \_zone_dp?

(register dp : DPvalue[dp[register]])

| Code               | Description                         | Data Stack                                  | Return Stack |
| ------------------ | ----------------------------------- | ------------------------------------------- | ------------ |
| Function Call      | On Call                             | register dp                                 |              |
| \>r                | Put last value on return stack      | register                                    | dp           |
| r@ 0 > if          | If dp is valid                      | register dp 0 > if                          | dp           |
| r@ dp.register.get | Get register for dp                 | dp[register]                                | dp           |
| dup 0 > if         | If register for dp is valid         | dp[register] dp[register] 0 > if            | dp           |
| dp.value.get       | Get value at dp[register]           | **DPvalue[dp[register]]**                   | dp           |
| else               | If register for dp is not valid     | dp[register]                                | dp           |
| drop 0 then        | Drop stack value and leave 0        | ~~dp[register]~~ **0**                      | dp           |
| else               | If dp is not valid                  | register                                    | dp           |
| drop 0 then        | Drop stack value and leave 0        | ~~register~~ **0**                          | dp&nbsp;     |
| r> drop            | Put return value back on data stack | (**DPvalue[dp[register]]** or **0**) ~~dp~~ | ~~dp~~       |

#### \_zone_dp_not? 

(register dp : not DPvalue[dp[register]])

| Code               | Description                                  | Data Stack                                      | Return Stack |
| ------------------ | -------------------------------------------- | ----------------------------------------------- | ------------ |
| Function Call      | On Call                                      | register dp                                     |              |
| \>r                | Put last value on return stack               | register                                        | dp           |
| r@ 0 > if          | If dp is valid                               | register dp 0 > if                              | dp           |
| r@ dp.register.get | Get register for dp                          | dp[register]                                    | dp           |
| dup 0 > if         | If register for dp is valid                  | dp[register] dp[register] 0 > if                | dp           |
| dp.value.get not   | Get NOT value at dp[register]                | **NOT DPvalue[dp[register]]**                   | dp           |
| else               | If register for dp is not valid              | dp[register]                                    | dp           |
| drop 0 then        | Drop stack value and leave 0                 | ~~dp[register]~~ **0**                          | dp           |
| else               | If dp is not valid                           | register                                        | dp           |
| drop 0 then        | Drop stack value and leave 0                 | ~~register~~ **0**                              | dp&nbsp;     |
| r> drop            | Put return value back on data stack and drop | (**NOT DPvalue[dp[register]]** or **0**) ~~dp~~ | ~~dp~~       |

#### \_zone_dp_on

(register dp : )

| Code               | Description                                  | Data Stack                       | Return Stack |
| ------------------ | -------------------------------------------- | -------------------------------- | ------------ |
| Function Call      | On Call                                      | register dp                      |              |
| \>r                | Put last value on return stack               | register                         | dp           |
| r@ 0 > if          | If dp is valid                               | register dp 0 > if               | dp           |
| r@ dp.register.get | Get register for dp                          | dp[register]                     | dp           |
| dup 0 > if         | If register for dp is valid                  | dp[register] dp[register] 0 > if | dp           |
| on                 | Turn on dp[register]                         | dp[register] on                  | dp           |
| else               | If register for dp is not valid              | dp[register]                     | dp           |
| drop then          | Drop stack value                             |                                  | dp           |
| else               | If dp is not valid                           | register                         | dp           |
| drop then          | Drop stack value                             |                                  | dp           |
| r> drop            | Put return value back on data stack and drop | ~~dp~~                           | ~~dp~~       |

#### \_zone_dp_off 

(register dp : )

| Code               | Description                                  | Data Stack                       | Return Stack |
| ------------------ | -------------------------------------------- | -------------------------------- | ------------ |
| Function Call      | On Call                                      | register dp                      |              |
| \>r                | Put last value on return stack               | register                         | dp           |
| r@ 0 > if          | If dp is valid                               | register dp 0 > if               | dp           |
| r@ dp.register.get | Get register for dp                          | dp[register]                     | dp           |
| dup 0 > if         | If register for dp is valid                  | dp[register] dp[register] 0 > if | dp           |
| off                | Turn on dp[register]                         | dp[register] off                 | dp           |
| else               | If register for dp is not valid              | dp[register]                     | dp           |
| drop then          | Drop stack value                             |                                  | dp           |
| else               | If dp is not valid                           | register                         | dp           |
| drop then          | Drop stack value                             |                                  | dp           |
| r> drop            | Put return value back on data stack and drop | ~~dp~~                           | ~~dp~~       |

#### \_zone_rp@ 

(register dp : RPValue[dp[register]])

| Code               | Description                                  | Data Stack                                  | Return Stack |
| ------------------ | -------------------------------------------- | ------------------------------------------- | ------------ |
| Function Call      | On Call                                      | register dp                                 |              |
| \>r                | Put last value on return stack               | register                                    | dp           |
| r@ 0 > if          | If dp is valid                               | register ~~dp~~ ~~0~~                       | dp           |
| r@ dp.register.get | Get register for dp                          | dp[register]                                | dp           |
| dup 0 > if         | If register for dp is valid                  | dp[register] ~~dp[register] 0~~             | dp           |
| rp.value.get       | Get value at dp[register]                    | **RPvalue[dp[register]]**                   | dp           |
| else               | If register for dp is not valid              | dp[register]                                | dp           |
| drop 0 then        | Drop stack value and leave 0                 | ~~dp[register]~~ **0**                      | dp           |
| else               | If dp is not valid                           | register                                    | dp           |
| drop 0 then        | Drop stack value and leave 0                 | ~~register~~ **0**                          | dp&nbsp;     |
| r> drop            | Put return value back on data stack and drop | (**RPvalue[dp[register]]** or **0**) ~~dp~~ | ~~dp~~       |

#### \_zone_box@

(dp : dp[REG_BOX])

| Code            | Description              | Data Stack  |
| --------------- | ------------------------ | ----------- |
| Function Call   | On Call                  | dp          |
| REG_BOX swap    | Put REG_BOX on stack     | dp REG_BOX  |
| swap            | Swap last 2 stack values | REG_BOX dp  |
| dp.register.get | Get value at dp[REG_BOX] | dp[REG_BOX] |

#### \_zone_box! 

(value dp : )

| Code            | Description                                  | Data Stack          | Return Stack |
| --------------- | -------------------------------------------- | ------------------- | ------------ |
| Function Call   | On Call                                      | value dp            |              |
| \>r             | Put last data stack value on return stack    | value               | dp           |
| r@ 0 > if       | If valid dp                                  | value ~~dp~~ ~~0~~  | dp           |
| REG_BOX r@      | Put REG_BOX and dp on stack                  | value REG_BOX dp    | dp           |
| dp.register.set | Set dp[REG_BOX]                              | dp[REG_BOX] = value | dp           |
| else            | If not valid dp                              | value               | dp           |
| drop then       | Drop data stack value                        | ~~value~~           | dp           |
| r> drop         | Put return value back on data stack and drop | ~~dp~~              | ~~dp~~       |

#### \_zone_tick 

(dp : )

| Code            | Description                                  | Data Stack                        | Return Stack |
| --------------- | -------------------------------------------- | --------------------------------- | ------------ |
| Function Call   | On Call                                      | dp                                |              |
| \>r             | Put last data stack value on return stack    | ~~dp~~                            | dp           |
| REG_TIMER r@    | Put REG_TIMER and dp on stack                | REG_TIMER dp                      | dp           |
| dp.register.get | Get dp[REG_TIMER]                            | dp[REG_TIMER]                     | dp           |
| 1 +             | Add one to dp[REG_TIMER]                     | dp[REG_TIMER] + 1                 | dp           |
| REG_TIMER r@    | Put REG_TIMER and dp on stack                | (dp[REG_TIMER] + 1) REG_TIMER dp  | dp           |
| dp.register.set | Set dp[REG_TIMER] to new value               | dp[REG_TIMER] = dp[REG_TIMER] + 1 | dp           |
| r> drop         | Put return value back on data stack and drop | ~~dp~~                            | ~~dp~~       |

#### \_zone_timer_clr 

(dp : )

| Code            | Description              | Data Stack        |
| --------------- | ------------------------ | ----------------- |
| Function Call   | On Call                  | dp                |
| 0               | Put 0 on stack           | dp 0              |
| swap            | Swap last 2 stack values | 0 dp              |
| REG_TIMER       | Put REG_TIMER on stack   | 0 dp REG_TIMER    |
| swap            | Swap last 2 stack values | 0 REG_TIMER dp    |
| dp.register.set | Set dp(REG_TIMER) = 0    | dp[REG_TIMER] = 0 |

#### \_zone_elapsed@ 

(dp : dp[REG_TIMER])

| Code            | Description              | Data Stack        |
| --------------- | ------------------------ | ----------------- |
| Function Call   | On Call                  | dp                |
| REG_TIMER       | Put REG_TIMER on stack   | dp REG_TIMER      |
| swap            | Swap last 2 stack values | REG_TIMER dp      |
| dp.register.get | Get dp[REG_TIMER]        | **dp[REG_TIMER]** |

#### \_zone_state@ 

(dp : dp[REG_STATE])

| Code            | Description              | Data Stack        |
| --------------- | ------------------------ | ----------------- |
| Function Call   | On Call                  | dp                |
| REG_STATE       | Put REG_STATEon stack    | dp REG_STATE      |
| swap            | Swap last 2 stack values | REG_STATE dp      |
| dp.register.get | Get dp[REG_STATE]        | **dp[REG_STATE]** |

#### \_zone_state! 

(value dp : )

| Code                | Description                                         | Data Stack                        | Return Stack |
| ------------------- | --------------------------------------------------- | --------------------------------- | ------------ |
| Function Call       | On Call                                             | value dp                          |              |
| \>r                 | Put last data stack value on return stack           | value ~~dp~~                      | dp           |
| dup REG_STATE r@    | Duplicate last value, Put REG_STATE and dp on stack | value value REG_STATE dp          | dp           |
| dp.register.get     | Get dp[REG_STATE]                                   | value value dp[REG_STATE]         | dp           |
| = not if            | If dp[REG_STATE] != value                           | value ~~value~~ ~~dp[REG_STATE]~~ | dp           |
| r@ \_zone_timer_clr | Clear dp[REG_TIMER]                                 | value  ~~dp~~                     | dp           |
| REG_STATE r@        | Put REG_STATE and dp on stack                       | value REG_STATE dp                | dp           |
| dp.register.set     | Set dp[REG_STATE] = value                           | dp[REG_STATE] = value             | dp           |
| r@ \_zone_state_msg | ?                                                   |                                   | dp           |
| else                | If dp[REG_STATE] = value                            | value                             | dp           |
| drop then           | Drop stack value                                    | ~~value~~                         | dp           |
| r> drop             | Put return value back on data stack and drop        | ~~dp~~                            | ~~dp~~       |

#### \_zone_motor_next 

(dp : )

| Code                           | Description                                  | Data Stack                | Return Stack |
| ------------------------------ | -------------------------------------------- | ------------------------- | ------------ |
| Function Call                  | On Call                                      | dp                        |              |
| \>r                            | Put last data stack value on return stack    | ~~dp~~                    | dp           |
| r@ \_zone_elapsed@             | Get dp[REG_TIMER]                            | dp[REG_TIMER]             | dp           |
| 2 < if                         | If dp[REG_TIMER] < 2                         | ~~dp[REG_TIMER]~~ ~~2~~   | dp           |
| REG_MOTOR r@ \_zone_dp_not? if | If dp[REG_MOTOR] is not on                   | ~~!dp[REG_MOTOR]~~ if     | dp           |
| REG_MOTOR r@ \_zone_dp_off     | dp[REG_MOTOR] off                            | dp[REG_MOTOR] off         | dp           |
| else                           | If dp[REG_MOTOR] is on                       |                           | dp           |
| REG_MOTOR r@ \_zone_dp_on then | dp[REG_MOTOR] on                             | dp[REG_MOTOR] on          | dp           |
| r@ \_zone_tick                 | Increment dp[REG_TIMER]                      | ~~dp~~                    | dp           |
| else                           | If dp[REG_TIMER] >= 2                        |                           | dp           |
| REG_MOTOR REG_NEXT r@          | Put REG_MOTOR, REG_NEXT, and dp on stack     | REG_MOTOR REG_NEXT dp     | dp           |
| dp.register.get                | Get dp[REG_NEXT]                             | REG_MOTOR dp[REG_NEXT]    | dp           |
| _zone_dp_not? if               | If dpNEXT[REG_MOTOR] is not on               | ~~!dpNEXT[REG_MOTOR]~~ if | dp           |
| REG_MOTOR r@ \_zone_dp_off     | dp[REG_MOTOR] off                            | dp[REG_MOTOR] off         | dp           |
| else                           | If dpNEXT[REG_MOTOR] is on                   |                           | dp           |
| REG_MOTOR r@ \_zone_dp_on      |                                              | dp[REG_MOTOR] on          | dp           |
| r@ \_zone_tick then then       | Increment dp[REG_TIMER]                      | ~~dp~~                    | dp           |
| r> drop                        | Put return value back on data stack and drop | ~~dp~~                    | ~~dp~~       |

#### \_zone_mode? 

(value dp : dp[REG_MODE] >> value AND 1)

| Code            | Description                                    | Data Stack                      |
| --------------- | ---------------------------------------------- | ------------------------------- |
| Function Call   | On Call                                        | value dp                        |
| REG_MODE        | Put REG_MODE on stack                          | value dp REG_MODE               |
| swap            | Swap last 2 stack values                       | value REG_MODE dp               |
| dp.register.get | Get dp[REG_MODE]                               | value dp[REG_MODE]              |
| swap            | Swap last 2 stack values                       | dp[REG_MODE] value              |
| shr             | Shift dp[REG_MODE] right by value              | dp[REG_MODE] >> value           |
| 1 and           | And with 1 to see if value is set (return 0/1) | **dp[REG_MODE] >> value 1 and** |

#### \_zone_next_avail? 

(dp : !dp[REG_NEXT])

| Code          | Description              | Data Stack        |
| ------------- | ------------------------ | ----------------- |
| Function Call | On Call                  | dp                |
| REG_NEXT      | Put REG_NEXT on stack    | dp REG_NEXT       |
| swap          | Swap last 2 stack values | REG_NEXT dp       |
| _zone_dp_not? | Get NOT dp[REG_NEXT]     | **!dp[REG_NEXT]** |

#### \_zone_run? 

(dp : DPValue[dp[REG_RUN]])

| Code            | Description                             | Data Stack                    |
| --------------- | --------------------------------------- | ----------------------------- |
| Function Call   | On Call                                 | dp                            |
| REG_RUN         | Put REG_RUN on stack                    | dp REG_RUN                    |
| swap            | Swap last 2 stack values                | REG_RUN dp                    |
| dp.register.get | Get dp[REG_RUN]                         | dp[REG_RUN]                   |
| dup 0           | Duplicate last value and put 0 on stack | dp[REG_RUN] dp[REG_RUN] 0     |
| > if            | If dp[REG_RUN] is valid                 | dp[REG_RUN] ~~dp[REG_RUN] 0~~ |
| dp.value.get    | DPValue[dp[REG_RUN]]                    | **DPValue[dp[REG_RUN]]**      |
| else            | If dp[REG_RUN] is not valid             | dp[REG_RUN]                   |
| drop 1 then     | Drop stack value and put 1 on stack     | ~~dp[REG_RUN]~~ **1**         |

#### \_zone_fault?

(dp : DPvalue[dp[REG_FAULT]] or DPvalue[dp[REG_ZONE_FAULT]])

| Code                           | Description                                  | Data Stack                                                   | Return Stack |
| ------------------------------ | -------------------------------------------- | ------------------------------------------------------------ | ------------ |
| Function Call                  | On Call                                      | dp                                                           |              |
| >r                             | Put last data stack value on return stack    | ~~dp~~                                                       | dp           |
| REG_FAULT r@                   | Put REG_FAULT and dp on stack                | REG_FAULT dp                                                 | dp           |
| _zone_dp?                      | Put dp[REG_FAULT] on stack                   | DPvalue[dp[REG_FAULT]]                                       | dp           |
| REG_ZONEFAULT r@ \_zone_dp? or | or with dp[REG_ZONEFAULT]                    | **DPvalue[dp[REG_FAULT]] or DPvalue[dp[REG_ZONE_FAULT]]**    | dp           |
| r> drop                        | Put return value back on data stack and drop | **DPvalue[dp[REG_FAULT]] or DPvalue[dp[REG_ZONE_FAULT]]** ~~dp~~ | ~~dp~~       |

#### \_zone_first@

 (dp : dp[REG_FIRST])

| Code            | Description                                               | Data Stack                                              |
| --------------- | --------------------------------------------------------- | ------------------------------------------------------- |
| Function Call   | On Call                                                   | dp                                                      |
| REG_FIRST       | Put REG_FIRST on stack                                    | dp REG_FIRST                                            |
| swap            | Swap last 2 stack values                                  | REG_FIRST dp                                            |
| dp.register.get | Get dp[REG_FIRST]                                         | dp[REG_FIRST]                                           |
| dup dup         | Duplicate last data value twice                           | dp[REG_FIRST] dp[REG_FIRST] dp[REG_FIRST]               |
| 1 <             | dp[REG_FIRST] < 1                                         | dp[REG_FIRST] dp[REG_FIRST] (dp[REG_FIRST] < 1)         |
| swap            | Swap last 2 stack values                                  | dp[REG_FIRST] (dp[REG_FIRST] < 1) dp[REG_FIRST]         |
| 999 >           | dp[REG_FIRST] > 999                                       | dp[REG_FIRST] (dp[REG_FIRST] < 1) (dp[REG_FIRST] > 999) |
| or if           | if (dp[REG_FIRST] < 1) or (dp[REG_FIRST] > 999) (invalid) | **dp[REG_FIRST]**                                       |
| drop 1 then     | Drop last data stack value and put 1                      | ~~dp[REG_FIRST]~~ **1**                                 |

#### \_zone_last@ 

(dp : dp[REG_LAST] )

| Code            | Description                                             | Data Stack                                           |
| --------------- | ------------------------------------------------------- | ---------------------------------------------------- |
| Function Call   | On Call                                                 | dp                                                   |
| REG_LAST        | Put REG_LASTon stack                                    | dp REG_LAST                                          |
| swap            | Swap last 2 stack values                                | REG_LAST dp                                          |
| dp.register.get | Get dp[REG_LAST]                                        | dp[REG_LAST]                                         |
| dup dup         | Duplicate last data value twice                         | dp[REG_LAST] dp[REG_LAST] dp[REG_LAST]               |
| 1 <             | dp[REG_LAST] < 1                                        | dp[REG_LAST] dp[REG_LAST] (dp[REG_LAST] < 1)         |
| swap            | Swap last 2 stack values                                | dp[REG_LAST] (dp[REG_LAST] < 1) dp[REG_LAST]         |
| 999 >           | dp[REG_LAST] > 999                                      | dp[REG_LAST] (dp[REG_LAST] < 1) (dp[REG_LAST] > 999) |
| or if           | if (dp[REG_LAST] < 1) or (dp[REG_LAST] > 999) (invalid) | **dp[REG_LAST]**                                     |
| drop 1 then     | Drop last data stack value and put 1                    | ~~dp[REG_LAST]~~ **1**                               |

#### \_zone_box_new 

(dp : )

| Code                           | Description                                                  | Data Stack                                                   | Return Stack |
| ------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------ |
| Function Call                  | On Call                                                      | dp                                                           |              |
| \>r                            | Put last data stack value on return stack                    | ~~dp~~                                                       | dp           |
| REG_CURRENT r@                 | Put REG_CURRENT and dp on stack                              | REG_CURRENT dp                                               | dp           |
| dp.register.get                | Get dp[REG_CURRENT]                                          | dp[REG_CURRENT]                                              | dp           |
| 1 +                            | Add one to dp[REG_CURRENT]                                   | (dp[REG_CURRENT] + 1)                                        | dp           |
| dup dup                        | Duplicate last data value twice                              | (dp[REG_CURRENT] + 1) (dp[REG_CURRENT] + 1) (dp[REG_CURRENT] + 1) | dp           |
| r@ \_zone_first@               | Get dp[REG_FIRST]                                            | (dp[REG_CURRENT] + 1) (dp[REG_CURRENT] + 1) (dp[REG_CURRENT] + 1) dp[REG_FIRST] | dp           |
| <                              | (dp[REG_CURRENT] + 1) < dp[REG_FIRST]                        | (dp[REG_CURRENT] + 1) (dp[REG_CURRENT] + 1) ((dp[REG_CURRENT] + 1) < dp[REG_FIRST]) | dp           |
| swap                           | Swap last 2 stack values                                     | (dp[REG_CURRENT] + 1) ((dp[REG_CURRENT] + 1) < dp[REG_FIRST]) (dp[REG_CURRENT] + 1) | dp           |
| r@ \_zone_last@                | Get dp[REG_LAST]                                             | (dp[REG_CURRENT] + 1) ((dp[REG_CURRENT] + 1) < dp[REG_FIRST]) (dp[REG_CURRENT] + 1) dp[REG_LAST] | dp           |
| >                              | (dp[REG_CURRENT] + 1) > dp[REG_LAST]                         | (dp[REG_CURRENT] + 1) ((dp[REG_CURRENT] + 1) < dp[REG_FIRST]) ((dp[REG_CURRENT] + 1) > dp[REG_LAST]) | dp           |
| or if                          | if (dp[REG_CURRENT] + 1) < dp[REG_FIRST] or (dp[REG_CURRENT] + 1) > dp[REG_LAST] (invalid) | (dp[REG_CURRENT] + 1) ~~((dp[REG_CURRENT] + 1) < dp[REG_FIRST])~~ ~~((dp[REG_CURRENT] + 1) > dp[REG_LAST])~~ | dp           |
| drop                           | Drop last data value                                         | ~~(dp[REG_CURRENT] + 1)~~                                    | dp           |
| r@ _zone_first@ then           | Get dp[REG_FIRST]                                            | dp[REG_FIRST]                                                | dp           |
| dup                            | Duplicate last data value                                    | (dp[REG_FIRST] or dp[REG_CURRENT] + 1) (dp[REG_FIRST] or dp[REG_CURRENT] + 1) | dp           |
| REG_CURRENT r@ dp.register.set | Set dp[REG_CURRENT] = dp[REG_FIRST] or dp[REG_CURRENT] + 1   | (dp[REG_FIRST] or dp[REG_CURRENT] + 1)  ~~(dp[REG_FIRST] or dp[REG_CURRENT] + 1)~~ ~~dp[REG_CURRENT]~~ | dp           |
| dup                            | Duplicate last data value                                    | dp[REG_CURRENT] dp[REG_CURRENT]                              | dp           |
| 1                              | Put 1 on stack                                               | dp[REG_CURRENT] dp[REG_CURRENT] 1                            | dp           |
| swap                           | Swap last 2 stack values                                     | dp[REG_CURRENT] 1 dp[REG_CURRENT]                            | dp           |
| bx.state.set                   | Set dp[REG_CURRENT] state (carton) = 1                       | dp[REG_CURRENT] ~~1~~ ~~dp[REG_CURRENT]~~                    | dp           |
| dup                            | Duplicate last data value                                    | dp[REG_CURRENT] dp[REG_CURRENT]                              | dp           |
| 0                              | Put 0 on stack                                               | dp[REG_CURRENT] dp[REG_CURRENT] 0                            | dp           |
| swap                           | Swap last 2 stack values                                     | dp[REG_CURRENT] 0 dp[REG_CURRENT]                            | dp           |
| bx.data.set                    | Set dp[REG_CURRENT] data (carton) =0                         | dp[REG_CURRENT] ~~0~~ ~~dp[REG_CURRENT]~~                    | dp           |
| r@ \_zone_box!                 | Set dp[REG_BOX] = dp[REG_CURRENT]                            | ~~dp[REG_CURRENT]~~ ~~dp~~                                   | dp           |
| r@ \_zone_create_msg           | ?                                                            |                                                              | dp           |
| r> drop                        | Put return value back on data stack and drop                 | ~~dp~~                                                       | ~~dp~~       |

#### \_zone_box_pass 

(dp : )

| Code                    | Description                                    | Data Stack                       | Return Stack |
| ----------------------- | ---------------------------------------------- | -------------------------------- | ------------ |
| Function Call           | On Call                                        | dp                               |              |
| \>r                     | Put last data stack value on return stack      | ~~dp~~                           | dp           |
| MODE_LAX r@             | Put MODE_LAX and dp on stack                   | MODE_LAX dp                      | dp           |
| _zone_mode? if          | If lax_mode is set for dp                      | ~~MODE_LAX~~ ~~dp~~              | dp           |
| BOX_ANON REG_NEXT r@    | Put BOX_ANON, REG_NEXT and dp on stack         | BOX_ANON REG_NEXT dp             | dp           |
| dp.register.get         | Get dp[REG_NEXT]                               | BOX_ANON dp[REG_NEXT]            | dp           |
| _zone_box!              | Set carton REG_BOX for dp[REG_NEXT] = BOX_ANON | ~~BOX_ANON~~ ~~dp[REG_NEXT]~~    | dp           |
| else                    | If lax_mode is not set for dp                  |                                  | dp           |
| r@ \_zone_box@          | Get dp[REG_BOX]                                | dp[REG_BOX]                      | dp           |
| REG_NEXT r@             | Put REG_NEXT and dp on stack                   | dp[REG_BOX] REG_NEXT r@          | dp           |
| dp.register.get         | Get dp[REG_NEXT]                               | dp[REG_BOX] dp[REG_NEXT]         | dp           |
| _zone_box! then         | Set carton for dp[REG_NEXT] = dp[REG_BOX]      | ~~dp[REG_BOX]~~ ~~dp[REG_NEXT]~~ | dp           |
| BOX_NONE r@ \_zone_box! | dp[REG_BOX] = BOX_NONE                         | ~~BOX_NONE~~ ~~dp~~              | dp           |
| r> drop                 | Put return value back on data stack and drop   | ~~dp~~                           | ~~dp~~       |

#### \_zone_box_valid

(dp : 0 < dp[REG_BOX] < 1000)

| Code          | Description                   | Data Stack                                           |
| ------------- | ----------------------------- | ---------------------------------------------------- |
| Function Call | On Call                       | dp                                                   |
| \_zone_box@   | Get dp[REG_BOX]               | dp[REG_BOX]                                          |
| dup           | Duplicate last stack value    | dp[REG_BOX] dp[REG_BOX]                              |
| 0 >           | dp[REG_BOX] > 0               | dp[REG_BOX] (dp[REG_BOX] > 0)                        |
| swap          | Swap last two values on stack | (dp[REG_BOX] > 0) dp[REG_BOX]                        |
| 1000 <        | dp[REG_BOX] < 1000            | (dp[REG_BOX] > 0) (dp[REG_BOX] < 1000)               |
| and           | if 0 < dp[REG_BOX] < 1000     | **(dp[REG_BOX] > 0) and (dp[REG_BOX] < 1000)**&nbsp; |

#### \_zone_box_dir@ 

(dp : BXdata[dp[REG_BOX]] >> (dp[REG_OFFSET] * 3) and 111 or DIR_NONE)

| Code                    | Description                                  | Data Stack                                                   | Return Stack |
| ----------------------- | -------------------------------------------- | ------------------------------------------------------------ | ------------ |
| Function Call           | On Call                                      | dp                                                           |              |
| \>r                     | Put dp on stack                              | ~~dp~~                                                       | dp           |
| r@ \_zone_box_valid? if | If 0 < dp[REG_BOX] < 1000                    | ~~dp~~                                                       | dp           |
| r@ \_zone_box@          | Get dp[REG_BOX]                              | dp[REG_BOX]                                                  | dp           |
| bx.data.get             | Get dp[REG_BOX] data                         | BXdata[dp[REG_BOX]]                                          | dp           |
| REG_OFFSET r@           | Put REG_OFFSET dp on stack                   | BXdata[dp[REG_BOX]] REG_OFFSET dp                            | dp           |
| dp.register.get         | Get dp[REG_OFFSET]                           | BXdata[dp[REG_BOX]] dp[REG_OFFSET]                           | dp           |
| 3 *                     | Put 3 on stack and multiply                  | BXdata[dp[REG_BOX]] (dp[REG_OFFSET] * 3)                     | dp           |
| shr                     | Shift Box Data right by Offset               | BXdata[dp[REG_BOX]] >> (dp[REG_OFFSET] * 3)                  | dp           |
| 7 and                   | AND with 111                                 | **BXdata[dp[REG_BOX]] >> (dp[REG_OFFSET] * 3) and 111**      | dp           |
| else                    | If NOT 0 < dp[REG_BOX] < 1000                |                                                              | dp           |
| DIR_NONE then           | Put DIR_NONE on stack                        | **DIR_NONE**                                                 | dp           |
| r> drop                 | Put return value back on data stack and drop | (**BXdata[dp[REG_BOX]] >> (dp[REG_OFFSET] * 3) and 111** or **DIR_NONE**) ~~dp~~ | ~~dp~~       |

#### \_zone_dest@ 

(value dp : (dp[DIR_] or 0))

| Code                         | Description                                  | Data Stack                             | Return Stack |
| ---------------------------- | -------------------------------------------- | -------------------------------------- | ------------ |
| Function Call                | On Call                                      | value dp                               |              |
| \>r                          | Put dp on return stack                       | value ~~dp~~                           | dp           |
| 0                            | Put 0 on data stack                          | value 0                                | dp           |
| swap                         | Swap last two values on stack                | 0 value                                | dp           |
| dup                          | Duplicate last stack value                   | 0 value value                          | dp           |
| DIR_STRAIGHT = if            | If value = DIR_STRAIGHT                      | **0** value ~~value~~ ~~DIR_STRAIGHT~~ | dp           |
| swap drop                    | Swap last two values on stack and drop       | value ~~0~~                            | dp           |
| r@                           | Put dp on stack                              | value dp                               | dp           |
| swap then                    | Swap last two values on stack                | **dp** value                           | dp           |
| **dup**                      | Duplicate last stack value                   | 0 value value                          | dp           |
| DIR_LEFT = if                | If value = DIR_LEFT                          | 0 value ~~value~~ ~~DIR_LEFT~~         | dp           |
| swap drop                    | Swap last two values and drop                | value ~~0~~                            | dp           |
| REG_LEFT r@ dp.register.get  | Get dp[REG_LEFT]                             | value dp[REG_LEFT]                     | dp           |
| swap then                    | Swap last two values on stack                | **dp[REG_LEFT]** value                 | dp           |
| dup                          | Duplicate last stack value                   | 0 value value                          | dp           |
| DIR_RIGHT = if               | If value = DIR_RIGHT                         | 0 value ~~value~~ ~~DIR_RIGHT~~        | dp           |
| swap drop                    | Swap last two values and drop                | value ~~0~~                            | dp           |
| REG_RIGHT r@ dp.register.get | Get dp[REG_RIGHT]                            | value dp[REG_RIGHT]                    | dp           |
| swap then                    | Swap last two values                         | **dp[REG_RIGHT]** value                | dp           |
| drop                         | Drop last data value                         | **dp[DIR_]** ~~value~~                 | dp           |
| r> drop                      | Put return value back on data stack and drop | (**dp[DIR_]** or **0**) ~~dp~~         | ~~dp~~       |

#### \_zone_slug_release? 

(dp : slug_mode and dp[REG_BOX]=BOX_NONE dp[REG_TIMER] > RPValue[dp[REG_TM_SLUG]])

| Code                      | Description                                           | Data Stack                                                   | Return Stack |
| ------------------------- | ----------------------------------------------------- | ------------------------------------------------------------ | ------------ |
| Function Call             | On Call                                               | dp                                                           |              |
| \>r                       | Put dp on stack                                       | ~~dp~~                                                       | dp           |
| MODE_SLUG r@              | Put MODE_SLUG and dp on stack                         | MODE_SLUG dp                                                 | dp           |
| \_zone_mode?              | If slug_mode is set, put 1 on stack                   | slug_mode                                                    | dp           |
| r@ \_zone_box@            | Put dp[REG_BOX] on stack                              | slug_mode and dp[REG_BOX]                                    | dp           |
| BOX_NONE = and            | If dp[REG_BOX] = BOX_NONE, put 1 on stack             | slug_mode and dp[REG_BOX]=BOX_NONE                           | dp           |
| r@ \_zone_elapsed@        | Get dp[REG_TIMER]                                     | slug_mode and dp[REG_BOX]=BOX_NONE dp[REG_TIMER]             | dp           |
| REG_TM_SLUG r@ \_zone_rp@ | Get RPValue[dp[REG_TM_SLUG]]                          | slug_mode and dp[REG_BOX]=BOX_NONE dp[REG_TIMER] RPValue[dp[REG_TM_SLUG]] | dp           |
| > and                     | Put dp[REG_TIMER] > RPValue[dp[REG_TM_SLUG]] on stack | slug_mode and dp[REG_BOX]=BOX_NONE dp[REG_TIMER] > RPValue[dp[REG_TM_SLUG]] | dp           |
| r> drop                   | Put return value back on data stack and drop          | **slug_mode and dp[REG_BOX]=BOX_NONE dp[REG_TIMER] > RPValue[dp[REG_TM_SLUG]]** ~~dp~~ | ~~dp~~       |

#### \_zone_set_output 

(dp : slug_mode and dp[REG_BOX]=BOX_NONE dp[REG_TIMER] > RPValue[dp[REG_TM_SLUG]])

| Code                             | Description                                  | Data Stack                                   | Return Stack |
| -------------------------------- | -------------------------------------------- | -------------------------------------------- | ------------ |
| Function Call                    | On Call                                      | dp                                           |              |
| \>r                              | Put dp on stack                              | ~~dp~~                                       | dp           |
| 1 r@ \_zone_state@               | Put 1 and dp[REG_STATE] on stack             | 1 dp[REG_STATE]                              | dp           |
| shl                              | Shift Left                                   | 1 << dp[REG_STATE]                           | dp           |
| REG_OUT_STATE r@ dp.register.get | Get dp[REG_OUT_STATE]                        | 1 << dp[REG_STATE] dp[REG_OUT_STATE]         | dp           |
| and if                           | If dp is in desired state                    | ~~1 << dp[REG_STATE]~~ ~~dp[REG_OUT_STATE]~~ | dp           |
| REG_OUTPUT r@ _zone_dp_on        | dp[REG_OUTPUT] on                            | ~~REG_OUTPUT~~ ~~dp~~                        | dp           |
| else                             | If dp is not in desired state                |                                              | dp           |
| REG_OUTPUT r@ \_zone_dp_off then | dp[REG_OUTPUT] off                           | ~~REG_OUTPUT~~ ~~dp~~                        | dp           |
| r> drop                          | Put return value back on data stack and drop | ~~dp~~                                       | ~~dp~~       |

## Base Function - createZone

| Code                                                         | Psuedocode                                                   |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| sub createZone {                                             |                                                              |
| my (name, desc, $data) = @_;                                 | Declare 3 scalars: name, desc, data store inputs respectively |
| my $val;                                                     | Declare a scalar val                                         |
| # create the zone dp                                         | <u>NOTE: ((val = data->{HASH}) ne '') sets val = HASH</u>    |
| if ((val = data->{desc}) ne '') { desc = val; }              | (val = desc) If val != ‘’, desc = val                        |
| my dp = dp::virtual( name, $desc );                          | Declare scalar dp set it to declared virtual dp              |
| # create controls                                            |                                                              |
| Trk::dp_registerset( $dp, REG_ZONEFAULT,                     | Create virtual dp name_fault and set it to dp(REG_ZONEFAULT) |
| dp::virtual( name . '_fault', name . ' fault' ) );           |                                                              |
| Trk::dp_registerset( $dp, REG_DEBUG,                         | Create virtual dp name_debug and set it to dp(REG_DEBUG)     |
| dp::virtual( name . '_debug', name . ' debug' ) );           |                                                              |
| Trk::dp_registerset( $dp, REG_HOLD,                          | Create virtual dp name_hold and set it to dp(REG_HOLD)       |
| dp::virtual( name . '_hold', name . ' hold' ) );             |                                                              |
| # set mode bits                                              |                                                              |
| my $mode = 0;                                                | Declare scalar mode = 0 (bitmask variable); Initialize to 0’s |
| if ($data->{create_mode} ne '') {                            | If create_mode != ‘’                                         |
| mode = mode \| (1 << MODE_CREATE);                           | *  Set MODE_CREATE into mode bitmask                         |
| if ((val = data->{first}) ne '')                             | *  if val != ‘’ (val = first)                                |
| { Trk::dp_registerset( dp, REG_FIRST, val ); }               | *  dp(REG_FIRST) = val                                       |
| if ((val = data->{last}) ne '')                              | *  if val != ‘’ (val = last)                                 |
| { Trk::dp_registerset( dp, REG_LAST, val );  }    }          | *  dp(REG_LAST) = val                                        |
| if ($data->{lax_mode} ne '') {                               | If lax_mode != ‘’                                            |
| mode = mode \| (1 << MODE_LAX);  }                           | *  Set MODE_LAX into mode bitmask                            |
| if ($data->{slug_mode} ne '') {                              | If slug_mode != ‘’                                           |
| mode = mode \| (1 << MODE_SLUG);                             | *  Set MODE_LAX into mode bitmask                            |
| if ((val = data->{slug}) ne '') {                            | *  (val = slug) If val != ‘’                                 |
| rp = rp::const( name . '_slug', $val,                        | *  rp = constant(name_slug) and name_slug = val              |
| $name . ' slug-release delay time (cycles)' );               |                                                              |
| } else { $rp = rp::handle( 'zone_slug' ); }                  | else rp = zone_slug                                          |
| Trk::dp_registerset( dp, REG_TM_SLUG, rp );  }               | *  dp(REG_TM_SLUG) = rp                                      |
| if ($data->{stop_mode} ne '') {                              | If stop_mode != ‘’                                           |
| mode = mode \| (1 << MODE_STOP);                             | *  Set MODE_STOP into mode bitmask                           |
| Trk::dp_registerset( $dp, REG_GO,                            | *  Create virtual dp name_go and set it to dp(REG_GO)        |
| dp::virtual( name . '_go', name . ' release' ) );  }         |                                                              |
| if ($data->{multi_mode} ne '') {                             | If multi_mode != ‘’                                          |
| mode = mode \| (1 << MODE_MULTI);  }                         | *  Set MODE_MULTI into mode bitmask                          |
| if ($data->{idlerun_mode} ne '') {                           | If idlerun_mode != ‘’                                        |
| mode = mode \| (1 << MODE_IDLERUN);  }                       | *  Set MODE_IDLERUN into mode bitmask                        |
| if ($data->{beltfeed_mode} ne '') {                          | If belfeed_mode != ‘’                                        |
| mode = mode \| (1 << MODE_BELTFEED) ;  }                     | * Set MODE_BELTFEED into mode bitmask                        |
| if ($data->{pushout_mode} ne '') {                           | If pushout_mode != ‘’                                        |
| mode = mode \| (1 << MODE_PUSHOUT) ;  }                      | *  Set MODE_PUSHOUT into mode bitmask                        |
| Trk::dp_registerset( dp, REG_MODE, mode );                   | dp(REG_MODE) = mode bitmask                                  |
| # set registers from input data                              |                                                              |
| my $val;                                                     | ??? – val already Declared?                                  |
| if ((val = data->{next}) ne '')                              | (val = next) If val != ‘’                                    |
| { Trk::dp_registerset( dp, REG_NEXT,  dp::handle( val ) ); } | *  dp(REG_NEXT) = val                                        |
| if ((val = data->{eye}) ne '')                               | (val = eye) If val != ‘’                                     |
| { Trk::dp_registerset( dp, REG_EYE,  dp::handle( val ) ); }  | *  dp(REG_EYE) = val                                         |
| if ((val = data->{motor}) ne '')                             | (val = motor) If val != ‘’                                   |
| { Trk::dp_registerset( dp, REG_MOTOR, dp::handle( val ) ); } | *  dp(REG_MOTOR) = val                                       |
| if ((val = data->{run}) ne '')                               | (val = run) If val != ‘’                                     |
| { Trk::dp_registerset( dp, REG_RUN,  dp::handle( val ) ); }  | *  dp(REG_RUN) = val                                         |
| if ((val = data->{fault}) ne '')                             | (val = fault) If val != ‘’                                   |
| { Trk::dp_registerset( dp, REG_FAULT, dp::handle( val ) ); } | *  dp(REG_FAULT) = val                                       |
| if ((val = data->{reset}) ne '') {                           | (val = reset) If val != ‘’                                   |
| Trk::dp_registerset(  dp, REG_RESET, dp::handle( val ) );    | *  dp(REG_RESET) = val                                       |
| my @stub = ( $name . '_fault', 'off' );                      | Declare array stub = [name_fault, off]                       |
| Trk::ex_compile( name . '_fault_clr', name . ' fault clear', \@stub ); | Create word name_fault_clr = stub                            |
| trakbase::leading( val, name . '_fault_clr',                 | Execute name_fault_clr on the leading edge of dp(REG_RESET)  |
| 'clear ' . $name . ' fault' );  }                            |                                                              |
| if ((val = data->{output}) ne '')                            | (val = output) If val != ‘’                                  |
| { Trk::dp_registerset( dp, REG_OUTPUT, dp::handle( val ) ); } | *  dp(REG_OUTPUT) = val                                      |
| if ((val = data->{out_states}) ne '')                        | (val = out_states) If val != ‘’                              |
| { Trk::dp_registerset( dp, REG_OUT_STATE, val ); }           | *  dp(REG_OUT_STATE) = val                                   |
| return $dp;  }                                               | return dp to calling function                                |

## End of File

| 1;   | Return TRUE value to requiring function (trak.pl) |
| ---- | ------------------------------------------------- |

