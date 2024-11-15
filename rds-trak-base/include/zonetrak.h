/*
 * zonetrak.h
 *
 * definitions for constants used by ZoneTRAK applications
 */


#ifndef __ZONETRAK_H
#define __ZONETRAK_H


/* --- Message Types --- */
#define MSG_ZONE_CREATE  7000
#define MSG_ZONE_STATE   7100
#define MSG_ZONE_CHOICE  7200
#define MSG_ZONE_ERR     7900
#define MSG_ZONE_RESET   7999


/* --- Box Types --- */
#define BOX_ZERO            0
#define BOX_NONE           -1
#define BOX_ANON           -2
#define BOX_LOAD           -3


/* --- Zone States --- */
#define STATE_INIT          0
#define STATE_IDLE          1
#define STATE_FILL          2
#define STATE_FILL_X        3
#define STATE_FULL          4
#define STATE_DRAIN_X       5
#define STATE_DRAIN         6
#define STATE_RUNUP         9
#define STATE_CHOOSE       10
#define STATE_ACTIVE       11
#define STATE_DEACT        12
#define STATE_FILL_O       13
#define STATE_DRAIN_O      14
#define STATE_FAULT        31


/* --- Direction Codes --- */
#define DIR_LAST           -1
#define DIR_NONE            0
#define DIR_STRAIGHT        1
#define DIR_LEFT            2
#define DIR_RIGHT           3


/* --- Error Codes --- */
#define ERR_UNEXPECTED      1
#define ERR_EARLY           2
#define ERR_MISSING         3
#define ERR_REMOVED         4
#define ERR_STUCK           5
#define ERR_SENSOR          6
#define ERR_LOGIC          99


/* --- Zone Modes --- */
#define MODE_CREATE         0
#define MODE_LAX            1
#define MODE_SLUG           2
#define MODE_STOP           3
#define MODE_MULTI          4
#define MODE_IDLERUN        5
#define MODE_BELTFEED       6
#define MODE_PUSHOUT        7


/* --- Registers --- */
#define REG_BOX             0
#define REG_STATE           1
#define REG_MODE            2
#define REG_NEXT            3
#define REG_EYE             4
#define REG_MOTOR           5
#define REG_JAMEYE          6
#define REG_INPUT           7
#define REG_OUTPUT          8
#define REG_TIMER           9

#define REG_RUN            10
#define REG_FAULT          11
#define REG_RESET          12
#define REG_ZONEFAULT      13
#define REG_DEBUG          14
#define REG_HOLD           15
#define REG_GO             16
#define REG_CURRENT        17
#define REG_FIRST          18
#define REG_LAST           19

#define REG_ACTIVE         20
#define REG_ACTIVE_EYE     21
#define REG_DEACT          22
#define REG_DEACT_EYE      23
#define REG_LEFT           24
#define REG_RIGHT          25
#define REG_OFFSET         26
#define REG_DEFAULT        27
#define REG_CHOICE         28
#define REG_CONTROL        29

#define REG_RUNUP          30
#define REG_RUNIN          31
#define REG_RUNOVER        32
#define REG_RUNOUT         33
#define REG_RUNOFF         34
#define REG_RUNDOWN        35
// unused                  36
#define REG_TEMP           37
#define REG_TEMP2          38
#define REG_OUT_STATE      39

#define REG_TM_MIN         40
#define REG_TM_MAX         41
#define REG_TM_CLEAR       42
#define REG_TM_CHOOSE      43
#define REG_TM_WAIT        44
#define REG_TM_DEAD        45
// unused                  46
// unused                  47
#define REG_TM_SLUG        48
#define REG_TM_JAM         49


#endif
