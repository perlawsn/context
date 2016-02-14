package composeBlock;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.dei.perla.context.ComposerManager;
import org.dei.perla.core.fpc.DataType;
import org.dei.perla.lang.parser.ParserContext;
import org.dei.perla.lang.parser.ast.BoolAST;
import org.dei.perla.lang.parser.ast.ComparisonAST;
import org.dei.perla.lang.parser.ast.ConstantAST;
import org.dei.perla.lang.parser.ast.ExpressionAST;
import org.dei.perla.lang.parser.ast.NotAST;
import org.dei.perla.lang.query.expression.ComparisonOperation;
import org.junit.Test;


public class HavingComposeTest {
	
    private static final ConstantAST trueExp = ConstantAST.TRUE;

	@Test
	public void composeTest(){
		ParserContext ctx = new ParserContext();
		ExpressionAST one = new ComparisonAST(ComparisonOperation.EQ, 
				new ConstantAST(3, DataType.INTEGER), new ConstantAST(3, DataType.INTEGER));
		ExpressionAST two = new ComparisonAST(ComparisonOperation.GT, 
				new ConstantAST(5, DataType.INTEGER), new ConstantAST(3, DataType.INTEGER));
		ExpressionAST three = new NotAST(trueExp);
		List<ExpressionAST> havingList = Arrays.asList(new ExpressionAST[]{one, two, three});
		ComposerManager c = new ComposerManager();
		BoolAST e = (BoolAST) c.composeHaving(havingList, ctx);
		assertThat(e.getRightOperand(), equalTo(three));
	}
	
	@Test
	public void everyListIsEmptyTest(){
		ComposerManager c = new ComposerManager();
		assertThat(c.composeHaving(Collections.emptyList(), new ParserContext()), equalTo(ConstantAST.TRUE));
	}
}
