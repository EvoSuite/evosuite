/**
 * 
 */
package de.unisb.cs.st.evosuite.OUM;

import org.apache.log4j.Logger;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.StringNameProvider;
import org.jgrapht.ext.VertexNameProvider;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import de.unisb.cs.st.ga.Randomness;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * @author Gordon Fraser
 *
 */
public class ClassUsage {

	private static Logger logger = Logger.getLogger(ClassUsage.class);
	
	private Class<?> clazz;
	
	private String class_name;
	
	/** Common usage of method sequences */
	DefaultDirectedWeightedGraph<AccessibleObject,DefaultWeightedEdge> usage_model 
		= new DefaultDirectedWeightedGraph<AccessibleObject,DefaultWeightedEdge>(DefaultWeightedEdge.class);

	/** Link method parameters to methods of other classes */
	Map<AccessibleObject, MethodUsage> method_model = new HashMap<AccessibleObject, MethodUsage>();

	/** How is this class generated? */
	Map<AccessibleObject, Integer> generators = new HashMap<AccessibleObject, Integer>();
	
	int total_generators = 0;
	
	public int getNumberOfMethods() {
		return usage_model.vertexSet().size();
	}
	
	public Set<AccessibleObject> getMethods() {
		return usage_model.vertexSet();
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
	
	public AccessibleObject getNextCall(AccessibleObject last) {
		
		if(last == null) {
			return Randomness.getInstance().choice(usage_model.vertexSet());
		}
		
		if(!usage_model.containsVertex(last)) {
			for(AccessibleObject call : usage_model.vertexSet()) {
				if(getName(call).equals(getName(last))) {
					logger.info("Found replacement call with same name: "+call);
					last = call;
					break;
				}
			}
			if(!usage_model.containsVertex(last)) {
				logger.error("Error: Class model for class "+class_name+" does not contain vertex for "+last);
				logger.info("We have the following vertices:");
				for(AccessibleObject o : usage_model.vertexSet()) {
					logger.info(o);
				}
				return Randomness.getInstance().choice(usage_model.vertexSet());
			}
		}
		
		if(usage_model.outDegreeOf(last) == 0) {
			return Randomness.getInstance().choice(usage_model.vertexSet());
		}
		
		int total = 0;

		for(DefaultWeightedEdge edge : usage_model.outgoingEdgesOf(last)) {
			total += usage_model.getEdgeWeight(edge);
		}
		
		int index = Randomness.getInstance().nextInt(total);
		
		for(DefaultWeightedEdge edge : usage_model.outgoingEdgesOf(last)) {
			int count = (int) usage_model.getEdgeWeight(edge);

            if (index < count) {
                return usage_model.getEdgeTarget(edge);
            }

            index -= count;
		}
		
		return null;
	}

	/*
	private boolean isGenerator(AccessibleObject o) {
		if(o instanceof Field) {
			
		} else if(o instanceof Method) {
			
		} else if(o instanceof Constructor<?>) {
			
		}
		return false;
	}
	*/
	
	public AccessibleObject getGenerator() {
		
		/*
		List<AccessibleObject> generators = new ArrayList<AccessibleObject>();
		for(AccessibleObject o : usage_model.vertexSet()) {
			if(usage_model.inDegreeOf(o) == 0 && usage_model.outDegreeOf(o) > 0) {
				logger.info("Generator for class "+class_name+": "+o);
				generators.add(o);
			}
		}
		if(generators.isEmpty())
			return null;
		else
			return Randomness.getInstance().choice(generators);
			*/
		int index = Randomness.getInstance().nextInt(total_generators);
		Iterator<Entry<AccessibleObject, Integer>> i = generators.entrySet().iterator();

        while (i.hasNext()) {
        	Entry<AccessibleObject, Integer> link = i.next();
            int count = link.getValue();

            if (index < count) {
                return link.getKey();
            }

            index -= count;
        }
        
        return null;
	}
	
	public void addGenerator(AccessibleObject call) {
		/*
		if(!usage_model.containsVertex(call)) {
			//logger.info("Adding new method vertex "+call1);
			usage_model.addVertex(call);
		}
		*/
		if(!generators.containsKey(call))
			generators.put(call, 1);
		else
			generators.put(call, generators.get(call) + 1);

		total_generators++;
	}
	
	public void addTransition(AccessibleObject call1, AccessibleObject call2) {

		if(usage_model.containsEdge(call1, call2)) {
			DefaultWeightedEdge edge = usage_model.getEdge(call1, call2);
			usage_model.setEdgeWeight(edge, usage_model.getEdgeWeight(edge) + 1.0);
		} else {
			if(!usage_model.containsVertex(call1)) {
				//logger.info("Adding new method vertex "+call1);
				usage_model.addVertex(call1);
			}
			if(!usage_model.containsVertex(call2)) {
				//logger.info("Adding new method vertex "+call2);
				usage_model.addVertex(call2);
			}
			
			DefaultWeightedEdge edge = usage_model.addEdge(call1, call2);
			//logger.info("Adding new edge from "+call1+" to "+call2+" in class "+class_name);
			usage_model.setEdgeWeight(edge, 1.0);
		}
	}
	
	public ClassUsage(Class<?> clazz) {
		this.clazz = clazz;
		this.class_name = clazz.getName();
	}
	
	public void addParameterUsage(AccessibleObject call, int parameter, AccessibleObject source) {
		if(!method_model.containsKey(call))
			method_model.put(call, new MethodUsage(call));
		
		MethodUsage usage = method_model.get(call);
		usage.addUsage(parameter, source);
	}
	
	public AccessibleObject getParameterUsage(AccessibleObject call, int parameter) {
		return method_model.get(call).getNextGenerator(parameter);
	}
	
	public boolean hasParameterUsage(AccessibleObject call, int parameter) {
		if(!method_model.containsKey(call))
			return false;
		
		return method_model.get(call).hasUsage(parameter);
	}
	
	public boolean hasParameterUsage(AccessibleObject call, int parameter, AccessibleObject generator) {
		if(!hasParameterUsage(call, parameter))
			return false;
		
		return method_model.get(call).hasUsage(parameter, generator);
	}
	
	private class WeightNameProvider implements EdgeNameProvider<DefaultWeightedEdge> {
	
		public String getEdgeName(DefaultWeightedEdge e) {
			return Integer.toString((int)usage_model.getEdgeWeight(e));
		}
	};
	
	private class AOStringNameProvider implements VertexNameProvider<AccessibleObject> {

		/* (non-Javadoc)
		 * @see org.jgrapht.ext.VertexNameProvider#getVertexName(java.lang.Object)
		 */
		@Override
		public String getVertexName(AccessibleObject o) {
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
			return null;
		}
		
	};
	
	public void writeDOTFile() {
		try {
			FileWriter fstream = new FileWriter(class_name+".dot");
			BufferedWriter out = new BufferedWriter(fstream);
			//FrameVertexNameProvider nameprovider = new FrameVertexNameProvider(mn.instructions);
			//	DOTExporter<Integer,DefaultEdge> exporter = new DOTExporter<Integer,DefaultEdge>();
			//DOTExporter<Integer,DefaultEdge> exporter = new DOTExporter<Integer,DefaultEdge>(new IntegerNameProvider(), nameprovider, new IntegerEdgeNameProvider());
			//			DOTExporter<Integer,DefaultEdge> exporter = new DOTExporter<Integer,DefaultEdge>(new LineNumberProvider(), new LineNumberProvider(), new IntegerEdgeNameProvider());
			DOTExporter<AccessibleObject, DefaultWeightedEdge> exporter = new DOTExporter<AccessibleObject, DefaultWeightedEdge>(new IntegerNameProvider(), new AOStringNameProvider(), new WeightNameProvider());
			exporter.export(out, usage_model);
			//exporter.export(out, g.getGraph());
		} catch (IOException e) {
		}
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("  Usage model: \n");
		for(AccessibleObject o : usage_model.vertexSet()) {
			for(DefaultWeightedEdge e : usage_model.outgoingEdgesOf(o)) {
				AccessibleObject o2 = usage_model.getEdgeTarget(e);
				sb.append("    "+o+" -> ");
				sb.append(usage_model.getEdgeWeight(e));
				sb.append(" -> ");
				sb.append(o2);
				sb.append("\n");
			}
		}
		sb.append("\n  Parameter model: \n");
		for(MethodUsage m : method_model.values()) {
			sb.append(m);
			sb.append("\n");
		}
		
		return sb.toString();
	}
	
}
