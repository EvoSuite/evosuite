/**
 * 
 */
package de.unisb.cs.st.evosuite.ma.parser;

import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;


/**
 * @author Yury Pavlov
 * 
 */
public class SEParser {

	public AstVisitor visitString(String source) {

		ASTParser parser = ASTParser.newParser(AST.JLS3);

		@SuppressWarnings("unchecked")
		Map<String, String> options = JavaCore.getOptions();

		parser.setCompilerOptions(options);

		parser.setResolveBindings(false);

		parser.setStatementsRecovery(false);

		parser.setBindingsRecovery(false);

		parser.setSource(source.toCharArray());

		parser.setIgnoreMethodBodies(false);

		CompilationUnit ast = (CompilationUnit) parser.createAST(null);

		// AstVisitor extends org.eclipse.jdt.core.dom.ASTVisitor

		AstVisitor visitor = new AstVisitor();

		ast.accept(visitor);

		return visitor;

	}

}
