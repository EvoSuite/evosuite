/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.eclipse.replace;
import java.io.*;
import java.util.Collection;
import java.util.HashMap;

public class TokenSlicer {
	private HashMap tokenRecord= new HashMap();	//.get(key)
	private HashMap typeRecord= new HashMap();
	private String[] keywords= 
		{"abstract", "assert", "boolean", "break", "byte", "case", "catch",
			"char", "class", "const", "continue", "default", "do", "double", "else",
			"enum", "extends", "final", "finally", "float", "for", "goto", "if",
			"implements", "import", "instanceof", "int", "interface", "long", "native",
			"new", "package", "private", "protected", "public", "return", "short",
			"static", "strictfp", "super"
			};
	private Collection<String> varNames;
	private Collection<String> classNames;
	private String 	testcaseWord;
	private String originalString;
	
	public static void main(String args[]){
		TokenSlicer ts= new TokenSlicer();
		ts.stringFilter("Mary1 had 1 (little) class.method new \n abc //yo\r a=20; ");
	}
	
	public boolean stringFilter(String str){
		boolean sliced= false;
	try{
		StreamTokenizer tokenizer = new StreamTokenizer( new StringReader(str)); //System.out.println("**"+ str);
		//tokenizer.parseNumbers();
		//tokenizer.wordChars('(','(');
		//tokenizer.wordChars(')',')');
		//tokenizer.wordChars(';',';');
		//tokenizer.wordChars('\n','\n');
		//tokenizer.eolIsSignificant(true);
		tokenizer.slashStarComments(true);
		tokenizer.slashSlashComments(true);
		//tokenizer.commenChar(
		//tokenizer.whitespaceChars('\n','\n');			//blank specific char '.'
		int token;
		char ch;
		int tokenNum=0;
		while( (token= tokenizer.nextToken() ) != StreamTokenizer.TT_EOF){
			switch (token){
			//switch (tokenizer.ttype){
				case StreamTokenizer.TT_WORD:
					String word = tokenizer.sval;
				
					if (word.contains(".")){
						String strings[]= word.split("\\.",-1); 
						//int i=0;
						//for(; i<strings.length; i++, tokenNum++){
						for(int i=0; i<strings.length; i++){
							//tokenRecord.put(tokenNum, strings[i]);
							//System.out.println(strings.length);
							if(i==0){
								tokenRecord.put(tokenNum, strings[i]); //System.out.println("inside " + token + "number "+tokenNum );
								//typeRecord.put(tokenNum, "classObject");
								if(isClass(strings[i])){
									typeRecord.put(tokenNum, "class");
								}else if(isVariable(strings[i])){
									typeRecord.put(tokenNum, "var");
								}else 
									typeRecord.put(tokenNum, "literal");
								tokenNum++;
							}else{
								tokenRecord.put(tokenNum,".");	//System.out.println("inside " + token + "number "+tokenNum );
								typeRecord.put(tokenNum, "spChar");
								tokenNum++;
								tokenRecord.put(tokenNum, strings[i]); //System.out.println("inside " + token + "number "+tokenNum );
								typeRecord.put(tokenNum, "literal");
							}
						}
					}else{
						tokenRecord.put(tokenNum, word);
						if(isClass(word)){
							typeRecord.put(tokenNum, "class");
						}else if(isVariable(word)){
							typeRecord.put(tokenNum, "var");
						}else if(isKeywords(word))
							typeRecord.put(tokenNum, "keyword");
						else 
							typeRecord.put(tokenNum, "literal");
						//instance of Boolean
					}
					break;  
				case StreamTokenizer.TT_NUMBER:
					double num = tokenizer.nval;
					 tokenRecord.put(tokenNum, num+"");
					 typeRecord.put(tokenNum, "literal");
					break;
				/*case StreamTokenizer.TT_EOL:
					 ch= 'n'; 
					 tokenRecord.put(tokenNum, ch+"");
					 typeRecord.put(tokenNum, "spCharNewLine");
					break;*/
				default:
					break;
				
		
			}// switch
			
			if (token!=StreamTokenizer.TT_WORD){
			//collect some common ASCII characters
			for(int i=33; i<=64; i++){
				if(token==i){
					if(i==34){
						//System.out.println("\n"+tokenizer.sval);
						 //ch= (char)i; 
						 tokenRecord.put(tokenNum, "\""+ tokenizer.sval +"\""); //System.out.println("a");
						 typeRecord.put(tokenNum, "literal");
					}else{
					 ch= (char)i; 
					 tokenRecord.put(tokenNum, ch+""); //System.out.println("inside " +ch);
					 typeRecord.put(tokenNum, "spChar");
					}
				}
					
			}
			}//if
				
			for(int i=91; i<=96; i++){
				if(token==i){
					 ch= (char)i; 
					 tokenRecord.put(tokenNum, ch+""); //System.out.println("inside " +ch);
					 typeRecord.put(tokenNum, "spChar");
					}
		
			}
			
			for(int i=123; i<=126; i++){
				if(token==i){
					 ch= (char)i; 
					 tokenRecord.put(tokenNum, ch+""); //System.out.println("inside " +ch);
					 typeRecord.put(tokenNum, "spChar");
					
				}
					
			}
			tokenNum++;
		}//while
		sliced=true;
		
	}catch(Exception ex){
		System.err.print(ex);
	}
	
	
	return sliced;
	}//strFil
	
	private boolean isKeywords(String uncertainWd){
		boolean keywordType = false;
		for (String keyWd: keywords)
			if (uncertainWd.equals(keyWd))
				keywordType= true;
		
		return keywordType;
				
	}
	
	private boolean isClass(String uncertainWd){
		boolean classType = false;
		for (String classWd: classNames)
			if (uncertainWd.equals(classWd))
				classType= true;
		
		return classType;
	}
	
	private boolean isVariable(String uncertainWd){
		boolean varType = false;
		for (String varWd: varNames)
			if (uncertainWd.equals(varWd))
				varType= true;
		
		return varType;
	}
	
	public TokenSlicer(String testcaseWord, Collection<String> varNames, Collection<String> classNames){
		this.testcaseWord= testcaseWord;
		this.varNames= varNames;
		this.classNames= classNames;
		stringFilter(testcaseWord);
	}
	
	//for testing
	public TokenSlicer(){

	}
	
	public HashMap getTokenRecord(){
		return tokenRecord;
	}
	
	public HashMap getTypeRecord(){
		return typeRecord;
	}

	
	
}
