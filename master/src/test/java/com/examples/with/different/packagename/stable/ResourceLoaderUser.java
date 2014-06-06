package com.examples.with.different.packagename.stable;

import java.net.URL;

public class ResourceLoaderUser {
	
	private URL url;

	public ResourceLoaderUser() {
		String packagename = ResourceLoaderUser.class.getPackage().getName()
				.replace('.', '/');
		
		url = ResourceLoaderUser.class.getClassLoader().getResource(
				packagename);
	}
	
	public String urlToString() {
		return String.valueOf(url);
	}
	
	public URL getURL() {
		return url;
	}

}
