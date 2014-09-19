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

package org.ejbca.ui.web.admin.administratorprivileges;

import java.util.Collection;

import org.cesecore.authorization.rules.AccessRuleData;

/**
 * 
 * @version $Id: AccessRuleCollection.java 12221 2011-08-09 14:18:26Z mikekushner $
 *
 */

public class AccessRuleCollection {

    private String name;
    private Collection<AccessRuleData> collection;

    public AccessRuleCollection(String name, Collection<AccessRuleData> collection) {
        this.name = name;
        this.collection = collection;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<AccessRuleData> getCollection() {
        return collection;
    }

    public void setCollection(Collection<AccessRuleData> collection) {
        this.collection = collection;
    }
}
