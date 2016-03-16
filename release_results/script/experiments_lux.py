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
EVOSUITE="$HOME/java8/jdk1.8.0_45/bin/java  -Xmx400M  -jar $HOME/evosuite/evosuite-master-1.0.3-SNAPSHOT.jar"

# Location of SF110
CASESTUDY_DIR="$HOME/SF110/dist"

CONFIG_NAME = "1.0.3"

EXPERIMENT_NAME="EvoSuite"

def getScriptHead():
    s =  "#!/bin/bash\n"
    s += "#OAR -n "+EXPERIMENT_NAME+" \n"
    s += "#OAR -O "+EXPERIMENT_NAME+"-%jobid%.log \n"
    s += "#OAR -E "+EXPERIMENT_NAME+"-%jobid%.log \n"
    s += "if [ -f /etc/profile ]; then \n"
    s += ". /etc/profile \n"
    s += "fi \n"
    s += "module load  lang/Java/1.7.0_21 \n"
    return s

#import experiments_base
execfile('experiments_base.py')