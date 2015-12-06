package com.examples.with.different.packagename.jee.jndi;

import javax.naming.InitialContext;

/**
 * Created by Andrea Arcuri on 06/12/15.
 */
public class BeanCastJndiLookupNoHint {

    public void foo() throws Exception{

        InitialContext ctx = new InitialContext();
        ABeanManagedByJNDI foo = (ABeanManagedByJNDI) ctx.lookup("foo/ABeanManagedByJNDI");

        System.out.println("Got object: "+foo.toString());
    }
}
