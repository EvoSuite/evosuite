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
	
	
	int nodeCount = 0;
	ControlFlowGraph<V> cfg;
	
	Map<V,DominatorNode<V>> dominatorNodesMap = new HashMap<V,DominatorNode<V>>();
	Map<Integer,DominatorNode<V>> dominatorIDMap = new HashMap<Integer,DominatorNode<V>>();
	
	public DominatorTree(ControlFlowGraph<V> cfg) {

		this.cfg = cfg;
		
		createDominatorNodes();

		V root = cfg.determineEntryPoint();
		DominatorNode<V> rootNode = getDominatorNodeFor(root);
		
		depthFirstAnalyze(rootNode);
		
		computeSemiDominators();
		computeImmediateDominators(rootNode);
	}

	private void computeSemiDominators() {
		
		for (int i = nodeCount; i >= 2; i--) {
			DominatorNode<V> w = getDominatorNodeById(i);

//			logger.debug("computeing semDom: "+w.node.toString());
//			logger.debug("w semi n: "+w.semiDominator.n);
			
			for (V current : cfg.getParents(w.node)) {
				DominatorNode<V> v = getDominatorNodeFor(current);
				DominatorNode<V> u = v.eval();
				
//				if(v.n != u.n)
//					logger.debug("eval returned this");
				
				
//				logger.debug("u semi n: "+u.semiDominator.n);
				
				if (u.semiDominator.n < w.semiDominator.n)
					w.semiDominator = u.semiDominator;
			}
			
//			logger.debug("semDom for "+w.n+" was "+w.semiDominator.n);

			w.semiDominator.bucket.add(w);

			w.link(w.parent);
			
			
			while (!w.parent.bucket.isEmpty()) {
				
				DominatorNode<V> v = w.parent.getFromBucket();
				if(!w.parent.bucket.remove(v))
					throw new IllegalStateException("internal error");

				
				
				DominatorNode<V> u = v.eval();
				
				v.immediateDominator = (u.semiDominator.n < v.semiDominator.n ? u
						: w.parent);

//				logger.debug("iDom for "+v.n+" set to "+v.immediateDominator.n);
				
			}
		}
		
	}
	
	private void computeImmediateDominators(DominatorNode<V> rootNode) {
		
		for(int i = 2; i<=nodeCount;i++) {
			DominatorNode<V> w = getDominatorNodeById(i);
			
			if(w.immediateDominator != w.semiDominator)
				w.immediateDominator = w.immediateDominator.immediateDominator;
			
			logger.debug("iDom for node "+i+" was: "+w.immediateDominator.n);
		}
		
		rootNode.immediateDominator = null;
	}

	private DominatorNode<V> getDominatorNodeById(int id) {
		DominatorNode<V> r = dominatorIDMap.get(id);
		if(r == null)
			throw new IllegalArgumentException("id unknown to this tree");
		
		return r;
	}

	private void depthFirstAnalyze(DominatorNode<V> currentNode) {
		
		initialize(currentNode);
		
		logger.debug("dfs-Analyze: "+currentNode.n);
		logger.debug(currentNode.node.toString());
		
		for(V w : cfg.getChildren(currentNode.node)) {
			DominatorNode<V> wNode = getDominatorNodeFor(w);
			if(wNode.semiDominator == null) {
				wNode.parent = currentNode;
				depthFirstAnalyze(wNode);
			}
		}
	}
	
	private void initialize(DominatorNode<V> currentNode) {
		
		nodeCount++;
		currentNode.n = nodeCount;
		currentNode.semiDominator = currentNode;
		
		dominatorIDMap.put(nodeCount, currentNode);
		
	}

	private DominatorNode<V> getDominatorNodeFor(V v) {
		DominatorNode<V> r = dominatorNodesMap.get(v);
		if(r == null)
			throw new IllegalStateException("expect dominatorNodesMap to contain domNodes for all Vs");
		
		return r;
	}

	private void createDominatorNodes() {
		
//		DepthFirstIterator<V, DefaultEdge> dfs = new DepthFirstIterator<V, DefaultEdge>(
//				cfg.graph);
		
		for(V v : cfg.vertexSet())
			dominatorNodesMap.put(v, new DominatorNode<V>(v));
		
//		while(dfs.hasNext()) {
//			V node = dfs.next();
//			DominatorNode<V> domNode = new DominatorNode<V>(node, n);
//			dfsPath.add(domNode);
//			dominatorNodesMap.put(node, domNode);
//			n++;
//		}
	}
	
}
