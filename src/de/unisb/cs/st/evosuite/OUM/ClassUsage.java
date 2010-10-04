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

import de.unisb.cs.st.evosuite.ga.Randomness;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
	DefaultDirectedWeightedGraph<ConcreteCall,DefaultWeightedEdge> usage_model 
		= new DefaultDirectedWeightedGraph<ConcreteCall,DefaultWeightedEdge>(DefaultWeightedEdge.class);

	/** Link method parameters to methods of other classes */
	Map<ConcreteCall, MethodUsage> method_model = new HashMap<ConcreteCall, MethodUsage>();

	/** How is this class generated? */
	Map<ConcreteCall, Integer> generators = new HashMap<ConcreteCall, Integer>();
	
	int total_generators = 0;
	
	public int getNumberOfMethods() {
		return usage_model.vertexSet().size();
	}
	
	public Set<ConcreteCall> getMethods() {
		return usage_model.vertexSet();
	}
	
	public Class<?> getUsedClass() {
		return clazz;
	}
	
	public ConcreteCall getNextCall(ConcreteCall last) {
		
		if(last == null) {
			return Randomness.getInstance().choice(usage_model.vertexSet());
		}
		
		if(!usage_model.containsVertex(last)) {
			for(ConcreteCall call : usage_model.vertexSet()) {
				if(call.getName().equals(last.getName())) {
					logger.info("Found replacement call with same name: "+call);
					last = call;
					break;
				}
			}
			if(!usage_model.containsVertex(last)) {
				logger.error("Error: Class model for class "+class_name+" does not contain vertex for last call "+last);
				//logger.info("We have the following vertices:");
				//for(AccessibleObject o : usage_model.vertexSet()) {
				//	logger.info(o);
				//}
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

	public boolean hasGenerators() {
		return total_generators > 0;
	}

	public ConcreteCall getGenerator() {
		
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
		int index = 0;
		if(total_generators > 1)
			index = Randomness.getInstance().nextInt(total_generators);
		else if(total_generators == 0) {
			/*
			try {
				Constructor<?> c = clazz.getConstructor(new Class<?>[]{});
				if(c != null) {
					generators.put(c, 1);
					usage_model.addVertex(c);
				}
				return c;
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			*/
			return null;
		}
		
		Iterator<Entry<ConcreteCall, Integer>> i = generators.entrySet().iterator();

        while (i.hasNext()) {
        	Entry<ConcreteCall, Integer> link = i.next();
            int count = link.getValue();

            if (index < count) {
                return link.getKey();
            }

            index -= count;
        }
        
        return null;
	}
	
	public void addCall(ConcreteCall call) {
		if(call.isPrivate())
			return;
		if(!usage_model.containsVertex(call)) {
			//logger.info("Adding new method vertex "+call1);
			usage_model.addVertex(call);
		}		
	}
	
	public void addGenerator(ConcreteCall call) {
		if(call.isPrivate())
			return;
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

		if(call.getClassName().equals(class_name)) {
			if(!usage_model.containsVertex(call)) {
				//logger.info("Adding new method vertex "+call1);
				usage_model.addVertex(call);
			}			
		}
		
		total_generators++;
	}
	
	public void addTransition(ConcreteCall call1, ConcreteCall call2) {
		if(call1.isPrivate() || call2.isPrivate())
			return;

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
	
	public void addParameterUsage(ConcreteCall call, int parameter, ConcreteCall source) {
		if(call.isPrivate())
			return;
		if(source.isPrivate())
			return;
		if(!method_model.containsKey(call))
			method_model.put(call, new MethodUsage(call));
		
		MethodUsage usage = method_model.get(call);
		usage.addUsage(parameter, source);
	}
	
	public ConcreteCall getParameterUsage(ConcreteCall call, int parameter) {
		return method_model.get(call).getNextGenerator(parameter);
	}
	
	public boolean hasParameterUsage(ConcreteCall call, int parameter) {
		if(!method_model.containsKey(call))
			return false;
		
		return method_model.get(call).hasUsage(parameter);
	}
	
	public boolean hasParameterUsage(ConcreteCall call, int parameter, ConcreteCall generator) {
		if(!hasParameterUsage(call, parameter))
			return false;
		
		return method_model.get(call).hasUsage(parameter, generator);
	}
	
	public boolean hasTransition(ConcreteCall call1, ConcreteCall call2) {
		return usage_model.containsEdge(call1, call2);
	}
	
	private class WeightNameProvider implements EdgeNameProvider<DefaultWeightedEdge> {
	
		public String getEdgeName(DefaultWeightedEdge e) {
			return Integer.toString((int)usage_model.getEdgeWeight(e));
		}
	};
	
	private class AOStringNameProvider implements VertexNameProvider<ConcreteCall> {

		/* (non-Javadoc)
		 * @see org.jgrapht.ext.VertexNameProvider#getVertexName(java.lang.Object)
		 */
		@Override
		public String getVertexName(ConcreteCall o) {
			return o.getName();
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
			DOTExporter<ConcreteCall, DefaultWeightedEdge> exporter = new DOTExporter<ConcreteCall, DefaultWeightedEdge>(new IntegerNameProvider(), new AOStringNameProvider(), new WeightNameProvider());
			exporter.export(out, usage_model);
			//exporter.export(out, g.getGraph());
		} catch (IOException e) {
		}
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("  Usage model: \n");
		for(ConcreteCall o : usage_model.vertexSet()) {
			for(DefaultWeightedEdge e : usage_model.outgoingEdgesOf(o)) {
				ConcreteCall o2 = usage_model.getEdgeTarget(e);
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
