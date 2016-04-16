package contextTest;
import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.HashSet;
import org.dei.perla.cdt.Concept;
import org.dei.perla.cdt.CreateAttr;
import org.dei.perla.cdt.EvaluatedOn;
import org.dei.perla.cdt.FunctionEvaluatedOn;
import org.dei.perla.cdt.QueryEvaluatedOn;
import org.dei.perla.cdt.parser.CDTParser;
import org.dei.perla.cdt.parser.ParseException;
import org.dei.perla.lang.parser.ParserContext;
import org.dei.perla.lang.query.statement.SelectionStatement;
import org.junit.Test;


public class CreateAttrTest {

	@Test
	public void createAttrWithQuery() throws ParseException, org.dei.perla.lang.parser.ParseException {
		ParserContext ctx = new ParserContext();
		CDTParser parser = new CDTParser(new StringReader(""
				 		+ " CREATE CONCEPT Casa CREATE ATTRIBUTE $temperature"
				 		+ " EVALUATED ON 'EVERY 1 m SELECT AVG(temp:integer, 1 m)" 
				 		+ " SAMPLING EVERY 5 s "
				 		+ " EXECUTE IF EXISTS(temp) AND EXISTS(room) AND room = salotto'"));
		Concept c = parser.CreateConcept(ctx, new HashSet<String>());
		CreateAttr tempAtt = c.getAttributes().get(0);
		EvaluatedOn ev = tempAtt.getEvaluatedOn();
		boolean b = (ev instanceof QueryEvaluatedOn);
		assertTrue(b);
		QueryEvaluatedOn q = (QueryEvaluatedOn) ev;
		assertTrue(q.getQueryEvaluatedOn().getClass().equals(SelectionStatement.class));
	}
	
	@Test
	public void createAttrWithFunction() throws ParseException, org.dei.perla.lang.parser.ParseException {
		ParserContext ctx = new ParserContext();
		CDTParser parser = new CDTParser(new StringReader(""
				 		+ " CREATE CONCEPT Casa CREATE ATTRIBUTE $temperature AS getUserId() "));
		Concept c = parser.CreateConcept(ctx, new HashSet<String>());
		CreateAttr tempAtt = c.getAttributes().get(0);
		EvaluatedOn q = tempAtt.getEvaluatedOn();
		boolean b = (q instanceof FunctionEvaluatedOn);
		assertTrue(b);
		FunctionEvaluatedOn f = (FunctionEvaluatedOn) q;
		assertTrue(f.getStringEvaluatedOn().equals("getUserId"));
		assertTrue(f.computeValue().equals("ab"));
	}
		
			
}
