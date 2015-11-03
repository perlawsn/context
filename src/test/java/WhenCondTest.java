import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

import java.io.StringReader;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.dei.perla.cdt.Concept;
import org.dei.perla.cdt.WhenCondition;
import org.dei.perla.cdt.parser.CDTParser;
import org.dei.perla.cdt.parser.ParseException;
import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.fpc.DataType;
import org.dei.perla.lang.parser.ParserAST;
import org.dei.perla.lang.parser.ParserContext;
import org.dei.perla.lang.parser.ast.AttributeReferenceAST;
import org.dei.perla.lang.parser.ast.ConstantAST;
import org.dei.perla.lang.parser.ast.ExpressionAST;
import org.dei.perla.lang.parser.ast.FieldSelectionAST;
import org.dei.perla.lang.parser.ast.SelectionStatementAST;
import org.dei.perla.lang.parser.ast.StatementAST;
import org.junit.Test;


public class WhenCondTest {

	@Test
	public void evaluatedWhenTest() throws ParseException {
		 ParserContext ctx = new ParserContext();
		 CDTParser parser = new CDTParser(new StringReader(""
				 		+ " CREATE CONCEPT Hot WHEN 10 > 35"
				 		+ " EVALUATED ON 'EVERY 1 m SELECT AVG(temp, 1 m)" 
				 		+ " SAMPLING EVERY 5 s "
				 		+ " EXECUTE IF EXISTS(temp)'"));
		  Concept c = parser.CreateConcept(ctx, null);
		  WhenCondition w = c.getWhen();
	      assertFalse(ctx.hasErrors());
	      StatementAST ast = w.getEvaluatedOn();
	      boolean b = (ast instanceof SelectionStatementAST);
	      assertTrue(b);
	      
	}
	
	@Test
	public void invalidEvaluatedTest() throws ParseException {
		 ParserContext ctx = new ParserContext();
		 CDTParser parser = new CDTParser(new StringReader(""
				 		+ " CREATE CONCEPT Hot WHEN temperature > 35"
				 		+ " EVALUATED ON 'SET alarm = true ON 21'"));
		  Concept c = parser.CreateConcept(ctx, null);
		  WhenCondition w = c.getWhen();
	 //    assertFalse(ctx.hasErrors());
	      StatementAST ast = w.getEvaluatedOn();
	      boolean b = (ast instanceof SelectionStatementAST);
	      assertFalse(b);
	}

	@Test
	public void iprimoTest() throws ParseException {
		 ParserContext ctx = new ParserContext();
		 CDTParser parser = new CDTParser(new StringReader(""
				 		+ " CREATE CONCEPT Hot WHEN temperature > 35 or pressure between 20 and 12"
				 		+ " "));
		  Concept c = parser.CreateConcept(ctx, null);
		  WhenCondition w = c.getWhen();
	      assertFalse(ctx.hasErrors());
	      SelectionStatementAST s = (SelectionStatementAST) w.getEvaluatedOn(); 
		  ExpressionAST def = ConstantAST.NULL;
		  List<FieldSelectionAST> fsl = new ArrayList<FieldSelectionAST>();
		  AttributeReferenceAST t = new AttributeReferenceAST("temperature",DataType.INTEGER);
		  AttributeReferenceAST p = new AttributeReferenceAST("pressure",DataType.INTEGER);
		  FieldSelectionAST temp = new FieldSelectionAST(t ,def);
		  FieldSelectionAST press = new FieldSelectionAST(p ,def);
		  fsl.add(temp);
		  fsl.add(press);
	      assertThat(w.getAttributes().get(0), equalTo("temperature"));
	      AttributeReferenceAST a1 = (AttributeReferenceAST) s.getFields().get(0).getField();
	      AttributeReferenceAST a2 = (AttributeReferenceAST) s.getFields().get(1).getField();
	      assertThat(a1.getId(), equalTo("temperature"));
	      assertThat(a2.getId(), equalTo("pressure"));
	      List<Attribute> atts = s.getExecutionConditions().getSpecifications().getSpecifications();
	      assertThat(atts.get(0), equalTo(Attribute.create("temperature", DataType.ANY)));
	      assertThat(atts.get(1), equalTo(Attribute.create("pressure", DataType.ANY)));
	      s.compile(ctx);
	      assertTrue(ctx.hasErrors());
	     
	}

	@Test
	public void iprimoTeswt() throws ParseException, org.dei.perla.lang.parser.ParseException {
		ParserContext ctx = new ParserContext();
		ParserAST ast = new ParserAST(new StringReader(""
		 		+ "EVERY ONE SELECT pippo SAMPLING EVERY 1 m "
		 		+ " "));
		ast.Statement(ctx);
		assertFalse(ctx.hasErrors());
	}

	@Test
	public void niente() {
		Duration a = Duration.of(30, ChronoUnit.SECONDS);
		System.out.println(a);
		System.out.println(a.getUnits());
		
	}
}
