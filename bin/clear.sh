#!/bin/bash

app_path=$(cd `dirname $0`; pwd)
app_path=$(cd `dirname $app_path`; pwd)
app_name=$(basename "$app_path")
app_log=$app_path/log
app_dump=$app_log/dump

apps=`ps -ef | grep java | grep ${app_path} | awk  '{print $2}'`
if [ -n "$apps" ];then
	echo -e "stop app ${app_name} \c"
	#kill -9 $apps
	for pid in $apps ; do
	    kill $pid > /dev/null 2>&1
	    #echo $pid
	done
fi

exist=0
COUNT=0
while [ $COUNT -lt 5 ]; do    
    echo -e ".\c"
    sleep 1
    let COUNT+=1;
    for pid in $apps ; do
        PID_EXIST=`ps -f -p $pid | grep java`
        if [ -n "$PID_EXIST" ]; then
	    exist=1
            break
        fi
    done
    if [ $exist -eq 0 ]; then
	COUNT=5
    fi
done

echo ""

apps=`ps -ef | grep java | grep ${app_path} | awk  '{print $2}'`
if [ -n "$apps" ];then
	echo -e "dumping the $app_name ...\c"
	if [ ! -d $app_log ]; then
		mkdir $app_log
	fi
	
        if [ ! -d $app_dump ]; then
                mkdir $app_dump
        fi
	DUMP_DATE=`date +%Y%m%d%H%M%S`
	DATE_DIR=$app_dump/$DUMP_DATE
	if [ ! -d $DATE_DIR ]; then
		mkdir $DATE_DIR
	fi

	for PID in $PIDS ; do
		jstack $PID > $DATE_DIR/jstack-$PID.dump 2>&1
		echo -e ".\c"
		jinfo $PID > $DATE_DIR/jinfo-$PID.dump 2>&1
		echo -e ".\c"
		jstat -gcutil $PID > $DATE_DIR/jstat-gcutil-$PID.dump 2>&1
		echo -e ".\c"
		jstat -gccapacity $PID > $DATE_DIR/jstat-gccapacity-$PID.dump 2>&1
		echo -e ".\c"
		jmap $PID > $DATE_DIR/jmap-$PID.dump 2>&1
		echo -e ".\c"
		jmap -heap $PID > $DATE_DIR/jmap-heap-$PID.dump 2>&1
		echo -e ".\c"
		jmap -histo $PID > $DATE_DIR/jmap-histo-$PID.dump 2>&1
		echo -e ".\c"
		if [ -r /usr/sbin/lsof ]; then
			/usr/sbin/lsof -p $PID > $DATE_DIR/lsof-$PID.dump
			echo -e ".\c"
		fi
	done	
	
	if [ -r /bin/netstat ]; then
		/bin/netstat -an > $DATE_DIR/netstat.dump 2>&1
		echo -e ".\c"
	fi
	if [ -r /usr/bin/iostat ]; then
		/usr/bin/iostat > $DATE_DIR/iostat.dump 2>&1
		echo -e ".\c"
	fi
	if [ -r /usr/bin/mpstat ]; then
		/usr/bin/mpstat > $DATE_DIR/mpstat.dump 2>&1
		echo -e ".\c"
	fi
	if [ -r /usr/bin/vmstat ]; then
		/usr/bin/vmstat > $DATE_DIR/vmstat.dump 2>&1
		echo -e ".\c"
	fi
	if [ -r /usr/bin/free ]; then
		/usr/bin/free -t > $DATE_DIR/free.dump 2>&1
		echo -e ".\c"
	fi
	if [ -r /usr/bin/sar ]; then
		/usr/bin/sar > $DATE_DIR/sar.dump 2>&1
		echo -e ".\c"
	fi
	if [ -r /usr/bin/uptime ]; then
		/usr/bin/uptime > $DATE_DIR/uptime.dump 2>&1
		echo -e ".\c"
	fi
	echo ""

	for pid in $apps ; do
            kill -9 $pid > /dev/null 2>&1
        done
	
	echo "killing the $app_name"
	COUNT=0
	while [ $COUNT -lt 1 ]; do
    		echo -e ".\c"
    		sleep 1
   	 	COUNT=1;
    		for pid in $apps ; do
        		PID_EXIST=`ps -f -p $pid | grep java`
        		if [ -n "$PID_EXIST" ]; then
            			COUNT=0
            			break
        		fi
    		done
	done

fi


echo "starting the $app_name"
cd ${app_path} && bin/start.sh

