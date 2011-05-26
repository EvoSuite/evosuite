package de.unisb.cs.st.evosuite.cfg;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.mutation.Mutateable;

/**
 * 
 * Given a CFG this class computes the immediateDominators for each CFG vertex
 * 
 * The current algorithm runs in time O( e * log n ) where e is the number of
 * control flow edges and n the number of CFG vertices and is taken from:
 * 
 * "A Fast Algorithm for Finding Dominators in a Flowgraph" THOMAS LENGAUER and
 * ROBERT ENDRE TARJAN 1979, Stanford University
 * 
 * DOI: 10.1145/357062.357071
 * http://portal.acm.org/citation.cfm?doid=357062.357071
 * 
 * 
 * TODO so far this is not really a tree - might want to change that and extend
 * from EvoSuiteGraph
 * 
 * @author Andre Mis
 */
public class DominatorTree<V extends Mutateable> {

	private static Logger logger = Logger.getLogger(DominatorTree.class);
	
	
	private  int nodeCount = 0;
	private  ControlFlowGraph<V> cfg;
	
	private Map<V,DominatorNode<V>> dominatorNodesMap = new HashMap<V,DominatorNode<V>>();
	private  Map<Integer,DominatorNode<V>> dominatorIDMap = new HashMap<Integer,DominatorNode<V>>();

	/**
	 * Will start the computation of all immediateDominators for the given CFG
	 * which can later be retrieved via getImmediateDominator()
	 */
	public DominatorTree(ControlFlowGraph<V> cfg) {

		this.cfg = cfg;
		
		createDominatorNodes();

		V root = cfg.determineEntryPoint(); // TODO change to getEntryPoint()
		DominatorNode<V> rootNode = getDominatorNodeFor(root);
		
		depthFirstAnalyze(rootNode);
		
		computeSemiDominators();
		computeImmediateDominators(rootNode);
	}

	/**
	 * Given a node of this objects CFG this method returns it's previously
	 * computed immediateDominator
	 * 
	 * The immediateDominator iDom of a node v has the following properties:
	 * 
	 * 1) iDom dominates v
	 * 
	 * 2) every other dominator of v dominates iDom
	 * 
	 * A node w dominates v or is a dominator of v if and only if every path
	 * from the CFG's entryPoint to v contains w
	 * 
	 * @param v A node within this objects CFG for wich the immediateDominator is to be returned
	 * @return 
	 */
	public V getImmediateDominator(V v) {
		if (v == null)
			throw new IllegalArgumentException("null given");
		DominatorNode<V> domNode = dominatorNodesMap.get(v);
		if (domNode == null)
			throw new IllegalStateException("unknown vertice given");
		
		if(domNode.immediateDominator == null) {
			// sanity check: this is only allowed to happen if v is root of CFG
			if(domNode.n != 1)
				throw new IllegalStateException("expect known node without iDom to be root of CFG");
			
			return null;
		}
		
		return domNode.immediateDominator.node;
	}
	
	// computation
	
	private void createDominatorNodes() {
		
		logger.info("Computing dominators for "+cfg.getName());
		
		for(V v : cfg.vertexSet())
			dominatorNodesMap.put(v, new DominatorNode<V>(v));
	}

	private void depthFirstAnalyze(DominatorNode<V> currentNode) {
		// step 1
		
		initialize(currentNode);
		
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
	
	private void computeSemiDominators() {
		
		for (int i = nodeCount; i >= 2; i--) {
			DominatorNode<V> w = getDominatorNodeById(i);
			
			// step 2
			for (V current : cfg.getParents(w.node)) {
				DominatorNode<V> v = getDominatorNodeFor(current);
				DominatorNode<V> u = v.eval();
				
				if (u.semiDominator.n < w.semiDominator.n)
					w.semiDominator = u.semiDominator;
			}
			
			w.semiDominator.bucket.add(w);
			w.link(w.parent);
			
			// step 3
			while (!w.parent.bucket.isEmpty()) {
				
				DominatorNode<V> v = w.parent.getFromBucket();
				if(!w.parent.bucket.remove(v))
					throw new IllegalStateException("internal error");

				DominatorNode<V> u = v.eval();
				v.immediateDominator = (u.semiDominator.n < v.semiDominator.n ? u
						: w.parent);
			}
		}
	}
	
	private void computeImmediateDominators(DominatorNode<V> rootNode) {
		// step 4
		for(int i = 2; i<=nodeCount;i++) {
			DominatorNode<V> w = getDominatorNodeById(i);
			
			if(w.immediateDominator != w.semiDominator)
				w.immediateDominator = w.immediateDominator.immediateDominator;
			
//			logger.debug("iDom for node "+i+" was: "+w.immediateDominator.n);
		}
		
		rootNode.immediateDominator = null;
	}
	
	private DominatorNode<V> getDominatorNodeById(int id) {
		DominatorNode<V> r = dominatorIDMap.get(id);
		if(r == null)
			throw new IllegalArgumentException("id unknown to this tree");
		
		return r;
	}

	private DominatorNode<V> getDominatorNodeFor(V v) {
		DominatorNode<V> r = dominatorNodesMap.get(v);
		if(r == null)
			throw new IllegalStateException("expect dominatorNodesMap to contain domNodes for all Vs");
		
		return r;
	}
}
