/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.idNaming;

import org.eclipse.jdt.core.dom.*;



public class ExceptionExtraction 
{
    private int total_exceptions;
    
    public ExceptionExtraction(String source){
    	total_exceptions=0;
    	set_extractions(source);
    }
    public void set_extractions(String source) {
    	source=source.replace("@Test", "");
    	source="public class A{"+source+"}";
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
