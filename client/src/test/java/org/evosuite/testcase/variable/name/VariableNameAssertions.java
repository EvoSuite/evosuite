package org.evosuite.testcase.variable.name;

import static org.junit.jupiter.api.Assertions.fail;

public final class VariableNameAssertions {

    private static final String[] JAVA_RESERVED_KEYWORDS = {
            // Reserved keywords
            "abstract",
            "continue",
            "for",
            "new",
            "switch",
            "assert",
            "default",
            "if",
            "package",
            "synchronized",
            "boolean",
            "do",
            "goto",
            "private",
            "this",
            "break",
            "double",
            "implements",
            "protected",
            "throw",
            "byte",
            "else",
            "import",
            "public",
            "throws",
            "case",
            "enum",
            "instanceof",
            "return",
            "transient",
            "catch",
            "extends",
            "int",
            "short",
            "try",
            "char",
            "final",
            "interface",
            "static",
            "void",
            "class",
            "finally",
            "long",
            "strictfp",
            "volatile",
            "const",
            "float",
            "native",
            "super",
            "while",
            "_",
            // Since Java 9
            // Boolean literals
            "true",
            "false",
            // Null literal
            "null",
            // Restricted identifiers
            "var",
            "yield",
            "record",
    };

    /**
     * Assert identifier matches the requirements in Java Language Specification.
     * <p>
     * See:
     * <a href="https://docs.oracle.com/javase/specs/jls/se16/html/jls-3.html#jls-3.8">Java Language Specification 16</a>
     *
     * @param identifierName The identifier to be tested.
     */
    public static void assertValidIdentifierName(String identifierName) {
        // Identifiers cannot be null
        if (identifierName == null) {
            fail("The identifiers name cannot be null");
        }

        // Identifiers cannot be empty
        if (identifierName.isEmpty()) {
            fail("The identifiers cannot be empty");
        }

        /*
         * An identifier is an unlimited-length sequence of Java letters and Java digits,
         * the first of which must be a Java letter.
         */
        if ( !Character.isJavaIdentifierStart(identifierName.charAt(0)) ) {
            fail(String.format("The identifiers must start with a Java Letter ([a-zA-Z_$]): %s", identifierName));
        }
        if (identifierName.length() > 1) {
            char[] characters = identifierName.substring(1).toCharArray();
            for (char character : characters) {
                if ( !Character.isJavaIdentifierPart(character) ) {
                    fail(String.format(
                            "The identifiers should only contain Java Letters ([a-zA-Z_$]) or Java Digit ([0-9])." +
                            "Found invalid char '%s' in identifier '%s'",
                            character, identifierName));
                }
            }
        }

        /*
         * An identifier cannot have the same spelling (Unicode character sequence) as a keyword (§3.9),
         * boolean literal (§3.10.3), or the null literal (§3.10.7), or a compile-time error occurs.
         */
        for (String reservedName : JAVA_RESERVED_KEYWORDS) {
            if (reservedName.equals(identifierName)) {
                fail(String.format("The name %s is a reserved keyword and will produce a compilation error", reservedName));
            }
        }
    }

}
