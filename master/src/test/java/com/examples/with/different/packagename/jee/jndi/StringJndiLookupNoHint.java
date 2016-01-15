package com.examples.with.different.packagename.jee.jndi;

import javax.naming.InitialContext;

/**
 * Created by Andrea Arcuri on 06/12/15.
 */
public class StringJndiLookupNoHint {

    public void foo() throws Exception{

        InitialContext ctx = new InitialContext();
        String foo = (String) ctx.lookup("foo/String");

        System.out.println("Got object: "+foo.toString());
    }
}
