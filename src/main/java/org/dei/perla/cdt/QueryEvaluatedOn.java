package org.dei.perla.cdt;

import java.io.StringReader;

import org.dei.perla.lang.parser.ParseException;
import org.dei.perla.lang.parser.ParserAST;
import org.dei.perla.lang.parser.ParserContext;
import org.dei.perla.lang.parser.ast.SelectionStatementAST;
import org.dei.perla.lang.parser.ast.StatementAST;
import org.dei.perla.lang.query.statement.Statement;

public class QueryEvaluatedOn implements EvaluatedOn{
	
	private final String evaluatedOn;
	public Statement evaluatedOnStat;
	
	private QueryEvaluatedOn(String evaluatedOn, Statement evaluatedOnStat){
		this.evaluatedOn = evaluatedOn;
		this.evaluatedOnStat = evaluatedOnStat;
	}

	@Override
	public String getStringEvaluatedOn() {
		return evaluatedOn;
	}
	
	public Statement getQueryEvaluatedOn(){
		return evaluatedOnStat;
	}

	
	public static QueryEvaluatedOn create(String attName, String evaluatedOn, ParserContext ctx, String src){
		StatementAST ast = null;
        ParserAST p = new ParserAST(new StringReader(evaluatedOn));
        try {
            ast = p.Statement(ctx);
        } catch(ParseException e) {
        	System.out.println(ctx.getErrorDescription());
            ctx.addError("Parse exception in defining the EVALUATED ON clause "
            		+ "of ATTRIBUTE " + attName + " OF " + src);
            return null;
        }
        if(!(ast instanceof SelectionStatementAST)) {
        	ctx.addError("Query Perla defined in the EVALUATED ON clause of ATTRIBUTE " + attName + " OF " 
        			+ src + " must be a SELECTION STATEMENT");
        	return null;
        }
		ParserContext compilationErrors = new ParserContext();
		Statement s = ast.compile(compilationErrors);
		if(compilationErrors.getErrorCount() > 0) {
            ctx.addError(compilationErrors.getErrorDescription());
		}
		return new QueryEvaluatedOn(evaluatedOn, s);
	}

}
