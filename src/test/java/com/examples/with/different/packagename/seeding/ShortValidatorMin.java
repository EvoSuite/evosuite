package com.examples.with.different.packagename.seeding;

/*
 * Reduced version of ShortValidator class
 * Trivial methods removed
 */


import java.text.Format;
import java.util.Locale;

public class ShortValidatorMin  {

    private static final ShortValidatorMin VALIDATOR = new ShortValidatorMin();

    
    /**
     * Return a singleton instance of this validator.
     * @return A singleton instance of the ShortValidator.
     */
    public static ShortValidatorMin getInstance() {
        return VALIDATOR;
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
