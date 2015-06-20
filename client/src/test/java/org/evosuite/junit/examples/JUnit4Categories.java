package org.evosuite.junit.examples;

import org.junit.experimental.categories.Categories;
import org.junit.experimental.categories.Categories.IncludeCategory;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Categories.class)
@IncludeCategory(SlowTests.class)
@Suite.SuiteClasses({
  JUnit3Test.class,
  JUnit4Test.class
})
public class JUnit4Categories {
	// Categories is a kind of Suite
}
