#!/usr/bin/python
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


import math
import sys
import os
import re
import random
import getpass



# Path to this script
SRC=os.path.dirname(sys.argv[0])
USERNAME=getpass.getuser()

if len(sys.argv) != 7:
    print "Usage:\n<nameOfScript>.py <dir> <minSeed> <maxSeed> <classFile> <maxJobs> <cores>"
    exit(1)


BASEDIR = os.path.abspath(sys.argv[1])

if not os.path.isdir(BASEDIR):
    print "creating folder: " + BASEDIR
    os.makedirs(BASEDIR)
else:
    print "target folder already exists"
    exit(1)

# Where to put stuff (default in subdirs of BASEDIR)
REPORTS="%s/reports" % BASEDIR
SCRIPTDIR="%s/scripts" % BASEDIR
LOGDIR="%s/logs" % BASEDIR
TESTDIR="%s/tests" % BASEDIR
os.makedirs(REPORTS)
os.makedirs(SCRIPTDIR)
os.makedirs(LOGDIR)
os.makedirs(TESTDIR)


MINSEED = int(sys.argv[2])
MAXSEED = int(sys.argv[3])
MAX_JOBS = int(sys.argv[5])


# Initialize DB of target classes
CLASSES_FILE=sys.argv[4]
if not os.path.isfile(CLASSES_FILE):
   print 'Could not find class file ' + sys.argv[4]
   exit(1)

CLASSES = []
f = open(CLASSES_FILE)
for line in f:
  entry = line.rstrip().split()
  CLASSES.append(entry)
f.close()
NUM_CLASSES=len(CLASSES)


CORES = int(sys.argv[6])

if CORES <= 0 :
    print 'Wrong number of cores'
    exit(1)

# Global counter of jobs created
JOB_ID=0
CALL_ID=0
CONFIG_ID=0
SEARCH_BUDGET=0


# Creates a single call to EvoSuite
def getEvoSuiteCall(seed, configId, config, project, clazz, id, strategy, coreIndex):
  global SCRIPTDIR
  global CASESTUDY_DIR
  global JOB_ID
  global EVOSUITE
  global REPORTS
  global SEARCH_BUDGET
  global FIXED
  global CALL_ID
  global CORES
  global TIMEOUT

  logfile = "%s/%d_%s_%s_%s" % (LOGDIR, JOB_ID, configId, seed, project)
  reportfile="%s/%d/c%d" % (REPORTS, id, coreIndex)

  project = project.rstrip()

  result = "pushd . > /dev/null 2>&1 \n"
  result += "cd %s/%s\n" % (CASESTUDY_DIR, project)

  if CORES != 1 :
      result += "timeout -k " + TIMEOUT + " " + TIMEOUT + "  "

  result += ""+EVOSUITE+" "+strategy+" -class "+ clazz +" -seed "+str(seed)
  result += " -Dconfiguration_id="+configId+ " -Dgroup_id="+project
  result += " "+config+" "+FIXED
  result += " -Dreport_dir="+reportfile
  result += " -Dtest_dir=" +TESTDIR + "/s" + str(seed) + "/" + project +"/evosuite-tests"
  result += " 2>&1 | tee -a "+logfile

  if CORES != 1 :
    result += " & "
    result += "\npids+=($!) "

  result += "\n"

  result += "popd > /dev/null 2>&1 \n\n"
  CALL_ID += 1
  return result


# Creates the scripts for a given config and seed range
def createJobs(minSeed, maxSeed, configId, config, startNum, strategy="-generateSuite"):
  global SCRIPTDIR
  global CASESTUDY_DIR
  global JOB_ID
  global CONFIG_ID
  global TIMEOUT

  path_1 = "%s/%s_EvoSuite_%d.sh" %(SCRIPTDIR, USERNAME, JOB_ID)
  script=open(path_1, "a")
  script.write(getScriptHead())

  num = startNum
  coreIndex = 0

  for seed in range(minSeed, maxSeed):

    #important if cluster gives issue
    random.shuffle(CLASSES)

    for entry in CLASSES:

      if num >= ENTRIES_PER_JOB:

        if(CORES > 1):
            script.write("wait \"${pids[@]}\" \n")
            coreIndex = 0
        script.close()

        JOB_ID +=1
        num = 1

        path_2 = "%s/%s_EvoSuite_%d.sh" %(SCRIPTDIR, USERNAME, JOB_ID)
        script=open(path_2, "a")
        script.write(getScriptHead())
      else:
        num += 1

      if(CORES>1 and coreIndex==0):
        #first call
        script.write("pids=() \n\n")

      script.write(getEvoSuiteCall(seed, configId, config, entry[0], entry[1], JOB_ID, strategy, coreIndex))

      if(CORES > 1):
        coreIndex += 1

      if(CORES > 1  and coreIndex == CORES):
        script.write("\n\n wait \"${pids[@]}\" \n\n")
        coreIndex = 0

  if(CORES > 1):
    script.write("wait \"${pids[@]}\" \n")
  script.close()

  #JOB_ID += 1
  CONFIG_ID += 1

  return num


TIMEOUT="10m"

# Fixed set of parameters to use in all jobs
FIXED = " -mem 2500 \
  -Dplot=false \
  -Dtest_comments=false \
  -Dshow_progress=false \
  -Denable_asserts_for_evosuite=true \
  -Dsearch_budget=120 \
  -Dinitialization_timeout=120 \
  -Dglobal_timeout=120 \
  -Dminimization_timeout=60 \
  -Dassertion_timeout=60 \
  -Dextra_timeout=60 \
  -Djunit_check_timeout=60 \
  -Doutput_variables=\"configuration_id,group_id,TARGET_CLASS,search_budget,Length,Size,LineCoverage,BranchCoverage,OutputCoverage,WeakMutationScore,Implicit_MethodExceptions\" \
 "


# How many calls to EvoSuite should go in one script
N_CONF = 1  #(depends on number of configurations)
ENTRIES_PER_JOB= math.ceil( (N_CONF * (NUM_CLASSES * (MAXSEED - MINSEED)) / float(MAX_JOBS) ) )


# Create the actual jobs

createJobs(MINSEED, MAXSEED, CONFIG_NAME , " " , 0, "-generateSuite")

print "Seeds: %d, projects: %d, configs: %d" % ((MAXSEED - MINSEED), NUM_CLASSES, CONFIG_ID)
print "Total number of jobs created: %d" % (JOB_ID+1)
print "Total number of calls to EvoSuite: %d" % CALL_ID
print "Calls per job: %d" % ENTRIES_PER_JOB
