/*
 * SFedEngine: Secure SPARQL Federation Engine for RDF data cubes

    Copyright (C) 2014  Yasar Khan <yasar.khan@insight-centre.org>
    
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.
    
    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
    This program incorporates the program covered by the following copyright and license 
    notice:
    
    
    Program name: FedX Federation Query Engine 
    
    Copyright (C) 2008-2013, fluid Operations AG
    
    FedX is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details. 
    
    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    You may download the Fedx source code and associated license at <http://www.fluidops.com/fedx/>.
 */

package org.deri.linked2safety.sfedengine.optimizer;

import info.aduna.iteration.CloseableIteration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.repository.RepositoryConnection;

import com.fluidops.fedx.EndpointManager;
import com.fluidops.fedx.FederationManager;
import com.fluidops.fedx.algebra.EmptyStatementPattern;
import com.fluidops.fedx.algebra.ExclusiveStatement;
import com.fluidops.fedx.algebra.StatementSource;
import com.fluidops.fedx.algebra.StatementSource.StatementSourceType;
import com.fluidops.fedx.algebra.StatementSourcePattern;
import com.fluidops.fedx.cache.Cache;
import com.fluidops.fedx.cache.CacheEntry;
import com.fluidops.fedx.cache.CacheUtils;
import com.fluidops.fedx.evaluation.TripleSource;
import com.fluidops.fedx.evaluation.concurrent.ControlledWorkerScheduler;
import com.fluidops.fedx.evaluation.concurrent.ParallelExecutor;
import com.fluidops.fedx.evaluation.concurrent.ParallelTask;
import com.fluidops.fedx.exception.ExceptionUtil;
import com.fluidops.fedx.exception.OptimizationException;
import com.fluidops.fedx.structures.Endpoint;
import com.fluidops.fedx.structures.QueryInfo;
import com.fluidops.fedx.structures.SubQuery;
import com.fluidops.fedx.util.QueryStringUtil;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;


/**
 * source selection 
 * 
 * @author Yasar Khan
 *
 */
public class SFedEngineSourceSelection {

	protected static Logger log = Logger.getLogger(SFedEngineSourceSelection.class);
	protected final List<Endpoint> endpoints;
	protected final Cache cache;
	protected final QueryInfo queryInfo;
	private String namedGraphs = "";
	long startTime = System.currentTimeMillis();
	long endTime = System.currentTimeMillis();
	String accessPolicyURL = "http://linked2safety.hcls.deri.org:3033/access_policy/sparql";
	
	
	/**
	 * 
	 * @param endpoints
	 * @param cache
	 * @param queryInfo
	 */
	public SFedEngineSourceSelection(List<Endpoint> endpoints, Cache cache, QueryInfo queryInfo) {
		this.endpoints = endpoints;
		this.cache = cache;
		this.queryInfo = queryInfo;
	}

	/**
	 * Map statements to their sources. Use synchronized access!
	 */
	protected  Map<StatementPattern, List<StatementSource>> stmtToSources = new ConcurrentHashMap<StatementPattern, List<StatementSource>>();  
	
	
	
	/**
	 * Method is responsible for doing source selection and access policy filtering
	 * @param stmts
	 */
	public void doSourceSelection(List<StatementPattern> stmts) {

		Set<String> sourcesSet = new HashSet<String>();

		boolean flag = false;
		
			ResultSet rsA = QueryInfo.getResultSet();
			
			HashMap<String, String> uniqueSources = new HashMap<String, String>();
			if(rsA != null) {
				while (rsA.hasNext()) {
					System.out
							.println("##########################################");
					QuerySolution qs = rsA.nextSolution();
					String se = qs.get("sparqlendpoint").toString();
					String gr = qs.get("graphuri").toString();
					String sparqlEndpoint[] = se.split("\\^");
					String graphURI[] = gr.split("\\^");
					String src = sparqlEndpoint[0];
					String graph = graphURI[0];
					namedGraphs += graph + "$$$";
					System.out.println(qs);
					System.out.println(src + "   ...........  " + graph);
					System.out
							.println("##########################################");
					String endpoint = "sparql_"
							+ src.replace("http://", "").replace("/", "_");
					for (StatementPattern stmt : stmts) {
						StatementSource stmtSrc = new StatementSource(endpoint, StatementSourceType.REMOTE);
						if(!stmtToSources.containsKey(stmt))
							stmtToSources.put(stmt, new ArrayList<StatementSource>());
						if(!stmtToSources.get(stmt).contains(stmtSrc)) {
							addSource(stmt, stmtSrc);
						}
					}
					
					flag = true;
				}
				
			}
			QueryInfo.setNamedGraphs(namedGraphs);
		if(flag == true) {
			
			for (StatementPattern stmt : stmtToSources.keySet()) {

				List<StatementSource> sources = stmtToSources.get(stmt);

				if (sources.size() > 1) {
					StatementSourcePattern stmtNode = new StatementSourcePattern(
							stmt, queryInfo);
					for (StatementSource s : sources) {
						stmtNode.addStatementSource(s);
					}
					stmt.replaceWith(stmtNode);
				} else if (sources.size() == 1) {
					stmt.replaceWith(new ExclusiveStatement(stmt, sources.get(0),
							queryInfo));
				} else {
					if (log.isDebugEnabled()) {
						log.debug("Statement "
								+ QueryStringUtil.toString(stmt)
								+ " does not produce any results at the provided sources, replacing node with EmptyStatementPattern.");
					}
					stmt.replaceWith(new EmptyStatementPattern(stmt));
				}
			}
		}
		else return;
		

	}
	  
	  
	/**
	 * Bind the finally selected sources to the corresponding statment
	 * @param stmt statement
	 * @param lstFinalSources sources to be bind
	 */
	public void bindSourcesToStatement(StatementPattern stmt,
			ArrayList<String> lstFinalSources) {
		
		for(String src:lstFinalSources) {
			String id = "sparql_" + src.replace("http://", "").replace("/", "_");
			addSource(stmt, new StatementSource(id, StatementSourceType.REMOTE));
		}
	}


	/**
	 * Retrieve a set of relevant sources for this query.
	 * @return
	 */
	public Set<Endpoint> getRelevantSources() {
		Set<Endpoint> endpointSet = new HashSet<Endpoint>();
		for (List<StatementSource> sourceList : stmtToSources.values())
			for (StatementSource source : sourceList)
				endpointSet.add( EndpointManager.getEndpointManager().getEndpoint(source.getEndpointID()));
		return endpointSet;
	}	
	
	/**
	 * Add a source to the given statement in the map (synchronized through map)
	 * 
	 * @param stmt
	 * @param source
	 */
	public void addSource(StatementPattern stmt, StatementSource source) {
		// The list for the stmt mapping is already initialized
		List<StatementSource> sources = stmtToSources.get(stmt);
//		JOptionPane.showMessageDialog(null, stmtToSources.isEmpty());
		synchronized (sources) {
			sources.add(source);
		}
	}
	
	
	
	/**
	 * 
	 * @author Andreas Schwarte
	 *
	 */
	protected static class SourceSelectionExecutorWithLatch implements ParallelExecutor<BindingSet> {
		
		/**
		 * Execute the given list of tasks in parallel, and block the thread until
		 * all tasks are completed. Synchronization is achieved by means of a latch.
		 * Results are added to the map of the source selection instance. Errors 
		 * are reported as {@link OptimizationException} instances.
		 * 
		 * @param tasks
		 */
		public static void run(SFedEngineSourceSelection sourceSelection, List<CheckTaskPair> tasks, Cache cache) {
			new SourceSelectionExecutorWithLatch(sourceSelection).executeRemoteSourceSelection(tasks, cache);
		}		
		
		private final SFedEngineSourceSelection sourceSelection;
		private ControlledWorkerScheduler<BindingSet> scheduler = FederationManager.getInstance().getJoinScheduler();
		private CountDownLatch latch;
		private boolean finished=false;
		private Thread initiatorThread;
		protected List<Exception> errors = new ArrayList<Exception>();
		

		/**
		 * 
		 * @param sourceSelection
		 */
		private SourceSelectionExecutorWithLatch(SFedEngineSourceSelection sourceSelection) {
			this.sourceSelection = sourceSelection;
		}

		/**
		 * Execute the given list of tasks in parallel, and block the thread until
		 * all tasks are completed. Synchronization is achieved by means of a latch
		 * 
		 * @param tasks
		 */
		private void executeRemoteSourceSelection(List<CheckTaskPair> tasks, Cache cache) {
			if (tasks.size()==0) {
				return;
			}
			
			initiatorThread = Thread.currentThread();
			latch = new CountDownLatch(tasks.size());
			for (CheckTaskPair task : tasks) {
				scheduler.schedule( new ParallelCheckTask(task.e, task.t, this) );
			}
			
			try	{
				// TODO maybe add timeout here
				latch.await();
			} catch (InterruptedException e) {
				log.debug("Error during source selection. Thread got interrupted.", e);
			}

			finished = true;
			
			// check for errors:
			if (errors.size()>0) {
				log.error(errors.size() + " errors were reported:");
				for (Exception e : errors) {
					log.error(ExceptionUtil.getExceptionString("Error occured", e));
				}
								
				Exception ex = errors.get(0);
				errors.clear();
				if (ex instanceof OptimizationException) {
					throw (OptimizationException)ex;
				}
				
				throw new OptimizationException(ex.getMessage(), ex);
			}
		}

		@Override
		public void run() { 
			/* not needed */
		}

		@Override
		public void addResult(CloseableIteration<BindingSet, QueryEvaluationException> res)	{
			latch.countDown();
		}

		@Override
		public void toss(Exception e) {
			errors.add(e);
			// abort all tasks belonging to this query id
			scheduler.abort(getQueryId());
			if (initiatorThread!=null) {
				initiatorThread.interrupt();
			}
		}

		@Override
		public void done()	{ 
			/* not needed */ 
		}

		@Override
		public boolean isFinished()	{
			return finished;
		}

		@Override
		public int getQueryId()	{
			return sourceSelection.queryInfo.getQueryID();
		}
	}
	
	
	/**
	 * 
	 * @author Andreas Schwarte
	 *
	 */
	public class CheckTaskPair {
		public final Endpoint e;
		public final StatementPattern t;
		/**
		 * 
		 * @param e
		 * @param t
		 */
		public CheckTaskPair(Endpoint e, StatementPattern t){
			this.e = e;
			this.t = t;
		}		
	}
	
	
	/**
	 * Task for sending an ASK request to the endpoints (for source selection)
	 * 
	 * @author Andreas Schwarte
	 */
	protected static class ParallelCheckTask implements ParallelTask<BindingSet> {

		protected final Endpoint endpoint;
		protected final StatementPattern stmt;
		protected final SourceSelectionExecutorWithLatch control;
		
		/**
		 * 
		 * @param endpoint
		 * @param stmt
		 * @param control
		 */
		public ParallelCheckTask(Endpoint endpoint, StatementPattern stmt, SourceSelectionExecutorWithLatch control) {
			this.endpoint = endpoint;
			this.stmt = stmt;
			this.control = control;
		}

		
		@Override
		public CloseableIteration<BindingSet, QueryEvaluationException> performTask() throws Exception {
			try {
				TripleSource t = endpoint.getTripleSource();
				RepositoryConnection conn = endpoint.getConn(); 

				boolean hasResults = t.hasStatements(stmt, conn, EmptyBindingSet.getInstance());

				SFedEngineSourceSelection sourceSelection = control.sourceSelection;
				CacheEntry entry = CacheUtils.createCacheEntry(endpoint, hasResults);
				sourceSelection.cache.updateEntry( new SubQuery(stmt), entry);

				if (hasResults) {
					sourceSelection.addSource(stmt, new StatementSource(endpoint.getId(), StatementSourceType.REMOTE));
				}
				
				return null;
			} catch (Exception e) {
				this.control.toss(e);
				throw new OptimizationException("Error checking results for endpoint " + endpoint.getId() + ": " + e.getMessage(), e);
			}
		}

		@Override
		public ParallelExecutor<BindingSet> getControl() {
			return control;
		}		
	}
	
		
}




