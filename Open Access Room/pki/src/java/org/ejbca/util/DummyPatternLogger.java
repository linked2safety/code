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

package org.ejbca.util;


/**
 * This class ignores all input.
 * @version $Id: DummyPatternLogger.java 8373 2009-11-30 14:07:00Z jeklund $
 */
public class DummyPatternLogger implements IPatternLogger {

	public void flush() { /* nothing done */ }
	public void paramPut(String key, byte[] value) { /* nothing done */ }
	public void paramPut(String key, String value) { /* nothing done */ }
	public void paramPut(String key, Integer value) { /* nothing done */ }
	public void writeln() { /* nothing done */ }

}
