package contextTest;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dei.perla.cdt.CDT;
import org.dei.perla.cdt.Concept;
import org.dei.perla.cdt.WhenCondition;
import org.dei.perla.cdt.parser.CDTParser;
import org.dei.perla.cdt.parser.ParseException;
import org.dei.perla.context.ComposerManager;
import org.dei.perla.context.ConflictDetector;
import org.dei.perla.context.ContextDetector;
import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.fpc.DataType;
import org.dei.perla.lang.parser.AttributeOrder;
import org.dei.perla.lang.parser.FieldSelection;
import org.dei.perla.lang.parser.ParserContext;
import org.dei.perla.lang.parser.ast.*;
import org.dei.perla.lang.query.expression.AttributeReference;
import org.dei.perla.lang.query.expression.Bool;
import org.dei.perla.lang.query.expression.Comparison;
import org.dei.perla.lang.query.expression.Constant;
import org.dei.perla.lang.query.expression.Expression;
import org.dei.perla.lang.query.statement.*;
import org.junit.Test;


public class WhenCondTest {
	
	@Test
	public void evaluatedWhenTest() throws ParseException {
		 ParserContext ctx = new ParserContext();
		 CDTParser parser = new CDTParser(new StringReader(""
				 		+ " CREATE CONCEPT Hot WHEN getIdCompagnia() = 'ab' "
				 		+ " EVALUATED ON 'EVERY 1 m SELECT AVG(temp:integer, 1 m)" 
				 		+ " SAMPLING EVERY 5 s "
				 		+ " EXECUTE IF EXISTS(temp)'"));
		  Concept c = parser.CreateConcept(ctx, null);
		  WhenCondition w = c.getWhen();
		  assertTrue(w.getWhen() instanceof Comparison);
	      Statement ast = w.getEvaluatedOn();
	      boolean b = (ast instanceof SelectionStatement);
	      assertTrue(b);
	      
	}
	
	@Test
	public void between() throws ParseException {
		 ParserContext ctx = new ParserContext();
		 CDTParser parser = new CDTParser(new StringReader(""
				 		+ " CREATE CONCEPT Hot WHEN temperature:integer BETWEEN 12 AND 14"));
		  Concept c = parser.CreateConcept(ctx, null);
		  WhenCondition w = c.getWhen();
		  System.out.println(w.getAttributes());
	}
	
	@Test
	public void invalidEvaluatedTest() throws ParseException {
		 ParserContext ctx = new ParserContext();
		 CDTParser parser = new CDTParser(new StringReader(""
				 		+ " CREATE CONCEPT Hot WHEN temperature:integer BETWEEN 12 AND 14"
				 		+ " EVALUATED ON 'SET alarm = true ON 21'"));
		  Concept c = parser.CreateConcept(ctx, null);
		  WhenCondition w = c.getWhen();
		  assertNull(w);
	}
	
	@Test
	public void defaultEvaluatedTest() throws ParseException {
		 ParserContext ctx = new ParserContext();
		 CDTParser parser = new CDTParser(new StringReader(""
				 		+ " CREATE CONCEPT Hot WHEN temperature:integer > 35 AND pressure:float > 0.35"));
		  Concept c = parser.CreateConcept(ctx, null);
		  WhenCondition w = c.getWhen();
		  assertTrue(w.getWhen() instanceof Bool);
		  assertFalse(ctx.hasErrors());
	}
 //da finire
//	@Test
//	public void defaultEvaluatedOn() throws ParseException {
//	 ParserContext ctx = new ParserContext();
//	 CDTParser parser = new CDTParser(new StringReader(""
//		 		+ " CREATE CONCEPT Hot WHEN temperature:integer > 35 or pressure:integer between 12 and 20"));
//	  Concept c = parser.CreateConcept(ctx, null);
//	  WhenCondition w = c.getWhen();
//	  SelectionStatement s = (SelectionStatement) w.getEvaluatedOn(); 
//	  AttributeReference tempRef = new AttributeReference("temperature", DataType.INTEGER, 0);
//	  AttributeReference pressRef = new AttributeReference("pressure", DataType.INTEGER, 1);
//	  FieldSelection temp = new FieldSelection(tempRef, ConstantAST.NULL);
//	  FieldSelection press = new FieldSelection(pressRef, ConstantAST.NULL);
//	  List<Expression> fields = new ArrayList<>();
//	  fields.add(temp.getField());
//	  fields.add(press.getField());
//	  Object[] deff = new Object[2];
//	  deff[0] = temp.getDefault();
//	  deff[1] = press.getDefault();
//	  Select select = new Select(fields, WindowSize.ONE, null, Constant.TRUE, null);
//	  AttributeOrder selAtts = new AttributeOrder();
//	  selAtts.getIndex("temperature");
//	  selAtts.getIndex("pressure");
//		  List<Attribute> compAttList = selAtts.toList(ctx);
//	      if (!compAttList.contains(Attribute.TIMESTAMP)) {
//	            compAttList = new ArrayList<>(compAttList);
//	            compAttList.add(Attribute.TIMESTAMP);
//	        }
//	      IfEvery ife = new IfEvery(
//	                Constant.TRUE,
//	                Constant.create(10, DataType.INTEGER),
//	                ChronoUnit.MINUTES
//	        );
//	      	AttributeOrder ord = new AttributeOrder();
//	        SamplingIfEvery sampling = new SamplingIfEvery(
//	                Arrays.asList(new IfEvery[] { ife }),
//	                ord.toList(ctx),
//	                RatePolicy.STRICT,
//	                Refresh.NEVER
//	        );
//	        Attribute tempAtt = Attribute.create("temperature", DataType.INTEGER);
//	        Attribute pressAtt = Attribute.create("pressure", DataType.INTEGER);
//	        Set<Attribute> specs = new HashSet<>();
//	        specs.add(tempAtt);
//	        specs.add(pressAtt);
//
//	        List<Attribute> atts = new ArrayList<>();
//	        atts.add(tempAtt);
//	        atts.add(pressAtt);
//
//	        ExecutionConditions ec = new ExecutionConditions(specs, Constant.TRUE, atts, Refresh.NEVER);
//		   SelectionStatement ss = new
//				   SelectionStatement(select, compAttList, WindowSize.ONE, sampling, Constant.TRUE, ec, null);
//	}
	

}
