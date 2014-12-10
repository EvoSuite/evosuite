package org.evosuite.runtime.mock;

import java.lang.annotation.*;

/**
 * This @tag is used for methods in the OverrideMocks that
 * cannot be handled with inheritance.
 * A typical example is "final" methods.
 *
 * <p>
 *     These methods will be handled like they were static
 *     replacement ones: ie, same method name, but static, and
 *     with class instance as first input parameter
 *
 * <p>
 * Note: such tag can also be used in unspecialized EvoSuiteMocks,
 * eg when one wants to mock one single method instead of using
 * a full-blown StaticReplacementMock.
 * So, what would be the point of having StaticReplacementMock then?
 * We still want to be sure that we mocked all methods (eg  to check
 * if by error we missed one, or new Java version introduces new
 * ones). However, in special cases StaticReplacementMethod can
 * be useful (ie, StaticReplacementMethod should be used with parsimony).
 *
 * <p>
 * WARNING: should only be used on mocking _static_ methods
 *
 * Created by arcuri on 9/25/14.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)  //TODO is there a way to specify only static ones?
public @interface StaticReplacementMethod {

    /**
     * @return whether this static method is mocking a static one
     */
    boolean staticMock() default false;


    /**
     *
     * @return the name of the mocked class. Note: no needed to be
     * specified in a {@link org.evosuite.runtime.mock.OverrideMock},
     * as it would be implicit
     */
    String mockedClassName();
}
