package org.deri.linked2safety.sfedengine.start.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
//@SuiteClasses({ ConfigTest.class, EndpointManagerTest.class,
//		FedXFactoryTest.class, L2SUtilTest.class, QueryEvaluationTest.class,
//		SFedEngineSourceSelectionIndexedTest.class })
@SuiteClasses({ SFedEngineSourceSelectionIndexedTest.class, QueryEvaluationTest.class, L2SUtilTest.class, SFedEngineSourceSelectionTest.class, QueryStringUtilTest.class })
public class AllTests {

}
