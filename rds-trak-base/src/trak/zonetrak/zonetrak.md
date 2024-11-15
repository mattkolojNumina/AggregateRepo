# **zonetrak.h**

## Zone States

| STATE_INIT | 0   |
| --- | --- |
| STATE_IDLE | 1   |
| STATE_FILL | 2   |
| STATE_FILL_X | 3   |
| STATE_FULL | 4   |
| STATE_DRAIN_X | 5   |
| STATE_DRAIN | 6   |
| STATE_RUNUP | 9   |
| STATE_CHOOSE | 10  |
| STATE_ACTIVE | 11  |
| STATE_DEACT | 12  |
| STATE_FILL_O | 13  |
| STATE_DRAIN_O | 14  |
| STATE_FAULT | 31  |

## Message Types

| MSG_ZONE_CREATE | 7000 |
| --- | --- |
| MSG_ZONE_STATE | 7100 |
| MSG_ZONE_CHOICE | 7200 |
| MSG_ZONE_ER | 7900 |
| MSG_ZONE_RESET | 7999 |

## Error Codes

| ERR_UNEXPECTED | 1   |
| --- | --- |
| ERR_EARLY | 2   |
| ERR_MISSING | 3   |
| ERR_REMOVED | 4   |
| ERR_STUCK | 5   |
| ERR_SENSOR | 6   |
| ERR_LOGIC | 99  |

## Direction Codes

| DIR_LAST | \-1 |
| --- | --- |
| DIR_NONE | 0   |
| DIR_STRAIGHT | 1   |
| DIR_LEFT | 2   |
| DIR_RIGHT | 3   |

## Box Types

| BOX_ZERO | 0   |
| --- | --- |
| BOX_NONE | \-1 |
| BOX_ANON | \-2 |
| BOX_LOAD | \-3 |

## Zone Modes

| MODE_CREATE | 0   |
| --- | --- |
| MODE_LAX | 1   |
| MODE_SLUG | 2   |
| MODE_STOP | 3   |
| MODE_MULTI | 4   |
| MODE_IDLERUN | 5   |
| MODE_BELTFEED | 6   |
| MODE_PUSHOUT | 7   |

## Zone Registers

| REG_BOX | 0   |
| --- | --- |
| REG_STATE | 1   |
| REG_MODE | 2   |
| REG_NEXT | 3   |
| REG_EYE | 4   |
| REG_MOTOR | 5   |
| REG_JAMEYE | 6   |
| REG_INPUT | 7   |
| REG_OUTPUT | 8   |
| REG_TIMER | 9   |
| REG_RUN | 10  |
| REG_FAULT | 11  |
| REG_RESET | 12  |
| REG_ZONEFAULT | 13  |
| REG_DEBUG | 14  |
| REG_HOLD | 15 |
| REG_GO | 16 |
| REG_CURRENT | 17 |
| REG_FIRST | 18 |
| REG_LAST | 19 |
| REG_ACTIVE | 20 |
| REG_ACTIVE_EYE | 21 |
| REG_DEACT | 22 |
| REG_DEACT_EYE | 23 |
| REG_LEFT | 24 |
| REG_RIGHT | 25 |
| REG_OFFSET | 26 |
| REG_DEFAULT | 27 |
| REG_CHOICE | 28 |
| REG_CONTROL | 29 |
| REG_RUNUP | 30 |
| REG_RUNIN | 31 |
| REG_RUNOVER | 32 |
| REG_RUNOUT | 33 |
| REG_RUNOFF | 34 |
| REG_RUNDOWN | 35 |
| REG_TEMP | 37 |
| REG_TEMP2 | 38 |
| REG_OUT_STATE | 39 |
| REG_TM_MIN | 40 |
| REG_TM_MAX | 41 |
| REG_TM_CLEAR | 42 |
| REG_TM_CHOOSE | 43 |
| REG_TM_WAIT | 44 |
| REG_TM_DEAD | 45 |
| REG_TM_SLUG | 48 |
