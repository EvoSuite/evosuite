package com.examples.with.different.packagename.fm;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IssueWithNumber {

    public static final String RESULT = "foo";

    public static String getResult(){
        Number number = mock(Number.class);
        when(number.toString()).thenReturn(RESULT);
        return number.toString();
    }

}
