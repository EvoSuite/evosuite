package org.evosuite.setup.dependencies;

import com.thoughtworks.xstream.XStream;
import org.evosuite.ClientProcess;
import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.classpath.ResourceList;
import org.evosuite.graphs.ddg.DataDependenceGraph;
import org.evosuite.utils.LoggingUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


// Copied and pasted from InheritanceTreeGenerator
public class DataDependenceGraphGenerator {
    private static Logger logger = LoggerFactory.getLogger(DataDependenceGraphGenerator.class);

    private static Logger evologger() {
        return LoggingUtils.getEvoLogger();
    }

    /**
     * Generates the method dependence path for the classes on the given class path.
     *
     * @param classPath the class path to consider
     * @return method dependence graph
     */
    public static DataDependenceGraph createFromClassPath(List<String> classPath) {
        // If the method dependence graph has already been constructed by the master process,
        // then try to reuse it.

        if (!Properties.INSTRUMENT_CONTEXT && !Properties.DEPENDENCY_FILE.isEmpty()) {
            try {
                DataDependenceGraph graph = readDataDependenceGraph(Properties.DEPENDENCY_FILE);
                evologger().info("* " + ClientProcess.getPrettyPrintIdentifier() +
                        "Method dependence graph loaded from {}", Properties.DEPENDENCY_FILE);
                return graph;

            } catch (IOException e) {
                evologger().warn("* " + ClientProcess.getPrettyPrintIdentifier() +
                        "Error loading method dependence graph: {}", e);
            }
        }

        evologger().info("* Creating method dependence graph");

        DataDependenceGraph graph = new DataDependenceGraph();

        logger.debug("CP: {}", classPath);
        for (String classPathEntry : classPath) {
            logger.debug("Looking at CP entry: {}", classPathEntry);
            if (classPathEntry.isEmpty())
                continue;

            if (classPathEntry.matches(".*evosuite-.*\\.jar"))
                continue;

            logger.debug("Analyzing classpath entry {}", classPathEntry);
            for(String className : ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getAllClasses(classPathEntry, "", true, false)) {
                final InputStream classAsStream = ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getClassAsStream(className);
                analyzeClassStream(graph, className, classAsStream);
            }

            // analyze(graph, classPathEntry);
        }

        return graph;
    }

    public static void writeDependenceGraph(DataDependenceGraph graph, File outputFile) throws IOException {
        XStream xstream = new XStream();
        XStream.setupDefaultSecurity(xstream);
        xstream.allowTypesByWildcard(new String[]{"org.evosuite.**", "org.jgrapht.**"});
        try (GZIPOutputStream output = new GZIPOutputStream(new FileOutputStream(outputFile))) {
            xstream.toXML(graph, output);
        }
    }

    public static DataDependenceGraph readDataDependenceGraph(String fileName) throws IOException {
        XStream xstream = new XStream();
        XStream.setupDefaultSecurity(xstream);
        xstream.allowTypesByWildcard(new String[] {"org.evosuite.**", "org.jgrapht.**"});
        GZIPInputStream graph = new GZIPInputStream(new FileInputStream(new File(fileName)));
        return (DataDependenceGraph) xstream.fromXML(graph);
    }

    private static void analyzeClassStream(DataDependenceGraph graph, String className, InputStream inputStream) {
        try {
            ClassReader reader = new ClassReader(inputStream);
            inputStream.close();
            ClassVisitor cv = new DependencyAnalysisClassVisitor(null, className, graph);
            reader.accept(cv, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
        } catch (IOException e) {
            logger.error(e.getMessage());
        } catch(java.lang.ArrayIndexOutOfBoundsException e) {
            logger.error("ASM Error while reading class ("+e.getMessage()+")");
        }
    }
}