package org.deri.linked2safety.sfedengine.start.tests;

import org.junit.Assert;
import org.junit.Test;

import com.fluidops.fedx.Config;
import com.fluidops.fedx.exception.FedXException;

public class ConfigTest {

	@Test
	public void testGetConfig() {
		Config config = Config.getConfig();
		
		Assert.assertNotNull(config);
		Config.reset();
	}

	@Test
	public void testReset() {
		Config.reset();
	}

	@Test
	public void testInitialize() throws FedXException {
		String dataConfig = "EndpointsList.ttl";
		Config.initialize(dataConfig);
		Config.reset();
	}

	@Test
	public void testGetPropertyString() {
		Config config = Config.getConfig();
		config.getProperty("");
		
		Config.reset();
	}

	@Test
	public void testGetPropertyStringString() {
		Config config = Config.getConfig();
		config.getProperty("", "");
		
		Config.reset();
	}

	@Test
	public void testGetBaseDir() {
		Config config = Config.getConfig();
		config.getBaseDir();
		
		Config.reset();
	}

	@Test
	public void testGetDataConfig() {
		Config config = Config.getConfig();
		config.getDataConfig();
		
		Config.reset();
	}

	@Test
	public void testGetCacheLocation() {
		Config config = Config.getConfig();
		config.getCacheLocation();
		
		Config.reset();
	}

	@Test
	public void testGetJoinWorkerThreads() {
		Config config = Config.getConfig();
		config.getJoinWorkerThreads();
		
		Config.reset();
	}

	@Test
	public void testGetUnionWorkerThreads() {
		Config config = Config.getConfig();
		config.getUnionWorkerThreads();
		
		Config.reset();
	}

	@Test
	public void testGetBoundJoinBlockSize() {
		Config config = Config.getConfig();
		config.getBoundJoinBlockSize();
		
		Config.reset();
	}

	@Test
	public void testGetEnforceMaxQueryTime() {
		Config config = Config.getConfig();
		config.getEnforceMaxQueryTime();
		
		Config.reset();
	}

	@Test
	public void testIsEnableMonitoring() {
		Config config = Config.getConfig();
		config.getEnforceMaxQueryTime();
		
		Config.reset();
	}

	@Test
	public void testIsLogQueryPlan() {
		Config config = Config.getConfig();
		config.isLogQueryPlan();
		
		Config.reset();
	}

	@Test
	public void testIsLogQueries() {
		Config config = Config.getConfig();
		config.isLogQueries();
		
		Config.reset();;
	}

	@Test
	public void testGetPrefixDeclarations() {
		Config config = Config.getConfig();
		config.getPrefixDeclarations();
		
		Config.reset();
	}

	@Test
	public void testIsDebugWorkerScheduler() {
		Config config = Config.getConfig();
		config.isDebugWorkerScheduler();
		
		Config.reset();
	}

	@Test
	public void testIsDebugQueryPlan() {
		Config config = Config.getConfig();
		config.isDebugQueryPlan();
		
		Config.reset();
	}

	@Test
	public void testSet() {
		Config config = Config.getConfig();
		String key = "";
		String value = "";
		config.set(key, value);
		
		Config.reset();
	}

}
