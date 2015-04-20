package org.evosuite.eclipse.replace;

public class TypeMatcher {
	
	public int RewardChooser(String originalType, String replaceType){
		int reward=5;
		if(originalType.equals(replaceType)){
			if((originalType.equals("literal") && replaceType.equals("var")) ||
					(originalType.equals("var") && replaceType.equals("literal")))
				reward= -3;
			else
				reward= -5;

				
		}
		return reward;
	}
	
	
}
