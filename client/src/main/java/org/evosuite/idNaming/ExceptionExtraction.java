package org.evosuite.idNaming;

import org.apache.commons.lang3.StringUtils;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.TryStatement;



public class ExceptionExtraction 
{
    private int total_exceptions;
    
    public ExceptionExtraction(String source){
    	total_exceptions=0;
    	//set_extractions(source);
    }
    public void set_extractions(String source) {
    	
    	ASTParser metparse = ASTParser.newParser(AST.JLS3);
    	metparse.setSource(source.toCharArray());
    	metparse.setKind(ASTParser.K_STATEMENTS);
    	Block block = (Block) metparse.createAST(null);
    	
    	block.accept(new ASTVisitor() {

    	     
    	      public boolean visit(CatchClause mycatch) {             
    	         total_exceptions++;
    	         return true;
    	      }

    	   });
    	
    }

	public int get_exceptions(){
    	return total_exceptions;
    }


}
