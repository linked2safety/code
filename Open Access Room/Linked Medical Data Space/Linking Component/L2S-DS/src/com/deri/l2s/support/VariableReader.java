package com.deri.l2s.support;

/*
 *  Linked2Safety SPARQL Endpoint Discovery and Linking Framework
 *  
 *  Copyright (C) 2014 Muntazir Mehdi <muntazir.mehdi@insight-centre.org>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;

import com.deri.l2s.entities.Variable;
import com.deri.l2s.managers.EMProvider;

public class VariableReader {
	
	private String[] variables;
	String csvFile = "data/CHUV/CHUV_ExtractedVars.csv";
	BufferedReader br = null;
	String line = "";
	String cvsSplitBy = ",";
	Set<String> uniqueVars = new HashSet<String>();

	public String[] parseVariables() {

		try {

			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
				String[] vars = line.split(cvsSplitBy);
				for (int i = 0; i < vars.length; i++)
					vars[i] = vars[i].trim();
				
				for (String var : vars) {
					uniqueVars.add(var);
				}
				
				this.setVariables(uniqueVars.toArray(new String[0]));
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return this.getVariables();
	}
	
	public String[] cleanVariables(String[] vars){
		Set<String> replaceVars = new HashSet<String>();
		for (String var : vars) {
			var.trim();
			if (var.endsWith("?")){
				var = var.substring(0, var.length() - 1);
			}
			var = var.replaceAll("\\d*$", "");
			replaceVars.add(var);
		}
		return replaceVars.toArray(new String[0]);
	}

	public void setVariables(String[] variables) {
		this.variables = variables;
	}

	public String[] getVariables() {
		return variables;
	}
}
