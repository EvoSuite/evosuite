#!/bin/bash 
#
# Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
# contributors
#
# This file is part of EvoSuite.
#
# EvoSuite is free software: you can redistribute it and/or modify it under the
# terms of the GNU Public License as published by the Free Software Foundation,
# either version 3 of the License, or (at your option) any later version.
#
# EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
# A PARTICULAR PURPOSE. See the GNU Public License for more details.
#
# You should have received a copy of the GNU Public License along with
# EvoSuite. If not, see <http://www.gnu.org/licenses/>.
#


## to add -DskipTests when we put them back inb pom file
mvn package appassembler:assemble
chmod +x target/bin/EvoSuite

## this is done due to strange behavior on cluster... in which EvoSuite reduce to 0 the size of this jar during search
chmod -w target/repo/org/evosuite/dsc-evosuite/0.3.13/dsc-evosuite-0.3.13.jar