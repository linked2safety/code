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

package org.deri.linked2safety.sfedengine.start;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.repository.sail.SailRepository;

import repository.MissingUserProfileException;
import repository.PolicyRepository;

import com.fluidops.fedx.Config;
import com.fluidops.fedx.FedXFactory;
import com.fluidops.fedx.FederationManager;
import com.fluidops.fedx.monitoring.MonitoringUtil;
import com.fluidops.fedx.structures.QueryInfo;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;

/**
 * 
 * @author Yasar Khan
 * 
 * @param <repo>
 */
public class QueryEvaluation<repo> {

	protected static Logger log = Logger.getLogger(QueryEvaluation.class);
	static String accessPolicyURL = "http://linked2safety.hcls.deri.org:3033/access_policy/sparql";

	/**
	 * 
	 * @param sparqlQuery
	 * @return InputStream
	 * @throws Exception
	 */
	public static InputStream federateQuery(String sparqlQuery)
			throws Exception {

		ByteArrayInputStream xmlInputStream = null;
		SailRepository repo = null;
		try {
			Config.getConfig();
			Config.getConfig().set("enableMonitoring", "true");
			Config.getConfig().set("monitoring.logQueryPlan", "true");

			repo = FedXFactory.initializeSparqlFederation(Arrays.asList(
					"http://1.1.1.1:3030/rep/sparql"));

			long startTime = System.currentTimeMillis();
			long endTime = 0;
			if (isAccessable()) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				SPARQLResultsXMLWriter xmlWriter = new SPARQLResultsXMLWriter(
						out);

				TupleQuery query = repo.getConnection().prepareTupleQuery(
						QueryLanguage.SPARQL, sparqlQuery);
				query.evaluate(xmlWriter);

				MonitoringUtil.printMonitoringInformation();

				endTime = System.currentTimeMillis();
				System.out.println("Execution time(msec) : "
						+ (endTime - startTime));

				xmlInputStream = new ByteArrayInputStream(out.toByteArray());

				endTime = System.currentTimeMillis();
			}

			System.out.println("Execution time(sec) : " + (endTime - startTime)
					/ 1000);
			System.out.println("Done.");
		} catch (Exception ex) {
			log.error("an exception occured ", ex);
			throw ex;
		} finally {
			FederationManager.getInstance().shutDown();
		}

		return xmlInputStream;

	}

	/**
	 * 
	 * @return
	 */
	public static boolean isAccessable() {

		PolicyRepository policyRepo = new PolicyRepository();
		policyRepo.setUrl(accessPolicyURL);

		policyRepo.setSparql(QueryInfo.getQueryWithCondition());
		System.out.println("Query: " + QueryInfo.getQueryWithCondition());

		boolean flag = false;
		try {
			policyRepo.getUserProfileFromQuery();
			ResultSet rsA = policyRepo.performCredentialsAuthorization();

			if (rsA != null) {
				QueryInfo.setResultSet(rsA);
				flag = true;
			}

		} catch (MissingUserProfileException e) {
			log.error("an exception occured ", e);
		}

		return flag;
	}

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		String queries = readFileAsString("Queries.txt").trim();
		String[] queriesArray = queries.split("####Query \\d+####");

		String queryString = queriesArray[31].trim();

		String queryArr[] = queryString.split("user_profile = \\{");
		QueryInfo.setQueryWithCondition(queryString);
		queryString = queryArr[0];

		ByteArrayInputStream xmlInputStream = (ByteArrayInputStream) federateQuery(queryString);

		if (xmlInputStream != null) {
			ResultSet rs = ResultSetFactory.fromXML(xmlInputStream);
			int count = 0;
			while (rs.hasNext()) {
				System.out.println(count + " [jena]: " + rs.next());
				count++;
			}

			System.out.println("Total Number of Records: " + count);
		}

	}

	/**
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	private static String readFileAsString(String filePath) throws IOException {
		StringBuffer fileData = new StringBuffer();
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
		}
		reader.close();
		return fileData.toString();
	}

}
