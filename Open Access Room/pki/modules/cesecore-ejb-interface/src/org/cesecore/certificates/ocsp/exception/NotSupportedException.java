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

package org.cesecore.certificates.ocsp.exception;

/**
 * Error due to some part of the request is not supported.
 * 
 * @version $Id: NotSupportedException.java 17625 2013-09-20 07:12:06Z netmackan $
 */
public class NotSupportedException extends Exception  {
 
    private static final long serialVersionUID = -3185825591813094581L;

    /**
     * Constructor used to create exception with an errormessage. Calls the same constructor in
     * baseclass <code>Exception</code>.
     *
     * @param message Human redable error message, can not be NULL.
     */
    public NotSupportedException(String message) {
        super(message);
    }

    /**
     * Constructor used to create exception with an embedded exception. Calls the same constructor
     * in baseclass <code>Exception</code>.
     *
     * @param exception exception to be embedded.
     */
    public NotSupportedException(Exception exception) {
        super(exception);
    }
}
