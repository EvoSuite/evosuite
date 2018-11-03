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


# How to run EvoSuite
EVOSUITE="java -Xmx400M -jar /fastdata/ac1gf/release_results/evosuite-master-1.0.6-SNAPSHOT.jar"

# Location of SF110
CASESTUDY_DIR="/data/ac1gf/sf110"

CONFIG_NAME = "1.0.6"

EXPERIMENT_NAME="EvoSuite"

def getScriptHead():
    s =  "#!/bin/bash\n"
    s += "#$  -l h_rt=8:00:00\n"
    s += "#$  -l mem=8G\n"
    s += "#$  -l rmem=4G\n"
    s += "module load apps/java/1.8u71\n"
    s += "export MALLOC_ARENA_MAX=1\n"
    s += "export PATH=$JAVA_HOME/bin:$PATH\n"
    s += "export _JAVA_OPTIONS=-Xmx1500M\n"
    return s

#import experiments_base
execfile('experiments_base.py')
