package org.dei.perla.context;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.fpc.DataType;
import org.dei.perla.lang.parser.OnEmptySelection;
import org.dei.perla.lang.parser.ParserContext;
import org.dei.perla.lang.parser.ast.*;
import org.dei.perla.lang.query.expression.BoolOperation;
import org.dei.perla.lang.query.statement.RatePolicy;
import org.dei.perla.lang.query.statement.RefreshType;
import org.dei.perla.lang.query.statement.WindowSize;
import org.dei.perla.lang.query.statement.WindowSize.WindowType;

public class ComposeBlock {
	
	public StatementAST composeEnableComponent(List<SelectionStatementAST> sels, ParserContext ctx) {
		List<WindowSizeAST> everyList = new ArrayList<WindowSizeAST>();			
		List<FieldSelectionAST> fieldList = new ArrayList<FieldSelectionAST>();
		List<String> groupByFields = new ArrayList<String>();
		List<ExpressionAST> havingList = new ArrayList<ExpressionAST>();				
		List<WindowSizeAST> upToList = new ArrayList<WindowSizeAST>();
		List<SamplingAST> samplingList = new ArrayList<SamplingAST>();
		List<ExpressionAST> whereList = new ArrayList<ExpressionAST>();
		List<ExecutionConditionsAST> execCondList = new ArrayList<ExecutionConditionsAST>();
		List<WindowSizeAST> terminateList = new ArrayList<WindowSizeAST>();
		
		for(SelectionStatementAST sel: sels){
			everyList.add(sel.getEvery());
			fieldList.addAll(sel.getFields());
			if(sel.getGroupBy() != null){
				groupByFields.addAll(sel.getGroupBy().getFields());
			}
			havingList.add(sel.getHaving());
			upToList.add(sel.getUpto());
			samplingList.add(sel.getSamplingAST());
			whereList.add(sel.getWhere());
			execCondList.add(sel.getExecutionConditions());
			terminateList.add(sel.getTerminateAfter());
		}
		
		WindowSizeAST every = composeWindowSize(everyList, ctx);
		ExpressionAST having = composeHaving(havingList, ctx);				
		WindowSizeAST upto = composeWindowSize(upToList, ctx);
		OnEmptySelection oes = OnEmptySelection.NOTHING;
		SamplingAST sampling = composeSampling(samplingList, ctx);
		ExpressionAST where = composeWhere(whereList, ctx);
		ExecutionConditionsAST execCond = composeExecCond(execCondList, ctx);
		WindowSizeAST terminate = composeTerminate(terminateList, ctx);
		
		return new SelectionStatementAST(every, fieldList, 
				new GroupByAST(groupByFields), having, upto, oes, sampling,
                where, execCond, terminate);
	}
	
	public WindowSizeAST composeWindowSize(List<WindowSizeAST> everyASTList, ParserContext ctx){
		if(everyASTList.isEmpty())
			return WindowSizeAST.ONE;
		ParserContext ctx2 = new ParserContext();
		WindowType type = everyASTList.get(0).getType();
		List<WindowSize> everyList = new ArrayList<WindowSize>();
		for(WindowSizeAST w: everyASTList){
			if(w.getType() != type){
				ctx2.addError("EVERY clauses must be of the same type");
				break;
			}
			everyList.add(w.compile(ctx2));
		}
		if(!ctx2.hasErrors()){
			WindowSize result = null;
			switch (type) {
	         case TIME: {
	             return findMinDuration(everyASTList, ctx);
	         }
	         case SAMPLE: {
	             result = findMinSamples(everyList, ctx);
	             WindowSizeAST aa = new WindowSizeAST(null, new ConstantAST(result.getSamples(), DataType.INTEGER));
	             System.out.println(result);
	             return aa;
	         }
	         default:
	             throw new RuntimeException("Unexpected WindowSize type " +
	                     type);
			}
		}
		else{
			ctx.addError(ctx2.getErrorDescription());
			return WindowSizeAST.ONE;
		}
	}
	
	private WindowSize findMinSamples(List<WindowSize> everyList, ParserContext ctx){
		Collections.sort(everyList, new Comparator<WindowSize>(){
		    public int compare(WindowSize wa, WindowSize wb){
		        if(wa.getSamples() > wb.getSamples())
		        	return 1;
		        if(wa.getSamples() < wb.getSamples())
		        	return -1;
		        else return 0;
		    }
		});
		return everyList.get(0);
	}
	
	private WindowSizeAST findMinDuration(List<WindowSizeAST> everyListAST, ParserContext ctx){
		Collections.sort(everyListAST, new Comparator<WindowSizeAST>(){
		    public int compare(WindowSizeAST wa, WindowSizeAST wb){
					Duration da = Duration.of(wa.getDurationValue().evalIntConstant(ctx), 
							wa.getDurationUnit());
					Duration db = Duration.of(wb.getDurationValue().evalIntConstant(ctx), 
							wb.getDurationUnit());
					return da.compareTo(db);
		    }
		});
		return everyListAST.get(0);
	}
	
	public ExpressionAST composeHaving(List<ExpressionAST> havingList, ParserContext ctx){
		if(havingList.isEmpty())
			return ConstantAST.TRUE;
		ExpressionAST having = havingList.get(0);
		int size = havingList.size();
		for(int i=1; i<size; i++){
			having = new BoolAST(BoolOperation.AND, having, havingList.get(i));
		}
		return having;
	}
	
	public SamplingAST composeSampling(List<SamplingAST> samplingList, ParserContext ctx){
		int event = 0;
		int ifEvery = 0;
		for(SamplingAST s: samplingList){
			if(s instanceof SamplingEventAST) event++;
			else ifEvery++;
		}
		if(event > 0 && ifEvery > 0)
			ctx.addError("SAMPLING clauses are incompatible");
		if(!ctx.hasErrors()){
			if(event > 0)
				return composeEventSampling(samplingList);
			else
				return composeIfEverySampling(samplingList, ctx);
		}
		return null;
	}
	
	private SamplingAST composeEventSampling(List<SamplingAST> samplingList){
		List<String> events = new ArrayList<String>();
		for(SamplingAST s: samplingList){
			SamplingEventAST e = (SamplingEventAST) s;
			events.addAll(e.getEvents());
		}
		return new SamplingEventAST(events);
	}
	
	private SamplingAST composeIfEverySampling(List<SamplingAST> samplingList, ParserContext ctx){
		List<IfEveryAST> ifevery = new ArrayList<IfEveryAST>();
		List<RefreshAST> refreshes = new ArrayList<RefreshAST>();
		for(SamplingAST s: samplingList){
			SamplingIfEveryAST e = (SamplingIfEveryAST) s;
			ifevery.addAll(e.getIfEvery());
			refreshes.add(e.getRefresh());
		}
		RefreshAST refresh = this.findMinRefreshValue(refreshes, ctx);
		return new SamplingIfEveryAST(ifevery, RatePolicy.STRICT, refresh);
	}
	
	public ExpressionAST composeWhere(List<ExpressionAST> whereList, ParserContext ctx){
		if(whereList.isEmpty())
			return ConstantAST.TRUE;
		ExpressionAST where = whereList.get(0);
		int size = whereList.size();
		for(int i=1; i<size; i++){
			where = new BoolAST(BoolOperation.AND, where, whereList.get(i));
		}
		return where;
	}
	
	public ExecutionConditionsAST composeExecCond(
			List<ExecutionConditionsAST> execCondList, ParserContext ctx){
		if(execCondList.isEmpty())
			return ExecutionConditionsAST.ALWAYS;
		List<ExpressionAST> conds = new ArrayList<ExpressionAST>();
		List<Attribute> atts = new ArrayList<Attribute>();
		List<RefreshAST> refreshes = new ArrayList<RefreshAST>();
		for(ExecutionConditionsAST e: execCondList){
			conds.add(e.getCondition());
			atts.addAll(e.getSpecifications().getSpecifications());
			refreshes.add(e.getRefresh());
		}
		ExpressionAST cond = conds.get(0);
		int size = conds.size();
		for(int i = 1; i<size; i++){
			cond = new BoolAST(BoolOperation.AND, cond, conds.get(i));
		}
		NodeSpecificationsAST specs = new NodeSpecificationsAST(atts);
		RefreshAST refresh = findMinRefreshValue(refreshes, ctx);
		return new ExecutionConditionsAST(cond, specs, refresh);
	}
	
	private RefreshAST findMinRefreshValue(List<RefreshAST> refreshes, ParserContext ctx){
		RefreshType type = RefreshType.NEVER;
		int event = 0;
		int time = 0;
		for(RefreshAST r: refreshes){
			if(r.getType() == RefreshType.TIME){
				type = RefreshType.TIME;
				time++;
			}
			if(r.getType() == RefreshType.EVENT){
				event++;
				type = RefreshType.EVENT;
			}
		}
		if(time>0 && event>0){
			ctx.addError("REFRESH clauses in EXECUTION CONDITION clauses are incompatible");
		}
		else{
			switch (type) {
	        case NEVER:
	            return RefreshAST.NEVER;
	        case EVENT: {
	        	List<String> events = new ArrayList<String>();
	            for(RefreshAST r: refreshes){
	            	events.addAll(r.getEvents());
	            }
	            return new RefreshAST(null, events);
	            }
	        case TIME: 
	        	return find(refreshes, ctx);
	        default:
	            throw new RuntimeException("Unknown refresh type " + type);
			}
		}
		return null;
	}
	
	private RefreshAST find(List<RefreshAST> refreshes, ParserContext ctx){
		refreshes = refreshes.stream().filter(e -> e.getType()!=RefreshType.NEVER).collect(Collectors.toList());
		Collections.sort(refreshes, new Comparator<RefreshAST>(){
		    public int compare(RefreshAST ra, RefreshAST rb){
		        Duration da = Duration.of(ra.getDurationValue().evalIntConstant(ctx),
		        		ra.getDurationUnit());
		        Duration db = Duration.of(rb.getDurationValue().evalIntConstant(ctx),
		        		rb.getDurationUnit());
		        return da.compareTo(db);
		    }
			});
		return refreshes.get(0);
	}
	
	public WindowSizeAST composeTerminate(List<WindowSizeAST> terminateList,
			ParserContext ctx){
		if(terminateList.isEmpty())
			return null;
		terminateList = terminateList.stream().filter(t -> t!=null).collect(Collectors.toList());
		return this.composeWindowSize(terminateList, ctx);
	}

}
