/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package com.examples.with.different.packagename.exception;

/**
 * Created by gordon on 19/03/2016.
 */
public class CatchWithUnknownThrow {

    char x = 0;

    // Based on com.soops.CEN4010.JMCA.JParser.JavaParserTokenManager
    public int jjStartNfaWithStates_0(ClassThrowingIOException s) {
        try {
            x = s.readChar();
        } catch (java.io.IOException e) {
            return 0;
        }
        return 0;
    }
}
