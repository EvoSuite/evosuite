/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import org.objectweb.asm.Label;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author fraser
 * 
 */
public class AnnotatedMethodNode extends MethodNode {

	/**
	 * @param access
	 * @param name
	 * @param desc
	 * @param signature
	 * @param exceptions
	 */
	public AnnotatedMethodNode(int access, String name, String desc, String signature,
	        String[] exceptions) {
		super(access, name, desc, signature, exceptions);
	}

	/**
	 * Returns the LabelNode corresponding to the given Label. Creates a new
	 * LabelNode if necessary. The default implementation of this method uses
	 * the {@link Label#info} field to store associations between labels and
	 * label nodes.
	 * 
	 * @param l
	 *            a Label.
	 * @return the LabelNode corresponding to l.
	 */
	@Override
	protected LabelNode getLabelNode(final Label l) {
		if (l instanceof AnnotatedLabel) {
			AnnotatedLabel al = (AnnotatedLabel) l;
			al.parent = new LabelNode(al);
			return al.parent;
		} else {
			if (!(l.info instanceof LabelNode)) {
				l.info = new LabelNode(l);
			}
			return (LabelNode) l.info;
		}
	}
}
