package com.examples.with.different.packagename.seeding;

/*
 * Exact copy of class ShortValidator from
 * package org.apache.commons.validator.routines; 
 */


import java.text.Format;
import java.util.Locale;

/**
 * <p><b>Short Validation</b> and Conversion routines (<code>java.lang.Short</code>).</p>
 *
 * <p>This validator provides a number of methods for
 *    validating/converting a <code>String</code> value to
 *    a <code>Short</code> using <code>java.text.NumberFormat</code>
 *    to parse either:</p>
 *    <ul>
 *       <li>using the default format for the default <code>Locale</code></li>
 *       <li>using a specified pattern with the default <code>Locale</code></li>
 *       <li>using the default format for a specified <code>Locale</code></li>
 *       <li>using a specified pattern with a specified <code>Locale</code></li>
 *    </ul>
 *
 * <p>Use one of the <code>isValid()</code> methods to just validate or
 *    one of the <code>validate()</code> methods to validate and receive a
 *    <i>converted</i> <code>Short</code> value.</p>
 *
 * <p>Once a value has been sucessfully converted the following
 *    methods can be used to perform minimum, maximum and range checks:</p>
 *    <ul>
 *       <li><code>minValue()</code> checks whether the value is greater
 *           than or equal to a specified minimum.</li>
 *       <li><code>maxValue()</code> checks whether the value is less
 *           than or equal to a specified maximum.</li>
 *       <li><code>isInRange()</code> checks whether the value is within
 *           a specified range of values.</li>
 *    </ul>
 *
 * <p>So that the same mechanism used for parsing an <i>input</i> value
 *    for validation can be used to format <i>output</i>, corresponding
 *    <code>format()</code> methods are also provided. That is you can
 *    format either:</p>
 *    <ul>
 *       <li>using the default format for the default <code>Locale</code></li>
 *       <li>using a specified pattern with the default <code>Locale</code></li>
 *       <li>using the default format for a specified <code>Locale</code></li>
 *       <li>using a specified pattern with a specified <code>Locale</code></li>
 *    </ul>
 *
 * @version $Revision: 1227719 $ $Date: 2012-01-05 18:45:51 +0100 (Thu, 05 Jan 2012) $
 * @since Validator 1.3.0
 */
public class ShortValidator extends AbstractNumberValidator {

    private static final long serialVersionUID = -5227510699747787066L;

    private static final ShortValidator VALIDATOR = new ShortValidator();

    /**
     * Return a singleton instance of this validator.
     * @return A singleton instance of the ShortValidator.
     */
    public static ShortValidator getInstance() {
        return VALIDATOR;
    }

    /**
     * Construct a <i>strict</i> instance.
     */
    public ShortValidator() {
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
    public ShortValidator(boolean strict, int formatType) {
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
     * Check if the value is within a specified range.
     *
     * @param value The <code>Number</code> value to check.
     * @param min The minimum value of the range.
     * @param max The maximum value of the range.
     * @return <code>true</code> if the value is within the
     *         specified range.
     */
    public boolean isInRange(short value, short min, short max) {
        return (value >= min && value <= max);
    }

    /**
     * Check if the value is within a specified range.
     *
     * @param value The <code>Number</code> value to check.
     * @param min The minimum value of the range.
     * @param max The maximum value of the range.
     * @return <code>true</code> if the value is within the
     *         specified range.
     */
    public boolean isInRange(Short value, short min, short max) {
        return isInRange(value.shortValue(), min, max);
    }

    /**
     * Check if the value is greater than or equal to a minimum.
     *
     * @param value The value validation is being performed on.
     * @param min The minimum value.
     * @return <code>true</code> if the value is greater than
     *         or equal to the minimum.
     */
    public boolean minValue(short value, short min) {
        return (value >= min);
    }

    /**
     * Check if the value is greater than or equal to a minimum.
     *
     * @param value The value validation is being performed on.
     * @param min The minimum value.
     * @return <code>true</code> if the value is greater than
     *         or equal to the minimum.
     */
    public boolean minValue(Short value, short min) {
        return minValue(value.shortValue(), min);
    }

    /**
     * Check if the value is less than or equal to a maximum.
     *
     * @param value The value validation is being performed on.
     * @param max The maximum value.
     * @return <code>true</code> if the value is less than
     *         or equal to the maximum.
     */
    public boolean maxValue(short value, short max) {
        return (value <= max);
    }

    /**
     * Check if the value is less than or equal to a maximum.
     *
     * @param value The value validation is being performed on.
     * @param max The maximum value.
     * @return <code>true</code> if the value is less than
     *         or equal to the maximum.
     */
    public boolean maxValue(Short value, short max) {
        return maxValue(value.shortValue(), max);
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
