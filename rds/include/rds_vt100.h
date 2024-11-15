/*3:*/
#line 139 "rds_vt100.w"

#ifndef __VT100_H 
#define __VT100_H 
/*7:*/
#line 185 "rds_vt100.w"

#define A_NORMAL    0 
#define A_BOLD      1 
#define A_DIM       2 
#define A_BLINK     3 
#define A_REVERSE   7 
#define A_F_BLACK   30 
#define A_F_RED     31 
#define A_F_GREEN   32 
#define A_F_BROWN   33 
#define A_F_BLUE    34 
#define A_F_MAGENTA 35 
#define A_F_CYAN    36 
#define A_F_WHITE   37 
#define A_B_BLACK   40 
#define A_B_RED     41 
#define A_B_GREEN   42 
#define A_B_BROWN   43 
#define A_B_BLUE    44 
#define A_B_MAGENTA 45 
#define A_B_CYAN    46 
#define A_B_WHITE   47 



/*:7*/
#line 142 "rds_vt100.w"

/*9:*/
#line 225 "rds_vt100.w"

void vt100_Open(int ifd,int ofd);



/*:9*//*11:*/
#line 244 "rds_vt100.w"

int vt100_Close(void);



/*:11*//*13:*/
#line 260 "rds_vt100.w"

void vt100_Clear(void);



/*:13*//*15:*/
#line 275 "rds_vt100.w"

void vt100_ClearLine(void);



/*:15*//*17:*/
#line 291 "rds_vt100.w"

void vt100_Move(int row,int column);



/*:17*//*19:*/
#line 329 "rds_vt100.w"

void vt100_Attr(int attr);
void vt100_Normal(void);
void vt100_Bold(void);
void vt100_Reverse(void);
void vt100_AltChar(int flag);



/*:19*//*21:*/
#line 355 "rds_vt100.w"

void vt100_Single(void);
void vt100_Double(void);



/*:21*//*23:*/
#line 374 "rds_vt100.w"

void vt100_ClearField(int row,int column,int len);



/*:23*//*25:*/
#line 394 "rds_vt100.w"

void vt100_ncPrint(int row,int column,char*fmt,...);



/*:25*//*27:*/
#line 416 "rds_vt100.w"

void vt100_Print(int row,int column,char*fmt,...);



/*:27*//*29:*/
#line 439 "rds_vt100.w"

void vt100_Center(int row,char*fmt,...);



/*:29*//*31:*/
#line 466 "rds_vt100.w"

int vt100_GetLine(char*input,int maxchars);


/*:31*//*33:*/
#line 479 "rds_vt100.w"

char vt100_GetChar();


/*:33*//*35:*/
#line 518 "rds_vt100.w"

int vt100_SelectLine(char*input,int maxchars,int timeout);


/*:35*//*37:*/
#line 541 "rds_vt100.w"

int vt100_GetPasswd(char*input,int maxchars);



/*:37*/
#line 143 "rds_vt100.w"

#endif 
#line 145 "rds_vt100.w"



/*:3*/
