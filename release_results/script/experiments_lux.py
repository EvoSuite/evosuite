#!/usr/bin/python

# How to run EvoSuite
EVOSUITE="java -Xmx400M -jar $HOME/evosuite/evosuite-master-0.1.1-SNAPSHOT-jar-minimal.jar"

# Location of SF110
CASESTUDY_DIR="$HOME/SF110/dist"

EXPERIMENT_NAME="BaseEvoSuite"

def getScriptHead():
    s =  "#!/bin/bash\n"
    s += "#OAR -n "+EXPERIMENT_NAME+" \n"
    s += "#OAR -O "+EXPERIMENT_NAME+"-%jobid%.log \n"
    s += "#OAR -E "+EXPERIMENT_NAME+"-%jobid%.log \n"
    s += "if [ -f /etc/profile ]; then \n"
    s += ". /etc/profile \n"
    s += "fi \n"
    s += "module load Java/1.7.0_21 \n"
    return s

#import experiments_base
execfile('experiments_base.py')