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
package com.examples.with.different.packagename.concolic;

import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;

/**
 * Base class for commons-math unchecked exceptions.
 *
 * @version $Revision: 786478 $ $Date: 2009-06-19 08:33:36 -0400 (Fri, 19 Jun 2009) $
 * @since 2.0
 */
public class MathRuntimeException extends RuntimeException {

    /**
     * Serializable version identifier.
     */
    private static final long serialVersionUID = -5128983364075381060L;

    /**
     * Pattern used to build the message.
     */
    private final String pattern;

    /**
     * Arguments used to build the message.
     */
    private final Object[] arguments;

    /**
     * Translate a string to a given locale.
     *
     * @param s      string to translate
     * @param locale locale into which to translate the string
     * @return translated string or original string
     * for unsupported locales or unknown strings
     */
    private static String translate(final String s, final Locale locale) {
        try {
            ResourceBundle bundle =
                    ResourceBundle.getBundle("org.apache.commons.math.MessagesResources", locale);
            if (bundle.getLocale().getLanguage().equals(locale.getLanguage())) {
                // the value of the resource is the translated string
                return bundle.getString(s);
            }

        } catch (MissingResourceException mre) {
            // do nothing here
        }

        // the locale is not supported or the resource is unknown
        // don't translate and fall back to using the string as is
        return s;

    }

    /**
     * Builds a message string by from a pattern and its arguments.
     *
     * @param locale    Locale in which the message should be translated
     * @param pattern   format specifier
     * @param arguments format arguments
     * @return a message string
     */
    private static String buildMessage(final Locale locale, final String pattern,
                                       final Object... arguments) {
        return (pattern == null) ? "" : new MessageFormat(translate(pattern, locale), locale).format(arguments);
    }

    /**
     * Constructs a new <code>MathRuntimeException</code> with specified
     * formatted detail message.
     * Message formatting is delegated to {@link java.text.MessageFormat}.
     *
     * @param pattern   format specifier
     * @param arguments format arguments
     */
    public MathRuntimeException(final String pattern, final Object... arguments) {
        super(buildMessage(Locale.US, pattern, arguments));
        this.pattern = pattern;
        this.arguments = (arguments == null) ? new Object[0] : arguments.clone();
    }

    /**
     * Constructs a new <code>MathRuntimeException</code> with specified
     * nested <code>Throwable</code> root cause.
     *
     * @param rootCause the exception or error that caused this exception
     *                  to be thrown.
     */
    public MathRuntimeException(final Throwable rootCause) {
        super(rootCause);
        this.pattern = getMessage();
        this.arguments = new Object[0];
    }

    /**
     * Constructs a new <code>MathRuntimeException</code> with specified
     * formatted detail message and nested <code>Throwable</code> root cause.
     * Message formatting is delegated to {@link java.text.MessageFormat}.
     *
     * @param rootCause the exception or error that caused this exception
     *                  to be thrown.
     * @param pattern   format specifier
     * @param arguments format arguments
     */
    public MathRuntimeException(final Throwable rootCause,
                                final String pattern, final Object... arguments) {
        super(buildMessage(Locale.US, pattern, arguments), rootCause);
        this.pattern = pattern;
        this.arguments = (arguments == null) ? new Object[0] : arguments.clone();
    }

    /**
     * Gets the pattern used to build the message of this throwable.
     *
     * @return the pattern used to build the message of this throwable
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Gets the arguments used to build the message of this throwable.
     *
     * @return the arguments used to build the message of this throwable
     */
    public Object[] getArguments() {
        return arguments.clone();
    }

    /**
     * Gets the message in a specified locale.
     *
     * @param locale Locale in which the message should be translated
     * @return localized message
     */
    public String getMessage(final Locale locale) {
        return buildMessage(locale, pattern, arguments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLocalizedMessage() {
        return getMessage(Locale.getDefault());
    }

    /**
     * Prints the stack trace of this exception to the standard error stream.
     */
    @Override
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    /**
     * Prints the stack trace of this exception to the specified stream.
     *
     * @param out the <code>PrintStream</code> to use for output
     */
    @Override
    public void printStackTrace(final PrintStream out) {
        synchronized (out) {
            PrintWriter pw = new PrintWriter(out, false);
            printStackTrace(pw);
            // Flush the PrintWriter before it's GC'ed.
            pw.flush();
        }
    }

    /**
     * Constructs a new <code>ArithmeticException</code> with specified formatted detail message.
     * Message formatting is delegated to {@link java.text.MessageFormat}.
     *
     * @param pattern   format specifier
     * @param arguments format arguments
     * @return built exception
     */
    public static ArithmeticException createArithmeticException(final String pattern,
                                                                final Object... arguments) {
        return new ArithmeticException(buildMessage(Locale.US, pattern, arguments)) {

            /** Serializable version identifier. */
            private static final long serialVersionUID = 7705628723242533939L;

            /** {@inheritDoc} */
            @Override
            public String getLocalizedMessage() {
                return buildMessage(Locale.getDefault(), pattern, arguments);
            }

        };
    }

    /**
     * Constructs a new <code>ArrayIndexOutOfBoundsException</code> with specified formatted detail message.
     * Message formatting is delegated to {@link java.text.MessageFormat}.
     *
     * @param pattern   format specifier
     * @param arguments format arguments
     * @return built exception
     */
    public static ArrayIndexOutOfBoundsException createArrayIndexOutOfBoundsException(final String pattern,
                                                                                      final Object... arguments) {
        return new ArrayIndexOutOfBoundsException(buildMessage(Locale.US, pattern, arguments)) {

            /** Serializable version identifier. */
            private static final long serialVersionUID = -3394748305449283486L;

            /** {@inheritDoc} */
            @Override
            public String getLocalizedMessage() {
                return buildMessage(Locale.getDefault(), pattern, arguments);
            }

        };
    }

    /**
     * Constructs a new <code>EOFException</code> with specified formatted detail message.
     * Message formatting is delegated to {@link java.text.MessageFormat}.
     *
     * @param pattern   format specifier
     * @param arguments format arguments
     * @return built exception
     */
    public static EOFException createEOFException(final String pattern,
                                                  final Object... arguments) {
        return new EOFException(buildMessage(Locale.US, pattern, arguments)) {

            /** Serializable version identifier. */
            private static final long serialVersionUID = 279461544586092584L;

            /** {@inheritDoc} */
            @Override
            public String getLocalizedMessage() {
                return buildMessage(Locale.getDefault(), pattern, arguments);
            }

        };
    }

    /**
     * Constructs a new <code>IOException</code> with specified nested
     * <code>Throwable</code> root cause.
     * <p>This factory method allows chaining of other exceptions within an
     * <code>IOException</code> even for Java 5. The constructor for
     * <code>IOException</code> with a cause parameter was introduced only
     * with Java 6.</p>
     *
     * @param rootCause the exception or error that caused this exception
     *                  to be thrown.
     * @return built exception
     */
    public static IOException createIOException(final Throwable rootCause) {
        IOException ioe = new IOException(rootCause.getLocalizedMessage());
        ioe.initCause(rootCause);
        return ioe;
    }

    /**
     * Constructs a new <code>IllegalArgumentException</code> with specified formatted detail message.
     * Message formatting is delegated to {@link java.text.MessageFormat}.
     *
     * @param pattern   format specifier
     * @param arguments format arguments
     * @return built exception
     */
    public static IllegalArgumentException createIllegalArgumentException(final String pattern,
                                                                          final Object... arguments) {
        return new IllegalArgumentException(buildMessage(Locale.US, pattern, arguments)) {

            /** Serializable version identifier. */
            private static final long serialVersionUID = -6555453980658317913L;

            /** {@inheritDoc} */
            @Override
            public String getLocalizedMessage() {
                return buildMessage(Locale.getDefault(), pattern, arguments);
            }

        };
    }

    /**
     * Constructs a new <code>IllegalArgumentException</code> with specified nested
     * <code>Throwable</code> root cause.
     *
     * @param rootCause the exception or error that caused this exception
     *                  to be thrown.
     * @return built exception
     */
    public static IllegalArgumentException createIllegalArgumentException(final Throwable rootCause) {
        IllegalArgumentException iae = new IllegalArgumentException(rootCause.getLocalizedMessage());
        iae.initCause(rootCause);
        return iae;
    }

    /**
     * Constructs a new <code>IllegalStateException</code> with specified formatted detail message.
     * Message formatting is delegated to {@link java.text.MessageFormat}.
     *
     * @param pattern   format specifier
     * @param arguments format arguments
     * @return built exception
     */
    public static IllegalStateException createIllegalStateException(final String pattern,
                                                                    final Object... arguments) {
        return new IllegalStateException(buildMessage(Locale.US, pattern, arguments)) {

            /** Serializable version identifier. */
            private static final long serialVersionUID = -95247648156277208L;

            /** {@inheritDoc} */
            @Override
            public String getLocalizedMessage() {
                return buildMessage(Locale.getDefault(), pattern, arguments);
            }

        };
    }

    /**
     * Constructs a new <code>ConcurrentModificationException</code> with specified formatted detail message.
     * Message formatting is delegated to {@link java.text.MessageFormat}.
     *
     * @param pattern   format specifier
     * @param arguments format arguments
     * @return built exception
     */
    public static ConcurrentModificationException createConcurrentModificationException(final String pattern,
                                                                                        final Object... arguments) {
        return new ConcurrentModificationException(buildMessage(Locale.US, pattern, arguments)) {

            /** Serializable version identifier. */
            private static final long serialVersionUID = 6134247282754009421L;

            /** {@inheritDoc} */
            @Override
            public String getLocalizedMessage() {
                return buildMessage(Locale.getDefault(), pattern, arguments);
            }

        };
    }

    /**
     * Constructs a new <code>NoSuchElementException</code> with specified formatted detail message.
     * Message formatting is delegated to {@link java.text.MessageFormat}.
     *
     * @param pattern   format specifier
     * @param arguments format arguments
     * @return built exception
     */
    public static NoSuchElementException createNoSuchElementException(final String pattern,
                                                                      final Object... arguments) {
        return new NoSuchElementException(buildMessage(Locale.US, pattern, arguments)) {

            /** Serializable version identifier. */
            private static final long serialVersionUID = 7304273322489425799L;

            /** {@inheritDoc} */
            @Override
            public String getLocalizedMessage() {
                return buildMessage(Locale.getDefault(), pattern, arguments);
            }

        };
    }

    /**
     * Constructs a new <code>NullPointerException</code> with specified formatted detail message.
     * Message formatting is delegated to {@link java.text.MessageFormat}.
     *
     * @param pattern   format specifier
     * @param arguments format arguments
     * @return built exception
     */
    public static NullPointerException createNullPointerException(final String pattern,
                                                                  final Object... arguments) {
        return new NullPointerException(buildMessage(Locale.US, pattern, arguments)) {

            /** Serializable version identifier. */
            private static final long serialVersionUID = -3075660477939965216L;

            /** {@inheritDoc} */
            @Override
            public String getLocalizedMessage() {
                return buildMessage(Locale.getDefault(), pattern, arguments);
            }

        };
    }

    /**
     * Constructs a new <code>ParseException</code> with specified
     * formatted detail message.
     * Message formatting is delegated to {@link java.text.MessageFormat}.
     *
     * @param offset    offset at which error occurred
     * @param pattern   format specifier
     * @param arguments format arguments
     * @return built exception
     */
    public static ParseException createParseException(final int offset,
                                                      final String pattern,
                                                      final Object... arguments) {
        return new ParseException(buildMessage(Locale.US, pattern, arguments), offset) {

            /** Serializable version identifier. */
            private static final long serialVersionUID = -1103502177342465975L;

            /** {@inheritDoc} */
            @Override
            public String getLocalizedMessage() {
                return buildMessage(Locale.getDefault(), pattern, arguments);
            }

        };
    }

    /**
     * Create an {@link java.lang.RuntimeException} for an internal error.
     *
     * @param cause underlying cause
     * @return an {@link java.lang.RuntimeException} for an internal error
     */
    public static RuntimeException createInternalError(final Throwable cause) {

        final String pattern = "internal error, please fill a bug report at {0}";
        final String argument = "https://issues.apache.org/jira/browse/MATH";

        return new RuntimeException(buildMessage(Locale.US, pattern, argument)) {

            /** Serializable version identifier. */
            private static final long serialVersionUID = -201865440834027016L;

            /** {@inheritDoc} */
            @Override
            public String getLocalizedMessage() {
                return buildMessage(Locale.getDefault(), pattern, argument);
            }

        };

    }

}
