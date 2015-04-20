package com.examples.with.different.packagename.agent;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * Created by arcuri on 12/8/14.
 */
public class GetURL {

    public static URL get(String url) throws MalformedURLException {
        return new URL(url);
    }

    public static URL getFromUri(String uri) throws Exception{
        URI foo = new URI(uri);
        return foo.toURL();
    }
}
