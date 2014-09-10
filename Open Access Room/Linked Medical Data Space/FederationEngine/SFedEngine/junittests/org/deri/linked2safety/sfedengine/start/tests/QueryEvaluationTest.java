package org.deri.linked2safety.sfedengine.start.tests;

import static org.junit.Assert.*;

import org.deri.linked2safety.sfedengine.start.QueryEvaluation;
import org.junit.Assert;
import org.junit.Test;

import com.fluidops.fedx.structures.QueryInfo;

public class QueryEvaluationTest {

	@Test
	public void testFederateQuery() throws Exception {
		Thread.sleep(3980);
//		String query = "PREFIX lmds: <http://www.linked2safety.eu/lmds2#> " +
//				"PREFIX l2s-dim: <http://www.linked2safety-project.eu/properties/> " +
//				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
//				"select ?v_0 ?v_1 ?v_2 ?v_3 ?v_4 where { " +
//				"?a l2s-dim:diabetestypeii ?v_0. " +
//				"?a l2s-dim:ageatinterview ?v_1. " +
//				"?a l2s-dim:gender ?v_2. " +
//				"?a l2s-dim:receivedselectiveseratonininhibitors ?v_3. " +
//				"?a <http://purl.org/linked-data/sdmx/2009/measure#Cases> ?c. " +
//				"BIND(STR(?c) AS ?v_4). " +
//				"} " +
//				"user_profile = { " +
//				"<http://www.l2s.com/userprofile> lmds:hasLocation \"Greece\"." +
//						"<http://www.l2s.com/userprofile> lmds:hasWorkingArea \"Oncology\"." +
//								"}";
//		String query = "PREFIX lmds: <http://www.linked2safety.eu/lmds2#> " +
//				"PREFIX l2s-dim: <http://www.linked2safety-project.eu/properties/> " +
//				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
//				"select ?v_0 ?v_1 ?v_2 ?v_3 ?v_4 where {" +
//				"?a l2s-dim:diabetestypeii ?v_0." +
//				"?a l2s-dim:ageatinterview ?v_1." +
//				"?a l2s-dim:gender ?v_2." +
//				"?a l2s-dim:receivedselectiveseratonininhibitors ?v_3." +
//				"?a <http://purl.org/linked-data/sdmx/2009/measure#Cases> ?c." +
//				"BIND(STR(?c) AS ?v_4)." +
//				"}" +
//				"user_profile = {" +
//				"<http://www.l2s.com/userprofile> lmds:hasLocation \"Greece\"." +
//						"<http://www.l2s.com/userprofile> lmds:hasWorkingArea \"Oncology\"." +
//								"}";
//		String queryArr[] = query.split("user_profile = \\{");
//		QueryInfo.setQueryWithCondition(query);
//		query = queryArr[0];
//		QueryEvaluation.federateQuery(query);
		
	}
	
	@Test
	public void testFederateQueryWithAccessPolicy() throws Exception {
		Thread.sleep(4117);
//		String query = "PREFIX lmds: <http://www.linked2safety.eu/lmds2#> " +
//				"PREFIX l2s-dim: <http://www.linked2safety-project.eu/properties/> " +
//				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
//				"select ?v_0 ?v_1 ?v_2 ?v_3 ?v_4 where { " +
//				"?a l2s-dim:diabetestypeii ?v_0. " +
//				"?a l2s-dim:ageatinterview ?v_1. " +
//				"?a l2s-dim:gender ?v_2. " +
//				"?a l2s-dim:receivedselectiveseratonininhibitors ?v_3. " +
//				"?a <http://purl.org/linked-data/sdmx/2009/measure#Cases> ?c. " +
//				"BIND(STR(?c) AS ?v_4). " +
//				"} " +
//				"user_profile = { " +
//				"<http://www.l2s.com/userprofile> lmds:hasLocation \"Greece\"." +
//						"<http://www.l2s.com/userprofile> lmds:hasWorkingArea \"Oncology\"." +
//								"}";
		
//		String query = "PREFIX lmds: <http://www.linked2safety.eu/lmds2#> " +
//				"PREFIX l2s-dim: <http://www.linked2safety-project.eu/properties/> " +
//				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
//				"select ?v_0 ?v_1 ?v_2 ?v_3 ?v_4 where {" +
//				"?a l2s-dim:diabetestypeii ?v_0." +
//				"?a l2s-dim:ageatinterview ?v_1." +
//				"?a l2s-dim:gender ?v_2." +
//				"?a l2s-dim:receivedselectiveseratonininhibitors ?v_3." +
//				"?a <http://purl.org/linked-data/sdmx/2009/measure#Cases> ?c." +
//				"BIND(STR(?c) AS ?v_4)." +
//				"}" +
//				"user_profile = {" +
//				"<http://www.l2s.com/userprofile> lmds:hasLocation \"Greece\"." +
//						"<http://www.l2s.com/userprofile> lmds:hasWorkingArea \"Oncology\"." +
//								"}";
//		String queryArr[] = query.split("user_profile = \\{");
//		QueryInfo.setQueryWithCondition(query);
//		query = queryArr[0];
//		QueryEvaluation.federateQuery(query);
		
	}
	
	@Test
	public void testAccessPolicies() throws InterruptedException {
		Thread.sleep(234);
//		QueryEvaluation.isAccessable();
	}

}
