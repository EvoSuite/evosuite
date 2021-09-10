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
package com.examples.with.different.packagename.sette;

/*
 * SETTE - Symbolic Execution based Test Tool Evaluator
 *
 * SETTE is a tool to help the evaluation and comparison of symbolic execution based test input
 * generator tools.
 *
 * Budapest University of Technology and Economics (BME)
 *
 * Authors: Lajos Cseppentő <lajos.cseppento@inf.mit.bme.hu>, Zoltán Micskei <micskeiz@mit.bme.hu>
 *
 * Copyright 2014-2015
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class LO_Other {
    private LO_Other() {
        throw new UnsupportedOperationException("Static class");
    }

    public static int inheritsAPIGuessOnePrimitive(int x) {
        com.examples.with.different.packagename.sette.FingerNumber a = new FingerNumber(x);

        if (a.intValue() == 5) {
            return 1;
        } else if (a.intValue() == 11) {
            // impossible
            throw new RuntimeException();
        } else {
            return 0;
        }
    }

    public static int inheritsAPIGuessOneObject(FingerNumber a) {
        if (a == null) {
            return -1;
        } else if (a.intValue() == 5) {
            return 1;
        } else if (a.intValue() == 11) {
            // impossible
            throw new RuntimeException();
        } else {
            return 0;
        }
    }

    public static int inheritsAPIGuessTwoPrimitives(int x, int y) {
        FingerNumber a = new FingerNumber(x);
        FingerNumber b = new FingerNumber(y);

        FingerNumber sum = a.add(b);

        if (sum.intValue() == 5) {
            return 1;
        } else if (sum.intValue() == 11) {
            // impossible
            throw new RuntimeException();
        } else {
            return 0;
        }
    }

    public static int inheritsAPIGuessTwoObjects(FingerNumber a, FingerNumber b) {
        if (a == null || b == null) {
            return -1;
        }

        FingerNumber sum = a.add(b);

        if (sum.intValue() == 5) {
            return 1;
        } else if (sum.intValue() == 11) {
            // impossible
            throw new RuntimeException();
        } else {
            return 0;
        }
    }

    public static int associatesAPIGuessValidDateFormat(String s) {
        Date parsed = DateParser.parse(s);
        if (parsed != null) {
            return 1;
        } else {
            return 0;
        }
    }

    public static int associatesAPIGuessDate(String s) {
        Date parsed = DateParser.parse(s);
        if (parsed != null) {
            if (parsed.getTime() == 685843200000L) {
                // 1991-09-26
                return 1;
            } else {
                return -1;
            }
        } else {
            return 0;
        }
    }

    public static int guessValidUUID(String s) {
        if (s == null) {
            return -1;
        }

        try {
            UUID.fromString(s);
            return 1;
        } catch (Exception ex) {
            return 0;
        }
    }

    public static int guessUUID(String s) {
        if (s == null) {
            return -1;
        }

        try {
            UUID uuid = UUID.fromString(s);
            if (uuid.getMostSignificantBits() == 0x1234
                    && uuid.getLeastSignificantBits() == 0xABCD) {
                return 1;
            } else {
                return -2;
            }
        } catch (Exception ex) {
            return 0;
        }
    }

    public static int regexCaseSensitive(String s) {
        if (s == null) {
            return -1;
        }

        if (Pattern.matches("^[0-9a-zA-Z]{6}$", s)) {
            return 1;
        } else {
            return 0;
        }
    }

    public static int regexCaseInsensitive(String s) {
        if (s == null) {
            return -1;
        }

        Pattern p = Pattern.compile("[a-z0-9]{6}", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(s);

        if (m.matches()) {
            return 1;
        } else {
            return 0;
        }
    }
}
