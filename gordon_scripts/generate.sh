#!/bin/bash
if [[ $OSTYPE == 'linux-gnu' ]]; then
	python_cmd='python'
else
	python_cmd='python2'
fi

if [ $# -eq 3 ] 
then
    echo 'Seed from ' $1 ' to ' $2 '- Threads = ' $3 
else
    echo "Invalid number of arguments"
    exit 1
fi

seed1=$1
seed2=$2
proc=$3

echo "Choose the version of MOSA/PerformanceMOSA that you want to execute"
echo "For more than one version, separate the number with a comma"
echo "1 = DYNAMOSA"
# echo "2 = Preference Criterion with SUM combination"
echo "3 = Crowding Distance with MIN_MAX combination"
read choice

rm -rf run.sh
echo '#!/bin/bash' >> run.sh
chmod 777 run.sh
for i in $(echo $choice | sed "s/,/ /g")
do
	if [ "$i" == "1" ]; then
		${python_cmd} scripts/DYNAMOSA.py mosa $seed1 $seed2 subject.txt 1 $proc
		echo 'chmod 777 mosa/scripts/ubuntu_EvoSuite_0.sh' >> run.sh
		echo './mosa/scripts/ubuntu_EvoSuite_0.sh' >> run.sh
	fi
	if [ "$i" == "2" ]; then
		${python_cmd} scripts/archive_sum.py archive_sum $seed1 $seed2 subject.txt 1 $proc	
		echo 'chmod 777 archive_sum/scripts/ubuntu_EvoSuite_0.sh' >> run.sh
		echo './archive_sum/scripts/ubuntu_EvoSuite_0.sh' >> run.sh
	fi
	if [ "$i" == "3" ]; then
		${python_cmd} scripts/crowding_min_max.py crowding_min_max $seed1 $seed2 subject.txt 1 $proc	
		echo 'chmod 777 crowding_min_max/scripts/ubuntu_EvoSuite_0.sh' >> run.sh
		echo './crowding_min_max/scripts/ubuntu_EvoSuite_0.sh' >> run.sh		
	fi
done
