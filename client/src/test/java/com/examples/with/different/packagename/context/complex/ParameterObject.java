package com.examples.with.different.packagename.context.complex;

public class ParameterObject extends AParameterObject {
	int intero;
	String stringa;

	public ParameterObject() {
		intero = 0;
		stringa = "";
	}

	public int getIntero() {
		return intero;
	}

	public boolean isEnabled(){
		if(intero > 146 && intero < 157){
			return true;
		}
		return false;
	}
	
	public String getStringa() {
		return stringa;
	}

	public void setIntero(int intero) {
		this.intero = intero;
	}

	public void setStringa(String stringa) {
		this.stringa = stringa;
	}
}
