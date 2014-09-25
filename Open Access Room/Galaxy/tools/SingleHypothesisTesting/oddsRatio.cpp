/*
 *   oddsRatio.cpp - Calculates the odds ratio of two binary variables
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

#define MISSING_DATA_I -1 //The missing data value that will be used in the data cube values

//Sets the values of the variable to 0 and 1.
//If the variable is not binary the process is terminated
void setVarValues(Datacube *datacube, int varIndex, int *varValues )
{
	//Check if binary
	string varName = datacube->variableNames[varIndex];
	int numOfValues = datacube->getNumOfVariableValues(varName);
	if(numOfValues>4 || numOfValues==1)
	{
		cerr << "Error: The variable with index " << varIndex+1 << " is not binary!" << endl; 
		exit(-1);				
	}
	
	//Set the varValues for the variable
	map < int,string> :: iterator valuesIt = datacube->variableCatMeaning[varName].begin();
	for(;valuesIt!=datacube->variableCatMeaning[varName].end();++valuesIt)
		//if we have a missing value
		if(valuesIt->second.compare(MISSING_ORIG_VAL)==0)
		{
			//if there were only two values it means that only one value
			//represents a non missing value
			if(numOfValues==2)
			{
				cerr << "Error: The variable with index " << varIndex+1 << " is not binary!" << endl; 
				exit(-1);				
			}
		}	
		//If it is a non applicable value
		else if(valuesIt->second.compare(NOT_APPLICABLE_ORIG_VAL)==0)
		{
			//Check if the value is actually present in the data cube
			for(int i=0; i<datacube->values.size();++i)
			{
				//If a non applicable value exists then add it as a possible value of the binary variable
				if(valuesIt->first == datacube->values[i][varIndex])
				{
					//store it at the position not used
					if(varValues[0]==-1)
						varValues[0] = valuesIt->first;
					else if(varValues[1]==-1)
						varValues[1] = valuesIt->first;
					//the third value is a non missing value => not binary var
					else
					{
						cerr << "Error: The variable with index " << varIndex+1 << " is not binary!" << endl; 
						exit(-1);				
					}	
					break;
				}
			}
		}
		//if it is not a missing value
		else
		{
			//store it at the position not used
			if(varValues[0]==-1)
				varValues[0] = valuesIt->first;
			else if(varValues[1]==-1)
				varValues[1] = valuesIt->first;
			//the third value is a non missing value => not binary var
			else
			{
				cerr << "Error: The variable with index " << varIndex+1 << " is not binary!" << endl; 
				exit(-1);				
			}				
		}

	//sort the values in ascending order
	if(varValues[0] > varValues[1])
	{
		int value = varValues[0];
		varValues[0] = varValues[1];
		varValues[1] = value;
	}	
}


//ODDS RATIO
int main (int argc, char *argv[]) {

	
	//Get the data	
	Datacube datacube;
	datacube.loadData(argv[1]);
	datacube.setVariablesMeaning(argv[5]);
	
	int countsCol = datacube.values.at(0).size() - 1;//The column with the counts

	//Set the index of the two variables
	int varIndex1, varIndex2;
	varIndex1 = countsCol-2;
	varIndex2 = countsCol-1;

	//Check the dimensions of the data cube
	if(countsCol<2)
	{
		cerr << " The data cube must have at least 2 variables." << endl << " Process Terminated." << endl;
		exit(-1);		
	}

	//Check if missing values will be ignored
	int ignoreMissing = atof(argv[2]);

	//Get the values of the two variables and check that the variables are binary
	int valuesVar1[2],valuesVar2[2];
	//valuesVarX[0] will contain the value that will be considered as 0 
	//and valuesVarX[1] the value that will be considered as 1
	for(int i=0;i<2;++i)
	{
		valuesVar1[i] = -1;
		valuesVar2[i] = -1;
	}
	setVarValues(&datacube, varIndex1, valuesVar1);
	setVarValues(&datacube, varIndex2, valuesVar2);

	
	//Set the counters	
	int totalInstances=0, instancesWithMissing=0, remainingInstances=0;
	float n00=0, n01=0, n10=0, n11=0;
	int value1, value2;
	for(int row=0; row<datacube.values.size(); ++row)
	{
		//Set the value for the first variable
		if(datacube.getMeaningOfValue(row,varIndex1).compare(MISSING_ORIG_VAL)==0)
			value1 = MISSING_DATA_I;
		else
			value1 = datacube.values.at(row).at(varIndex1);
		//Set the value for the second variable
		if(datacube.getMeaningOfValue(row,varIndex2).compare(MISSING_ORIG_VAL)==0)
			value2 = MISSING_DATA_I;
		else
			value2 = datacube.values.at(row).at(varIndex2);
		
		//Count the total instances
		totalInstances += datacube.values.at(row).at(countsCol);
		//count the instances with missing values
		if(value1 == MISSING_DATA_I || value2 == MISSING_DATA_I)
			instancesWithMissing += datacube.values.at(row).at(countsCol);
		
		//If none of the variable values are missing
		if(value1 != MISSING_DATA_I && value2 != MISSING_DATA_I)
		{
			//00
			if(value1 == valuesVar1[0] && value2 == valuesVar2[0])
				n00+=datacube.values.at(row).at(countsCol);
			//01
			else if(value1 == valuesVar1[0] && value2 == valuesVar2[1])
				n01+=datacube.values.at(row).at(countsCol);
			//10
			else if(value1 == valuesVar1[1] && value2 == valuesVar2[0])
				n10+=datacube.values.at(row).at(countsCol);
			//11
			else
				n11+=datacube.values.at(row).at(countsCol);
		}
		//if we have a missing value and we do not ignore missing values
		else if(ignoreMissing == 0)
		{
			//MISS MISS
			if(value1 == MISSING_DATA_I && value2 == MISSING_DATA_I)
			{
				n00+=datacube.values.at(row).at(countsCol);
				n01+=datacube.values.at(row).at(countsCol);
				n10+=datacube.values.at(row).at(countsCol);
				n11+=datacube.values.at(row).at(countsCol);
			}
			//MISS 0
			else if(value1 == MISSING_DATA_I && value2 == valuesVar2[0])
			{
				n00+=datacube.values.at(row).at(countsCol);
				n10+=datacube.values.at(row).at(countsCol);
			}
			//MISS 1
			else if(value1 == MISSING_DATA_I && value2 == valuesVar2[1])
			{
				n01+=datacube.values.at(row).at(countsCol);
				n11+=datacube.values.at(row).at(countsCol);
			}
			//0 MISS
			else if(value1 == valuesVar1[0] && value2 == MISSING_DATA_I)
			{
				n00+=datacube.values.at(row).at(countsCol);
				n01+=datacube.values.at(row).at(countsCol);
			}
			//1 MISS
			else if(value1 == valuesVar1[1] && value2 == MISSING_DATA_I)
			{
				n10+=datacube.values.at(row).at(countsCol);
				n11+=datacube.values.at(row).at(countsCol);
			}			
		}
	}

	if(n01*n10 == 0)
	{
		cerr << "The analysis results in a division by zero.\n Process Terminated." <<endl;
		exit(-1);
	}
	else
	{
		//Calculate the odds ratio
		float OR = (n00*n11)/(n01*n10);
		//Calculate the number of instances used in the analysis
		if(ignoreMissing == 1)
			remainingInstances = totalInstances - instancesWithMissing;
		else
			remainingInstances = totalInstances;
			
		//Output the results
		ofstream outp;
		outp.open(argv[3]);
		outp << "Association" << endl;
		outp << datacube.variableNames[varIndex1] << " => " << datacube.variableNames[varIndex2] << "    Odds Ratio = " << OR << endl;
		outp << endl << "Meaning of variable values as used in the analysis" << endl;
		outp << datacube.variableNames[varIndex1] << " =>    0=" << datacube.variableCatMeaning[datacube.variableNames[varIndex1]][valuesVar1[0]] << "   1=" << datacube.variableCatMeaning[datacube.variableNames[varIndex1]][valuesVar1[1]] << endl;
		outp << datacube.variableNames[varIndex2] << " =>    0=" << datacube.variableCatMeaning[datacube.variableNames[varIndex2]][valuesVar2[0]] << "   1=" << datacube.variableCatMeaning[datacube.variableNames[varIndex2]][valuesVar2[1]] << endl;
		outp << endl << "Total instances = " << totalInstances << "\nInstances with missing values = " << instancesWithMissing << "\nNumber of instances used in the analysis = " << remainingInstances;
		outp.close();
		
		//Output the XML contents describing the algorithm and the association	
		ofstream outpXML;
		outpXML.open(argv[4]);

		outpXML << "<log>" << endl;
		outpXML << "<SingleHypothesisTesting algorithm_name=\"Odds Ratio\" algorithmParamsSetting=\"ignoreMissing=" << argv[2] <<"\"/>" << endl;
		outpXML << "<Association Description=\"" <<  datacube.variableNames[varIndex1] << " =&gt; " << datacube.variableNames[varIndex2] << "\" odds_ratio=\"" <<  OR << "\"/>" << endl;
		outpXML << "</log>";

		outpXML.close();
	}
			
}
