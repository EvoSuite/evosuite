package com.examples.with.different.packagename.dse.invokedynamic;

import java.util.function.Function;

/**
 * Simple example of closures as fields.
 *
 * Here the method descriptor of the call made in test will differ from the one
 * used when begging executing the closure (intCompare).
 *
 * See the resulting bytecode for more information.
 *
 * @author Ignacio Lebrero
 */
class ClosureField {
    public Function<Integer, Boolean> intCompare;

    public ClosureField() {
        // Closure lambda.
        Integer y = new Integer(12);
        Integer z = new Integer(22);
        this.intCompare = x -> (x > y && x < z);
    }

    boolean test(int x) {
        return this.intCompare.apply(new Integer(x));
    }
}