This directory is intended to hold the system configuration files which may be
modified for different applications.  It should always contain copies of the
current system files so that backups of the app tree will include them.  The
scripts 'install-config' and 'archive-config' in ~/bin/config may be used to
facilitate installation and archiving of the files; note that the scripts
should be run as root in order to access or overwrite system files.

General usage is as follows:
 * make a copy of the existing system files by running
   'sudo ~/bin/config/archive-config all'
 * edit the file(s) whose contents should be modified
 * install the files into the proper system locations by running
   'sudo ~/bin/config/install-config [args]', where 'args' is any combination
   of 'app', 'sys', 'ntp', 'ups', or 'all' (the last of which will
   install all of the configuration files)

Files are stored in subdirectories grouped by general function; the files are
installed or achived by using the name of the directory (ntp, etc.) as
the option to the appropriate script.  To install or archive all configuration
files from all subdirectories, use the 'all' option.

Most of the files directly replace existing system configuration files.  An
exception is 'timezone.txt', the contents of which should be a fully qualified
path to a timezone file (e.g. '/usr/share/zoneinfo/America/Chicago') that
will be linked to from '/etc/localtime'.

In some cases the installation script runs appropriate system commands
('/sbin/ldconfig' for 'rds.conf' and 'kill -1 1' for '/etc/inittab', for
example) in addition to copying files; in most cases, however, daemon programs
such as httpd and mysqld should be restarted manually.  Alternately, a full
reboot would suffice (assuming, of course, that those services are configured
to start automatically).

Finally, for security purposes, the system file '/etc/sudoers' is not included
in the installation script and should be edited by running '/usr/sbin/visudo'
as root.
