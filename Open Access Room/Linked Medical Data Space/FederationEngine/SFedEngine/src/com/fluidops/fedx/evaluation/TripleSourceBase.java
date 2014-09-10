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

package com.fluidops.fedx.evaluation;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.EmptyIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import com.fluidops.fedx.evaluation.iterator.GraphToBindingSetConversionIteration;
import com.fluidops.fedx.evaluation.iterator.SingleBindingSetIteration;
import com.fluidops.fedx.monitoring.Monitoring;
import com.fluidops.fedx.structures.Endpoint;
import com.fluidops.fedx.structures.QueryType;

/**
 * 
 * @author Andreas Schwarte
 *
 */
public abstract class TripleSourceBase implements TripleSource {
	protected final Monitoring monitoringService;
	protected final Endpoint endpoint;

	/**
	 * 
	 * @param monitoring
	 * @param endpoint
	 */
	public TripleSourceBase(Monitoring monitoring, Endpoint endpoint) {
		this.monitoringService = monitoring;
		this.endpoint = endpoint;
	}


	@Override
	public CloseableIteration<BindingSet, QueryEvaluationException> getStatements(
			String preparedQuery, RepositoryConnection conn, QueryType queryType)
			throws RepositoryException, MalformedQueryException,
			QueryEvaluationException {
		switch (queryType)
		{
		case SELECT:
			monitorRemoteRequest();
//			JOptionPane.showMessageDialog(null, "query:  "+preparedQuery);
			TupleQueryResult res = conn.prepareTupleQuery(QueryLanguage.SPARQL, preparedQuery).evaluate();
//			if(res.hasNext())
//				JOptionPane.showMessageDialog(null, res.next().getValue("number"));
			return res;
		case CONSTRUCT:
			monitorRemoteRequest();
			return new GraphToBindingSetConversionIteration(conn
					.prepareGraphQuery(QueryLanguage.SPARQL, preparedQuery)
					.evaluate());
		case ASK:
			monitorRemoteRequest();
			return booleanToBindingSetIteration(conn
					.prepareBooleanQuery(QueryLanguage.SPARQL, preparedQuery)
					.evaluate());
		default:
			throw new UnsupportedOperationException(
					"Operation not supported for query type " + queryType);
		}
	}
	
	
	@Override
	public boolean hasStatements(String preparedAskQuery,
			RepositoryConnection conn, BindingSet bindings)
			throws RepositoryException, MalformedQueryException,
			QueryEvaluationException 	{
		
		monitorRemoteRequest();
		return conn.prepareBooleanQuery(QueryLanguage.SPARQL, preparedAskQuery).evaluate();
	}


	/**
	 * @return void
	 */
	protected void monitorRemoteRequest() {
		monitoringService.monitorRemoteRequest(endpoint);
	}
	
	/**
	 * 
	 * @param hasResult
	 * @return
	 */
	private CloseableIteration<BindingSet, QueryEvaluationException> booleanToBindingSetIteration(boolean hasResult) {
		if (hasResult) {
			return new SingleBindingSetIteration(EmptyBindingSet.getInstance());
		}
		return new EmptyIteration<BindingSet, QueryEvaluationException>();
	}
	
}
