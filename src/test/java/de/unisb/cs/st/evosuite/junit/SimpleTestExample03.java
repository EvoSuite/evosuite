package de.unisb.cs.st.evosuite.junit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.junit.Test;

public class SimpleTestExample03 {

	@Test
	public void test() throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("dd.MMM.yyyy", Locale.FRENCH);
		System.out.println(formatter.format(System.currentTimeMillis()));
		String date = "11.sept..2007";
		System.out.println(date);
		Date result = formatter.parse(date);
		// Assert.assertNotNull(result);
	}
}
