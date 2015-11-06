package org.dei.perla.cdt;

public final class CDTUtils {

	private static final CDT cdt = CDT.getCDT();
	
	public static Concept getConcept(String dimension, String concept){
		return cdt.getConceptOfDim(dimension, concept);
	}
}
