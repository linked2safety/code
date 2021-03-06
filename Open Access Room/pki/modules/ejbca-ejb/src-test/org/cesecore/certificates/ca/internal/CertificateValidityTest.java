/*************************************************************************
 *                                                                       *
 *  CESeCore: CE Security Core                                           *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/ 
package org.cesecore.certificates.ca.internal;

import static org.junit.Assert.assertTrue;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.bouncycastle.jce.X509KeyUsage;
import org.cesecore.certificates.ca.IllegalValidityException;
import org.cesecore.certificates.certificateprofile.CertificateProfile;
import org.cesecore.certificates.certificateprofile.CertificateProfileConstants;
import org.cesecore.certificates.endentity.EndEntityInformation;
import org.cesecore.certificates.endentity.ExtendedInformation;
import org.cesecore.certificates.util.AlgorithmConstants;
import org.cesecore.keys.util.KeyTools;
import org.cesecore.util.CertTools;
import org.cesecore.util.CryptoProviderTools;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests calculation of certificate validity dates
 * 
 * @author tomas
 * @version $Id: CertificateValidityTest.java 18052 2013-11-04 14:01:50Z mikekushner $
 *
 */
public class CertificateValidityTest {

	@Before
	public void setUp() throws Exception {
		CryptoProviderTools.installBCProviderIfNotAvailable();
    }

	@Test
    public void test01TestCertificateValidity() throws Exception {
        testBaseTestCertificateValidity(50);
    }

	@Test
    public void test02TestCertificateValidity() throws Exception {
        final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.add(Calendar.DATE, 50);
        testBaseTestCertificateValidity(cal.getTime().getTime());
    }
    private void testBaseTestCertificateValidity(long encodedValidity) throws Exception {

		KeyPair keys = KeyTools.genKeys("1024", "RSA");
		
    	X509Certificate cacert = CertTools.genSelfCertForPurpose("CN=dummy2", 100, null, keys.getPrivate(), keys.getPublic(),
    			AlgorithmConstants.SIGALG_SHA1_WITH_RSA, true, X509KeyUsage.cRLSign|X509KeyUsage.keyCertSign, true);

    	EndEntityInformation subject = new EndEntityInformation();

    	final CertificateProfile cp = new CertificateProfile(CertificateProfileConstants.CERTPROFILE_FIXED_ENDUSER);
    	cp.setValidity(encodedValidity);
    	cp.setAllowValidityOverride(false);
    
    	// First see that when we don't have a specified time requested and validity override is not allowed, the end time shouldbe ruled by the certificate profile.
    	
    	CertificateValidity cv = new CertificateValidity(subject, cp, null, null, cacert, false);
    	Date notBefore = cv.getNotBefore();
    	Date notAfter = cv.getNotAfter();
    	Date now = new Date();
        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.DAY_OF_MONTH, 49);
        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.DAY_OF_MONTH, 51);
    	assertTrue(notBefore.before(now));
    	assertTrue(notAfter.after(cal1.getTime()));
    	assertTrue(notAfter.before(cal2.getTime()));
    	
    	// See that a requested validity does not affect it
        Calendar requestNotBefore = Calendar.getInstance();
        requestNotBefore.add(Calendar.DAY_OF_MONTH, 2);
        Calendar requestNotAfter = Calendar.getInstance();
        requestNotAfter.add(Calendar.DAY_OF_MONTH, 25);
        cv = new CertificateValidity(subject, cp, requestNotBefore.getTime(), requestNotAfter.getTime(), cacert, false);
    	notBefore = cv.getNotBefore();
    	notAfter = cv.getNotAfter();
    	assertTrue(notBefore.before(now));
    	assertTrue(notAfter.after(cal1.getTime()));
    	assertTrue(notAfter.before(cal2.getTime()));
    	
    	// Add extended information for the user and see that it does not affect it either
    	ExtendedInformation ei = new ExtendedInformation();
    	ei.setCustomData(ExtendedInformation.CUSTOM_STARTTIME, "10:0:0");
    	ei.setCustomData(ExtendedInformation.CUSTOM_ENDTIME, "30:0:0");
    	subject.setExtendedinformation(ei);
        cv = new CertificateValidity(subject, cp, requestNotBefore.getTime(), requestNotAfter.getTime(), cacert, false);
    	notBefore = cv.getNotBefore();
    	notAfter = cv.getNotAfter();
    	assertTrue(notBefore.before(now));
    	assertTrue(notAfter.after(cal1.getTime()));
    	assertTrue(notAfter.before(cal2.getTime()));
    	
    	// Now allow validity override
    	cp.setAllowValidityOverride(true);
    	
    	// Now we should get what's in the EndEntityInformation extended information
        cv = new CertificateValidity(subject, cp, requestNotBefore.getTime(), requestNotAfter.getTime(), cacert, false);
    	notBefore = cv.getNotBefore();
    	notAfter = cv.getNotAfter();
        cal1 = Calendar.getInstance();
        cal1.add(Calendar.DAY_OF_MONTH, 9);
        cal2 = Calendar.getInstance();
        cal2.add(Calendar.DAY_OF_MONTH, 11);
    	assertTrue(notBefore.after(cal1.getTime()));
    	assertTrue(notBefore.before(cal2.getTime()));
        cal1 = Calendar.getInstance();
        cal1.add(Calendar.DAY_OF_MONTH, 29);
        cal2 = Calendar.getInstance();
        cal2.add(Calendar.DAY_OF_MONTH, 31);
    	assertTrue(notAfter.after(cal1.getTime()));
    	assertTrue(notAfter.before(cal2.getTime()));
    	
    	// Remove extended information from EndEntityInformation and we should get what we pass as parameters to CertificateValidity
    	subject.setExtendedinformation(null);
        cv = new CertificateValidity(subject, cp, requestNotBefore.getTime(), requestNotAfter.getTime(), cacert, false);
    	notBefore = cv.getNotBefore();
    	notAfter = cv.getNotAfter();
        cal1 = Calendar.getInstance();
        cal1.add(Calendar.DAY_OF_MONTH, 1);
        cal2 = Calendar.getInstance();
        cal2.add(Calendar.DAY_OF_MONTH, 3);
    	assertTrue(notBefore.after(cal1.getTime()));
    	assertTrue(notBefore.before(cal2.getTime()));
        cal1 = Calendar.getInstance();
        cal1.add(Calendar.DAY_OF_MONTH, 23);
        cal2 = Calendar.getInstance();
        cal2.add(Calendar.DAY_OF_MONTH, 26);
    	assertTrue(notAfter.after(cal1.getTime()));
    	assertTrue(notAfter.before(cal2.getTime()));
    	
    	// Check that we can not supersede the certificate profile end time
        requestNotAfter = Calendar.getInstance();
        requestNotAfter.add(Calendar.DAY_OF_MONTH, 200);
        cv = new CertificateValidity(subject, cp, requestNotBefore.getTime(), requestNotAfter.getTime(), cacert, false);
    	notBefore = cv.getNotBefore();
    	notAfter = cv.getNotAfter();
        cal1 = Calendar.getInstance();
        cal2 = Calendar.getInstance();
        // This will be counted in number of days since notBefore, and notBefore here is taken from requestNotBefore which is two, 
        // so we have to add 2 to certificate profile validity to get the resulting notAfter but not if certificate end is an 
        // absolute end date.
        if ( encodedValidity > Integer.MAX_VALUE) {
            cal1.add(Calendar.DAY_OF_MONTH, 49);
            cal2.add(Calendar.DAY_OF_MONTH, 51);
        } else {
            cal1.add(Calendar.DAY_OF_MONTH, 51);
            cal2.add(Calendar.DAY_OF_MONTH, 53);            
        }
    	assertTrue(notAfter.after(cal1.getTime()));
    	assertTrue(notAfter.before(cal2.getTime()));

    	// Check that we can not supersede the CA end time
    	cp.setValidity(400);
        cv = new CertificateValidity(subject, cp, requestNotBefore.getTime(), requestNotAfter.getTime(), cacert, false);
    	notBefore = cv.getNotBefore();
    	notAfter = cv.getNotAfter();
        // This will be the CA certificate's notAfter
        cal1 = Calendar.getInstance();
        cal1.add(Calendar.DAY_OF_MONTH, 99);
        cal2 = Calendar.getInstance();
        cal2.add(Calendar.DAY_OF_MONTH, 101);
    	assertTrue(notAfter.after(cal1.getTime()));
    	assertTrue(notAfter.before(cal2.getTime()));

    	// Unless it is a root CA, then we should be able to get a new validity after, to be able to update CA certificate
        cv = new CertificateValidity(subject, cp, requestNotBefore.getTime(), requestNotAfter.getTime(), cacert, true);
    	notBefore = cv.getNotBefore();
    	notAfter = cv.getNotAfter();
        cal1 = Calendar.getInstance();
        cal1.add(Calendar.DAY_OF_MONTH, 199);
        cal2 = Calendar.getInstance();
        cal2.add(Calendar.DAY_OF_MONTH, 201);
    	assertTrue(notAfter.after(cal1.getTime()));
    	assertTrue(notAfter.before(cal2.getTime()));
    	
    	// Check that we can request a validity time before "now"
        requestNotBefore = Calendar.getInstance();
        requestNotBefore.add(Calendar.DAY_OF_MONTH, -10);
        cv = new CertificateValidity(subject, cp, requestNotBefore.getTime(), requestNotAfter.getTime(), cacert, false);
    	notBefore = cv.getNotBefore();
    	notAfter = cv.getNotAfter();
        cal1 = Calendar.getInstance();
        cal1.add(Calendar.DAY_OF_MONTH, -9);
        cal2 = Calendar.getInstance();
        cal2.add(Calendar.DAY_OF_MONTH, -11);
    	assertTrue(notBefore.before(cal1.getTime()));
    	assertTrue(notBefore.after(cal2.getTime()));
        // This will be the CA certificate's notAfter
        cal1 = Calendar.getInstance();
        cal1.add(Calendar.DAY_OF_MONTH, 99);
        cal2 = Calendar.getInstance();
        cal2.add(Calendar.DAY_OF_MONTH, 101);
    	assertTrue(notAfter.after(cal1.getTime()));
    	assertTrue(notAfter.before(cal2.getTime()));
    	
    	// Check that ca.toolateexpiredate setting in ejbca.properties is in effect
    	Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 5);
        CertificateValidity.setTooLateExpireDate(cal.getTime());
        boolean thrown = false;
        try {
            cv = new CertificateValidity(subject, cp, requestNotBefore.getTime(), requestNotAfter.getTime(), cacert, false);        	
        } catch (IllegalValidityException e) {
        	thrown = true;
        	//log.debug(e.getMessage());
        	String msg = e.getMessage();
        	// When running from within eclipse it will not have the correct internalresources.
        	if (!msg.contains("Requested expire date is not before the configured 'ca.toolateexpiredate'") && (!msg.equals("signsession.errorbeyondtoolateexpiredate"))) {
            	assertTrue(msg, false);        		
        	}
        }
        assertTrue(thrown);
        CertificateValidity.setTooLateExpireDate(new Date(Long.MAX_VALUE));
	}
}
