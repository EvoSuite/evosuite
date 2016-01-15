package org.evosuite.runtime.mock.javax.naming;

import com.sun.naming.internal.ResourceManager;
import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.mock.OverrideMock;

import javax.naming.*;
import java.util.Hashtable;



/**
 * Created by Andrea Arcuri on 02/12/15.
 */
public class MockInitialContext extends javax.naming.InitialContext implements OverrideMock {


    //-------- constructors  ----------------------------

    public MockInitialContext() throws NamingException {
        super();
    }

    public MockInitialContext(Hashtable<?,?> environment) throws NamingException {
        super(environment);
    }


    //-------------- static methods ------------------------------

    public static <T> T doLookup(Name name) throws NamingException {
        return (T) (new MockInitialContext()).lookup(name);
    }

    public static <T> T doLookup(String name) throws NamingException {
        return (T) (new MockInitialContext()).lookup(name);
    }


    // --------------------------------

    @Override
    protected Context getDefaultInitCtx() throws NamingException{
        if(! MockFramework.isEnabled()) {
            return super.getDefaultInitCtx();
        }
        return EvoNamingContext.getInstance();
    }

    @Override
    protected Context getURLOrDefaultInitCtx(String name) throws NamingException {
        if(! MockFramework.isEnabled()) {
            return super.getURLOrDefaultInitCtx(name);
        }
        return EvoNamingContext.getInstance();
    }

    @Override
    protected Context getURLOrDefaultInitCtx(Name name)  throws NamingException {
        if(! MockFramework.isEnabled()) {
            return super.getURLOrDefaultInitCtx(name);
        }
        return EvoNamingContext.getInstance();
    }


    // ---- TODO -----
    @Override
    public void close() throws NamingException {
        super.close();
    }


    @Override
    public String getNameInNamespace() throws NamingException {
        return super.getNameInNamespace();
    }

    //--------------- overwritten methods using Context -------------------------


    @Override
    public Object lookup(String name) throws NamingException {
        return getURLOrDefaultInitCtx(name).lookup(name);
    }

    @Override
    public Object lookup(Name name) throws NamingException {
        return getURLOrDefaultInitCtx(name).lookup(name);
    }

    @Override
    public Object lookupLink(String name) throws NamingException  {
        return getURLOrDefaultInitCtx(name).lookupLink(name);
    }

    @Override
    public Object lookupLink(Name name) throws NamingException {
        return getURLOrDefaultInitCtx(name).lookupLink(name);
    }

    @Override
    public void bind(String name, Object obj) throws NamingException {
        getURLOrDefaultInitCtx(name).bind(name, obj);
    }

    @Override
    public void bind(Name name, Object obj) throws NamingException {
        getURLOrDefaultInitCtx(name).bind(name, obj);
    }

    @Override
    public void rebind(String name, Object obj) throws NamingException {
        getURLOrDefaultInitCtx(name).rebind(name, obj);
    }

    @Override
    public void rebind(Name name, Object obj) throws NamingException {
        getURLOrDefaultInitCtx(name).rebind(name, obj);
    }

    @Override
    public void unbind(String name) throws NamingException  {
        getURLOrDefaultInitCtx(name).unbind(name);
    }

    @Override
    public void unbind(Name name) throws NamingException  {
        getURLOrDefaultInitCtx(name).unbind(name);
    }

    @Override
    public void rename(String oldName, String newName) throws NamingException {
        getURLOrDefaultInitCtx(oldName).rename(oldName, newName);
    }

    @Override
    public void rename(Name oldName, Name newName) throws NamingException{
        getURLOrDefaultInitCtx(oldName).rename(oldName, newName);
    }

    @Override
    public NamingEnumeration<NameClassPair> list(String name)throws NamingException{
        return (getURLOrDefaultInitCtx(name).list(name));
    }

    @Override
    public NamingEnumeration<NameClassPair> list(Name name)throws NamingException {
        return (getURLOrDefaultInitCtx(name).list(name));
    }

    @Override
    public NamingEnumeration<Binding> listBindings(String name)throws NamingException  {
        return getURLOrDefaultInitCtx(name).listBindings(name);
    }

    @Override
    public NamingEnumeration<Binding> listBindings(Name name)throws NamingException  {
        return getURLOrDefaultInitCtx(name).listBindings(name);
    }


    @Override
    public void destroySubcontext(String name) throws NamingException  {
        getURLOrDefaultInitCtx(name).destroySubcontext(name);
    }

    @Override
    public void destroySubcontext(Name name) throws NamingException  {
        getURLOrDefaultInitCtx(name).destroySubcontext(name);
    }

    @Override
    public Context createSubcontext(String name) throws NamingException  {
        return getURLOrDefaultInitCtx(name).createSubcontext(name);
    }

    @Override
    public Context createSubcontext(Name name) throws NamingException  {
        return getURLOrDefaultInitCtx(name).createSubcontext(name);
    }



    @Override
    public NameParser getNameParser(String name) throws NamingException {
        return getURLOrDefaultInitCtx(name).getNameParser(name);
    }

    @Override
    public NameParser getNameParser(Name name) throws NamingException {
        return getURLOrDefaultInitCtx(name).getNameParser(name);
    }

    @Override
    public String composeName(String name, String prefix) throws NamingException {
        return super.composeName(name, prefix);
    }

    @Override
    public Name composeName(Name name, Name prefix)throws NamingException{
        return super.composeName(name, prefix);
    }

    @Override
    public Object addToEnvironment(String propName, Object propVal)throws NamingException {
        myProps.put(propName, propVal);
        return getDefaultInitCtx().addToEnvironment(propName, propVal);
    }

    @Override
    public Object removeFromEnvironment(String propName) throws NamingException {
        myProps.remove(propName);
        return getDefaultInitCtx().removeFromEnvironment(propName);
    }

    @Override
    public Hashtable<?,?> getEnvironment() throws NamingException {
        return getDefaultInitCtx().getEnvironment();
    }



}
