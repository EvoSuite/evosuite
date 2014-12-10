package org.evosuite.runtime.mock.java.net;

import org.evosuite.runtime.mock.EvoSuiteMock;
import org.evosuite.runtime.mock.StaticReplacementMethod;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * URI is a final class. It seems there is the
 * need to only mock one of its methods, in which URL is used
 */
public class MockURI implements EvoSuiteMock{

    @StaticReplacementMethod(staticMock = false , mockedClassName = "java.net.URI")
    public static URL toURL(URI uri) throws MalformedURLException {
        if (!uri.isAbsolute())
            throw new IllegalArgumentException("URI is not absolute");
        return MockURL.URL(uri.toString());
    }

}
