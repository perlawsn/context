package contextTest;
import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.dei.perla.cdt.Concept;
import org.dei.perla.cdt.PartialComponent;
import org.dei.perla.cdt.WhenCondition;
import org.dei.perla.cdt.parser.CDTParser;
import org.dei.perla.cdt.parser.ParseException;
import org.dei.perla.lang.parser.ParserContext;
import org.dei.perla.lang.parser.ast.BoolAST;
import org.dei.perla.lang.parser.ast.SelectionStatementAST;
import org.dei.perla.lang.parser.ast.SetStatementAST;
import org.dei.perla.lang.query.expression.Bool;
import org.dei.perla.lang.query.expression.Expression;
import org.dei.perla.lang.query.expression.LogicValue;
import org.junit.Test;


public class PartialCompTest {

	@Test
	public void enableSelectionTest() throws ParseException {
		 ParserContext ctx = new ParserContext();
		 CDTParser parser = new CDTParser(new StringReader(""
				 		+ " CREATE CONCEPT Hot WHEN temperature:float > 35.0 and snow:integer < 34"
				 		+ " WITH ENABLE COMPONENT: \"EVERY 1 m SELECT AVG(temperature:integer, 1 m)" 
				 		+ " SAMPLING EVERY 5 s "
				 		+ " EXECUTE IF room = 'salotto' \""));
		  Set<String> att = new TreeSet<>();
		  List<Concept> concepts = parser.CreateConcepts(ctx, att);
		  Concept c = concepts.get(0);
		  Expression when = c.getWhen().getWhen() ;
		  assertTrue(when instanceof Bool);
		  PartialComponent enable = c.getEnableComponent();
	      assertFalse(ctx.hasErrors());
	      boolean b = (enable.getStatementAST() instanceof SelectionStatementAST);
	      assertTrue(b);
	      enable.getStatementAST().compile(ctx);
	      assertFalse(ctx.hasErrors());
	}
	
	@Test
	public void enableSetTestFalse() throws ParseException {
		 ParserContext ctx = new ParserContext();
		 CDTParser parser = new CDTParser(new StringReader(""
				 		+ " CREATE CONCEPT Hot WHEN temperature:integer > 35"
				 		+ " WITH ENABLE COMPONENT: 'SETT alarm = true ON 20'"));
		  Set<String> att = new TreeSet<>();
		  parser.CreateConcepts(ctx, att);
	      assertTrue(ctx.hasErrors());
	}
	
	@Test
	public void enableSetTestTrue() throws ParseException {
		 ParserContext ctx = new ParserContext();
		 CDTParser parser = new CDTParser(new StringReader(""
				 		+ " CREATE CONCEPT Hot WHEN temperature:integer > 35 "
				 		+ " WITH ENABLE COMPONENT: 'SET alarm = true ON 20'"));
		  Set<String> att = new TreeSet<>();
		  List<Concept> concepts = parser.CreateConcepts(ctx, att);
		  Concept c = concepts.get(0);
		  PartialComponent enable = c.getEnableComponent();
	      assertFalse(ctx.hasErrors());
	      assertTrue(enable.getStatementAST() instanceof SetStatementAST);
	}
	
	@Test
	public void disableSelect() throws ParseException {
		 ParserContext ctx = new ParserContext();
		 CDTParser parser = new CDTParser(new StringReader(""
				 		+ " CREATE CONCEPT Hot WHEN temperature:integer > 35 "
				 		+ " WITH DISABLE COMPONENT: \"EVERY 1 m SELECT AVG(temperature:integer, 1 m)" 
				 		+ " SAMPLING EVERY 5 s \""));
		  parser.CreateConcept(ctx, new TreeSet<>());
		  assertTrue(ctx.hasErrors());
	}
	@Test
	public void moreAttributes() throws ParseException {
		 ParserContext ctx = new ParserContext();
		 CDTParser parser = new CDTParser(new StringReader("CREATE CONCEPT Icy1 WHEN temperature:float < -5 "
				 + " AND snow_density:float > 0.30 " + 
				 " EVALUATED ON \"EVERY 5s SELECT avg(temperature:float,5s), avg(snow_density:float,5s) " +
				 " SAMPLING EVERY 1 s EXECUTE IF position = 'pista1' \""));
		 Concept c = parser.CreateConcept(ctx, new TreeSet<>());
		 assertTrue(c.getWhen().getAttributes().containsKey("temperature"));
		 assertTrue(c.getWhen().getAttributes().containsKey("snow_density"));
	}

	@Test
	public void an() throws ParseException{
		 ParserContext ctx = new ParserContext();
		 CDTParser parser = new CDTParser(new StringReader("CREATE CONCEPT Icy1 WHEN temperature:float < -5.0 "
				 + " AND snow_density:float > 0.30 " + 
				 " "));
		Concept concept = parser.CreateConcept(ctx, new TreeSet<>());
		WhenCondition when = concept.getWhen();
		assertTrue(concept.getWhen().getAttributes().containsKey("temperature"));
		assertTrue(concept.getWhen().getAttributes().containsKey("snow_density"));
		
		
	}
	
}
