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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.*;

import java.util.*;

public class IdentifierExtraction 
{
	private int total_identifiers;
	private int max_identifiers_line;
	private double avr_identifiers;
	
	private int total_single_identifiers;
	private int max_single_identifiers;
	
	private String source_code;

	private int[] single_identifier;

	private int[] max_identifiers;
	
	private String max_identifier_length;

	private int total_identifier_length;
	private int max_identifier_length_line;
	private double avr_identifier_length;	
	
	private int total_unique_identifiers;
	private int max_unique_identifiers;
	private double avr_unique_identifiers;	
	
	private double total_identifier_ratio;
	
	private int totalUnusedIdentifiers;
	
	private Set<String> identifierNames ;
	
	private Map<String, Integer> idAndLine ;


	public IdentifierExtraction(String s) {
		total_identifiers = 0;
		single_identifier = null;
		max_identifiers = null;
		max_identifier_length = "";
		total_identifier_length = 0;
		max_identifier_length_line = 0;
		max_identifiers_line = 0;
		avr_identifiers = 0.0;
		avr_identifier_length = 0.0;
		max_single_identifiers = 0;
		total_single_identifiers = 0;
		total_unique_identifiers=0;
		max_unique_identifiers=0;
		avr_unique_identifiers=0.0;
		total_identifier_ratio=0.0;
		totalUnusedIdentifiers=0;
		identifierNames = new LinkedHashSet<String>();
		idAndLine =  new HashMap<String, Integer>();
		this.setIdentifiers(s);
	}

	private void setIdentifiers(String source) {
		source_code = source;
		String [] lines=source_code.split("\n");
		if(lines.length>1&&source.substring(0,source.indexOf("\n")).contains("public")||lines.length>1&&source.substring(0,source.indexOf("\n")).contains("private")){
			source="public class A{"+source+"  }";
		} else {
			source="public class A{ public void A (){ \n"+source+" } }";
		}
		source_code = source_code.toString().replaceAll("[\\p{Punct}&&[^_]]"," ");
		lines=source_code.split("\n");

		single_identifier = new int[lines.length];
		max_identifiers = new int[lines.length];

		

		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(source.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		cu.accept(new ASTVisitor() {
			public boolean visit(VariableDeclarationFragment node) {
				SimpleName name = node.getName();
				identifierNames.add(name.getIdentifier());
				idAndLine.put(name.getIdentifier(), cu.getLineNumber(name.getStartPosition()));
				return true; // do not continue
			}

		}); 
		//total and max single identifiers 
		
		int [] occurance_single_iden= new int[identifierNames.size()];
		int x=0;
		for(String identifier: identifierNames){
			for (int z=0; z<lines.length; z++){
				String[] words=lines[z].split(" ");
				for(String word : words){
					if(word.equals(identifier)){
						single_identifier[z]++;
					}
				}
							
			}			
			
			x++;
			single_identifier=new int[lines.length];
		}
		occurance_single_iden= new int[identifierNames.size()];
		x=0;
		for(String identifier: identifierNames){		
			String[] words=source_code.split(" ");
			for(String word : words){
				if(word.trim().equals(identifier)){
					occurance_single_iden[x]++;
				}
			}
			
			x++;
		}		
		
		//total and max unique identifiers		
		total_unique_identifiers = identifierNames.size();
		int[] max_unique_ident=new int [lines.length];
		String[] tokens;
		Set<String> uniqueWords;
		for (int q=0;q<lines.length;q++){
			tokens=lines[q].split(" ");
			uniqueWords = new HashSet<String>();
			for (String token : tokens) {
				uniqueWords.add(token);
			}
			for (String str : identifierNames){
				for(String token: uniqueWords){
					if (token.equals(str)){
						max_unique_ident[q]++;
					}

				}
			}
		}
		
//unused identifiers
		 for (String str : identifierNames) {
	        	if(str!=null){
	        		if(StringUtils.countMatches(source_code," "+str+" ")==1){
	        			if(source.contains(str+" =")||source.contains(str+"=")){
	        				totalUnusedIdentifiers++;
	        			}
	        		}
	        	}
	        	
	        }
		 
		String [] words= null;
		for(String identifierName : identifierNames) {
			total_identifier_length += identifierName.length();
			if (identifierName.length() > max_identifier_length_line) {
				max_identifier_length_line = identifierName.length();
			}
			for (int i = 0; i < lines.length; i++) {
				words = lines[i].split(" ");
				for (int j = 0; j < words.length; j++) {
					if (identifierName.equals(words[j])) {
						max_identifiers[i]++;
						total_identifiers++;
						max_identifier_length = max_identifier_length+identifierName;

						
					}

				}
			}
		}
		//max occurence of an identifier in a line
		
		
		avr_identifiers=(double)total_identifiers/(double)lines.length;
		avr_identifier_length=(double)max_identifier_length.length()/(double)lines.length;
		
		total_identifier_length=max_identifier_length.length();
		avr_unique_identifiers=(double)total_unique_identifiers/(double)lines.length;
		if(total_identifiers!=0){
			total_identifier_ratio=(double)total_unique_identifiers/(double)total_identifiers;
		} else{
			total_identifier_ratio=0;
		}
	}

	public int get_total_identifiers(){
		return total_identifiers;		
	}
	public int get_max_identifiers(){
		return max_identifiers_line;
	}
	public double get_av_identifiers(){
		return avr_identifiers;
	}
	public int get_total_identifiers_length(){
		return total_identifier_length;
	}
	public int get_max_identifiers_length(){
		return max_identifier_length_line;
	}
	public double get_av_identifiers_length(){
		return avr_identifier_length;
	}
	public int get_max_single_iden(){
		return max_single_identifiers;
	}
	public int get_total_single_iden(){
		return total_single_identifiers;
	}
	public int get_total_unique_iden(){
		return total_unique_identifiers;
	}
	public int get_max_unique_iden(){
		return max_unique_identifiers;
	}	
	public double get_avr_unique_iden(){
		return avr_unique_identifiers;
	}
	public double get_identifier_ratio(){
		return total_identifier_ratio;
	}
	public int get_total_unused_identifiers(){
		return totalUnusedIdentifiers;
	}
	public Map<String,Integer> get_identifier_lines(){
		return idAndLine;
	}
	public Set<String> get_identifier_names(){
		return identifierNames;
	}
}
