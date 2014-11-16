package org.evosuite.eclipse.quickfixes;

import java.util.ArrayList;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class ClassMethodVisitor extends ASTVisitor {
	private ArrayList<MethodDeclaration> methods;
	
	public ClassMethodVisitor(){
		super();
		methods = new ArrayList<MethodDeclaration>();
	}
	public boolean visit(MethodDeclaration n) {
		methods.add(n);
		return true;
	}
	
	public ArrayList<MethodDeclaration> getMethods(){
		return methods;
	}

}
