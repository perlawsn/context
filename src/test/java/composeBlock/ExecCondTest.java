package composeBlock;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dei.perla.context.ComposerManager;
import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.fpc.DataType;
import org.dei.perla.lang.parser.ParserContext;
import org.dei.perla.lang.parser.ast.AttributeReferenceAST;
import org.dei.perla.lang.parser.ast.ConstantAST;
import org.dei.perla.lang.parser.ast.ExecutionConditionsAST;
import org.dei.perla.lang.parser.ast.ExpressionAST;
import org.dei.perla.lang.parser.ast.NodeSpecificationsAST;
import org.dei.perla.lang.parser.ast.RefreshAST;
import org.dei.perla.lang.query.statement.RefreshType;
import org.junit.Test;


public class ExecCondTest {
	
	private static Attribute temp = Attribute.create("temperature", DataType.FLOAT);
	private static Attribute press = Attribute.create("pressure", DataType.INTEGER);
	private static Attribute meters = Attribute.create("meters", DataType.INTEGER);
	
	private final static List<Attribute> atts = Arrays.asList(new Attribute[] {
            temp, press, meters
    });

	@Test
	public void composeExecTest(){
		ParserContext ctx = new ParserContext();
	    ExecutionConditionsAST test1 = new ExecutionConditionsAST(
	                ConstantAST.TRUE,
	                NodeSpecificationsAST.EMPTY,
	                RefreshAST.NEVER
	        );
	    NodeSpecificationsAST specs = new NodeSpecificationsAST(atts);
        ConstantAST c = new ConstantAST(10, DataType.INTEGER);
        RefreshAST ref = new RefreshAST(null, c, ChronoUnit.MINUTES);
        ExecutionConditionsAST test2 = new ExecutionConditionsAST(ConstantAST.TRUE, specs,ref);
        ExpressionAST exp = new AttributeReferenceAST("run", DataType.BOOLEAN);
        ExecutionConditionsAST test3 = new ExecutionConditionsAST(
                exp,
                NodeSpecificationsAST.ALL,
                RefreshAST.NEVER
        );
        List<ExecutionConditionsAST> ec = Arrays.asList(new ExecutionConditionsAST[]{test1, test2, test3});
        ComposerManager cBlock = new ComposerManager();
        ExecutionConditionsAST result = cBlock.composeExecCond(ec, ctx);
        assertThat(result.getRefresh().getType(), equalTo(RefreshType.TIME));
        assertThat(result.getRefresh().getDurationValue(), equalTo(c));
	}
	
	@Test
	public void composeInvalidRefreshTest(){
		ParserContext ctx = new ParserContext();
	    NodeSpecificationsAST specs = new NodeSpecificationsAST(atts);
        ConstantAST c = new ConstantAST(10, DataType.INTEGER);
        RefreshAST ref = new RefreshAST(null, c, ChronoUnit.MINUTES);
        ExecutionConditionsAST test1 = new ExecutionConditionsAST(ConstantAST.TRUE, specs,ref);
        ExpressionAST exp = new AttributeReferenceAST("run", DataType.BOOLEAN);
        List<String> events = new ArrayList<String>();
        events.add("test1");
        events.add("test2");
        ExecutionConditionsAST test2 = new ExecutionConditionsAST(
                exp,
                NodeSpecificationsAST.ALL,
                new RefreshAST(events)
        );
        ExecutionConditionsAST test3 = new ExecutionConditionsAST(
                ConstantAST.TRUE,
                NodeSpecificationsAST.ALL,
                RefreshAST.NEVER
        );
        List<ExecutionConditionsAST> ec = Arrays.asList(new ExecutionConditionsAST[]{test1, test2, test3});
        ComposerManager cBlock = new ComposerManager();
        cBlock.composeExecCond(ec, ctx);
        assertTrue(ctx.hasErrors());
	}
	
	@Test
	public void composeNodeSpecificationTest(){
		ParserContext ctx = new ParserContext();
	    NodeSpecificationsAST specs = new NodeSpecificationsAST(atts);
        ConstantAST c = new ConstantAST(10, DataType.INTEGER);
        RefreshAST ref = new RefreshAST(null, c, ChronoUnit.MINUTES);
        ExecutionConditionsAST test1 = new ExecutionConditionsAST(ConstantAST.TRUE, specs,ref);
        List<String> events = new ArrayList<String>();
        events.add("test1");
        events.add("test2");
        Attribute vapore = Attribute.create("vapore", DataType.FLOAT);
        Attribute acqua = Attribute.create("acqua", DataType.INTEGER);
        List<Attribute> atts2 = Arrays.asList(new Attribute[] { vapore, acqua });
        NodeSpecificationsAST specs2 = new NodeSpecificationsAST(atts2);
        ExecutionConditionsAST test2 = new ExecutionConditionsAST(
                ConstantAST.TRUE,
                specs2,
                new RefreshAST(events)
        );
        List<ExecutionConditionsAST> ec = Arrays.asList(new ExecutionConditionsAST[]{test1, test2});
        ComposerManager cBlock = new ComposerManager();
        ExecutionConditionsAST e = cBlock.composeExecCond(ec, ctx);
        assertTrue(e.getSpecifications().getSpecifications().contains(temp));
        assertTrue(e.getSpecifications().getSpecifications().contains(press));
        assertTrue(e.getSpecifications().getSpecifications().contains(meters));
        assertTrue(e.getSpecifications().getSpecifications().contains(vapore));
        assertTrue(e.getSpecifications().getSpecifications().contains(acqua));
	}
	
}
