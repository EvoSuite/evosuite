package org.evosuite.runtime.instrumentation;

import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.util.ComputeClassWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for the bytecode instrumentation
 * needed for the generated JUnit test cases.
 *
 * <p>
 * Note: the instrumentation will be part of the final JUnit tests and, as such, we should
 * only keep the instrumentation that affect the functional behavior (so, no branch distances, etc).
 *
 * Created by arcuri on 6/11/14.
 */
public class RuntimeInstrumentation {

    private static Logger logger = LoggerFactory.getLogger(RuntimeInstrumentation.class);

    /**
     * If we are re-instrumenting a class, then we cannot change its
     * signature: eg add new methods
     *
	 * TODO: remove once we fix instrumentation
	 */
	private volatile boolean retransformingMode;

	public RuntimeInstrumentation(){
		retransformingMode = false;
	}

	public void setRetransformingMode(boolean on){
		retransformingMode = on;
	}

    public static boolean checkIfCanInstrument(String className) {
        for (String s : RuntimeInstrumentation.getPackagesShouldNotBeInstrumented()) {
            if (className.startsWith(s)) {
                return false;
            }
        }

        if(className.contains("EnhancerByMockito")){
            //very special case, as Mockito will create classes on the fly
            return false;
        }

        return true;
    }


    /**
     * <p>
     * getPackagesShouldNotBeInstrumented
     * </p>
     *
     * @return the names of class packages EvoSuite is not going to instrument
     */
    private static String[] getPackagesShouldNotBeInstrumented() {
        //explicitly blocking client projects such as specmate is only a
        //temporary solution, TODO allow the user to specify
        //packages that should not be instrumented
        return new String[]{"java.", "javax.", "sun.", "org.evosuite", "org.exsyst",
                "de.unisb.cs.st.testcarver", "de.unisb.cs.st.evosuite", "org.uispec4j",
                "de.unisb.cs.st.specmate", "org.xml", "org.w3c",
                "testing.generation.evosuite", "com.yourkit", "com.vladium.emma.", "daikon.",
                "org.netbeans.lib.profiler", // VisualVM profiler
                // Need to have these in here to avoid trouble with UnsatisfiedLinkErrors on Mac OS X and Java/Swing apps
                "apple.", "com.apple.", "com.sun",
                "org.junit", "junit.framework","org.mockito", // do not instrument test code which will be part of final JUnit
                "org.apache.xerces.dom3", "de.unisl.cs.st.bugex",  "org.mozilla.javascript.gen.c",
                "corina.cross.Single",  // I really don't know what is wrong with this class, but we need to exclude it
                "org.slf4j",
                "jdk.internal",
                "dk.brics.automaton", //used in DSE, and we have a class with that package inside EvoSutie
                "org.apache.commons.discovery.tools.DiscoverSingleton",
                "org.apache.commons.discovery.resource.ClassLoaders",
                "org.apache.commons.discovery.resource.classes.DiscoverClasses",
                "org.apache.commons.logging.Log",// Leads to ExceptionInInitializerException when re-instrumenting classes that use a logger
                "org.jcp.xml.dsig.internal.dom.", //Security exception in ExecutionTracer?
                "com_cenqua_clover", "com.cenqua", //these are for Clover code coverage instrumentation
                "net.sourceforge.cobertura", // cobertura code coverage instrumentation
                "javafx.", // JavaFX crashes when instrumented
                "ch.qos.logback", // Instrumentation makes logger events sent to the master un-serialisable
                "major.mutation", // Runtime library Major mutation tool
                "org.apache.lucene.util.SPIClassIterator", "org.apache.lucene.analysis.util.AnalysisSPILoader", "org.apache.lucene.analysis.util.CharFilterFactory",
                "org.apache.struts.util.MessageResources", "org.dom4j.DefaultDocumentFactory", // These classes all cause problems with re-instrumentation
                /**
                 * FIXME:
                 * JavaEE libraries should be shaded, as many side-effects.
                 * We still want them in the blacklist, as to avoid problems when running tests before shading.
                 * If there is need of having any blacklisted library which is also an EvoSuite
                 * dependency, then we MUST modify classloader to always load the version of the SUT
                 * (currently it seems we delegate to parent classloader, which gives EvoSuite's version?)
                 *
                 * Main problem due to libraries used in the generated JUnit files to test JavaEE applications relying on database
                 * */
                "org.hibernate","org.hsqldb","org.jboss",
                 "org.springframework", "org.apache.commons.logging", "javassist.util"

        };
    }

    public byte[] transformBytes(ClassLoader classLoader, String className,
                                 ClassReader reader) {

        String classNameWithDots = className.replace("/", ".");

        if (!checkIfCanInstrument(classNameWithDots)) {
            throw new IllegalArgumentException("Should not transform a shared class ("
                    + classNameWithDots + ")! Load by parent (JVM) classloader.");
        }

        int asmFlags = ClassWriter.COMPUTE_FRAMES;
        ClassWriter writer = new ComputeClassWriter(asmFlags);

        ClassVisitor cv = writer;

        if(RuntimeSettings.resetStaticState && !retransformingMode) {
        		/*
        		 * FIXME: currently reset does add a new method, but that does no work
        		 * when retransformingMode :(
        		 */
            CreateClassResetClassAdapter resetClassAdapter = new CreateClassResetClassAdapter(cv, className);
            resetClassAdapter.setRemoveFinalModifierOnStaticFields(true);
            cv = resetClassAdapter;
        }

        if(RuntimeSettings.isUsingAnyMocking()) {
            cv = new MethodCallReplacementClassAdapter(cv, className, !retransformingMode);
        }

        cv = new KillSwitchClassAdapter(cv);

        if(RuntimeSettings.maxNumberOfIterationsPerLoop >= 0){
        		cv = new LoopCounterClassAdapter(cv);
        }

        ClassNode cn = new AnnotatedClassNode();

        int readFlags = ClassReader.SKIP_FRAMES;
        reader.accept(cn, readFlags);
        

        cv = new JSRInlinerClassVisitor(cv);

        try {
            cn.accept(cv);
        } catch (Throwable ex) {
           logger.error("Error while instrumenting class "+className+": "+ex.getMessage(),ex);
        }

        return writer.toByteArray();
    }

}