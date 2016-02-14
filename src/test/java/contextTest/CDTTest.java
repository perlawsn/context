package contextTest;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.List;
import java.util.TreeSet;

import org.junit.Test;
import org.dei.perla.cdt.*;
import org.dei.perla.cdt.parser.CDTParser;
import org.dei.perla.cdt.parser.ParseException;
import org.dei.perla.lang.parser.*;

public class CDTTest {
	
//    private List<Attribute> attr2 = Arrays.asList(new Attribute[] {
//            Attribute.create("temperature", DataType.FLOAT),
//            Attribute.create("pressure", DataType.INTEGER),
//            Attribute.create("meters", DataType.INTEGER),
//    });
    
	 @Test 
	  public void firstTest() throws ParseException {
		 ParserContext ctx = new ParserContext();
		 CDTParser parser = new CDTParser(
				 new StringReader("CREATE DIMENSION Location "
				 		+ "CREATE ATTRIBUTE $node_id EVALUATED ON "
				 		+ "'EVERY ONE SELECT location:string SAMPLING EVERY 1 s EXECUTE IF EXISTS (id)' " ));
		    CDT cdt = parser.CDT(ctx);
		    Dimension dim = cdt.getDimensions().get(0);
		    CreateAttr att = dim.getAttribute();
		    assertThat(att.getName(), equalTo("node_id"));
		    assertThat(att.getEvaluatedOn().getStringEvaluatedOn(), equalTo("EVERY ONE"
		    		+ " SELECT location:string SAMPLING EVERY 1 s EXECUTE IF EXISTS (id)"));
	        assertFalse(ctx.hasErrors());
	  }
        
	 @Test
	  public void createFatherDimension() throws ParseException {
		 ParserContext ctx = new ParserContext();
		 CDTParser parser = new CDTParser(
				 new StringReader("CREATE DIMENSION Location "
				 		+ " CREATE CONCEPT cucina WHEN location:string = 'cucina'"
				 		+ " WITH ENABLE COMPONENT: 'EVERY ONE SELECT location:string SAMPLING EVERY 1 s' "
				 		+ " WITH DISABLE COMPONENT: 'SET alarm = true ON 20 '"
				 		+ " WITH REFRESH COMPONENT: 5 s "
				 		+ " CREATE ATTRIBUTE $giallo EVALUATED ON 'EVERY ONE SELECT location:string SAMPLING EVERY 1 s'"));
		    CDT cdt = parser.CDT(ctx);
		    List<Dimension> dims = cdt.getDimensions();
	        assertFalse(dims.isEmpty());
		    Dimension location = dims.get(0);
		    System.out.println(ctx.getErrorDescription());
	        assertFalse(ctx.hasErrors());
	        assertThat(location.getName(), equalTo("Location"));
	        assertThat(location.getFather(), equalTo("ROOT"));
	 }
	 
	 @Test
	  public void createConcept() throws ParseException {
		 ParserContext ctx = new ParserContext();
		 CDTParser parser = new CDTParser(new StringReader(""
				 		+ " CREATE CONCEPT cucina WHEN location:string = 'cucina'"
				 		+ " WITH ENABLE COMPONENT: 'EVERY ONE SELECT location:string SAMPLING EVERY 1 s' "
				 		+ " WITH DISABLE COMPONENT: 'SET alarm = true ON 20'"
				 		+ " WITH REFRESH COMPONENT: 10 s "
				 		+ " CREATE ATTRIBUTE $power EVALUATED ON 'EVERY ONE SELECT location:string SAMPLING EVERY 1 s' "
				 		+ " CREATE ATTRIBUTE $alert EVALUATED ON 'EVERY ONE SELECT location:string SAMPLING EVERY 1 s' "));
		    Concept c = parser.CreateConcept(ctx, new TreeSet<String>());
	        assertFalse(ctx.hasErrors());
	        assertThat(c.getName(), equalTo("cucina"));

	 }

	 @Test   
	  public void enableComponentTest() throws ParseException {
		 ParserContext ctx = new ParserContext();
		 CDTParser parser = new CDTParser(
				 new StringReader("CREATE CONCEPT cucina WHEN location:string = 'cucina'"
				 		+ " WITH ENABLE COMPONENT: 'set alarm = True ON 10' "));
		    Concept c = parser.CreateConcept(ctx, new TreeSet<>());
		    assertThat(c.getName(), equalTo("cucina"));
	        assertFalse(ctx.hasErrors());
	 }
		
	 
	 @Test
	  public void disableComponentTest() throws ParseException {
		 ParserContext ctx = new ParserContext();
		 CDTParser parser = new CDTParser(
				 new StringReader("CREATE CONCEPT cucina WHEN location:string = 'office' "
				 		+ " WITH DISABLE COMPONENT: 'set alarm = True ON 10'  "));
		    List<Concept> concepts = parser.CreateConcepts(ctx, null);
		    Concept c = concepts.get(0);
		    assertThat(c.getName(), equalTo("cucina"));
	        assertFalse(ctx.hasErrors());
	  }
	 
	 @Test(expected=RuntimeException.class)
	  public void refreshComponentTest() throws ParseException {
		 ParserContext ctx = new ParserContext();
		 CDTParser parser = new CDTParser(
				 new StringReader("CREATE CONCEPT cucina WHEN location:string > 32"
				 		+ " WITH REFRESH COMPONENT: 10+5 s  "));
		    Concept c = parser.CreateConcept(ctx, new TreeSet<>());
		    assertThat(c.getName(), equalTo("cucina"));
	        assertTrue(ctx.hasErrors());
	  }
	 
	 @Test
	  public void createCDTTest() throws ParseException {
		 ParserContext ctx = new ParserContext();
		 CDTParser parser = new CDTParser(new StringReader(" "
		 	+ "CREATE DIMENSION Location "
		 		+ "CREATE CONCEPT office WHEN location:string = 'office' "
		 		+ "CREATE CONCEPT meeting_room WHEN location:string = 'meeting_room' "
		 		+ "EVALUATED ON 'EVERY ONE SELECT location:string SAMPLING EVERY 1 s' "
		 		+ "CREATE DIMENSION Smoke "
				+ "CREATE CONCEPT none WHEN smoke:float < 0.4 "
				+ "CREATE CONCEPT little WHEN smoke:float >= 0.4 AND smoke:float <= 1 "
				));
		 CDT cdt = parser.CDT(ctx);
		 assertFalse(ctx.hasErrors());
		 List<Dimension> dims = cdt.getDimensions();
		 assertThat(dims.get(0).getName(), equalTo("Location"));
		 List<Concept> concepts = dims.get(0).getConcepts();
		 assertThat(concepts.get(0).getName(), equalTo("office"));
		 WhenCondition w = concepts.get(1).getWhen();
		 assertThat(w.getEvaluatedOnString(), equalTo("EVERY ONE SELECT location:string SAMPLING EVERY 1 s"));
	 } 
	 
	 @Test
	  public void dimensionWithTwoAttributesEquals() throws ParseException {
		 ParserContext ctx = new ParserContext();
		 CDTParser parser = new CDTParser(new StringReader(" "
		 	+ "CREATE DIMENSION Location "
		 		+ "CREATE CONCEPT office WHEN location:string = 'office' "
		 			+ "CREATE ATTRIBUTE $prova EVALUATED ON \"EVERY ONE SELECT prova:string SAMPLING EVERY 2 d \" "
		 		+ "CREATE CONCEPT meeting_room WHEN location:string = 'meeting_room' "
		 			+ "CREATE ATTRIBUTE $prova EVALUATED ON \"EVERY ONE SELECT prova:string SAMPLING EVERY 2 H \" "));
		 parser.CDT(ctx);
		 assertTrue(ctx.hasErrors());
		 System.out.println(ctx.getErrorDescription());
	 } 


}
