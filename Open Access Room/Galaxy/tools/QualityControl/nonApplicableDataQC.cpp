/*
 *   nonApplicableDataQC.cpp -  Removes instances with non applicable values from the data cube
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

int main (int argc, char *argv[]) {

	//NON APPLICABLE DATA QUALITY CONTROL
	//Get the data		
	Datacube datacube;
	datacube.loadData(argv[1]);
	datacube.setVariablesMeaning(argv[5]);

	//Set the target variables index if the test will only be applied 
	//only on the target variable values
	bool onlyOnTargetVar = false;
	int countsCol = datacube.values.at(0).size() - 1;
	int targetVarIndex = countsCol - 1;
	if(argv[2][0] == '1')
		onlyOnTargetVar = true;
		
    //initialize keep vector
	bool keep[datacube.values.size()];
	for(int i=0; i<datacube.values.size(); ++i)
		keep[i] = true;
	int removedRows = 0;
	
	//Check which instances will pass the test
	for(int row=0;row<datacube.values.size();row++)
	{
		//If we consider the values of all variables
		if(!onlyOnTargetVar)
		{	
			//for each variable check its value
			for(int column=0;column<countsCol;column++) 
			{	
				//if the value of a column is non applicable then the row will be removed
				if(datacube.getMeaningOfValue(row,column).compare(NOT_APPLICABLE_ORIG_VAL)==0)
				{
					keep[row] = false;
					++removedRows;
					break;
				}

			}
		}
		//If we only consider the values of the target variable
		else if(datacube.getMeaningOfValue(row,targetVarIndex).compare(NOT_APPLICABLE_ORIG_VAL)==0)
		{
			keep[row] = false;
			++removedRows;
		}
	}


	//If the remaining number of rows after the test
	//is less than two create an error message
	if((datacube.values.size() - removedRows) <= 1)
	{
		cerr << "\nError! The remaining instances are not sufficient for further analysis." << endl << "Process Terminated." << endl;
    	exit(-1);		
	}
	
	//Remove the instances that did not pass the test
    datacube.removeInstances(keep);
    //output the data cube
	datacube.writeToFile(argv[3]);
	
	//Output the XML contents describing the algorithm
	ofstream outpXML;
	outpXML.open(argv[4]);
	
	outpXML << "<log>" << endl;
	outpXML << "<QualityControl algorithm_name=\"Non Applicable Data Test\" algorithmParamsSetting=\"onlyOnTargetVar=" << argv[2] << "\"/>" << endl;
	outpXML << "</log>";
	
	outpXML.close();
	
}
