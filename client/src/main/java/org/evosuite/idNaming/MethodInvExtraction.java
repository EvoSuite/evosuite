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

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class MethodInvExtraction
{
	private static final Logger logger = LoggerFactory.getLogger(MethodInvExtraction.class);

    private int total_method_invocations;

    private double av_method_invocations;

	private int max_method_invocation;
	
	private List<String>  method_names;
	
	private int counter;
	
	private String[] unique_methods;
	
    public MethodInvExtraction(String s) {
        total_method_invocations = 0;
        av_method_invocations = 0.0;
        counter=0;		
        method_names = new ArrayList<String>();
        setMethods(s);
    }

    private void setMethods(String str) {
        String source="";
		String[] lines=str.split("\n");
		
		if(lines.length>1&&str.substring(0,str.indexOf("\n")).contains("public")||lines.length>1&&str.substring(0,str.indexOf("\n")).contains("private")){
			source="public class A{"+str+"  }";
			} else {
			source="public class A{ public void A (){ \n"+str+" } }";
			}

			//method_names=new String[lines.length*2];
			
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
                total_method_invocations++;
                method_names.add(node.getName().toString()); 
				counter++;
				
                return true;
            }
        });
		dist_methods( method_names.toArray(new String [counter]));
		int[] count_max=new int [lines.length];
		String []word=null;
		String [] wordWithPeriod=null;
		
		for (int i=0; i<lines.length;i++){
			for (Object method : dist_methods( method_names.toArray(new String [counter]))){
				if(method!=null){	
					lines[i]=lines[i].trim().replaceAll("[\\p{Punct}&&[^_]]"," ");
					String[] words=lines[i].split(" ");
					for(String wordName: words){
						if(method.toString().equals(wordName)){
							//		logger.debug(method.toString());
									count_max[i]++;
								}
					}			
					
				}
			}
		}
		//logger.debug(Arrays.toString(count_max));
		//max_method_invocation=find_max(count_max);
        av_method_invocations = (double) total_method_invocations / (double) lines.length;
    }

    public int get_methods() {
        return total_method_invocations;
    }

    public double get_av_methodInv() {
        return av_method_invocations;
    }
	public int get_max_methods(){
		return max_method_invocation;
	}
	public static Set dist_methods(String[] method_names){
			 Set tempSet = new HashSet();
		        for (String str : method_names) {
		        	if(str!=null){
		        		if (!tempSet.add(str)) {
		              
		        		}
		        	}
		        }
		        
		        return tempSet;
		}

	public String[] get_method_names() {
		
		return method_names.toArray(new String [counter]);
	}
}
