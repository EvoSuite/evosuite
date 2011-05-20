package de.unisb.cs.st.evosuite.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;

import de.unisb.cs.st.evosuite.mutation.Mutateable;

/**
 * 
 * Given a CFG this class is supposed to compute the resulting Dominator Tree of
 * that CFG
 * 
 * TODO implement this!
 * 
 * 	CURRENT VERSION DOES NOT WORK!
 * 
 * @author Andre Mis
 */
public class DominatorTree<V extends Mutateable> extends EvoSuiteGraph<DominatorNode<V>> {

	private static Logger logger = Logger.getLogger(DominatorTree.class);
	
	
	ControlFlowGraph<V> cfg;
	
	List<DominatorNode<V>> dfsPath = new ArrayList<DominatorNode<V>>();
	
	Map<V,DominatorNode<V>> dominatorNodesMap = new HashMap<V,DominatorNode<V>>(); // TODO check this is OK
	
	public DominatorTree(ControlFlowGraph<V> cfg) {
		
		logger.info("Computing DominatorTree");
		
		this.cfg = cfg;
		
		createDominatorNodes();
		
		logger.debug(".. DominatorNodes: "+dfsPath.size());
		
		
		for (int i = dfsPath.size()-1; i >= 2; i--) {
			DominatorNode<V> w = dfsPath.get(i);
			
			logger.debug(".. processing node: "+w.n);
			
			Set<V> preds = cfg.getParents(w.node); // is this was pred() did in Hack's code?
			
			logger.debug(".. preds: "+preds.size());
			
			for(V pred : preds) {
				
				logger.debug("pred: "+pred.toString());
				
				DominatorNode<V> v = dominatorNodesMap.get(pred); // ??? is this what Hack's get() did?
				DominatorNode<V> u = v.eval();
				
				if (u.semi.n < w.semi.n)
					w.semi = u.semi;
			}

			w.bucket = w.semi.bucket;
			w.semi.bucket = w;

			w.link(w.parent);
			DominatorNode<V> v = w.parent.bucket;
			while (v != null) {
				DominatorNode<V> next = v.bucket;
				v.bucket = null;
				DominatorNode<V> u = v.eval();
				v.dom = u.semi.n < v.semi.n ? u : w.parent;
				v = next;
			}
		}

		for (int i = 2, n = dfsPath.size(); i < n; ++i) {
			DominatorNode<V> w = dfsPath.get(i);
			if (w.dom != w.semi)
				w.dom = w.dom.dom;
		}

		// free the DFS array list
		dfsPath = null;
	}

	private void createDominatorNodes() {
		
		DepthFirstIterator<V, DefaultEdge> dfs = new DepthFirstIterator<V, DefaultEdge>(
				cfg.graph);
		
		int n = 0;
		while(dfs.hasNext()) {
			V node = dfs.next();
			DominatorNode<V> domNode = new DominatorNode<V>(node, n);
			dfsPath.add(domNode);
			dominatorNodesMap.put(node, domNode);
			n++;
		}
	}
	
}
