package org.dei.perla.cdt;

import java.io.StringReader;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.fpc.DataType;
import org.dei.perla.lang.parser.OnEmptySelection;
import org.dei.perla.lang.parser.ParseException;
import org.dei.perla.lang.parser.ParserAST;
import org.dei.perla.lang.parser.ParserContext;
import org.dei.perla.lang.parser.ast.*;
import org.dei.perla.lang.query.statement.RatePolicy;
import org.dei.perla.lang.query.statement.WindowSize;


/*	CREATE CONCEPT Normal 
	WHEN temperature > 35 AND pressure < 10
	EVALUATED ON 'SELECT temperature, pressure IF EXISTS (temperature, pressure)'
*/
public class WhenCondition {

	private final ExpressionAST when; // temperature > 35
	private final List<String> attributes; // temperature, pressure
	private final String evaluatedOn; //
	private StatementAST ast; 
	
	
	private WhenCondition(List<String> atts, ExpressionAST when, String evaluatedOn, StatementAST ast){
		this.when = when;
		this.ast = ast;
		attributes = atts; 
		this.evaluatedOn = evaluatedOn;
	}

	public ExpressionAST getWhen() {
		return when;
	}

	public List<String> getAttributes() {
		return attributes;
	}

	public String getEvaluatedOnString() {
		return evaluatedOn;
	}
	
	public StatementAST getEvaluatedOn(){
		return ast;
	}
	
	/*

	 */
	public static WhenCondition create(List<String> attString, ExpressionAST when, 
		String evaluatedOn, ParserContext ctx, String src){
		StatementAST s = null;
		if(attString != null && evaluatedOn == null) {
			s = createDefaultEvaluatedOn(attString);
		}	
		else{
	        ParserAST p = new ParserAST(new StringReader(evaluatedOn));
	        try {
	            s = p.Statement(ctx);
	        } catch(ParseException e) {
	            ctx.addError("Parse exception in defining the EVALUATED ON clause of CONCEPT " + src);
	        }
	        if(!(s instanceof SelectionStatementAST))
	        	ctx.addError("Query Perla defined in the EVALUATED ON clause of CONCEPT " + src 
	        			+ " must be a SELECTION STATEMENT");
		}
		return new WhenCondition(attString, when, evaluatedOn, s);
	}
	
	 /* It is advisable that the user specifies a EVALUATED ON clause associated to the 
	 * WHEN condition clause. Otherwise, the system creates a default EVALUATED ON clause pf the type
	 * EVERY ONE 
	 * SELECT attributes (of the WHEN condition)
	 * SAMPLING EVERY 1 m
	 * EXECUTE IF REQUIRE(attributes)
	 * TERMINATE AFTER 1 SELECTIONS
	 */ 
	private static SelectionStatementAST createDefaultEvaluatedOn(List<String> attString){
		List<Attribute> atts = attString.stream().
				map(name -> Attribute.create(name, DataType.ANY)).collect(Collectors.toList());
		
	    WindowSizeAST every = new WindowSizeAST(null, ConstantAST.ONE);
	    ExpressionAST def = ConstantAST.NULL;
	    List<FieldSelectionAST> fsl = attString.stream()
	    		.map(name -> new FieldSelectionAST(new AttributeReferenceAST(name,DataType.ANY),def))
	    		.collect(Collectors.toList()); 
	    
	    List<IfEveryAST> ifeList = new ArrayList<IfEveryAST>();
	    EveryAST every2 = new EveryAST(ConstantAST.ONE, ChronoUnit.MINUTES);
	    IfEveryAST ife = new IfEveryAST(ConstantAST.TRUE, every2);
        ifeList.add(ife);
	    SamplingAST sampling = new SamplingIfEveryAST(ifeList, RatePolicy.STRICT, RefreshAST.NEVER);
	    NodeSpecificationsAST specs = new NodeSpecificationsAST(atts); 
	    ExecutionConditionsAST ec = 
	    		new ExecutionConditionsAST(ConstantAST.TRUE, specs, RefreshAST.NEVER); 
	    return new SelectionStatementAST(every, fsl, null, ConstantAST.TRUE, WindowSizeAST.ONE,
	    		OnEmptySelection.NOTHING, sampling, ConstantAST.TRUE, ec, WindowSizeAST.ONE);
	}
	
	public String toString(){
		return "expression  EVALUATED ON " + evaluatedOn;
	}
}
