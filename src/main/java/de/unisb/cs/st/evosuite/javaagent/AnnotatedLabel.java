/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import org.objectweb.asm.Label;
import org.objectweb.asm.tree.LabelNode;

/**
 * @author fraser
 * 
 */
public class AnnotatedLabel extends Label {

	protected LabelNode parent;

	public AnnotatedLabel() {
		this.parent = null;
	}

	public AnnotatedLabel(LabelNode parent) {
		this.parent = parent;
	}

}
