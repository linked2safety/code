package org.deri.linked2safety.sfedengine.start.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.fluidops.fedx.Config;
import com.fluidops.fedx.FedXFactory;
import com.fluidops.fedx.FederationManager;
import com.fluidops.fedx.exception.FedXException;
import com.fluidops.fedx.structures.Endpoint;
import com.fluidops.fedx.util.EndpointFactory;

public class FedXFactoryTest {

	@Test
	public void testInitializeSparqlFederation() throws Exception {
		if(FederationManager.isInitialized()) {
			FederationManager.getInstance().shutDown();
		}
		FedXFactory.initializeSparqlFederation(Arrays.asList(
				"http://localhost:3030/cubesproviderA/sparql",
				"http://localhost:3030/cubesproviderB/sparql"
				));
	}

	@Test
	public void testInitializeFederationString() throws Exception {
		if(FederationManager.isInitialized()) {
			FederationManager.getInstance().shutDown();
		}
		String dataConfig = "EndpointsList.ttl";
		FedXFactory.initializeFederation(dataConfig);
	}

	@Test
	public void testInitializeFederationStringListOfEndpoint() throws FedXException {
		if(FederationManager.isInitialized()) {
			FederationManager.getInstance().shutDown();
		}
		String dataConfig = "EndpointsList.ttl";
		FedXFactory.initializeFederation(dataConfig, null);
	}

	@Test
	public void testInitializeFederationListOfEndpoint() throws FedXException {
		if(FederationManager.isInitialized()) {
			FederationManager.getInstance().shutDown();
		}
		List<String> sparqlEndpoints = Arrays.asList(
				"http://localhost:3030/cubesproviderA/sparql",
				"http://localhost:3030/cubesproviderB/sparql"
				);
		List<Endpoint> endpoints = new ArrayList<Endpoint>();
		for (String url : sparqlEndpoints) {
			endpoints.add( EndpointFactory.loadSPARQLEndpoint(url));
		}
		FedXFactory.initializeFederation(endpoints);
	}

}
