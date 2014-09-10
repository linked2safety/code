/*
 * Copyright (C) 2008-2012, fluid Operations AG
 *
 * FedX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.fluidops.fedx.evaluation.iterator;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.LookAheadIteration;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;

/**
 * Inserts original bindings into the result.
 * 
 * @author Andreas Schwarte
 */
public class IndependentJoingroupBindingsIteration2 extends LookAheadIteration<BindingSet, QueryEvaluationException>{

	// a pattern matcher for the binding resolver, pattern: myVar_%outerID%#bindingId, e.g. name_0#0
	protected static final Pattern PATTERN = Pattern.compile("(.*)_(.*)_(.*)");	
	
	protected final List<BindingSet> bindings;
	protected final CloseableIteration<BindingSet, QueryEvaluationException> iter;
	protected ArrayList<BindingSet> result = null;
	protected int currentIdx = 0;
	
	/**
	 * 
	 * @param iter
	 * @param bindings
	 */
	public IndependentJoingroupBindingsIteration2(CloseableIteration<BindingSet, QueryEvaluationException> iter, List<BindingSet> bindings) {
		this.bindings = bindings;
		this.iter = iter;
	}

	@Override
	protected BindingSet getNextElement() throws QueryEvaluationException {
		
		if (result==null) {
			result = computeResult();
		}
		
		if (currentIdx>=result.size()) {
			return null;
		}
		
		return result.get(currentIdx++);
	}

	
	/**
	 * 
	 * @return
	 * @throws QueryEvaluationException
	 */
	protected ArrayList<BindingSet> computeResult() throws QueryEvaluationException {
		
		List<BindingInfo> ares = new ArrayList<BindingInfo>();
		List<BindingInfo> bres = new ArrayList<BindingInfo>();
		
		// collect results XXX later asynchronously
		// assumes that bindingset of iteration has exactly one binding
		while (iter.hasNext()) {
			
			BindingSet bIn = iter.next();
			
			if (bIn.size()!=1) {
				throw new RuntimeException("For this optimization a bindingset needs to have exactly one binding, it has " + bIn.size() + ": " + bIn);
			}

			Binding b = bIn.getBinding( bIn.getBindingNames().iterator().next() );
			
			// name is something like myVar_%outerID%_bindingId, e.g. name_0_0
			Matcher m = PATTERN.matcher(b.getName());
			if (!m.find()) {
				throw new QueryEvaluationException("Unexpected pattern for binding name: " + b.getName());
			}
			
			BindingInfo bInfo = new BindingInfo(m.group(1), Integer.parseInt(m.group(3)), b.getValue());
			int bIndex = Integer.parseInt(m.group(2));
			
			// add a new binding info to the correct result list
			if (bIndex==0) {
				ares.add(bInfo);
			} else if (bIndex==1) {
				bres.add(bInfo);
			} else {
				throw new RuntimeException("Unexpected binding value.");
			}
		}
		
		ArrayList<BindingSet> res = new ArrayList<BindingSet>(ares.size() * bres.size());
		
		for (BindingInfo a : ares) {
			for (BindingInfo b : bres) {
				if (a.bindingsIdx!=b.bindingsIdx) {
					continue;
				}
				QueryBindingSet newB = new QueryBindingSet(bindings.size() + 2);
				newB.addBinding(a.name, a.value);
				newB.addBinding(b.name, b.value);
				newB.addAll(bindings.get(a.bindingsIdx));
				res.add(newB);
			}
		}
		
		return res;
	}
	
	
	/**
	 * 
	 * @author Andreas Schwarte
	 *
	 */
	protected class BindingInfo {
		public final String name;
		public final int bindingsIdx;
		public final Value value;
		
		/**
		 * 
		 * @param name
		 * @param bindingsIdx
		 * @param value
		 */
		public BindingInfo(String name, int bindingsIdx, Value value) {
			super();
			this.name = name;
			this.bindingsIdx = bindingsIdx;
			this.value = value;
		}
		
		/**
		 * @return String
		 */
		public String toString() {
			return name + ":" + value.stringValue();
		}
	}

}
