package de.unisb.cs.st.evosuite.cfg;


public class DominatorNode<V> {
	
	DominatorNode(V node, int n) {
		this.node = node;
		this.lab = this;
		this.n = n;
		
		// TODO ???
		this.semi = this;
	}

	void compress() {
		if (anc.anc != null) {
			anc.compress();
			if (anc.lab.semi.n < lab.semi.n)
				lab = anc.lab;
			anc = anc.anc;
		}
	}

	DominatorNode<V> eval() {
		if (anc == null)
			return this;
		else {
			compress();
			return lab;
		}
	}

	void link(DominatorNode<V> v) {
		anc = v;
	}
	
	final V node;
	DominatorNode<V> lab;
	DominatorNode<V> anc;
	DominatorNode<V> bucket;
	DominatorNode<V> semi;
	DominatorNode<V> dom;
	int n;
}