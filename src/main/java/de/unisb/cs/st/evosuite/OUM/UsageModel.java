/**
 * 
 */
package de.unisb.cs.st.evosuite.OUM;

import java.io.File;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.softevo.jadet.sca.EventPair;
import org.softevo.jadet.sca.SCAAbstractor;
import org.softevo.oumextractor.modelcreator1.ModelAnalyzer;
import org.softevo.oumextractor.modelcreator1.ModelVisitor;
import org.softevo.oumextractor.modelcreator1.model.FieldValueTransition;
import org.softevo.oumextractor.modelcreator1.model.InvokeMethodTransition;
import org.softevo.oumextractor.modelcreator1.model.ReturnValueOfMethodTransition;
import org.softevo.oumextractor.modelcreator1.model.Transition;
import org.softevo.oumextractor.modelcreator1.model.Model;
import org.softevo.oumextractor.modelcreator1.ModelData;


import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.cfg.CFGMethodAdapter;

/**
 * A usage model for the entire API
 * 
 * @author Gordon Fraser
 *
 */
public class UsageModel {

	private static Logger logger = Logger.getLogger(UsageModel.class);
	
	private static UsageModel instance = null;
	
	private Map<String, Integer> models = new HashMap<String, Integer>();
	private Map<String, Integer> constraints = new HashMap<String, Integer>();
	
	private UsageModel() {
		generateUsageModel();
	}
	
	public static UsageModel getInstance() {
		if(instance == null)
			instance = new UsageModel();
		
		return instance;
	}
	
	/** Map from classname to class usage markov model */
	private Map<String, ClassUsage> usage_models = new HashMap<String, ClassUsage>();

	private static Map<String, ConcreteCall> calls = new HashMap<String, ConcreteCall>();
	
	/**
	 * Get markov model for given class
	 * @param className
	 * @return
	 */
	public ClassUsage getUsageModel(String className) {
		return usage_models.get(className);
	}

	/**
	 * 
	 * @param className: Class under test
	 * @param last: Last executed method
	 * @return
	 */
	public ConcreteCall getNextMethod(String className, ConcreteCall last) {
		return usage_models.get(className).getNextCall(last);
	}
	
	/**
	 * Return the generator that should be used for a given parameter
	 * 
	 * @param className - name of the class which owns the method
	 * @param method - the method/constructor that needs the parameter
	 * @param parameter - number of the parameter
	 * @return
	 */
	public ConcreteCall getGenerator(String className, ConcreteCall method, int parameter) {
		return usage_models.get(className).getParameterUsage(method, parameter);
	}

	/**
	 * Check whether there is usage information for this method/parameter pair
	 * @param className
	 * @param method
	 * @param parameter
	 * @return
	 */
	public boolean hasGenerators(String className, ConcreteCall method, int parameter) {
		if(!usage_models.containsKey(className))
			return false;
		
		return usage_models.get(className).hasParameterUsage(method, parameter);
	}

	public boolean hasClass(String className) {
		return usage_models.containsKey(className);
	}
	
	/**
	 * Check whether there is usage information for this method/parameter pair
	 * @param className
	 * @param method
	 * @param parameter
	 * @return
	 */
	public boolean hasGenerator(String className, ConcreteCall method, int parameter, ConcreteCall generator) {
		if(!usage_models.containsKey(className))
			return false;
		
		return usage_models.get(className).hasParameterUsage(method, parameter, generator);
	}

	
	public boolean hasUsageTransition(ConcreteCall method1, ConcreteCall method2) {
		if(method1 == null || method2 == null)
			return true;
		if(!usage_models.containsKey(method1.getClassName()))
			return false;
		return usage_models.get(method1.getClassName()).hasTransition(method1, method2);
	}
	
	/**
	 * Return a generator that should be used outside of parameters
	 * 
	 * @param className - name of the class that should be constructed
	 * @return
	 */
	public ConcreteCall getGenerator(String className, Class<?> clazz) {
		if(!usage_models.containsKey(className)) {
			for(String subclass : usage_models.keySet()) {
				if(clazz.isAssignableFrom(usage_models.get(subclass).getUsedClass())) {
					return usage_models.get(subclass).getGenerator();
				}
			}
			return null; // TODO: Check hierarchy?
		}
		return usage_models.get(className).getGenerator();
	}
	
	/**
	 * If a model for this class does not yet exist, create it
	 * @param className
	 */
	private boolean ensureClassModel(String className) {
		if(!usage_models.containsKey(className)) {
			try {
				Class<?> clazz = Class.forName(className);
				usage_models.put(className, new ClassUsage(clazz));
				return true;
			} catch(ClassNotFoundException e) {
				logger.debug("Could not find class "+className);
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Add new parameter generator, or increase weight
	 * @param className
	 * @param method
	 * @param parameter
	 * @param sourceClass
	 * @param sourceMethod
	 */
	private void updateParameterModel(String className, String method, int parameter, String sourceClass, String sourceMethod) {
		if(ensureClassModel(className)) {
			ConcreteCall target_call = getMethod(className, method);
			ConcreteCall source_call = getMethod(sourceClass, sourceMethod);
			if(target_call == null) {
				logger.debug("Could not find method "+className+"."+method);
				return;
			}
			if(source_call == null) {
				logger.debug("Could not find method "+sourceClass+"."+sourceMethod);
				return;
			}
			logger.debug("Adding parameter usage: "+target_call+" parameter "+parameter+": "+source_call);
			// TODO: Need to store concrete class sourceClass
			usage_models.get(className).addParameterUsage(target_call, parameter, source_call); // TODO: This could be more efficient
		}
	}
	
	/**
	 * Add a transition between method 1 and 2, or increase edge weight
	 * @param className
	 * @param method1
	 * @param method2
	 */
	private void updateUsageModel(String className, String method1, String method2) {
		if(ensureClassModel(className)) {
			ConcreteCall call1 = getMethod(className, method1);
			ConcreteCall call2 = getMethod(className, method2);
			if(call1 == null) {
				logger.debug("Could not find method "+className+"."+call1);
				return;
			}
			if(call2 == null) {
				logger.debug("Could not find method "+className+"."+call2);
				return;
			}
			logger.debug("Adding usage transition for class "+className+": "+call1+" -> "+call2);
			usage_models.get(className).addTransition(call1, call2); // TODO: This could be more efficient
		}
	}
	
	private void updateGenerator(String className, String sourceClass, String sourceCall) {
		if(ensureClassModel(className)) {
			ConcreteCall source_call = getMethod(sourceClass, sourceCall);
			if(source_call == null) {
				logger.debug("Could not find method "+sourceClass+"."+sourceCall);
				return;
			}
			logger.debug("Adding generator for "+className+": "+source_call+" in class "+sourceClass);
			// TODO: Have to store concrete class as well for generators
			usage_models.get(className).addGenerator(source_call);
			if(ensureClassModel(sourceClass)) 
				usage_models.get(sourceClass).addCall(source_call);
		}
	}
	
	private void updateModel(String className, Transition left, Transition right) {

		// TODO: When adding a parameter call or a generator, we need to check that the classusage object of the parameter actually has this vertex as well?
		
		// TODO: Create a vertex if a method is used, even if we have no parameter/usage data!
		// TODO: Check if a generator is actually available (e.g. java.text.DateTime.parse)
		if(!className.startsWith(Properties.PROJECT_PREFIX)) {
			logger.debug("Ignoring pair outside project prefix: "+left.getShortEventString()+ " -> "+right.getShortEventString());
			return;
		}
		if(right instanceof InvokeMethodTransition) {
			InvokeMethodTransition t2 = (InvokeMethodTransition)right;
			//if(!t2.getMethodCall().getTypeName().startsWith(Properties.PROJECT_PREFIX) || getClassByName(t2.getMethodCall().getTypeName()) == null) {
			//	logger.debug("Ignoring pair outside project prefix: "+left.getShortEventString()+ " -> "+t2.getShortEventString());
			//	return;
			//}
			logger.debug("Current pair: "+left.getShortEventString() + " -> "+right.getShortEventString());
			//addGenerator(className, left);
			//addGenerator(className, right);

			// It is a generator if:
			// - RHS Parameter == 0, and
			// - LHS is a Field, or
			// - LHS is a ReturnValueMethod, or
			// - LHS is a Constructor
			
			if(left instanceof InvokeMethodTransition) {
				InvokeMethodTransition t1 = (InvokeMethodTransition)left;
				int param1 = t1.getParameterIndices().get(0); // TODO: Loop over parameters
				int param2 = t2.getParameterIndices().get(0); // TODO: Loop over parameters
				
				// Parameter 0 on both -> method sequence
				if(param1 == 0 && param2 == 0) {
					//						updateUsageModel(t1.getMethodCall().getTypeName(), t1.getMethodCall().getMethodName(), t2.getMethodCall().getMethodName());
					updateUsageModel(className, t1.getMethodCall().getMethodName(), t2.getMethodCall().getMethodName());

					// If LHS is a constructor this is a generator
					if(t1.getMethodCall().getMethodName().contains("<init>")) {
						updateGenerator(className, t1.getMethodCall().getTypeName(), t1.getMethodCall().getMethodName());
					}
				}
				
				// Parameter 0 on left side means the callee is used as parameter on right hand side
				if(param1 == 0 ) { //&& !t1.getMethodCall().getTypeName().equals("java.lang.Object")) {
				// TODO: param1 should be 0?
					updateParameterModel(className, t2.getMethodCall().getMethodName(), param2, t1.getMethodCall().getTypeName(), t1.getMethodCall().getMethodName());
//					updateParameterModel(t2.getMethodCall().getTypeName(), t2.getMethodCall().getMethodName(), param2, t1.getMethodCall().getTypeName(), t1.getMethodCall().getMethodName());
				}
				
				
			} else if(left instanceof ReturnValueOfMethodTransition) {
				ReturnValueOfMethodTransition t1 = (ReturnValueOfMethodTransition) left;

				for(int param2 : t2.getParameterIndices()) {
//					updateParameterModel(t2.getMethodCall().getTypeName(), t2.getMethodCall().getMethodName(), param2, t1.getMethodCall().getTypeName(), t1.getMethodCall().getMethodName());
					updateParameterModel(className, t2.getMethodCall().getMethodName(), param2, t1.getMethodCall().getTypeName(), t1.getMethodCall().getMethodName());

					// If parameter on RHS == 0, then this is a generator
					if(param2 == 0) {
//						if(className.equals(t1.getMethodCall().getTypeName()))
//							logger.info("This could be a useful omission: "+t1.getMethodCall().getTypeName()+"."+t1.getMethodCall().getMethodName());

						updateGenerator(className, t1.getMethodCall().getTypeName(), t1.getMethodCall().getMethodName());
					}
				}
			} else if(left instanceof FieldValueTransition) {
				FieldValueTransition t1 = (FieldValueTransition) left;
				for(int param2 : t2.getParameterIndices()) {
					String fullName = t1.getFieldName();
					int dot = fullName.lastIndexOf(".");
					String className2 = fullName.substring(0, dot);
					//if(!className.equals(className2))
					//	logger.info("Different field sources: "+className+"/"+className2);
					String fieldName = fullName.substring(dot + 1);
//					updateParameterModel(t2.getMethodCall().getTypeName(), t2.getMethodCall().getMethodName(), param2, className, fieldName);
					updateParameterModel(className, t2.getMethodCall().getMethodName(), param2, className2, fieldName);
					
					// If parameter on RHS == 0, then this is a generator
					if(param2 == 0) {
//						if(className.equals(className2))
//							logger.info("This could be a useful omission: "+className2+"."+fieldName);

						updateGenerator(className, className2, fieldName);
					}

				}
			} else {
				logger.trace("Unknown transition A: "+left.getShortEventString());
			}
		} else {
			logger.trace("Unknown transition B: "+left.getShortEventString()+" -> "+right.getShortEventString());			
		}
	}
	
	private String getModelClassName(String modelName) {
		int pos_at = modelName.indexOf("@");
		int pos_sp = modelName.indexOf(" ", pos_at);
		return modelName.substring(pos_at+1, pos_sp);
	}
	
	private void generateUsageModel() {
		logger.info("Generating usage model");
		String model_names = Properties.getProperty("usage_models");
		
		
		for(String filename : model_names.split(":")) {
			ModelAnalyzer analyzer = new ModelAnalyzer (new File(filename), false);
			analyzer.analyzeModels (new ModelVisitor() {
				public void visit (int id, Model model, ModelData modelData) {
					//String fullMethodName = modelData.getClassName () + "." +
					//	modelData.getMethodName ();
					String className = getModelClassName(modelData.getModelName());
					if(!className.startsWith(Properties.PROJECT_PREFIX)) {
						if(!className.startsWith("java") && !className.startsWith("junit"))
							logger.debug("Ignoring model for external class: "+className);
					} else {
						if(!models.containsKey(className)) {
							models.put(className, 1);
							constraints.put(className, model.getAllTransitions().size());
						} else {
							models.put(className, models.get(className) + 1);
							constraints.put(className, models.get(className) + model.getAllTransitions().size());
						}
						
						logger.debug("Current model: "+modelData.getModelName()+" generated in "+modelData.getClassName()+"."+modelData.getMethodName());
						logger.debug("   Number of transitions: "+model.getAllTransitions().size());
						if(model.getAllTransitions().size() == 1) {
							for(Transition t : model.getAllTransitions()) {
								logger.debug(t.getLongEventString());
								if(t instanceof InvokeMethodTransition) {
									InvokeMethodTransition mt = (InvokeMethodTransition)t;
									if(mt.getMethodCall().getTypeName().equals(className) && mt.getMethodCall().getMethodName().contains("<init>") && mt.getParameterIndices().contains(0)) {
										updateGenerator(className, mt.getMethodCall().getTypeName(), mt.getMethodCall().getMethodName());
									}
//									logger.info("A method transition? "+t.getShortEventString());

								} else if(t instanceof ReturnValueOfMethodTransition) {
									ReturnValueOfMethodTransition rt = (ReturnValueOfMethodTransition)t;
									updateGenerator(className, rt.getMethodCall().getTypeName(), rt.getMethodCall().getMethodName());

								} else if(t instanceof FieldValueTransition) {
									FieldValueTransition ft = (FieldValueTransition)t;
									String fullName = ft.getFieldName();
									int dot = fullName.lastIndexOf(".");
									String className2 = fullName.substring(0, dot);
									String fieldName = fullName.substring(dot + 1);
									updateGenerator(className, className2, fieldName);
								}
							}
						}
						//logger.info("Model class: "+getModelClassName(modelData.getModelName()));
						Set<EventPair> sca_model = SCAAbstractor.getSCAAbstraction (model, true);
						for(EventPair pair : sca_model) {
							//constraints.put(className, constraints.get(className) + 1);
							updateModel(className, pair.getLeft(), pair.getRight());
						}
					}
				}
			});
		}
		logger.info("Usage model information loaded from "+model_names);
		logger.info("Got usage information for "+usage_models.size()+" classes");
		
		int num_models = 0;
		int num_constraints = 0;
		int num_classes = 0;
		
		for(String classname: models.keySet()) {
			if(!classname.startsWith(Properties.PROJECT_PREFIX))
				continue;
			//logger.info(classname);
			num_models += models.get(classname);
			num_constraints += constraints.get(classname);
			num_classes++;
		}
		
		logger.info("Number of classes: "+num_classes);
		logger.info("Number of models: "+num_models);
		logger.info("Number of constraints: "+num_constraints);
		
//		for(String classname : usage_models.keySet()) {
//			usage_models.get(classname).usage_model.
//		}
		
		ClassUsage u = usage_models.get(Properties.TARGET_CLASS);
		if(u != null && logger.isDebugEnabled()) {
			logger.info("Target class methods: "+u.getNumberOfMethods());
			for(ConcreteCall call : u.getMethods()) {
				String name = Properties.TARGET_CLASS+"."+call.getName();
				if(!CFGMethodAdapter.methods.contains(name)) {
					logger.debug("  Have usage of unknown method "+name);				
				}
			}

			for(ConcreteCall call : u.getMethods()) {
				String name = Properties.TARGET_CLASS+"."+call.getName();
				if(CFGMethodAdapter.methods.contains(name)) {
					logger.debug("  HAVE usage of method "+name);				
				}
			}

			for(String name : CFGMethodAdapter.methods){
				//logger.info("Need :"+name);
				boolean found = false;
				for(ConcreteCall call : u.getMethods()) {
					String n = Properties.TARGET_CLASS+"."+call.getName();
					if(name.equals(n)) {
						logger.debug("Found test method: "+n);
						found = true;
						break;
					}
				}
				if(!found)
					logger.debug("NOT found: "+name);
			}
		}		
		//for(ClassUsage c : usage_models.values())
		//	c.writeDOTFile();
			
		//usage_models.get(Properties.TARGET_CLASS).writeDOTFile();
		/*
			for(EventPair pair : pattern.getProperties()) {
				updateModel(pair.getLeft(), pair.getRight(), pattern.getSupport());
			}
		}
		*/
	}
	
	private static String getName(AccessibleObject o) {
		if(o instanceof java.lang.reflect.Method) {
			java.lang.reflect.Method method = (java.lang.reflect.Method)o;
			return method.getName() + org.objectweb.asm.Type.getMethodDescriptor(method);
		} else if(o instanceof java.lang.reflect.Constructor<?>) {
			java.lang.reflect.Constructor<?> constructor = (java.lang.reflect.Constructor<?>)o;
			return "<init>" + org.objectweb.asm.Type.getConstructorDescriptor(constructor);
		} else if(o instanceof java.lang.reflect.Field) {
			java.lang.reflect.Field field = (Field)o;
			return field.getName();
		}
		return ""; // TODO
	}
	
	public static Class<?> getClassByName(String className) {
		try {
			Class<?> clazz = Class.forName(className);
			return clazz;
		} catch(ClassNotFoundException e) {
			//logger.debug("Class not found! "+className);
			return null;
		}
	}
	
	public static ConcreteCall getMethod(String className, String methodName) {
		// TODO: Create ConcreteCall object here
		String key = className+"."+methodName;
		if(calls.containsKey(key))
			return calls.get(key);
		
		Class<?> clazz = getClassByName(className);
		if(clazz == null) {
			logger.debug("Could not find class "+className);
			return null;
		}
		AccessibleObject o = getMethod(clazz, methodName);
		if(o == null) {
			logger.debug("Could not find method: "+methodName+" in class "+className);
			return null;
		}
		
		//String key2 = o.toString();
		//if(calls.containsKey(key2))
		//	return calls.get(key2);
		
		//logger.info("Generating new accessibleobject for "+key);
		calls.put(key, new ConcreteCall(className, o));
		return calls.get(key);
	}
	
	private static AccessibleObject getMethod(Class<?> clazz, String methodName) {
		if(methodName.startsWith("<init>")) {
			for(Constructor<?> c : clazz.getConstructors()) {
				String name = getName(c);
				if(name.equals(methodName) && !Modifier.isPrivate(c.getModifiers())) {
					return c;
				}
			}
		} else {
			for(Method m : clazz.getMethods()) {
				String name = getName(m);
				//logger.info("Comparing "+clazz.getName()+"."+methodName+" with "+clazz.getName()+"."+name);
				if(name.equals(methodName) && !Modifier.isPrivate(m.getModifiers())) {
					return m;
				}
			}
			for(Field f : clazz.getFields()) {
				String name = getName(f);
				if(name.equals(methodName) && ! Modifier.isPrivate(f.getModifiers()))
					return f;
			}
		}
		return null; // not found
	}
	
	public void dumpModel() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(usage_models.size());
		sb.append(" class usage models: \n");
		for(String className : usage_models.keySet()) {
			sb.append(" Class ");
			sb.append(className);
			sb.append("\n");
			sb.append(usage_models.get(className).toString());
		}
		
		System.out.println(sb.toString());
	}
	
}
