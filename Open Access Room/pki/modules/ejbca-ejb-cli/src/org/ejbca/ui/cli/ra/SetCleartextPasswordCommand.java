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
 
package org.ejbca.ui.cli.ra;

import javax.ejb.FinderException;

import org.cesecore.authorization.AuthorizationDeniedException;
import org.ejbca.core.ejb.ra.EndEntityManagementSessionRemote;
import org.ejbca.core.model.ra.raadmin.UserDoesntFullfillEndEntityProfile;
import org.ejbca.ui.cli.CliUsernameException;
import org.ejbca.ui.cli.ErrorAdminCommandException;

/**
 * Set the clear text password for an end entity in the database.  Clear text passwords are used for batch
 * generation of keystores (pkcs12/pem).
 *
 * @version $Id: SetCleartextPasswordCommand.java 17764 2013-10-09 13:01:40Z mikekushner $
 */
public class SetCleartextPasswordCommand extends BaseRaCommand {

    @Override
	public String getSubCommand() { return "setclearpwd"; }
    @Override
    public String getDescription() { return "Set a clear text password for an end entity for batch generation"; }
    
    @Override
    public String[] getSubCommandAliases() {
        return new String[]{};
    }
    
	@Override
    public void execute(String[] args) throws ErrorAdminCommandException {
        try {
            args = parseUsernameAndPasswordFromArgs(args);
        } catch (CliUsernameException e) {
            return;
        }
        
        try {
            if (args.length < 3) {
    			getLogger().info("Description: " + getDescription());
            	getLogger().info("Usage: " + getCommand() + " <username> <password>");
                return;
            }
            String username = args[1];
            String password = args[2];
            getLogger().info("Setting clear text password for user " + username);

            try {
                ejb.getRemoteSession(EndEntityManagementSessionRemote.class).setClearTextPassword(getAuthenticationToken(cliUserName, cliPassword), username, password);
            } catch (AuthorizationDeniedException e) {
            	getLogger().error("Not authorized to modify end entity.");
            } catch (UserDoesntFullfillEndEntityProfile e) {
            	getLogger().error("Given userdata doesn't fullfill end entity profile. : " + e.getMessage());
            } catch (FinderException e) {
                getLogger().error("End entity with username '"+username+"' does not exist.");
            }
        } catch (Exception e) {
            throw new ErrorAdminCommandException(e);
        }
    }
}
