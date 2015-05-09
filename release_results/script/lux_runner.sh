#!/bin/bash

if [ $# != 4 ] 
 then
	echo Usage: $0 DIR  RULE CORES TIME
	exit 1
fi

DIR=$1
RULE=$2
CORES=$3
TIME=$4

if [ ! -e $DIR ] 
 then
		echo Error:  directory $DIR does not exist
		exit 1
fi	


SCRIPTS=`ls $DIR/*sh | grep $RULE`

echo Going to submit `echo $SCRIPTS | wc -w` jobs

for script in $SCRIPTS
do
		echo submitting $script	
		chmod +x 	$script	
		##oarsub -t besteffort -l nodes=1/core=$CORES,walltime=$TIME  $script  	
		oarsub  -l nodes=1/core=$CORES,walltime=$TIME  $script  	
done