#!/bin/bash

BASEDIR=$(cd $(dirname $0)/..; pwd -P)
EVOSUITE=$BASEDIR/evosuite/EvoSuite
CASESTUDY=$BASEDIR/casestudies
#EVOSUITE=$BASEDIR/EvoSuite
#CASESTUDY=/Users/fraser/Documents/Source/javalanche-0.3.2/examples/test2/casestudy

#PROJECTS="commons_collections google_collections nano_xml commons_primitives java_collections ncs joda_time scs" #industrial
MIN_SEED=0
MAX_SEED=2

PROJECTS="ncs scs"
BASEDIR=$(pwd)

for (( SEED = $MIN_SEED; SEED < $MAX_SEED; SEED++ ))
do
    for PROJECT in $PROJECTS; do
	mkdir -p $SEED/$PROJECT
	mkdir -p report/$PROJECT
	for LENGTH in 10 50 100; do

	    NAME=${PROJECT}/rank_parents_best_${LENGTH}
	    echo -e "#!/bin/sh\n$EVOSUITE -generateTests -base_dir $CASESTUDY/$PROJECT -Dcheck_rank_length=true  -Dcheck_parents_length=true  -Dcheck_best_length=true  -Dreport_dir=$BASEDIR/report/$NAME" > $SEED/${NAME}.sh
	    chmod +x $SEED/${NAME}.sh
	    NAME=${PROJECT}/rank_parents_nobest_${LENGTH}
	    echo -e "#!/bin/sh\n$EVOSUITE -generateTests -base_dir $CASESTUDY/$PROJECT -Dcheck_rank_length=true  -Dcheck_parents_length=true  -Dcheck_best_length=false -Dreport_dir=$BASEDIR/report/$NAME" > $SEED/${NAME}.sh
	    chmod +x $SEED/${NAME}.sh
	    NAME=${PROJECT}/rank_noparents_best_${LENGTH}
	    echo -e "#!/bin/sh\n$EVOSUITE -generateTests -base_dir $CASESTUDY/$PROJECT -Dcheck_rank_length=true  -Dcheck_parents_length=false -Dcheck_best_length=true  -Dreport_dir=$BASEDIR/report/$NAME" > $SEED/${NAME}.sh
	    chmod +x $SEED/${NAME}.sh
	    NAME=${PROJECT}/rank_noparents_nobest_${LENGTH}
	    echo -e "#!/bin/sh\n$EVOSUITE -generateTests -base_dir $CASESTUDY/$PROJECT -Dcheck_rank_length=true  -Dcheck_parents_length=false -Dcheck_best_length=false -Dreport_dir=$BASEDIR/report/$NAME" > $SEED/${NAME}.sh
	    chmod +x $SEED/${NAME}.sh
	    NAME=${PROJECT}/norank_parents_best_${LENGTH}
	    echo -e "#!/bin/sh\n$EVOSUITE -generateTests -base_dir $CASESTUDY/$PROJECT -Dcheck_rank_length=false -Dcheck_parents_length=true  -Dcheck_best_length=true  -Dreport_dir=$BASEDIR/report/$NAME" > $SEED/${NAME}.sh
	    chmod +x $SEED/${NAME}.sh
	    NAME=${PROJECT}/norank_parents_nobest_${LENGTH}
	    echo -e "#!/bin/sh\n$EVOSUITE -generateTests -base_dir $CASESTUDY/$PROJECT -Dcheck_rank_length=false -Dcheck_parents_length=true  -Dcheck_best_length=false -Dreport_dir=$BASEDIR/report/$NAME" > $SEED/${NAME}.sh
	    chmod +x $SEED/${NAME}.sh
	    NAME=${PROJECT}/norank_noparents_best_${LENGTH}
	    echo -e "#!/bin/sh\n$EVOSUITE -generateTests -base_dir $CASESTUDY/$PROJECT -Dcheck_rank_length=false -Dcheck_parents_length=false -Dcheck_best_length=true  -Dreport_dir=$BASEDIR/report/$NAME" > $SEED/${NAME}.sh
	    chmod +x $SEED/${NAME}.sh
	    NAME=${PROJECT}/norank_noparents_nobest_${LENGTH}
	    echo -e "#!/bin/sh\n$EVOSUITE -generateTests -base_dir $CASESTUDY/$PROJECT -Dcheck_rank_length=false -Dcheck_parents_length=false -Dcheck_best_length=false -Dreport_dir=$BASEDIR/report/$NAME" > $SEED/${NAME}.sh
	    chmod +x $SEED/${NAME}.sh

	done
    done
done