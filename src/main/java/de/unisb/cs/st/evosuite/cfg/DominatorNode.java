package de.unisb.cs.st.evosuite.cfg;

import java.util.HashSet;
import java.util.Set;


public class DominatorNode<V> {

	final V node;
	int n = 0;	
	
	DominatorNode<V> parent;
	DominatorNode<V> semiDominator;
	
	DominatorNode<V> ancestor;
	DominatorNode<V> label;
	
	Set<DominatorNode<V>> bucket = new HashSet<DominatorNode<V>>();
	DominatorNode<V> immediateDominator;
	
	
	DominatorNode(V node) {
		this.node = node;
		
		this.label = this;
	}
	
	void link(DominatorNode<V> v) {
		ancestor = v;
	}
	
	DominatorNode<V> eval() {
		if(ancestor == null)
			return this;
		
		compress();
		
		return label;
	}
	
	void compress() {
		if(ancestor == null)
			throw new IllegalStateException("may only be called when ancestor is set");
		
		if(ancestor.ancestor != null) {
			ancestor.compress();
			if(ancestor.label.semiDominator.n < label.semiDominator.n)
				label = ancestor.label;
			
			ancestor = ancestor.ancestor;
		}
	}

	public DominatorNode<V> getFromBucket() {
		
		for(DominatorNode<V> r : bucket)
			return r;
		
		return null;
	}
}