package org.evosuite.runtime.testdata;

/**
 * A object wrapper for URLs accessed by the SUT
 *
 * Created by arcuri on 12/12/14.
 */
public class EvoSuiteURL {

    private final String url;

    public EvoSuiteURL(String url) throws IllegalArgumentException{
        this.url = url;

        if(url == null || url.isEmpty()){
            throw new IllegalArgumentException("Undefined url");
        }
    }

    public String getUrl() {
        return url;
    }
}
