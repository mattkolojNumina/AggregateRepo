#!/usr/bin/perl

require Trk ;
require trakbase ;
require dp ;
require rp ;
require timer ;

require ethercat ;
require h3g ;
require cif ;
require linx ;

my @code ;

ethercat::add('EK1100',{prefix=>''}) ;
ethercat::add('EL1018',{prefix=>'m1'}) ;
ethercat::add('EL2008',{prefix=>'m2'}) ;

@code=qw( 1 swap dp.value.set ) ;
Trk::ex_compile('on','dp on',\@code) ;

@code=qw( 0 swap dp.value.set ) ;
Trk::ex_compile('off','dp off',\@code) ;

exit(1) ;

h3g::add('P1',{stem=>'3g11/',ip=>'192.168.0.11'}) ;

cif::add('o1','O:0/0','output 1') ;
cif::add('o2','O:0/1','output 2') ;
cif::add('o3','O:0/2','output 3') ;
cif::add('o4','O:0/3','output 4') ;
cif::add('o5','O:0/4','output 5') ;
cif::add('o6','O:0/5','output 6') ;
cif::add('o7','O:0/6','output 7') ;
cif::add('o8','O:0/7','output 8') ;

cif::add('i1','I:0/0','input 1') ;
cif::add('i2','I:0/1','input 2') ;
cif::add('i3','I:0/2','input 3') ;
cif::add('i4','I:0/3','input 4') ;
cif::add('i5','I:0/4','input 5') ;
cif::add('i6','I:0/5','input 6') ; 
cif::add('i7','I:0/6','input 7') ;
cif::add('i8','I:0/7','input 8') ;

linx::add('z1',{'ip'=>'192.168.1.11'}) ;


