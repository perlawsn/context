package org.dei.perla.app;

import org.dei.perla.core.fpc.DataType;
import org.dei.perla.lang.query.expression.AttributeReference;
import org.dei.perla.lang.query.expression.*;

public class Prova {

	public static void main(String[] args) {
//		 Expression e = cond.getCondition();
//         LogicValue v = (LogicValue) e.run(sample.values(), null);
		AttributeReference att = new AttributeReference("temperature", DataType.INTEGER, 0);
		Expression e = new Comparison(ComparisonOperation.GT, att, Constant.create(5, DataType.INTEGER));
		Object[] record = new Integer[]{10};
		LogicValue v = (LogicValue) e.run(record, null);
		System.out.println(v.toBoolean());
	}

	

}
