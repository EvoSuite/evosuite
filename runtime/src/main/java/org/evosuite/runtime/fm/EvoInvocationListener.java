package org.evosuite.runtime.fm;

import org.mockito.invocation.DescribedInvocation;
import org.mockito.listeners.InvocationListener;
import org.mockito.listeners.MethodInvocationReport;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * During the test generation, we need to know which methods have been called,
 * and how often they were called.
 * This is however not needed in the final generated JUnit tests
 *
 * Created by Andrea Arcuri on 27/07/15.
 */
public class EvoInvocationListener implements InvocationListener {

    private Map<String, MethodDescriptor> map = new LinkedHashMap<>();

    public List<MethodDescriptor> getViewOfMethodDescriptors(){
        return map.values().stream().collect(Collectors.toList());
    }

    @Override
    public void reportInvocation(MethodInvocationReport methodInvocationReport) {

        DescribedInvocation di = methodInvocationReport.getInvocation();
        /*
            Current Mockito API seems quite limited. Here, to know what
            was called, it looks like the only way is to parse the results
            of toString.
            We can identify primitive types and String, but likely not the
            exact type of input objects. This is a problem if methods are overloaded
            and having same number of input parameters :(
         */
        String description = di.toString();

        int openingP = description.indexOf('(');
        assert openingP >= 0;

        String[] leftTokens = description.substring(0,openingP).split("\\.");
        String methodName = leftTokens[leftTokens.length-1];

        int closingP = description.lastIndexOf(')');
        String[] inputTokens = description.substring(openingP+1, closingP).split(",");

        String mockitoMatchers = "";
        if(inputTokens.length > 0) {
            /*
                TODO: For now it does not seem really feasible to infer the correct types.
                Left a feature request on Mockito mailing list, let's see if it ll be done
             */
            mockitoMatchers += "any()";
            for (int i=1; i<inputTokens.length; i++) {
                mockitoMatchers += " , any()";
            }
        }

        MethodDescriptor md = new MethodDescriptor(methodName,mockitoMatchers);
        synchronized (map){
            MethodDescriptor current = map.get(md.getID());
            if(current == null){
                current = md;
            }
            current.increaseCounter();
            map.put(md.getID(),current);
        }
    }
}
