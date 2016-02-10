package org.evosuite.setup;

import com.examples.with.different.packagename.otherpackage.ExampleWithInnerClass;
import com.examples.with.different.packagename.otherpackage.ExampleWithStaticPackagePrivateInnerClass;
import org.evosuite.Properties;
import org.junit.Assert;
import org.junit.Test;


/**
 * Created by gordon on 31/01/2016.
 */
public class TestAccessClass {

    @Test
    public void testPublicClass() {
        Properties.CLASS_PREFIX = "some.package";
        Properties.TARGET_CLASS = "some.package.Foo";
        boolean result = TestUsageChecker.canUse(ExampleWithStaticPackagePrivateInnerClass.class);
        Assert.assertTrue(result);
    }

    @Test
    public void testPublicInnerClass() {
        Properties.CLASS_PREFIX = "some.package";
        Properties.TARGET_CLASS = "some.package.Foo";
        boolean result = TestUsageChecker.canUse(ExampleWithInnerClass.Foo.class);
        Assert.assertTrue(result);
    }

    @Test
    public void testDefaultInnerClass() throws ClassNotFoundException {
        Properties.CLASS_PREFIX = "some.package";
        Properties.TARGET_CLASS = "some.package.Foo";
        Class<?> clazz = Class.forName("com.examples.with.different.packagename.otherpackage.ExampleWithStaticPackagePrivateInnerClass$Foo");
        boolean result = TestUsageChecker.canUse(clazz);
        Assert.assertFalse(result);
    }

    @Test
    public void testDefaultInnerClassInSamePackage() throws ClassNotFoundException {
        Properties.CLASS_PREFIX = "com.examples.with.different.packagename.otherpackage";
        Properties.TARGET_CLASS = "com.examples.with.different.packagename.otherpackage.ExampleWithStaticPackagePrivateInnerClass";
        Class<?> clazz = Class.forName("com.examples.with.different.packagename.otherpackage.ExampleWithStaticPackagePrivateInnerClass$Foo");
        boolean result = TestUsageChecker.canUse(clazz);
        Assert.assertTrue(result);
    }
}
