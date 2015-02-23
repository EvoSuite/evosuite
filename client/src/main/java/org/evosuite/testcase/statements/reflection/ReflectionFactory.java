package org.evosuite.testcase.statements.reflection;

import org.evosuite.testcase.statements.MethodStatement;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by Andrea Arcuri on 22/02/15.
 */
public class ReflectionFactory {

    public boolean hasPrivateFieldsOrMethods(){
        return false; //TODO
    }

    public boolean nextUseField(){
        return true; //TODO based on proportions
    }

    public Field nextField(){
        return null; //TODO
    }

    public Method nextMethod(){
        return null; //TODO
    }

    public Class<?> getReflectedClass(){
        return null; //TODO
    }
}

/*
 public synchronized void addForSUT(){

        if(hasBeenAdded){
            return;
        }
        hasBeenAdded = true;

        Class<?> sut = Properties.getTargetClass();

        for(Method m : sut.getDeclaredMethods()){
            if(!Modifier.isPrivate(m.getModifiers())){
                continue; //only interested in private methods, as the others can be called directly
            }


        }
    }
 */