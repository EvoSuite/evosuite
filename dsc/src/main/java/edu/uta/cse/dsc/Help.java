package edu.uta.cse.dsc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/*
    This class is taken and adapted from the DSC tool developed by Christoph Csallner.
    Link at :
    http://ranger.uta.edu/~csallner/dsc/index.html
 */

/**
 * Help text. Idea is to access JavaDoc kind of text comment at runtime.
 * 
 * @author csallner@uta.edu (Christoph Csallner)
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Help {
	String value();
}
