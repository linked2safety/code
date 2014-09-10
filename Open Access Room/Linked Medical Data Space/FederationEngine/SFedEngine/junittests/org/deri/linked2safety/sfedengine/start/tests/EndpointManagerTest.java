package org.deri.linked2safety.sfedengine.start.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.openrdf.repository.RepositoryException;

import com.fluidops.fedx.EndpointManager;
import com.fluidops.fedx.FederationManager;
import com.fluidops.fedx.exception.FedXException;
import com.fluidops.fedx.structures.Endpoint;
import com.fluidops.fedx.util.EndpointFactory;

public class EndpointManagerTest {

	@Test
	public void testGetEndpointManager() {
		
		if(EndpointManager.getEndpointManager() == null) {
			EndpointManager.initialize();
		}
		EndpointManager.getEndpointManager();
	}

	@Test
	public void testInitialize() throws FedXException {
		if(EndpointManager.getEndpointManager() == null) {
			EndpointManager.initialize();
		}
	}

	@Test
	public void testInitializeListOfEndpoint() throws FedXException {
		List<String> sparqlEndpoints = Arrays.asList(
				"http://localhost:3030/cubesproviderA/sparql",
				"http://localhost:3030/cubesproviderB/sparql"
				);
		List<Endpoint> endpoints = new ArrayList<Endpoint>();
		for (String url : sparqlEndpoints) {
			endpoints.add( EndpointFactory.loadSPARQLEndpoint(url));
		}
		if(EndpointManager.getEndpointManager() == null) {
			EndpointManager.initialize(endpoints);
		}
		
	}


	@Test
	public void testRepairConnection() throws RepositoryException {
		EndpointManager.getEndpointManager().repairAllConnections();
	}

	@Test
	public void testGetAvailableEndpoints() {
		EndpointManager.getEndpointManager().getAvailableEndpoints();
	}

	@Test
	public void testGetEndpointString() {
		EndpointManager.getEndpointManager().getEndpoint("http://localhost:3030/cubesproviderB/sparql");
	}

	@Test
	public void testGetEndpointByUrl() {
		EndpointManager.getEndpointManager().getEndpointByUrl("http://localhost:3030/cubesproviderB/sparql");
	}

}
