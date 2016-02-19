/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.setup;


import java.lang.reflect.Field;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.evosuite.Properties;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class TestAccessField {

	@After
	public void resetProperties() {
		Properties.CLASS_PREFIX = "";
		Properties.TARGET_CLASS = "";
	}

	@Test
	public void testPublicField() {
		Properties.CLASS_PREFIX = "some.package";
		Properties.TARGET_CLASS = "some.package.Foo";
		Field f = FieldUtils.getField(com.examples.with.different.packagename.AccessExamples.class, "publicField");
		boolean result = TestUsageChecker.canUse(f);
		Assert.assertTrue(result);
	}

	@Test
	public void testDefaultField() {
		Properties.CLASS_PREFIX = "some.package";
		Properties.TARGET_CLASS = "some.package.Foo";
		Field f = FieldUtils.getField(com.examples.with.different.packagename.AccessExamples.class, "defaultField", true);
		boolean result = TestUsageChecker.canUse(f);
		Assert.assertFalse(result);
	}

	@Test
	public void testProtectedField() {
		Properties.CLASS_PREFIX = "some.package";
		Properties.TARGET_CLASS = "some.package.Foo";
		Field f = FieldUtils.getField(com.examples.with.different.packagename.AccessExamples.class, "protectedField", true);
		boolean result = TestUsageChecker.canUse(f);
		Assert.assertFalse(result);
	}

	@Test
	public void testPrivateField() {
		Properties.CLASS_PREFIX = "some.package";
		Properties.TARGET_CLASS = "some.package.Foo";
		Field f = FieldUtils.getField(com.examples.with.different.packagename.AccessExamples.class, "privateField", true);
		boolean result = TestUsageChecker.canUse(f);
		Assert.assertFalse(result);
	}

	@Test
	public void testPublicFieldTargetPackage() {
		Properties.CLASS_PREFIX = "com.examples.with.different.packagename";
		Properties.TARGET_CLASS = "com.examples.with.different.packagename.Foo";
		Field f = FieldUtils.getField(com.examples.with.different.packagename.AccessExamples.class, "publicField");
		boolean result = TestUsageChecker.canUse(f);
		Assert.assertTrue(result);
	}

	@Test
	public void testDefaultFieldTargetPackage() {
		Properties.CLASS_PREFIX = "com.examples.with.different.packagename";
		Properties.TARGET_CLASS = "com.examples.with.different.packagename.Foo";
		Field f = FieldUtils.getField(com.examples.with.different.packagename.AccessExamples.class, "defaultField", true);
		boolean result = TestUsageChecker.canUse(f);
		Assert.assertTrue(result);
	}

	@Test
	public void testProtectedFieldTargetPackage() {
		Properties.CLASS_PREFIX = "com.examples.with.different.packagename";
		Properties.TARGET_CLASS = "com.examples.with.different.packagename.Foo";
		Field f = FieldUtils.getField(com.examples.with.different.packagename.AccessExamples.class, "protectedField", true);
		boolean result = TestUsageChecker.canUse(f);
		Assert.assertTrue(result);
	}

	@Test
	public void testPrivateFieldTargetPackage() {
		Properties.CLASS_PREFIX = "com.examples.with.different.packagename";
		Properties.TARGET_CLASS = "com.examples.with.different.packagename.Foo";
		Field f = FieldUtils.getField(com.examples.with.different.packagename.AccessExamples.class, "privateField", true);
		boolean result = TestUsageChecker.canUse(f);
		Assert.assertFalse(result);
	}

}
