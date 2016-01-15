package com.examples.with.different.packagename.jee.jndi;

import javax.naming.InitialContext;

/**
 * Created by Andrea Arcuri on 06/12/15.
 */
public class BeanCastJndiLookupWithHint {

    public void foo() throws Exception{

        InitialContext ctx = new InitialContext();
        ABeanManagedByJNDI foo = (ABeanManagedByJNDI) ctx.lookup("foo/Bar!"+ABeanManagedByJNDI.class.getName());

        System.out.println("Got object: "+foo.toString());
    }
}
