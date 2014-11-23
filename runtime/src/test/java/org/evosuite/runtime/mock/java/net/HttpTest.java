package org.evosuite.runtime.mock.java.net;

import org.junit.Assert;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by arcuri on 11/21/14.
 */
public class HttpTest {

    @Test
    public void testUrlParsingHttp() throws MalformedURLException {

        String location = "http://www.evosuite.org/index.html";
        URL url =  MockURL.URL(location);
        Assert.assertEquals("/index.html",url.getFile());
        Assert.assertEquals("http",url.getProtocol());
    }

    @Test
    public void testHttpReading() throws Exception {

        String location = "http://www.evosuite.org/index.html";
        URL url = MockURL.URL(location);
        URLConnection connection = url.openConnection();
        Assert.assertTrue(connection instanceof HttpURLConnection);

        EvoHttpURLConnection evo = (EvoHttpURLConnection) connection;
        evo.connect();

        //TODO before read, create actual content

    }
}
