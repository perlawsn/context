package org.dei.perla.context;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.dei.perla.lang.query.statement.SetParameter;
import org.dei.perla.lang.query.statement.SetStatement;
import org.dei.perla.lang.query.statement.Statement;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class ConflictDetector implements IConflictDetector {

	@Override
	public boolean isInConflict(Context context, List<Context> activeContexts) {
		for(Context c: activeContexts){
			if(isInConflict(context, c))
				return true;
		}
		return false;
	}

	/*
	 * Due contexts are in conflict if they have at least a SET query of the same attribute on the same FPC
	 */
	public boolean isInConflict(Context a, Context b) {
		List<Statement> setA = a.getEnable().stream().
				filter(e -> (e instanceof SetStatement)).collect(Collectors.toList());
		List<Statement> setB = b.getEnable().stream().
				filter(e -> (e instanceof SetStatement)).collect(Collectors.toList());
		
		//variable for temporary storing the SetParameter of A
		Multimap<String, Integer> map = ArrayListMultimap.create();
		for(Statement stat: setA) {
			SetStatement set = (SetStatement) stat;
			for(SetParameter p: set.getParameters()){
				map.putAll(p.getAttribute().getId(), set.getIds());
			}
		}
		for(Statement stat: setB) {
			SetStatement set = (SetStatement) stat;
			for(SetParameter p: set.getParameters()){
				if(map.containsKey(p.getAttribute().getId())){
					for(Integer id: set.getIds()){
						if(map.containsEntry(p.getAttribute().getId(), id))
							return true;
					}
				}
			}
		}
		return false;
	}

}
