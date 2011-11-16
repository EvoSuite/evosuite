package de.unisb.cs.st.evosuite.ma.parser;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;

/**
 * @author Yury Pavlov
 *
 */
public class AstVisitor extends ASTVisitor{

	public boolean visit(Block node) {
		System.out.println("Visit node " + node);
		return true;
	}
	

}
