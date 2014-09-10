package org.deri.linked2safety.sfedengine.start.tests;

import static org.junit.Assert.*;

import org.deri.linked2safety.sfedengine.start.L2SUtil;
import org.junit.Assert;
import org.junit.Test;

public class L2SUtilTest {

	@Test
	public void testAddNamedGraphToQuery() {
		String query = "";
		String namedG = "";
		
		String returnedQuery = L2SUtil.addNamedGraphToQuery(query, namedG);
		
		Assert.assertNotNull(returnedQuery);
	}

}
