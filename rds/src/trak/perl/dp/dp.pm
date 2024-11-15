package dp;

use strict;
use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

require Exporter;
require AutoLoader;

@ISA = qw(Exporter AutoLoader);
# Items to export into callers namespace by default. Note: do not export
# names by default without a very good reason. Use EXPORT_OK instead.
# Do not simply export all your public functions/methods/constants.
@EXPORT = qw(
 slave,link,revlink,alias,linkout,handle
);
$VERSION = '0.01';


# Preloaded methods go here.

require Trk ;
require trakbase ;

my @code = qw(  edge.value swap  dp.value.set ) ;
Trk::ex_compile('slave_edge',"Attach to Slave I/O",\@code) ;
@code=qw(  edge.value  not swap dp.value.set ) ;
Trk::ex_compile("unslave_edge","Attach to Slave I/O (reverse)",\@code) ;

sub slave {
   my $slave = @_[0] ;
   my $master= @_[1] ;
   my $direction = @_[2] ;

   my $m_h ;
   my $s_h ;

   $s_h = Trk::dp_handle($slave) ;
   $m_h = Trk::dp_handle($master) ;
   my @code ;

   if ($direction eq "reverse") {
      @code=($slave, "unslave_edge") ;
      Trk::dp_set($s_h,!Trk::dp_get($m_h)) ;
   }
   else {
      @code=($slave, "slave_edge") ;
   }
   my $name =   $slave . ">" . $master ;
   Trk::ex_compile($name,"slave data points",\@code) ;
   trakbase::leading($master,$name,"-> " . $slave) ;
   trakbase::trailing($master,$name,"-> " . $slave) ;
   my $foo = pop(@code) ;
   return $m_h ;
}

sub alias {
    my $a = @_[0] ;
    my $b = @_[1] ;
    
    slave($a,$b) ;
    slave($b,$a) ;
}

sub link {
    my $new = @_[0] ;
    my $old = @_[1] ;
    my $desc = @_[2] ;
    
  Trk::dp_new($new,"virtual","blank",$desc) ;
  return dp::slave ($new,$old) ;
}

sub revlink {
    my $new = @_[0] ;
    my $old = @_[1] ;
    my $desc = @_[2] ;
    

  Trk::dp_new($new,"virtual","blank",$desc) ;
  return dp::slave ($new,$old,"reverse") ;
}

sub linkout {
    my $new = @_[0] ;
    my $old = @_[1] ;
    my $desc = @_[2] ;
    
  my $n = $new . "<" . $old ;
  my $o_h = Trk::dp_new($new,"virtual","blank",$desc) ;
#  $o_h = trakbase::dhandle($old) ;
#  print "\tlinkout old handle = $o_h\n" ;
  my @code=( "edge.value", $old, "dp.value.set") ;
  Trk::ex_compile($n,"Slave output",\@code) ;
  my $foo = pop(@code)  ;
#  print "code compiled\n" ;
  trakbase::leading($new,$n,$desc) ;
  trakbase::trailing($new,$n,$desc) ;
  return $o_h ;

}


sub handle {
   my $name = @_[0] ;
  
   my $h = Trk::dp_handle($name) ;
   if ($h == -1) {
     die "Failed to locate dp: " . $name ;
   }
   return $h ;
}

sub virtual {
   my $name = @_[0] ;
   my $desc = @_[1] ;
   my $h = Trk::dp_handle($name) ;


   if ($h == -1) {
      $h = Trk::dp_new($name,"virtual","blank",$desc) ;
   }
   return $h ;
}
# Autoload methods go after =cut, and are processed by the autosplit program.

1;
__END__
# Below is the stub of documentation for your module. You better edit it!

=head1 NAME

dp - Perl extension for blah blah blah

=head1 SYNOPSIS

  require dp;
 dp::handle(name) ;
 dp::link(new,old,desc) ;
 dp::alias(a,b) ;
 dp::revlink(new,old,desc) ;
 dp::slave(new,old,["reverse"]) ;
 dp::linkout(new,old,desc) ;
 
=head1 DESCRIPTION

This module provides a few helper "subs" for working with dp's in Trak. Two
subs: slave links two data points. Both are assumed to already exist. The 
"new" data point, is slaved to the old. The "new" data point will change to 
match the old on transitions of the old (code is attached to both edges of
the old data point).

link is the same as above, but creates the new data point (in the virtual
device).

  handle returns the handle of the dp by name.
  slave links a master to a slave (or reverses it). The slave is not 
    created. This ties the slave to the master *only* on transitions of the
    master.
  link, creates a new dp, linking the old to the new (on transition).
  linkout creates a new variable and slaves an existing dp to it.
  alias doesn't create a dp, but 
=head1 AUTHOR

Mark Olson marko@numinasys.com

=head1 SEE ALSO

Trk,perl(1).

=cut
