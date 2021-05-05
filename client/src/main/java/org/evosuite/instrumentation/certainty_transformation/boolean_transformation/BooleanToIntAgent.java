package org.evosuite.instrumentation.certainty_transformation.boolean_transformation;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Arrays;
import java.util.List;

public class BooleanToIntAgent {

    public static void premain(String agentArgument, Instrumentation instrumentation){
        BooleanToIntTransformer transformer;
        if(agentArgument != null && agentArgument.length() > 0) {
            System.out.println(agentArgument);
            String[] s = agentArgument.split(" ");
            List<String> args = Arrays.asList(s);
            transformer = new BooleanToIntTransformer(args, false, null, false,true);
        } else {
            transformer = new BooleanToIntTransformer();
        }
        instrumentation.addTransformer(transformer, true);
        for (Class<?> loadedClass : instrumentation.getAllLoadedClasses()) {
            if(transformer.classIsInstrumented(loadedClass.getCanonicalName())) {
                try {
                    instrumentation.retransformClasses(loadedClass);
                } catch (UnmodifiableClassException e) {
                    throw new IllegalStateException("Unmodifiable class " + loadedClass.getCanonicalName() +
                            "that should be transformed is loaded before agent is" +
                            " attached", e);
                }
            }
        }
    }
}
