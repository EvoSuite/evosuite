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
package org.evosuite.symbolic.vm.apache.regex;

import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Matcher;
import org.objectweb.asm.Type;

public interface Types {

	public static final Type STR_TYPE = Type.getType(String.class);

	public static final Type PATTERN_TYPE = Type.getType(Pattern.class);

	public static final String STR_STR_TO_BOOLEAN = Type.getMethodDescriptor(
			Type.BOOLEAN_TYPE, STR_TYPE, PATTERN_TYPE);

	public static final String ORG_APACHE_ORO_TEXT_REGEX_PERL5MATCHER = Perl5Matcher.class
			.getName().replace('.', '/');

}
