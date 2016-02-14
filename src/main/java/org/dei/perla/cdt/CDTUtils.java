package org.dei.perla.cdt;

public final class CDTUtils {

	private static final CDT cdt = CDT.getCDT();
	
	public static Concept getConcept(String dimension, String concept){
		return cdt.getConceptOfDim(dimension, concept);
	}
	
	public static boolean isDimensionConceptValid(String dimension, String concept){
		return cdt.containsConceptOfDim(dimension, concept);
	}
	
	public static boolean isDimensionAttValid(String dimension, String attribute){
		return cdt.containsAttributeOfDim(dimension, attribute);
	}
	
	public static Concept getConceptByAttr(String dimension, String attribute){
		Dimension d = cdt.getDimByName(dimension);
		return d.getConceptContainingAtt(attribute);
	}
}
