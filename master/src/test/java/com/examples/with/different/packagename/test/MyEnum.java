package com.examples.with.different.packagename.test;

public enum MyEnum {
    /** Equality relationship. */
    EQ("="),

    /** Lesser than or equal relationship. */
    LEQ("<="),

    /** Greater than or equal relationship. */
    GEQ(">=");

    /** Display string for the relationship. */
    private String stringValue;

    /** Simple constructor.
     * @param stringValue display string for the relationship
     */
    private MyEnum(String stringValue) {
        this.stringValue = stringValue;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return stringValue;
    }
  
}
