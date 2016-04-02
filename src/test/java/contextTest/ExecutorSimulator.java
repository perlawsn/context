package contextTest;

import java.io.StringReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.dei.perla.core.PerLaSystem;
import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.fpc.DataType;
import org.dei.perla.core.utils.Errors;
import org.dei.perla.lang.SelectionStatementTask;
import org.dei.perla.lang.StatementHandler;
import org.dei.perla.lang.StatementTask;
import org.dei.perla.lang.executor.QueryException;
import org.dei.perla.lang.executor.Record;
import org.dei.perla.lang.parser.FieldSelection;
import org.dei.perla.lang.parser.ParseException;
import org.dei.perla.lang.parser.ParserAST;
import org.dei.perla.lang.parser.ParserContext;
import org.dei.perla.lang.parser.ast.StatementAST;
import org.dei.perla.lang.query.expression.Aggregate;
import org.dei.perla.lang.query.expression.AttributeReference;
import org.dei.perla.lang.query.expression.Expression;
import org.dei.perla.lang.query.statement.CreationStatement;
import org.dei.perla.lang.query.statement.InsertionStatement;
import org.dei.perla.lang.query.statement.Select;
import org.dei.perla.lang.query.statement.SelectionStatement;
import org.dei.perla.lang.query.statement.SetStatement;
import org.dei.perla.lang.query.statement.Statement;

public class ExecutorSimulator {

	 private final PerLaSystem perla;

	    public ExecutorSimulator(PerLaSystem perla) {
	        this.perla = perla;
	    }

	    public StatementTask execute(String query, StatementHandler h)
	            throws QueryException {
	        Errors err = new Errors();

	        Statement s = parseQuery(query);

	       return execute(s, h);
	    }
	 

	    private Statement parseQuery(String query) throws QueryException {
	        ParserContext ctx = new ParserContext();
	        ParserAST p = new ParserAST(new StringReader(query));
	        StatementAST ast;
	        try {
	            ast = p.Statement(ctx);
	        } catch(ParseException e) {
	            throw new QueryException("Cannot parse query", e);
	        }
	        if (ctx.hasErrors()) {
	            throw new QueryException(ctx.getErrorDescription());
	        }

	        Statement s = ast.compile(ctx);
	        if (ctx.hasErrors()) {
	            throw new QueryException(ctx.getErrorDescription());
	        }
	        return s;
	    }

	    private StatementTask executeSelection(SelectionStatement sel,
	            StatementHandler h) {
	    	Select select = sel.getSelect();
	    	List<Expression> exps = select.getFields();
	    	List<Attribute> fields = new ArrayList<Attribute>();
	    	for(Expression e: exps){
	    		if(e instanceof AttributeReference){
		    		String id = ((AttributeReference) e).getId();
		    		fields.add(Attribute.create(id, e.getType()));
	    		} else {
	    			Aggregate agg = (Aggregate) e;
	    			String id = ((AttributeReference)agg.getOperand()).getId();
		    		fields.add(Attribute.create(id, e.getType()));
	    		}
	    	}
	    	Object[] values = new Object[fields.size()];
	    	DataType type;
	    	for(int i = 0; i < values.length; i++){
	    		type = fields.get(i).getType();
	    		Object value = generateRandomValue(type.getId());
	    		values[i] = value;
	    	}
	    	Record record = new Record(fields, values);
	    	h.data(sel, record);
	        return new SelectionStatementTask(null);
	    }
	    
	    private Object generateRandomValue(String idType){
	    	  switch (idType.toLowerCase()) {
              case "id":
                  return DataType.ID;
              case "integer":
                  return generateRandomInteger();
              case "float":
                  return generateRandomFloat();
              case "string":
                  return generateRandomString();
              case "boolean":
            	  generateRandomBoolean();
              case "timestamp":
            	  generateRandomTimestamp();
              default:
                  return null;
          }
	    }
	    
	    private Integer generateRandomInteger(){
	    	Random random = new Random();
	    	return -10 + random.nextInt(50); 
	    }
	    
	    private Float generateRandomFloat(){
	    	Random random = new Random(); 
	    	return random.nextInt(50) - 10 + random.nextFloat(); 
	    }

	    private Boolean generateRandomBoolean(){
	    	Random random = new Random(); 
	    	return random.nextBoolean(); 
	    }
	    //TODO
	    private String generateRandomString(){
	    	String[] strings = new String[] { "cucina", "salotto", "meeting_room", "professore"};
	    	Random random = new Random();
	    	int index = random.nextInt(strings.length);
	    	return strings[index];
	    }
	    
	    private Instant generateRandomTimestamp(){
	    	return Instant.now();
	    }
	    private StatementTask executeCreation(CreationStatement cre,
	            StatementHandler h) {
	        throw new RuntimeException("unimplemented");
	    }

	    private StatementTask executeInsertion(InsertionStatement ins,
	            StatementHandler h) {
	        throw new RuntimeException("unimplemented");
	    }

	    private StatementTask executeSet(SetStatement set,
	            StatementHandler h) {
	        throw new RuntimeException("unimplemented");
	    }

		public StatementTask execute(Statement query, StatementHandler h)  throws QueryException {
	        Errors err = new Errors();

	        if (query instanceof SelectionStatement) {
	            SelectionStatement sel = (SelectionStatement) query;
	            return executeSelection(sel, h);

	        } else if (query instanceof CreationStatement) {
	            CreationStatement cre = (CreationStatement) query;
	            return executeCreation(cre, h);

	        } else if (query instanceof InsertionStatement) {
	            InsertionStatement ins = (InsertionStatement) query;
	            return executeInsertion(ins, h);

	        } else if (query instanceof SetStatement) {
	            SetStatement set = (SetStatement) query;
	            return executeSet(set, h);

	        } else {
	            throw new RuntimeException("Unknown statement type " +
	            		query.getClass().getName());
	        }
	}
		
		
}

