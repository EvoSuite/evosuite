package org.evosuite.testcase.fm;

import org.evosuite.TestGenerationContext;
import org.evosuite.runtime.util.Inputs;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;


/**
 * Created by Andrea Arcuri on 27/07/15.
 */
public class MethodDescriptor implements Comparable<MethodDescriptor>, Serializable{

	private static final long serialVersionUID = -6747363265640233704L;

	protected static final Logger logger = LoggerFactory.getLogger(MethodDescriptor.class);

    private final String methodName;
    private final String inputParameterMatchers;
    private final String className;
    /**
     * How often the method was called
     */
    private int counter;

    private transient volatile Method method;

    private transient volatile String id; //derived field


    public MethodDescriptor(Method method){
        Inputs.checkNull(method);
        this.method = method;
        methodName = method.getName();
        className = method.getDeclaringClass().getName();

        String matchers = "";
        Type[] types = method.getParameterTypes();
        for(int i=0; i<types.length; i++){
            if(i > 0){
                matchers += " , ";
            }

            Type type = types[i];
            if(type.equals(Integer.TYPE) || type.equals(Integer.class)){
                matchers += "anyInt()";
            } else if(type.equals(Long.TYPE) || type.equals(Long.class)){
                matchers += "anyLong()";
            }else if(type.equals(Double.TYPE) || type.equals(Double.class)){
                matchers += "anyDouble()";
            }else if(type.equals(Float.TYPE) || type.equals(Float.class)){
                matchers += "anyFloat()";
            }else if(type.equals(Short.TYPE) || type.equals(Short.class)){
                matchers += "anyString()";
            }else if(type.equals(String.class)){
                matchers += "anyString()";
            }else{
                matchers += "any(" + type.getTypeName()+".class)";
            }
        }

        inputParameterMatchers = matchers;
    }

    /**
     * For example, do not mock methods with no return value
     *
     * @return
     */
    public boolean shouldBeMocked(){
        if(method.getReturnType().equals(Void.TYPE)){
            return false;
        }
        return true;
    }

    public MethodDescriptor getCopy(){
        MethodDescriptor copy = new MethodDescriptor(method);
        copy.counter = this.counter;
        return copy;
    }

    public int getNumberOfInputParameters(){
        return getMethod().getParameterCount();
    }

    public Object executeMatcher(int i) throws IllegalArgumentException{
        if(i<0 || i>= getNumberOfInputParameters()){
            throw new IllegalArgumentException("Invalid index: "+i);
        }

        Type[] types = method.getParameterTypes();
        Type type = types[i];

        if(type.equals(Integer.TYPE) || type.equals(Integer.class)){
            return  Mockito.anyInt();
        } else if(type.equals(Long.TYPE) || type.equals(Long.class)){
            return Mockito.anyLong();
        }else if(type.equals(Double.TYPE) || type.equals(Double.class)){
            return Mockito.anyDouble();
        }else if(type.equals(Float.TYPE) || type.equals(Float.class)){
            return Mockito.anyFloat();
        }else if(type.equals(Short.TYPE) || type.equals(Short.class)){
            return Mockito.anyShort();
        }else if(type.equals(String.class)){
            return Mockito.anyString();
        }else{
            return Mockito.any(type.getClass());
        }
    }

    @Deprecated // better (more precise results) to use the other constructor
    public MethodDescriptor(String className, String methodName, String inputParameterMatchers) throws IllegalArgumentException{
        Inputs.checkNull(methodName,inputParameterMatchers);
        this.className = className;
        this.methodName = methodName;
        this.inputParameterMatchers = inputParameterMatchers;
        counter = 0;
    }

    public Method getMethod(){
        /*
         Deprecated code

         if(method == null){


            int nParams = inputParameterMatchers.trim().isEmpty() ? 0 :
                    (inputParameterMatchers.length() - inputParameterMatchers.replace(",", "").length()) + 1;//# of "," + 1

            Class<?> klass = null;
            try {
                klass = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Failed reflection: "+e.getMessage(),e);
            }


            //    TODO: as now, we cannot get the correct method: we just return the first one
            //    matching at least the number of parameters.
            //    However, at least we force it to be deterministic


            List<Method> list = Arrays.asList(klass.getDeclaredMethods()).stream()
                    .filter(m -> m.getName().equals(methodName) && m.getParameterTypes().length==nParams)
                    .sorted().collect(Collectors.toList());
            if(list.size() == 0){
                String msg = "Failed reflection: cannot find method "+methodName+" in "+className+" with "+nParams+" params";
                logger.error(msg);
                throw new RuntimeException(msg);
            }
            if(list.size() > 1){
                //TODO remove once Mockito is extended to get the right method
                logger.warn("Class "+className+" has "+list.size()+" overloaded methods for "+methodName+" with "+
                    nParams+ " parameters: likely Functional Mocking with Mockito will not work properly");
            }

            method = list.get(0);

        }
        */

        assert method != null;
        return method;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getInputParameterMatchers() {
        return inputParameterMatchers;
    }

    public String getID(){
        if(id == null){
            id = className +"."+ getMethodName() + "#" + getInputParameterMatchers();
        }
        return id;
    }

    public int getCounter() {
        return counter;
    }

    public void increaseCounter(){
        counter++;
    }


    @Override
    public int compareTo(MethodDescriptor o) {
        int com = this.className.compareTo(o.className);
        if(com!=0){
            return com;
        }
        com = this.methodName.compareTo(o.methodName);
        if(com!=0){
            return com;
        }
        com = this.inputParameterMatchers.compareTo(o.inputParameterMatchers);
        if(com!=0){
            return com;
        }
        return this.counter - o.counter;
    }


    //for Serialization
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        // Write/save additional fields
        oos.writeObject(method.getDeclaringClass().getName());
        oos.writeObject(method.getName());
        oos.writeObject(org.objectweb.asm.Type.getMethodDescriptor(method));
    }


    //for Serialization
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();

        // Read/initialize additional fields
        Class<?> methodClass = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass((String) ois.readObject());

        String methodName = (String) ois.readObject();
        String methodDesc = (String) ois.readObject();

        for (Method method : methodClass.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                if (org.objectweb.asm.Type.getMethodDescriptor(method).equals(methodDesc)) {
                    this.method = method;
                    return;
                }
            }
        }

        if (this.method==null) {
            throw new IllegalStateException("Unknown method for " + methodName
                    + " in class " + methodClass.getCanonicalName());
        }
    }
}
