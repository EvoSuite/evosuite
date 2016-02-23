#!/bin/bash
#
# Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
# contributors
#
# This file is part of EvoSuite.
#
# EvoSuite is free software: you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as published
# by the Free Software Foundation, either version 3.0 of the License, or
# (at your option) any later version.
#
# EvoSuite is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
#


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