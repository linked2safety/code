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

package com.fluidops.fedx.monitoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.openrdf.query.algebra.TupleExpr;

import com.fluidops.fedx.Config;
import com.fluidops.fedx.exception.FedXRuntimeException;
import com.fluidops.fedx.structures.Endpoint;
import com.fluidops.fedx.structures.QueryInfo;


/**
 * Implementation supporting the following monitoring features:
 * 
 * - monitor remote requests per endpoint
 * - maintain a query backlog using {@link QueryLog}
 * 
 * 
 * @author andreas_s
 *
 */
public class MonitoringImpl implements MonitoringService {

	protected static Logger log = Logger.getLogger(Config.class);
	/**
	 * Map endpoints to their request information
	 */
	private final Map<Endpoint, MonitoringInformation> requestMap = new ConcurrentHashMap<Endpoint, MonitoringInformation>();
	private final QueryLog queryLog;
	
	/**
	 * 
	 */
	MonitoringImpl() {
		
		if (Config.getConfig().isLogQueries()) {
			try {
				queryLog = new QueryLog();
			} catch (IOException e) {
				log.error("an exception occured ", e);
				throw new FedXRuntimeException("QueryLog cannot be initialized: " + e.getMessage());
			}
		} else {
			queryLog=null;
		}
	}

	
	@Override
	public void monitorRemoteRequest(Endpoint e) {
		MonitoringInformation m = requestMap.get(e);
		if (m==null) {
			m = new MonitoringInformation(e);
			requestMap.put(e, m);
		}
		m.increaseRequests();
	}	

	@Override
	public MonitoringInformation getMonitoringInformation(Endpoint e) {
		return requestMap.get(e);
	}


	@Override
	public List<MonitoringInformation> getAllMonitoringInformation() {
		return new ArrayList<MonitoringInformation>(requestMap.values());
	}


	@Override
	public void resetMonitoringInformation() {
		requestMap.clear();		
	}
	
	/**
	 * 
	 * @author
	 *
	 */
	public static class MonitoringInformation { 
		private final Endpoint e;
		private int numberOfRequests = 0;
		/**
		 * 
		 * @param e
		 */
		public MonitoringInformation(Endpoint e){
			this.e = e;
		}
		/**
		 * 
		 */
		private void increaseRequests() {
			// TODO make thread safe
			numberOfRequests++;
		}
		/**
		 * 
		 */
		public String toString() {
			return e.getName() + " => " + numberOfRequests;
		}
		/**
		 * 
		 * @return
		 */
		public Endpoint getE() {
			return e;
		}
		/**
		 * 
		 * @return
		 */
		public int getNumberOfRequests(){
			return numberOfRequests;
		}		
	}

	@Override
	public void monitorQuery(QueryInfo query)	{
		if (queryLog!=null) {
			queryLog.logQuery(query);
		}
	}

	@Override
	public void logQueryPlan(TupleExpr tupleExpr) {
		QueryPlanLog.setQueryPlan(tupleExpr);		
	}	
}
