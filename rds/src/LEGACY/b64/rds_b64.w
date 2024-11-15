% 
%   rds_b64.web
%
%   Author: Alex Korzhuk 
%
%   History:
%  07/08/2003 ank: init
%  06/09/2017 ank: to be complient with the current version of BASE64 encoding,
%    replaced alphabet char62 from '~' to '+', replaced alphabet char63 from
%    '-' to '/', replaced padding char from '_' to '='.
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
%     (C) Copyright 2003 Numina Systems Corporation.  All Rights Reserved.
%
%
%

% --- helpful macros ---
\def\dot{\item{ $\bullet$}}
\def\boxit#1#2{\vbox{\hrule\hbox{\vrule\kern#1\vbox{\kern#1#2\kern#1}%
   \kern#1\vrule}\hrule}}
\def\today{\ifcase\month\or
   January\or February\or March\or April\or May\or June\or
   July\or August\or September\or October\or November\or December\or\fi
   \space\number\day, \number\year}

% --- title block ---
\def\title{B64 -- Base64 library}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This library encapsulates functions to provide encoding of byte arrays into
Base64-encoded strings, and decoding the other way. This is modified Base64
with slightly different characters than usual, so it won't require escaping
when used in URLs.

% --- confidentiality statement ---
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

% --- author and version ---
\bigskip
\centerline{Author: Alex Korzhuk}
\centerline{Printing Date: \today}
\centerline{Control Revision: $ $Revision: 1.3 $ $}
\centerline{Control Date: $ $Date: 2017/06/09 20:22:07 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2003 Numina Systems Corporation.
All Rights Reserved.}
}



@* Overview. This is a shared C library.
@c
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <fcntl.h>
#include <ctype.h>

@<rds_b64.h@>@;
@<Globals@>@;
@<Functions@>@;
@<Exported Functions@>@;



@ Header file. We put useful definitions and the exported prototypes in
an external header.
@(rds_b64.h@>=
#ifndef __RDS_B64_H
#define __RDS_B64_H
  @<Defines@>@;
  int b64_encode( const char *pBin, char *pszMime, int iBinLen, int bPad );
  int b64_decode( const char *pszMime, char *pBin );
  int b64_decode_len( const char *pszMime );
#endif



@ Useful definitions.
@<Defines@>+=
#ifndef EOS 
#define EOS '\0'
#endif



@ Globals. These variables are used internally in the library.
No needs to export.
@<Globals@>+=
char alphabet[ 64 ] = {                     @;
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', @;
    'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', @;
    'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', @;
    'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', @;
    'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', @;
    'o', 'p', 'q', 'r', 's', 't', 'u', 'v', @;
    'w', 'x', 'y', 'z', '0', '1', '2', '3', @;
    '4', '5', '6', '7', '8', '9', '+', '/'};
char reverse[ 256 ];



@ The |set_reverse| function. Used internally.
@<Functions@>+=
void set_reverse( void ) {
  int i;

  for( i = 0; i < 256; i++ ) reverse[ i ] = 0xFF; //not a legal base64 letter
  for( i = 0; i < 64; i++) reverse[ ( int )alphabet[ i ] ] = i;
}



@* The |b64_encode| function. Caller should specify |bPad=true| if they want
a standard-compliant encoding. This is because Base64 requires that the length
of the encoded text be a multiple of four characters, padded with |'='|.
Without the |'true'| flag, we don't add these |'='| characters.

The |pBin| parameter is a binary array of size |iBinLen|.

The |pszMime| parameter is a buffer where the resulting base64-encoded
null-terminated string is stored.
@<Exported Functions@>+=
int b64_encode( const char *pBin, char *pszMime, int iBinLen, int bPad ) {
  int i, o, val, realoutlen, outlen;

  for( i = 0, o = 0; i < iBinLen; ) {
    val = ( ( int )pBin[ i++ ] & 0xFF ) << 16;
    if( i < iBinLen ) val |= ( ( int )pBin[ i++ ] & 0xFF ) << 8;
    if( i < iBinLen ) val |= ( ( int )pBin[ i++ ] & 0xFF );
    pszMime[ o++ ] = alphabet[ ( val >> 18 ) & 0x3F ];
    pszMime[ o++ ] = alphabet[ ( val >> 12 ) & 0x3F ];
    pszMime[ o++ ] = alphabet[ ( val >> 6 ) & 0x3F ];
    pszMime[ o++ ] = alphabet[ val & 0x3F ];
  }

  outlen = ( ( iBinLen + 2 ) / 3 ) * 4;
  realoutlen = outlen;
  switch ( iBinLen % 3 ) {
    case 1: realoutlen -= 2; break;
    case 2: realoutlen -= 1; break;
  }

  //pad with |'='| signs up to a multiple of four if requested
  if( bPad ) while( realoutlen < outlen ) pszMime[ realoutlen++ ] = '=';

  pszMime[ realoutlen ] = '\0';  //null terminate
  return( realoutlen );
}



@* The |b64_decode| function. Handles the standards-compliant
(padded with |'='| signs) as well as our shortened form.

The |pszMime| parameter is a base64-encoded null-terminated string.

The |pBin| parameter is a buffer where the resulting decoded binary data
is stored. The |pBin| buffer should have enough room to store decoded data.

The function returns size of the stored data - on success, or (|-1|) - on error.
@<Exported Functions@>+=
int b64_decode( const char *pszMime, char *pBin ) {
  int inlen, outlen, wholeinlen, wholeoutlen, blocks, remainder;
  int o, i, in1, in2, in3, in4, orvalue, outval;

  set_reverse();

  // strip trailing equals signs.
  inlen = strlen( pszMime );
  while( inlen > 0 && pszMime[ inlen - 1 ] == '=' ) inlen--;

  // WholeInLen and WholeOutLen are the the length of the input and output
  // sequences respectively, not including any partial block at the end
  blocks = inlen / 4;
  wholeinlen  = blocks * 4;
  wholeoutlen = blocks * 3;
  outlen = wholeoutlen;

  remainder = inlen & 3;
  switch( remainder ) {
    case 1: return( -1 );
    case 2: outlen = wholeoutlen + 1; break;
    case 3: outlen = wholeoutlen + 2; break;
    default: outlen = wholeoutlen;
  }

  for( i = 0, o = 0; i < wholeinlen; i += 4, o += 3 ) {
    in1 = reverse[ ( int )pszMime[ i ] ];
    in2 = reverse[ ( int )pszMime[ i + 1 ] ];
    in3 = reverse[ ( int )pszMime[ i + 2 ] ];
    in4 = reverse[ ( int )pszMime[ i + 3 ] ];
    orvalue = in1 | in2 | in3 | in4;
    if( ( orvalue & 0x80 ) != 0 ) return( -1 );
    outval = ( in1 << 18 ) | ( in2 << 12 ) | ( in3 << 6 ) | in4;
    pBin[ o ] = ( outval >> 16 );
    pBin[ o + 1 ] = ( outval >> 8 );
    pBin[ o + 2 ] = outval;
  }

  switch ( remainder ) {
    case 2: @;
      in1 = reverse[ ( int )pszMime[ i ] ];
      in2 = reverse[ ( int )pszMime[ i + 1 ] ];
      orvalue = in1 | in2;
      outval = ( in1 << 18 ) | ( in2 << 12 );
      pBin[ o ] = ( outval >> 16 );
      break;
    case 3: @;
      in1 = reverse[ ( int )pszMime[ i ] ];
      in2 = reverse[ ( int )pszMime[ i + 1 ] ];
      in3 = reverse[ ( int )pszMime[ i + 2 ] ];
      orvalue = in1 | in2 | in3;
      outval = ( in1 << 18 ) | ( in2 << 12 ) | ( in3 << 6 );
      pBin[ o ] = ( outval >> 16 );
      pBin[ o + 1 ] = ( outval >> 8 );
      break;
    default: @;
      orvalue = 0; //keep compiler happy
  }

  if( ( orvalue & 0x80 ) != 0 ) return( -1 );
  return outlen;
}



@* The |b64_decode_len| function.

The |pMime| parameter is a null-terminated base64-encoded string.
@<Exported Functions@>+=
int b64_decode_len( const char *pszMime ) {
  int inlen, outlen;

  //strip trailing equals signs.
  inlen = strlen( pszMime );
  while( inlen > 0 && pszMime[ inlen - 1 ] == '=' ) inlen--;

  //length of the output seq, not including any partial block at the end
  outlen = inlen / 4 * 3;

  switch( inlen & 3 ) {
    case 1: @; return( -1 );
    case 2: @; outlen += 1; break;
    case 3: @; outlen += 2; break;
  }
  return( outlen );
}


@* Index.
