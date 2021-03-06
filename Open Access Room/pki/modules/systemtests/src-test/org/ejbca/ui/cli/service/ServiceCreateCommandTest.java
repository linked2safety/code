/*************************************************************************
 *                                                                       *
 *  EJBCA: The OpenSource Certificate Authority                          *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.ejbca.ui.cli.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Properties;

import org.cesecore.authentication.tokens.AuthenticationToken;
import org.cesecore.authentication.tokens.UsernamePrincipal;
import org.cesecore.mock.authentication.tokens.TestAlwaysAllowLocalAuthenticationToken;
import org.cesecore.util.CryptoProviderTools;
import org.ejbca.core.model.services.ServiceConfiguration;
import org.ejbca.ui.cli.ErrorAdminCommandException;
import org.ejbca.util.TraceLogMethodsRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * System test class for ServiceCreateCommand
 * 
 * @version $Id: ServiceCreateCommandTest.java 16751 2013-05-06 15:14:55Z samuellb $
 */
public class ServiceCreateCommandTest extends ServiceTestCase {
    
    @org.junit.Rule
    public org.junit.rules.TestRule traceLogMethodsRule = new TraceLogMethodsRule();
    
    private static final AuthenticationToken admin = new TestAlwaysAllowLocalAuthenticationToken(new UsernamePrincipal("ServiceCreateCommandTest"));
    private ServiceCreateCommand serviceCreateCommand;
    private static final String SERVICE_NAME = "TestServiceCLICreate";
    
    private static final String[] CREATE_ARGS = { "create", SERVICE_NAME };
    private static final String[] CREATE_WITH_PROPERTIES_ARGS = { "create", SERVICE_NAME, "intervalClassPath=org.ejbca.core.model.services.intervals.PeriodicalInterval", "interval.periodical.unit=DAYS", "interval.periodical.value=1234" };
    private static final String[] LIST_FIELDS_ARGS = { "create", SERVICE_NAME, "-listFields" };
    private static final String[] LIST_PROPERTIES_ARGS = { "create", SERVICE_NAME, "-listProperties" };
    private static final String[] MISSING_NAME_ARGS = { "create" };
    
    @Before
    public void setUp() throws Exception {
        CryptoProviderTools.installBCProviderIfNotAvailable();
        serviceCreateCommand = new ServiceCreateCommand();
        getServiceSession().removeService(admin, SERVICE_NAME);
    }

    @After
    public void tearDown() throws Exception {
        getServiceSession().removeService(admin, SERVICE_NAME);
    }
    
    @Test
    public void testExecuteCreate() throws ErrorAdminCommandException {
        assertNull("service should not yet exist", getServiceSession().getService(SERVICE_NAME));
        serviceCreateCommand.execute(CREATE_ARGS);
        assertNotNull("service should have been created", getServiceSession().getService(SERVICE_NAME));
    }
    
    @Test
    public void testExecuteCreateWithProperties() throws ErrorAdminCommandException {
        assertNull("service should not yet exist", getServiceSession().getService(SERVICE_NAME));
        
        serviceCreateCommand.execute(CREATE_WITH_PROPERTIES_ARGS);
        assertNotNull("service should have been created", getServiceSession().getService(SERVICE_NAME));
        
        ServiceConfiguration sc = getServiceSession().getService(SERVICE_NAME);
        assertEquals("intervalClassPath", "org.ejbca.core.model.services.intervals.PeriodicalInterval", sc.getIntervalClassPath());
        Properties props = sc.getIntervalProperties();
        assertEquals("interval.periodical.unit", "DAYS", props.getProperty("interval.periodical.unit"));
        assertEquals("interval.periodical.value", "1234", props.getProperty("interval.periodical.value"));
    }
    
    @Test
    public void testExecuteMissingName() throws ErrorAdminCommandException {
        // should log an error
        serviceCreateCommand.execute(MISSING_NAME_ARGS);
    }
    
    @Test
    public void testExecuteDuplicate() throws ErrorAdminCommandException {
        // should log an error
        serviceCreateCommand.execute(CREATE_ARGS);
        serviceCreateCommand.execute(CREATE_ARGS);
    }
    
    @Test
    public void testExecuteList() throws ErrorAdminCommandException {
        // should not create anything, just list the available fields/properties
        serviceCreateCommand.execute(LIST_FIELDS_ARGS);
        assertNull("service should NOT have been created", getServiceSession().getService(SERVICE_NAME));
        serviceCreateCommand.execute(LIST_PROPERTIES_ARGS);
        assertNull("service should NOT have been created", getServiceSession().getService(SERVICE_NAME));
    }
    
}
