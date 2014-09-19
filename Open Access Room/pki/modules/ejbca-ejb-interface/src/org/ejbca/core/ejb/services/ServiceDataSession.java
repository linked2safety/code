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
package org.ejbca.core.ejb.services;


/** Session bean to manage services, these are services that runs periodically.
 * 
 * @version $Id: ServiceDataSession.java 15170 2012-08-01 15:20:23Z mikekushner $
 */
public interface ServiceDataSession {

    /** @return the found entity instance or null if the entity does not exist */
    ServiceData findById(Integer id);
    
}
