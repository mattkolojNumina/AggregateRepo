/*
 * zonefold.h
 *
 * definitions for constants used by Zone folder applications
 */


#ifndef __ZONEFOLD_H
#define __ZONEFOLD_H


/* --- Box Types --- */
#ifndef BOX_NONE
#define BOX_NONE           -1
#endif
#ifndef BOX_ANON
#define BOX_ANON           -2
#endif


/* --- Zone folder States --- */
#define SF_INIT            0
#define SF_IDLE            1
#define SF_WAIT            2
#define SF_BLOCK           3
#define SF_SETTLE          4
#define SF_SQUARE          5
#define SF_FULL            6
#define SF_EXTEND          7
#define SF_RETRACT         8
#define SF_GRIP            9
#define SF_RELEASE        10
#define SF_CRIMP          11
#define SF_XFER_GRAB      12
#define SF_RAISE          13
#define SF_XFER           14
#define SF_CLOSE          15
#define SF_XFER_RELEASE   16
#define SF_LOWER          17
#define SF_SETSERVO       18
#define SF_SETSLIDE       19
#define SF_DONE           20
#define SF_SLIDEIN        21
#define SF_SLIDEOUT       22
#define SF_FAULT          99


/* --- Error Codes --- */
#define ERF_PAPER_JAM        100
#define ERF_PAPER_LOST       101
#define ERF_PRINTER_TIMEOUT  102
#define ERF_FOLDER_FAIL      103
#define ERF_POCKET_FAULT     104
#define ERF_PAPER_MISFEED    105
#define ERF_CHUTE_JAM        106
#define ERF_SERVO_FAULT      107
#define ERF_SETUP_FAIL       108
#define ERF_YGRIP_FAIL       109


/* --- Registers --- */
#define RF_BOX              0
#define RF_STATE            1
#define RF_NEXT             2
#define RF_TOTAL            3
#define RF_COUNT            4
#define RF_EYE_IN           5
#define RF_EYE_OUT          6
#define RF_KNIFE_ISHOME     7
#define RF_KNIFE_ISACTIVE   8
#define RF_TIMER            9
#define RF_BAIL_ISHOME     10
#define RF_BAIL_ISACTIVE   11
#define RF_SLIDE_ISHOME    12
#define RF_SLIDE_ISACTIVE  13
#define RF_GRIP_OPEN       14
#define RF_KNIFE_EXTEND    15
#define RF_KNIFE_RETRACT   16
#define RF_BAIL_EXTEND     17
#define RF_SLIDE_EXTEND    18
#define RF_SLIDE_RETRACT   19
#define RF_XGRIP_CLOSE     20
#define RF_GO              21
#define RF_CHUTE           22
#define RF_RUN             23
#define RF_DEBUG           24
#define RF_HOLD            25
#define RF_RESET           26
#define RF_FMAX_ENABLE     27
#define RF_FMAX_RUN        28
#define RF_POCKET_EYE      29
#define RF_INTERLOCK1      30
#define RF_INTERLOCK2      31
#define RF_CRIMP_CLOSE     32
#define RF_CRIMPA_ISOPEN   33
#define RF_CRIMPB_ISOPEN   34
#define RF_SERVO           35
#define RF_YGRIP_CLOSE     36
#define RF_YGRIP_ISOPEN    37
#define RF_XGRIP_ISOPEN    38
#define RF_F2              39
#define RF_F3              40
#define RF_F4              41
#define RF_XYPAPER         42
#define RF_SERVO_FAULT     43
#define RF_PURGE           44



#endif
