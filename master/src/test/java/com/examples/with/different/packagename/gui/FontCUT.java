package com.examples.with.different.packagename.gui;

import java.awt.Font;


public class FontCUT {

	public boolean foo(int x){

		Font font = new Font("SansSerif", Font.PLAIN, 12);
		font.toString();
		
		if(x>0){
			return true;
		}else {
			return false;
		}
	}
}
