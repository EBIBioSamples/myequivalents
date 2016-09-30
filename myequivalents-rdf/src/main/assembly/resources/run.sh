#!/bin/sh

# This is the Bash Launcher.
# 

# This is only used at the EBI. TODO: generalise
# Do you use a proxy?
#if [ "$http_proxy" != '' ]; then
#  OPTS="$OPTS -DproxySet=true -DproxyHost=wwwcache.ebi.ac.uk -DproxyPort=3128 -DnonProxyHosts='*.ebi.ac.uk|localhost'"
#fi

# These are passed to the JVM. they're appended, so that you can predefine it from the shell
OPTS="$OPTS -Xms2G -Xmx4G -XX:PermSize=128m -XX:MaxPermSize=256m"

# We always work with universal text encoding.
OPTS="$OPTS -Dfile.encoding=UTF-8"

# Monitoring with jconsole (end-user doesn't usually need this)
#OPTS="$OPTS 
# -Dcom.sun.management.jmxremote.port=5010
# -Dcom.sun.management.jmxremote.authenticate=false
# -Dcom.sun.management.jmxremote.ssl=false"
       
# Used for invoking a command in debug mode (end user doesn't usually need this)
#OPTS="$OPTS -Xdebug -Xnoagent"
#OPTS="$OPTS -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"

# You shouldn't need to change the rest
#
###

cd "$(dirname $0)"
MYDIR="$(pwd)"

# This includes the core and the db module, plus the H2 JDBC driver. If you want to use other databases 
# you need to download the .jar files you need and set up the classpath here
# (see http://kevinboone.net/classpath.html for details)  
export CLASSPATH="$CLASSPATH:$MYDIR:$MYDIR/lib/*"

# See here for an explaination about ${1+"$@"} :
# http://stackoverflow.com/questions/743454/space-in-java-command-line-arguments 

java \
  $OPTS uk.ac.ebi.example.App ${1+"$@"}

EXCODE=$?

echo Java Finished. Quitting the Shell Too.
exit $EXCODE
