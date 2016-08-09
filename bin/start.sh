#!/bin/bash

## who am i? ##
_SCRIPT="$(readlink -f ${BASH_SOURCE[0]})"

# A POSIX variable
# Reset in case getopts has been used previously in the shell. 
OPTIND=1

function show_help {
    echo ""
    echo "start.sh [-d]"
    echo ""
    echo "-d     Start server in debug mode, allowing to connect via the jdwp protocol."
    echo ""
}


## Delete last component from $_script ##
BINDIR=$(cd "$(dirname "$0")"; pwd)
ROOTDIR="$(dirname $BINDIR)"
CONFDIR=$ROOTDIR/conf
LIBDIR=$ROOTDIR/libs
LOGDIR=$ROOTDIR/log
DATADIR=$ROOTDIR/data

MAIN_FUN=`sed '/server.main/!d;s/.*=//' ${CONFDIR}/server.properties | tr -d '\r'`
echo $MAIN_FUN
if [ -z "$MAIN_FUN" ]; then
    echo "server.properties file do not existed or main function not existed"
    exit
fi

if [ "$JAVA_HOME" != "" ]; then
  JAVA="$JAVA_HOME/bin/java"
else
  JAVA=`which java`
fi

debug=0

while getopts "h?d" opt; do
    case "$opt" in
    h|\?)
        show_help
        exit 0
        ;;
    d)  debug=1
        ;;
    esac
done

#add the conf dir to classpath
CLASSPATH="$CONFDIR:$CLASSPATH"

_jarlist=`find $LIBDIR -name *.jar`
for i in $_jarlist
do
  CLASSPATH="$i:$CLASSPATH"
done

# Set number of open files to a large number
ulimit -n 500000


DEBUG_OPTION="-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=4000,suspend=n"


if [ $debug -eq 1 ]
then
    $JAVA $DEBUG_OPTION -Xms1024m -Xmx2048m -cp $CLASSPATH $MAIN_FUN
else
    $JAVA -Xms1024m -Xmx2048m -cp $CLASSPATH  $MAIN_FUN
fi

