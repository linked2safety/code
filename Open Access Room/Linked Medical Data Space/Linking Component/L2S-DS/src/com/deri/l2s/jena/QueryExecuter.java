package com.deri.l2s.jena;

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
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.deri.l2s.dao.LinkedInfoDAO;
import com.deri.l2s.dao.ResultsDAO;
import com.deri.l2s.dao.TriplesDAO;
import com.deri.l2s.entities.Dataset;
import com.deri.l2s.entities.LinkedInfo;
import com.deri.l2s.entities.Result;
import com.deri.l2s.entities.Triple;
import com.deri.l2s.entities.Variable;
import com.deri.l2s.exception.DeadEndpointException;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.sparql.resultset.ResultSetMem;

public class QueryExecuter {
	
	public final String defaultQuery = " SELECT ?S ?P " +
			" WHERE {" +
			" ?S ?P ?var ." +
			" } ";
	
	public final String languageQuery = " SELECT ?S ?P " +
			" WHERE {" +
			" ?S ?P ?var @en. " +
			" } ";
	
	public final String advancedQuery = " SELECT ?S ?P ?O " +
			" WHERE {" +
			" ?S ?P ?O. " +
			" FILTER REGEX(?O, ?var, \"i\") " +
			" } ";
	
	
	public final String sameAsQuery = " SELECT ?O " +
			" WHERE { " +
			" ?sub <http://www.w3.org/2002/07/owl#sameAs> ?O." +
			" } ";
	
	public final String subjectQuery = "SELECT ?P " +
			"WHERE { " +
			" ?subject ?P ?O. " +
			" FILTER (REGEX(STR(?P), \"_vocabulary:x-\", \"i\")) " +
			"}";
	
	public final String subjectQuery2 = "SELECT ?P ?O " +
			"WHERE { " +
			" ?subject ?P ?O. " +
			" FILTER (REGEX(STR(?P), \"xref\", \"i\"))" +
			"}";
	
	private ResultsDAO resultsDAO;
	private TriplesDAO triplesDAO;
	private LinkedInfoDAO liDAO;
	private Triple triple;
	
	public QueryExecuter(){
		resultsDAO = new ResultsDAO();
		triplesDAO = new TriplesDAO();
		liDAO = new LinkedInfoDAO();
	}
	
	public void checkDefaultQuery(Dataset dataset, Variable var) throws DeadEndpointException{
		ParameterizedSparqlString paramString = new ParameterizedSparqlString(defaultQuery);
		Model m = ModelFactory.createDefaultModel();
		paramString.setLiteral("var", m.createTypedLiteral(var.getVariableName()));
		System.out.println(paramString.toString());
	}
	
	public void executeDefaultQueryOnEndpoint(Dataset dataset, Variable var) throws DeadEndpointException{
		try {
			ParameterizedSparqlString paramString = new ParameterizedSparqlString(defaultQuery);
			Model m = ModelFactory.createDefaultModel();
			paramString.setLiteral("var", m.createTypedLiteral(var.getVariableName()));
			System.out.println(paramString.toString());
			QueryEngineHTTP queryEngine = new QueryEngineHTTP(dataset.getEndpoint(), paramString.asQuery());
			queryEngine.addParam("timeout", "10000");
			ResultSet results = queryEngine.execSelect();
			ResultSetMem inMem = new ResultSetMem(results);
			Result r = new Result();
			if (inMem.size() > 0){
				r.setDataset(dataset);
				r.setVariable(var);
				r.setFrequency(inMem.size());
				r.setIsLike(false);
//				System.out.println("Inserting result record");
				resultsDAO.insert(r);
//				System.out.println("Done inserting record");
				writeResultsTriples(inMem, r, var.getVariableName());
			}
//			System.out.println("Done Running.");
			queryEngine.close();
//			qExec.close();
		} catch (QueryException e) {
			e.printStackTrace();
			throw new DeadEndpointException();
		} catch (org.apache.jena.atlas.web.HttpException e1){
			e1.printStackTrace();
			throw new DeadEndpointException();
		} catch (Exception e) {
			e.printStackTrace();
			throw new DeadEndpointException();
		}
	}
	
	public void executeLangAndCaseBasedQueries(Dataset dataset, Variable var) throws DeadEndpointException {
		this.executeLowerCaseQueryOnEndpoint(dataset, var);
		this.executeUpperCaseQueryOnEndpoint(dataset, var);
		this.executeProperCaseQueryOnEndpoint(dataset, var);
//		this.executeLanguageQueryOnEndpoint(dataset, var);
	}
	
	public void checkLangAndCaseBasedQueries(Dataset dataset, Variable var) throws DeadEndpointException {
		this.checkLowerCaseQuery(dataset, var);
		this.checkUpperCaseQuery(dataset, var);
		this.checkProperCaseQuery(dataset, var);
//		this.checkLangQuery(dataset, var);
	}
	
	public void checkLowerCaseQuery(Dataset dataset, Variable var){
		ParameterizedSparqlString paramString = new ParameterizedSparqlString(defaultQuery);
		Model m = ModelFactory.createDefaultModel();
		paramString.setLiteral("var", m.createTypedLiteral(var.getVariableName().toLowerCase()));
		System.out.println(paramString.toString());
		var.setVariableName(var.getVariableName().toLowerCase());
		this.checkLangQuery(dataset, var);
	}
	
	public void executeLowerCaseQueryOnEndpoint(Dataset dataset, Variable var) throws DeadEndpointException{
		try {
//			System.out.println("i am here..");
			ParameterizedSparqlString paramString = new ParameterizedSparqlString(defaultQuery);
			Model m = ModelFactory.createDefaultModel();
			paramString.setLiteral("var", m.createTypedLiteral(var.getVariableName().toLowerCase()));
//			System.out.println(paramString.toString());
			QueryExecution qExec = QueryExecutionFactory.sparqlService(dataset.getEndpoint(), paramString.asQuery());
			ResultSet results = qExec.execSelect();
			ResultSetMem inMem = new ResultSetMem(results);
			Result r = new Result();
//			System.out.println("Size of result " + inMem.size());
			if (inMem.size() > 0){
				r.setDataset(dataset);
				r.setVariable(var);
				r.setFrequency(inMem.size());
				r.setIsLike(false);
				resultsDAO.insert(r);
				List<Triple> triples = writeResultsTriples(inMem, r, var.getVariableName().toLowerCase());
				this.processCrossTriplesBySubject(triples, dataset);
//				this.processCrossTriples(crossLinks);
			}
			var.setVariableName(var.getVariableName().toLowerCase());
			this.executeLanguageQueryOnEndpoint(dataset, var);
			qExec.close();
		} catch (QueryException e) {
			e.printStackTrace();
			throw new DeadEndpointException();
		} catch (org.apache.jena.atlas.web.HttpException e1){
			e1.printStackTrace();
			throw new DeadEndpointException();
		} catch (Exception e) {
			e.printStackTrace();
			throw new DeadEndpointException();
		}
	}
	
	public void checkUpperCaseQuery(Dataset dataset, Variable var){
		ParameterizedSparqlString paramString = new ParameterizedSparqlString(defaultQuery);
		Model m = ModelFactory.createDefaultModel();
		paramString.setLiteral("var", m.createTypedLiteral(var.getVariableName().toUpperCase()));
		System.out.println(paramString.toString());
		var.setVariableName(var.getVariableName().toUpperCase());
		this.checkLangQuery(dataset, var);
	}
	
	public void executeUpperCaseQueryOnEndpoint(Dataset dataset, Variable var) throws DeadEndpointException{
		try {
			ParameterizedSparqlString paramString = new ParameterizedSparqlString(defaultQuery);
			Model m = ModelFactory.createDefaultModel();
			paramString.setLiteral("var", m.createTypedLiteral(var.getVariableName().toUpperCase()));
//			System.out.println(paramString.toString());
			QueryExecution qExec = QueryExecutionFactory.sparqlService(dataset.getEndpoint(), paramString.asQuery());
			ResultSet results = qExec.execSelect();
			ResultSetMem inMem = new ResultSetMem(results);
			Result r = new Result();
//			System.out.println("Size of result" + inMem.size());
			if (inMem.size() > 0){
				r.setDataset(dataset);
				r.setVariable(var);
				r.setFrequency(inMem.size());
				r.setIsLike(false);
				resultsDAO.insert(r);
				List<Triple> triples = writeResultsTriples(inMem, r, var.getVariableName().toUpperCase());
				this.processCrossTriplesBySubject(triples, dataset);
//				this.processCrossTriples(crossLinks);
			}
			var.setVariableName(var.getVariableName().toUpperCase());
			this.executeLanguageQueryOnEndpoint(dataset, var);
			qExec.close();
		} catch (QueryException e) {
			e.printStackTrace();
			throw new DeadEndpointException();
		} catch (org.apache.jena.atlas.web.HttpException e1){
			e1.printStackTrace();
			throw new DeadEndpointException();
		} catch (Exception e) {
			e.printStackTrace();
			throw new DeadEndpointException();
		}
	}
	
	public void checkProperCaseQuery(Dataset dataset, Variable var){
		ParameterizedSparqlString paramString = new ParameterizedSparqlString(defaultQuery);
		Model m = ModelFactory.createDefaultModel();
		paramString.setLiteral("var", m.createTypedLiteral(StringUtils.capitalize(var.getVariableName().toLowerCase())));
		System.out.println(paramString.toString());
		var.setVariableName(StringUtils.capitalize(var.getVariableName().toLowerCase()));
		this.checkLangQuery(dataset, var);
	}
	
	public void executeProperCaseQueryOnEndpoint(Dataset dataset, Variable var) throws DeadEndpointException{
		try {
			ParameterizedSparqlString paramString = new ParameterizedSparqlString(defaultQuery);
			Model m = ModelFactory.createDefaultModel();
			paramString.setLiteral("var", m.createTypedLiteral(StringUtils.capitalize(var.getVariableName().toLowerCase())));
//			System.out.println(paramString.toString());
			QueryExecution qExec = QueryExecutionFactory.sparqlService(dataset.getEndpoint(), paramString.asQuery());
			ResultSet results = qExec.execSelect();
			ResultSetMem inMem = new ResultSetMem(results);
			Result r = new Result();
//			System.out.println("Size of result" + inMem.size());
			if (inMem.size() > 0){
				r.setDataset(dataset);
				r.setVariable(var);
				r.setFrequency(inMem.size());
				r.setIsLike(false);
				resultsDAO.insert(r);
				List<Triple> triples = writeResultsTriples(inMem, r, StringUtils.capitalize(var.getVariableName().toLowerCase()));
				this.processCrossTriplesBySubject(triples, dataset);
//				this.processCrossTriples(crossLinks);
			}
			var.setVariableName(StringUtils.capitalize(var.getVariableName().toLowerCase()));
			this.executeLanguageQueryOnEndpoint(dataset, var);
			qExec.close();
		} catch (QueryException e) {
			e.printStackTrace();
			throw new DeadEndpointException();
		} catch (org.apache.jena.atlas.web.HttpException e1){
			e1.printStackTrace();
			throw new DeadEndpointException();
		} catch (Exception e) {
			e.printStackTrace();
			throw new DeadEndpointException();
		}
	}
	
	public void checkLangQuery(Dataset dataset, Variable var){
		ParameterizedSparqlString paramString = new ParameterizedSparqlString(languageQuery);
		paramString.setLiteral("var", var.getVariableName());
		System.out.println(paramString.toString());
	}
	
	public void executeLanguageQueryOnEndpoint(Dataset dataset, Variable var) throws DeadEndpointException{
		try {
			ParameterizedSparqlString paramString = new ParameterizedSparqlString(languageQuery);
			paramString.setLiteral("var", var.getVariableName());
//			System.out.println(paramString.toString());
			QueryExecution qExec = QueryExecutionFactory.sparqlService(dataset.getEndpoint(), paramString.asQuery());
			ResultSet results = qExec.execSelect();
			ResultSetMem inMem = new ResultSetMem(results);
			Result r = new Result();
//			System.out.println("Size of result" + inMem.size());
			if (inMem.size() > 0){
				r.setDataset(dataset);
				r.setVariable(var);
				r.setFrequency(inMem.size());
				r.setIsLike(false);
				resultsDAO.insert(r);
				List<Triple> triples = writeResultsTriples(inMem, r, var.getVariableName()+"@en");
				this.processCrossTriplesBySubject(triples, dataset);
//				this.processCrossTriples(crossLinks);
			}
			qExec.close();
		} catch (QueryException e) {
			e.printStackTrace();
			throw new DeadEndpointException();
		} catch (org.apache.jena.atlas.web.HttpException e1){
			e1.printStackTrace();
			throw new DeadEndpointException();
		} catch (Exception e) {
			e.printStackTrace();
			throw new DeadEndpointException();
		}
	}
	
	public void processCrossTriplesBySubject(List<Triple> triples, Dataset dataset){
		ParameterizedSparqlString paramString = null;
		
//		Getting for x-type
		paramString = new ParameterizedSparqlString(subjectQuery);
		for (Triple triple : triples) {
			String subject = triple.getSubject(); 
			paramString.setIri("subject", subject);
			QueryExecution qExec = QueryExecutionFactory.sparqlService(dataset.getEndpoint(), paramString.asQuery());
			ResultSet results = qExec.execSelect();
			ResultSetMem inMem = new ResultSetMem(results);
			while (inMem.hasNext()) {
				QuerySolution solution = inMem.nextSolution();
				RDFNode predicate = solution.get("P");
				RDFNode object = solution.get("O");
//				System.out.println("subject: " + subject.toString() + " predicate: " + predicate.toString() + " object: " + object.toString());
				String crossLink = predicate.toString();
				LinkedInfoDAO liDAO = new LinkedInfoDAO();
				crossLink = crossLink.substring(crossLink.lastIndexOf("-")+1, crossLink.length());
//				System.out.println("x-type " + crossLink + " subject " + triple.getSubject().toString() + " URL: " + dataset.getEndpoint());
				LinkedInfo li = new LinkedInfo();
				li.setLinkedsource(crossLink);
				li.setTriple(triple);
				liDAO.insert(li);
			}
			qExec.close();
		}
		
//		Getting for x-ref
		paramString = new ParameterizedSparqlString(subjectQuery2);
		for (Triple triple : triples) {
			String subject = triple.getSubject(); 
			paramString.setIri("subject", subject);
			QueryExecution qExec = QueryExecutionFactory.sparqlService(dataset.getEndpoint(), paramString.asQuery());
			ResultSet results = qExec.execSelect();
			ResultSetMem inMem = new ResultSetMem(results);
			while (inMem.hasNext()) {
				QuerySolution solution = inMem.nextSolution();
				RDFNode predicate = solution.get("P");
				RDFNode object = solution.get("O");
//				System.out.println("subject: " + subject.toString() + " predicate: " + predicate.toString() + " object: " + object.toString());
				String crossLink = object.toString();
				System.out.println(predicate + " : " + crossLink);
				LinkedInfoDAO liDAO = new LinkedInfoDAO();
				crossLink = crossLink.substring(crossLink.lastIndexOf("/")+1, crossLink.length());
				if(crossLink.contains(":")){
					crossLink = crossLink.substring(0, crossLink.lastIndexOf(":"));
					System.out.println("xref: " + crossLink + " subject " + triple.getSubject().toString() + " URL: " + dataset.getEndpoint());
					LinkedInfo li = new LinkedInfo();
					li.setLinkedsource(crossLink);
					li.setTriple(triple);
					liDAO.insert(li);
				}
			}
			qExec.close();
		}
	}
	
	public List<Triple> writeResultsTriples(ResultSetMem inMem, Result r, String varName){
//		System.out.println("Found");
		List<Triple> triples = new ArrayList<Triple>();
		RDFNode subject = null;
		if (inMem.size() < 5000) {
			while (inMem.hasNext()) {
				QuerySolution solution = inMem.nextSolution();
				Triple t = new Triple();
				subject = solution.get("S");
				RDFNode predicate = solution.get("P");
//				System.out.println("sub " + subject.toString() + " pred " + predicate.toString() + " obj " + varName);
				t.setSubject(subject.toString());
				t.setPredicate(predicate.toString());
				t.setObject(varName);
				t.setResult(r);
				triplesDAO.insert(t);
				triples.add(t);
			}
		}
		return triples;
	}
	
	public void executeAdvancedQueryOnEndpoint(Dataset dataset, Variable var) throws DeadEndpointException{
		try {
			System.out.print(".");
			ParameterizedSparqlString paramString = new ParameterizedSparqlString(advancedQuery);
			paramString.setLiteral("var", "^" + var.getVariableName());
			System.out.println(paramString.toString());
			QueryExecution qExec = QueryExecutionFactory.sparqlService(dataset.getEndpoint(), paramString.asQuery());
			ResultSet results = qExec.execSelect();
			ResultSetMem inMem = new ResultSetMem(results);
			Result r = new Result();
			if (inMem.hasNext()){
				r.setDataset(dataset);
				r.setVariable(var);
				r.setFrequency(inMem.size());
				r.setIsLike(true);
				resultsDAO.insert(r);
				writeResultsTriples(inMem, r, var.getVariableName());
			}
			qExec.close();
		} catch (QueryException e) {
			e.printStackTrace();
			throw new DeadEndpointException();
		} catch (org.apache.jena.atlas.web.HttpException e1){
			e1.printStackTrace();
			throw new DeadEndpointException();
		} catch (Exception e) {
			e.printStackTrace();
			throw new DeadEndpointException();
		}
	}
	
	public void processCrossTriples(List<String> crossLinks){
		LinkedInfoDAO liDAO = new LinkedInfoDAO();
		for (String crossLink : crossLinks) {
			crossLink = crossLink.substring(crossLink.lastIndexOf("/")+1, crossLink.length());
			if(crossLink.contains(":")){
				crossLink = crossLink.substring(0, crossLink.lastIndexOf(":"));
				System.out.println(crossLink);
				LinkedInfo li = new LinkedInfo();
				li.setLinkedsource(crossLink);
				li.setTriple(this.getTriple());
				liDAO.checkAndInsert(li);
			}
//			System.out.println(crossLink + " sub: " + this.getTriple().getSubject());
		}
	}
	
	public void executeCrossLinkingQuery(Triple triple) throws DeadEndpointException{
		try {
			ParameterizedSparqlString paramString = new ParameterizedSparqlString(sameAsQuery);
			paramString.setParam("sub", Node.createURI(triple.getSubject()));
			QueryExecution qExec = QueryExecutionFactory.sparqlService(triple.getResult().getDataset().getEndpoint(), paramString.asQuery());
			ResultSet results = qExec.execSelect();
			ResultSetMem inMem = new ResultSetMem(results);
			LinkedInfo li = new LinkedInfo();
			while (inMem.hasNext()){
				QuerySolution sol = inMem.nextSolution();
//				System.out.println(triple.getSubject() + " :sameAS " + sol.get("O"));
				li.setLinkedsource(sol.get("O").toString());
				li.setTriple(triple);
				liDAO.checkAndInsert(li);
			}
			qExec.close();
		} catch (org.apache.jena.atlas.web.HttpException e1){
			e1.printStackTrace();
			throw new DeadEndpointException();
		} catch (QueryParseException e) {
			System.out.println(e.getMessage() + " for subject " + triple.getSubject());
//			e.printStackTrace();
//			throw new DeadEndpointException();
		} catch (Exception e) {
			// TODO: handle exception
			throw new DeadEndpointException();
		}
	}
	
	public void executeCustomQueryOnEndpoint(String endpoint, String query){
		
	}

	public Triple getTriple() {
		return triple;
	}

	public void setTriple(Triple triple) {
		this.triple = triple;
	}

}
