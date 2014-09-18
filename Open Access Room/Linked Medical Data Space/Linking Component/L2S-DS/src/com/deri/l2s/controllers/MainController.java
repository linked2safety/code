package com.deri.l2s.controllers;

/*
 *  Linked2Safety SPARQL Endpoint Discovery and Linking Framework
 *  
 *  Copyright (C) 2014 Muntazir Mehdi <muntazir.mehdi@insight-centre.org>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.deri.l2s.dao.DatasetDAO;
import com.deri.l2s.dao.ResultsDAO;
import com.deri.l2s.dao.TriplesDAO;
import com.deri.l2s.dao.VariablesDAO;
import com.deri.l2s.entities.Dataset;
import com.deri.l2s.entities.Result;
import com.deri.l2s.entities.Triple;
import com.deri.l2s.entities.Variable;
import com.deri.l2s.exception.DeadEndpointException;
import com.deri.l2s.jena.QueryExecuter;
import com.deri.l2s.support.Stopwords;
import com.deri.l2s.support.VariableReader;

public class MainController {
	
	private ResultsDAO resultsDAO;
	private DatasetDAO datasetDAO;
	private VariablesDAO variablesDAO;
	private TriplesDAO triplesDAO;
	private QueryExecuter qE;
	
	public MainController(){
		resultsDAO = new ResultsDAO();
		datasetDAO = new DatasetDAO();
		variablesDAO = new VariablesDAO();
		triplesDAO = new TriplesDAO();
		qE = new QueryExecuter();
	}
	
	public void collectResultsWithTimeout(int timeoutSecs){
		ExecutorService exec = Executors.newFixedThreadPool(1);
		Future<?> future = exec.submit(new Runnable() {
			
			@Override
			public void run() {
				try {
					collectResults();
				} catch (Exception e) {
					System.out.println("DBException");
					// TODO: handle exception
				}
			}
		});
		
		try {
			future.get(timeoutSecs, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			future.cancel(true);
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			future.cancel(true);
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			future.cancel(true);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			future.cancel(true);
		}
	}
	
	public void collectResults(){
		
		List<Dataset> datasets = datasetDAO.getAllDatasets();
//		List<Dataset> datasets = datasetDAO.getSomeDatasets();
//		List<Dataset> datasets = datasetDAO.getDataById(0);
		List<Variable> vars = variablesDAO.getAllVariables();
//		List<Variable> vars = variablesDAO.getSomeVariables();
//		List<Variable> vars = variablesDAO.getVariableById(5);
		for (Dataset dataset : datasets) {
//			System.out.println();
//			System.out.println("Running on Dataset : " + dataset.getName());
			for (Variable var : vars) {
				try {
//					System.out.print("." + var.getVariableName());
					if (!dataset.getFlag()){
//						qE.checkDefaultQuery(dataset, var);
//						qE.executeDefaultQueryOnEndpoint(dataset, var);
						
//						qE.checkLangAndCaseBasedQueries(dataset, var);
						qE.executeLangAndCaseBasedQueries(dataset, var);
					} else {
						break;
					}
//					if (!resultsDAO.existsForDatasetAndVariable(dataset, var)){
//						System.out.print("." + var.getVariableName());
//						if (!dataset.getFlag()){
////							qE.executeLanguageQueryOnEndpoint(dataset, var);
////							qE.executeDefaultQueryOnEndpoint(dataset, var);
//							qE.executeLangAndCaseBasedQueries(dataset, var);
////							qE.executeQueriesWithTimeout(dataset, var, 10);
//						} else {
//							break;
//						}
//					}
				} catch (DeadEndpointException e) {
					e.printStackTrace();
					datasetDAO.flagDataset(dataset);
					break;
				}
			}
		}
		
	}
	
	public void collectVariables(){
		VariableReader vr = new VariableReader();
		String[] vars = vr.parseVariables();
		String[] cleanVars = vr.cleanVariables(vars);
		for (String cleanVar : cleanVars) {
			Variable v = new Variable();
			v.setVariableName(cleanVar);
			v.setVariableCode("CHUV");
			variablesDAO.checkAndInsert(v);
		}
		
	}
	
	public void breakVariables(){
		List<Variable> vars = variablesDAO.getAllVariables();
		Set<String> tokenSet = new HashSet<String>();
		Stopwords stopWords = new Stopwords();
		for (Variable variable : vars) {
			String name = variable.getVariableName();
			name = name.replaceAll("/", " ");
			String[] tokens = getSpaceSeperatedTokens(name);
			for (String token : tokens) {
				token.trim();
				token = token.replace("(", "");
				token = token.replace(")", "");
				token = token.replace("[", "");
				token = token.replace("]", "");
				if (!stopWords.isStopword(token) && !stopWords.isNumeric(token)){
					tokenSet.add(token);
				}
			}
		}
		for (String cleanToken : tokenSet) {
			Variable v = new Variable();
			v.setVariableName(cleanToken);
			v.setVariableCode("BROKEN");
			variablesDAO.checkAndInsert(v);
		}
		
	}
	
	public String[] getSpaceSeperatedTokens(String sentence){
		String[] tokens = sentence.split("\\s+");
		return tokens;
	}
	
	public void collectLinks(){
		List<Variable> vars = resultsDAO.getUniqueVariableFromResults();
		for (Variable variable : vars) {
			Set<Result> results = variable.getResults();
//			System.out.println("Running for variable: " + variable.getVariableName());
//			System.out.println("On Datasets: ");
//			for (Result result : results) {
//				System.out.println(result.getDataset().getName());
//			}
//			System.out.println(".............................................................");
			for (Result result : results) {
				Set<Triple> triples = result.getTriples();
				for (Triple triple : triples) {
					try {
						qE.executeCrossLinkingQuery(triple);
					} catch (DeadEndpointException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
//			System.out.println("---------------------------------------------------------");
		}
		
	}

}
