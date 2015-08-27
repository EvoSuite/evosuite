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

import org.apache.commons.lang.WordUtils;
import org.evosuite.testcase.TestCase;

import java.util.List;

public abstract class DistinguishNames {
	protected String checkAssertions(TestCase tc, String methodName){	
		AssertionExtraction assertions = new AssertionExtraction(tc.toCode());
		List<String> assertionContent = assertions.getStatements();
		if(assertionContent.size()>0){
			methodName="test";
			for(String assertion: assertionContent){
				String statement = "";
				if(assertion.split(",").length > 1){
					if(assertion.split(",").length == 2){
						statement = assertion.substring(assertion.indexOf(",")+1, assertion.lastIndexOf(")"));
					}else {						
						statement = assertion.substring(assertion.indexOf(",")+1, assertion.lastIndexOf(","));							
					}
					if(statement.contains("(") && statement.contains("") && statement.contains(".")){
						//testChecksMethod
						methodName+= "_Checks"+WordUtils.capitalize(statement.substring(statement.indexOf(".")+1, statement.indexOf("(")))+
								"With"+findVarInTest(tc,assertion.substring(assertion.indexOf("(")+1, assertion.indexOf(",")));
					} else {
						if(statement.contains("(") && statement.contains(")") && !statement.contains(".")){
							methodName+= "_Checks"+WordUtils.capitalize(statement.substring(0, statement.indexOf("(")))+
									"With"+findVarInTest(tc,assertion.substring(assertion.indexOf("(")+1, assertion.indexOf(",")));
						}else{
							//testChecksMethodWithValue
							methodName+= "_Checks"+ findVarInTest(tc, statement)+"With"+
									findVarInTest(tc,assertion.substring(assertion.indexOf("(")+1, assertion.indexOf(",")));
						}					
					}				
				}else {
					//testChecksMethod
					statement = assertion.substring(assertion.indexOf("(")+1, assertion.lastIndexOf(")"));
					if(statement.contains("(") && statement.contains(")") && !statement.contains(".")){
						methodName+= "_Checks"+
								WordUtils.capitalize(statement.substring(0, statement.lastIndexOf("(")));
					} else {
						if(statement.contains("(") && statement.contains(")") && statement.contains(".")){
							methodName+= "_Checks"+
									WordUtils.capitalize(statement.substring(statement.indexOf(".")+1, statement.lastIndexOf("(")));
						} else {
							methodName+= "_Checks"+
									findVarInTest(tc, statement);
						}
					}
					
				}
				
			}
		
		}
		methodName = methodName.replace("<","").replace(">","").replace("(","").replace(")","").replace("-", "Minus").replace("+", "Plus");
		return methodName;
	}

	protected String findVarInTest(TestCase tc, String variable){
		String testToString = tc.toCode();
		String[] lines = testToString.split("\n");
		String method=variable.trim();
		IdentifierExtraction identifiers = new IdentifierExtraction(testToString);
		for(String s: identifiers.get_identifier_names()){
			if(s.equals(variable.trim())){
				int lineNo=identifiers.get_identifier_lines().get(variable.trim());
				if(lines[lineNo-2].contains(".")){
					method = WordUtils.capitalize(lines[lineNo-2].substring(lines[lineNo-2].indexOf(".")+1, lines[lineNo-2].indexOf("(")).trim());
				}else {
					if(lines[lineNo-2].contains(" new ")){
						method = lines[lineNo-2].substring(lines[lineNo-2].indexOf(" new ")+5, lines[lineNo-2].indexOf("(")).trim();
					}
				}
			}
		}
		
		
		return method;
		
	}
	
	public static String translateBranch(String option){
		String translate="";
		if(option.contains("IFGE")){
			translate = "BranchGE";
		} else {
			if(option.contains("IFLE")){
				translate = "BranchLE";
			} else {
				if(option.contains("IFGT")){
					translate = "BranchGT";
				} else{
					if(option.contains("IFLT")){
						translate = "BranchLT";
					} else{
						if(option.contains("IFEQ")){
							translate = "BranchEQ";
						} else {
							if(option.contains("ICMPGE")){
								translate = "BranchCompareGE";
							} else {
								if(option.contains("ICMPLE")){
									translate = "BranchCompareLE";
								} else {
									if(option.contains("ICMPGT")){
										translate = "BranchCompareGT";
									} else {
										if(option.contains("ICMPLT")){
											translate = "BranchCompareLT";
										} else {
											if(option.contains("ICMPEQ")){
												translate = "BranchCompareEQ";
											} else {
												if(option.contains("IFNONNULL")){
													translate = "BranchNoNull";
												}else {
													if(option.contains("IFNNULL")){
														translate = "BranchNull";
													}
														else {
															if(option.contains("ICMPNE")){
																translate = "BranchCompareNE";
															}														
														}
													}
												}
											}
										}	
									}
								}
							}
						}
					}
				}
			}	
		return translate;
	}
}
