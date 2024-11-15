#
#   rack.pm
#
#   (C) Copyright 2000 Numina Systems Corporation.  All Rights Reserved.
#

package rack;

use strict;
use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

require Exporter;
require AutoLoader;

@ISA = qw(Exporter AutoLoader);

@EXPORT = qw(
	     input,output
);
$VERSION = '0.01';

require Trk ;

#  This module provides some helper function to help define a rack.
#  two subs. These take arguments, rack, slot, size.

sub input 
   {
   my $rack = @_[0] ;
   my $slot = @_[1] ;
   my $size = @_[2] ;

   my $i ;
   my $name ;
   my $desc ;
   my $io ;
   my $bank ;
   my $bnum ;

   for ($i=0 ; $i<$size ; $i++) 
      {
      $bnum = ($i % 8) + 1 ;
      $bank = chr(ord('a')+int($i/8)) ; 
      if($rack==1)
         {
         $name = sprintf("i%d%s%d",$slot,$bank,$bnum) ;
         }
      else
         {
         $name = sprintf("i%d:%d%s%d",$rack,$slot,$bank,$bnum) ;
         }
      $io = sprintf("I:%02.2d-%02.2d/%02.2d",$rack,$slot,$i) ;
      $desc = sprintf("Rack %d Module %d Bank %s Input %d",$rack,$slot,$bank,$bnum) ;
      Trk::dp_new($name,"pcif0",$io,$desc) ;
      }
   }


sub output 
   {
   my $rack = @_[0] ;
   my $slot = @_[1] ;
   my $size = @_[2] ;

   my $i ;
   my $name ;
   my $desc ;
   my $io ;
   my $bank ;
   my $bnum ;

   for ($i=0 ; $i<$size ; $i++) 
      {
      $bnum = ($i % 8) + 1 ;
      $bank = chr(ord('a')+int($i/8)) ; 
      if($rack==1)
         {
         $name = sprintf("o%d%s%d",$slot,$bank,$bnum) ;
         }
      else
         {
         $name = sprintf("o%d:%d%s%d",$rack,$slot,$bank,$bnum) ;
         }
      $io = sprintf("O:%02.2d-%02.2d/%02.2d",$rack,$slot,$i) ;
      $desc = sprintf("Rack %d Module %d Bank %s Output %d",$rack,$slot,$bank,$bnum) ;
      Trk::dp_new($name,"pcif0",$io,$desc) ;
      }
   }

my @code=qw( 20 a b mb_post ) ;
Trk::ex_compile("on_change","Post on Change",\@code) ;

sub atdinput {
    my $slot = @_[0] ;
    my $start = @_[1] ;
    my $size = @_[2] ;
    my $prefix = @_[3] ;

    my $i ;
    my $name ;
    my$desc ;
    my $io ;

    for ($i = $start ; $i < $size + $size ; $i++) {

	$name = sprintf("i%d/%d",$slot,$i) ;
	$io = sprintf("I:%02.2d/%02.2d",$slot,$i) ;
	$desc = sprintf("Card %d Input %d",$slot,$i) ;
	

	my $h = Trk::dp_new($prefix . $name,"atdio0",$io,$desc) ;

	
    }
}

       

sub atdoutput {
    my $slot = @_[0] ;
    my $start = @_[1] ;
    my $size = @_[2] ;
    my $prefix = @_[3] ;
    my $i ;
    my $name ;
    my $desc ;
    my $io ;

    for ($i = $start ; $i < $size + $start ; $i++) {
	$name = sprintf("o%d/%d",$slot,$i) ;
	$io = sprintf("O:%02.2d/%02.2d",$slot,$i) ;
	$desc = sprintf("Card %d Output %d",$slot,$i) ;
	
	
      Trk::dp_new($prefix . $name,"atdio0",$io,$desc) ;
    }
} 



# Autoload methods go after =cut, and are processed by the autosplit program.

1;
__END__
# Below is the stub of documentation for your module. You better edit it!

=head1 NAME

rack - Perl extension for Trak. I/O definition.

=head1 SYNOPSIS

  use rack;
  rack::input(rack,slot,count)
  rack::output(rack,slot,count)

=head1 DESCRIPTION

This module assists in defining I/O points for a GE rack. It creates data
points for a GE 9030 style module. These data points are created with the 
following syntax (for reference). [io]s/b (if rack = 1) and [io]r:s/b if 
rack > 1.
=head1 AUTHOR

Mark Olson. marko@numinasys.com

=head1 SEE ALSO

perl(1).

=cut
