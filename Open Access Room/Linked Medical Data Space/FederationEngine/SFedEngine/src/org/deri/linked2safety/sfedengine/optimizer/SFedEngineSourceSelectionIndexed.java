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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import javax.swing.JOptionPane;

import org.apache.log4j.Level;
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
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;


/**
 * source selection 
 * 
 * @author Yasar Khan
 *
 */
public class SFedEngineSourceSelectionIndexed {

	protected static Logger log = Logger.getLogger(SFedEngineSourceSelectionIndexed.class);
	
	protected final List<Endpoint> endpoints;
	protected final Cache cache;
	protected final QueryInfo queryInfo;
	String dsdSPARQLEndPoint = "http://localhost:3031/dsd/sparql";
	String accessPolicySPARQLEndPoint = "http://localhost:3032/access_policy/sparql";
	
	
	/**
	 * 
	 * @param endpoints
	 * @param cache
	 * @param queryInfo
	 */
	public SFedEngineSourceSelectionIndexed(List<Endpoint> endpoints, Cache cache, QueryInfo queryInfo) {
		this.endpoints = endpoints;
		this.cache = cache;
		this.queryInfo = queryInfo;
	}

	/**
	 * Map statements to their sources. Use synchronized access!
	 */
	protected  Map<StatementPattern, List<StatementSource>> stmtToSources = new ConcurrentHashMap<StatementPattern, List<StatementSource>>();
	
	
	   
	 long startTime = System.currentTimeMillis();
	 long endTime = System.currentTimeMillis();
	
	  
	  
	 /**
	  *  
	  * @param stmts
	  */
	 public void doSourceSelection(List<StatementPattern> stmts) {
		  
		  HashMap<StatementPattern, String> stmtSourceMap = new HashMap<StatementPattern, String>();
		  Set<String> sourcesSet = new HashSet<String>();
		  int count = 1;
		  for (StatementPattern stmt : stmts) {

			  stmtToSources.put(stmt, new ArrayList<StatementSource>());
			  
			  
			  String subject, predicate, object;
			  if(stmt.getSubjectVar().getValue() != null) {
				  subject = "<" + stmt.getSubjectVar().getValue().toString() + ">";
			  } else {
				  subject = "?"+stmt.getSubjectVar().getName();
			  }
			  if(stmt.getPredicateVar().getValue() != null) {
				  predicate = "<" + stmt.getPredicateVar().getValue().toString() + ">";
			  } else {
				  predicate = "?"+stmt.getPredicateVar().getName();
			  }
			  if(stmt.getObjectVar().getValue() != null) {
				  object = "<" + stmt.getObjectVar().getValue().toString() + ">";
			  } else {
				  object = "?"+stmt.getObjectVar().getName();
			  }
			  
			  String queryString = "SELECT DISTINCT ?g WHERE { GRAPH ?g { "+ subject + " "+ predicate + " " + object +" } }";
			  
			  Query query = QueryFactory.create(queryString);
			  QueryExecution qe = QueryExecutionFactory.sparqlService(dsdSPARQLEndPoint, query);
			  ResultSet rs = qe.execSelect();
			  
			  String sources = "";
			  Set<String> subSourcesSet = new HashSet<String>();
			  while(rs.hasNext()) {
				  QuerySolution qs = rs.next();
				  sources += qs.get("g").toString() + ";";
				  subSourcesSet.add(qs.get("g").toString());
			  }
			  
			  if(stmt.getPredicateVar().getValue() != null) {
				  String query1 = "SELECT DISTINCT ?g WHERE { GRAPH ?g { "+ predicate + " ?p ?o. } }";
				  QueryExecution qe1 = QueryExecutionFactory.sparqlService(dsdSPARQLEndPoint, query1);
				  ResultSet rs1 = qe1.execSelect();
				  
				  while(rs1.hasNext()) {
					  QuerySolution qs1 = rs1.next();
					  sources += qs1.get("g").toString() + ";";
					  subSourcesSet.add(qs1.get("g").toString());
				  }
				  
				  String query2 = "SELECT DISTINCT ?g WHERE { GRAPH ?g { ?s "+ predicate + " ?o. } }";
				  QueryExecution qe2 = QueryExecutionFactory.sparqlService(dsdSPARQLEndPoint, query2);
				  ResultSet rs2 = qe2.execSelect();
				  
				  while(rs2.hasNext()) {
					  QuerySolution qs2 = rs2.next();
					  sources += qs2.get("g").toString() + ";";
					  subSourcesSet.add(qs2.get("g").toString());
				  }
				  
				  String query3 = "SELECT DISTINCT ?g WHERE { GRAPH ?g { ?s ?p "+ predicate + ". } }";
				  QueryExecution qe3 = QueryExecutionFactory.sparqlService(dsdSPARQLEndPoint, query3);
				  ResultSet rs3 = qe3.execSelect();
				  
				  while(rs3.hasNext()) {
					  QuerySolution qs3 = rs3.next();
					  sources += qs3.get("g").toString() + ";";
					  subSourcesSet.add(qs3.get("g").toString());
				  }
			  }
			  
			  if(count == 1) {
				  sourcesSet.addAll(subSourcesSet);
			  } else {
				  sourcesSet.retainAll(subSourcesSet);
			  }
			  stmtSourceMap.put(stmt, sources);
			  
			  count++;
		  }

		  Iterator<String> iter1 = sourcesSet.iterator();
		  Set<String> sourceSet1 = new HashSet<String>();
		  while(iter1.hasNext()) {
			  sourceSet1.add(iter1.next());
		  }
		  Iterator<String> iter = sourceSet1.iterator();
		  while(iter.hasNext()) {
			  String graphID = iter.next();
			  String queryStringAP = "PREFIX acl: <http://www.w3.org/ns/auth/acl#>" +
				  		"ASK WHERE { " +
				  			"?accessPolicy <http://www.linked2safety.eu/lmds2#appliesToNamedGraph> ?namedGraph. " +
				  			"?accessPolicy a <http://www.linked2safety.eu/lmds2#AccessPolicy>. " +
				  			"?namedGraph <http://www.linked2safety.eu/lmds2#hasGraphURI> '" + graphID + "'. " +
				  			"?accessPolicy <http://www.linked2safety.eu/lmds2#grantsAccess> acl:Read_l2s. " +
				  			"?accessPolicy <http://www.linked2safety.eu/lmds2#hasUserProfile> <http://www.linked2safety.eu/lmds2#UserProfile_1>." +
				  		"}";
				  Query accessPolicyQuery = QueryFactory.create(queryStringAP);
				  System.out.println(accessPolicyQuery.toString());
				  QueryExecution qeAP = QueryExecutionFactory.sparqlService(accessPolicySPARQLEndPoint, accessPolicyQuery);
				  boolean rsAP = qeAP.execAsk();
				  if(!rsAP) {
					  sourcesSet.remove(graphID);
				  }
		  }
		  
		  for (StatementPattern stmt : stmts) {
			  Iterator<String> sourceIter = sourcesSet.iterator();
			  while(sourceIter.hasNext()) {
				  String sourceStr = sourceIter.next();
//				  queryInfo.setQueryNG(sourceStr);
				  String q = "SELECT ?endpoint WHERE { " +
				  		"?NG <http://www.linked2safety.eu/lmds2#hasGraphURI> '" + sourceStr + "'.  " +
				  		"?NG <http://www.linked2safety.eu/lmds2#hasSparqlEndpoint> ?endpoint." +
				  		"}";
				  Query query = QueryFactory.create(q);
				  QueryExecution qe = QueryExecutionFactory.sparqlService(accessPolicySPARQLEndPoint, query);
				  ResultSet rs = qe.execSelect();
				  QuerySolution qs = rs.next();
				  String[] strArr = qs.get("endpoint").toString().split("\\^");
//				  JOptionPane.showMessageDialog(null, strArr[0]);
				  String endpoint = "sparql_" + strArr[0].replace("http://", "").replace("/", "_");
				  addSource(stmt, new StatementSource(endpoint, StatementSourceType.REMOTE));
			  }
			  
		  }
		  
		  for (StatementPattern stmt : stmtToSources.keySet()) {
				List<StatementSource> sources = stmtToSources.get(stmt);
				
				// if more than one source -> StatementSourcePattern
				// exactly one source -> OwnedStatementSourcePattern
				// otherwise: No resource seems to provide results
				
				if (sources.size()>1) {
					StatementSourcePattern stmtNode = new StatementSourcePattern(stmt, queryInfo);
					for (StatementSource s : sources) {
						stmtNode.addStatementSource(s);
					}
					stmt.replaceWith(stmtNode);
				} else if (sources.size()==1) {
					stmt.replaceWith( new ExclusiveStatement(stmt, sources.get(0), queryInfo));
				} else {
					if (log.isDebugEnabled()) {
						log.debug("Statement " + QueryStringUtil.toString(stmt) + " does not produce any results at the provided sources, replacing node with EmptyStatementPattern." );
					}
						
					stmt.replaceWith( new EmptyStatementPattern(stmt));
				}
			}
		  
		  
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
		for (List<StatementSource> sourceList : stmtToSources.values()) {
			for (StatementSource source : sourceList) {
				endpointSet.add( EndpointManager.getEndpointManager().getEndpoint(source.getEndpointID()));
			}
		}
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
		public static void run(SFedEngineSourceSelectionIndexed sourceSelection, List<CheckTaskPair> tasks, Cache cache) {
			new SourceSelectionExecutorWithLatch(sourceSelection).executeRemoteSourceSelection(tasks, cache);
		}		
		
		private final SFedEngineSourceSelectionIndexed sourceSelection;
		private ControlledWorkerScheduler<BindingSet> scheduler = FederationManager.getInstance().getJoinScheduler();
		private CountDownLatch latch;
		private boolean finished=false;
		private Thread initiatorThread;
		protected List<Exception> errors = new ArrayList<Exception>();
		

		private SourceSelectionExecutorWithLatch(SFedEngineSourceSelectionIndexed sourceSelection) {
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

				SFedEngineSourceSelectionIndexed sourceSelection = control.sourceSelection;
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




