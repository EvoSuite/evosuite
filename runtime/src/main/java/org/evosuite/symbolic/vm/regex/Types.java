package org.evosuite.symbolic.vm.regex;

import static org.objectweb.asm.Type.BOOLEAN_TYPE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.asm.Type;

public interface Types {

	public static final String JAVA_UTIL_REGEX_MATCHER = Matcher.class
			.getName().replace(".", "/");
	public static final String JAVA_UTIL_REGEX_PATTERN = Pattern.class
			.getName().replace(".", "/");

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
			.replace(".", "/");
	public static final String JAVA_LANG_STRING_BUILDER = StringBuilder.class
			.getName().replace(".", "/");

}
