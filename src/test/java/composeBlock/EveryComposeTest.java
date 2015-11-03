package composeBlock;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.dei.perla.context.ComposeBlock;
import org.dei.perla.core.fpc.DataType;
import org.dei.perla.lang.parser.ParserContext;
import org.dei.perla.lang.parser.ast.*;
import org.junit.Test;


public class EveryComposeTest {
	
	@Test
	public void invalidWindowSize(){
		WindowSizeAST uno = new WindowSizeAST(new ConstantAST(-2, DataType.INTEGER));
		ParserContext ctx = new ParserContext();
		uno.compile(ctx);
		assertTrue(ctx.hasErrors());
	}
	
	@Test
	public void findMinSampleTest(){
		ParserContext ctx = new ParserContext();
		WindowSizeAST cinque = new WindowSizeAST(new ConstantAST(5, DataType.INTEGER));
		WindowSizeAST tre = new WindowSizeAST(new ConstantAST(3, DataType.INTEGER));
		WindowSizeAST dieci = new WindowSizeAST(new ConstantAST(10, DataType.INTEGER));
		List<WindowSizeAST> everyList = Arrays.asList(new WindowSizeAST[]{cinque, tre, dieci});
		ComposeBlock c = new ComposeBlock();
		WindowSizeAST w = c.composeWindowSize(everyList, ctx);
		assertThat(w.getSamples(), 
				equalTo(new ConstantAST(3, DataType.INTEGER)));
	}

	@Test
	public void findMinDurationTest(){
		ParserContext ctx = new ParserContext();
		WindowSizeAST cinque_days = new WindowSizeAST(new ConstantAST(5, DataType.INTEGER), ChronoUnit.DAYS);
		WindowSizeAST tre_ore = new WindowSizeAST(new ConstantAST(3, DataType.INTEGER), ChronoUnit.HOURS);
		WindowSizeAST dieci_minuti = new WindowSizeAST(new ConstantAST(10, DataType.INTEGER), ChronoUnit.MINUTES);
		List<WindowSizeAST> everyList = Arrays.asList(new WindowSizeAST[]
				{cinque_days, tre_ore, dieci_minuti});
		ComposeBlock c = new ComposeBlock();
		assertThat(c.composeWindowSize(everyList, ctx), equalTo(dieci_minuti));
	}
	
	@Test
	public void everyClausesWithDifferentTypes(){
		ParserContext ctx = new ParserContext();
		WindowSizeAST cinque_days = new WindowSizeAST(new ConstantAST(5, DataType.INTEGER), ChronoUnit.DAYS);
		WindowSizeAST tre_ore = new WindowSizeAST(new ConstantAST(3, DataType.INTEGER), ChronoUnit.HOURS);
		WindowSizeAST one = WindowSizeAST.ONE;
		List<WindowSizeAST> everyList = Arrays.asList(new WindowSizeAST[]
				{cinque_days, tre_ore, one});
		ComposeBlock c = new ComposeBlock();
		c.composeWindowSize(everyList, ctx);
		assertTrue(ctx.hasErrors());
	}
	
	@Test
	public void everyListIsEmptyTest(){
		ComposeBlock c = new ComposeBlock();
		assertThat(c.composeWindowSize(Collections.emptyList(), new ParserContext()), equalTo(WindowSizeAST.ONE));
	}
	
	
}
