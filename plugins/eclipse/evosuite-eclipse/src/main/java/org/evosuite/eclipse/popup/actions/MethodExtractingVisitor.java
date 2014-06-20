/**
 * 
 */
package org.evosuite.eclipse.popup.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * @author Gordon Fraser
 * 
 */
public class MethodExtractingVisitor extends ASTVisitor {

	public String result = "";
	
	private List<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MethodDeclaration)
	 */
	@Override
	public boolean visit(MethodDeclaration node) {
		result += node.toString();
		methods.add(node);
		System.out.println("Adding method: "+node.toString());
		return super.visit(node);
	}
	
	public List<MethodDeclaration> getMethods() {
		return methods;
	}
}
