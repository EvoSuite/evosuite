/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import static org.objectweb.asm.Type.BOOLEAN_TYPE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.asm.Type;

public interface Types {

	public static final String JAVA_UTIL_REGEX_MATCHER = Matcher.class
			.getName().replace('.', '/');
	public static final String JAVA_UTIL_REGEX_PATTERN = Pattern.class
			.getName().replace('.', '/');

	public static final Type STR_TYPE = Type.getType(String.class);

	public static final Type CHARSEQ_TYPE = Type.getType(CharSequence.class);

	public static final Type MATCHER_TYPE = Type.getType(Matcher.class);

	public static final String CHARSEQ_TO_MATCHER = Type.getMethodDescriptor(
			MATCHER_TYPE, CHARSEQ_TYPE);
	public static final String TO_BOOLEAN = Type
			.getMethodDescriptor(Type.BOOLEAN_TYPE);
	public static final String STR_CHARSEQ_TO_BOOLEAN = Type
			.getMethodDescriptor(BOOLEAN_TYPE, STR_TYPE, CHARSEQ_TYPE);

	public static final String JAVA_LANG_STRING = String.class.getName()
			.replace('.', '/');
	public static final String JAVA_LANG_STRING_BUILDER = StringBuilder.class
			.getName().replace('.', '/');

}
