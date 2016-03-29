package com.examples.with.different.packagename.exception;

/**
 * Created by gordon on 19/03/2016.
 */
public class CatchWithUnknownThrow {

    char x = 0;

    // Based on com.soops.CEN4010.JMCA.JParser.JavaParserTokenManager
    public int jjStartNfaWithStates_0(ClassThrowingIOException s)
    {
        try { x = s.readChar(); }
        catch(java.io.IOException e) { return 0; }
        return 0;
    }
}
