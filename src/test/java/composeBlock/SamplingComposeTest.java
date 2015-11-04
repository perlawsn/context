package composeBlock;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dei.perla.context.ComposeBlock;
import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.fpc.DataType;
import org.dei.perla.lang.parser.ParserContext;
import org.dei.perla.lang.parser.ast.AttributeReferenceAST;
import org.dei.perla.lang.parser.ast.ComparisonAST;
import org.dei.perla.lang.parser.ast.ConstantAST;
import org.dei.perla.lang.parser.ast.EveryAST;
import org.dei.perla.lang.parser.ast.ExecutionConditionsAST;
import org.dei.perla.lang.parser.ast.ExpressionAST;
import org.dei.perla.lang.parser.ast.IfEveryAST;
import org.dei.perla.lang.parser.ast.NodeSpecificationsAST;
import org.dei.perla.lang.parser.ast.RefreshAST;
import org.dei.perla.lang.parser.ast.SamplingAST;
import org.dei.perla.lang.parser.ast.SamplingEventAST;
import org.dei.perla.lang.parser.ast.SamplingIfEveryAST;
import org.dei.perla.lang.query.expression.ComparisonOperation;
import org.dei.perla.lang.query.statement.RatePolicy;
import org.dei.perla.lang.query.statement.RefreshType;
import org.dei.perla.lang.query.statement.SamplingEvent;
import org.junit.Test;


public class SamplingComposeTest {
	
	@Test
	public void testSamplingEvent() {
        List<String> evs1 = Arrays.asList(new String[] {
                "test1",
                "test2",
        });
        List<String> evs2 = Arrays.asList(new String[] {
                "test3",
                "test4",
        });
        SamplingEventAST ast1 = new SamplingEventAST(evs1);
        SamplingEventAST ast2 = new SamplingEventAST(evs2);
        ParserContext ctx = new ParserContext();
        ComposeBlock c = new ComposeBlock();
        List<SamplingAST> sampling = new ArrayList<SamplingAST>();
        sampling.add(ast1);
        sampling.add(ast2);
        SamplingAST result = c.composeSampling(sampling, ctx);
        assertFalse(ctx.hasErrors());
        assertTrue(result instanceof SamplingEventAST);
        SamplingEventAST e = (SamplingEventAST) result;
        assertTrue(e.getEvents().containsAll(evs1));
        assertTrue(e.getEvents().containsAll(evs2));
	}
	
	@Test
	public void testSamplingIfEvery() {
		ParserContext ctx = new ParserContext();
		
        List<IfEveryAST> ifes = new ArrayList<>();
        EveryAST ev1 = new EveryAST(new ConstantAST(20, DataType.INTEGER),
                ChronoUnit.MINUTES);
        ExpressionAST cond = new ComparisonAST(ComparisonOperation.GT,
                new AttributeReferenceAST("temperature", DataType.ANY),
                new ConstantAST(40, DataType.INTEGER));
        IfEveryAST ife1 = new IfEveryAST(cond, ev1);
        ifes.add(ife1);
        EveryAST ev2 = new EveryAST(new ConstantAST(10, DataType.INTEGER),
                ChronoUnit.MINUTES);
        IfEveryAST ife2 = new IfEveryAST(ConstantAST.TRUE, ev2);
        ifes.add(ife2);
        SamplingIfEveryAST sa1 = new SamplingIfEveryAST(ifes, RatePolicy.STRICT,
        		new RefreshAST(null, new ConstantAST(20, DataType.INTEGER), ChronoUnit.MINUTES));
       
        List<IfEveryAST> ifes2 = new ArrayList<>();
        EveryAST ev3 = new EveryAST(new ConstantAST(30, DataType.INTEGER),
                ChronoUnit.SECONDS);
        ExpressionAST cond2 = new ComparisonAST(ComparisonOperation.GT,
                new AttributeReferenceAST("pressure", DataType.ANY),
                new ConstantAST(40, DataType.INTEGER));
        IfEveryAST ife3 = new IfEveryAST(cond2, ev3);
        ifes2.add(ife3);
        ev2 = new EveryAST(new ConstantAST(10, DataType.INTEGER),
                ChronoUnit.MINUTES);
        IfEveryAST ife4 = new IfEveryAST(ConstantAST.TRUE, ev3);
        ifes2.add(ife4);
        RefreshAST refresh = new RefreshAST(null, new ConstantAST(30, DataType.INTEGER), ChronoUnit.SECONDS);
        SamplingIfEveryAST sa2 = new SamplingIfEveryAST(ifes2, RatePolicy.STRICT,
                refresh);
        
        ComposeBlock c = new ComposeBlock();
        List<SamplingAST> sampling = new ArrayList<SamplingAST>();
        sampling.add(sa1);
        sampling.add(sa2);
        SamplingAST result = c.composeSampling(sampling, ctx);
        assertFalse(ctx.hasErrors());
        assertTrue(result instanceof SamplingIfEveryAST);
        SamplingIfEveryAST e = (SamplingIfEveryAST) result;
        assertThat(e.getRefresh().getDurationUnit(), equalTo(ChronoUnit.SECONDS));
        assertThat(e.getRefresh(), equalTo(refresh));
        assertTrue(e.getIfEvery().contains(ife1));
        assertTrue(e.getIfEvery().contains(ife2));
        assertTrue(e.getIfEvery().contains(ife3));
        assertTrue(e.getIfEvery().contains(ife4));
	}
	
	@Test
	public void testIncompatibleSamplingEvent() {
        List<String> evs1 = Arrays.asList(new String[] {
                "test1",
                "test2",
        });
        SamplingEventAST ast1 = new SamplingEventAST(evs1);

        List<IfEveryAST> ifes = new ArrayList<>();
        EveryAST ev1 = new EveryAST(new ConstantAST(20, DataType.INTEGER),
                ChronoUnit.MINUTES);
        ExpressionAST cond = new ComparisonAST(ComparisonOperation.GT,
                new AttributeReferenceAST("temperature", DataType.ANY),
                new ConstantAST(40, DataType.INTEGER));
        IfEveryAST ife1 = new IfEveryAST(cond, ev1);
        ifes.add(ife1);
        EveryAST ev2 = new EveryAST(new ConstantAST(10, DataType.INTEGER),
                ChronoUnit.MINUTES);
        IfEveryAST ife2 = new IfEveryAST(ConstantAST.TRUE, ev2);
        ifes.add(ife2);
        SamplingIfEveryAST ast2 = new SamplingIfEveryAST(ifes, RatePolicy.STRICT,
        		RefreshAST.NEVER);
        
        ParserContext ctx = new ParserContext();
        ComposeBlock c = new ComposeBlock();
        List<SamplingAST> sampling = new ArrayList<SamplingAST>();
        sampling.add(ast1);
        sampling.add(ast2);
        SamplingAST result = c.composeSampling(sampling, ctx);
        assertTrue(ctx.hasErrors());
        assertNull(result);
	}
}

