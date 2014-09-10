/*
 * SFedEngine: Secure SPARQL Federation Engine for RDF data cubes

    Copyright (C) 2014  Yasar Khan <yasar.khan@insight-centre.org>
    
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.
    
    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
    This program incorporates the program covered by the following copyright and license 
    notice:
    
    
    Program name: FedX Federation Query Engine 
    
    Copyright (C) 2008-2013, fluid Operations AG
    
    FedX is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details. 
    
    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    You may download the Fedx source code and associated license at <http://www.fluidops.com/fedx/>.
 */

package org.deri.linked2safety.sfedengine.start;

/**
 * 
 * @author Yasar Khan
 * 
 */
public class L2SUtil {

	/**
	 * 
	 * @param query
	 * @param NG
	 * @return String
	 */
	public static String addNamedGraphToQuery(String query, String NG) {

		String namedGraphs = "";

		if (NG == null) {
			query = query.replaceFirst("\\{", "{ Graph ?g {");
		} else {
			String graphsArr[] = NG.split("\\$\\$\\$");
			for (int i = 0; i < graphsArr.length; i++) {
				namedGraphs += " FROM NAMED <" + graphsArr[i] + ">"
						+ System.getProperty("line.separator");
			}

			if (query.contains("where")) {
				query = query.replaceFirst("where", namedGraphs + "WHERE ");
				query = query.replaceFirst("\\{", " { Graph ?g { ");
			} else {
				query = query.replaceFirst("WHERE", namedGraphs + "WHERE ");
				query = query.replaceFirst("\\{", " { Graph ?g { ");
			}

		}
		query += " }";

		return query;
	}

	/**
	 * 
	 * @param graphs
	 * @return
	 */
	public static String addNamedGraphs(String graphs) {
		String namedGraphs = "";

		String graphsArr[] = graphs.split("\\$\\$\\$");

		for (int i = 0; i < graphsArr.length; i++) {
			namedGraphs += " FROM NAMED <" + graphsArr[i] + ">"
					+ System.getProperty("line.separator");
		}

		return namedGraphs;
	}

}
