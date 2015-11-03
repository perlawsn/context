import static org.hamcrest.Matchers.equalTo;
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
import org.dei.perla.lang.query.statement.SelectionStatement;
import org.dei.perla.lang.query.statement.SetStatement;
import org.junit.Test;


public class PartialCompTest {

	@Test
	public void enableSelectionTest() throws ParseException {
		 ParserContext ctx = new ParserContext();
		 CDTParser parser = new CDTParser(new StringReader(""
				 		+ " CREATE CONCEPT Hot WHEN temperature > 35"
				 		+ " WITH ENABLE COMPONENT: 'EVERY 1 m SELECT AVG(temp, 1 m)" 
				 		+ " SAMPLING EVERY 5 s "
				 		+ " EXECUTE IF room = salotto'"));
		  Set<String> att = new TreeSet<>();
		  List<Concept> concepts = parser.CreateConcepts(ctx, att);
		  Concept c = concepts.get(0);
		  PartialComponent enable = c.getEnableComponent();
	      assertFalse(ctx.hasErrors());
	      boolean b = (enable.getStatement() instanceof SelectionStatementAST);
	      assertTrue(b);
	}
	
	@Test
	public void enableSetTestFalse() throws ParseException {
		 ParserContext ctx = new ParserContext();
		 CDTParser parser = new CDTParser(new StringReader(""
				 		+ " CREATE CONCEPT Hot WHEN temperature > 35"
				 		+ " WITH ENABLE COMPONENT: 'SETT alarm = true ON 20'"));
		  Set<String> att = new TreeSet<>();
		  List<Concept> concepts = parser.CreateConcepts(ctx, att);
	      assertTrue(ctx.hasErrors());
	}
	
	@Test
	public void enableSetTestTrue() throws ParseException {
		 ParserContext ctx = new ParserContext();
		 CDTParser parser = new CDTParser(new StringReader(""
				 		+ " CREATE CONCEPT Hot WHEN temperature = pippo "
				 		+ " WITH ENABLE COMPONENT: 'SET alarm = true ON 20'"));
		  Set<String> att = new TreeSet<>();
		  List<Concept> concepts = parser.CreateConcepts(ctx, att);
		  Concept c = concepts.get(0);
		  PartialComponent enable = c.getEnableComponent();
	      assertFalse(ctx.hasErrors());
	      assertTrue(enable.getStatement() instanceof SetStatementAST);
	}

}
