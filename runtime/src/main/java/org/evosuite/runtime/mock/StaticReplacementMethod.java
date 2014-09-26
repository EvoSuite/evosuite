package org.evosuite.runtime.mock;

/**
 * This @tag is used for methods in the OverrideMocks that
 * cannot be handled with inheritance.
 * A typical example is "final" methods.
 *
 * <p>
 *     These methods will be handled like they were static
 *     replacement ones: ie, same method name, but static, and
 *     with class instance as first input parameter
 *    </p>
 *
 * Created by arcuri on 9/25/14.
 */
public @interface StaticReplacementMethod {
}
