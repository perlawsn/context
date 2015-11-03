package org.dei.perla.cdt;

import java.io.StringReader;

import org.dei.perla.lang.parser.ParseException;
import org.dei.perla.lang.parser.ParserAST;
import org.dei.perla.lang.parser.ParserContext;
import org.dei.perla.lang.parser.ast.SelectionStatementAST;
import org.dei.perla.lang.parser.ast.StatementAST;

public class QueryEvaluatedOn implements EvaluatedOn{
	
	private final String evaluatedOn;
	public StatementAST ast;
	
	private QueryEvaluatedOn(String evaluatedOn, StatementAST ast){
		this.evaluatedOn = evaluatedOn;
		this.ast = ast;
	}

	@Override
	public String getEvaluatedOn() {
		return evaluatedOn;
	}
	
	public StatementAST getQueryEvaluatedOn(){
		return ast;
	}

	
	public static QueryEvaluatedOn create(String evaluatedOn, ParserContext ctx, String src){
		StatementAST s = null;
        ParserAST p = new ParserAST(new StringReader(evaluatedOn));
        try {
            s = p.Statement(ctx);
        } catch(ParseException e) {
            ctx.addError("Parse exception in defining the EVALUATED ON clause "
            		+ "of ATTRIBUTE " + src);
        }
        if(!(s instanceof SelectionStatementAST))
        	ctx.addError("Query Perla defined in the EVALUATED ON clause of ATTRIBUTE " + src 
        			+ " must be a SELECTION STATEMENT");
		return new QueryEvaluatedOn(evaluatedOn, s);
	}

}
