use ExtUtils::MakeMaker;
# See lib/ExtUtils/MakeMaker.pm for details of how to influence
# the contents of the Makefile that is written.
WriteMakefile(
    'NAME'	=> 'Trk',
    'VERSION_FROM' => 'Trk.pm', # finds $VERSION
    'LIBS'	=> ['-L/home/rds/lib -lrds_trak'],   # e.g., '-lm' 
    'DEFINE'	=> '',     # e.g., '-DHAVE_SOMETHING' 
    'INC'	=> '-I/home/rds/include',     # e.g., '-I/usr/include/other' 
);
