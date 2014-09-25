/*
 *   setTarget.cpp - Sets the target variable in a data cube. If multiple target variables 
 *   are defined, only the selected variables remain in the data cube
 *   
 *   Copyright (C) 2014  -  University of Cyprus  -  aristodimou.aristos@ucy.ac.cy
 * 
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *   This component links (bundles) to data.h, which is available under GPLv3. 
 *   You may download the source code and associated license at  https://github.com/linked2safety/code
 * 
*/

#include "data.h"

//Returns the index value of the variable
int getVarIndex(string indexString,int maxIndexSize)
{
	//The indexString will be in the form     indexNum:_varName hence we
	//need to remove everything from : to end so that we only have the number
	for(int i=0;indexString[i] != '\0';++i)
	{
		if(indexString[i] == ':')
		{
			indexString[i] = '\0';
			break;
		}
	}
	int varIndex = atof(indexString.c_str()) - 1;
	
	//check if the index is in the expected range
	if(varIndex >= maxIndexSize || varIndex < 0)
	{
		cerr << "Error: The index of the variable should be in the range [1," << maxIndexSize << "]" << endl; 
		exit(-1);
	}	
	return varIndex;
}


int main (int argc, char *argv[]) {

	//ALLELE FREQUENCY QUALITY CONTROL
	//Get the data	
	Datacube datacube;
	datacube.loadData(argv[1]);

	//Get the target variables
	vector <int> targetIndx; 
	int maxIndexSize = datacube.variableNames.size()-1;
	vector <string> targetVars = split(argv[4],',');
	//Get the indices of the target variables
	for(int i=0;i<targetVars.size();++i)
		targetIndx.push_back(getVarIndex(targetVars[i],maxIndexSize));
	
	//No target variable was selected
	if(targetIndx.size() < 1)
	{
		cerr << "No target variable was selected.\n Please select at least one variable."<<endl;
		exit(-1);
	}
	//Only one target variable was selected
	//It will be positioned in the last dimension of the cube
	else if(targetIndx.size() == 1)
	{
		int newTargetPos = datacube.variableNames.size()-2;
		//if it is not positioned in the last dimension
		if(targetIndx[0] != newTargetPos)
		{
			//Swap the variable names
			string tempName;
			tempName = datacube.variableNames[newTargetPos];
			datacube.variableNames[newTargetPos] = datacube.variableNames[targetIndx[0]];
			datacube.variableNames[targetIndx[0]] = tempName;
			//Swap the variable values			
			int tempVal;
			for(int i=0;i<datacube.values.size();++i)
			{
				tempVal = datacube.values[i][newTargetPos];
				datacube.values[i][newTargetPos] = datacube.values[i][targetIndx[0]];
				datacube.values[i][targetIndx[0]] = tempVal;
			}
		}
		
	}
	//Multiple target variables were selected
	//The rest of the variables will be removed from the
	//data cube
	else
	{
		//Remove the variables that did not pass the allele frequency QC
		bool keep[datacube.variableNames.size()];
		for(int i=0; i<datacube.variableNames.size(); ++i)
			keep[i] = false;
		//Also keep the counts	
		keep[datacube.variableNames.size()-1]=true;
		//Select the variables that will be kept (the ones selected as target variables) 
		for(int i=0;i<targetIndx.size();++i)
			keep[targetIndx[i]]=true;
		//Remove the variables not needed
		datacube.removeVariables(keep);
	}
	
    //output the datacube 
	datacube.writeToFile(argv[2]);
	
	//Output the XML contents describing the algorithm
	ofstream outpXML;
	outpXML.open(argv[3]);
	
	outpXML << "<log>" << endl;
	outpXML << "<SetTarget algorithm_name=\"Set Target\" algorithmParamsSetting=\"targetIndx=" << argv[4] << "\"/>" << endl;
	outpXML << "</log>";
	
	outpXML.close();
}
