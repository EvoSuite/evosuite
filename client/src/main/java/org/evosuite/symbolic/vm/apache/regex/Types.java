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
			.getName().replace(".", "/");

}
