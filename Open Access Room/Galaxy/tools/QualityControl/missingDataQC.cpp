/*
 *   missingDataQC.cpp - Removes variables from the data cube that have a missing rate
 *   above the user defined threshold
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

	//MISSING DATA QUALITY CONTROL
	//Get the data		
	Datacube datacube;
	datacube.loadData(argv[1]);
	datacube.setVariablesMeaning(argv[5]);


	//Set the threshold
	float threshold;
	if(argv[2]!=NULL)
		threshold = atof(argv[2]);
	else
		threshold = 0.05;
    //initialize keep vector
	bool keep[datacube.values.at(0).size()];
	for(int i=0; i<datacube.values.at(0).size(); ++i)
		keep[i] = true;
	int missing, total;
	
	//Check which variables pass the missing data QC
	int countsCol = datacube.values.at(0).size() - 1;
	for(int column=0;column<countsCol;column++) 
	{
		missing = 0;
		total = 0;
		for(int row=0;row<datacube.values.size();row++)
		{	
            //count the missing values
			if(datacube.getMeaningOfValue(row,column).compare(MISSING_ORIG_VAL)==0)
				missing += datacube.values.at(row).at(countsCol);
			//count the total values
			total += datacube.values.at(row).at(countsCol);
		}
		//check the ratio of missing values
		if(double(missing)/double(total) > threshold)
			keep[column] = false;
		else
			keep[column] = true;
	}


	//If the target variable (last variable in data cube)
	//is removed exit and inform the users
	if(keep[countsCol-1] == false)
	{
		cerr << "\nThe target variable did not pass the missing data test!" << endl << "Process Terminated." << endl;
    	exit(-1);		
	}
	
	//Remove the variables that did not pass the missing data QC
    datacube.removeVariables(keep);
    //output the data cube
	datacube.writeToFile(argv[3]);
	
	//Output the XML contents describing the algorithm
	ofstream outpXML;
	outpXML.open(argv[4]);
	
	outpXML << "<log>" << endl;
	outpXML << "<QualityControl algorithm_name=\"Missing Data Test\" algorithmParamsSetting=\"threshold=" << argv[2] << "\"/>" << endl;
	outpXML << "</log>";
	
	outpXML.close();
	
}
