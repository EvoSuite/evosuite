/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.runtime.mock.javax.naming;

import org.evosuite.runtime.annotation.EvoSuiteClassExclude;
import org.evosuite.runtime.annotation.EvoSuiteInclude;
import org.evosuite.runtime.javaee.TestDataJavaEE;
import org.evosuite.runtime.testdata.EvoName;

import javax.naming.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Defining a "Context" for EvoSuite where beans/objects can be stored
 * and looked up through JNDI
 *
 * Created by Andrea Arcuri on 06/12/15.
 */
@EvoSuiteClassExclude
public class EvoNamingContext implements Context{

    private static final EvoNamingContext singleton = new EvoNamingContext();


    private final Map<String, Binding> bindings = new ConcurrentHashMap<>();

    @EvoSuiteInclude
    public static EvoNamingContext getInstance(){
        return singleton;
    }

    public void reset(){
        bindings.clear();
    }

    // ---- ES test methods  -------------

    @EvoSuiteInclude
    public static void add(EvoName name, Object obj){
        try {
            getInstance().bind(name.getName(), obj);
        } catch (NamingException e) {
            throw new RuntimeException("Invaliding binding: "+e.toString());
        }
    }
    //------ private -------------

    private String getClassName(String name){
        String[] tokens = name.split("/");
        String cn = tokens[tokens.length-1]; //take last
        return cn;
    }

    private void checkNaming(String name, Object obj) throws NamingException {
        if(obj == null){
            return; //null is OK
        }

        String cn = getClassName(name);
        if(! cn.contains("!")){
            return;
        }

        String[] tokens = cn.split("!");
        if(tokens.length != 2){
            throw new NamingException("Invalid <name>!<type>: "+name);
        }
        String type = tokens[1];

        Class<?> klass;
        try {
            ClassLoader loader = obj.getClass().getClassLoader();
            if(loader == null){
                //this can happen for example for String
                loader = this.getClass().getClassLoader();
            }
            klass = loader.loadClass(type);
        } catch (ClassNotFoundException e) {
            throw new NamingException("Cannot load type "+type+": "+e.toString());
        }

        if(! klass.isAssignableFrom(obj.getClass())){
            throw new NamingException("Invaliding binding of class "+obj.getClass().getName() + " for name "+name);
        }
    }

    //----- override -------------

    @Override
    public Object lookup(Name name) throws NamingException {
        if(name==null){
            throw new NamingException("Null name");
        }
        return lookup(name.toString());
    }

    @Override
    public Object lookup(String name) throws NamingException {
        if(name==null){
            throw new NamingException("Null name");
        }

        /*
            We need to keep track of what the SUT has tried to look up,
            so we can generate in the next generations
         */
        TestDataJavaEE.getInstance().accessLookUpContextName(name);

        Binding b =  bindings.get(name);
        if(b == null){
            return null;
        } else {
            return b.getObject();
        }
    }

    @Override
    public Object lookupLink(String name) throws NamingException {
        return lookup(name); //TODO
    }

    @Override
    public Object lookupLink(Name name) throws NamingException {
        return lookup(name); //TODO
    }

    @Override
    public void bind(Name name, Object obj) throws NamingException {
        if(name==null){
            throw new NamingException("Null name");
        }
        bind(name.toString(), obj);
    }

    @Override
    public void bind(String name, Object obj) throws NamingException {
        if(name==null){
            throw new NamingException("Null name");
        }

        if(bindings.containsKey(name)){
            throw new NameAlreadyBoundException("Already bounded object for: "+name);
        }

        checkNaming(name, obj);
        Binding b = new Binding(name, obj);
        bindings.put(name, b);
    }


    @Override
    public void rebind(Name name, Object obj) throws NamingException {
        if(name==null){
            throw new NamingException("Null name");
        }
        rebind(name.toString(), obj);
    }

    @Override
    public void rebind(String name, Object obj) throws NamingException {
        if(name==null){
            throw new NamingException("Null name");
        }

        checkNaming(name, obj);
        Binding b = new Binding(name, obj);
        bindings.put(name, b);
    }

    @Override
    public void unbind(Name name) throws NamingException {
        if(name==null){
            throw new NamingException("Null name");
        }
        unbind(name.toString());
    }

    @Override
    public void unbind(String name) throws NamingException {
        bindings.remove(name);
    }

    @Override
    public void rename(Name oldName, Name newName) throws NamingException {
        if(oldName==null || newName == null){
            throw new NamingException("Null name");
        }
        rename(oldName.toString(), newName.toString());
    }

    @Override
    public void rename(String oldName, String newName) throws NamingException {
        if(oldName==null || newName == null){
            throw new NamingException("Null name");
        }

        if(bindings.containsKey(newName)){
            throw new NameAlreadyBoundException("Already bounded object for: "+newName);
        }
        if(! bindings.containsKey(oldName)){
            throw new NamingException("No "+oldName+" is bounded");
        }
        Binding r = bindings.remove(oldName);
        Binding n = new Binding(newName, r.getObject());
        bindings.put(newName, n);
    }


    //TODO --------------

    @Override
    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        return null;
    }

    @Override
    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        return null;
    }

    @Override
    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        return null;
    }

    @Override
    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        return null;
    }
    @Override
    public void destroySubcontext(Name name) throws NamingException {

    }

    @Override
    public void destroySubcontext(String name) throws NamingException {

    }

    @Override
    public Context createSubcontext(Name name) throws NamingException {
        return null;
    }

    @Override
    public Context createSubcontext(String name) throws NamingException {
        return null;
    }



    @Override
    public NameParser getNameParser(Name name) throws NamingException {
        return null;
    }

    @Override
    public NameParser getNameParser(String name) throws NamingException {
        return null;
    }

    @Override
    public Name composeName(Name name, Name prefix) throws NamingException {
        return null;
    }

    @Override
    public String composeName(String name, String prefix) throws NamingException {
        return null;
    }

    @Override
    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        return null;
    }

    @Override
    public Object removeFromEnvironment(String propName) throws NamingException {
        return null;
    }

    @Override
    public Hashtable<?, ?> getEnvironment() throws NamingException {
        return null;
    }

    @Override
    public void close() throws NamingException {

    }

    @Override
    public String getNameInNamespace() throws NamingException {
        return null;
    }
}
