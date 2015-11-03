import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.dei.perla.cdt.Concept;
import org.dei.perla.cdt.CreateAttr;
import org.dei.perla.cdt.PartialComponent;
import org.dei.perla.cdt.QueryEvaluatedOn;
import org.dei.perla.cdt.parser.CDTParser;
import org.dei.perla.cdt.parser.ParseException;
import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.fpc.DataType;
import org.dei.perla.lang.parser.AttributeOrder;
import org.dei.perla.lang.parser.ParserContext;
import org.dei.perla.lang.parser.ast.*;
import org.dei.perla.lang.query.expression.Comparison;
import org.dei.perla.lang.query.expression.ComparisonOperation;
import org.dei.perla.lang.query.expression.Expression;
import org.junit.Test;


public class CreateAttrTest {

	@Test
	public void createAttrTest() throws ParseException {
		ParserContext ctx = new ParserContext();
		CDTParser parser = new CDTParser(new StringReader(""
				 		+ " CREATE CONCEPT Casa CREATE ATTRIBUTE $temperature"
				 		+ " EVALUATED ON 'EVERY 1 m SELECT AVG(temp, 1 m)" 
				 		+ " SAMPLING EVERY 5 s "
				 		+ " EXECUTE IF EXISTS(temp) AND EXISTS(room) AND room = salotto'"));
		Concept c = parser.CreateConcept(ctx, new HashSet<String>());
		CreateAttr tempAtt = c.getAttributes().get(0);
		assertFalse(ctx.hasErrors());
		QueryEvaluatedOn q = (QueryEvaluatedOn) tempAtt.getEvaluatedOn();
		boolean b = (q.getQueryEvaluatedOn() instanceof SelectionStatementAST);
		assertTrue(b);
	}

	@Test
	public void unoTest(){
		List<String> names = new ArrayList<String>();
		names.add("temperature");
		names.add("pressure");
		List<Attribute> atts = names.stream().map(name -> Attribute.create(name, DataType.ANY)).collect(Collectors.toList());
		assertTrue(atts.get(0) instanceof Attribute);
		assertThat(atts.get(0).getId(), equalTo("temperature")); 
		ComparisonAST c = new ComparisonAST(ComparisonOperation.EQ, new ConstantAST(1, DataType.INTEGER),
				new ConstantAST(2, DataType.INTEGER));

	}
	
}
