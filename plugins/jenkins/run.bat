@REM
@REM Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
@REM contributors
@REM
@REM This file is part of EvoSuite.
@REM
@REM EvoSuite is free software: you can redistribute it and/or modify it
@REM under the terms of the GNU Lesser General Public License as published
@REM by the Free Software Foundation, either version 3.0 of the License, or
@REM (at your option) any later version.
@REM
@REM EvoSuite is distributed in the hope that it will be useful, but
@REM WITHOUT ANY WARRANTY; without even the implied warranty of
@REM MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
@REM Lesser Public License for more details.
@REM
@REM You should have received a copy of the GNU Lesser General Public
@REM License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
@REM

@ECHO off

del /F /Q work\plugins
call mvn -Dmaven.test.skip=true -DskipTests=true clean hpi:run <nul
