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
package org.evosuite.symbolic.vm.regex;

import org.objectweb.asm.Type;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.objectweb.asm.Type.BOOLEAN_TYPE;

public interface Types {

    String JAVA_UTIL_REGEX_MATCHER = Matcher.class
            .getName().replace('.', '/');
    String JAVA_UTIL_REGEX_PATTERN = Pattern.class
            .getName().replace('.', '/');

    Type STR_TYPE = Type.getType(String.class);

    Type CHARSEQ_TYPE = Type.getType(CharSequence.class);

    Type MATCHER_TYPE = Type.getType(Matcher.class);

    String CHARSEQ_TO_MATCHER = Type.getMethodDescriptor(
            MATCHER_TYPE, CHARSEQ_TYPE);
    String TO_BOOLEAN = Type
            .getMethodDescriptor(Type.BOOLEAN_TYPE);
    String STR_CHARSEQ_TO_BOOLEAN = Type
            .getMethodDescriptor(BOOLEAN_TYPE, STR_TYPE, CHARSEQ_TYPE);

    String JAVA_LANG_STRING = String.class.getName()
            .replace('.', '/');
    String JAVA_LANG_STRING_BUILDER = StringBuilder.class
            .getName().replace('.', '/');

}
