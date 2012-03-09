package de.unisb.cs.st.evosuite.junit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import de.unisb.cs.st.evosuite.junit.TestExample.MockingBird;

public class SimpleTestExample {

	@Ignore
	@Test
	public void test01() {
		MockingBird bird = new MockingBird("killSelf");
		bird.executeCmd(10);
	}

	@Ignore
	@Test
	public void test02() {
		MockingBird bird = MockingBird.create("killSelf");
		bird.executeCmd(10);
	}

	@Ignore
	@Test
	public void test03() throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("dd.MMM.yyyy", Locale.FRENCH);
		System.out.println(formatter.format(System.currentTimeMillis()));
		String date = "11.sept..2007";
		System.out.println(date);
		Date result = formatter.parse(date);
		System.out.println(result);
//		Assert.assertNotNull(result);
	}

	@Ignore
	@Test
	public void test04() {
		MockingBird bird = new MockingBird(new String("killSelf"));
		bird.doIt(new String("You")).doIt("Me").doIt("Them").doIt("Everybody!");
	}

	@Ignore
	@Test
	public void test05() {
		String input = "killSelf";
		input = "flyAway";
		MockingBird bird = MockingBird.create(input);
		int value = 10;
		bird.executeCmd(value);
	}
}
