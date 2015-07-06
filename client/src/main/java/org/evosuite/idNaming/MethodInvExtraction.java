package org.evosuite.idNaming;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class MethodInvExtraction 
{
    	
	private String[] method_names;
	
	private int counter;
	
	private String[] unique_methods;
	
	
    public MethodInvExtraction() {
        
    }

    /*
    public void setMethods(String str) {
        String source="";
		String[] lines=str.split("\n");

		if(lines.length>1&&str.substring(0,str.indexOf("\n")).contains("public")||lines.length>1&&str.substring(0,str.indexOf("\n")).contains("private")){
			source="public class A{"+str+"  }";
			} else {
			source="public class A{ public void A (){ \n"+str+" } }";
			}

		//	method_names=new String[lines.length*2];
			final List<String> method_names= new ArrayList<String>();
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(source.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        final List<String> assertionMethods = new ArrayList<String>(Arrays.asList(AssertionExtraction.ASSERTION_TYPES));
        cu.accept(new ASTVisitor() {
        	@Override
            public boolean visit(MethodInvocation node) {
        		if(assertionMethods.contains(node.getName().toString())) {
        			return true;
        		}
				method_names.add(node.getName().toString());
				counter++;

                return true;
            }
        });

    }
    */
    public String[] get_method_names(){
    	return method_names;
    }

    
}
