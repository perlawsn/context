package contextTest;
import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.dei.perla.cdt.Concept;
import org.dei.perla.cdt.PartialComponent;
import org.dei.perla.cdt.parser.CDTParser;
import org.dei.perla.cdt.parser.ParseException;
import org.dei.perla.lang.parser.ParserContext;
import org.dei.perla.lang.parser.ast.SelectionStatementAST;
import org.dei.perla.lang.parser.ast.SetStatementAST;
import org.junit.Test;


public class PartialCompTest {

	@Test
	public void enableSelectionTest() throws ParseException {
		 ParserContext ctx = new ParserContext();
		 CDTParser parser = new CDTParser(new StringReader(""
				 		+ " CREATE CONCEPT Hot WHEN temperature:integer > 35"
				 		+ " WITH ENABLE COMPONENT: \"EVERY 1 m SELECT AVG(temperature:integer, 1 m)" 
				 		+ " SAMPLING EVERY 5 s "
				 		+ " EXECUTE IF room = 'salotto' \""));
		  Set<String> att = new TreeSet<>();
		  List<Concept> concepts = parser.CreateConcepts(ctx, att);
		  Concept c = concepts.get(0);
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

}
