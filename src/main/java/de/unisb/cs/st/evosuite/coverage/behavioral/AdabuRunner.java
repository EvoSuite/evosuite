package de.unisb.cs.st.evosuite.coverage.behavioral;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import de.unisaarland.cs.st.adabu.core.deep.miner.TypeSetDeepTransitiveModelMiner;
import de.unisaarland.cs.st.adabu.trans.abstraction.MissingFieldsAdder;
import de.unisaarland.cs.st.adabu.trans.abstraction.NeverChangingFieldsRemover;
import de.unisaarland.cs.st.adabu.trans.abstraction.typestate.TypeStateStateAbstracter;
import de.unisaarland.cs.st.adabu.trans.model.MethodInvocationSet;
import de.unisaarland.cs.st.adabu.trans.model.TransitiveObjectModel;
import de.unisaarland.cs.st.adabu.trans.model.TransitiveObjectState;
import de.unisaarland.cs.st.adabu.util.datastructures.Graph;
import de.unisaarland.cs.st.adabu.util.datastructures.Graph.EdgeDataMerger;
import de.unisaarland.cs.st.adabu.util.datastructures.GraphNode;
import de.unisaarland.cs.st.adabu.util.source.ClassPathClassFileSource;
import de.unisb.cs.st.evosuite.EvoSuite;
import de.unisb.cs.st.evosuite.Properties;

/** Class that provides methods to run <tt>ADABU</tt> - only default run configuration so far. */
public class AdabuRunner {
	
	/** The default name of the trace output file. */
	public static final String TRACE_NAME = "TmpTrace.out";
	
	/** The relative directory to the tracer jar file. */
	public static final String ADABU_TRACER = "lib" + File.separator + "adabu-tracer.jar";
	
	/** The relative directory to the j-unit jar file. */
	public static final String JUNIT = "lib" + File.separator + "junit.jar";
	
	/** The sources files of the class-path classes. */
	private ClassPathClassFileSource source;
	
	/**
	 * Creates a new <tt>AdabuRunner</tt> with default
	 * configuration.
	 */
	public AdabuRunner() {
		try {
			source = new ClassPathClassFileSource(Properties.CP);
		} catch (IOException e) {
			System.out.println("Error occurred reading the target package: " + e.getMessage());
		}
	}
	
	/**
	 * Mines the object behavior model for the class under test
	 * by running <tt>ADABU</tt> - {@link #runAdabu()}.</br>
	 * The method merges the mined object behavior model and the
	 * object model given as parameter or only returns the mined
	 * object model if <tt>null</tt> is given.</p>
	 * 
	 * @param oldObjectModel - the object model to merge with.
	 * 
	 * @return the mined object behavior model.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public TransitiveObjectModel mineObjectModel(TransitiveObjectModel oldObjectModel) {
		// generate the new object models using ADABU
		List<TransitiveObjectModel> models = runAdabu();
		
		// merge the new models
		if (!models.isEmpty()) {
			// get all model graphs to merge
			Vector<Graph> graphs = new Vector<Graph>();
			for (TransitiveObjectModel model : models) {
				graphs.add(model.getModel());
			}
			// in first iteration old model is null
			if (oldObjectModel == null) {
				oldObjectModel = models.get(0);
			} else {
				graphs.add(oldObjectModel.getModel());
			}
			
			// create a new model with the merged graph
			String className = oldObjectModel.getClassName();
			Integer objectId = oldObjectModel.getObjectId();
			oldObjectModel = new TransitiveObjectModel(className, objectId, Graph.mergeGraphs(graphs, new EdgeDataMerger()));
			
			// update starting and current state
			Vector<GraphNode<TransitiveObjectState,MethodInvocationSet>> nodes = oldObjectModel.getModel().getNodes();
			if (!nodes.isEmpty()) {
				oldObjectModel.setStartingState(nodes.firstElement().getData());
				oldObjectModel.setCurrentState(nodes.lastElement().getData());
			}
		}
		return oldObjectModel;
	}
	
	/**
	 * Runs <tt>ADABU</tt>.
	 * 
	 * <p>First the tracer is launched with default test given
	 * by {@link ModelExplorer#TEST_NAME}.</p>
	 * 
	 * <p>Then the trace is read and for each instantiated object
	 * of the class under test given by {@link Properties#TARGET_CLASS}
	 * an object behavior model is mined.</p>
	 * 
	 * @return the list of mined object behavior models.
	 */
	public List<TransitiveObjectModel> runAdabu() {
		// generate the trace of the test-run using ADABU
		File traceFile = new File(Properties.OUTPUT_DIR + File.separator + TRACE_NAME);
		
		// create the class-path
		String userDir = System.getProperty("user.dir");
		String cp = Properties.CP; // target class directory
		cp += File.pathSeparator + userDir + File.separator + JUNIT; // j-unit jar directory
		cp += File.pathSeparator + userDir + File.separator + Properties.OUTPUT_DIR; // test class directory
		
		List<String> command = new ArrayList<String>();
		command.add(EvoSuite.JAVA_CMD);
		command.add("-cp"); // add class-path of target classes and test-suite
		command.add(cp);
		command.add("-Xmx1000M");
		command.add("-Xbootclasspath/a:" + ADABU_TRACER); // add tracer jar to boot-class-path
		command.add("-Dadabu.resultfilename=" + traceFile.getPath()); // add result file
		command.add("-javaagent:" + ADABU_TRACER); // start java-agent
		command.add(Properties.PROJECT_PREFIX + "." + ModelExplorer.TEST_NAME); // add test class name
		
		// launch the tracer
		try {
			ProcessBuilder builder = new ProcessBuilder(command);
			builder.directory(new File(userDir));
			Process process = builder.start();
			process.waitFor(); // wait until process has terminated
			
			// check whether an error occurred
			if (process.getErrorStream().available() != 0) {
				 BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				 StringBuffer msg = new StringBuffer();
				 String line = reader.readLine();
				 while (line != null) {
					 msg.append(line + "\n");
					 line = reader.readLine();
				 }
				 System.out.println("Error occurred running the tracer: " + msg.toString());
			}
		} catch (IOException e) {
			System.out.println("Error occurred tracing '" + ModelExplorer.TEST_NAME + "': " + e.getMessage());
		} catch (InterruptedException e) {
			System.out.println("* Warning: Tracer process - interrupted");
		}
		
		// mine the models reading the trace using ADABU
		Set<String> classNameSet = new HashSet<String>();
		// need package name separator here, i.e. '/'
		classNameSet.add(Properties.TARGET_CLASS.replace('.', '/'));
		TypeSetDeepTransitiveModelMiner miner =
				new TypeSetDeepTransitiveModelMiner(traceFile, 10, null, classNameSet);
		
		// read the trace
		try {
			miner.readTrace();
		} catch (IOException e) {
			System.out.println("Error occurred reading '" + TRACE_NAME + "': " + e.getMessage());
		}
		
		List<TransitiveObjectModel> models = miner.getModels();
		if (models.isEmpty())
			System.out.println("* Error: Could not mine the object models from the trace");
		return models;
	}
	
	/**
	 * Creates a new abstract object behavior model from the given model
	 * where state abstraction was applied.</br>
	 * State abstraction maps the concrete values of the object state
	 * to small finite domains.
	 * <ul>
	 * <li>A concrete numeric value <tt>x</tt> is mapped to one of the
	 * three abstract domains x &lt; 0, x = 0 or x &gt; 0.</li>
	 * <li>A boolean value is mapped to <tt>true</tt> or <tt>false</tt>.</li>
	 * <li>An object reference <tt>x</tt> is mapped to <tt>null</tt> or
	 * <tt>x instance_of c</tt>, where <tt>c</tt> is the class of the object <tt>x</tt>.</li>
	 * <li>Enumeration values are mapped to each single value as abstract domain.</li>
	 * </ul>
	 * So abstract states are vectors &lt;x <sub>1</sub>, ... , x <sub>n</sub>&gt;
	 * of abstract values x <sub>i</sub> of object fields.
	 * 
	 * <p><b>Note:</b> This method does <b>not</b> modify the object behavior model
	 * given as parameter.</p>
	 * 
	 * @param objectModel - the object model to abstract.
	 * 
	 * @return the abstract object behavior model.
	 */
	public TransitiveObjectModel getAbstractModel(TransitiveObjectModel objectModel) {
		return objectModel; // TODO abstraction
//		TransitiveObjectModel abstractModel;
//		MissingFieldsAdder adder = new MissingFieldsAdder(objectModel);
//		abstractModel = adder.createAbstractModel();
//		NeverChangingFieldsRemover remover = new NeverChangingFieldsRemover(abstractModel);
//		abstractModel = remover.createAbstractModel();
//		TypeStateStateAbstracter abstracter = new TypeStateStateAbstracter(abstractModel, source);
//		abstractModel = abstracter.createAbstractModel();
//		return abstractModel;
	}
}
