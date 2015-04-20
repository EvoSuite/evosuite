package com.examples.with.different.packagename;

import java.util.Locale;

public class ReturnALocale {

	public Locale getLocale(int x) {
		if(x > 0)
			return Locale.ENGLISH;
		else
			return Locale.ITALIAN;
	}
	
	public Locale[] getMoreLocales() {
		return new Locale[] { Locale.CANADA, Locale.CHINA };
	}
}
