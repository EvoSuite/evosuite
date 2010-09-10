/**
 * 
 */
package de.unisb.cs.st.evosuite.OUM;

import java.io.File;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.softevo.jadet.sca.EventPair;
import org.softevo.jadet.sca.Method;
import org.softevo.jadet.sca.Pattern;
import org.softevo.jadet.sca.PatternsList;
import org.softevo.jadet.sca.SCAAbstractor;
import org.softevo.oumextractor.analysis.Analyzer;
import org.softevo.oumextractor.modelcreator1.ModelAnalyzer;
import org.softevo.oumextractor.modelcreator1.ModelData;
import org.softevo.oumextractor.modelcreator1.ModelVisitor;
import org.softevo.oumextractor.modelcreator1.model.InvokeMethodTransition;
import org.softevo.oumextractor.modelcreator1.model.Model;
import org.softevo.oumextractor.modelcreator1.model.ReturnValueOfMethodTransition;
import org.softevo.oumextractor.modelcreator1.model.Transition;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.testcase.Statement;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCluster;
import de.unisb.cs.st.evosuite.testcase.TestFactory;
import de.unisb.cs.st.ga.ConstructionFailedException;
import de.unisb.cs.st.ga.Randomness;

/**
 * @author Gordon Fraser
 *
 */
public class OUMTestFactory  {
	
	private static Logger logger = Logger.getLogger(OUMTestFactory.class);
	
	private static OUMTestFactory instance = null;
	
	private Randomness randomness = Randomness.getInstance();
	
	private TestFactory test_factory = TestFactory.getInstance();
	
	/** Accessor to the test cluster */
	private TestCluster test_cluster = TestCluster.getInstance();

	private PatternsList patterns = null;
	
	/** Map class name to set of OUMs */
	private Map<String, Set< List<Transition > > > models; 
	
	private Map<AccessibleObject, Set<Pattern> > pattern_cache;
	
	/** Map methods to OUMs */
	private Map<AccessibleObject, Set<EventPair> > model_cache;
	
	private OUMTestFactory() {
		readOUMs(Properties.getProperty("OUM.file"));
	}
	
	public static OUMTestFactory getInstance() {
		if(instance == null) {
			instance = new OUMTestFactory();
		}
		return instance;
	}
	
	private void readOUMs(String filename) {
		//patterns = PatternsList.readFromXML(new File("test.xml"));
		ModelAnalyzer analyzer = new ModelAnalyzer (new File(Properties.getProperty("usage_models")), false);
		
		models = new HashMap<String, Set< List<Transition>>> ();
		
		analyzer.analyzeModels (new ModelVisitor() {
			public void visit (int id, Model model, ModelData modelData) {
				//String fullMethodName = modelData.getClassName () + "." +
				//	modelData.getMethodName ();
				
				if (!models.containsKey (modelData.getClassName())) {
					models.put(modelData.getClassName(), new HashSet<List<Transition>>());
				}
				Set< List<Transition > > sca = models.get(modelData.getClassName());
				Set<EventPair> sca_model = SCAAbstractor.getSCAAbstraction (model, true);
				if(!sca_model.isEmpty())
					sca.add (getEventSequence(sca_model));
			}
		});
		/*
		for(Pattern pattern : patterns) {
			System.out.println("EventPairs:");
			for(EventPair pair : pattern.getProperties()) {
				System.out.println(pair);
				
			}
			System.out.println("Methods:");
			for(Method method : pattern.getObjects()) {
				System.out.println(method);		
			}
		}
		*/
		pattern_cache = new HashMap<AccessibleObject, Set<Pattern> >();
	}

	private boolean matches(Transition transition, String method) {
		if(transition instanceof InvokeMethodTransition) {
			InvokeMethodTransition itransition = (InvokeMethodTransition)transition;
			return itransition.getMethodCall().getMethodName().equals(method);
		} else if(transition instanceof ReturnValueOfMethodTransition) {
			ReturnValueOfMethodTransition rtransition = (ReturnValueOfMethodTransition)transition;
			return rtransition.getMethodCall().getMethodName().equals(method);
		}
		return false;
	}

	
	private boolean matches(EventPair pair, String method) {
		return matches(pair.getLeft(), method) || matches(pair.getRight(), method);
	}
	
	private String getName(AccessibleObject o) {
		if(o instanceof java.lang.reflect.Method) {
			java.lang.reflect.Method method = (java.lang.reflect.Method)o;
			return method.getName() + org.objectweb.asm.Type.getMethodDescriptor(method);
		} else if(o instanceof java.lang.reflect.Constructor<?>) {
			java.lang.reflect.Constructor<?> constructor = (java.lang.reflect.Constructor<?>)o;
			return "<init>" + org.objectweb.asm.Type.getConstructorDescriptor(constructor);
		}
		return ""; // TODO
	}
	
	private void cachePatterns(AccessibleObject o) {
		if(pattern_cache.containsKey(o))
			return;
		
		Set<Pattern> matched_patterns = new HashSet<Pattern>();
		
		String method_name = getName(o);
		for(Pattern pattern : patterns) {
			for(EventPair pair : pattern.getProperties()) {
				if(matches(pair, method_name)) {
					matched_patterns.add(pattern);
					break;
				}
			}
		}
		
		pattern_cache.put(o, matched_patterns);
	}
	
	public Set<Pattern> getPatterns(AccessibleObject o) {
		if(!pattern_cache.containsKey(o))
			cachePatterns(o);
		
		return pattern_cache.get(o);
	}
	
	/**
	 * Insert a random statement at a random position in the test
	 * @param test
	 */
	public void insertRandomStatement(TestCase test) {
		final double P = 1d/2d;
		
		double r = randomness.nextDouble();
		insertRandomOUM(test);

		/*
		if(r <= P) {
			// Insert a completely new OUM
			// What about methods that are not part of any OUM?
			insertRandomOUM(test);
		} 
		else if( r <= 2*P)
		{
			// Insert a OUM that makes use of an existing object
//			insertRandomPattern(test);
		}	
		*/
	}
	
	public void insertRandomPattern(TestCase test) {
		int position = randomness.nextInt(test.size() + 1);

		AccessibleObject o = test_cluster.getRandomTestCall();
		Set<Pattern> patterns = getPatterns(o);
		logger.info("Found "+patterns.size()+" patterns that contain "+o);
		
		if(patterns.isEmpty()) {
			// insert statement on its own
		} else {
			Pattern pattern = randomness.choice(patterns);
			//insertPattern(test, pattern, position);
		}
	}
	
	private List<Transition> getEventSequence(Set<EventPair> model) {
		List<EventPair> event_list = new ArrayList<EventPair>();
			
		for(EventPair pair1 : model) {
			int y = 0;
			int min_position = 0;
			int max_position = event_list.size();
			for(EventPair pair2 : event_list) {
				
				// pair1 is after pair2
				if(pair1.getLeft().equals(pair2.getRight())) {
					min_position = Math.max(min_position, y); 
				}
				// pair1 is before pair2
				if(pair1.getRight().equals(pair2.getLeft())) {
					max_position = Math.min(max_position, y); 			
				} 
				y++;
			}
			int position = 0;
			if(max_position > 0)
				randomness.nextInt(min_position, max_position);
			event_list.add(position, pair1);
		}
		List<Transition> transitions = new ArrayList<Transition>();
		for(EventPair pair : event_list) {
			if(transitions.isEmpty() || !transitions.get(transitions.size() - 1).equals(pair.getLeft())) {
				transitions.add(pair.getLeft());
			}
			transitions.add(pair.getRight());
		}
		
		return transitions;
	}
	
	private void selectStatement(TestCase test, String type, String method, int position) {
		try {
			Class<?> clazz = Class.forName(type);
			if(method.startsWith("<init>")) {
				for(Constructor<?> c : clazz.getDeclaredConstructors()) {
					String name = getName(c);
					if(name.equals(method)) {
						logger.info("Adding constructor: "+c);
						test_factory.addConstructor(test, c, position, 0);
					}
				}
			} else {
				for(java.lang.reflect.Method m : clazz.getDeclaredMethods()) {
					String name = getName(m);
					if(name.equals(method)) {
						logger.info("Adding method: "+m);
						test_factory.addMethod(test, m, position, 0);
}
				}
				
			}
		} catch(ClassNotFoundException e) {
			logger.warn("Invalid class name: "+type);
		} catch(ConstructionFailedException e) {
			logger.warn("Failed to add statement");
		}
	}
	
	private void insertTransition(TestCase test, Transition transition, int position) {
		logger.info("Current transition: "+transition.getLongEventString());
		// Convert to statement
		if(transition instanceof InvokeMethodTransition) {
			InvokeMethodTransition t = (InvokeMethodTransition)transition;
			String type = t.getMethodCall().getTypeName();
			String method = t.getMethodCall().getMethodName();
			List<Integer> parameters = t.getParameterIndices();
			logger.info("Adding method call: "+type+"."+method);
			selectStatement(test, type, method, position);
			
		} else if(transition instanceof ReturnValueOfMethodTransition) {
			ReturnValueOfMethodTransition t = (ReturnValueOfMethodTransition) transition;
			String type = t.getMethodCall().getTypeName();
			String method = t.getMethodCall().getMethodName();
			logger.info("Adding method call: "+type+"."+method);
			selectStatement(test, type, method, position);			

		} else {
			logger.info("Ignoring transition: "+transition.getLongEventString());
		}
	}
	
	public void insertRandomOUM(TestCase test) {
		int position = randomness.nextInt(test.size() + 1);

		// Randomly choose a OUM
		Set<List<Transition>> ms = models.get(Properties.TARGET_CLASS);
		//logger.info("Have "+ms.size()+" models for class "+Properties.TARGET_CLASS);
		/*
		for(List<Transition> model : ms) {
			logger.info("Model: ");
			for(Transition t : model) {
				logger.info(" "+t.getLongEventString());
			}
			
		}
		*/
		List<Transition> model = randomness.choice(models.get(Properties.TARGET_CLASS));
		
		logger.info("Selected model with "+model.size()+" transitions");

		for(Transition transition : model) {
			int old_length = test.size();
			insertTransition(test, transition, position);
			position += test.size() - old_length;
		}
	}
	
}
