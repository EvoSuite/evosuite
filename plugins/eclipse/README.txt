====
    Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
    contributors

    This file is part of EvoSuite.

    EvoSuite is free software: you can redistribute it and/or modify it
    under the terms of the GNU Lesser Public License as published by the
    Free Software Foundation, either version 3.0 of the License, or (at your
    option) any later version.

    EvoSuite is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
    Lesser Public License for more details.

    You should have received a copy of the GNU Lesser Public License along
    with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
====

Building Eclipse projects with Maven is cumbersome, as mixing Eclipse and
Maven dependencies seems rather impossible. For now, the build thus isn't
automated.

To build the plugin, evosuite-eclipse-core expects a copy of evosuite.jar.
Then, either build a deployable feature from Eclipse for
evosuite-eclipse-feature, or build the evosuite-eclipse-site from within
Eclipse.
