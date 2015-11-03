package org.dei.perla.context;


import org.dei.perla.cdt.CDT;

/*
 * 
 */
public final class ContextUtils {

	public static boolean isDimensionConceptValid(String dimension, String concept){
		CDT cdt = CDT.getCDT();
		return cdt.containsConceptOfDim(dimension, concept);
	}
	
	public static boolean isDimensionAttValid(String dimension, String attribute){
		CDT cdt = CDT.getCDT();
		return cdt.containsConceptOfDim(dimension, attribute);
	}

}
