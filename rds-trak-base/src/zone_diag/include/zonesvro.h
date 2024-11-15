/*
 * zonesvro.h
 *
 * definitions for constants used by Servo Zone applications
 */


#ifndef __ZONESVRO_H
#define __ZONESVRO_H


/* --- Servo Zone States --- */
#define SVR_POWERUP        0
#define SVR_INIT           1
#define SVR_IDLE           2
#define SVR_MOVING         3
#define SVR_FAULT         99


/* --- Servo zone Registers --- */
#define REGS_SETP            0
#define REGS_STATE           1
#define REGS_COMPARE         2
#define REGS_HOLD            3
#define REGS_OUT0            4
#define REGS_OUT1            5
#define REGS_OUT2            6
#define REGS_OUT3            7
#define REGS_OUT4            8
#define REGS_OUT5           10
#define REGS_DRIVE          11
#define REGS_BUSY           12
#define REGS_FAULT          13
#define REGS_INP            14
#define REGS_AREA           15
#define REGS_SVON           16
#define REGS_ESTOP          17
#define REGS_RESET          18
#define REGS_SETUP          19
#define REGS_DEBUG          20
#define REGS_GO             21
#define REGS_HOLDDP         22
#define REGS_SETON          23
#define REGS_SVRE           24
#define REGS_ESTOPIN        25
#define REGS_FAULTDP        26

#endif
