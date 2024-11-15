% 
%   hog.w 
%
%   Author: Mark Woodworth 
%
%   History:
%      2004-07-19 -- check in (mrw)
%
%
%         C O N F I D E N T I A L
%
%     This information is confidential and should not be disclosed to
%     anyone who does not have a signed non-disclosure agreement on file
%     with Numina Systems Corporation.  
%
%
%
%
%
%
%     (C) Copyright 2004 Numina Systems Corporation.  All Rights Reserved.
%
%
%

%
% --- macros ---
%
\def\boxit#1#2{\vbox{\hrule\hbox{\vrule\kern#1\vbox{\kern#1#2\kern#1}%
   \kern#1\vrule}\hrule}}
\def\today{\ifcase\month\or
   January\or February\or March\or April\or May\or June\or
   July\or August\or September\or October\or November\or December\or\fi
   \space\number\day, \number\year}
\def\myitem{\quad\qquad\item{$\bullet$\ }}

%
% --- title ---
%
\def\title{Hog}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

Eats up CPU capacity.

%
% --- confidential ---
%
\bigskip
\centerline{\boxit{10pt}{\hsize 4in
\bigskip
\centerline{\bf CONFIDENTIAL}
\smallskip
This material is confidential.  
It must not be disclosed to any person
who does not have a current signed non-disclosure form on file with Numina
Systems Corporation.  
It must only be disseminated on a need-to-know basis.
It must be stored in a secure location. 
It must not be left out unattended.  
It must not be copied.  
It must be destroyed by burning or shredding. 
\smallskip
}}

%
% --- id ---
%
\bigskip
\centerline{Authors: Mark Woodworth}
\centerline{RCS ID: $ $Id: hog.w,v 1.1 2008/03/25 19:55:08 rds Exp $ $}
}

%
% --- copyright ---
\def\botofcontents{\vfill
\centerline{\copyright 2004 Numina Systems Corporation.  
All Rights Reserved.}
}

@* Overview.
@c
static char rcsid[] = "$Id: hog.w,v 1.1 2008/03/25 19:55:08 rds Exp $" ;
@<Includes@>@;
main(int argc, char *argv[])
   {
   int i ;
   while(1)
      i++ ;
   }
@ Includes.  Standard system includes are collected here.
@<Includes@>+=
#include <stdio.h>

