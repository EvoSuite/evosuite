package com.examples.with.different.packagename.jee.jndi;

import javax.naming.InitialContext;

/**
 * Created by Andrea Arcuri on 06/12/15.
 */
public class NoCastJndiLookup {

    public void foo() throws Exception{

        InitialContext ctx = new InitialContext();
        Object foo = ctx.lookup("foo/Bar");

        System.out.println("Got object: "+foo.toString());
    }
}
