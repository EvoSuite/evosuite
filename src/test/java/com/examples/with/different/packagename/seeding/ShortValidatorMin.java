package com.examples.with.different.packagename.seeding;

/*
 * Reduced version of ShortValidator class
 * Trivial methods removed
 */


import java.text.Format;
import java.util.Locale;

public class ShortValidatorMin  extends AbstractNumberValidator {

    private static final ShortValidatorMin VALIDATOR = new ShortValidatorMin();

    
    /**
     * Return a singleton instance of this validator.
     * @return A singleton instance of the ShortValidator.
     */
    public static ShortValidatorMin getInstance() {
        return VALIDATOR;
    }

    /**
     * Construct a <i>strict</i> instance.
     */
    public ShortValidatorMin() {
        this(true, STANDARD_FORMAT);
    }

    /**
     * <p>Construct an instance with the specified strict setting
     *    and format type.</p>
     *
     * <p>The <code>formatType</code> specified what type of
     *    <code>NumberFormat</code> is created - valid types
     *    are:</p>
     *    <ul>
     *       <li>AbstractNumberValidator.STANDARD_FORMAT -to create
     *           <i>standard</i> number formats (the default).</li>
     *       <li>AbstractNumberValidator.CURRENCY_FORMAT -to create
     *           <i>currency</i> number formats.</li>
     *       <li>AbstractNumberValidator.PERCENT_FORMAT -to create
     *           <i>percent</i> number formats (the default).</li>
     *    </ul>
     *
     * @param strict <code>true</code> if strict
     *        <code>Format</code> parsing should be used.
     * @param formatType The <code>NumberFormat</code> type to
     *        create for validation, default is STANDARD_FORMAT.
     */
    public ShortValidatorMin(boolean strict, int formatType) {
        super(strict, formatType, false);
    }

    /**
     * <p>Validate/convert a <code>Short</code> using the default
     *    <code>Locale</code>.
     *
     * @param value The value validation is being performed on.
     * @return The parsed <code>Short</code> if valid or <code>null</code>
     *  if invalid.
     */
    public Short validate(String value) {
        return (Short)parse(value, (String)null, (Locale)null);
    }

    /**
     * <p>Validate/convert a <code>Short</code> using the
     *    specified <i>pattern</i>.
     *
     * @param value The value validation is being performed on.
     * @param pattern The pattern used to validate the value against.
     * @return The parsed <code>Short</code> if valid or <code>null</code> if invalid.
     */
    public Short validate(String value, String pattern) {
        return (Short)parse(value, pattern, (Locale)null);
    }

    /**
     * <p>Validate/convert a <code>Short</code> using the
     *    specified <code>Locale</code>.
     *
     * @param value The value validation is being performed on.
     * @param locale The locale to use for the number format, system default if null.
     * @return The parsed <code>Short</code> if valid or <code>null</code> if invalid.
     */
    public Short validate(String value, Locale locale) {
        return (Short)parse(value, (String)null, locale);
    }

    /**
     * <p>Validate/convert a <code>Short</code> using the
     *    specified pattern and/ or <code>Locale</code>.
     *
     * @param value The value validation is being performed on.
     * @param pattern The pattern used to validate the value against, or the
     *        default for the <code>Locale</code> if <code>null</code>.
     * @param locale The locale to use for the date format, system default if null.
     * @return The parsed <code>Short</code> if valid or <code>null</code> if invalid.
     */
    public Short validate(String value, String pattern, Locale locale) {
        return (Short)parse(value, pattern, locale);
    }


    /**
     * <p>Perform further validation and convert the <code>Number</code> to
     * a <code>Short</code>.</p>
     *
     * @param value The parsed <code>Number</code> object created.
     * @param formatter The Format used to parse the value with.
     * @return The parsed <code>Number</code> converted to a
     *   <code>Short</code> if valid or <code>null</code> if invalid.
     */
    protected Object processParsedValue(Object value, Format formatter) {

        long longValue = ((Number)value).longValue();

        if (longValue < Short.MIN_VALUE ||
            longValue > Short.MAX_VALUE) {
            return null;
        } else {
            return new Short((short)longValue);
        }
    }
}
