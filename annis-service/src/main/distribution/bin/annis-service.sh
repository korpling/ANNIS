#!/bin/bash

#ANNIS_HOME=/Users/he-sk/Desktop/Zeugs/Workspace/dddquery/dist

if [ -z "$ANNIS_HOME" ]; then
	echo Please set the environment variable ANNIS_HOME to the Annis distribution directory.
	exit 2
fi

# build classpath
classpath=`$ANNIS_HOME/bin/classpath.sh`

# the pid (process id) of the service is saved to this file
pid_file=$ANNIS_HOME/var/annisservice.pid

jvm_args=""
service_args=""

export ANNIS_HOME pid_file jvm_args service_args classpath

launch() {
	case `status` in
		0)
			echo "AnnisService already running."
			;;
		1)
			# launch daemon and remember pid
			echo -n "Starting AnnisService ... "
			pid=`$ANNIS_HOME/bin/internal-service-launch.sh`
         
			# check if daemon is running
			if ps -p "$pid" > /dev/null 2>&1; then
            
				echo "writing PID"
				# daemon is running, save pid
				echo $pid > $pid_file
				echo "done." 
			else
				echo "FAILED."
				exit 1
			fi
			;;
		2)
			stop
			launch
			;;
	esac
}

stop() {
	case `status` in
		0)
			kill `cat $pid_file`
			sleep 2s
			case `status` in
				1)
					echo "AnnisService stopped."
					;;
				*)
					echo "AnnisService could not be stopped."
					exit 1
			esac
			;;
		1)
			echo "AnnisService not running."
			;;
		2)
			rm $pid_file
			echo "Removed stale PID file."
	esac
}

# 0: running, 1: not running/no pid, 2: stale pid file
status() {
	# check if pid file exists
	if [ -f $pid_file ]; then
		pid=`cat $pid_file`
		
		# check if there's a process with that pid
		if ps -p "$pid" > /dev/null 2>&1; then
			echo 0
		else
			echo 2
		fi
	else
		echo 1
	fi
}

case "$1" in
	run)
		java -Dfile.encoding=UTF-8 -Dannis.home="$ANNIS_HOME" -Dannis.nosecurity="$ANNIS_NOSECURITY" -cp "$classpath" annis.service.internal.AnnisServiceRunner
		;;
	stop)
		stop 
		;;
	start)
		export service_args="-d"
		export jvm_args="-Dannisservice.pid_file=$pid_file"
		launch

		;;
	restart)
		export service_args="-d"
		export jvm_args="-Dannisservice.pid_file=$pid_file"
		stop
		launch
		;;
	status)
		case `status` in
			0)	
				echo "AnnisService: running."
				;;
			1)	
				echo "AnnisService: stopped." 
				;;
			2)	
				echo "AnnisService: stale PID file: $pid_file"
				;;
		esac
		;;
	*)
		echo "usage: annisservice.sh start|stop|run|restart|status"
		exit 4
esac

exit 0
