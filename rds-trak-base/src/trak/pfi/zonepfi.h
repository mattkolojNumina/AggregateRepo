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
#define PFI_STATE_INIT         0
#define PFI_STATE_PURGE        1
#define PFI_STATE_PURGE_A      2
#define PFI_STATE_PURGE_B      3
#define PFI_STATE_PURGE_C      4
#define PFI_STATE_PURGE_D      5
#define PFI_STATE_PURGE_E      6
#define PFI_STATE_PURGE_F      7
#define PFI_STATE_PURGE_G      8
#define PFI_STATE_PURGE_H      9
#define PFI_STATE_PURGE_I      10
#define PFI_STATE_PURGE_K      11
#define PFI_STATE_RESET        12
#define PFI_STATE_RUN          13
#define PFI_STATE_FAULT        99

#define PFI1_STATE_IDLE        0
#define PFI1_STATE_FILL        1
#define PFI1_STATE_FILL_X      2
#define PFI1_STATE_FULL        3
#define PFI1_STATE_DRAIN_X     4
#define PFI1_STATE_DRAIN       5
#define PFI1_STATE_FAULT       99

#define PFI2_STATE_IDLE        0
#define PFI2_STATE_FILL        1
#define PFI2_STATE_FILL_X      2
#define PFI2_STATE_BAIL        3
#define PFI2_STATE_HOLD        4
#define PFI2_STATE_PREP        5
#define PFI2_STATE_RAM_DOWN    6
#define PFI2_STATE_RAM_UP      7
#define PFI2_STATE_CRIMP       8
#define PFI2_STATE_FAULT       99

#define PFI3_STATE_IDLE        0
#define PFI3_STATE_FILL        1
#define PFI3_STATE_FULL        2
#define PFI3_STATE_DRAIN       3
#define PFI3_STATE_FAULT       99

#define PFI4_STATE_INIT        0
#define PFI4_STATE_RESET       1
#define PFI4_STATE_IDLE        2
#define PFI4_STATE_GRIP        3
#define PFI4_STATE_UNCRIMP     4
#define PFI4_STATE_EXTEND      5
#define PFI4_STATE_HOLD        6
#define PFI4_STATE_DROP        7
#define PFI4_STATE_RETRACT     8
#define PFI4_STATE_FAULT       99


/* --- Error Codes --- */
#define PFI_ERR_UNKNOWN          1000
#define PFI_ERR_RAM_STUCK_UP     1001
#define PFI_ERR_RAM_STALL_UP     1002
#define PFI_ERR_RAM_STUCK_DOWN   1003
#define PFI_ERR_RAM_STALL_DOWN   1004
#define PFI_ERR_CRIMP1_STUCK_IN  1005
#define PFI_ERR_CRIMP1_STALL_IN  1006
#define PFI_ERR_CRIMP1_STUCK_OUT 1007
#define PFI_ERR_CRIMP1_STALL_OUT 1008
#define PFI_ERR_CRIMP2_STUCK_IN  1009
#define PFI_ERR_CRIMP2_STALL_IN  1010
#define PFI_ERR_CRIMP2_STUCK_OUT 1011
#define PFI_ERR_CRIMP2_STALL_OUT 1012
#define PFI_ERR_Y_GRIP_STUCK     1013
#define PFI_ERR_Y_GRIP_STALL     1014
#define PFI_ERR_NOT_HI           1015
#define PFI_ERR_NOT_LO           1016
#define PFI_ERR_BAIL_STUCK_UP    1017
#define PFI_ERR_BAIL_STALL_UP    1018
#define PFI_ERR_BAIL_STUCK_DOWN  1019
#define PFI_ERR_BAIL_STALL_DOWN  1020
#define PFI_ERR_PE_SCAN_JAM      1021
#define PFI_ERR_PE_SCAN_NONE     1022
#define PFI_ERR_PE_FORMAX_JAM    1023
#define PFI_ERR_PE_FORMAX_NONE   1024
#define PFI_ERR_PE_POCKET_JAM    1025
#define PFI_ERR_PE_POCKET_NONE   1026


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
