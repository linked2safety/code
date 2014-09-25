
/*
 *   hardyWeinbergEquilibrium.cpp - Removes any SNPs that fail the Hardy-Weinberg Equilibrium test
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
 *   This component links (bundles) to data.h and snp_hwe.h, which are available under GPLv3. 
 *   You may download the source code and associated license at  https://github.com/linked2safety/code
 * 
*/



#include "data.h"
#include "snp_hwe.h"

//Define the constant values
#define MISSING_DATA 0
#define HOMOZYGOUS_1 1
#define HETEROZYGOUS 2
#define HOMOZYGOUS_2 3

//Returns the value for controls for the variable
int getControlValue(Datacube *datacube,int varIndex)
{
	string varName = datacube->variableNames[varIndex];
	//Check if the target variable is of the correct type
	if(datacube->getVariableType(varName).compare("SNP")==0 || datacube->getVariableType(varName).compare("D")==0)
	{
		cerr << "Error! The target variable is not an Adverse Event or a Medical Measure.\nTerminating process" << endl;
		exit(-1);
	}
	//Get the number of categorical values of the target variable
	int numOfCategories = datacube->getNumOfVariableValues(varName);
	//Only one categorical value
	if(numOfCategories < 2)
	{
		cerr << "Error! The target variable has only one possible value.\nTerminating process" << endl;
		exit(-1);		
	}
	//More than three categorical values
	else if(numOfCategories > 3)
	{
		cerr << "Error! The target variable has " << numOfCategories << " possible values.\nTerminating process" << endl;
		exit(-1);		
	}
	//Binary
	if(numOfCategories == 2)
		return datacube->variableCatMeaning[varName].begin()->first;
		
	//Check that there is a missing value
	map < int,string> :: iterator catIt ;
	bool missingFound = false;
	for(catIt = datacube->variableCatMeaning[varName].begin(); catIt!= datacube->variableCatMeaning[varName].end(); ++catIt)
	{
		if(catIt->second.compare(MISSING_ORIG_VAL)==0)
		{
			missingFound = true;
			break;
		}
	}
	
	//No missing value was found
	if(!missingFound)
	{
		cerr << "Error! The target variable has 3 possible non missing values.\nTerminating process" << endl;
		exit(-1);			
	}
	
	//Return the first non missing value
	if(catIt == datacube->variableCatMeaning[varName].begin())
		return (++catIt)->first;
	else
		return datacube->variableCatMeaning[varName].begin()->first;
}


int main (int argc, char *argv[]) {
	//HARDY WEINBERG EQUILIBRIUM QUALITY CONTROL
	//Get the data	
	Datacube datacube;
	datacube.loadData(argv[1]);
	datacube.setVariablesMeaning(argv[6]);
	
	int countsCol = datacube.values.at(0).size() - 1;//The column with the counts

	//Set the threshold
	float threshold;
	threshold = atof(argv[2]);

	//Set the target variables index if the test will only be applied on controls
	bool onlyOnControls = false;
	int targetVarIndex = countsCol - 1;
	int CONTROL_VAL;
	if(argv[3][0] == '1')
	{
		onlyOnControls = true;
		CONTROL_VAL = getControlValue(&datacube,targetVarIndex);
	}
	
	
	bool keep[countsCol];
	for(int i=0; i<datacube.values.at(0).size(); ++i)
	{
        //initialize the values of the vectors
		keep[i] = true;
	}
	
    //initialize the counters
	int minorHom=0, majorHom=0, h1count=0, h2count=0, heterozygous=0;
	
	//Check which variables pass the HWE QC
	double pValue;
	for(int column=0;column<countsCol;column++) 
	{
        //Only if the variable is a SNP
		if(datacube.getVariableType(column).compare("SNP")==0)
		{
			h1count = 0;h2count = 0; heterozygous = 0;
			for(int row=0;row<datacube.values.size();row++)
			{	
				//chech if only controls should be used 
				if(!onlyOnControls || (onlyOnControls && datacube.values.at(row).at(targetVarIndex) == CONTROL_VAL))
				{
					switch(datacube.values.at(row).at(column))
					{
                        //count the minor alleles
						case HOMOZYGOUS_1:
							h1count += datacube.values.at(row).at(countsCol);
							break;
						//count the heterozygous alleles
						case HETEROZYGOUS:
							heterozygous += datacube.values.at(row).at(countsCol);
							break;	
						//count the major alleles
						case HOMOZYGOUS_2:
							h2count += datacube.values.at(row).at(countsCol);
					}
				}
			}
				
			//Set the minor and major alleles
			if(h1count<h2count)
			{
				minorHom = h1count;
				majorHom = h2count;
			}
			else
			{
				minorHom = h2count;
				majorHom = h1count;				
			}
				
			//If no alleles were counted (e.g. all missing, no controls)
			if(h1count == 0 && h2count==0 && heterozygous==0)
			{
				keep[column] = false;
			}
			else
			{	
				//calculate the HWE pValue
				pValue = SNPHWE(heterozygous,minorHom,majorHom);
				//Check if the SNP will be kept
				if(pValue < threshold )
					keep[column] = false;
			}
		}
	}

	//Remove the variables that did not pass the HWE QC
    datacube.removeVariables(keep);
    //output the data cube
	datacube.writeToFile(argv[4]);		
	
	
	//Output the XML contents describing the algorithm
	ofstream outpXML;
	outpXML.open(argv[5]);
	
	outpXML << "<log>" << endl;
	outpXML << "<QualityControl algorithm_name=\"Hardy Weinberg Equilibrium Test\" algorithmParamsSetting=\"threshold=" << argv[2];
	outpXML << " only_on_controls=" << argv[3] << "\"/>" << endl;
	outpXML << "</log>";	
	
	outpXML.close();
	
}
