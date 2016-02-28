package org.dei.perla.cdt;

import java.io.StringReader;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.fpc.DataType;
import org.dei.perla.lang.parser.AttributeOrder;
import org.dei.perla.lang.parser.OnEmptySelection;
import org.dei.perla.lang.parser.ParseException;
import org.dei.perla.lang.parser.ParserAST;
import org.dei.perla.lang.parser.ParserContext;
import org.dei.perla.lang.parser.ast.*;
import org.dei.perla.lang.query.expression.Expression;
import org.dei.perla.lang.query.statement.RatePolicy;
import org.dei.perla.lang.query.statement.Statement;


/*	CREATE CONCEPT Normal 
	WHEN temperature > 35 AND pressure < 10
	EVALUATED ON 'SELECT temperature, pressure IF EXISTS (temperature, pressure)'
*/
public class WhenCondition {

	private final Expression when; 
	private final Map<String, DataType> attributes; 
	private final String evaluatedOn; 
	private Statement evaluatedOnStat; 
	
	public final static WhenCondition EMPTY = new WhenCondition(Collections.EMPTY_MAP, null, "", null);
	
	private WhenCondition(Map<String, DataType> attributes, Expression when, String evaluatedOn, Statement ast){
		this.when = when;
		this.evaluatedOnStat = ast;
		this.attributes = attributes; 
		this.evaluatedOn = evaluatedOn;
	}

	public Expression getWhen() {
		return when;
	}

	public Map<String, DataType> getAttributes() {
		return attributes;
	}

	public String getEvaluatedOnString() {
		return evaluatedOn;
	}
	
	public Statement getEvaluatedOn(){
		return evaluatedOnStat;
	}
	
	/*

	 */
	public static WhenCondition create(Map<String, DataType> attributes, ExpressionAST whenAST, 
		String evaluatedOn, ParserContext ctx, String src){
		StatementAST ast = null;
		if(attributes.isEmpty() && whenAST.equals(ConstantAST.TRUE)) { 
			ast = null;
		}
		else if(attributes.size() > 0 && evaluatedOn == null) { 
			ast = createDefaultEvaluatedOn(attributes);
		} else if(evaluatedOn != null){
	        ParserAST p = new ParserAST(new StringReader(evaluatedOn)); 
	        try {
	            ast = p.Statement(ctx);
	        } catch(ParseException e) {
	            ctx.addError("Parse exception in defining the EVALUATED ON clause of CONCEPT " + src);
	        }
	        if(!(ast instanceof SelectionStatementAST)) {
	        	ctx.addError("Query Perla defined in the EVALUATED ON clause of CONCEPT " + src 
	        			+ " must be a SELECTION STATEMENT");
	        	return null;
	        }
		}
		ParserContext compilationErrors = new ParserContext();
		Statement s = null;
		if( ast != null) {
			s = ast.compile(compilationErrors);
			if(compilationErrors.getErrorCount() > 0) {
	            ctx.addError(compilationErrors.getErrorDescription());
			}
		}
		Expression when = null;
		if(whenAST != null){
			compilationErrors = new ParserContext();
			when = whenAST.compile(DataType.BOOLEAN, compilationErrors, new AttributeOrder());
			if(compilationErrors.getErrorCount() > 0) {
	            ctx.addError(ctx.getErrorDescription());
			}
		}
		return new WhenCondition(attributes, when, evaluatedOn, s);
	}
	
	
	 /* It is advisable that the user specifies a EVALUATED ON clause associated to the 
	 * WHEN condition clause. Otherwise, the system creates a default EVALUATED ON clause of the type
	 * EVERY ONE 
	 * SELECT attributes (of the WHEN condition)
	 * SAMPLING EVERY 10 m
	 * EXECUTE IF REQUIRE(attributes)
	 */ 
	private static SelectionStatementAST createDefaultEvaluatedOn(Map<String, DataType> attributes){
		List<Attribute> atts = new ArrayList<Attribute>(attributes.size());
		List<FieldSelectionAST> fsl = new ArrayList<FieldSelectionAST>(attributes.size());
		for(Map.Entry<String, DataType> att : attributes.entrySet()) {
			atts.add(Attribute.create(att.getKey(), att.getValue()));
			fsl.add(new FieldSelectionAST(new AttributeReferenceAST(att.getKey(), att.getValue()), ConstantAST.NULL));
		}
	    WindowSizeAST every = new WindowSizeAST(ConstantAST.ONE);
	    List<IfEveryAST> ifeList = new ArrayList<IfEveryAST>();
	    EveryAST every2 = new EveryAST(new ConstantAST(10, DataType.INTEGER), ChronoUnit.MINUTES);
	    IfEveryAST ife = new IfEveryAST(ConstantAST.TRUE, every2);
        ifeList.add(ife);
	    SamplingAST sampling = new SamplingIfEveryAST(ifeList, RatePolicy.STRICT, RefreshAST.NEVER);
	    NodeSpecificationsAST specs = new NodeSpecificationsAST(atts); 
	    ExecutionConditionsAST ec = new ExecutionConditionsAST(ConstantAST.TRUE, specs, RefreshAST.NEVER); 
	    return new SelectionStatementAST(every, fsl, null, ConstantAST.TRUE, WindowSizeAST.ONE,
	    		OnEmptySelection.NOTHING, sampling, ConstantAST.TRUE, ec, null);
	}
	
	public String toString(){
		return "expression  EVALUATED ON " + evaluatedOn;
	}
}
