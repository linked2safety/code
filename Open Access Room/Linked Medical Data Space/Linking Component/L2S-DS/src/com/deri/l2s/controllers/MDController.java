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

import java.io.InputStream;

import com.deri.l2s.dao.DatasetDAO;
import com.deri.l2s.dao.ResultsDAO;
import com.deri.l2s.dao.TriplesDAO;
import com.deri.l2s.dao.VariablesDAO;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

public class MDController {
	
	private ResultsDAO resultsDAO;
	private DatasetDAO datasetDAO;
	private VariablesDAO variablesDAO;
	private TriplesDAO triplesDAO;
	
	String repoA_DS1 = "data/repoA/BMI-diabetes-hypertension.ttl";
	String repoA_DS2 = "data/repoA/BMI-diabetes-smokingEver.ttl";
	String repoA_DS3 = "data/repoA/educat-ethn-weight-gender.ttl";
	String repoA_DS4 = "data/repoA/weight-diabetes-eth-gender.ttl";
	
	String repoB_DS1 = "data/repoB/BMI-diabetes-dislipidemia.ttl";
	String repoB_DS2 = "data/repoB/BMI-diabetes-hypertension.ttl";
	String repoB_DS3 = "data/repoB/datacubeOR.ttl";
	String repoB_DS4 = "data/repoB/fakeDataCSVdatacube.ttl";
	
	public MDController(){
		resultsDAO = new ResultsDAO();
		datasetDAO = new DatasetDAO();
		variablesDAO = new VariablesDAO();
		triplesDAO = new TriplesDAO();
	}
	
	public void processModel(){
		String fName = repoA_DS2;
		InputStream in = FileManager.get().open(fName);
		if (in == null) {
		    throw new IllegalArgumentException(
		                                 "File: " + fName + " not found");
		}
		Model m = ModelFactory.createDefaultModel();
		m.read(in, null, "TTL");
		System.out.println(m.size());
		
		StmtIterator ite = m.listStatements();
		while(ite.hasNext()){
			Statement stmt = ite.nextStatement();
			System.out.println(stmt.toString());
		}
		
//		m.read(in, null, "TTL");
//		System.out.println(m.size());
	}

}
