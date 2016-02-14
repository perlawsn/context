package org.dei.perla.cdt;

import org.dei.perla.lang.parser.ParserContext;

/* 
 * CREATE DIMENSION Citta
 * CREATE ATTRIBUTE Nome 
 * EVALUATED ON PerlaQuery / static 
 */

public class CreateAttr {

	private final String name;
	private EvaluatedOn eo;
	
	public static CreateAttr EMPTY  = new CreateAttr(" ", null);
	
	public CreateAttr(String name, EvaluatedOn eo){
		this.name = name;
		this.eo = eo;
	}
	
	public String getName() {
		return name;
	}
	
	public EvaluatedOn getEvaluatedOn(){
		return eo;
	}
	
	public static CreateAttr createWithQuery(String attName, String evaluatedOn, ParserContext ctx, String src){
		EvaluatedOn e = QueryEvaluatedOn.create(attName, evaluatedOn, ctx, src);
        return new CreateAttr(attName, e);
	}
	
	public static CreateAttr createWithMethod(String attName, String evaluatedOn, ParserContext ctx, String src){
		EvaluatedOn e = FunctionEvaluatedOn.create(attName, evaluatedOn, ctx, src);
        return new CreateAttr(attName, e);
	}
	
	


	
	
	
}
